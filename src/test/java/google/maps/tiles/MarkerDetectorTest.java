package google.maps.tiles;

import google.maps.IOUtil;
import google.maps.PixelCoordinate;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class MarkerDetectorTest {
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
        List<PixelCoordinate> coordinates = MarkerDetector.getMarkerPixelCoordinates(tiles);
        assertEquals(5, coordinates.size());
    }

    @Test
    public void getMarkerPixelCoordinates4() {
        // one marker spread out over 4 tiles
        Tile[][] tiles = new Tile[2][2]; // rows/columns rows=Y columns = X
        // file names are x/y
        tiles[0][0] = getTile("markermatching_0_0.png");
        tiles[0][1] = getTile("markermatching_1_0.png");
        tiles[1][0] = getTile("markermatching_0_1.png");
        tiles[1][1] = getTile("markermatching_1_1.png");

        List<PixelCoordinate> coordinates = MarkerDetector.getMarkerPixelCoordinates(tiles);
        assertEquals(1, coordinates.size());
    }

    @Test
    public void getPixelSequences1() {
        Tile[][] tiles = new Tile[1][1];
        tiles[0][0] = getTile("colors_and_matches_0_0.png");
        List<List<PixelSequence>> sequences = MarkerDetector.getPixelSequences(tiles);
        assertEquals(4, sequences.get(0).size());
        assertEquals(0, sequences.get(1).size());
        assertEquals(1, sequences.get(2).size());

        assertEquals(new PixelSequence(6, 0, 4, -1), sequences.get(0).get(0));
        assertEquals(new PixelSequence(10, 0, 11, -1), sequences.get(0).get(1));
        assertEquals(new PixelSequence(21, 0, 11, -1), sequences.get(0).get(2));
        assertEquals(new PixelSequence(33, 0, 11, -1), sequences.get(0).get(3));
        assertEquals(new PixelSequence(0, 2, 21, -1), sequences.get(2).get(0));
    }

    @Test
    public void getPixelSequences4() {
        Tile[][] tiles = new Tile[2][2]; // rows/columns rows=Y columns = X
        // file names are y/x
        tiles[0][0] = getTile("pixelsequence_0_0.png");
        tiles[0][1] = getTile("pixelsequence_1_0.png");
        tiles[1][0] = getTile("pixelsequence_0_1.png");
        tiles[1][1] = getTile("pixelsequence_1_1.png");

        List<List<PixelSequence>> sequences = MarkerDetector.getPixelSequences(tiles);
        assertEquals(3, sequences.get(0).size());
        assertEquals(0, sequences.get(1).size());
        assertEquals(2, sequences.get(2).size());
        assertEquals(3, sequences.get(256).size());
        assertEquals(0, sequences.get(257).size());
        assertEquals(2, sequences.get(258).size());

        assertEquals(new PixelSequence(0, 0, 245, 0), sequences.get(0).get(0));
        assertEquals(new PixelSequence(251, 0, 4, 0), sequences.get(0).get(1));
        assertEquals(new PixelSequence(255, 0, 11, 0), sequences.get(0).get(2));
        assertEquals(new PixelSequence(0, 2, 245, 0), sequences.get(2).get(0));
        assertEquals(new PixelSequence(245, 2, 21, 0), sequences.get(2).get(1));

        assertEquals(new PixelSequence(0, 256, 245, 0), sequences.get(256).get(0));
        assertEquals(new PixelSequence(251, 256, 4, 0), sequences.get(256).get(1));
        assertEquals(new PixelSequence(255, 256, 11, 0), sequences.get(256).get(2));
        assertEquals(new PixelSequence(0, 258, 245, 0), sequences.get(258).get(0));
        assertEquals(new PixelSequence(245, 258, 21, 0), sequences.get(258).get(1));
    }

    @Test
    public void analyze_cultural_and_religious() {
        Tile[][] tiles = new Tile[1][1];
        tiles[0][0] = getTile("m_tile_23541_15211.png");
        List<PixelCoordinate> coordinates = MarkerDetector.getMarkerPixelCoordinates(tiles);
        assertEquals(2, coordinates.size());
        assertEquals(60265, coordinates.get(0).x / 100);
        assertEquals(60265, coordinates.get(1).x / 100);
        assertEquals(38940, coordinates.get(0).y / 100);
        assertEquals(38940, coordinates.get(1).y / 100);
    }

    @Test
    public void analyze_four() {
        Tile[][] tiles = new Tile[2][2]; // rows/columns rows=Y columns = X
        // file names are y/x
        tiles[0][0] = getTile("upperleft_23548_15210.png");
        tiles[0][1] = getTile("upperright_23549_15210.png");
        tiles[1][0] = getTile("lowerleft_23548_15210.png");
        tiles[1][1] = getTile("lowerright_23549_15210.png");

        List<PixelCoordinate> coordinates = MarkerDetector.getMarkerPixelCoordinates(tiles)
                .stream().map(c -> new PixelCoordinate(c.x % 256, c.y % 256))
                .collect(Collectors.toList());

        assertEquals(16, coordinates.size());
    }

    private Tile getTile(String fileName) {
        String path = tileFolder + fileName;
        BufferedImage image = IOUtil.loadImage(path);
        TileCoordinate p = getTileCoordinate(fileName);
        return new Tile(p.x(), p.y(), 15, image);
    }

    private TileCoordinate getTileCoordinate(String fileName) {
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




