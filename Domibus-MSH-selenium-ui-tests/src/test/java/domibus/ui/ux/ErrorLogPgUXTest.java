package domibus.ui.ux;

import io.qameta.allure.*;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dobjects.DatePicker;
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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Catalin Comanici
 * @version 4.1
 */


@Epic("Error Log")
@Feature("UX")
public class ErrorLogPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.ERROR_LOG);

	/* Login as super admin and open Error log page */
	/*  ERR-1 - Login as super admin and open Error log page  */
	@Description("ERR-1 - Login as super admin and open Error log page")
	@Link(name = "EDELIVERY-5105", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5105")
	@AllureId("ERR-1")
	@Test(description = "ERR-1", groups = {"multiTenancy", "singleTenancy"})
	public void openErrorLogPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		Allure.step("checking page default state");
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
	/*  ERR-2 - Doubleclik on one error  */
	@Description("ERR-2 - Doubleclik on one error")
	@Link(name = "EDELIVERY-5106", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5106")
	@AllureId("ERR-2")
	@Test(description = "ERR-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickErr() throws Exception {
		SoftAssert soft = new SoftAssert();

		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		page.grid().waitForRowsToLoad();

		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

		Allure.step("getting listed info for row 0");
		log.info("getting listed info for row 0");
		page.grid().getGridCtrl().showAllColumns();
		HashMap<String, String> info = page.grid().getRowInfo(0);

		Allure.step("double click row 0");
		log.info("double click row 0");
		page.grid().doubleClickRow(0);

		if (!page.hasOpenDialog()) {
			Allure.step("Error details dialog might not be opened");
			log.warn("Error details dialog might not be opened");
		}

		Allure.step("get info from modal");
		log.info("get info from modal");
		HashMap<String, String> modalInfo = new ErrorModal(driver).getListedInfo();
		page.clickVoidSpace();

		Allure.step("comparing data");
		log.info("comparing data");
		soft.assertTrue(ListUtils.isEqualList(info.keySet(), modalInfo.keySet()), "Same fields are listed in grid and modal");
		soft.assertTrue(ListUtils.isEqualList(info.values(), modalInfo.values()), "Same values are listed in grid and modal");

		soft.assertAll();
	}

	/* Filter errors using basic filters */
	/*  ERR-3 - Filter errors using basic filters  */
	@Description("ERR-3 - Filter errors using basic filters")
	@Link(name = "EDELIVERY-5107", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5107")
	@AllureId("ERR-3")
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

		Allure.step("filtering by data listed in first row of grid: " + row);
		log.info("filtering by data listed in first row of grid: " + row);
		page.filters().basicSearch(row.get("Signal Message Id"), row.get("Message Id"), row.get("Timestamp"), null);
		grid.waitForRowsToLoad();

		Allure.step("Checking that all listed rows have the correct message id and signal message id");
		log.info("Checking that all listed rows have the correct message id and signal message id");
		for (int i = 0; i < grid.getRowsNo(); i++) {
			HashMap<String, String> row2 = page.grid().getRowInfo(i);
			soft.assertTrue(StringUtils.equalsIgnoreCase(row.get("Message Id"), row2.get("Message Id")), "Errors for the desired message id are shown");
			soft.assertTrue(StringUtils.equalsIgnoreCase(row.get("Signal Message Id"), row2.get("Signal Message Id")), "Errors for the desired signal message id are shown");
		}

		soft.assertAll();
	}


	/* Open advanced filters */
	/*  ERR-4 - Open advanced filters  */
	@Description("ERR-4 - Open advanced filters")
	@Link(name = "EDELIVERY-5108", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5108")
	@AllureId("ERR-4")
	@Test(description = "ERR-4", groups = {"multiTenancy", "singleTenancy"})
	public void openAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		page.grid().waitForRowsToLoad();

		Allure.step("checking available filters in expanded state");
		log.info("checking available filters in expanded state");
		page.filters().expandArea();
		advancedFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));

		soft.assertAll();
	}

	/* Filter errors using advanced filters */
	/*  ERR-5 - Filter errors using advanced filters  */
	@Description("ERR-5 - Filter errors using advanced filters")
	@Link(name = "EDELIVERY-5109", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5109")
	@AllureId("ERR-5")
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

		Allure.step("filtering by data listed in first row of grid: " + row);
		log.info("filtering by data listed in first row of grid: " + row);

		page.filters().advancedSearch(row.get("Signal Message Id"), row.get("Message Id"), row.get("Timestamp"), null, null, row.get("AP Role"), row.get("Error Code"), null, null);
		grid.waitForRowsToLoad();

		Allure.step("Checking that all listed rows have the correct info");
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
	/*  ERR-6 - Filter errors so that there are no results  */
	@Description("ERR-6 - Filter errors so that there are no results")
	@Link(name = "EDELIVERY-5110", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5110")
	@AllureId("ERR-6")
	@Test(description = "ERR-6", groups = {"multiTenancy", "singleTenancy"})
	public void filterToEmptyGrid() throws Exception {
		SoftAssert soft = new SoftAssert();

		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		DGrid grid = page.grid();

		Allure.step("filtering by invalid data");
		log.info("filtering by invalid data");
		page.filters().basicSearch("invalidSignalMessageID", "invalidMessageID", "invalidDate", null);
		grid.waitForRowsToLoad();

		Allure.step("checking that there are no rows shown after invalid search");
		log.info("checking that there are no rows shown after invalid search");
		soft.assertTrue(page.grid().getRowsNo() == 0, "No rows are shown");

		soft.assertAll();
	}

	/* Filter to empty grid and delete all criteria and press Search */
	/*  ERR-7 - Filter to empty grid and delete all criteria and press Search  */
	@Description("ERR-7 - Filter to empty grid and delete all criteria and press Search")
	@Link(name = "EDELIVERY-5111", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5111")
	@AllureId("ERR-7")
	@Test(description = "ERR-7", groups = {"multiTenancy", "singleTenancy"})
	public void filterToEmptyGridAndReset() throws Exception {
		SoftAssert soft = new SoftAssert();

		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		DGrid grid = page.grid();

		grid.waitForRowsToLoad();
		int gridRows = grid.getRowsNo();
		int allRows = grid.getPagination().getTotalItems();

		Allure.step("filtering by invalid data");
		log.info("filtering by invalid data");
		page.filters().basicSearch("invalidSignalMessageID", "invalidMessageID", "invalidDate", null);
		grid.waitForRowsToLoad();

		Allure.step("checking that there are no rows shown after invalid search");
		log.info("checking that there are no rows shown after invalid search");
		soft.assertTrue(page.grid().getRowsNo() == 0, "No rows are shown");

//		filtering by empty strings doesn't trigger the onchange event
//		until I can fix this we will refresh the page
		Allure.step("reset search");
		log.info("reset search");
		page.refreshPage();
		grid.waitForRowsToLoad();


		Allure.step("check that the same number of rows is shown");
		log.info("check that the same number of rows is shown");
		soft.assertTrue(grid.getRowsNo() == gridRows, "Grid shows the same number of rows as before");
		soft.assertTrue(grid.getPagination().getTotalItems() >= allRows, "Pagination shows at least the same number of rows as before");

		soft.assertAll();
	}

	/* Download list of errors */
	/*  ERR-9 - Download list of errors  */
	@Description("ERR-9 - Download list of errors")
	@Link(name = "EDELIVERY-5113", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5113")
	@AllureId("ERR-9")
	@Test(description = "ERR-9", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		HashMap<String, String> params = new HashMap<>();
		params.put("orderBy", "timestamp");
		params.put("asc", "false");

		String fileName = rest.csv().downloadGrid(RestServicePaths.ERROR_LOG_CSV, params, null);
		Allure.step("downloaded errors to file " + fileName);
		log.info("downloaded errors to file " + fileName);
		page.grid().relaxCheckCSVvsGridInfo(fileName, soft, "datetime"); //checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	/* Click Show columns link */
	/*  ERR-10 - Click Show columns link  */
	@Description("ERR-10 - Click Show columns link")
	@Link(name = "EDELIVERY-5114", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5114")
	@AllureId("ERR-10")
	@Test(description = "ERR-10", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		Allure.step("Checking visibility of All/None links");
		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}

	/* Check/Uncheck of fields on Show links */
	/*  ERR-11 - CheckUncheck of fields on Show links  */
	@Description("ERR-11 - CheckUncheck of fields on Show links")
	@Link(name = "EDELIVERY-5115", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5115")
	@AllureId("ERR-11")
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
	/*  ERR-12 - Click Hide link without any new selection  */
	@Description("ERR-12 - Click Hide link without any new selection")
	@Link(name = "EDELIVERY-5116", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5116")
	@AllureId("ERR-12")
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
	/*  ERR-13 - Click Hide link after selecting some new fields  */
	@Description("ERR-13 - Click Hide link after selecting some new fields")
	@Link(name = "EDELIVERY-5117", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5117")
	@AllureId("ERR-13")
	@Test(description = "ERR-13", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWNewSelection() throws Exception {
		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Allure.step("getting list of columns: " + columnsPre);
		log.info("getting list of columns: " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		Allure.step("enable column with name " + colName);
		log.info("enable column with name " + colName);
		grid.getGridCtrl().checkBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		Allure.step("getting list of columns " + columnsPost);
		log.info("getting list of columns " + columnsPost);

		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() + 1 == columnsPost.size(), "One more column is shown");
		soft.assertTrue(columnsPost.contains(colName), "Correct column is now in the list of columns");

		soft.assertAll();
	}

	/* Click All/None link */
	/*  ERR-14 - Click All None link  */
	@Description("ERR-14 - Click All None link")
	@Link(name = "EDELIVERY-5118", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5118")
	@AllureId("ERR-14")
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
	/*  ERR-15 - Change Rows field data  */
	@Description("ERR-15 - Change Rows field data")
	@Link(name = "EDELIVERY-5119", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5119")
	@AllureId("ERR-15")
	@Test(description = "ERR-15", groups = {"multiTenancy", "singleTenancy"})
	public void checkChangeNumberOfRows() throws Exception {

		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		DGrid grid = page.grid();

		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/* ERR-16 - Download list as CSV */
	/*  ERR-16 - Download list as CSV  */
	@Description("ERR-16 - Download list as CSV")
	@Link(name = "EDELIVERY-5120", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5120")
	@AllureId("ERR-16")
	@Test(description = "ERR-16", groups = {"multiTenancy", "singleTenancy"})
	public void downloadErrCSV() throws Exception {

		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		Allure.step("Click on download csv button");
		log.info("Click on download csv button");
		String csvFile = page.pressSaveCsvAndSaveFile();

		Allure.step("Click on show link");
		log.info("Click on show link");
		page.grid().getGridCtrl().showCtrls();

		Allure.step("Click on All link to show all available column headers");
		log.info("Click on All link to show all available column headers");
		page.grid().getGridCtrl().showAllColumns();

		page.grid().checkCSVvsGridHeaders(csvFile, soft);
		int maxMess = page.grid().getRowsNo();

		page.grid().relaxCheckCSVvsGridInfo(csvFile, soft, "datetime"); //checkCSVvsGridInfo(completeFilePath, soft);
		soft.assertAll();
	}

	/* Check sorting on the basis of Headers of Grid  */
	/*  ERR-22 - Check sorting on the basis of Headers of Grid   */
	@Description("ERR-22 - Check sorting on the basis of Headers of Grid ")
	@Link(name = "EDELIVERY-5126", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5126")
	@AllureId("ERR-22")
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
	/*  ERR-23 - Verify headers in downloaded CSV sheet   */
	@Description("ERR-23 - Verify headers in downloaded CSV sheet ")
	@Link(name = "EDELIVERY-5127", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5127")
	@AllureId("ERR-23")
	@Test(description = "ERR-23", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {

		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		HashMap<String, String> params = new HashMap<>();
		params.put("orderBy", "timestamp");
		params.put("asc", "false");

		String fileName = rest.csv().downloadGrid(RestServicePaths.ERROR_LOG_CSV, params, null);
		Allure.step("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Allure.step("set page size to 100");
		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		Allure.step("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);
		soft.assertAll();
	}


	/*  ERR-23 - Verify headers in downloaded CSV sheet   */
	@Description("ERR-23 - Verify headers in downloaded CSV sheet ")
	@Link(name = "EDELIVERY-5127", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5127")
	@AllureId("ERR-23")
	@Test(description = "ERR-23", groups = {"multiTenancy", "singleTenancy"})
	public void maxMinCalendar() throws Exception {
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm").parse("2019-05-05 03:03");
		page.filters().getErrFrom().selectDate(date);
	}


	/* ERR-24 - Perform sorting on last page when multiple pages are available */
	/*  ERR-24 - Perform sorting on last page when multiple pages are available   */
	@Description("ERR-24 - Perform sorting on last page when multiple pages are available ")
	@Link(name = "EDELIVERY-6355", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-6355")
	@AllureId("ERR-24")
	@Test(description = "ERR-24", groups = {"multiTenancy", "singleTenancy"})
	public void checkSortingOnLastPage() throws Exception {

		SoftAssert soft = new SoftAssert();
		Allure.step("Navigate to Error log page");
		log.info("Navigate to Error log page");
		ErrorLogPage page = new ErrorLogPage(driver);
		page.getSidebar().goToPage(PAGES.ERROR_LOG);

		DGrid grid = page.grid();
		Pagination pg = grid.getPagination();

		Allure.step("Checking if we have enough pages to perform test");
		log.info("Checking if we have enough pages to perform test");
		if (pg.getExpectedNoOfPages() <= 1) {
			throw new SkipException("Not enough pages for test");
		}

		Allure.step("Going to last page");
		log.info("Going to last page");
		pg.skipToLastPage();

		grid.waitForRowsToLoad();

		Allure.step("Sorting by Error Code");
		log.info("Sorting by Error Code");
		grid.sortBy("Error Code");
		grid.waitForRowsToLoad();


		Allure.step("Checking if current page is first page");
		log.info("Checking if current page is first page");
		soft.assertEquals(pg.getActivePage(), Integer.valueOf(1), "Current page is reset to 1 after sorting");


		soft.assertAll();
	}

	/*ERR-17 - Verify max date Error To field & Notified To field */
	/*  ERR-17 - Verify max date Error To field  Notified To field  */
	@Description("ERR-17 - Verify max date Error To field  Notified To field")
	@Link(name = "EDELIVERY-5121", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5121")
	@AllureId("ERR-17")
	@Test(description = "ERR-17", groups = {"multiTenancy", "singleTenancy"})
	public void receivedToMaxValue() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("logged in");
		log.info("logged in");
		ErrorLogPage page = new ErrorLogPage(driver);
		Calendar cal = Calendar.getInstance();

		Allure.step("Current date is :" + cal.get(Calendar.DAY_OF_MONTH));
		log.info("Current date is :" + cal.get(Calendar.DAY_OF_MONTH));
		Allure.step("Current Hour is :" + cal.get(Calendar.HOUR_OF_DAY));
		log.info("Current Hour is :" + cal.get(Calendar.HOUR_OF_DAY));
		Allure.step("Current minute is :" + cal.get(Calendar.MINUTE));
		log.info("Current minute is :" + cal.get(Calendar.MINUTE));

		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		page.errorToClock.click();
		DatePicker errorToDatePicker = new DatePicker(driver, page.errorTo);
		soft.assertTrue(errorToDatePicker.verifyMaxClockValue(soft, cal) > 0, "field is accepting correct value , smaller than System's current date & time");

		page.filters().expandArea();
		page.notifiedToClock.click();

		DatePicker notifiedToDatePicker = new DatePicker(driver, page.notifiedTo);
		soft.assertTrue(notifiedToDatePicker.verifyMaxClockValue(soft, cal) > 0, "field is accepting correct value, smaller than current System's date & time");

		soft.assertAll();
	}


}

