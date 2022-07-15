package pages.tlsTrustStore;

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

public class TlsTrustStorePage extends DomibusPage {

	@FindBy(css = "#pageGridId")
	WebElement tlsTruststoreTable;

	@FindBy(id = "uploadbutton_id")
	WebElement uploadButton;
	@FindBy(id = "downloadbutton_id")
	WebElement downloadButton;
	@FindBy(id = "addCertificate_id")
	WebElement addCertButton;
	@FindBy(id = "removeCertificate_id")
	WebElement removeCertButton;

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

	@FindBy(css = "app-column-picker > div[class='ng-star-inserted']")
	public WebElement showHideAdditionalArea;

	public TlsTrustStorePage(WebDriver driver) {
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

	public DButton getAddCertButton() {
		return new DButton(driver, addCertButton);
	}

	public DButton getRemoveCertButton() {
		return new DButton(driver, removeCertButton);
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
		log.debug("uploading truststore certificate");

		getUploadButton().click();
		chooseFileButton.sendKeys(filePath);
		getPassInputField().fill(password);

		wait.forElementToBeClickable(okButton);
		getOkButton().click();
	}

	public void addCertificate(String filePath, String alias) throws Exception {

		log.debug("adding certificate");
		getAddCertButton().click();
		chooseFileButton.sendKeys(filePath);
		getAliasInputField().fill(alias);

		wait.forElementToBeClickable(okButton);
		getOkButton().click();
	}

	public Boolean isDefaultElmPresent(Boolean tlsConfig) throws Exception {

		Boolean isElmPresent;

		if (tlsConfig) {
			isElmPresent = !getAlertArea().isShown() && getUploadButton().isEnabled() &&
					getDownloadButton().isEnabled() && getAddCertButton().isEnabled() &&
					getRemoveCertButton().isDisabled();
		} else {
			isElmPresent = getUploadButton().isEnabled() && getAddCertButton().isDisabled()
					&& getRemoveCertButton().isDisabled();
		}

		return isElmPresent;
	}

	public void pressDownloadCertAndSaveFile(String filePath) throws Exception {

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

}



