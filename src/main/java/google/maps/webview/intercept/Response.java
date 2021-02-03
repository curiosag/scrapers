package google.maps.webview.intercept;

import java.net.URLConnection;

public class Response {

    public final URLConnection urlConnection;
    public byte[] data;

    public Response(URLConnection urlConnection) {
        this.urlConnection = urlConnection;
    }

    public Response setData(byte[] data) {
        if (this.data == null)
            this.data = data;
        return this;
    }

}
