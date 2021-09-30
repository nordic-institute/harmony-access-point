package pages.Alert;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import io.qameta.allure.Allure;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.testng.asserts.SoftAssert;

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

	@FindBy(id = "showDomainAlerts_id")
	public WebElement showDomainChkLct;

	
	
	
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

	public void deleteAlertAndVerify(int rowNumber, Boolean isProcessed, SoftAssert soft) throws Exception {
		String alertId = grid().getRowSpecificColumnVal(rowNumber, "Alert Id");

		if(isProcessed){
			filters().getProcessedSelect().selectOptionByText("PROCESSED");
			log.info("Click on search button");
			filters().getSearchButton().click();

		}
		log.info("Delete selected alert");
		grid().selectRow(rowNumber);
		getDeleteButton().click();
		getSaveButton().click();
		new Dialog(driver).confirm();
		grid().waitForRowsToLoad();

		soft.assertTrue(grid().getIndexOf("Alert Id", alertId) < 0);


	}
	public void verifyProcessed(SoftAssert soft, Boolean isProcessed) throws Exception {
		alertsGrid().markAsProcessed(1);
		soft.assertTrue(getSaveButton().isEnabled(),"Save button is enabled");
		soft.assertTrue(getCancelButton().isEnabled(),"Cancel button is enabled");
		getSaveButton().click();
		if(isProcessed) {
			new Dialog(driver).confirm();
		}
		else{
			new Dialog(driver).cancel();
		}
	}

}
