package domibus.ui.functional;


import io.qameta.allure.*;
import com.bluecatcode.junit.shaded.org.apache.commons.lang3.StringUtils;
import com.sun.jersey.api.client.ClientResponse;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.popups.InfoModal;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import utils.Gen;


/**
* @author Catalin Comanici
* @version 4.1
*/


@Epic("")
@Feature("")
public class LoginPgTest extends SeleniumTest {


private void checkUserLogin(String role, SoftAssert soft) throws Exception {
logout();

String username = Gen.randomAlphaNumeric(10);
rest.users().createUser(username, role, data.defaultPass(), null);
Allure.step(String.format("Created user %s with role %s", username, role));
log.info(String.format("Created user %s with role %s", username, role));

Allure.step(String.format("Login %s with role %s", username, role));
log.info(String.format("Login %s with role %s", username, role));
login(username, data.defaultPass());

DomibusPage page = new DomibusPage(driver);
soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");

Allure.step("Logout");
log.info("Logout");
page.getSandwichMenu().logout();

Allure.step("Deleted user: " + username);
log.info("Deleted user: " + username);
rest.users().deleteUser(username, null);
}

/**
* Checks whether login as system admin works
*/
/*  LGN-1 - Login with valid user  */
@Description("LGN-1 - Login with valid user")
@Link(name = "EDELIVERY-5033", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5033")
@AllureId("LGN-1")
@Test(description = "LGN-1", groups = {"multiTenancy", "singleTenancy"})
public void validLogin() throws Exception {
Allure.step("Testing valid login with every type of user");
log.info("Testing valid login with every type of user");
SoftAssert soft = new SoftAssert();

if (data.isMultiDomain()) {
checkUserLogin(DRoles.SUPER, soft);
}
checkUserLogin(DRoles.ADMIN, soft);
checkUserLogin(DRoles.USER, soft);

soft.assertAll();
}

/**
* Login using invalid username
*/
/*  LGN-2 - Login using invalid username  */
@Description("LGN-2 - Login using invalid username")
@Link(name = "EDELIVERY-5034", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5034")
@AllureId("LGN-2")
@Test(description = "LGN-2", groups = {"multiTenancy", "singleTenancy"})
public void invalidUsername() throws Exception {
logout();

Allure.step("Testing login using invalid username");
log.info("Testing login using invalid username");
SoftAssert soft = new SoftAssert();

String username = Gen.randomAlphaNumeric(10);
rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);
Allure.step(String.format("Created user %s with role %s", username, DRoles.USER));
log.info(String.format("Created user %s with role %s", username, DRoles.USER));

LoginPage page = new LoginPage(driver);

page.login("invalidUserTest", data.defaultPass());
Allure.step(String.format("Trying to login with user=%s and pass=%s", "invalidUserTest", data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", "invalidUserTest", data.defaultPass()));

soft.assertFalse(page.getSandwichMenu().isLoggedIn(), "User not logged in");
soft.assertTrue(page.isLoaded(), "User is still on Login page");

Allure.step("Verifying correct error message");
log.info("Verifying correct error message");
soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_INVALID_CREDENTIALS, "Displayed message is correct");

//rest.deleteUser(username, null);
Allure.step("Deleted user: " + username);
log.info("Deleted user: " + username);

soft.assertAll();
}

/**
* Login using invalid password but valid username
*/
/*  LGN-3 - Login using invalid password but valid username  */
@Description("LGN-3 - Login using invalid password but valid username")
@Link(name = "EDELIVERY-5035", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5035")
@AllureId("LGN-3")
@Test(description = "LGN-3", groups = {"multiTenancy", "singleTenancy"})
public void invalidPassword() throws Exception {

logout();

Allure.step("Testing login using invalid password");
log.info("Testing login using invalid password");
SoftAssert soft = new SoftAssert();

String username = Gen.randomAlphaNumeric(10);
rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);
Allure.step(String.format("Created user %s with role %s", username, DRoles.USER));
log.info(String.format("Created user %s with role %s", username, DRoles.USER));

LoginPage page = new LoginPage(driver);

page.login(username, "invalidPassword");
Allure.step(String.format("Trying to login with user=%s and pass=%s", username, "invalidPassword"));
log.info(String.format("Trying to login with user=%s and pass=%s", username, "invalidPassword"));

soft.assertFalse(page.getSandwichMenu().isLoggedIn(), "User not logged in");
soft.assertTrue(page.isLoaded(), "User is still on Login page");

Allure.step("Verifying correct error message");
log.info("Verifying correct error message");
soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_INVALID_CREDENTIALS, "Displayed message is correct");

Allure.step("Deleted user: " + username);
log.info("Deleted user: " + username);

soft.assertAll();
}

/**
* Try to login with valid username and invalid password more than 5 times
*/
/*  LGN-4 - Try to login with valid username and nvalid password more than 5 times  */
@Description("LGN-4 - Try to login with valid username and nvalid password more than 5 times")
@Link(name = "EDELIVERY-5036", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5036")
@AllureId("LGN-4")
@Test(description = "LGN-4", groups = {"multiTenancy", "singleTenancy"})
public void blockUserAccountTest() throws Exception {

logout();

Allure.step("Try to login with valid username and invalid password more than 5 times");
log.info("Try to login with valid username and invalid password more than 5 times");
SoftAssert soft = new SoftAssert();
String username = "testBlockAcc_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);
Allure.step(String.format("Created user %s with role %s", username, DRoles.USER));
log.info(String.format("Created user %s with role %s", username, DRoles.USER));


LoginPage page = new LoginPage(driver);

for (int i = 0; i < 5; i++) {
Allure.step(String.format("Trying to login with user=%s and pass=%s", username, "password So Wrong"));
log.info(String.format("Trying to login with user=%s and pass=%s", username, "password So Wrong"));
page.login(username, "password So Wrong");

soft.assertFalse(page.getSandwichMenu().isLoggedIn(), "User not logged in");
soft.assertTrue(page.isLoaded(), "User is still on Login page");

Allure.step("Verifying error is displayed");
log.info("Verifying error is displayed");
soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed");

if (i <= 4) {
Allure.step("Verifying LOGIN_INVALID_CREDENTIALS error message is displayed");
log.info("Verifying LOGIN_INVALID_CREDENTIALS error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_INVALID_CREDENTIALS, "Displayed message is correct");
} else {
Allure.step("Verifying LOGIN_ACCOUNT_SUSPENDED error message is displayed");
log.info("Verifying LOGIN_ACCOUNT_SUSPENDED error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_ACCOUNT_SUSPENDED, "Account blocked message displayed as expected");
}
page.getAlertArea().closeAlert();
}

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());
soft.assertTrue(page.isLoaded(), "User is still on Login page");
soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed (2)");
Allure.step("Verifying LOGIN_ACCOUNT_SUSPENDED_1 error message is displayed");
log.info("Verifying LOGIN_ACCOUNT_SUSPENDED_1 error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_ACCOUNT_SUSPENDED_1, "Displayed message is correct (2)");

rest.users().activate(username, null);
Allure.step("Unblocked user account");
log.info("Unblocked user account");

//		wait required because the unlock is done trough REST API
page.wait.forXMillis(500);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());
soft.assertTrue(new DomibusPage(driver).getSandwichMenu().isLoggedIn(), "User is on Messages page, account is unblocked");

Allure.step("Deleted user: " + username);
log.info("Deleted user: " + username);

soft.assertAll();
}

/**
* Admin unlocks account and user tries to login with valid username and password
*/
/*  LGN-5 - Admin unlocks account and user tries to login with valid username and password  */
@Description("LGN-5 - Admin unlocks account and user tries to login with valid username and password")
@Link(name = "EDELIVERY-5037", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5037")
@AllureId("LGN-5")
@Test(description = "LGN-5", groups = {"multiTenancy", "singleTenancy"})
public void unblockedAccountCanLogin() throws Exception {

logout();

Allure.step("Admin unlocks account and user tries to login with valid username and password");
log.info("Admin unlocks account and user tries to login with valid username and password");
SoftAssert soft = new SoftAssert();
String username = "testBlockAcc_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);
Allure.step(String.format("Created user %s with role %s", username, DRoles.USER));
log.info(String.format("Created user %s with role %s", username, DRoles.USER));


LoginPage page = new LoginPage(driver);

for (int i = 0; i < 5; i++) {
Allure.step(String.format("Trying to login with user=%s and pass=%s", username, "password So Wrong"));
log.info(String.format("Trying to login with user=%s and pass=%s", username, "password So Wrong"));
page.login(username, "password So Wrong");
}

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());
Allure.step("Verifying LOGIN_ACCOUNT_SUSPENDED_1 error message is displayed");
log.info("Verifying LOGIN_ACCOUNT_SUSPENDED_1 error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_ACCOUNT_SUSPENDED_1, "User account blocked confirmed");

rest.users().activate(username, null);
Allure.step("Unblocked user account");
log.info("Unblocked user account");

//		wait required because the unlock is done trough REST API
page.wait.forXMillis(500);

Allure.step("Attempting login after account is unblocked");
log.info("Attempting login after account is unblocked");
page.login(username, data.defaultPass());
soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User is on Messages page, account is unblocked");

Allure.step("Deleted user: " + username);
log.info("Deleted user: " + username);

soft.assertAll();
}

/* LGN-6 - Login with locked account */
/*  LGN-6 - Login with locked account  */
@Description("LGN-6 - Login with locked account")
@Link(name = "EDELIVERY-7166", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7166")
@AllureId("LGN-6")
@Test(description = "LGN-6", groups = {"multiTenancy", "singleTenancy"})
public void lockedUserAccountCantLogin() throws Exception {
SoftAssert soft = new SoftAssert();
logout();


Allure.step("Create user and lock account with multiple invalid login attempts");
log.info("Create user and lock account with multiple invalid login attempts");
String username = "TestBlockAcc_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);
Allure.step(String.format("Created user %s with role %s", username, DRoles.USER));
log.info(String.format("Created user %s with role %s", username, DRoles.USER));

blockUserByRest(username, soft);

LoginPage page = new LoginPage(driver);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());

soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_ACCOUNT_SUSPENDED_1, "Correct error message displayed");
Allure.step("Verifying error message is displayed");
log.info("Verifying error message is displayed");

soft.assertAll();
}

/* LGN-7 - Login with deactivated account */
/*  LGN-7 - Login with deactivated account  */
@Description("LGN-7 - Login with deactivated account")
@Link(name = "EDELIVERY-7167", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7167")
@AllureId("LGN-7")
@Test(description = "LGN-7", groups = {"multiTenancy", "singleTenancy"})
public void deactivatedUserAccountCantLogin() throws Exception {
SoftAssert soft = new SoftAssert();
logout();

Allure.step("Create user and deactivate account");
log.info("Create user and deactivate account");
String username = "TestDeacc_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);
rest.users().deactivate(username, null);
Allure.step(String.format("Created user and deactivated %s with role %s", username, DRoles.USER));
log.info(String.format("Created user and deactivated %s with role %s", username, DRoles.USER));

LoginPage page = new LoginPage(driver);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());

Allure.step("Verifying error message is displayed");
log.info("Verifying error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_USER_INACTIVE, "Correct error message displayed");

soft.assertAll();
}

/* LGN-8 - Login with deleted user */
/*  LGN-8 - Login with deleted user  */
@Description("LGN-8 - Login with deleted user")
@Link(name = "EDELIVERY-7168", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7168")
@AllureId("LGN-8")
@Test(description = "LGN-8", groups = {"multiTenancy", "singleTenancy"})
public void deletedUserAccountCantLogin() throws Exception {
SoftAssert soft = new SoftAssert();
logout();

Allure.step("Create user and delete account");
log.info("Create user and delete account");
String username = "TestDel_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);
rest.users().deleteUser(username, null);
Allure.step(String.format("Created user and deleted %s with role %s", username, DRoles.USER));
log.info(String.format("Created user and deleted %s with role %s", username, DRoles.USER));

LoginPage page = new LoginPage(driver);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());

Allure.step("Verifying error message is displayed");
log.info("Verifying error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_INVALID_CREDENTIALS, "Correct error message displayed");

soft.assertAll();
}

/* LGN-9 - Login with deleted super user */
/*  LGN-9 - Login with deleted super user  */
@Description("LGN-9 - Login with deleted super user")
@Link(name = "EDELIVERY-7169", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7169")
@AllureId("LGN-9")
@Test(description = "LGN-9", groups = {"multiTenancy"})
public void deletedSuperUserCantLogin() throws Exception {
SoftAssert soft = new SoftAssert();
logout();

Allure.step("Create user and lock account with multiple invalid login attempts");
log.info("Create user and lock account with multiple invalid login attempts");
String username = "TestSupDel_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.SUPER, data.defaultPass(), null);
rest.users().deleteUser(username, null);
Allure.step(String.format("Created user and deleted %s with role %s", username, DRoles.SUPER));
log.info(String.format("Created user and deleted %s with role %s", username, DRoles.SUPER));

LoginPage page = new LoginPage(driver);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());

Allure.step("Verifying error message is displayed");
log.info("Verifying error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_INVALID_CREDENTIALS, "Correct error message displayed");

soft.assertAll();
}

/* LGN-10 - Login with deactivated super user */
/*  LGN-10 - Login with deactivated super user  */
@Description("LGN-10 - Login with deactivated super user")
@Link(name = "EDELIVERY-7170", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7170")
@AllureId("LGN-10")
@Test(description = "LGN-10", groups = {"multiTenancy"})
public void deactivatedSuperUserCantLogin() throws Exception {
SoftAssert soft = new SoftAssert();
logout();

Allure.step("Create super user and deactivate account");
log.info("Create super user and deactivate account");
String username = "TestSupDeact_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.SUPER, data.defaultPass(), null);
rest.users().deactivate(username, null);

Allure.step(String.format("Created user and deactivated %s with role %s", username, DRoles.SUPER));
log.info(String.format("Created user and deactivated %s with role %s", username, DRoles.SUPER));

LoginPage page = new LoginPage(driver);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());

Allure.step("Verifying error message is displayed");
log.info("Verifying error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_USER_INACTIVE, "Correct error message displayed");

soft.assertAll();
}

/* LGN-11 - Login with locked super user */
/*  LGN-11 - Login with locked super user  */
@Description("LGN-11 - Login with locked super user")
@Link(name = "EDELIVERY-7171", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7171")
@AllureId("LGN-11")
@Test(description = "LGN-11", groups = {"multiTenancy"})
public void lockedSuperUserCantLogin() throws Exception {
SoftAssert soft = new SoftAssert();
logout();

Allure.step("Create super user and lock account");
log.info("Create super user and lock account");
String username = "TestSup_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.SUPER, data.defaultPass(), null);
blockUserByRest(username, soft);
rest.users().deactivate(username, null);

Allure.step(String.format("Created user and locked %s with role %s", username, DRoles.SUPER));
log.info(String.format("Created user and locked %s with role %s", username, DRoles.SUPER));

LoginPage page = new LoginPage(driver);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());

Allure.step("Verifying error message is displayed");
log.info("Verifying error message is displayed");
soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_ACCOUNT_SUSPENDED_1, "Correct error message displayed");

soft.assertAll();
}

/* LGN-12 - Login with unblocked super user */
/*  LGN-12 - Login with unblocked super user  */
@Description("LGN-12 - Login with unblocked super user")
@Link(name = "EDELIVERY-7172", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7172")
@AllureId("LGN-12")
@Test(description = "LGN-12", groups = {"multiTenancy"})
public void unlockedSuperUserLogin() throws Exception {
SoftAssert soft = new SoftAssert();
logout();

Allure.step("Create super user and lock and unblock account");
log.info("Create super user and lock and unblock account");
String username = "TestSup_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.SUPER, data.defaultPass(), null);
blockUserByRest(username, soft);
rest.users().activate(username, null);

Allure.step(String.format("Created user, bocked and locked %s with role %s", username, DRoles.SUPER));
log.info(String.format("Created user, bocked and locked %s with role %s", username, DRoles.SUPER));

LoginPage page = new LoginPage(driver);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());

soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "Login success");

soft.assertAll();
}


/* LGN-13 - Login with reactivated super user  */
/*  LGN-13 - Login with reactivated super user  */
@Description("LGN-13 - Login with reactivated super user")
@Link(name = "EDELIVERY-7173", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7173")
@AllureId("LGN-13")
@Test(description = "LGN-13", groups = {"multiTenancy"})
public void reactivatedSuperUserLogin() throws Exception {
SoftAssert soft = new SoftAssert();
logout();

Allure.step("Create super user and deactivate and reactivate account");
log.info("Create super user and deactivate and reactivate account");
String username = "TestSup_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.SUPER, data.defaultPass(), null);

rest.users().deactivate(username, null);
rest.users().activate(username, null);

Allure.step(String.format("Created user, deactivated and reactivated %s with role %s", username, DRoles.SUPER));
log.info(String.format("Created user, deactivated and reactivated %s with role %s", username, DRoles.SUPER));

LoginPage page = new LoginPage(driver);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());

soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "Login success");

soft.assertAll();
}

/* LGN-14 - Login with reactivated user  */
/*  LGN-14 - Login with reactivated user  */
@Description("LGN-14 - Login with reactivated user")
@Link(name = "EDELIVERY-7174", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7174")
@AllureId("LGN-14")
@Test(description = "LGN-14", groups = {"multiTenancy", "singleTenancy"})
public void reactivatedDomainUserLogin() throws Exception {
SoftAssert soft = new SoftAssert();
logout();

Allure.step("Create user, deactivate and reactivate account");
log.info("Create user, deactivate and reactivate account");
String username = "TestUser_" + Gen.randomAlphaNumeric(3);
rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);

rest.users().deactivate(username, null);
rest.users().activate(username, null);

Allure.step(String.format("Created user, deactivated and reactivated %s with role %s", username, DRoles.ADMIN));
log.info(String.format("Created user, deactivated and reactivated %s with role %s", username, DRoles.ADMIN));

LoginPage page = new LoginPage(driver);

Allure.step(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
page.login(username, data.defaultPass());

soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "Login success");

soft.assertAll();
}

/* LGN-17 - Expired session popup */
/*  LGN-17 - Expired session popup  */
@Description("LGN-17 - Expired session popup")
@Link(name = "EDELIVERY-7354", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7354")
@AllureId("LGN-17")
@Test(description = "LGN-17", groups = {"multiTenancy", "singleTenancy"})
public void expiredSession() throws Exception {
SoftAssert soft = new SoftAssert();

driver.manage().deleteAllCookies();

new DomibusPage(driver).getSandwichMenu().openchangePassword();

LoginPage page = new LoginPage(driver);
soft.assertTrue(page.hasOpenDialog(), "Popup is present");
soft.assertTrue(page.isLoaded(), "Login page is loaded");

InfoModal modal = new InfoModal(driver);

soft.assertEquals(modal.getTitle(), "Session Expired", "correct popup title shown");
soft.assertEquals(modal.getMessage(), DMessages.SESSION_EXPIRED_MESSAGE, "correct message shown");

modal.getCloseBtn().click();

soft.assertAll();
}


private void blockUserByRest(String username, SoftAssert soft) {
for (int i = 0; i < 10; i++) {
ClientResponse response = rest.callLogin(username, "invalidPass");
if (response.getStatus() == 200) {
soft.fail("Login with rest service succeeded with wrong password");
}
String strResponse = response.getEntity(String.class);

if (StringUtils.containsIgnoreCase(strResponse, "blocked")) {
break;
}
}
}

}


//	You have been logged out because of inactivity or missing access permissions.


//   domibus.passwordPolicy.expiration
