package pages.pmode.current;

import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class PModeCofirmationModal extends EditModal {
	
	@FindBy(id = "description_id")
	WebElement descriptionTextArea;
	
	@FindBy(id="pmode")
	WebElement chooseFilePmode;
	
	@FindBy(id="okbuttonupload_id")
	WebElement okButton;
	
	
	
	public PModeCofirmationModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DInput getDescriptionTextArea() {
		return new DInput(driver, descriptionTextArea);
	}
	
	
	public void uploadPmodeFile(String path, String descriptionTxt) throws Exception{
		
		chooseFilePmode.sendKeys(path);
		getDescriptionTextArea().fill(descriptionTxt);
		weToDButton(okButton).click();
		
	}
}
