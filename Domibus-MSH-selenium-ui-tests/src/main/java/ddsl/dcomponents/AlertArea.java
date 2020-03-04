package ddsl.dcomponents;

import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


@SuppressWarnings("SpellCheckingInspection")
public class AlertArea extends DComponent {

	public AlertArea(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(id = "alertmessage_id")
	public WebElement alertMessage;

	@FindBy(css = "#alertmessage_id span")
	public WebElement closeButton;

	@FindBy(tagName = "snack-bar-container")
	public WebElement alertContainer;

	public String getAlertMessage(){
		try {
			wait.forElementToBeVisible(alertMessage, true);
		} catch (Exception e) {		}
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
		} catch (Exception e) {}

		DObject alertObject = new DObject(driver, alertContainer);

		if (alertObject.isPresent()) {
			return (alertObject.getAttribute("class").contains("error"));
		}
		throw new Exception("Alert message not present");
	}

}
