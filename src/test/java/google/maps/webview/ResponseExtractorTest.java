package google.maps.webview;

import google.maps.searchapi.PlaceSearchResultItem;
import org.junit.Test;
import google.maps.responseparser.ResponseExtractor;

import java.io.*;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class ResponseExtractorTest {

    @Test
    public void testExtract() {
        InputStream s = this.getClass().getClassLoader().getResourceAsStream("result.json");
        if (s == null) {
            throw new IllegalStateException();
        }
        String data = new BufferedReader(new InputStreamReader(s)).lines().collect(Collectors.joining());
        Optional<PlaceSearchResultItem> ex = ResponseExtractor.extract(data);
        assertTrue(ex.isPresent());
    }

    @Test
    public void hu() throws IOException {

    }
}