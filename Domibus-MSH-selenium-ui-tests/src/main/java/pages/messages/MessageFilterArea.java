package pages.messages;

import ddsl.dcomponents.FilterArea;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DatePicker;
import ddsl.dobjects.Select;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class MessageFilterArea extends FilterArea {
	//	-------------------- Basic filters ---------------------------
	@FindBy(id = "messageid_id")
	public WebElement messageIDInput;
	@FindBy(id = "messagestatus_id")
	public WebElement messageStatusContainer;
	@FindBy(id = "frompartyid_id")
	public WebElement fromPartyInput;
	@FindBy(id = "topartyid_id")
	public WebElement toPartyInput;
	@FindBy(id = "conversationid_id")
	public WebElement conversationIDInput;

//	-------------------- Advanced filters ---------------------------
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
	
	public MessageFilterArea(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}
	
	public DInput getMessageIDInput() {
		return new DInput(driver, messageIDInput);
	}
	
	public void basicFilterBy(String messageID, String messageStatus, String fromParty, String toParty) throws Exception {
		log.debug("messageID = " + messageID);
		log.debug("messageStatus = " + messageStatus);
		log.debug("fromParty = " + fromParty);
		log.debug("toParty = " + toParty);
		
		weToDInput(messageIDInput).fill(messageID);
		weToSelect(messageStatusContainer).selectOptionByText(messageStatus);
		weToDInput(fromPartyInput).fill(fromParty);
		weToDInput(toPartyInput).fill(toParty);
		
		clickSearch();
	}
	
	public void advancedFilterBy(String messageID,
	                             String messageStatus,
	                             String fromParty,
	                             String toParty,
	                             String conversationId,
	                             String apRole,
	                             String messageType,
	                             String notificationStatus,
	                             String refMessageId,
	                             String origSender,
	                             String finalRecipient,
	                             String receivedFromDate,
	                             String receivedUpToDate) throws Exception {
		log.debug("messageID = " + messageID);
		log.debug("messageStatus = " + messageStatus);
		log.debug("fromParty = " + fromParty);
		log.debug("toParty = " + toParty);
		log.debug("conversationId = " + conversationId);
		log.debug("apRole = " + apRole);
		log.debug("messageType = " + messageType);
		log.debug("notificationStatus = " + notificationStatus);
		log.debug("refMessageId = " + refMessageId);
		log.debug("origSender = " + origSender);
		log.debug("finalRecipient = " + finalRecipient);
		log.debug("receivedFromDate = " + receivedFromDate);
		log.debug("receivedUpToDate = " + receivedUpToDate);
		
		expandArea();
		
		weToDInput(messageIDInput).fill(messageID);
		weToSelect(messageStatusContainer).selectOptionByText(messageStatus);
		weToDInput(fromPartyInput).fill(fromParty);
		weToDInput(toPartyInput).fill(toParty);
		weToDInput(conversationIDInput).fill(conversationId);
		weToSelect(apRoleContainer).selectOptionByText(apRole);
		weToSelect(messageTypeContainer).selectOptionByText(messageType);
		weToSelect(notificationStatusContainer).selectOptionByText(notificationStatus);
		weToDInput(referenceMessageIDInput).fill(refMessageId);
		weToDInput(originalSenderInput).fill(origSender);
		weToDInput(finalRecipientInput).fill(finalRecipient);
		weToDatePicker(receivedFromContainer).selectDate(receivedFromDate);
		weToDatePicker(receivedToContainer).selectDate(receivedUpToDate);
		
		clickSearch();
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

	public DatePicker getReceivedFromContainer() {
		return weToDatePicker(receivedFromContainer);
	}
}
