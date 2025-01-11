package dev.osunolimits.routes.get.simple;

import dev.osunolimits.main.App;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.modules.utils.SEOBuilder;
import spark.Request;
import spark.Response;

public class Login extends Shiina {


    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 0);

        shiina.data.put("seo", new SEOBuilder("Login", App.customization.get("homeDescription").toString()));
        return renderTemplate("login.html", shiina, res, req);
    }
    
}
