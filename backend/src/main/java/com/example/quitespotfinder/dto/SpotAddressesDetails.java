package com.example.quitespotfinder.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;
import java.util.Map;

public record SpotAddressesDetails(
        @JsonPropertyDescription("The user's current GPS latitude")
        double currentLatitude,

        @JsonPropertyDescription("The user's current GPS longitude")
        double currentLongitude,

        @JsonPropertyDescription("List of candidate places sorted by relevance")
        List<Place> places
) {
    public record Place(
            @JsonPropertyDescription("Latitude coordinate of this specific place")
            double placeLatitude,

            @JsonPropertyDescription("Longitude coordinate of this specific place")
            double placeLongitude,

            @JsonPropertyDescription("OpenStreetMap semantic tags describing the amenity, leisure, or landuse")
            Map<String, String> tags,

            @JsonPropertyDescription("Reverse-geocoded location attributes such as name, city, and street")
            Map<String, String> properties,

            @JsonPropertyDescription("Unique identifier matching the satellite image filename")
            String imageName,

            @JsonPropertyDescription("Route distance and duration calculated from the user's current coordinates")
            RouteResult routeResult,
            
            @JsonPropertyDescription("This denotes the possibility of changes of how much this place meets our searching, supposed to empty initially and later feed by AI")
            double possibility,
            
            @JsonPropertyDescription("Google map url for the spot, supposed to be empty and filled manually in end")
            String googleMapUrl
    
    ) {
        public Place(double placeLatitude, double placeLongitude, Map<String, String> tags, 
                     Map<String, String> properties, String imageName, RouteResult routeResult) {
            this(placeLatitude, placeLongitude, tags, properties, imageName, routeResult, 0.0, null);
        }
    }
}
