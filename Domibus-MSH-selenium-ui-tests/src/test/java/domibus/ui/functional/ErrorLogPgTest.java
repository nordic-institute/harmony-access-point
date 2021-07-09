package domibus.ui.functional;

import io.qameta.allure.*;
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

@Epic("")
@Feature("")
public class ErrorLogPgTest extends SeleniumTest {


/* This method will verify domain specific error messages on each domain by changing domain through domain
selector by Super user */
//	TODO: find a better way to select domains and check
/*  ERR-8 - Change current domain  */
@Description("ERR-8 - Change current domain")
@Link(name = "EDELIVERY-5112", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5112")
@AllureId("ERR-8")
@Test(description = "ERR-8", groups = {"multiTenancy"})
public void openErrorLogPage() throws Exception {
SoftAssert soft = new SoftAssert();

String domain = selectRandomDomain();
String domain1 = rest.getNonDefaultDomain();
List<String> messIds = rest.messages().getListOfMessagesIds(domain1);

ErrorLogPage page = new ErrorLogPage(driver);
page.getSidebar().goToPage(PAGES.ERROR_LOG);
page.grid().waitForRowsToLoad();

Allure.step("Compare grid data for both domain");
log.info("Compare grid data for both domain");
List<String> errorIds = page.grid().getListedValuesOnColumn("Message Id");
for (String errorId : errorIds) {
soft.assertFalse(messIds.contains(errorId), "ID is NOT present in list of message ids for the other domain : " + errorId);
}

soft.assertAll();

}

/*  ERR-18 - Check record presence for failed message   */
@Description("ERR-18 - Check record presence for failed message ")
@Link(name = "EDELIVERY-5122", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5122")
@AllureId("ERR-18")
@Test(description = "ERR-18", groups = {"multiTenancy", "singleTenancy"})
public void errorLogForFailedMsg() throws Exception {
SoftAssert soft = new SoftAssert();

String domain = selectRandomDomain();

Allure.step("Navigate to Errors page");
log.info("Navigate to Errors page");
ErrorLogPage page = new ErrorLogPage(driver);
page.getSidebar().goToPage(PAGES.ERROR_LOG);

Allure.step("Wait for grid row to load");
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

/*  disabled pending investigation why there are mo errors than message send attempts */
/*  ERR-19 - Check no of error logs for single  failed message  */
@Description("ERR-19 - Check no of error logs for single  failed message")
@Link(name = "EDELIVERY-5123", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5123")
@AllureId("ERR-19")
@Test(description = "ERR-19", groups = {"multiTenancy", "singleTenancy"})
public void checkNoOFErrorsPerMessage() throws Exception {
SoftAssert soft = new SoftAssert();

String domain = selectRandomDomain();

Allure.step("getting list of failed messages");
log.info("getting list of failed messages");
List<String> ids = rest.getMessageIDsWithStatus(domain, "SEND_FAILURE");
if (ids.size() == 0) {
throw new SkipException("No messages found with status SEND_FAILURE");
}

String messId = ids.get(0);
Allure.step("messID = " + messId);
log.debug("messID = " + messId);

Allure.step("getting send attempts for message");
log.info("getting send attempts for message");
JSONObject message = rest.messages().searchMessage(messId, domain);
int sendAttempts = message.getInt("sendAttempts");
Allure.step("sendAttempts = " + sendAttempts);
log.debug("sendAttempts = " + sendAttempts);

Allure.step("Navigate to Errors page");
log.info("Navigate to Errors page");
ErrorLogPage page = new ErrorLogPage(driver);
page.getSidebar().goToPage(PAGES.ERROR_LOG);

Allure.step("Wait for grid row to load");
log.info("Wait for grid row to load");
page.grid().waitForRowsToLoad();

Allure.step("filter errors by message id");
log.info("filter errors by message id");
page.filters().getMessageIDInput().fill(messId);
page.filters().getSearchButton().click();
page.grid().waitForRowsToLoad();

Allure.step("checking number of errors");
log.info("checking number of errors");
soft.assertEquals(page.grid().getPagination().getTotalItems(), sendAttempts, "Number of errors is number of sendAttempts");

soft.assertAll();

}


/*  ERR-20 - Check Timestamp of each error log record after retry  */
@Description("ERR-20 - Check Timestamp of each error log record after retry")
@Link(name = "EDELIVERY-5124", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5124")
@AllureId("ERR-20")
@Test(description = "ERR-20", groups = {"multiTenancy", "singleTenancy"})
public void checkTimestamp() throws Exception {
SoftAssert soft = new SoftAssert();

String domain = selectRandomDomain();

Allure.step("getting list of failed messages");
log.info("getting list of failed messages");
List<String> ids = rest.getMessageIDsWithStatus(domain, "SEND_FAILURE");
if (ids.size() == 0) {
throw new SkipException("No messages found with status SEND_FAILURE");
}

String messId = ids.get(0);
Allure.step("messID = " + messId);
log.debug("messID = " + messId);

Allure.step("getting failed timestamp for message");
log.info("getting failed timestamp for message");
JSONObject message = rest.messages().searchMessage(messId, domain);
long failed = message.getLong("failed");
Allure.step("failed = " + failed);
log.debug("failed = " + failed);

Allure.step("Navigate to Errors page");
log.info("Navigate to Errors page");
ErrorLogPage page = new ErrorLogPage(driver);
page.getSidebar().goToPage(PAGES.ERROR_LOG);

Allure.step("Wait for grid row to load");
log.info("Wait for grid row to load");
page.grid().waitForRowsToLoad();

Allure.step("filter errors by message id");
log.info("filter errors by message id");
page.filters().getMessageIDInput().fill(messId);
page.filters().getSearchButton().click();
page.grid().waitForRowsToLoad();

List<String> dates = page.grid().getValuesOnColumn("Timestamp");


boolean foundErr = false;
for (String dateStr : dates) {
long errorTime = TestRunData.UI_DATE_FORMAT.parse(dateStr).getTime();
Allure.step("time = " + errorTime);
log.debug("time = " + errorTime);
if (Math.abs(errorTime - failed) < 60000) {
Allure.step("found error");
log.info("found error");
foundErr = true;
}
}

soft.assertTrue(foundErr, "Found error at the time of failed property of message");

soft.assertAll();
}

/* disabled pending investigation why there are more errors than send attempts */
/*  ERR-21 - Check Error Code  Message id  for each record created after each retry for one failed message  */
@Description("ERR-21 - Check Error Code  Message id  for each record created after each retry for one failed message")
@Link(name = "EDELIVERY-5125", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5125")
@AllureId("ERR-21")
@Test(description = "ERR-21", groups = {"multiTenancy", "singleTenancy"})
public void checkErrorCode() throws Exception {
SoftAssert soft = new SoftAssert();
String domain = selectRandomDomain();

Allure.step("getting list of failed messages");
log.info("getting list of failed messages");
List<String> ids = rest.getMessageIDsWithStatus(domain, "SEND_FAILURE");
if (ids.size() == 0) {
throw new SkipException("No messages found with status SEND_FAILURE");
}

String messId = ids.get(0);
Allure.step("messID = " + messId);
log.debug("messID = " + messId);

int sendAttempts = rest.messages().searchMessage(messId, domain).getInt("sendAttempts");

Allure.step("Navigate to Errors page");
log.info("Navigate to Errors page");
ErrorLogPage page = new ErrorLogPage(driver);
page.getSidebar().goToPage(PAGES.ERROR_LOG);

Allure.step("Wait for grid row to load");
log.info("Wait for grid row to load");
page.grid().waitForRowsToLoad();

Allure.step("filter errors by message id");
log.info("filter errors by message id");
page.filters().getMessageIDInput().fill(messId);
page.filters().getSearchButton().click();
page.grid().waitForRowsToLoad();

soft.assertEquals(page.grid().getRowsNo(), sendAttempts, "The number of errors matches the number of send attempts");

soft.assertAll();
}


}
