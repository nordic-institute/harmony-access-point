package pages.Audit;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;



public class AuditPage extends DomibusPage {

    public AuditPage(WebDriver driver) {
        super(driver);
        log.info("Messages page init");
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

    }
    public static final String Table_FieldLabel="Table";
    public static final String User_FieldLabel="User";
    public static final String Action_FieldLabel="Action";

    @FindBy(id = "auditTable")
    private WebElement gridContainer;


    public DGrid grid() {
        return new DGrid(driver, gridContainer);
    }

    public AuditSearchFilters getFilters() {
        return new AuditSearchFilters(driver);
    }
    public Pagination getPagination() { return new Pagination(driver);}

    public boolean isLoaded() throws Exception {
        wait.forElementToBeVisible(getFilters().getElementByFieldLabel(Table_FieldLabel));
        wait.forElementToBeVisible(getFilters().getElementByFieldLabel(User_FieldLabel));
        wait.forElementToBeVisible(getFilters().getElementByFieldLabel(Action_FieldLabel));

        if(!getFilters().getElementByFieldLabel(Table_FieldLabel).isDisplayed()){
            return false;
        }
        if (!getFilters().getElementByFieldLabel(User_FieldLabel).isDisplayed()) {
            return false;
        }
        if (!getFilters().getElementByFieldLabel(Action_FieldLabel).isDisplayed()) {
            return false;
        }
        if (!getFilters().searchButton.isEnabled()) {
            return false;
        }
        if (!getFilters().searchButton.isEnabled()) {
            return false;
        }
        if (!getFilters().advancedSearchExpandLnk.isDisplayed()) {
            return false;
        }

        if (Integer.valueOf(grid().getPagination().getPageSizeSelect().getSelectedValue()) != 10) {
            return false;
        }
        return true;
    }


    public boolean isGridEmpty() {
        if (grid().getRowsNo() != 0) {
            log.info("Data is found in Archive grid");
            return false;
        }
        return true;
    }

}
