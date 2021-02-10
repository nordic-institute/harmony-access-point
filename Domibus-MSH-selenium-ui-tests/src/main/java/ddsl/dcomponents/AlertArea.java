package ddsl.dcomponents;

import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class AlertArea extends DComponent {

	@FindBy(id = "alertmessage_id")
	public WebElement alertMessage;
	@FindBy(css = "#alertmessage_id > span.closebtn")
	public WebElement closeButton;
	@FindBy(tagName = "snack-bar-container")
	public WebElement alertContainer;

	public AlertArea(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public void waitForAlert(){
		try {
			wait.forElementToBeVisible(alertContainer);
		} catch (Exception e) {
		}
	}

	public void closeAlert() throws Exception{
		weToDobject(closeButton).click();
	}

	public String getAlertMessage() {
		try {
			wait.forElementToBeVisible(alertMessage, true);
		} catch (Exception e) {
		}
		DObject alertObject = new DObject(driver, alertMessage);

		if (!alertObject.isPresent()) {
			log.debug("No messages displayed.");
			return null;
		}

		String messageTxt = alertMessage.getText().replace(closeButton.getText(), "").replaceAll("\n", "").trim();

		log.debug("messageTxt = " + messageTxt);

		log.debug("Getting alert message ...");
		return messageTxt.trim();
	}

	public boolean isError() throws Exception {
		try {
			wait.forElementToBeVisible(alertContainer, true);
		} catch (Exception e) {
		}

		DObject alertObject = new DObject(driver, alertContainer);

		if (alertObject.isPresent()) {
			return (alertObject.getAttribute("class").contains("error"));
		}
		throw new Exception("Alert message not present");
	}

	public boolean isShown() throws Exception {
		try {
			wait.forElementToBeVisible(alertContainer, true);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	
	
}
