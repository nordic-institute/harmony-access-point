package pages.messages;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class MessageResendModal extends DComponent {
	@FindBy(id = "yesbuttondialog_id")
	WebElement resendButton;
	@FindBy(id = "nobuttondialog_id")
	WebElement cancel;
	@FindBy(css = ".mat-dialog-title")
	WebElement title;

	public MessageResendModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

	}

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
