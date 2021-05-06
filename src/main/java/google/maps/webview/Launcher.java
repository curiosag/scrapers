package google.maps.webview;

import google.maps.webview.intercept.URLLoaderInterceptor;
import util.StackTrace;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Launcher {

    private static class ShutDownHook extends Thread {
        public void run() {
            releaseJob(null);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            if (Arrays.asList(args).contains("headless")) {
                //setHeadless();
            }
            ScraperApplication.main(args);
            Runtime.getRuntime().addShutdownHook(new ShutDownHook());
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Scraper died from: " + e.getMessage());
            e.printStackTrace();
            releaseJob(StackTrace.get(e));
            System.exit(1);
        }
    }

    private static void releaseJob(String errorMsg) {
        if (ScraperApplication.setUp != null && ScraperApplication.setUp.getScrapeJob() != null) {
            ScraperApplication.setUp.getScrapeJob().release(errorMsg);
        }
    }

    private static void setHeadless() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }

    // to trigger pass jvm parameter: -javaagent:<path to byte-buddy-agent.jar>
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        URLLoaderInterceptor.setup(instrumentation);
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
