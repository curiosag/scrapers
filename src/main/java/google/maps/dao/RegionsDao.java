package google.maps.dao;

import google.maps.CConst;
import google.maps.Point;
import google.maps.ScrapeArea;
import org.intellij.lang.annotations.Language;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static persistence.Common.createConnection;

public class RegionsDao {
    private final Connection connection = createConnection(CConst.connectionUrl, true);

    public List<List<Point>> getBoundaries(String name) {
        @Language("SQL")
        String query = "select ST_AsText(geom) as geom from temple.region where name = '%s'";

        List<List<Point>> result = new ArrayList<>();
        try {
            ResultSet r = connection.createStatement().executeQuery(String.format(query, name));
            while (r.next()) {
                result.add(GeomUtil.fromGeomString(r.getString("geom")));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Point> getStateBoundaries(String name) {
        String query = "select ST_AsText(ST_ConcaveHull(geom, 0.99)) as geom from temple.region where name = '%s'";

        try {
            ResultSet r = connection.createStatement().executeQuery(String.format(query, name));
            if (r.next()) {
                List<Point> result = GeomUtil.fromGeomString(r.getString("geom"));
                if(r.next())
                {
                    throw new IllegalStateException("ambiguous result for " + name);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Collections.emptyList();
    }

    public ScrapeArea getStateBoundary(String name) {
        return new ScrapeArea(getStateBoundaries(name));
    }

    private Polygon asPolygon(List<Point> points) {
        GeometryFactory geoFactory = new GeometryFactory();
        Coordinate[] coordinates = points.stream()
                .map(p -> new Coordinate(p.lat, p.lon))
                .toArray(Coordinate[]::new);
        return geoFactory.createPolygon(coordinates);
    }

}
