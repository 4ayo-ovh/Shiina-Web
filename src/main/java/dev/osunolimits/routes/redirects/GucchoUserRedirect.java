package dev.osunolimits.routes.redirects;

import java.sql.ResultSet;

import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import spark.Request;
import spark.Response;

public class GucchoUserRedirect extends Shiina{
    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);

        String handle = req.params("handle");
        if(handle == null) {
            return notFound(res, shiina);
        }

        if(handle.startsWith("@")) {
            handle = handle.substring(1);
        }

        ResultSet searchForId = shiina.mysql.Query("SELECT id FROM users WHERE safe_name = ?", handle);
        if(searchForId.next()) {
            res.redirect("/u/" + searchForId.getInt("id"));
        }
        return notFound(res, shiina);
    }
}
