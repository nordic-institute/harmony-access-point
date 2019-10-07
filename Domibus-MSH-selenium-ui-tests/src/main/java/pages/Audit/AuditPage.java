package pages.Audit;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.List;


public class AuditPage extends DomibusPage {

    public AuditPage(WebDriver driver) {
        super(driver);
        log.debug("Audit page init");
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

    }

    public static final String Table_FieldLabel = "Table";
    public static final String User_FieldLabel = "User";
    public static final String Action_FieldLabel = "Action";

    @FindBy(id = "auditTable")
    public WebElement gridContainer;
    @FindBy(id = "auditHeader_id")
    public WebElement auditPageHeader;
    @FindBy(css = "datatable-row-wrapper > datatable-body-row")
    List<WebElement> gridRows;

    public DGrid grid() {
        return new DGrid(driver, gridContainer);
    }


    public AuditSearchFilters getFilters() {
        return new AuditSearchFilters(driver);
    }

    public Pagination getPagination() {
        return new Pagination(driver);
    }

    public boolean isLoaded() throws Exception {
        wait.forElementToBeVisible(auditPageHeader);
        if (!getFilters().getElementByFieldLabel(Table_FieldLabel).isDisplayed()) {
            return false;
        }
        if (!getFilters().getElementByFieldLabel(User_FieldLabel).isDisplayed()) {
            return false;
        }
        if (!getFilters().getElementByFieldLabel(Action_FieldLabel).isDisplayed()) {
            return false;
        }
        wait.forElementToBeVisible(getFilters().searchButton);
        if (!getFilters().searchButton.isEnabled()) {
            return false;

        }
        wait.forElementToBeVisible(getFilters().advancedSearchExpandLnk);
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
            log.debug("Data is found in Archive grid");
            return false;
        }
        return true;
    }

    public Boolean isRowSelected(int rowNumber) {
        if (gridRows.get(rowNumber).getAttribute("class").contains("active")) {
            log.debug("Row is selected on double click");
            return false;
        } else {
            log.debug("Row is not selected on double click");
            return true;
        }
    }
    public String getActionData(String tableName, int rowIndex)throws Exception{
        if(tableName.equals("Pmode")){
            if(grid().getRowInfo(rowIndex).containsValue("Created")){
                log.info("Action field data for row : " + rowIndex +" is Created");
                return "Created";
            }
            else if(grid().getRowInfo(rowIndex).containsValue("Deleted")){
                log.info("Action field data for row : " + rowIndex +" is Deleted");
                return "Deleted";
            }
            else{
                log.info("Some wrong action is logged for the event");
                return "";
            }
        }
        return "";

    }
}


