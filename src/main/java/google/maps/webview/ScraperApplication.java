package google.maps.webview;

import google.maps.webview.datasink.PlaceCoordWriter;
import google.maps.webview.datasink.PlaceDataSink;
import google.maps.webview.datasink.PlaceDetailsWriter;
import google.maps.webview.intercept.URLLoaderInterceptor;
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

    private SetUp setUp;
    public static ScrapeBrowser browser;

    @Override
    public void start(Stage stage) {

        stage.setTitle("maps");
        setUp = new SetUp(getParameters().getRaw());

        ScrapeBrowser scrapeBrowser = new ScrapeBrowser(setUp);
        browser = scrapeBrowser;
        setupUrlLoaderInterceptor();

        stage.setScene(new Scene(scrapeBrowser, 1910, 900, Color.web("#666970")));
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
        final PlaceDataSink detailsWriter = switch (setUp.markerProcessingType) {
            case temple -> new PlaceDetailsWriter();
            case any -> new PlaceCoordWriter();
        };

        URLLoaderInterceptor.onSendRequest = (c) -> {
            if (c.getURL().toString().contains("/maps/preview/place")) {
                detailsWriter.put(c.getURL().toString());
                return false;
            }
            return true;
        };

        URLLoaderInterceptor.onDidReceiveData = (data) -> {
        };
        URLLoaderInterceptor.onFinishedLoading = () -> {
        };
        URLLoaderInterceptor.onDidReceiveResponse = (c) -> {
        };
    }

    @Override
    public void stop() throws Exception {
        setUp.getScrapeJob().release();
        super.stop();
    }
}
