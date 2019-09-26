package domibus.ui.ux;

import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.BaseUXTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.users.UserModal;
import pages.users.UsersPage;
import utils.Generator;
import utils.TestUtils;

import java.util.HashMap;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class UsersPgUXTest extends BaseUXTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.USERS);

	/* Login as super admin and open Users page */
	@Test(description = "USR-1", groups = {"multiTenancy", "singleTenancy"})
	public void openWindow() throws Exception {
		SoftAssert soft = new SoftAssert();

		UsersPage page = new UsersPage(driver);
		page.getSidebar().gGoToPage(PAGES.USERS);

		log.info("checking page default state");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}

		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");

		soft.assertAll();
	}

	/* Doubleclick on one user (active) */
	@Test(description = "USR-2", groups = {"multiTenancy", "singleTenancy"})
	public void openDoubleClickModal() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = getUser(null, DRoles.USER, true, false, false).getString("userName");
		log.info("found user " + username);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().gGoToPage(PAGES.USERS);
		page.refreshPage();

		log.info("double clicking on user");
		page.grid().scrollToAndDoubleClick("Username", username);

		UserModal um = new UserModal(driver);
		soft.assertTrue(um.isLoaded(), "Doubleclick opens modal");

		soft.assertEquals(username, um.getUserNameInput().getText(), "Usernames match");
		soft.assertEquals(DRoles.USER, um.getRoleSelect().getSelectedValue(), "Roles match");

		if (data.isIsMultiDomain()) {
			soft.assertEquals(um.getDomainSelect().getSelectedValue(), "Default", "Domain matches selected domain in page header");
		}
		soft.assertAll();
	}

	/* Doubleclick on one user (deleted) */
	@Test(description = "USR-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickDeletedUser() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = getUser(null, DRoles.USER, true, true, false).getString("userName");
		log.info("found user " + username);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().gGoToPage(PAGES.USERS);
		page.refreshPage();

		log.info("double clicking on user");
		page.grid().scrollToAndDoubleClick("Username", username);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.USER_CANNOT_EDIT_DELETED, "Page shows error message");

		soft.assertTrue(!page.getEditBtn().isEnabled(), "Edit button is not enabled for deleted users!");
		soft.assertTrue(!page.getDeleteBtn().isEnabled(), "Delete button is not enabled for deleted users!");

		soft.assertAll();
	}

	/* Admin adds invalid email */
	@Test(description = "USR-13", groups = {"multiTenancy", "singleTenancy"})
	public void addEditInvalidEmail() throws Exception {
		SoftAssert soft = new SoftAssert();

//		edit scenario
		String username = getUser(null, DRoles.USER, true, false, false).getString("userName");
		log.info("found user " + username);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().gGoToPage(PAGES.USERS);
		page.refreshPage();

		log.info("double clicking on user");
		page.grid().scrollToAndDoubleClick("Username", username);

		log.info("adding invalid email");
		UserModal modal = new UserModal(driver);
		modal.getEmailInput().fill("invalidEmail@");

		soft.assertTrue(modal.getEmailErrMess().isPresent(), "When entering an invalid email an error message is shown");
		soft.assertTrue(StringUtils.equalsIgnoreCase(modal.getEmailErrMess().getText(), DMessages.USER_EMAIL_INVALID), "When entering an invalid email the CORRECT error message is shown");

		page.clickVoidSpace();

//		new user scenario

		page.getNewBtn().click();
		log.info("adding invalid email");
		modal.getEmailInput().fill("invalidEmail@");

		soft.assertTrue(modal.getEmailErrMess().isPresent(), "When entering an invalid email an error message is shown (new)");
		soft.assertTrue(StringUtils.equalsIgnoreCase(modal.getEmailErrMess().getText(), DMessages.USER_EMAIL_INVALID), "When entering an invalid email the CORRECT error message is shown (new)");

		soft.assertAll();
	}




//	@Test(description = "USR-9", groups = {"multiTenancy", "singleTenancy"})
//	public void newUserPopupValidations() throws Exception {
//		SoftAssert soft = new SoftAssert();
//		UsersPage page = new UsersPage(driver);
//		page.getSidebar().gGoToPage(PAGES.USERS);
//
////		create new user
//		log.info("click New");
//		page.getNewBtn().click();
//		UserModal um = new UserModal(driver);
//
//		log.info("user enters invalid data");
//		um.fillData("", "asdsa", "", "assd", "asdaa");
//
//		log.info("checking listed error messages");
//		soft.assertEquals(um.getUsernameErrMess().getText(), DMessages.USER_USERNAME_NO_EMPTY, "Correct username err message appears");
//		soft.assertEquals(um.getEmailErrMess().getText(), DMessages.USER_EMAIL_INVALID, "Correct email err message appears");
//		soft.assertEquals(um.getPassErrMess().getText(), DMessages.PASS_POLICY_MESSAGE, "Correct pass err message appears");
//		soft.assertEquals(um.getConfirmationErrMess().getText(), DMessages.PASS_NO_MATCH_MESSAGE, "Correct pass err message appears");
//
//		soft.assertTrue(!um.isOKBtnEnabled(), "OK button is not enabled for invalid input");
//
//		soft.assertAll();
//	}

	/* Admin wants to edit username */
	@Test(description = "USR-11", groups = {"multiTenancy", "singleTenancy"})
	public void editUsername() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().gGoToPage(PAGES.USERS);

		String username = getUser(null, DRoles.USER, true, false, false).getString("userName");
		log.info("test for user " + username);

		page.grid().scrollToAndDoubleClick("Username", username);
		log.info("modal opened");

		soft.assertTrue(!new UserModal(driver).getUserNameInput().isEnabled(), "Username input is not available for editing");

		soft.assertAll();
	}
//
//	@Test(description = "USR-10", groups = {"multiTenancy", "singleTenancy"})
//	public void inactiveUserLogin() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
////		create new user
//		page.getNewBtn().click();
//		UserModal um = new UserModal(driver);
//		um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
//		um.getActiveChk().uncheck();
//
//		um.clickOK();
//
//		page.saveAndConfirm();
//
//		page.getSandwichMenu().logout();
//		LoginPage loginPage = new LoginPage(driver);
//		loginPage.login(username, data.getDefaultTestPass());
//
//		soft.assertTrue(!loginPage.getSandwichMenu().isLoggedIn(), "User is not logged in");
//		soft.assertTrue(loginPage.getAlertArea().isError(), "Error message shown");
//		soft.assertEquals(loginPage.getAlertArea().getAlertMessage(), DMessages.LOGIN_USER_INACTIVE, "Correct error message is shown");
//
//
//		loginPage.login(data.getAdminUser());
//		loginPage.getSidebar().gGoToPage(PAGES.USERS);
//
//		page.grid().scrollToAndSelect("Username", username);
//		page.getEditBtn().click();
//
//		um = new UserModal(driver);
//		um.getActiveChk().check();
//		um.clickOK();
//		page.saveAndConfirm();
//
//		page.getSandwichMenu().logout();
//		loginPage.login(username, data.getDefaultTestPass());
//
//		soft.assertTrue(loginPage.getSandwichMenu().isLoggedIn(), "Active user is logged in");
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-11", groups = {"multiTenancy", "singleTenancy"})
//	public void editUserRoleAndCheckPrivileges() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//		rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), "default");
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
//		int index = page.grid().scrollTo("Username", username);
//		if (index > -1) {
//			page.grid().scrollToAndSelect("Username", username);
//			page.getEditBtn().click();
//			UserModal um = new UserModal(driver);
//			um.getRoleSelect().selectOptionByText(DRoles.USER);
//
//			um.clickOK();
//			page.saveAndConfirm();
//
//			page.getSandwichMenu().logout();
//			LoginPage loginPage = new LoginPage(driver);
//			loginPage.login(username, data.getDefaultTestPass());
//
//			soft.assertEquals(loginPage.getSidebar().availableOptions().size(), 2, "User has only 2 options available in sidebar");
//
//		}
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-12", groups = {"multiTenancy", "singleTenancy"})
//	public void duplicateUsername() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//		rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), "default");
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
//
//		int index = page.grid().scrollTo("Username", username);
//		if (index > -1) {
//
//			page.getNewBtn().click();
//			UserModal um = new UserModal(driver);
//			um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
//
//			um.clickOK();
//			page.getSaveBtn().click();
//
//			soft.assertEquals(page.getAlertArea().isError(), true, "Error message displayed");
//			soft.assertEquals(page.getAlertArea().getAlertMessage(), "Duplicate user name for user: " + username + ".", "Correct message displayed");
//
//		}
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-13", groups = {"multiTenancy"})
//	public void duplicateUsernameOnAnotherDomain() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//		List<String> domains = rest.getDomainNames();
//		rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), domains.get(1));
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//		page.getDomainSelector().selectOptionByText(domains.get(0));
//
//		page.getNewBtn().click();
//		UserModal um = new UserModal(driver);
//		um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
//		um.clickOK();
//
//		page.saveAndConfirm();
//
//		soft.assertEquals(page.getAlertArea().isError(), true, "Error message displayed");
//		String expectedError = String.format(DMessages.USER_DUPLICATE_USERNAME, username, domains.get(1));
//		soft.assertEquals(page.getAlertArea().getAlertMessage(), expectedError, "Correct message is displayed");
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-14", groups = {"multiTenancy"})
//	public void duplicateUserVSPluginUser() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//		rest.createPluginUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
//		page.getNewBtn().click();
//		UserModal um = new UserModal(driver);
//		um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
//
//		um.clickOK();
//		page.saveAndConfirm();
//
//		soft.assertEquals(page.getAlertArea().isError(), true, "Error message displayed");
//		String expectedMessage = String.format(DMessages.USER_DUPLICATE_USERNAME, username, "default");
//		soft.assertEquals(page.getAlertArea().getAlertMessage(), expectedMessage, "Correct message displayed");
//
//		rest.deletePluginUser(username, null);
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-15", groups = {"multiTenancy"})
//	public void duplicateUserVSPluginUserOtherDomain() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//		List<String> domains = rest.getDomainNames();
//		rest.createPluginUser(username, DRoles.ADMIN, data.getDefaultTestPass(), domains.get(1));
//
//		SoftAssert soft = new SoftAssert();
//
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
//		page.getDomainSelector().selectOptionByText(domains.get(0));
//
//		page.getNewBtn().click();
//		UserModal um = new UserModal(driver);
//		um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
//		um.clickOK();
//
//		page.saveAndConfirm();
//
//		soft.assertEquals(page.getAlertArea().isError(), true, "Error message displayed");
//
//		String expectedMessage = String.format(DMessages.USER_DUPLICATE_USERNAME, username, domains.get(1));
//		soft.assertEquals(page.getAlertArea().getAlertMessage(), expectedMessage, "Correct message displayed");
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-16", groups = {"multiTenancy", "singleTenancy"})
//	public void downloadUserList() throws Exception {
//		SoftAssert soft = new SoftAssert();
//		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());
//
//		String fileName = rest.downloadGrid(RestServicePaths.USERS_CSV, null, null);
//
//		page.grid().getGridCtrl().showCtrls();
//		page.grid().getGridCtrl().getAllLnk().click();
//		page.grid().getGridCtrl().uncheckBoxWithLabel("Password");
//
//		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");
//
//		page.getUsersGrid().checkCSVvsGridInfo(fileName, soft);
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-17", groups = {"multiTenancy", "singleTenancy"})
//	public void userNameValidations() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
//		soft.assertTrue(page.isLoaded(), "Page is loaded");
//
//		soft.assertTrue(page.grid().getRowsNo() > 0, "Grid lists existing users");
//		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");
//
//		page.getNewBtn().click();
//		UserModal modal = new UserModal(driver);
//
//		modal.getUserNameInput().fill("t");
//		soft.assertEquals(modal.getUsernameErrMess().getText(), DMessages.USER_USERNAME_VALIDATION, "Correct error message shown (1)");
//
//		modal.getUserNameInput().fill("te");
//		soft.assertEquals(modal.getUsernameErrMess().getText(), DMessages.USER_USERNAME_VALIDATION, "Correct error message shown (2)");
//
//		modal.getUserNameInput().fill("te$%^*");
//		soft.assertEquals(modal.getUsernameErrMess().getText(), DMessages.USER_USERNAME_VALIDATION, "Correct error message shown (3)");
//
//		modal.getUserNameInput().fill("testUser");
//
//		String errMess = null;
//		try {
//			errMess = modal.getUsernameErrMess().getText();
//		} catch (Exception e) {
//		}
//
//		soft.assertNull(errMess, "When correct username is entered the error message dissapears");
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-18", groups = {"multiTenancy", "singleTenancy"})
//	public void adminDeleteSelf() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//		String password = data.getDefaultTestPass();
//		rest.createUser(username, DRoles.ADMIN, password, null);
//
//		SoftAssert soft = new SoftAssert();
//		login(username, password).getSidebar().gGoToPage(PAGES.USERS);
//
//		UsersPage page = new UsersPage(driver);
//		page.grid().scrollToAndSelect("Username", username);
//
//		page.getDeleteBtn().click();
//
//		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
//		soft.assertEquals(page.getAlertArea().getAlertMessage(),
//				String.format(DMessages.USER_DELETE_LOGGED_IN_USER, username),
//				"Correct error message is shown");
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-19", groups = {"multiTenancy"})
//	public void availableRoles() throws Exception {
//
//		String adminUsername = Generator.randomAlphaNumeric(9);
//		String superUsername = Generator.randomAlphaNumeric(9);
//		String password = data.getDefaultTestPass();
//		rest.createUser(adminUsername, DRoles.ADMIN, password, null);
//		rest.createUser(superUsername, DRoles.SUPER, password, null);
//
//		SoftAssert soft = new SoftAssert();
//		login(adminUsername, password).getSidebar().gGoToPage(PAGES.USERS);
//
//		UsersPage page = new UsersPage(driver);
//
//		page.getNewBtn().click();
//		UserModal modal = new UserModal(driver);
//
//		List<String> roles = modal.getRoleSelect().getOptionsTexts();
//
//		soft.assertTrue(roles.size() == 2, "2 roles available to admin");
//		soft.assertTrue(roles.contains(DRoles.USER), "User role is avalable to admin");
//		soft.assertTrue(roles.contains(DRoles.ADMIN), "Admin role is avalable to admin");
//
//		page.getSandwichMenu().logout();
//		login(superUsername, password).getSidebar().gGoToPage(PAGES.USERS);
//
//		page = new UsersPage(driver);
//
//		page.getNewBtn().click();
//		modal = new UserModal(driver);
//
//		List<String> rolesSuper = modal.getRoleSelect().getOptionsTexts();
//
//		soft.assertTrue(rolesSuper.size() == 3, "3 roles available to admin");
//		soft.assertTrue(rolesSuper.contains(DRoles.USER), "User role is avalable to super");
//		soft.assertTrue(rolesSuper.contains(DRoles.ADMIN), "Admin role is avalable to super");
//		soft.assertTrue(rolesSuper.contains(DRoles.SUPER), "Super Admin role is avalable to super");
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-20", groups = {"multiTenancy", "singleTenancy"})
//	public void gridSelfAssert() throws Exception {
//		SoftAssert soft = new SoftAssert();
//		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());
//
//		page.grid().assertControls(soft);
//
//		soft.assertAll();
//	}

	public JSONObject getUser(String domain, String role, boolean active, boolean deleted, boolean forceNew) throws Exception {
		String username = Generator.randomAlphaNumeric(10);

		if(StringUtils.isEmpty(domain)){ domain = "default";}

		if (!forceNew) {
			log.info("trying to find existing user with desired config");
			JSONArray users = rest.getUsers();
			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(user.getString("userName"), "super")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "admin")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "user")
				) {
					log.info("skipping default users");
					continue;
				}

				if (StringUtils.equalsIgnoreCase(user.getString("domain"), domain)
						&& StringUtils.equalsIgnoreCase(user.getString("roles"), role)
						&& user.getBoolean("active") == active
						&& user.getBoolean("deleted") == deleted) {
					log.info("found user " + user.getString("userName"));
					return user;
				}
			}
		}

		rest.createUser(username, role, data.getDefaultTestPass(), domain);
		log.info("created user " + username);
		if(deleted){
			rest.deleteUser(username, domain);
			log.info("deleted user " + username);
		}
		if(!active){
			rest.blockUser(username);
			log.info("deactivated user " + username);
		}

		JSONArray users = rest.getUsers();
		log.info("searching for user in the system");
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(user.getString("userName"), username)){
				log.info("user found and returned");
				return user;
			}
		}
		log.info("user not found .. returning null");
		return null;
	}


}
