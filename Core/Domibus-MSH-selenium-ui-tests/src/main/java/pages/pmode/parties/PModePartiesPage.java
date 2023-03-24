package pages.pmode.parties;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


public class PModePartiesPage extends DomibusPage {
	@FindBy(css = "#pageGridId")
	WebElement gridContainer;
	@FindBy(css = "#cancelButtonId")
	WebElement cancelButton;
	@FindBy(css = "#saveButtonId")
	WebElement saveButton;
	@FindBy(css = "#addButtonId")
	WebElement newButton;
	@FindBy(css = "#editButtonId")
	WebElement editButton;
	@FindBy(css = "#deleteButtonId")
	WebElement deleteButton;

	public PModePartiesPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DButton getCancelButton() {
		return new DButton(driver, cancelButton);
	}

	public DButton getSaveButton() {
		return new DButton(driver, saveButton);
	}

	public DButton getNewButton() {
		return new DButton(driver, newButton);
	}

	public DButton getEditButton() {
		return new DButton(driver, editButton);
	}

	public DButton getDeleteButton() {
		return new DButton(driver, deleteButton);
	}

	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public PartiesFilters filters() {
		return new PartiesFilters(driver);
	}


	/**
	 * This method will return Party Id which is neither Responder nor initiator
	 *
	 * @param noOfParty Total number of Party available on Pmode parties page
	 * @return the Party Id
	 */
	public String getNoResIniPartyId(int noOfParty) throws Exception {
		for (int i = 0; i < noOfParty; i++) {
			if (grid().getRowSpecificColumnVal(i, "Process (I=Initiator, R=Responder, IR=Both)").isEmpty()) {
				return grid().getRowSpecificColumnVal(i, "Party Id");
			}
		}
		return "";
	}

}
