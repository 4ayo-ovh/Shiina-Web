package dev.osunolimits.utils.osu;

import java.util.Map;
import java.util.regex.*;

public class OsuAchievementParser {

    // Maps mode_vn integer values to readable game mode names
    private static final Map<Integer, String> MODES = Map.of(
        0, "OSU",
        1, "TAIKO",
        2, "CATCH",
        3, "MANIA"
    );

    public static String parseCondition(String condition) {
        StringBuilder description = new StringBuilder();

        // Regex for different conditions
        Pattern modPattern = Pattern.compile("score\\.mods & (\\d+)");
        Pattern srPattern = Pattern.compile("(\\d+) <= score\\.sr < (\\d+)");
        Pattern comboPattern = Pattern.compile("(\\d+) <= score\\.max_combo < (\\d+)");
        Pattern modePattern = Pattern.compile("mode_vn == (\\d+)");
        Pattern perfectPattern = Pattern.compile("score\\.perfect");

        // Match mods
        Matcher modMatcher = modPattern.matcher(condition);
        while (modMatcher.find()) {
            int modValue = Integer.parseInt(modMatcher.group(1));
            description.append(describeMod(modValue)).append(" ");
        }

        // Match star rating
        Matcher srMatcher = srPattern.matcher(condition);
        while (srMatcher.find()) {
            int lower = Integer.parseInt(srMatcher.group(1));
            int upper = Integer.parseInt(srMatcher.group(2));
            description.append("The star rating needs to be between ").append(lower).append(" and ").append(upper).append(". ");
        }

        // Match max combo
        Matcher comboMatcher = comboPattern.matcher(condition);
        while (comboMatcher.find()) {
            int lower = Integer.parseInt(comboMatcher.group(1));
            int upper = Integer.parseInt(comboMatcher.group(2));
            description.append("The max combo needs to be between ").append(lower).append(" and ").append(upper).append(". ");
        }

        // Match mode_vn
        Matcher modeMatcher = modePattern.matcher(condition);
        while (modeMatcher.find()) {
            int mode = Integer.parseInt(modeMatcher.group(1));
            description.append("Mode: ").append(MODES.getOrDefault(mode, "Unknown")).append(". ");
        }

        // Match perfect scores
        if (perfectPattern.matcher(condition).find()) {
            description.append("The score nees to be a perfect play. ");
        }

        return description.toString().trim();
    }

    private static String describeMod(int modValue) {
        // TODO: Refactor this to use Mods class
        switch (modValue) {
            case 1: 
                return "NoFail mod needs to be disabled.";
            case 2: 
                return "Easy mod needs to be enabled.";
            case 8: 
                return "Hidden mod needs to be enabled.";
            case 16: 
                return "HardRock mod needs to be enabled.";
            case 64: 
                return "Flashlight mod needs to be enabled.";
            case 256: 
                return "DoubleTime mod needs to be enabled.";
            case 512: 
                return "Nightcore mod needs to be enabled.";
            case 1024: 
                return "HalfTime mod needs to be enabled.";
            case 4096: 
                return "Relax mod needs to be enabled.";
            case 16384: 
                return "Autoplay mod needs to be enabled.";
            case 0: 
                return "No mods are enabled.";
            default: 
                return "Unknown mod value: " + modValue + ".";
        }
    }
}
