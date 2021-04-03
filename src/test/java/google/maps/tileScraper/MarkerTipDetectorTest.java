package google.maps.tileScraper;

import google.maps.IOUtil;
import google.maps.MarkerCoordinate;
import google.maps.webview.markers.MarkerTipDetector;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

import static google.maps.tileScraper.TilePixelSequenceExtractor.getPixelSequences;
import static org.junit.Assert.assertEquals;

public class MarkerTipDetectorTest {
    private static final String tileFolder = "/tiles/";

    private record TileCoordinate(int x, int y) {
    }

    private enum NumRecognitionState {
        INIT, BETWEEN_X_Y, X, Y, DONE
    }

    @Test
    public void getMarkerPixelCoordinates1() {
        Tile[][] tiles = new Tile[1][1];
        tiles[0][0] = getTile("markermatching_five_0_0.png");
        List<MarkerCoordinate> coordinates = MarkerTipDetector.getMarkerTipCoordinates(getPixelSequences(tiles));
        assertEquals(5, coordinates.size());
    }

    @Test
    public void getMarkerPixelCoordinates1MarkerSpreadOn4Tiles() {
        // one marker spread out over 4 tiles
        Tile[][] tiles = new Tile[2][2]; // rows/columns rows=Y columns = X
        // file names are x/y
        tiles[0][0] = getTile("markermatching_0_0.png");
        tiles[0][1] = getTile("markermatching_1_0.png");
        tiles[1][0] = getTile("markermatching_0_1.png");
        tiles[1][1] = getTile("markermatching_1_1.png");

        List<MarkerCoordinate> coordinates = MarkerTipDetector.getMarkerTipCoordinates(getPixelSequences(tiles));
        assertEquals(1, coordinates.size());
    }


    @Test
    public void analyze_four_tiles() {
        Tile[][] tiles = new Tile[2][2]; // rows/columns rows=Y columns = X
        // file names are y/x
        tiles[0][0] = getTile("upperleft_23548_15210.png");
        tiles[0][1] = getTile("upperright_23549_15210.png");
        tiles[1][0] = getTile("lowerleft_23548_15210.png");
        tiles[1][1] = getTile("lowerright_23549_15210.png");

        List<MarkerCoordinate> coordinates = MarkerTipDetector.getMarkerTipCoordinates(getPixelSequences(tiles))
                .stream().map(c -> new MarkerCoordinate(c.x % 256, c.y % 256, c.color))
                .collect(Collectors.toList());

        assertEquals(16, coordinates.size());
    }


    public static Tile getTile(String fileName) {
        String path = tileFolder + fileName;
        BufferedImage image = IOUtil.loadImage(path);
        TileCoordinate p = getTileCoordinate(fileName);
        return new Tile(p.x(), p.y(), 15, image);
    }

    private static TileCoordinate getTileCoordinate(String fileName) {
        StringBuilder x = new StringBuilder();
        StringBuilder y = new StringBuilder();

        NumRecognitionState state = NumRecognitionState.INIT;
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            boolean isNum = c >= '0' && c <= '9';

            if (isNum)
                switch (state) {
                    case INIT -> state = NumRecognitionState.X;
                    case BETWEEN_X_Y -> state = NumRecognitionState.Y;
                }
            else switch (state) {
                case X -> state = NumRecognitionState.BETWEEN_X_Y;
                case Y -> state = NumRecognitionState.DONE;
            }

            if (isNum) {
                switch (state) {
                    case X -> x.append(c);
                    case Y -> y.append(c);
                }
            }
        }
        int ix = Integer.parseInt(x.toString());
        int iy = Integer.parseInt(y.toString());
        return new TileCoordinate(ix, iy);
    }

    @Test
    public void getGetTileCoordinate() {
        TileCoordinate p = getTileCoordinate("green_tile_23542_15209.png");
        assertEquals(p.x(), 23542);
        assertEquals(p.y(), 15209);
    }
}




