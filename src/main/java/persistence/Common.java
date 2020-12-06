package persistence;

import java.sql.*;

public class Common {

    public static PreparedStatement prepare(Connection connection, String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection createConnection(String jdbcUrl, boolean autocommit) {
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
            preparePackagedDriver();
            Connection result = DriverManager.getConnection(jdbcUrl);
            result.setAutoCommit(autocommit);
            return result;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void preparePackagedDriver() throws ClassNotFoundException {
        Class.forName("org.postgresql.jdbc.PgConnection");
    }

}
