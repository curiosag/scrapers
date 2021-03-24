package google.maps.dao;

import google.maps.Point;

import java.util.ArrayList;
import java.util.List;

public class GeomUtil {

    public static List<Point> fromGeomString(String geom) {
        List<Point> points = new ArrayList<>();
        String s0 = geom.replace("POLYGON((", "").replace("))", "");
        for (String s : s0.split(",")) {
            String[] lalo = s.split(" ");
            if (lalo.length != 2) {
                throw new IllegalStateException();
            }
            points.add(new Point(Double.parseDouble(lalo[0]), Double.parseDouble(lalo[1])));
        }
        return points;
    }
}
