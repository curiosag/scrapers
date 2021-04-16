package google.maps.webview.scrapejob;

import google.maps.Point;
import google.maps.ScrapeArea;

public class ScrapeJob {
    public final int id;
    public final ScrapeArea scrapeArea;
    private Point currentPosition;
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
            store.setProgress(id, p, getPctLatitudeDone());
            if(done)
            {
                store.releaseJob(id, true, null);
            }
        }
    }

    public boolean isDone() {
        return exceedsSouth(currentPosition);
    }

    public boolean exceedsSouth(Point p) {
        return scrapeArea.exceedsSouth(p);
    }

    public void release(String error) {
        store.releaseJob(id, isDone(), error);
    }

    public double getPctLatitudeDone() {
        double north = scrapeArea.getNorthernMost().lat;
        double south = scrapeArea.getSouthernMost().lat;
        double curr = currentPosition.lat;
        return ((north - curr) / (north - south)) * 100;
    }
}
