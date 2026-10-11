package com.example.quitespotfinder.controller;

import com.example.quitespotfinder.dto.SpotAddressesDetails;
import com.example.quitespotfinder.services.AIRouteFinderService;
import com.example.quitespotfinder.services.OpenStreetMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;

@RestController
@RequestMapping("/api/ai")
public class ChatController {
    
    private final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final AIRouteFinderService aiRouteFinderService;
    private final OpenStreetMapService openStreetMapService;

    public ChatController(AIRouteFinderService aiRouteFinderService,
                          OpenStreetMapService openStreetMapService) {
  
        this.aiRouteFinderService = aiRouteFinderService;
        this.openStreetMapService = openStreetMapService;
    }
    
    
    @GetMapping("/find-spots")
    public SpotAddressesDetails findTheSpots(@RequestParam double lat,
                                             @RequestParam double lon) throws JacksonException {
        SpotAddressesDetails spotsNearby = openStreetMapService.getSpots(lat, lon);
        logger.info("spots find !");
        SpotAddressesDetails filterResults = aiRouteFinderService.filterTheSpots(spotsNearby);
        logger.info("filter results !");
        return filterResults;
    }
}