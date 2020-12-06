package persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static persistence.Common.prepare;

public class Slice {
    private final Connection connection;

    private static final String insertSliceSQL =
            "insert into scrapeSlice (" +
                    "dtFrom," +
                    "dtTo," +
                    "pagesDone) values(?, ?, 0)";


    private static final String selectSliceSql =
            "select id, dtFrom, dtTo, pagesDone " +
                    "from scrapeSlice " +
                    "where sliceDone = 0 " +
                    "order by id";

    private PreparedStatement updateSlice;

    public Slice(Connection connection) {
        this.connection = connection;
        updateSlice = prepare(connection, "update scrapeSlice set pagesDone=?, sliceDone=? where id=?;");
    }

    public void updateSlice(int id, int pagesDone, boolean sliceDone) {
        try {
            updateSlice.setInt(1, pagesDone);
            updateSlice.setInt(2, sliceDone ? 1 : 0);
            updateSlice.setInt(3, id);
            updateSlice.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ScrapeSlice> getSlicesToDo() {
        List<ScrapeSlice> result = new ArrayList<>();

        try {
            ResultSet r = connection.createStatement().executeQuery(selectSliceSql);

            while (r.next()) {
                ScrapeSlice slice = new ScrapeSlice(r.getInt("id"))
                        .setDtFrom(r.getString("dtFrom"))
                        .setDtTo(r.getString("dtTo"))
                        .setPagesDone(r.getInt("pagesDone"))
                        .setSliceDone(false);
                result.add(slice);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    public void initializeTmdSlizes(LocalDate from, LocalDate to, int thickness) {
        if (from.isAfter(to) || thickness < 1)
            throw new IllegalArgumentException();
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        PreparedStatement stmt = prepare(connection, insertSliceSQL);

        LocalDate intervalBegin = from;
        LocalDate intervalEnd = from.minusDays(1);
        while (!intervalEnd.equals(to)) {
            intervalEnd = intervalBegin.plusDays(thickness - 1);
            if (intervalEnd.isAfter(to)) {
                intervalEnd = to;
            }

            try {
                stmt.setString(1, toQueryDate(intervalBegin));
                stmt.setString(2, toQueryDate(intervalEnd));
                stmt.executeUpdate();

                String q = "insert into scrapeSlice (" +
                        "dtFrom," +
                        "dtTo," +
                        "pagesDone) values('%s', '%s', 0);";

                System.out.println(String.format(q, toQueryDate(intervalBegin), toQueryDate(intervalEnd)));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            intervalBegin = intervalEnd.plusDays(1);
        }

    }

    private final static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String toQueryDate(LocalDate d) {
        return d.format(fmt);
    }
}
