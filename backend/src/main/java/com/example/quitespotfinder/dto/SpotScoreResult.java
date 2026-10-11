package com.example.quitespotfinder.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record SpotScoreResult(
        @JsonPropertyDescription("Scores for public places only")
        List<SpotScore> scores
) {
    public record SpotScore(
            @JsonPropertyDescription("The imageName of the place exactly as given in the input")
            String imageName,

            @JsonPropertyDescription("Score from 0.0 to 100.0 for quietness, greenery and peacefulness")
            double possibility
    ) {
    }
}
