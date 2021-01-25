package google.maps.webview;

import google.maps.Area;
import google.maps.Point;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import util.Tuple;

import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static google.maps.Area.rectangle;
import static google.maps.webview.Log.log;

/*
 intellij vm options to run this stuff without launcher: --module-path /opt/javafx-sdk-15.0.1/lib --add-modules=javafx.web,javafx.controls --add-exports javafx.web/com.sun.javafx.webkit=ALL-UNNAMED
* */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ScraperApplication extends Application {
    private static final int mb = 1024 * 1024;
    private static final int maxMb = 1024 * 2;
    ScrapingBrowser scrapingBrowser;
    Point currentPoint;
    Timer timer = new Timer();
    boolean autorun;

    @Override
    public void start(Stage stage) {
        log("parameters received: " + getParameters().getRaw());
        autorun = getParameters().getRaw().contains("autorun");

        stage.setTitle("maps");
        resetStage(stage);
        //setUpLogger();
    }

    private Area searchArea;

    private void resetStage(Stage stage) {
        log("reset stage");

        if (scrapingBrowser != null) {
            scrapingBrowser.cancelled.set(true);
            stage.close();
        }

        List<String> parameters = getParameters().getRaw();
        Optional<Tuple<Point>> boundaries = getBoundaries(parameters);
        searchArea = searchArea == null ? getSearchArea(boundaries) : searchArea;
        currentPoint = currentPoint == null ? getCurrentPoint(boundaries): currentPoint;

        scrapingBrowser = new ScrapingBrowser(autorun, searchArea, currentPoint, this::onPointSeen);
        stage.setScene(new Scene(scrapingBrowser, 1910, 900, Color.web("#666970")));

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.gc();
                if (exceedsMemBoundary()) {
                    Platform.runLater(() -> resetStage(stage));
                }
            }
        }, scrapingBrowser == null ? 0 : 300000);
        stage.show();
    }

    private boolean exceedsMemBoundary() {
        Runtime instance = Runtime.getRuntime();
        if ( Math.toIntExact(instance.totalMemory() / (1024 * 1024)) > maxMb){
            log("***** Heap utilization statistics [MB] *****\n");
            log("Total Memory: " + instance.totalMemory() / mb);
            log("Free Memory: " + instance.freeMemory() / mb);
            log("Used Memory: " + (instance.totalMemory() - instance.freeMemory()) / mb);
            log("Max Memory: " + instance.maxMemory() / mb);
            return true;
        }
        return false;
    }

    private void onPointSeen(Point p) {
        currentPoint = p;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Point getCurrentPoint(Optional<Tuple<Point>> searchArea) {
        assert searchArea.isPresent();
        Point cornerLeftUpper = searchArea.get().left;

        // 0.0005 ... so the map is centered inside the boundaries
        var result =  new Point(cornerLeftUpper.lat - 0.0005, cornerLeftUpper.lon + 0.0005);
        log(String.format("starting at (%.7f/%.7f)\n", result.lat, result.lon));
        return result;
    }

    private Area getSearchArea(Optional<Tuple<Point>> boundaries) {
        Area result;
        if (boundaries.isPresent()) {
            result = new Area(rectangle(boundaries.get().left, boundaries.get().right));
            Point lu = boundaries.get().left;
            Point rl = boundaries.get().right;
            log(String.format("scraping area (%.7f/%.7f)/(%.7f/%.7f)\n", lu.lat, lu.lon, rl.lat, rl.lon));
        } else {
            result = new Area(rectangle(new Point(12.9, 78.6), new Point(11.9, 79.7)));
        }
        return result;
    }

    private Optional<Tuple<Point>> getBoundaries(List<String> params) {
        if (params.size() > 0) {
            if (params.size() < 4) {
                throw new IllegalArgumentException("invalid number of params");
            }
            List<Double> c = params.subList(0, 4).stream().map(Double::valueOf).collect(Collectors.toList());
            return Optional.of(Tuple.of(new Point(c.get(0), c.get(1)), new Point(c.get(2), c.get(3))));
        }
        return Optional.empty();
    }


    @SuppressWarnings("unused")
    private void setUpLogger() {
        Enumeration<String> names = LogManager.getLogManager().getLoggerNames();
        for (Iterator<String> it = names.asIterator(); it.hasNext(); ) {
            String name = it.next();
            Logger logger = LogManager.getLogManager().getLogger(name);
            logger.setLevel(Level.FINEST);
            logger.addHandler(new ConsoleHandler());
        }
        Logger.getLogger("").setLevel(Level.ALL);
        Logger.getLogger("").addHandler(new ConsoleHandler());
    }
}
