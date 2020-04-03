package pages.Alert;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
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

    @FindBy(id = "pageGridId")
    public WebElement gridContainer;

    @FindBy(id = "alertsHeader_id")
    public WebElement alertsPageHeader;



    public DGrid grid() {
        return new DGrid(driver, gridContainer);
    }

    public AlertFilters filters(){ return new AlertFilters(driver);}



}
