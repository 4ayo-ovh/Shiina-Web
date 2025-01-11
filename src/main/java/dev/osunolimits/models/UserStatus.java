package dev.osunolimits.models;

import lombok.Data;

@Data
public class UserStatus {
    private String status;
    private PlayerStatus player_status;

    @Data
    public class PlayerStatus {
        private boolean online;
        private double login_time;
        private PlayerStatusExtended status;
        
    }

    @Data
    public class PlayerStatusExtended {
        private int action;
        private String info_text;
    }
}

