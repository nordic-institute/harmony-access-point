package pages.pmode;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class PModePartiesPage extends DomibusPage {
	public PModePartiesPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(css = "#partyTable")
	WebElement gridContainer;

	@FindBy(css = "app-party > table button:nth-child(1)")
	WebElement cancelButton;

	@FindBy(css = "app-party > table button:nth-child(2)")
	WebElement saveButton;

	@FindBy(css = "app-party > table button:nth-child(3)")
	WebElement newButton;

	@FindBy(css = "app-party > table button:nth-child(4)")
	WebElement editButton;

	@FindBy(css = "app-party > table button:nth-child(5)")
	WebElement deleteButton;


	public DButton getCancelButton() {
		return new DButton(driver, cancelButton);
	}

	public DButton getSaveButton() {
		return new DButton(driver,saveButton);
	}

	public DButton getNewButton() {
		return new DButton(driver,newButton);
	}

	public DButton getEditButton() {
		return new DButton(driver,editButton);
	}

	public DButton getDeleteButton() {
		return new DButton(driver,deleteButton);
	}

	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public PartiesFilters filters(){
		return new PartiesFilters(driver);
	}

}
