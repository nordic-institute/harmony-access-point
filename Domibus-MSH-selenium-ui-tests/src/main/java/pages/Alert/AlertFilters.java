package pages.Alert;

import ddsl.dcomponents.FilterArea;
import ddsl.dobjects.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlertFilters extends FilterArea {

	//-----------------Basic Filters---------------------
	@FindBy(id = "processed_id")
	public WebElement processedContainer;
	@FindBy(id = "alerttype_id")
	public WebElement alertTypeContainer;
	@FindBy(id = "alertstatus_id")
	public WebElement alertStatusContainer;
	@FindBy(id = "alertlevel_id")
	public WebElement alertLevelContainer;
	@FindBy(id = "creationfrom_id")
	public WebElement creationFromContainer;
	@FindBy(id = "creationto_id")
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
	
	@FindBy(id = "advancedlink_id")
	public WebElement advancedLink;
	
	@FindBy(css="mat-error.mat-error")
	public WebElement alertIdValidation;
	
//	------------------ Selectors for extra filters

	@FindBy(css="div.selectionCriteria div form div.panel.ng-star-inserted")
	public WebElement extraFiltersContainer;



	List<String> processedFilterData = Arrays.asList("UNPROCESSED", "PROCESSED","");
	List<String> alertTypeFilterData = Arrays.asList("", "MSG_STATUS_CHANGED", "CERT_IMMINENT_EXPIRATION", "CERT_EXPIRED", "USER_LOGIN_FAILURE", "USER_ACCOUNT_DISABLED", "USER_ACCOUNT_ENABLED", "PLUGIN_USER_LOGIN_FAILURE", "PLUGIN_USER_ACCOUNT_DISABLED", "PLUGIN_USER_ACCOUNT_ENABLED", "PASSWORD_IMMINENT_EXPIRATION", "PASSWORD_EXPIRED", "PLUGIN_PASSWORD_IMMINENT_EXPIRATION", "PLUGIN_PASSWORD_EXPIRED");
	List<String> alertStatusFilterData = Arrays.asList("SEND_ENQUEUED", "SUCCESS","FAILED","RETRY","");
	List<String> alertLevelFilterData = Arrays.asList("HIGH", "LOW","MEDIUM","");
	
	public boolean isAlertIdValidationMessageVisible() throws Exception {
		try {
			return weToDobject(alertIdValidation).isVisible();
		} catch (Exception e) {
			return false;
		}
	}
	public String getAlertValidationMess() throws Exception {
		return weToDobject(alertIdValidation).getText();
	}
	
	public AlertFilters(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DButton getSearchButton() {
		return new DButton(driver, searchButton);
	}

	public Checkbox getShowDomainCheckbox() {
		return new Checkbox(driver, showDomainAlert);
	}

	public void basicFilterBy(String processedStatus, String alertType, String alertStatus, String alertLevel, String creationFromDate, String creationToDate) throws Exception {
		log.debug("processedStatus = " + processedStatus);
		log.debug("alertType = " + alertType);
		log.debug("alertStatus = " + alertStatus);
		log.debug("alertLevel = " + alertLevel);
		log.debug("CreationFromDate = " + creationFromDate);
		log.debug("CreationToDate = " + creationToDate);

		weToSelect(processedContainer).selectOptionByText(processedStatus);
		weToSelect(alertTypeContainer).selectOptionByText(alertType);
		weToSelect(alertStatusContainer).selectOptionByText(alertStatus);
		weToSelect(alertLevelContainer).selectOptionByText(alertLevel);
		weToDatePicker(creationFromContainer).selectDate(creationFromDate);
		weToDatePicker(creationToContainer).selectDate(creationToDate);

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
		weToSelect(alertTypeContainer).selectOptionByText(alertType);
		weToSelect(alertStatusContainer).selectOptionByText(alertStatus);
		weToSelect(alertLevelContainer).selectOptionByText(alertLevel);
		weToDatePicker(creationFromContainer).selectDate(creationFromDate);
		weToDatePicker(creationToContainer).selectDate(creationToDate);
		weToDInput(alertIdInput).fill(alertId);
		weToDatePicker(reportingFromContainer).selectDate(reportingFromDate);
		weToDatePicker(reportingToContainer).selectDate(reportingToDate);

		clickSearch();
	}


	public void showDomainAlert() throws Exception {
		log.info("switching to domain alerts");
		getShowDomainCheckbox().check();
		getSearchButton().click();
	}

	public DInput getMsgIdInput() {
		return weToDInput(msgIdInput);
	}
	
	public boolean verifyDropdownValues(String fieldLabel) throws Exception {
		List<String> valuesOnPage = new ArrayList<>();
		List<String> expectedValues = new ArrayList<>();
		switch (fieldLabel){
			case "Processed":
				expectedValues = processedFilterData;
				valuesOnPage = weToSelect(processedContainer).getOptionsTexts();
				break;
			case "Alert Type":
				expectedValues = alertTypeFilterData;
				valuesOnPage = weToSelect(alertTypeContainer).getOptionsTexts();
				break;
			case "Alert Status":
				expectedValues = alertStatusFilterData;
				valuesOnPage = weToSelect(alertStatusContainer).getOptionsTexts();
				break;
			case "Alert Level":
				expectedValues = alertLevelFilterData;
				valuesOnPage = weToSelect(alertLevelContainer).getOptionsTexts();
				break;
			default:
				throw new Exception(fieldLabel + " is not in the expected list of dropdown labels");
		}
		clickVoidSpace();
		return CollectionUtils.isEqualCollection(valuesOnPage,expectedValues);
	}
	
	public DLink getAdvancedLink() {
		return weToDLink(advancedLink);
	}
	
	public DInput getAlertIdInput() {
		return weToDInput(alertIdInput);
	}
	
	public Select getProcessedSelect() {
		return weToSelect(processedContainer);
	}

	public boolean isXFiltersSectionVisible(){
		boolean areXFiltersVisible = false;

		try {
			areXFiltersVisible = weToDobject(extraFiltersContainer).isVisible();
		} catch (Exception e) { }

		return areXFiltersVisible;
	}

	public String getXFilterSectionName() throws Exception {

		if(!isXFiltersSectionVisible()){
			return null;
		}
		return weToDobject(extraFiltersContainer.findElement(By.tagName("mat-card-title"))).getText();
	}

		public List<String> getXFilterNames() throws Exception{
		List<String> filterNames = new ArrayList<String>();

		if(!isXFiltersSectionVisible()){
			return filterNames;
		}

		List<WebElement> filterInputs = extraFiltersContainer.findElements(By.tagName("input"));
		List<WebElement> filterDatePick = extraFiltersContainer.findElements(By.tagName("md2-datepicker"));
		for (WebElement filterInput : filterInputs) {
			String placeHolder = weToDobject(filterInput).getAttribute("placeholder");

			if(!StringUtils.isEmpty(placeHolder)){
				filterNames.add(placeHolder);
			}
		}

		for (WebElement element : filterDatePick) {
			String name = weToDobject(element).getAttribute("aria-label");

			if(!StringUtils.isEmpty(name)){
				filterNames.add(name);
			}
		}

		return filterNames;
	}

	public Select getAlertTypeSelect(){
		return weToSelect(alertTypeContainer);
	}


}
