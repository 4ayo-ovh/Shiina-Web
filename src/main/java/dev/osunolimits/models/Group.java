package dev.osunolimits.models;

import lombok.Data;

@Data
public class Group {
    public int id;
    public String name;
    public String emoji;
    public String desc;

    public Group(String name, String emoji, String desc) {
        this.id = 0;
        this.name = name;
        this.emoji = emoji;
        this.desc = desc;
    }

    public Group() {}
}
