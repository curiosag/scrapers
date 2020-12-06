package persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static persistence.Common.prepare;

public class Status {

    private final Connection connection;


    private final static String updateSql = "update scrapeStatus set lastPage=? where id=1";

    private PreparedStatement update;

    public Status(Connection connection) {
        this.connection = connection;
        update = prepare(connection, updateSql);
    }

    public int getLastPage() {
        try {
            ResultSet r = connection.createStatement().executeQuery("select lastPage from scrapeStatus where id=1");
            if (!r.next()) {
                throw new IllegalStateException("scrapeStatus is empty");
            }
            return r.getInt("lastPage");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setLastPage(int i) {
        try {
            update.setInt(1, i);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
