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

import java.util.concurrent.TimeUnit;


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
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
		return driver;
	}

	private static WebDriver getFirefoxDriver() {
		System.setProperty("webdriver.gecko.driver", data.getFirefoxDriverPath());
		WebDriver driver = new FirefoxDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);

		return driver;
	}

	private static Proxy getProxy() {
		String proxyAddress = data.getProxyAddress();
		Proxy proxy = new Proxy();
		proxy.setHttpProxy(proxyAddress).setSslProxy(proxyAddress);
		return proxy;
	}


}
