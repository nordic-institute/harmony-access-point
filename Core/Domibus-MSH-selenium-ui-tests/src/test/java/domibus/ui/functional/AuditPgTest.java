package domibus.ui.functional;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Audit.AuditPage;
import pages.jms.JMSMonitoringPage;
import pages.jms.JMSMoveMessageModal;
import pages.pmode.current.PModeArchivePage;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;
import pages.pmode.parties.PModePartiesPage;
import pages.pmode.parties.PartyModal;
import utils.Gen;
import utils.TestUtils;

import java.util.*;


public class AuditPgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.AUDIT);

	private AuditPage navigateToAudit() throws Exception {

		DomibusPage page = new DomibusPage(driver);
		page.waitForPageTitle();

		if (page.getTitle().contains("Audit")) {
			Reporter.log("Was on Audit page thus refreshing");
			log.info("Was on Audit page thus refreshing");
			page.refreshPage();
		} else {
			page.getSidebar().goToPage(PAGES.AUDIT);
		}

		Reporter.log("waiting for rows to load");
		log.info("waiting for rows to load");
		AuditPage auditPage = new AuditPage(driver);
		auditPage.grid().waitForRowsToLoad();

		return auditPage;
	}


	/* EDELIVERY-5247 - AU-6 - Filter events so that there are no results */
	@Test(description = "AU-6", groups = {"multiTenancy", "singleTenancy"}, enabled = true)
	public void searchWithNoData() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Generate Random string for Username");
		log.info("Generate Random string for Username");
		String user = Gen.randomAlphaNumeric(10);
		Reporter.log("Create user with rest service");
		log.info("Create user with rest service");
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), null);

		Reporter.log("Login with admin user");
		log.info("Login with admin user");
		Reporter.log("Navigate to Audit page");
		log.info("Navigate to Audit page");

		AuditPage page = navigateToAudit();

		page.getFilters().setFilterData("table", "User");
		Reporter.log("Select logged in user username in User input filter");
		log.info("Select logged in user username in User input filter");
		page.getFilters().setFilterData("user", user);
		Reporter.log("Click on Search button");
		log.info("Click on Search button");
		page.getFilters().getSearchButton().click();

		page.grid().waitForRowsToLoad();

		Reporter.log("Search result count:" + page.getFilters().getPagination().getTotalItems());
		log.info("Search result count:" + page.getFilters().getPagination().getTotalItems());
		Reporter.log("Validate no data presence for this user on audit page");
		log.info("Validate no data presence for this user on audit page");
		soft.assertTrue(page.getFilters().getPagination().getTotalItems() == 0, "Search has no data");
		soft.assertAll();
	}

	/* EDELIVERY-5248 - AU-7 - Delete all criteria and press Search */
	@Test(description = "AU-7", groups = {"multiTenancy", "singleTenancy"}, enabled = true)
	public void deleteSearchCriteria() throws Exception {
		SoftAssert soft = new SoftAssert();

		String user = Gen.randomAlphaNumeric(10);
		Reporter.log("Create user with rest service");
		log.info("Create user with rest service");
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), null);


		AuditPage page = navigateToAudit();

		int prevCount = page.grid().getPagination().getTotalItems();
		Reporter.log("started out with items " + prevCount);
		log.info("started out with items " + prevCount);

		Reporter.log("Set Table filter data as User");
		log.info("Set Table filter data as User");
		page.filters().getTableFilter().selectOptionByText("User");

		Reporter.log("Set User filter data as created user");
		log.info("Set User filter data as created user");
		page.filters().getUserFilter().selectOptionByText(user);

		Reporter.log("Click on search button");
		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Reporter.log("Total search record is :" + page.grid().getPagination().getTotalItems());
		log.info("Total search record is :" + page.grid().getPagination().getTotalItems());
		page.refreshPage();
		page.waitForPageTitle();
		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.grid().getPagination().getTotalItems() == prevCount, "Page shows all records after deletion of all selected filter values");
		soft.assertAll();
	}


	/* EDELIVERY-5255 - AU-14 - Check Audit page for DOWNLOAD MESSAGE event */
	@Test(description = "AU-14", groups = {"multiTenancy", "singleTenancy"})
	public void messageDownloadedLog() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> ids = rest.getMessageIDsWithStatus(null, "SEND_FAILURE");

		if (ids.size() == 0) {
			throw new SkipException("No messages found");
		}

		String messID = ids.get(0);

		Reporter.log("Download message " + messID);
		log.info("Download message " + messID);
		rest.messages().downloadMessage(messID, null);


		AuditPage page = navigateToAudit();

		Reporter.log("Set Table data as Message");
		log.info("Set Table data as Message");
		page.filters().getTableFilter().selectOptionByText("Message");
		Reporter.log("Select Created as Action Field data");
		log.info("Select Created as Action Field data");
		page.filters().getActionFilter().selectOptionByText("Downloaded");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(page.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Reporter.log("Validate top record Action as Deleted");
		log.info("Validate top record Action as Deleted");
		boolean result = page.grid().getRowInfo(0).containsValue("Message")
				&& page.grid().getRowInfo(0).containsValue("Downloaded")
				&& page.grid().getRowInfo(0).containsValue(messID);
		soft.assertTrue(result, "Top row has Table value as Message, User value as Admin & Action as Downloaded ");
		soft.assertAll();
	}


	/* EDELIVERY-5256 - AU-15 - Check Audit page for Create NEW Message filter event  */
	@Test(description = "AU-15", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterCreation() throws Exception {
		SoftAssert soft = new SoftAssert();
		String rndStr = Gen.randomAlphaNumeric(5);

		Reporter.log("Create one message filter " + rndStr);
		log.info("Create one message filter " + rndStr);

		Long messageFilterID = rest.messFilters().createMessageFilter("backendWebservice", null, null, "action" + rndStr, "service:" + rndStr, null);

		Reporter.log("messageFilterID = " + messageFilterID);
		log.debug("messageFilterID = " + messageFilterID);

		if (messageFilterID < 0) {
			soft.fail("Could not identify message filter");
		}

		AuditPage page = navigateToAudit();

		Reporter.log("Set table filter as Message filter");
		log.info("Set table filter as Message filter");
		page.filters().getTableFilter().selectOptionByText("Message filter");

		Reporter.log("Click on search button");
		log.info("Click on search button");
		page.filters().clickSearch();
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate log presence on Audit page");
		log.info("Validate log presence on Audit page");
		int rowIndex = page.grid().scrollTo("Id", messageFilterID + "");

		if (rowIndex < 0) {
			soft.fail("Could not find event in audit table");
		}

		HashMap<String, String> info = page.grid().getRowInfo(rowIndex);

		soft.assertEquals(info.get("Action"), "Created", "Correct action is logged");
		soft.assertEquals(info.get("Id"), "" + messageFilterID, "Correct id is present on row");


		soft.assertAll();
	}

	/* EDELIVERY-5257 - AU-16 - Check Audit page for EDIT Message filter event */
	@Test(description = "AU-16", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterEdit() throws Exception {
		SoftAssert soft = new SoftAssert();
		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		List<Integer> ids = new ArrayList<>();
		for (int i = 0; i < msgfs.length(); i++) {
			JSONObject msgf = msgfs.getJSONObject(i);
			ids.add(msgf.getInt("entityId"));
		}


		rest.messFilters().saveMessageFilters(msgfs, null);

		Reporter.log("Navigate to Audit page");
		log.info("Navigate to Audit page");
		AuditPage page = navigateToAudit();

		Reporter.log("Set all data in search filters");
		log.info("Set all data in search filters");
		page.getFilters().setFilterData("table", "Message filter");

		Reporter.log("Click on search button");
		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		for (int i = 0; i < ids.size(); i++) {
			DGrid grid = page.grid();
			int index = grid.scrollTo("Id", ids.get(i) + "");
			if (index >= 0) {
				HashMap<String, String> info = grid.getRowInfo(index);
				soft.assertEquals(info.get("Action"), "Deleted", "Found Action " + info.get("Action"));
				soft.assertEquals(info.get("Id"), ids.get(i) + "", "Correct ID is desplayed");
			}
		}
		soft.assertAll();

	}


	/* EDELIVERY-5258 - AU-17 - Check Audit page for Move upMove Down Message Filter event */
	@Test(description = "AU-17", groups = {"multiTenancy", "singleTenancy"}, enabled = true)
	public void msgFilterMoveAction() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<Long> ids = new ArrayList<>();

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		JSONArray new_msgfs = new JSONArray();

		for (int i = 0; i < msgfs.length(); i++) {
			JSONObject msgf = msgfs.getJSONObject(msgfs.length() - i - 1);

			int currentIndex = msgf.getInt("index");
			int newIndex = msgfs.length() - currentIndex - 1;
			msgf.put("index", newIndex);

			ids.add(msgf.getLong("entityId"));

			msgf.put("index", newIndex);
			new_msgfs.put(msgf);
		}


		rest.messFilters().saveMessageFilters(new_msgfs, null);

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Select data in search filters");
		log.info("Select data in search filters");

		auditPage.getFilters().setFilterData("table", "Message filter");
		Reporter.log("Click on search button");
		log.info("Click on search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		for (int i = 0; i < ids.size(); i++) {
			int rowInd = auditPage.grid().scrollTo("Id", "" + ids.get(i));
			Map<String, String> info = auditPage.grid().getRowInfo(rowInd);
			soft.assertEquals(info.get("Action"), "Deleted");
		}

		soft.assertAll();

	}


	/* EDELIVERY-5260 - AU-19 - Check page for Text update of Pmode event */
	@Test(description = "AU-19", groups = {"multiTenancy", "singleTenancy"})
	public void txtUpdatePmode() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("Login into application with Admin credentials and navigate to Pmode current page");
		log.info("Login into application with Admin credentials and navigate to Pmode current page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
		Reporter.log("Upload pmode");
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		PModeCurrentPage pPage = new PModeCurrentPage(driver);
		Reporter.log("Extract data from current Pmode");
		log.info("Extract data from current Pmode");
		String beforeEditPmode = pPage.getTextArea().getText();
		Reporter.log("Modify some text");
		log.info("Modify some text");
		String afterEditPmode = beforeEditPmode.replaceAll("\\t", " ").replaceAll("localhost", "mockhost");
		Reporter.log("Fill pmode current area with updated pmode text");
		log.info("Fill pmode current area with updated pmode text");
		pPage.getTextArea().fill(afterEditPmode);
		Reporter.log("Click on save button");
		log.info("Click on save button");
		pPage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		Reporter.log("Enter description");
		log.info("Enter description");
		modal.getDescriptionTextArea().fill("Valid Modification");
		Reporter.log("Click on ok button");
		log.info("Click on ok button");
		modal.clickOK();

		Reporter.log(pPage.getAlertArea().getAlertMessage());
		log.info(pPage.getAlertArea().getAlertMessage());

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Select Pmode as Table field data");
		log.info("Select Pmode as Table field data");
		auditPage.getFilters().setFilterData("table", "Pmode");
		Reporter.log("click on search button");
		log.info("click on search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		Reporter.log("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/* EDELIVERY-5261 - AU-20 - Check page for PMode Upload event */
	@Test(description = "AU-20", groups = {"multiTenancy", "singleTenancy"})
	public void pmodeUpload() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("Upload pmode");
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		Reporter.log("Login into application with Admin credentials and navigate to Audit page");
		log.info("Login into application with Admin credentials and navigate to Audit page");

		AuditPage page = navigateToAudit();

		Reporter.log("Select Pmode as Table field data");
		log.info("Select Pmode as Table field data");
		page.getFilters().setFilterData("table", "Pmode");
		Reporter.log("click on search button");
		log.info("click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();
		Reporter.log("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(page.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(page.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/* EDELIVERY-5263 - AU-22 - Check page for Create Party as ADMIN event */
	@Test(description = "AU-22", groups = {"multiTenancy", "singleTenancy"})
	public void createParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Upload pmode");
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/pmode-red.xml", null);
		String newPartyName = Gen.randomAlphaNumeric(5);

		Reporter.log("login into application and navigate to Pmode parties page");
		log.info("login into application and navigate to Pmode parties page");
		PModePartiesPage pPage = new PModePartiesPage(driver);
		pPage.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		pPage.grid().waitForRowsToLoad();

		Reporter.log("Click on New button");
		log.info("Click on New button");
		pPage.getNewButton().click();
		PartyModal modal = new PartyModal(driver);
		Reporter.log("Fill new party info");
		log.info("Fill new party info");
		modal.fillNewPartyForm(newPartyName, "http://test.com", "pid");

		Reporter.log("Click ok button");
		log.info("Click ok button");
		modal.clickOK();

		pPage.getSaveButton().click();
		new Dialog(driver).confirm();

		Reporter.log("validate presence of success message");
		log.info("validate presence of success message");
		soft.assertTrue(!pPage.getAlertArea().isError(), "page shows success message");

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Set all search filter data");
		log.info("Set all search filter data");
		auditPage.filters().getTableFilter().selectOptionByText("Pmode");

		Reporter.log("Click in search button");
		log.info("Click in search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Reporter.log("Validate data on Audit page");
		log.info("Validate data on Audit page");

		List<String> expectedActions = Arrays.asList("", "");

		soft.assertEquals(auditPage.grid().getRowInfo(0).get("Action"), "Created", "Proper action is logged on row 0");
		soft.assertEquals(auditPage.grid().getRowInfo(1).get("Action"), "Deleted", "Proper action is logged on row 1");

		soft.assertAll();
	}

	/* EDELIVERY-5264 - AU-23 - Check page for Edit parties as ADMIN event */
	@Test(description = "AU-23", groups = {"multiTenancy", "singleTenancy"})
	public void editParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("upload pmode");
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		rest.pmodeParties().updatePartyURL("blue_gw");

		AuditPage page = navigateToAudit();

		Reporter.log("Set all search filter data");
		log.info("Set all search filter data");
		page.getFilters().setFilterData("table", "Pmode");
		Reporter.log("Click on search button");
		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();
		Reporter.log("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(page.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(page.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/* EDELIVERY-5265 - AU-24 - Check page for Delete parties as ADMIN event */
	@Test(description = "AU-24", groups = {"multiTenancy", "singleTenancy"})
	public void deleteParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("upload pmode");
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);

		String username = rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName");
		login(username, data.defaultPass());

		Reporter.log("Login and navigate to pmode parties page");
		log.info("Login and navigate to pmode parties page");
		PModePartiesPage pPage = new PModePartiesPage(driver);
		pPage.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		pPage.grid().scrollToAndSelect("Party Name", "orange_gw");
		pPage.getDeleteButton().click();
		pPage.getSaveButton().click();
		new Dialog(driver).confirm();
		Reporter.log("Message shown : " + pPage.getAlertArea().getAlertMessage());
		log.info("Message shown : " + pPage.getAlertArea().getAlertMessage());


		AuditPage auditPage = navigateToAudit();

		Reporter.log("Set all search filter data");
		log.info("Set all search filter data");
		auditPage.filters().getTableFilter().selectOptionByText("Pmode");
		auditPage.filters().clickSearch();
		auditPage.grid().waitForRowsToLoad();

		Reporter.log("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).containsValue("Created"), "Created action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).containsValue("Deleted"), "Deleted action is logged");

		soft.assertAll();
	}

	/* EDELIVERY-5266 - AU-25 - Check page for Download oldcurrent  PModes from Pmodes Archive as ADMIN event */
	@Test(description = "AU-25", groups = {"multiTenancy", "singleTenancy"})
	public void pmodeDownload() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		Reporter.log("getting pmode id");
		log.info("getting pmode id");
		String pmodeID = rest.pmode().getLatestPModeID(null);
		Reporter.log("downloading PMODE with id " + pmodeID);
		log.info("downloading PMODE with id " + pmodeID);
		String filename = rest.pmode().downloadPmodeFile(null, pmodeID);
		Reporter.log("downloaded file with name " + filename);
		log.info("downloaded file with name " + filename);

		AuditPage page = navigateToAudit();

		page.getFilters().getTableFilter().selectOptionByText("Pmode");
		page.getFilters().getActionFilter().selectOptionByText("Downloaded");

		Reporter.log("click on search button");
		log.info("click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate data on Audit page");
		log.info("Validate data on Audit page");

		int index = page.grid().scrollTo("Id", pmodeID.toString());
		if (index < 0) {
			soft.fail("event for desired pmode download not present");
		}

		HashMap<String, String> info = page.grid().getRowInfo(index);

		soft.assertEquals(info.get("Table"), "Pmode", "Table column has value Pmode");
		soft.assertEquals(info.get("Action"), "Downloaded", "Action column has value Downloaded");
		soft.assertEquals(info.get("Id"), String.valueOf(pmodeID), "Correct pmodeID listed");

		soft.assertAll();

	}

	/* EDELIVERY-5267 - AU-26 - Check page for Restore PMode from Pmode Archive event */
	@Test(description = "AU-26", groups = {"multiTenancy", "singleTenancy"})
	public void restorePmodeFromArchive() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("upload pmode");
		log.info("upload pmode");
		for (int i = rest.pmode().getPmodesList(null).length(); i < 3; i++) {
			rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		}

		Reporter.log("Login and navigate to pmode parties page");
		log.info("Login and navigate to pmode parties page");
		PModeArchivePage archivePage = new PModeArchivePage(driver);
		archivePage.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
		archivePage.grid().waitForRowsToLoad();


		Reporter.log("Select row with index 1");
		log.info("Select row with index 1");
		archivePage.grid().selectRow(1);

		Reporter.log("Click on restore button");
		log.info("Click on restore button");
		archivePage.getRestoreButton().click();

		Reporter.log("Click on save and then yes button on confirmation pop up");
		log.info("Click on save and then yes button on confirmation pop up");
		archivePage.getConfirmation().confirm();

		Reporter.log("Success message shown : " + archivePage.getAlertArea().getAlertMessage());
		log.info("Success message shown : " + archivePage.getAlertArea().getAlertMessage());

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Set all search filters");
		log.info("Set all search filters");
		auditPage.getFilters().setFilterData("table", "Pmode");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		Reporter.log("click on search button");
		log.info("click on search button");
		Reporter.log("Validate log presence on Audit page");
		log.info("Validate log presence on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/* EDELIVERY-5268 - AU-27 - Check page for Delete Pmode from Pmode Archive event */
	@Test(description = "AU-27", groups = {"multiTenancy", "singleTenancy"})
	public void deletePmodeFromArchive() throws Exception {
		Reporter.log("upload pmode");
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		SoftAssert soft = new SoftAssert();

		Reporter.log("Login and navigate to pmode archive page");
		log.info("Login and navigate to pmode archive page");
		PModeArchivePage archivePage = new PModeArchivePage(driver);
		archivePage.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
		archivePage.grid().waitForRowsToLoad();


		if (archivePage.grid().getRowsNo() == 1) {
			Reporter.log("Upload pmode");
			log.info("Upload pmode");
			rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
			archivePage.refreshPage();
			archivePage.grid().waitForRowsToLoad();
		}

		Reporter.log("Select row with index 1");
		log.info("Select row with index 1");
		archivePage.grid().selectRow(1);

		Reporter.log("Click on delete button");
		log.info("Click on delete button");
		archivePage.getDeleteButton().click();
		Reporter.log("click on save button");
		log.info("click on save button");
		archivePage.getSaveButton().click();
		Reporter.log("Click on yes button on confirmation pop up");
		log.info("Click on yes button on confirmation pop up");
		archivePage.getConfirmation().confirm();

		soft.assertTrue(!archivePage.getAlertArea().isError(), "Success message is shown");
		soft.assertEquals(archivePage.getAlertArea().getAlertMessage(), DMessages.PMODE_ARCHIVE_DELETE_SUCCESS, "Correct message is shown");

		archivePage.getSidebar().goToPage(PAGES.AUDIT);

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Set search filters");
		log.info("Set search filters");
		auditPage.getFilters().setFilterData("table", "Pmode Archive");

		Reporter.log("Click on search button");
		log.info("Click on search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Reporter.log("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).containsValue("Deleted"), "Delete action is logged");

		soft.assertAll();
	}

	/* EDELIVERY-5269 - AU-28 - Check page for New user event */
	@Test(description = "AU-28", groups = {"multiTenancy", "singleTenancy"})
	public void createUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Select User in Table input filter");
		log.info("Select User in Table input filter");
		auditPage.getFilters().setFilterData("table", "User");
		Reporter.log("Select Created as Action in filter");
		log.info("Select Created as Action in filter");
		auditPage.getFilters().setFilterData("Action", "Created");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Reporter.log("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Reporter.log("Validate top record Action as Created");
		log.info("Validate top record Action as Created");
		boolean result1 = auditPage.grid().getRowInfo(0).containsValue("Created");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("User");

		soft.assertTrue(result, "Top row shows Action as created ");
		soft.assertTrue(result1, "Top row has Table value as User");
		soft.assertAll();
	}

	/* EDELIVERY-5270 - AU-29 - Check page for Edit user event */
	@Test(description = "AU-29", groups = {"multiTenancy", "singleTenancy"})
	public void editUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("Login into application with Admin credentials and navigate to Audit page");
		log.info("Login into application with Admin credentials and navigate to Audit page");
		Reporter.log("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);
		HashMap<String, String> params = new HashMap<>();
		params.put("password", data.getNewTestPass());
		rest.users().updateUser(username, params, null);

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Select User in Table input filter");
		log.info("Select User in Table input filter");
		auditPage.getFilters().setFilterData("table", "User");
		Reporter.log("Select Created as Action in filter");
		log.info("Select Created as Action in filter");
		auditPage.getFilters().setFilterData("Action", "Modified");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		Reporter.log("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Reporter.log("Validate top record Action as Modified");
		log.info("Validate top record Action as Modified");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("User")
				&& auditPage.grid().getRowInfo(0).containsValue("Modified");
		soft.assertTrue(result, "Top row has Table value as User, User value as Admin & Action as Modified ");
		soft.assertAll();
	}


	/* EDELIVERY-5271 - AU-30 - Check page for Delete user event */
	@Test(description = "AU-30", groups = {"multiTenancy", "singleTenancy"})
	public void deleteUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);
		rest.users().deleteUser(username, null);

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Select User in Table input filter");
		log.info("Select User in Table input filter");
		auditPage.getFilters().setFilterData("table", "User");
		Reporter.log("Select Created as Action in filter");
		log.info("Select Created as Action in filter");
		auditPage.getFilters().setFilterData("Action", "Modified");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Reporter.log("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Reporter.log("Validate top record Action as Modified");
		log.info("Validate top record Action as Modified");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("User")
				&& auditPage.grid().getRowInfo(0).containsValue("Modified");
		soft.assertTrue(result, "Top row has Table value as User, User value as Admin & Action as Modified ");
		soft.assertAll();
	}


	/* EDELIVERY-5281 - AU-39 - Check data for New Basic Plugin User event */
	@Test(description = "AU-39", groups = {"multiTenancy", "singleTenancy"})
	public void createPluginUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);
		rest.pluginUsers().createPluginUser(username, DRoles.ADMIN, data.defaultPass(), null);

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Select PluginUser as Table field data");
		log.info("Select PluginUser as Table field data");
		auditPage.getFilters().setFilterData("table", "PluginUser");
		Reporter.log("Select Created as Action Field data");
		log.info("Select Created as Action Field data");
		auditPage.getFilters().setFilterData("Action", "Created");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Reporter.log("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Reporter.log("Validate top record Action as Created");
		log.info("Validate top record Action as Created");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("PluginUser")
				&& auditPage.grid().getRowInfo(0).containsValue("Created");
		soft.assertTrue(result, "Top row has Table value as PluginUser, User value as Admin & Action as created ");
		soft.assertAll();
	}


	/* EDELIVERY-5282 - AU-40 - Check data for Delete Basic Plugin User event */
	@Test(description = "AU-40", groups = {"multiTenancy", "singleTenancy"})
	public void deletePluginUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);

		rest.pluginUsers().createPluginUser(username, DRoles.ADMIN, data.defaultPass(), null);
		rest.pluginUsers().deletePluginUser(username, null);

		AuditPage auditPage = navigateToAudit();

		Reporter.log("Select PluginUser as Table field data");
		log.info("Select PluginUser as Table field data");
		auditPage.getFilters().setFilterData("table", "PluginUser");
		Reporter.log("Select Created as Action Field data");
		log.info("Select Created as Action Field data");
		auditPage.getFilters().setFilterData("Action", "Deleted");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Reporter.log("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Reporter.log("Validate top record Action as Deleted");
		log.info("Validate top record Action as Deleted");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("PluginUser")
				&& auditPage.grid().getRowInfo(0).containsValue("Deleted");
		soft.assertTrue(result, "Top row has Table value as PluginUser, User value as Admin & Action as Deleted ");
		soft.assertAll();
	}

	/* EDELIVERY-5259 - AU-18 - Check Audit page for Delete Message Filter event */
	@Test(description = "AU-18", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterDeletion() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		String rndStr = Gen.randomAlphaNumeric(5);
		Reporter.log("Create one message filter");
		log.info("Create one message filter");

		Long messageFilterID = rest.messFilters().createMessageFilter("backendWebservice", null, null, "action" + rndStr, "service:" + rndStr, domain);
		Reporter.log("Created filter with id " + messageFilterID);
		log.info("Created filter with id " + messageFilterID);
		rest.messFilters().deleteMessageFilter("action" + rndStr, domain);


		Reporter.log("Navigate to Audit page");
		log.info("Navigate to Audit page");
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		Reporter.log("Set all search filter");
		log.info("Set all search filter");
		page.getFilters().getTableFilter().selectOptionByText("Message filter");
		page.getFilters().getActionFilter().selectOptionByText("Deleted");

		Reporter.log("Click on search button");
		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate presence of log on Audit page");
		log.info("Validate presence of log on Audit page");
		soft.assertTrue(page.grid().scrollTo("Id", String.valueOf(messageFilterID)) > -1, "Delete event identified");

		soft.assertAll();
	}

	/* EDELIVERY-5249 - AU-8 - Change current domain */
	@Test(description = "AU-8", groups = {"multiTenancy"})
	public void changeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		Reporter.log("Check total number of records for default domain");
		log.info("Check total number of records for default domain");
		int defaultDomainGridCount = page.grid().getPagination().getTotalItems();

		Reporter.log("Extract row info ");
		log.info("Extract row info ");
		ArrayList<HashMap<String, String>> defaultDomainData = page.grid().getListedRowInfo();

		Reporter.log("Change domain");
		log.info("Change domain");
		page.getDomainSelector().selectOptionByIndex(1);
		page.grid().waitForRowsToLoad();

		Reporter.log("Extract total number of items for second domain");
		log.info("Extract total number of items for second domain");
		int secondGridCount = page.grid().getPagination().getTotalItems();

		Reporter.log("Extract  row infos");
		log.info("Extract  row infos");
		ArrayList<HashMap<String, String>> secDomainData = page.grid().getListedRowInfo();


		Reporter.log("Verify grid row data for both domains");
		log.info("Verify grid row data for both domains");
		if (defaultDomainGridCount == 0 && secondGridCount == 0) {
			Reporter.log("Both domains have no data on this page");
			log.info("Both domains have no data on this page");
			throw new SkipException("No data to verify");
		} else if (defaultDomainGridCount != secondGridCount) {
			Reporter.log("Both domains have different number of data");
			log.info("Both domains have different number of data");
		} else if (defaultDomainData == secDomainData) {
			Reporter.log("Both domains have same number of data but all are different");
			log.info("Both domains have same number of data but all are different");

			boolean same = true;

			for (int i = 0; i < defaultDomainData.size(); i++) {
				if (!TestUtils.areMapsEqual(defaultDomainData.get(i), secDomainData.get(i))) {
					same = false;
					break;
				}
			}

			soft.assertFalse(!same, "Lists are not the same");

		} else {
			Reporter.log("Something went wrong on this page");
			log.info("Something went wrong on this page");
		}

		soft.assertAll();

	}

	/* EDELIVERY-5251 - AU-10 - Navigate to page 2 of events and change domain */
	@Test(description = "AU-10", groups = {"multiTenancy"})
	public void changeDomainFromSecPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		Reporter.log("Check if pagination is present");
		log.info("Check if pagination is present");
		if (!page.grid().getPagination().isPaginationPresent()) {
			Reporter.log("Default domain grid has data less than 10 so no pagination exists");
			log.info("Default domain grid has data less than 10 so no pagination exists");
		} else {
			Reporter.log("Navigate to page 2");
			log.info("Navigate to page 2");
			page.grid().getPagination().goToPage(2);
			soft.assertTrue(page.grid().getPagination().getActivePage() == 2, "User is on second page of Default domain");

			Reporter.log("Change domain");
			log.info("Change domain");
			page.getDomainSelector().selectOptionByIndex(1);
			page.grid().waitForRowsToLoad();

			Reporter.log("Check if pagination is present");
			log.info("Check if pagination is present");
			if (page.grid().getPagination().isPaginationPresent()) {
				Reporter.log("Pagination is present for second domain");
				log.info("Pagination is present for second domain");
				soft.assertEquals(page.grid().getPagination().getActivePage(), Integer.valueOf(1), "Pagination reset to first page");
			}
		}
		soft.assertAll();
	}

	/* EDELIVERY-5253 - AU-12 - Check Audit page for DELETE JMS Message event */
	@Test(description = "AU-12", groups = {"multiTenancy", "singleTenancy"})
	public void deleteJMSMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String queue = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(queue)) {
			throw new SkipException("No queue has messages");
		}

		String messageId = rest.jms().getQueueMessages(queue).getJSONObject(0).getString("id");
		Reporter.log("deleting message " + messageId);
		log.info("deleting message " + messageId);
		ClientResponse response = rest.jms().deleteMessages(queue, messageId);

		String strResponse = response.getEntity(String.class);
		Reporter.log("deleting message has returned: " + strResponse);
		log.info("deleting message has returned: " + strResponse);

		AuditPage page = new AuditPage(driver);

		Reporter.log("Navigate to Audit page");
		log.info("Navigate to Audit page");
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		page.filters().getTableFilter().selectOptionByText("Jms message");
		page.filters().getActionFilter().selectOptionByText("Deleted");
		page.filters().clickSearch();
		page.grid().waitForRowsToLoad();


		HashMap<String, String> info = page.grid().getRowInfo("Id", messageId);

		Reporter.log("Verify first row Action column data as Deleted");
		log.info("Verify first row Action column data as Deleted");
		soft.assertEquals(info.get("Action"), "Deleted", "Row contains Deleted action");

		Reporter.log("Verify first row Table column data as Jms Message");
		log.info("Verify first row Table column data as Jms Message");
		soft.assertEquals(info.get("Table"), "Jms message", "Table is Jms message");

		Reporter.log("Verify first row ID column data as ID shown for Message on Jms monitoring page");
		log.info("Verify first row ID column data as ID shown for Message on Jms monitoring page");
		soft.assertEquals(info.get("Id"), messageId, "Row contains jms message id");

		soft.assertAll();


	}


	/* EDELIVERY-5254 - AU-13 - Check Audit page for RESEND MESSAGE event */
	@Test(description = "AU-13", groups = {"multiTenancy", "singleTenancy"})
	public void msgResendLog() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> statuses = Arrays.asList(new String[]{"SEND_ENQUEUED", "SEND_FAILURE"});
		String domain = selectRandomDomain();

		String messId = null;

		JSONArray messages = rest.messages().getListOfMessages(domain);
		for (int i = 0; i < messages.length(); i++) {
			JSONObject mess = messages.getJSONObject(i);
			if (statuses.contains(mess.getString("messageStatus"))) {
				messId = mess.getString("messageId");
				rest.messages().resendMessage(messId, domain);
				break;
			}
		}

		if (null == messId) {
			throw new SkipException("No message with the correct status has been found");
		}

		AuditPage page = new AuditPage(driver);
		Reporter.log("Navigate to Audit page");
		log.info("Navigate to Audit page");
		page.getSidebar().goToPage(PAGES.AUDIT);

		Reporter.log("Wait for grid row to load");
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();

		page.filters().getTableFilter().selectOptionByText("Message");
		page.filters().getActionFilter().selectOptionByText("Resent");
		page.filters().clickSearch();
		page.grid().waitForRowsToLoad();

		HashMap<String, String> info = page.grid().getRowInfo("Id", messId);

		Reporter.log("Check ID as message id , Action as resent Table as Message and User as Super(for multitenancy) or Admin(for Singletenancy) log on audit page");
		log.info("Check ID as message id , Action as resent Table as Message and User as Super(for multitenancy) or Admin(for Singletenancy) log on audit page");
		soft.assertEquals(info.get("Id"), messId, "Row info contains message id");
		soft.assertEquals(info.get("Action"), "Resent", "Row info contain Resent action");
		soft.assertEquals(info.get("Table"), "Message", "Row info contains Message table name");


		soft.assertAll();
	}

	/* EDELIVERY-5262 - AU-21 - Check page for Download event from Pmode-current page */
	@Test(description = "AU-21", groups = {"multiTenancy", "singleTenancy"})
	public void currentPmodeDownloadLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		String domain = selectRandomDomain();

		String pmodeID = rest.pmode().getLatestPModeID(domain);
		rest.pmode().downloadPmode(domain, pmodeID);

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		page.filters().getTableFilter().selectOptionByText("Pmode");
		page.filters().getActionFilter().selectOptionByText("Downloaded");
		page.filters().clickSearch();
		page.grid().waitForRowsToLoad();

		HashMap<String, String> info = page.grid().getRowInfo("Id", String.valueOf(pmodeID));

		Reporter.log("Verify value for column table, action and user on audit page for first row");
		log.info("Verify value for column table, action and user on audit page for first row");
		soft.assertEquals(info.get("Table"), "Pmode", "verify table name as pmode");
		soft.assertEquals(info.get("Action"), "Downloaded", "verify action name as downloaded");
		soft.assertEquals(info.get("Id"), String.valueOf(pmodeID), "verify id is correct");

		soft.assertAll();

	}

	/* EDELIVERY-5252 - AU-11 - Check JMS MOVE message event on Audit page */
	@Test(description = "AU-11", groups = {"multiTenancy", "singleTenancy"})
	public void jmsMoveEvent() throws Exception {
		SoftAssert soft = new SoftAssert();
		String q = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(q)) {
			throw new SkipException("no queue has messages");
		}

		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		page.filters().getJmsQueueSelect().selectQueueWithMessages();
		page.grid().waitForRowsToLoad();

		String id = page.grid().getRowSpecificColumnVal(0, "ID");
		page.grid().selectRow(0);

		soft.assertTrue(page.moveButton.isEnabled(), "Move button is enabled");
		page.moveButton.click();
		JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
		modal.clickOK();
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.JMS_MOVE_MESSAGE_SUCCESS));
		page.getSidebar().goToPage(PAGES.AUDIT);
		AuditPage aPage = new AuditPage(driver);
		aPage.grid().waitForRowsToLoad();
		int index = aPage.grid().getIndexOf(4, id);
		soft.assertTrue(index >= 0, "Record is present in grid");
		soft.assertTrue(aPage.grid().getRowInfo(index).get("Id").equals(id), " same id is available on audit log page");

		soft.assertAll();

	}

}
