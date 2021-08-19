package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Link;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.logging.LoggingPage;

public class LoggingPgUXTest extends SeleniumTest {





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
		soft.assertTrue(page.getSearchInputField().isVisible(), "search input is visible");
		soft.assertTrue(page.getSearchButton().isVisible(), "search button is visible");
		soft.assertTrue(page.getResetButton().isVisible(), "reset button is visible");
		soft.assertTrue(page.getShowClassesCheckbox().isVisible(), "show classes checkbox is visible");
		soft.assertFalse(page.getShowClassesCheckbox().isChecked(), "show classes checkbox is not checked by default");
		soft.assertTrue(page.loggingGrid().isPresent(), "grid with loggers is shown");
		soft.assertTrue(page.loggingGrid().getRowsNo() > 0, "grid has loggers listed");

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
		soft.assertTrue(page.getSearchInputField().isVisible(), "search input is visible");

		soft.assertTrue(page.getSearchInputField().getText().equals("eu.domibus"), "default package name is correct");
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

	/* EDELIVERY-7180 - LOG-15 - Click/Doubleclick on a grid row */
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
