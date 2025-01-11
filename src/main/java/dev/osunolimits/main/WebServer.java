package dev.osunolimits.main;

import java.io.File;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import dev.osunolimits.modules.utils.ShiinaTemplateException;
import dev.osunolimits.utils.osu.OsuConverter;
import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import spark.Spark;

public class WebServer extends Spark {

    private final Logger LOG = (Logger) LoggerFactory.getLogger(WebServer.class);

    public static Configuration freemarkerCfg = new Configuration(Configuration.VERSION_2_3_23);

    public void setThreadPool(int minThreads, int maxThreads, int timeOutMillis) {
        threadPool(maxThreads, minThreads, timeOutMillis);
    }

    public void setTemplateUpdateDelay(int delay) {
        freemarkerCfg.setTemplateUpdateDelayMilliseconds(delay);
    }

    public void createDefaultDirectories() {
        if (new File("templates/").mkdirs()) {
            LOG.info("Created templates directory 'templates/'");
        }
        if (new File("static/").mkdirs()) {
            LOG.info("Created templates directory 'static/'");
        }
    }

    public void setDefaultDirectories() {
        Spark.staticFiles.externalLocation("static/");
    }

    public void ignite(String ip, int port, int updateDelay) {
        ipAddress(ip);
        port(port);
        freemarkerCfg.setWhitespaceStripping(true);
        freemarkerCfg.setTemplateExceptionHandler(new ShiinaTemplateException());
        freemarkerCfg.setLogTemplateExceptions(false);
        try {
            freemarkerCfg.setDirectoryForTemplateLoading(new File("templates/"));
        } catch (IOException e) {
            LOG.error("Failed to load templates directory", e);
        }
        freemarkerCfg.setTemplateUpdateDelayMilliseconds(updateDelay);
        if(updateDelay == 0) {
            freemarkerCfg.setCacheStorage(new NullCacheStorage());
        }
        try {
            freemarkerCfg.setSharedVariable("OsuConverter", OsuConverter.class);
        } catch (TemplateModelException e) {
            App.log.error("Error injecting OsuConverter into Freemarker");
        }
        after((req, res) -> {
            res.header("Server", "ShiinaONL");
            
            LOG.info(req.ip()  + " | " + req.requestMethod() + " (" + res.status() + ") | " + req.url() + " | " + req.userAgent() + " | " + req.headers("Referer"));
        });
        awaitInitialization();

        LOG.info("WebServer ignited on -> " +   ip + ":" + port + ")");
    }

}
