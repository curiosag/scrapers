package util;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static util.HttpUtil.getImageByUrl;

public class HttpUtilTest {

    @Test
    public void testGetMapsPng() throws IOException {
        String url = "https://maps.google.com/maps/vt?z=15&x=%d&y=%d";
        int startX = 23540;
        int startY = 15209;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int tileX = startX + i;
                int tileY = startY + j;
                var img = getImageByUrl(String.format(url, tileX, tileY), 5000);
                ImageIO.write(img, "png", new File(String.format("./src/test/resources/tiles/tile_%d_%d.png", tileX, tileY)));
            }
        }

    }
}