package google.maps.webview.intercept;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UrlLoaderContentInterception {

    private static final Map<String, List<byte[]>> content = new ConcurrentHashMap<>();

    public static void setUp(ContentInterception c) {

        URLLoaderInterceptor.onSendRequest = (urlLoader, urlConnection) -> {
            String url = elicitUrl(urlLoader);
            if (c.urlWanted(url)) {
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
            String url = elicitUrl(urlLoader);
            List<byte[]> bytes = content.get(url);
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
                content.remove(url);
                c.onContentReceived(url, target);
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

}
