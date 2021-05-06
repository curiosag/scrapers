package google.maps.webview;

import google.maps.Point;
import google.maps.ScrapeArea;
import google.maps.dao.ScrapeJobDao;
import google.maps.webview.scrapejob.DummyScrapeJobStore;
import google.maps.webview.scrapejob.ScrapeJob;
import util.ProcessUtil;
import util.Tuple;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static google.maps.ScrapeArea.rectangle;
import static google.maps.webview.Log.log;

public class SetUp {

    public final boolean autorun;
    public int zoom;
    public float maxGbMem;
    public final int displayNumber;
    private ScrapeJob scrapeJob;
    public final ProcessingType processingType;

    public SetUp(List<String> params) {
        log("parameters received: " + params);
        log("kill " + ProcessUtil.getPid() + " to stop this.");
        if (params.size() == 0)
            throw new IllegalArgumentException("No params passed");

        if(params.contains("manual_search"))
        {
            processingType = ProcessingType.manual_search;
            displayNumber = 0;
            zoom = 12;
            autorun = false;
            maxGbMem = 6000;
            scrapeJob = new ScrapeJob(0,  new Point(27d, 70d), null, new DummyScrapeJobStore());
            return;
        }

        autorun = params.contains("autorun");

        processingType = params.contains(ProcessingType.marker_temple.place_type) ?
                ProcessingType.marker_temple :
                ProcessingType.marker_any;

        String errorMsg = """
                invalid number of arguments (should be 4 or 8): %d
                4: zoom(a number)  autorun(or not) markerprocessing("hindu_temple" or "any") maxGbMem
                8: x1 y1 x2 y2 zoom autorun markerprocessing maxGbMem
                """;
        switch (params.size()) {
            case 4 -> setupDbJobStore(params);
            case 8 -> setupFromParams(params);
            default -> throw new IllegalArgumentException(errorMsg + params.size());
        }

        maxGbMem = Float.parseFloat(params.get(params.size() - 1));
        displayNumber = getDisplayNumber();
    }


    private int getDisplayNumber() {
        String n = System.getenv().get("DISPLAY");
        return n == null ? 0 : Integer.parseInt(n.replace(":", "").replace(".", ""));
    }

    private void setupFromParams(List<String> params) {
        zoom = Integer.parseInt(params.get(4));
        Tuple<Point> boundaries = getBoundaries(params);
        ScrapeArea area = getSearchArea(boundaries);
        scrapeJob = new ScrapeJob(0, getCurrentPoint(boundaries), area, DummyScrapeJobStore.instance);
    }

    private void setupDbJobStore(List<String> params) {
        zoom = Integer.parseInt(params.get(0));
        Optional<ScrapeJob> next = new ScrapeJobDao(processingType.place_type).getNext();
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
