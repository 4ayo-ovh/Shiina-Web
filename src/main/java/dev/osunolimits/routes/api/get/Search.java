package dev.osunolimits.routes.api.get;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.google.gson.Gson;

import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.modules.utils.MySQLRoute;
import dev.osunolimits.utils.Validation;
import lombok.Data;
import spark.Request;
import spark.Response;

public class Search extends MySQLRoute {

    public Search() {
        GSON = new Gson();
    }

    @Data
    public class SearchEntry {
        public int id;
        public String item_name;
        public int set_id;
        public String source_table;
    }


    private final Gson GSON;
    private final String SEARCH_QUERY = "SELECT id, name AS item_name, NULL AS set_id, 'users' AS source_table FROM users WHERE LOWER(name) LIKE CONCAT('%', LOWER(?), '%') UNION SELECT id, name AS item_name, NULL AS set_id, 'clans' AS source_table FROM clans WHERE LOWER(name) LIKE CONCAT('%', LOWER(?), '%') UNION SELECT id, CONCAT(COALESCE(artist, ''), ' ', COALESCE(title, ''), ' ', COALESCE(version, '')) AS item_name, set_id, 'maps' AS source_table FROM maps WHERE LOWER(CONCAT(COALESCE(artist, ''), ' ', COALESCE(title, ''), ' ', COALESCE(version, ''))) LIKE CONCAT('%', LOWER(?), '%') ORDER BY CASE WHEN source_table = 'users' THEN 1 WHEN source_table = 'clans' THEN 2 WHEN source_table = 'maps' THEN 3 END, item_name LIMIT ? OFFSET ?;";

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = getRequest();

        String query = req.queryParams("query");
        if (query == null || (query.length() == 0)) {
            shiina.mysql.close();
            return notFound(res, shiina);
        }
        int page = 1;
        int offset = 0;
        int pageSize = 11;
        if (req.queryParams("page") != null && Validation.isNumeric(req.queryParams("page"))) { 
            page = Integer.parseInt(req.queryParams("page"));
        }

        if(page != 1) {
            offset = (page - 1) * pageSize;
        }
        


        ResultSet searchResult = shiina.mysql.Query(SEARCH_QUERY, query, query, query, pageSize, offset);
        ArrayList<SearchEntry> searchResults = new ArrayList<>();
        while(searchResult.next()) {

            if(searchResults.size() == 10) {
                break;
            }

            SearchEntry entry = new SearchEntry();
            entry.id = searchResult.getInt("id");
            entry.item_name = searchResult.getString("item_name");
            entry.set_id = searchResult.getInt("set_id");
            entry.source_table = searchResult.getString("source_table");
            searchResults.add(entry);
        }
        
        res.type("application/json");
        shiina.mysql.close();
        return GSON.toJson(searchResults);
    }
}
