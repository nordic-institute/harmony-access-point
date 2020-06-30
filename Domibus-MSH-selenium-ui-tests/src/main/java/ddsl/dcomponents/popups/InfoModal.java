package ddsl.dcomponents.popups;

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
 * @version 4.1
 */


public class InfoModal extends DComponent {

	@FindBy(css = "mat-dialog-actions > button")
	WebElement closeBtn;
	@FindBy(css = "mat-dialog-container h2")
	WebElement title;

	public InfoModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
		wait.forElementToBeVisible(closeBtn);
	}

	public DButton getCloseBtn() {
		return new DButton(driver, closeBtn);
	}

	public String getTitle() throws Exception {
		return new DObject(driver, title).getText();
	}


}
