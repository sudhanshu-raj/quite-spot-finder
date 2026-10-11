package com.example.quitespotfinder.services;


import com.example.quitespotfinder.dto.OverpassResponse;
import com.example.quitespotfinder.dto.RouteResult;
import com.example.quitespotfinder.dto.SpotAddressesDetails;
import com.example.quitespotfinder.exception.NoSpotFoundException;
import com.example.quitespotfinder.utils.AppConstants;
import com.example.quitespotfinder.utils.MapboxUrlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;


@Service
public class OpenStreetMapService {

    private final Logger logger = LoggerFactory.getLogger(OpenStreetMapService.class);

    private static final List<String> OVERPASS_ENDPOINTS = List.of(
            "https://lz4.overpass-api.de/api",
            "https://overpass-api.de/api",
            "https://overpass.kumi.systems/api",
            "https://overpass.private.coffee/api"
    );

    private final RestClient.Builder restClientBuilder;
    private final RestClient restClientKomoot;
    private final RestClient restClientOSRM;
    private final ImageDownloadService imageDownloadService;

    public OpenStreetMapService(RestClient.Builder restClientBuilder, ImageDownloadService imageDownloadService) {
        this.restClientBuilder = restClientBuilder;
        this.restClientKomoot = restClientBuilder.clone()
                .baseUrl(AppConstants.KOMOOT_API_BASE_URL)
                .defaultHeader("User-Agent", "QuietSpotFinder/1.0")
                .build();
        this.restClientOSRM = restClientBuilder.clone()
                .baseUrl(AppConstants.OSRM_API_BASE_URL)
                .defaultHeader("User-Agent", "QuietSpotFinder/1.0")
                .build();
        this.imageDownloadService = imageDownloadService;
    }

    @Tool(description = "Find the quite, chill and peace spots near me under some radius")
    public OverpassResponse findQuietSpots(
            @ToolParam(description = "latitude of address") double lat,
            @ToolParam(description = "longitude of address") double lon,
            @ToolParam(description = "radius under which we want to find spot and it's in meter") int radiusMeters,
            @ToolParam(description = "limit of how many max results/spots we want to find") int limit) {
        String overpassQl = String.format(Locale.US, """
            [out:json][timeout:25];
            (
               nw["leisure"~"^(park|garden)$"]["access"!~"^(private|no|customers)$"](around:%d, %f, %f);
               node["amenity"="bench"]["access"!~"^(private|no)$"](around:%d, %f, %f);
               nw["amenity"="place_of_worship"]["access"!="private"](around:%d, %f, %f);
            );
            out center %d;
            """, radiusMeters, lat, lon, radiusMeters, lat, lon, radiusMeters, lat, lon, limit);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("data", overpassQl);

        for (String endpoint : OVERPASS_ENDPOINTS) {
            logger.info("Trying overpass endpoint: " + endpoint);
            try {
                RestClient client = restClientBuilder.clone()
                        .baseUrl(endpoint)
                        .defaultHeader("User-Agent", "QuietSpotFinder/1.0")
                        .build();

                OverpassResponse response = client.post()
                        .uri("/interpreter")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(formData)
                        .retrieve()
                        .body(OverpassResponse.class);

                if (response != null && response.places() != null && !response.places().isEmpty()) {
                    return response;
                }
            } catch (Exception e) {
                logger.warn("Overpass endpoint failed: " + endpoint, e);
            }
        }

        logger.info("Falling back to Photon OSM search for spots");
        return findSpotsViaPhoton(lat, lon, limit);
    }

    private OverpassResponse findSpotsViaPhoton(double lat, double lon, int limit) {
        try {
            JsonNode response = restClientBuilder.clone()
                    .baseUrl("https://photon.komoot.io")
                    .defaultHeader("User-Agent", "QuietSpotFinder/1.0")
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/")
                            .queryParam("q", "park")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("features")) {
                return new OverpassResponse(List.of());
            }

            List<OverpassResponse.Place> places = new ArrayList<>();
            JsonNode features = response.get("features");
            for (JsonNode feature : features) {
                JsonNode coords = feature.path("geometry").path("coordinates");
                if (!coords.isArray() || coords.size() < 2) {
                    continue;
                }
                double pLon = coords.get(0).asDouble();
                double pLat = coords.get(1).asDouble();
                JsonNode props = feature.path("properties");
                Map<String, String> tags = new HashMap<>();
                if (props.hasNonNull("osm_key") && props.hasNonNull("osm_value")) {
                    tags.put(props.get("osm_key").asText(), props.get("osm_value").asText());
                }
                if (props.hasNonNull("name")) {
                    tags.put("name", props.get("name").asText());
                }
                long id = props.hasNonNull("osm_id") ? props.get("osm_id").asLong() : 0L;
                places.add(new OverpassResponse.Place(id, "node", pLat, pLon, null, tags));
            }
            return new OverpassResponse(places);
        } catch (Exception e) {
            logger.error("Photon fallback failed", e);
            return new OverpassResponse(List.of());
        }
    }
    
    @Tool(description = "Get the detail of the place/address like what's it's type and it's complete address by placeLatitude/placeLongitude")
     public Map<String, String> findPlaceDetails(
             @ToolParam(description = "latitude of the address") double lat, 
             @ToolParam(description = "longitude of the address") double lon) {
        JsonNode apiResponse = restClientKomoot.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .build())
                .retrieve()
                .body(JsonNode.class);
        if (apiResponse == null || !apiResponse.has("features")) {
            return Map.of();
        }
        JsonNode features = apiResponse.get("features");
        if (!features.isArray() || features.isEmpty()) {
            return Map.of();
        }
        JsonNode properties = features.get(0).path("properties");
        if (properties.isMissingNode()) {
            return Map.of();
        }
        Map<String, String> placeDetails = new HashMap<>();
        List<String> fields = List.of(
                "osm_type", "osm_key", "osm_value", "type", 
                "name", "street", "district", "city", 
                "state", "country", "postcode", "countrycode"
        );
        for (String field : fields) {
            if (properties.hasNonNull(field)) {
                placeDetails.put(field, properties.get(field).asText());
            }
        }
        return placeDetails;
    }

    @Tool(description = "Calculates walking distance in meters and travel time in minutes between two geographic coordinates")
    public RouteResult getWalkingDistanceAndTime(
            @ToolParam(description = "Starting latitude") double startLat,
            @ToolParam(description = "Starting longitude") double startLon,
            @ToolParam(description = "Destination latitude") double destLat,
            @ToolParam(description = "Destination longitude") double destLon) {

        String path = String.format(Locale.US, "/route/v1/walking/%f,%f;%f,%f?overview=false",
                startLon, startLat, destLon, destLat);

        JsonNode response = restClientOSRM.get()
                .uri(path)
                .retrieve()
                .body(JsonNode.class);
        
        if(response == null || !response.has("routes")) {
            return null;
        }
        JsonNode routes = response.get("routes");
        if (!routes.isArray() || routes.isEmpty()) {
            return null;
        }

        JsonNode data = routes.get(0);
        double distanceMeters = data.path("distance").asDouble();
        double durationSeconds = data.path("duration").asDouble();
        int durationMinutes = (int) Math.round(durationSeconds / 60.0);

        return new RouteResult(distanceMeters, durationMinutes);
    }

    @Tool(description = "Get the calm quite spot near me")
    public SpotAddressesDetails getSpots(double lat, double lon) {
        try {
            OverpassResponse spotsList = findQuietSpots(lat, lon, 2000, 20);
            if (spotsList == null || spotsList.places() == null || spotsList.places().isEmpty()) {
                throw new NoSpotFoundException("No quite spot found near the given address");
            }
            List<SpotAddressesDetails.Place> addressDetailList = new ArrayList<>();
            for (OverpassResponse.Place place : spotsList.places()) {
                double placeLat = place.getLatitude();
                double placeLon = place.getLongitude();
                if (placeLat == 0.0 && placeLon == 0.0) {
                    continue;
                }
                Map<String, String> details = findPlaceDetails(placeLat, placeLon);
                String imageName = "st_" + placeLat + "_" + placeLon + "_" + Instant.now().getEpochSecond();
                try {
                    String mapImageUrl = MapboxUrlGenerator.buildDefaultSatellitePreviewUrl(placeLat, placeLon);
                    imageDownloadService.downloadImage(mapImageUrl, imageName);
                }
                catch (Exception e) {
                    logger.error("Error while downloading the image", e);
                }
                Path imagePath = Path.of(AppConstants.MAP_IMAGE_PATH + imageName + ".png");
                if (!Files.exists(imagePath)) {
                    imageName  = "";
                }
                RouteResult routeResult = getWalkingDistanceAndTime(lat, lon, placeLat, placeLon);
                SpotAddressesDetails.Place formattedData = new SpotAddressesDetails.Place(
                        placeLat,
                        placeLon,
                        place.tags(),
                        details,
                        imageName,
                        routeResult
                );
                addressDetailList.add(formattedData);
            }
            return new SpotAddressesDetails(lat, lon, addressDetailList);

        } catch (Exception e) {
            logger.error("Error while downloading the spots", e);
            throw new RuntimeException("Error while fetching the spots", e);
        }
    }
    
    
    
}