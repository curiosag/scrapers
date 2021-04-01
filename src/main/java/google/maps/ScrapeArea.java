package google.maps;

import google.maps.webview.AreaExceeded;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ScrapeArea {

    private final List<Point> boundary;
    private Point southernMost;
    private Point northernMost;

    private GeometryFactory geoFactory;
    private Polygon geo;

    public Point getSouthernMost() {
        if (southernMost == null) {
            southernMost = getApexSouth(boundary);
        }
        return southernMost;
    }

    public Point getNorthernMost() {
        if (northernMost == null) {
            northernMost = getApexNorth(boundary);
        }
        return northernMost;
    }

    public boolean exceedsSouth(Point p) {
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
        Coordinate[] point = {new Coordinate(p.lat, p.lon)};

        return geo.contains(new org.locationtech.jts.geom.Point(new CoordinateArraySequence(point), geoFactory));
    }

    public AreaExceeded exceeded(Point p) {
        if (geo == null) {
            setupGeo();
        }

        Coordinate[] linePoints = {new Coordinate( p.lat, p.lon - 30), new Coordinate( p.lat, p.lon + 30)};
        LineString line = new LineString(new CoordinateArraySequence(linePoints), geoFactory);
        List<Coordinate> intersection = Arrays.asList(geo.intersection(line).getCoordinates());
        if (intersection.size() > 0) {
            if (intersection.stream().allMatch(c -> c.y < p.lon))
                return AreaExceeded.RIGHT;
            if (intersection.stream().allMatch(c -> c.y > p.lon))
                return AreaExceeded.LEFT;
        }
        if (exceedsSouth(p))
            return AreaExceeded.SOUTH;
        if (p.lat > getNorthernMost().lat)
            return AreaExceeded.NORTH;
        return AreaExceeded.NO;
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
