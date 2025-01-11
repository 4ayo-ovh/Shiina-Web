package dev.osunolimits.modules.utils;

import dev.osunolimits.common.Database;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import spark.Request;
import spark.Response;
import spark.Route;

public class MySQLRoute implements Route {


    public ShiinaRequest getRequest() {
        ShiinaRequest shiina = new ShiinaRoute(). new ShiinaRequest();
        shiina.mysql = Database.getConnection();
        return shiina;
    }

    public String returnResponse(ShiinaRequest shiina, String response) {
        shiina.mysql.close();
        return response;
    }

    public String notFound(Response res, ShiinaRequest shiina) {
        res.status(404);
        shiina.mysql.close();
        return null;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }
    
}
