package dev.osunolimits.routes.redirects;

import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import spark.Request;
import spark.Response;

public class GucchoBmRedirect extends Shiina{
    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);

        String set = req.params("set");
        if(set == null) {
            return notFound(res, shiina);
        }

        String id = req.queryParams("beatmap");
        if(id == null) {
            return notFound(res, shiina);
        }

        res.redirect("/b/"+id);
        return notFound(res, shiina);
    }
}
