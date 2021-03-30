package google.maps.webview;

import google.maps.dao.ScrapeJobDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ScrapeJobRunner {

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .forEach(consumer);
        }
    }

    public static void main(String[] args) {
        ScrapeJobDao dao = new ScrapeJobDao();

        while (!dao.allDone()) {
            runNext();
        }
    }

    private static int runNext() {
        Process process;
        try {
            String cmd = "/opt/jdk-15/bin/java -jar --enable-preview -javaagent:./scrapers-1.0-SNAPSHOT.jar ./Launcher-jar-with-dependencies.jar 18 autorun";
            process = Runtime.getRuntime().exec(cmd);

            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            return exitCode;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
