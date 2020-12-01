package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
    public static void write(String value, String targetPath) {
        try {
            File f = new File(targetPath);
            if (f.exists())
            {
                f.delete();
            }
            FileWriter myWriter = new FileWriter(targetPath);
            myWriter.write(value);
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


