package google.maps.tileScraper;

import google.maps.IOUtil;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class PngColors {

    @Test
    public void identifyColors() {
        BufferedImage image = IOUtil.loadImage("/tiles/singleOm_13542_23779.png");

        Map<Integer, Integer> frequencies = new HashMap<>();

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                Integer f = frequencies.get(rgb);
                if (f == null) {
                    frequencies.put(rgb, 1);
                } else {
                    frequencies.put(rgb, f + 1);
                }
            }
        }

        frequencies.forEach((color, freq) -> System.out.printf("%s  freq:%d\n", decomposeRgb(color), freq));
    }

    public String decomposeRgb(int rgb) {
        int red = (rgb >> 16) & 0x000000FF;
        int green = (rgb >> 8) & 0x000000FF;
        int blue = (rgb) & 0x000000FF;

        return String.format("R:%d G:%d B:%d", red, green, blue);
    }
}
