package google.maps.webview;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/*
 intellij vm options to run this stuff without launcher: --module-path /opt/javafx-sdk-15.0.1/lib --add-modules=javafx.web,javafx.controls --add-exports javafx.web/com.sun.javafx.webkit=ALL-UNNAMED
* */

public class ScraperApplication extends Application {

    private Scene scene;

    @Override
    public void start(Stage stage) {
        stage.setTitle("maps");
        Browser browser = new Browser(getParameters().getRaw());
        scene = new Scene(browser, 1910, 900, Color.web("#666970"));

        stage.setScene(scene);

        stage.show();
        //setUpLogger();
    }


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

    public static void main(String[] args) {
        launch(args);
    }
}
