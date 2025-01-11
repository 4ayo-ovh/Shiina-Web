package dev.osunolimits.api;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import dev.osunolimits.common.APIRequest;
import dev.osunolimits.main.App;
import dev.osunolimits.models.Group;
import dev.osunolimits.models.UserInfoObject;
import dev.osunolimits.utils.CacheInterceptor;
import lombok.Data;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LeaderboardQuery {

    private Gson gson;


    @Data
    public class LeaderboardResponse {
        private String status;
        private LeaderboardItem[] leaderboard;
    }

    @Data
    public class LeaderboardItem {
        @SerializedName("player_id")
        private int playerId;

        @SerializedName("name")
        private String name;

        @SerializedName("country")
        private String country;

        @SerializedName("tscore")
        private long totalScore;

        @SerializedName("rscore")
        private long rankedScore;

        @SerializedName("pp")
        private int pp;

        @SerializedName("plays")
        private int plays;

        @SerializedName("playtime")
        private int playtime;

        @SerializedName("acc")
        private double accuracy;

        @SerializedName("max_combo")
        private int maxCombo;

        @SerializedName("xh_count")
        private int xhCount;

        @SerializedName("x_count")
        private int xCount;

        @SerializedName("sh_count")
        private int shCount;

        @SerializedName("s_count")
        private int sCount;

        @SerializedName("a_count")
        private int aCount;

        @SerializedName("clan_id")
        private Integer clanId;

        @SerializedName("clan_name")
        private String clanName;

        @SerializedName("clan_tag")
        private String clanTag;

        private List<Group> groups;
    }

    private OkHttpClient client;

    public LeaderboardQuery() {
        client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor(5, TimeUnit.MINUTES))
                .cache(new Cache(new File(".cache/leaderboard"), 100L * 1024L * 1024L))
                .connectionPool(new ConnectionPool(50, 20, TimeUnit.SECONDS)).build();
        gson = new Gson();
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

    public LeaderboardResponse getLeaderboard(String sort, int mode, int limit, int offset, Optional<String> country) {
        String url = "/v1/get_leaderboard";
        url += getParameter() + "sort=" + sort;
        url += getParameter() + "mode=" + mode;
        url += getParameter() + "limit=" + limit;
        url += getParameter() + "offset=" + offset;

        if (country.isPresent()) {
            url += getParameter() + "country=" + country.get();
        }

        Request request = APIRequest.build(url);

        try {
            Response response = client.newCall(request).execute();
            JsonElement element = JsonParser.parseString(response.body().string());
            LeaderboardResponse leaderboardResponse = gson.fromJson(element, LeaderboardResponse.class);
            for (LeaderboardItem item : leaderboardResponse.getLeaderboard()) {
                UserInfoObject userInfo = gson.fromJson(App.jedisPool.get("shiina:user:" + item.getPlayerId()),
                        UserInfoObject.class);
                if(userInfo != null) {
                    item.setGroups(userInfo.getGroups());
                }
            }
            return leaderboardResponse;

        } catch (Exception e) {
            App.log.error("Failed to get Leaderboard", e);
            return null;
        }
    }
}
