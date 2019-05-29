package ddsl.dcomponents;

import ddsl.dobjects.DWait;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TestRunData;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DComponent {

	protected WebDriver driver;
	public DWait wait;
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	protected TestRunData data = new TestRunData();

	public DComponent(WebDriver driver) {
		this.driver = driver;
		this.wait = new DWait(driver);
	}

	public void clickVoidSpace() {
		By selector = By.cssSelector("[class*=\"overlay-backdrop\"]");
		try {
			((JavascriptExecutor) driver).executeScript("document.querySelector('[class*=\"overlay-backdrop\"]').click()");
		} catch (Exception e) {	}

		wait.forElementToBeGone(driver.findElement(selector));

	}

}
