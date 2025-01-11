package dev.osunolimits.routes.get.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.osunolimits.common.Database;
import dev.osunolimits.common.MySQL;
import dev.osunolimits.models.Invoice;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import spark.Request;
import spark.Response;

public class Invoices extends Shiina {

    public static ConcurrentLinkedQueue<Invoice> invoices = new ConcurrentLinkedQueue<>();

    public static void populate() {
        MySQL mysql = Database.getConnection();
        try {
            ResultSet result = mysql.Query("SELECT * FROM `sh_payments`");
            try {
                while (result.next()) {
                    Invoice invoice = new Invoice();
                    invoice.setUser_id(result.getInt("user_id"));
                    invoice.setMonths(result.getInt("months"));
                    invoice.setTotal(result.getDouble("total"));
                    invoice.setPayment_id(result.getString("payment_id"));
                    invoices.add(invoice);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally {
            mysql.close();
        }
    }

    public static void populate(Invoice invoice) {
        invoices.add(invoice);
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 0);

        return renderTemplate("leaderboard.html", shiina, res, req);
    }

}
