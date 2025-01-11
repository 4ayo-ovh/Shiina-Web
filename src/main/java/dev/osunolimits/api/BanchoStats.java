package dev.osunolimits.api;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.osunolimits.common.APIRequest;
import dev.osunolimits.main.App;
import dev.osunolimits.utils.CacheInterceptor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BanchoStats {

    public class PlayerCountResponse {
        @Getter @Setter
        private int total;
        @Getter @Setter
        private int online;
    }

    @Data
    public class CustomCountResponse {
        private int clans;
        private int plays;
        private int beatmaps;
        private int totalLogins;
        private int loginsToday;
        private int staff;
        private int restricted;
    }

    private OkHttpClient client;
    public BanchoStats() {
        client = new OkHttpClient.Builder()
    .addNetworkInterceptor(new CacheInterceptor(30, TimeUnit.MINUTES)) 
    .cache(new Cache(new File(".cache/stats"), 100L * 1024L * 1024L))
    .connectionPool(new ConnectionPool(50, 30, TimeUnit.MINUTES)).build(); 
    }



    public CustomCountResponse getCustomCount() {
        Request requestMaps = APIRequest.build("/v2/maps");
        Request requestClans = APIRequest.build("/v2/clans");
        Request requestPlays = APIRequest.build("/v2/scores");

        try {
            Response responseMaps = client.newCall(requestMaps).execute();
            Response responseClans = client.newCall(requestClans).execute();
            Response responsePlays = client.newCall(requestPlays).execute();
            JsonElement mapsElement = JsonParser.parseString(responseMaps.body().string());
            JsonElement clansElement = JsonParser.parseString(responseClans.body().string());
            JsonElement playsElement = JsonParser.parseString(responsePlays.body().string());


            CustomCountResponse customCount = new CustomCountResponse();
            customCount.setBeatmaps(mapsElement.getAsJsonObject().get("meta").getAsJsonObject().get("total").getAsInt());
            customCount.setClans(clansElement.getAsJsonObject().get("meta").getAsJsonObject().get("total").getAsInt());
            customCount.setPlays(playsElement.getAsJsonObject().get("meta").getAsJsonObject().get("total").getAsInt());
            return customCount;
        }catch(Exception e) {
            App.log.error("Failed to get custom counts", e);
            return null;
        }
    }

    public PlayerCountResponse getPlayerCount() {
        Request request = APIRequest.build("/v1/get_player_count");

        try {
            Response response = client.newCall(request).execute();
            JsonElement playerCountElement = JsonParser.parseString(response.body().string());
            JsonElement counts = playerCountElement.getAsJsonObject().get("counts");
            PlayerCountResponse playerCount = new PlayerCountResponse();
            playerCount.setTotal(counts.getAsJsonObject().get("total").getAsInt());
            playerCount.setOnline(counts.getAsJsonObject().get("online").getAsInt());
            return playerCount;
        }catch(Exception e) {
            App.log.error("Failed to get /v1/get_player_count", e);
            return null;
        }

    }
    
}
