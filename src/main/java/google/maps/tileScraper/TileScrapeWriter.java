package google.maps.tileScraper;

import google.maps.Point;

import java.util.List;
import java.util.Optional;

public interface TileScrapeWriter {
    void write(List<Point> points, int savePointX, int savePointY);
    Optional<TileCoordinate> getSafePoint();
}
