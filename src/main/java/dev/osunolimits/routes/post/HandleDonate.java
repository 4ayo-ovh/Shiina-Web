package dev.osunolimits.routes.post;

import dev.osunolimits.main.App;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.monetization.StripeMethod;
import spark.Request;
import spark.Response;

public class HandleDonate extends Shiina {
    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
    
        if (!shiina.loggedIn) {
            return redirect(res, shiina, "/login?path=donate");
        }

        String donationType = req.queryParams("donation-type");
        if(donationType == null || donationType.isEmpty()) {
            return redirect(res, shiina, "/donate?error=Invalid donation type");
        }
    
        String username;
        if(donationType.equals("self")) {
            username = shiina.user.name;
        } else if(donationType.equals("gift")) {
            username = req.queryParams("player-name");
            if(username == null || username.isEmpty()) {
                return redirect(res, shiina, "/donate?error=Invalid username");
            }
        } else {
            return redirect(res, shiina, "/");
        }
    
        int duration = Integer.parseInt(req.queryParams("duration"));
        if(duration < 1 || duration > 12) {
            return redirect(res, shiina, "/donate?error=Invalid duration");
        }
        
        double basePrice = 5.00; // Base price is now 5.00 EUR
        double discountPerMonth = 0.025; // 2.5% discount per month
    
        // Calculate total price without discount
        double totalPriceWithoutDiscount = basePrice * duration;
    
        // Calculate discount in euros
        double totalDiscount = 0;
        if (duration > 1) {
            totalDiscount = totalPriceWithoutDiscount - (basePrice * (1 - (discountPerMonth * (duration - 1))) * duration);
        }
    
        // Calculate price with discount
        double priceAfterDiscount = totalPriceWithoutDiscount;
        if (duration > 1) {
            priceAfterDiscount = basePrice * (1 - (discountPerMonth * (duration - 1))) * duration;
        }
    
        // Round the total price to the nearest 0.50 EUR increment
        double roundedPrice = Math.round(priceAfterDiscount * 2) / 2.0;
    
        // Convert the price to cents for Stripe
        long priceInCents = (long) (roundedPrice * 100);
        App.log.info("Price: " + priceInCents + " cents (" + roundedPrice + " EUR)");
    
        String checkoutUrl = StripeMethod.createCheckoutSession(priceInCents, "EUR", username, "ONL Supporter", duration);
    
        return redirect(res, shiina, checkoutUrl);
    }
}