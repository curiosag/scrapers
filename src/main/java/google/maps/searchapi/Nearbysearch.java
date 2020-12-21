package google.maps.searchapi;

import google.maps.Area;
import google.maps.Point;
import google.maps.dao.SearchDao;
import google.maps.dao.RegionsDao;
import util.FileUtil;
import util.HttpUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static google.maps.searchapi.Circles.getCircleCenters;

public class Nearbysearch {

    private final static String inputPath = "/home/ssm/dev/data/temples/scraped/lanka/";

    private static final String fileNamePattern = "%.7f_%.7f__%d.json";

    private final static String apiKey = "AIzaSyC-9YPM9emNZ7KFRMZQuHpgV9LJGaBpSt0";
    //URIPatter with 2 placeholders for latitude/longitude and one for radius
    private final static String searchPattern = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%.7f,%.7f&radius=%d&type=hindu_temple&key=" + apiKey;
    // nextpage if there's a next page token
    final static String nextPage = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=%s&key=" + apiKey;

    public static void main(String[] args) throws SQLException {
        //lat 5.5 lon 81.8    ... just taking half of sri lanka to 7.3d
        //lat 9.9 lon 79.6

        //createSearchCircles(9.9d, 79.6d, 7.3d, 81.8d, "Sri Lanka");
        runSearchQueries();
    }

    private static void runSearchQueries() throws SQLException {
        SearchDao dao = new SearchDao();
        List<SearchDao.Search> todo = dao.getToDoList();
        for (SearchDao.Search s : todo) {
            runQuery(new Point(s.lat(), s.lon()));
            dao.setDone(s.id());
        }
    }

    public static void createSearchCircles(double latLeftUpper, double lonLeftUpper, double latRightLower, double lonRightLower, String state) {
        RegionsDao rd = new RegionsDao();

        List<Point> boundaries = rd.getStateBoundaries("name0", state);
        Area area = new Area(boundaries);

        if (boundaries.size() == 0) {
            System.out.println("no boundaries found for:" + state);
            throw new IllegalStateException();

        }
        List<Point> points = getCircleCenters(latLeftUpper, lonLeftUpper, latRightLower, lonRightLower);

        points = points.stream()
                .filter(area::contains)
                .collect(Collectors.toList());

        try {
            new SearchDao().createSearches(points);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runQuery(Point p) {
        String json = getNearby(p.lat, p.lon, 5000);
        store(json, p, 0);
        Optional<String> tokenNext = getPageToken(json);
        int i = 0;
        while (tokenNext.isPresent()) {
            String next = getNext(tokenNext.get());
            i++;
            store(next, p, i);
            tokenNext = getPageToken(next);
        }
    }

    private static void store(String json, Point p, int i) {
        String name = String.format(fileNamePattern, p.lat, p.lon, i);
        System.out.println("writing " + name);
        FileUtil.write(json, inputPath + name);
    }

    public static Optional<String> getPageToken(String json) {
        String tokenmarker = "\"next_page_token\" : \"";
        int markerIndex = json.indexOf(tokenmarker);
        if (markerIndex < 0) {
            return Optional.empty();
        }

        StringBuilder sb = new StringBuilder();
        int i = markerIndex + tokenmarker.length();
        while (i < json.length()) {
            char current = json.charAt(i);
            if (current == '"')
                break;
            else
                sb.append(current);
            i++;
        }
        return Optional.of(sb.toString());
    }

    static String getNearby(double latitude, double longitude, int radius) {
        return HttpUtil.getByUrlConnection(String.format(searchPattern, latitude, longitude, radius));
    }

    static String getNext(String pageToken) {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return HttpUtil.getByUrlConnection(String.format(nextPage, pageToken));
    }

}
