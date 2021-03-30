package google.maps.webview.markers;

import google.maps.PixelCoordinate;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static google.maps.IOUtil.loadImage;
import static google.maps.webview.markers.MarkerDetector.getMarkerPixelCoordinates;
import static org.junit.Assert.*;

public class MarkerDetectorTest {

    @Test
    public void getTemples() {
        BufferedImage image = loadImage("/markers.png");
        for(PixelCoordinate c:  getMarkerPixelCoordinates(image))
        {
            System.out.printf("x:%d y:%d r:%5f\n", c.x, c.y, new MarkerDetector(image, (int) c.x, (int) c.y).getWhitePixelRatio(c, image));
        }
    }

    @Test
    public void getTemple() {
        BufferedImage image = loadImage("/3whitePixels.png");
        MarkerDetector m = new MarkerDetector(image, 100, 100);

        float r = m.getWhitePixelRatio(new PixelCoordinate(100, 100), image);
        assertEquals((float)3 / 32, r, 0.001);
        assertEquals(expectedMarkerCenter, surroundingAsString(m.matchMatrix));
    }

    private String surroundingAsString(int[][][] s){
        Map<Integer, Integer> colors = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (int[][] line : s) {
            for (int x = 0; x < line.length; x++) {
                int color = line[x][MarkerDetector.idxColor];
                int colorId = colors.computeIfAbsent(color, c -> colors.size());
                sb.append(colorId);
                sb.append(' ');
            }
            sb.append('\n');
        }
        colors.forEach((key, value) -> System.out.printf("%d ... %d\n", value, key ));
        return sb.toString();
    }

    private final static String expectedMarkerCenter = """
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 1 1 1 1 1 1 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 1 1 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0\s
            0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0\s
            """;
}