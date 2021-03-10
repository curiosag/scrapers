package google.maps.tiles;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.HttpUtil.getImageByUrl;

public class TileAnalyzerTest {

    @Test
    public void listColors() {
        BufferedImage image = IOUtil.loadImage("/tiles/colors_and_matches.png");
        for (int x = 0; x < COLORS_WANTED.values().length; x++) {
            System.out.println(x + ": " + image.getRGB(x, 0));
        }
    }

    @Test
    public void analyze_online() throws IOException {
        String url = "https://maps.google.com/maps/vt?z=15&x=%d&y=%d";

        BufferedImage image = getImageByUrl(String.format(url, 23542, 15218), 5000);
        Tile tile = new Tile(23542, 15218 , 15, image);

        TileAnalyzer analyzer = new TileAnalyzer();
        analyzer.analyze(tile);
        assertEquals(5, analyzer.matched.size());
        analyzer.matched.forEach(m -> {
            assertTrue(m.lat >= 12.69534 && m.lat <= 12.70407);
            assertTrue(m.lon >= 78.64368 && m.lon <= 78.64675);
        });
    }

    @Test
    public void analyze_two() {
        TileAnalyzer analyzer = new TileAnalyzer();

        analyzer.analyze(getTile("/tiles/left_23548_15210.png", 23548, 15210));
        analyzer.analyze(getTile("/tiles/right_23549_15210.png", 23549, 15210));
        assertEquals(15, analyzer.matched.size());
    }

    @Test
    public void analyze_cultural_and_religious() {
        TileAnalyzer analyzer = new TileAnalyzer();

        analyzer.analyze(getTile("/tiles/tile_23541_15211.png", 23541, 15211));

        assertEquals(4, analyzer.matched.size());
    }

    private Tile getTile(String path, int tileX, int tileY) {
        BufferedImage image = IOUtil.loadImage(path);
        return new Tile(tileX, tileY, 15, image);
    }


}