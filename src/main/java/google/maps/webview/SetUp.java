package google.maps.webview;

import google.maps.Point;
import google.maps.ScrapeArea;
import google.maps.dao.ScrapeJobDao;
import google.maps.webview.scrapejob.DummyScrapeJobStore;
import google.maps.webview.scrapejob.ScrapeJob;
import util.Tuple;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static google.maps.ScrapeArea.rectangle;
import static google.maps.webview.Log.log;

public class SetUp {

    public enum ImageMarkerProcessingType {
        temple, any
    }

    public final boolean autorun;
    public int zoom;
    private ScrapeJob scrapeJob;
    public final ImageMarkerProcessingType imageMarkerProcessingType;

    public SetUp(List<String> params) {
        log("parameters received: " + params);

        if (params.size() == 0)
            throw new IllegalArgumentException("No params passed");

        autorun = params.contains("autorun");
        imageMarkerProcessingType = params.contains("marker_temple") ?
                ImageMarkerProcessingType.temple :
                ImageMarkerProcessingType.any;

        String errorMsg = """
                invalid number of arguments (should be 3 or 6): %d
                3: zoom(a number)  autorun(or not) markerprocessing("marker_temple" or "any")
                7: x1 y1 x2 y2 zoom autorun markerprocessing
                """;
        switch (params.size()) {
            case 3 -> setupDbJobStore(params);
            case 7 -> setupFromParams(params);
            default -> throw new IllegalArgumentException(errorMsg + params.size());
        }
    }

    private void setupFromParams(List<String> params) {
        zoom = Integer.parseInt(params.get(4));
        Tuple<Point> boundaries = getBoundaries(params);
        ScrapeArea area = getSearchArea(boundaries);
        scrapeJob = new ScrapeJob(0, getCurrentPoint(boundaries), area, DummyScrapeJobStore.instance);
    }

    private void setupDbJobStore(List<String> params) {
        zoom = Integer.parseInt(params.get(0));
        Optional<ScrapeJob> next = new ScrapeJobDao().getNext();
        if (next.isPresent())
            scrapeJob = next.get();
        else
            System.exit(-1);
    }

    private Tuple<Point> getBoundaries(List<String> params) {
        List<Double> c = params.subList(0, 4).stream().map(Double::valueOf).collect(Collectors.toList());
        return Tuple.of(new Point(c.get(0), c.get(1)), new Point(c.get(2), c.get(3)));
    }

    private ScrapeArea getSearchArea(Tuple<Point> boundaries) {
        ScrapeArea result = new ScrapeArea(rectangle(boundaries.left, boundaries.right));
        Point lu = boundaries.left;
        Point rl = boundaries.right;
        log(String.format("scraping area (%.7f/%.7f)/(%.7f/%.7f)\n", lu.lat, lu.lon, rl.lat, rl.lon));

        return result;
    }

    private Point getCurrentPoint(Tuple<Point> searchArea) {
        Point cornerLeftUpper = searchArea.left;

        // 0.0005 ... so the map is centered inside the boundaries
        var result = new Point(cornerLeftUpper.lat - 0.0005, cornerLeftUpper.lon + 0.0005);
        log(String.format("starting at (%.7f/%.7f)\n", result.lat, result.lon));
        return result;
    }

    public ScrapeJob getScrapeJob() {
        return scrapeJob;
    }
}
