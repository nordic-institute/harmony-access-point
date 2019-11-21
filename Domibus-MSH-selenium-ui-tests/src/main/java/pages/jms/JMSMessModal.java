package pages.jms;

import ddsl.dcomponents.popups.InfoModal;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;

import java.util.HashMap;

/**
 * @author Catalin Comanici

 * @since 4.1
 */
public class JMSMessModal extends InfoModal {
	public JMSMessModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);


	}

	//	HEADER
	@FindBy(css = "input[id^=md-input][placeholder=\"Source\"]")
	WebElement sourceQueueInput;

	@FindBy(css = "input[id^=md-input][placeholder=\"Id\"]")
	WebElement idInput;

	@FindBy(css = "input[id^=md-input][placeholder=\"Timestamp\"]")
	WebElement timestampInput;

	@FindBy(css = ".mat-dialog-container mat-card:nth-of-type(1) mat-form-field:nth-of-type(4) input")
	WebElement jmsTypeInput;

	@FindBy(css = "textarea[id^=md-input][placeholder^=\"Custom\"]")
	WebElement customPropertiesArea;

//	Properties

	@FindBy(css = "input[id^=md-input][placeholder=\"JMSMessageID\"]")
	WebElement jmsMessageIDInput;

	@FindBy(css = "input[id^=md-input][placeholder=\"JMSDestination\"]")
	WebElement destinationQueueInput;

	@FindBy(css = "input[id^=md-input][placeholder=\"JMSDeliveryMode\"]")
	WebElement deliveryModeInput;


	@FindBy(css = ".mat-dialog-container mat-card:nth-of-type(2) mat-form-field:nth-of-type(4) input")
	WebElement jmsType2Input;


	@FindBy(css = ".mat-dialog-container mat-card:nth-of-type(3) mat-form-field:nth-of-type(1) input")
	WebElement msgContentInput;

	public DInput getSourceQueueInput() {
		return new DInput(driver, sourceQueueInput);
	}

	public DInput getIdInput() {
		return new DInput(driver, idInput);
	}

	public DInput getTimestampInput() {
		return new DInput(driver, timestampInput);
	}

	public DInput getJmsTypeInput() {
		return new DInput(driver, jmsTypeInput);
	}

	public DInput getCustomPropertiesArea() {
		return new DInput(driver, customPropertiesArea);
	}

	public DInput getJmsMessageIDInput() {
		return new DInput(driver, jmsMessageIDInput);
	}

	public DInput getDestinationQueueInput() {
		return new DInput(driver, destinationQueueInput);
	}

	public DInput getDeliveryModeInput() {
		return new DInput(driver, deliveryModeInput);
	}

	public DInput getJmsType2Input() {
		return new DInput(driver, jmsType2Input);
	}

	public DInput getMsgContentInput() {
		return new DInput(driver, msgContentInput);
	}

	public HashMap<String, String> getMessageInfo() throws Exception{

		log.debug("retreive info from popup");


		HashMap<String, String> toReturn = new HashMap<String, String>();

		toReturn.put(getSourceQueueInput().getAttribute("placeholder"), getSourceQueueInput().getText());
		toReturn.put(getIdInput().getAttribute("placeholder"), getIdInput().getText());
		toReturn.put(getTimestampInput().getAttribute("placeholder"), getTimestampInput().getText());
		toReturn.put(getJmsTypeInput().getAttribute("placeholder"), getJmsTypeInput().getText());
		toReturn.put(getCustomPropertiesArea().getAttribute("placeholder"), getCustomPropertiesArea().getText());
		toReturn.put(getJmsMessageIDInput().getAttribute("placeholder"), getJmsMessageIDInput().getText());
		toReturn.put(getDestinationQueueInput().getAttribute("placeholder"), getDestinationQueueInput().getText());
		toReturn.put(getDeliveryModeInput().getAttribute("placeholder"), getDeliveryModeInput().getText());
		toReturn.put(getJmsType2Input().getAttribute("placeholder"), getJmsType2Input().getText());
		toReturn.put(getMsgContentInput().getAttribute("placeholder"), getMsgContentInput().getText());
		toReturn.put(getMsgContentInput().getAttribute("placeholder"), getMsgContentInput().getText());

		return toReturn;
	}


}
