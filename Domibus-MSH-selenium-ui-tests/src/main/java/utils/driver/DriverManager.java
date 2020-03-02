package utils.driver;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import utils.TestRunData;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DriverManager {

	static TestRunData data = new TestRunData();

	public static WebDriver getDriver() {
		if (StringUtils.equalsIgnoreCase(data.getRunBrowser(), "chrome")) {
			return getChromeDriver();
		} else if (StringUtils.equalsIgnoreCase(data.getRunBrowser(), "firefox")) {
			return getFirefoxDriver();
		}

		return getChromeDriver();
	}

	private static WebDriver getChromeDriver() {
		System.setProperty("webdriver.chrome.driver", data.getChromeDriverPath());
//		ChromeOptions options = new ChromeOptions();
//		options.addArguments("window-size=1920,1080");
//		options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
//		options.addArguments("--ignore-certificate-errors");
//		if (data.useProxy()) {
//			options.setCapability(CapabilityType.PROXY, getProxy());
//		}
//		options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
//		options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS,true);
//		options.setCapability(ChromeOptions.CAPABILITY, options);
//		options.setHeadless(data.isHeadless());
//		WebDriver driver = new ChromeDriver(options);
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		return driver;
	}

	private static WebDriver getFirefoxDriver() {
		System.setProperty("webdriver.gecko.driver", data.getFirefoxDriverPath());
//		System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
//		System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
//
//		FirefoxOptions options = new FirefoxOptions();
//		options.addArguments("window-size=1920,1080");
//		options.setHeadless(data.isHeadless());
//		if (data.useProxy()) {
//			options.setCapability(CapabilityType.PROXY, getProxy());
//		}
//		options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
//		options.addArguments("--ignore-certificate-errors");
//		options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
//		options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS,true);
//		options.setCapability(ChromeOptions.CAPABILITY, options);

//		WebDriver driver = new FirefoxDriver(options);
		WebDriver driver = new FirefoxDriver();
		driver.manage().window().maximize();
		return driver;
	}

	private static Proxy getProxy() {
		String proxyAddress = data.getProxyAddress();
		Proxy proxy = new Proxy();
		proxy.setHttpProxy(proxyAddress).setSslProxy(proxyAddress);
		return proxy;
	}


}
