package google.maps.webview;


import boofcv.struct.feature.Match;
import com.sun.javafx.webkit.WebConsoleListener;
import google.maps.Area;
import google.maps.Point;
import google.maps.extraction.ResultFileExtractor;
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
import javafx.scene.input.MouseButton;
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
import java.io.FileWriter;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.charset.StandardCharsets;
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

class ScrapeBrowser extends Region {
    final JsBridge jsbridge;

    public AtomicBoolean cancelled = new AtomicBoolean();
    Consumer<Point> onCoordinateSeen;

    private final static String markedImagePath = "./scraped/markedImages/";
    private final static String mapScreenshotPath = "./scraped/mapCapture.png";
    private final static String templatePath = "./om.png";

    final WebView webView = new WebView();
    final WebEngine webEngine = webView.getEngine();

    private final HBox toolBar;
    final TextField coordinateDisplay = new TextField("hu");
    final Button move = new Button("go");

    final Robot robot = new Robot();

    private final Area searchArea;
    private GrazingDirection grazingDirection = LEFT_TO_RIGHT;
    private AreaExceeded areaExceeded = AreaExceeded.NO;

    File templateImage = setupFileStuff();
    private final ConditionalTimer timer = new ConditionalTimer(() -> !cancelled.get(), "mapops", true);
    private final static boolean clickEmptyAreaToElicitCoordinates = false;

    private File setupFileStuff() {
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

        File f = new File(templatePath);
        if (!f.exists()) {
            throw new IllegalStateException(templatePath + " doesn't exist");
        }
        return f;
    }

    public ScrapeBrowser(boolean autorun, Area searchArea, Point startAt, float zoom, Consumer<Point> onCoordinateSeen) {
        this.searchArea = searchArea;
        this.onCoordinateSeen = onCoordinateSeen;

        webView.setOnMouseMoved(this::onMouseMove);
        webView.setOnKeyPressed(this::onKeyPressed);

        move.setOnAction(this::onMoveButtonPressed);

        coordinateDisplay.setPrefWidth(400);

        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getStyleClass().add("browser-toolbar");
        toolBar.getChildren().add(coordinateDisplay);
        toolBar.getChildren().addAll(move);
        toolBar.getChildren().add(createSpacer());

        getChildren().add(toolBar);
        getChildren().add(webView);

        CookieManager manager = new CookieManager(new TrivialCookieStore(), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);

        setupJsConsoleListener();

        webEngine.load(getStartUrl(startAt, zoom));
        maybeAutorun(autorun);

        jsbridge = new JsBridge(webEngine, this::onUrlSeen, (hu, ha) -> {
        });
    }

    private void maybeAutorun(boolean autorun) {
        if (autorun) {
            System.out.println("Starting to graze in 10 secs");
            schedule(this::startGrazing, 10000);
        }
    }

    private String getStartUrl(Point p, float zoom) {
        var result = String.format("https://www.google.com/maps/@%.7f,%.7f,%.2fz", p.lat, p.lon, zoom);
        log(result);
        return result;
    }

    private final String logfile = "./grazelog.txt";
    private int urls = 0;

    private void onUrlSeen(String url) {
        if (cancelled.get())
            return;
        if (url.startsWith("/maps/preview/") || url.startsWith("/maps/rpc/vp?")) {
            CoordExtractor.extract(url).ifPresent(p -> {
                log(url);
                if (searchArea.contains(p)) {
                    onCoordinateSeen.accept(p);
                    areaExceeded = AreaExceeded.NO;
                } else {
                    areaExceeded = grazingDirection == LEFT_TO_RIGHT ? AreaExceeded.RIGHT : AreaExceeded.LEFT;
                    if (p.lat < searchArea.getSouthernMost().lat) {
                        areaExceeded = AreaExceeded.SOUTH;
                    }
                    System.out.println("Area exceeded at " + p.toString());
                }
            });
        }
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.C) {
            Platform.exit();
            System.exit(0);
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            areaExceeded = AreaExceeded.SOUTH;
        }
    }

    private void setupJsConsoleListener() {
        WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> System.out.println("console: " + message));
    }

    private void onMoveButtonPressed(ActionEvent actionEvent) {
        startGrazing();
    }

    private void startGrazing() {
        areaExceeded = AreaExceeded.NO;
        klickNcheckAreaExceeded(this::gatherLocationsAndGraze, () -> {
            throw new IllegalArgumentException("Initial area not within boundaries");
        });
    }

    private final ImageTemplateMatching imageTemplateMatching = new ImageTemplateMatching(null /*markedImagePath*/);

    private void graze(List<Point> locations) {
        if (!isDone() && !cancelled.get()) {
            ScreenCoordinatesMap sc = new ScreenCoordinatesMap(webView);
            Runnable continueWith = () ->
                    moveMapHorizontally(() -> klickNcheckAreaExceeded(this::gatherLocationsAndGraze, this::moveSouth));

            new GrazingTimerTask(new ArrayDeque<>(locations), this::mouseMove, 500, continueWith, timer).run();
        }
    }

    void klickNcheckAreaExceeded(Runnable continuedOnOk, Runnable continueOnFail) {
        Image image = saveScreenshotToFile(mapScreenshotPath);
        if (clickEmptyAreaToElicitCoordinates) {
            EmptyAreaDetector.getEmptyKlickableLocation(image).ifPresent(l -> Platform.runLater(() -> {
                MapScreenMeasures measures = new MapScreenMeasures(webView);
                Point p = toScreenCoordinates(l, measures);
                mouseClick((float) p.lon, (float) p.lat);
            }));
        }
        // "check" is done onUrlSeen(), capturing the coordinates in the request triggered by mouseClick
        // so schedule needs to be delayed a bit
        schedule(() -> {
            if (areaExceeded == AreaExceeded.NO) {
                continuedOnOk.run();
            } else {
                continueOnFail.run();
            }
        }, 500);
    }

    @SuppressWarnings("SimplifiableConditionalExpression")
    private void gatherLocationsAndGraze() {
        if (cancelled.get())
            return;
        saveScreenshotToFile(mapScreenshotPath);
        List<Match> locations = imageTemplateMatching.getTemplateLocations(templateImage, new File(mapScreenshotPath));

        MapScreenMeasures measures = new MapScreenMeasures(webView);

        // only half of the map can be grazed, because the other half may still be loading
        List<Point> screenLocations = locations.stream()
                .filter(l -> grazingDirection == LEFT_TO_RIGHT ? l.x < measures.mapCenterX : l.x >= measures.mapCenterX)
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

    private void reenterSearchArea() {
        // areaExceeded is set in onUrlSeen
        switch (areaExceeded) {
            case RIGHT, LEFT -> moveMapHorizontally(() -> klickNcheckAreaExceeded(this::gatherLocationsAndGraze, this::reenterSearchArea));
            case SOUTH -> {
            }
            case NO -> gatherLocationsAndGraze();
        }
    }

    private void moveSouth() {
        if (cancelled.get())
            return;
        ScreenCoordinatesMap l = new ScreenCoordinatesMap(webView);

        Runnable thenTurnAround = () -> schedule(this::turnAround, 1500);
        float overlapOffset = -15;
        float startY = l.y + l.h + overlapOffset; // make sure that MoveTimerTask y and starty are initially equal
        float x = l.x + 500; // avoid the hidden map elements
        int delay = 30;
        new MoveTimerTask(x, x, x, 0, startY, startY, l.y, -10, delay, robot, thenTurnAround, timer).run();
    }

    private void turnAround() {
        switch (areaExceeded) {
            case RIGHT -> {
                grazingDirection = RIGHT_TO_LEFT;
                reenterSearchArea();
            }
            case LEFT -> {
                grazingDirection = LEFT_TO_RIGHT;
                reenterSearchArea();
            }
            case SOUTH, NO -> {
            }
        }
        System.out.println("turned around " + grazingDirection);
    }

    private void onMouseMove(MouseEvent e) {
        coordinateDisplay.setText(String.format("x: %.5f y: %.5f", e.getSceneX(), e.getSceneY()));
    }

    public void mouseMove(float screenX, float screenY) {
        Platform.runLater(() -> robot.mouseMove(screenX, screenY));
    }

    public void mouseClick(float screenX, float screenY) {
        Platform.runLater(() -> {
            robot.mouseMove(screenX, screenY);
            robot.mouseClick(MouseButton.PRIMARY);
        });
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
    private Image saveScreenshotToFile(String capturedImagePath) {
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
        return image;
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

    private Point toScreenCoordinates(Match m, MapScreenMeasures measures) {
        return new Point(m.x + measures.screenOffsetX, m.y + measures.screenOffsetY);
    }

    private Point toScreenCoordinates(Point p, MapScreenMeasures measures) {
        return new Point(p.lat + measures.screenOffsetX, p.lon + measures.screenOffsetY);
    }

    private void appendToFile(String filename, String s) {
        try (FileWriter w = new FileWriter(filename, StandardCharsets.UTF_8, true)) {
            w.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}