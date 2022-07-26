package domibus.ui.functional;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.testng.Reporter;
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


public class PluginUsersPgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PLUGIN_USERS);

	/* EDELIVERY-5213 - PU-3 - Create new user and press Cancel */
	@Test(description = "PU-3", groups = {"multiTenancy", "singleTenancy"})
	public void newUserCancel() throws Exception {

		String username = Gen.randomAlphaNumeric(9);
		Reporter.log("creating user " + username);
		log.info("creating user " + username);
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);

		PluginUsersPage page = new PluginUsersPage(driver);
		Reporter.log("checking Cancel button state");
		log.info("checking Cancel button state");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");

//		create new user
		Reporter.log("filling form for new user " + username);
		log.info("filling form for new user " + username);
		page.newUser(username, DRoles.ADMIN, data.defaultPass(), data.defaultPass());
		page.grid().waitForRowsToLoad();

		Reporter.log("checking Cancel button state");
		log.info("checking Cancel button state");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");

		Reporter.log("click Cancel");
		log.info("click Cancel");
		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("checking grid for user " + username);
		log.info("checking grid for user " + username);
		int index = page.grid().scrollTo("User Name", username);
		soft.assertEquals(index, -1, "User not present in the list of users");

		soft.assertAll();
	}

	/* EDELIVERY-5215 - PU-5 - Admin creates new user and presses Save */
	@Test(description = "PU-5", groups = {"multiTenancy", "singleTenancy"})
	public void newUserSave() throws Exception {

		String username = Gen.randomAlphaNumeric(9);
		Reporter.log("creating user " + username);
		log.info("creating user " + username);

		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);
		PluginUsersPage page = new PluginUsersPage(driver);

//		create new user
		Reporter.log("filling plugin user form");
		log.info("filling plugin user form");
		page.newUser(username, DRoles.ADMIN, data.defaultPass(), data.defaultPass());

		page.grid().waitForRowsToLoad();

		Reporter.log("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		page.clickVoidSpace();

		Reporter.log(page.getAlertArea().getAlertMessage()); //.equalsIgnoreCase(DMessages.PLUGINUSER_SAVE_SUCCESS);
		log.info(page.getAlertArea().getAlertMessage()); //.equalsIgnoreCase(DMessages.PLUGINUSER_SAVE_SUCCESS);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		Reporter.log("checking grid for user " + username);
		log.info("checking grid for user " + username);
		int index = page.grid().scrollTo("User Name", username);
		soft.assertTrue(index > -1, "Created user present in grid");

		Reporter.log("checking buttons state");
		log.info("checking buttons state");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled after new user persisted");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled after new user persisted");

		rest.pluginUsers().deletePluginUser(username, null);

		soft.assertAll();
	}

	/* EDELIVERY-5226 - PU-16 - Admin changes a user role */
	@Test(description = "PU-16", groups = {"multiTenancy", "singleTenancy"})
	public void editUserRole() throws Exception {
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Reporter.log("editing user " + username);
		log.info("editing user " + username);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);

		PluginUsersPage page = new PluginUsersPage(driver);
		DGrid grid = page.grid();
		Reporter.log("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);
		page.getEditBtn().click();

		PluginUserModal pum = new PluginUserModal(driver);

		Reporter.log("change role");
		log.info("change role");
		pum.getRolesSelect().selectOptionByText(DRoles.ADMIN);
		pum.clickOK();

		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		HashMap<String, String> info = grid.getRowInfo("User Name", username);
		soft.assertEquals(info.get("Role"), DRoles.ADMIN, "role is updated");

		Reporter.log("click Save");
		log.info("click Save");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		info = grid.getRowInfo("User Name", username);
		soft.assertEquals(info.get("Role"), DRoles.ADMIN, "role is updated");

		soft.assertAll();
	}

	/* EDELIVERY-5216 - PU-6 - Admin edits an existing user and presses Cancel */
	@Test(description = "PU-6", groups = {"multiTenancy", "singleTenancy"})
	public void editAndCancel() throws Exception {

		SoftAssert soft = new SoftAssert();


		String originalUserStr = Gen.rndStr(15);
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Reporter.log("editing user " + username);
		log.info("editing user " + username);

//		login with Admin and go to plugin users page

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		Reporter.log("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);
		page.getEditBtn().click();

		PluginUserModal pum = new PluginUserModal(driver);

		Reporter.log("fill Original User input with string");
		log.info("fill Original User input with string");
		pum.getOriginalUserInput().fill(originalUserStr);
		pum.changeFocus();
		pum.clickOK();

		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		HashMap<String, String> userInfo = grid.getRowInfo("User Name", username);
		soft.assertEquals(userInfo.get("Original User"), originalUserStr, "Edited value is visible in the grid");

		Reporter.log("click cancel");
		log.info("click cancel");
		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		grid.waitForRowsToLoad();
		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		userInfo = grid.getRowInfo("User Name", username);
		soft.assertNotEquals(userInfo.get("Original User"), originalUserStr, "Edited value is NOT visible in the grid after Cancel");

		soft.assertAll();

	}

	/* EDELIVERY-5217 - PU-7 - Admin edits an existing user and presses Save */
	@Test(description = "PU-7", groups = {"multiTenancy", "singleTenancy"})
	public void editAndSave() throws Exception {
		SoftAssert soft = new SoftAssert();

		String originalUserStr = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:G" + Gen.randomNumber(100);
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Reporter.log("editing user " + username);
		log.info("editing user " + username);

//		login with Admin and go to plugin users page

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();

		Reporter.log("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);
		page.getEditBtn().click();

		PluginUserModal pum = new PluginUserModal(driver);

		Reporter.log("fill Original User input with string");
		log.info("fill Original User input with string");
		pum.getOriginalUserInput().fill(originalUserStr);
		pum.changeFocus();
		pum.clickOK();

		page.grid().waitForRowsToLoad();

		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("Original User", originalUserStr) > -1, "Edited value is visible in the grid");

		Reporter.log("click Save");
		log.info("click Save");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("Original User", originalUserStr) > -1, "Edited value is visible in the grid after Save");

		soft.assertAll();

	}

	/* EDELIVERY-5218 - PU-8 - Admin deletes user and presses Cancel */
	@Test(description = "PU-8", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndCancel() throws Exception {
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Reporter.log("testing for user " + username);
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);
		PluginUsersPage page = new PluginUsersPage(driver);

		DGrid grid = page.grid();
		Reporter.log("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);

		Reporter.log("click delete ");
		log.info("click delete ");
		page.getDeleteBtn().click();

		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("User Name", username) == -1, "Plugin user is not present in the grid after delete");

		Reporter.log("canceling");
		log.info("canceling");
		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("User Name", username) > -1, "Plugin user is present in the grid after Cancel");

		soft.assertAll();
	}

	/* EDELIVERY-5219 - PU-9 - Admin deletes user and presses Save */
	@Test(description = "PU-9", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndSave() throws Exception {
		String username = rest.getPluginUser(null, DRoles.USER, true, false).getString("userName");
		Reporter.log("testing for user " + username);
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);
		PluginUsersPage page = new PluginUsersPage(driver);

		DGrid grid = page.grid();
		Reporter.log("selecting user " + username);
		log.info("selecting user " + username);
		grid.scrollToAndSelect("User Name", username);

		Reporter.log("click delete ");
		log.info("click delete ");
		page.getDeleteBtn().click();

		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("User Name", username) == -1, "Plugin user is not present in the grid after delete");

		Reporter.log("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("check grid for updated info");
		log.info("check grid for updated info");
		soft.assertTrue(grid.scrollTo("User Name", username) == -1, "Plugin user is NOT present in the grid after Save");

		soft.assertAll();
	}


	/* EDELIVERY-5223 - PU-13 - Create a certificate plugin user and press save */
	@Test(description = "PU-13", groups = {"multiTenancy", "singleTenancy"})
	public void createCertificatePluginUserSave() throws Exception {
		String id = Gen.randomAlphaNumeric(5);
		String certId = "CN=puser,O=eDelivery,C=BE:" + id;
		Reporter.log("creating plugin user with certificate " + certId);
		log.info("creating plugin user with certificate " + certId);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		Reporter.log("switching to auth type certificate");
		log.info("switching to auth type certificate");
		page.filters.getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		Reporter.log("click new user button");
		log.info("click new user button");
		page.getNewBtn().click();

		Reporter.log("adding data in form");
		log.info("adding data in form");
		CertPluginUserModal modal = new CertPluginUserModal(driver);
		modal.getUserInput().fill(certId);
		modal.getRoleSelect().selectOptionByText(DRoles.ADMIN);
		modal.clickOK();

		Reporter.log("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("checking grid for new data");
		log.info("checking grid for new data");
		soft.assertTrue(page.grid().scrollTo("Certificate Id", certId) > -1, "New user is present in the grid");

		soft.assertAll();
	}

	/* EDELIVERY-5224 - PU-14 - Create a certificate plugin user and press cancel */
	@Test(description = "PU-14", groups = {"multiTenancy", "singleTenancy"})
	public void createCertificatePluginUserCancel() throws Exception {
		String id = Gen.randomAlphaNumeric(5);
		String certId = "CN=puser,O=eDelivery,C=BE:" + id;
		Reporter.log("creating plugin user with certificate " + certId);
		log.info("creating plugin user with certificate " + certId);

		SoftAssert soft = new SoftAssert();

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();


		Reporter.log("switching to auth type certificate");
		log.info("switching to auth type certificate");
		page.filters.getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		Reporter.log("click new user button");
		log.info("click new user button");
		page.getNewBtn().click();

		Reporter.log("adding data in form");
		log.info("adding data in form");
		CertPluginUserModal modal = new CertPluginUserModal(driver);
		modal.getUserInput().fill(certId);
		modal.getRoleSelect().selectOptionByText(DRoles.ADMIN);
		modal.clickOK();

		Reporter.log("canceling");
		log.info("canceling");
		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("checking grid for data");
		log.info("checking grid for data");
		soft.assertTrue(page.grid().scrollTo("Certificate Id", certId) == -1, "New user is NOT present in the grid");

		soft.assertAll();
	}

	/* EDELIVERY-5228 - PU-18 - Admin tries to create users with the same username on multiple domains */
	@Test(description = "PU-18", groups = {"multiTenancy"})
	public void duplicatePluginUsersDifferentDomain() throws Exception {
		String domainName = rest.getNonDefaultDomain();
		if (StringUtils.isEmpty(domainName)) {
			throw new SkipException("could not get domains");
		}
		String domainCode = rest.getDomainCodeForName(domainName);
		String username = rest.getPluginUser(domainCode, DRoles.USER, true, false).getString("userName");
		Reporter.log("testing for username " + username);
		log.info("testing for username " + username);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);

		PluginUsersPage page = new PluginUsersPage(driver);
		Reporter.log("creating new user on default domain");
		log.info("creating new user on default domain");
		page.newUser(username, DRoles.USER, data.defaultPass(), data.defaultPass());
		Reporter.log("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("checking for error message");
		log.info("checking for error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.PLUGINUSER_DUPLICATE_USERNAME, username, domainCode),
				"Error message is shown");

		rest.pluginUsers().deletePluginUser(username, domainCode);
		soft.assertAll();
	}

	/* EDELIVERY-5229 - PU-19 - Admin tries to create plugin user with the same name as a normal user from his domain */
	@Test(description = "PU-19", groups = {"multiTenancy"})
	public void sameUsernameAsUserOnSameDomain() throws Exception {
		String username = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");
		Reporter.log("testing for user " + username);
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);

		PluginUsersPage page = new PluginUsersPage(driver);

		Reporter.log("adding new plugin user with username " + username);
		log.info("adding new plugin user with username " + username);
		page.newUser(username, DRoles.USER, data.defaultPass(), data.defaultPass());
		Reporter.log("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("checking page for errors");
		log.info("checking page for errors");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage().toLowerCase(),
				String.format(DMessages.PLUGINUSER_DUPLICATE_USERNAME, username, "default").toLowerCase(),
				"Error message is shown");

		soft.assertAll();
	}

	/* EDELIVERY-5230 - PU-20 - Admin tries to create plugin user with the same name as a normal user from another domain */
	@Test(description = "PU-20", groups = {"multiTenancy"})
	public void sameUsernameAsUserOnDifferentDomain() throws Exception {
		String domainName = rest.getNonDefaultDomain();
		if (StringUtils.isEmpty(domainName)) {
			throw new SkipException("could not get domains");
		}
		String domainCode = rest.getDomainCodeForName(domainName);
		String username = rest.getUser(domainCode, DRoles.USER, true, false, false).getString("userName");
		Reporter.log("testing for username " + username);
		log.info("testing for username " + username);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PLUGIN_USERS);
		PluginUsersPage page = new PluginUsersPage(driver);
		Reporter.log("creating new user");
		log.info("creating new user");
		page.newUser(username, DRoles.USER, data.defaultPass(), data.defaultPass());

		Reporter.log("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("checking page for errors");
		log.info("checking page for errors");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.PLUGINUSER_DUPLICATE_USERNAME, username, domainCode),
				"Error message is shown");

		soft.assertAll();
	}

	/* EDELIVERY-5241 - PU-31 - Create certificate user with duplicated certificate ID on the same domain */
	@Test(description = "PU-31", groups = {"multiTenancy", "singleTenancy"})
	public void certificatePluginUserDuplicateSameDomain() throws Exception {
		String id = Gen.randomAlphaNumeric(5);
		String certId = "CN=puser,O=eDelivery,C=BE:" + id;
		Reporter.log("testing for certificate id " + certId);
		log.info("testing for certificate id " + certId);

		SoftAssert soft = new SoftAssert();

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		page.filters.getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		Reporter.log("click new");
		log.info("click new");
		page.getNewBtn().click();

		Reporter.log("creating user");
		log.info("creating user");
		CertPluginUserModal modal = new CertPluginUserModal(driver);
		modal.getUserInput().fill(certId);
		modal.getRoleSelect().selectOptionByText(DRoles.ADMIN);
		modal.clickOK();

		Reporter.log("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("clicking new");
		log.info("clicking new");
		page.getNewBtn().click();

		Reporter.log("creating the same user");
		log.info("creating the same user");
		modal = new CertPluginUserModal(driver);
		modal.getUserInput().fill(certId);
		modal.getRoleSelect().selectOptionByText(DRoles.ADMIN);
		modal.clickOK();

		Reporter.log("saving");
		log.info("saving");
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		Reporter.log("checking for errors");
		log.info("checking for errors");
		soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");

		soft.assertAll();
	}


	/* EDELIVERY-5508 - PU-32 - Create duplicate plugin users by smashing the save button multiple times */
	@Test(description = "PU-32", groups = {"multiTenancy", "singleTenancy"})
	public void newUserSaveMultipleSaves() throws Exception {

		String username = Gen.randomAlphaNumeric(9);
		Reporter.log("creating user " + username);
		log.info("creating user " + username);

		SoftAssert soft = new SoftAssert();


		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		Reporter.log("checking buttons state");
		log.info("checking buttons state");
		soft.assertTrue(page.getCancelBtn().isDisabled(), "Cancel button is disabled on page load");
		soft.assertTrue(page.getSaveBtn().isDisabled(), "Save button is disabled on page load");

//		create new user
		Reporter.log("filling plugin user form");
		log.info("filling plugin user form");
		page.newUser(username, DRoles.ADMIN, data.defaultPass(), data.defaultPass());

		for (int i = 0; i < 10; i++) {
			try {
				Reporter.log("saving");
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


	/* EDELIVERY-7208 - PU-47 - Mark as inactive a BASIC plugin user */
	@Test(description = "PU-47", groups = {"multiTenancy"})
	public void inactivePLUSendMess() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getPluginUser(null, DRoles.ADMIN, false, true).getString("userName");
		Reporter.log("testing for basic plugin user " + username);
		log.info("testing for basic plugin user " + username);
		boolean success = true;
		try {
			messageSender.sendMessage(username, data.defaultPass(), null, null);
		} catch (Exception e) {
			success = false;
			e.printStackTrace();
		}
		soft.assertFalse(success, "Message was not sent with disabled plugin user");

		soft.assertAll();
	}


}
