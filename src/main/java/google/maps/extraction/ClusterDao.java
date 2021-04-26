package google.maps.extraction;

import google.maps.CConst;
import google.maps.Point;
import google.maps.dao.GeomUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static persistence.Common.createConnection;

public class ClusterDao {
    private final Connection connection = createConnection(CConst.connectionUrl, true);

    public record Cluster(int id, List<Point> area){}

    public List<Cluster> getClusters() {
        String clusterQuery = """
                select c.id, ST_AsText(ST_FlipCoordinates(c.geom)) as geom
                from fsc_cluster_nonoverlapping c join temple.region r on r.id=676 and st_within(c.geom, ST_FlipCoordinates(r.geom))
                """;

        String query = """
                select c.id, ST_AsText(c.area) as geom
                from temple.scrape_job c join temple.region r on r.id=50 and st_within(c.area, r.geom)
                """;

        List<Cluster> result = new ArrayList<>();
        try {
            ResultSet r = connection.createStatement().executeQuery(query);
            while (r.next()) {
                result.add(new Cluster(r.getInt("id"), GeomUtil.fromGeomString(r.getString("geom"))));
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
