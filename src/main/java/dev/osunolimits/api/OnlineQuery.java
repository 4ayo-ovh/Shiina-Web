package dev.osunolimits.api;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.osunolimits.common.APIRequest;
import dev.osunolimits.main.App;
import dev.osunolimits.utils.CacheInterceptor;
import lombok.Data;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OnlineQuery {

    private OkHttpClient client;

    public OnlineQuery() {
        client = new OkHttpClient.Builder()
    .addNetworkInterceptor(new CacheInterceptor(5, TimeUnit.MINUTES))
    .cache(new Cache(new File(".cache/users"), 100L * 1024L * 1024L))
    .connectionPool(new ConnectionPool(200, 10, TimeUnit.SECONDS)).build(); 
    }


    public OnlineResponse getOnline() {
        String url = "/v1/online";
        Request request = APIRequest.build(url);
        try {
            Response response = client.newCall(request).execute();
            JsonElement element = JsonParser.parseString(response.body().string());
            OnlineResponse userResponse = new Gson().fromJson(element, OnlineResponse.class);
            return userResponse;

        } catch (Exception e) {
            App.log.error("Failed to get Online EX", e);
        }
        return null;
    }

    @Data
    public class OnlineUser {
        private int id;
        private String name;
    }

    @Data
    public class OnlineResponse {
        private String status;
        private OnlineUser[] players;
        private OnlineUser[] bots;
    }

}
