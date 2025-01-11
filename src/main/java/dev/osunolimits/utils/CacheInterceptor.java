package dev.osunolimits.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CacheInterceptor implements Interceptor {

    private int time;
    private TimeUnit unit;

    public CacheInterceptor(int time , TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        
        CacheControl cacheControl = new CacheControl.Builder()
            .maxAge(time, unit)
            .build();
        
        return response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build();
    }
}