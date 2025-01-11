package dev.osunolimits.routes.api.get;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.google.gson.Gson;

import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.modules.utils.MySQLRoute;
import dev.osunolimits.utils.Validation;
import dev.osunolimits.utils.osu.OsuConverter;
import lombok.Data;
import spark.Request;
import spark.Response;

public class GetFirstPlaces extends MySQLRoute {

    private final String GET_FIRST_PLACES = "SELECT s.id AS score_id, s.userid, s.map_md5, m.id AS map_id, m.set_id AS map_set_id, m.filename AS map_name, s.score AS max_score, s.pp, s.acc, s.mods, s.grade, s.play_time " +
                                            "FROM scores s " +
                                            "JOIN (SELECT map_md5, MAX(score) AS max_score FROM scores WHERE status = 2 GROUP BY map_md5) AS max_scores " +
                                            "ON s.map_md5 = max_scores.map_md5 AND s.score = max_scores.max_score " +
                                            "JOIN maps m ON s.map_md5 = m.md5 " +
                                            "WHERE s.userid = ? AND s.status = 2 AND s.mode = ? " +
                                            "ORDER BY s.play_time DESC LIMIT ? OFFSET ?;";

    private final String COUNT_FIRST_PLACES = "SELECT COUNT(DISTINCT s.map_md5) AS first_place_count " +
                                              "FROM scores s " +
                                              "JOIN (SELECT map_md5, MAX(score) AS max_score FROM scores WHERE status = 2 GROUP BY map_md5) AS max_scores " +
                                              "ON s.map_md5 = max_scores.map_md5 AND s.score = max_scores.max_score " +
                                              "WHERE s.userid = ? AND s.status = 2 AND s.mode = ?;";

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = getRequest();

        int mode = 0;
        if (req.queryParams("mode") != null && Validation.isNumeric(req.queryParams("mode"))) {
            mode = Integer.parseInt(req.queryParams("mode"));
        }

        Integer id = null;
        if (req.queryParams("id") != null && Validation.isNumeric(req.queryParams("id"))) {
            id = Integer.parseInt(req.queryParams("id"));
        }

        Integer offset = 0;
        if (req.queryParams("offset") != null && Validation.isNumeric(req.queryParams("offset"))) {
            offset = Integer.parseInt(req.queryParams("offset"));
        }

        if (id == null) {
            shiina.mysql.close();
            return notFound(res, shiina);
        }

        // Fetch first places
        ArrayList<FirstPlace> firstPlaces = new ArrayList<>();
        FirstPlacesResponse response = new FirstPlacesResponse();
        boolean hasNextPage = false;

        // Fetch first places based on the query
        ResultSet firstPlacesQuery = shiina.mysql.Query(GET_FIRST_PLACES, id, mode, 6, offset);
        while (firstPlacesQuery.next()) {
            FirstPlace firstPlace = new FirstPlace();
            firstPlace.setScore_id(firstPlacesQuery.getInt("score_id"));
            firstPlace.setUser_id(firstPlacesQuery.getInt("userid"));
            firstPlace.setMap_md5(firstPlacesQuery.getString("map_md5"));
            firstPlace.setMap_id(firstPlacesQuery.getInt("map_id"));
            firstPlace.setMap_set_id(firstPlacesQuery.getInt("map_set_id"));
            firstPlace.setMap_name(firstPlacesQuery.getString("map_name"));
            firstPlace.setMax_score(firstPlacesQuery.getInt("max_score"));
            firstPlace.setPp(firstPlacesQuery.getInt("pp"));
            firstPlace.setAcc(firstPlacesQuery.getInt("acc"));
            firstPlace.setMods(OsuConverter.convertMods(firstPlacesQuery.getInt("mods")));  // Added mods
            firstPlace.setGrade(firstPlacesQuery.getString("grade"));
            firstPlace.setPlay_time(firstPlacesQuery.getString("play_time"));
            firstPlaces.add(firstPlace);
        }

        // Check if there is a next page
        if (firstPlaces.size() == 6) {
            hasNextPage = true;
            firstPlaces.remove(5); // Remove the extra entry
        }

        // Fetch count of first places based on mode
        ResultSet countQuery = shiina.mysql.Query(COUNT_FIRST_PLACES, id, mode);
        if (countQuery.next()) {
            response.setCount(countQuery.getInt("first_place_count"));
        }

        response.setFirstPlaces(firstPlaces.toArray(new FirstPlace[0]));
        response.setStatus("success");
        response.setHasNextPage(hasNextPage);

        res.type("application/json");
        Gson gson = new Gson();
        shiina.mysql.close();
        return gson.toJson(response);
    }

    @Data
    public class FirstPlacesResponse {
        private String status;
        private boolean hasNextPage;
        private int count;
        private FirstPlace[] firstPlaces;
    }

    @Data
    public class FirstPlace {
        private int score_id;      // Score ID
        private int user_id;
        private String map_md5;
        private int map_id;
        private int map_set_id;    // Map set ID
        private String map_name;   // Map name
        private int max_score;
        private int pp;
        private int acc;
        private String[] mods;       // Added mods field
        private String grade;
        private String play_time;
    }
}
