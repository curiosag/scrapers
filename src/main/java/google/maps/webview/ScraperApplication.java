package google.maps.webview;

import google.maps.webview.datasink.PlaceCoordWriter;
import google.maps.webview.datasink.PlaceDataSink;
import google.maps.webview.datasink.PlaceDetailsFileDbComboWriter;
import google.maps.webview.intercept.ManualSearchContentInterception;
import google.maps.webview.intercept.URLLoaderInterceptor;
import google.maps.webview.intercept.UrlLoaderContentInterception;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/*
 intellij vm options to run this stuff without launcher: --module-path /opt/javafx-sdk-15.0.1/lib --add-modules=javafx.web,javafx.controls --add-exports javafx.web/com.sun.javafx.webkit=ALL-UNNAMED
* */

public class ScraperApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public static SetUp setUp;
    public static ScrapeBrowser browser;

    @Override
    public void start(Stage stage) {
        stage.setTitle("maps");
        setUp = new SetUp(getParameters().getRaw());

        setupUrlLoaderInterceptor();
        browser = new ScrapeBrowser(setUp);
        stage.setScene(new Scene(browser, 1910, 1000, Color.web("#666970")));
        stage.setX(0);
        stage.setY(0);
        stage.show();
    }

    private static int shots = 0;

    @SuppressWarnings("unused")
    public static void screenshot() {
        shots++;
        Platform.runLater(() -> {
            WritableImage image = browser.snapshot(new SnapshotParameters(), null);

            File file = new File(shots + "_screenshot.png");
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setupUrlLoaderInterceptor() {
        if (setUp.processingType == ProcessingType.manual_search) {
            UrlLoaderContentInterception.setUp(new ManualSearchContentInterception());
        } else
            setupAutomatedProcessing();
    }

    private void setupAutomatedProcessing() {
        final PlaceDataSink detailsWriter = switch (setUp.processingType) {
            case marker_temple -> new PlaceDetailsFileDbComboWriter(CConst.resultFilePath + setUp.displayNumber);
            case marker_any -> new PlaceCoordWriter(CConst.scrapedDataFilePath + "/scn" + setUp.displayNumber);
            default -> throw new IllegalStateException();
        };

        URLLoaderInterceptor.onSendRequest = (whatever, urlConnection) -> {
            if (urlConnection.getURL().toString().contains("/maps/preview/place")) {
                detailsWriter.put(urlConnection.getURL().toString());
                return false;
            }
            return true;
        };
    }

    @Override
    public void stop() throws Exception {
        if (setUp.processingType != ProcessingType.manual_search)
            setUp.getScrapeJob().release(null);
        super.stop();
    }

}
