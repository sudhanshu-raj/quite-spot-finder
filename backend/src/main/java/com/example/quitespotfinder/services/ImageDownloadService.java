package com.example.quitespotfinder.services;

import com.example.quitespotfinder.utils.AppConstants;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class ImageDownloadService {

    private final RestClient restClient;

    public ImageDownloadService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.clone()
                .defaultHeader("User-Agent", "QuietSpotFinder/1.0")
                .build();
    }
    
    public  void downloadImage(String imageUrl, String imageName) throws IOException {
        Path targetPath = Path.of(AppConstants.MAP_IMAGE_PATH + imageName + ".png");
        if (targetPath.getParent() != null) {
            Files.createDirectories(targetPath.getParent());
        }

        byte[] imageBytes = restClient.get()
                .uri(imageUrl)
                .accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
                .retrieve()
                .body(byte[].class);

        if (imageBytes != null && imageBytes.length > 0) {
            Files.write(targetPath, imageBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            throw new IOException("Received empty image body from " + imageUrl);
        }
    }
    
    
}