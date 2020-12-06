package google.maps.webview;

import javafx.geometry.Bounds;
import javafx.scene.web.WebView;

import static google.maps.webview.Const.mapOffsetX;

class MapScreenMeasures {
    public final float screenOffsetX;
    public final float screenOffsetY;
    public final float minX;
    public final float minY;
    public final float maxX;
    public final float maxY;
    public final float mapCenterX;
    public final float width;
    public final float height;

    public MapScreenMeasures(WebView v) {
        Bounds bounds = v.getBoundsInLocal();
        Bounds screenBounds = v.localToScreen(bounds);
        screenOffsetX = (float) screenBounds.getMinX();
        screenOffsetY = (float) screenBounds.getMinY();
        minX = screenOffsetX + mapOffsetX;
        minY = screenOffsetY;
        maxX = (float) screenBounds.getMaxX();
        maxY = (float) screenBounds.getMaxY();
        width = (float) bounds.getWidth() - mapOffsetX;
        height = (float) screenBounds.getHeight();
        mapCenterX = mapOffsetX + width / 2;
    }

}
