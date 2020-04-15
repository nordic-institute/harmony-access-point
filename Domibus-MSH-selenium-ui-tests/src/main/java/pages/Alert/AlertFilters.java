package pages.Alert;

import ddsl.dcomponents.FilterArea;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DLink;
import ddsl.dobjects.multi_select.MultiSelect;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlertFilters extends FilterArea {

	public AlertFilters(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	//-----------------Basic Filters---------------------
	@FindBy(id = "processed_id")
	public WebElement ProcessedContainer;
	@FindBy(id = "alerttype_id")
	public WebElement alertTypeContainer;
	@FindBy(id = "alertstatus_id")
	public WebElement alertStatusContainer;
	@FindBy(id = "alertlevel_id")
	public WebElement alertLevelContainer;
	@FindBy(id = "creationfrom_id")
	public WebElement creationFromContainer;
	@FindBy(id = "creation_id")
	public WebElement creationToContainer;

	//--------------Advance Filters-------------
	@FindBy(id = "reportingfrom_id")
	public WebElement reportingFromContainer;
	@FindBy(id = "reportingto_id")
	public WebElement reportingToContainer;

	@FindBy(id = "alertid_id")
	public WebElement alertIdInput;
	@FindBy(id = "showDomainAlerts_id")
	public WebElement showDomainAlert;

	//---------Extended Basic Filters------------------
	@FindBy(id = "MESSAGE_ID_id")
	public WebElement msgIdInput;
	@FindBy(id = "OLD_STATUS_id")
	public WebElement oldStatusInput;
	@FindBy(id = "NEW_STATUS_id")
	public WebElement newStatusInput;
	@FindBy(id = "FROM_PARTY_id")
	public WebElement fromPartyInput;
	@FindBy(id = "TO_PARTY_id")
	public WebElement toPartyInput;
	@FindBy(id = "ROLE_id")
	public WebElement roleInput;
	@FindBy(id = "DESCRIPTION_id")
	public WebElement descriptionInput;

	@FindBy(id = "searchbutton_id")
	public WebElement searchButton;

	@FindBy(id="advancedlink_id")
	public WebElement advanceLink;

	@FindBy(css="md-error.mat-input-error")
	public WebElement alertIdValidation;

	public DButton getSearchButton() {
		return new DButton(driver, searchButton);
	}

	public Checkbox getShowDomainCheckbox() {
		return new Checkbox(driver, showDomainAlert);
	}

	public DLink getAdvanceLink() { return new DLink(driver,advanceLink);}

	public DInput getAlertId() { return new DInput(driver,alertIdInput);}

	public MultiSelect getProcessedSelect() { return new MultiSelect(driver, ProcessedContainer);}

	public MultiSelect getAlertTypeSelect() { return new MultiSelect(driver,alertTypeContainer);}

	public MultiSelect getAlertStatusSelect(){ return new MultiSelect(driver,alertStatusContainer);}

	public MultiSelect getAlertLevelSelect() { return new MultiSelect(driver,alertLevelContainer);}

	public void basicFilterBy(String processedStatus, String alertType, String alertStatus, String alertLevel, String creationFromDate, String creationToDate) throws Exception {
		log.debug("processedStatus = " + processedStatus);
		log.debug("alertType = " + alertType);
		log.debug("alertStatus = " + alertStatus);
		log.debug("alertLevel = " + alertLevel);
		log.debug("CreationFromDate = " + creationFromDate);
		log.debug("CreationToDate = " + creationToDate);

		weToSelect(ProcessedContainer).selectOptionByText(processedStatus);
		weToSelect(alertTypeContainer).selectOptionByText(alertType);
		weToSelect(alertStatusContainer).selectOptionByText(alertStatus);
		weToSelect(alertLevelContainer).selectOptionByText(alertLevel);
		weToDatePicker(creationFromContainer).selectDate(creationFromDate);
		weToDatePicker(creationToContainer).selectDate(creationToDate);
		mouseOverAndClick();

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
		weToSelect(alertTypeContainer).selectOptionByText(alertType);
		weToSelect(alertStatusContainer).selectOptionByText(alertStatus);
		weToSelect(alertLevelContainer).selectOptionByText(alertLevel);
		weToDatePicker(creationFromContainer).selectDate(creationFromDate);
		weToDatePicker(creationToContainer).selectDate(creationToDate);
		weToDInput(alertIdInput).fill(alertId);
		weToDatePicker(reportingFromContainer).selectDate(reportingFromDate);
		weToDatePicker(reportingToContainer).selectDate(reportingToDate);
		mouseOverAndClick();
	}


	public void showDomainAlert() throws Exception {
	    log.info("switching to domain alerts");
		getShowDomainCheckbox().check();
		getSearchButton().click();
	}

	public List<String> getAllDropDownOptions(WebElement containerName) throws Exception{
		log.info("Extracting all options for given container");
		List<String> texts =weToSelect(containerName).getOptionsTexts();

		clickVoidSpace();
		return texts;
	}

	public void verifyDropDownOptions(WebElement containerName, String fieldLabel, SoftAssert soft) throws Exception{

		List<String> texts =weToSelect(containerName).getOptionsTexts();

		List<String> processedFilterData = Arrays.asList("UNPROCESSED", "PROCESSED","");
		List<String> alertTypeFilterData = Arrays.asList("MSG_STATUS_CHANGED", "CERT_IMMINENT_EXPIRATION","CERT_EXPIRED","USER_LOGIN_FAILURE","USER_ACCOUNT_DISABLED","PLUGIN_USER_LOGIN_FAILURE","PLUGIN_USER_ACCOUNT_DISABLED","PASSWORD_IMMINENT_EXPIRATION","PASSWORD_EXPIRED","PLUGIN_PASSWORD_IMMINENT_EXPIRATION","PLUGIN_PASSWORD_EXPIRED","");
		List<String> alertStatusFilterData = Arrays.asList("SEND_ENQUEUED", "SUCCESS","FAILED","RETRY","");
		List<String> alertLevelFilterData = Arrays.asList("HIGH", "LOW","MEDIUM","");

		if(fieldLabel.equals("Processed")){
			log.info("Verify Processed filter has all default options present");
			soft.assertTrue(CollectionUtils.isEqualCollection(processedFilterData,texts),"Comparing default value of Processed filter with available option on admin console");
			clickVoidSpace();

		}
		 if(fieldLabel.equals("Alert Type")){
		 	log.info("Verify Alert Type filter has all default options present");
			 soft.assertTrue(CollectionUtils.isEqualCollection(alertTypeFilterData,texts),"Comparing default value of alert type with available option on admin console");
			 clickVoidSpace();

		} if(fieldLabel.equals("Alert Status")){
		 	log.info("Verify Alert Status filter has all default options present ");
			soft.assertTrue(CollectionUtils.isEqualCollection(alertStatusFilterData,texts),"Comparing default value of Alert status with available option on admin console");
			clickVoidSpace();

		} if(fieldLabel.equals("Alert Level")){
		 	log.info("Verify Alert Level filter has all default options present");
			soft.assertTrue(CollectionUtils.isEqualCollection(alertLevelFilterData,texts),"Comparing default value of Alert level with available option on admin console");
			clickVoidSpace();
		 }

		}

}
