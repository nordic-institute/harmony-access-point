package pages.truststore;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


public class TruststorePage extends DomibusPage {
	@FindBy(css = "#pageGridId")
	WebElement truststoreTable;

	@FindBy(id = "uploadbutton_id")
	WebElement uploadButton;
	@FindBy(id = "truststore")
	WebElement chooseFileButton;
	@FindBy(id = "password_id")
	WebElement passwordInputField;
	@FindBy(id = "okbuttonupload_id")
	WebElement okButton;

	@FindBy(id = "reloadStorebutton_id")
	WebElement reloadButton;

	@FindBy(css = ".error")
	WebElement passValidationMsg;


	public TruststorePage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DGrid grid() {
		return new DGrid(driver, truststoreTable);
	}

	public DButton getUploadButton() {
		return new DButton(driver, uploadButton);
	}


	public DButton getOkButton() {
		return new DButton(driver, okButton);
	}

	public DInput getPassInputField() {
		return new DInput(driver, passwordInputField);
	}


	public void uploadFile(String filePath, String password) throws Exception {
		getUploadButton().click();

		chooseFileButton.sendKeys(filePath);

//		soft.assertTrue(passValidationMsg.isDisplayed(), "Validation under password field is present");

		if (!password.isEmpty()) {
			log.info("Entering value ");
			getPassInputField().fill(password);
			wait.forElementToBeClickable(okButton);
			log.info("check ok button status : " + okButton.isEnabled());
			getOkButton().click();
		} else {
			log.info("File can't be uploaded without password");
		}

	}

	public DButton getReloadButton() {
		return new DButton(driver, reloadButton);
	}
}
