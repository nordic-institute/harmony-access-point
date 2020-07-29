package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.errorLog.ErrorLogPage;
import pages.errorLog.ErrorModal;
import rest.RestServicePaths;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class ErrorLogPgUXTest extends SeleniumTest {
	
	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.ERROR_LOG);
	
	/* Login as super admin and open Error log page */
	@Test(description = "ERR-1", groups = {"multiTenancy", "singleTenancy"})
	public void openErrorLogPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		log.info("checking page default state");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");
		basicFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));
		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));
		
		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}
		
		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");
		
		soft.assertAll();
	}
	
	/* Double click on one error */
	@Test(description = "ERR-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickErr() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		page.grid().waitForRowsToLoad();
		
		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}
		
		log.info("getting listed info for row 0");
		page.grid().getGridCtrl().showAllColumns();
		HashMap<String, String> info = page.grid().getRowInfo(0);
		
		log.info("double click row 0");
		page.grid().doubleClickRow(0);
		
		if (!page.hasOpenDialog()) {
			log.warn("Error details dialog might not be opened");
		}
		
		log.info("get info from modal");
		HashMap<String, String> modalInfo = new ErrorModal(driver).getListedInfo();
		page.clickVoidSpace();
		
		log.info("comparing data");
		soft.assertTrue(ListUtils.isEqualList(info.keySet(), modalInfo.keySet()), "Same fields are listed in grid and modal");
		soft.assertTrue(ListUtils.isEqualList(info.values(), modalInfo.values()), "Same values are listed in grid and modal");
		
		soft.assertAll();
	}
	
	/* Filter errors using basic filters */
	@Test(description = "ERR-3", groups = {"multiTenancy", "singleTenancy"})
	public void filterErrorLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		if (grid.getRowsNo() < 3) {
			throw new SkipException("Not enough rows to test filtering");
		}
		
		grid.getGridCtrl().showAllColumns();
		HashMap<String, String> row = grid.getRowInfo(0);
		
		log.info("filtering by data listed in first row of grid: " + row);
		page.filters().basicSearch(row.get("Signal Message Id"), row.get("Message Id"), row.get("Timestamp"), null);
		grid.waitForRowsToLoad();
		
		log.info("Checking that all listed rows have the correct message id and signal message id");
		for (int i = 0; i < grid.getRowsNo(); i++) {
			HashMap<String, String> row2 = page.grid().getRowInfo(i);
			soft.assertTrue(StringUtils.equalsIgnoreCase(row.get("Message Id"), row2.get("Message Id")), "Errors for the desired message id are shown");
			soft.assertTrue(StringUtils.equalsIgnoreCase(row.get("Signal Message Id"), row2.get("Signal Message Id")), "Errors for the desired signal message id are shown");
		}
		
		soft.assertAll();
	}
	
	
	/* Open advanced filters */
	@Test(description = "ERR-4", groups = {"multiTenancy", "singleTenancy"})
	public void openAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		page.grid().waitForRowsToLoad();
		
		log.info("checking available filters in expanded state");
		page.filters().expandArea();
		advancedFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));
		
		soft.assertAll();
	}
	
	/* Filter errors using advanced filters */
	@Test(description = "ERR-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterUsingAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		page.grid().waitForRowsToLoad();
		
		DGrid grid = page.grid();
		if (grid.getRowsNo() < 3) {
			throw new SkipException("Not enough rows to test filtering");
		}
		
		grid.getGridCtrl().showAllColumns();
		HashMap<String, String> row = grid.getRowInfo(0);
		
		log.info("filtering by data listed in first row of grid: " + row);
		
		page.filters().advancedSearch(row.get("Signal Message Id"), row.get("Message Id"), row.get("Timestamp"), null, null, row.get("AP Role"), row.get("Error Code"), null, null);
		grid.waitForRowsToLoad();
		
		log.info("Checking that all listed rows have the correct info");
		for (int i = 0; i < grid.getRowsNo(); i++) {
			HashMap<String, String> row2 = page.grid().getRowInfo(i);
			soft.assertTrue(StringUtils.equalsIgnoreCase(row.get("Message Id"), row2.get("Message Id")), "Errors for the desired message id are shown");
			soft.assertTrue(StringUtils.equalsIgnoreCase(row.get("Signal Message Id"), row2.get("Signal Message Id")), "Errors for the desired signal message id are shown");
			soft.assertTrue(StringUtils.equalsIgnoreCase(row.get("Error Detail"), row2.get("Error Detail")), "Error Detail is as filtered");
			soft.assertTrue(StringUtils.equalsIgnoreCase(row.get("AP Role"), row2.get("AP Role")), "AP Role is as filtered");
			soft.assertTrue(StringUtils.equalsIgnoreCase(row.get("Error Code"), row2.get("Error Code")), "Error Code is as filtered");
		}
		
		soft.assertAll();
	}
	
	/* Filter errors so that there are no results */
	@Test(description = "ERR-6", groups = {"multiTenancy", "singleTenancy"})
	public void filterToEmptyGrid() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		DGrid grid = page.grid();
		
		log.info("filtering by invalid data");
		page.filters().basicSearch("invalidSignalMessageID", "invalidMessageID", "invalidDate", null);
		grid.waitForRowsToLoad();
		
		log.info("checking that there are no rows shown after invalid search");
		soft.assertTrue(page.grid().getRowsNo() == 0, "No rows are shown");
		
		soft.assertAll();
	}
	
	/* Filter to empty grid and delete all criteria and press Search */
	@Test(description = "ERR-7", groups = {"multiTenancy", "singleTenancy"})
	public void filterToEmptyGridAndReset() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		DGrid grid = page.grid();
		
		int gridRows = grid.getRowsNo();
		int allRows = grid.getPagination().getTotalItems();
		
		log.info("filtering by invalid data");
		page.filters().basicSearch("invalidSignalMessageID", "invalidMessageID", "invalidDate", null);
		grid.waitForRowsToLoad();
		
		log.info("checking that there are no rows shown after invalid search");
		soft.assertTrue(page.grid().getRowsNo() == 0, "No rows are shown");

//		filtering by empty strings doesn't trigger the onchange event
//		until I can fix this we will refresh the page
		log.info("reset search");
		page.refreshPage();
		grid.waitForRowsToLoad();
		
		
		log.info("check that the same number of rows is shown");
		soft.assertTrue(grid.getRowsNo() == gridRows, "Grid shows the same number of rows as before");
		soft.assertTrue(grid.getPagination().getTotalItems() == allRows, "Pagination shows the same number of rows as before");
		
		soft.assertAll();
	}
	
	/* Download list of errors */
	@Test(description = "ERR-9", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		HashMap<String, String> params = new HashMap<>();
		params.put("orderBy", "timestamp");
		params.put("asc", "false");
		
		String fileName = rest.csv().downloadGrid(RestServicePaths.ERROR_LOG_CSV, params, null);
		log.info("downloaded errors to file " + fileName);
		page.grid().relaxCheckCSVvsGridInfo(fileName, soft, "datetime"); //checkCSVvsGridInfo(fileName, soft);
		
		soft.assertAll();
	}
	
	/* Click Show columns link */
	@Test(description = "ERR-10", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();
		
		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));
		
		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");
		
		soft.assertAll();
	}
	
	/* Check/Uncheck of fields on Show links */
	@Test(description = "ERR-11", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		grid.checkModifyVisibleColumns(soft);
		
		
		soft.assertAll();
	}
	
	/* Click Hide link without any new selection */
	@Test(description = "ERR-12", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");
		
		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");
		
		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");
		
		List<String> columnsPost = grid.getColumnNames();
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		
		soft.assertAll();
	}
	
	/* Click Hide link after selecting some new fields */
	@Test(description = "ERR-13", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWNewSelection() throws Exception {
		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		log.info("getting list of columns: " + columnsPre);
		
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");
		
		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");
		
		log.info("enable column with name " + colName);
		grid.getGridCtrl().checkBoxWithLabel(colName);
		
		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");
		
		List<String> columnsPost = grid.getColumnNames();
		log.info("getting list of columns " + columnsPost);
		
		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() + 1 == columnsPost.size(), "One more column is shown");
		soft.assertTrue(columnsPost.contains(colName), "Correct column is now in the list of columns");
		
		soft.assertAll();
	}
	
	/* Click All/None link */
	@Test(description = "ERR-14", groups = {"multiTenancy", "singleTenancy"})
	public void checkAllNoneLnk() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		DGrid grid = page.grid();
		
		grid.checkAllLink(soft);
		grid.checkNoneLink(soft);
		
		soft.assertAll();
	}
	
	/* Change Rows field data */
	@Test(description = "ERR-15", groups = {"multiTenancy", "singleTenancy"})
	public void checkChangeNumberOfRows() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		DGrid grid = page.grid();
		
		grid.checkChangeNumberOfRows(soft);
		
		soft.assertAll();
	}
	
	/* Check sorting on the basis of Headers of Grid  */
	@Test(description = "ERR-22", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");
		
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		DGrid grid = page.grid();
		grid.getPagination().getPageSizeSelect().selectOptionByText("100");
		
		for (int i = 0; i < colDescs.length(); i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}
		
		soft.assertAll();
	}
	
	/* Verify headers in downloaded CSV sheet  */
	@Test(description = "ERR-23", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		
		HashMap<String, String> params = new HashMap<>();
		params.put("orderBy", "timestamp");
		params.put("asc", "false");
		
		String fileName = rest.csv().downloadGrid(RestServicePaths.ERROR_LOG_CSV, params, null);
		log.info("downloaded file with name " + fileName);
		
		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();
		
		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");
		
		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);
		soft.assertAll();
	}
	
}

