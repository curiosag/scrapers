package google.maps.tileScraper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;

import static util.HttpUtil.getImageByUrl;

public class HttpVectorTileFetcher implements SingleTileFetcher {
    private final static String urlPattern = "https://maps.google.com/maps/vt?z=%d&x=%d&y=%d";
    private final static int maxRetries = 3;
    private final static int timeoutPerTry = 5000;
    private Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("5.61.58.211", 4032));

    public Tile fetchTile(int x, int y, int zoom) {
        for (int retries = 0; retries < HttpVectorTileFetcher.maxRetries; retries++) {
            try {
                return new Tile(x, y, zoom, getImageByUrl(String.format(HttpVectorTileFetcher.urlPattern, zoom, x, y), timeoutPerTry, null));
            } catch (IOException e) {
                System.out.println(e.getMessage() + " fetching " + String.format(HttpVectorTileFetcher.urlPattern, zoom, x, y));
                retries++;
            }
        }
        throw new IllegalStateException(String.format("retries exceeded for x:%d y:%d", x, y));
    }



    public static String getResourcePath() {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        return resourceDirectory.toFile().getAbsolutePath();
    }

}
