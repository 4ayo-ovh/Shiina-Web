package dev.osunolimits.routes.get.user;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.osunolimits.main.App;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.modules.utils.SEOBuilder;
import lombok.Data;
import spark.Request;
import spark.Response;

public class Settings extends Shiina {


    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 0);

        if(!shiina.loggedIn) {
            res.redirect("/login");
            return notFound(res, shiina);
        }

        if(req.queryParams("info") != null) {
            shiina.data.put("info", req.queryParams("info"));
        }

        if(req.queryParams("error") != null) {
            shiina.data.put("error", req.queryParams("error"));
        }   

        String[] countryCodes = Locale.getISOCountries();
        List<DataCountry> countries = new ArrayList<>();
        for (String countryCode : countryCodes) {
            Locale locale = Locale.of("", countryCode); 
            DataCountry country = new DataCountry();
            country.setCode(countryCode);
            country.setName(locale.getDisplayCountry());
            countries.add(country);
        }

        ResultSet countryRS = shiina.mysql.Query("SELECT `country`, `raw` FROM `users` LEFT JOIN `userpages` ON `users`.`id` = `userpages`.`user_id` WHERE `users`.`id` = ?;", shiina.user.id);
        if(countryRS.next()) {
            shiina.data.put("curCountry", countryRS.getString("country"));
            shiina.data.put("curUserpage", countryRS.getString("raw"));
        }
        
        shiina.data.put("countries", countries);
        shiina.data.put("seo", new SEOBuilder("Settings", App.customization.get("homeDescription").toString()));
        return renderTemplate("user/settings.html", shiina, res, req);
    }
    

    @Data
    public class DataCountry {
        private String code;
        private String name;
    }
}
