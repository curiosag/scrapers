package google.maps.webview.scrapejob;

import google.maps.Point;

import java.util.Optional;

public interface ScrapeJobStore {

    Optional<ScrapeJob> getNext();

    void setProgress(int jobId, Point currentPosition);

    void releaseJob(int id, boolean done);
}
