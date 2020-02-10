package utils.driver;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.TestRunData;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DriverManager {

    static TestRunData data = new TestRunData();

    static String downloadPath= System.getProperty("user.dir")+File.separator +"downloadFiles";


    public static WebDriver getDriver() {
        if (StringUtils.equalsIgnoreCase(data.getRunBrowser(), "chrome")) {
            return getChromeDriver();
        } else if (StringUtils.equalsIgnoreCase(data.getRunBrowser(), "firefox")) {
            return getFirefoxDriver();
        } else if (StringUtils.equalsIgnoreCase(data.getRunBrowser(), "edge")) {
            return getEdgeDriver();
        }
        return getChromeDriver();
    }

    private static WebDriver getChromeDriver() {
        System.setProperty("webdriver.chrome.driver", data.getChromeDriverPath());


        //Code added for auto download
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("profile.default_content_settings.popups", 0);
        File file =new File(downloadPath);
        file.mkdir();
        prefs.put("download.default_directory", downloadPath);
        prefs.put("safebrowsing.enabled", "true");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        options.setHeadless(data.isHeadless());
        options.addArguments("--disable-popup-blocking");

        options.setExperimentalOption("prefs", prefs);
        options.addArguments("window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        return driver;
    }

    private static WebDriver getFirefoxDriver() {
        System.setProperty("webdriver.gecko.driver", data.getFirefoxDriverPath());
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");

        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(data.isHeadless());

        //code added for auto download
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.manager.showWhenStarting",false);
        File file =new File(downloadPath);
        file.mkdir();
        options.addPreference("browser.download.dir",downloadPath);
        options.addPreference("browser.helperApps.neverAsk.openFile","application/ms-excel text/xml");
        options.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/ms-excel text/xml");

        options.addArguments("window-size=1920,1080");

        WebDriver driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        return driver;
    }

    private static WebDriver getEdgeDriver() {

        System.setProperty("webdriver.edge.driver", data.getEdgeDriverPath());
        WebDriver driver = new EdgeDriver();

        driver.manage().window().maximize();
        return driver;
    }





}
