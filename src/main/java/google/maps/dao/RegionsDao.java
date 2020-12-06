package google.maps.dao;

import google.maps.Const;
import google.maps.Point;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static persistence.Common.createConnection;

public class RegionsDao {
    private final Connection connection = createConnection(Const.connectionUrl, true);

    public List<List<Point>> getBoundaries(String nameField, String name) {
        String query = "select ST_AsText(geom) as geom from regions where %s = '%s'";

        List<List<Point>> result = new ArrayList<>();
        try {
            ResultSet r = connection.createStatement().executeQuery(String.format(query, nameField, name));
            while (r.next()) {
                result.add(fromGeomString(r.getString("geom")));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Point> getStateBoundaries(String nameField, String name) {
        String query = "select ST_AsText(geom) as geom from regions where %s = '%s'";

        try {
            ResultSet r = connection.createStatement().executeQuery(String.format(query, nameField, name));
            if (r.next()) {
                List<Point> result = fromGeomString(r.getString("geom"));
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

    private List<Point> fromGeomString(String geom) {
        List<Point> points = new ArrayList<>();
        String s0 = geom.replace("POLYGON((", "").replace("))", "");
        for (String s : s0.split(",")) {
            String[] lalo = s.split(" ");
            if (lalo.length != 2) {
                throw new IllegalStateException();
            }
            points.add(new Point(Double.parseDouble(lalo[0]), Double.parseDouble(lalo[1])));
        }
        return points;
    }
}
