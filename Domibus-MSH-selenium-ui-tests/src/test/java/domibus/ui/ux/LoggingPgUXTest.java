package domibus.ui.ux;

import org.testng.Reporter;
import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.logging.LoggingPage;

public class LoggingPgUXTest extends SeleniumTest {


	/* EDELIVERY-5354 - LOG-2 - Check Ui of Logging page  */
	@Test(description = "LOG-2", groups = {"multiTenancy", "singleTenancy"})
	public void openPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Login and go to Logging page");
		log.info("Login and go to Logging page");
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();

		Reporter.log("Checking page components");
		log.info("Checking page components");
		soft.assertTrue(page.getSearchInputField().isVisible(), "search input is visible");
		soft.assertTrue(page.getSearchButton().isVisible(), "search button is visible");
		soft.assertTrue(page.getResetButton().isVisible(), "reset button is visible");
		soft.assertTrue(page.getShowClassesCheckbox().isVisible(), "show classes checkbox is visible");
		soft.assertFalse(page.getShowClassesCheckbox().isChecked(), "show classes checkbox is not checked by default");
		soft.assertTrue(page.loggingGrid().isPresent(), "grid with loggers is shown");
		soft.assertTrue(page.loggingGrid().getRowsNo() > 0, "grid has loggers listed");

		soft.assertAll();
	}

	/* EDELIVERY-5355 - LOG-3 - Check default value in search filter  */
	@Test(description = "LOG-3", groups = {"multiTenancy", "singleTenancy"})
	public void verifyPackageName() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Login and go to Logging page");
		log.info("Login and go to Logging page");
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();

		Reporter.log("Checking page components");
		log.info("Checking page components");
		soft.assertTrue(page.getSearchInputField().isVisible(), "search input is visible");

		soft.assertTrue(page.getSearchInputField().getText().equals("eu.domibus"), "default package name is correct");
		soft.assertAll();
	}

	/* EDELIVERY-5359 - LOG-7 - User changes number of visible rows */
	@Test(description = "LOG-7", groups = {"multiTenancy", "singleTenancy"})
	public void changeNumberOfVisibleRows() throws Exception {
		SoftAssert soft = new SoftAssert();
		LoggingPage page = new LoggingPage(driver);

		Reporter.log("Login into application with admin user");
		log.info("Login into application with admin user");
		page.getSidebar().goToPage(PAGES.LOGGING);
		page.grid().waitForRowsToLoad();

		Reporter.log("Extract total number of items available");
		log.info("Extract total number of items available");
		page.grid().getPagination().getTotalItems();

		Reporter.log("Check number of expected pages ");
		log.info("Check number of expected pages ");
		int totalPagesBeforeUpdate = page.grid().getPagination().getExpectedNoOfPages();

		Reporter.log("Check presence of last page by navigation");
		log.info("Check presence of last page by navigation");
		page.grid().getPagination().goToPage(totalPagesBeforeUpdate);
		page.grid().waitForRowsToLoad();

		Reporter.log("Change page selector size value to 25");
		log.info("Change page selector size value to 25");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("25");
		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.grid().getRowsNo() == 25, "Number of rows is  equal to 25");

		Reporter.log("Calculate no of expected pages ");
		log.info("Calculate no of expected pages ");
		int totalPagesAfterUpdate = page.grid().getPagination().getExpectedNoOfPages();

		Reporter.log("Verify last page presence by navigation ");
		log.info("Verify last page presence by navigation ");
		page.grid().getPagination().goToPage(totalPagesAfterUpdate);

		soft.assertAll();
	}

	/* EDELIVERY-7180 - LOG-15 - ClickDoubleclick on a grid row */
	@Test(description = "LOG-15", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		LoggingPage page = new LoggingPage(driver);
		page.getSidebar().goToPage(PAGES.LOGGING);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		grid.doubleClickRow(0);

		soft.assertFalse(page.hasOpenDialog(), "Doubleclicking a row has no effect");

		soft.assertAll();
	}

	/* EDELIVERY-7181 - LOG-16 - Sort the grid */
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
