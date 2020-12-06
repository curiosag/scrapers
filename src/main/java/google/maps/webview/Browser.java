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
import util.Tuple;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static google.maps.webview.GrazingDirection.LEFT_TO_RIGHT;
import static google.maps.webview.GrazingDirection.RIGHT_TO_LEFT;

class Browser extends Region {
    private final static String markedImagePath = "./scraped/markedImages/";
    private final static String mapScreenshotPath = "./scraped/mapCapture.png";
    private final static String templatePath = "./om.png";

    final WebView webView = new WebView();
    final WebEngine webEngine = webView.getEngine();

    private final HBox toolBar;
    final TextField coordinateDisplay = new TextField("hu");
    final Button move = new Button("go");

    final JsBridge jsbridge;
    final Robot robot = new Robot();
    final ResultCsvExtractor resultCsvExtractor = new ResultCsvExtractor();

    private final Area searchArea;
    private GrazingDirection grazingDirection = LEFT_TO_RIGHT;
    private AreaExceeded areaExceeded = AreaExceeded.NO;

    File templateImage = setupFileStuff();
    private final Timer timer = new Timer("mapops", true);
    private final static boolean clickEmptyAreaToElicitCoordinates = false;

    private File setupFileStuff() {
        try {
            System.out.println("Setting up directories in " + System.getProperty("user.dir"));
            Files.createDirectories(Paths.get(markedImagePath));
            Files.createDirectories(Paths.get(ResultFileExtractor.resultFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File f = new File(templatePath);
        if (!f.exists()) {
            throw new IllegalStateException(templatePath + " doesn't exist");
        }
        return f;
    }

    public Browser(List<String> parameters) {
        System.out.println("parameters received: " + parameters);

        webView.setOnMouseMoved(this::onMouseMove);
        webView.setOnKeyPressed(this::onKeyPressed);

        move.setOnAction(this::onMoveButtonPressed);

        jsbridge = new JsBridge(webEngine, this::onUrlSeen, this::onResponse);

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

        String url;
        Optional<Tuple<Point>> boundaries = getBoundaries(parameters);
        if (boundaries.isPresent()) {
            searchArea = getSearchArea(boundaries.get());
            url = getStartUrl(boundaries.get().left);
            Point lu = boundaries.get().left;
            Point rl = boundaries.get().right;
            System.out.printf("scraping (%.7f/%.7f)/(%.7f/%.7f)\n", lu.lat, lu.lon, rl.lat, rl.lon);
        } else {
            searchArea = new Area(rectangle(new Point(12.9, 78.6), new Point(11.9, 79.7)));
            url = "https://www.google.com/maps/@12.8607531,79.4603154,18.5z";
        }

        webEngine.load(url);

        maybeAutorun(parameters.contains("autorun"));
    }

    private void maybeAutorun(boolean autorun) {
        if(autorun)
        {
            System.out.println("Starting to graze in 10 secs");
            schedule(this::startGrazing, 10000);
        }
    }

    private String getStartUrl(Point p) {
        // 0.001 ... so the map is centered inside the boundaries
        return String.format("https://www.google.com/maps/@%.7f,%.7f,18.5z", p.lat - 0.001, p.lon + 0.001);
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

    private Area getSearchArea(Tuple<Point> corners) {
        return new Area(rectangle(corners.left, corners.right));
    }

    private List<Point> rectangle(Point lu, Point rl) {
        return List.of(new Point(lu.lat, lu.lon), new Point(lu.lat, rl.lon), new Point(rl.lat, rl.lon),
                new Point(rl.lat, lu.lon), new Point(lu.lat, lu.lon));
    }

    private void onUrlSeen(String url) {
        if (url.startsWith("/maps/preview/") || url.startsWith("/maps/rpc/vp?")) {
            CoordExtractor.extract(url).ifPresent(p -> {
                if (searchArea.contains(p)) {
                    System.out.println("Coordinates " + p.toString());
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

    public void onResponse(String headers, String body) {
        resultCsvExtractor.onResponse(body);
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

    private final ImageLocations imageLocations = new ImageLocations(null /*markedImagePath*/);

    private void graze(List<Point> locations) {
        if (!isDone()) {
            Runnable continueWith = () ->
                    moveMapHorizontally(() -> klickNcheckAreaExceeded(this::gatherLocationsAndGraze, this::moveSouth));
            new GrazingTimerTask(new ArrayDeque<>(locations), this::mouseMove, 500, continueWith, timer).run();
        }
    }

    void klickNcheckAreaExceeded(Runnable continuedOnOk, Runnable continueOnFail) {
        Image image = saveScreenshotToFile(mapScreenshotPath);
        if(clickEmptyAreaToElicitCoordinates) {
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

    private void gatherLocationsAndGraze() {
        saveScreenshotToFile(mapScreenshotPath);
        List<Match> locations = imageLocations.getTemplateLocations(templateImage, new File(mapScreenshotPath));

        MapScreenMeasures measures = new MapScreenMeasures(webView);
        // watch mouse coordinates in bash using
        // watch -n 0.1 "xdotool getmouselocation"

        // only half of the map can be grazed, because the other half may still be loading
        List<Point> grazableLocations = locations.stream()
                .filter(l -> grazingDirection == LEFT_TO_RIGHT ? l.x < measures.mapCenterX : l.x >= measures.mapCenterX)
                .map(l -> toScreenCoordinates(l, measures))
                .collect(Collectors.toList());

        graze(grazableLocations);
    }

    private void moveMapHorizontally(Runnable next) {
        MapDisplayLocation l = new MapDisplayLocation(webView);
        float y = l.y + 10;
        float centerX = l.x + (l.w - 10) / 2;
        switch (grazingDirection) {
            case LEFT_TO_RIGHT -> {
                new MoveTimerTask(centerX - 50, centerX - 50, l.x + 10, -10, y, y, y, 0, 10, robot, next, timer).run();
            }
            case RIGHT_TO_LEFT -> {
                new MoveTimerTask(l.x + 10, l.x + 10, centerX - 50, 10, y, y, y, 0, 10, robot, next, timer).run();
            }
        }
    }

    private void reenterSearchArea() {
        // areaExceeded is set in onUrlSeen
        switch (areaExceeded) {
            case RIGHT, LEFT -> {
                moveMapHorizontally(() -> klickNcheckAreaExceeded(this::gatherLocationsAndGraze, this::reenterSearchArea));
            }
            case SOUTH -> {
            }
            case NO -> gatherLocationsAndGraze();
        }
    }

    private void moveSouth() {
        MapDisplayLocation l = new MapDisplayLocation(webView);

        Runnable whatsNext = () -> schedule(this::turnAround, 1500);
        float startY = l.y + l.h - 50;
        float x = l.x + 20;
        new MoveTimerTask(x, x, x, 0, startY, startY, l.y + 10, -10, 100, robot, whatsNext, timer).run();
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
        Platform.runLater(() -> {
            robot.mouseMove(screenX, screenY);
        });
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
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(r);
            }
        }, delay);
    }

    private boolean isDone() {
        return areaExceeded == AreaExceeded.SOUTH;
    }

    private Point toScreenCoordinates(Match m, MapScreenMeasures measures) {
        return new Point(m.x + measures.screenOffsetX, m.y + measures.screenOffsetY);
    }

    private Point toScreenCoordinates(Point p, MapScreenMeasures measures) {
        return new Point(p.lat + measures.screenOffsetX, p.lon + measures.screenOffsetY);
    }


}