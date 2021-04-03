package google.maps.webview.markers;

import google.maps.MarkerCoordinate;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static google.maps.webview.markers.RGB.blue;

public class MarkerDetector {
    static final int idxColor = 0;
    static final int idxVisited = 1;
    static final int matchMatrixWidth = 21;
    private static final int matchMatrixHeight = 29;
    private static final int centerX = matchMatrixWidth / 2 + 1;
    private static final int centerY = matchMatrixHeight - 4; // coordinates from pyramid shape detection should be near the bottom tip

    // matchMatrix is y/x oriented
    private final int matchOffsetX;
    private final int matchOffsetY;
    private final int imageOffsetX;
    private final int imageOffsetY;

    final int[][][] matchMatrix = new int[matchMatrixHeight][matchMatrixWidth][2]; // each: visited/color
    private final BufferedImage image;
    private final int markerColor;

    public MarkerDetector(BufferedImage image, int startX, int startY) {
        this.image = image;
        matchOffsetX = -1 * startX + centerX;
        matchOffsetY = -1 * startY + centerY;
        imageOffsetX = startX - centerX;
        imageOffsetY = startY - centerY;

        markerColor = image.getRGB(startX, startY);
    }

    public static List<MarkerCoordinate> getMarkerTipCoordinates(BufferedImage image) {
        return MarkerTipDetector.getMarkerTipCoordinates(ImagePixelSequenceExtractor.getPixelSequences(image));
    }

    public static List<MarkerCoordinate> getHinduTempleMarkers(BufferedImage image) {
        return getMarkerTipCoordinates(image).stream()
                .filter(c -> {
                    float r = new MarkerDetector(image, (int) c.x, (int) c.y).getWhitePixelRatio(c, image);
                    return r >= 0.03 & r <= 0.07;
                })
                .filter(MarkerDetector::noRestaurant)
                .map(c -> new MarkerCoordinate(c.x, c.y - 7, c.color))
                .collect(Collectors.toList());
    }

    private static boolean noRestaurant(MarkerCoordinate c){
        return blue(c.color) > 150; // restaurants have 0 blue
    }

    float getWhitePixelRatio(MarkerCoordinate c, BufferedImage image) {
        gatherSurrounding((int) c.x, (int) c.y);
        List<XBoundary> xBoundaries = IntStream.range(0, matchMatrix.length)
                .mapToObj(i -> getXBoundary(i, matchMatrix))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        int countBackground = 0;
        int countWhite = 0;

        for (XBoundary b : xBoundaries) {
            int y = b.y + imageOffsetY;
            for (int x = b.xFrom(); x <= b.xTo(); x++) {
                if (((image.getRGB(x + imageOffsetX, y)) & 0x00ffffff) == 0xffffff) {
                    countWhite++;
                } else {
                    countBackground++;
                }
            }
        }

        return (float) countWhite / (countWhite + countBackground);
    }

    record XBoundary(int y, int xFrom, int xTo) {
    }

    private Optional<XBoundary> getXBoundary(int markerY, int[][][] matchMatrix) {
        int min = -1;
        int max = -1;
        int[][] line = matchMatrix[markerY];
        for (int x = 0; x < line.length; x++) {
            if (line[x][idxColor] == markerColor) {
                min = x;
                break;
            }
        }
        for (int x = line.length - 1; x >= 0; x--) {
            if (line[x][idxColor] == markerColor) {
                max = x;
                break;
            }
        }
        return min < 0 ? Optional.empty() : Optional.of(new XBoundary(markerY, min, max));
    }

    // surrounding is x/y oriented
    private static final int[][] surrounding = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

    private void gatherSurrounding(final int imgX, final int imgY) {
        int markerX = imgX + matchOffsetX;
        int markerY = imgY + matchOffsetY;

        if (imgX < 0 || imgY < 0 || imgX >= image.getWidth() || imgY >= image.getHeight()
                || markerX < 0 || markerY < 0 || markerX >= matchMatrixWidth || markerY >= matchMatrixHeight
                || matchMatrix[markerY][markerX][idxVisited] == 1) {
            return;
        }

        int color = image.getRGB(imgX, imgY);

        if (color == markerColor) {
            matchMatrix[markerY][markerX][idxVisited] = 1;
            matchMatrix[markerY][markerX][idxColor] = color;

            for (int[] s : surrounding) {
                gatherSurrounding(imgX + s[0], imgY + s[1]);
            }
        }
    }

}
