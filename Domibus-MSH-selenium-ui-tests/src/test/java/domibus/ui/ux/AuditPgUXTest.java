package domibus.ui.ux;

import io.qameta.allure.*;
import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Audit.AuditPage;
import pages.tlsTrustStore.TlsTrustStorePage;
import rest.RestServicePaths;
import utils.TestRunData;
import utils.TestUtils;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
@Epic("")
@Feature("")
public class AuditPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.AUDIT);


	/* AU-1 - Login as super admin and open Audit page */
	/*  AU-1 - Login as super admin and open Audit page  */
	@Description("AU-1 - Login as super admin and open Audit page")
	@Link(name = "EDELIVERY-5242", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5242")
	@AllureId("AU-1")
	@Test(description = "AU-1", groups = {"multiTenancy", "singleTenancy"})
	public void openAuditPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

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

	/* AU-2 - Doubleclick/Single click on one event */
	/*  AU-2 - DoubleclickSingle click on one event  */
	@Description("AU-2 - DoubleclickSingle click on one event")
	@Link(name = "EDELIVERY-5243", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5243")
	@AllureId("AU-2")
	@Test(description = "AU-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickAuditRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

		Allure.step("double click row 0");
		log.info("double click row 0");
		page.grid().doubleClickRow(0);

		Allure.step("checking the current selected row");
		log.info("checking the current selected row");
		soft.assertTrue(page.grid().getSelectedRowIndex() == -1, "Rows are not selectablde in Audit page");
		soft.assertTrue(!page.hasOpenDialog(), "No dialog is visible on the page");

		soft.assertAll();
	}

	/* AU-3 - Filter messages using basic filters */
	/*  AU-3 - Filter messages using basic filters  */
	@Description("AU-3 - Filter messages using basic filters")
	@Link(name = "EDELIVERY-5244", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5244")
	@AllureId("AU-3")
	@Test(description = "AU-3", groups = {"multiTenancy", "singleTenancy"})
	public void basicFilter() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

		Allure.step("getting info from row 0");
		log.info("getting info from row 0");
		HashMap<String, String> info = page.grid().getRowInfo(0);

		page.filters().simpleFilter(info.get("Table")
				, null
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

	/* AU-4 - Open advanced filters */
	/*  AU-4 - Open advanced filters  */
	@Description("AU-4 - Open advanced filters")
	@Link(name = "EDELIVERY-5245", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5245")
	@AllureId("AU-4")
	@Test(description = "AU-4", groups = {"multiTenancy", "singleTenancy"})
	public void checkAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		Allure.step("checking available filters in expanded state");
		log.info("checking available filters in expanded state");
		page.filters().expandArea();
		advancedFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));

		soft.assertAll();
	}/* AU-4 - Open advanced filters */


	/*EDELIVERY-5246 - AU-5 - Filter events using advanced filters */
	/*  AU-5 - Filter events using advanced filters  */
	@Description("AU-5 - Filter events using advanced filters")
	@Link(name = "EDELIVERY-5246", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5246")
	@AllureId("AU-5")
	@Test(description = "AU-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterWAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		Allure.step("checking available filters in expanded state");
		log.info("checking available filters in expanded state");
		page.filters().expandArea();

		JSONObject event = rest.audit().filterAuditLog(null, null, null, null).getJSONObject(0);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(event.getLong("changed"));
		Date eventDate = cal.getTime();

		Date fromDate = DateUtils.addHours(eventDate, -1);
		Date toDate = DateUtils.addHours(eventDate, 1);

		Allure.step("Filtering ...");
		log.info("Filtering ...");
		page.filters().advancedFilter(
				event.getString("auditTargetName")
				, null
				, event.getString("action")
				, fromDate
				, toDate);

		Allure.step("waiting for rows to load");
		log.info("waiting for rows to load");
		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.grid().getRowsNo() >= 1, "At least the source event matches filters");

		ArrayList<HashMap<String, String>> info = page.grid().getListedRowInfo();

		for (HashMap<String, String> map : info) {
			soft.assertEquals(map.get("Table"), event.getString("auditTargetName"), "Table - value corresponds to filter value");
//			soft.assertEquals(map.get("User"), event.getString("user"), "User - value corresponds to filter value");
			soft.assertEquals(map.get("Action"), event.getString("action"), "Action - value corresponds to filter value");

			Date gridRowDate = TestRunData.UI_DATE_FORMAT2.parse(map.get("Changed") + ":00");
			soft.assertTrue(gridRowDate.after(fromDate), "Row date is after event date");
			soft.assertTrue(gridRowDate.before(toDate), "Row date is before event date");
		}

		soft.assertAll();
	}

	/*   AU-9 - Download list of events    */
	/*  AU-9 - Download list of events  */
	@Description("AU-9 - Download list of events")
	@Link(name = "EDELIVERY-5250", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5250")
	@AllureId("AU-9")
	@Test(description = "AU-9", groups = {"multiTenancy", "singleTenancy"})
	public void downloadCSV() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		Allure.step("Validate Audit page");
		log.info("Validate Audit page");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "page is loaded successfully");

		Allure.step("Download all grid record csv");
		log.info("Download all grid record csv");
		String fileName = rest.csv().downloadGrid(RestServicePaths.AUDIT_CSV, null, null);

		Allure.step("downloaded audit logs to file :" + fileName);
		log.info("downloaded audit logs to file :" + fileName);
		Allure.step("" + page.grid().getRowsNo());
		log.debug("" + page.grid().getRowsNo());

		Allure.step("comparing any random row data from downloaded csv and grid");
		log.info("comparing any random row data from downloaded csv and grid");
		csvCheck(fileName, page.grid(), soft);


		soft.assertAll();
	}

	/*   AU-38 - Verify headers in downloaded CSV sheet     */
	/*  AU-38 - Verify headers in downloaded CSV file  */
	@Description("AU-38 - Verify headers in downloaded CSV file")
	@Link(name = "EDELIVERY-5280", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5280")
	@AllureId("AU-38")
	@Test(description = "AU-38", groups = {"multiTenancy", "singleTenancy"})
	public void verifyCSVHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		String fileName = rest.csv().downloadGrid(RestServicePaths.AUDIT_CSV, null, null);
		Allure.step("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Allure.step("checking headers in grid against the file");
		log.info("checking headers in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	/*   AU-37 - Check sorting on the basis of Headers of Grid	*/
	/*  AU-37 - Check sorting  */
	@Description("AU-37 - Check sorting")
	@Link(name = "EDELIVERY-5279", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5279")
	@AllureId("AU-37")
	@Test(description = "AU-37", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		DGrid grid = page.grid();
		Allure.step("Getting column names");
		log.info("Getting column names");
		List<String> columns = grid.getColumnNames();
		for (String column : columns) {
			Allure.step("sorting by column " + column);
			log.info("sorting by column " + column);
			grid.sortBy(column);

			String sortCol = grid.getSortedColumnName();
			Allure.step("checking sorting");
			log.info("checking sorting");
			soft.assertEquals(sortCol, null, "After sorting, grid still has no sorted column");
		}

		soft.assertAll();
	}

	/*   AU-36 - Change Rows field data	*/
	/*  AU-36 - Change number of visible rows  */
	@Description("AU-36 - Change number of visible rows")
	@Link(name = "EDELIVERY-5278", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5278")
	@AllureId("AU-36")
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

	/*	AU-35 - Click All None link	*/
	/*  AU-35 - Click All None link  */
	@Description("AU-35 - Click All None link")
	@Link(name = "EDELIVERY-5277", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5277")
	@AllureId("AU-35")
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

	/*	AU-34 - Click Hide link after selecting some new fields	*/
	/*  AU-34 - Click Hide link after selecting some new fields  */
	@Description("AU-34 - Click Hide link after selecting some new fields")
	@Link(name = "EDELIVERY-5276", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5276")
	@AllureId("AU-34")
	@Test(description = "AU-34", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		String colName = "Id";

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Allure.step("getting list of columns: " + columnsPre);
		log.info("getting list of columns: " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		Allure.step("disable column with name " + colName);
		log.info("disable column with name " + colName);
		grid.getGridCtrl().uncheckBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		Allure.step("getting list of columns " + columnsPost);
		log.info("getting list of columns " + columnsPost);

		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() - 1 == columnsPost.size(), "One less column is shown");
		soft.assertTrue(!columnsPost.contains(colName), "Correct column is now in the list of columns");


		soft.assertAll();
	}

	/*	AU-33- Click Hide link without any new selection	*/
	/*  AU-33 - Click Hide link without any new selection  */
	@Description("AU-33 - Click Hide link without any new selection")
	@Link(name = "EDELIVERY-5275", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5275")
	@AllureId("AU-33")
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

	/*	AU-32 - Check/Uncheck of fields on Show links	*/
	/*  AU-32 - CheckUncheck of fields on Show links  */
	@Description("AU-32 - CheckUncheck of fields on Show links")
	@Link(name = "EDELIVERY-5274", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5274")
	@AllureId("AU-32")
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

	/*	AU-31 - Click Show columns link	*/
	/*  AU-31 - Click Show columns link  */
	@Description("AU-31 - Click Show columns link")
	@Link(name = "EDELIVERY-5273", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5273")
	@AllureId("AU-31")
	@Test(description = "AU-31", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		Allure.step("Checking visibility of All/None links");
		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}


	/**
	 * This method will verify data for action column in downloaded csv and grid on admin console
	 */
	/*  AU-41 - Verify downloaded CSV file of events  */
	@Description("AU-41 - Verify downloaded CSV file of events")
	@Link(name = "EDELIVERY-6361", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-6361")
	@AllureId("AU-41")
	@Test(description = "AU-41", groups = {"multiTenancy", "singleTenancy"})
	public void verifyActionData() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		Allure.step("Customized location for download");
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

		Allure.step("Checking csv file vs grid order");
		log.info("Checking csv file vs grid order");

		Allure.step("checking number of records");
		log.info("checking number of records");

		List<HashMap<String, String>> gridInfo = new ArrayList<>();
		for (int i = 0; i < grid.getRowsNo(); i++) {
			gridInfo.add(grid.getRowInfo(i));
		}

		Allure.step("checking individual records");
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

	/* This test method will check audit log for download tls truststore event*/
	/*  AU-54-Check log for Download Certificate   */
	@Description("AU-54-Check log for Download Certificate ")
	@Link(name = "EDELIVERY-8210", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8210")
	@AllureId("AU-54")
	@Test(description = "AU-54", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void tlsCertDownload() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		page.getDownloadButton().click();

		page.getSidebar().goToPage(PAGES.AUDIT);
		AuditPage aPage = new AuditPage(driver);
		aPage.getFilters().setFilterData("table", "Truststore");
		Allure.step("click on search button");
		log.info("click on search button");
		aPage.getFilters().getSearchButton().click();
		aPage.grid().waitForRowsToLoad();
		soft.assertTrue(aPage.grid().getRowInfo(0).get("Table").equals("Truststore"), "Grid has first record for Truststore");
		soft.assertTrue(aPage.grid().getRowInfo(0).get("Action").equals("Downloaded"), "Grid has first record with Downloaded event");
		soft.assertAll();
	}


}
