package util;

import java.lang.management.ManagementFactory;

public class ProcessUtil {

    public static String getPid() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

}
