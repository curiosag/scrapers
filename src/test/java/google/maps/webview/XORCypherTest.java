package google.maps.webview;

import google.maps.webview.intercept.XORCypher;
import org.junit.Test;

import static org.junit.Assert.*;

public class XORCypherTest {

    @Test
    public void encryptSingle() {
        byte[] b = bytes(1);

        for (int i = 0; i < 255; i++) {
            assertArrayEquals(b,  XORCypher.encryptSingle(XORCypher.encryptSingle(b, i), i));
        }

    }

    @Test
    public void name() {

    }

    public byte[] bytes(int i) {
        byte[] conv = new byte[1];
        conv[0] = (byte) i;
        return conv;
    }
}