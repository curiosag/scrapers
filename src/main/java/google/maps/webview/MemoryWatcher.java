package google.maps.webview;

import google.maps.webview.scrapejob.ScrapeJob;

import static google.maps.webview.Log.log;

public class MemoryWatcher {

    private static final int mb = 1024 * 1024;
    private static final int maxMb = 1024 * 12;

    public static void watch(ScrapeJob job) {
        if (exceedsMemBoundary(job.id)) {
            job.release();
            System.exit(0);
        }
    }

    private static boolean exceedsMemBoundary(int jobId) {
        Runtime instance = Runtime.getRuntime();
        if (Math.toIntExact(instance.totalMemory() / (1024 * 1024)) > maxMb) {
            log("***** Shutting down job " + jobId + " for reastart. Ate too much memory. *****\n");
            log("***** Heap utilization statistics [MB] *****\n");
            log("Total Memory: " + instance.totalMemory() / mb);
            log("Free Memory: " + instance.freeMemory() / mb);
            log("Used Memory: " + (instance.totalMemory() - instance.freeMemory()) / mb);
            log("Max Memory: " + instance.maxMemory() / mb);
            return true;
        }
        return false;
    }

}
