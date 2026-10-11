package com.example.quitespotfinder.config;

import com.example.quitespotfinder.utils.AppConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "OPTIONS")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path imagePath = Path.of(AppConstants.MAP_IMAGE_PATH).toAbsolutePath().normalize();
        try {
            java.nio.file.Files.createDirectories(imagePath);
        } catch (Exception ignored) {
        }
        String imageLocation = imagePath.toUri().toString();
        if (!imageLocation.endsWith("/")) {
            imageLocation += "/";
        }
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations(imageLocation);
    }
}
