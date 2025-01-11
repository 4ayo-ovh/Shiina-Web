package dev.osunolimits.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.OkHttpClient;

public class GeoLocQueryTest {

    private OkHttpClient client;
    private GeoLocQuery geoLocQuery;

    @BeforeEach
    void setUp() {
        client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        geoLocQuery = new GeoLocQuery(client);
    }

    @Test
    void testGetCountryCodeSuccess() {
        String countryCode = geoLocQuery.getCountryCode("8.8.8.8");
        assertNotNull(countryCode);
    }

    @Test
    void testGetCountryCodeFail() {
        String countryCode = geoLocQuery.getCountryCode("0.0.0.0");
        assertEquals("XX", countryCode);
    }
}
