package domibus.ui.ux;

import io.qameta.allure.*;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.parties.PModePartiesPage;
import pages.pmode.parties.PartyModal;

import java.util.HashMap;
import java.util.List;

@Epic("")
@Feature("")
public class PmodePartiesPgUXTest extends SeleniumTest {

	/*  PMP-1 - Open PMode - Parties page  */
	@Description("PMP-1 - Open PMode - Parties page")
	@Link(name = "EDELIVERY-5323", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5323")
	@AllureId("PMP-1")
	@Test(description = "PMP-1", groups = {"multiTenancy", "singleTenancy"})
	public void openPModePartiesPage() throws Exception {

		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		SoftAssert soft = new SoftAssert();

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		soft.assertTrue(page.filters().getNameInput().isEnabled(), "Page contains filter for party name");
		soft.assertTrue(page.filters().getEndpointInput().isEnabled(), "Page contains filter for party endpoint");
		soft.assertTrue(page.filters().getPartyIDInput().isEnabled(), "Page contains filter for party id");
		soft.assertTrue(page.filters().getProcessRoleSelect().isDisplayed(), "Page contains filter for process role");

		List<HashMap<String, String>> partyInfo = page.grid().getAllRowInfo();
		soft.assertTrue(partyInfo.size() == 2, "Grid contains both the parties described in PMode file");

		soft.assertTrue(!page.getCancelButton().isEnabled(), "Cancel button is NOT enabled");
		soft.assertTrue(!page.getSaveButton().isEnabled(), "Save button is NOT enabled");
		soft.assertTrue(!page.getEditButton().isEnabled(), "Edit button is NOT enabled");
		soft.assertTrue(!page.getDeleteButton().isEnabled(), "Delete button is NOT enabled");
		soft.assertTrue(page.getNewButton().isEnabled(), "New button is enabled");

		soft.assertAll();

	}

	/*  PMP-18 - User singleclicks on a row on Pmode-Parties  */
	@Description("PMP-18 - User singleclicks on a row on Pmode-Parties")
	@Link(name = "EDELIVERY-5340", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5340")
	@AllureId("PMP-18")
	@Test(description = "PMP-18", groups = {"multiTenancy", "singleTenancy"})
	public void selectRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		soft.assertTrue(!page.getEditButton().isEnabled(), "Edit button is not enabled");
		soft.assertTrue(!page.getDeleteButton().isEnabled(), "Delete button is not enabled");

		page.grid().selectRow(0);

		soft.assertTrue(page.getEditButton().isEnabled(), "Edit button is enabled after select row");
		soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled after select row");

		soft.assertAll();
	}

	/*  PMP-3 - User chooses to download the list of parties  */
	@Description("PMP-3 - User chooses to download the list of parties")
	@Link(name = "EDELIVERY-5325", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5325")
	@AllureId("PMP-3")
	@Test(description = "PMP-3", groups = {"multiTenancy", "singleTenancy"})
	public void downloadFilteredPartiesList() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Navigate to Pmode parties page");
		log.info("Navigate to Pmode parties page");
		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		String partyName = page.grid().getRowInfo(0).get("Party Name");

		Allure.step("Search for party with id " + partyName);
		log.info("Search for party with id " + partyName);
		page.filters().getNameInput().fill(partyName);
		Allure.step("Click on search button");
		log.info("Click on search button");
		page.filters().getSearchButton().click();

		page.grid().waitForRowsToLoad();

		String filename = page.pressSaveCsvAndSaveFile();

		page.grid().checkCSVvsGridInfo(filename, soft);

		soft.assertAll();
	}

	/*  PMP-16 - Download all lists of records  */
	@Description("PMP-16 - Download all lists of records")
	@Link(name = "EDELIVERY-5338", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5338")
	@AllureId("PMP-16")
	@Test(description = "PMP-16", groups = {"multiTenancy", "singleTenancy"})
	public void downloadAllParties() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Navigate to Pmode parties page");
		log.info("Navigate to Pmode parties page");
		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		page.grid().waitForRowsToLoad();

		String filename = page.pressSaveCsvAndSaveFile();

		page.grid().checkCSVvsGridInfo(filename, soft);

		soft.assertAll();
	}

	/* PMP-8 User makes changes and Saves them*/
	/*  PMP-8 - User makes changes and Saves them  */
	@Description("PMP-8 - User makes changes and Saves them")
	@Link(name = "EDELIVERY-5330", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5330")
	@AllureId("PMP-8")
	@Test(description = "PMP-8", groups = {"multiTenancy", "singleTenancy"})
	public void updateParty() throws Exception {

		SoftAssert soft = new SoftAssert();

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		page.grid().waitForRowsToLoad();
		page.grid().selectRow(0);
		page.getEditButton().click();
		PartyModal pm = new PartyModal(driver);
		String newPartyName = "domibus_new_party";
		pm.getNameInput().fill(newPartyName);
		pm.getOkBtn().click();
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PMODE_PARTIES_UPDATE_SUCCESS), "update is done successfully");
		soft.assertTrue(page.grid().getRowSpecificColumnVal(0, "Party Name").contains(newPartyName), "Party name is updated successfully");
		soft.assertAll();

	}

	/* PMP-19 User single click on row on Pmode-Parties*/
	/*  PMP-19 - User single click on row on Pmode-Parties  */
	@Description("PMP-19 - User single click on row on Pmode-Parties")
	@Link(name = "EDELIVERY-5341", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5341")
	@AllureId("PMP-19")
	@Test(description = "PMP-19", groups = {"multiTenancy", "singleTenancy"})
	public void singleClick() throws Exception {

		SoftAssert soft = new SoftAssert();

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		page.grid().waitForRowsToLoad();
		if (page.grid().getPagination().getTotalItems() > 0) {
			page.grid().selectRow(0);
			soft.assertTrue(page.getEditButton().isEnabled(), "Edit button gets enabled");
			soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button gets enabled");
			soft.assertTrue(page.grid().gridRows.get(0).getAttribute("class").contains("active"), "Row gets highlighted");
		} else {
			throw new SkipException("No Parties for selection");
		}
		soft.assertAll();
	}


	/* EDELIVERY-5333 - PMP-11 - Click Show columns link on Pmode Archive/Parties */
	/*  PMP-11 - Click Show columns link on Pmode ArchiveParties  */
	@Description("PMP-11 - Click Show columns link on Pmode ArchiveParties")
	@Link(name = "EDELIVERY-5333", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5333")
	@AllureId("PMP-11")
	@Test(description = "PMP-11", groups = {"multiTenancy", "singleTenancy"})
	public void checkShowLnk() throws Exception {

		SoftAssert soft = new SoftAssert();

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		page.grid().waitForRowsToLoad();

		page.grid().checkShowLink(soft);


		soft.assertAll();
	}

	/* EDELIVERY-5334  - PMP-12 - Check/Uncheck of fields on Show links */
	/*  PMP-12 - CheckUncheck of fields on Show links  */
	@Description("PMP-12 - CheckUncheck of fields on Show links")
	@Link(name = "EDELIVERY-5334", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5334")
	@AllureId("PMP-12")
	@Test(description = "PMP-12", groups = {"multiTenancy", "singleTenancy"})
	public void showHideOfFields() throws Exception {

		SoftAssert soft = new SoftAssert();

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		page.grid().waitForRowsToLoad();

		page.grid().checkModifyVisibleColumns(soft);

		soft.assertAll();
	}

	/* EDELIVERY-5335 - PMP-13 - Click Hide link without any new selection */
	/*  PMP-13 - Click Hide link without any new selection  */
	@Description("PMP-13 - Click Hide link without any new selection")
	@Link(name = "EDELIVERY-5335", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5335")
	@AllureId("PMP-13")
	@Test(description = "PMP-13", groups = {"multiTenancy", "singleTenancy"})
	public void showHideNoNewSelection() throws Exception {

		SoftAssert soft = new SoftAssert();

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		DGrid grid = page.grid();

		grid.waitForRowsToLoad();
		grid.checkShowLink(soft);
		grid.checkHideLink(soft);

		soft.assertAll();
	}

	/* EDELIVERY-5337 - PMP-15 - Click All None link */
	/*  PMP-15 - Click All None link  */
	@Description("PMP-15 - Click All None link")
	@Link(name = "EDELIVERY-5337", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5337")
	@AllureId("PMP-15")
	@Test(description = "PMP-15", groups = {"multiTenancy", "singleTenancy"})
	public void checkAllNoneLnk() throws Exception {

		SoftAssert soft = new SoftAssert();

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		DGrid grid = page.grid();

		grid.waitForRowsToLoad();
		grid.checkAllLink(soft);
		grid.checkNoneLink(soft);

		soft.assertAll();
	}

	/* EDELIVERY-5339 - PMP-17 - Change Rows field data */
	/*  PMP-17 - Change Rows field data  */
	@Description("PMP-17 - Change Rows field data")
	@Link(name = "EDELIVERY-5339", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5339")
	@AllureId("PMP-17")
	@Test(description = "PMP-17", groups = {"multiTenancy", "singleTenancy"})
	public void checkRowsNo() throws Exception {

		SoftAssert soft = new SoftAssert();

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		DGrid grid = page.grid();

		grid.waitForRowsToLoad();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}


}
