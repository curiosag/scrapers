package util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTrace {

    public static String get(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return t.getMessage() + "\n" + sw;
    }
}
