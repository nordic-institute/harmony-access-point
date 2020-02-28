package ddsl.dcomponents;

import ddsl.dobjects.*;
import ddsl.dobjects.multi_select.MultiSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
			if (driver.findElement(selector) != null)
			((JavascriptExecutor) driver).executeScript("document.querySelector('[class*=\"overlay-backdrop\"]').click()");
			wait.forElementToBeGone(driver.findElement(selector));
		} catch (Exception e) {	}
	}

	public WebDriver getDriver() {
		return driver;
	}

	protected DButton weToDButton(WebElement element){
		return new DButton(driver, element);
	}

	protected Checkbox weToCheckbox(WebElement element){
		return new Checkbox(driver, element);
	}

	protected Select weToSelect(WebElement element){
		return new Select(driver, element);
	}

	protected MultiSelect weToMultiSelect(WebElement element){
		return new MultiSelect(driver, element);
	}

	protected DatePicker weToDatePicker(WebElement element){
		return new DatePicker(driver, element);
	}

	protected DObject weToDobject(WebElement element){
		return new DObject(driver, element);
	}

	protected DLink weToDLink(WebElement element){
		return new DLink(driver, element);
	}

	protected DInput weToDInput(WebElement element){
		return new DInput(driver, element);
	}



}
