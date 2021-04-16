package google.maps.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import google.maps.responseparser.JsonResponse;
import google.maps.searchapi.PlaceSearchResultItem;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static google.maps.responseparser.JsonResponse.getChildren;

public class MapPlaceDetailsResponseExtractor {

    public static Optional<PlaceSearchResultItem> extractFromMapKlickResult(String s) {
        JsonResponse n = new JsonResponse(s.substring(4));

        String placeId = n.getValue(6, 78).orElse(n.getValue(6, 0).orElse(null));
        double lat = Double.parseDouble(n.getValue(4, 0, 2).orElse("0"));
        double lon = Double.parseDouble(n.getValue(4, 0, 1).orElse("0"));
        String adress = n.getValue(6, 2, 0).orElse("null");
        adress = adress + n.getValue(6, 2, 1).map(v -> ", " + v).orElse("");
        adress = adress + n.getValue(6, 2, 2).map(v -> ", " + v).orElse("");
        String vicinity = n.getValue(6, 39).orElse("null");
        String name = n.getValue(6, 11).orElse("null");
        String plusCode = n.getValue(6, 183, 2, 1, 0).orElse("null");

        return placeId == null ? Optional.empty() : Optional.of(new PlaceSearchResultItem(placeId, lat, lon, name, plusCode, adress, vicinity));
    }

    public static List<PlaceSearchResultItem> extractFromMapSearchResult(String result) {

        String data = stripCrapFromManualSearchResult(result);

        JsonResponse r = new JsonResponse(data);

        JsonNode nodes = r.getNode(0, 1).get();
        List<JsonNode> v = getChildren(nodes).subList(1, nodes.size() - 1);

        return v.stream().map(MapPlaceDetailsResponseExtractor::getContent)
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }

    public static String stripCrapFromManualSearchResult(String result) {
        String data = result.replace("\\\\", "\\")
                .replace("\\n", "")
                .replace("\\\"", "\"")
                .replace("\\/", "/");
        data = data.substring(data.indexOf(")]}") + 4);
        data = data.substring(0, data.indexOf("\",\"e\":"));
        return data;
    }

    private static Optional<PlaceSearchResultItem> getContent(JsonNode node) {
        return JsonResponse.getNode(node, 14).map(n -> {
            String name = JsonResponse.getValue(n, 11).orElse("");
            double lat = JsonResponse.getValue(n, 9, 2).map(Double::parseDouble).orElse(0d);
            double lon = JsonResponse.getValue(n, 9, 3).map(Double::parseDouble).orElse(0d);
            String address = JsonResponse.getValue(n, 18).orElse("").replace(name + ",", "").trim();
            String vicinity = JsonResponse.getValue(n, 14).orElse("");
            String placeID = JsonResponse.getValue(n, 78).orElse("");
            PlaceSearchResultItem result = new PlaceSearchResultItem(placeID, lat, lon, name, "", address, vicinity);
            result.resultType = JsonResponse.getValue(n, 88, 1).orElse("");
            return result;
        });
    }

}
