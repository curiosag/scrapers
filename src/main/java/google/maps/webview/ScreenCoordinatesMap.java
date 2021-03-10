package google.maps.webview;

import javafx.scene.web.WebView;

public class ScreenCoordinatesMap {
    public static final float offsetX = 80; // avoid the menu in the left upper corner, the browser gets totally confused
                                            // if the hidden menu gets triggered and actually pops up
    public static final float offsetY = 0; // for the window border

    public final float x;
    public final float y;
    public final float w;
    public final float h;
    public final float windowX;
    public final float windowY;

    public ScreenCoordinatesMap(WebView v) {
        windowX = (float) v.getScene().getWindow().getX();
        windowY = (float) v.getScene().getWindow().getY();

        x = windowX + offsetX;
        y = windowY + offsetY;
        w = ((float) v.getWidth()) - offsetX;
        h = (float) v.getHeight() - offsetY;
    }
}
