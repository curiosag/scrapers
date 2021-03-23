package google.maps.tiles;;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Stitching {

    public static void main(String[] args) {
        new Stitching().stitch(23450, 23450 + 6, 15200, 15200 + 6, "/home/ssmertnig/temp/tiles");
    }

    public void stitch(int minx, int maxx, int miny, int maxy, String path) {
        int rows = (maxy - miny) + 1;
        int cols = (maxx - minx) + 1;

        BufferedImage[][] images = new BufferedImage[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                images[y][x] = readImage(String.format(path + "/tile_%d_%d.png", miny + y, minx + x));
            }
        }

        BufferedImage outputImage = new BufferedImage(256 * cols, 256 * rows, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = outputImage.createGraphics();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                g.drawImage(images[i][j], j * 256, i * 256, 256, 256, null);
            }
        }

        storeImage(outputImage, path + "/../stiched.png");
    }

    private BufferedImage readImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage() + " " + path);
        }
    }

    private void storeImage(BufferedImage image, String path) {
        try {
            ImageIO.write(image, "png", new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
