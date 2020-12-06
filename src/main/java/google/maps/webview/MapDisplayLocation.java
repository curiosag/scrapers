package google.maps.webview;

import javafx.scene.web.WebView;

public class MapDisplayLocation {
    public final float x;
    public final float y;
    public final float w;
    public final float h;

    public MapDisplayLocation(WebView v) {
        float absx = (float) v.getScene().getWindow().getX();
        float absy = (float) v.getScene().getWindow().getY();
        float offsetX = 410; // vor the detail view in the map
        float offsetY = 40; // for the window border

        x = absx + offsetX;
        y = absy + offsetY;
         w = ((float) v.getWidth()) - offsetX;
         h = (float) v.getHeight();
    }
}
