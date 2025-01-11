package dev.osunolimits.monetization;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import dev.osunolimits.common.Database;
import dev.osunolimits.common.MySQL;
import dev.osunolimits.main.App;
import spark.Spark;

public class StripeMethod {

    public static void registerWebhookRoute(MonetizationConfig config) {
        Spark.get("/c/success", (request, response) -> {
            response.redirect("/?payment=success");
            return null;
        });

        Spark.get("/c/cancel", (request, response) -> {
            response.redirect("/?payment=cancel");
            return null;
        });

        Spark.post("/c/process", (request, response) -> {
        
            String payload = request.body();
            String sigHeader = request.headers("Stripe-Signature");

            try {
                Event event = Webhook.constructEvent(payload, sigHeader, config.getStripeConfig().getWebhookSecret());

                if ("checkout.session.completed".equals(event.getType())) {
                    try (MySQL mysql = Database.getConnection()) {
                        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
                
                        String username = session.getMetadata().get("username");
                        int months = Integer.parseInt(session.getMetadata().get("months"));
                        int total = session.getAmountTotal().intValue();
                        String paymentId = session.getId();
                        String currency = session.getCurrency();
                        double totalDecimal = total / 100.0;
                
                        App.log.info("Payment received: " + totalDecimal + " " + currency + " for " + username);
                
                        try (ResultSet userRs = mysql.Query("SELECT `id` FROM `users` WHERE `name` = ?;", username)) {
                            if (!userRs.next()) {
                                App.log.error("User not found: " + username);
                                return "User not found";
                            }
                            String userId = userRs.getString("id");
                
                            mysql.Exec("INSERT INTO `sh_payments`(`user_id`, `months`, `total`, `payment_id`) VALUES (?,?,?,?);", 
                                       userId, months, String.valueOf(totalDecimal), paymentId);
                        }
                    } catch (Exception e) {
                        App.log.error("Error processing payment: ", e);
                    }
                }
                

                response.status(200);
                return "Webhook received successfully";
            } catch (Exception e) {
                response.status(400);
                return "Webhook error: " + e.getMessage();
            }
        });
    }

    public static String createCheckoutSession(long amount, String currency, String username, String product,
            int months) {
        List<SessionCreateParams.PaymentMethodType> paymentMethods = Arrays.asList(
                SessionCreateParams.PaymentMethodType.CARD,
                SessionCreateParams.PaymentMethodType.IDEAL,
                SessionCreateParams.PaymentMethodType.SEPA_DEBIT,
                SessionCreateParams.PaymentMethodType.BANCONTACT,
                SessionCreateParams.PaymentMethodType.SOFORT,
                SessionCreateParams.PaymentMethodType.GIROPAY,
                SessionCreateParams.PaymentMethodType.KLARNA);

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(App.env.get("DOMAIN") + "/c/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(App.env.get("DOMAIN") + "/c/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amount) 
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(product)
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("username", username).putMetadata("months", String.valueOf(months));

        for (SessionCreateParams.PaymentMethodType method : paymentMethods) {
            paramsBuilder.addPaymentMethodType(method);
        }

        SessionCreateParams params = paramsBuilder.build();
        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            App.log.error("Failed to create stripe checkout session", e);
            return null;
        }
        return session.getUrl();

    }
}
