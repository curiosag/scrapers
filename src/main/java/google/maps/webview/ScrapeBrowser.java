package google.maps.webview;

import com.sun.javafx.webkit.WebConsoleListener;
import google.maps.PixelCoordinate;
import google.maps.Point;
import google.maps.extraction.ResultFileExtractor;
import google.maps.webview.markers.MarkerDetector;
import google.maps.webview.scrapejob.ScrapeJob;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.robot.Robot;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static google.maps.webview.GrazingDirection.LEFT_TO_RIGHT;
import static google.maps.webview.GrazingDirection.RIGHT_TO_LEFT;
import static google.maps.webview.Log.log;
import static google.maps.webview.ScanChunkifyer.chunkify;

class ScrapeBrowser extends Region {
    final JsBridge jsbridge;

    public AtomicBoolean cancelled = new AtomicBoolean();
    Consumer<Point> onCoordinateSeen;

    private final static String markedImagePath = "./scraped/markedImages/";
    private final static String mapScreenshotPath = "./scraped/mapCapture.png";

    final WebView webView = new WebView();
    final WebEngine webEngine = webView.getEngine();

    private final HBox toolBar;
    final TextField coordinateDisplay = new TextField("hu");
    final Button move = new Button("go");
    final Button stop = new Button("stop");

    final Robot robot = new Robot();

    private final ScrapeJob scrapeJob;
    private GrazingDirection grazingDirection = LEFT_TO_RIGHT;
    private AreaExceeded areaExceeded = AreaExceeded.NO;

    private final ConditionalTimer timer = new ConditionalTimer(() -> true, "mapops", true);
    private MarkerProcessingType markerProcessingType = MarkerProcessingType.temple;

    public ScrapeBrowser(SetUp setUp) {
        this(setUp.autorun, setUp.getScrapeJob(), setUp.zoom, setUp.getScrapeJob()::setCurrentPosition);
        markerProcessingType = setUp.markerProcessingType;
    }

    private void setupFileStuff() {
        Path pMarkedImage = Paths.get(markedImagePath);
        Path pResultFiles = Paths.get(ResultFileExtractor.resultFilePath);
        if (!Files.exists(pResultFiles))
            try {
                System.out.println("Setting up directories in " + System.getProperty("user.dir"));
                Files.createDirectories(pMarkedImage);
                Files.createDirectories(pResultFiles);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    private ScrapeBrowser(boolean autorun, ScrapeJob scrapeJob, float zoom, Consumer<Point> onCoordinateSeen) {
        setupFileStuff();

        this.scrapeJob = scrapeJob;
        this.onCoordinateSeen = onCoordinateSeen;

        webView.setOnMouseMoved(this::onMouseMove);
        webView.setOnKeyPressed(this::onKeyPressed);
        move.setOnAction(this::onMoveButtonPressed);
        stop.setOnAction(this::onStopButtonPressed);

        coordinateDisplay.setPrefWidth(400);

        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getStyleClass().add("browser-toolbar");
        toolBar.getChildren().add(coordinateDisplay);
        toolBar.getChildren().addAll(move, stop);
        toolBar.getChildren().add(createSpacer());

        getChildren().add(toolBar);
        getChildren().add(webView);

        CookieManager manager = new CookieManager(new TrivialCookieStore(), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);

        setupJsConsoleListener();
        Point p = scrapeJob.getCurrentPosition();
        String url = String.format("https://www.google.com/maps/@%.7f,%.7f,%.2fz", p.lat, p.lon, zoom);
        feedback(scrapeJob, url);

        webEngine.load(url);
        maybeAutorun(autorun);

        jsbridge = new JsBridge(webEngine, this::onUrlSeen, (hu, ha) -> {
        }, this::onContextMenuItem);
    }

    private void onMoveButtonPressed(ActionEvent actionEvent) {
        cancelled.set(false);
        startGrazing();
    }

    private void onStopButtonPressed(ActionEvent actionEvent) {
        cancelled.set(true);
    }

    private void feedback(ScrapeJob scrapeJob, String url) {
        if (scrapeJob.id > 0) {
            log(String.format("Scrape job id:%d at %s",
                    scrapeJob.id, scrapeJob.getCurrentPosition()));
        } else
            log(url);
    }

    private void maybeAutorun(boolean autorun) {
        if (autorun) {
            System.out.println("Starting to graze in 10 secs");
            schedule(this::startGrazing, 10000);
        }
    }

    private String prevCoords = "";

    private void onContextMenuItem(String coords) {
        if (cancelled.get())
            return;

        if (!coords.equals(prevCoords)) {
            log(coords);
            prevCoords = coords;
        }

        String[] parts = coords.split(",");
        if (parts.length != 2)
            throw new IllegalStateException();

        double lat = Double.parseDouble(parts[0]);
        double lon = Double.parseDouble(parts[1]);

        Point p = new Point(lat, lon);
        scrapeJob.setCurrentPosition(p);

        areaExceeded = scrapeJob.scrapeArea.exceeded(p);
        switch (areaExceeded) {
            case RIGHT, LEFT -> { // will be treated in the navigation events below
            }
            case NORTH -> throw new IllegalStateException();
            case SOUTH -> exitApplication(p);
            case NO -> onCoordinateSeen.accept(p);
        }
    }

    private void onUrlSeen(String url) {
    }

    private void exitApplication(Point p) {
        System.out.printf("Finished scrape job %d at %s\n", scrapeJob.id, p.toString());
        Platform.exit();
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.C) {
            exitApplication(scrapeJob.getCurrentPosition());
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            exitApplication(scrapeJob.getCurrentPosition());
        }
    }

    private void setupJsConsoleListener() {
        WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> System.out.println("console: " + message));
    }

    private void startGrazing() {
        areaExceeded = scrapeJob.scrapeArea.exceeded(scrapeJob.getCurrentPosition());
        schedule(() -> {
            if (areaExceeded == AreaExceeded.NO) {
                gatherLocationsAndGraze();
            } else {
                log("Initial area not within boundaries");
                System.exit(1);
            }
        }, 500);
    }

    private void graze(List<Point> locations) {
        MemoryWatcher.watch(scrapeJob);

        if (!isDone() && !cancelled.get()) {
            Runnable continueWith = () -> moveMapHorizontally(this::checkAndTurn);
            new GrazingTimerTask(new ArrayDeque<>(locations), this::mouseMove, 500, continueWith, timer).run();
        }
    }

    void checkAndTurn() {
        schedule(() -> {
            if (areaExceeded == AreaExceeded.NO) {
                gatherLocationsAndGraze();
            } else {
                turnAround_moveSouth();
            }
        }, 500);
    }

    private void gatherLocationsAndGraze() {
        if (cancelled.get())
            return;

        List<PixelCoordinate> locations;
        switch (markerProcessingType) {
            case temple -> {
                locations = MarkerDetector.getTemples(saveScreenshotToFile(mapScreenshotPath));
            }
            case any -> {
                BufferedImage image = saveScreenshotToFile(mapScreenshotPath);
                locations = chunkify(MarkerDetector.getMarkerPixelCoordinates(image));
            }
            default -> throw new IllegalStateException();
        }

        MapScreenMeasures measures = new MapScreenMeasures(webView);

        List<Point> screenLocations = locations.stream()
                .filter(l -> grazingDirection == LEFT_TO_RIGHT ? l.x < measures.mapCenterX + 20 : l.x >= measures.mapCenterX - 20)
                .filter(l -> l.x >= ScreenCoordinatesMap.offsetX)
                .map(l -> toScreenCoordinates(l, measures))
                .collect(Collectors.toList());

        graze(screenLocations);
    }

    private void moveMapHorizontally(Runnable next) {
        if (cancelled.get())
            return;
        ScreenCoordinatesMap l = new ScreenCoordinatesMap(webView);
        float y = l.y + 450; // avoid the hidden map elements
        float centerX = l.x + l.w / 2;
        // create some overlap between moved map sections in order to catch markers that otherwise may be precisely at the border line
        float overlapOffset = grazingDirection == LEFT_TO_RIGHT ? -15 : 15;
        float startX = centerX + overlapOffset; // make sure that MoveTimerTask params x and startx are initially equal
        int delay = 10;
        switch (grazingDirection) {
            // search window moves right, map moves left, mouse moves left
            case LEFT_TO_RIGHT -> new MoveTimerTask(startX, startX, l.x, -10, y, y, y, 0, delay, robot, next, timer).run();
            // search window moves left, map moves right, mouse moves right
            case RIGHT_TO_LEFT -> new MoveTimerTask(startX, startX, centerX + l.w / 2, 10, y, y, y, 0, delay, robot, next, timer).run();
        }
    }

    private GrazingDirection lastGrazingDirection = LEFT_TO_RIGHT;

    private void turnAround_moveSouth() {
        grazingDirection = switch (areaExceeded) {
            case RIGHT -> RIGHT_TO_LEFT;
            case LEFT -> LEFT_TO_RIGHT;
            default -> throw new IllegalStateException();
        };
        // after a turn we could end up here repeatedly until search are has been re-entered.
        // only move south once
        if (lastGrazingDirection != grazingDirection) {
            lastGrazingDirection = grazingDirection;
            moveSouth(() -> schedule(this::gatherLocationsAndGraze, 1500));
            System.out.println("turned around " + grazingDirection + " at " + scrapeJob.getCurrentPosition());
        } else {
            schedule(this::gatherLocationsAndGraze, 1500);
        }
    }

    private void moveSouth(Runnable andThen) {
        if (cancelled.get())
            return;
        ScreenCoordinatesMap l = new ScreenCoordinatesMap(webView);

        float overlapOffset = -15;
        float startY = l.y + l.h + overlapOffset; // make sure that MoveTimerTask y and starty are initially equal
        float x = l.x + 500; // avoid the hidden map elements
        int delay = 30;
        new MoveTimerTask(x, x, x, 0, startY, startY, l.y, -10, delay, robot, andThen, timer).run();
    }

    private void onMouseMove(MouseEvent e) {
        coordinateDisplay.setText(String.format("x: %.5f y: %.5f", e.getSceneX(), e.getSceneY()));
    }

    public void mouseMove(float screenX, float screenY) {
        Platform.runLater(() -> robot.mouseMove(screenX, screenY));
    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(webView, 0, 0, w, h - tbHeight, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar, 0, h - tbHeight, w, tbHeight, 0, HPos.CENTER, VPos.CENTER);
    }

    @SuppressWarnings("SameParameterValue")
    private BufferedImage saveScreenshotToFile(String capturedImagePath) {
        //SnapshotParameters params = new SnapshotParameters();
        //params.setViewport(new Rectangle2D(mapOffset, 0, (w - mapOffset) / 2, h));
        // SnapshotParameters.viewport lead to frequent ArrayOutOfBoundsExceptions in image detection
        Image image = webView.snapshot(null, null);
        BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bi, "png", new File(capturedImagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bi;
    }

    private void schedule(Runnable r, long delay) {
        if (cancelled.get())
            return;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(r);
            }
        }, delay);
    }

    private boolean isDone() {
        return cancelled.get() || areaExceeded == AreaExceeded.SOUTH;
    }

    private Point toScreenCoordinates(PixelCoordinate m, MapScreenMeasures measures) {
        return new Point(m.x + measures.screenOffsetX, m.y + measures.screenOffsetY);
    }

    private Point toScreenCoordinates(Point p, MapScreenMeasures measures) {
        return new Point(p.lat + measures.screenOffsetX, p.lon + measures.screenOffsetY);
    }

}