package dev.osunolimits.routes.get;

import java.util.List;

import dev.osunolimits.api.ClanQuery;
import dev.osunolimits.api.ClanQuery.ClanResponse;
import dev.osunolimits.main.App;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.modules.utils.SEOBuilder;
import dev.osunolimits.utils.Validation;
import dev.osunolimits.utils.osu.OsuConverter;
import spark.Request;
import spark.Response;

public class Clans extends Shiina {

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 4);

        ClanQuery clanQuery = new ClanQuery(shiina.mysql);
        int page = 1;
        boolean hasNext = false;
        int pageSize = 11;
        int offset = 0;
        if (req.queryParams("page") != null && Validation.isNumeric(req.queryParams("page"))) { 
            page = Integer.parseInt(req.queryParams("page"));
        }

        if(page != 1) {
            offset = (page - 1) * pageSize;
        }

        int mode = 0;
        if (req.queryParams("mode") != null && Validation.isNumeric(req.queryParams("mode"))) {
            mode = Integer.parseInt(req.queryParams("mode"));
        }

        String sort = "totalpp";
        if (req.queryParams("sort") != null && ClanQuery.CompetitionType.fromName(sort) != null) {
            sort = req.queryParams("sort");
        }
        

        List<ClanResponse> clans = clanQuery.getClan(ClanQuery.CompetitionType.fromName(sort), mode, pageSize, offset);
        if(clans.size() == pageSize) {
            hasNext = true;
            clans.remove(clans.size() - 1);
        }
        
        shiina.data.put("clans", clans);
        shiina.data.put("seo", new SEOBuilder("Clan Leaderboard | " + OsuConverter.convertModeBack(String.valueOf(mode)) + " | " + sort.toUpperCase(), App.customization.get("homeDescription").toString()));
        shiina.data.put("pageSize", clans.size());
        shiina.data.put("hasNext", hasNext);
        shiina.data.put("totalPageSize", pageSize);
        shiina.data.put("page", page);
        shiina.data.put("mode", mode);
        shiina.data.put("sort", sort);
        shiina.data.put("offset", offset + 1);



        return renderTemplate("clans.html", shiina, res, req);
    }
    
}
