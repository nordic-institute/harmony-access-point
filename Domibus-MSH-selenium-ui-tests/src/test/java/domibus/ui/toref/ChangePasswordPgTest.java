package domibus.ui.toref;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.BaseTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.ChangePassword.ChangePasswordPage;
import pages.errorLog.ErrorLogPage;
import pages.login.LoginPage;
import pages.messages.MessagesPage;
import utils.Generator;



/**
 * @author Rupam
 * @version 4.1
 *
 */

public class ChangePasswordPgTest extends BaseTest {

    /**
     * Checks whether changePassword page exists and it is opened successfully
     */
    @Test(description = "CP-1", groups = {"multiTenancy", "singleTenancy"})
    public void NavigateToChangePassword() throws Exception {

        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("Validate login page ");
        soft.assertTrue(loginPage.isLoaded(),"Login page is loaded successfully");
        log.info("Login into application with Admin credentials");
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        loginPage.waitForTitle();
        page.clickVoidSpace();
        log.info("Validate if Change password link is available in Sandwich menu");
        soft.assertTrue(page.getSandwichMenu().isPresent(), "Change Password link is available");
        log.info("Open ChangePassword Page by clicking link");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        log.info("Validate Page Header");
        soft.assertTrue(Cpage.verifyFieldHeader(), "Correct page is opened");
        soft.assertAll();
    }

    /**
     * This method verifies whether page is properly loaded with all its elements
     */
    @Test(description = "CP-2", groups = {"multiTenancy", "singleTenancy"})
    public void VerifyPageElements() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("validate login page");
        soft.assertTrue(loginPage.isLoaded(),"Login Page is loaded successfully");
        log.info("Login into application with Admin credentials");
        loginPage.login(data.getAdminUser());
        loginPage.waitForTitle();
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        log.info("Check Change password link availability");
        soft.assertTrue(page.getSandwichMenu().isPresent(), "Change Password link is available");
        log.info("Open ChangePassword Page by clicking link");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        log.info("Page is loaded successfully with all required elements");
        soft.assertTrue(Cpage.isLoaded(), "Page is loaded successfully");
        soft.assertAll();
    }

    /*
    This method confirms no change in password if user navigates to other page without changing password
     */
    @Test(description = "CP-3", groups = {"multiTenancy", "singleTenancy"})
    public void UnchangedPasswordOnNavigation() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("validate login page");
        soft.assertTrue(loginPage.isLoaded(),"Login Page is loaded successfully");
        log.info("Login into application with Admin credentials");
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        log.info("Open ChangePassword Page by clicking link");
        page.getSandwichMenu().OpenchangePassword();
        log.info("Navigate to error log page without changing password ");
        page.getSidebar().gGoToPage(PAGES.ERROR_LOG);
        ErrorLogPage errorLogPage = new ErrorLogPage(driver);
        page.waitForTitle();
        page.waitForTitle();
        log.info("logout from application");
        logout();
        log.info("Again login into application with Admin credential");
        loginPage.login(data.getAdminUser());
        log.info("Validate user is logged in successfully");
        loginPage.waitForTitle();
        soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");
        soft.assertAll();
    }

    /**
     * This method confirms no change in password if user navigate to other page after filling data in all three password
     * related fields but  before clicking on update button
     */
    @Test(description = "CP-4", groups = {"multiTenancy", "singleTenancy"})
    public void NavigationwithoutClickingUpdate() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        MessagesPage Mpage= new MessagesPage(driver);
        log.info("Validate login page ");
        soft.assertTrue(loginPage.isLoaded(),"page is opened successfully");
        log.info("Login into application with admin credentials");
        loginPage.login(data.getAdminUser());
        loginPage.waitForTitle();
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        log.info("Open ChangePassword Page by clicking link");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        log.info("Fill data in Current password,new password and confirmation fields");
        Cpage.setPassFields(data.getDefaultTestPass(), data.getDefaultTestPass(), data.getDefaultTestPass());
        log.info("Navigate to Error log page");
        page.getSidebar().gGoToPage(PAGES.ERROR_LOG);
        ErrorLogPage errorLogPage = new ErrorLogPage(driver);
        page.waitForTitle();
        log.info("logout from application");
        logout();
        log.info("login into application with previous admin credentials");
        loginPage.login(data.getAdminUser());
        loginPage.waitForTitle();
        log.info("Validate User is logged in into application");
        soft.assertTrue(Mpage.isLoaded(), "User logged in");
        soft.assertAll();
    }

    /**
     * This method shows the implemented error on update when user enters wrong current password but valid new password
     */
    @Test(description = "CP-5", groups = {"multiTenancy", "singleTenancy"})
    public void WrongCurrentPassword() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("Validate login page is loaded successfully");
        soft.assertTrue(loginPage.isLoaded(),"page is loaded successfully");
        log.info("Login into application with Admin credentials");
        loginPage.login(data.getAdminUser());
        loginPage.waitForTitle();
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        log.info("Open ChangePassword Page by clicking link");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        log.info("Fill wrong data for current field and correct data for new password and confirmation");
        Cpage.setPassFields(data.getDefaultTestPass(), data.getDefaultTestPass(), data.getDefaultTestPass());
        log.info("Click on update button");
        Cpage.getUpdateButton().click();
        log.info("Error message shown:"+page.getAlertArea().getAlertMessage() );
        soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.CHANGEPASSWORD_WRONG_CURRENT_PASSWORD, "Displayed message is correct");
        soft.assertAll();
    }

    /**
     * This method ensures validation message for Confirmation if New password and Confirmation field is different but valid
     */
    @Test(description = "CP-6", groups = {"multiTenancy", "singleTenancy"})
    public void DiffValidData() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("Validate login page ");
        soft.assertTrue(loginPage.isLoaded(),"page is loaded successfully");
        log.info("Login with Admin credential");
        loginPage.login(data.getAdminUser());
        loginPage.waitForTitle();
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        log.info("Validate change password link availability in Sandwich menu");
        soft.assertTrue(page.getSandwichMenu().isPresent(), "Change Password link is available");
        log.info("Open change password page by clicking its link");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        log.info("Fill correct data in current password , valid and different data in new password and confirmation field");
        Cpage.setPassFields(data.getDefaultTestPass(), data.getDefaultTestPass(), data.getNewTestPass());
        log.info("press tab key");
        Cpage.getConfirmationField().pressTABKey(Cpage.getConfirmationField().element);
        log.info("Validation message appears");
        soft.assertTrue(Cpage.getValidationMsg(Cpage.Confirmation_Field_label),"Message is displayed");
        soft.assertAll();
    }

    /**
     * This method ensures validation message for password policy if New password and Confirmation field is same but invalid
     */
    @Test(description = "CP-7", groups = {"multiTenancy", "singleTenancy"})
    public void SameInValidData() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("Validate login page ");
        soft.assertTrue(loginPage.isLoaded(),"page is loaded successfully");
        log.info("login into application with Admin user");
        loginPage.login(data.getAdminUser());
        loginPage.waitForTitle();
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        log.info("Open change password page");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        log.info("Generate invalid password");
        String pass = Cpage.generateInvalidPass();
        log.info("Fill correct data for current password and invalid but same for new password and confirmation field");
        Cpage.setPassFields(data.getDefaultTestPass(), pass, pass);
        log.info("press tab ");
        Cpage.getConfirmationField().pressTABKey(Cpage.getConfirmationField().element);
        log.info("Validation message for password policy under New password ");
        soft.assertTrue(Cpage.getValidationMsg(Cpage.NewPassword_Field_label),"Message is displayed");
        soft.assertAll();
    }

    /**
     * This method ensures validation message for no match to password policy and with confirmation password when
     * New password and Confirmation field is different and invalid
     */
    @Test(description = "CP-8", groups = {"multiTenancy", "singleTenancy", "incomplete"})
    public void DiffInValidData() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("Validate login page");
        soft.assertTrue(loginPage.isLoaded(),"page is loaded successsfully");
        log.info("login into application with Admin user credentials");
        loginPage.login(data.getAdminUser());
        loginPage.waitForTitle();
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        log.info("Open Change Password page");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        log.info("Fill current data for current password, invalid  and differet data for new password and confirmation field");
        Cpage.setPassFields(data.getDefaultTestPass(), Cpage.generateInvalidPass(), Cpage.generateInvalidPass());
        log.info("press tab key");
        Cpage.getConfirmationField().pressTABKey(Cpage.getConfirmationField().element);
        log.info("Validation message for password policy under new password field");
       soft.assertTrue(Cpage.getValidationMsg(Cpage.NewPassword_Field_label),"Message is displayed");
       log.info("Validation message for mismatch under confirmation field ");
        soft.assertTrue(Cpage.getValidationMsg(Cpage.Confirmation_Field_label),"Message is displayed");
        soft.assertAll();
    }

    /**
     * This method will change password and allow to login successfully with new credentials
     */
    @Test(description = "CP-9", groups = {"multiTenancy", "singleTenancy"})
    public void ChangePassword() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Generate random username");
        String username = Generator.randomAlphaNumeric(10);
        log.info("Create user with rest call");
        rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
        LoginPage loginPage = new LoginPage(driver);
        log.info("Validate login page" );
        soft.assertTrue(loginPage.isLoaded(),"page is loaded successfully");
        log.info("login into application with created user credentials");
        loginPage.login(username, data.getDefaultTestPass());
        loginPage.waitForTitle();
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        log.info("Validate chnage password link presence");
        soft.assertTrue(page.getSandwichMenu().isPresent(), "Change Password link is available");
        log.info("Open Change password page ");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        log.info("Fill correct data in current password , Valid and same data in new password and confirmation field");
        Cpage.setPassFields(data.getDefaultTestPass(), data.getNewTestPass(), data.getNewTestPass());
        log.info("Click on update button");
        Cpage.getUpdateButton().click();
        log.info("wait for Message page title");
        page.waitForTitle();
        log.info("logout from application");
        logout();
        log.info("login with generated username and updated password ");
        loginPage.login(username, data.getNewTestPass());
        loginPage.waitForTitle();
        log.info("Validate user logged in into application");
        soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");
        soft.assertAll();
    }

    /**
     * This method will throw error in new password is among previous 5 passwords.
     */
    @Test(description = "CP-10", groups = {"multiTenancy", "singleTenancy"})
    public void NewPasswordAmongLast5() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Generate random username");
        String username = Generator.randomAlphaNumeric(10);
        log.info("Create user with above created username and default password through rest call");
        rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
        LoginPage loginPage = new LoginPage(driver);
        log.info("Validate login page");
        soft.assertTrue(loginPage.isLoaded(),"page is loaded successfully");
        log.info("login into application with cretaed user credentials");
        loginPage.login(username, data.getDefaultTestPass());
        loginPage.waitForTitle();
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        log.info("Open change password page");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        log.info("Fill correct and valid data in all fields");
        Cpage.setPassFields(data.getDefaultTestPass(), data.getNewTestPass(), data.getNewTestPass());
        log.info("click on update button");
        Cpage.getUpdateButton().click();
        log.info("Wait for Message page title");
        page.waitForTitle();
        log.info("logout from application");
        logout();
        log.info("Login with username and updated password");
        loginPage.login(username, data.getNewTestPass());
        loginPage.waitForTitle();
        page.clickVoidSpace();
        log.info("Open Change password page");
        page.getSandwichMenu().OpenchangePassword();
        log.info("Fill correct data in current password , same in new password and confirmation field");
        Cpage.setPassFields(data.getNewTestPass(), data.getNewTestPass(), data.getNewTestPass());
        log.info("Click on update button");
        Cpage.getUpdateButton().click();
        log.info("Error message shown:"+page.getAlertArea().getAlertMessage() );
        soft.assertTrue(!page.getAlertArea().getAlertMessage().isEmpty(),"Message is not displayed");
        soft.assertAll();
    }
}