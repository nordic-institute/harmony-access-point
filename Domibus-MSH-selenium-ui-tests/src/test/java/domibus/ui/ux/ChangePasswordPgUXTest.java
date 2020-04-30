package domibus.ui.ux;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import utils.BaseTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.ChangePassword.ChangePasswordPage;
import pages.errorLog.ErrorLogPage;


/**
 * @author Rupam
 * @version 4.1
 */

public class ChangePasswordPgUXTest extends BaseTest {

	/**
	 * Checks whether changePassword page exists and it is opened successfully
	 */
	@Test(description = "CP-1", groups = {"multiTenancy", "singleTenancy"})
	public void navigateToChangePassword() throws Exception {
		SoftAssert soft = new SoftAssert();
		DomibusPage page = new DomibusPage(driver);

		log.info("Validate if Change password link is available in Sandwich menu");
		soft.assertTrue(page.getSandwichMenu().isChangePassLnkPresent(), "Change Password link is available");
		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();

		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		log.info("Validate Page Header");
		soft.assertTrue(cpage.verifyFieldHeader(), "Correct page is opened");

		soft.assertAll();
	}

	/**
	 * This method verifies whether page is properly loaded with all its elements
	 */
	@Test(description = "CP-2", groups = {"multiTenancy", "singleTenancy"})
	public void verifyPageElements() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);

		log.info("Check Change password link availability");
		soft.assertTrue(page.getSandwichMenu().isChangePassLnkPresent(), "Change Password link is available");
		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);
		log.info("Page is loaded successfully with all required elements");
		soft.assertTrue(cpage.isLoaded(), "Page is loaded successfully");
		soft.assertAll();
	}

	/*
	This method confirms no change in password if user navigates to other page without changing password
	 */
	@Test(description = "CP-3", groups = {"multiTenancy", "singleTenancy"})
	public void unchangedPasswordOnNavigation() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);

		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();

		log.info("Navigate to error log page without changing password ");
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		ErrorLogPage errorLogPage = new ErrorLogPage(driver);
		page.waitForPageToLoad();

		log.info("logout from application");
		logout();
		log.info("Again login into application with Admin credential");
		login(data.getAdminUser());

		log.info("Validate user is logged in successfully");

		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");
		soft.assertAll();
	}

	/**
	 * This method confirms no change in password if user navigate to other page after filling data in all three password
	 * related fields but  before clicking on update button
	 */
	@Test(description = "CP-4", groups = {"multiTenancy", "singleTenancy"})
	public void navigationWithoutClickingUpdate() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Open changePassword Page by clicking link");
		new DomibusPage(driver).getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		log.info("Fill data in Current password,new password and confirmation fields");
		cpage.setPassFields(data.defaultPass(), data.defaultPass(), data.defaultPass());

		log.info("Navigate to Error log page");
		cpage.getSidebar().goToPage(PAGES.ERROR_LOG);
		ErrorLogPage errorLogPage = new ErrorLogPage(driver);
		cpage.waitForPageToLoad();

		log.info("logout from application");
		logout();

		log.info("login into application with previous admin credentials");
		login(data.getAdminUser());

		log.info("Validate User is logged in into application");
		soft.assertTrue(cpage.getSandwichMenu().isLoggedIn(), "User logged in");
		soft.assertAll();
	}


	/**
	 * This method ensures validation message for Confirmation if New password and Confirmation field is different but valid
	 */
	@Test(description = "CP-6", groups = {"multiTenancy", "singleTenancy"})
	public void differentPasswordsValidData() throws Exception {

		SoftAssert soft = new SoftAssert();

	DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		log.info("Open change password page by clicking its link");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		log.info("Fill correct data in current password , valid and different data in new password and confirmation field");
		cpage.setPassFields(data.defaultPass(), data.defaultPass(), data.getNewTestPass());

		log.info("press tab key");
		cpage.getConfirmationField().pressTABKey();

		log.info("Validation message appears");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.confirmationFieldLabel), "Message is displayed");
		soft.assertAll();
	}

	/**
	 * This method ensures validation message for password policy if New password and Confirmation field is same but invalid
	 */
	@Test(description = "CP-7", groups = {"multiTenancy", "singleTenancy"})
	public void sameInvalidData() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver); //login(data.getAdminUser());
		page.getSidebar().goToPage(PAGES.USERS);

		log.info("Open change password page");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		log.info("Generate invalid password");
		String pass = "invalid_pass";

		log.info("Fill correct data for current password and invalid but same for new password and confirmation field");
		cpage.setPassFields(data.defaultPass(), pass, pass);
		log.info("press tab ");
		cpage.getConfirmationField().pressTABKey();

		soft.assertTrue(!cpage.getUpdateButton().isEnabled(), "Update button is disabled");

		log.info("Validation message for password policy under New password ");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.newPasswordFieldLabel), "Message is displayed");
		soft.assertTrue(cpage.getValidationMsg(cpage.newPasswordFieldLabel).contains(DMessages.PASS_POLICY_MESSAGE), "Password policy is displayed");
		soft.assertAll();
	}

	/* This method ensures validation message for no match to password policy and with confirmation password when
	New password and Confirmation field is different and invalid */
	@Test(description = "CP-8", groups = {"multiTenancy", "singleTenancy", "incomplete"})
	public void differentPasswordsInvalidData() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);

		log.info("Open Change Password page");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		log.info("Fill current data for current password, invalid  and different data for new password and confirmation field");
		cpage.setPassFields(data.defaultPass(), "INVALID_PASS_1", "invalid_pass_2");

		log.info("press tab key");
		cpage.getConfirmationField().pressTABKey();

		log.info("Validation message for password policy under new password field");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.newPasswordFieldLabel), "Message is displayed");
		soft.assertTrue(cpage.getValidationMsg(cpage.newPasswordFieldLabel).contains(DMessages.PASS_POLICY_MESSAGE),
				"Password policy message is displayed");

		log.info("Validation message for mismatch under confirmation field ");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.confirmationFieldLabel), "Message is displayed");
		soft.assertEquals(cpage.getValidationMsg(cpage.confirmationFieldLabel), DMessages.PASS_NO_MATCH_MESSAGE, "Passwords do not match is displayed.");

		soft.assertAll();
	}


}