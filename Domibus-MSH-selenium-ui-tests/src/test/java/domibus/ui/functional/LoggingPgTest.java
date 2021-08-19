package domibus.ui.functional;

import com.mysql.cj.log.Log;
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

import java.util.HashMap;
import java.util.List;

@Epic("Logging")
@Feature("Functional")
public class LoggingPgTest extends SeleniumTest {

	/*This method will verify page presence for different domibus user roles  */
	/*  LOG-1 - Check availability of Logging page to Admin Super admin   */
	@Description("LOG-1 - Check availability of Logging page to Admin Super admin ")
	@Link(name = "EDELIVERY-5353", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5353")
	@AllureId("LOG-1")
	@Test(description = "LOG-1", groups = {"multiTenancy"})
	public void accessRights() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);
		soft.assertNotNull(page.getSidebar().getPageLnk(PAGES.LOGGING), "Link to logging page is present in the sidebar");

		if (data.isMultiDomain()) {
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
		if (!StringUtils.equalsAnyIgnoreCase(loggerLevel, "TRACE")) {
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
		page.getSearchInputField().fill(searchTerm);

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

	/*  EDELIVERY-7179 - LOG-14 - Modify log level for a few packages perform a search and reset logging level   */
	@Test(description = "LOG-14", groups = {"multiTenancy", "singleTenancy"})
	public void searchAndReset() throws Exception {
		String packageNameToModify = "eu.domibus.core.alerts.configuration.certificate";
		String packageNameToSearch = "eu.domibus.api.property";

		SoftAssert soft = new SoftAssert();
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();

		page.setLoggingLevel("ALL", packageNameToModify);

		log.info(" performing search");
		page.search(packageNameToSearch);

		log.info("reset log levels");
		page.getResetButton().click();

		log.info("checking level is properly reset");
		page.search(packageNameToModify);
		soft.assertEquals(page.loggingGrid().getRowInfo(0).get("Logger Level"), "DEBUG", "Level is reset to DEBUG");

		soft.assertAll();
	}


	/*  EDELIVERY-7178 - LOG-13 - Modify log level for a package and then set it to a different value for it's parent package   */
	@Test(description = "LOG-13", groups = {"multiTenancy", "singleTenancy"})
	public void modifyLevelForChildAndParent() throws Exception {
		String parentPackageName = "eu.domibus.core.alerts.configuration.certificate";
		String childPackageName = "eu.domibus.core.alerts.configuration.certificate.expired";

		SoftAssert soft = new SoftAssert();
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();

		page.getResetButton().click();

		page.setLoggingLevel("ALL", childPackageName);

		page.setLoggingLevel("ERROR", parentPackageName);

		soft.assertEquals(page.loggingGrid().getRowInfo("Logger Name", childPackageName).get("Logger Level"), "ALL", "Level is set to ALL after parent level is set to ERROR");

		soft.assertAll();
	}


	/*  EDELIVERY-7177 - LOG-12 - Modify log level for a package and set it to a different value for a subpackage   */
	@Test(description = "LOG-12", groups = {"multiTenancy", "singleTenancy"})
	public void modifyLevelForPackageAndSubpackage() throws Exception {
		String parentPackageName = "eu.domibus.web";
		String childPackageName = "eu.domibus.web.rest";
		String levelForParent = "TRACE";
		String levelForChild = "WARN";

		SoftAssert soft = new SoftAssert();
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();

		page.getResetButton().click();

		page.setLoggingLevel(levelForParent, parentPackageName);
		page.setLoggingLevel(levelForChild, childPackageName);

		page.search(parentPackageName);

		LoggingGrid grid = page.loggingGrid();
		for (int i = 0; i < grid.getRowsNo(); i++) {
			HashMap<String, String> info = grid.getRowInfo(i);
			if (info.get("Logger Name").startsWith(childPackageName)) {
				soft.assertEquals(info.get("Logger Level"), levelForChild);
			} else {
				soft.assertEquals(info.get("Logger Level"), levelForParent);
			}
		}

		soft.assertAll();
	}


	/*  EDELIVERY-7176 - LOG-11 - Modify log level for a particular package  */
	@Test(description = "LOG-11", groups = {"multiTenancy", "singleTenancy"})
	public void modifyLevelForPackage() throws Exception {
		String parentPackageName = "eu.domibus.web";
		String levelForParent = "TRACE";

		SoftAssert soft = new SoftAssert();
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();

		page.getResetButton().click();

		page.setLoggingLevel(levelForParent, parentPackageName);
		page.search(parentPackageName);

		LoggingGrid grid = page.loggingGrid();
		for (int i = 0; i < grid.getRowsNo(); i++) {
			HashMap<String, String> info = grid.getRowInfo(i);
			soft.assertEquals(info.get("Logger Level"), levelForParent);
		}

		soft.assertAll();
	}


}
