package google.maps.dao;

import google.maps.Const;
import google.maps.Point;
import google.maps.webview.scrapejob.ScrapeJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.Assert.*;
import static persistence.Common.createConnection;

public class ScrapeJobDaoTest {

    private final Connection connection = createConnection(Const.connectionUrl, true);

    @Before
    public void setUp() throws Exception {
        if (exists("select count(1) as count from temple.scrape_job"))
            throw new IllegalStateException("can't perform ScrapeJobDaoTest on non-empty table");
    }

    private boolean exists(String sql) {
        try {
            ResultSet bla = connection.createStatement().executeQuery(sql);
            assertTrue(bla.next());
            return bla.getInt("count") > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void execute(String update) throws SQLException {
        connection.createStatement().executeUpdate(update);
    }

    @After
    public void tearDown() throws Exception {
        connection.createStatement().executeUpdate("delete from temple.scrape_job");
    }

    @Test
    public void getNext_autoFinish() throws SQLException {
        execute("""
                insert into temple.scrape_job (id, current_lat, current_lon, started, finished, clst_id, area) values (
                0, 0.1, 0.1, null, null, 1,
                public.ST_GeomFromText('POLYGON((-71.1776585052917 42.3902909739571,-71.1776820268866 42.3903701743239,
                -71.1776063012595 42.3903825660754,-71.1775826583081 42.3903033653531,-71.1776585052917 42.3902909739571))', 4326)
                )
                """);
        ScrapeJobDao dao = new ScrapeJobDao();
        Optional<ScrapeJob> next = dao.getNext();
        assertTrue(next.isPresent());
        assertEquals(0, next.get().id);

        exists("select count(1) as count from temple.scrape_job where busy > 0");

        next.get().setCurrentPosition(new Point(-71.1776585052917, 42.3902909739571));
        exists("select count(1) as count from temple.scrape_job where current_lat < 0 and started is not null and finished is null");

        next.get().setCurrentPosition(new Point(-81.1776585052917, 42.3902909739571));
        exists("select count(1) as count from temple.scrape_job where current_lat < 0 and started is not null and finished is not null and busy = 0");

        assertFalse(dao.getNext().isPresent());

    }

    @Test
    public void getNext_explicitFinish() throws SQLException {
        execute("""
                insert into temple.scrape_job (id, current_lat, current_lon, started, finished, clst_id, area) values (
                1, 0.1, 0.1, null, null, 1,
                public.ST_GeomFromText('POLYGON((-71.1776585052917 42.3902909739571,-71.1776820268866 42.3903701743239,
                -71.1776063012595 42.3903825660754,-71.2775826583081 42.3903033653531,-71.1776585052917 42.3902909739571))', 4326)
                )
                """);

        exists("select count(1) as count from temple.scrape_job where busy = 0");

        ScrapeJobDao dao = new ScrapeJobDao();
        Optional<ScrapeJob> next = dao.getNext();
        assertTrue(next.isPresent());
        exists("select count(1) as count from temple.scrape_job where busy = 1");

        assertFalse(next.get().isDone()); // only work because polygon here is tweaked to -71.2775826583081
        next.get().release();

        exists("select count(1) as count from temple.scrape_job where busy = 0");
    }
}