package google.maps.webview;

import google.maps.IOUtil;
import google.maps.extraction.MapPlaceDetailsResponseExtractor;
import google.maps.responseparser.JsonResponse;
import google.maps.searchapi.PlaceSearchResultItem;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static google.maps.extraction.MapPlaceDetailsResponseExtractor.stripCrapFromManualSearchResult;
import static org.junit.Assert.assertTrue;

public class MapPlaceDetailsResponseExtractorTest {



    @Test
    public void testExtractMapSearchResult() {
String la = "PlaceSearchResultItem{id=null, placeId='ChIJL_o1-SAUvjkRf6mwFzs29LY', lat=28.816846299999998, lon=83.87177299999999, name='Shree Muktinath Temple, Nepal', plus_compound_code='', adress='Muktinath-Chumig Gyatsa Pilgrimage Site, Muktinath 33100, Nepal', vicinity='null', resultType='SearchResult.TYPE_HINDU_TEMPLE'}";
        //String data = (IOUtil.getString("responses/UISearch/q3.json"));
        String data = IOUtil.getString("./responses/markerklicks/SaligramTemple.json").substring(4);
        //System.out.println(JsonResponse.prettyPrint(stripCrapFromManualSearchResult(data)));
        System.out.println(JsonResponse.prettyPrint(data));


        List<PlaceSearchResultItem> responses = MapPlaceDetailsResponseExtractor.extractFromMapSearchResult(data);
        responses.forEach(System.out::println);

    }

    @Test
    public void testExtract() {
        String data = IOUtil.getString("responses/markerklicks/ShreeMuktinathTemple.json");

        Optional<PlaceSearchResultItem> ex = MapPlaceDetailsResponseExtractor.extractFromMapKlickResult(data);
        assertTrue(ex.isPresent());
    }

}