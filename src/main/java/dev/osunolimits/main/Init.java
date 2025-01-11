package dev.osunolimits.main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import dev.osunolimits.common.Database;
import dev.osunolimits.common.Database.ServerTimezone;
import dev.osunolimits.common.MySQL;
import dev.osunolimits.models.DbVersion;
import dev.osunolimits.utils.SQLFileLoader;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

public class Init {

    public final Logger log = (Logger) LoggerFactory.getLogger(Init.class);

    public void initializeWebServer(WebServer webServer) {
        try {
            webServer.setThreadPool(Integer.parseInt(App.env.get("MIN_THREADS")),
                    Integer.parseInt(App.env.get("MAX_THREAS")), Integer.parseInt(App.env.get("TIMEOUT_MS")));
        } catch (Exception e) {
            log.warn("Failed to set WebServer Thread Configuration");
            System.exit(1);
        }

        webServer.createDefaultDirectories();
        webServer.setDefaultDirectories();
        try {
            webServer.setTemplateUpdateDelay(Integer.parseInt(App.env.get("TEMPLATE_UPDATE_DELAY")));
        } catch (Exception e) {
            log.warn("Failed to set Template Update Delay (Caching)");
            System.exit(1);
        }

        try {
            webServer.ignite(App.env.get("HOST"), Integer.parseInt(App.env.get("PORT")), 3000);
        } catch (Exception e) {
            log.error("Failed to ignite WebServer", e);
            System.exit(1);
        }
    }

    public void initializeOkHttpCacheReset() {
        try {
            Files.walk(Paths.get(".cache/"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {

        }
    }

    public Map<String, Object> initializeCustomizations() {
        Yaml yaml = new Yaml();
        try {
            String yamlContent = Files.readString(Paths.get(".config/customization.yml"));
            Map<String, Object> obj = yaml.load(yamlContent);
            log.info("Loaded customization.yml");
            return obj;
        } catch (IOException e) {
            log.error("Failed to load customization.yml", e);
            System.exit(1);
            return null;
        }
    }

    public void initializeRedisConfiguration() {
        if (App.loggerEnv.get("HIKARI_LOG").equalsIgnoreCase("FALSE")) {
            log.info("Disabling HikariCP logging");
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger hikariLogger = loggerContext.getLogger("com.zaxxer.hikari");
            hikariLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        }
    }

    public void initializeJettyLogging() {
        if (App.loggerEnv.get("JETTY_LOG").equalsIgnoreCase("FALSE")) {
            log.info("Disabling Jetty logging");
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            // Disable logging for Jetty
            Logger jettyLogger = loggerContext.getLogger("org.eclipse.jetty");
            jettyLogger.setLevel(ch.qos.logback.classic.Level.OFF);

            // Optionally, disable specific Jetty components if needed
            Logger serverLogger = loggerContext.getLogger("org.eclipse.jetty.server");
            serverLogger.setLevel(ch.qos.logback.classic.Level.OFF);

            Logger handlerLogger = loggerContext.getLogger("org.eclipse.jetty.server.handler");
            handlerLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        }
    }

    public void initializeDatabase() {
        try {
            Database database = new Database();
            database.setOptimizedSettings();
            database.setMaximumPoolSize(Integer.parseInt(App.env.get("POOLSIZE")));
            database.setConnectionTimeout(Integer.parseInt(App.env.get("TIMEOUT")));
            database.connectToMySQL(App.env.get("DBHOST"), App.env.get("DBUSER"), App.env.get("DBPASS"),
                    App.env.get("DBNAME"),
                    ServerTimezone.UTC);
        } catch (Exception e) {
            log.error("Failed to configure Database", e);
            System.exit(1);
        }
    }

    public void initializeJedis() {
        try {
            HostAndPort hostAndPort = new HostAndPort(App.env.get("REDISHOST"),
                    Integer.parseInt(App.env.get("REDISPORT")));

            DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                    .connectionTimeoutMillis(Integer.parseInt(App.env.get("REDISTIMEOUT")))
                    .database(Integer.parseInt(App.env.get("REDISDB")))
                    .password(App.env.get("REDISPASS"))
                    .user(App.env.get("REDISUSER"))
                    .build();

            App.jedisPool = new JedisPooled(hostAndPort, clientConfig);

            log.info("Connected to Redis: " + App.jedisPool.ping());

        } catch (Exception e) {
            log.error("Failed to configure Jedis", e);
            System.exit(1);
        }
    }

    public void initializeAutorunSQL() {
        MySQL mysql = Database.getConnection();

        try {
           for (String s : new SQLFileLoader("autorun_sql/").loadSQLFiles()) {
                mysql.Exec(s);
           }
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to autorun sql files", e);
            System.exit(1);
        }
        mysql.close();
    }

    public void initializeConnectionWatcher() {
        // Thread t = new Thread(() -> {
        //     while (true) {
        //         try {
        //             Thread.sleep(1000 * 30);
        //         } catch (InterruptedException e) {
        //             log.error("ConnectionWatcher Thread interrupted", e);
        //             return;
        //         }
    
        //         for (MySQL con : Database.runningConnections) {
        //             if (System.currentTimeMillis() - con.connectionCreated > 1000 * 30) { // 30 seconds
        //                 con.close();
        //             }
        //         }
        //     }
        // });
        // t.setName("ConnectionWatcher (shiina)");
        // t.start();
    }    

    public void initializeDataDirectory() {
        try {
            if (!Files.exists(Paths.get("data"))) {
                Files.createDirectories(Paths.get("data"));
            }

            if (!Files.exists(Paths.get("data/dbversion.json"))) {
                
                Gson gson = new Gson();
                DbVersion dbVersion = new DbVersion();
                dbVersion.setVersion(App.version);
                dbVersion.setDbVersion(App.dbVersion);

                Files.writeString(Paths.get("data/dbversion.json"), gson.toJson(dbVersion));
            }
        } catch (IOException e) {
            log.error("Failed to create .data directory", e);
            System.exit(1);
        }


    }

}
