package google.maps.searchapi;

import google.maps.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Circles {
    public static List<List<Point>> createCircleCenters(double lat, double lon, int numLat, int numLon) {
        List<List<Point>> rows = new ArrayList<>();
        List<Point> row = fillRow(new Point(lat, lon), numLat, rows);
        rows.add(gapFillers(row));

        for (int i = 1; i < numLon; i++) {
            Point prev = row.get(0);
            double llat = mvLat(prev.lat, -10000);
            double llon = mvLon(prev.lon, llat, 0);
            row = fillRow(new Point(llat, llon), numLat, rows);

            if (i < numLon - 1) {
                rows.add(gapFillers(row));
            }
        }

        return rows;
    }

    public static List<Point> getCircleCenters(double latLeftUpper, double lonLeftUpper, double latRightLower, double lonRightLower) {
        return createCircleCenters(latLeftUpper, lonLeftUpper, latRightLower, lonRightLower).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static List<List<Point>> createCircleCenters(double latLeftUpper, double lonLeftUpper, double latRightLower, double lonRightLower) {
        List<List<Point>> rows = new ArrayList<>();
        List<Point> row = fillRow(new Point(latLeftUpper, lonLeftUpper), lonRightLower);
        rows.add(gapFillers(row));

        Point current = row.get(0);
        while (current.lat > latRightLower) {
            double llat = mvLat(current.lat, -10000);
            double llon = mvLon(current.lon, llat, 0);

            current = new Point(llat, llon);
            row = fillRow(current, lonRightLower);
            rows.add(row);

            if (llat > latRightLower) {
                rows.add(gapFillers(row));
            }
        }

        return rows;
    }

    private static List<Point> fillRow(Point first, double lonRightLower) {
        List<Point> row = new ArrayList<>();

        row.add(first);
        Point current = first;
        while (current.lon < lonRightLower) {
            double llat = mvLat(current.lat, 0);
            double llon = mvLon(current.lon, llat, 10000);
            Point next = new Point(llat, llon);
            row.add(next);
            current = next;
        }
        return row;
    }

    private static List<Point> fillRow(Point first, int rowLenght, List<List<Point>> columns) {
        List<Point> row = new ArrayList<>();
        columns.add(row);

        row.add(first);
        for (int i = 1; i < rowLenght; i++) {
            Point prev = row.get(i - 1);
            double llat = mvLat(prev.lat, 0);
            double llon = mvLon(prev.lon, llat, 10000);
            row.add(new Point(llat, llon));
        }
        return row;
    }

    private static List<Point> gapFillers(List<Point> rows) {
        List<Point> gapFillers = new ArrayList<>();
        for (int i = 0; i < rows.size() - 1; i++) {
            Point p = rows.get(i);
            double llat = mvLat(p.lat, -5000);
            double llon = mvLon(p.lon, llat, 5000);
            gapFillers.add(new Point(llat, llon));
        }
        return gapFillers;
    }

    //https://stackoverflow.com/questions/7477003/calculating-new-longitude-latitude-from-old-n-meters
    private static final double radius_earth = 6371000.0;

    static double mvLat(double from_latitude, double m) {
        return from_latitude + (m / radius_earth) * (180d / Math.PI);
    }

    static double mvLon(double from_longitude, double from_latitude, double m) {
        return from_longitude + (m / radius_earth) * (180d / Math.PI) / Math.cos(from_latitude * Math.PI / 180d);
    }
}
