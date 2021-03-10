package google.maps.tiles;

import google.maps.Point;
import org.junit.Test;

import static google.maps.tiles.IOUtil.getResourcePath;
import static google.maps.tiles.Tile.project;

public class TileTest {
    private static final int zoom = 15;

    // for https://maps.google.com/maps/vt?z=15&x=23548&y=15209
    // taken from https://medium.com/google-design/google-maps-cb0326d165f5
    @Test
    public void testUnproject() {
        int tileWidth = 256;
        int ty = 23547;
        int tx = 15213;

        int pixelX = tx * tileWidth + 25;
        int pixelY = ty * tileWidth + 95;
        //pixelX =3894624;3894537
        //pixelY =6028153;6028125
        //Point p = unproject(pixelX, pixelY, zoom);
        String p = getResourcePath();
        System.out.println(p.toString());
    }

    @Test
    public void testProject() {

        double lat = 12.7542;
        double lon = 78.6986;
        Point p = project(lat, lon, zoom);
        System.out.println(p.toString());
    }



}