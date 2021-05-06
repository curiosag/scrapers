package google.maps.webview.intercept;

public interface ContentInterception {
    void onContentReceived(String url, byte[] c);

    boolean urlWanted(String r);
}
