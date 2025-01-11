package dev.osunolimits.utils;

import java.util.regex.Pattern;

public class Validation {
    // and n
    private static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false; 
        }
        
        // Check if the string is a valid numeric value
        if (!pattern.matcher(strNum).matches()) {
            return false;
        }
        
        // Parse the number and check if it's less than zero
        try {
            double num = Double.parseDouble(strNum);
            return num >= 0; // Allow zero and positive numbers
        } catch (NumberFormatException e) {
            return false; // In case parsing fails
        }
    }
}
