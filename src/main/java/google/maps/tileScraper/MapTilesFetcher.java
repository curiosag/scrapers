package google.maps.tileScraper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapTilesFetcher {
    final Map<Integer, Map<Integer, Tile>> tileCache = new ConcurrentHashMap<>();
    private final SingleTileFetcher singleTileFetcher;

    private final ExecutorService executorService;

    private final int squareSize;
    private final int zoom;

    private final AtomicBoolean aborted = new AtomicBoolean(false);

    private int startX;
    private int startY;

    public MapTilesFetcher(int squareSize, int zoom, int maxParallelRequests, SingleTileFetcher singleTileFetcher) {
        this.squareSize = squareSize;
        this.zoom = zoom;
        executorService = Executors.newFixedThreadPool(maxParallelRequests);
        this.singleTileFetcher = singleTileFetcher;
    }

    public Optional<Tile[][]> fetchTiles(int startX, int startY) {

        final Tile[][] result = new Tile[squareSize][squareSize];
        this.startX = startX;
        this.startY = startY;
        List<Callable<Tile>> requests = new ArrayList<>();
        for (int y = startY; y < startY + squareSize; y++) {
            for (int x = startX; x < startX + +squareSize; x++) {
                if (aborted.get()) {
                    break;
                }
                final int currX = x;
                final int currY = y;
                requests.add(() -> {
                    try {
                        result[currY - startY][currX - startX] = tryGet(currX, currY, zoom);
                    } catch (Exception e) {
                        hdlException(currX, currY, e);
                    }
                    return null;
                });
            }
        }

        try {
            executorService.invokeAll(requests).forEach(c -> {
                try {
                    c.get();
                } catch (InterruptedException | ExecutionException e) {
                    hdlException(0, 0, e);
                }
            });
        } catch (InterruptedException e) {
            hdlException(0, 0, e);
        }
        return aborted.get() ? Optional.empty() : Optional.of(result);
    }

    private void hdlException(int currX, int currY, Exception e) {
        aborted.set(true);
        System.out.println("Exception fetching tile x:" + currX + " y:" + currY);
        e.printStackTrace();
    }

    private Tile tryGet(int x, int y, int zoom) {
        Optional<Tile> maybeTile = getCached(x, y);
        if (maybeTile.isPresent())
            return maybeTile.get();
        else {
            Tile t = singleTileFetcher.fetchTile(x, y, zoom);
            cacheTile(t);
            return t;
        }
    }

    private void cacheTile(Tile t) {
        int expectedAccess = calculateExpectedAccesses(t.getTileX(), t.getTileY());
        if (expectedAccess > 0) {
            t.setExpectedAccesses(expectedAccess);
            Map<Integer, Tile> yMap = tileCache.computeIfAbsent(t.getTileX(), x -> new ConcurrentHashMap<>());
            yMap.put(t.getTileY(), t);
        }
    }

    private Optional<Tile> getCached(int x, int y) {
        Map<Integer, Tile> yMap = tileCache.get(x);
        if (yMap != null) {
            Optional<Tile> result = Optional.ofNullable(yMap.get(y));
            result.ifPresent(r -> {
                        r.countAccess();
                        if (r.getAccessCount() == r.getExpectedAccesses()) {
                            yMap.remove(y);
                            if (yMap.isEmpty()) {
                                tileCache.remove(x);
                            }
                        }
                    }
            );
            return result;
        }
        return Optional.empty();
    }

    /**
     * simplified incomplete logic, for the very right and very bottom square received cached items won't be cleared ever
     * <p>
     * 0001
     * 0001
     * 0001
     * 1113
     */

    int calculateExpectedAccesses(int x, int y) {
        int mx = startX + (squareSize - 1);
        int my = startY + (squareSize - 1);
        if (x < mx && y < my) {
            return 0;
        }
        if (x == mx && y == my) {
            return 3;
        }
        return 1;
    }
}