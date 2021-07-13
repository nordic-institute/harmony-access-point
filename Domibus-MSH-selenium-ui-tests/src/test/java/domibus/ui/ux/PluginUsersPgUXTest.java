package domibus.ui.ux;

import io.qameta.allure.*;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.DButton;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.plugin_users.PluginUserModal;
import pages.plugin_users.PluginUsersPage;
import utils.Gen;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


@Epic("Plugin Users")
@Feature("UX")
public class PluginUsersPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PLUGIN_USERS);

	/*	PU-1 - Login as super admin and open Plugin Users page	*/
	/*  PU-1 - Login as super admin and open Plugin Users page  */
	@Description("PU-1 - Login as super admin and open Plugin Users page")
	@Link(name = "EDELIVERY-5211", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5211")
	@AllureId("PU-1")
	@Test(description = "PU-1", groups = {"multiTenancy", "singleTenancy"})
	public void openWindow() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

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

	/*	PU-2 - Doubleclick on one user	*/
	/*  PU-2 - Doubleclick on one user  */
	@Description("PU-2 - Doubleclick on one user")
	@Link(name = "EDELIVERY-5212", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5212")
	@AllureId("PU-2")
	@Test(description = "PU-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		if (page.grid().getRowsNo() == 0) {
			rest.getPluginUser(null, DRoles.USER, true, true);
			page.refreshPage();
		}

		Allure.step("getting user info");
		log.info("getting user info");
		HashMap<String, String> row = page.grid().getRowInfo(0);

		Allure.step("double click user");
		log.info("double click user");
		page.grid().doubleClickRow(0);

		PluginUserModal pum = new PluginUserModal(driver);

		Allure.step("checking user info");
		log.info("checking user info");
		soft.assertEquals(row.get("User Name"), pum.getUserNameInput().getText(), "Correct username is displayed");
		soft.assertEquals(row.get("Role"), pum.getRolesSelect().getSelectedValue(), "Correct role is displayed");
		soft.assertEquals(row.get("Original User"), pum.getOriginalUserInput().getText(), "Correct orig user is displayed");

		soft.assertAll();
	}

	/*	PU-12 - Admin changes password (also applies to user creation)	*/
	/*  PU-12 - Admin changes password also applies to user creation  */
	@Description("PU-12 - Admin changes password also applies to user creation")
	@Link(name = "EDELIVERY-5222", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5222")
	@AllureId("PU-12")
	@Test(description = "PU-12", groups = {"multiTenancy", "singleTenancy"})
	public void editPassErrMess() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Allure.step("testing for user " + username);
		log.info("testing for user " + username);

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.refreshPage();
		page.grid().waitForRowsToLoad();

		Allure.step("editing user");
		log.info("editing user");
		page.grid().scrollToAndSelect("User Name", username);
		page.getEditBtn().click();


		PluginUserModal pum = new PluginUserModal(driver);

		Allure.step("setting invalid password");
		log.info("setting invalid password");
		pum.getPasswordInput().fill("tst");
		String errMess = pum.getPassErrMess().getText();

		Allure.step("check error message");
		log.info("check error message");
		soft.assertTrue(errMess.contains(DMessages.PASS_POLICY_MESSAGE), "Password policy clearly displayed when wrong pass is entered");

		Allure.step("setting passord and confirmation not to match");
		log.info("setting passord and confirmation not to match");
		pum.getPasswordInput().fill(data.defaultPass());
		pum.getConfirmationInput().fill("lksjdlkfdskj");
		Allure.step("check error message");
		log.info("check error message");
		errMess = pum.getConfirmationErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_NO_MATCH_MESSAGE, "Password and confirmation should match");

		page.clickVoidSpace();

		soft.assertAll();
	}

	/*	PU-10 - Admin wants to edit username	*/
	/*  PU-10 - Admin wants to edit username  */
	@Description("PU-10 - Admin wants to edit username")
	@Link(name = "EDELIVERY-5220", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5220")
	@AllureId("PU-10")
	@Test(description = "PU-10", groups = {"multiTenancy", "singleTenancy"}, enabled = true)
	public void editUsername() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Allure.step("testing for user " + username);
		log.info("testing for user " + username);

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		Allure.step("opening edit modal");
		log.info("opening edit modal");
		page.grid().scrollToAndSelect("User Name", username);
		page.getEditBtn().click();

		PluginUserModal pum = new PluginUserModal(driver);
		Allure.step("check that username field is disabled");
		log.info("check that username field is disabled");
		soft.assertTrue(pum.getUserNameInput().isDisabled(), "Username is disabled");

		page.clickVoidSpace();

		soft.assertAll();

	}

	/*	PU-11 - Admin wants to edit certificate ID	*/
	/*  PU-11 - Admin wants to edit certificate ID  */
	@Description("PU-11 - Admin wants to edit certificate ID")
	@Link(name = "EDELIVERY-5221", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5221")
	@AllureId("PU-11")
	@Test(description = "PU-11", groups = {"multiTenancy", "singleTenancy"})
	public void editCertificateID() throws Exception {
		String certName = Gen.randomAlphaNumeric(5);
		String id = Gen.randomAlphaNumeric(5);
		String username = String.format("CN=%s,O=eDelivery,C=BE:%s", certName, id);
		rest.pluginUsers().createCertPluginUser(username, DRoles.USER, null);
		Allure.step("testing for user " + username);
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		Allure.step("changing to auth type CERTIFICATE");
		log.info("changing to auth type CERTIFICATE");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		Allure.step("opening edit modal");
		log.info("opening edit modal");
		page.grid().scrollToAndDoubleClick("Certificate Id", username);

		PluginUserModal pum = new PluginUserModal(driver);
		Allure.step("check that username field is disabled");
		log.info("check that username field is disabled");
		soft.assertTrue(pum.getUserNameInput().isDisabled(), "Username is disabled.");

		page.clickVoidSpace();

		soft.assertAll();
	}


	/*  PU-36 - Filter list of plugin users  */
	@Description("PU-36 - Filter list of plugin users")
	@Link(name = "EDELIVERY-7119", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7119")
	@AllureId("PU-36")
	@Test(description = "PU-36", groups = {"multiTenancy", "singleTenancy"})
	public void filterPluginUserList() throws Exception {
		List<String> usernames = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String username = Gen.randomAlphaNumeric(10);
			rest.pluginUsers().createPluginUser(username, DRoles.USER, data.defaultPass(), null);
			usernames.add(username);
		}

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		page.filters().search(null, null, null, usernames.get(0));
		soft.assertEquals(page.grid().getRowInfo(0).get("User Name"), usernames.get(0), "Search by username return correct result");
		soft.assertEquals(page.grid().getRowsNo(), 1, "Search by username return only one result");

		page.filters().getUsernameInput().clear();
		page.filters().search(null, DRoles.USER, null, null);
		DGrid grid = page.grid();
		for (int i = 0; i < grid.getRowsNo(); i++) {
			soft.assertEquals(grid.getRowInfo(i).get("Role"), DRoles.USER, "Result has correct role");
		}

		for (int i = 0; i < usernames.size(); i++) {
			rest.pluginUsers().deletePluginUser(usernames.get(i), null);
		}
		soft.assertAll();
	}

	/* PU-15 - Admin tries to create new user with username less than 3 letters long */
	/*  PU-15 - Admin tries to create new user with username less than 3 letters long  */
	@Description("PU-15 - Admin tries to create new user with username less than 3 letters long")
	@Link(name = "EDELIVERY-5225", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5225")
	@AllureId("PU-15")
	@Test(description = "PU-15", groups = {"multiTenancy", "singleTenancy"})
	public void pluginUsernameTooShort() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("testing username with only 2 letters");
		log.info("testing username with only 2 letters");

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		Allure.step("click New");
		log.info("click New");
		page.getNewBtn().click();

		PluginUserModal modal = new PluginUserModal(driver);
		Allure.step("fill username with aa");
		log.info("fill username with aa");
		modal.getUserNameInput().fill("aa");
		modal.getRolesSelect().selectOptionByText(DRoles.ADMIN);
		modal.getPasswordInput().fill(data.defaultPass());
		modal.getConfirmationInput().fill(data.defaultPass());

		Allure.step("checking the ok button to be disabled");
		log.info("checking the ok button to be disabled");
		soft.assertTrue(!modal.getOkBtn().isEnabled(), "OK button is disabled until user enters username with more than 3 characters");

		soft.assertAll();
	}

	/* PU-21 - Click Show columns link for Basic authetication type */
	/*  PU-21 - Click Show columns link for Basic authetication type  */
	@Description("PU-21 - Click Show columns link for Basic authetication type")
	@Link(name = "EDELIVERY-5231", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5231")
	@AllureId("PU-21")
	@Test(description = "PU-21", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLinkBasicAuth() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		Allure.step("checking show columns link");
		log.info("checking show columns link");
		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		Allure.step("Checking visibility of All/None links");
		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}

	/* PU-22 - Click Show columns link for Certificate authetication type */
	/*  PU-22 - Click Show columns link for Certificate authetication type  */
	@Description("PU-22 - Click Show columns link for Certificate authetication type")
	@Link(name = "EDELIVERY-5232", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5232")
	@AllureId("PU-22")
	@Test(description = "PU-22", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLinkCertAuth() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		Allure.step("checking show columns link");
		log.info("checking show columns link");
		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("cert_columns"));

		Allure.step("Checking visibility of All/None links");
		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}

	/* PU-23 - Check/Uncheck of fields on Show links */
	/*  PU-23 - CheckUncheck of fields on Show links  */
	@Description("PU-23 - CheckUncheck of fields on Show links")
	@Link(name = "EDELIVERY-5233", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5233")
	@AllureId("PU-23")
	@Test(description = "PU-23", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		Allure.step("modifying column visibility on basic auth view");
		log.info("modifying column visibility on basic auth view");

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		page.grid().checkModifyVisibleColumns(soft);

		page.refreshPage();
		Allure.step("modifying column visibility on certificate auth view");
		log.info("modifying column visibility on certificate auth view");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();
		page.grid().checkModifyVisibleColumns(soft);


		soft.assertAll();
	}

	/* PU-24 - Click Hide link without any new selection */
	/*  PU-24 - Click Hide link without any new selection  */
	@Description("PU-24 - Click Hide link without any new selection")
	@Link(name = "EDELIVERY-5234", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5234")
	@AllureId("PU-24")
	@Test(description = "PU-24", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		page.refreshPage();
		Allure.step("modifying column visibility on certificate auth view");
		log.info("modifying column visibility on certificate auth view");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		grid = page.grid();
		columnsPre = grid.getColumnNames();

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		columnsPost = grid.getColumnNames();
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		soft.assertAll();
	}

	/* PU-25 - Click Hide link after selecting some new fields */
	/*  PU-25 - Click Hide link after selecting some new fields  */
	@Description("PU-25 - Click Hide link after selecting some new fields")
	@Link(name = "EDELIVERY-5235", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5235")
	@AllureId("PU-25")
	@Test(description = "PU-25", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWNewSelection() throws Exception {
		String colName = "Role";
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

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
		soft.assertTrue(!columnsPost.contains(colName), "Correct column is now out of the list of columns");

		soft.assertAll();
	}

	/* PU-26 - Click All/None link */
	/*  PU-26 - Click All None link  */
	@Description("PU-26 - Click All None link")
	@Link(name = "EDELIVERY-5236", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5236")
	@AllureId("PU-26")
	@Test(description = "PU-26", groups = {"multiTenancy", "singleTenancy"})
	public void checkAllNoneLnk() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();

		grid.checkAllLink(soft);
		grid.checkNoneLink(soft);

		soft.assertAll();
	}

	/* PU-28 - Change Rows field data */
	/*  PU-28 - Change Rows field data  */
	@Description("PU-28 - Change Rows field data")
	@Link(name = "EDELIVERY-5238", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5238")
	@AllureId("PU-28")
	@Test(description = "PU-28", groups = {"multiTenancy", "singleTenancy"})
	public void checkChangeNumberOfRows() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/* This test method will verify presence of all duplicate users in error message*/
	/*  PU-33 - Verify presence of all duplicate plugin user name in error message while their addition  */
	@Description("PU-33 - Verify presence of all duplicate plugin user name in error message while their addition")
	@Link(name = "EDELIVERY-8178", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8178")
	@AllureId("PU-33")
	@Test(description = "PU-33", groups = {"multiTenancy", "singleTenancy"})
	public void duplicateUsrError() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		String username = Gen.randomAlphaNumeric(9);
		String uname = Gen.randomAlphaNumeric(9);

		page.newUser(uname, DRoles.ADMIN, data.defaultPass(), data.defaultPass());
		page.newUser(username, DRoles.ADMIN, data.defaultPass(), data.defaultPass());
		page.grid().waitForRowsToLoad();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		page.grid().waitForRowsToLoad();

		page.newUser(username, DRoles.ADMIN, data.getNewTestPass(), data.getNewTestPass());
		page.newUser(uname, DRoles.ADMIN, data.getNewTestPass(), data.getNewTestPass());
		page.grid().waitForRowsToLoad();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(username), "Error message contains" + username);
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(uname), "Error message contains" + uname);
		soft.assertTrue(page.getAlertArea().getAlertMessage().equals(String.format(DMessages.DUAL_PLUGINUSER_DUPLICATE, username, uname)), "Correct error message is shown");
		soft.assertAll();
	}

	private PluginUsersPage navigateToPluginUserPage() throws Exception {
		Allure.step("logged in");
		log.info("logged in");
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();
		return page;
	}


}
