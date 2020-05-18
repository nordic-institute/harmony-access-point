package domibus.ui.ux;

import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.current.PModeCurrentPage;
import pages.pmode.archive.PMAModal;
import pages.pmode.archive.PModeArchivePage;
import rest.RestServicePaths;
import utils.TestUtils;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class PModeArchiveUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PMODE_ARCHIVE);

	/*PMA-1 - Open PMode - Archive page*/
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
	@Test(description = "PMA-2", groups = {"multiTenancy", "singleTenancy"})
	public void restoreOrDeleteCurrentPMode() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			log.info("uploading PMode");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		log.info("checking the first row is the current pmode");
		soft.assertTrue(page.grid().getRowInfo(0).get("Description").contains("[CURRENT]:"), "First row contains current pmode");

		log.info("selecting row 0");
		page.grid().selectRow(0);

		log.info("checking page buttons");
		soft.assertTrue(!page.getDeleteBtn().isEnabled(), "Delete is not enabled for the current pmode");
		soft.assertTrue(!page.getRestoreBtn().isEnabled(), "Restore is not enabled for the current pmode");

		log.info("checking row action buttons");
		soft.assertTrue(!page.pmagrid().isActionEnabledForRow(0,"Delete"), "Delete is not enabled for the current pmode");
		soft.assertTrue(!page.pmagrid().isActionEnabledForRow(0,"Restore"), "Restore is not enabled for the current pmode");

		soft.assertAll();
	}

	/*PMA-3 - User tries to download current file*/
	@Test(description = "PMA-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickCurrentPMode() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			log.info("uploading PMode");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		log.info("checking the first row is the current pmode");
		soft.assertTrue(Boolean.valueOf(page.pmagrid().getRowInfo(0).get("Current")), "First row contains current pmode");

		log.info("doubleclick row 0");
		page.grid().doubleClickRow(0);

		log.info("checking the info listed in the modal");
		PMAModal modal = new PMAModal(driver);
		soft.assertTrue(modal.getTitle().getText().contains("[CURRENT]:"), "Title lists pmode as current");
		soft.assertTrue(modal.getTitle().getText().contains("Current PMode:"), "Title lists pmode as current");

		String pmode = modal.getTextarea().getText();
		modal.getOkBtn().click();

		log.info("downloading current pmode");
		String downloadedPMode = new String(Files.readAllBytes(Paths.get(rest.pmode().downloadPmode(null, rest.pmode().getLatestPModeID(null)))));

		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
		log.info("getting listed current pmode");
		String listedPmodeCurrent = pmcPage.getTextArea().getText();

		log.info("comparing all 3 pmodes");
		System.out.println("pmode = " + pmode);

		soft.assertTrue(XMLUnit.compareXML(pmode, downloadedPMode).identical(), "PMode in modal and the one downloaded are the same");
		soft.assertTrue(XMLUnit.compareXML(pmode, listedPmodeCurrent).identical(), "PMode in modal and the one in Pmode-Current page are the same");

		soft.assertAll();
	}

	/*PMA-4 - User tries to restore older PMode file*/
	@Test(description = "PMA-4", groups = {"multiTenancy", "singleTenancy"})
	public void restoreOldFile() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info(" go to PMode Archive page");
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		log.info("make sure there are at least 2 entries in grid");
		if (page.grid().getRowsNo() < 2) {
			rest.pmode().uploadPMode("pmodes/doNothingSelfSending.xml", null);
			rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
			page.refreshPage();
		}

		log.info("doubleclick row 1");
		String configDate = page.grid().getRowInfo(1).get("Configuration Date");
		page.grid().doubleClickRow(1);

		log.info("getting listed pmode from the modal for row 1");
		PMAModal modal = new PMAModal(driver);
		String pmode = modal.getTextarea().getText();
		modal.getOkBtn().click();

		log.info("click restore and confirm");
		page.pmagrid().clickAction(1, "Restore");
		new Dialog(driver).confirm();

		page.grid().waitForRowsToLoad();

		log.info("checking description");
		String currentPmodeDescription = page.grid().getRowInfo(0).get("Description");

		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);

		log.info("getting listed current pmode");
		String listedPmodeCurrent = pmcPage.getTextArea().getText();

		log.info("comparing pmodes");
		soft.assertTrue(XMLUnit.compareXML(pmode, listedPmodeCurrent).identical(), "PMode in modal and the one in Pmode-Current page are the same");

		soft.assertAll();
	}

	/*PMA-5 - User tries to delete an older file*/
	@Test(description = "PMA-5", groups = {"multiTenancy", "singleTenancy"})
	public void deleteOldFile() throws Exception {

		while (rest.getPmodesList(null).length() < 3){
			rest.uploadPMode("pmodes/pmode-blue.xml", null);
		}

		SoftAssert soft = new SoftAssert();

		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
		page.grid().waitForRowsToLoad();

		log.info("getting config date for row 1");
		String description = page.grid().getRowInfo(1).get("Description");
		page.deleteRow(1);

		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.PMODE_ARCHIVE_DELETE_SUCCESS, "Correct message is displayed");
		soft.assertFalse(page.getAlertArea().isError(), "Message is success");

		log.info("searching for deleted row...");
		int index = page.grid().scrollTo("Description", description);

		soft.assertTrue(index==-1, "Row doesn't appear in the grid anymore");

		soft.assertAll();
	}

	/*PMA-6 - User downloads content of the grid*/
	@Test(description = "PMA-6", groups = {"multiTenancy", "singleTenancy"})
	public void downloadGrid() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		String fileName = rest.csv().downloadGrid(RestServicePaths.PMODE_ARCHIVE_CSV, null, null);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		log.info("checking info in grid against the file");
		page.pmagrid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	/*PMA-7 - User Doubleclick on grid row*/
	@Test(description = "PMA-6", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		PModeArchivePage page = new PModeArchivePage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		if (page.grid().getRowsNo() == 0) {
			log.info("uploading pmode");
			rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
			page.refreshPage();
		}

		log.info("double clicking row 0");
		page.grid().doubleClickRow(0);

		PMAModal modal = new PMAModal(driver);
		String pmode = modal.getTextarea().getText();

		log.info("Checking modal contains text");
		soft.assertTrue(!StringUtils.isEmpty(pmode), "Modal contains pmode text");

		soft.assertAll();
	}




}
