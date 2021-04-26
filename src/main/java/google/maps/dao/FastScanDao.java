package google.maps.dao;

import google.maps.CConst;
import google.maps.Point;
import google.maps.tileScraper.TileCoordinate;
import google.maps.tileScraper.TileScrapeWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static persistence.Common.createConnection;

public class FastScanDao implements TileScrapeWriter {

    private final Connection connection = createConnection(CConst.connectionUrl, true);
    private static final String insertSql = "insert into temple.fast_scan_lola_place (geom) values(public.st_geomfromtext('POINT(%.7f %.7f)', 4326));";

    @Override
    public void write(List<Point> points, int savePointX, int savePointY) {
        try {
            connection.setAutoCommit(false);
            for (Point p : points) {
                connection.createStatement().executeUpdate(String.format(insertSql, p.getLongitude(), p.getLatitude()));
            }
            connection.createStatement().executeUpdate(String.format("update temple.fast_scan_savepoint set x=%d, y=%d where id=1", savePointX, savePointY));
            connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<TileCoordinate> getSafePoint() {
        try (ResultSet rs = connection.createStatement().executeQuery("select x, y from fast_scan_savepoint where id =1")) {
            if (!rs.next())
                throw new IllegalStateException();

            return Optional.of(new TileCoordinate(rs.getInt(1), rs.getInt(2)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
