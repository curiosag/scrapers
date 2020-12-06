package google.maps.webview;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TrivialCookieStore implements CookieStore {
    private final Map<URI, List<HttpCookie>> cookies = new ConcurrentHashMap<>();

    @Override
    public void add(URI uri, HttpCookie cookie) {
        cookies.computeIfAbsent(uri, k -> new ArrayList<>());
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        List<HttpCookie> result = cookies.get(uri);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    @Override
    public List<HttpCookie> getCookies() {
        return cookies.entrySet().stream().flatMap(c -> c.getValue().stream()).collect(Collectors.toList());
    }

    @Override
    public List<URI> getURIs() {
        return new ArrayList<>(cookies.keySet());
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        return true;
    }

    @Override
    public boolean removeAll() {
        return true;
    }
}
