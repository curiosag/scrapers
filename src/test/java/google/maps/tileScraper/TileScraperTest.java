package google.maps.tileScraper;

import google.maps.dao.FastScanDao;
import org.junit.Test;

public class TileScraperTest {

    @Test
    public void run() throws InterruptedException {
        TileScraper s = new TileScraper(24139 ,13682 ,24250 ,13888, 15,  3, new FastScanDao());
        s.run();
    }
}