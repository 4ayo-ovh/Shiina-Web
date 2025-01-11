package dev.osunolimits.utils.osu;

public class LevelCalculator {
 
    public static long getRequiredScoreForLevel(int level) {
        if (level < 1)
            return 0;
        if (level <= 100) {
            return (long) Math.ceil(
                    (5000 / 3.0) * (4 * Math.pow(level, 3) - 3 * Math.pow(level, 2) - level)
                            + 1.25 * Math.pow(1.8, (level - 60)));
        } else {
            return 26931190827L + 99999999999L * (level - 100) + 2L;
        }
    }

    public static int getLevel(long xp) {
        if (xp < 0)
            return 1;

        long maxLevelXP = getRequiredScoreForLevel(99);
        if (xp >= maxLevelXP) {
            return 100 + (int) ((xp - maxLevelXP) / 100000000000L);
        }

        for (int level = 1; level <= 100; level++) {
            if (xp < getRequiredScoreForLevel(level)) {
                return level;
            }
        }
        return 1;
    }

    public static double getLevelPrecise(long xp) {
        int baseLevel = getLevel(xp);
        long baseLevelXP = getRequiredScoreForLevel(baseLevel);
        long nextLevelXP = getRequiredScoreForLevel(baseLevel + 1);

        long scoreProgress = xp - baseLevelXP;
        long scoreLevelDifference = nextLevelXP - baseLevelXP;

        double preciseLevel = (double) scoreProgress / scoreLevelDifference + baseLevel;
        return Double.isInfinite(preciseLevel) || Double.isNaN(preciseLevel) ? 0 : preciseLevel;
    }

    public static double getPercentageToNextLevel(long xp) {
        double level = getLevelPrecise(xp);
        int baseLevel = (int) level;
        long currentLevelXP = getRequiredScoreForLevel(baseLevel);
        long nextLevelXP = getRequiredScoreForLevel(baseLevel + 1);

    
        if (nextLevelXP == Long.MAX_VALUE) {
            return 100.0;
        }
    
        long xpProgress = xp - currentLevelXP;
        long xpForNextLevel = nextLevelXP - currentLevelXP;
    
        double progress = Math.max(0, (double) xpProgress / xpForNextLevel) * 100;
        return Math.min(progress, 100); 
    }
}
