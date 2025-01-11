package dev.osunolimits.routes.post;

import java.util.Locale;

import com.google.gson.Gson;

import dev.osunolimits.main.App;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.routes.ap.api.PubSubModels;
import dev.osunolimits.routes.ap.api.PubSubModels.CountryChangeInput;
import dev.osunolimits.utils.osu.PermissionHelper;
import spark.Request;
import spark.Response;

public class HandleFlagChange extends Shiina {
    private final Gson GSON;
    public HandleFlagChange() {
        this.GSON = new Gson();
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
       ShiinaRequest shiina = new ShiinaRoute().handle(req, res);

        if (!shiina.loggedIn) {
            // TODO: impl customization redirect on login
            return redirect(res, shiina, "/login?path=customization");
        }

        if(!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.SUPPORTER)) {
            return redirect(res, shiina, "/settings?error=You do not have permission to do this");
        }

        int userId = shiina.user.id;
        String flag = req.queryParams("newCountry");

        if(flag == null || flag.isEmpty()) {
            return redirect(res, shiina, "/settings?error=Invalid flag");
        }

        String[] countryCodes = Locale.getISOCountries();
        boolean valid = false;
        for (String countryCode : countryCodes) {
            if(countryCode.equalsIgnoreCase(flag)) {
                valid = true;
                break;
            }
        }

        if(!valid) {
            return redirect(res, shiina, "/settings?error=Invalid flag");
        }

        CountryChangeInput input = new PubSubModels().new CountryChangeInput();
        input.id = userId;
        input.country = flag.toLowerCase();
        App.jedisPool.publish("country_change", GSON.toJson(input));

        return redirect(res, shiina, "/settings?info=Flag was changed, it may take a few minutes to update fully");
    }
}
