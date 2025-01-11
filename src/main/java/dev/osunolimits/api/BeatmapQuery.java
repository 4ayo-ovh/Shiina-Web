package dev.osunolimits.api;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.osunolimits.common.APIRequest;
import dev.osunolimits.main.App;
import dev.osunolimits.models.Beatmap;
import dev.osunolimits.utils.CacheInterceptor;
import lombok.Data;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BeatmapQuery {

    @Data
    public class BeatmapResponse {
        private String status;
        private Beatmap[] data;
        private BeatmapMeta meta;
    }

    @Data
    public class BeatmapMeta {
        private int total;
        private int page;
        private int page_size;
    }

    int maxSize;
    private OkHttpClient client;
    public BeatmapQuery() {
        client = new OkHttpClient.Builder()
    .addNetworkInterceptor(new CacheInterceptor(30, TimeUnit.MINUTES))
    .cache(new Cache(new File(".cache/beatmaps"), 100L * 1024L * 1024L))
    .connectionPool(new ConnectionPool(50, 10, TimeUnit.SECONDS)).build(); 
    }


    private int parameter = 0;

    public String getParameter() {
        if (parameter == 0) {
            parameter++;
            return "?";
        } else {
            return "&";
        }
    }

    public BeatmapResponse getBeatmaps(int page, int pageSize, int status, Optional<String> artist, Optional<String> creator, int mode) {
        String url = "/v2/maps";
        url += getParameter() + "page=" + page;
        url += getParameter() + "page_size=" + pageSize;

        if (status != 999) {
            url += getParameter() + "status=" + status;
        }
        // Use isPresent() to check if Optional has a value
        if (artist.isPresent()) {
            url += getParameter() + "artist=" + artist.get();
        }
        // Use isPresent() to check if Optional has a value
        if (creator.isPresent()) {
            url += getParameter() + "creator=" + creator.get();
        }
        if (mode != 999) {
            url += getParameter() + "mode=" + mode;
        }

        Request request = APIRequest.build(url);

        try {
            Response response = client.newCall(request).execute();
            JsonElement element = JsonParser.parseString(response.body().string());
            BeatmapResponse beatmapResponse = new Gson().fromJson(element, BeatmapResponse.class);
            return beatmapResponse;

        } catch (Exception e) {
            App.log.error("Failed to get Beatmaps", e);
            return null;
        }
    }
}
