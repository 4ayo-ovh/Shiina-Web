package dev.osunolimits.routes.ap.api;

import lombok.Data;

public class PubSubModels {

    @Data
    public class RankOutput {
        public int beatmap_id;
        public int status;
        public boolean frozen;
    }

    @Data
    public class RestrictInput {
        public int id;
        public int userId; // Admin ID
        public String reason;
    }

    @Data
    public class UnrestrictInput {
        public int id;
        public int userId; // Admin ID
        public String reason;
    }

    @Data 
    public class WipeInput {
        public int id;
        public int mode;
    }

    @Data
    public class AlertAllInput {
        public String message;
    }

    @Data
    public class GiveDonatorInput {
        public int id;
        public String duration; // Durations: s/h/m/d/w
    }

    @Data
    public class AddPrivInput {
        public int id;
        public String[] privs; // Privileges: normal, verified, whitelisted, etc.
    }

    @Data
    public class RemovePrivInput {
        public int id;
        public String[] privs; // Privileges: normal, verified, whitelisted, etc.
    }

    @Data
    public class CountryChangeInput {
        public int id;
        public String country;
    }

    @Data 
    public class NameChangeInput {
        public int id;
        public String name;
    }
}
