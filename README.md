Moving to a new city alone can make it difficult to find peaceful spaces to disconnect, relax, or spend time outdoors. Standard navigation apps prioritize commerce and traffic rather than calm, green, or low-noise environments. Quiet Spot Finder was built to identify hidden green patches, parks, and quiet corners near a user's location.

### What It Does

1. **Location Detection:** Captures user coordinates directly through the browser.
2. **Geospatial Discovery:** Queries OpenStreetMap data for parks, gardens, and quiet public spaces within walking distance.
3. **Route & Address Enrichment:** Computes walking routes, distance, duration, and reverse-geocoded address metadata.
4. **Satellite Visual Verification:** Downloads high-resolution Mapbox satellite imagery for each candidate spot.
5. **Multimodal AI Evaluation:** Submits the location metadata, semantic tags, walking routes, and satellite images to an open-source multimodal model (`gemma4:e4b` via Ollama) to filter out private property and rank candidate spots by quietness and greenery.
6. **Curated Interface:** Displays top-scoring spots with direct walking metrics, satellite previews, calm percentage scores, and navigation links.

### Architecture and Tech Stack

- **Frontend:** React 19, TypeScript, Vite, Tailwind CSS.
- **Backend:** Spring Boot 4, Spring AI, Java 17.
- **Geospatial & Mapping:**
  - OpenStreetMap Overpass API for candidate POI queries.
  - Photon (Komoot) for reverse geocoding and resilient fallback search.
  - Project OSRM for walking distance and travel duration calculations.
  - Mapbox Static Images API for high-resolution aerial snapshots.
- **AI / LLM:** Ollama hosting `gemma4:e4b`, orchestrated using Spring AI's `ChatClient` with image and JSON context.

### Challenges and Engineering Solutions

- **Public Overpass API Reliability:** Public Overpass endpoints frequently suffer from global concurrency limits and 504 Gateway Timeouts. This was mitigated by implementing multi-endpoint failover (`lz4.overpass-api.de`, primary Overpass mirrors) combined with an automated fallback to Komoot's Photon search API when Overpass clusters are unavailable.
- **Query Optimization:** Dropped heavy relational tree scans (`nwr`) in favor of direct node/way queries (`nw`), avoiding server-side load shedding.
- **Multimodal Evaluation Pipeline:** Coordinated parallel ingestion of external geographic data, satellite tile generation, and local vision LLM inference within a non-blocking UI flow featuring cancelable background requests.

**Note** : Due to the Overpass APIs overload stuff issue, it can be possible we can get errors fetching places, and yeah I use this recording(not much optimized but..) bcz this is one of best one from my list.
