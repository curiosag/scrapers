package google.maps.tiles;

import google.maps.PixelCoordinate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MarkerDetector {

    /*
     *   Trying to find this kind of inverted pyramid like shape. incomplete patterns at edges of tiles without adjacent
     *   tiles (at the very border of the square of tiles) will not be considered.
     *
     *   A line of length n may occur twice max in succession.
     *   A line starting at x of lentth L may be follwod by
     *     an equal line
     *     a line starting at x + 1 with lenght L - 2 (symmetrical case)
     *                        X + 1             L - 1
     *                        X                 L - 1
     *
     *     we limit the number of occurences of successive storeys with equal start or end index to 2
     *
     *      +++++++++
     *       +++++++
     *        +++++
     *         +++   --> topx is the global pixel coordinate of the first "+" in this line
     *                   towWidth is 3
     *                   height is 4
     * */

    private static final int minSeqLength = 3;

    record MarkerPattern(long topX, long topY, int topWidth, int height, int countAlignedStoreys, int color) {
    }

    private static final List<Integer> validPatternBaseSizes = List.of(9, 11);

    static List<PixelCoordinate> getMarkerPixelCoordinates(Tile[][] tiles) {
        Set<PixelCoordinate> result = new HashSet<>();
        List<MarkerPattern> currPatterns = new ArrayList<>();

        List<List<PixelSequence>> sequences = getPixelSequences(tiles);
        List<MarkerPattern> prevPatterns = sequences.get(0).stream()
                .filter(s -> validPatternBaseSizes.contains(s.length()))
                .map(s -> new MarkerPattern(s.x(), s.y(), s.length(), 1, 0, s.color()))
                .collect(Collectors.toList());

        for (int i = 1; i < sequences.size(); i++) {
            for (PixelSequence c : sequences.get(i)) {
                if (colorQualified(c.color())) {
                    prevPatterns.stream()
                            .filter(p -> (p.topX == c.x() || p.topX + 1 == c.x()) && p.topWidth - c.length() <= 2 && p.color() == c.color())
                            .findAny()
                            .ifPresentOrElse(p -> {
                                int incAligned = p.topX == c.x() || c.x() + c.length() == p.topX + p.topWidth ? 1 : 0;
                                if (c.length() > 3) {
                                    MarkerPattern m = new MarkerPattern(c.x(), c.y(), c.length(),
                                            p.height + 1, p.countAlignedStoreys + incAligned, c.color());
                                    currPatterns.add(m);
                                } else {
                                    if (p.countAlignedStoreys + incAligned <= 2) {
                                        result.add(getAveragedCoordinate(c, p));
                                    }
                                }
                            }, () -> {
                                if (validPatternBaseSizes.contains(c.length())) {
                                    currPatterns.add(new MarkerPattern(c.x(), c.y(), c.length(), 1, 0, c.color()));
                                }
                            });
                }
            }
            prevPatterns.clear();
            prevPatterns.addAll(currPatterns);
            currPatterns.clear();
        }

        return new ArrayList<>(result);
    }

    private static boolean colorQualified(int color) {
        int green = ((color >> 8) & 0x000000FF);
        return green <= 160;
    }

    private static PixelCoordinate getAveragedCoordinate(PixelSequence c, MarkerPattern p) {
        return new PixelCoordinate(c.x() + c.length() / 2, c.y() - p.height / 2);
    }


    /**
     * for each line (y-coordinate) of the stitched area sequences of equally colored pixels of length >= 3
     * x/y values of PixelSequence are global pixel values for the zoom level of the tiles
     */
    static List<List<PixelSequence>> getPixelSequences(Tile[][] tiles) {
        int width = tiles.length;
        Tile tileZero = tiles[0][0];

        int globalLeftUpperX = tileZero.getTileX() * 256 - 1;
        int globalLeftUpperY = tileZero.getTileY() * 256 - 1;

        List<List<PixelSequence>> result = new ArrayList<>();

        int globalY = globalLeftUpperY;
        for (int tileY = 0; tileY < width; tileY++) {
            for (int y = 0; y <= 255; y++) {
                List<PixelSequence> lineSequences = new ArrayList<>();
                int currentColor = 0;
                int currentLength = 0;

                globalY++;
                int globalX = globalLeftUpperX;

                for (int tileX = 0; tileX < width; tileX++) {
                    BufferedImage image = tiles[tileY][tileX].getImage();
                    for (int x = 0; x <= 255; x++) {
                        globalX++;
                        int rgb = image.getRGB(x, y);

                        if (rgb == currentColor) {
                            currentLength++;
                        } else {
                            if (currentLength >= minSeqLength) {
                                lineSequences.add(new PixelSequence(globalX - currentLength, globalY, currentLength, currentColor));
                            }
                            currentLength = 1;
                            currentColor = rgb;
                        }
                    }
                }
                result.add(lineSequences);
            }
        }

        return result;
    }
}
