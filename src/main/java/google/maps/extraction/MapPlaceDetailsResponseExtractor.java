package google.maps.extraction;

import google.maps.responseparser.Node;
import google.maps.responseparser.ResponseParser;
import google.maps.searchapi.PlaceSearchResultItem;

import java.util.Optional;

public class MapPlaceDetailsResponseExtractor {

    public static Optional<PlaceSearchResultItem> extract(String s) {
        Node n = new ResponseParser(s).parse();

        String placeId = n.getValue(6, 78).orElse(null);
        double lat = Double.parseDouble(n.getValue(4, 0, 2).orElse("0"));
        double lon = Double.parseDouble(n.getValue(4, 0, 1).orElse("0"));
        String adress = n.getValue(6, 2, 0).orElse("null");
        String vicinity = n.getValue(6, 39).orElse("null");
        String name = n.getValue(6, 11).orElse("null");
        String plusCode = n.getValue(6, 183, 2, 1, 0).orElse("null");

        return placeId == null ? Optional.empty() : Optional.of(new PlaceSearchResultItem(placeId, lat, lon, name, plusCode, adress, vicinity));
    }


}
