package com.example.quitespotfinder.dto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record NearbySearchResponse(
        List<Place> places
) {
    public record Place(
            String id,
            String formattedAddress,
            Location location,
            String googleMapsUri,
            DisplayName displayName,
            GoogleMapsLinks googleMapsLinks,
            List<String> photosUrl
    ) {
        private static final Pattern DATA_ID_PATTERN = Pattern.compile("!1s(0x[0-9a-fA-F]+:0x[0-9a-fA-F]+)");

        public Place withPhotosUrl(List<String> newPhotosUrl) {
            return new Place(id, formattedAddress, location, googleMapsUri, displayName, googleMapsLinks, newPhotosUrl);
        }

        public String getDataId() {
            if (googleMapsLinks == null) {
                return null;
            }
            String targetUri = googleMapsLinks.photosUri() != null
                    ? googleMapsLinks.photosUri()
                    : googleMapsLinks.reviewsUri();
            if (targetUri == null) {
                return null;
            }
            Matcher matcher = DATA_ID_PATTERN.matcher(targetUri);
            return matcher.find() ? matcher.group(1) : null;
        }
    }

    public record Location(
            Double latitude,
            Double longitude
    ) {}

    public record DisplayName(
            String text,
            String languageCode
    ) {}

    public record GoogleMapsLinks(
            String directionsUri,
            String placeUri,
            String writeAReviewUri,
            String reviewsUri,
            String photosUri
    ) {}
}
