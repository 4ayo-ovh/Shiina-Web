package dev.osunolimits.plugins;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.qos.logback.classic.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;

public class PluginConfig {

    private Logger log = (Logger) LoggerFactory.getLogger(PluginLoader.class);

    private String pluginName;
    private Class<?> configObject;
    private Object config;

    public PluginConfig(String pluginName, Class<?> configObject) {
        this.pluginName = pluginName;
        this.configObject = configObject;
    }

    public Object getConfig() {
        return config;
    }

    public void loadConfig() {
        if (configObject == null) {
            log.error("[" + pluginName + "] No config object found for plugin.");
            return;
        }

        File pluginDir = new File("plugins/" + pluginName);

        if (!pluginDir.exists()) {
            log.info("[" + pluginName + "] Plugin directory not found, creating directory...");
            if (pluginDir.mkdirs()) {
                log.info("[" + pluginName + "] Plugin directory created successfully.");
            } else {
                log.error("[" + pluginName + "] Failed to create plugin directory.");
                return;
            }
        }

        File configFile = new File(pluginDir, "config.json");
        if (!configFile.exists()) {
            log.info("[" + pluginName + "] Config file not found, creating default config...");

            Gson gson = new Gson();
            String defaultConfig;
            try {
                defaultConfig = gson.toJson(configObject.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                log.error("[" + pluginName + "] Failed to create default config.", e);
                return;
            }
            if (defaultConfig == null) {
                log.error("[" + pluginName + "] Failed to create default config.");
                return;
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(defaultConfig);

            } catch (IOException e) {
                log.error("[" + pluginName + "] Failed to create default config.");
                return;
            }

        } else {
            log.info("[" + pluginName + "] Config file already exists.");
        }

        try {
            config = new Gson().fromJson(Files.readString(configFile.toPath()), configObject);
        } catch (Exception e) {
            log.error("[" + pluginName + "] Failed to load config.", e);
        }

    }


}
