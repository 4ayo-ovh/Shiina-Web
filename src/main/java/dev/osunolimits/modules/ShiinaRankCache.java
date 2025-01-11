package dev.osunolimits.modules;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dev.osunolimits.common.Database;
import dev.osunolimits.common.MySQL;
import dev.osunolimits.main.App;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.resps.Tuple;

public class ShiinaRankCache {
    long initialDelay = calculateInitialDelay(9, 59);
    long period = TimeUnit.DAYS.toSeconds(1);
    ScheduledExecutorService scheduler;
    
    public ShiinaRankCache() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(getRankCacheThread(), initialDelay, period, TimeUnit.SECONDS);
    }

    public Runnable getRankCacheThread() {
        return new Runnable() {

            @Override
            public void run() {
                App.log.info("Updating rank cache at " + LocalDateTime.now());
                MySQL mysql = Database.getConnection();
                JedisPooled jedis = App.jedisPool;
                for(int mode = 0; mode <= 8; mode++) {
                    if(mode == 7) continue;

                    List<Tuple> topPlayers = jedis.zrevrangeWithScores("bancho:leaderboard:"+mode, 0, -1);
                    int rank = 0;
                    for(Tuple player : topPlayers) {
                        rank++;
                        String userId = player.getElement();
                        mysql.Exec("INSERT INTO `sh_rank_cache`(`user_id`, `date`, `mode`, `rank`) VALUES (?,CURDATE(),?,?)", userId, mode, rank);
                    }
                }
                mysql.close();
            }

        };
    }

    /**
     * Calculate the initial delay to the next occurrence of the given time.
     *
     * @param targetHour   Target hour (0-23)
     * @param targetMinute Target minute (0-59)
     * @return Initial delay in seconds
     */
    private static long calculateInitialDelay(int targetHour, int targetMinute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0);

        if (now.isAfter(nextRun)) {
            // If the time has already passed today, schedule for tomorrow
            nextRun = nextRun.plusDays(1);
        }

        return Duration.between(now, nextRun).getSeconds();
    }
    
}
