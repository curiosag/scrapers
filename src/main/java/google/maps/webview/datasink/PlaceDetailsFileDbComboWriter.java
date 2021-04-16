package google.maps.webview.datasink;

import google.maps.extraction.MapPlaceDetailsResponseExtractor;
import google.maps.webview.ProcessingType;
import util.HttpUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static google.maps.extraction.ResultFileExtractor.getCsv;

public class PlaceDetailsFileDbComboWriter implements PlaceDataSink {

    PlaceDao dao = new PlaceDao();
    private final ExecutorService ex = Executors.newFixedThreadPool(1);
    private final FileWriter writer;

    public PlaceDetailsFileDbComboWriter(String path) {
        writer = new FileWriter(path);
    }

    @Override
    public synchronized void put(String url) {
        ex.submit(() -> process(url));
    }

    void process(String url) {
        String response = HttpUtil.getByUrlConnection(url, 10000);
        if (response.contains(ProcessingType.marker_temple.place_type)) {
            long id = dao.getNextId();
            writer.writeResponse(id, response);
            MapPlaceDetailsResponseExtractor.extractFromMapKlickResult(response).ifPresent(r -> {
                r.setId(id);
                dao.add(r);
                writer.writeCsvRecord(getCsv(r));
            });
        }
    }

}
