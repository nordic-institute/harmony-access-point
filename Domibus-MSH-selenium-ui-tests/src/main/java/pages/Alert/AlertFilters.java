package pages.Alert;

import ddsl.dcomponents.FilterArea;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import pages.Audit.AuditSearchFilters;

public class AlertFilters extends FilterArea {

    public AlertFilters(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }
    //-----------------Basic Filters---------------------
    @FindBy(id = "processed_id")
    public WebElement ProcessedContainer;
    @FindBy(id="alerttype_id")
    public WebElement AlertTypeContainer;
    @FindBy(id="alertstatus_id")
    public WebElement AlertStatusContainer;
    @FindBy(id="alertlevel_id")
    public WebElement AlertLevelContainer;
    @FindBy(id="creationfrom_id")
    public  WebElement CreationFromContainer;
    @FindBy(id="creation_id")
    public WebElement CreationToContainer;
    //--------------Advance Filters-------------
    @FindBy(id="reportingfrom_id")
    public  WebElement ReportingFromContainer;
    @FindBy(id="reportingto_id")
    public WebElement ReportingToContainer;

    @FindBy(id="alertid_id")
    public WebElement alertIdInput;
    @FindBy(id="showDomainAlerts_id")
    public WebElement showDomainAlert;
    //---------Extended Basic Filters------------------
    @FindBy(id="MESSAGE_ID_id")
    public WebElement msgIdInput;
    @FindBy(id="OLD_STATUS_id")
    public WebElement oldStatusInput;
    @FindBy(id="NEW_STATUS_id")
    public WebElement newStatusInput;
    @FindBy(id="FROM_PARTY_id")
    public WebElement fromPartyInput;
    @FindBy(id="TO_PARTY_id")
    public WebElement toPartyInput;
    @FindBy(id="ROLE_id")
    public WebElement roleInput;
    @FindBy(id="DESCRIPTION_id")
    public WebElement descriptionInput;

    @FindBy(id="searchbutton_id")
    public WebElement searchButton;
    public DButton getSearchButton() { return new DButton(driver,searchButton);  }

    public Checkbox getShowDomainCheckbox() { return new Checkbox(driver,showDomainAlert); }

    public void basicFilterBy(String processedStatus, String alertType, String alertStatus, String alertLevel,String creationFromDate, String creationToDate) throws Exception {
        log.debug("processedStatus = " + processedStatus);
        log.debug("alertType = " + alertType);
        log.debug("alertStatus = " + alertStatus);
        log.debug("alertLevel = " + alertLevel);
        log.debug("CreationFromDate = " + creationFromDate);
        log.debug("CreationToDate = "+ creationToDate);

        weToSelect(ProcessedContainer).selectOptionByText(processedStatus);
        weToSelect(AlertTypeContainer).selectOptionByText(alertType);
        weToSelect(AlertStatusContainer).selectOptionByText(alertStatus);
        weToSelect(AlertLevelContainer).selectOptionByText(alertLevel);
        weToDatePicker(CreationFromContainer).selectDate(creationFromDate);
        weToDatePicker(CreationToContainer).selectDate(creationToDate);

        clickSearch();
    }

    public void advancedFilterBy(String processedStatus,
                                 String alertType,
                                 String alertStatus,
                                 String alertId,
                                 String alertLevel,
                                 String creationFromDate,
                                 String creationToDate,
                                 String reportingFromDate,
                                 String reportingToDate) throws Exception {
        log.debug("processedStatus = " + processedStatus);
        log.debug("alertType = " + alertType);
        log.debug("alertStatus = " + alertStatus);
        log.debug("alertId = " + alertId);
        log.debug("alertLevel = " + alertLevel);
        log.debug("CreationFromDate = " + creationFromDate);
        log.debug("CreationToDate = " + creationToDate);
        log.debug("ReportingFrom = " + reportingFromDate);
        log.debug("ReportingTo = " + reportingToDate);


        expandArea();
        weToSelect(AlertTypeContainer).selectOptionByText(alertType);
        weToSelect(AlertStatusContainer).selectOptionByText(alertStatus);
        weToSelect(AlertLevelContainer).selectOptionByText(alertLevel);
        weToDatePicker(CreationFromContainer).selectDate(creationFromDate);
        weToDatePicker(CreationToContainer).selectDate(creationToDate);
        weToDInput(alertIdInput).fill(alertId);
        weToDatePicker(ReportingFromContainer).selectDate(reportingFromDate);
        weToDatePicker(ReportingToContainer).selectDate(reportingToDate);

        clickSearch();
    }


}
