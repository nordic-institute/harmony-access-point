package pages.Alert;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.GridControls;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DButton;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class AlertPage extends DomibusPage {
    public AlertPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    @FindBy(id = "alertsTable")
    public WebElement gridContainer;

    @FindBy(id = "alertsHeader_id")
    public WebElement alertsPageHeader;

    @FindBy(id="alertsSaveButton")
    public WebElement alertSaveButton;

    @FindBy(id="alertsCancelButton")
    public WebElement alertCancelButton;


    public DGrid grid() {
        return new DGrid(driver, gridContainer);
    }

    public AlertFilters filters() {
        return new AlertFilters(driver);
    }

    public GridControls gridControl() {
        return new GridControls(driver);
    }

    public Dialog confirmationPopup() {
        return new Dialog(driver);
    }

    public DButton getSaveButton(){ return new DButton(driver,alertSaveButton);}

    public DButton getCancelButton(){ return new DButton(driver,alertCancelButton);}

    public WebElement getCssForProcessedCheckBox(int rowNumber){
        return driver.findElement(By.id("processed"+rowNumber+"_id"));
    }
}
