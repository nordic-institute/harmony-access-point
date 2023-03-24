package ddsl.dcomponents.popups;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;




public class EditModal extends DComponent {
	@FindBy(css = "mat-dialog-container #editbuttonok_id")
	protected WebElement okBtn;
	@FindBy(css = "mat-dialog-container #editbuttoncancel_id")
	protected WebElement cancelBtn;

	public EditModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
		wait.forElementToBe(okBtn);
	}

	public DButton getOkBtn() {
		return new DButton(driver, okBtn);
	}

	public DButton getCancelBtn() {
		return new DButton(driver, cancelBtn);
	}

	public void clickOK() throws Exception {
//		hardcoded wait because of fadeIn
		wait.forXMillis(500);

		getOkBtn().click();
		wait.forElementToBeGone(okBtn);

//		hardcoded wait because of fadeOut
		wait.forXMillis(500);
	}

	public void clickCancel() throws Exception {
		getCancelBtn().click();
		wait.forElementToBeGone(cancelBtn);
	}

}
