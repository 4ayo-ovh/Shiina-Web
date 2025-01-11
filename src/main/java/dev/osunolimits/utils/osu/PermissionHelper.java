package dev.osunolimits.utils.osu;

import java.util.Arrays;
import java.util.EnumSet;

public class PermissionHelper {

    public enum Privileges {
        UNRESTRICTED(1 << 0),
        VERIFIED(1 << 1),
        WHITELISTED(1 << 2),
        SUPPORTER(1 << 4),
        PREMIUM(1 << 5),
        ALUMNI(1 << 7),
        TOURNEY_MANAGER(1 << 10),
        NOMINATOR(1 << 11),
        MODERATOR(1 << 12),
        ADMINISTRATOR(1 << 13),
        DEVELOPER(1 << 14),
        STAFF(MODERATOR.value | ADMINISTRATOR.value | DEVELOPER.value);

        private final int value;

        Privileges(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static EnumSet<Privileges> fromInt(int privileges) {
            EnumSet<Privileges> result = EnumSet.noneOf(Privileges.class);
        
            for (Privileges priv : Privileges.values()) {
                if ((priv.value & privileges) == priv.value) {
                    result.add(priv);
                }
            }

            if(!result.contains(STAFF) && result.contains(MODERATOR)) {
                result.add(STAFF);
            }
        
            return result;
        }
        
        
        public static int fromPrivs(Privileges... privs) {
            int result = 0;
            for (Privileges priv : privs) {
                result |= priv.getValue(); // Combine privileges using bitwise OR
            }
            return result;
        }
    }

    public static boolean hasPrivileges(int userPriv, Privileges... privs) {
        return Privileges.fromInt(userPriv).containsAll(EnumSet.copyOf(Arrays.asList(privs)));
    }
}
