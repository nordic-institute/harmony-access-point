package domibus.ui.ux;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.ChangePassword.ChangePasswordPage;
import pages.errorLog.ErrorLogPage;
import utils.Gen;


public class ChangePasswordPgUXTest extends SeleniumTest {

	/* EDELIVERY-5038 - CP-1 - Check availability of change password link and navigate to page */
	@Test(description = "CP-1", groups = {"multiTenancy", "singleTenancy"})
	public void navigateToChangePassword() throws Exception {
		SoftAssert soft = new SoftAssert();
		DomibusPage page = new DomibusPage(driver);

		Reporter.log("Validate if Change password link is available in Sandwich menu");
		log.info("Validate if Change password link is available in Sandwich menu");
		soft.assertTrue(page.getSandwichMenu().isChangePassLnkPresent(), "Change Password link is available");
		Reporter.log("Open changePassword Page by clicking link");
		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();

		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		Reporter.log("Validate Page Header");
		log.info("Validate Page Header");
		soft.assertTrue(cpage.verifyFieldHeader(), "Correct page is opened");

		soft.assertAll();
	}

	/* EDELIVERY-5039 - CP-2 - Open change password page and verify all its elements */
	@Test(description = "CP-2", groups = {"multiTenancy", "singleTenancy"})
	public void verifyPageElements() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);

		Reporter.log("Check Change password link availability");
		log.info("Check Change password link availability");
		soft.assertTrue(page.getSandwichMenu().isChangePassLnkPresent(), "Change Password link is available");
		Reporter.log("Open changePassword Page by clicking link");
		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);
		Reporter.log("Page is loaded successfully with all required elements");
		log.info("Page is loaded successfully with all required elements");
		soft.assertTrue(cpage.isLoaded(), "Page is loaded successfully");
		soft.assertAll();
	}

	/* EDELIVERY-5040 - CP-3 - Login with superadminuser and change password */
	@Test(description = "CP-3", groups = {"multiTenancy", "singleTenancy"})
	public void unchangedPasswordOnNavigation() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);

		Reporter.log("Open changePassword Page by clicking link");
		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();

		Reporter.log("Navigate to error log page without changing password ");
		log.info("Navigate to error log page without changing password ");
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		ErrorLogPage errorLogPage = new ErrorLogPage(driver);
		page.waitForPageToLoad();

		Reporter.log("logout from application");
		log.info("logout from application");
		logout();
		Reporter.log("Again login into application with Admin credential");
		log.info("Again login into application with Admin credential");
		login(data.getAdminUser());

		Reporter.log("Validate user is logged in successfully");
		log.info("Validate user is logged in successfully");

		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");
		soft.assertAll();
	}

	/* EDELIVERY-5041 - CP-4 - Login with superadminuser and fill change password form and navigate away without saving */
	@Test(description = "CP-4", groups = {"multiTenancy", "singleTenancy"})
	public void navigationWithoutClickingUpdate() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Open changePassword Page by clicking link");
		log.info("Open changePassword Page by clicking link");
		new DomibusPage(driver).getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		Reporter.log("Fill data in Current password,new password and confirmation fields");
		log.info("Fill data in Current password,new password and confirmation fields");
		cpage.setPassFields(data.defaultPass(), data.defaultPass(), data.defaultPass());

		Reporter.log("Navigate to Error log page");
		log.info("Navigate to Error log page");
		cpage.getSidebar().goToPage(PAGES.ERROR_LOG);
		ErrorLogPage errorLogPage = new ErrorLogPage(driver);
		cpage.waitForPageToLoad();

		Reporter.log("logout from application");
		log.info("logout from application");
		logout();

		Reporter.log("login into application with previous admin credentials");
		log.info("login into application with previous admin credentials");
		login(data.getAdminUser());

		Reporter.log("Validate User is logged in into application");
		log.info("Validate User is logged in into application");
		soft.assertTrue(cpage.getSandwichMenu().isLoggedIn(), "User logged in");
		soft.assertAll();
	}


	/* EDELIVERY-5043 - CP-6 - Try to change password using wrong value for Current password */
	@Test(description = "CP-6", groups = {"multiTenancy", "singleTenancy"})
	public void differentPasswordsValidData() throws Exception {

		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		Reporter.log("Open change password page by clicking its link");
		log.info("Open change password page by clicking its link");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);
		cpage.waitForPageTitle();

		Reporter.log("Fill correct data in current password , valid and different data in new password and confirmation field");
		log.info("Fill correct data in current password , valid and different data in new password and confirmation field");
		cpage.setPassFields(data.defaultPass(), data.defaultPass(), data.getNewTestPass());

		Reporter.log("press tab key");
		log.info("press tab key");
		cpage.pressTABKey();

		Reporter.log("Validation message appears");
		log.info("Validation message appears");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.confirmationFieldLabel), "Message is displayed");
		soft.assertAll();
	}

	/* EDELIVERY-5044 - CP-7 - New password doesnt respect password policy */
	@Test(description = "CP-7", groups = {"multiTenancy", "singleTenancy"})
	public void sameInvalidData() throws Exception {
		SoftAssert soft = new SoftAssert();
		logout();

		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);


		Reporter.log("Open change password page");
		log.info("Open change password page");
		login(username, data.defaultPass()).getSandwichMenu().openchangePassword();
		ChangePasswordPage page = new ChangePasswordPage(driver);
		page.waitForPageTitle();

		Reporter.log("Generate invalid password");
		log.info("Generate invalid password");
		String pass = "invalid_pass";

		Reporter.log("Fill correct data for current password and invalid but same for new password and confirmation field");
		log.info("Fill correct data for current password and invalid but same for new password and confirmation field");
		page.setPassFields(data.defaultPass(), pass, pass);
		Reporter.log("press tab ");
		log.info("press tab ");
//		page.getConfirmationField().pressTABKey();
		page.pressTABKey();

		soft.assertTrue(!page.getUpdateButton().isEnabled(), "Update button is disabled");

		Reporter.log("Validation message for password policy under New password ");
		log.info("Validation message for password policy under New password ");
		soft.assertTrue(page.isValidationMsgPresent(page.newPasswordFieldLabel), "Message is displayed");
		soft.assertTrue(page.getValidationMsg(page.newPasswordFieldLabel).contains(DMessages.PASS_POLICY_MESSAGE), "Password policy is displayed");
		soft.assertAll();
	}

	/* EDELIVERY-5045 - CP-8 - New password doesnt match the confirmation and both are invalid  */
	@Test(description = "CP-8", groups = {"multiTenancy", "singleTenancy", "incomplete"})
	public void differentPasswordsInvalidData() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);

		Reporter.log("Open Change Password page");
		log.info("Open Change Password page");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		Reporter.log("Fill current data for current password, invalid  and different data for new password and confirmation field");
		log.info("Fill current data for current password, invalid  and different data for new password and confirmation field");
		cpage.setPassFields(data.defaultPass(), "INVALID_PASS_1", "invalid_pass_2");

		Reporter.log("press tab key");
		log.info("press tab key");
		cpage.pressTABKey();

		Reporter.log("Validation message for password policy under new password field");
		log.info("Validation message for password policy under new password field");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.newPasswordFieldLabel), "Message is displayed");
		soft.assertTrue(cpage.getValidationMsg(cpage.newPasswordFieldLabel).contains(DMessages.PASS_POLICY_MESSAGE),
				"Password policy message is displayed");

		Reporter.log("Validation message for mismatch under confirmation field ");
		log.info("Validation message for mismatch under confirmation field ");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.confirmationFieldLabel), "Message is displayed");
		soft.assertEquals(cpage.getValidationMsg(cpage.confirmationFieldLabel), DMessages.PASS_NO_MATCH_MESSAGE, "Passwords do not match is displayed.");

		soft.assertAll();
	}


}
