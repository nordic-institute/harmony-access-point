package pages.jms;

import ddsl.dcomponents.popups.InfoModal;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.HashMap;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class JMSMessModal extends InfoModal {
	//	HEADER
	@FindBy(id = "source_id")
	WebElement sourceQueueInput;
	@FindBy(id = "id_id")
	WebElement idInput;
	@FindBy(id = "timestamp_id")
	WebElement timestampInput;
	@FindBy(id = "type_id")
	WebElement jmsTypeInput;
	@FindBy(id = "customProperties_id")
	WebElement customPropertiesArea;
	@FindBy(id = "propJmsMessageId_id")
	WebElement jmsMessageIDInput;

//	Properties
	@FindBy(id = "propDestination_id")
	WebElement destinationQueueInput;
	@FindBy(id = "propDeliveryMode_id")
	WebElement deliveryModeInput;
	@FindBy(id = "propMessageId_id")
	WebElement jmsType2Input;
	@FindBy(id = "content_id")
	WebElement msgContentInput;

	public JMSMessModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);


	}

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

	public HashMap<String, String> getMessageInfo() throws Exception {

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
