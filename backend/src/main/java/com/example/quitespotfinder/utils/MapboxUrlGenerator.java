package com.example.quitespotfinder.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public final class MapboxUrlGenerator {

    private static String accessToken;

    @Value("${mapbox.access-token}")
    public void setAccessToken(String token) {
        accessToken = token;
    }

    public MapboxUrlGenerator() {}
    
    public static String buildSatellitePreviewUrl(
            double lat,
            double lon,
            int zoom,
            int width,
            int height,
            boolean retina,
            String accessToken
    ) {
        String pinMarker = String.format(Locale.US, "pin-s-park+2ecc71(%f,%f)", lon, lat);
        String retinaSuffix = retina ? "@2x" : "";

        return String.format(
                Locale.US,
                "%s/mapbox/satellite-streets-v12/static/%s/%f,%f,%d,0,0/%dx%d%s?access_token=%s",
                AppConstants.MAPBOX_BASE_URL,
                pinMarker,
                lon,
                lat,
                zoom,
                width,
                height,
                retinaSuffix,
                accessToken
        );
    }
    
    public static String buildDefaultSatellitePreviewUrl(double lat, double lon) {
        return buildSatellitePreviewUrl(lat, lon, 17, 600, 400, true, accessToken);
    }
}