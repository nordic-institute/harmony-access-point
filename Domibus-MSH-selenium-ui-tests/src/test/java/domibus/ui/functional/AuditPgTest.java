package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import domibus.ui.SeleniumTest;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Audit.AuditPage;
import pages.pmode.current.PModeArchivePage;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;
import pages.pmode.parties.PModePartiesPage;
import pages.pmode.parties.PartyModal;
import utils.Generator;
import utils.TestUtils;

import java.util.HashMap;


public class AuditPgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.AUDIT);

	/*    AU-6 - Filter events so that there are no results   */
	@Test(description = "AU-6", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void searchWithNoData() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Generate Random string for Username");
		String user = Generator.randomAlphaNumeric(10);
		log.info("Create user with rest service");
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), null);

		log.info("Login with admin user");
		log.info("Navigate to Audit page");

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		page.getFilters().setFilterData("table", "User");
		log.info("Select logged in user username in User input filter");
		page.getFilters().setFilterData("user", user);
		log.info("Click on Search button");
		page.getFilters().getSearchButton().click();

		page.grid().waitForRowsToLoad();

		log.info("Search result count:" + page.getFilters().getPagination().getTotalItems());
		log.info("Validate no data presence for this user on audit page");
		soft.assertTrue(page.getFilters().getPagination().getTotalItems() == 0, "Search has no data");
		soft.assertAll();
	}

	/*   AU-7 - Delete all criteria and press Search    */
	@Test(description = "AU-7", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void deleteSearchCriteria() throws Exception {
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		log.info("Create user with rest service");
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), null);


		AuditPage page = new AuditPage(driver);
		log.info("Login into application with Admin credentials and navigate to Audit page");
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		int prevCount = page.grid().getPagination().getTotalItems();
		log.info("started out with items " + prevCount);

		log.info("Set Table filter data as User");
		page.filters().getTableFilter().selectOptionByText("User");

		log.info("Set User filter data as created user");
		page.filters().getUserFilter().selectOptionByText(user);

		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		log.info("Total search record is :" + page.grid().getPagination().getTotalItems());
		page.refreshPage();
		page.waitForPageTitle();
		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.grid().getPagination().getTotalItems() == prevCount, "Page shows all records after deletion of all selected filter values");
		soft.assertAll();
	}


	/*   AU-14 - Check Action On Audit page Grid data for Record created on Download action on Message page  */
	@Test(description = "AU-14", groups = {"multiTenancy", "singleTenancy"})
	public void messageDownloadedLog() throws Exception {
		SoftAssert soft = new SoftAssert();

		String messID = rest.getMessageIDsWithStatus(null, "SEND_FAILURE").get(0);
		log.info("Download message");
		rest.messages().downloadMessage(messID, null);


		AuditPage page = new AuditPage(driver);
		log.info("Login into application with Admin credentials and navigate to Audit page");
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		log.info("Set Table data as Message");
		page.filters().getTableFilter().selectOptionByText("Message");
		log.info("Select Created as Action Field data");
		page.filters().getActionFilter().selectOptionByText("Downloaded");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		log.info("Validate non zero Search result count ");
		soft.assertTrue(page.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		log.info("Validate top record Action as Deleted");
		boolean result = page.grid().getRowInfo(0).containsValue("Message")
				&& page.grid().getRowInfo(0).containsValue("Downloaded")
				&& page.grid().getRowInfo(0).containsValue(messID);
		soft.assertTrue(result, "Top row has Table value as Message, User value as Admin & Action as Downloaded ");
		soft.assertAll();
	}


	/*  AU-15 - Check action on Create event on Message filter  */
	@Test(description = "AU-15", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterCreation() throws Exception {
			SoftAssert soft = new SoftAssert();
			String actionName = Generator.randomAlphaNumeric(5);
			log.info("Create one message filter with action field value as :" + actionName);
			rest.messFilters().createMessageFilter(actionName, null);

			AuditPage page = new AuditPage(driver);

			page.getSidebar().goToPage(PAGES.AUDIT);
			page.grid().waitForRowsToLoad();

			log.info("Set table filter as Message filter");
			page.filters().getTableFilter().selectOptionByText("Message filter");

			log.info("Click on search button");
			page.filters().clickSearch();
			page.grid().waitForRowsToLoad();

			log.info("Validate log presence on Audit page");
			soft.assertTrue(page.grid().getRowInfo(0).containsValue("Created"), "Message filter action is logged successfully");
			soft.assertAll();
	}

	/*  AU-16 - Check action on edit  event on Message filter   */
	@Test(description = "AU-16", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterEdit() throws Exception {
		SoftAssert soft = new SoftAssert();
		JSONArray msgfs = rest.messFilters().getMessageFilters(null);

		JSONObject action = new JSONObject();
		action.put("entityId", "null");
		action.put("name", "action");

		JSONArray rcs = msgfs.getJSONObject(0).getJSONArray("routingCriterias");
		for (int i = 0; i < rcs.length(); i++) {
			JSONObject rc = rcs.getJSONObject(i);
			if(StringUtils.equalsIgnoreCase(rc.getString("name"),"action")){
				action = rc;
				rcs.remove(i);
			}
		}
		action.put("expression", Generator.randomAlphaNumeric(10));
		rcs.put(action);

		msgfs.getJSONObject(0).put("routingCriterias", rcs);

		rest.messFilters().saveMessageFilters(msgfs, null);

		log.info("Navigate to Audit page");
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "page is loaded successfully");
		log.info("Set all data in search filters");
		page.getFilters().setFilterData("table", "Message filter");
		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();
		soft.assertEquals(page.grid().getRowInfo(0).get("Action").trim(), "Modified", "Correct action is logged");
		soft.assertAll();

	}

	/* AU-17 - Check action on Move up/Move Down on Message Filter */
	@Test(description = "AU-17", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterMoveAction() throws Exception {
		SoftAssert soft = new SoftAssert();

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		JSONObject obj = msgfs.getJSONObject(0);
		msgfs.remove(0);
		msgfs.put(obj);

		rest.messFilters().saveMessageFilters(msgfs, null);

		AuditPage auditPage = new AuditPage(driver);
		auditPage.getSidebar().goToPage(PAGES.AUDIT);
		auditPage.grid().waitForRowsToLoad();

		log.info("Select data in search filters");

		auditPage.getFilters().setFilterData("table", "Message filter");
		log.info("Click on search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		log.info("Validate logs on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).containsValue("Modified"), "Correct action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).containsValue("Modified"), "Correct action is logged");
		soft.assertAll();

	}

	/*  AU-18 - Check action on Delete on Message Filter    */
	@Test(description = "AU-18", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterDeletion() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.messFilters().createMessageFilter(Generator.randomAlphaNumeric(10), null);

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);

		int msgfid = msgfs.getJSONObject(msgfs.length()-1).getInt("entityId");

		msgfs.remove(msgfs.length()-1);
		rest.messFilters().saveMessageFilters(msgfs, null);


		AuditPage page = new AuditPage(driver);
		log.info("Navigate to Audit page");
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "page is loaded successfully");

		log.info("Set all search filter");

		page.getFilters().getTableFilter().selectOptionByText("Message filter");
		page.getFilters().getActionFilter().selectOptionByText("Deleted");

		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		log.info("Validate presence of log on Audit page");

		HashMap<String, String> info = page.grid().getRowInfo(0);

		soft.assertEquals(info.get("Action"), "Deleted", "Correct action is logged");
		soft.assertEquals(info.get("Table"), "Message filter", "Correct table is logged");
		soft.assertEquals(info.get("Id"), msgfid+"", "Correct ID is logged");
		soft.assertAll();
	}

	/*  AU-19 - Check action on Text update on Pmode -current page  */
	@Test(description = "AU-19", groups = {"multiTenancy", "singleTenancy"})
	public void txtUpdatePmode() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login into application with Admin credentials and navigate to Pmode current page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		PModeCurrentPage pPage = new PModeCurrentPage(driver);
		log.info("Extract data from current Pmode");
		String beforeEditPmode = pPage.getTextArea().getText();
		log.info("Modify some text");
		String afterEditPmode = beforeEditPmode.replaceAll("\\t", " ").replaceAll("localhost", "mockhost");
		log.info("Fill pmode current area with updated pmode text");
		pPage.getTextArea().fill(afterEditPmode);
		log.info("Click on save button");
		pPage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		log.info("Enter description");
		modal.getDescriptionTextArea().fill("Valid Modification");
		log.info("Click on ok button");
		modal.clickOK();

		log.info(pPage.getAlertArea().getAlertMessage());

		log.info("Navigate to Audit page");
		pPage.getSidebar().goToPage(PAGES.AUDIT);

		AuditPage auditPage = new AuditPage(driver);
		auditPage.grid().waitForRowsToLoad();

		log.info("Select Pmode as Table field data");
		auditPage.getFilters().setFilterData("table", "Pmode");
		log.info("click on search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/*  AU-20 - Check action on Successful Upload on Pmode-current  */
	@Test(description = "AU-20", groups = {"multiTenancy", "singleTenancy"})
	public void pmodeUpload() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		log.info("Login into application with Admin credentials and navigate to Audit page");

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		log.info("Select Pmode as Table field data");
		page.getFilters().setFilterData("table", "Pmode");
		log.info("click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();
		log.info("Validate data on Audit page");
		soft.assertTrue(page.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(page.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/*  AU-22 - Login as domain admin, go to page Parties and Create parties    */
	@Test(description = "AU-22", groups = {"multiTenancy", "singleTenancy"})
	public void createParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
		String newPartyName = Generator.randomAlphaNumeric(5);

		log.info("login into application and navigate to Pmode parties page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage pPage = new PModePartiesPage(driver);

		log.info("Validate new button is enabled");
		soft.assertTrue(pPage.getNewButton().isEnabled(), "New button is enabled");

		log.info("Click on New button");
		pPage.getNewButton().click();
		PartyModal modal = new PartyModal(driver);
		log.info("Fill new party info");
		modal.fillNewPartyForm(newPartyName, "http://test.com", "pid");

		log.info("Click ok button");
		modal.clickOK();

		pPage.getSaveButton().click();
		new Dialog(driver).confirm();

		log.info("validate presence of success message");
		soft.assertTrue(!pPage.getAlertArea().isError(), "page shows success message");

		log.info("Go to Audit page");
		pPage.getSidebar().goToPage(PAGES.AUDIT);
		AuditPage auditPage = new AuditPage(driver);
		auditPage.grid().waitForRowsToLoad();

		log.info("Set all search filter data");
		auditPage.filters().getTableFilter().selectOptionByText("Pmode");

		log.info("Click in search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");

		soft.assertAll();
	}

	/*  AU-23 - Login as domain admin, go to page Parties and Edit parties  */
	@Test(description = "AU-23", groups = {"multiTenancy", "singleTenancy"})
	public void editParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		rest.pmodeParties().updatePartyURL("blue_gw");

		new DomibusPage(driver).getSidebar().goToPage(PAGES.AUDIT);
		AuditPage page = new AuditPage(driver);
		page.grid().waitForRowsToLoad();

		log.info("Set all search filter data");
		page.getFilters().setFilterData("table", "Pmode");
		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();
		log.info("Validate data on Audit page");
		soft.assertTrue(page.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(page.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/*  AU-24 - Login as domain admin, go to page Parties and Delete parties    */
	@Test(description = "AU-24", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void deleteParty() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
		SoftAssert soft = new SoftAssert();

		log.info("Login and navigate to pmode parties page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage pPage = new PModePartiesPage(driver);
		pPage.grid().scrollToAndSelect("Party Name", "red_gw");
		pPage.getDeleteButton().click();
		pPage.getSaveButton().click();
		new Dialog(driver).confirm();
		log.info("Message shown : " + pPage.getAlertArea().getAlertMessage());

		pPage.getSidebar().goToPage(PAGES.AUDIT);
		AuditPage auditPage = new AuditPage(driver);

		log.info("Set all search filter data");
		auditPage.filters().getTableFilter().selectOptionByText("Pmode");
		auditPage.filters().clickSearch();
		auditPage.grid().waitForRowsToLoad();

		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).containsValue("Created"), "Created action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).containsValue("Deleted"), "Deleted action is logged");

		soft.assertAll();
	}

	/*   AU-25 - Login as domain admin, go to page PMode Archive and Download old/current  PModes   */
	@Test(description = "AU-25", groups = {"multiTenancy", "singleTenancy"})
	public void pmodeDownload() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		log.info("getting pmode id");
		Integer pmodeID = rest.pmode().getLatestPModeID(null);
		log.info("downloading PMODE with id " + pmodeID);
		String filename = rest.pmode().downloadPmode(null, pmodeID);
		log.info("downloaded file with name " + filename);

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		page.getFilters().getTableFilter().selectOptionByText("Pmode");
		page.getFilters().getActionFilter().selectOptionByText("Downloaded");

		log.info("click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		log.info("Validate data on Audit page");

		int index = page.grid().scrollTo("Id", pmodeID.toString());
		if(index<0){
			soft.fail("event for desired pmode download not present");
		}

		HashMap<String, String> info = page.grid().getRowInfo(index);

		soft.assertEquals(info.get("Table"), "Pmode", "Table column has value Pmode");
		soft.assertEquals(info.get("Action"), "Downloaded", "Action column has value Downloaded");
		soft.assertEquals(info.get("Id"), String.valueOf(pmodeID), "Correct pmodeID listed");

		soft.assertAll();

	}

	/*  AU-26 - Login as domain admin, go to page PMode Archive and Restore  old  PModes    */
	@Test(description = "AU-26", groups = {"multiTenancy", "singleTenancy"})
	public void restorePmodeFromArchive() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("upload pmode");
		for (int i = rest.pmode().getPmodesList(null).length(); i < 3; i++) {
			rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		}

		log.info("Login and navigate to pmode parties page");
		PModeArchivePage archivePage = new PModeArchivePage(driver);
		archivePage.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
		archivePage.grid().waitForRowsToLoad();


		log.info("Select row with index 1");
		archivePage.grid().selectRow(1);

		log.info("Click on restore button");
		archivePage.getRestoreButton().click();

		log.info("Click on save and then yes button on confirmation pop up");
		archivePage.getConfirmation().confirm();

		log.info("Success message shown : " + archivePage.getAlertArea().getAlertMessage());
		archivePage.getSidebar().goToPage(PAGES.AUDIT);

		AuditPage auditPage = new AuditPage(driver);
		auditPage.waitForPageToLoad();
		auditPage.grid().waitForRowsToLoad();

		log.info("Set all search filters");
		auditPage.getFilters().setFilterData("table", "Pmode");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		log.info("click on search button");
		log.info("Validate log presence on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/*  AU-27 - Login as domain admin, go to page PMode Archive and Delete old PModes   */
	@Test(description = "AU-27", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void deletePmodeFromArchive() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		SoftAssert soft = new SoftAssert();

		log.info("Login and navigate to pmode archive page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_ARCHIVE);

		PModeArchivePage archivePage = new PModeArchivePage(driver);


		if (archivePage.grid().getRowsNo() == 1) {
			log.info("Upload pmode");
			rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
			archivePage.refreshPage();
			archivePage.grid().waitForRowsToLoad();
		}

		log.info("Select row with index 1");
		archivePage.grid().selectRow(1);

		log.info("Click on delete button");
		archivePage.getDeleteButton().click();
		log.info("click on save button");
		archivePage.getSaveButton().click();
		log.info("Click on yes button on confirmation pop up");
		archivePage.getConfirmation().confirm();

		log.info("Success message shown : " + archivePage.getAlertArea().getAlertMessage());
		archivePage.getSidebar().goToPage(PAGES.AUDIT);

		AuditPage auditPage = new AuditPage(driver);
		auditPage.waitForPageToLoad();
		auditPage.grid().waitForRowsToLoad();

		log.info("Set all search filters");
		auditPage.getFilters().setFilterData("table", "Pmode Archive");

		log.info("Click on search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).containsValue("Deleted"), "Delete action is logged");

		soft.assertAll();
	}

	/*    AU-28 - Check action on New user creation     */
	@Test(description = "AU-28", groups = {"multiTenancy", "singleTenancy"})
	public void createUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Create user with rest call");
		String username = Generator.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);
		AuditPage auditPage = new AuditPage(driver);
		auditPage.getSidebar().goToPage(PAGES.AUDIT);
		auditPage.grid().waitForRowsToLoad();

		log.info("Select User in Table input filter");
		auditPage.getFilters().setFilterData("table", "User");
		log.info("Select Created as Action in filter");
		auditPage.getFilters().setFilterData("Action", "Created");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		log.info("Validate top record Action as Created");
		boolean result1 = auditPage.grid().getRowInfo(0).containsValue("Created");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("User");

		soft.assertTrue(result, "Top row shows Action as created ");
		soft.assertTrue(result1, "Top row has Table value as User");
		soft.assertAll();
	}

	/*  AU-29 - Check action on Edit user event */
	@Test(description = "AU-29", groups = {"multiTenancy", "singleTenancy"})
	public void editUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login into application with Admin credentials and navigate to Audit page");
		log.info("Create user with rest call");
		String username = Generator.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);
		HashMap<String, String> params = new HashMap<>();
		params.put("password", data.getNewTestPass());
		rest.users().updateUser(username, params, null);

		AuditPage auditPage = new AuditPage(driver);
		auditPage.getSidebar().goToPage(PAGES.AUDIT);
		auditPage.grid().waitForRowsToLoad();

		log.info("Select User in Table input filter");
		auditPage.getFilters().setFilterData("table", "User");
		log.info("Select Created as Action in filter");
		auditPage.getFilters().setFilterData("Action", "Modified");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		log.info("Validate top record Action as Modified");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("User")
				&& auditPage.grid().getRowInfo(0).containsValue("Modified");
		soft.assertTrue(result, "Top row has Table value as User, User value as Admin & Action as Modified ");
		soft.assertAll();
	}


	/*  AU-30 - Check action on Delete user event   */
	@Test(description = "AU-30", groups = {"multiTenancy", "singleTenancy"})
	public void deleteUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Create user with rest call");
		String username = Generator.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);
		rest.users().deleteUser(username, null);

		AuditPage auditPage = new AuditPage(driver);
		log.info("Login into application with Admin credentials and navigate to Audit page");
		auditPage.getSidebar().goToPage(PAGES.AUDIT);
		auditPage.grid().waitForRowsToLoad();

		log.info("Select User in Table input filter");
		auditPage.getFilters().setFilterData("table", "User");
		log.info("Select Created as Action in filter");
		auditPage.getFilters().setFilterData("Action", "Modified");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		log.info("Validate top record Action as Modified");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("User")
				&& auditPage.grid().getRowInfo(0).containsValue("Modified");
		soft.assertTrue(result, "Top row has Table value as User, User value as Admin & Action as Modified ");
		soft.assertAll();
	}


	/*    AU-39 - Check action on plugin user creation event    */
	@Test(description = "AU-39", groups = {"multiTenancy", "singleTenancy"})
	public void createPluginUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Create user with rest call");
		String username = Generator.randomAlphaNumeric(10);
		rest.pluginUsers().createPluginUser(username, DRoles.ADMIN, data.defaultPass(), null);

		AuditPage auditPage = new AuditPage(driver);
		log.info("Login into application with Admin credentials and navigate to Audit page");
		auditPage.getSidebar().goToPage(PAGES.AUDIT);
		auditPage.grid().waitForRowsToLoad();

		log.info("Select PluginUser as Table field data");
		auditPage.getFilters().setFilterData("table", "PluginUser");
		log.info("Select Created as Action Field data");
		auditPage.getFilters().setFilterData("Action", "Created");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		log.info("Validate top record Action as Created");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("PluginUser")
				&& auditPage.grid().getRowInfo(0).containsValue("Created");
		soft.assertTrue(result, "Top row has Table value as PluginUser, User value as Admin & Action as created ");
		soft.assertAll();
	}


	/*   AU-40 - Check action on plugin user Delete event   */
	@Test(description = "AU-40", groups = {"multiTenancy", "singleTenancy"})
	public void deletePluginUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Create user with rest call");
		String username = Generator.randomAlphaNumeric(10);

		rest.pluginUsers().createPluginUser(username, DRoles.ADMIN, data.defaultPass(), null);
		rest.pluginUsers().deletePluginUser(username, null);
		AuditPage auditPage = new AuditPage(driver);
		log.info("Login into application with Admin credentials and navigate to Audit page");
		auditPage.getSidebar().goToPage(PAGES.AUDIT);
		auditPage.grid().waitForRowsToLoad();

		log.info("Select PluginUser as Table field data");
		auditPage.getFilters().setFilterData("table", "PluginUser");
		log.info("Select Created as Action Field data");
		auditPage.getFilters().setFilterData("Action", "Deleted");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		log.info("Validate top record Action as Deleted");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("PluginUser")
				&& auditPage.grid().getRowInfo(0).containsValue("Deleted");
		soft.assertTrue(result, "Top row has Table value as PluginUser, User value as Admin & Action as Deleted ");
		soft.assertAll();
	}


}