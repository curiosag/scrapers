package google.maps.tiles;

import google.maps.Point;

import java.awt.image.BufferedImage;
import java.util.*;

public class TileAnalyzer {

    public static final int maxX = 255;
    public static final int maxY = 255;
    private static final int indicatingPixelWidth = 11;

    private final PixelSequence[] matchers = initMatchers();
    List<Point> matched = new ArrayList<>();

    public TileAnalyzer() {
    }

    public void analyze(Tile t) {
        BufferedImage image = t.getImage();
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                probe(t, x, y);
            }
        }
    }

    public List<Point> syphonOff() {
        ArrayList<Point> result = new ArrayList<>(matched);
        matched.clear();
        return result;
    }

    /* we want this pattern and don't care if we miss it because it is horizontally split on 2 tiles
     *   +++++++++++ (11)
     *   +++++++++++ (11)
     *    +++++++++  (9)
     *  if they are shorter or longer, ignore them
     **/
    private void probe(Tile t, int x, int y) {
        PixelSequence m = matchers[y];
        int rgb = t.getImage().getRGB(x, y);
        if (m.length == indicatingPixelWidth && m.color != rgb) {
            if (checkPattern(t.getImage(), m.color, x - 1, y)) {
                matched.add(t.unproject(x, y));
                System.out.printf("m: %d, %d%n", x, y);
            }
        }

        if (m.color == 0 && x == 0) {
            m.color = rgb;
            m.length = 1;
        } else if (m.color == rgb) {
            m.length++;
        } else {
            m.color = rgb;
            m.length = 1;
        }
    }

    private boolean checkPattern(BufferedImage image, int rgb, int x, int y) {
        if (y > 255 - 3) {
            return false;
        }
        int from = x - 10;
        int to = x;
        int row = y + 1;

        try {
            for (int i = from; i <= to; i++) {
                if (!(image.getRGB(i, row) == rgb)) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        row = y + 2;
        if (image.getRGB(from, row) == rgb || image.getRGB(to, row) == rgb) {
            return false;
        }
        for (int i = from + 1; i <= to - 1; i++) {
            if (!(image.getRGB(i, row) == rgb)) {
                return false;
            }
        }
        return true;
    }

    PixelSequence[] initMatchers() {
        PixelSequence[] result = new PixelSequence[maxY + 1];

        for (int i = 0; i < maxY; i++) {
            result[i] = new PixelSequence();
        }
        return result;
    }

}
