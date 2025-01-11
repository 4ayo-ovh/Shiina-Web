package dev.osunolimits.routes.ap.get;

import dev.osunolimits.api.BanchoStats;
import dev.osunolimits.api.OnlineQuery;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.utils.osu.PermissionHelper;
import spark.Request;
import spark.Response;

public class Bancho extends Shiina {


    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 16);

        if(!shiina.loggedIn) {
            res.redirect("/login");
            return notFound(res, shiina);
        }

        if(!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.MODERATOR)) {
            res.redirect("/");
            return notFound(res, shiina);
        }

        BanchoStats stats = new BanchoStats();
        shiina.data.put("stats", stats.getPlayerCount());
        
        OnlineQuery onlineQuery = new OnlineQuery();
        shiina.data.put("online", onlineQuery.getOnline());
        
        return renderTemplate("ap/bancho.html", shiina, res, req);
    }
    
}
