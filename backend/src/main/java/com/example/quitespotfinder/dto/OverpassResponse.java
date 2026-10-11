package com.example.quitespotfinder.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OverpassResponse(
        @JsonProperty("elements") List<Place> places
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Place(
            Long id,
            String type,
            Double lat,
            Double lon,
            Center center,
            Map<String, String> tags
    ) {
        public double getLatitude() {
            return lat != null ? lat : (center != null ? center.lat() : 0.0);
        }

        public double getLongitude() {
            return lon != null ? lon : (center != null ? center.lon() : 0.0);
        }

        public String getName() {
            return tags != null ? tags.getOrDefault("name", "Unnamed Spot") : "Unnamed Spot";
        }
    }

    public record Center(Double lat, Double lon) {}
}