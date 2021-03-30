package google.maps.tiles;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TilePixelSequenceExtractor {

    private static final int minSeqLength = 3;
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
