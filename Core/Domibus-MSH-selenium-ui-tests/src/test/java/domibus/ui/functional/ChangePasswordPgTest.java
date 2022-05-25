package domibus.ui.functional;

import org.testng.Reporter;
import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.ChangePassword.ChangePasswordPage;


public class ChangePasswordPgTest extends SeleniumTest {

	/* EDELIVERY-5042 - CP-5 - Login with superadminuser and change password and navigate away from the page after entering data on fields */
	@Test(description = "CP-5", groups = {"multiTenancy", "singleTenancy"})
	public void wrongCurrentPassword() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, true);

		Reporter.log("Login into application with Admin credentials");
		log.info("Login into application with Admin credentials");
		DomibusPage page = login(username, data.defaultPass());

		Reporter.log("Open changePassword Page by clicking link");
		log.info("Open changePassword Page by clicking link");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);
		Reporter.log("Fill wrong data for current field and correct data for new password and confirmation");
		log.info("Fill wrong data for current field and correct data for new password and confirmation");
		cpage.setPassFields(data.getNewTestPass(), data.defaultPass(), data.defaultPass());
		Reporter.log("Click on update button");
		log.info("Click on update button");
		cpage.getUpdateButton().click();
		Reporter.log("Error message shown:" + page.getAlertArea().getAlertMessage());
		log.info("Error message shown:" + page.getAlertArea().getAlertMessage());
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.CHANGEPASSWORD_WRONG_CURRENT_PASSWORD, "Displayed message is correct");
		soft.assertAll();
	}

	/* EDELIVERY-5046 - CP-9 - New password doesnt match the confirmation and both are valid  */
	@Test(description = "CP-9", groups = {"multiTenancy", "singleTenancy"})
	public void changePassword() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, true);

		Reporter.log("Login into application with Admin credentials");
		log.info("Login into application with Admin credentials");
		DomibusPage page = login(username, data.defaultPass());
		page.waitForPageToLoad();

		Reporter.log("Validate change password link presence");
		log.info("Validate change password link presence");
		soft.assertTrue(page.getSandwichMenu().isChangePassLnkPresent(), "Change Password link is available");

		Reporter.log("Open Change password page ");
		log.info("Open Change password page ");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);
		cpage.waitForPageTitle();

		Reporter.log("Fill correct data in current password , Valid and same data in new password and confirmation field");
		log.info("Fill correct data in current password , Valid and same data in new password and confirmation field");
		cpage.setPassFields(data.defaultPass(), data.getNewTestPass(), data.getNewTestPass());

		Reporter.log("Click on update button");
		log.info("Click on update button");
		cpage.getUpdateButton().click();

		Reporter.log("wait for Message page title");
		log.info("wait for Message page title");
		page.waitForPageTitle();

		Reporter.log("logout from application");
		log.info("logout from application");
		logout();

		Reporter.log("login with generated username and updated password ");
		log.info("login with generated username and updated password ");

		login(username, data.getNewTestPass());

		Reporter.log("Validate user logged in into application");
		log.info("Validate user logged in into application");
		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");
		soft.assertAll();
	}

	/* EDELIVERY-5047 - CP-10 - Try to change password with new password among previous 5 */
	@Test(description = "CP-10", groups = {"multiTenancy", "singleTenancy"})
	public void newPasswordAmongLast5() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, true);

		Reporter.log("Login into application with Admin credentials");
		log.info("Login into application with Admin credentials");
		DomibusPage page = login(username, data.defaultPass());


		Reporter.log("Open change password page");
		log.info("Open change password page");
		page.getSandwichMenu().openchangePassword();
		ChangePasswordPage cpage = new ChangePasswordPage(driver);
		cpage.waitForPageTitle();

		Reporter.log("Fill correct and valid data in all fields");
		log.info("Fill correct and valid data in all fields");
		cpage.setPassFields(data.defaultPass(), data.getNewTestPass(), data.getNewTestPass());

		Reporter.log("click on update button");
		log.info("click on update button");
		cpage.getUpdateButton().click();

		Reporter.log("Wait for Message page title");
		log.info("Wait for Message page title");
		page.waitForPageTitle();

		Reporter.log("logout from application");
		log.info("logout from application");
		logout();

		Reporter.log("Login with username and updated password");
		log.info("Login with username and updated password");

		login(username, data.getNewTestPass());


		Reporter.log("Open Change password page");
		log.info("Open Change password page");
		page.getSandwichMenu().openchangePassword();

		Reporter.log("Fill correct data in current password , same in new password and confirmation field");
		log.info("Fill correct data in current password , same in new password and confirmation field");
		cpage.setPassFields(data.getNewTestPass(), data.defaultPass(), data.defaultPass());

		Reporter.log("Click on update button");
		log.info("Click on update button");
		cpage.getUpdateButton().click();

		Reporter.log("Error message shown:" + page.getAlertArea().getAlertMessage());
		log.info("Error message shown:" + page.getAlertArea().getAlertMessage());
		soft.assertEquals(page.getAlertArea().getAlertMessage(), String.format(DMessages.CHANGEPASSWORD_LAST_FIVE, username), "Correct error message is displayed");
		soft.assertAll();
	}
}
