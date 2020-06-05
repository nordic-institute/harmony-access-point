package pages.pmode.current;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import rest.RestServicePaths;
import utils.Generator;

public class PModeArchivePage extends DomibusPage {

	@FindBy(id = "pageGridId")
	private WebElement archiveGridContainer;
	@FindBy(id = "cancelButtonId")
	private WebElement cancelButton;
	@FindBy(id = "saveButtonId")
	private WebElement saveButton;
	@FindBy(id = "deleteButtonId")
	private WebElement deleteButton;
	@FindBy(id = "downloadArchivebutton_id")
	private WebElement downloadButton;
	@FindBy(id = "restoreArchivebutton_id")
	private WebElement restoreButton;
	@FindBy(id = "deleteButtonRow0_id")
	private WebElement CRowDeleteIcon;
	@FindBy(id = "restoreButtonRow0_id")
	private WebElement CRowRestoreIcon;
	@FindBy(css = ".mat-button-wrapper>img")
	private WebElement DownloadCurrentFile;
	@FindBy(xpath = ".//*[contains(text(),'[CURRENT]: Restored')]")
	private WebElement cDescription;
	@FindBy(id = "pmodeheader_id")
	private WebElement pModeViewHeader;
	@FindBy(css = "div>textarea")
	private WebElement XmlTextArea;
	@FindBy(css = ".mat-raised-button.mat-primary:last-child")
	private WebElement OkButton;
	public PModeArchivePage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DObject getDownloadCSV() {
		return new DObject(driver, DownloadCurrentFile);
	}

	public DGrid grid() {
		return new DGrid(driver, archiveGridContainer);
	}

	public DButton getCancelButton() {
		return new DButton(driver, cancelButton);
	}

	public DButton getSaveButton() {
		return new DButton(driver, saveButton);
	}

	public DButton getCDeleteIcon() {
		return new DButton(driver, CRowDeleteIcon);
	}

	public DButton getCRestoreIcon() {
		return new DButton(driver, CRowDeleteIcon);
	}

	public DButton getDeleteButton() {
		return new DButton(driver, deleteButton);
	}

	public DButton getDownloadButton() {
		return new DButton(driver, downloadButton);
	}

	public DButton getRestoreButton() {
		return new DButton(driver, restoreButton);
	}

	public Pagination getpagination() {
		return new Pagination(driver);
	}

	public Dialog getConfirmation() {
		return new Dialog(driver);
	}

	public DInput getXml() {
		return new DInput(driver, XmlTextArea);
	}

	public DButton getOkButton() {
		return new DButton(driver, OkButton);
	}


	public boolean isArchiveGridEmpty() {
		if (grid().getRowsNo() != 0) {
			log.debug("Data is found in Archive grid");
			return false;
		}
		return true;
	}

	public Boolean isLoaded() {
		log.debug("check if is loaded");
		wait.forElementToBeVisible(cancelButton);
		wait.forElementToBeVisible(saveButton);
		wait.forElementToBeVisible(deleteButton);
		wait.forElementToBeVisible(downloadButton);
		wait.forElementToBeVisible(restoreButton);
		if (cancelButton.isEnabled()
				&& saveButton.isEnabled()
				&& deleteButton.isEnabled()
				&& downloadButton.isEnabled()
				&& restoreButton.isEnabled()) {
			log.error("Buttons are disabled now");
			return false;
		}

		log.debug("Pmode Archive page is loaded");
		return true;
	}

	public Boolean getCurDescTxt() {
		wait.forElementToBeVisible(cDescription);
		if (!cDescription.isDisplayed()) {
			return false;
		}
		return true;
	}

	public void pModeView() {
		wait.forElementToBeVisible(pModeViewHeader);
		if (!pModeViewHeader.isDisplayed()) {
			log.debug("View pop up for pmode is opened");
		}
	}

	public String getXpathOfPmodeViewPopUpHeader(String FieldName) {
		return ".//*[@id='pmodeheader_id'][contains(text(),'" + FieldName + "')]";
	}

	public void DoubleClickRow() throws Exception {
		int rIndex;
		log.debug("Grid row count is: " + grid().getRowsNo());
		if (grid().getRowsNo() > 10) {
			log.debug("Generate random row count");
			rIndex = Generator.randomNumber(10);
		} else {
			rIndex = Generator.randomNumber(grid().getRowsNo());
		}
		if (rIndex == 0) {
			log.debug("Row number is zero");
			log.debug("Double click 0th row ");
			grid().doubleClickRow(rIndex);
			log.debug("Validate View pop up ");
			pModeView();
			WebElement cRow = driver.findElement(By.xpath(getXpathOfPmodeViewPopUpHeader("Current PMode:")));
			wait.forElementToBeVisible(cRow);
			log.debug("View pop up is opened on double click:" + cRow.isDisplayed());
		} else {
			log.debug("Row number is :" + rIndex);
			grid().doubleClickRow(rIndex);
			log.debug("Validate View pop up ");
			pModeView();
			WebElement gRow = driver.findElement(By.xpath(getXpathOfPmodeViewPopUpHeader("Archive")));
			wait.forElementToBeVisible(gRow);
			log.debug("View pop up is opened on double click:" + gRow.isDisplayed());

		}
	}

	public String getRestServicePath() throws Exception {
		String restPath = RestServicePaths.PMODE_CURRENT_DOWNLOAD.concat(String.valueOf(getpagination().getTotalItems()));
		return restPath;
	}

}










