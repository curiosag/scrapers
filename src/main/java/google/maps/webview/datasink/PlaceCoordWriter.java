package google.maps.webview.datasink;

import google.maps.webview.UrlCoordExtractor;

public class PlaceCoordWriter implements PlaceDataSink {

    private final FileWriter writer = new FileWriter("./scraped/scanned_coordinates");
    
    @Override
    public synchronized void put(String url) {
        UrlCoordExtractor.extract(url).ifPresent(
                c -> writer.writeCsvRecord(String.format("%.7f;%.7f\n", c.getLongitude(), c.getLatitude()))
        );
    }

}
