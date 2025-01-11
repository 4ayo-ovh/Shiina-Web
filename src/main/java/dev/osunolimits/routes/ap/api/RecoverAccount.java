package dev.osunolimits.routes.ap.api;

import com.google.gson.Gson;

import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.utils.Auth;
import dev.osunolimits.utils.Validation;
import dev.osunolimits.utils.osu.PermissionHelper;
import lombok.Data;
import spark.Request;
import spark.Response;

public class RecoverAccount extends Shiina {
    private final Gson GSON;

    public RecoverAccount() {
        this.GSON = new Gson();
    }


    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);

        if(shiina.user == null) {
            return redirect(res, shiina, "/login");
        }
        

        if(!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.ADMINISTRATOR)) {
            return redirect(res, shiina, "/");
        }

        int userid = 0;
        if (req.queryParams("id") != null && Validation.isNumeric(req.queryParams("id"))) {
            userid = Integer.parseInt(req.queryParams("id"));
        }

        if(userid == 0) return redirect(res, shiina, "/ap/users");

        String token = Auth.generateNewToken();
        
        shiina.mysql.Exec("INSERT INTO `sh_recovery`(`token`, `user`) VALUES (?,?)", token, userid);
        
        res.status(200);
        shiina.mysql.close();

        RecoveryOutput output = new RecoveryOutput();
        output.url = "/auth/recover?token=" + token;

        return GSON.toJson(output);
    }

    @Data
    public class RecoveryOutput {
        public String url;
    }
}
