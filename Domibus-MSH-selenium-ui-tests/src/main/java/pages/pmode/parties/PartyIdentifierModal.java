package pages.pmode.parties;

import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.Generator;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class PartyIdentifierModal extends DomibusPage {
	@FindBy(css = "app-party-identifier-details form button:nth-child(1)")
	WebElement okBtn;
	@FindBy(css = "app-party-identifier-details form button:nth-child(2)")
	WebElement cancelBtn;
	@FindBy(css = "#partyId_id")
	WebElement partyIdInput;
	@FindBy(css = "#partyIdType_id")
	WebElement partyIdTypeInput;
	@FindBy(css = "#partyIdValue_id")
	WebElement partyIdValueInput;

	public PartyIdentifierModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public void clickOK() throws Exception {
		new DButton(driver, okBtn).click();
		wait.forElementToBeGone(okBtn);
	}

	public DInput getPartyIdInput() {
		return new DInput(driver, partyIdInput);
	}

	public DInput getPartyIdTypeInput() {
		return new DInput(driver, partyIdTypeInput);
	}

	public DInput getPartyIdValueInput() {
		return new DInput(driver, partyIdValueInput);
	}

	public void fillFields(String partyId) throws Exception {
		getPartyIdInput().fill(partyId);
		getPartyIdTypeInput().fill(Generator.randomAlphaNumeric(5));
		getPartyIdValueInput().fill("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
	}
}
