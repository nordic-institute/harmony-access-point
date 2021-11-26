package pages.pmode.current;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

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

	public DGrid grid() {
		return new DGrid(driver, archiveGridContainer);
	}

	public DButton getCancelButton() {
		return new DButton(driver, cancelButton);
	}

	public DButton getSaveButton() {
		return new DButton(driver, saveButton);
	}

	public DButton getDeleteButton() {
		return new DButton(driver, deleteButton);
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






}










