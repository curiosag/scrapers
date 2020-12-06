package google.maps;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Area {

    private final List<Point> boundary;
    private Point southernMost;

    private GeometryFactory geoFactory;
    private Polygon geo;

    public Point getSouthernMost(){
        if(southernMost == null)
        {
            southernMost = boundary.stream().min(Comparator.comparingDouble(a -> a.lat)).orElseThrow(IllegalStateException::new);
        }
        return southernMost;
    }

    public Area(List<Point> boundaries) {
        this.boundary = boundaries;
    }

    public List<Point> getBoundary() {
        return Collections.unmodifiableList(boundary);
    }

    public boolean contains(Point p) {
        if (geo == null) {
            setupGeo();
        }

        return geo.contains(new org.locationtech.jts.geom.Point(new Coordinate(p.lat, p.lon), geoFactory.getPrecisionModel(), geoFactory.getSRID()));
    }

    private void setupGeo() {
        geoFactory = new GeometryFactory();

        Coordinate[] coordinates = boundary.stream()
                .map(p -> new Coordinate(p.lat, p.lon))
                .toArray(Coordinate[]::new);

        geo = geoFactory.createPolygon(coordinates);
    }
}
