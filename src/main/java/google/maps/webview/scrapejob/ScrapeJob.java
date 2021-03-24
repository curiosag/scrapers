package google.maps.webview.scrapejob;

import google.maps.Point;
import google.maps.ScrapeArea;

public class ScrapeJob {
    public final int id;
    private Point currentPosition;
    public final ScrapeArea scrapeArea;
    private final ScrapeJobStore store;

    public ScrapeJob(int id, Point currentPosition, ScrapeArea scrapeArea, ScrapeJobStore store) {
        this.id = id;
        this.scrapeArea = scrapeArea;
        this.currentPosition = currentPosition;
        this.store = store;
    }

    public Point getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Point p) {
        boolean done = exceedsSouth(p);
        if (scrapeArea.contains(p) || done) {
            currentPosition = p;
            store.setProgress(id, p);
            if(done)
            {
                store.releaseJob(id, true);
            }
        }
    }

    public boolean isDone() {
        return exceedsSouth(currentPosition);
    }

    public boolean exceedsSouth(Point p) {
        return scrapeArea.exceedsSouth(p);
    }

    public boolean areaContains(Point p) {
        return scrapeArea.contains(p);
    }

    public void release() {
        store.releaseJob(id, isDone());
    }
}
