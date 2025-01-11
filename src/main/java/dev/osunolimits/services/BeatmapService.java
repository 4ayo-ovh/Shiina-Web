package dev.osunolimits.services;

import com.google.gson.Gson;

import dev.osunolimits.common.Database;
import dev.osunolimits.common.MySQL;
import dev.osunolimits.main.App;
import dev.osunolimits.models.Beatmap;
import dev.osunolimits.routes.ap.api.PubSubModels;

public class BeatmapService implements AutoCloseable {

    private final MySQL MYSQL;
    private final Gson GSON = new Gson();

    public BeatmapService() {
        MYSQL = Database.getConnection();
    }

    public BeatmapService(MySQL mysql) {
        MYSQL = mysql;
    }

    public void changeBeatmapRankStatus(Beatmap beatmap, int newStatus, boolean newFrozenStatus) {
        int beatmapId = beatmap.getId();
        beatmap.setStatus(newStatus);
        beatmap.setFrozen(newFrozenStatus);
        
        int affectedRows = MYSQL.Exec("UPDATE beatmaps SET status = ?, frozen = ? WHERE id = ?", newStatus, newFrozenStatus  ? 1 : 0, beatmapId);
        if (affectedRows == 0) {
            App.log.error("Failed to update beatmap status for beatmap id: " + beatmapId);
        }

        PubSubModels models = new PubSubModels();
        PubSubModels.RankOutput rankOutput = models.new RankOutput();
        rankOutput.setBeatmap_id(beatmapId);
        rankOutput.setStatus(newStatus);
        rankOutput.setFrozen(newFrozenStatus);

        long subscribers = App.jedisPool.publish("rank", GSON.toJson(rankOutput));
        if (subscribers == 0) {
            App.log.error("No subscribers for rank pubsub");
        }
    }

    @Override
    public void close() {
        MYSQL.close();
    }
    
}
