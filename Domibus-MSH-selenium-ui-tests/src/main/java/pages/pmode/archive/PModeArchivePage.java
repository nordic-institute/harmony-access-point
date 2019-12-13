package pages.pmode.archive;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class PModeArchivePage extends DomibusPage {


	public PModeArchivePage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}


	@FindBy(id = "pageGridId")
	public WebElement gridContainer;

	@FindBy(id = "cancelbutton_id")
	public WebElement cancelBtn;

	@FindBy(id = "savebutton_id")
	public WebElement saveBtn;

	@FindBy(id = "deleteArchivebutton_id")
	public WebElement deleteBtn;

	@FindBy(id = "downloadArchivebutton_id")
	public WebElement downloadBtn;

	@FindBy(id = "restoreArchivebutton_id")
	public WebElement restoreBtn;

	public PMAGrid pmagrid() {
		return new PMAGrid(driver, gridContainer);
	}

	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public DButton getCancelBtn() {
		return weToDButton(cancelBtn);
	}

	public DButton getSaveBtn() {
		return weToDButton(saveBtn);
	}

	public DButton getDeleteBtn() {
		return weToDButton(deleteBtn);
	}

	public DButton getDownloadBtn() {
		return weToDButton(downloadBtn);
	}

	public DButton getRestoreBtn() {
		return weToDButton(restoreBtn);
	}

	public String deleteRow(int rowNo) throws Exception {
		log.info("deleting row " + rowNo);
		String toRet = grid().getRowInfo(rowNo).get("Configuration Date");

		grid().selectRow(rowNo);

		getDeleteBtn().click();
		getSaveBtn().click();

		new Dialog(driver).confirm();
		return toRet;
	}

}
