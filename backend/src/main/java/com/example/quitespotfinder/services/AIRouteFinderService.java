package com.example.quitespotfinder.services;

import com.example.quitespotfinder.dto.SpotAddressesDetails;
import com.example.quitespotfinder.dto.SpotScoreResult;
import com.example.quitespotfinder.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIRouteFinderService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AIRouteFinderService(ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.chatClient = builder
                .defaultSystem("You are an expert local guide and geospatial analyst. Analyze given place attributes, tags, route details, and satellite/street map imagery to find, filter, and rank the quietest, calmest, greenest, and most peaceful spots.")
                .build();
    }

    public SpotAddressesDetails filterTheSpots(SpotAddressesDetails spotAddressesDetails) throws JacksonException {
        List<Media> mediaList = getMediaResources(spotAddressesDetails);
        String jsonPayload = objectMapper.writeValueAsString(spotAddressesDetails);

        String promptText = """
                Analyze the following SpotAddressesDetails JSON and the attached satellite/map images corresponding to each place.

                Field Context:
                - currentLatitude, currentLongitude: Starting GPS coordinates of the user.
                - places[].placeLatitude, places[].placeLongitude: Exact coordinates of candidate spots.
                - places[].tags: OpenStreetMap semantic tags defining place nature (e.g., amenity, leisure, landuse, nature).
                - places[].properties: Reverse-geocoding data giving place name, street, locality, and type.
                - places[].imageName: Identifier matching attached satellite/aerial imagery for visual validation of greenery and isolation.
                - places[].routeResult: Navigation distance in meters and travel duration in minutes from current position.
                - places[].possibility: Unpopulated placeholder field (currently 0.0). You must compute and assign this score.

                Task:
                1. Inspect tags, properties, routes, and visual imagery.
                2. Filter out any private properties, private buildings, corporate/commercial office complexes, or private addresses. Retain only places accessible to the general public.
                3. For each public place, compute a new possibility score from 0.0 to 100.0 based on quietness, green space, and peacefulness.
                4. Return ONLY the imageName and possibility for each retained place. Do not repeat any other fields.

                Input JSON:
                """ + jsonPayload;

        SpotScoreResult response = chatClient.prompt()
                .user(userSpec -> {
                    userSpec.text(promptText);
                    if (!mediaList.isEmpty()) {
                        userSpec.media(mediaList.toArray(Media[]::new));
                    }
                })
                .call()
                .entity(SpotScoreResult.class);

        Map<String, Double> scoreByImage = (response == null || response.scores() == null)
                ? Map.of()
                : response.scores().stream()
                        .filter(s -> s.imageName() != null)
                        .collect(Collectors.toMap(SpotScoreResult.SpotScore::imageName, SpotScoreResult.SpotScore::possibility, Math::max));

        List<SpotAddressesDetails.Place> rankedPlaces = spotAddressesDetails.places().stream()
                .filter(place -> scoreByImage.containsKey(place.imageName()))
                .map(place -> new SpotAddressesDetails.Place(
                        place.placeLatitude(),
                        place.placeLongitude(),
                        place.tags(),
                        place.properties(),
                        place.imageName(),
                        place.routeResult(),
                        scoreByImage.get(place.imageName()),
                        buildGoogleMapUrl(place.placeLatitude(), place.placeLongitude())))
                .filter(place -> place.possibility() >= 30.0)
                .sorted(Comparator.comparingDouble(SpotAddressesDetails.Place::possibility).reversed())
                .toList();
        
        return new SpotAddressesDetails(spotAddressesDetails.currentLatitude(), spotAddressesDetails.currentLongitude(), rankedPlaces);
    }

    private List<Media> getMediaResources(SpotAddressesDetails spotAddressesDetails) {
        List<Media> mediaList = new ArrayList<>();
        if (spotAddressesDetails == null || spotAddressesDetails.places() == null) {
            return mediaList;
        }

        for (SpotAddressesDetails.Place place : spotAddressesDetails.places()) {
            if (place.imageName() != null && !place.imageName().isBlank()) {
                Resource resource = new FileSystemResource(AppConstants.MAP_IMAGE_PATH + place.imageName() + ".png");
                if (resource.exists()) {
                    mediaList.add(Media.builder()
                            .mimeType(MimeTypeUtils.IMAGE_PNG)
                            .name(place.imageName())
                            .data(resource)
                            .build());
                }
            }
        }
        return mediaList;
    }

    private String buildGoogleMapUrl(double lat, double lon) {
        return String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%f,%f", lat, lon);
    }
}
