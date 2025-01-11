package dev.osunolimits.routes.ap.get.users;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import dev.osunolimits.main.App;
import dev.osunolimits.models.AuditLogEntry;
import dev.osunolimits.models.Group;
import dev.osunolimits.models.UserInfoObject;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.utils.Validation;
import dev.osunolimits.utils.osu.OsuConverter;
import dev.osunolimits.utils.osu.PermissionHelper;
import spark.Request;
import spark.Response;

public class ApUser extends Shiina {

    private Gson gson;

    public ApUser() {
        gson = new Gson();
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 14);

        if (!shiina.loggedIn) {
            return redirect(res, shiina, "/login");
        }

        if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.MODERATOR)) {
            return redirect(res, shiina, "/");
        }

        String userId = req.queryParams("id");
        if(userId == null || !Validation.isNumeric(userId))  {
            return redirect(res, shiina, "/ap/users");
        }

        ResultSet user = shiina.mysql.Query("SELECT `users`.`id`, `users`.`priv`, `users`.`email`, `users`.`country`, `users`.`silence_end`, `users`.`donor_end`, `users`.`creation_time`, `users`.`latest_activity`, `users`.`clan_id`, `clans`.`name` AS `clan_name`, `clans`.`tag` AS `clan_tag`, `users`.`clan_priv` FROM users LEFT JOIN `clans` ON `users`.`clan_id` = `clans`.`id` WHERE `users`.`id` = ?;", userId);
        if(!user.next()) {
            return redirect(res, shiina, "/ap/users");
        }

        ResultSet standingResult = shiina.mysql.Query("SELECT `sh_audit`.`id`,  `sh_audit`.`action`,  `sh_audit`.`user_id`, `u1`.`name` AS `user_name`,  `sh_audit`.`target_id`, `u2`.`name` AS `target_name`,  `sh_audit`.`reason`,  `sh_audit`.`mode`  FROM `sh_audit` LEFT JOIN `users` `u1` ON `user_id` = `u1`.`id` LEFT JOIN `users` `u2` ON `target_id` = `u2`.`id` WHERE (`action` = 'RESTRICT' OR `action` = 'UNRESTRICT' OR `action` = 'WIPE' OR `action` = 'RMPB') AND `target_id` = ?;", userId);
        List<AuditLogEntry> standing = new ArrayList<>();
        while(standingResult.next()) {
            AuditLogEntry entry = new AuditLogEntry();
            entry.setId(standingResult.getInt("id"));
            entry.setAction(standingResult.getString("action"));
            entry.setUserId(standingResult.getInt("user_id"));
            entry.setUserName(standingResult.getString("user_name"));
            entry.setTargetId(standingResult.getInt("target_id"));
            entry.setTargetName(standingResult.getString("target_name"));
            if((standingResult.getString("mode") != null)) {
                entry.setMode(standingResult.getInt("mode"));
            }
            
            entry.setReason(standingResult.getString("reason"));
            standing.add(entry);

        }
        shiina.data.put("standings", standing);
        UserInfoObject userInfo = gson.fromJson(App.jedisPool.get("shiina:user:" + userId), UserInfoObject.class);
        if(userInfo == null) {
            return redirect(res, shiina, "/ap/users");
        }

        ResultSet groupResult = shiina.mysql.Query("SELECT * FROM `sh_groups`");
        List<Group> groups = new ArrayList<>();
        while(groupResult.next()) {
            Group g = new Group();
            g.id = groupResult.getInt("id");
            g.name = groupResult.getString("name");
            g.emoji = groupResult.getString("emoji");
            groups.add(g);
        }

        shiina.data.put("allGroups", groups);
        shiina.data.put("id", userId);
        shiina.data.put("aname", userInfo.name);
        shiina.data.put("priv", PermissionHelper.Privileges.fromInt(user.getInt("priv")));
        shiina.data.put("privLevel", user.getInt("priv"));
        shiina.data.put("safe_name", userInfo.safe_name);
        shiina.data.put("groups", userInfo.groups);
        shiina.data.put("name", user.getString("email"));
        shiina.data.put("country", user.getString("country"));
        shiina.data.put("allModes", OsuConverter.modeArray);
        shiina.data.put("latest_activity", user.getString("latest_activity"));
        shiina.data.put("creation_time", user.getString("creation_time"));
        shiina.data.put("silence_end", user.getString("silence_end"));
        shiina.data.put("donor_end", user.getString("donor_end"));

        shiina.data.put("clan_id", user.getString("clan_id"));
        shiina.data.put("clan_name", user.getString("clan_name"));
        shiina.data.put("clan_tag", user.getString("clan_tag"));
        shiina.data.put("clan_priv", user.getString("clan_priv"));
        return renderTemplate("ap/users/user.html", shiina, res, req);
    }

}