package dev.osunolimits.modules.utils;

import java.util.ArrayList;
import java.util.List;

import dev.osunolimits.main.App;
import lombok.Data;

@Data
public class SEOBuilder {
    private String domain = App.env.get("DOMAIN");
    public String title;
    public String description;
    public String image;
    public List<String> keywords = new ArrayList<>();

    // Default values for fields
    public SEOBuilder(String title, String description) {
        this.title = title + " | " + App.customization.get("serverName");
        this.description = description;
        this.image = domain + App.customization.get("logoUrl");
        this.keywords.add("osu!");
        this.keywords.add("osu! private server");
    }

     // Default values for fields
     public SEOBuilder(String title, String description, String image) {
        this.title = title + " | " + App.customization.get("serverName");
        this.description = description;
        this.image = image;
        this.keywords.add("osu!");
        this.keywords.add("osu! private server");
    }

    public SEOBuilder(String title, String description, String... keywords) {
        this.title = title + " | " + App.customization.get("serverName");
        this.description = description;
        this.image = domain + App.customization.get("logoUrl");
        this.keywords.add("osu!");
        this.keywords.add("osu! private server");
        for (String string : keywords) {
            this.keywords.add(string);
        }
    }

    public SEOBuilder(String title, String description,String image, String... keywords) {
        this.title = title + " | " + App.customization.get("serverName");
        this.description = description;
        this.image = image;
        this.keywords.add("osu!");
        this.keywords.add("osu! private server");
        for (String string : keywords) {
            this.keywords.add(string);
        }
    }



}
