package google.maps.tiles;

import google.maps.Point;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RowAnalyzer {

    private enum RecognitionState {
        INITIAL, WANTED_SEEN, POTENTIAL_MATCH
    }

    private static class Match {
        public Color color;
        RecognitionState state = RecognitionState.INITIAL;
        public int length;

    }

    public static final int maxX = 255;
    public static final int maxY = 255;
    public static final int zoom = 15;
    private static final int indicatingPixelWidth = 11;

    private final Map<Integer, Color> wanted = new HashMap<>();
    Match[] matchers = new Match[maxY + 1];

    private final Consumer<Point> onPoint;

    public RowAnalyzer(Consumer<Point> onPoint) {
        this.onPoint = onPoint;
    }

    public void checkTile(Tile t) {
        BufferedImage image = t.getImage();
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                probe(t, x, y, image.getRGB(x, y));
            }
        }
    }

    private void probe(Tile t, int x, int y, int rgb) {
        Match m = matchers[y];
        Color c = asColor(rgb);
        if (COLORS_WANTED.finance.isWanted(c)) {
            probeWanted(t, x, y, m, c);
        } else {
            probeNotWanted(t, x, y, m);
        }
    }

    private void probeNotWanted(Tile t, int x, int y, Match m) {
        switch (m.state) {
            case INITIAL -> {
            }
            case WANTED_SEEN -> {
                reset(m);
            }
            case POTENTIAL_MATCH -> {
                handleMatch(t, x, y);
                reset(m);
            }
        }
    }

    private void probeWanted(Tile t, int x, int y, Match m, Color c) {
        switch (m.state) {
            case INITIAL -> {
                m.state = RecognitionState.WANTED_SEEN;
                m.color = c;
                m.length = 1;
            }
            case WANTED_SEEN -> {
                if (c.equals(m.color)) {
                    m.length++;
                    if (m.length == indicatingPixelWidth) {
                        m.state = RecognitionState.POTENTIAL_MATCH;
                    }
                } else {
                    m.color = c;
                    m.length = 1;
                }
            }
            case POTENTIAL_MATCH -> {
                if (!c.equals(m.color)) {
                    handleMatch(t, x, y);
                }
                reset(m);
            }
        }
    }

    private void reset(Match m) {
        m.state = RecognitionState.INITIAL;
        m.color = null;
        m.length = 0;
    }

    private Color asColor(int i) {
        return wanted.computeIfAbsent(i, j -> Color.rgb(j >> 16 & 255, j >> 8 & 255, j & 255));
    }

    private void handleMatch(Tile t, int x, int y) {
        onPoint.accept(t.unproject(x, y, zoom));
    }

}
