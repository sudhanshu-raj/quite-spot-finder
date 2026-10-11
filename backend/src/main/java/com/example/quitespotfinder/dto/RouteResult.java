package com.example.quitespotfinder.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RouteResult(
        @JsonPropertyDescription("Travel distance in meters from the user's location")
        double distanceMeters,

        @JsonPropertyDescription("Estimated travel duration in minutes from the user's location")
        int durationMinutes
) {
}
