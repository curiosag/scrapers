package google.maps.dao;

import google.maps.Point;

import java.util.ArrayList;
import java.util.List;

public class GeomUtil {

    public static List<Point> fromGeomString(String geom) {
        List<Point> points = new ArrayList<>();
        String s0 = geom.replace("POLYGON((", "").replace("))", "");
        for (String s : s0.split(",")) {
            String[] lola = s.split(" ");
            if (lola.length != 2) {
                throw new IllegalStateException();
            }
            try {
                points.add(new Point(Double.parseDouble(lola[1]), Double.parseDouble(lola[0])));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return points;
    }
}
