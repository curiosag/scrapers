package google.maps.webview.datasink;

import google.maps.Const;
import google.maps.searchapi.PlaceSearchResultItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static persistence.Common.createConnection;

public class PlaceDao {
    private final Connection connection = createConnection(Const.connectionUrl, true);
    private final PreparedStatement qNext;
    private final PreparedStatement qAdd;

    public static void main(String[] args) {
        PlaceDao dao = new PlaceDao();
        dao.add(new PlaceSearchResultItem(3L, "plcid", 12.3f, 34.5f, "name", "global_code", "addr", "vicinity"));
    }

    public PlaceDao() {
        try {
            qNext = connection.prepareStatement("select nextval('temple.place_scraped_id_seq'::regclass) as next");
            qAdd = connection.prepareStatement("insert into staging_place_scraped(id, place_id, name, global_code, vicinity, lat, lon) values(?,?,?,?,?,?,?)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void add(PlaceSearchResultItem item) {
        try {
            qAdd.setInt(1, (int)((long)item.id));
            qAdd.setString(2, item.placeId);
            qAdd.setString(3, item.name);
            qAdd.setString(4, item.global_code);
            qAdd.setString(5, item.vicinity);
            qAdd.setDouble(6, item.lat);
            qAdd.setDouble(7, item.lon);
            qAdd.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized int getNextId() {
        try {
            ResultSet r = qNext.executeQuery();
            r.next();
            return r.getInt("next");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
