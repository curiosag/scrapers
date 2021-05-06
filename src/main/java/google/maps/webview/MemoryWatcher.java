package google.maps.webview;

import google.maps.webview.scrapejob.ScrapeJob;

import static google.maps.webview.Log.err;
import static google.maps.webview.Log.log;

public class MemoryWatcher {

    private static final int mb = 1024 * 1024;

    public static void watch(ScrapeJob job, float maxMb) {
        System.gc();
        if (exceedsMemBoundary(job.id, maxMb)) {
            job.release(null);
            System.exit(0);
        }
    }

    private static boolean exceedsMemBoundary(int jobId, float maxMb) {
        Runtime instance = Runtime.getRuntime();
        if (Math.toIntExact(instance.totalMemory() / (1024 * 1024)) > maxMb) {
            err("***** Shutting down job " + jobId + " for reastart. Ate too much memory. *****\n");
            err("***** Heap utilization statistics [MB] *****\n");
            err("Total Memory: " + instance.totalMemory() / mb);
            err("Free Memory: " + instance.freeMemory() / mb);
            err("Used Memory: " + (instance.totalMemory() - instance.freeMemory()) / mb);
            err("Max Memory: " + instance.maxMemory() / mb);
            return true;
        }
        return false;
    }

}
