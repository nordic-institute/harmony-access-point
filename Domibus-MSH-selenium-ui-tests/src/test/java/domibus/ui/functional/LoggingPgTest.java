package domibus.ui.functional;

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

public class LoggingPgTest extends SeleniumTest {
	
	/*This method will verify page presence for different domibus user roles  */
	@Test(description = "LOG-1", groups = {"multiTenancy", "singleTenancy"})
	public void accessRights() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		DomibusPage page = new DomibusPage(driver);
		soft.assertNotNull(page.getSidebar().getPageLnk(PAGES.LOGGING), "Link to logging page is present in the sidebar");
		
		if(data.isMultiDomain()) {
			log.info("get new domain admin");
			String username = rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName");
			
			log.info("login with admin " + username);
			page = login(username, data.defaultPass());
			soft.assertNotNull(page.getSidebar().getPageLnk(PAGES.LOGGING), "Link to logging page is present in the sidebar");
		
		}
		soft.assertAll();
		
	}
	
	/* Check Ui of Logging page */
	@Test(description = "LOG-2", groups = {"multiTenancy", "singleTenancy"})
	public void openPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login and go to Logging page");
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();
		
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
	
	@Test(description = "LOG-3", groups = {"multiTenancy", "singleTenancy"})
	public void verifyPackageName() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login and go to Logging page");
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();
		
		log.info("Checking page components");
		soft.assertTrue(page.getPackageClassInputField().isVisible() , "search input is visible");
		
		soft.assertTrue(page.getPackageClassInputField().getText().equals("eu.domibus"),"default package name is correct");
		soft.assertAll();
	}

	/*  Verify Checkbox feature to show classes */
	@Test(description = "LOG-4", groups = {"multiTenancy", "singleTenancy"})
	public void verifyCheckbox() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login into application with admin user");
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();
		
		LoggingGrid grid = page.loggingGrid();
		log.info("Total number of Packages shown on logging page : " + grid.getPagination().getTotalItems());
		int prevCount = grid.getPagination().getTotalItems();
		soft.assertTrue(!page.getShowClassesCheckbox().isChecked(), "Checkbox is not checked");

		log.info("Click and check show classes check box");
		page.getShowClassesCheckbox().click();

		soft.assertTrue(page.getShowClassesCheckbox().isChecked(), " Checkbox is checked now");

		log.info("Click on search button");
		page.getSearchButton().click();

		log.info("wait for rows to load");
		page.grid().waitForRowsToLoad();
		log.info("Total no of package and classes shown after checking show classes checkbox " + grid.getPagination().getTotalItems());

		int newCount = grid.getPagination().getTotalItems();
		
		soft.assertTrue(prevCount < newCount, " Current count count is greater than previous one");
		soft.assertAll();
	}


	/* This method will verify reset button functionality  */
	@Test(description = "LOG-5", groups = {"multiTenancy", "singleTenancy"})
	public void verifyReset() throws Exception {
		SoftAssert soft = new SoftAssert();
		LoggingPage page = new LoggingPage(driver);

		log.info("Login into application with admin user");
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.loggingGrid().waitForRowsToLoad();
		
		String loggerLevel = page.loggingGrid().getRowInfo(0).get("Logger Level");
		log.info("current logger level for row 0 : " + loggerLevel);
		
		log.info("Validate Current logger level");
		if (!StringUtils.equalsAnyIgnoreCase(loggerLevel,"TRACE")) {
			log.info("Change logger level to TRACE");
			page.loggingGrid().setLoggLevel(0, "TRACE");
			soft.assertEquals(page.loggingGrid().getRowInfo(0).get("Logger Level"), "TRACE", "Updated logger level for row 0 is correct ");
			log.info("Click on reset button");
			page.getResetButton().click();
			log.info("Wait for grid row to upload");
			page.grid().waitForRowsToLoad();

			String resetLoggerLevel = page.loggingGrid().getRowInfo(0).get("Logger Level");
			log.info("Logger level for row 0 after reset" + resetLoggerLevel);
			soft.assertEquals(resetLoggerLevel, loggerLevel, "Updated logger level for row 0 is correct ");
		}

		soft.assertAll();
	}

	/* This method will verify search data for package and class name field*/
	@Test(description = "LOG-6", groups = {"multiTenancy", "singleTenancy"})
	public void searchData() throws Exception {
		String searchTerm = "cxf";
		SoftAssert soft = new SoftAssert();
		LoggingPage page = new LoggingPage(driver);

		log.info("Login into application with admin user");
		page.getSidebar().goToPage(PAGES.LOGGING);
		
		log.info("Enter cxf in search filter");
		page.getPackageClassInputField().fill(searchTerm);

		log.info("Click on search button");
		page.getSearchButton().click();
		page.grid().waitForRowsToLoad();
		
		List<String> names = page.loggingGrid().getListedValuesOnColumn("Logger Name");
		log.info("Verify cxf presence in  package and class name  shown as a search result");
		
		for (String name : names) {
			soft.assertTrue(name.contains(searchTerm), String.format("Logger Name %s contains search string %s", name, searchTerm));
		}
		
		soft.assertAll();
	}

	/* LOG-7 - User changes number of visible rows */
	@Test(description = "LOG-7", groups = {"multiTenancy", "singleTenancy"})
	public void changeNumberOfVisibleRows() throws Exception {
		SoftAssert soft = new SoftAssert();
		LoggingPage page = new LoggingPage(driver);

		log.info("Login into application with admin user");
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();

		log.info("Extract total number of items available");
		page.grid().getPagination().getTotalItems();

		log.info("Check number of expected pages ");
		int totalPagesBeforeUpdate = page.grid().getPagination().getExpectedNoOfPages();

		log.info("Check presence of last page by navigation");
		page.grid().getPagination().goToPage(totalPagesBeforeUpdate);
		page.grid().waitForRowsToLoad();

		log.info("Change page selector size value to 25");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("25");
		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.grid().getRowsNo() == 25, "Number of rows is  equal to 25");

		log.info("Calculate no of expected pages ");
		int totalPagesAfterUpdate = page.grid().getPagination().getExpectedNoOfPages();

		log.info("Verify last page presence by navigation ");
		page.grid().getPagination().goToPage(totalPagesAfterUpdate);

		soft.assertAll();
	}
	
	
	
}
