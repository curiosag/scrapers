package google.maps.tiles;

import google.maps.Point;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class TileAnalyzer {
    public static int[] colors_wanted = {
            -11234827,
            -8875876,
            -878336,
            -1153188,
            -8812853,
            -15551029};

    public static final int maxX = 255;
    public static final int maxY = 255;
    public static final int zoom = 15;
    private static final int indicatingPixelWidth = 11;

    private final Match[] matchers = initMatchers();
    public final Set<Integer> colorsEncountered = new HashSet<>();
    List<Point> matched = new ArrayList<>();

    public List<Integer> wantedEncountered(){
        return Arrays.stream(colors_wanted).boxed().filter(colorsEncountered::contains).collect(Collectors.toList());
    }

    public TileAnalyzer() {
    }

    public void analyze(Tile t) {
        BufferedImage image = t.getImage();
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                probe(t, x, y, image.getRGB(x, y));
            }
        }
    }

    public List<Point> syphonOff() {
        ArrayList<Point> result = new ArrayList<>(matched);
        matched.clear();
        return result;
    }

    private void probe(Tile t, int x, int y, int rgb) {
        colorsEncountered.add(rgb);
        Match m = matchers[y];
        if (isWanted(rgb)) {
            probeWanted(t, x, y, m, rgb);
        } else {
            probeNotWanted(t, x, y, m);
        }
    }

    private void probeNotWanted(Tile t, int x, int y, Match m) {
        switch (m.state) {
            case INITIAL -> {
            }
            case WANTED_SEEN -> reset(m);
            case POTENTIAL_MATCH -> {
                handleMatch(t, x, y);
                reset(m);
            }
        }
    }

    private void probeWanted(Tile t, int x, int y, Match m, int c) {
        switch (m.state) {
            case INITIAL -> {
                m.state = RecognitionState.WANTED_SEEN;
                m.color = c;
                m.length = 1;
            }
            case WANTED_SEEN -> {
                if (c == m.color) {
                    m.length++;
                    if (m.length == indicatingPixelWidth) {
                        m.state = RecognitionState.POTENTIAL_MATCH;
                    }
                } else {
                    reset(m); // we only search for markers which necessarily are apart
                }
            }
            case POTENTIAL_MATCH -> {
                if (c != m.color) {
                    handleMatch(t, x, y);
                }
                reset(m);
            }
        }
    }

    private void reset(Match m) {
        m.state = RecognitionState.INITIAL;
        m.color = 0;
        m.length = 0;
    }

    private void handleMatch(Tile t, int x, int y) {
        matched.add(t.unproject(x, y, zoom));
    }

    Match[] initMatchers() {
        Match[] result = new Match[maxY + 1];

        for (int i = 0; i < maxY; i++) {
            result[i] = new Match();
        }
        return result;
    }

    public boolean isWanted(int color) {
        for (int j : colors_wanted) {
            if (j == color) {
                return true;
            }
        }
        return false;
    }

}
