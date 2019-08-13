package pages.pmode;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import ddsl.enums.PAGES;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class PModeArchivePage extends DomibusPage {

    public PModeArchivePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    @FindBy(id = "archivePmodeTable")
    private WebElement archiveGridContainer;
    @FindBy(id = "cancelbutton_id")
    private WebElement cancelButton;
    @FindBy(id = "savebutton_id")
    private WebElement saveButton;
    @FindBy(id = "deleteArchivebutton_id")
    private WebElement deleteButton;
    @FindBy(id = "downloadArchivebutton_id")
    private WebElement downloadButton;
    @FindBy(id = "restoreArchivebutton_id")
    private WebElement restoreButton;
    @FindBy(id="deleteButtonRow0_id")
    private WebElement CRowDeleteIcon;
    @FindBy(id="restoreButtonRow0_id")
    private WebElement CRowRestoreIcon;
    @FindBy(css = ".mat-button-wrapper>img")
    private WebElement DownloadCsv;
    @FindBy(xpath = ".//*[contains(text(),'[CURRENT]: Restored')]")
    private WebElement cDescription;
    @FindBy(id="pmodeheader_id")
    private WebElement pModeView;

    public DObject getDownloadCSV(){
        return new DObject(driver,DownloadCsv);}
    public DGrid grid() {
        return new DGrid(driver, archiveGridContainer);
    }
    public DButton getCancelButton() {
        return new DButton(driver, cancelButton);
    }
    public DButton getSaveButton() {
        return new DButton(driver, saveButton);
    }
    public DButton getCDeleteIcon() { return new DButton(driver,CRowDeleteIcon);}
    public DButton getCRestoreIcon() { return new DButton(driver,CRowDeleteIcon);}
    public DButton getDeleteButton() { return new DButton(driver, deleteButton); }
    public DButton getDownloadButton() {return new DButton(driver, downloadButton); }
    public DButton getRestoreButton() { return new DButton(driver, restoreButton); }
    public Pagination getpagination() {  return new Pagination(driver);}
    public Dialog getConfirmation() { return new Dialog(driver);}
    public PModeCurrentPage getPage() { return new PModeCurrentPage(driver); }


    public boolean isArchiveGridEmpty() {
        if (grid().getRowsNo() != 0) {
            log.info("Data is found in Archive grid");
            return false;
        }
        return true;
    }

    public Boolean isLoaded() {
        log.info("check if is loaded");
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

        log.info("Pmode Archive page is loaded");
        return true;
    }

    public void getPmodeStatus() throws Exception {
        wait.forElementToBeVisible(archiveGridContainer);
        if (isArchiveGridEmpty()) {
            getPage().getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
            log.info(getPage().infoTxt.getText().trim());

        }else{

        log.info("Uploaded Pmode count:" + getpagination().getTotalItems());}
    }
public Boolean getCurDescTxt(){
        wait.forElementToBeVisible(cDescription);
        if(!cDescription.isDisplayed())
        {
            return false;
        }
        return true;
}

public boolean pModeView(){
        wait.forElementToBeVisible(pModeView);
        if(!pModeView.isDisplayed()){
            return false;
        }
        return true;
}
    public void clickOk() throws Exception {
        log.info("dialog .. confirm");
//        new DButton(driver, yesBtn).click();
//        wait.forElementToBeGone(yesBtn);
    }
}










