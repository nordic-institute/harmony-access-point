package utils;

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

	public static WebDriver getDriver() {

		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
//		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

		return driver;
	}


}
