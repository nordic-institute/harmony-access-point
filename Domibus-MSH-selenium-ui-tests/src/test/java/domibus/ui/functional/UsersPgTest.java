package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.json.JSONArray;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.users.UserModal;
import pages.users.UsersPage;
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


public class UsersPgTest extends SeleniumTest {
	
	ArrayList<String> ADMIN_VISIBLE_ROLES = new ArrayList<>(Arrays.asList(DRoles.ADMIN, DRoles.USER));
	
	ArrayList<String> SUPER_NEW_VISIBLE_ROLES = new ArrayList<>(Arrays.asList(DRoles.SUPER, DRoles.ADMIN, DRoles.USER));
	
	ArrayList<String> SUPER_EDIT_USER_VISIBLE_ROLES = new ArrayList<>(Arrays.asList(DRoles.ADMIN, DRoles.USER));
	ArrayList<String> SUPER_EDIT_ADMIN_VISIBLE_ROLES = new ArrayList<>(Arrays.asList(DRoles.ADMIN, DRoles.USER));
	ArrayList<String> SUPER_EDIT_SUPER_VISIBLE_ROLES = new ArrayList<>(Arrays.asList(DRoles.SUPER));
	
	private boolean testRoleList(List<String> expectedOptions, UserModal modal) throws Exception {
		log.info("getting visible options");
		List<String> visibleOptions = modal.getRoleSelect().getOptionsTexts();
		return TestUtils.isEqualListContent(visibleOptions, expectedOptions);
	}
	
	
	private UsersPage loginAndGoToUsersPage(HashMap<String, String> user) throws Exception {
		log.info("Login with user" + user);
		login(user).getSidebar().goToPage(PAGES.USERS);
		UsersPage page = new UsersPage(driver);
		page.grid().waitForRowsToLoad();
		return page;
	}
	
	private UsersPage loginAndGoToUsersPage(String user, String pass) throws Exception {
		HashMap<String, String> userInfo = new HashMap<>();
		userInfo.put("username", user);
		userInfo.put("pass", pass);
		
		return loginAndGoToUsersPage(userInfo);
	}
	
	/* Admin deletes user and presses Save */
	@Test(description = "USR-10", groups = {"multiTenancy", "singleTenancy"})
	public void deleteUserAndSave() throws Exception {
		String username = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");
		
		SoftAssert soft = new SoftAssert();
		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());
		page.includeDeletedUsers();
		page.grid().waitForRowsToLoad();
		
		log.info("Selecting user " + username);
		page.grid().scrollToAndSelect("Username", username);
		
		log.info("Press Delete button");
		page.getDeleteBtn().click();
		
		soft.assertTrue(page.getSaveBtn().isEnabled(), "After pressing delete the Save button is active");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "After pressing delete the Cancel button is active");
		
		page.saveAndConfirm();
		
		soft.assertTrue(page.getUsersGrid().isDeleted(username), "User presented as deleted in the grid");
		
		soft.assertAll();
	}
	
	/* Admin deletes user and presses Cancel */
	@Test(description = "USR-9", groups = {"multiTenancy", "singleTenancy"})
	public void deleteUserAndCancel() throws Exception {
		String username = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");
		
		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());
		
		UsersPage page = new UsersPage(driver);
		
		log.info("Selecting user " + username);
		int index = page.grid().scrollTo("Username", username);
		page.grid().selectRow(index);
		
		log.info("Press Delete button");
		page.getDeleteBtn().click();
		
		soft.assertTrue(page.getSaveBtn().isEnabled(), "After pressing delete the Save button is active");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "After pressing delete the Cancel button is active");
		
		page.cancelAndConfirm();
		
		soft.assertFalse(page.getUsersGrid().isDeleted(username), "User presented as NOT deleted in the grid");
		
		soft.assertAll();
	}
	
	/* Available roles in Role drop down on new/edit pop up */
	@Test(description = "USR-12", groups = {"multiTenancy", "singleTenancy"})
	public void availableRolesAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String adminUser = rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName");
		String toEditUser = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");

		log.info("got user " + toEditUser);
		log.info("got admin " + adminUser);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		if (data.isMultiDomain()) {
			String superUser = rest.getUser(null, DRoles.SUPER, true, false, true).getString("userName");

			page.refreshPage();

			log.info("click NEW");
			page.getNewBtn().click();

			UserModal modal = new UserModal(driver);

			soft.assertTrue(testRoleList(SUPER_NEW_VISIBLE_ROLES, modal), "All roles available for SUPER when creating new user");
			log.info("closing modal");
			page.clickVoidSpace();

			log.info("editing user " + toEditUser);
			page.grid().scrollToAndSelect("Username", toEditUser);
			page.getEditBtn().click();
			soft.assertTrue(testRoleList(SUPER_EDIT_USER_VISIBLE_ROLES, modal), "All roles available for SUPER when editing a user");
			log.info("closing modal");
			page.clickVoidSpace();

			log.info("editing admin " + adminUser);
			page.grid().scrollToAndSelect("Username", adminUser);
			page.getEditBtn().click();
			soft.assertTrue(testRoleList(SUPER_EDIT_ADMIN_VISIBLE_ROLES, modal), "All roles available for SUPER when editing an ADMIN");
			log.info("closing modal");
			page.clickVoidSpace();

			log.info("editing super user " + superUser);
			page.grid().scrollToAndSelect("Username", superUser);
			page.getEditBtn().click();
			soft.assertTrue(testRoleList(SUPER_EDIT_SUPER_VISIBLE_ROLES, modal), "All roles available for SUPER when editing an SUPER");
			log.info("closing modal");
			page.clickVoidSpace();


			login(adminUser, data.defaultPass()).getSidebar().goToPage(PAGES.USERS);
		}

		log.info("click NEW");
		page.getNewBtn().click();
		UserModal modal = new UserModal(driver);
		soft.assertTrue(testRoleList(ADMIN_VISIBLE_ROLES, modal), "Roles available for ADMIN");
		
		log.info("closing user modal");
		page.clickVoidSpace();
		
		log.info("editing user " + toEditUser);
		page.grid().scrollToAndSelect("Username", toEditUser);
		page.getEditBtn().click();
		soft.assertTrue(testRoleList(ADMIN_VISIBLE_ROLES, modal), "Roles available for ADMIN");
		
		log.info("closing user modal");
		page.clickVoidSpace();
		

		
		soft.assertAll();
	}
	
	
	/* USR-4 - Create new user and press cancel */
	@Test(description = "USR-4", groups = {"multiTenancy", "singleTenancy"})
	public void newUserCancel() throws Exception {
		
		String username = Gen.randomAlphaNumeric(9);
		
		SoftAssert soft = new SoftAssert();
		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());
		
		soft.assertTrue(page.getCancelBtn().isDisabled(), "Cancel button is disabled on page load");

//		create new user
		log.info("creating new user " + username);
		page.newUser(username, "tuser@bnc.com", DRoles.ADMIN, data.defaultPass(), data.defaultPass());
		page.grid().waitForRowsToLoad();
		
		log.info("Press cancel");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");
		page.cancelAndConfirm();
		
		log.info("searching for user in grid");
		int index = page.grid().scrollTo("Username", username);
		soft.assertEquals(index, -1, "User not present in the list of users");
		
		soft.assertAll();
	}
	
	/* USR-6 - Admin creates new user and presses Save */
	@Test(description = "USR-6", groups = {"multiTenancy", "singleTenancy"})
	public void newUserSave() throws Exception {
		
		String username = Gen.randomAlphaNumeric(9);
		
		SoftAssert soft = new SoftAssert();
		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());
		
		soft.assertTrue(page.getSaveBtn().isDisabled(), "Save button is disabled on page load");

//		create new user
		log.info("creating new user " + username);
		page.newUser(username, "tuser@bnc.com", DRoles.ADMIN, data.defaultPass(), data.defaultPass());
		page.grid().waitForRowsToLoad();
		
		log.info("Press Save");
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled after new user creation");
		page.saveAndConfirm();
		
		log.info("searching for user in grid");
		int index = page.grid().scrollTo("Username", username);
		soft.assertTrue(index > -1, "User present in the list of users");
		
		soft.assertAll();
	}
	
	/* USR-7 - Admin edits an existing user and presses Cancel */
	@Test(description = "USR-7", groups = {"multiTenancy", "singleTenancy"})
	public void editUserAndCancel() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("acquiring user for edit");
		String username = rest.getUser(null, DRoles.USER, false, false, false).getString("userName");
		
		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());
		
		soft.assertTrue(page.getSaveBtn().isDisabled(), "Save button is disabled on page load");
		soft.assertTrue(page.getCancelBtn().isDisabled(), "Cancel button is disabled on page load");
		
		log.info("editing user");
		page.grid().scrollToAndDoubleClick("Username", username);
		
		log.info("make the user active");
		UserModal modal = new UserModal(driver);
		modal.getActiveChk().check();
		
		String email = Gen.randomAlphaNumeric(5) + "@test.com";
		log.info("editing email to " + email);
		modal.getEmailInput().fill(email);
		modal.clickOK();
		
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled after edit");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after edit");
		
		page.cancelAndConfirm();
		
		log.info("checking edited values");
		page.grid().scrollToAndDoubleClick("Username", username);
		modal = new UserModal(driver);
		
		soft.assertFalse(modal.getActiveChk().isChecked(), "User is still disabled");
		soft.assertNotEquals(modal.getEmailInput().getText(), email, "User email is NOT the one set by editing");
		
		soft.assertAll();
	}
	
	/* USR-7 - Admin edits an existing user and presses Save */
	@Test(description = "USR-8", groups = {"multiTenancy", "singleTenancy"})
	public void editUserAndSave() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("acquiring user for edit");
		String username = rest.getUser(null, DRoles.USER, false, false, false).getString("userName");
		
		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());
		
		soft.assertTrue(page.getSaveBtn().isDisabled(), "Save button is disabled on page load");
		soft.assertTrue(page.getCancelBtn().isDisabled(), "Cancel button is disabled on page load");
		
		log.info("editing user");
		page.grid().scrollToAndDoubleClick("Username", username);
		
		log.info("make the user active");
		UserModal modal = new UserModal(driver);
		modal.getActiveChk().check();
		
		String email = Gen.randomAlphaNumeric(5) + "@test.com";
		log.info("editing email to " + email);
		modal.getEmailInput().fill(email);
		modal.clickOK();
		
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled after edit");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after edit");
		
		page.saveAndConfirm();
		
		log.info("checking edited values");
		page.grid().scrollToAndDoubleClick("Username", username);
		modal = new UserModal(driver);
		
		soft.assertTrue(modal.getActiveChk().isChecked(), "User is enabled");
		soft.assertEquals(modal.getEmailInput().getText(), email, "User email is the one set by editing");
		
		soft.assertAll();
	}
	
	/* USR-15 - Admin unticks the Active checkbox for user (also applies to user creation) */
	@Test(description = "USR-15", groups = {"multiTenancy", "singleTenancy"})
	public void adminDeactivatesUser() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);
		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());

//		admin creates new disabled user
		log.info("Create new disabled user");
		page.getNewBtn().click();
		
		log.info("Creating user " + username);
		UserModal modal = new UserModal(driver);
		modal.fillData(username, null, DRoles.USER, data.defaultPass(), data.defaultPass());
		
		log.info("Uncheck the active checkbox");
		modal.getActiveChk().uncheck();
		modal.clickOK();
		
		page.saveAndConfirm();
		log.info(page.getAlertArea().getAlertMessage());
		
		log.info("logging out");
		page.getSandwichMenu().logout();
		DomibusPage pg = login(username, data.defaultPass());
		soft.assertTrue(pg.getAlertArea().isError(), "Error displayed for deactivated user trying to login");
		soft.assertEquals(pg.getAlertArea().getAlertMessage(), DMessages.LOGIN_USER_INACTIVE, "User is notified that his account is inactive");

//		admin edits to disable active user
		log.info("Disable active user");
		username = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");
		
		page = loginAndGoToUsersPage(data.getAdminUser());
		
		log.info("editing user " + username);
		page.grid().scrollToAndSelect("Username", username);
		page.getEditBtn().click();
		
		modal = new UserModal(driver);
		
		log.info("Uncheck the active checkbox");
		modal.getActiveChk().uncheck();
		modal.clickOK();
		
		page.saveAndConfirm();
		log.info(page.getAlertArea().getAlertMessage());
		
		log.info("logging out");
		page.getSandwichMenu().logout();
		
		pg = login(username, data.defaultPass());
		soft.assertTrue(pg.getAlertArea().isError(), "Error displayed for deactivated user trying to login");
		soft.assertEquals(pg.getAlertArea().getAlertMessage(), DMessages.LOGIN_USER_INACTIVE, "User is notified that his account is inactive");
		
		soft.assertAll();
	}
	
	/* USR-14 - Admin changes password (also applies to user creation) */
	@Test(description = "USR-14", groups = {"multiTenancy", "singleTenancy"})
	public void adminChangesUserPassword() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, DRoles.USER, true, false, true).getString("userName");
		
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		
		log.info("changing password for " + username);
		page.grid().scrollToAndSelect("Username", username);
		page.getEditBtn().click();
		
		UserModal modal = new UserModal(driver);
		modal.getPasswordInput().fill(data.getNewTestPass());
		modal.getConfirmationInput().fill(data.getNewTestPass());
		modal.clickOK();
		
		log.info("Saving");
		page.saveAndConfirm();
		
		log.info("admin logging out");
		page.getSandwichMenu().logout();
		
		log.info("try to login with old password");
		DomibusPage pg = login(username, data.defaultPass());
		soft.assertTrue(pg.getAlertArea().isError(), "Error displayed when trying to login with old password");
		soft.assertEquals(pg.getAlertArea().getAlertMessage(), DMessages.LOGIN_INVALID_CREDENTIALS, "User notified he has wrong credentials");
		
		log.info("try to login with new password");
		pg = login(username, data.getNewTestPass());
		soft.assertTrue(pg.getSandwichMenu().isLoggedIn(), "User can login with new pass");
		
		soft.assertAll();
	}
	
	/*USR-16 - Admin tries to create new user with username less than 3 letters long*/
	@Test(description = "USR-16", groups = {"multiTenancy", "singleTenancy"})
	public void userNameValidations() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		
		
		log.info("click New");
		page.getNewBtn().click();
		
		UserModal modal = new UserModal(driver);
		log.info("checking with only one letter");
		modal.getUserNameInput().fill("t");
		soft.assertEquals(modal.getUsernameErrMess().getText(), DMessages.USER_USERNAME_VALIDATION_SHORT, "Correct error message shown (1)");
		
		log.info("checking with only two letters");
		modal.getUserNameInput().fill("te");
		soft.assertEquals(modal.getUsernameErrMess().getText(), DMessages.USER_USERNAME_VALIDATION_SHORT, "Correct error message shown (2)");
		
		log.info("checking with only two letters and special characters");
		modal.getUserNameInput().fill("te$%^*");
		soft.assertEquals(modal.getUsernameErrMess().getText(), DMessages.USER_USERNAME_VALIDATION_SPECIAL_CHR, "Correct error message shown (3)");
		
		log.info("checking with valid username");
		modal.getUserNameInput().fill("testUser");
		
		String errMess = null;
		try {
			errMess = modal.getUsernameErrMess().getText();
		} catch (Exception e) {
		}
		soft.assertNull(errMess, "When correct username is entered the error message dissapears");
		
		soft.assertAll();
	}
	
	/*USR-17 - Admin changes a user role*/
	@Test(description = "USR-17", groups = {"multiTenancy", "singleTenancy"})
	public void editUserRoleAndCheckPrivileges() throws Exception {
		// we need to create a new user, because a random existing one may have a different password
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);
		
		log.info("changing role to User for Admin " + username);
		
		SoftAssert soft = new SoftAssert();
		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());
		
		log.info("editing user:" + username);
		page.grid().scrollToAndDoubleClick("Username", username);
		
		log.info("changing role");
		UserModal um = new UserModal(driver);
		um.getRoleSelect().selectOptionByText(DRoles.USER);
		um.clickOK();
		
		page.saveAndConfirm();
		
		log.info("logout");
		page.getSandwichMenu().logout();
		
		log.info("login with username " + username);
		login(username, data.defaultPass());

//		soft.assertEquals(new DomibusPage(driver).getSidebar().availableOptions().size(), 2, "User has only 2 options available in sidebar");
		soft.assertTrue(new DomibusPage(driver).getSidebar().isUserState(), "User has only 2 options available in sidebar");
		
		// TODO: add other combinations of privileges (User to Admin, Super to Admin and viceversa, Super to user and reverse)
		soft.assertAll();
	}
	
	/*USR-19 - Admin tries to create a user with username that exists already (active deleted) both*/
	@Test(description = "USR-19", groups = {"multiTenancy", "singleTenancy"})
	public void duplicateUsername() throws Exception {
		String username = rest.getUser(null, DRoles.USER, false, false, false).getString("userName");
		String deleted_username = rest.getUser(null, DRoles.USER, false, true, false).getString("userName");
		
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.grid().waitForRowsToLoad();

//		active user
		log.info("creating new user with existing active username");
		page.getNewBtn().click();
		UserModal um = new UserModal(driver);
		um.fillData(username, "", DRoles.USER, data.defaultPass(), data.defaultPass());
		um.clickOK();
		
		page.saveAndConfirm();
		
		log.info("checking error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message displayed");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), String.format(DMessages.Users.DUPLICATE_USERNAME_SAMEDOMAIN_ERROR, username), "Correct message displayed");

//		deleted user
		log.info("creating new user with existing deleted username");
		page.getNewBtn().click();
		um = new UserModal(driver);
		um.fillData(deleted_username, "", DRoles.USER, data.defaultPass(), data.defaultPass());
		um.clickOK();
		
		page.saveAndConfirm();
		
		log.info("checking error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message displayed");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), String.format(DMessages.Users.DUPLICATE_USERNAME_SAMEDOMAIN_ERROR, username + ", " + deleted_username), "Correct message displayed");
		
		soft.assertAll();
	}
	
	/*USR-20 - Admin tries to create a user with username that exists on another domain*/
	@Test(description = "USR-20", groups = {"multiTenancy"})
	public void duplicateUsernameOnAnotherDomain() throws Exception {
		String domainName = rest.getNonDefaultDomain();
		String domainCode = rest.getDomainCodeForName(domainName);
		String username = rest.getUser(domainCode, DRoles.USER, false, false, false).getString("userName");
		String deleted_username = rest.getUser(domainCode, DRoles.USER, false, true, false).getString("userName");
		
		SoftAssert soft = new SoftAssert();
		UsersPage page = loginAndGoToUsersPage(data.getAdminUser());

//		active user
		log.info("creating new user with existing active username");
		page.getNewBtn().click();
		UserModal um = new UserModal(driver);
		um.fillData(username, "", DRoles.USER, data.defaultPass(), data.defaultPass());
		um.clickOK();
		
		page.saveAndConfirm();
		
		log.info("checking error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message displayed");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), String.format(DMessages.Users.DUPLICATE_USERNAME_ERROR, username, domainCode), "Correct message displayed");
		
		page.refreshPage();

//		deleted user
		log.info("creating new user with existing deleted username");
		page.getNewBtn().click();
		um = new UserModal(driver);
		um.fillData(deleted_username, "", DRoles.USER, data.defaultPass(), data.defaultPass());
		um.clickOK();
		
		page.saveAndConfirm();
		
		log.info("checking error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message displayed");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), String.format(DMessages.Users.DUPLICATE_USERNAME_ERROR, deleted_username, domainCode), "Correct message displayed");
		
		
		soft.assertAll();
	}
	
	/*USR-21 - Admin tries to create a user with username that exists on a Plugin user*/
	@Test(description = "USR-21", groups = {"multiTenancy"})
	public void duplicateUserVSPluginUser() throws Exception {
		
		String username = rest.getPluginUser(null, DRoles.ADMIN, true, false).getString("userName");
		log.info("got plugin user " + username);
		
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.grid().waitForRowsToLoad();
		
		log.info("creating new user");
		page.getNewBtn().click();
		UserModal um = new UserModal(driver);
		um.fillData(username, "", DRoles.USER, data.defaultPass(), data.defaultPass());
		um.clickOK();
		
		page.saveAndConfirm();
		
		log.info("checking");
		soft.assertTrue(page.getAlertArea().isError(), "Error message displayed");
		String expectedMessage = String.format(DMessages.USER_DUPLICATE_USERNAME, username, "default");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), expectedMessage, "Correct message displayed");
		
		soft.assertAll();
	}
	
	/*USR-22 - Admin tries to create a user with username that exists on a Plugin user on another domain*/
	@Test(description = "USR-22", groups = {"multiTenancy"})
	public void duplicateUserVSPluginUserOtherDomain() throws Exception {
		String domainName = rest.getNonDefaultDomain();
		String domainCode = rest.getDomainCodeForName(domainName);
		String username = rest.getPluginUser(domainCode, DRoles.ADMIN, true, false).getString("userName");
		log.info("got plugin user " + username + " on domain " + domainCode);
		
		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());
		UsersPage page = new UsersPage(driver);
		
		log.info("creating new user");
		page.getNewBtn().click();
		UserModal um = new UserModal(driver);
		um.fillData(username, "", DRoles.USER, data.defaultPass(), data.defaultPass());
		um.clickOK();
		
		page.saveAndConfirm();
		
		log.info("checking");
		soft.assertTrue(page.getAlertArea().isError(), "Error message displayed");
		String expectedMessage = String.format(DMessages.USER_DUPLICATE_USERNAME, username, domainCode);
		soft.assertEquals(page.getAlertArea().getAlertMessage(), expectedMessage, "Correct message displayed");
		
		soft.assertAll();
	}
	
	/*USR-32 - Delete logged in user*/
	@Test(description = "USR-32", groups = {"multiTenancy", "singleTenancy"})
	public void adminDeleteSelf() throws Exception {
		String username = rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName");
		log.info("created user " + username);
		
		SoftAssert soft = new SoftAssert();
		UsersPage page = loginAndGoToUsersPage(username, data.defaultPass());
		
		log.info("deleting created user");
		page.grid().scrollToAndSelect("Username", username);
		page.getDeleteBtn().click();
		
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.USER_DELETE_LOGGED_IN_USER, username),
				"Correct error message is shown");
		
		soft.assertAll();
	}
	
	/*USR-39 - Change password of Super user by Another super user*/
	@Test(description = "USR-39", groups = {"multiTenancy"})
	public void changePassOtherSuperUsr() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Getting super user");
		String userName = rest.getUser(null, DRoles.SUPER, true, false, true).
				getString("userName");
		log.info("got " + userName);
		
		HashMap<String, String> params = new HashMap<>();
		params.put("password", data.getNewTestPass());
		
		log.info("Change password of new super user  with username :" + userName);
		rest.users().updateUser(userName, params, null);
		
		log.info("logout and login with new password for super user " + userName);
		logout();
		
		DomibusPage page = login(userName, data.getNewTestPass());
		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User is able to login with new password successfully");
		
		soft.assertAll();
	}
	
	
	/*USR-40 - Update/Delete super user when only 1 super user exist*/
	@Test(description = "USR-40", groups = {"multiTenancy"})
	public void superDelUpdateSelf() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Delete all super user except Default one");
		JSONArray userArray = rest.users().getUsers(null);
		int userCount = userArray.length();
		
		for (int i = 0; i < userCount; i++) {
			String userName = userArray.getJSONObject(i).getString("userName");
			String role = userArray.getJSONObject(i).getString("roles");
			if (role.equals("ROLE_AP_ADMIN") && !userName.equals("super")) {
				rest.users().deleteUser(userName, null);
			}
		}
		
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		
		page.grid().scrollToAndSelect("Username", "super");
		page.getDeleteBtn().click();
		soft.assertTrue(page.getAlertArea().getAlertMessage().equals(DMessages.Users.LOGGEDINUSER_DELETE+": super"));
		log.info("Super user can't be deleted as no other super user exists");
		
		page.grid().scrollToAndSelect("Username", "super");
		page.getEditBtn().click();
		UserModal modal = new UserModal(driver);
		
		soft.assertFalse(modal.getRoleSelect().isEnabled()
				, "role select is disabled");
		page.clickVoidSpace();
		log.info("Role change is not possible for super user ");
		soft.assertAll();
	}
	
	
	/*USR-41 - Update/Delete super user when other super user exist*/
	@Test(description = "USR-41", groups = {"multiTenancy"})
	public void superDelUpdateOtherSuper() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Add new Super user");
		String userName = rest.getUser(null, DRoles.SUPER, true, false, true).
				getString("userName");
		
		log.info("Login with default Super user");
		
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		
		log.info("Update Email for new Super user " + userName);
		page.grid().scrollToAndSelect("Username", userName);
		page.getEditBtn().click();

		UserModal modal = new UserModal(driver);
		modal.fillData(null, "abc@gmail.com", "", "", "");
		modal.getOkBtn().click();
		page.saveAndConfirm();
		soft.assertEquals(page.getAlertArea().getAlertMessage(), "The operation 'update users' completed successfully.");
		
		page.grid().waitForRowsToLoad();
		
		log.info("Delete new Super user {}", userName);
		
		page.grid().scrollToAndSelect("Username", userName);
		page.getDeleteBtn().click();
		page.saveAndConfirm();
		
		soft.assertEquals(page.getAlertArea().getAlertMessage(), "The operation 'update users' completed successfully.");
		
		soft.assertAll();
	}

	// This Test Case verifies error while deleting/deactivating logged in admin user
	@Test(description = "USR-52", groups = {"singleTenancy","multiTenancy"})
	public void adminDeleteAll() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName");
		log.info("created user " + username);
		UsersPage userPage =new UsersPage(driver);
		if(data.isMultiDomain()) {
			loginAndGoToUsersPage(username, data.defaultPass());
		}
		else{
			loginAndGoToUsersPage(data.getAdminUser());
		}

		String loggedInUser = userPage.getSandwichMenu().getCurrentUserID();
		userPage.clickVoidSpace();

		log.info("Get all users");
		JSONArray userArray = rest.users().getUsers(userPage.getDomainFromTitle());
		int userCount = userArray.length();

		log.info("Get all active admin users");
		JSONArray activeUserArray = new JSONArray();
		for (int i = 0; i < userCount; i++) {
			Boolean isDeleted = userArray.getJSONObject(i).getBoolean("deleted");
			String userRole = userArray.getJSONObject(i).getString("roles");

			if (!isDeleted && userRole.equalsIgnoreCase(DRoles.ADMIN)) {
				activeUserArray.put(userArray.get(i));
			}
		}
		int activeUserCount = activeUserArray.length();

		for (int i = 0; i < activeUserCount; i++) {
			String userName = activeUserArray.getJSONObject(i).getString("userName");

			if (userName.equals(loggedInUser)) {

				log.info("Try deactivating logged in user");
				userPage.grid().scrollToAndSelect("Username", loggedInUser);
				userPage.getEditBtn().click();
				UserModal modal = new UserModal(driver);
				soft.assertTrue( modal.getActiveChk().getAttribute("class").contains("disabled"),"Active checkbox is disabled");
				modal.getCancelBtn().click();

				log.info("try deleting logged in user");
				userPage.getDeleteBtn().click();
				soft.assertTrue(userPage.getAlertArea().getAlertMessage().contains(DMessages.Users.LOGGEDINUSER_DELETE), "correct error message is shown");
				userPage.refreshPage();
				userPage.waitForPageTitle();

			} else {

				rest.users().deactivate(userName, userPage.getDomainFromTitle());
				log.info("Deactivated user : " + userName);
				rest.users().deleteUser(userName, userPage.getDomainFromTitle());
				log.info("Deleting user :" + userName);
			}
			userPage.refreshPage();
			userPage.waitForPageTitle();

		}
		soft.assertAll();
	}

	// This Test Case verifies error while deleting/deactivating logged in Super admin user.
	@Test(description = "USR-51", groups = {"multiTenancy"})
	public void superDeleteAll() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage userPage = new UsersPage(driver);
		userPage.getSidebar().goToPage(PAGES.USERS);
		userPage.grid().waitForRowsToLoad();

		String loggedInUser = userPage.getSandwichMenu().getCurrentUserID();
		userPage.clickVoidSpace();

		log.info("Get all users");
		JSONArray userArray = rest.users().getUsers(userPage.getDomainFromTitle());
		int userCount = userArray.length();

		JSONArray activeUserArray = new JSONArray();
		for (int i = 0; i < userCount; i++) {
			Boolean isDeleted = userArray.getJSONObject(i).getBoolean("deleted");
			String userRole = userArray.getJSONObject(i).getString("roles");

			if (!isDeleted && userRole.equalsIgnoreCase(DRoles.SUPER)) {
				activeUserArray.put(userArray.get(i));
			}
		}
		int activeUserCount = activeUserArray.length();
		log.info("Active super user count" + activeUserCount);

		for (int i = 0; i < activeUserCount; i++) {
			String userName = activeUserArray.getJSONObject(i).getString("userName");

			if (userName.equals(loggedInUser)) {

				log.info("Try deactivating logged in user");
				userPage.grid().scrollToAndSelect("Username", loggedInUser);
				userPage.getEditBtn().click();
				UserModal modal = new UserModal(driver);
				soft.assertTrue( modal.getActiveChk().getAttribute("class").contains("disabled"),"Active checkbox is disabled");
				modal.getCancelBtn().click();

				log.info("try deleting logged in user");
				userPage.getDeleteBtn().click();
				soft.assertTrue(userPage.getAlertArea().getAlertMessage().contains(DMessages.Users.LOGGEDINUSER_DELETE), "Error while deleting logged in user");
				userPage.refreshPage();
				userPage.waitForPageTitle();

			} else {

				rest.users().deactivate(userName, userPage.getDomainFromTitle());
				log.info("Deactivated user : " + userName);
				rest.users().deleteUser(userName, userPage.getDomainFromTitle());
				log.info("Deleting user :" + userName);
			}

			userPage.refreshPage();
			userPage.waitForPageTitle();

		}
		soft.assertAll();
	}

	// This test case verifies error while adding new user with role ROLE_USER in absence of domain admin
	@Test(description = "USR-50", groups = {"multiTenancy"})
	public void addUserWithNoAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();
		DomibusPage page = new DomibusPage(driver);
		String newUser = Gen.randomAlphaNumeric(10);
		rest.users().createUser(newUser, DRoles.ADMIN, data.getNewTestPass(), page.getDomainFromTitle());

		log.info("Get all users");
		JSONArray userArray = rest.users().getUsers(page.getDomainFromTitle());

		int userCount = userArray.length();
		page.refreshPage();
		page.waitForPageTitle();

		page.getSidebar().goToPage(PAGES.USERS);
		UsersPage userPage = new UsersPage(driver);
		for (int i = 0; i < userCount; i++) {
			String userName = userArray.getJSONObject(i).getString("userName");
			String userRole = userArray.getJSONObject(i).getString("roles");

			if (userRole.equalsIgnoreCase(DRoles.ADMIN) && !userName.equals(newUser)) {
				rest.users().deactivate(userName, page.getDomainFromTitle());
			}
		}
		logout();
		for (int j = 0; j < 5; j++) {
			login(newUser, data.defaultPass());
			soft.assertTrue(page.getAlertArea().getAlertMessage().equals(DMessages.LOGIN_INVALID_CREDENTIALS), "Error on login with wrong credentials");
		}

		loginAndGoToUsersPage(data.getAdminUser());
		userPage.newUser(Gen.randomAlphaNumeric(10), "tuser@bnc.com", DRoles.USER, data.defaultPass(), data.defaultPass());
		userPage.grid().waitForRowsToLoad();
		userPage.saveAndConfirm();
		soft.assertTrue(userPage.getAlertArea().getAlertMessage().equals(DMessages.Users.ONLY_ADMINUSER_DEACTIVATE), "Error while adding user when no admin user exists");
		soft.assertAll();

	}

	//This test cases verifies error while deactivating all admin users
	@Test(description = "USR-49", groups = {"multiTenancy"})
	public void deactivateAllAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage userPage = new UsersPage(driver);

		String newUser = Gen.randomAlphaNumeric(10);
		rest.users().createUser(newUser, DRoles.ADMIN, data.getNewTestPass(), userPage.getDomainFromTitle());

		userPage.getSidebar().goToPage(PAGES.USERS);
		userPage.grid().waitForRowsToLoad();

		log.info("Get all users");
		JSONArray userArray = rest.users().getUsers(userPage.getDomainFromTitle());
		int userCount = userArray.length();

		JSONArray activeUserArray = new JSONArray();
		log.info("Get all active admin users");
		for (int i = 0; i < userCount; i++) {
			Boolean isDeleted = userArray.getJSONObject(i).getBoolean("deleted");
			String userRole = userArray.getJSONObject(i).getString("roles");

			if (!isDeleted && userRole.equalsIgnoreCase(DRoles.ADMIN)) {
				activeUserArray.put(userArray.get(i));
			}
		}
		int activeUserCount = activeUserArray.length();

		for (int i = 0; i < activeUserCount; i++) {
			String userName = activeUserArray.getJSONObject(i).getString("userName");

			if (!userName.equals(newUser)) {
				rest.users().deactivate(userName, userPage.getDomainFromTitle());
				log.info("Deactivated admin user : " + userName);
			} else {
				userPage.grid().scrollToAndSelect("Username", newUser);
				userPage.getEditBtn().click();
				UserModal modal = new UserModal(driver);
				modal.getActiveChk().click();
				modal.clickOK();
				userPage.saveAndConfirm();
				soft.assertTrue(userPage.getAlertArea().getAlertMessage().contains(DMessages.Users.ONLY_ADMINUSER_DEACTIVATE),
						"Error while deactivating only active admin user");
				userPage.refreshPage();
				userPage.waitForPageTitle();
			}
		}
		soft.assertAll();
	}

	// This test case verifies error while deleting all admin users
	@Test(description = "USR-48", groups = {"multiTenancy"})
	public void deleteAllAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage userPage = new UsersPage(driver);

		String newUser = Gen.randomAlphaNumeric(10);
		rest.users().createUser(newUser, DRoles.ADMIN, data.getNewTestPass(), userPage.getDomainFromTitle());
		userPage.getSidebar().goToPage(PAGES.USERS);
		userPage.grid().waitForRowsToLoad();

		log.info("Get all users");
		JSONArray userArray = rest.users().getUsers(userPage.getDomainFromTitle());

		int userCount = userArray.length();

		JSONArray activeUserArray = new JSONArray();
		log.info("Get all active admin users");
		for (int i = 0; i < userCount; i++) {
			Boolean isDeleted = userArray.getJSONObject(i).getBoolean("deleted");
			String userRole = userArray.getJSONObject(i).getString("roles");

			if (!isDeleted && userRole.equalsIgnoreCase(DRoles.ADMIN)) {
				activeUserArray.put(userArray.get(i));
			}
		}
		int activeUserCount = activeUserArray.length();

		for (int i = 0; i < activeUserCount; i++) {
			String userName = activeUserArray.getJSONObject(i).getString("userName");

			if (!userName.equals(newUser)) {
				rest.users().deleteUser(userName, userPage.getDomainFromTitle());
				log.info("Deleted admin user : " + userName);
			} else {
				userPage.grid().scrollToAndSelect("Username", newUser);
				userPage.getDeleteBtn().click();
				userPage.saveAndConfirm();

				soft.assertTrue(userPage.getAlertArea().getAlertMessage().contains(DMessages.Users.ONLY_ADMINUSER_DEACTIVATE),
						"Error while deleting only active admin user");
				userPage.refreshPage();
				userPage.waitForPageTitle();
			}
		}
		soft.assertAll();
	}

	//This test cases verifies error while adding new user with username same as deleted user
	@Test(description = "USR-46", groups = {"multiTenancy"})
	public void duplicateUser() throws Exception {
		String deletedUsername = rest.getUser(null, DRoles.USER, false, true, false).getString("userName");

		SoftAssert soft = new SoftAssert();
		UsersPage userPage = new UsersPage(driver);
		userPage.getSidebar().goToPage(PAGES.USERS);
		userPage.grid().waitForRowsToLoad();

		int index = userPage.grid().scrollTo("Username", deletedUsername);
		soft.assertEquals(index, -1, "User is not visible in the grid");

		userPage.newUser(deletedUsername, "tuser@bnc.com", DRoles.USER, data.defaultPass(), data.defaultPass());
		userPage.grid().waitForRowsToLoad();
		userPage.saveAndConfirm();
		soft.assertEquals(userPage.getAlertArea().getAlertMessage(), String.format(DMessages.Users.DUPLICATE_USERNAME_SAMEDOMAIN_ERROR, deletedUsername), "Correct message is displayed");

		soft.assertAll();


	}

	// This test case verifies error while adding new user without presence of any domain admin user
	@Test(description = "USR-47", groups = {"multiTenancy"})
	public void userAddition() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage userPage = new UsersPage(driver);

		userPage.getSidebar().goToPage(PAGES.USERS);
		userPage.grid().waitForRowsToLoad();

		String newUser = Gen.randomAlphaNumeric(10);
		rest.users().createUser(newUser, DRoles.ADMIN, data.getNewTestPass(), userPage.getDomainFromTitle());
		log.info("newUser" + newUser);


		log.info("Get all users");
		JSONArray userArray = rest.users().getUsers(userPage.getDomainFromTitle());
		int userCount = userArray.length();
		String loggedInUser = userPage.getSandwichMenu().getCurrentUserID();

		JSONArray activeUserArray = new JSONArray();
		log.info("Get all active admin users");
		for (int i = 0; i < userCount; i++) {
			Boolean isDeleted = userArray.getJSONObject(i).getBoolean("deleted");
			String userRole = userArray.getJSONObject(i).getString("roles");

			if (!isDeleted && userRole.equalsIgnoreCase(DRoles.ADMIN)) {
				activeUserArray.put(userArray.get(i));
			}
		}
		int activeUserCount = activeUserArray.length();

		for (int i = 0; i < activeUserCount; i++) {
			String userName = activeUserArray.getJSONObject(i).getString("userName");

			if (!userName.equals(newUser) && !userName.equals(loggedInUser)) {
				rest.users().deleteUser(userName, userPage.getDomainFromTitle());
				log.info("Deleted admin user : " + userName);
			}
		}
		logout();
		for (int j = 0; j < 5; j++) {
			login(newUser, data.defaultPass());
			soft.assertTrue(userPage.getAlertArea().getAlertMessage().equals(DMessages.LOGIN_INVALID_CREDENTIALS), "Error on login with wrong credentials");
		}

		loginAndGoToUsersPage(data.getAdminUser());
		String userName1 = Gen.randomAlphaNumeric(9);
		userPage.newUser(userName1, "tuser@bnc.com", DRoles.USER, data.defaultPass(), data.defaultPass());
		userPage.grid().waitForRowsToLoad();
		userPage.saveAndConfirm();
		soft.assertTrue(userPage.getAlertArea().getAlertMessage().contains(DMessages.Users.ONLY_ADMINUSER_DEACTIVATE),
				"Error while deleting only active admin user");
		soft.assertAll();


	}
}

