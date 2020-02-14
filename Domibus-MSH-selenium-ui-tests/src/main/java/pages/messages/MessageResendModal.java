package pages.messages;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;

/**
 * @author Catalin Comanici

 * @since 4.1
 */
public class MessageResendModal extends DComponent {
	public MessageResendModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements( new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

	}

//	@FindBy(id = "messageDialogResendButton")
	@FindBy(id = "yesbuttondialog_id")
	WebElement resendButton;
	@FindBy(id = "messageDialogCancelButton")
	WebElement cancel;

	@FindBy(id = "messageDialogTitle")
	WebElement title;

	public DButton getResendButton() {
		return new DButton(driver, resendButton);
	}

	public DButton getCancel() {
		return new DButton(driver, cancel);
	}

	public DObject getTitle() {
		return new DObject(driver, title);
	}
}
