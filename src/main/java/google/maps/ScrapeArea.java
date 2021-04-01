package google.maps;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.Comparator;
import java.util.List;

public class ScrapeArea {

    private final List<Point> boundary;
    private Point southernMost;

    private GeometryFactory geoFactory;
    private Polygon geo;

    public Point getSouthernMost(){
        if(southernMost == null)
        {
            southernMost = getApexSouth(boundary);
        }
        return southernMost;
    }

    public boolean exceedsSouth(Point p){
        return p.lat < getSouthernMost().lat;
    }

    public static Point getApexSouth(List<Point> boundary) {
        return boundary.stream().min(Comparator.comparingDouble(a -> a.lat)).orElseThrow(IllegalStateException::new);
    }

    public static Point getApexNorth(List<Point> boundary) {
        return boundary.stream().max(Comparator.comparingDouble(a -> a.lat)).orElseThrow(IllegalStateException::new);
    }

    public static List<Point> rectangle(Point lu, Point rl) {
        return List.of(new Point(lu.lat, lu.lon), new Point(lu.lat, rl.lon), new Point(rl.lat, rl.lon),
                new Point(rl.lat, lu.lon), new Point(lu.lat, lu.lon));
    }

    public ScrapeArea(List<Point> boundaries) {
        this.boundary = boundaries;
    }

    public boolean contains(Point p) {
        if (geo == null) {
            setupGeo();
        }
        Coordinate[] point= {new Coordinate(p.lat, p.lon)};
        return geo.contains(new org.locationtech.jts.geom.Point(new CoordinateArraySequence(point), geoFactory));
    }

    private void setupGeo() {
        geoFactory = new GeometryFactory();

        Coordinate[] coordinates = boundary.stream()
                .map(p -> new Coordinate(p.lat, p.lon))
                .toArray(Coordinate[]::new);

        geo = geoFactory.createPolygon(coordinates);
    }

    public List<Point> getBoundary() {
        return boundary;
    }

}
