package google.maps.webview;

import google.maps.extraction.ResultFileExtractor;
import google.maps.extraction.MapPlaceDetailsResponseExtractor;
import util.HttpUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static google.maps.extraction.ResultFileExtractor.getCsv;

public class PlaceDetailsLoader {

    int numberResponses;
    private final static String path = ResultFileExtractor.resultFilePath;

    private final ExecutorService ex = Executors.newFixedThreadPool(1);

    public PlaceDetailsLoader() {
        String[] dir = new File(path).list();
        numberResponses = dir == null ? 0 : dir.length;
    }

    public void put(String url) {
        ex.submit(() -> get(url));
    }

    void get(String url) {

        String response = HttpUtil.getByUrlConnection(url, 10);
        if (response.contains("[\"hindu_temple\"]")) {
            MapPlaceDetailsResponseExtractor.extract(response).ifPresent(b -> {
                writeToFile(response, numberResponses);
                appendToFile(getCsv(b));
                incResponses();
            });
        }

    }

    private void incResponses() {
        numberResponses++;
    }

    private void appendToFile(String s) {
        try (FileWriter w = new FileWriter(path + ".csv", StandardCharsets.UTF_8, true)) {
            w.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToFile(String s, int fileId) {
        try (FileWriter w = new FileWriter(path + "/" + numberResponses + ".txt")) {
            w.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
