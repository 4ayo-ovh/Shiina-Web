package dev.osunolimits.routes.get;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;

import dev.osunolimits.api.UserQuery;
import dev.osunolimits.api.UserStatusQuery;
import dev.osunolimits.main.App;
import dev.osunolimits.models.FullUser;
import dev.osunolimits.models.UserInfoObject;
import dev.osunolimits.models.UserStatus;
import dev.osunolimits.models.FullUser.Player;
import dev.osunolimits.models.Group;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.modules.utils.SEOBuilder;
import dev.osunolimits.utils.Validation;
import dev.osunolimits.utils.osu.LevelCalculator;
import dev.osunolimits.utils.osu.OsuConverter;
import dev.osunolimits.utils.osu.PermissionHelper;
import lombok.Data;
import spark.Request;
import spark.Response;

public class User extends Shiina {

    public User() {
        gson = new Gson();
    }

    private final Gson gson;
    private final String ACH_QUERY = "SELECT `achievements`.`file`, `achievements`.`name`, `achievements`.`desc` FROM `user_achievements` LEFT JOIN `achievements` ON `user_achievements`.`achid` = `achievements`.`id` WHERE `user_achievements`.`userid` = ? AND (`achievements`.`file` LIKE ? OR `achievements`.`file` LIKE 'all%');";

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 0);

        Integer id = null;
        if (req.params("id") != null && Validation.isNumeric(req.params("id"))) {
            id = Integer.parseInt(req.params("id"));
        }

        int mode = 0;
        if (req.queryParams("mode") != null && Validation.isNumeric(req.queryParams("mode"))) {
            mode = Integer.parseInt(req.queryParams("mode"));
        }

        if (id == null) {
            return notFound(res, shiina);
        }

        FullUser user = new UserQuery().getUser(id);
        if (user == null) {
            return notFound(res, shiina);
        }

        Player player = user.getPlayer();

        if (player == null)
            return notFound(res, shiina);

        EnumSet<PermissionHelper.Privileges> playerPrivileges = PermissionHelper.Privileges
                .fromInt(player.getInfo().getPriv());

        boolean isRestricted = !playerPrivileges.contains(PermissionHelper.Privileges.UNRESTRICTED);

        shiina.data.put("restricted", isRestricted);

        if (isRestricted) {
            if (shiina.user == null) {
                return notFound(res, shiina);
            }

            if (String.valueOf(shiina.user.id).equals(String.valueOf(id))) {
                shiina.data.put("self", true);
            } else {
                if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.ADMINISTRATOR)) {
                    return notFound(res, shiina);

                } else {
                    shiina.data.put("self", false);
                }
            }
        } else {
            shiina.data.put("restricted", false);
            if (shiina.user != null) {
                if (String.valueOf(shiina.user.id).equals(String.valueOf(id))) {
                    shiina.data.put("self", true);
                } else {
                    shiina.data.put("self", false);
                }
            } else {
                shiina.data.put("self", false);
            }

        }

        UserInfoObject userInfo = gson.fromJson(App.jedisPool.get("shiina:user:" + id), UserInfoObject.class);

        UserStatusQuery userStatusQuery = new UserStatusQuery();
        UserStatus userStatus = userStatusQuery.getUserStatus(id);

        LinkedHashMap<String, Integer> userStats = new LinkedHashMap<>();
        ResultSet playCountGraphRs = shiina.mysql.Query(
                "WITH RECURSIVE months AS ( SELECT 1 AS month UNION ALL SELECT month + 1 FROM months WHERE month < 12 ) SELECT MONTHNAME(DATE(CONCAT(YEAR(CURDATE()), '-', m.month, '-01'))) AS month_name, COALESCE(COUNT(s.play_time), 0) AS play_count FROM months m LEFT JOIN scores s ON MONTH(s.play_time) = m.month AND YEAR(s.play_time) = YEAR(CURDATE()) AND s.userid = ? AND s.mode = ? WHERE DATE(CONCAT(YEAR(CURDATE()), '-', m.month, '-01')) <= CURDATE() GROUP BY m.month ORDER BY m.month;",
                id, mode);
        while (playCountGraphRs.next()) {
            userStats.put(playCountGraphRs.getString("month_name"), playCountGraphRs.getInt("play_count"));
        }

        ResultSet userpageRs = shiina.mysql.Query("SELECT * FROM `userpages` WHERE `user_id` = ?", id);
        if(userpageRs.next()) {
            shiina.data.put("userpage", userpageRs.getString("html"));
        }

        ResultSet achRs = shiina.mysql.Query(ACH_QUERY, id,
                OsuConverter.convertModeBackNoRx(String.valueOf(mode)).toLowerCase() + "%");
        ArrayList<Achievements> achievements = new ArrayList<>();
        while (achRs.next()) {
            Achievements ach = new Achievements();
            ach.setFile(achRs.getString("file"));
            ach.setName(achRs.getString("name"));
            ach.setDesc(achRs.getString("desc"));
            achievements.add(ach);
        }

        ResultSet followerRs = shiina.mysql
                .Query("SELECT COUNT(*) AS followers FROM relationships WHERE user2 = ? AND user1 != user2;", id);
        int follower = 0;
        if (followerRs.next()) {
            follower = followerRs.getInt("followers");
        }

        if (shiina.loggedIn) {

            ResultSet statusRs = shiina.mysql.Query(
                    "SELECT CASE WHEN EXISTS ( SELECT 1 FROM relationships r2 WHERE r2.user1 = r.user2 AND r2.user2 = r.user1 ) THEN 'mutual' WHEN r.user1 = ? THEN 'known' ELSE 'follower' END AS status, CASE WHEN r.user1 =? THEN r.user2 ELSE r.user1 END AS id, CASE WHEN r.user1 =? THEN u2.name ELSE u1.name END AS name, CASE WHEN r.user1 = ? THEN u2.latest_activity ELSE u1.latest_activity END AS latest_activity FROM relationships r LEFT JOIN `users` u1 ON r.user1 = u1.id LEFT JOIN `users` u2 ON r.user2 = u2.id WHERE (r.user1 = ? AND r.user2 = ?) OR (r.user1 = ? AND r.user2 = ?) LIMIT 1;",
                    shiina.user.id, shiina.user.id, shiina.user.id, shiina.user.id, shiina.user.id, id, shiina.user.id,
                    id);

            if (statusRs.next()) {
                shiina.data.put("relationship", statusRs.getString("status"));
            } else {
                shiina.data.put("relationship", "none");
            }
        }
        EnumSet<PermissionHelper.Privileges> userPriv = PermissionHelper.Privileges.fromInt(user.getPlayer().getInfo().getPriv());
        List<Group> groups = userInfo.getGroups();

        shiina.data.put("follower", follower);

        if(userPriv.contains(PermissionHelper.Privileges.SUPPORTER)) {
            groups.add(new Group("Supporter", "🌟", "Supporter"));
        }

        shiina.data.put("groups", userInfo.getGroups());
        shiina.data.put("achievements", achievements);
        
        shiina.data.put("uprivs", userPriv);
        Locale locale = Locale.of("", user.getPlayer().getInfo().getCountry().toUpperCase());
        if(!locale.getDisplayName().equals("XX")) {
            shiina.data.put("ucountry", locale.getDisplayCountry());
        } else {
            shiina.data.put("ucountry", "Unknown");
        }

        shiina.data.put("id", id);
        shiina.data.put("level",
                LevelCalculator.getLevelPrecise(user.getPlayer().getStats().get(String.valueOf(mode)).getTscore()));
        shiina.data.put("levelProgress", LevelCalculator
                .getPercentageToNextLevel(user.getPlayer().getStats().get(String.valueOf(mode)).getTscore()));
        shiina.data.put("playCountGraph", userStats);
        shiina.data.put("u", user);
        shiina.data.put("mode", mode);
        shiina.data.put("status", userStatus);
        SEOBuilder seo = new SEOBuilder("Profile of " + userInfo.getName(), App.customization.get("homeDescription").toString(), App.env.get("AVATARSRV") + "/" + id);
        shiina.data.put("seo", seo);
        return renderTemplate("user.html", shiina, res, req);
    }

    @Data
    public class Achievements {
        private String file;
        private String name;
        private String desc;
    }

}
