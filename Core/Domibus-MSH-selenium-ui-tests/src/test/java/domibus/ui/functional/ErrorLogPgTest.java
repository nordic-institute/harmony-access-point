package domibus.ui.functional;

import org.testng.Reporter;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.errorLog.ErrorLogPage;
import utils.TestRunData;

import java.util.List;

public class ErrorLogPgTest extends SeleniumTest {


	/* EDELIVERY-5112 - ERR-8 - Change current domain */
	@Test(description = "ERR-8", groups = {"multiTenancy"})
	public void errorLogDomainSegregation() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> messIds = rest.getMessageIDsWithStatus(null, "SEND_FAILURE");

		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		page.getDomainSelector().selectAnotherDomain();
		page.grid().waitForRowsToLoad();

		Reporter.log("Compare grid data for both domain");
		log.info("Compare grid data for both domain");
		List<String> errorIds = page.grid().getListedValuesOnColumn("Message Id");
		for (String errorId : errorIds) {
			soft.assertFalse(messIds.contains(errorId), "ID is NOT present in list of message ids for the other domain : " + errorId);
		}

		soft.assertAll();

	}

	/* EDELIVERY-5122 - ERR-18 - Check record presence for failed message  */
	@Test(description = "ERR-18", groups = {"multiTenancy", "singleTenancy"})
	public void errorLogForFailedMsg() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		Reporter.log("Navigate to Errors page");
		log.info("Navigate to Errors page");
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		Reporter.log("Wait for grid row to load");
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();

		List<String> ids = rest.getMessageIDsWithStatus(domain, "SEND_FAILURE");
		if (ids.size() == 0) {
			throw new SkipException("No messages found with status SEND_FAILURE");
		}

		page.filters().getMessageIDInput().fill(ids.get(0));
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.grid().scrollTo("Message Id", ids.get(0)) > -1, "Message ID found in error page");


		soft.assertAll();

	}

	/* EDELIVERY-5123 - ERR-19 - Check no of error logs for single  failed message */
	@Test(description = "ERR-19", groups = {"multiTenancy", "singleTenancy"})
	public void checkNoOFErrorsPerMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		Reporter.log("getting list of failed messages");
		log.info("getting list of failed messages");
		List<String> ids = rest.getMessageIDsWithStatus(domain, "SEND_FAILURE");
		if (ids.size() == 0) {
			throw new SkipException("No messages found with status SEND_FAILURE");
		}

		String messId = ids.get(0);
		Reporter.log("messID = " + messId);
		log.debug("messID = " + messId);

		Reporter.log("getting send attempts for message");
		log.info("getting send attempts for message");
		JSONObject message = rest.messages().searchMessage(messId, domain);
		int sendAttempts = message.getInt("sendAttempts");
		Reporter.log("sendAttempts = " + sendAttempts);
		log.debug("sendAttempts = " + sendAttempts);

		Reporter.log("Navigate to Errors page");
		log.info("Navigate to Errors page");
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		Reporter.log("Wait for grid row to load");
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();

		Reporter.log("filter errors by message id");
		log.info("filter errors by message id");
		page.filters().getMessageIDInput().fill(messId);
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Reporter.log("checking number of errors");
		log.info("checking number of errors");
		soft.assertEquals(page.grid().getPagination().getTotalItems(), sendAttempts, "Number of errors is number of sendAttempts");

		soft.assertAll();

	}


	/* EDELIVERY-5124 - ERR-20 - Check Timestamp of each error log record after retry */
	@Test(description = "ERR-20", groups = {"multiTenancy", "singleTenancy"})
	public void checkTimestamp() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		Reporter.log("getting list of failed messages");
		log.info("getting list of failed messages");
		List<String> ids = rest.getMessageIDsWithStatus(domain, "SEND_FAILURE");
		if (ids.size() == 0) {
			throw new SkipException("No messages found with status SEND_FAILURE");
		}

		String messId = ids.get(0);
		Reporter.log("messID = " + messId);
		log.debug("messID = " + messId);

		Reporter.log("getting failed timestamp for message");
		log.info("getting failed timestamp for message");
		JSONObject message = rest.messages().searchMessage(messId, domain);
		long failed = message.getLong("failed");
		Reporter.log("failed = " + failed);
		log.debug("failed = " + failed);

		Reporter.log("Navigate to Errors page");
		log.info("Navigate to Errors page");
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		Reporter.log("Wait for grid row to load");
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();

		Reporter.log("filter errors by message id");
		log.info("filter errors by message id");
		page.filters().getMessageIDInput().fill(messId);
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		List<String> dates = page.grid().getValuesOnColumn("Timestamp");


		boolean foundErr = false;
		for (String dateStr : dates) {
			long errorTime = TestRunData.UI_DATE_FORMAT.parse(dateStr).getTime();
			Reporter.log("time = " + errorTime);
			log.debug("time = " + errorTime);
			if (Math.abs(errorTime - failed) < 60000) {
				Reporter.log("found error");
				log.info("found error");
				foundErr = true;
			}
		}

		soft.assertTrue(foundErr, "Found error at the time of failed property of message");

		soft.assertAll();
	}

	/* EDELIVERY-5125 - ERR-21 - Check Error Code  Message id  for each record created after each retry for one failed message */
	@Test(description = "ERR-21", groups = {"multiTenancy", "singleTenancy"})
	public void checkErrorCode() throws Exception {
		SoftAssert soft = new SoftAssert();
		String domain = selectRandomDomain();

		Reporter.log("getting list of failed messages");
		log.info("getting list of failed messages");
		List<String> ids = rest.getMessageIDsWithStatus(domain, "SEND_FAILURE");
		if (ids.size() == 0) {
			throw new SkipException("No messages found with status SEND_FAILURE");
		}

		String messId = ids.get(0);
		Reporter.log("messID = " + messId);
		log.debug("messID = " + messId);

		int sendAttempts = rest.messages().searchMessage(messId, domain).getInt("sendAttempts");

		Reporter.log("Navigate to Errors page");
		log.info("Navigate to Errors page");
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		Reporter.log("Wait for grid row to load");
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();

		Reporter.log("filter errors by message id");
		log.info("filter errors by message id");
		page.filters().getMessageIDInput().fill(messId);
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.grid().getRowsNo(), sendAttempts, "The number of errors matches the number of send attempts");

		soft.assertAll();
	}


}
