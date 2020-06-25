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

	public PModeCofirmationModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

	}

	public DInput getDescriptionTextArea() {
		return new DInput(driver, descriptionTextArea);
	}
}
