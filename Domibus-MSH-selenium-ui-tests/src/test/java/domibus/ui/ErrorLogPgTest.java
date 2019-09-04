package domibus.ui;

		import ddsl.dcomponents.grid.DGrid;
		import ddsl.dcomponents.grid.Pagination;
		import ddsl.enums.PAGES;
		import domibus.BaseUXTest;
		import org.testng.SkipException;
		import org.testng.annotations.Test;
		import org.testng.asserts.SoftAssert;
		import pages.errorLog.ErrorLogPage;
		import rest.RestServicePaths;

		import java.util.HashMap;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class ErrorLogPgTest extends BaseUXTest {


	@Test(description = "ERR-1", groups = {"multiTenancy", "singleTenancy"})
	public void openErrorLogPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.ERROR_LOG);

		ErrorLogPage errorLogPage = new ErrorLogPage(driver);

		soft.assertTrue(errorLogPage.isLoaded(), "Expected elements appear in the page");
		soft.assertAll();
	}


	@Test(description = "ERR-2", groups = {"multiTenancy", "singleTenancy"})
	public void filterErrorLog() throws Exception {
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.ERROR_LOG);

		ErrorLogPage errorLogPage = new ErrorLogPage(driver);

		soft.assertTrue(errorLogPage.isLoaded());

		DGrid grid = errorLogPage.grid();

		if (grid.getRowsNo() < 3) {
			throw new SkipException("Not enough rows to test filtering");
		}

		HashMap<String, String> row = grid.getRowInfo(0);

		errorLogPage.basicSearch(null, row.get("Message Id"), null, null);

		HashMap<String, String> row2 = errorLogPage.grid().getRowInfo(0);

		soft.assertTrue(row2.equals(row), "Errors for correct message id is displayed");

		soft.assertAll();
	}


	@Test(description = "ERR-3", groups = {"multiTenancy", "singleTenancy"})
	public void paginationTest() throws Exception {
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.ERROR_LOG);

		ErrorLogPage page = new ErrorLogPage(driver);

		soft.assertTrue(page.isLoaded());

		Pagination pgCtrl = page.grid().getPagination();

		int noOfErrors = pgCtrl.getTotalItems();
		if (noOfErrors < 11) {
			throw new SkipException("Cannot test pagination because with so little errors");
		}

		if (!pgCtrl.isPaginationPresent() && pgCtrl.getExpectedNoOfPages() > 1) {
			soft.fail("Pagination controls are not present although expected number of pages is bigger than 1");
		}

		soft.assertEquals(pgCtrl.getPageSizeSelect().getSelectedValue(), "10", "Default page size is 10");

		pgCtrl.skipToLastPage();
		soft.assertTrue(pgCtrl.getActivePage() == pgCtrl.getExpectedNoOfPages(), "Skipped to last page");

		soft.assertEquals(page.grid().getRowsNo(), pgCtrl.getNoOfItemsOnLastPg(), "Number of items on the last page");

		pgCtrl.skipToFirstPage();
		soft.assertTrue(pgCtrl.getActivePage() == 1, "Skipped to first page");

		pgCtrl.goToNextPage();
		soft.assertTrue(pgCtrl.getActivePage() == 2, "Page 2");

		pgCtrl.goToPrevPage();
		soft.assertTrue(pgCtrl.getActivePage() == 1, "Previous page is 1");

		pgCtrl.goToPage(2);
		soft.assertTrue(pgCtrl.getActivePage() == 2, "Next page is 2");

		page.grid().sortBy("Error Code");
		page.grid().waitForRowsToLoad();
		soft.assertTrue(pgCtrl.getActivePage() == 1, "After sorting the active page is 1");
		pgCtrl.goToPage(2);

		pgCtrl.getPageSizeSelect().selectOptionByText("25");
		pgCtrl.skipToLastPage();
		soft.assertTrue(pgCtrl.getActivePage() == pgCtrl.getExpectedNoOfPages());

		soft.assertAll();
	}

	@Test(description = "ERR-4", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.ERROR_LOG);
		ErrorLogPage page = new ErrorLogPage(driver);

		String fileName = rest.downloadGrid(RestServicePaths.ERROR_LOG_CSV, null, null);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");
		page.grid().sortBy("Timestamp");
		page.grid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	@Test(description = "ERR-5", groups = {"multiTenancy", "singleTenancy"})
	public void gridSelfAssert() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.ERROR_LOG);
		ErrorLogPage page = new ErrorLogPage(driver);

		page.grid().assertControls(soft);

		soft.assertAll();
	}



}
