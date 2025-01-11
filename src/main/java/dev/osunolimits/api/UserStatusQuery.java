package dev.osunolimits.api;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.osunolimits.common.APIRequest;
import dev.osunolimits.main.App;
import dev.osunolimits.models.UserStatus;
import dev.osunolimits.utils.CacheInterceptor;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class UserStatusQuery {
    private OkHttpClient client;

    public UserStatusQuery() {
        client = new OkHttpClient.Builder()
        .addNetworkInterceptor(new CacheInterceptor(15, TimeUnit.MINUTES))
        .cache(new Cache(new File(".cache/status"), 100L * 1024L * 1024L))
        .connectionPool(new ConnectionPool(200, 10, TimeUnit.SECONDS)).build(); 
    }

    public UserStatus getUserStatus(int id) {
        String url = "/v1/get_player_status?id=" + id;
        Request request = APIRequest.build(url);
        try {
            Response response = client.newCall(request).execute();
            JsonElement element = JsonParser.parseString(response.body().string());
            UserStatus userStatus = new Gson().fromJson(element, UserStatus.class);
            return userStatus;

        } catch (Exception e) {
            App.log.error("Failed to get User Status", e);
        }
        return null;
    }


}
