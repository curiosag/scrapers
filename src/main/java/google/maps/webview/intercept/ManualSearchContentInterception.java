package google.maps.webview.intercept;

import google.maps.extraction.ResultFileExtractor;
import google.maps.webview.datasink.FileWriter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static google.maps.extraction.MapPlaceDetailsResponseExtractor.extractFromMapSearchResult;

public class ManualSearchContentInterception implements ContentInterception {
    private final String targetPath = "./manualSearchResponses";
    private final FileWriter writer = new FileWriter(targetPath);

    @Override
    public void onContentReceived(String url, byte[] c) {
        String text = new String(c, StandardCharsets.UTF_8);
        if (text.contains("SearchResult.TYPE_")) {
            List<String> csv = extractFromMapSearchResult(text).stream()
                    .map(ResultFileExtractor::getCsv)
                    .collect(Collectors.toList());
            writer.writeCsvRecords(csv);
        }
    }

    @Override
    public boolean urlWanted(String r) {
        return r.contains("search?tbm=map");
    }
}
