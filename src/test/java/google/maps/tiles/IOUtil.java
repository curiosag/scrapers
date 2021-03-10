package google.maps.tiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtil {
    static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(getResourcePath() + path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String getResourcePath() {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        return resourceDirectory.toFile().getAbsolutePath();
    }
}
