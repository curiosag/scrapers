package google.maps.tiles;

import google.maps.Point;

public class MercatorProjection {
    private static final double R2D = 180 / Math.PI;
    private static final double D2R = Math.PI / 180;
    private static final int tilesize = 256;
    public static final double PiDouble = 2 * Math.PI;
    public static final double PiHalf = 0.5 * Math.PI;

    public static Point unproject(long globalX, long globalY, int zoom) {
        var size = tilesize * Math.pow(2, zoom);
        var bc = (size / 360);
        var cc = (size / PiDouble);
        var zc = size / 2;
        var g = (globalY - zc) / -cc;
        var lon = (globalX - zc) / bc;
        var lat = R2D * (2 * Math.atan(Math.exp(g)) - PiHalf);
        return new Point(lat, lon);
    }

    public static Point project(double lat, double lon, int zoomLevel) {
        if(true)
            throw new IllegalStateException("probably x/y flipped");
        var size = tilesize * Math.pow(2, zoomLevel);
        var d = size / 2;
        var bc = (size / 360);
        var cc = (size / PiDouble);
        var ac = size;
        var f = Math.min(Math.max(Math.sin(D2R * lat), -0.9999), 0.9999);
        var x = d + lon * bc;
        var y = d + 0.5 * Math.log((1 + f) / (1 - f)) * -cc;
        x = Math.min(x, ac);
        y = Math.min(y, ac);
        return new Point(x, y);
    }

}
