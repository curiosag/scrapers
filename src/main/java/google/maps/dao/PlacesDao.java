package google.maps.dao;

import google.maps.CConst;
import google.maps.searchapi.PlaceSearchResultItem;
import google.maps.Point;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static persistence.Common.createConnection;

public class PlacesDao {

    private final Connection connection = createConnection(CConst.connectionUrl, true);

    public List<PlaceSearchResultItem> getPlaces(int regionId) {
        String query = """
                select p.id, p.place_id, p.name, p.address, p.global_code, p.vicinity, ST_AsText(p.geom) as geom
                from temple.temple.place_scraped p join temple.temple.region r on r.id = %d and st_within(p.geom, r.geom) 
                """;

        return getQueryResult(String.format(query, regionId));
    }

    public List<PlaceSearchResultItem> getQueryResult(String query) {
        List<PlaceSearchResultItem> result = new ArrayList<>();
        try {
            ResultSet r = connection.createStatement().executeQuery(query);
            while (r.next()) {
                Point p = fromPointGeom(r.getString("geom"));
                result.add(new PlaceSearchResultItem(r.getLong("id"), r.getString("place_id"), p.lat, p.lon, r.getString("name"),
                        r.getString("address"), r.getString("global_code"), r.getString("vicinity")));
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Point fromPointGeom(String geom) {
        String[] coord = geom.replace("POINT(", "").replace(")", "").split(" ");
        return new Point(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
    }

}
