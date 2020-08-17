package pages.jms;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class JMSMoveMessageModal extends DComponent {
	
	@FindBy(id = "messageDialogResendButton")
	protected WebElement okBtn;
	
	@FindBy(id = "messageDialogCancelButton")
	protected WebElement cancelBtn;
	
	@FindBy(id = "jmsqueuedestination_id")
	protected WebElement queueSelect;

	public JMSMoveMessageModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public JMSSelect getQueueSelect() {
		return new JMSSelect(driver, queueSelect);
	}

	public DButton getOkBtn() {
		return new DButton(driver, okBtn);
	}

	public DButton getCancelBtn() {
		return new DButton(driver, cancelBtn);
	}
	
	public void clickOK() throws Exception {
		getOkBtn().click();
		wait.forElementToBeGone(okBtn);

//		hardcoded wait to match the time needed for the dialog to disappear
		wait.forXMillis(200);
	}
	
	public void clickCancel() throws Exception {
		getCancelBtn().click();
		wait.forElementToBeGone(cancelBtn);
		//		hardcoded wait to match the time needed for the dialog to disappear
		wait.forXMillis(200);
	}
	
	
}
