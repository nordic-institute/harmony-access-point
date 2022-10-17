package pages.certificates;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.DFileUtils;

import java.io.File;

public class CertificatePage extends DomibusPage {

	@FindBy(css = "app-column-picker > div[class='ng-star-inserted']")
	public WebElement showHideAdditionalArea;
	@FindBy(css = "#pageGridId")
	WebElement tlsTruststoreTable;
	@FindBy(id = "uploadbutton_id")
	WebElement uploadButton;
	@FindBy(id = "downloadbutton_id")
	WebElement downloadButton;
	@FindBy(id = "reloadStorebutton_id")
	WebElement reloadButton;

	@FindBy(id = "truststore")
	WebElement chooseFileButton;
	@FindBy(id = "password_id")
	WebElement passwordInputField;
	@FindBy(id = "alias_id")
	WebElement aliasInputField;
	@FindBy(id = "okbuttonupload_id")
	WebElement okButton;
	@FindBy(id = "cancelbuttonupload_id")
	WebElement cancelButton;
	@FindBy(css = ".error")
	WebElement passValidationMsg;

	public CertificatePage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DGrid grid() {
		return new DGrid(driver, tlsTruststoreTable);
	}

	public DButton getUploadButton() {
		return new DButton(driver, uploadButton);
	}

	public DButton getDownloadButton() {
		return new DButton(driver, downloadButton);
	}

	public DButton getReloadButton() {
		return new DButton(driver, reloadButton);
	}

	public DInput getPassInputField() {
		return new DInput(driver, passwordInputField);
	}

	public DInput getAliasInputField() {
		return new DInput(driver, aliasInputField);
	}

	public DButton getOkButton() {
		return new DButton(driver, okButton);
	}

	public void uploadTruststore(String filePath, String password) throws Exception {
		log.info("uploading truststore certificate");

		File file = new File(filePath);

		getUploadButton().click();
		chooseFileButton.sendKeys(file.getAbsolutePath());
		getPassInputField().fill(password);

		wait.forElementToBeClickable(okButton);
		getOkButton().click();
	}


	public Boolean isDefaultElmPresent(Boolean tlsConfig) throws Exception {

		Boolean isElmPresent;

		tlsConfig = true;

		isElmPresent = getUploadButton().isEnabled()
				&& getDownloadButton().isEnabled()
				&& getReloadButton().isEnabled();

		return isElmPresent;
	}

	public void pressDownloadAndSaveFile(String filePath) throws Exception {

		log.info("Clean given directory");
		FileUtils.cleanDirectory(new File(filePath));

		log.info("Click on download csv button");
		getDownloadButton().click();

		log.info("Wait for download to complete");

		wait.forFileToBeDownloaded(filePath);

		log.info("Check if file is downloaded at given location");
		if (!DFileUtils.isFileDownloaded(filePath)) {
			throw new Exception("Could not find file");
		}

	}

	public void uploadFile(String filePath, String password) throws Exception {
		getUploadButton().click();

		chooseFileButton.sendKeys(filePath);

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

}



