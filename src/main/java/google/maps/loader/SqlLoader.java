package google.maps.loader;

import google.maps.Const;
import google.maps.extraction.GeoJsonConverter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static persistence.Common.createConnection;

public class SqlLoader {

    private static String path = "/home/ssmertnig/dev/data/temples/qgisstuff/";

    public static void main(String[] args) {
        //load(GeoJsonConverter.toSql("ST_NM", 2, 1, 1, 0, path + "india_state.json"));
        load(GeoJsonConverter.toSql("admin1Name_en", null, path + "Indonesia.json"));

    }

    public static void load(List<String> queries) {
        if (queries == null)
            throw new IllegalArgumentException();

        Connection connection = createConnection(Const.connectionUrl, true);
        String s = "";
        try {
            connection.setAutoCommit(false);
            for (String q : queries) {
                s = q;
                connection.createStatement().executeUpdate(q);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("%d queries executed\n", queries.size());
    }
}
