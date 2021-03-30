package google.maps.webview;

import google.maps.webview.datasink.PlaceCoordWriter;
import google.maps.webview.datasink.PlaceDataSink;
import google.maps.webview.datasink.PlaceDetailsWriter;
import google.maps.webview.intercept.URLLoaderInterceptor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/*
 intellij vm options to run this stuff without launcher: --module-path /opt/javafx-sdk-15.0.1/lib --add-modules=javafx.web,javafx.controls --add-exports javafx.web/com.sun.javafx.webkit=ALL-UNNAMED
* */

public class ScraperApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private SetUp setUp;

    @Override
    public void start(Stage stage) {
        stage.setTitle("maps");
        setUp = new SetUp(getParameters().getRaw());

        ScrapeBrowser scrapeBrowser = new ScrapeBrowser(setUp);

        setupUrlLoaderInterceptor();

        stage.setScene(new Scene(scrapeBrowser, 1910, 900, Color.web("#666970")));
        stage.show();
    }

    private void setupUrlLoaderInterceptor() {
        final PlaceDataSink detailsWriter =  switch (setUp.markerProcessingType){
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
