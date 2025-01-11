package dev.osunolimits.routes.post;

import java.sql.ResultSet;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;
import org.owasp.encoder.Encode;

import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import spark.Request;
import spark.Response;

public class HandleUserpageChange extends Shiina {

    @Override
    public Object handle(Request req, Response res) throws Exception {
       ShiinaRequest shiina = new ShiinaRoute().handle(req, res);

        if (!shiina.loggedIn) {
            // TODO: impl customization redirect on login
            return redirect(res, shiina, "/login?path=customization");
        }
        int userId = shiina.user.id;
        String userpage = req.queryParams("userpage");

        if(userpage == null || userpage.isEmpty()) {
            return redirect(res, shiina, "/settings?error=Userpage cannot be empty");
        }

        String raw = Encode.forHtmlContent(userpage);
        BBProcessorFactory processor = BBProcessorFactory.getInstance();
        TextProcessor bb = processor.create();
    
        userpage = bb.process(raw);

        ResultSet userpageRs = shiina.mysql.Query("SELECT * FROM `userpages` WHERE `user_id` = ?", userId);
        if(userpageRs.next()) {
            shiina.mysql.Exec("UPDATE `userpages` SET `html` = ?, `raw` = ?, `raw_type` = ? WHERE `user_id` = ?", userpage, raw, "tiptap", userId);
        }else {
            shiina.mysql.Exec("INSERT INTO `userpages` (`user_id`, `html`, `raw`, `raw_type`) VALUES (?, ?, ?, ?)", userId, userpage, raw, "tiptap");
        }

        return redirect(res, shiina, "/settings?info=Userpage was changed");
    }
}
