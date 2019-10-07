package ddsl.dobjects;

import org.openqa.selenium.*;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DObject {

	protected WebDriver driver;
	protected DWait wait;

	public WebElement element;

	public DObject(WebDriver driver, WebElement element) {
		wait = new DWait(driver);
		this.driver = driver;
		this.element = element;
	}

	public boolean isPresent() {
		try {
			wait.forElementToBe(element);
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean isEnabled() throws Exception {
		if (isPresent()) {
			wait.forElementToBeEnabled(element);
			return element.isEnabled();
		}
		throw new DObjectNotPresentException();
	}

	public boolean isVisible() throws Exception {
		if (isPresent()) {
			wait.forElementToBeEnabled(element);
			return element.isDisplayed();
		}
		throw new DObjectNotPresentException();
	}

	public String getText() throws Exception {
		if (!isPresent()) {
			throw new DObjectNotPresentException();
		}
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element);
		String text = ((JavascriptExecutor) driver).executeScript("return arguments[0].innerText;", element).toString();
		return text.trim();
	}

	public void click() throws Exception {
		if (isEnabled()) {
			wait.forElementToBeClickable(element).click();
		} else {
			throw new Exception("Not enabled");
		}
	}

	public String getAttribute(String attributeName) throws Exception {
		if (isPresent()) {
			return element.getAttribute(attributeName).trim();
		}
		throw new DObjectNotPresentException();
	}
	/*
	 * This Method is used to press Tab key
	 */
	public void pressTABKey(WebElement element) throws Exception {
		element.sendKeys(Keys.TAB);
		element.sendKeys(Keys.ENTER);
	}
}
