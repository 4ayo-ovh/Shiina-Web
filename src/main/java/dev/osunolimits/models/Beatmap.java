package dev.osunolimits.models;

import lombok.Data;

@Data
public class Beatmap {
    private int id;
    private String server;
    private int set_id;
    private int status;
    private String md5;
    private String artist;
    private String title;
    private String version;
    private String creator;
    private String filename;
    private String last_update;
    private int totalLength;
    private int maxCombo;
    private boolean frozen;
    private int plays;
    private int passes;
    private int mode;
    private int bpm;
    private float cs;
    private float ar;
    private float od;
    private float hp;
    private float diff;
}
