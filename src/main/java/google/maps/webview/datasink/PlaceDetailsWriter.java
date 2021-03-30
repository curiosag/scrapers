package google.maps.webview.datasink;

import google.maps.extraction.MapPlaceDetailsResponseExtractor;
import util.HttpUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static google.maps.extraction.ResultFileExtractor.getCsv;

public class PlaceDetailsWriter implements PlaceDataSink {

    private final ExecutorService ex = Executors.newFixedThreadPool(1);
    private final FileWriter writer;
    public PlaceDetailsWriter() {
        writer = new FileWriter("./scraped/responses");
    }

    @Override
    public synchronized void put(String url) {
        ex.submit(() -> process(url));
    }

    void process(String url) {
        String response = HttpUtil.getByUrlConnection(url, 10000);
        if (response.contains("[\"hindu_temple\"]")) {
            MapPlaceDetailsResponseExtractor.extract(response).ifPresent(b -> {
                writer.writeResponse(response);
                writer.writeCsvRecord(getCsv(b));
            });
        }
    }





}
