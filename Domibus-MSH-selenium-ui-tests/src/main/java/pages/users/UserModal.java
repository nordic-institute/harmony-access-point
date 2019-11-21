package pages.users;

import ddsl.dobjects.Select;
import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DInput;
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


public class UserModal extends EditModal {
	public UserModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
		wait.forElementToBeVisible(okBtn);
	}

	@FindBy(id = "username_id")
	WebElement usernameInput;

	@FindBy(id = "email_id")
	WebElement emailInput;

	@FindBy(id = "password_id")
	WebElement passwordInput;

	@FindBy(id = "confirmation_id")
	WebElement confirmationInput;


	@FindBy(css = "mat-card > div:nth-child(4) > mat-select")
	WebElement domainSelectContainer;

	@FindBy(css = "mat-select[placeholder=\"Role\"]")
	WebElement rolesSelectContainer;

	@FindBy(css = "edituser-form > div > form > mat-card > div:nth-child(7) input")
	WebElement activeChk;


	@FindBy(css = "edituser-form > div > form > mat-card > div:nth-child(1) > mat-form-field > div > div.mat-form-field-flex > div > div")
	private WebElement usernameErrMess;
	@FindBy(css = "edituser-form > div > form > mat-card > div:nth-child(2) > mat-form-field > div > div.mat-form-field-flex > div > div")
	private WebElement emailErrMess;

	@FindBy(css = "edituser-form > div > form > mat-card > div:nth-child(5) > mat-form-field > div > div.mat-form-field-flex > div > div")
	private WebElement passErrMess;
	@FindBy(css = "edituser-form > div > form > mat-card > div:nth-child(6) > mat-form-field > div > div.mat-form-field-flex > div > div")
	private WebElement confirmationErrMess;


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

	public boolean isLoaded() throws Exception{
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
		return new DObject(driver, usernameErrMess);
	}

	public DObject getEmailErrMess() {
		return new DObject(driver, emailErrMess);
	}

	public DObject getPassErrMess() {
		return new DObject(driver, passErrMess);
	}

	public DObject getConfirmationErrMess() {
		return new DObject(driver, confirmationErrMess);
	}
}
