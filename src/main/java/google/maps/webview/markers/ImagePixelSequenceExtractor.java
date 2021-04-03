package google.maps.webview.markers;

import google.maps.tileScraper.PixelSequence;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImagePixelSequenceExtractor {
    private static final int minSeqLength = 3;

    /**
     * for each line (y-coordinate) of the image sequences of equally colored pixels of length >= 3
     * x/y values of PixelSequence are global pixel values for the zoom level of the tiles
     *
     * @param image
     */
    public static List<List<PixelSequence>> getPixelSequences(BufferedImage image) {

        List<List<PixelSequence>> result = new ArrayList<>();

        for (int y = 0; y < image.getHeight(); y++) {
            List<PixelSequence> lineSequences = new ArrayList<>();
            int currentColor = 0;
            int currentLength = 0;

            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);

                if (rgb == currentColor) {
                    currentLength++;
                } else {
                    if (currentLength >= minSeqLength) {
                        lineSequences.add(new PixelSequence(x - currentLength, y, currentLength, currentColor));
                    }
                    currentLength = 1;
                    currentColor = rgb;
                }
            }
            result.add(lineSequences);
        }

        return result;
    }

}
