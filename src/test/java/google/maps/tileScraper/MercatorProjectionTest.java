package google.maps.tileScraper;

import google.maps.Point;
import org.junit.Test;

import static org.junit.Assert.*;

public class MercatorProjectionTest {

    @Test
    public void unproject() {
        Point m = MercatorProjection.unproject(6026561, 3894060, 15);
        assertTrue(m.lat >= 12.777 && m.lat <= 12.778);
        assertTrue(m.lon >= 78.631 && m.lon <= 78.632);
    }
}