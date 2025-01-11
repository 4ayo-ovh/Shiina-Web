package dev.osunolimits.externals;

import com.google.gson.Gson;

import dev.osunolimits.main.App;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TurnstileQuery {

    private OkHttpClient client;
    public TurnstileQuery(OkHttpClient client) {
        this.client = client;
    }

    public class TurnstileRespone {
        public boolean success;
        public String[] messages;
        public String hostname;
        public String[] errorCodes;
    }

    public TurnstileRespone verifyCaptcha(String captchaResponse) {

        RequestBody formBody = new FormBody.Builder()
        .add("secret", App.env.get("TURNSTILE_SECRET"))
        .add("response", captchaResponse)
        .build();

        Request request = new Request.Builder()
                .url("https://challenges.cloudflare.com/turnstile/v0/siteverify")
                .post(formBody)
                .build();

        try {
            String response = client.newCall(request).execute().body().string();
            Gson gson = new Gson();
            TurnstileRespone turnstileRespone = gson.fromJson(response, TurnstileRespone.class);
            return turnstileRespone;
        } catch (Exception e) {
            App.log.error("Error while verifying captcha", e);
        }
        return null;
    }
    
}
