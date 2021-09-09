package domibus.ui.functional;

import io.qameta.allure.*;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.DWait;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Audit.AuditPage;
import pages.jms.JMSMonitoringPage;
import pages.jms.JMSMoveMessageModal;
import pages.msgFilter.MessageFilterPage;
import pages.pmode.current.PModeArchivePage;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;
import pages.pmode.parties.PModePartiesPage;
import pages.pmode.parties.PartyModal;
import utils.Gen;
import utils.TestUtils;

import java.util.*;


@Epic("Audit")
@Feature("Functional")
public class AuditPgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.AUDIT);

	private AuditPage navigateToAudit() throws Exception {

		DomibusPage page = new DomibusPage(driver);
		page.waitForPageTitle();

		if (page.getTitle().contains("Audit")) {
			Allure.step("Was on Audit page thus refreshing");
			log.info("Was on Audit page thus refreshing");
			page.refreshPage();
		} else {
			page.getSidebar().goToPage(PAGES.AUDIT);
		}

		Allure.step("waiting for rows to load");
		log.info("waiting for rows to load");
		AuditPage auditPage = new AuditPage(driver);
		auditPage.grid().waitForRowsToLoad();

		return auditPage;
	}


	/*    AU-6 - Filter events so that there are no results   */
	/*  AU-6 - Filter events so that there are no results  */
	@Description("AU-6 - Filter events so that there are no results")
	@Link(name = "EDELIVERY-5247", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5247")
	@AllureId("AU-6")
	@Test(description = "AU-6", groups = {"multiTenancy", "singleTenancy"}, enabled = true)
	public void searchWithNoData() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Generate Random string for Username");
		log.info("Generate Random string for Username");
		String user = Gen.randomAlphaNumeric(10);
		Allure.step("Create user with rest service");
		log.info("Create user with rest service");
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), null);

		Allure.step("Login with admin user");
		log.info("Login with admin user");
		Allure.step("Navigate to Audit page");
		log.info("Navigate to Audit page");

		AuditPage page = navigateToAudit();

		page.getFilters().setFilterData("table", "User");
		Allure.step("Select logged in user username in User input filter");
		log.info("Select logged in user username in User input filter");
		page.getFilters().setFilterData("user", user);
		Allure.step("Click on Search button");
		log.info("Click on Search button");
		page.getFilters().getSearchButton().click();

		page.grid().waitForRowsToLoad();

		Allure.step("Search result count:" + page.getFilters().getPagination().getTotalItems());
		log.info("Search result count:" + page.getFilters().getPagination().getTotalItems());
		Allure.step("Validate no data presence for this user on audit page");
		log.info("Validate no data presence for this user on audit page");
		soft.assertTrue(page.getFilters().getPagination().getTotalItems() == 0, "Search has no data");
		soft.assertAll();
	}

	/*   AU-7 - Delete all criteria and press Search    */
	/*  AU-7 - Delete all criteria and press Search  */
	@Description("AU-7 - Delete all criteria and press Search")
	@Link(name = "EDELIVERY-5248", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5248")
	@AllureId("AU-7")
	@Test(description = "AU-7", groups = {"multiTenancy", "singleTenancy"}, enabled = true)
	public void deleteSearchCriteria() throws Exception {
		SoftAssert soft = new SoftAssert();

		String user = Gen.randomAlphaNumeric(10);
		Allure.step("Create user with rest service");
		log.info("Create user with rest service");
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), null);


		AuditPage page = navigateToAudit();

		int prevCount = page.grid().getPagination().getTotalItems();
		Allure.step("started out with items " + prevCount);
		log.info("started out with items " + prevCount);

		Allure.step("Set Table filter data as User");
		log.info("Set Table filter data as User");
		page.filters().getTableFilter().selectOptionByText("User");

		Allure.step("Set User filter data as created user");
		log.info("Set User filter data as created user");
		page.filters().getUserFilter().selectOptionByText(user);

		Allure.step("Click on search button");
		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Allure.step("Total search record is :" + page.grid().getPagination().getTotalItems());
		log.info("Total search record is :" + page.grid().getPagination().getTotalItems());
		page.refreshPage();
		page.waitForPageTitle();
		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.grid().getPagination().getTotalItems() == prevCount, "Page shows all records after deletion of all selected filter values");
		soft.assertAll();
	}


	/*   AU-14 - Check Action On Audit page Grid data for Record created on Download action on Message page  */
	/*  AU-14 - Check Audit page for DOWNLOAD MESSAGE event  */
	@Description("AU-14 - Check Audit page for DOWNLOAD MESSAGE event")
	@Link(name = "EDELIVERY-5255", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5255")
	@AllureId("AU-14")
	@Test(description = "AU-14", groups = {"multiTenancy", "singleTenancy"})
	public void messageDownloadedLog() throws Exception {
		SoftAssert soft = new SoftAssert();

		String messID = rest.getMessageIDsWithStatus(null, "SEND_FAILURE").get(0);
		Allure.step("Download message " + messID);
		log.info("Download message " + messID);
		rest.messages().downloadMessage(messID, null);


		AuditPage page = navigateToAudit();

		Allure.step("Set Table data as Message");
		log.info("Set Table data as Message");
		page.filters().getTableFilter().selectOptionByText("Message");
		Allure.step("Select Created as Action Field data");
		log.info("Select Created as Action Field data");
		page.filters().getActionFilter().selectOptionByText("Downloaded");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Allure.step("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(page.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Allure.step("Validate top record Action as Deleted");
		log.info("Validate top record Action as Deleted");
		boolean result = page.grid().getRowInfo(0).containsValue("Message")
				&& page.grid().getRowInfo(0).containsValue("Downloaded")
				&& page.grid().getRowInfo(0).containsValue(messID);
		soft.assertTrue(result, "Top row has Table value as Message, User value as Admin & Action as Downloaded ");
		soft.assertAll();
	}


	/*  AU-15 - Check action on Create event on Message filter  */
	/*  AU-15 - Check Audit page for Create NEW Message filter event   */
	@Description("AU-15 - Check Audit page for Create NEW Message filter event ")
	@Link(name = "EDELIVERY-5256", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5256")
	@AllureId("AU-15")
	@Test(description = "AU-15", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterCreation() throws Exception {
		SoftAssert soft = new SoftAssert();
		String rndStr = Gen.randomAlphaNumeric(5);

		Allure.step("Create one message filter " + rndStr);
		log.info("Create one message filter " + rndStr);

		Long messageFilterID = rest.messFilters().createMessageFilter("backendWebservice", null, null, "action" + rndStr, "service:" + rndStr, null);

		Allure.step("messageFilterID = " + messageFilterID);
		log.debug("messageFilterID = " + messageFilterID);

		if (messageFilterID < 0) {
			soft.fail("Could not identify message filter");
		}

		AuditPage page = navigateToAudit();

		Allure.step("Set table filter as Message filter");
		log.info("Set table filter as Message filter");
		page.filters().getTableFilter().selectOptionByText("Message filter");

		Allure.step("Click on search button");
		log.info("Click on search button");
		page.filters().clickSearch();
		page.grid().waitForRowsToLoad();

		Allure.step("Validate log presence on Audit page");
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

	/*  AU-16 - Check Audit page for EDIT Message filter event  */
	@Description("AU-16 - Check Audit page for EDIT Message filter event")
	@Link(name = "EDELIVERY-5257", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5257")
	@AllureId("AU-16")
	@Test(description = "AU-16", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterEdit() throws Exception {
		/*  AU-16 - Check action on edit  event on Message filter   */
		SoftAssert soft = new SoftAssert();
		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		List<Integer> ids = new ArrayList<>();
		for (int i = 0; i < msgfs.length(); i++) {
			JSONObject msgf = msgfs.getJSONObject(i);
			ids.add(msgf.getInt("entityId"));
		}


		rest.messFilters().saveMessageFilters(msgfs, null);

		Allure.step("Navigate to Audit page");
		log.info("Navigate to Audit page");
		AuditPage page = navigateToAudit();

		Allure.step("Set all data in search filters");
		log.info("Set all data in search filters");
		page.getFilters().setFilterData("table", "Message filter");

		Allure.step("Click on search button");
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


	/* AU-17 - Check action on Move up/Move Down on Message Filter */
	/*  AU-17 - Check Audit page for Move upMove Down Message Filter event  */
	@Description("AU-17 - Check Audit page for Move upMove Down Message Filter event")
	@Link(name = "EDELIVERY-5258", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5258")
	@AllureId("AU-17")
	@Test(description = "AU-17", groups = {"multiTenancy", "singleTenancy"}, enabled = true)
	public void msgFilterMoveAction() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<Integer> ids = new ArrayList<>();

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		JSONArray new_msgfs = new JSONArray();

		for (int i = 0; i < msgfs.length(); i++) {
			JSONObject msgf = msgfs.getJSONObject(msgfs.length() - i - 1);

			int currentIndex = msgf.getInt("index");
			int newIndex = msgfs.length() - currentIndex - 1;
			msgf.put("index", newIndex);

			ids.add(msgf.getInt("entityId"));

			msgf.put("index", newIndex);
			new_msgfs.put(msgf);
		}


		rest.messFilters().saveMessageFilters(new_msgfs, null);

		AuditPage auditPage = navigateToAudit();

		Allure.step("Select data in search filters");
		log.info("Select data in search filters");

		auditPage.getFilters().setFilterData("table", "Message filter");
		Allure.step("Click on search button");
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


	/*  AU-19 - Check action on Text update on Pmode -current page  */
	/*  AU-19 - Check page for Text update of Pmode event  */
	@Description("AU-19 - Check page for Text update of Pmode event")
	@Link(name = "EDELIVERY-5260", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5260")
	@AllureId("AU-19")
	@Test(description = "AU-19", groups = {"multiTenancy", "singleTenancy"})
	public void txtUpdatePmode() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("Login into application with Admin credentials and navigate to Pmode current page");
		log.info("Login into application with Admin credentials and navigate to Pmode current page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
		Allure.step("Upload pmode");
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		PModeCurrentPage pPage = new PModeCurrentPage(driver);
		Allure.step("Extract data from current Pmode");
		log.info("Extract data from current Pmode");
		String beforeEditPmode = pPage.getTextArea().getText();
		Allure.step("Modify some text");
		log.info("Modify some text");
		String afterEditPmode = beforeEditPmode.replaceAll("\\t", " ").replaceAll("localhost", "mockhost");
		Allure.step("Fill pmode current area with updated pmode text");
		log.info("Fill pmode current area with updated pmode text");
		pPage.getTextArea().fill(afterEditPmode);
		Allure.step("Click on save button");
		log.info("Click on save button");
		pPage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		Allure.step("Enter description");
		log.info("Enter description");
		modal.getDescriptionTextArea().fill("Valid Modification");
		Allure.step("Click on ok button");
		log.info("Click on ok button");
		modal.clickOK();

		Allure.step(pPage.getAlertArea().getAlertMessage());
		log.info(pPage.getAlertArea().getAlertMessage());

		AuditPage auditPage = navigateToAudit();

		Allure.step("Select Pmode as Table field data");
		log.info("Select Pmode as Table field data");
		auditPage.getFilters().setFilterData("table", "Pmode");
		Allure.step("click on search button");
		log.info("click on search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		Allure.step("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/*  AU-20 - Check action on Successful Upload on Pmode-current  */
	/*  AU-20 - Check page for PMode Upload event  */
	@Description("AU-20 - Check page for PMode Upload event")
	@Link(name = "EDELIVERY-5261", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5261")
	@AllureId("AU-20")
	@Test(description = "AU-20", groups = {"multiTenancy", "singleTenancy"})
	public void pmodeUpload() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("Upload pmode");
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		Allure.step("Login into application with Admin credentials and navigate to Audit page");
		log.info("Login into application with Admin credentials and navigate to Audit page");

		AuditPage page = navigateToAudit();

		Allure.step("Select Pmode as Table field data");
		log.info("Select Pmode as Table field data");
		page.getFilters().setFilterData("table", "Pmode");
		Allure.step("click on search button");
		log.info("click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();
		Allure.step("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(page.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(page.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/*  AU-22 - Login as domain admin, go to page Parties and Create parties    */
	/*  AU-22 - Check page for Create Party as ADMIN event  */
	@Description("AU-22 - Check page for Create Party as ADMIN event")
	@Link(name = "EDELIVERY-5263", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5263")
	@AllureId("AU-22")
	@Test(description = "AU-22", groups = {"multiTenancy", "singleTenancy"})
	public void createParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Upload pmode");
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/pmode-red.xml", null);
		String newPartyName = Gen.randomAlphaNumeric(5);

		Allure.step("login into application and navigate to Pmode parties page");
		log.info("login into application and navigate to Pmode parties page");
		PModePartiesPage pPage = new PModePartiesPage(driver);
		pPage.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		pPage.grid().waitForRowsToLoad();

		Allure.step("Click on New button");
		log.info("Click on New button");
		pPage.getNewButton().click();
		PartyModal modal = new PartyModal(driver);
		Allure.step("Fill new party info");
		log.info("Fill new party info");
		modal.fillNewPartyForm(newPartyName, "http://test.com", "pid");

		Allure.step("Click ok button");
		log.info("Click ok button");
		modal.clickOK();

		pPage.getSaveButton().click();
		new Dialog(driver).confirm();

		Allure.step("validate presence of success message");
		log.info("validate presence of success message");
		soft.assertTrue(!pPage.getAlertArea().isError(), "page shows success message");

		AuditPage auditPage = navigateToAudit();

		Allure.step("Set all search filter data");
		log.info("Set all search filter data");
		auditPage.filters().getTableFilter().selectOptionByText("Pmode");

		Allure.step("Click in search button");
		log.info("Click in search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Allure.step("Validate data on Audit page");
		log.info("Validate data on Audit page");

		List<String> expectedActions = Arrays.asList("", "");

		soft.assertEquals(auditPage.grid().getRowInfo(0).get("Action"), "Created", "Proper action is logged on row 0");
		soft.assertEquals(auditPage.grid().getRowInfo(1).get("Action"), "Deleted", "Proper action is logged on row 1");

		soft.assertAll();
	}

	/*  AU-23 - Login as domain admin, go to page Parties and Edit parties  */
	/*  AU-23 - Check page for Edit parties as ADMIN event  */
	@Description("AU-23 - Check page for Edit parties as ADMIN event")
	@Link(name = "EDELIVERY-5264", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5264")
	@AllureId("AU-23")
	@Test(description = "AU-23", groups = {"multiTenancy", "singleTenancy"})
	public void editParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("upload pmode");
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		rest.pmodeParties().updatePartyURL("blue_gw");

		AuditPage page = navigateToAudit();

		Allure.step("Set all search filter data");
		log.info("Set all search filter data");
		page.getFilters().setFilterData("table", "Pmode");
		Allure.step("Click on search button");
		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();
		Allure.step("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(page.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(page.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/*  AU-24 - Login as domain admin, go to page Parties and Delete parties    */
	/*  AU-24 - Check page for Delete parties as ADMIN event  */
	@Description("AU-24 - Check page for Delete parties as ADMIN event")
	@Link(name = "EDELIVERY-5265", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5265")
	@AllureId("AU-24")
	@Test(description = "AU-24", groups = {"multiTenancy", "singleTenancy"})
	public void deleteParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("upload pmode");
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);

		String username = rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName");
		login(username, data.defaultPass());

		Allure.step("Login and navigate to pmode parties page");
		log.info("Login and navigate to pmode parties page");
		PModePartiesPage pPage = new PModePartiesPage(driver);
		pPage.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		pPage.grid().scrollToAndSelect("Party Name", "orange_gw");
		pPage.getDeleteButton().click();
		pPage.getSaveButton().click();
		new Dialog(driver).confirm();
		Allure.step("Message shown : " + pPage.getAlertArea().getAlertMessage());
		log.info("Message shown : " + pPage.getAlertArea().getAlertMessage());


		AuditPage auditPage = navigateToAudit();

		Allure.step("Set all search filter data");
		log.info("Set all search filter data");
		auditPage.filters().getTableFilter().selectOptionByText("Pmode");
		auditPage.filters().clickSearch();
		auditPage.grid().waitForRowsToLoad();

		Allure.step("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).containsValue("Created"), "Created action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).containsValue("Deleted"), "Deleted action is logged");

		soft.assertAll();
	}

	/*   AU-25 - Login as domain admin, go to page PMode Archive and Download old/current  PModes   */
	/*  AU-25 - Check page for Download oldcurrent  PModes from Pmodes Archive as ADMIN event  */
	@Description("AU-25 - Check page for Download oldcurrent  PModes from Pmodes Archive as ADMIN event")
	@Link(name = "EDELIVERY-5266", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5266")
	@AllureId("AU-25")
	@Test(description = "AU-25", groups = {"multiTenancy", "singleTenancy"})
	public void pmodeDownload() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		Allure.step("getting pmode id");
		log.info("getting pmode id");
		String pmodeID = rest.pmode().getLatestPModeID(null);
		Allure.step("downloading PMODE with id " + pmodeID);
		log.info("downloading PMODE with id " + pmodeID);
		String filename = rest.pmode().downloadPmodeFile(null, pmodeID);
		Allure.step("downloaded file with name " + filename);
		log.info("downloaded file with name " + filename);

		AuditPage page = navigateToAudit();

		page.getFilters().getTableFilter().selectOptionByText("Pmode");
		page.getFilters().getActionFilter().selectOptionByText("Downloaded");

		Allure.step("click on search button");
		log.info("click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Allure.step("Validate data on Audit page");
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

	/*  AU-26 - Login as domain admin, go to page PMode Archive and Restore  old  PModes    */
	/*  AU-26 - Check page for Restore PMode from Pmode Archive event  */
	@Description("AU-26 - Check page for Restore PMode from Pmode Archive event")
	@Link(name = "EDELIVERY-5267", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5267")
	@AllureId("AU-26")
	@Test(description = "AU-26", groups = {"multiTenancy", "singleTenancy"})
	public void restorePmodeFromArchive() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("upload pmode");
		log.info("upload pmode");
		for (int i = rest.pmode().getPmodesList(null).length(); i < 3; i++) {
			rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		}

		Allure.step("Login and navigate to pmode parties page");
		log.info("Login and navigate to pmode parties page");
		PModeArchivePage archivePage = new PModeArchivePage(driver);
		archivePage.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
		archivePage.grid().waitForRowsToLoad();


		Allure.step("Select row with index 1");
		log.info("Select row with index 1");
		archivePage.grid().selectRow(1);

		Allure.step("Click on restore button");
		log.info("Click on restore button");
		archivePage.getRestoreButton().click();

		Allure.step("Click on save and then yes button on confirmation pop up");
		log.info("Click on save and then yes button on confirmation pop up");
		archivePage.getConfirmation().confirm();

		Allure.step("Success message shown : " + archivePage.getAlertArea().getAlertMessage());
		log.info("Success message shown : " + archivePage.getAlertArea().getAlertMessage());

		AuditPage auditPage = navigateToAudit();

		Allure.step("Set all search filters");
		log.info("Set all search filters");
		auditPage.getFilters().setFilterData("table", "Pmode");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		Allure.step("click on search button");
		log.info("click on search button");
		Allure.step("Validate log presence on Audit page");
		log.info("Validate log presence on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).get("Action") != null, "Proper action is logged");
		soft.assertTrue(auditPage.grid().getRowInfo(1).get("Action") != null, "Proper action is logged");
		soft.assertAll();
	}

	/*  AU-27 - Login as domain admin, go to page PMode Archive and Delete old PModes   */
	/*  AU-27 - Check page for Delete Pmode from Pmode Archive event  */
	@Description("AU-27 - Check page for Delete Pmode from Pmode Archive event")
	@Link(name = "EDELIVERY-5268", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5268")
	@AllureId("AU-27")
	@Test(description = "AU-27", groups = {"multiTenancy", "singleTenancy"})
	public void deletePmodeFromArchive() throws Exception {
		Allure.step("upload pmode");
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		SoftAssert soft = new SoftAssert();

		Allure.step("Login and navigate to pmode archive page");
		log.info("Login and navigate to pmode archive page");
		PModeArchivePage archivePage = new PModeArchivePage(driver);
		archivePage.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
		archivePage.grid().waitForRowsToLoad();


		if (archivePage.grid().getRowsNo() == 1) {
			Allure.step("Upload pmode");
			log.info("Upload pmode");
			rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
			archivePage.refreshPage();
			archivePage.grid().waitForRowsToLoad();
		}

		Allure.step("Select row with index 1");
		log.info("Select row with index 1");
		archivePage.grid().selectRow(1);

		Allure.step("Click on delete button");
		log.info("Click on delete button");
		archivePage.getDeleteButton().click();
		Allure.step("click on save button");
		log.info("click on save button");
		archivePage.getSaveButton().click();
		Allure.step("Click on yes button on confirmation pop up");
		log.info("Click on yes button on confirmation pop up");
		archivePage.getConfirmation().confirm();

		soft.assertTrue(!archivePage.getAlertArea().isError(), "Success message is shown");
		soft.assertEquals(archivePage.getAlertArea().getAlertMessage(), DMessages.PMODE_ARCHIVE_DELETE_SUCCESS, "Correct message is shown");

		archivePage.getSidebar().goToPage(PAGES.AUDIT);

		AuditPage auditPage = navigateToAudit();

		Allure.step("Set search filters");
		log.info("Set search filters");
		auditPage.getFilters().setFilterData("table", "Pmode Archive");

		Allure.step("Click on search button");
		log.info("Click on search button");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Allure.step("Validate data on Audit page");
		log.info("Validate data on Audit page");
		soft.assertTrue(auditPage.grid().getRowInfo(0).containsValue("Deleted"), "Delete action is logged");

		soft.assertAll();
	}

	/*    AU-28 - Check action on New user creation     */
	/*  AU-28 - Check page for New user event  */
	@Description("AU-28 - Check page for New user event")
	@Link(name = "EDELIVERY-5269", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5269")
	@AllureId("AU-28")
	@Test(description = "AU-28", groups = {"multiTenancy", "singleTenancy"})
	public void createUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);

		AuditPage auditPage = navigateToAudit();

		Allure.step("Select User in Table input filter");
		log.info("Select User in Table input filter");
		auditPage.getFilters().setFilterData("table", "User");
		Allure.step("Select Created as Action in filter");
		log.info("Select Created as Action in filter");
		auditPage.getFilters().setFilterData("Action", "Created");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Allure.step("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Allure.step("Validate top record Action as Created");
		log.info("Validate top record Action as Created");
		boolean result1 = auditPage.grid().getRowInfo(0).containsValue("Created");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("User");

		soft.assertTrue(result, "Top row shows Action as created ");
		soft.assertTrue(result1, "Top row has Table value as User");
		soft.assertAll();
	}

	/*  AU-29 - Check action on Edit user event */
	/*  AU-29 - Check page for Edit user event  */
	@Description("AU-29 - Check page for Edit user event")
	@Link(name = "EDELIVERY-5270", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5270")
	@AllureId("AU-29")
	@Test(description = "AU-29", groups = {"multiTenancy", "singleTenancy"})
	public void editUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("Login into application with Admin credentials and navigate to Audit page");
		log.info("Login into application with Admin credentials and navigate to Audit page");
		Allure.step("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);
		HashMap<String, String> params = new HashMap<>();
		params.put("password", data.getNewTestPass());
		rest.users().updateUser(username, params, null);

		AuditPage auditPage = navigateToAudit();

		Allure.step("Select User in Table input filter");
		log.info("Select User in Table input filter");
		auditPage.getFilters().setFilterData("table", "User");
		Allure.step("Select Created as Action in filter");
		log.info("Select Created as Action in filter");
		auditPage.getFilters().setFilterData("Action", "Modified");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();
		Allure.step("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Allure.step("Validate top record Action as Modified");
		log.info("Validate top record Action as Modified");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("User")
				&& auditPage.grid().getRowInfo(0).containsValue("Modified");
		soft.assertTrue(result, "Top row has Table value as User, User value as Admin & Action as Modified ");
		soft.assertAll();
	}


	/*  AU-30 - Check action on Delete user event   */
	/*  AU-30 - Check page for Delete user event  */
	@Description("AU-30 - Check page for Delete user event")
	@Link(name = "EDELIVERY-5271", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5271")
	@AllureId("AU-30")
	@Test(description = "AU-30", groups = {"multiTenancy", "singleTenancy"})
	public void deleteUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);
		rest.users().deleteUser(username, null);

		AuditPage auditPage = navigateToAudit();

		Allure.step("Select User in Table input filter");
		log.info("Select User in Table input filter");
		auditPage.getFilters().setFilterData("table", "User");
		Allure.step("Select Created as Action in filter");
		log.info("Select Created as Action in filter");
		auditPage.getFilters().setFilterData("Action", "Modified");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Allure.step("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Allure.step("Validate top record Action as Modified");
		log.info("Validate top record Action as Modified");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("User")
				&& auditPage.grid().getRowInfo(0).containsValue("Modified");
		soft.assertTrue(result, "Top row has Table value as User, User value as Admin & Action as Modified ");
		soft.assertAll();
	}


	/*    AU-39 - Check action on plugin user creation event    */
	/*  AU-39 - Check data for New Basic Plugin User event  */
	@Description("AU-39 - Check data for New Basic Plugin User event")
	@Link(name = "EDELIVERY-5281", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5281")
	@AllureId("AU-39")
	@Test(description = "AU-39", groups = {"multiTenancy", "singleTenancy"})
	public void createPluginUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);
		rest.pluginUsers().createPluginUser(username, DRoles.ADMIN, data.defaultPass(), null);

		AuditPage auditPage = navigateToAudit();

		Allure.step("Select PluginUser as Table field data");
		log.info("Select PluginUser as Table field data");
		auditPage.getFilters().setFilterData("table", "PluginUser");
		Allure.step("Select Created as Action Field data");
		log.info("Select Created as Action Field data");
		auditPage.getFilters().setFilterData("Action", "Created");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Allure.step("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Allure.step("Validate top record Action as Created");
		log.info("Validate top record Action as Created");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("PluginUser")
				&& auditPage.grid().getRowInfo(0).containsValue("Created");
		soft.assertTrue(result, "Top row has Table value as PluginUser, User value as Admin & Action as created ");
		soft.assertAll();
	}


	/*   AU-40 - Check action on plugin user Delete event   */
	/*  AU-40 - Check data for Delete Basic Plugin User event  */
	@Description("AU-40 - Check data for Delete Basic Plugin User event")
	@Link(name = "EDELIVERY-5282", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5282")
	@AllureId("AU-40")
	@Test(description = "AU-40", groups = {"multiTenancy", "singleTenancy"})
	public void deletePluginUserLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("Create user with rest call");
		log.info("Create user with rest call");
		String username = Gen.randomAlphaNumeric(10);

		rest.pluginUsers().createPluginUser(username, DRoles.ADMIN, data.defaultPass(), null);
		rest.pluginUsers().deletePluginUser(username, null);

		AuditPage auditPage = navigateToAudit();

		Allure.step("Select PluginUser as Table field data");
		log.info("Select PluginUser as Table field data");
		auditPage.getFilters().setFilterData("table", "PluginUser");
		Allure.step("Select Created as Action Field data");
		log.info("Select Created as Action Field data");
		auditPage.getFilters().setFilterData("Action", "Deleted");
		auditPage.getFilters().getSearchButton().click();
		auditPage.grid().waitForRowsToLoad();

		Allure.step("Validate non zero Search result count ");
		log.info("Validate non zero Search result count ");
		soft.assertTrue(auditPage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
		Allure.step("Validate top record Action as Deleted");
		log.info("Validate top record Action as Deleted");
		boolean result = auditPage.grid().getRowInfo(0).containsValue("PluginUser")
				&& auditPage.grid().getRowInfo(0).containsValue("Deleted");
		soft.assertTrue(result, "Top row has Table value as PluginUser, User value as Admin & Action as Deleted ");
		soft.assertAll();
	}

	/*  AU-18 - Check action on Delete on Message Filter    */
	/*  AU-18 - Check Audit page for Delete Message Filter event  */
	@Description("AU-18 - Check Audit page for Delete Message Filter event")
	@Link(name = "EDELIVERY-5259", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5259")
	@AllureId("AU-18")
	@Test(description = "AU-18", groups = {"multiTenancy", "singleTenancy"})
	public void msgFilterDeletion() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		String rndStr = Gen.randomAlphaNumeric(5);
		Allure.step("Create one message filter");
		log.info("Create one message filter");

		Long messageFilterID = rest.messFilters().createMessageFilter("backendWebservice", null, null, "action" + rndStr, "service:" + rndStr, domain);
		Allure.step("Created filter with id " + messageFilterID);
		log.info("Created filter with id " + messageFilterID);
		rest.messFilters().deleteMessageFilter("action" + rndStr, domain);


		Allure.step("Navigate to Audit page");
		log.info("Navigate to Audit page");
		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		Allure.step("Set all search filter");
		log.info("Set all search filter");
		page.getFilters().getTableFilter().selectOptionByText("Message filter");
		page.getFilters().getActionFilter().selectOptionByText("Deleted");

		Allure.step("Click on search button");
		log.info("Click on search button");
		page.getFilters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Allure.step("Validate presence of log on Audit page");
		log.info("Validate presence of log on Audit page");
		soft.assertTrue(page.grid().scrollTo("Id", String.valueOf(messageFilterID)) > -1, "Delete event identified");

		soft.assertAll();
	}

	/* AU-8 Verify data after changing domain*/
	/*  AU-8 - Change current domain  */
	@Description("AU-8 - Change current domain")
	@Link(name = "EDELIVERY-5249", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5249")
	@AllureId("AU-8")
	@Test(description = "AU-8", groups = {"multiTenancy"})
	public void changeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);

		Allure.step("Check total number of records for default domain");
		log.info("Check total number of records for default domain");
		int defaultDomainGridCount = page.grid().getPagination().getTotalItems();

		Allure.step("Extract row info ");
		log.info("Extract row info ");
		ArrayList<HashMap<String, String>> defaultDomainData = page.grid().getListedRowInfo();

		Allure.step("Change domain");
		log.info("Change domain");
		page.getDomainSelector().selectOptionByIndex(1);
		page.grid().waitForRowsToLoad();

		Allure.step("Extract total number of items for second domain");
		log.info("Extract total number of items for second domain");
		int secondGridCount = page.grid().getPagination().getTotalItems();

		Allure.step("Extract  row infos");
		log.info("Extract  row infos");
		ArrayList<HashMap<String, String>> secDomainData = page.grid().getListedRowInfo();


		Allure.step("Verify grid row data for both domains");
		log.info("Verify grid row data for both domains");
		if (defaultDomainGridCount == 0 && secondGridCount == 0) {
			Allure.step("Both domains have no data on this page");
			log.info("Both domains have no data on this page");
			throw new SkipException("No data to verify");
		} else if (defaultDomainGridCount != secondGridCount) {
			Allure.step("Both domains have different number of data");
			log.info("Both domains have different number of data");
		} else if (defaultDomainData == secDomainData) {
			Allure.step("Both domains have same number of data but all are different");
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
			Allure.step("Something went wrong on this page");
			log.info("Something went wrong on this page");
		}

		soft.assertAll();

	}

	/* Verify data and page number after changing domain from second page of default domain*/
	/*  AU-10 - Navigate to page 2 of events and change domain  */
	@Description("AU-10 - Navigate to page 2 of events and change domain")
	@Link(name = "EDELIVERY-5251", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5251")
	@AllureId("AU-10")
	@Test(description = "AU-10", groups = {"multiTenancy"})
	public void changeDomainFromSecPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		AuditPage page = new AuditPage(driver);
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		Allure.step("Check if pagination is present");
		log.info("Check if pagination is present");
		if (!page.grid().getPagination().isPaginationPresent()) {
			Allure.step("Default domain grid has data less than 10 so no pagination exists");
			log.info("Default domain grid has data less than 10 so no pagination exists");
		} else {
			Allure.step("Navigate to page 2");
			log.info("Navigate to page 2");
			page.grid().getPagination().goToPage(2);
			soft.assertTrue(page.grid().getPagination().getActivePage() == 2, "User is on second page of Default domain");

			Allure.step("Change domain");
			log.info("Change domain");
			page.getDomainSelector().selectOptionByIndex(1);
			page.grid().waitForRowsToLoad();

			Allure.step("Check if pagination is present");
			log.info("Check if pagination is present");
			if (page.grid().getPagination().isPaginationPresent()) {
				Allure.step("Pagination is present for second domain");
				log.info("Pagination is present for second domain");
				soft.assertEquals(page.grid().getPagination().getActivePage(), Integer.valueOf(1), "Pagination reset to first page");
			}
		}
		soft.assertAll();
	}

	/* AU-12 Check log presence on jms message deletion event*/
	/*  AU-12 - Check Audit page for DELETE JMS Message event  */
	@Description("AU-12 - Check Audit page for DELETE JMS Message event")
	@Link(name = "EDELIVERY-5253", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5253")
	@AllureId("AU-12")
	@Test(description = "AU-12", groups = {"multiTenancy", "singleTenancy"})
	public void deleteJMSMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String queue = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(queue)) {
			throw new SkipException("No queue has messages");
		}

		String messageId = rest.jms().getQueueMessages(queue).getJSONObject(0).getString("id");
		rest.jms().deleteMessages(queue, messageId);

		AuditPage page = new AuditPage(driver);
		page.wait.forXMillis(1000);

		Allure.step("Navigate to Audit page");
		log.info("Navigate to Audit page");
		page.getSidebar().goToPage(PAGES.AUDIT);
		page.grid().waitForRowsToLoad();

		page.filters().getTableFilter().selectOptionByText("Jms message");
		page.filters().getActionFilter().selectOptionByText("Deleted");
		page.filters().clickSearch();
		page.grid().waitForRowsToLoad();


		HashMap<String, String> info = page.grid().getRowInfo("Id", messageId);

		Allure.step("Verify first row Action column data as Deleted");
		log.info("Verify first row Action column data as Deleted");
		soft.assertEquals(info.get("Action"), "Deleted", "Row contains Deleted action");

		Allure.step("Verify first row Table column data as Jms Message");
		log.info("Verify first row Table column data as Jms Message");
		soft.assertEquals(info.get("Table"), "Jms message", "Table is Jms message");

		Allure.step("Verify first row ID column data as ID shown for Message on Jms monitoring page");
		log.info("Verify first row ID column data as ID shown for Message on Jms monitoring page");
		soft.assertEquals(info.get("Id"), messageId, "Row contains jms message id");

		soft.assertAll();


	}


	/* AU-13 Check log presence on Message resend  event*/
	/*  AU-13 - Check Audit page for RESEND MESSAGE event  */
	@Description("AU-13 - Check Audit page for RESEND MESSAGE event")
	@Link(name = "EDELIVERY-5254", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5254")
	@AllureId("AU-13")
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


		AuditPage page = new AuditPage(driver);
		Allure.step("Navigate to Audit page");
		log.info("Navigate to Audit page");
		page.getSidebar().goToPage(PAGES.AUDIT);

		Allure.step("Wait for grid row to load");
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();

		page.filters().getTableFilter().selectOptionByText("Message");
		page.filters().getActionFilter().selectOptionByText("Resent");
		page.filters().clickSearch();
		page.grid().waitForRowsToLoad();

		HashMap<String, String> info = page.grid().getRowInfo("Id", messId);

		Allure.step("Check ID as message id , Action as resent Table as Message and User as Super(for multitenancy) or Admin(for Singletenancy) log on audit page");
		log.info("Check ID as message id , Action as resent Table as Message and User as Super(for multitenancy) or Admin(for Singletenancy) log on audit page");
		soft.assertEquals(info.get("Id"), messId, "Row info contains message id");
		soft.assertEquals(info.get("Action"), "Resent", "Row info contain Resent action");
		soft.assertEquals(info.get("Table"), "Message", "Row info contains Message table name");


		soft.assertAll();
	}

	/* AU-21 Check log presence on Download event on Pmode Current page*/
	/*  AU-21 - Check page for Download event from Pmode-current page  */
	@Description("AU-21 - Check page for Download event from Pmode-current page")
	@Link(name = "EDELIVERY-5262", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5262")
	@AllureId("AU-21")
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

		Allure.step("Verify value for column table, action and user on audit page for first row");
		log.info("Verify value for column table, action and user on audit page for first row");
		soft.assertEquals(info.get("Table"), "Pmode", "verify table name as pmode");
		soft.assertEquals(info.get("Action"), "Downloaded", "verify action name as downloaded");
		soft.assertEquals(info.get("Id"), String.valueOf(pmodeID), "verify id is correct");

		soft.assertAll();

	}

	/*AU-11 - Check JMS MOVE message event on Audit page*/
	/*  AU-11 - Check JMS MOVE message event on Audit page  */
	@Description("AU-11 - Check JMS MOVE message event on Audit page")
	@Link(name = "EDELIVERY-5252", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5252")
	@AllureId("AU-11")
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
