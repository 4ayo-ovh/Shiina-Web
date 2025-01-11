package dev.osunolimits.routes.api.get;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import spark.Request;
import spark.Response;
import spark.Route;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import dev.osunolimits.modules.Shiina;

public class GetBmThumbnail implements Route {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3";
    public static final HashMap<String, byte[]> thumbnailCache = new HashMap<>();
    private static final OkHttpClient client = new OkHttpClient();

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String setId = req.queryParams("setId");
        if (setId == null) return "Invalid set ID";

        Shiina.setCachePolicy(res, 7);
        
        if (thumbnailCache.containsKey(setId)) {
            res.type("image/jpeg");
            return thumbnailCache.get(setId);
        }

        String reqString = "https://assets.ppy.sh/beatmaps/" + setId + "/covers/card.jpg";
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(reqString)
                .header("User-Agent", USER_AGENT)
                .build();

        okhttp3.Response response = client.newCall(request).execute();
        byte[] imageBytes = null;
        try {
            if (response.code() == 200) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    imageBytes = responseBody.bytes();
                }
            } else {
                Path imagePath = Paths.get("static/img/nobeatmapicon.png");
                imageBytes = Files.readAllBytes(imagePath);
            }
        } finally {
            response.close(); // Ensure the response is closed
        }

        if (imageBytes != null) {
            thumbnailCache.put(setId, imageBytes);
            res.type("image/jpeg");
            return imageBytes;
        } else {
            return "Failed to retrieve image";
        }
    }
}