package selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Closeable;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class Driver implements Closeable {

    public final WebDriver webDriver;
int[] a ={};

    public Driver(String proxyUrl, String proxPort) {
        webDriver = createChromeDriver(proxyUrl, proxPort);
    }

    public static WebDriver createFirefoxDriver(String proxyUrl, String proxPort) {
        System.setProperty("webdriver.gecko.driver", "/home/ssm/dev/tool/chromedriver/geckodriver/geckodriver-v0.24.0-linux64/geckodriver");

        FirefoxOptions options = new FirefoxOptions();

/*        Proxy proxy = new Proxy();

        proxy.setHttpProxy(proxyUrl + ':' + proxPort);
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability(CapabilityType.PROXY, proxy);
*/
        FirefoxDriver driver = new FirefoxDriver();
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);

        return driver;
    }

    public static WebDriver createChromeDriver(String proxyUrl, String proxPort) {
        System.setProperty("webdriver.chrome.driver", "/home/ssm/dev/tool/chromedriver/74/chromedriver");

        ChromeOptions options = new ChromeOptions();

        options.addExtensions(new File("/home/ssm/dev/tool/chromedriver/extensions/tunnelbaervpn/extension_3_2_8_0.crx"));
        options.addExtensions(new File("/home/ssm/dev/tool/chromedriver/extensions/showip/extension_1_3_1_0.crx"));
        options.addExtensions(new File("/home/ssm/dev/tool/chromedriver/extensions/proxyswitcher/extension_2_5_20_0.crx"));

        if (proxyUrl != null) {
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyUrl + ':' + proxPort);
            options.setProxy(proxy);
        }

        options.addArguments("start-maximized");
        options.addArguments("disable-infobars");
        //options.addArguments("--disable-extensions");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--blink-settings=imagesEnabled=false");
        //options.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);

        return driver;
    }

    public String getPageSource() {
        return webDriver.getPageSource();
    }

    public void get(String url) {
        webDriver.get(url);
    }

    public WebElement waitFor(String xpath) {
        WebDriverWait wait = new WebDriverWait(webDriver, 60000);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    @Override
    public void close() {
        webDriver.quit();
    }
}
