package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.ChangePassword.ChangePasswordLocators;
import pages.ChangePassword.ChangePasswordPage;
import pages.errorLog.ErrorLogPage;
import pages.login.LoginPage;
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
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        soft.assertTrue(page.getSandwichMenu().isPresent(), "Change Password link is available");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        soft.assertTrue(Cpage.VerifyFieldHeader(), "Correct page is opened");
        soft.assertAll();
    }

    /**
     * This method verifies whether page is properly loaded with all its elements
     */
    @Test(description = "CP-2", groups = {"multiTenancy", "singleTenancy"})
    public void VerifyPageElements() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        soft.assertTrue(page.getSandwichMenu().isPresent(), "Change Password link is available");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
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
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        page.getSandwichMenu().OpenchangePassword();
        page.getSidebar().gGoToPage(PAGES.ERROR_LOG);
        ErrorLogPage errorLogPage = new ErrorLogPage(driver);
        soft.assertTrue(errorLogPage.isLoaded(), "Expected elements of ErrorLog appear in the page");
        this.logout();
        loginPage.login(data.getAdminUser());
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
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        Cpage.setPassFields(data.getDefaultTestPass(), data.getDefaultTestPass(), data.getDefaultTestPass());
        page.getSidebar().gGoToPage(PAGES.ERROR_LOG);
        ErrorLogPage errorLogPage = new ErrorLogPage(driver);
        soft.assertTrue(errorLogPage.isLoaded(), "Expected elements of ErrorLog appear in the page");
        logout();
        loginPage.login(data.getAdminUser());
        soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");
        soft.assertAll();
    }

    /**
     * This method shows the implemented error on update when user enters wrong current password but valid new password
     */
    @Test(description = "CP-5", groups = {"multiTenancy", "singleTenancy"})
    public void WrongCurrentPassword() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        Cpage.setPassFields(data.getDefaultTestPass(), data.getDefaultTestPass(), data.getDefaultTestPass());
        Cpage.getUpdateButton().click();
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
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        soft.assertTrue(page.getSandwichMenu().isPresent(), "Change Password link is available");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        Cpage.setPassFields(data.getDefaultTestPass(), data.getDefaultTestPass(), data.getNewTestPass());
        Cpage.pressTABKey();
        soft.assertTrue(!Cpage.getValidationMsg(ChangePasswordLocators.Confirmation_Field_label).isEmpty(),"Message is displayed");

        soft.assertAll();
    }

    /**
     * This method ensures validation message for password policy if New password and Confirmation field is same but invalid
     */
    @Test(description = "CP-7", groups = {"multiTenancy", "singleTenancy"})
    public void SameInValidData() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        String pass = Cpage.generateInvalidPass();
        Cpage.setPassFields(data.getDefaultTestPass(), pass, pass);
        Cpage.pressTABKey();
        soft.assertTrue(!Cpage.getValidationMsg(ChangePasswordLocators.NewPassword_Field_label).isEmpty(),"Message is displayed");
        soft.assertAll();
    }

    /**
     * This method ensures validation message for no match to password policy and with confirmation password when
     * New password and Confirmation field is different and invalid //remaining to implement message printing for second field
     */
    @Test(description = "CP-8", groups = {"multiTenancy", "singleTenancy", "incomplete"})
    public void DiffInValidData() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        Cpage.setPassFields(data.getDefaultTestPass(), Cpage.generateInvalidPass(), Cpage.generateInvalidPass());
        Cpage.pressTABKey();
       soft.assertTrue(!Cpage.getValidationMsg(ChangePasswordLocators.NewPassword_Field_label).isEmpty(),"Message is displayed");
        soft.assertTrue(!Cpage.getValidationMsg(ChangePasswordLocators.Confirmation_Field_label).isEmpty(),"Message is displayed");
        soft.assertAll();
    }

    /**
     * This method will change password and allow to login successfully with new credentials
     */
    @Test(description = "CP-9", groups = {"multiTenancy", "singleTenancy"})
    public void ChangePassword() throws Exception {
        SoftAssert soft = new SoftAssert();
        String username = Generator.randomAlphaNumeric(10);
        rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
        LoginPage loginPage = new LoginPage(driver);
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(username, data.getDefaultTestPass());
        System.out.println(data.getDefaultTestPass());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        soft.assertTrue(page.getSandwichMenu().isPresent(), "Change Password link is available");
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        Cpage.setPassFields(data.getDefaultTestPass(), data.getNewTestPass(), data.getNewTestPass());
        Cpage.getUpdateButton().click();
        page.waitForTitle();
        logout();
        loginPage.login(username, data.getNewTestPass());
        soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");
        soft.assertAll();
    }

    /**
     * This method will throw error in new password is among previous 5 passwords.
     */
    @Test(description = "CP-10", groups = {"multiTenancy", "singleTenancy"})
    public void NewPasswordAmongLast5() throws Exception {
        SoftAssert soft = new SoftAssert();
        String username = Generator.randomAlphaNumeric(10);
        rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
        LoginPage loginPage = new LoginPage(driver);
        soft.assertTrue(loginPage.isLoaded());
        loginPage.login(username, data.getDefaultTestPass());
        DomibusPage page = new DomibusPage(driver);
        page.clickVoidSpace();
        page.getSandwichMenu().OpenchangePassword();
        ChangePasswordPage Cpage = new ChangePasswordPage(driver);
        Cpage.setPassFields(data.getDefaultTestPass(), data.getNewTestPass(), data.getNewTestPass());
        Cpage.getUpdateButton().click();
        page.waitForTitle();
        logout();
        loginPage.login(username, data.getNewTestPass());
        page.clickVoidSpace();
        page.getSandwichMenu().OpenchangePassword();
        Cpage.setPassFields(data.getNewTestPass(), data.getNewTestPass(), data.getNewTestPass());
        Cpage.getUpdateButton().click();
        System.out.println(page.getAlertArea().getAlertMessage());
        soft.assertTrue(!page.getAlertArea().getAlertMessage().isEmpty(),"Message is not displayed");
        soft.assertAll();
    }
}