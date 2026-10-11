package com.example.quitespotfinder;

import com.example.quitespotfinder.dto.OverpassResponse;
import com.example.quitespotfinder.dto.RouteResult;
import com.example.quitespotfinder.dto.SpotAddressesDetails;
import com.example.quitespotfinder.services.AIRouteFinderService;
import com.example.quitespotfinder.services.OpenStreetMapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
public class Test1 {

    @Autowired
    private AIRouteFinderService aiRouteFinderService;
    
    @Autowired
    private OpenStreetMapService openStreetMapService;
    
    @Test
    public void test() {
        OverpassResponse response = openStreetMapService.findQuietSpots(28.540823271124275, 77.33931663661876, 200, 20);
        System.out.println(response);
        assertTrue(true);
        
    }
    
    @Test
    public void test1() throws Exception {
        

//        SpotAddressesDetails result = aiRouteFinderService.filterTheSpots(getSpotAddressesDetails());
//        System.out.println("llm filter the result");
//        System.out.println(result);
        assertTrue(true);
    }
    
    SpotAddressesDetails getSpotAddressesDetails() {
        return  new SpotAddressesDetails(
                28.540721,
                77.339405,
                List.of(
                        new SpotAddressesDetails.Place(
                                28.5361737,
                                77.3396984,
                                Map.of(
                                        "amenity", "place_of_worship",
                                        "religion", "hindu"
                                ),
                                Map.of(
                                        "osm_type", "W",
                                        "country", "India",
                                        "osm_key", "landuse",
                                        "city", "Noida",
                                        "district", "Ralpur Khadar",
                                        "countrycode", "IN",
                                        "osm_value", "commercial",
                                        "name", "HCL",
                                        "state", "Uttar Pradesh",
                                        "type", "locality"
                                ),
                                "st_28.5361737_77.3396984_1791702769",
                                new RouteResult(1533.7, 5),0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5572261,
                                77.3337328,
                                Map.of("leisure", "park"),
                                Map.ofEntries(
                                        entry("osm_type", "W"),
                                        entry("country", "India"),
                                        entry("osm_key", "highway"),
                                        entry("city", "Noida"),
                                        entry("district", "Chhalera"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "secondary"),
                                        entry("name", "Dadri Road"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "street")
                                ),
                                "st_28.5572261_77.3337328_1791702770",
                                new RouteResult(4958.8, 7),0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5567507,
                                77.3318033,
                                Map.of("leisure", "park"),
                                Map.ofEntries(
                                        entry("osm_type", "W"),
                                        entry("country", "India"),
                                        entry("osm_key", "railway"),
                                        entry("city", "Noida"),
                                        entry("street", "Noida-Greater Noida Expressway"),
                                        entry("district", "Bakhtawarpur"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "subway"),
                                        entry("name", "Delhi Metro Magenta Line"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5567507_77.3318033_1791702772",
                                new RouteResult(4893.0, 7),0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5523543,
                                77.3351626,
                                Map.of("leisure", "park"),
                                Map.ofEntries(
                                        entry("osm_type", "N"),
                                        entry("country", "India"),
                                        entry("osm_key", "railway"),
                                        entry("city", "Noida"),
                                        entry("street", "Sector 44 Road"),
                                        entry("district", "Ralpur Khadar"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "proposed"),
                                        entry("name", "Noida Sector 44"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5523543_77.3351626_1791702774",
                                new RouteResult(5109.8, 7), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5534822,
                                77.3345409,
                                Map.of("leisure", "park"),
                                Map.ofEntries(
                                        entry("osm_type", "N"),
                                        entry("country", "India"),
                                        entry("osm_key", "railway"),
                                        entry("city", "Noida"),
                                        entry("street", "Sector 44 Road"),
                                        entry("district", "Ralpur Khadar"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "proposed"),
                                        entry("name", "Noida Sector 44"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5534822_77.3345409_1791702775",
                                new RouteResult(4921.1, 7), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5453928,
                                77.3336139,
                                Map.of(
                                        "designation", "Park",
                                        "leisure", "park"
                                ),
                                Map.ofEntries(
                                        entry("osm_type", "W"),
                                        entry("country", "India"),
                                        entry("osm_key", "building"),
                                        entry("city", "Noida"),
                                        entry("street", "Sector 125, Noida"),
                                        entry("district", "Ralpur Khadar"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "yes"),
                                        entry("name", "E1 Block"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5453928_77.3336139_1791702778",
                                new RouteResult(1040.3, 2), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5367387,
                                77.3441193,
                                Map.of(
                                        "addr:city", "noida",
                                        "addr:housenumber", "SEZ, sector 126",
                                        "leisure", "park",
                                        "name", "HCL open park"
                                ),
                                Map.of(
                                        "osm_type", "W",
                                        "country", "India",
                                        "osm_key", "leisure",
                                        "city", "noida",
                                        "district", "Ralpur Khadar",
                                        "countrycode", "IN",
                                        "osm_value", "park",
                                        "name", "HCL open park",
                                        "state", "Uttar Pradesh",
                                        "type", "other"
                                ),
                                "st_28.5367387_77.3441193_1791702779",
                                new RouteResult(1813.2, 4), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5367055,
                                77.3419703,
                                Map.of("leisure", "park"),
                                Map.ofEntries(
                                        entry("osm_type", "W"),
                                        entry("country", "India"),
                                        entry("osm_key", "building"),
                                        entry("city", "Noida"),
                                        entry("street", "Bandh Road"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "yes"),
                                        entry("name", "Cafe 1"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5367055_77.3419703_1791702780",
                                new RouteResult(1432.2, 3), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5373049,
                                77.3417482,
                                Map.of("leisure", "park"),
                                Map.of(
                                        "osm_type", "W",
                                        "country", "India",
                                        "osm_key", "landuse",
                                        "city", "Noida",
                                        "district", "Ralpur Khadar",
                                        "countrycode", "IN",
                                        "osm_value", "commercial",
                                        "name", "HCL",
                                        "state", "Uttar Pradesh",
                                        "type", "locality"
                                ),
                                "st_28.5373049_77.3417482_1791702781",
                                new RouteResult(1261.4, 3), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5394809,
                                77.3389887,
                                Map.of("leisure", "park"),
                                Map.ofEntries(
                                        entry("osm_type", "N"),
                                        entry("country", "India"),
                                        entry("osm_key", "amenity"),
                                        entry("city", "Noida"),
                                        entry("street", "Raipur Khadar Main Road"),
                                        entry("district", "Ralpur Khadar"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "community_centre"),
                                        entry("name", "Urban Health Centre, Raipur"),
                                        entry("postcode", "201303"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5394809_77.3389887_1791702783",
                                new RouteResult(320.1, 1), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5391134,
                                77.3362154,
                                Map.of("leisure", "park"),
                                Map.ofEntries(
                                        entry("osm_type", "W"),
                                        entry("country", "India"),
                                        entry("osm_key", "building"),
                                        entry("city", "Noida"),
                                        entry("street", "KH-13"),
                                        entry("district", "Ralpur Khadar"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "yes"),
                                        entry("name", "Galaxy Stays"),
                                        entry("postcode", "201303"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5391134_77.3362154_1791702787",
                                new RouteResult(966.6, 3), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5458213,
                                77.3343585,
                                Map.of(
                                        "designation", "Park",
                                        "leisure", "park"
                                ),
                                Map.ofEntries(
                                        entry("osm_type", "N"),
                                        entry("country", "India"),
                                        entry("osm_key", "amenity"),
                                        entry("city", "Noida"),
                                        entry("street", "Amity Road"),
                                        entry("district", "Ralpur Khadar"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "cafe"),
                                        entry("name", "Cafe Coffee Day"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5458213_77.3343585_1791702788",
                                new RouteResult(1040.3, 2), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5462918,
                                77.3338565,
                                Map.of(
                                        "designation", "Park",
                                        "leisure", "park"
                                ),
                                Map.ofEntries(
                                        entry("osm_type", "W"),
                                        entry("country", "India"),
                                        entry("osm_key", "building"),
                                        entry("city", "Noida"),
                                        entry("street", "Sector 125, Noida"),
                                        entry("district", "Ralpur Khadar"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "yes"),
                                        entry("name", "E1 Block"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5462918_77.3338565_1791702789",
                                new RouteResult(1116.3, 2), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5460446,
                                77.3341052,
                                Map.of(
                                        "designation", "Park",
                                        "leisure", "park"
                                ),
                                Map.ofEntries(
                                        entry("osm_type", "N"),
                                        entry("country", "India"),
                                        entry("osm_key", "amenity"),
                                        entry("city", "Noida"),
                                        entry("street", "Amity Road"),
                                        entry("district", "Ralpur Khadar"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "cafe"),
                                        entry("name", "Cafe Coffee Day"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5460446_77.3341052_1791702791",
                                new RouteResult(1040.3, 2), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5465238,
                                77.3336134,
                                Map.of(
                                        "designation", "Park",
                                        "leisure", "park"
                                ),
                                Map.ofEntries(
                                        entry("osm_type", "W"),
                                        entry("country", "India"),
                                        entry("osm_key", "building"),
                                        entry("city", "Noida"),
                                        entry("street", "Sector 125, Noida"),
                                        entry("district", "Ralpur Khadar"),
                                        entry("countrycode", "IN"),
                                        entry("osm_value", "yes"),
                                        entry("name", "E1 Block"),
                                        entry("postcode", "201313"),
                                        entry("state", "Uttar Pradesh"),
                                        entry("type", "house")
                                ),
                                "st_28.5465238_77.3336134_1791702792",
                                new RouteResult(1151.4, 2), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5458313,
                                77.3484982,
                                Map.of("leisure", "park"),
                                Map.of(
                                        "osm_type", "W",
                                        "country", "India",
                                        "osm_key", "landuse",
                                        "city", "Noida",
                                        "district", "Sadarpur",
                                        "countrycode", "IN",
                                        "osm_value", "residential",
                                        "name", "UGCC Amber",
                                        "state", "Uttar Pradesh",
                                        "type", "locality"
                                ),
                                "st_28.5458313_77.3484982_1791702795",
                                new RouteResult(3194.7, 6), 0, null
                        ),
                        new SpotAddressesDetails.Place(
                                28.5376652,
                                77.3399524,
                                Map.of("leisure", "park"),
                                Map.of(
                                        "osm_type", "W",
                                        "country", "India",
                                        "osm_key", "landuse",
                                        "city", "Noida",
                                        "district", "Ralpur Khadar",
                                        "countrycode", "IN",
                                        "osm_value", "commercial",
                                        "name", "HCL",
                                        "state", "Uttar Pradesh",
                                        "type", "locality"
                                ),
                                "st_28.5376652_77.3399524_1791702797",
                                new RouteResult(1261.4, 3), 0, null
                        )
                )
        );
    }
}
