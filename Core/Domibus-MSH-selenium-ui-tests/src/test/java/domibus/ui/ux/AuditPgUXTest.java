package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONObject;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Audit.AuditPage;
import pages.tlsTrustStore.TlsTrustStorePage;
import rest.RestServicePaths;
import utils.DFileUtils;
import utils.TestRunData;
import utils.TestUtils;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AuditPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.AUDIT);


	/* EDELIVERY-5242 - AU-1 - Login as super admin and open Audit page */
	@Test(description = "AU-1", groups = {"multiTenancy", "singleTenancy"})
	public void openAuditPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		Reporter.log("checking page default state");
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

	/* EDELIVERY-5243 - AU-2 - DoubleclickSingle click on one event */
	@Test(description = "AU-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickAuditRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

		Reporter.log("double click row 0");
		log.info("double click row 0");
		page.grid().doubleClickRow(0);

		Reporter.log("checking the current selected row");
		log.info("checking the current selected row");
		soft.assertTrue(page.grid().getSelectedRowIndex() == -1, "Rows are not selectablde in Audit page");
		soft.assertTrue(!page.hasOpenDialog(), "No dialog is visible on the page");

		soft.assertAll();
	}

	/* EDELIVERY-5244 - AU-3 - Filter messages using basic filters */
	@Test(description = "AU-3", groups = {"multiTenancy", "singleTenancy"})
	public void basicFilter() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

		Reporter.log("getting info from row 0");
		log.info("getting info from row 0");
		HashMap<String, String> info = page.grid().getRowInfo(0);

		page.filters().simpleFilter(info.get("Table")
				, info.get("User")
				, info.get("Action")
				, null, null);
		page.grid().waitForRowsToLoad();


		List<HashMap<String, String>> allInfo = page.grid().getAllRowInfo();
		for (HashMap<String, String> row : allInfo) {
			soft.assertEquals(info.get("Table"), row.get("Table"), "Table column conforms to criteria");
			soft.assertEquals(info.get("User"), row.get("User"), "User column conforms to criteria");
			soft.assertEquals(info.get("Action"), row.get("Action"), "Action column conforms to criteria");
		}

		soft.assertAll();
	}

	/* EDELIVERY-5245 - AU-4 - Open advanced filters */
	@Test(description = "AU-4", groups = {"multiTenancy", "singleTenancy"})
	public void checkAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		Reporter.log("checking available filters in expanded state");
		log.info("checking available filters in expanded state");
		page.filters().expandArea();
		advancedFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));

		soft.assertAll();
	}

	/* EDELIVERY-5246 - AU-5 - Filter events using advanced filters */
	@Test(description = "AU-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterWAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		DGrid grid = page.grid();
		log.info("checking available filters in expanded state");
		page.filters().expandArea();

		HashMap<String, String> rowInfo = grid.getRowInfo(0);
		Date date = DateUtils.parseDate(rowInfo.get("Changed").replaceAll("UTC\\+\\d", ""), "dd-MM-yyyy hh:mm:ss");

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR, -1);
		Date fromDate = cal.getTime();
		cal.add(Calendar.HOUR, 2);
		Date toDate = cal.getTime();


		log.info("Filtering ...");
		page.filters().advancedFilter(
				rowInfo.get("Table")
				, null
				, rowInfo.get("Action")
				, fromDate
				, toDate);

		log.info("waiting for rows to load");
		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.grid().getRowsNo() >= 1, "At least the source event matches filters");

		ArrayList<HashMap<String, String>> info = page.grid().getListedRowInfo();

		for (HashMap<String, String> map : info) {
			soft.assertEquals(map.get("Table"), rowInfo.get("Table"), "Table - value corresponds to filter value");
			soft.assertEquals(map.get("Action"), rowInfo.get("Action") , "Action - value corresponds to filter value");

			Date gridRowDate = DateUtils.parseDate(map.get("Changed").replaceAll("UTC\\+\\d", ""), "dd-MM-yyyy hh:mm:ss");
			soft.assertTrue(gridRowDate.after(fromDate), "Row date is after event date");
			soft.assertTrue(gridRowDate.before(toDate), "Row date is before event date");
		}

		soft.assertAll();
	}

	/* EDELIVERY-5250 - AU-9 - Download list of events */
	@Test(description = "AU-9", groups = {"multiTenancy", "singleTenancy"})
	public void downloadCSV() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate Audit page");
		log.info("Validate Audit page");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "page is loaded successfully");

		Reporter.log("Download all grid record csv");
		log.info("Download all grid record csv");
		String fileName = rest.csv().downloadGrid(RestServicePaths.AUDIT_CSV, null, null);

		Reporter.log("downloaded audit logs to file :" + fileName);
		log.info("downloaded audit logs to file :" + fileName);
		Reporter.log("" + page.grid().getRowsNo());
		log.debug("" + page.grid().getRowsNo());

		Reporter.log("comparing any random row data from downloaded csv and grid");
		log.info("comparing any random row data from downloaded csv and grid");
		csvCheck(fileName, page.grid(), soft);


		soft.assertAll();
	}

	/* EDELIVERY-5280 - AU-38 - Verify headers in downloaded CSV file */
	@Test(description = "AU-38", groups = {"multiTenancy", "singleTenancy"})
	public void verifyCSVHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		String fileName = rest.csv().downloadGrid(RestServicePaths.AUDIT_CSV, null, null);
		Reporter.log("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Reporter.log("checking headers in grid against the file");
		log.info("checking headers in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	/* EDELIVERY-5279 - AU-37 - Check sorting */
	@Test(description = "AU-37", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		DGrid grid = page.grid();
		Reporter.log("Getting column names");
		log.info("Getting column names");
		List<String> columns = grid.getColumnNames();
		for (String column : columns) {
			Reporter.log("sorting by column " + column);
			log.info("sorting by column " + column);
			grid.sortBy(column);

			String sortCol = grid.getSortedColumnName();
			Reporter.log("checking sorting");
			log.info("checking sorting");
			soft.assertEquals(sortCol, null, "After sorting, grid still has no sorted column");
		}

		soft.assertAll();
	}

	/* EDELIVERY-5278 - AU-36 - Change number of visible rows */
	@Test(description = "AU-36", groups = {"multiTenancy", "singleTenancy"})
	public void changeNumberOfRows() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/* EDELIVERY-5277 - AU-35 - Click All None link */
	@Test(description = "AU-35", groups = {"multiTenancy", "singleTenancy"})
	public void clickAllNoneLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		DGrid grid = page.grid();

		grid.checkAllLink(soft);
		grid.checkNoneLink(soft);

		soft.assertAll();
	}

	/* EDELIVERY-5276 - AU-34 - Click Hide link after selecting some new fields */
	@Test(description = "AU-34", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		String colName = "Id";

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Reporter.log("getting list of columns: " + columnsPre);
		log.info("getting list of columns: " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		Reporter.log("disable column with name " + colName);
		log.info("disable column with name " + colName);
		grid.getGridCtrl().uncheckBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		Reporter.log("getting list of columns " + columnsPost);
		log.info("getting list of columns " + columnsPost);

		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() - 1 == columnsPost.size(), "One less column is shown");
		soft.assertTrue(!columnsPost.contains(colName), "Correct column is now in the list of columns");


		soft.assertAll();
	}

	/* EDELIVERY-5275 - AU-33 - Click Hide link without any new selection */
	@Test(description = "AU-33", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

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

	/* EDELIVERY-5274 - AU-32 - CheckUncheck of fields on Show links */
	@Test(description = "AU-32", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
//		grid.getGridCtrl().showCtrls();
//
//		List<String> columnList = new ArrayList<>(grid.getGridCtrl().getAllCheckboxStatuses().keySet());
		grid.checkModifyVisibleColumns(soft);

		soft.assertAll();
	}

	/* EDELIVERY-5273 - AU-31 - Click Show columns link */
	@Test(description = "AU-31", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		Reporter.log("Checking visibility of All/None links");
		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}


	/* EDELIVERY-6361 - AU-41 - Verify downloaded CSV file of events */
	@Test(description = "AU-41", groups = {"multiTenancy", "singleTenancy"})
	public void verifyActionData() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		Reporter.log("Customized location for download");
		log.info("Customized location for download");
		String filePath = page.pressSaveCsvAndSaveFile();

		soft.assertTrue(new File(filePath).exists(), "File is downloaded successfully");
		String completeFilePath = filePath;

		page.grid().relaxCheckCSVvsGridInfo(completeFilePath, soft, "datetime");
		soft.assertAll();
	}

	private void csvCheck(String filename, DGrid grid, SoftAssert soft) throws Exception {
		Reader reader = Files.newBufferedReader(Paths.get(filename));
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase()
				.withTrim());
		List<CSVRecord> records = csvParser.getRecords();

		Reporter.log("Checking csv file vs grid order");
		log.info("Checking csv file vs grid order");

		Reporter.log("checking number of records");
		log.info("checking number of records");

		List<HashMap<String, String>> gridInfo = new ArrayList<>();
		for (int i = 0; i < grid.getRowsNo(); i++) {
			gridInfo.add(grid.getRowInfo(i));
		}

		Reporter.log("checking individual records");
		log.info("checking individual records");
		for (HashMap<String, String> gridRow : gridInfo) {
			boolean found = false;
			for (CSVRecord record : records) {
				if (grid.csvRowVsGridRow(record, gridRow)) {
					found = true;
					break;
				}
			}
			soft.assertTrue(found, "Row has been identified in CSV file");
		}
	}

	/* EDELIVERY-8210 - AU-54-Check log for Download Certificate  */
	@Test(description = "AU-54", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void tlsCertDownload() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");

		if (page.getAlertArea().isShown() || page.grid().getRowsNo() == 0) {
			try {
				page.getAlertArea().closeAlert();
			} catch (Exception e) {

			}
			page.uploadTruststore(path, "test123");
			page.wait.forXMillis(1000);
		}

		page.getDownloadButton().click();

		page.getSidebar().goToPage(PAGES.AUDIT);
		AuditPage aPage = new AuditPage(driver);
		aPage.getFilters().setFilterData("table", "Truststore");
		Reporter.log("click on search button");
		log.info("click on search button");
		aPage.getFilters().getSearchButton().click();
		aPage.grid().waitForRowsToLoad();
		soft.assertTrue(aPage.grid().getRowInfo(0).get("Table").equals("Truststore"), "Grid has first record for Truststore");
		soft.assertTrue(aPage.grid().getRowInfo(0).get("Action").equals("Downloaded"), "Grid has first record with Downloaded event");
		soft.assertAll();
	}


}
