package google.maps;

import google.maps.webview.AreaExceeded;
import org.checkerframework.checker.signature.qual.ArrayWithoutPackage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ScrapeAreaTest {
    private static final List<Point> points = List.of(new Point(1,1), new Point(1,2),new Point(2,2),new Point(2,1), new Point(1,1));
    private static final ScrapeArea r= new ScrapeArea(points);

    @Test
    public void exceeding() {
        float epsilon = 0.00001f;
        assertEquals(AreaExceeded.NO, r.exceeded(new Point(1.1,1.1)));
        assertEquals(AreaExceeded.RIGHT, r.exceeded(new Point(1.1,21)));
        assertEquals(AreaExceeded.LEFT, r.exceeded(new Point(1.1,0)));
        assertEquals(AreaExceeded.SOUTH, r.exceeded(new Point(0,1.1)));
        assertEquals(AreaExceeded.NORTH, r.exceeded(new Point(10,1.1)));
    }

    @Test
    public void contains() {

        assertTrue(r.contains(new Point(1.1,1.1)));
        assertTrue(r.contains(new Point(1.5,1.5)));
        assertFalse(r.contains(new Point(1,1)));
        assertFalse(r.contains(new Point(0.9,1.9)));
    }
}