package dev.osunolimits.common;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import dev.osunolimits.main.App;
import okhttp3.Request;

public class APIRequest {
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(APIRequest.class);

    public static Request build(String url) {
        if (Boolean.parseBoolean(App.loggerEnv.get("API_LOG")))
            LOG.info("| Bancho.py | API Request | " + App.env.get("APIURL") + url);
        return new Request.Builder()
                .url(App.env.get("APIURL") + url)
                .build();
    }
}
