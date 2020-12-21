package util;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


public final class HttpUtil {

    private static final int HTTP_TIMEOUT = 10000;
    static WebDriver driver = null;

    private static  WebDriver getDriver() {
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
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static synchronized String getBySelenium(String url){
        return getBySelenium(url, 6000);
    }

    public static synchronized String getBySelenium(String url, int timeout) {
        driver= getDriver();
        driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.MILLISECONDS);
                driver.get(url);
        return getDriver().getPageSource();
    }

}
