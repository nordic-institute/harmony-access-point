package pages.messages;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.FilterArea;
import ddsl.dobjects.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MessageFilters extends FilterArea {
	public MessageFilters(WebDriver driver) {
		super(driver);
		PageFactory.initElements( new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

//	-------------------- Basic filters ---------------------------
	@FindBy(id = "messageid_id")
	public WebElement messageIDInput;

	@FindBy(id = "messagestatus_id")
	public WebElement messageStatusContainer;

	@FindBy(id = "frompartyid_id")
	public WebElement fromPartyInput;

	@FindBy(id = "topartyid_id")
	public WebElement toPartyInput;

//	-------------------- Advanced filters ---------------------------


	@FindBy(id = "conversationid_id")
	public WebElement conversationIDInput;

	@FindBy(id = "aprole_id")
	public WebElement apRoleContainer;

	@FindBy(id = "messagetype_id")
	public WebElement messageTypeContainer;

	@FindBy(id = "notificationstatus_id")
	public WebElement notificationStatusContainer;

	@FindBy(id = "referencemessageid_id")
	public WebElement referenceMessageIDInput;

	@FindBy(id = "originalsender_id")
	public WebElement originalSenderInput;

	@FindBy(id = "finalrecipient_id")
	public WebElement finalRecipientInput;

	@FindBy(id = "receivedfrom_id")
	public WebElement receivedFromContainer;

	@FindBy(id = "receivedto_id")
	public WebElement receivedToContainer;

	@FindBy(id = "showTestMessages_id")
	public WebElement showTestMessagesChk;


	public DInput getMessageIDInput() {
		return new DInput(driver, messageIDInput);
	}

	public DInput getFromPartyInput() {
		return new DInput(driver, fromPartyInput);
	}

	public DInput getToPartyInput() {
		return new DInput(driver, toPartyInput);
	}

	public Select getMessageStatus() {
		return new Select(driver, messageStatusContainer);
	}

	public DInput getConversationIDInput() {
		return new DInput(driver, conversationIDInput);
	}

	public DInput getReferenceMessageIDInput() {
		return new DInput(driver, referenceMessageIDInput);
	}

	public DInput getOriginalSenderInput() {
		return new DInput(driver, originalSenderInput);
	}

	public DInput getFinalRecipientInput() {
		return new DInput(driver, finalRecipientInput);
	}

	public Select getApRoleSelect() {
		return new Select(driver, apRoleContainer);
	}

	public Select getMessageTypeSelect() {
		return new Select(driver, messageTypeContainer);
	}

	public Select getNotificationStatus() {
		return new Select(driver, notificationStatusContainer);
	}

	public boolean basicFiltersLoaded()throws Exception{
		return (getMessageIDInput().isEnabled()
		&& getMessageStatus().isDisplayed()
		&& getFromPartyInput().isEnabled()
		&& getToPartyInput().isEnabled()
		&& getToPartyInput().isEnabled()
		);
	}

	public boolean advancedFiltersLoaded()throws Exception{
		return (getMessageIDInput().isEnabled()
		&& getMessageStatus().isDisplayed()
		&& getFromPartyInput().isEnabled()
		&& getToPartyInput().isEnabled()
		&& getToPartyInput().isEnabled()

		&& getConversationIDInput().isPresent()
		&& getApRoleSelect().isDisplayed()
		&& getMessageTypeSelect().isDisplayed()
		&& getNotificationStatus().isDisplayed()
		&& getReferenceMessageIDInput().isPresent()
		&& getOriginalSenderInput().isPresent()
		&& getFinalRecipientInput().isPresent()
		);
	}

	public DatePicker getReceivedTo() {
		return weToDatePicker(receivedToContainer);
	}
}
