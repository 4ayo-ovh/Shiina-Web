package dev.osunolimits.models;

import lombok.Data;

@Data
public class AuditLogEntry {
    private int id;
    private int userId;
    private String userName;
    private int targetId;
    private String targetName;
    private String action;
    private int status;
    private int mode;
    private String reason;
    private String privs;
}
