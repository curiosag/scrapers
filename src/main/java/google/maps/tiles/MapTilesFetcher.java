package google.maps.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MapTilesFetcher {

    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private int zoom = 15;
    private int processMaxParallelRows = 1;

    private int highWaterMarkY;
    private final Consumer<Integer> onRowStarting;
    private final Consumer<Integer> onRowFinished;
    private final BiConsumer<Integer, Tile> onTile;
    private final Runnable onDone;
    private final Runnable onAbort;

    ExecutorService executorService = Executors.newFixedThreadPool(processMaxParallelRows);

    Map<Integer, RowFetcher> rowFetchers = new HashMap<>();

    public void setProcessMaxParallelRows(int processMaxParallelRows) {
        this.processMaxParallelRows = processMaxParallelRows;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public MapTilesFetcher(int minX, int maxX, int minY, int maxY, Consumer<Integer> onRowStarting, Consumer<Integer> onRowFinished, BiConsumer<Integer, Tile> onTile, Runnable onDone, Runnable onAbort) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        highWaterMarkY = minY;
        this.onRowStarting = onRowStarting;
        this.onRowFinished = onRowFinished;
        this.onTile = onTile;
        this.onDone = onDone;
        this.onAbort = onAbort;
    }

    public void run() {
        for (int y = minY; y <= maxY && rowFetchers.size() < processMaxParallelRows; y++) {
            rowFetchers.put(y, createRow(y));
            highWaterMarkY = y;
        }
        new ArrayList<>(rowFetchers.values()).forEach(RowFetcher::run);
    }

    private RowFetcher createRow(int y) {
       return new RowFetcher(y, minX, maxX, zoom, this::rowFinished, onTile, this::onAborting, executorService);
    }

    private void onAborting(Integer i) {
        executorService.shutdown();
        onAbort.run();
    }

    private synchronized void rowFinished(Integer i) {
        onRowFinished.accept(i);
        rowFetchers.remove(i);
        highWaterMarkY++;
        if (highWaterMarkY <= maxY) {
            onRowStarting.accept(highWaterMarkY);

            RowFetcher r = createRow(highWaterMarkY);
            rowFetchers.put(highWaterMarkY, r);
            r.run();

        } else {
            if (rowFetchers.isEmpty()) {
                onDone.run();
            }
        }
    }

}
