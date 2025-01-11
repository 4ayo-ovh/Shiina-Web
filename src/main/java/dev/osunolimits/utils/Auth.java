package dev.osunolimits.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.bcrypt.BCrypt;

import dev.osunolimits.main.App;
import lombok.Data;

public class Auth {

    @Data
    public class User {
        public Integer id;
        public String name;
        public String safe_name;
        public String email;
        public Integer priv;
        public Integer created;
    }

    @Data
    public class SessionUser {
        public Integer id;
        public Integer created;
        public String ip;
    }

    private static final SecureRandom secureRandom = new SecureRandom(); 
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); 

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public static Boolean checkPw(String raw, String bcrypt) {
        try {
            return BCrypt.checkpw(md5(raw), bcrypt);
        } catch (Exception e) {
            App.log.error("Failed to encode MD5", e);
            return null;
        }
    }

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return new BigInteger(1, md.digest(input.getBytes())).toString(16);
        } catch (Exception e) {
            App.log.error("Failed to hash using MD5", e);
            return null;
        }
    }

    public static String bcrypt(String input) {
        return BCrypt.hashpw(input, BCrypt.gensalt());
    }
}
