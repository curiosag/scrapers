package google.maps.webview;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import google.maps.Point;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;
import static google.maps.webview.EmptyAreaDetector.isEmptyArea;

public class EmptyScrapeAreaDetectorTest {

    @Test
    public void testIsEmptyArea() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("mapFragment.png");
        Image image = new Image(stream);
        PixelReader r = image.getPixelReader();
        assertTrue(isEmptyArea(r, 120, 80)); // land
        assertTrue(isEmptyArea(r, 170, 200)); // water
        drawTestArea(r, 206, 145);
        assertFalse(isEmptyArea(r, 206, 145));
    }

    public static void main(String[] args) {
        new EmptyScrapeAreaDetectorTest().testIsEmptyArea();
    }

    private void drawTestArea(PixelReader r, int x, int y) {
        String outputPath = "/home/ssmertnig/temp/testArea.png";
        GrayF32 image = UtilImageIO.loadImage("/home/ssmertnig/dev/repo/scrapers/src/test/resources/mapFragment.png", GrayF32.class);
        BufferedImage output = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_BGR);

        ConvertBufferedImage.convertTo(image, output);

        List<Point> area = EmptyAreaDetector.getAreaChecked(x, y);

        area.forEach(p -> output.setRGB((int) p.lat, (int) p.lon, 0));

        UtilImageIO.saveImage(output, outputPath);
    }
}