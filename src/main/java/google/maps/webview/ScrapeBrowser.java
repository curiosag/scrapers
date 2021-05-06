package google.maps.webview;

import com.sun.javafx.webkit.WebConsoleListener;
import google.maps.MarkerCoordinate;
import google.maps.Point;
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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static google.maps.webview.GrazingDirection.LEFT_TO_RIGHT;
import static google.maps.webview.GrazingDirection.RIGHT_TO_LEFT;
import static google.maps.webview.Log.log;
import static google.maps.webview.ScanChunkifyer.chunkify;
import static google.maps.webview.markers.RGB.green;

class ScrapeBrowser extends Region {
    final JsBridge jsbridge;
    private SetUp setUp;
    private AtomicBoolean cancelled = new AtomicBoolean();

    Consumer<Point> onCoordinateSeen;

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

    private final ConditionalTimer timer = new ConditionalTimer(() -> true, "mapOps", true);
    private ProcessingType processingType = ProcessingType.marker_temple;
    private StallingMonitor monitor;

    public void cancel() {
        cancelled.set(true);
    }

    public ScrapeBrowser(SetUp setUp) {
        this(setUp.autorun, setUp, setUp.zoom, setUp.getScrapeJob()::setCurrentPosition);
        processingType = setUp.processingType;
    }

    private ScrapeBrowser(boolean autorun, SetUp setUp, float zoom, Consumer<Point> onCoordinateSeen) {
        this.setUp = setUp;
        this.scrapeJob = setUp.getScrapeJob();
        this.onCoordinateSeen = onCoordinateSeen;

        setupFileStuff();

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

        if (setUp.processingType != ProcessingType.manual_search) {
            jsbridge = new JsBridge(webEngine, this::onUrlSeen, (hu, ha) -> {
            }, this::onCoordinatesSeen);
        } else
            jsbridge = null;

        setScrapingTimeout(30 * 60 * 1000);
        monitor = new StallingMonitor(() -> exitApplication(scrapeJob.getCurrentPosition()));
    }

    private void setScrapingTimeout(int delay) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log("Max scrape time reached, terminating.");
                exitApplication(scrapeJob.getCurrentPosition());
            }
        }, delay);
    }


    private void setupFileStuff() {
        Path pMarkedImage = Paths.get(getMarkedImagePath());
        Path pResultFiles = Paths.get(getResultFilePath());
        if (!Files.exists(pResultFiles))
            try {
                log("Setting up directories in " + System.getProperty("user.dir"));
                Files.createDirectories(pMarkedImage);
                Files.createDirectories(pResultFiles);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    private String getMarkedImagePath() {
        return CConst.markedImagePath + setUp.displayNumber + "/";
    }

    private String getResultFilePath() {
        return CConst.resultFilePath + setUp.displayNumber;
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
            log(String.format("working job id:%d at %s",
                    scrapeJob.id, scrapeJob.getCurrentPosition()));
        } else
            log(url);
    }

    private void maybeAutorun(boolean autorun) {
        if (autorun) {
            log("Starting to graze in 11 secs");
            schedule(this::startGrazing, 11000);
        }
    }

    private String prevCoords = "";

    private void onCoordinatesSeen(String coords) {
        if (cancelled.get())
            return;

        monitor.signalProgress();

        String[] parts = coords.split(",");
        if (parts.length != 2)
            throw new IllegalStateException();

        double lat = avoidFakeTooFarNorthCoordinate(Double.parseDouble(parts[0]));
        double lon = Double.parseDouble(parts[1]);

        Point p = new Point(lat, lon);
        scrapeJob.setCurrentPosition(p);

        if (!coords.equals(prevCoords)) {
            log(String.format("%.2f%s %s", scrapeJob.getPctLatitudeDone(), "%", coords));
            prevCoords = coords;
        }

        areaExceeded = scrapeJob.scrapeArea.exceeded(p);
        switch (areaExceeded) {
            case RIGHT, LEFT -> { // will be treated in the navigation events below
            }
            case NORTH -> throw strangelyExceededException(areaExceeded.name(), p, scrapeJob.scrapeArea.getNorthernMost());
            case SOUTH -> exitApplication(p);
            case NO -> onCoordinateSeen.accept(p);
        }
    }

    private double avoidFakeTooFarNorthCoordinate(double lat) {
        return Math.min(lat, scrapeJob.getCurrentPosition().lat);
    }

    private IllegalStateException strangelyExceededException(String how, Point p, Point northernMost) {
        return new IllegalStateException(String.format("Exceeding %s????.\napexNorth:%s\ncurrent:%s", how, northernMost, p));
    }

    private void onUrlSeen(String url) {
    }

    private void exitApplication(Point p) {
        cancel();
        try {
            scrapeJob.release(null);
        } finally {
            Platform.runLater(Platform::exit);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.printf("Finished scrape job %d at %s\n", scrapeJob.id, p.toString());
                    System.exit(0);
                }
            }, 2000);
        }
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
        WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> log("JS console: " + message));
    }

    private void startGrazing() {
        areaExceeded = scrapeJob.scrapeArea.exceeded(scrapeJob.getCurrentPosition());

        if (areaExceeded == AreaExceeded.NO) {
            gatherLocationsAndGraze();
        } else {
            log("Initial area exceeds " + areaExceeded.name());
            System.exit(1);
        }
    }

    private void graze(List<Point> locations) {
        MemoryWatcher.watch(scrapeJob, setUp.maxGbMem);

        if (!isDone() && !cancelled.get()) {
            Runnable continueWith = () -> moveMapHorizontally(this::checkAndTurn);
            new GrazingTimerTask(new ArrayDeque<>(locations), this::mouseMove, 10, continueWith, timer).run();
        }
    }

    private void gatherLocationsAndGraze() {
        if (cancelled.get())
            return;

        List<MarkerCoordinate> locations;
        switch (processingType) {
            case marker_temple -> locations = MarkerDetector.getHinduTempleMarkers(getScreenshot());
            case marker_any -> {
                BufferedImage image = getScreenshot();
                locations = chunkify(removeGreenMarkers(MarkerDetector.getMarkerTipCoordinates(image)));
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

    private List<MarkerCoordinate> removeGreenMarkers(List<MarkerCoordinate> markerMarkerCoordinates) {
        return markerMarkerCoordinates.stream()
                .filter(c -> notTooGreen(c.color))
                .collect(Collectors.toList());
    }

    private static boolean notTooGreen(int color) {
        return green(color) <= 160;
    }

    private void moveMapHorizontally(Runnable next) {
        if (cancelled.get())
            return;
        ScreenCoordinatesMap l = new ScreenCoordinatesMap(webView);
        float y = l.y + 450; // avoid the hidden map elements
        float centerX = l.x + l.w / 2;
        // create some overlap between moved map sections in order to catch markers that otherwise may be precisely at the border line
        float overlapOffset = grazingDirection == LEFT_TO_RIGHT ? -15 : 15;
        float startX = centerX + overlapOffset; // make sure that MoveTimerTask params x and startX are initially equal
        int delay = 20;
        switch (grazingDirection) {
            // search window moves right, map moves left, mouse moves left
            case LEFT_TO_RIGHT -> new MoveTimerTask(startX, startX, l.x, -10, y, y, y, 0, delay, robot, next, timer).run();
            // search window moves left, map moves right, mouse moves right
            case RIGHT_TO_LEFT -> new MoveTimerTask(startX, startX, centerX + l.w / 2, 10, y, y, y, 0, delay, robot, next, timer).run();
        }
    }

    void checkAndTurn() {
        if (isDone()) {
            scrapeJob.release(null);
            exitApplication(scrapeJob.getCurrentPosition());
        } else {
            schedule(() -> {
                if (areaExceeded == AreaExceeded.NO) {
                    gatherLocationsAndGraze();
                } else {
                    turnAround_moveSouth();
                }
            }, 500);
        }
    }

    private GrazingDirection lastGrazingDirection = LEFT_TO_RIGHT;

    private void turnAround_moveSouth() {
        grazingDirection = switch (areaExceeded) {
            case RIGHT -> RIGHT_TO_LEFT;
            case LEFT -> LEFT_TO_RIGHT;
            default -> throw strangelyExceededException(areaExceeded.name(), scrapeJob.getCurrentPosition(), scrapeJob.scrapeArea.getNorthernMost());
        };
        // after a turn we could end up here repeatedly until search are has been re-entered.
        // only move south once
        if (lastGrazingDirection != grazingDirection) {
            lastGrazingDirection = grazingDirection;
            moveSouth(() -> schedule(this::gatherLocationsAndGraze, 1000));
            log("turned around " + grazingDirection + " at " + scrapeJob.getCurrentPosition());
        } else {
            schedule(this::gatherLocationsAndGraze, 1000);
        }
    }

    private void moveSouth(Runnable andThen) {
        if (cancelled.get())
            return;
        ScreenCoordinatesMap l = new ScreenCoordinatesMap(webView);

        float overlapOffset = -15;
        float startY = l.y + l.h + overlapOffset; // make sure that MoveTimerTask y and startY are initially equal
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

    @SuppressWarnings({"SameParameterValue", "CommentedOutCode"})
    private BufferedImage getScreenshot() {
        //SnapshotParameters params = new SnapshotParameters();
        //params.setViewport(new Rectangle2D(mapOffset, 0, (w - mapOffset) / 2, h));
        // SnapshotParameters.viewport lead to frequent ArrayOutOfBoundsExceptions in image detection
        Image image = webView.snapshot(null, null);
        /*try {
            ImageIO.write(bi, "png", new File(mapScreenshotPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        return SwingFXUtils.fromFXImage(image, null);
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

    private Point toScreenCoordinates(MarkerCoordinate m, MapScreenMeasures measures) {
        return new Point(m.x + measures.screenOffsetX, m.y + measures.screenOffsetY);
    }

}