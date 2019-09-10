package pages.ChangePassword;

import com.bluecatcode.junit.shaded.org.apache.commons.lang3.StringUtils;
import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.Generator;


public class ChangePasswordPage extends DomibusPage {
    public ChangePasswordPage(WebDriver driver) {
        super(driver);
        log.debug("Change Password  page init");
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }
    public static final String NewPassword_Field_label ="New Password";
    public static final String CurrentPassword_Field_label="Current Password";
    public static final String Confirmation_Field_label="Confirmation";


    @FindBy(xpath = "//p[contains(text(),'Change Password')]")
    protected WebElement fieldHeader;

    @FindBy(id = "currentPassword_id")
    private WebElement CurrentPassField;

    @FindBy(id = "newPassword_id")
    private WebElement NewPassField;

    @FindBy(id = "confirmation_id")
    private WebElement ConfirmationField;

    @FindBy(id = "editbuttonok_id")
    private WebElement UpdateButton;


    public DInput getCPassField() {
        return new DInput(driver, CurrentPassField);
    }

    public DInput getNPassField() {
        return new DInput(driver, NewPassField);
    }

    public DInput getConfirmationField() {
        return new DInput(driver, ConfirmationField);
    }

    public DObject getPageNameObj() {
        return new DObject(driver, fieldHeader);
    }

    public DButton getUpdateButton() {
        return new DButton(driver, UpdateButton);
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
        wait.forElementToBeVisible(CurrentPassField);
        wait.forElementToBeVisible(NewPassField);
        wait.forElementToBeVisible(ConfirmationField);
        if (!CurrentPassField.isEnabled()) {
            log.debug("Could not find current password  input");
            return false;
        }
        if (!NewPassField.isEnabled()) {
            log.debug("Could not find new password  input");
            return false;
        }
        if (!ConfirmationField.isEnabled()) {
            log.debug("Could not find Confirmation input");
            return false;
        }
        if (UpdateButton.isEnabled()) {
            log.debug("Could not find disable Update Button");
            return false;
        }
        log.debug("Change Password page controls loaded");
        return true;
    }

    /*Method allows user to enter data in current password, new password and Confirmation field
     *@param Cpass:- Data for Current Password field
     *@param Npass :- Data for New Password Field
     * @param ConfirmPass:- Data for Confirmation Field
     */
    public void setPassFields(String CPass, String NPass, String ConfirmPass) throws Exception {
        log.debug("User enters data in current password field");
        getCPassField().fill(CPass);
        log.debug("User enters data in New password field");
        getNPassField().fill(NPass);
        log.debug("User enters data in Confirmation field");
        getConfirmationField().fill(ConfirmPass);

    }

    //Method is used to generate random password data
    public String generateInvalidPass() {
        String randomPassword = Generator.randomAlphaNumeric(10);
        return randomPassword;
    }

    /*
     * This Method is used to press Tab key
     */
    public void pressTABKey() throws Exception {

        WebElement element = driver.findElement(By.id("confirmation_id"));
        element.sendKeys(Keys.TAB);
        element.sendKeys(Keys.ENTER);
    }
    /*
    This method returns xpath of validation message shown under field with provided FieldLabel
    *   @param FieldName :- Name of Input Field
    *   @return :- xpath of validation message under input field
     */
//    public String getXpathOfValidationMsg(String FieldName)  {
//        return ".//*[@class='mat-input-infix']/input[@placeholder='" + FieldName + "']/..//div/div";
//    }
//
    public String getCssOfValidationMsg(String fieldName) {
        if (fieldName.equals(NewPassword_Field_label)) {
            String str1 = fieldName;
            String[] str = str1.split(" ");
            String FieldName1 = str[0].toLowerCase().concat(str[1]);
            return "input[id='" + FieldName1 + "_id']~div>div";
        }
        else if (fieldName.equals(Confirmation_Field_label)) {
            String str = fieldName.toLowerCase();
            return "input[id='" + str + "_id']~div>div";
        }
        else{
            return "";
        }
    }

    /*
    This method print message under provided FieldLabel
    *@param FieldName :- Name of Input Field
    * @return :-Boolean result for Presence of Validation message under input field
     */
    public Boolean getValidationMsg(String fieldName) throws Exception {
        WebElement elm = driver.findElement(By.cssSelector(getCssOfValidationMsg(fieldName)));
        wait.forElementToBeVisible(elm);
        if (!elm.isDisplayed()) {
            log.info("message is not displayed");
            return false;
        } else {
            log.info("Validation message under field " + fieldName + "\r\n" + elm.getText().trim());
            return true;
        }
    }



}

