package google.maps.dao;

import google.maps.Const;
import google.maps.Point;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static persistence.Common.createConnection;

public class SearchDao {
    public record Search(long id, double lat, double lon) {
    }

    private final Connection connection = createConnection(Const.connectionUrl, true);

    public void createSearches(List<Point> points) throws SQLException {
        String query = "insert into searches (lat, lon) values (%14f, %14f)";
        for (Point p : points) {
            connection.createStatement().execute(String.format(query, p.lat, p.lon));
        }
    }

    public List<Search> getToDoList() throws SQLException {
        String query = "select search_id, lat, lon from searches where done = 0";

        List<Search> result = new ArrayList<>();
        ResultSet r = connection.createStatement().executeQuery(query);
        while (r.next()) {
            result.add(new Search(r.getLong("search_id"), r.getDouble("lat"), r.getDouble("lon")));
        }
        return result;
    }

    public void setDone(long searchId) throws SQLException {
        String query = "update searches set done = 1 where search_id = %d";
        connection.createStatement().execute(String.format(query, searchId));
    }

}
