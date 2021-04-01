package google.maps.webview;

import google.maps.PixelCoordinate;
import org.junit.Test;

import static org.junit.Assert.*;

public class ScanChunkifyerTest {

    @Test
    public void getDistance() {
        assertEquals(1, ScanChunkifyer.getDistance(of(0,0), of(1,0)), 0.0001);
        assertEquals(1, ScanChunkifyer.getDistance(of(0,0), of(0,1)), 0.0001);
        assertEquals(Math.sqrt(200), ScanChunkifyer.getDistance(of(10,10), of(20,20)), 0.0001);
    }

    private PixelCoordinate of(int x, int y){
        return new PixelCoordinate(x, y);
    }
}