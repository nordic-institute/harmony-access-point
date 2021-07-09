package domibus.ui.functional;

import io.qameta.allure.*;
import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.logging.LoggingGrid;
import pages.logging.LoggingPage;

import java.util.List;

@Epic("")
@Feature("")
public class LoggingPgTest extends SeleniumTest {

/*This method will verify page presence for different domibus user roles  */
/*  LOG-1 - Check availability of Logging page to Admin Super admin   */
@Description("LOG-1 - Check availability of Logging page to Admin Super admin ")
@Link(name = "EDELIVERY-5353", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5353")
@AllureId("LOG-1")
@Test(description = "LOG-1", groups = {"multiTenancy", "singleTenancy"})
public void accessRights() throws Exception {
SoftAssert soft = new SoftAssert();

DomibusPage page = new DomibusPage(driver);
soft.assertNotNull(page.getSidebar().getPageLnk(PAGES.LOGGING), "Link to logging page is present in the sidebar");

if(data.isMultiDomain()) {
Allure.step("get new domain admin");
log.info("get new domain admin");
String username = rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName");

Allure.step("login with admin " + username);
log.info("login with admin " + username);
page = login(username, data.defaultPass());
soft.assertNotNull(page.getSidebar().getPageLnk(PAGES.LOGGING), "Link to logging page is present in the sidebar");

}
soft.assertAll();

}

/* Check Ui of Logging page */
/*  LOG-2 - Check Ui of Logging page   */
@Description("LOG-2 - Check Ui of Logging page ")
@Link(name = "EDELIVERY-5354", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5354")
@AllureId("LOG-2")
@Test(description = "LOG-2", groups = {"multiTenancy", "singleTenancy"})
public void openPage() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("Login and go to Logging page");
log.info("Login and go to Logging page");
LoggingPage page = new LoggingPage(driver);
page.getSidebar().goToPage(PAGES.LOGGING);
page.grid().waitForRowsToLoad();

Allure.step("Checking page components");
log.info("Checking page components");
soft.assertTrue(page.getPackageClassInputField().isVisible() , "search input is visible");
soft.assertTrue(page.getSearchButton().isVisible() , "search button is visible");
soft.assertTrue(page.getResetButton().isVisible() , "reset button is visible");
soft.assertTrue(page.getShowClassesCheckbox().isVisible() , "show classes checkbox is visible");
soft.assertFalse(page.getShowClassesCheckbox().isChecked() , "show classes checkbox is not checked by default");
soft.assertTrue(page.loggingGrid().isPresent() , "grid with loggers is shown");
soft.assertTrue(page.loggingGrid().getRowsNo() > 0 , "grid has loggers listed");

soft.assertAll();
}

/*  LOG-3 - Check default value in search filter   */
@Description("LOG-3 - Check default value in search filter ")
@Link(name = "EDELIVERY-5355", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5355")
@AllureId("LOG-3")
@Test(description = "LOG-3", groups = {"multiTenancy", "singleTenancy"})
public void verifyPackageName() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("Login and go to Logging page");
log.info("Login and go to Logging page");
LoggingPage page = new LoggingPage(driver);
page.getSidebar().goToPage(PAGES.LOGGING);
page.grid().waitForRowsToLoad();

Allure.step("Checking page components");
log.info("Checking page components");
soft.assertTrue(page.getPackageClassInputField().isVisible() , "search input is visible");

soft.assertTrue(page.getPackageClassInputField().getText().equals("eu.domibus"),"default package name is correct");
soft.assertAll();
}

/*  Verify Checkbox feature to show classes */
/*  LOG-4 - User selected checkbox for Show classes    */
@Description("LOG-4 - User selected checkbox for Show classes  ")
@Link(name = "EDELIVERY-5356", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5356")
@AllureId("LOG-4")
@Test(description = "LOG-4", groups = {"multiTenancy", "singleTenancy"})
public void verifyCheckbox() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("Login into application with admin user");
log.info("Login into application with admin user");
LoggingPage page = new LoggingPage(driver);
page.getSidebar().goToPage(PAGES.LOGGING);
page.grid().waitForRowsToLoad();

LoggingGrid grid = page.loggingGrid();
Allure.step("Total number of Packages shown on logging page : " + grid.getPagination().getTotalItems());
log.info("Total number of Packages shown on logging page : " + grid.getPagination().getTotalItems());
int prevCount = grid.getPagination().getTotalItems();
soft.assertTrue(!page.getShowClassesCheckbox().isChecked(), "Checkbox is not checked");

Allure.step("Click and check show classes check box");
log.info("Click and check show classes check box");
page.getShowClassesCheckbox().click();

soft.assertTrue(page.getShowClassesCheckbox().isChecked(), " Checkbox is checked now");

Allure.step("Click on search button");
log.info("Click on search button");
page.getSearchButton().click();

Allure.step("wait for rows to load");
log.info("wait for rows to load");
page.grid().waitForRowsToLoad();
Allure.step("Total no of package and classes shown after checking show classes checkbox " + grid.getPagination().getTotalItems());
log.info("Total no of package and classes shown after checking show classes checkbox " + grid.getPagination().getTotalItems());

int newCount = grid.getPagination().getTotalItems();

soft.assertTrue(prevCount < newCount, " Current count count is greater than previous one");
soft.assertAll();
}


/* This method will verify reset button functionality  */
/*  LOG-5 - Modify log levels then reset them via RESET button   */
@Description("LOG-5 - Modify log levels then reset them via RESET button ")
@Link(name = "EDELIVERY-5357", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5357")
@AllureId("LOG-5")
@Test(description = "LOG-5", groups = {"multiTenancy", "singleTenancy"})
public void verifyReset() throws Exception {
SoftAssert soft = new SoftAssert();
LoggingPage page = new LoggingPage(driver);

Allure.step("Login into application with admin user");
log.info("Login into application with admin user");
page.getSidebar().goToPage(PAGES.LOGGING);
page.loggingGrid().waitForRowsToLoad();

String loggerLevel = page.loggingGrid().getRowInfo(0).get("Logger Level");
Allure.step("current logger level for row 0 : " + loggerLevel);
log.info("current logger level for row 0 : " + loggerLevel);

Allure.step("Validate Current logger level");
log.info("Validate Current logger level");
if (!StringUtils.equalsAnyIgnoreCase(loggerLevel,"TRACE")) {
Allure.step("Change logger level to TRACE");
log.info("Change logger level to TRACE");
page.loggingGrid().setLoggLevel(0, "TRACE");
soft.assertEquals(page.loggingGrid().getRowInfo(0).get("Logger Level"), "TRACE", "Updated logger level for row 0 is correct ");
Allure.step("Click on reset button");
log.info("Click on reset button");
page.getResetButton().click();
Allure.step("Wait for grid row to upload");
log.info("Wait for grid row to upload");
page.grid().waitForRowsToLoad();

String resetLoggerLevel = page.loggingGrid().getRowInfo(0).get("Logger Level");
Allure.step("Logger level for row 0 after reset" + resetLoggerLevel);
log.info("Logger level for row 0 after reset" + resetLoggerLevel);
soft.assertEquals(resetLoggerLevel, loggerLevel, "Updated logger level for row 0 is correct ");
}

soft.assertAll();
}

/* This method will verify search data for package and class name field*/
/*  LOG-6 - Check search data for Package or class name filed   */
@Description("LOG-6 - Check search data for Package or class name filed ")
@Link(name = "EDELIVERY-5358", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5358")
@AllureId("LOG-6")
@Test(description = "LOG-6", groups = {"multiTenancy", "singleTenancy"})
public void searchData() throws Exception {
String searchTerm = "cxf";
SoftAssert soft = new SoftAssert();
LoggingPage page = new LoggingPage(driver);

Allure.step("Login into application with admin user");
log.info("Login into application with admin user");
page.getSidebar().goToPage(PAGES.LOGGING);

Allure.step("Enter cxf in search filter");
log.info("Enter cxf in search filter");
page.getPackageClassInputField().fill(searchTerm);

Allure.step("Click on search button");
log.info("Click on search button");
page.getSearchButton().click();
page.grid().waitForRowsToLoad();

List<String> names = page.loggingGrid().getListedValuesOnColumn("Logger Name");
Allure.step("Verify cxf presence in  package and class name  shown as a search result");
log.info("Verify cxf presence in  package and class name  shown as a search result");

for (String name : names) {
soft.assertTrue(name.contains(searchTerm), String.format("Logger Name %s contains search string %s", name, searchTerm));
}

soft.assertAll();
}

/* LOG-7 - User changes number of visible rows */
/*  LOG-7 - User changes number of visible rows  */
@Description("LOG-7 - User changes number of visible rows")
@Link(name = "EDELIVERY-5359", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5359")
@AllureId("LOG-7")
@Test(description = "LOG-7", groups = {"multiTenancy", "singleTenancy"})
public void changeNumberOfVisibleRows() throws Exception {
SoftAssert soft = new SoftAssert();
LoggingPage page = new LoggingPage(driver);

Allure.step("Login into application with admin user");
log.info("Login into application with admin user");
page.getSidebar().goToPage(PAGES.LOGGING);
page.grid().waitForRowsToLoad();

Allure.step("Extract total number of items available");
log.info("Extract total number of items available");
page.grid().getPagination().getTotalItems();

Allure.step("Check number of expected pages ");
log.info("Check number of expected pages ");
int totalPagesBeforeUpdate = page.grid().getPagination().getExpectedNoOfPages();

Allure.step("Check presence of last page by navigation");
log.info("Check presence of last page by navigation");
page.grid().getPagination().goToPage(totalPagesBeforeUpdate);
page.grid().waitForRowsToLoad();

Allure.step("Change page selector size value to 25");
log.info("Change page selector size value to 25");
page.grid().getPagination().getPageSizeSelect().selectOptionByText("25");
page.grid().waitForRowsToLoad();

soft.assertTrue(page.grid().getRowsNo() == 25, "Number of rows is  equal to 25");

Allure.step("Calculate no of expected pages ");
log.info("Calculate no of expected pages ");
int totalPagesAfterUpdate = page.grid().getPagination().getExpectedNoOfPages();

Allure.step("Verify last page presence by navigation ");
log.info("Verify last page presence by navigation ");
page.grid().getPagination().goToPage(totalPagesAfterUpdate);

soft.assertAll();
}


/* EDELIVERY-7181 - LOG-16 - Sort the grid */
/*  LOG-16 - Sort the grid  */
@Description("LOG-16 - Sort the grid")
@Link(name = "EDELIVERY-7181", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7181")
@AllureId("LOG-16")
@Test(description = "LOG-16", groups = {"multiTenancy", "singleTenancy"})
public void gridNotSortable() throws Exception {
SoftAssert soft = new SoftAssert();
LoggingPage page = new LoggingPage(driver);
page.getSidebar().goToPage(PAGES.LOGGING);
page.grid().waitForRowsToLoad();

String columnName = page.grid().getSortedColumnName();
soft.assertNull(columnName, "do default sorted column");

page.loggingGrid().sortBy("Logger Name");

columnName = page.grid().getSortedColumnName();
soft.assertNull(columnName, "column marked as sorted");

soft.assertAll();
}



}
