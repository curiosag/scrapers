package google.maps.tileScraper;

import google.maps.Point;
import google.maps.ScrapeArea;
import google.maps.dao.RegionsDao;
import org.locationtech.jts.geom.Envelope;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TilesForArea {

    public static void main(String[] args) {
        String targetPath = "/home/ssmertnig/temp/tiles.csv";
        ScrapeArea bounds = new RegionsDao().getStateBoundary("Bali");
        Envelope e = new Envelope();
        Arrays.stream(bounds.geo.getCoordinates()).forEach(e::expandToInclude);

        Set<TileCoordinate> tiles = new HashSet<>();

        double luLat = e.getMaxX();
        double luLon = e.getMinY();
        double rlLat = e.getMinX();
        double rlLon = e.getMaxY();

        double delta = 0.005;
        double currLat = luLat;

        try (FileWriter w = new FileWriter(targetPath, false)) {
            while (currLat >= rlLat) {
                double currLon = luLon;
                while (currLon <= rlLon) {
                    if (bounds.contains(new Point(currLat, currLon))) {
                        TileCoordinate t = getTileCoordinate(currLon, currLat, 15);
                        tiles.add(t);
                        if (tiles.size() % 500 == 0) {
                            System.out.printf("%d %.3f/%.3f %d %d\n", tiles.size(), currLon, currLat,  t.x, t.y);
                        }
                    }
                    currLon = currLon + delta;
                }
                currLat = currLat - delta;
            }
            tiles.forEach(i -> {
                try {
                    w.write(String.format("%d, %d\n", i.x, i.y));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
        } catch (IOException ioException) {
            throw new RuntimeException();
        }

        System.out.println(tiles.size());
    }

    private final static int TILE_SIZE = 256;

    private static TileCoordinate getTileCoordinate(double lng, double lat, int zoom) {
        var scale = 1 << zoom;
        var siny = Math.sin((lat * Math.PI) / 180);
        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        siny = Math.min(Math.max(siny, -0.9999), 0.9999);
        var worldX = TILE_SIZE * (0.5 + lng / 360);
        var worldY = TILE_SIZE * (0.5 - Math.log((1 + siny) / (1 - siny)) / (4 * Math.PI));

        double tilex = Math.floor((worldX * scale) / TILE_SIZE);
        double tiley = Math.floor((worldY * scale) / TILE_SIZE);

        return new TileCoordinate((int) tilex, (int) tiley, zoom);
    }

}
