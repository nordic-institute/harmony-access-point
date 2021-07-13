package domibus.ui.ux;

import io.qameta.allure.*;
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

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
@Epic("Pmode Archive")
@Feature("UX")
public class PModeArchiveUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PMODE_ARCHIVE);

	/*PMA-1 - Open PMode - Archive page*/
	/*  PMA-1 - Open PMode - Archive page  */
	@Description("PMA-1 - Open PMode - Archive page")
	@Link(name = "EDELIVERY-5316", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5316")
	@AllureId("PMA-1")
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

		Allure.step("Checking page title");
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

	/*PMA-2 - User tries to Delete or Restore current PMode file*/
	/*  PMA-2 - User tries to Delete or Restore current PMode file  */
	@Description("PMA-2 - User tries to Delete or Restore current PMode file")
	@Link(name = "EDELIVERY-5317", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5317")
	@AllureId("PMA-2")
	@Test(description = "PMA-2", groups = {"multiTenancy", "singleTenancy"})
	public void restoreOrDeleteCurrentPMode() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			Allure.step("uploading PMode");
			log.info("uploading PMode");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		Allure.step("checking the first row is the current pmode");
		log.info("checking the first row is the current pmode");
		soft.assertTrue(page.grid().getRowInfo(0).get("Description").contains("[CURRENT]:"), "First row contains current pmode");

		Allure.step("selecting row 0");
		log.info("selecting row 0");
		page.grid().selectRow(0);

		Allure.step("checking page buttons");
		log.info("checking page buttons");
		soft.assertTrue(!page.getDeleteBtn().isEnabled(), "Delete is not enabled for the current pmode");
		soft.assertTrue(!page.getRestoreBtn().isEnabled(), "Restore is not enabled for the current pmode");

		Allure.step("checking row action buttons");
		log.info("checking row action buttons");
		soft.assertTrue(!page.pmagrid().isActionEnabledForRow(0, "Delete"), "Delete is not enabled for the current pmode");
		soft.assertTrue(!page.pmagrid().isActionEnabledForRow(0, "Restore"), "Restore is not enabled for the current pmode");

		soft.assertAll();
	}

	/*PMA-3 - User tries to download current file*/
	/*  PMA-3 - User tries to download current file  */
	@Description("PMA-3 - User tries to download current file")
	@Link(name = "EDELIVERY-5318", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5318")
	@AllureId("PMA-3")
	@Test(description = "PMA-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickCurrentPMode() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			Allure.step("uploading PMode");
			log.info("uploading PMode");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		Allure.step("checking the first row is the current pmode");
		log.info("checking the first row is the current pmode");
		soft.assertTrue(Boolean.valueOf(page.pmagrid().getRowInfo(0).get("Current")), "First row contains current pmode");

		Allure.step("doubleclick row 0");
		log.info("doubleclick row 0");
		page.grid().doubleClickRow(0);

		Allure.step("checking the info listed in the modal");
		log.info("checking the info listed in the modal");
		PMAModal modal = new PMAModal(driver);
		soft.assertTrue(modal.getTitle().getText().contains("[CURRENT]:"), "Title lists pmode as current");
		soft.assertTrue(modal.getTitle().getText().contains("Current PMode:"), "Title lists pmode as current");

		String pmode = modal.getTextarea().getText();
		modal.getOkBtn().click();

		Allure.step("downloading current pmode");
		log.info("downloading current pmode");
		String downloadedPMode = new String(Files.readAllBytes(Paths.get(rest.pmode().downloadPmode(null, rest.pmode().getLatestPModeID(null)))));

		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
		Allure.step("getting listed current pmode");
		log.info("getting listed current pmode");
		String listedPmodeCurrent = pmcPage.getTextArea().getText();

		Allure.step("comparing all 3 pmodes");
		log.info("comparing all 3 pmodes");
		Allure.step("pmode = " + pmode);
		log.debug("pmode = " + pmode);

		soft.assertTrue(XMLUnit.compareXML(pmode, downloadedPMode).identical(), "PMode in modal and the one downloaded are the same");
		soft.assertTrue(XMLUnit.compareXML(pmode, listedPmodeCurrent).identical(), "PMode in modal and the one in Pmode-Current page are the same");

		soft.assertAll();
	}

	/*PMA-4 - User tries to restore older PMode file*/
	/*  PMA-4 - User tries to restore older PMode file  */
	@Description("PMA-4 - User tries to restore older PMode file")
	@Link(name = "EDELIVERY-5319", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5319")
	@AllureId("PMA-4")
	@Test(description = "PMA-4", groups = {"multiTenancy", "singleTenancy"})
	public void restoreOldFile() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step(" go to PMode Archive page");
		log.info(" go to PMode Archive page");
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		Allure.step("make sure there are at least 2 entries in grid");
		log.info("make sure there are at least 2 entries in grid");
		if (page.grid().getRowsNo() < 2) {
			rest.pmode().uploadPMode("pmodes/doNothingSelfSending.xml", null);
			rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
			page.refreshPage();
		}

		Allure.step("doubleclick row 1");
		log.info("doubleclick row 1");
		String configDate = page.grid().getRowInfo(1).get("Configuration Date");
		page.grid().doubleClickRow(1);

		Allure.step("getting listed pmode from the modal for row 1");
		log.info("getting listed pmode from the modal for row 1");
		PMAModal modal = new PMAModal(driver);
		String pmode = modal.getTextarea().getText();
		modal.getOkBtn().click();

		Allure.step("click restore and confirm");
		log.info("click restore and confirm");
		page.pmagrid().clickAction(1, "Restore");
		new Dialog(driver).confirm();

		page.grid().waitForRowsToLoad();

		Allure.step("checking description");
		log.info("checking description");
		String currentPmodeDescription = page.grid().getRowInfo(0).get("Description");

		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);

		Allure.step("getting listed current pmode");
		log.info("getting listed current pmode");
		String listedPmodeCurrent = pmcPage.getTextArea().getText();

		Allure.step("comparing pmodes");
		log.info("comparing pmodes");
		soft.assertTrue(XMLUnit.compareXML(pmode, listedPmodeCurrent).identical(), "PMode in modal and the one in Pmode-Current page are the same");

		soft.assertAll();
	}

	/*PMA-5 - User tries to delete an older file*/
	/*  PMA-5 - User tries to delete an older file  */
	@Description("PMA-5 - User tries to delete an older file")
	@Link(name = "EDELIVERY-5320", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5320")
	@AllureId("PMA-5")
	@Test(description = "PMA-5", groups = {"multiTenancy", "singleTenancy"})
	public void deleteOldFile() throws Exception {

		while (rest.pmode().getPmodesList(null).length() < 3) {
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		SoftAssert soft = new SoftAssert();

		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
		page.grid().waitForRowsToLoad();

		Allure.step("getting config date for row 1");
		log.info("getting config date for row 1");
		String description = page.grid().getRowInfo(1).get("Description");
		page.deleteRow(1);

		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.PMODE_ARCHIVE_DELETE_SUCCESS, "Correct message is displayed");
		soft.assertFalse(page.getAlertArea().isError(), "Message is success");

		Allure.step("searching for deleted row...");
		log.info("searching for deleted row...");
		int index = page.grid().scrollTo("Description", description);

		soft.assertTrue(index == -1, "Row doesn't appear in the grid anymore");

		soft.assertAll();
	}

	/*PMA-6 - User downloads content of the grid*/
	/*  PMA-6 - User downloads content of the grid  */
	@Description("PMA-6 - User downloads content of the grid")
	@Link(name = "EDELIVERY-5321", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5321")
	@AllureId("PMA-6")
	@Test(description = "PMA-6", groups = {"multiTenancy", "singleTenancy"})
	public void downloadGrid() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		String fileName = rest.csv().downloadGrid(RestServicePaths.PMODE_ARCHIVE_CSV, null, null);
		Allure.step("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Allure.step("set page size to 100");
		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		Allure.step("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.pmagrid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	/*PMA-7 - User Doubleclick on grid row*/
	/*  PMA-7 - User Doubleclick on grid row  */
	@Description("PMA-7 - User Doubleclick on grid row")
	@Link(name = "EDELIVERY-5322", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5322")
	@AllureId("PMA-7")
	@Test(description = "PMA-7", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			Allure.step("uploading pmode");
			log.info("uploading pmode");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		Allure.step("double clicking row 0");
		log.info("double clicking row 0");
		page.grid().doubleClickRow(0);

		PMAModal modal = new PMAModal(driver);
		String pmode = modal.getTextarea().getText();

		Allure.step("Checking modal contains text");
		log.info("Checking modal contains text");
		soft.assertTrue(!StringUtils.isEmpty(pmode), "Modal contains pmode text");

		soft.assertAll();
	}


	/* PMA-10 - User tries to sort the grid */
	/*  PMA-10 - User tries to sort the grid  */
	@Description("PMA-10 - User tries to sort the grid")
	@Link(name = "EDELIVERY-7189", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7189")
	@AllureId("PMA-10")
	@Test(description = "PMA-10", groups = {"multiTenancy", "singleTenancy"})
	public void sortGrid() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("navigating to Pmode Archive page");
		log.info("navigating to Pmode Archive page");
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		Allure.step("Check default sorted column");
		log.info("Check default sorted column");
		soft.assertNull(grid.getSortedColumnName(), "Grid is not sortable and no column is marked as sorted by default");

		grid.sortBy("Description");

		Allure.step("Check sorted column name after sorting attempt");
		log.info("Check sorted column name after sorting attempt");
		soft.assertNull(grid.getSortedColumnName(), "Grid is not sortable and no column is marked as sorted ");


		soft.assertAll();
	}


	/* PMA-11 - User modifies visible columns */
	/*  PMA-11 - User modifies visible columns  */
	@Description("PMA-11 - User modifies visible columns")
	@Link(name = "EDELIVERY-7190", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7190")
	@AllureId("PMA-11")
	@Test(description = "PMA-11", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("navigating to Pmode Archive page");
		log.info("navigating to Pmode Archive page");
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		grid.checkModifyVisibleColumns(soft);


		soft.assertAll();
	}

	/* PMA-12 - Change current domain */
	/*  PMA-12 - Change current domain  */
	@Description("PMA-12 - Change current domain")
	@Link(name = "EDELIVERY-7191", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7191")
	@AllureId("PMA-12")
	@Test(description = "PMA-12", groups = {"multiTenancy"})
	public void domainSegregation() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("navigating to Pmode Archive page");
		log.info("navigating to Pmode Archive page");
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		Allure.step("extracting listed info");
		log.info("extracting listed info");
		ArrayList<HashMap<String, String>> infoDom1 = grid.getListedRowInfo();

		Allure.step("changing domain");
		log.info("changing domain");
		page.getDomainSelector().selectAnotherDomain();

		grid.waitForRowsToLoad();
		Allure.step("extracting listed info");
		log.info("extracting listed info");
		ArrayList<HashMap<String, String>> infoDom2 = grid.getListedRowInfo();

		Allure.step("checking for similarities in the data");
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
