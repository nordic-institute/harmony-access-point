package domibus.ui.ux;

import io.qameta.allure.*;
import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.ChangePassword.ChangePasswordPage;
import pages.errorLog.ErrorLogPage;
import utils.Gen;


/**
 * @author Rupam
 * @version 4.1
 */

@Epic("Change Password")
@Feature("UX")
public class ChangePasswordPgUXTest extends SeleniumTest {

	/**
	 * Checks whether changePassword page exists and it is opened successfully
	 */
	/*  CP-1 - Check availability of change password link and navigate to page  */
	@Description("CP-1 - Check availability of change password link and navigate to page")
	@Link(name = "EDELIVERY-5038", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5038")
	@AllureId("CP-1")
	@Test(description = "CP-1", groups = {"multiTenancy", "singleTenancy"})
	public void navigateToChangePassword() throws Exception {
		SoftAssert soft = new SoftAssert();
		DomibusPage page = new DomibusPage(driver);

		Allure.step("Validate if Change password link is available in Sandwich menu");
		log.info("Validate if Change password link is available in Sandwich menu");
		soft.assertTrue(page.getSandwichMenu().isChangePassLnkPresent(), "Change Password link is available");
		Allure.step("Open changePassword Page by clicking link");
		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();

		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		Allure.step("Validate Page Header");
		log.info("Validate Page Header");
		soft.assertTrue(cpage.verifyFieldHeader(), "Correct page is opened");

		soft.assertAll();
	}

	/**
	 * This method verifies whether page is properly loaded with all its elements
	 */
	/*  CP-2 - Open change password page and verify all its elements  */
	@Description("CP-2 - Open change password page and verify all its elements")
	@Link(name = "EDELIVERY-5039", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5039")
	@AllureId("CP-2")
	@Test(description = "CP-2", groups = {"multiTenancy", "singleTenancy"})
	public void verifyPageElements() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);

		Allure.step("Check Change password link availability");
		log.info("Check Change password link availability");
		soft.assertTrue(page.getSandwichMenu().isChangePassLnkPresent(), "Change Password link is available");
		Allure.step("Open changePassword Page by clicking link");
		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);
		Allure.step("Page is loaded successfully with all required elements");
		log.info("Page is loaded successfully with all required elements");
		soft.assertTrue(cpage.isLoaded(), "Page is loaded successfully");
		soft.assertAll();
	}

	/*
	This method confirms no change in password if user navigates to other page without changing password
	*/
	/*  CP-3 - Login with superadminuser and change password  */
	@Description("CP-3 - Login with superadminuser and change password")
	@Link(name = "EDELIVERY-5040", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5040")
	@AllureId("CP-3")
	@Test(description = "CP-3", groups = {"multiTenancy", "singleTenancy"})
	public void unchangedPasswordOnNavigation() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);

		Allure.step("Open changePassword Page by clicking link");
		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();

		Allure.step("Navigate to error log page without changing password ");
		log.info("Navigate to error log page without changing password ");
		page.getSidebar().goToPage(PAGES.ERROR_LOG);
		ErrorLogPage errorLogPage = new ErrorLogPage(driver);
		page.waitForPageToLoad();

		Allure.step("logout from application");
		log.info("logout from application");
		logout();
		Allure.step("Again login into application with Admin credential");
		log.info("Again login into application with Admin credential");
		login(data.getAdminUser());

		Allure.step("Validate user is logged in successfully");
		log.info("Validate user is logged in successfully");

		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");
		soft.assertAll();
	}

	/**
	 * This method confirms no change in password if user navigate to other page after filling data in all three password
	 * related fields but  before clicking on update button
	 */
	/*  CP-4 - Login with superadminuser and fill change password form and navigate away without saving  */
	@Description("CP-4 - Login with superadminuser and fill change password form and navigate away without saving")
	@Link(name = "EDELIVERY-5041", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5041")
	@AllureId("CP-4")
	@Test(description = "CP-4", groups = {"multiTenancy", "singleTenancy"})
	public void navigationWithoutClickingUpdate() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Open changePassword Page by clicking link");
		log.info("Open changePassword Page by clicking link");
		new DomibusPage(driver).getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		Allure.step("Fill data in Current password,new password and confirmation fields");
		log.info("Fill data in Current password,new password and confirmation fields");
		cpage.setPassFields(data.defaultPass(), data.defaultPass(), data.defaultPass());

		Allure.step("Navigate to Error log page");
		log.info("Navigate to Error log page");
		cpage.getSidebar().goToPage(PAGES.ERROR_LOG);
		ErrorLogPage errorLogPage = new ErrorLogPage(driver);
		cpage.waitForPageToLoad();

		Allure.step("logout from application");
		log.info("logout from application");
		logout();

		Allure.step("login into application with previous admin credentials");
		log.info("login into application with previous admin credentials");
		login(data.getAdminUser());

		Allure.step("Validate User is logged in into application");
		log.info("Validate User is logged in into application");
		soft.assertTrue(cpage.getSandwichMenu().isLoggedIn(), "User logged in");
		soft.assertAll();
	}


	/**
	 * This method ensures validation message for Confirmation if New password and Confirmation field is different but valid
	 */
	/*  CP-6 - Try to change password using wrong value for Current password  */
	@Description("CP-6 - Try to change password using wrong value for Current password")
	@Link(name = "EDELIVERY-5043", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5043")
	@AllureId("CP-6")
	@Test(description = "CP-6", groups = {"multiTenancy", "singleTenancy"})
	public void differentPasswordsValidData() throws Exception {

		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		Allure.step("Open change password page by clicking its link");
		log.info("Open change password page by clicking its link");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);
		cpage.waitForPageTitle();

		Allure.step("Fill correct data in current password , valid and different data in new password and confirmation field");
		log.info("Fill correct data in current password , valid and different data in new password and confirmation field");
		cpage.setPassFields(data.defaultPass(), data.defaultPass(), data.getNewTestPass());

		Allure.step("press tab key");
		log.info("press tab key");
		cpage.pressTABKey();

		Allure.step("Validation message appears");
		log.info("Validation message appears");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.confirmationFieldLabel), "Message is displayed");
		soft.assertAll();
	}

	/**
	 * This method ensures validation message for password policy if New password and Confirmation field is same but invalid
	 */
	/*  CP-7 - New password doesnt respect password policy  */
	@Description("CP-7 - New password doesnt respect password policy")
	@Link(name = "EDELIVERY-5044", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5044")
	@AllureId("CP-7")
	@Test(description = "CP-7", groups = {"multiTenancy", "singleTenancy"})
	public void sameInvalidData() throws Exception {
		SoftAssert soft = new SoftAssert();
		logout();

		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);


		Allure.step("Open change password page");
		log.info("Open change password page");
		login(username, data.defaultPass()).getSandwichMenu().openchangePassword();
		ChangePasswordPage page = new ChangePasswordPage(driver);
		page.waitForPageTitle();

		Allure.step("Generate invalid password");
		log.info("Generate invalid password");
		String pass = "invalid_pass";

		Allure.step("Fill correct data for current password and invalid but same for new password and confirmation field");
		log.info("Fill correct data for current password and invalid but same for new password and confirmation field");
		page.setPassFields(data.defaultPass(), pass, pass);
		Allure.step("press tab ");
		log.info("press tab ");
//		page.getConfirmationField().pressTABKey();
		page.pressTABKey();

		soft.assertTrue(!page.getUpdateButton().isEnabled(), "Update button is disabled");

		Allure.step("Validation message for password policy under New password ");
		log.info("Validation message for password policy under New password ");
		soft.assertTrue(page.isValidationMsgPresent(page.newPasswordFieldLabel), "Message is displayed");
		soft.assertTrue(page.getValidationMsg(page.newPasswordFieldLabel).contains(DMessages.PASS_POLICY_MESSAGE), "Password policy is displayed");
		soft.assertAll();
	}

	/* This method ensures validation message for no match to password policy and with confirmation password when
	New password and Confirmation field is different and invalid */
	/*  CP-8 - New password doesnt match the confirmation and both are invalid   */
	@Description("CP-8 - New password doesnt match the confirmation and both are invalid ")
	@Link(name = "EDELIVERY-5045", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5045")
	@AllureId("CP-8")
	@Test(description = "CP-8", groups = {"multiTenancy", "singleTenancy", "incomplete"})
	public void differentPasswordsInvalidData() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);

		Allure.step("Open Change Password page");
		log.info("Open Change Password page");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);

		Allure.step("Fill current data for current password, invalid  and different data for new password and confirmation field");
		log.info("Fill current data for current password, invalid  and different data for new password and confirmation field");
		cpage.setPassFields(data.defaultPass(), "INVALID_PASS_1", "invalid_pass_2");

		Allure.step("press tab key");
		log.info("press tab key");
		cpage.pressTABKey();
		page.wait.forXMillis(2000);

		Allure.step("Validation message for password policy under new password field");
		log.info("Validation message for password policy under new password field");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.newPasswordFieldLabel), "Message is displayed");
		soft.assertTrue(cpage.getValidationMsg(cpage.newPasswordFieldLabel).contains(DMessages.PASS_POLICY_MESSAGE),
				"Password policy message is displayed");

		Allure.step("Validation message for mismatch under confirmation field ");
		log.info("Validation message for mismatch under confirmation field ");
		soft.assertTrue(cpage.isValidationMsgPresent(cpage.confirmationFieldLabel), "Message is displayed");
		soft.assertEquals(cpage.getValidationMsg(cpage.confirmationFieldLabel), DMessages.PASS_NO_MATCH_MESSAGE, "Passwords do not match is displayed.");

		soft.assertAll();
	}


}
