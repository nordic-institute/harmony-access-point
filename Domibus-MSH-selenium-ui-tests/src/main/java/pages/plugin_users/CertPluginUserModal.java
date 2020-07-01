package pages.plugin_users;

import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.DInput;
import ddsl.dobjects.Select;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class CertPluginUserModal extends EditModal {
	@FindBy(id = "username_id")
	private WebElement userInput;
	@FindBy(id = "originalUser_id")
	private WebElement originalUserInput;
	@FindBy(css = "editcertificatepluginuser-form mat-select")
	private WebElement roleSelect;

	public CertPluginUserModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DInput getUserInput() {
		return new DInput(driver, userInput);
	}

	public DInput getOriginalUserInput() {
		return new DInput(driver, originalUserInput);
	}

	public Select getRoleSelect() {
		return new Select(driver, roleSelect);
	}
}
