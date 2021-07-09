package domibus.ui.functional;

import io.qameta.allure.*;
import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.ChangePassword.ChangePasswordPage;


/**
* @author Rupam
* @version 4.1
*/

@Epic("")
@Feature("")
public class ChangePasswordPgTest extends SeleniumTest {

/**
* This method shows the implemented error on update when user enters wrong current password but valid new password
*/
/*  CP-5 - Login with superadminuser and change password and navigate away from the page after entering data on fields  */
@Description("CP-5 - Login with superadminuser and change password and navigate away from the page after entering data on fields")
@Link(name = "EDELIVERY-5042", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5042")
@AllureId("CP-5")
@Test(description = "CP-5", groups = {"multiTenancy", "singleTenancy"})
public void wrongCurrentPassword() throws Exception {
SoftAssert soft = new SoftAssert();

String username = rest.getUsername(null, DRoles.USER, true, false, true);

Allure.step("Login into application with Admin credentials");
log.info("Login into application with Admin credentials");
DomibusPage page = login(username, data.defaultPass());

Allure.step("Open changePassword Page by clicking link");
log.info("Open changePassword Page by clicking link");
page.getSandwichMenu().openchangePassword();
ChangePasswordPage cpage = new ChangePasswordPage(driver);
Allure.step("Fill wrong data for current field and correct data for new password and confirmation");
log.info("Fill wrong data for current field and correct data for new password and confirmation");
cpage.setPassFields(data.getNewTestPass(), data.defaultPass(), data.defaultPass());
Allure.step("Click on update button");
log.info("Click on update button");
cpage.getUpdateButton().click();
Allure.step("Error message shown:" + page.getAlertArea().getAlertMessage());
log.info("Error message shown:" + page.getAlertArea().getAlertMessage());
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.CHANGEPASSWORD_WRONG_CURRENT_PASSWORD, "Displayed message is correct");
soft.assertAll();
}

/**
* This method will change password and allow to login successfully with new credentials
*/
/*  CP-9 - New password doesnt match the confirmation and both are valid   */
@Description("CP-9 - New password doesnt match the confirmation and both are valid ")
@Link(name = "EDELIVERY-5046", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5046")
@AllureId("CP-9")
@Test(description = "CP-9", groups = {"multiTenancy", "singleTenancy"})
public void changePassword() throws Exception {
SoftAssert soft = new SoftAssert();

String username = rest.getUsername(null, DRoles.USER, true, false, true);

Allure.step("Login into application with Admin credentials");
log.info("Login into application with Admin credentials");
DomibusPage page = login(username, data.defaultPass());
page.waitForPageToLoad();

Allure.step("Validate change password link presence");
log.info("Validate change password link presence");
soft.assertTrue(page.getSandwichMenu().isChangePassLnkPresent(), "Change Password link is available");

Allure.step("Open Change password page ");
log.info("Open Change password page ");
page.getSandwichMenu().openchangePassword();
ChangePasswordPage cpage = new ChangePasswordPage(driver);
cpage.waitForPageTitle();

Allure.step("Fill correct data in current password , Valid and same data in new password and confirmation field");
log.info("Fill correct data in current password , Valid and same data in new password and confirmation field");
cpage.setPassFields(data.defaultPass(), data.getNewTestPass(), data.getNewTestPass());

Allure.step("Click on update button");
log.info("Click on update button");
cpage.getUpdateButton().click();

Allure.step("wait for Message page title");
log.info("wait for Message page title");
page.waitForPageTitle();

Allure.step("logout from application");
log.info("logout from application");
logout();

Allure.step("login with generated username and updated password ");
log.info("login with generated username and updated password ");

login(username, data.getNewTestPass());

Allure.step("Validate user logged in into application");
log.info("Validate user logged in into application");
soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");
soft.assertAll();
}

/**
* This method will throw error in new password is among previous 5 passwords.
*/
/*  CP-10 - Try to change password with new password among previous 5  */
@Description("CP-10 - Try to change password with new password among previous 5")
@Link(name = "EDELIVERY-5047", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5047")
@AllureId("CP-10")
@Test(description = "CP-10", groups = {"multiTenancy", "singleTenancy"})
public void newPasswordAmongLast5() throws Exception {
SoftAssert soft = new SoftAssert();

String username = rest.getUsername(null, DRoles.USER, true, false, true);

Allure.step("Login into application with Admin credentials");
log.info("Login into application with Admin credentials");
DomibusPage page = login(username, data.defaultPass());


Allure.step("Open change password page");
log.info("Open change password page");
page.getSandwichMenu().openchangePassword();
ChangePasswordPage cpage = new ChangePasswordPage(driver);
cpage.waitForPageTitle();

Allure.step("Fill correct and valid data in all fields");
log.info("Fill correct and valid data in all fields");
cpage.setPassFields(data.defaultPass(), data.getNewTestPass(), data.getNewTestPass());

Allure.step("click on update button");
log.info("click on update button");
cpage.getUpdateButton().click();

Allure.step("Wait for Message page title");
log.info("Wait for Message page title");
page.waitForPageTitle();

Allure.step("logout from application");
log.info("logout from application");
logout();

Allure.step("Login with username and updated password");
log.info("Login with username and updated password");

login(username, data.getNewTestPass());


Allure.step("Open Change password page");
log.info("Open Change password page");
page.getSandwichMenu().openchangePassword();

Allure.step("Fill correct data in current password , same in new password and confirmation field");
log.info("Fill correct data in current password , same in new password and confirmation field");
cpage.setPassFields(data.getNewTestPass(), data.defaultPass(), data.defaultPass());

Allure.step("Click on update button");
log.info("Click on update button");
cpage.getUpdateButton().click();

Allure.step("Error message shown:" + page.getAlertArea().getAlertMessage());
log.info("Error message shown:" + page.getAlertArea().getAlertMessage());
soft.assertEquals(page.getAlertArea().getAlertMessage(), String.format(DMessages.CHANGEPASSWORD_LAST_FIVE, username), "Correct error message is displayed");
soft.assertAll();
}
}
