package dev.osunolimits.routes.get;

import java.util.Optional;

import dev.osunolimits.api.BeatmapQuery;
import dev.osunolimits.api.BeatmapQuery.BeatmapResponse;
import dev.osunolimits.main.App;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.modules.utils.SEOBuilder;
import dev.osunolimits.utils.Validation;
import spark.Request;
import spark.Response;

public class Beatmaps extends Shiina {

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 3);
        BeatmapQuery beatmapQuery = new BeatmapQuery();
        int page = 1;

        if (req.queryParams("page") != null && Validation.isNumeric(req.queryParams("page"))) {
            page = Integer.parseInt(req.queryParams("page"));
        }

        int status = 999;
        if (req.queryParams("status") != null && Validation.isNumeric(req.queryParams("status"))) {
            status = Integer.parseInt(req.queryParams("status"));
        }

        Optional<String> artist = Optional.empty();
        if (req.queryParams("artist") != null) {
            artist = Optional.of(req.queryParams("artist"));
        }

        Optional<String> creator = Optional.empty();
        if (req.queryParams("creator") != null) {
            creator = Optional.of(req.queryParams("creator"));
        }
        int mode = 0;
        if (req.queryParams("mode") != null && Validation.isNumeric(req.queryParams("mode"))) {
            mode = Integer.parseInt(req.queryParams("mode"));
        }

        BeatmapResponse beatmapResponse = beatmapQuery.getBeatmaps(page, 100, status, artist, creator, mode);
        shiina.data.put("beatmaps", beatmapResponse.getData());
        shiina.data.put("mode", mode);
        shiina.data.put("status", status);
        if (creator.isPresent()) {
            shiina.data.put("creator", creator.get());
        }
        if (artist.isPresent()) {
            shiina.data.put("artist", artist.get());
        }
        SEOBuilder seo = new SEOBuilder("Beatmap Listing", App.customization.get("homeDescription").toString());
        shiina.data.put("seo", seo);
        shiina.data.put("entries", beatmapResponse.getMeta().getTotal());
        shiina.data.put("page", beatmapResponse.getMeta().getPage());
        return renderTemplate("beatmaps.html", shiina, res, req);
    }

}
