package google.maps.tileScraper;

import google.maps.MarkerCoordinate;
import google.maps.webview.markers.MarkerTipDetector;
import org.junit.Test;

import java.util.List;

import static google.maps.tileScraper.MarkerTipDetectorTest.getTile;
import static google.maps.tileScraper.TilePixelSequenceExtractor.getPixelSequences;
import static org.junit.Assert.*;

public class TilePixelSequenceExtractorTest {
    @Test
    public void getPixelSequences1() {
        Tile[][] tiles = new Tile[1][1];
        tiles[0][0] = getTile("colors_and_matches_0_0.png");
        List<List<PixelSequence>> sequences = getPixelSequences(tiles);
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

        List<List<PixelSequence>> sequences = getPixelSequences(tiles);
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
        List<MarkerCoordinate> coordinates = MarkerTipDetector.getMarkerTipCoordinates(getPixelSequences(tiles));
        assertEquals(2, coordinates.size());
        assertEquals(60265, coordinates.get(0).x / 100);
        assertEquals(60265, coordinates.get(1).x / 100);
        assertEquals(38940, coordinates.get(0).y / 100);
        assertEquals(38940, coordinates.get(1).y / 100);
    }

}