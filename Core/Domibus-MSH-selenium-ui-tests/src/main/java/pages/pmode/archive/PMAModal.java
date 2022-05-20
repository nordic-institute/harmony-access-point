package pages.pmode.archive;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class PMAModal extends DComponent {
	@FindBy(css = "mat-dialog-container h1#pmodeheader_id")
	public WebElement title;
	@FindBy(css = "mat-card-content textarea")
	public WebElement textarea;
	@FindBy(css = "mat-card-content button")
	public WebElement okBtn;

	public PMAModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DObject getTitle() {
		return weToDobject(title);
	}

	public DInput getTextarea() {
		return weToDInput(textarea);
	}

	public DButton getOkBtn() {
		return weToDButton(okBtn);
	}
}
