package google.maps.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import google.maps.searchapi.PlaceSearchResultItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ApiPlaceDetailsExtractor {

    public static List<PlaceSearchResultItem> extract(String json) {
        List<PlaceSearchResultItem> result = new ArrayList<>();
        try {
            ObjectNode content = (ObjectNode) (new ObjectMapper().readTree(json));
            List<JsonNode> results = getElements((ArrayNode) content.get("results"));
            for (JsonNode n : results) {
                String placeId = n.get("place_id").textValue();
                double lat = n.get("geometry").get("location").get("lat").asDouble();
                double lon = n.get("geometry").get("location").get("lng").asDouble();
                String compoundCode = n.get("plus_code").get("compound_code").textValue();
                String vicinity = n.get("vicinity").textValue();
                String name = n.get("name").textValue();
                result.add(new PlaceSearchResultItem(placeId, lat, lon, name, compoundCode, "", vicinity));
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return result;
    }

    private static List<JsonNode> getElements(ObjectNode content) {
        return StreamSupport
                .stream(content.spliterator(), false)
                .collect(Collectors.toList());
    }

    private static List<JsonNode> getElements(ArrayNode content) {
        return StreamSupport
                .stream(content.spliterator(), false)
                .collect(Collectors.toList());
    }
}
