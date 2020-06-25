package utils.driver;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import utils.TestRunData;

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
		
		ChromeOptions options = new ChromeOptions();
		if (data.useProxy()) {
			options.setCapability(CapabilityType.PROXY, getProxy());
		}
		
		return new ChromeDriver(options);
	}
	
	private static WebDriver getFirefoxDriver() {
		System.setProperty("webdriver.gecko.driver", data.getFirefoxDriverPath());
		
		FirefoxOptions options = new FirefoxOptions();
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
