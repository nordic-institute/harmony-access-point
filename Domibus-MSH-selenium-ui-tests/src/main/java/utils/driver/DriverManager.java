package utils.driver;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import utils.DFileUtils;
import utils.TestRunData;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DriverManager {
	
	static TestRunData data = new TestRunData();
	
	
	public static WebDriver getDriver() {
		
		WebDriver driver;
		if (StringUtils.equalsIgnoreCase(data.getRunBrowser(), "firefox")) {
			driver = getFirefoxDriver();
		} else {
			driver = getChromeDriver();
		}
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		return driver;
	}
	
	private static WebDriver getChromeDriver() {
		System.setProperty("webdriver.chrome.driver", data.getChromeDriverPath());
		
		
		//Code added for auto download
		HashMap<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("profile.default_content_settings.popups", 0);
		prefs.put("download.default_directory", data.downloadFolderPath());
		prefs.put("safebrowsing.enabled", "true");
		
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
		options.setHeadless(data.isHeadless());
		options.addArguments("--disable-popup-blocking");
		
		options.setExperimentalOption("prefs", prefs);
		
		if (data.useProxy()) {
			options.setCapability(CapabilityType.PROXY, getProxy());
		}
		
		return new ChromeDriver(options);
	}
	
	private static WebDriver getFirefoxDriver() {
		System.setProperty("webdriver.gecko.driver", data.getFirefoxDriverPath());
		
		FirefoxOptions options = new FirefoxOptions();
		
		//code added for auto download
		options.addPreference("browser.download.folderList", 2);
		options.addPreference("browser.download.manager.showWhenStarting",false);
		options.addPreference("browser.download.dir", data.downloadFolderPath());
		options.addPreference("browser.helperApps.neverAsk.openFile","application/ms-excel text/xml");
		options.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/ms-excel text/xml");
		
		if (data.useProxy()) {
			options.setCapability(CapabilityType.PROXY, getProxy());
		}
		return new FirefoxDriver(options);
	}
	
	private static Proxy getProxy() {
		String proxyAddress = data.getProxyAddress();
		Proxy proxy = new Proxy();
		proxy.setHttpProxy(proxyAddress).setSslProxy(proxyAddress);
		return proxy;
	}
	
	
}
