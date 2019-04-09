package pages.messages;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MessageResendModal extends DComponent {
	public MessageResendModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements( new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);

	}

	@FindBy(id = "messageDialogResendButton")
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
