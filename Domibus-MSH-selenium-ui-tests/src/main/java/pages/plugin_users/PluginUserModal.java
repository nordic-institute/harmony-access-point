package pages.plugin_users;

import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DObject;
import ddsl.dobjects.Select;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class PluginUserModal extends EditModal {
	@FindBy(css = "#username_id")
	private WebElement userNameInput;
	@FindBy(css = "#originalUser_id")
	private WebElement originalUserInput;
	@FindBy(css = "mat-select[placeholder=\"Role\"]")
	private WebElement rolesSelectContainer;
	@FindBy(css = "#password_id")
	private WebElement passwordInput;
	@FindBy(css = "#confirmation_id")
	private WebElement confirmationInput;
	@FindBy(css = "#editbuttonok_id")
	private WebElement okBtn;
	@FindBy(css = "#editbuttoncancel_id")
	private WebElement cancelBtn;
	@FindBy(css = "editbasicpluginuser-form form #username_id + span.help-block>div")
	private WebElement usernameErrMess;
	@FindBy(css = "editbasicpluginuser-form form #originalUser_id + span.help-block>div")
	private WebElement originalUserErrMess;
	@FindBy(css = "editbasicpluginuser-form form #password_id + span.help-block>div")
	private WebElement passErrMess;
	@FindBy(css = "editbasicpluginuser-form form #confirmation_id + span.help-block>div")
	private WebElement confirmationErrMess;
	@FindBy(css = "editbasicpluginuser-form form mat-select[placeholder=\"Role\"] + span.help-block>div")
	private WebElement roleErrMess;

	public PluginUserModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public void fillData(String user, String role, String password, String confirmation) throws Exception {
		getUserNameInput().fill(user);
		getPasswordInput().fill(password);
		getConfirmationInput().fill(confirmation);

		getRolesSelect().selectOptionByText(role);
	}

	public DInput getUserNameInput() {
		return new DInput(driver, userNameInput);
	}

	public DInput getOriginalUserInput() {
		return new DInput(driver, originalUserInput);
	}

	public Select getRolesSelect() {
		return new Select(driver, rolesSelectContainer);
	}

	public DInput getPasswordInput() {
		return new DInput(driver, passwordInput);
	}

	public DInput getConfirmationInput() {
		return new DInput(driver, confirmationInput);
	}

	@Override
	public DButton getOkBtn() {
		return new DButton(driver, okBtn);
	}

	@Override
	public DButton getCancelBtn() {
		return new DButton(driver, cancelBtn);
	}

	public DObject getUsernameErrMess() {
		return new DObject(driver, usernameErrMess);
	}

	public DObject getPassErrMess() {
		return new DObject(driver, passErrMess);
	}

	public DObject getConfirmationErrMess() {
		return new DObject(driver, confirmationErrMess);
	}

	public DObject getOriginalUserErrMess() {
		return new DObject(driver, originalUserErrMess);
	}

	public DObject getRoleErrMess() {
		return new DObject(driver, roleErrMess);
	}


}
