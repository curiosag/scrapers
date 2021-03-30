package google.maps.webview;

import google.maps.Point;
import org.junit.Test;

import static org.junit.Assert.*;

public class UrlCoordExtractorTest {

    @Test
    public void extract() {
        assertEquals(new Point(28.7040592,77.1024902), UrlCoordExtractor.extract("!2d77.1024902!3d28.7040592!").orElse(null));
        assertFalse(UrlCoordExtractor.extract(null).isPresent());
        assertFalse(UrlCoordExtractor.extract("").isPresent());
        assertFalse(UrlCoordExtractor.extract("!2d77.1n024902!3d28.7040592!").isPresent());
    }

}