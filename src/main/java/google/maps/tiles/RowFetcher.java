package google.maps.tiles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static util.HttpUtil.getImageByUrl;

public class RowFetcher {

    private final static String urlPattern = "https://maps.google.com/maps/vt?z=%d&x=%d&y=%d";
    private final static int maxRetries = 3;
    private final static int timeoutPerTry = 5000;

    private final int y;
    private final int minX;
    private final int maxX;
    private final int zoom;

    private final Consumer<Integer> onRowFinished;
    private final BiConsumer<Integer, Tile> onTile;
    private final Consumer<Integer> onAbortingTile;
    private final ExecutorService executorService;

    private boolean isDone;

    public RowFetcher(int y, int minX, int maxX, int zoom, Consumer<Integer> onRowFinished, BiConsumer<Integer, Tile> onTile, Consumer<Integer> onAbortingTile, ExecutorService executorService) {
        this.y = y;
        this.minX = minX;
        this.maxX = maxX;
        this.zoom = zoom;
        this.onRowFinished = onRowFinished;
        this.onTile = onTile;
        this.onAbortingTile = onAbortingTile;
        this.executorService = executorService;
    }

    public void run() {
        executorService.submit(() -> {
            for (int y = minX; y <= maxX; y++) {
                try {
                    BufferedImage image = tryGet(y);
                    onTile.accept(y, new Tile(this.y, y, zoom, image));
                } catch (IOException e) {
                    onAbortingTile.accept(y);
                }
            }
            isDone = true;
            onRowFinished.accept(y);
        });
    }

    private BufferedImage tryGet(int y) throws IOException {
        for (int retries = 0; retries < maxRetries; retries++) {
            try {
                return getImageByUrl(String.format(urlPattern, zoom, this.y, y), timeoutPerTry);
            } catch (IOException e) {
                retries++;
            }
        }
        throw new IOException(String.format("retries exceeded for x:%d y:%d", this.y, y));
    }

    public boolean isDone() {
        return isDone;
    }
}
