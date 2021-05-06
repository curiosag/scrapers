package google.maps.dao;

import google.maps.CConst;
import google.maps.Point;
import google.maps.ScrapeArea;
import google.maps.webview.scrapejob.ScrapeJob;
import google.maps.webview.scrapejob.ScrapeJobStore;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static google.maps.ScrapeArea.getApexNorth;
import static persistence.Common.createConnection;

public class ScrapeJobDao implements ScrapeJobStore {
    private final Connection connection = createConnection(CConst.connectionUrl, false);
    private final String place_type;

    public ScrapeJobDao(String place_type) {
        this.place_type = place_type;
    }

    @Override
    public Optional<ScrapeJob> getNext() {
        @Language ("SQL")
        String query = """
                select id, public.ST_AsText(area) as area, current_lat, current_lon, started from temple.scrape_job
                where place_type='%s' and finished is null and busy=0 limit 1 for update
                """;
        @Language ("SQL")
        String setBusy = "update temple.scrape_job set busy=1, started=CURRENT_TIMESTAMP where id=";

        try {
            try {
                ResultSet r = connection.createStatement().executeQuery(String.format(query, place_type));
                if (!r.next())
                    return Optional.empty();

                int id = r.getInt("id");
                connection.createStatement().executeUpdate(setBusy + id);

                ScrapeArea area = new ScrapeArea(GeomUtil.fromGeomString(r.getString("area")));

                double clat = r.getDouble("current_lat");
                double clon = r.getDouble("current_lon");
                if (r.getInt("current_lat") == 0 && r.getInt("current_lon") == 0) {
                    Point north = getApexNorth(area.getBoundary());
                    clat = north.getLatitude() - 0.0005;
                    clon = north.getLongitude();
                }

                return Optional.of(new ScrapeJob(id, new Point(clat, clon), area, this));
            } finally {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setProgress(int jobId, Point currentPosition, double pctLatitudeDone) {
        try {
            String updateSql = "update temple.scrape_job set current_lat=%.7f, current_lon=%.7f, pct_done=%.2f, last_update=CURRENT_TIMESTAMP where id=%d";
            connection.createStatement().executeUpdate(
                    String.format(updateSql, currentPosition.getLatitude(), currentPosition.getLongitude(), pctLatitudeDone, jobId));
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseJob(int jobId, boolean done, String error) {
        try {
            String updateSql = done ?
                    "update temple.scrape_job set busy=0, finished=CURRENT_TIMESTAMP, last_update=CURRENT_TIMESTAMP where id=%d" :
                    ("update temple.scrape_job set busy=0, last_update=CURRENT_TIMESTAMP " + getErrorClause(error) + " where id=%d");

            connection.createStatement().executeUpdate(String.format(updateSql, jobId));
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getErrorClause(String error) {
        return String.format(", error='%s' ", error == null ? "" : error);
    }

    public boolean allDone() {
        try {
            String query = "select id from temple.scrape_job where finished is null and place_type='" + place_type + "'";
            ResultSet r = connection.createStatement().executeQuery(query);
            r.next();
            r.getString("id");
            return r.wasNull();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
