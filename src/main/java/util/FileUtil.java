package util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    public static String readString(String path){
        return new String(readBytes(path), StandardCharsets.UTF_8);
    }

    public static void writeString(String value, String targetPath) {
        try {
            FileWriter w = new FileWriter(targetPath, false);
            w.write(value);
            w.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readBytes(String path)  {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}


