package pages.tlsTrustStore;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.GridControls;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

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

    public void uploadAddCert(String filePath, String password, DButton button, DInput inputField) throws Exception {

        button.click();
        wait.forXMillis(100);
        chooseFileButton.sendKeys(filePath);
        inputField.fill(password);

        wait.forElementToBeClickable(okButton);
        getOkButton().click();
    }
    public Boolean isDefaultElmPresent(Boolean tlsConfig) throws Exception {

        if (tlsConfig) {
            Boolean isElmPresent = !getAlertArea().isShown() && getUploadButton().isEnabled() &&
                    getDownloadButton().isEnabled() && getAddCertButton().isEnabled() &&
                    getRemoveCertButton().isDisabled();

            return isElmPresent;
        } else {
            Boolean isElmPresent =  getUploadButton().isEnabled() && getAddCertButton().isDisabled()
                    && getRemoveCertButton().isDisabled();
            return isElmPresent;
        }

    }
}



