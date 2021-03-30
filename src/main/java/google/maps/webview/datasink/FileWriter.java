package google.maps.webview.datasink;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileWriter {
    int numberResponses;

    private final String path;

    public FileWriter(String path) {
        this.path = path;
        String[] dir = new File(this.path).list();
        numberResponses = dir == null ? 0 : dir.length;
    }

    void writeCsvRecord(String csv) {
        try (java.io.FileWriter w = new java.io.FileWriter(path + ".csv", StandardCharsets.UTF_8, true)) {
            w.write(csv);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void writeResponse(String s) {
        try (java.io.FileWriter w = new java.io.FileWriter(path + "/" + numberResponses + ".txt")) {
            w.write(s);
            numberResponses++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
