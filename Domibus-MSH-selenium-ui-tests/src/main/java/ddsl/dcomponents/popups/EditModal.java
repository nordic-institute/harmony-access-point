package ddsl.dcomponents.popups;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class EditModal extends DComponent {
	public EditModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
		wait.forElementToBe(okBtn);
	}


	@FindBy(css = "mat-dialog-container button:nth-of-type(1)")
	protected WebElement okBtn;

	@FindBy(css = "mat-dialog-container button:nth-of-type(2)")
	protected WebElement cancelBtn;

	@FindBy(css = "mat-dialog-container p")
	protected WebElement title;

	public DButton getOkBtn() {
		return new DButton(driver, okBtn);
	}

	public DButton getCancelBtn() {
		return new DButton(driver, cancelBtn);
	}

	public void clickOK() throws Exception {
		getOkBtn().click();
		wait.forElementToBeGone(okBtn);

//		hardcoded wait to match the time needed fot the dialog to disappear
		wait.forXMillis(200);
	}

	public void clickCancel() throws Exception {
		getCancelBtn().click();
		wait.forElementToBeGone(cancelBtn);
	}




}
