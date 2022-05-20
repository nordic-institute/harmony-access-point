package pages.pmode.parties;

import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.Select;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class PartiesFilters extends DomibusPage {
	@FindBy(id = "name_id")
	WebElement nameInput;
	@FindBy(id = "endPoint_id")
	WebElement endpointInput;
	@FindBy(id = "partyID_id")
	WebElement partyIDInput;
	@FindBy(id = "process_id")
	WebElement processInput;
	@FindBy(id = "process_role_id")
	WebElement processRoleSelect;
	@FindBy(id = "searchbutton_id")
	WebElement searchButton;
	public PartiesFilters(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DInput getNameInput() {
		return new DInput(driver, nameInput);
	}

	public DInput getEndpointInput() {
		return new DInput(driver, endpointInput);
	}

	public DInput getPartyIDInput() {
		return new DInput(driver, partyIDInput);
	}

	public DInput getProcessInput() {
		return new DInput(driver, processInput);
	}

	public Select getProcessRoleSelect() {
		return new Select(driver, processRoleSelect);
	}

	public DButton getSearchButton() {
		return new DButton(driver, searchButton);
	}

	public void filter(String name, String endpoint, String partyID, String process, PROCESS_ROLE role) throws Exception {
		getNameInput().fill(name);
		getEndpointInput().fill(endpoint);
		getPartyIDInput().fill(partyID);
		getProcessInput().fill(process);
		getProcessRoleSelect().selectOptionByText(role.name());

		getSearchButton().click();
	}

	public enum PROCESS_ROLE {I, R, IR}


}

