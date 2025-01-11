package dev.osunolimits.routes.ap.get.groups;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dev.osunolimits.models.Group;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.utils.osu.PermissionHelper;
import spark.Request;
import spark.Response;

public class Groups extends Shiina {


    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 13);

        if(!shiina.loggedIn) {
            res.redirect("/login");
            return notFound(res, shiina);
        }

        if(!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.ADMINISTRATOR)) {
            res.redirect("/");
            return notFound(res, shiina);
        }

        ResultSet groupResult = shiina.mysql.Query("SELECT * FROM sh_groups");
        List<Group> groups = new ArrayList<>();
        while(groupResult.next()) {
            Group group = new Group();
            group.setId(groupResult.getInt("id"));
            group.setName(groupResult.getString("name"));
            group.setEmoji(groupResult.getString("emoji"));
            group.setDesc(groupResult.getString("desc"));
            groups.add(group);
        }

        shiina.data.put("groups", groups);

        return renderTemplate("ap/groups/groups.html", shiina, res, req);
    }
    
}
