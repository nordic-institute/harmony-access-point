package domibus.ui.ux;

import org.testng.Reporter;
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


public class PluginUsersPg2UXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PLUGIN_USERS);

	/* EDELIVERY-5239 - PU-29 - Check sorting on the basis of Headers of Grid  */
	@Test(description = "PU-29", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();

		for (int i = 0; i < 2; i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}

		colDescs = descriptorObj.getJSONObject("grid").getJSONArray("cert_columns");

		Reporter.log("switching to CERT users");
		log.info("switching to CERT users");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		grid.waitForRowsToLoad();

		for (int i = 0; i < 2; i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}

		soft.assertAll();
	}


	/* EDELIVERY-5237 - PU-27 - Download all lists of users */
	@Test(description = "PU-27", groups = {"multiTenancy", "singleTenancy"})
	public void downloadAsCSV() throws Exception {
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		String fileName = page.pressSaveCsvAndSaveFile();
		Reporter.log("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();
		page.grid().sortBy("User Name");

		Reporter.log("set page size to 100");
		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		Reporter.log("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.grid().relaxCheckCSVvsGridInfo(fileName, soft, "text");

		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		fileName = page.pressSaveCsvAndSaveFile();
		Reporter.log("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();
		page.grid().sortBy("Certificate Id");

		Reporter.log("set page size to 100");
		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		Reporter.log("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.grid().relaxCheckCSVvsGridInfo(fileName, soft, "text");


		soft.assertAll();
	}


	/* EDELIVERY-5240 - PU-30 - Verify headers in downloaded CSV sheet  */
	@Test(description = "PU-30", groups = {"multiTenancy", "singleTenancy"})
	public void verifyCSVHeaders() throws Exception {
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		String fileName = page.pressSaveCsvAndSaveFile();
		Reporter.log("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();


		Reporter.log("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		fileName = page.pressSaveCsvAndSaveFile();
		Reporter.log("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Reporter.log("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);


		soft.assertAll();
	}


	/* EDELIVERY-6373 - PU-35 - Verify presence of confirmation pop up while saving plugin user */
	@Test(description = "PU-35", groups = {"multiTenancy", "singleTenancy"})
	public void verifyConfPopUp() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(5);

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		List<String> authType = page.filters.getAuthTypeSelect().getOptionsTexts();
		for (String auth : authType) {
// check scenario for both types of plugin users
			page.filters.getAuthTypeSelect().selectOptionByText(auth);
			page.grid().waitForRowsToLoad();

			Reporter.log("checking Cancel button state");
			log.info("checking Cancel button state");
			soft.assertFalse(page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");

			if (page.filters.getAuthTypeSelect().getSelectedValue().equals("BASIC")) {

				Reporter.log("filling form for new user  for BASIC auth type" + username);
				log.info("filling form for new user  for BASIC auth type" + username);
				page.newUser(username, DRoles.ADMIN, data.defaultPass(), data.defaultPass());
			} else {
				String certUserName = "CN=" + Gen.randomAlphaNumeric(1) + "," + "O=" + Gen.randomAlphaNumeric(2) +
						"," + "C=" + RandomStringUtils.random(2, true, false) + ":" + Gen.randomNumber(2);

				Reporter.log("filling form for new cert user for CERTIFICATION auth type :" + certUserName);
				log.info("filling form for new cert user for CERTIFICATION auth type :" + certUserName);
				page.newCertUser(certUserName, DRoles.ADMIN);
			}

			page.grid().waitForRowsToLoad();
			Reporter.log("checking Cancel button state");
			log.info("checking Cancel button state");
			soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");

			page.getSaveBtn().click();
			new Dialog(driver).confirm();

			soft.assertFalse(page.getAlertArea().isError(), "Success message is shown");
			soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.PLUGINUSER_SAVE_SUCCESS, "Correct success message is shown");

			page.grid().waitForRowsToLoad();
		}
		soft.assertAll();
	}


	/* EDELIVERY-6358 - PU-34 - Verify error while addition of Pluginuser with username super  SUPER in multitenant */
	@Test(description = "PU-34", groups = {"multiTenancy"})
	public void addSUPERPluginUsr() throws Exception {
		SoftAssert soft = new SoftAssert();
		List<String> usernames = Arrays.asList(new String[]{"super", "SUPER"});

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		for (String userToAdd : usernames) {
			Reporter.log("Username to be added" + userToAdd);
			log.info("Username to be added" + userToAdd);
			page.newUser(userToAdd, DRoles.ADMIN, data.defaultPass(), data.defaultPass());
			page.getSaveBtn().click();
			new DButton(driver, new Dialog(driver).yesBtn).click();

			if (userToAdd.equals("super")) {
				soft.assertTrue(page.getAlertArea().getAlertMessage().contains("name already exists"), "Error message is shown");
				page.getCancelBtn().click();
				new DButton(driver, new Dialog(driver).yesBtn).click();
			} else {
				soft.assertTrue(page.getAlertArea().getAlertMessage().contains("success"), "Success message is shown");

				Reporter.log("Logout and login again to confirm super user details remain same after addition of SUPER plugin user");
				log.info("Logout and login again to confirm super user details remain same after addition of SUPER plugin user");
				logout();
				login(data.getAdminUser());
			}
			page.refreshPage();
			page.grid().waitForRowsToLoad();
			soft.assertAll();
		}
	}

	/* EDELIVERY-7195 - PU-37 - Edit certificate user and Cancel */
	@Test(description = "PU-37", groups = {"multiTenancy", "singleTenancy"})
	public void certificateUsrEditCancel() throws Exception {
		String certName = Gen.randomAlphaNumeric(5);
		String role = DRoles.USER;
		String username = String.format("CN=%s,O=eDelivery,C=BE:%s", certName, Gen.randomAlphaNumeric(5));
		rest.pluginUsers().createCertPluginUser(username, role, null);
		Reporter.log("new Certificate User " + username);
		log.info("new Certificate User " + username);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = navigateToPluginUserPage();

		Reporter.log("Search CERTIFICATE type plugin users");
		log.info("Search CERTIFICATE type plugin users");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();
		int row = page.grid().scrollToAndSelect("Certificate Id", username);

		page.getEditBtn().click();
		PluginUserModal pum = new PluginUserModal(driver);
		Reporter.log("Change Role of selected plugin user");
		log.info("Change Role of selected plugin user");
		pum.getRolesSelect().selectOptionByText(DRoles.ADMIN);
		pum.getOkBtn().click();

		page.getCancelBtn().click();
		new Dialog(driver).confirm();
		soft.assertTrue(role.equals(page.grid().getRowSpecificColumnVal(row, "Role")), "Data remains same due to cancel event");
		soft.assertAll();

	}

	/* EDELIVERY-7196 - PU-38 - Edit certificate user and Save */
	@Test(description = "PU-38", groups = {"multiTenancy", "singleTenancy"})
	public void editCertificateUsr() throws Exception {
		String certName = Gen.randomAlphaNumeric(5);
		String role = DRoles.USER;
		String username = String.format("CN=%s,O=eDelivery,C=BE:%s", certName, Gen.randomAlphaNumeric(5));
		rest.pluginUsers().createCertPluginUser(username, role, null);
		Reporter.log("new Certificate User " + username);
		log.info("new Certificate User " + username);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = navigateToPluginUserPage();

		Reporter.log("Search CERTIFICATE type plugin users");
		log.info("Search CERTIFICATE type plugin users");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();
		page.grid().scrollToAndSelect("Certificate Id", username);

		page.getEditBtn().click();
		PluginUserModal pum = new PluginUserModal(driver);
		Reporter.log("Change Role of selected plugin user");
		log.info("Change Role of selected plugin user");
		pum.getRolesSelect().selectOptionByText(DRoles.ADMIN);
		pum.getOkBtn().click();

		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PLUGINUSER_SAVE_SUCCESS), "User is updated successfully");
		soft.assertAll();

	}

	/* EDELIVERY-7198 - PU-40 - Delete certificate user and Save */
	@Test(description = "PU-40", groups = {"multiTenancy", "singleTenancy"})
	public void delCertificateUsr() throws Exception {

		String certName = Gen.randomAlphaNumeric(5);
		String role = DRoles.USER;
		String username = String.format("CN=%s,O=eDelivery,C=BE:%s", certName, Gen.randomAlphaNumeric(5));
		rest.pluginUsers().createCertPluginUser(username, role, null);
		Reporter.log("new Certificate User " + username);
		log.info("new Certificate User " + username);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = navigateToPluginUserPage();

		Reporter.log("Search CERTIFICATE type plugin users");
		log.info("Search CERTIFICATE type plugin users");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();
		Reporter.log("index" + page.grid().getIndexOf(0, username));
		log.info("index" + page.grid().getIndexOf(0, username));
		page.grid().scrollToAndSelect("Certificate Id", username);

		page.getDeleteBtn().click();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PLUGINUSER_SAVE_SUCCESS), "User is deleted successfully");
		soft.assertTrue(page.grid().getIndexOf(0, username) < 0, "User is not present in grid");
		soft.assertAll();

	}

	/* EDELIVERY-7197 - PU-39 - Delete certificate user and Cancel */
	@Test(description = "PU-39", groups = {"multiTenancy", "singleTenancy"})
	public void certificateUsrDelCancel() throws Exception {

		String certName = Gen.randomAlphaNumeric(5);
		String username = String.format("CN=%s,O=eDelivery,C=BE:%s", certName, Gen.randomAlphaNumeric(5));
		rest.pluginUsers().createCertPluginUser(username, DRoles.USER, null);
		Reporter.log("new Certificate User " + username);
		log.info("new Certificate User " + username);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = navigateToPluginUserPage();

		Reporter.log("Search CERTIFICATE type plugin users");
		log.info("Search CERTIFICATE type plugin users");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();
		page.grid().scrollToAndSelect("Certificate Id", username);

		page.getDeleteBtn().click();
		page.getCancelBtn().click();
		new Dialog(driver).confirm();
		page.grid().waitForRowsToLoad();
		soft.assertTrue(page.grid().scrollTo("Certificate Id", username) > -1, "User is present in grid");
		soft.assertAll();

	}

	/* EDELIVERY-7202 - PU-44 - Original user field is mandatory when adding or editing CERTIFICATE plugin user with role user */
	@Test(description = "PU-44", groups = {"multiTenancy", "singleTenancy"})
	public void originalUserOnAddEdit() throws Exception {

		String certName = Gen.randomAlphaNumeric(5);
		String username = String.format("CN=%s,O=eDelivery,C=BE:%s", certName, Gen.randomAlphaNumeric(5));

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = navigateToPluginUserPage();

		Reporter.log("Search CERTIFICATE type plugin users");
		log.info("Search CERTIFICATE type plugin users");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();
		page.getNewBtn().click();

		PluginUserModal pum = new PluginUserModal(driver);
		pum.fillCertUserData(username, DRoles.USER);

		soft.assertTrue(pum.getOkBtn().isEnabled(), "Ok button is enabled after original user data entrance");
		pum.getOkBtn().click();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();

		page.grid().scrollToAndSelect("Certificate Id", username);
		page.getEditBtn().click();
		pum.getCancelBtn().click();

		soft.assertAll();

	}


	/* EDELIVERY-7201 - PU-43 - Original user field is mandatory when editing CERTIFICATE admin plugin user to role user */
	@Test(description = "PU-43", groups = {"multiTenancy", "singleTenancy"})
	public void originalUserOnRoleChange() throws Exception {

		String certName = Gen.randomAlphaNumeric(5);
		String username = String.format("CN=%s,O=eDelivery,C=BE:%s", certName, Gen.randomAlphaNumeric(5));
		rest.pluginUsers().createCertPluginUser(username, DRoles.ADMIN, null);

		String originalUser = Gen.randomOrigUsrStr();

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = navigateToPluginUserPage();

		Reporter.log("Search CERTIFICATE type plugin users");
		log.info("Search CERTIFICATE type plugin users");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();
		page.grid().scrollToAndSelect("Certificate Id", username);
		page.getEditBtn().click();
		PluginUserModal pum = new PluginUserModal(driver);

		pum.getRolesSelect().selectOptionByText(DRoles.USER);
		pum.getOriginalUserInput().fill(originalUser);

		pum.getOkBtn().click();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PLUGINUSER_SAVE_SUCCESS), "Success message is shown");
		soft.assertAll();
	}

	/* EDELIVERY-7199 - PU-41 - Original user field is mandatory when adding or editing a BASIC plugin user with role user */
	@Test(description = "PU-41", groups = {"multiTenancy", "singleTenancy"})
	public void originalUserOnAddEditBasic() throws Exception {

		String username = Gen.randomAlphaNumeric(5);

		String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:" + Gen.randomAlphaNumeric(2);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = navigateToPluginUserPage();

		page.getNewBtn().click();
		PluginUserModal pum = new PluginUserModal(driver);
		pum.fillData(username, DRoles.USER, data.defaultPass(), data.defaultPass());

		pum.getOkBtn().click();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PLUGINUSER_SAVE_SUCCESS), "Success message is shown");

		page.grid().scrollToAndSelect("User Name", username);
		page.getEditBtn().click();
		pum.getOriginalUserInput().clear();
		soft.assertTrue(pum.getOkBtn().isDisabled(), "Ok button is disabled");
		pum.getOriginalUserInput().fill(originalUser);

		pum.getOkBtn().click();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PLUGINUSER_SAVE_SUCCESS), "Success message is shown");
		soft.assertAll();


	}

	/* EDELIVERY-7200 - PU-42 - Original user field is mandatory when editing BASIC plugin user to role user */
	@Test(description = "PU-42", groups = {"multiTenancy", "singleTenancy"})
	public void originalUserOnRoleChangeBasic() throws Exception {

		String username = Gen.randomAlphaNumeric(5);
		String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:" + Gen.randomAlphaNumeric(2);

		rest.pluginUsers().createPluginUser(username, DRoles.ADMIN, null, null);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = navigateToPluginUserPage();

		page.grid().scrollToAndSelect("User Name", username);
		page.getEditBtn().click();
		PluginUserModal pum = new PluginUserModal(driver);

		pum.getRolesSelect().selectOptionByText(DRoles.USER);
		pum.getOriginalUserInput().fill(originalUser);

		pum.getOkBtn().click();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PLUGINUSER_SAVE_SUCCESS), "User is edited successfully");
		soft.assertAll();


	}

	/* EDELIVERY-7206 - PU-45 - Create CERTIFICATE plugin user with duplicated certificate ID on different domain */
	@Test(description = "PU-45", groups = {"multiTenancy"})
	public void duplicatCertUserDiffDomain() throws Exception {

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = navigateToPluginUserPage();

		String domainName = page.getDomainFromTitle();

		String certName = Gen.randomAlphaNumeric(5);
		String username = String.format("CN=%s,O=eDelivery,C=BE:%s", certName, Gen.randomAlphaNumeric(5));
		rest.pluginUsers().createCertPluginUser(username, DRoles.USER, domainName);
		Reporter.log("new Certificate User " + username);
		log.info("new Certificate User " + username);

		page.getDomainSelector().selectAnotherDomain();
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");

		page.grid().waitForRowsToLoad();
		page.getNewBtn().click();
		PluginUserModal pum = new PluginUserModal(driver);

		pum.fillCertUserData(username, DRoles.ADMIN);
		pum.getOkBtn().click();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		soft.assertTrue(page.getAlertArea().isError(), "Error message displayed");
		String expectedMessage = String.format(DMessages.PLUGINUSER_DUPLICATE_USERNAME, username, domainName);
		soft.assertEquals(page.getAlertArea().getAlertMessage(), expectedMessage, "Correct message is displayed");
		soft.assertAll();

	}

	private PluginUsersPage navigateToPluginUserPage() throws Exception {
		Reporter.log("logged in");
		log.info("logged in");
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();
		return page;
	}


}
