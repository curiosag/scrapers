package google.maps.webview;

import google.maps.PixelCoordinate;

import java.util.ArrayList;
import java.util.List;

// get scan results for a page, use iteration order, if element n+1 is near element n, meaning within a x/y range of
// +/y 500 plus a bit meters then drop it. search convex hulls will use a radius of ~750 meters per point found
public class ScanChunkifyer {
    private static final long range = 25 * 5; // 25 pixels per 100 meter at zoom 15,  I'm using squares, so reduce the area a bit

    public static List<PixelCoordinate> chunkify(List<PixelCoordinate> points) {
        List<PixelCoordinate> result = new ArrayList<>();
        if (points.size() == 0)
            return result;

        result.add(points.get(0));
        for (PixelCoordinate p : points) {
            System.out.println("checking point " + p);
            if (!withinChunks(p, result)) {
                result.add(p);
            } else
                System.out.println("skipped point " + p);
        }

        return result;
    }

    private static boolean withinChunks(PixelCoordinate p, List<PixelCoordinate> current) {
        return current.stream()
                .anyMatch(i -> i.x - range <= p.x && i.x + range >= p.x && i.y - range <= p.y && i.y + range >= i.y);
    }

}
