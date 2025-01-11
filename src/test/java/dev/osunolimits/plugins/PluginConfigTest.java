package dev.osunolimits.plugins;

import org.junit.jupiter.api.*;
import java.io.File;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class PluginConfigTest {

    static class ExampleConfig {
        public String name = "default";
        public int version = 1;
    }

    private static final String PLUGIN_NAME = "TestPlugin";
    private static final File PLUGIN_DIR = new File("plugins/" + PLUGIN_NAME);
    private static final File CONFIG_FILE = new File(PLUGIN_DIR, "config.json");

    @BeforeEach
    void setup() {
        deleteDirectory(PLUGIN_DIR);
    }

    @AfterEach
    void cleanup() {
        deleteDirectory(PLUGIN_DIR);
    }

    @Test
    void testCreateDefaultConfig() {
        PluginConfig pluginConfig = new PluginConfig(PLUGIN_NAME, ExampleConfig.class);
        pluginConfig.loadConfig();

        assertTrue(CONFIG_FILE.exists(), "Config file should be created");
    }

    @Test
    void testLoadExistingConfig() throws Exception {
        PluginConfig pluginConfig = new PluginConfig(PLUGIN_NAME, ExampleConfig.class);
        pluginConfig.loadConfig();

        String modifiedConfig = "{\"name\":\"modified\",\"version\":2}";
        Files.writeString(CONFIG_FILE.toPath(), modifiedConfig);

        pluginConfig.loadConfig();
        ExampleConfig config = (ExampleConfig) pluginConfig.getConfig();

        assertNotNull(config, "Config should be loaded");
        assertEquals("modified", config.name, "Config name should be 'modified'");
        assertEquals(2, config.version, "Config version should be 2");
    }

    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}
