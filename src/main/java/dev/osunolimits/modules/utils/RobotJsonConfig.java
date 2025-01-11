package dev.osunolimits.modules.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import dev.osunolimits.main.App;
import lombok.Data;

public class RobotJsonConfig {

    public static Robots robots;

    private final Gson GSON;

    public RobotJsonConfig() {
        this.GSON = new Gson();

        File robotConfig = new File("data/robots.json");

        if(!robotConfig.exists()) {
            Robots robots = new Robots();
            robots.robots.add(new RobotEntry("Googlebot", RobotType.DISALLOW, "/shiina"));
            String fileContent = GSON.toJson(robots);
            try (FileWriter writer = new FileWriter(robotConfig)) {
                writer.write(fileContent);
                App.log.info("Created default robot configuration at {}", robotConfig.getPath());
            } catch (IOException e) {
                App.log.error("Failed to create default robot configuration", e);
            }
            RobotJsonConfig.robots = robots;
        }else {
            try {
                String fileContent = Files.readString(robotConfig.toPath());
                RobotJsonConfig.robots = GSON.fromJson(fileContent, Robots.class);
            } catch (IOException e) {
                App.log.error("Failed to read robot configuration", e);
            }
        }

    }

    public void updateRobotsTxt() {
        File robotsTxt = new File("static/robots.txt");
        try (FileWriter writer = new FileWriter(robotsTxt)) {
            for (RobotEntry robotEntry : robots.getRobots()) {
                writer.write("User-agent: " + robotEntry.getUserAgent() + "\n");
                writer.write(Character.toUpperCase(robotEntry.getType().name().charAt(0)) + 
                robotEntry.getType().name().substring(1).toLowerCase() + ": " + robotEntry.getPath() + "\n");
            
            }
        } catch (IOException e) {
            App.log.error("Failed to update robots.txt", e);
        }
    }

    @Data
    public class Robots {
        List<RobotEntry> robots = new ArrayList<>();
    }

    @Data
    public class RobotEntry {
        public String userAgent;
        public RobotType type;
        public String path;

        public RobotEntry(String userAgent, RobotType type, String path) {
            this.userAgent = userAgent;
            this.type = type;
            this.path = path;
        }
    }

    public enum RobotType{
        DISALLOW,
        ALLOW
    }
    
}
