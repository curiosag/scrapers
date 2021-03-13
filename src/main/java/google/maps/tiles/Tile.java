package google.maps.tiles;

import google.maps.Point;

import java.awt.image.BufferedImage;

public class Tile {
    private static final int tileWidth = 256;

    private static final double R2D = 180 / Math.PI;
    private static final double D2R = Math.PI / 180;
    private static final int tilesize = 256;
    public static final double PiDouble = 2 * Math.PI;
    public static final double PiHalf = 0.5 * Math.PI;

    private final int tileX;
    private final int tileY;
    private final int zoom;
    private final BufferedImage image;

    public Tile(int tileX, int tileY, int zoom, BufferedImage image) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.zoom = zoom;
        this.image = image;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public int getZoom() {
        return zoom;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Point unproject(int x, int y) {
        int globalX = tileX * tileWidth + x;
        int globalY = tileY * tileWidth + y;

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
