package ddsl.dobjects;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DObject {
	
	public WebElement element;
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	protected WebDriver driver;
	protected DWait wait;
	
	public DObject(WebDriver driver, WebElement element) {
		wait = new DWait(driver);
		this.driver = driver;
		this.element = element;
	}
	
	public boolean isPresent() {
		try {
			wait.forElementToBe(element);
			scrollIntoView();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public boolean isEnabled() throws Exception {
		if (isPresent()) {
			wait.forElementToBeEnabled(element);
			wait.forXMillis(100);
			return element.isEnabled();
		}
		throw new DObjectNotPresentException();
	}
	
	public boolean isDisabled() throws Exception {
		if (isPresent()) {
			wait.forElementToBeDisabled(element);
			return !element.isEnabled();
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
		scrollIntoView();
		String text = ((JavascriptExecutor) driver).executeScript("return arguments[0].innerText;", element).toString();
		return text.trim();
	}
	
	public void scrollIntoView() {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element);
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
			String attr = element.getAttribute(attributeName);
			if (attr == null) {
				log.debug("Attribute " + attributeName + " not found");
				return null;
			}
			return attr.trim();
		}
		throw new DObjectNotPresentException();
	}
	
	/*
	 * This Method is used to press Tab key
	 */
	public void pressTABKey() throws Exception {
		try {
			Robot robot = new Robot();
//			Simulate a key press
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_SHIFT);
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		
	}
}
