package utils;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;

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
		} else if (StringUtils.equalsIgnoreCase(data.getRunBrowser(), "edge")) {
			return getEdgeDriver();
		}
		return getChromeDriver();
	}

	private static WebDriver getChromeDriver() {
		System.setProperty("webdriver.chrome.driver", data.getChromeDriverPath());
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		return driver;
	}

	private static WebDriver getFirefoxDriver() {
		System.setProperty("webdriver.gecko.driver", data.getFirefoxDriverPath());
		WebDriver driver = new FirefoxDriver();
		driver.manage().window().maximize();
		return driver;
	}

	private static WebDriver getEdgeDriver() {

		System.setProperty("webdriver.edge.driver", "MicrosoftWebDriver.exe");
		WebDriver driver = new EdgeDriver();

		driver.manage().window().maximize();
		return driver;
	}


}
