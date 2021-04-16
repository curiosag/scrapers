package google.maps.webview.intercept;

import google.maps.extraction.ResultFileExtractor;
import google.maps.webview.datasink.FileWriter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static google.maps.extraction.MapPlaceDetailsResponseExtractor.extractFromMapSearchResult;

public class ManualSearchUrlLoaderInterception {

    private static final String targetPath = "./manualSearchResponses";

    private static final FileWriter writer = new FileWriter(targetPath);
    private static final Map<String, List<byte[]>> content = new ConcurrentHashMap<>();

    public static void setUp() {

        URLLoaderInterceptor.onSendRequest = (urlLoader, urlConnection) -> {
            String url = elicitUrl(urlLoader);
            if (isSearchUrl(url)) {
                content.put(url, new ArrayList<>());
            }
            return true;
        };

        URLLoaderInterceptor.onDidReceiveData = (urlLoader, data) -> {
            List<byte[]> bytes = content.get(elicitUrl(urlLoader));
            if (bytes != null) {
                byte[] current = new byte[data.limit()];
                data.duplicate().get(current);
                bytes.add(current);
            }
        };

        URLLoaderInterceptor.onFinishedLoading = urlLoader -> {
            List<byte[]> bytes = content.get(elicitUrl(urlLoader));
            if (bytes != null) {
                int size = bytes.stream().map(Array::getLength).reduce(0, Integer::sum);
                byte[] target = new byte[size];

                int i = 0;
                for (byte[] bb : bytes) {
                    for (byte b : bb) {
                        target[i] = b;
                        i++;
                    }
                }
                content.remove(elicitUrl(urlLoader));
                onContentReceived(new String(target, StandardCharsets.UTF_8));
            }
        };
    }

    private static String elicitUrl(Object urlLoader) {
        try {
            Field fUrl = urlLoader.getClass().getDeclaredField("url");
            fUrl.setAccessible(true);
            String url = (String) fUrl.get(urlLoader);
            return url;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void onContentReceived(String c) {
        if (c.contains("SearchResult.TYPE_")) {
            List<String> csv = extractFromMapSearchResult(c).stream()
                    .map(ResultFileExtractor::getCsv)
                    .collect(Collectors.toList());
            writer.writeCsvRecords(csv);
        }
    }

    private static boolean isSearchUrl(String r) {
        return r.contains("search?tbm=map");
    }

}
