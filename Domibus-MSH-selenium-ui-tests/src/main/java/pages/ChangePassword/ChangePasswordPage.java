package pages.ChangePassword;

import com.bluecatcode.junit.shaded.org.apache.commons.lang3.StringUtils;
import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


public class ChangePasswordPage extends DomibusPage {

	public final String newPasswordFieldLabel = "New Password";
	public final String currentPasswordFieldLabel = "Current Password";
	public final String confirmationFieldLabel = "Confirmation";
	@FindBy(xpath = "//p[contains(text(),'Change Password')]")
	protected WebElement fieldHeader;
	@FindBy(id = "currentPassword_id")
	private WebElement currentPassField;
	@FindBy(id = "newPassword_id")
	private WebElement newPassField;
	@FindBy(id = "confirmation_id")
	private WebElement confirmationField;
	@FindBy(id = "editbuttonok_id")
	private WebElement updateButton;

	public ChangePasswordPage(WebDriver driver) {
		super(driver);
		log.debug("Change Password  page init");
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DInput getCPassField() {
		return new DInput(driver, currentPassField);
	}

	public DInput getNPassField() {
		return new DInput(driver, newPassField);
	}

	public DInput getConfirmationField() {
		return new DInput(driver, confirmationField);
	}

	public DObject getPageNameObj() {
		return new DObject(driver, fieldHeader);
	}

	public DButton getUpdateButton() {
		return new DButton(driver, updateButton);
	}

	/**
	 * This method will verify field header of ChangePassword page
	 */

	public Boolean verifyFieldHeader() throws Exception {
		log.debug("Verifying Field Header...");
		String rawTitle = getPageNameObj().getText();
		boolean toReturn = StringUtils.equalsIgnoreCase(rawTitle, "Change Password");
		log.debug("Opened Page is  : " + toReturn);
		return toReturn;
	}

	/**
	 * This method is written to check whether Change Password page has proper element present.
	 */
	public boolean isLoaded() {

		log.debug("check if is loaded");
		wait.forElementToBeVisible(currentPassField);
		wait.forElementToBeVisible(newPassField);
		wait.forElementToBeVisible(confirmationField);
		if (!currentPassField.isEnabled()) {
			log.debug("Could not find current password  input");
			return false;
		}
		if (!newPassField.isEnabled()) {
			log.debug("Could not find new password  input");
			return false;
		}
		if (!confirmationField.isEnabled()) {
			log.debug("Could not find Confirmation input");
			return false;
		}
		if (updateButton.isEnabled()) {
			log.debug("Could not find disable Update Button");
			return false;
		}
		log.debug("Change Password page controls loaded");
		return true;
	}

	/*Method allows user to enter data in current password, new password and Confirmation field
	 *@param cpass:- Data for Current Password field
	 *@param npass :- Data for New Password Field
	 * @param confirmPass:- Data for Confirmation Field
	 */
	public void setPassFields(String currentPass, String newPass, String confirmPass) throws Exception {
		log.debug("User enters data in current password field");
		getCPassField().click();
		getCPassField().fill(currentPass);

		log.debug("User enters data in New password field");
		getNPassField().click();
		getNPassField().fill(newPass);

		log.debug("User enters data in Confirmation field");
		getConfirmationField().click();
		getConfirmationField().fill(confirmPass);
		getCPassField().click();
		if (isValidationMsgPresent(confirmationFieldLabel) || isValidationMsgPresent(newPasswordFieldLabel)) {
			return;
		}

		wait.forElementToBeEnabled(updateButton);
	}



    /*
    This method returns CSS of validation message shown under field with provided FieldLabel
    *   @param FieldName :- Name of Input Field
    *   @return :- CSS of validation message under input field
     */

	public String getCssOfValidationMsg(String fieldName) {
		if (fieldName.equals(newPasswordFieldLabel)) {
			String fieldLabel = fieldName;
			String[] labels = fieldLabel.split(" ");
			String FieldName1 = labels[0].toLowerCase().concat(labels[1]);
			return "input[id='" + FieldName1 + "_id']~span.help-block>div";
		} else if (fieldName.equals(confirmationFieldLabel)) {
			String str = fieldName.toLowerCase();
			return "input[id='" + str + "_id']~span.help-block>div";
		} else {
			return "";
		}
	}

	/*
	This method print message under provided FieldLabel
	*@param FieldName :- Name of Input Field
	* @return :-Boolean result for Presence of Validation message under input field
	 */
	public Boolean isValidationMsgPresent(String fieldName) throws Exception {
		try {
			WebElement elm = driver.findElement(By.cssSelector(getCssOfValidationMsg(fieldName)));
			wait.forElementToBe(elm);
			if (elm.isDisplayed()) {
				log.info("Validation message under field " + fieldName + "\r\n" + elm.getText().trim());
				return true;
			}
		} catch (Exception e) {
		}
		log.info("message is not displayed");
		return false;
	}

	/*
	This method returns message under provided FieldLabel
	* @param FieldName :- Name of Input Field
	* @return : the string under the input
	 */
	public String getValidationMsg(String fieldName) throws Exception {
		if (isValidationMsgPresent(fieldName)) {
			WebElement elm = driver.findElement(By.cssSelector(getCssOfValidationMsg(fieldName)));
			log.info("Validation message under field " + fieldName + "\r\n" + elm.getText().trim());
			return weToDobject(elm).getText();
		}
		log.info("message is not displayed");
		return "";
	}


}

