package google.maps;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AreaTest {

    @Test
    public void contains() {
        List<Point> points = List.of(new Point(1,1), new Point(1,2),new Point(2,2),new Point(2,1), new Point(1,1));
        Area r= new Area(points);

        assertTrue(r.contains(new Point(1.1,1.1)));
        assertTrue(r.contains(new Point(1.5,1.5)));
        assertFalse(r.contains(new Point(1,1)));
        assertFalse(r.contains(new Point(0.9,1.9)));
    }
}