package domibus.ui.functional;

import io.qameta.allure.*;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.plugin_users.CertPluginUserModal;
import pages.plugin_users.PluginUserModal;
import pages.plugin_users.PluginUsersPage;
import utils.Gen;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


@Epic("Plugin Users")
@Feature("Functional")
public class PluginUsersPgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PLUGIN_USERS);

	/*	PU-3 - Create new user and press Cancel	*/
	/*  PU-3 - Create new user and press Cancel  */
	@Description("PU-3 - Create new user and press Cancel")
	@Link(name = "EDELIVERY-5213", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5213")
	@AllureId("PU-3")
	@Test(description = "PU-3", groups = {"multiTenancy", "singleTenancy"})
	public void newUserCancel() throws Exception {

		String username = Gen.randomAlphaNumeric(9);
		Allure.step("creating user " + username);
		log.info("creating user " + username);
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);

		PluginUsersPage page = new PluginUsersPage(driver);
		Allure.step("checking Cancel button state");
		log.info("checking Cancel button state");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");

//		create new user
		Allure.step("filling form for new user " + username);
		log.info("filling form for new user " + username);
		page.newUser(username, DRoles.ADMIN, data.defaultPass(), data.defaultPass());
		page.grid().waitForRowsToLoad();

		Allure.step("checking Cancel button state");
		log.info("checking Cancel button state");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");

		Allure.step("click Cancel");
		log.info("click Cancel");
		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		Allure.step("checking grid for user " + username);
		log.info("checking grid for user " + username);
		int index = page.grid().scrollTo("User Name", username);
		soft.assertEquals(index, -1, "User not present in the list of users");

		soft.assertAll();
	}

	/*	PU-5 - Admin creates new user and presses Save	*/
	/*  PU-5 - Admin creates new user and presses Save  */
	@Description("PU-5 - Admin creates new user and presses Save")
	@Link(name = "EDELIVERY-5215", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5215")
	@AllureId("PU-5")
	@Test(description = "PU-5", groups = {"multiTenancy", "singleTenancy"})
	public void newUserSave() throws Exception {

		String username = Gen.randomAlphaNumeric(9);
		Allure.step("creating user " + username);
		log.info("creating user " + username);

		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);
		PluginUsersPage page = new PluginUsersPage(driver);

//		create new user
		Allure.step("filling plugin user form");
		log.info("filling plugin user form");
		page.newUser(username, DRoles.ADMIN, data.defaultPass(), data.defaultPass());

		page.grid().waitForRowsToLoad();

		Allure.step("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		page.clickVoidSpace();

		Allure.step(page.getAlertArea().getAlertMessage()); //.equalsIgnoreCase(DMessages.PLUGINUSER_SAVE_SUCCESS);
		log.info(page.getAlertArea().getAlertMessage()); //.equalsIgnoreCase(DMessages.PLUGINUSER_SAVE_SUCCESS);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		Allure.step("checking grid for user " + username);
		log.info("checking grid for user " + username);
		int index = page.grid().scrollTo("User Name", username);
		soft.assertTrue(index > -1, "Created user present in grid");

		Allure.step("checking buttons state");
		log.info("checking buttons state");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled after new user persisted");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled after new user persisted");

		rest.pluginUsers().deletePluginUser(username, null);

		soft.assertAll();
	}

	/*	PU-16 - Admin changes a user role	*/
	/*  PU-16 - Admin changes a user role  */
	@Description("PU-16 - Admin changes a user role")
	@Link(name = "EDELIVERY-5226", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5226")
	@AllureId("PU-16")
	@Test(description = "PU-16", groups = {"multiTenancy", "singleTenancy"})
	public void editUserRole() throws Exception {
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Allure.step("editing user " + username);
		log.info("editing user " + username);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);

		PluginUsersPage page = new PluginUsersPage(driver);
		DGrid grid = page.grid();
		Allure.step("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);
		page.getEditBtn().click();

		PluginUserModal pum = new PluginUserModal(driver);

		Allure.step("change role");
		log.info("change role");
		pum.getRolesSelect().selectOptionByText(DRoles.ADMIN);
		pum.clickOK();

		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		HashMap<String, String> info = grid.getRowInfo("User Name", username);
		soft.assertEquals(info.get("Role"), DRoles.ADMIN, "role is updated");

		Allure.step("click Save");
		log.info("click Save");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		info = grid.getRowInfo("User Name", username);
		soft.assertEquals(info.get("Role"), DRoles.ADMIN, "role is updated");

		soft.assertAll();
	}

	/*	PU-6 - Admin edits an existing user and presses Cancel	*/
	/*  PU-6 - Admin edits an existing user and presses Cancel  */
	@Description("PU-6 - Admin edits an existing user and presses Cancel")
	@Link(name = "EDELIVERY-5216", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5216")
	@AllureId("PU-6")
	@Test(description = "PU-6", groups = {"multiTenancy", "singleTenancy"})
	public void editAndCancel() throws Exception {
		SoftAssert soft = new SoftAssert();

		String toAdd = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C7";
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Allure.step("editing user " + username);
		log.info("editing user " + username);

//		login with Admin and go to plugin users page

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		Allure.step("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);
		page.getEditBtn().click();

		PluginUserModal pum = new PluginUserModal(driver);

		Allure.step("fill Original User input with invalid string");
		log.info("fill Original User input with invalid string");
		pum.getOriginalUserInput().fill("testEdit");
		pum.changeFocus();
		Allure.step("check error message");
		log.info("check error message");
		soft.assertEquals(pum.getOriginalUserErrMess().getText(), DMessages.PLUGIN_USER_ORIGINAL_USER_INVALID, "Invalid value cannot be saved in the Original User field");

		Allure.step("fill Original User input with valid string");
		log.info("fill Original User input with valid string");
		pum.getOriginalUserInput().fill(toAdd);
		pum.changeFocus();
		pum.clickOK();

		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		HashMap<String, String> userInfo = grid.getRowInfo("User Name", username);
		soft.assertEquals(userInfo.get("Original User"), toAdd, "Edited value is visible in the grid");

		Allure.step("click cancel");
		log.info("click cancel");
		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		grid.waitForRowsToLoad();
		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		userInfo = grid.getRowInfo("User Name", username);
		soft.assertNotEquals(userInfo.get("Original User"), toAdd, "Edited value is NOT visible in the grid after Cancel");

		soft.assertAll();

	}

	/* PU-7 - Admin edits an existing user and presses Save */
	/*  PU-7 - Admin edits an existing user and presses Save  */
	@Description("PU-7 - Admin edits an existing user and presses Save")
	@Link(name = "EDELIVERY-5217", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5217")
	@AllureId("PU-7")
	@Test(description = "PU-7", groups = {"multiTenancy", "singleTenancy"})
	public void editAndSave() throws Exception {
		SoftAssert soft = new SoftAssert();

		String toAdd = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:G" + Gen.randomNumber(100);
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Allure.step("editing user " + username);
		log.info("editing user " + username);

//		login with Admin and go to plugin users page

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();

		Allure.step("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);
		page.getEditBtn().click();

		PluginUserModal pum = new PluginUserModal(driver);

		Allure.step("fill Original User input with invalid string");
		log.info("fill Original User input with invalid string");
		pum.getOriginalUserInput().fill("testEdit");
		pum.changeFocus();

		Allure.step("check error message");
		log.info("check error message");
		soft.assertEquals(pum.getOriginalUserErrMess().getText(), DMessages.PLUGINUSER_MODAL_ORIGINAL_USER_ERR, "Correct error message is shown when wrong original user is entered");
		soft.assertTrue(!pum.getOkBtn().isEnabled(), "Invalid value cannot be saved in the Original User field");

		Allure.step("fill Original User input with valid string");
		log.info("fill Original User input with valid string");
		pum.getOriginalUserInput().fill(toAdd);
		pum.changeFocus();
		pum.clickOK();

		page.grid().waitForRowsToLoad();

		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("Original User", toAdd) > -1, "Edited value is visible in the grid");

		Allure.step("click Save");
		log.info("click Save");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("Original User", toAdd) > -1, "Edited value is visible in the grid after Save");

		soft.assertAll();

	}

	/*	PU-8 - Admin deletes user and presses Cancel	*/
	/*  PU-8 - Admin deletes user and presses Cancel  */
	@Description("PU-8 - Admin deletes user and presses Cancel")
	@Link(name = "EDELIVERY-5218", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5218")
	@AllureId("PU-8")
	@Test(description = "PU-8", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndCancel() throws Exception {
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Allure.step("testing for user " + username);
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);
		PluginUsersPage page = new PluginUsersPage(driver);

		DGrid grid = page.grid();
		Allure.step("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);

		Allure.step("click delete ");
		log.info("click delete ");
		page.getDeleteBtn().click();

		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("User Name", username) == -1, "Plugin user is not present in the grid after delete");

		Allure.step("canceling");
		log.info("canceling");
		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("User Name", username) > -1, "Plugin user is present in the grid after Cancel");

		soft.assertAll();
	}

	/*	PU-9 - Admin deletes user and presses Save	*/
	/*  PU-9 - Admin deletes user and presses Save  */
	@Description("PU-9 - Admin deletes user and presses Save")
	@Link(name = "EDELIVERY-5219", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5219")
	@AllureId("PU-9")
	@Test(description = "PU-9", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndSave() throws Exception {
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Allure.step("testing for user " + username);
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);
		PluginUsersPage page = new PluginUsersPage(driver);

		DGrid grid = page.grid();
		Allure.step("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);

		Allure.step("click delete ");
		log.info("click delete ");
		page.getDeleteBtn().click();

		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("User Name", username) == -1, "Plugin user is not present in the grid after delete");

		Allure.step("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Allure.step("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("User Name", username) == -1, "Plugin user is NOT present in the grid after Save");

		soft.assertAll();
	}


	/*PU-13 - Create a certificate plugin userand press save*/
	/*  PU-13 - Create a certificate plugin user and press save  */
	@Description("PU-13 - Create a certificate plugin user and press save")
	@Link(name = "EDELIVERY-5223", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5223")
	@AllureId("PU-13")
	@Test(description = "PU-13", groups = {"multiTenancy", "singleTenancy"})
	public void createCertificatePluginUserSave() throws Exception {
		String id = Gen.randomAlphaNumeric(5);
		String certId = "CN=puser,O=eDelivery,C=BE:" + id;
		Allure.step("creating plugin user with certificate " + certId);
		log.info("creating plugin user with certificate " + certId);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		Allure.step("switching to auth type certificate");
		log.info("switching to auth type certificate");
		page.filters.getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		Allure.step("click new user button");
		log.info("click new user button");
		page.getNewBtn().click();

		Allure.step("adding data in form");
		log.info("adding data in form");
		CertPluginUserModal modal = new CertPluginUserModal(driver);
		modal.getUserInput().fill(certId);
		modal.getRoleSelect().selectOptionByText(DRoles.ADMIN);
		modal.clickOK();

		Allure.step("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Allure.step("checking grid for new data");
		log.info("checking grid for new data");
		soft.assertTrue(page.grid().scrollTo("Certificate Id", certId) > -1, "New user is present in the grid");

		soft.assertAll();
	}

	/*PU-14 - Create a certificate plugin userand press cancel*/
	/*  PU-14 - Create a certificate plugin user and press cancel  */
	@Description("PU-14 - Create a certificate plugin user and press cancel")
	@Link(name = "EDELIVERY-5224", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5224")
	@AllureId("PU-14")
	@Test(description = "PU-14", groups = {"multiTenancy", "singleTenancy"})
	public void createCertificatePluginUserCancel() throws Exception {
		String id = Gen.randomAlphaNumeric(5);
		String certId = "CN=puser,O=eDelivery,C=BE:" + id;
		Allure.step("creating plugin user with certificate " + certId);
		log.info("creating plugin user with certificate " + certId);

		SoftAssert soft = new SoftAssert();

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();


		Allure.step("switching to auth type certificate");
		log.info("switching to auth type certificate");
		page.filters.getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		Allure.step("click new user button");
		log.info("click new user button");
		page.getNewBtn().click();

		Allure.step("adding data in form");
		log.info("adding data in form");
		CertPluginUserModal modal = new CertPluginUserModal(driver);
		modal.getUserInput().fill(certId);
		modal.getRoleSelect().selectOptionByText(DRoles.ADMIN);
		modal.clickOK();

		Allure.step("canceling");
		log.info("canceling");
		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		Allure.step("checking grid for data");
		log.info("checking grid for data");
		soft.assertTrue(page.grid().scrollTo("Certificate Id", certId) == -1, "New user is NOT present in the grid");

		soft.assertAll();
	}

	/*PU-18 - Admin tries to create users with the same username on multiple domains*/
	/*  PU-18 - Admin tries to create users with the same username on multiple domains  */
	@Description("PU-18 - Admin tries to create users with the same username on multiple domains")
	@Link(name = "EDELIVERY-5228", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5228")
	@AllureId("PU-18")
	@Test(description = "PU-18", groups = {"multiTenancy"})
	public void duplicatePluginUsersDifferentDomain() throws Exception {
		String domainName = rest.getNonDefaultDomain();
		if (StringUtils.isEmpty(domainName)) {
			throw new SkipException("could not get domains");
		}
		String domainCode = rest.getDomainCodeForName(domainName);
		String username = rest.getPluginUser(domainCode, DRoles.USER, true, false).getString("userName");
		Allure.step("testing for username " + username);
		log.info("testing for username " + username);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);

		PluginUsersPage page = new PluginUsersPage(driver);
		Allure.step("creating new user on default domain");
		log.info("creating new user on default domain");
		page.newUser(username, DRoles.USER, data.defaultPass(), data.defaultPass());
		Allure.step("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Allure.step("checking for error message");
		log.info("checking for error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.PLUGINUSER_DUPLICATE_USERNAME, username, domainCode),
				"Error message is shown");

		rest.pluginUsers().deletePluginUser(username, domainCode);
		soft.assertAll();
	}

	/* PU-19 - Admin tries to create plugin user with the same name as a normal user from his domain */
	/*  PU-19 - Admin tries to create plugin user with the same name as a normal user from his domain  */
	@Description("PU-19 - Admin tries to create plugin user with the same name as a normal user from his domain")
	@Link(name = "EDELIVERY-5229", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5229")
	@AllureId("PU-19")
	@Test(description = "PU-19", groups = {"multiTenancy"})
	public void sameUsernameAsUserOnSameDomain() throws Exception {
		String username = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");
		Allure.step("testing for user " + username);
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);

		PluginUsersPage page = new PluginUsersPage(driver);

		Allure.step("adding new plugin user with username " + username);
		log.info("adding new plugin user with username " + username);
		page.newUser(username, DRoles.USER, data.defaultPass(), data.defaultPass());
		Allure.step("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Allure.step("checking page for errors");
		log.info("checking page for errors");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage().toLowerCase(),
				String.format(DMessages.PLUGINUSER_DUPLICATE_USERNAME, username, "default").toLowerCase(),
				"Error message is shown");

		soft.assertAll();
	}

	/*	PU-20 - Admin tries to create plugin user with the same name as a normal user from another domain	*/
	/*  PU-20 - Admin tries to create plugin user with the same name as a normal user from another domain  */
	@Description("PU-20 - Admin tries to create plugin user with the same name as a normal user from another domain")
	@Link(name = "EDELIVERY-5230", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5230")
	@AllureId("PU-20")
	@Test(description = "PU-20", groups = {"multiTenancy"})
	public void sameUsernameAsUserOnDifferentDomain() throws Exception {
		String domainName = rest.getNonDefaultDomain();
		if (StringUtils.isEmpty(domainName)) {
			throw new SkipException("could not get domains");
		}
		String domainCode = rest.getDomainCodeForName(domainName);
		String username = rest.getUser(domainCode, DRoles.USER, true, false, false).getString("userName");
		Allure.step("testing for username " + username);
		log.info("testing for username " + username);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);
		PluginUsersPage page = new PluginUsersPage(driver);
		Allure.step("creating new user");
		log.info("creating new user");
		page.newUser(username, DRoles.USER, data.defaultPass(), data.defaultPass());

		Allure.step("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Allure.step("checking page for errors");
		log.info("checking page for errors");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.PLUGINUSER_DUPLICATE_USERNAME, username, domainCode),
				"Error message is shown");

		soft.assertAll();
	}

	/* PU-31 - Check duplicate user addition with same certificate id  */
	/*  PU-31 - Create certificate user with duplicated certificate ID on the same domain  */
	@Description("PU-31 - Create certificate user with duplicated certificate ID on the same domain")
	@Link(name = "EDELIVERY-5241", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5241")
	@AllureId("PU-31")
	@Test(description = "PU-31", groups = {"multiTenancy", "singleTenancy"})
	public void certificatePluginUserDuplicateSameDomain() throws Exception {
		String id = Gen.randomAlphaNumeric(5);
		String certId = "CN=puser,O=eDelivery,C=BE:" + id;
		Allure.step("testing for certificate id " + certId);
		log.info("testing for certificate id " + certId);

		SoftAssert soft = new SoftAssert();

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		page.filters.getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		Allure.step("click new");
		log.info("click new");
		page.getNewBtn().click();

		Allure.step("creating user");
		log.info("creating user");
		CertPluginUserModal modal = new CertPluginUserModal(driver);
		modal.getUserInput().fill(certId);
		modal.getRoleSelect().selectOptionByText(DRoles.ADMIN);
		modal.clickOK();

		Allure.step("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Allure.step("clicking new");
		log.info("clicking new");
		page.getNewBtn().click();

		Allure.step("creating the same user");
		log.info("creating the same user");
		modal = new CertPluginUserModal(driver);
		modal.getUserInput().fill(certId);
		modal.getRoleSelect().selectOptionByText(DRoles.ADMIN);
		modal.clickOK();

		Allure.step("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Allure.step("checking for errors");
		log.info("checking for errors");
		soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");

		soft.assertAll();
	}


	/*	PU-32 - Create duplicate plugin users by smashing the save button multiple times 	*/
	/*  PU-32 - Create duplicate plugin users by smashing the save button multiple times  */
	@Description("PU-32 - Create duplicate plugin users by smashing the save button multiple times")
	@Link(name = "EDELIVERY-5508", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5508")
	@AllureId("PU-32")
	@Test(description = "PU-32", groups = {"multiTenancy", "singleTenancy"})
	public void newUserSaveMultipleSaves() throws Exception {

		String username = Gen.randomAlphaNumeric(9);
		Allure.step("creating user " + username);
		log.info("creating user " + username);

		SoftAssert soft = new SoftAssert();


		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		Allure.step("checking buttons state");
		log.info("checking buttons state");
		soft.assertTrue(page.getCancelBtn().isDisabled(), "Cancel button is disabled on page load");
		soft.assertTrue(page.getSaveBtn().isDisabled(), "Save button is disabled on page load");

//		create new user
		Allure.step("filling plugin user form");
		log.info("filling plugin user form");
		page.newUser(username, DRoles.ADMIN, data.defaultPass(), data.defaultPass());

		for (int i = 0; i < 10; i++) {
			try {
				Allure.step("saving");
				log.info("saving");
				page.saveBtn.click();
				new Dialog(driver).yesBtn.click();
			} catch (Exception e) {
				break;
			}
		}

		page.grid().waitForRowsToLoad();

		List<String> knownUsernames = new ArrayList<>();
		List<HashMap<String, String>> allInfo = page.grid().getAllRowInfo();
		for (HashMap<String, String> info : allInfo) {
			String pluginUser = info.get("User Name");
			soft.assertTrue(!knownUsernames.contains(pluginUser), "Username already present in list: " + pluginUser);
			knownUsernames.add(pluginUser);
		}

		soft.assertAll();
	}

}
