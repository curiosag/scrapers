package util;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public final class HttpUtil {

    private static final int HTTP_TIMEOUT = 10000;
    static WebDriver driver = null;

    private static WebDriver getDriver() {
        if (driver == null) {
            System.setProperty("webdriver.chrome.driver", "/home/ssm/dev/tool/chromedriver/74/chromedriver");

            ChromeOptions options = new ChromeOptions();
            options.addArguments("headless");
            driver = new ChromeDriver(options);
        }
        return driver;
    }

    public static Document getJsoupDoc(String url, boolean jsEnabled) {

        try {
            String s = jsEnabled ? getBySelenium(url) : getByUrlConnection(url);
            if (s != null) {
                return Jsoup.parse(s, url);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static String getByUrlConnection(String url) {
        return getByUrlConnection(url, HTTP_TIMEOUT);
    }

    public static String getByUrlConnection(String url, int timeout) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(timeout);
            conn.connect();
            return IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /*
    * https://maps.google.com/maps/vt?z=15&x=24167&y=13718
    */

    public static BufferedImage getImageByUrl(String url, int timeout, Proxy proxy) throws IOException {
        HttpURLConnection conn;
        if (proxy == null)
            conn = (HttpURLConnection) new URL(url).openConnection();
        else
            conn = (HttpURLConnection) new URL(url).openConnection(proxy);

        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(timeout);
        conn.connect();

        ImageInputStream stream = ImageIO.createImageInputStream(conn.getInputStream());
        ImageReader reader = ImageIO.getImageReaders(stream).next();
        reader.setInput(stream);

        int width = reader.getWidth(0);
        int height = reader.getHeight(0);

        ImageReadParam param = reader.getDefaultReadParam();

        param.setSourceRegion(new Rectangle(0, 0, width, height));
        return reader.read(0, param);
    }

    public static void saveResponse(String url, int timeout, Proxy proxy, String targetPath) throws IOException {
        HttpURLConnection conn;
        if (proxy == null)
            conn = (HttpURLConnection) new URL(url).openConnection();
        else
            conn = (HttpURLConnection) new URL(url).openConnection(proxy);

        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(timeout);
        conn.connect();

        try(FileOutputStream out = new FileOutputStream(targetPath)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = conn.getInputStream().read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public static synchronized String getBySelenium(String url) {
        return getBySelenium(url, 6000);
    }

    public static synchronized String getBySelenium(String url, int timeout) {
        driver = getDriver();
        driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.MILLISECONDS);
        driver.get(url);
        return getDriver().getPageSource();
    }

}
