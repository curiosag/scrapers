package google.maps.webview;

import java.util.Timer;
import java.util.TimerTask;

public class StallingMonitor {
    private final static long threshold = 1000 * 30;

    public long lastProgressEvent = currentTime(); // millis
    private final Runnable shutdown;

    Timer timer = new Timer();

    public StallingMonitor(Runnable shutdown) {
        this.shutdown = shutdown;
        monitor();
    }

    public synchronized void signalProgress() {
        lastProgressEvent = currentTime();
    }

    private void monitor() {
        if (currentTime() - lastProgressEvent > threshold) {
            System.out.println("Scraper seems stalling. Terminating.");
            shutdown.run();
        } else {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    monitor();
                }
            }, threshold);
        }
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }
}
