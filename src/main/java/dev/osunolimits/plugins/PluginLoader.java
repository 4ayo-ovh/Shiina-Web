package dev.osunolimits.plugins;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

public class PluginLoader {

    private static final String PLUGINS_DIR = "plugins/";
    private static Logger log = (Logger) LoggerFactory.getLogger(PluginLoader.class);

    public void loadPlugins() {
        try {
            Path pluginsPath = Paths.get(PLUGINS_DIR);
            if (!Files.exists(pluginsPath)) {
                Files.createDirectories(pluginsPath);
                log.info("Created plugins directory: " + PLUGINS_DIR);
                return;
            }

            try (DirectoryStream<Path> jarFiles = Files.newDirectoryStream(pluginsPath, "*.jar")) {
                for (Path jarPath : jarFiles) {
                    loadPluginFromJar(jarPath.toString());
                }
            }
        } catch (Exception e) {
            log.error("Error loading plugins: ", e);
        }
    }

    private void loadPluginFromJar(String jarFilePath) {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            JarEntry pluginYmlEntry = jarFile.getJarEntry("plugin.yml");
            if (pluginYmlEntry == null) {
                log.error("No plugin.yml in " + jarFilePath);
                return;
            }

            // Read plugin.yml
            try (InputStream inputStream = jarFile.getInputStream(pluginYmlEntry)) {
                java.util.Properties properties = new java.util.Properties();
                properties.load(inputStream);
                String mainClass = properties.getProperty("main");
                String pluginName = properties.getProperty("name");

                if (mainClass == null) {
                    log.error("plugin.yml does not specify a main class in " + jarFilePath);
                    return;
                }

                if (pluginName == null) {
                    log.error("plugin.yml does not specify a name in " + jarFilePath);
                    return;
                }

                loadAndEnablePlugin(jarFilePath, mainClass, pluginName);
            }
        } catch (Exception e) {
            log.error("Error loading plugin from jar: ", e);
        }
    }

    private void loadAndEnablePlugin(String jarFilePath, String mainClass, String pluginName) {
        try {
            URL[] urls = {Paths.get(jarFilePath).toUri().toURL()};
            URLClassLoader classLoader = new URLClassLoader(urls, PluginLoader.class.getClassLoader());

            Class<?> clazz = Class.forName(mainClass, true, classLoader);

            if (!ShiinaPlugin.class.isAssignableFrom(clazz)) {
                log.info("[" + pluginName + "] " + mainClass + " does not extend ShiinaPlugin");
                return;
            }

            ShiinaPlugin pluginInstance = (ShiinaPlugin) clazz.getDeclaredConstructor().newInstance();
            pluginInstance.onEnable(pluginName);
            log.info("[" + pluginName + "] Loaded and enabled plugin");
        } catch (Exception e) {
            log.error("Error loading and enabling plugin: ", e);
        }
    }
}
