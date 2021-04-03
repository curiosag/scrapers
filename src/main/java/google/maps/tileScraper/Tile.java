package google.maps.tileScraper;

import google.maps.Point;

import java.awt.image.BufferedImage;

public class Tile implements CachedItem<Tile> {
    private int accessCount;
    private int expectedAccesses;

    private static final int tileWidth = 256;

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

    public Point unprojectTilePixelCoordinates(int x, int y) {
        int globalX = tileX * tileWidth + x;
        int globalY = tileY * tileWidth + y;

        return MercatorProjection.unproject(globalX, globalY, zoom);
    }

    @Override
    public int getAccessCount() {
        return accessCount;
    }

    @Override
    public void countAccess() {
        accessCount ++;
    }

    @Override
    public Tile setExpectedAccesses(int expected) {
        expectedAccesses = expected;
        return this;
    }

    @Override
    public int getExpectedAccesses() {
        return expectedAccesses;
    }
}
