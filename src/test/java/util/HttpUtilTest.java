package util;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

import static util.HttpUtil.getImageByUrl;

public class HttpUtilTest {

    @Test
    public void testGetMapsPng() throws IOException {
        String url = "https://maps.google.com/maps/vt?z=15&x=%d&y=%d";
        int startX = 23540;
        int startY = 15209;

        //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("103.134.18.209", 1080));

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int tileX = startX + i;
                int tileY = startY + j;
                var img = getImageByUrl(getUrl(), 5000, null);
                ImageIO.write(img, "png", new File(String.format("./src/test/resources/tiles/tile_%d_%d.png", tileX, tileY)));
            }
        }

    }

    String getUrl() {
        return "https://maps.google.com/maps/vt?z=15&x=24167&y=13718";
    }
}