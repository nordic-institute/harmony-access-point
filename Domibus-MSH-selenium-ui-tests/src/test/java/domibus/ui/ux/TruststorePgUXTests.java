package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.truststore.TruststoreModal;
import pages.truststore.TruststorePage;
import rest.RestServicePaths;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class TruststorePgUXTests extends SeleniumTest {
	
	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.TRUSTSTORE);
	
	/* Login as super admin and open Truststore page */
	@Test(description = "TRST-1", groups = {"multiTenancy", "singleTenancy"})
	public void openTrustorePage() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		log.info("checking page default state");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");
		
		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));
		
		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}
		
		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");
		
		
		soft.assertAll();
	}
	
	/* User doubleclicks on one entry */
	@Test(description = "TRST-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickTruststoreEntry() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		HashMap<String, String> entryInfo = page.grid().getRowInfo(0);
		log.info("gathering grid data - " + entryInfo);
		
		log.info("double click row 0");
		page.grid().doubleClickRow(0);
		HashMap<String, String> modalInfo = new TruststoreModal(driver).getInfo();
		log.info("gathering modal data - " + modalInfo);
		
		boolean eq = TestUtils.areMapsEqual(entryInfo, modalInfo);
		soft.assertTrue(eq, "Same info listed in grid and modal");
		
		soft.assertAll();
	}
	
	/* Download list of truststore entries */
	@Test(description = "TRST-3", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		String fileName = rest.csv().downloadGrid(RestServicePaths.TRUSTSTORE_CSV, null, null);
		log.info("downloaded rows to file " + fileName);
		page.grid().checkCSVvsGridInfo(fileName, soft);
		
		
		soft.assertAll();
	}
	
	/* Click Show columns link */
	@Test(description = "TRST-6", groups = {"multiTenancy", "singleTenancy"})
	public void checkShowColumnsLink() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();
		
		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));
		
		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");
		
		
		soft.assertAll();
	}
	
	/* Check/Uncheck of fields on Show links */
	@Test(description = "TRST-7", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		grid.checkModifyVisibleColumns(soft);
		
		soft.assertAll();
	}
	
	/* Click Hide link without any new selection */
	@Test(description = "TRST-8", groups = {"multiTenancy", "singleTenancy"})
	public void hideNoNewSelection() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
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
	@Test(description = "TRST-9", groups = {"multiTenancy", "singleTenancy"})
	public void modifyColumnsAndHideLnk() throws Exception {
		String colName = "Issuer";
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
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
		soft.assertTrue(columnsPre.size() - 1 == columnsPost.size(), "One more column is shown");
		soft.assertTrue(!columnsPost.contains(colName), "Correct column is now in the list of columns");
		
		
		soft.assertAll();
	}
	
	
	/* Click All None link */
	@Test(description = "TRST-10", groups = {"multiTenancy", "singleTenancy"})
	public void checkAllNoneLnk() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		DGrid grid = page.grid();
		
		grid.checkAllLink(soft);
		grid.checkNoneLink(soft);
		
		
		soft.assertAll();
	}
	
	
	/* Change Rows field data */
	@Test(description = "TRST-11", groups = {"multiTenancy", "singleTenancy"})
	public void modifyRows() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);
		soft.assertAll();
	}
	
	
	/* Check sorting on the basis of Headers of Grid  */
	@Test(description = "TRST-16", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		DGrid grid = page.grid();
		grid.getPagination().getPageSizeSelect().selectOptionByText("100");
		
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");
		for (int i = 0; i < colDescs.length(); i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}
		
		soft.assertAll();
	}
	
	/* Verify headers in downloaded CSV sheet  */
	@Test(description = "TRST-17", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {
		
		SoftAssert soft = new SoftAssert();
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		String fileName = rest.csv().downloadGrid(RestServicePaths.TRUSTSTORE_CSV, null, null);
		log.info("downloaded file with name " + fileName);
		
		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();
		
		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);
		
		soft.assertAll();
	}
	
}
