package google.maps.tileScraper;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class FileTileFetcher implements SingleTileFetcher {

    private final String path;

    public FileTileFetcher(String path) {
        this.path = path;
    }

    public Tile fetchTile(int x, int y, int zoom) {
        try {
            return  new Tile(x, y, zoom, ImageIO.read(new File(path + String.format("/tile_%d_%d.png", y, x))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
