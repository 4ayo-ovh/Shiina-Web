package dev.osunolimits.models;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import dev.osunolimits.modules.utils.ThemeLoader;
import lombok.Data;

@Data
public class Theme {
    private String name;
    private String fullname;
    private String author;
    private boolean included;
    private String screenshot;

    public static Theme loadTheme(File f) {
        try {
            String file = String.join("\n", Files.readAllLines(f.toPath()));
            Yaml yaml = new Yaml();
            Map<String, Object> obj = yaml.load(file);
            Theme theme = new Theme();
            theme.setName((String) obj.get("name"));
            theme.setFullname((String) obj.get("fullname"));
            theme.setAuthor((String) obj.get("author"));
            theme.setIncluded((boolean) obj.get("included"));
            theme.setScreenshot((String) obj.get("screenshot"));

            return theme;
        } catch (Exception e) {
            ThemeLoader.logger.error("Failed to load theme file: " + f.getName());
        }
        return null;
    }
}
