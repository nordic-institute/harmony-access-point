package pages.Audit;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DLink;
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
    @FindBy(css = "#table_id>div [class='mat-select-arrow']")
    WebElement tableContainerArrow;
    @FindBy(css = "#action_id[placeholder='Action']>div [class='mat-select-arrow']")
    WebElement actionContainerArrow;
    @FindBy(css = "#user_id[placeholder='User']>div [class='mat-select-arrow']")
    WebElement userContainerArrow;

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


    public DomibusPage getPage() {
        return new DomibusPage(driver);
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


    public void setFilterData(String fieldLabel, String data) {
        if (fieldLabel.equalsIgnoreCase("table")) {
            log.debug("Input field label is :" + fieldLabel);
            wait.forElementToBeVisible(tableContainerArrow);
            tableContainerArrow.click();
        } else if (fieldLabel.equalsIgnoreCase("action")) {
            log.debug("Input field label is :" + fieldLabel);
            wait.forElementToBeVisible(actionContainerArrow);
            actionContainerArrow.click();
        } else if (fieldLabel.equalsIgnoreCase("user")) {
            log.debug("Input field label is :" + fieldLabel);
            wait.forElementToBeVisible(userContainerArrow);
            userContainerArrow.click();
        } else {
            log.debug("Invalid Input field label is passed");
        }
        getInputFieldValue(data).click();
        getPage().clickVoidSpace();

    }

    public String getXpathOfInputCheckbox(String fieldName) {
        if(fieldName.equals("Pmode") || fieldName.equals("PluginUser") || fieldName.equals("Message"))
        {
            return ".//*[@class='md2-option md2-option-multiple'][contains(text(),'" + fieldName + "')][1]";

        }
        else if(fieldName.equals("User")) {
            return ".//*[@class='md2-option md2-option-multiple'][contains(text(),'" + fieldName + "')][2]";
        }
        else{
            return ".//*[@class='md2-option md2-option-multiple'][contains(text(),'" + fieldName + "')]";

        }
    }
    public WebElement getInputFieldValue(String fieldLabel) {
        wait.forElementToBeVisible(driver.findElement(By.xpath(getXpathOfInputCheckbox(fieldLabel))));
        return driver.findElement(By.xpath(getXpathOfInputCheckbox(fieldLabel)));
    }

}
