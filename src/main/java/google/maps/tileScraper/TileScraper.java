package google.maps.tileScraper;

import google.maps.MarkerCoordinate;
import google.maps.Point;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static google.maps.webview.markers.MarkerTipDetector.getMarkerTipCoordinates;
import static google.maps.tileScraper.TilePixelSequenceExtractor.getPixelSequences;

public class TileScraper {

    private final int squareSize;
    private final static int maxParallelRequests = 1;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int zoom;
    private final MapTilesFetcher fetcher;
    private final TileScrapeWriter writer;
    private final ExecutorService ex;
    private final int totalSquares;

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 5)
            throw new IllegalArgumentException("too few parameters");

        int minX = Integer.parseInt(args[0]);
        int minY = Integer.parseInt(args[1]);
        int maxX = Integer.parseInt(args[2]);
        int maxY = Integer.parseInt(args[3]);
        int zoom = Integer.parseInt(args[4]);
        new TileScraper(minX, minY, maxX, maxY, zoom, 10, new TileScrapeFileWriter("./tilescan.sql")).run();
    }

    public TileScraper(int minX, int minY, int maxX, int maxY, int zoom, int squareSize, TileScrapeWriter writer) {
        System.out.printf("running tilescraper xmin %d ymin %d xmax %d ymax %d zoom %d sqsize %d\n" , minX, minY, maxX, maxY, zoom, squareSize);
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.zoom = zoom;
        this.squareSize = squareSize;
        totalSquares = (((maxX - minX) + 1) / (squareSize - 1)) * (((maxY - minY) + 1) / (squareSize - 1));
        fetcher = new MapTilesFetcher(squareSize, zoom, maxParallelRequests, new HttpTileFetcher());
        this.writer = writer;
        ex = Executors.newSingleThreadExecutor();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    /**
     * fetches squares of tiles of squareSize overlapping by 1 tile at the edges
     * */
    public void run() throws InterruptedException {
        //Optional<TileCoordinate> safePoint = writer.getSafePoint();
        //if (safePoint.isPresent()) {
            //    x = safePoint.get().x();
            //    y = safePoint.get().y();
        // }
        int squareCount = 0;
        int y = minY;

        while (y <= maxY) {
            int x = minX;
            while (x <= maxX) {

                if(true || y > 13718 || (y == 13718 && x > 24166)) {
                    Optional<Tile[][]> square = fetcher.fetchTiles(x, y);
                    if (square.isEmpty())
                        break;

                    final int fx = x;
                    final int fy = y;

                    ex.submit(() -> processTiles(fx, fy, square.get()));
                }
                feedBack(++squareCount, x, y);

                x = x + (squareSize - 1);
            }
            y = y + (squareSize - 1);
        }
        ex.shutdown();
        ex.awaitTermination(10, TimeUnit.DAYS);
    }

    private void feedBack(int squareCount, int x, int y) {
        String feedback = String.format("%d/%d ", squareCount, totalSquares) + (squareCount % 10 == 0 ? "\n": "");
        System.out.print(feedback);
        if(squareCount % 10 == 0)
        {
            System.out.printf("current square tileX/tileY %d/%d\n", x, y);
        }
    }

    private void processTiles(int squareX, int squareY, Tile[][] square) {
        List<List<PixelSequence>> sequences = getPixelSequences(square);
        List<MarkerCoordinate> markerMarkerCoordinates = getMarkerTipCoordinates(sequences);
        List<Point> coordinates = markerMarkerCoordinates.stream()
                .map(i -> MercatorProjection.unproject(i.x, i.y, zoom))
                .collect(Collectors.toList());
        writer.write(coordinates, squareX, squareY);
    }

    @SuppressWarnings("unused")
    private void writeTiles(Tile[][] square) {
        for (Tile[] tiles : square) {
            for (Tile t : tiles) {
                storeImage(t.getImage(), String.format("/home/ssmertnig/temp/tiles/tile_%d_%d.png", t.getTileY(), t.getTileX()));
            }
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
