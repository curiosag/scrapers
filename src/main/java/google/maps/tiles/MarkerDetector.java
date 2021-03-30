package google.maps.tiles;

import google.maps.PixelCoordinate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static google.maps.tiles.TilePixelSequenceExtractor.getPixelSequences;

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

    public static List<PixelCoordinate> getMarkerPixelCoordinates(List<List<PixelSequence>> sequences) {
        Set<PixelCoordinate> result = new HashSet<>();
        List<MarkerPattern> currPatterns = new ArrayList<>();

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
        return new PixelCoordinate(c.x() + c.length() / 2, c.y());
    }

}
