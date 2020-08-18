package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.logging.LoggingGrid;
import pages.logging.LoggingPage;

public class LoggingPgTest extends SeleniumTest {
	
	/*This method will verify page presence for different domibus user roles  */
	@Test(description = "LOG-1", groups = {"multiTenancy", "singleTenancy"})
	public void openPage() throws Exception {
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


//	/* This method will verify reset button functionality  */
//	@Test(description = "LOG-5", groups = {"multiTenancy", "singleTenancy"})
//	public void verifyReset() throws Exception {
//		SoftAssert soft = new SoftAssert();
//		LoggingPage lPage = new LoggingPage(driver);
//
//		log.info("Login into application with admin user");
//		login(data.getAdminUser()).getSidebar().goToPage(PAGES.LOGGING);
//
//		String level0 = lPage.getloggerLevelvalue(0);
//		log.info("current logger level for row 0 : " + level0);
//		log.info("Validate Current logger level");
//
//		if (!StringUtils.equalsAnyIgnoreCase(level0,"TRACE")) {
//			log.info("Change logger level to TRACE");
//			lPage.setLoggerLevel("TRACE");
//			soft.assertTrue(lPage.getloggerLevelvalue(0).contains("TRACE"), "Updated logger level for row 0 is correct ");
//			log.info("Click on reset button");
//			lPage.getResetButton().click();
//			log.info("Wait for grid row to upload");
//			lPage.grid().waitForRowsToLoad();
//
//			log.info("Logger level for row 0 after reset" + lPage.getloggerLevelvalue(0));
//			soft.assertTrue(lPage.getloggerLevelvalue(0).contains(level0), "Updated logger level for row 0 is correct ");
//		}
//
//		soft.assertAll();
//	}

//	/* This method will verify search data for package and class name field*/
//	@Test(description = "LOG-6", groups = {"multiTenancy", "singleTenancy"})
//	public void searchData() throws Exception {
//		SoftAssert soft = new SoftAssert();
//		LoggingPage lPage = new LoggingPage(driver);
//
//		log.info("Login into application with admin user");
//		login(data.getAdminUser()).getSidebar().goToPage(PAGES.LOGGING);
//
//		log.info("Clear data from package or class name field");
//		lPage.getPackageClassInputField().clear();
//
//		log.info("Enter cxf in search filter");
//		lPage.getPackageClassInputField().fill("cxf");
//
//		log.info("Click on search button");
//		lPage.getSearchButton().click();
//		lPage.grid().waitForRowsToLoad();
//		log.info("Verify cxf presence in  package and class name  shown as a search result");
//		soft.assertTrue(lPage.getLoggerName("cxf", 0), "Logger Name for row 0 contains search string ");
//
//		soft.assertAll();
//	}
//
//	/*This method will verify changes on  page selector value modification */
//	@Test(description = "LOG-7", groups = {"multiTenancy", "singleTenancy"})
//	public void changePageSelector() throws Exception {
//		SoftAssert soft = new SoftAssert();
//		LoggingPage lPage = new LoggingPage(driver);
//
//		log.info("Login into application with admin user");
//		login(data.getAdminUser()).getSidebar().goToPage(PAGES.LOGGING);
//
//		log.info("Extract total number of items available");
//		lPage.grid().getPagination().getTotalItems();
//
//		log.info("Check number of expected pages ");
//		int totalPagesBeforeUpdate = lPage.grid().getPagination().getExpectedNoOfPages();
//
//		log.info("Check presence of last page by navigation");
//		lPage.grid().getPagination().goToPage(totalPagesBeforeUpdate);
//
//		log.info("Change page selector size value to 25");
//		lPage.grid().getPagination().getPageSizeSelect().selectOptionByText("25");
//
//		lPage.grid().waitForRowsToLoad();
//		soft.assertTrue(lPage.grid().getRowsNo() == 25, "Number of rows is  equal to 25");
//
//		log.info("Calculate no of expected pages ");
//		int totalPagesAfterUpdate = lPage.grid().getPagination().getExpectedNoOfPages();
//
//		log.info("Verify last page presence by navigation ");
//		lPage.grid().getPagination().goToPage(totalPagesAfterUpdate);
//
//		soft.assertAll();
//
//
//	}
	
	
	
}
