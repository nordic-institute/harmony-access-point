package utils;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.concurrent.TimeUnit;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DriverManager {

	static TestRunData data = new TestRunData();

	public static WebDriver getDriver() {
		if (StringUtils.equalsIgnoreCase(data.getRunBrowser(), "chrome")){
			return getChromeDriver();
		}else if (StringUtils.equalsIgnoreCase(data.getRunBrowser(), "firefox")){
			return getFirefoxDriver();
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


}
