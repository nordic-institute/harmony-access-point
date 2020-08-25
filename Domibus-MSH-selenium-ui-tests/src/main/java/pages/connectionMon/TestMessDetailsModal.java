package pages.connectionMon;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.HashMap;

public class TestMessDetailsModal extends DComponent {
	
	
	public TestMessDetailsModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}
	
	@FindBy(id = "messageLogDetailsCloseButton")
	private WebElement closeBtn;
	
	@FindBy(id = "updatebutton_id")
	private WebElement updateBtn;
	
	@FindBy(id = "testbutton_id")
	private WebElement testbutton_id;
	
	//	sent
	@FindBy(id = "toPartyId_id")
	private WebElement toPartyId;
	@FindBy(id = "toAccessPoint_id")
	private WebElement toAccessPoint;
	@FindBy(id = "timeSent_id")
	private WebElement timeSent;
	@FindBy(id = "toMessageId_id")
	private WebElement toMessageId;
	
	
	//  from
	@FindBy(id = "fromPartyId_id")
	private WebElement fromPartyId;
	@FindBy(id = "fromAccessPoint_id")
	private WebElement fromAccessPoint;
	@FindBy(id = "timeReceived_id")
	private WebElement timeReceived;
	@FindBy(id = "fromMessageId_id")
	private WebElement fromMessageId;

//	----------------------------------------------------------
	
	public DButton getCloseBtn() {
		return weToDButton(closeBtn);
	}
	
	public DButton getUpdateBtn() {
		return weToDButton(updateBtn);
	}
	
	public DButton getTestbutton() {
		return weToDButton(testbutton_id);
	}
	
	public HashMap<String, String> getSentMessInfo() {
		HashMap<String, String> info = new HashMap<>();
		info.put("party", weToDInput(toPartyId).getText());
		info.put("url", weToDInput(toAccessPoint).getText());
		info.put("time", weToDInput(timeSent).getText());
		info.put("id", weToDInput(toMessageId).getText());
		
		return info;
	}
	
	public HashMap<String, String> getRespMessInfo() {
		HashMap<String, String> info = new HashMap<>();
		info.put("party", weToDInput(fromPartyId).getText());
		info.put("url", weToDInput(fromAccessPoint).getText());
		info.put("time", weToDInput(timeReceived).getText());
		info.put("id", weToDInput(fromMessageId).getText());
		
		return info;
	}
	
	
	
	
	
}
