package dev.osunolimits.routes.ap.get;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.utils.osu.PermissionHelper;
import lombok.Data;
import spark.Request;
import spark.Response;

public class Multiaccounts extends Shiina {

    private final String MULTI_SQL = "WITH UserPairs AS (SELECT c1.userid AS user1, c2.userid AS user2, c1.uninstall_id, c1.disk_serial, c1.adapters, c1.latest_time FROM client_hashes c1 JOIN client_hashes c2 ON c1.uninstall_id = c2.uninstall_id AND c1.userid != c2.userid) SELECT up.user1, up.user2, u1.name AS user1_name, u2.name AS user2_name, CASE WHEN MAX(uninstall_id) = MIN(uninstall_id) THEN 1 ELSE 0 END AS same_uninstall_id, CASE WHEN MAX(disk_serial) = MIN(disk_serial) THEN 1 ELSE 0 END AS same_disk_serial, CASE WHEN MAX(adapters) = MIN(adapters) THEN 1 ELSE 0 END AS same_adapters, MAX(latest_time) AS latest_time FROM UserPairs up JOIN users u1 ON up.user1 = u1.id JOIN users u2 ON up.user2 = u2.id GROUP BY up.user1, up.user2, u1.name, u2.name HAVING NOT (same_uninstall_id = 0 AND same_disk_serial = 0 AND same_adapters = 0) ORDER BY latest_time DESC LIMIT ? OFFSET ?;";

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 11);

        if(!shiina.loggedIn) {
            res.redirect("/login");
            return notFound(res, shiina);
        }

        if(!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.MODERATOR)) {
            res.redirect("/");
            return notFound(res, shiina);
        }
        int page = 0;
        if(req.queryParams("page") != null) {
            page = Integer.parseInt(req.queryParams("page"));
        }

        int offset = page * 10;

        ResultSet statsResultSet = shiina.mysql.Query(MULTI_SQL, 11, offset);
        List<MultiAccountDetection> multiAccountDetections = new ArrayList<>();
        boolean hasNextPage = false;
        while (statsResultSet.next()) {
            MultiAccountDetection multiAccountDetection = new MultiAccountDetection();
            multiAccountDetection.setUser1(statsResultSet.getInt("user1"));
            multiAccountDetection.setUser2(statsResultSet.getInt("user2"));
            multiAccountDetection.setUser1_name(statsResultSet.getString("user1_name"));
            multiAccountDetection.setUser2_name(statsResultSet.getString("user2_name"));
            multiAccountDetection.setSame_uninstall_id(statsResultSet.getBoolean("same_uninstall_id"));
            multiAccountDetection.setSame_disk_serial(statsResultSet.getBoolean("same_disk_serial"));
            multiAccountDetection.setSame_adapters(statsResultSet.getBoolean("same_adapters"));
            multiAccountDetection.setLatest_time(statsResultSet.getString("latest_time"));

            if(multiAccountDetections.size() == 10) {
                hasNextPage = true;
                continue;
            }
            multiAccountDetections.add(multiAccountDetection);
           
        }
        shiina.data.put("multiAccountDetections", multiAccountDetections);
        shiina.data.put("hasNextPage", hasNextPage);
        shiina.data.put("page", page);
        return renderTemplate("ap/multiaccs.html", shiina, res, req);
    }

    @Data
    public class MultiAccountDetection {
        private int user1;
        private int user2;
        private String user1_name;
        private String user2_name;
        private boolean same_uninstall_id;
        private boolean same_disk_serial;
        private boolean same_adapters;
        private String latest_time;
    }
    
}
