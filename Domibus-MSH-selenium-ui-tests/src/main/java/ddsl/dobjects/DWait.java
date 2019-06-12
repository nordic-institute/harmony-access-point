package ddsl.dobjects;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.TestRunData;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DWait {

	public WebDriverWait webDriverWait;
	private TestRunData data = new TestRunData();


	public DWait(WebDriver driver) {
		this.webDriverWait = new WebDriverWait(driver, data.getTIMEOUT());
	}

	public void forXMillis(Integer millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public WebElement forElementToBeClickable(WebElement element) {
		return webDriverWait.until(ExpectedConditions.elementToBeClickable(element));
	}

	public WebElement forElementToBeVisible(WebElement element) {
		return webDriverWait.until(ExpectedConditions.visibilityOf(element));
	}

	public void forElementToBeEnabled(WebElement element) {
		int maxTimeout = data.getTIMEOUT() * 1000;
		int waitedSoFar = 0;
		while ((null != element.getAttribute("disabled")) && (waitedSoFar < maxTimeout)) {
			waitedSoFar += 300;
			forXMillis(300);
		}
	}

	public void forAttributeNotEmpty(WebElement element, String attributeName) {
		webDriverWait.until(ExpectedConditions.attributeToBeNotEmpty(element, attributeName));
	}

	public void forElementToBeGone(WebElement element) {
		try {
			webDriverWait.until(ExpectedConditions.not(ExpectedConditions.visibilityOf(element)));
		} catch (Exception e) {
		}
	}

	public void forElementToBe(WebElement element) {
		int secs = 0;
		while (secs < data.getTIMEOUT() * 10) {
			try {
				if (null != element.getText()) {
					break;
				}
			} catch (Exception e) {
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			secs++;
		}
	}

	public void forAttributeToContain(WebElement element, String attributeName, String value) {
		webDriverWait.until(ExpectedConditions.attributeContains(element, attributeName, value));
	}

	public void forElementToHaveText(WebElement element) {
		webDriverWait.until(new ExpectedCondition<Boolean>() {
			@NullableDecl
			@Override
			public Boolean apply(@NullableDecl WebDriver driver) {
				return !element.getText().trim().isEmpty();
			}
		});
	}


}
