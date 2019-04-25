package pages.truststore;

import ddsl.dcomponents.popups.InfoModal;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class TruststoreModal extends InfoModal {
	public TruststoreModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

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
}
