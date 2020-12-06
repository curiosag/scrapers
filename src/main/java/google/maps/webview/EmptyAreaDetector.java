package google.maps.webview;

import google.maps.Point;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmptyAreaDetector {
    public static final int halfSideLength = 20;

    public static Optional<Point> getEmptyKlickableLocation(Image image) {
        double w = image.getWidth();
        double h = image.getHeight();
        int delta = 10;
        PixelReader reader = image.getPixelReader();

        for (int i = halfSideLength; i < w - halfSideLength; i = i + delta) {
            for (int j = halfSideLength; j < h - halfSideLength; j = j + delta) {
                if (isEmptyArea(reader, i, j)) {
                    return Optional.of(new Point(i, j));
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isEmptyArea(PixelReader reader, int x, int y) {
        for (int i = x - halfSideLength; i <= x + halfSideLength; i++) {
            for (int j = y - halfSideLength; j <= y + halfSideLength; j++) {
                if (!isEmpty(reader.getColor(i, j))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<Point> getAreaChecked(int x, int y) {
        List<Point> result = new ArrayList<>();
        for (int i = x - halfSideLength; i <= x + halfSideLength; i = i + 2) {
            for (int j = y - halfSideLength; j <= y + halfSideLength; j = j + 2) {
                result.add(new Point(i, j));
            }
        }
        return result;
    }

    private static final double rLand = 248d / 255d;
    private static final double gLand = 249d / 255d;
    private static final double bLand = 250d / 255d;
    private static final double rWater = 156d / 255d;
    private static final double gWater = 192d / 255d;
    private static final double bWater = 249d / 255d;

    private static boolean isEmpty(Color c) {
        return (eq(c.getRed(), rLand) && eq(c.getGreen(), gLand) && eq(c.getBlue(), bLand)) ||
                (eq(c.getRed(), rWater) && eq(c.getGreen(), gWater) && eq(c.getBlue(), bWater));
    }

    private static boolean eq(double d1, double d2) {
        return Math.abs(d1 - d2) < 0.0001;
    }
}

