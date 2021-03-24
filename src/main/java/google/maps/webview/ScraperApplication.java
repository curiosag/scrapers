package google.maps.webview;

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
        stage.setScene(new Scene(scrapeBrowser, 1910, 900, Color.web("#666970")));
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        setUp.getScrapeJob().release();
        super.stop();
    }
}
