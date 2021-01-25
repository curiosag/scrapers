package google.maps.webview;

import google.maps.extraction.ResultFileExtractor;
import google.maps.responseparser.ResponseExtractor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static google.maps.extraction.ResultFileExtractor.getCsv;

public class ResultCsvExtractor {

    int numberResponses;
    private final static String path = ResultFileExtractor.resultFilePath;

    public ResultCsvExtractor() {
        String[] dir = new File(path).list();
        numberResponses = dir == null ? 0 : dir.length;
    }

    public synchronized void onResponse(String response) {

        if (response.contains("[\"hindu_temple\"]")) {
            ResponseExtractor.extract(response).ifPresent(b -> {
                writeToFile(response, numberResponses);
                writeToFile(getCsv(b));
                incResponses();
            });
        }

    }

    private void incResponses() {
        numberResponses++;
    }

    private void writeToFile(String s) {
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
