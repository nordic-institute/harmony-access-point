package pages.users;

import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.Checkbox;
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


public class UserModal extends EditModal {
	@FindBy(css = "edituser-form form #username_id")
	WebElement usernameInput;
	@FindBy(css = "edituser-form form #email_id")
	WebElement emailInput;
	@FindBy(css = "edituser-form form #password_id")
	WebElement passwordInput;
	@FindBy(css = "edituser-form form #confirmation_id")
	WebElement confirmationInput;
	@FindBy(css = "edituser-form form #domain_id")
	WebElement domainSelectContainer;
	@FindBy(css = "edituser-form form #role_id")
	WebElement rolesSelectContainer;
	@FindBy(css = "edituser-form form #active_id")
	WebElement activeChk;
	@FindBy(css="edituser-form form #active_id-input")
	WebElement chkBoxIp;
	@FindBy(css = "edituser-form form #username_id + span")
	private WebElement usernameErrMess;
	@FindBy(css = "edituser-form form #email_id + span>div")
	private WebElement emailErrMess;
	@FindBy(css = "edituser-form form #password_id + span>div")
	private WebElement passErrMess;
	@FindBy(css = "edituser-form form #confirmation_id + span")
	private WebElement confirmationErrMess;
	public UserModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
		wait.forElementToBeVisible(okBtn);
	}

	public DInput getUserNameInput() {
		return new DInput(driver, usernameInput);
	}

	public DInput getEmailInput() {
		return new DInput(driver, emailInput);
	}

	public DInput getPasswordInput() {
		return new DInput(driver, passwordInput);
	}

	public DInput getConfirmationInput() {
		return new DInput(driver, confirmationInput);
	}

	public Select getDomainSelect() {
		return new Select(driver, domainSelectContainer);
	}

	public Select getRoleSelect() {
		return new Select(driver, rolesSelectContainer);
	}

	public Checkbox getActiveChk() {
		return new Checkbox(driver, activeChk);
	}

	public void fillData(String user, String email, String role, String password, String confirmation) throws Exception {
		getUserNameInput().fill(user);
		getEmailInput().fill(email);
		getRoleSelect().selectOptionByText(role);
		getPasswordInput().fill(password);
		getConfirmationInput().fill(confirmation);
	}

	public boolean isLoaded() throws Exception {
		return (getUserNameInput().isPresent()
				&& getPasswordInput().isPresent()
				&& getRoleSelect().isDisplayed()
				&& getPasswordInput().isPresent()
				&& getConfirmationInput().isPresent());
	}

	public boolean isActive() throws Exception {
		return getActiveChk().isChecked();
	}

	public DObject getUsernameErrMess() {
		wait.forXMillis(700);
		return new DObject(driver, usernameErrMess);
	}

	public DObject getEmailErrMess() {
		wait.forXMillis(700);
		return new DObject(driver, emailErrMess);
	}

	public DObject getPassErrMess() {
		wait.forXMillis(700);
		return new DObject(driver, passErrMess);
	}

	public DObject getConfirmationErrMess() {
		wait.forXMillis(700);
		return new DObject(driver, confirmationErrMess);
	}
	public Checkbox getInputChkBox() {
		return new Checkbox(driver, chkBoxIp);
	}
}
