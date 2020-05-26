package pages.Audit;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DLink;
import ddsl.dobjects.multi_select.MultiSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import static pages.Audit.AuditPage.*;


public class AuditSearchFilters extends DComponent {
    public AuditSearchFilters(WebDriver driver) {
        super(driver);
        log.debug("AuditSearchfilter page init");
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    @FindBy(id = "searchbutton_id")
    WebElement searchButton;
    @FindBy(id = "advancedlink_id")
    WebElement advancedSearchExpandLnk;
    @FindBy(id = "basiclink_id")
    WebElement basicSearchLnk;
    @FindBy(id = "to_id")
    WebElement changedToContainer;
    @FindBy(id = "from_id")
    WebElement changedFromContainer;

    @FindBy(css = "#table_id")
    public WebElement tableFilterContainer;

    @FindBy(css = "#user_id:nth-of-type(2)")
    public WebElement userFilterContainer;

    @FindBy(css = "#action_id:nth-of-type(3)")
    public WebElement actionFilterContainer;

    public MultiSelect getTableFilter() {
        return weToMultiSelect(tableFilterContainer);
    }

    public MultiSelect getUserFilter() {
        return weToMultiSelect(userFilterContainer);
    }

    public MultiSelect getActionFilter() {
        return weToMultiSelect(actionFilterContainer);
    }

    public DLink getAdvancedSearchExpandLnk() {
        return new DLink(driver, advancedSearchExpandLnk);
    }

    public AuditPage getAuditPage() {
        return new AuditPage(driver);
    }

    public Pagination getPagination() {
        return new Pagination(driver);
    }


    public DButton getSearchButton() {
        return new DButton(driver, searchButton);
    }

    public DLink getBasicSearchLnk() {
        return new DLink(driver, basicSearchLnk);
    }

    public String getXpathOfSearchFilter(String fieldLabel) {
        return ".//*[@placeholder='" + fieldLabel + "']";
    }

    public WebElement getElementByFieldLabel(String fieldLabel) {
        return driver.findElement(By.xpath(getXpathOfSearchFilter(fieldLabel)));
    }

    public boolean advanceFiltersLoaded() throws Exception {
        log.debug("Loading Advanced filter ");
        return (getElementByFieldLabel(Table_FieldLabel).isDisplayed()
                && getElementByFieldLabel(Action_FieldLabel).isDisplayed()
                && getElementByFieldLabel(User_FieldLabel).isDisplayed()
                && changedFromContainer.isDisplayed()
                && changedToContainer.isDisplayed()
        );
    }

    public void setFilterData(String fieldLabel, String data) throws Exception {
        log.debug("Input field label is : " + fieldLabel + "; selecting " + data + " value...");

        MultiSelect multiSelect = null;
        if (fieldLabel.equalsIgnoreCase("table")) {
            multiSelect = getTableFilter();
        } else if (fieldLabel.equalsIgnoreCase("action")) {
            multiSelect = getActionFilter();
        } else if (fieldLabel.equalsIgnoreCase("user")) {
            multiSelect = getUserFilter();
        } else {
            log.debug("Invalid Input field label is passed");
            return;
        }
        multiSelect.selectOptionByText(data);
    }


}
