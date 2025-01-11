package dev.osunolimits.api;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.osunolimits.main.App;

public class GeoLocQuery {

    public final String URL = "http://ip-api.com/json/%ip%?fields=status,message,countryCode";

    private OkHttpClient client;

    public GeoLocQuery() {
        client = new OkHttpClient.Builder().build(); 
    }

    public GeoLocQuery(OkHttpClient client) {
        this.client = client;
    }

    public String getCountryCode(String ip) {
        
        String url = URL.replace("%ip%", ip);
        Request request = new Request.Builder().url(url).build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "XX";
            }
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            if ("success".equals(json.get("status").getAsString())) {
                return json.get("countryCode").getAsString();
            }
        } catch (IOException e) {
            App.log.error("Failed to request country code from geoloc");
        }
        return "XX"; 
    }
}
