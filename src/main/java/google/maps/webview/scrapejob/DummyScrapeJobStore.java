package google.maps.webview.scrapejob;

import google.maps.Point;

import java.util.Optional;

public class DummyScrapeJobStore implements ScrapeJobStore {

    public static final DummyScrapeJobStore instance = new DummyScrapeJobStore();

    public DummyScrapeJobStore() {
    }

    @Override
    public Optional<ScrapeJob> getNext() {
        return Optional.empty();
    }

    @Override
    public void setProgress(int jobId, Point currentPosition, double pctLatitudeDone) {
    }

    @Override
    public void releaseJob(int id, boolean done, String error) {

    }

}
