package pages.Alert;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class AlertPage extends DomibusPage {
	@FindBy(id = "pageGridId")
	public WebElement gridContainer;
	
	@FindBy(id = "alertsHeader_id")
	public WebElement alertsPageHeader;
	
	@FindBy(id = "cancelButtonId")
	public WebElement cancelButton;
	
	@FindBy(id = "saveButtonId")
	public WebElement saveButton;
	
	@FindBy(id = "deleteButtonId")
	public WebElement deleteButton;

	
	
	
	public AlertPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public AlertsGrid alertsGrid() {
		return new AlertsGrid(driver, gridContainer);
	}

	public AlertFilters filters() {
		return new AlertFilters(driver);
	}
	
	public DButton getCancelButton() {
		return weToDButton(cancelButton);
	}
	
	public DButton getSaveButton() {
		return weToDButton(saveButton);
	}
	
	public DButton getDeleteButton() {
		return weToDButton(deleteButton);
	}
}
