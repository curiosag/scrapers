package google.maps.webview.markers;

import google.maps.IOUtil;
import google.maps.MarkerCoordinate;
import google.maps.tileScraper.Stitching;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static google.maps.IOUtil.loadImage;
import static google.maps.webview.markers.ImagePixelSequenceExtractor.getPixelSequences;

import static google.maps.webview.markers.MarkerTipDetector.getMarkerTipCoordinates;
import static google.maps.webview.markers.RGB.green;
import static org.junit.Assert.assertEquals;

public class MarkerDetectorTest {

    @Test
    public void dandagaunXvfbZ15() {

        List<MarkerCoordinate> markertipsX11 = getMarkerTipCoordinates(getPixelSequences(IOUtil.loadImage("/markers/dandagaunX11Z15.png")));
        List<MarkerCoordinate> markertipsXvfb = getMarkerTipCoordinates(getPixelSequences(IOUtil.loadImage("/markers/dandagaunXvfbZ15.png")));

        List<MarkerCoordinate> nonGreenmarkertipsX11 = removeGreenMarkers(markertipsX11);
        List<MarkerCoordinate> nonGreenmarkertipsXvfb = removeGreenMarkers(markertipsXvfb);

    }

    private List<MarkerCoordinate> removeGreenMarkers(List<MarkerCoordinate> markerMarkerCoordinates) {
        return markerMarkerCoordinates.stream()
                .filter(c -> notTooGreen(c.color))
                .collect(Collectors.toList());
    }

    private static boolean notTooGreen(int color) {
        return green(color) <= 160;
    }

    @Test
    public void tenMarkers_1FalsePositive() {
        BufferedImage image = IOUtil.loadImage("/markers/10Markers.png");
        List<MarkerCoordinate> coordinates = getMarkerTipCoordinates(getPixelSequences(image));
        List<MarkerCoordinate> templeCoordinates = MarkerDetector.getHinduTempleMarkers(image);

        assertEquals(0, templeCoordinates.size());
        assertEquals(11, coordinates.size()); // one false positive, a house in the right angle
    }

    @Test
    public void fourTemples() {
        BufferedImage image = IOUtil.loadImage("/markers/4TemplesZ18.png");
        List<MarkerCoordinate> coordinates = getMarkerTipCoordinates(getPixelSequences(image));
        List<MarkerCoordinate> templeCoordinates = MarkerDetector.getHinduTempleMarkers(image);

        assertEquals(4, templeCoordinates.size());
    }

    @Test
    public void twoTemplesZ15() {
        BufferedImage image = IOUtil.loadImage("/markers/z15.png");
        List<MarkerCoordinate> coordinates = getMarkerTipCoordinates(getPixelSequences(image));
        List<MarkerCoordinate> templeCoordinates = MarkerDetector.getHinduTempleMarkers(image);

        assertEquals(27, coordinates.size()); //some false positives
        assertEquals(2, templeCoordinates.size());
    }

    @Test
    public void analyze_four_tiles() {
        BufferedImage[][] segments = new BufferedImage[2][2]; // rows/columns rows=Y columns = X

        segments[0][0] = IOUtil.loadImage("/tiles/upperleft_23548_15210.png");
        segments[0][1] = IOUtil.loadImage("/tiles/upperright_23549_15210.png");
        segments[1][0] = IOUtil.loadImage("/tiles/lowerleft_23548_15210.png");
        segments[1][1] = IOUtil.loadImage("/tiles/lowerright_23549_15210.png");

        BufferedImage image = Stitching.stitch(segments);
        List<MarkerCoordinate> coordinates = getMarkerTipCoordinates(getPixelSequences(image));
        assertEquals(16, coordinates.size());

        List<MarkerCoordinate> templeCoordinates = MarkerDetector.getHinduTempleMarkers(image);
        assertEquals(2, templeCoordinates.size());
    }

    @Test
    public void getTemples() {
        BufferedImage image = loadImage("/oms.png");
        for (MarkerCoordinate c : MarkerDetector.getMarkerTipCoordinates(image)) {
            System.out.printf("x:%d y:%d r:%5f\n", c.x, c.y, new MarkerDetector(image, (int) c.x, (int) c.y).getWhitePixelRatio(c, image));
        }
    }

    @Test
    public void getWhitePixelRatio() {
        BufferedImage image = loadImage("/markers/3whitePixels.png");
        MarkerDetector m = new MarkerDetector(image, 100, 100);

        float r = m.getWhitePixelRatio(new MarkerCoordinate(100, 100, 0), image);
        assertEquals((float) 3 / 32, r, 0.001);
        assertEquals(expectedMarkerCenter, surroundingAsString(m.matchMatrix));
    }

    private String surroundingAsString(int[][][] s) {
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
        colors.forEach((key, value) -> System.out.printf("%d ... %d\n", value, key));
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