package domibus.ui.ux;

import org.testng.Reporter;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.archive.PMAModal;
import pages.pmode.archive.PModeArchivePage;
import pages.pmode.current.PModeCurrentPage;
import rest.RestServicePaths;
import utils.TestUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PModeArchiveUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PMODE_ARCHIVE);

	/* EDELIVERY-5316 - PMA-1 - Open PMode - Archive page */
	@Test(description = "PMA-1", groups = {"multiTenancy", "singleTenancy"})
	public void openPmodeArchivePage() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			soft.assertTrue(!page.getDownloadBtn().isEnabled(), "If archive is empty the download button is disabled");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		Reporter.log("Checking page title");
		log.info("Checking page title");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		soft.assertTrue(page.grid().getRowInfo(0).get("Description").contains("[CURRENT]:"), "First row contains current pmode");

		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}

		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");

		testButtonPresence(soft, page, descriptorObj.getJSONArray("buttons"));

		soft.assertAll();

	}

	/* EDELIVERY-5317 - PMA-2 - User tries to Delete or Restore current PMode file */
	@Test(description = "PMA-2", groups = {"multiTenancy", "singleTenancy"})
	public void restoreOrDeleteCurrentPMode() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			Reporter.log("uploading PMode");
			log.info("uploading PMode");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		Reporter.log("checking the first row is the current pmode");
		log.info("checking the first row is the current pmode");
		soft.assertTrue(page.grid().getRowInfo(0).get("Description").contains("[CURRENT]:"), "First row contains current pmode");

		Reporter.log("selecting row 0");
		log.info("selecting row 0");
		page.grid().selectRow(0);

		Reporter.log("checking page buttons");
		log.info("checking page buttons");
		soft.assertTrue(!page.getDeleteBtn().isEnabled(), "Delete is not enabled for the current pmode");
		soft.assertTrue(!page.getRestoreBtn().isEnabled(), "Restore is not enabled for the current pmode");

		Reporter.log("checking row action buttons");
		log.info("checking row action buttons");
		soft.assertTrue(!page.pmagrid().isActionEnabledForRow(0, "Delete"), "Delete is not enabled for the current pmode");
		soft.assertTrue(!page.pmagrid().isActionEnabledForRow(0, "Restore"), "Restore is not enabled for the current pmode");

		soft.assertAll();
	}

	/* EDELIVERY-5318 - PMA-3 - User tries to download current file */
	@Test(description = "PMA-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickCurrentPMode() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			Reporter.log("uploading PMode");
			log.info("uploading PMode");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		Reporter.log("checking the first row is the current pmode");
		log.info("checking the first row is the current pmode");
		soft.assertTrue(Boolean.valueOf(page.pmagrid().getRowInfo(0).get("Current")), "First row contains current pmode");

		Reporter.log("doubleclick row 0");
		log.info("doubleclick row 0");
		page.grid().doubleClickRow(0);

		Reporter.log("checking the info listed in the modal");
		log.info("checking the info listed in the modal");
		PMAModal modal = new PMAModal(driver);
		soft.assertTrue(modal.getTitle().getText().contains("[CURRENT]:"), "Title lists pmode as current");
		soft.assertTrue(modal.getTitle().getText().contains("Current PMode:"), "Title lists pmode as current");

		String pmode = modal.getTextarea().getText();
		modal.getOkBtn().click();

		Reporter.log("downloading current pmode");
		log.info("downloading current pmode");
		String downloadedPMode = new String(Files.readAllBytes(Paths.get(rest.pmode().downloadPmodeFile(null, rest.pmode().getLatestPModeID(null)))));

		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
		Reporter.log("getting listed current pmode");
		log.info("getting listed current pmode");
		String listedPmodeCurrent = pmcPage.getTextArea().getText();

		Reporter.log("comparing all 3 pmodes");
		log.info("comparing all 3 pmodes");
		Reporter.log("pmode = " + pmode);
		log.debug("pmode = " + pmode);

		soft.assertTrue(XMLUnit.compareXML(pmode, downloadedPMode).identical(), "PMode in modal and the one downloaded are the same");
		soft.assertTrue(XMLUnit.compareXML(pmode, listedPmodeCurrent).identical(), "PMode in modal and the one in Pmode-Current page are the same");

		soft.assertAll();
	}

	/* EDELIVERY-5319 - PMA-4 - User tries to restore older PMode file */
	@Test(description = "PMA-4", groups = {"multiTenancy", "singleTenancy"})
	public void restoreOldFile() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log(" go to PMode Archive page");
		log.info(" go to PMode Archive page");
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		Reporter.log("make sure there are at least 2 entries in grid");
		log.info("make sure there are at least 2 entries in grid");
		if (page.grid().getRowsNo() < 2) {
			rest.pmode().uploadPMode("pmodes/doNothingSelfSending.xml", null);
			rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
			page.refreshPage();
		}

		Reporter.log("doubleclick row 1");
		log.info("doubleclick row 1");
		String configDate = page.grid().getRowInfo(1).get("Configuration Date");
		page.grid().doubleClickRow(1);

		Reporter.log("getting listed pmode from the modal for row 1");
		log.info("getting listed pmode from the modal for row 1");
		PMAModal modal = new PMAModal(driver);
		String pmode = modal.getTextarea().getText();
		modal.getOkBtn().click();

		Reporter.log("click restore and confirm");
		log.info("click restore and confirm");
		page.pmagrid().clickAction(1, "Restore");
		new Dialog(driver).confirm();

		page.grid().waitForRowsToLoad();

		Reporter.log("checking description");
		log.info("checking description");
		String currentPmodeDescription = page.grid().getRowInfo(0).get("Description");

		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);

		Reporter.log("getting listed current pmode");
		log.info("getting listed current pmode");
		String listedPmodeCurrent = pmcPage.getTextArea().getText();

		Reporter.log("comparing pmodes");
		log.info("comparing pmodes");
		soft.assertTrue(XMLUnit.compareXML(pmode, listedPmodeCurrent).identical(), "PMode in modal and the one in Pmode-Current page are the same");

		soft.assertAll();
	}

	/* EDELIVERY-5320 - PMA-5 - User tries to delete an older file */
	@Test(description = "PMA-5", groups = {"multiTenancy", "singleTenancy"})
	public void deleteOldFile() throws Exception {

		while (rest.pmode().getPmodesList(null).length() < 3) {
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		SoftAssert soft = new SoftAssert();

		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
		page.grid().waitForRowsToLoad();

		Reporter.log("getting config date for row 1");
		log.info("getting config date for row 1");
		String description = page.grid().getRowInfo(1).get("Description");
		page.deleteRow(1);

		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.PMODE_ARCHIVE_DELETE_SUCCESS, "Correct message is displayed");
		soft.assertFalse(page.getAlertArea().isError(), "Message is success");

		Reporter.log("searching for deleted row...");
		log.info("searching for deleted row...");
		int index = page.grid().scrollTo("Description", description);

		soft.assertTrue(index == -1, "Row doesn't appear in the grid anymore");

		soft.assertAll();
	}

	/* EDELIVERY-5321 - PMA-6 - User downloads content of the grid */
	@Test(description = "PMA-6", groups = {"multiTenancy", "singleTenancy"})
	public void downloadGrid() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		String fileName = rest.csv().downloadGrid(RestServicePaths.PMODE_ARCHIVE_CSV, null, null);
		Reporter.log("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Reporter.log("set page size to 100");
		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		Reporter.log("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.pmagrid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	/* EDELIVERY-5322 - PMA-7 - User Doubleclick on grid row */
	@Test(description = "PMA-7", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			Reporter.log("uploading pmode");
			log.info("uploading pmode");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		Reporter.log("double clicking row 0");
		log.info("double clicking row 0");
		page.grid().doubleClickRow(0);

		PMAModal modal = new PMAModal(driver);
		String pmode = modal.getTextarea().getText();

		Reporter.log("Checking modal contains text");
		log.info("Checking modal contains text");
		soft.assertTrue(!StringUtils.isEmpty(pmode), "Modal contains pmode text");

		soft.assertAll();
	}


	/* EDELIVERY-7189 - PMA-10 - User tries to sort the grid */
	@Test(description = "PMA-10", groups = {"multiTenancy", "singleTenancy"})
	public void sortGrid() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("navigating to Pmode Archive page");
		log.info("navigating to Pmode Archive page");
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		Reporter.log("Check default sorted column");
		log.info("Check default sorted column");
		soft.assertNull(grid.getSortedColumnName(), "Grid is not sortable and no column is marked as sorted by default");

		grid.sortBy("Description");

		Reporter.log("Check sorted column name after sorting attempt");
		log.info("Check sorted column name after sorting attempt");
		soft.assertNull(grid.getSortedColumnName(), "Grid is not sortable and no column is marked as sorted ");


		soft.assertAll();
	}


	/* EDELIVERY-7190 - PMA-11 - User modifies visible columns */
	@Test(description = "PMA-11", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("navigating to Pmode Archive page");
		log.info("navigating to Pmode Archive page");
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		grid.checkModifyVisibleColumns(soft);


		soft.assertAll();
	}

	/* EDELIVERY-7191 - PMA-12 - Change current domain */
	@Test(description = "PMA-12", groups = {"multiTenancy"})
	public void domainSegregation() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("navigating to Pmode Archive page");
		log.info("navigating to Pmode Archive page");
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		Reporter.log("extracting listed info");
		log.info("extracting listed info");
		ArrayList<HashMap<String, String>> infoDom1 = grid.getListedRowInfo();

		Reporter.log("changing domain");
		log.info("changing domain");
		page.getDomainSelector().selectAnotherDomain();

		grid.waitForRowsToLoad();
		Reporter.log("extracting listed info");
		log.info("extracting listed info");
		ArrayList<HashMap<String, String>> infoDom2 = grid.getListedRowInfo();

		Reporter.log("checking for similarities in the data");
		log.info("checking for similarities in the data");

		ArrayList<String> similarities = new ArrayList<>();
		for (int i = 0; i < infoDom1.size(); i++) {
			String rowDom1 = infoDom1.get(i).toString();

			for (int j = 0; j < infoDom2.size(); j++) {
				String rowDom2 = infoDom2.get(j).toString();
				soft.assertNotEquals(rowDom1, rowDom2, "Rows differ between domains");
			}
		}


		soft.assertAll();
	}


}
