package pages.errorLog;

import ddsl.dcomponents.FilterArea;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DLink;
import ddsl.dobjects.DatePicker;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class ErrFilters extends FilterArea {

	@FindBy(id = "searchbutton_id")
	public WebElement searchButton;
	//-------------------------------------- Basic filters --------------------------
	@FindBy(id = "signalmessageid_id")
	public WebElement signalMessIDInput;
	@FindBy(id = "messageid_id")
	public WebElement messageIDInput;
	@FindBy(id = "advancedlink_id")
	public WebElement advancedLink;
	@FindBy(id = "fromtimestamp_id")
	public WebElement errFromContainer;
	@FindBy(id = "totimestamp_id")
	public WebElement errToContainer;
	//-------------------------------------- Advanced filters --------------------------
	@FindBy(id = "errordetail_id")
	public WebElement errordetailInput;
	@FindBy(id = "aprole_id")
	public WebElement apRole;
	@FindBy(id = "errorcode_id")
	public WebElement errorCode;
	@FindBy(id = "notifiedfrom_id")
	public WebElement notifiedFrom;
	@FindBy(id = "notifiedto_id")
	public WebElement notifiedto;

	public ErrFilters(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DInput getSignalMessIDInput() {
		return new DInput(driver, signalMessIDInput);
	}

	public DInput getMessageIDInput() {
		return new DInput(driver, messageIDInput);
	}

	public DButton getSearchButton() {
		return new DButton(driver, searchButton);
	}

	public DLink getAdvancedLink() {
		return new DLink(driver, advancedLink);
	}

	public DatePicker getErrFrom() {
		return new DatePicker(driver, errFromContainer);
	}

	public DatePicker getErrTo() {
		return new DatePicker(driver, errToContainer);
	}


	public void basicSearch(String signalMessID, String messageID, String fromDate, String toDate) throws Exception {
		log.debug("submit basic search");

		getSignalMessIDInput().fill(signalMessID);
		getMessageIDInput().fill(messageID);

		getErrFrom().selectDate(fromDate);
		getErrTo().selectDate(toDate);

		getSearchButton().click();
	}

	public void advancedSearch(String signalMessID
			, String messageID
			, String fromDate
			, String toDate
			, String errDetail
			, String apRoleStr
			, String errCode
			, String notifiedDateFrom
			, String notifiedDateTo) throws Exception {

		log.debug("signalMessID = " + signalMessID);
		log.debug("messageID = " + messageID);
		log.debug("fromDate = " + fromDate);
		log.debug("toDate = " + toDate);
		log.debug("errDetail = " + errDetail);
		log.debug("apRoleStr = " + apRoleStr);
		log.debug("errCode = " + errCode);
		log.debug("notifiedDateFrom = " + notifiedDateFrom);
		log.debug("notifiedDateTo = " + notifiedDateTo);

		expandArea();

		getSignalMessIDInput().fill(signalMessID);
		getMessageIDInput().fill(messageID);
		getErrFrom().selectDate(fromDate);
		getErrTo().selectDate(toDate);

		weToDInput(errordetailInput).fill(errDetail);
		weToSelect(apRole).selectOptionByText(apRoleStr);
		weToSelect(errorCode).selectOptionByText(errCode);
		weToDatePicker(notifiedFrom).selectDate(notifiedDateFrom);
		weToDatePicker(notifiedto).selectDate(notifiedDateTo);

		getSearchButton().click();
	}

}
