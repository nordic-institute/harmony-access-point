package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import utils.BaseTest;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Audit.AuditPage;
import rest.RestServicePaths;
import utils.TestUtils;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class AuditPgUXTest extends BaseTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.AUDIT);


	/* AU-1 - Login as super admin and open Audit page */
	@Test(description = "AU-1", groups = {"multiTenancy", "singleTenancy"})
	public void openAuditPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

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
	@Test(description = "AU-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickAuditRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

		log.info("double click row 0");
		page.grid().doubleClickRow(0);

		log.info("checking the current selected row");
		soft.assertTrue(page.grid().getSelectedRowIndex()==-1, "Rows are not selectablde in Audit page");
		soft.assertTrue(!page.hasOpenDialog(), "No dialog is visible on the page");

		soft.assertAll();
	}

	/* AU-3 - Filter messages using basic filters */
	@Test(description = "AU-3", groups = {"multiTenancy", "singleTenancy"})
	public void basicFilter() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

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

	/* AU-4 - Open advanced filters */
	@Test(description = "AU-4", groups = {"multiTenancy", "singleTenancy"})
	public void checkAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		log.info("checking available filters in expanded state");
		page.filters().expandArea();
		advancedFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));

		soft.assertAll();
	}

	/*   AU-9 - Download list of events    */
	@Test(description = "AU-9", groups = {"multiTenancy", "singleTenancy"})
	public void downloadCSV() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage auditPage = new AuditPage(driver);
		auditPage.getSidebar().goToPage(PAGES.AUDIT);

		log.info("Validate Audit page");
		soft.assertEquals(auditPage.getTitle(), descriptorObj.getString("title"), "page is loaded successfully");

		log.info("Download all grid record csv");
		String fileName = rest.csv().downloadGrid(RestServicePaths.AUDIT_CSV, null, null);

		log.info("downloaded audit logs to file :" + fileName);
		System.out.println(auditPage.grid().getRowsNo());

		log.info("comparing any random row data from downloaded csv and grid");
		csvCheck(fileName, auditPage.grid(), soft);


		soft.assertAll();
	}

	/*   AU-38 - Verify headers in downloaded CSV sheet     */
	@Test(description = "AU-38", groups = {"multiTenancy", "singleTenancy"})
	public void verifyCSVHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		String fileName = rest.csv().downloadGrid(RestServicePaths.AUDIT_CSV, null, null);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		log.info("checking headers in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	/*   AU-37 - Check sorting on the basis of Headers of Grid	*/
	@Test(description = "AU-37", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		DGrid grid = page.grid();
		log.info("Getting column names");
		List<String> columns = grid.getColumnNames();
		for (String column : columns) {
			log.info("sorting by column " + column);
			grid.sortBy(column);

			log.info("checking sorting");
			soft.assertNull(grid.getSortedColumnName(), "After sorting by "+ column +", no column is marked as sorted");
		}

		soft.assertAll();
	}

	/*   AU-36 - Change Rows field data	*/
	@Test(description = "AU-36", groups = {"multiTenancy", "singleTenancy"})
	public void changeNumberOfRows() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/*	AU-35 - Click All None link	*/
	@Test(description = "AU-35", groups = {"multiTenancy", "singleTenancy"})
	public void clickAllNoneLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		DGrid grid = page.grid();

		grid.checkAllLink(soft);
		grid.checkNoneLink(soft);

		soft.assertAll();
	}

	/*	AU-34 - Click Hide link after selecting some new fields	*/
	@Test(description = "AU-34", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		String colName = "Id";

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		log.info("getting list of columns: " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		log.info("disable column with name " + colName);
		grid.getGridCtrl().uncheckBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		log.info("getting list of columns " + columnsPost);

		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() - 1 == columnsPost.size(), "One less column is shown");
		soft.assertTrue(!columnsPost.contains(colName), "Correct column is now in the list of columns");


		soft.assertAll();
	}

	/*	AU-33- Click Hide link without any new selection	*/
	@Test(description = "AU-33", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

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
	@Test(description = "AU-32", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		List<String> columnList = new ArrayList<>(grid.getGridCtrl().getAllCheckboxStatuses().keySet());
		grid.checkModifyVisibleColumns(soft, columnList);

		soft.assertAll();
	}

	/*	AU-31 - Click Show columns link	*/
	@Test(description = "AU-31", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}


	private void csvCheck(String filename, DGrid grid, SoftAssert soft) throws Exception {
		Reader reader = Files.newBufferedReader(Paths.get(filename));
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase()
				.withTrim());
		List<CSVRecord> records = csvParser.getRecords();

		log.info("Checking csv file vs grid order");

		log.info("checking number of records");

		List<HashMap<String, String>> gridInfo = new ArrayList<>();
		for (int i = 0; i < grid.getRowsNo(); i++) {
			gridInfo.add(grid.getRowInfo(i));
		}

		log.info("checking individual records");
		for (HashMap<String, String> gridRow : gridInfo) {
			boolean found = false;
			for (CSVRecord record : records) {
				if(grid.csvRowVsGridRow(record, gridRow)){
					found = true;
					break;
				}
			}
			soft.assertTrue(found, "Row has been identified in CSV file");
		}
	}


}
