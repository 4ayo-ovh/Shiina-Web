package dev.osunolimits.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.osunolimits.common.MySQL;
import lombok.Data;

public class CountryLeaderboardCache {

    @Data
    public class cacheObject {
        private final List<String> countries;
        private final long created;
    }

    private static final HashMap<Integer, cacheObject> cache = new HashMap<>();

    public static ArrayList<String> getOrPut(int mode, MySQL mysql) throws SQLException {
        if (cache.containsKey(mode)
                && CountryLeaderboardCache.cache.get(mode).created + 1000 > System.currentTimeMillis() / 1000) {
            if (cache.get(mode).countries == null) {
                return new ArrayList<>();
            } else {
                return new ArrayList<>(CountryLeaderboardCache.cache.get(mode).countries);
            }
        } else {
            List<String> countries = new ArrayList<>();
            ResultSet countriesResultSet = mysql.Query(
                    "SELECT COUNT(`id`) AS `people`, `country` FROM `users` WHERE `id` IN ( SELECT `userid` FROM `scores` WHERE `mode` = ? AND `scores`.`status`!= 0 ) GROUP BY `country` ORDER BY `people` DESC;",
                    String.valueOf(mode));
            while (countriesResultSet.next()) {
                countries.add(countriesResultSet.getString("country"));
            }
            CountryLeaderboardCache.cache.put(mode,
                    new CountryLeaderboardCache().new cacheObject(countries, System.currentTimeMillis() / 1000));
            return new ArrayList<>(countries);
        }

    }

}
