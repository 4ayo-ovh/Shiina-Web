package dev.osunolimits.routes.get.simple;

import dev.osunolimits.main.App;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.modules.utils.SEOBuilder;
import dev.osunolimits.monetization.MonetizationConfig;
import spark.Request;
import spark.Response;

public class Donate extends Shiina {
    public MonetizationConfig monetizationConfig;
    public Donate(MonetizationConfig monetizationConfig) {
        this.monetizationConfig = monetizationConfig;
    }


    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 15);

        if(!monetizationConfig.ENABLED) {
            return redirect(res, shiina, "/");
        }

        if(!shiina.loggedIn) {
            return redirect(res, shiina, "/login");
        }
       
        shiina.data.put("seo", new SEOBuilder("Donate", App.customization.get("homeDescription").toString()));
        return renderTemplate("donate.html", shiina, res, req);
    }
    
}
