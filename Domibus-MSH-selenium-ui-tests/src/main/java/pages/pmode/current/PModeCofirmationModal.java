package pages.pmode.current;

import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.testng.asserts.SoftAssert;
import utils.TestRunData;

/**
 * @author Catalin Comanici

 * @since 4.1
 */
public class PModeCofirmationModal extends EditModal {
	public PModeCofirmationModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

	}

	@FindBy(id = "description_id")
	WebElement descriptionTextArea;

	@FindBy(id="pmode")
	WebElement chooseFilePmode;

	@FindBy(id="okbuttonupload_id")
	WebElement okButton;


	public DInput getDescriptionTextArea() {
		return new DInput(driver, descriptionTextArea);
	}
	public PModeCurrentPage getPmodeCurrentPage() { return new PModeCurrentPage(driver);}
	public DButton getOkButton() { return new DButton(driver,okButton);}

	public void uploadPmodeFile(String path, SoftAssert soft, String descriptionTxt) throws Exception{

		chooseFilePmode.sendKeys(path);
		getDescriptionTextArea().fill(descriptionTxt);
		getOkButton().click();

	}
}
