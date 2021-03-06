package google.maps.webview;

import google.maps.MarkerCoordinate;

import java.util.ArrayList;
import java.util.List;

// get scan results for a page, use iteration order, if element n+1 is near element n, meaning within a x/y range of
// +/- 700 plus meters then drop it. search convex hulls will use a radius of ~750 meters per point found
public class ScanChunkifyer {
    private static final long range = 25 * 7; // 25 pixels per 100 meter at zoom 15,

    public static List<MarkerCoordinate> chunkify(List<MarkerCoordinate> points) {
        List<MarkerCoordinate> result = new ArrayList<>();
        if (points.size() == 0)
            return result;

        result.add(points.get(0));
        for (MarkerCoordinate p : points) {
            if (!withinChunks(p, result)) {
                result.add(p);
            }
        }

        return result;
    }

    private static boolean withinChunks(MarkerCoordinate p, List<MarkerCoordinate> current) {
        return current.stream()
                .anyMatch(i -> getDistance(p, i) <= range );
    }

    public static double getDistance(MarkerCoordinate p, MarkerCoordinate i) {
        return Math.sqrt(Math.pow(i.x - p.x, 2) + Math.pow(i.y - p.y, 2));
    }

}
