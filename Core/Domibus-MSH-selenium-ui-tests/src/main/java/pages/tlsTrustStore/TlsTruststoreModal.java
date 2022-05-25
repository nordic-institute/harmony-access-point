package pages.tlsTrustStore;

import ddsl.dcomponents.popups.InfoModal;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


/**
 * @author Rupam
 * @since 5.0
 */

public class TlsTruststoreModal extends InfoModal {
    @FindBy(css = "input[placeholder=Name]")
    WebElement nameInput;
    @FindBy(css = "input[placeholder=Subject]")
    WebElement subjectInput;
    @FindBy(css = "input[placeholder=Issuer]")
    WebElement issuerInput;
    @FindBy(css = "input[placeholder=\"Valid from\"]")
    WebElement validFromInput;
    @FindBy(css = "input[placeholder=\"Valid until\"]")
    WebElement validToInput;

    public TlsTruststoreModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }
    public DInput getNameInput() {
        return new DInput(driver, nameInput);
    }

    public DInput getSubjectInput() {
        return new DInput(driver, subjectInput);
    }

    public DInput getIssuerInput() {
        return new DInput(driver, issuerInput);
    }

    public DInput getValidFromInput() {
        return new DInput(driver, validFromInput);
    }

    public DInput getValidToInput() {
        return new DInput(driver, validToInput);
    }


    public String getFieldData(String fieldLabel){
        if(fieldLabel.equals("Valid from")){
            return validFromInput.getText();
        }
        if(fieldLabel.equals("Issuer")){
            return getSubjectInput().getText();
        }
        if(fieldLabel.equals("Valid until")){
            return getValidToInput().getText();
        }
        if(fieldLabel.equals("Subject")){
            return getSubjectInput().getText();
        }
        return "";
    }

}
