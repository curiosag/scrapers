package google.maps.webview.cs_remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import google.maps.Point;
import google.maps.webview.scrapejob.ScrapeJob;
import google.maps.webview.scrapejob.ScrapeJobStore;

import java.util.Optional;

public class RemoteScrapeJobStore implements ScrapeJobStore {

    @Override
    public Optional<ScrapeJob> getNext() {

        return Optional.empty();
    }

    @Override
    public void setProgress(int jobId, Point currentPosition, double pctLatitudeDone) {
        byte[] msg = serialize(new JobProgressDto(jobId, currentPosition.lon, currentPosition.lat, pctLatitudeDone));
    }

    @Override
    public void releaseJob(int id, boolean done, String error) {
        byte[] msg = serialize(new ReleaseJobDto(id, done, error));

    }

    private byte[] serialize(Object o){
        try {
          return  new ObjectMapper().writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException();
        }
    }

}
