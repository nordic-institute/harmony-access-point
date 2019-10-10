package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.BaseTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Alert.AlertFilters;
import pages.Alert.AlertPage;
import pages.login.LoginPage;
import pages.messages.MessageResendModal;
import pages.messages.MessagesPage;
import utils.Generator;

import java.util.HashMap;
import java.util.List;

public class AlertPgFunctionalTest extends BaseTest {

    //This method will do Search using Basic filters

    @Test(description = "ALRT-5", groups = {"multiTenancy", "singleTenancy"})
    public void SearchBasicFilters() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        LoginPage Lpage = new LoginPage(driver);
        String userName = Generator.randomAlphaNumeric(3);
        rest.createUser(userName, DRoles.USER, data.getDefaultTestPass(), null);
        Lpage.login(userName, data.getNewTestPass());
        soft.assertFalse(Lpage.getSandwichMenu().isLoggedIn(), "User not logged in");
        log.info("Alert Message shown : " + Lpage.getAlertArea().getAlertMessage());
        Lpage.login(data.getAdminUser());
        page.getSidebar().gGoToPage(PAGES.ALERTS);
        AlertPage Apage = new AlertPage(driver);
        log.info("Number of records : " + Apage.grid().getRowsNo());
        log.info("Getting all listed alert info");
        log.info("Alert type for top row : " + Apage.grid().getRowInfo(0).get("Alert Type"));
        String beforeSearchalertType = Apage.grid().getRowInfo(0).get("Alert Type");
        List<HashMap<String, String>> allRowInfo = Apage.grid().getAllRowInfo();
        HashMap<String, String> fAlert = allRowInfo.get(0);
        log.info("Basic filtering by " + fAlert);
        Apage.getFilters().basicFilterBy(null, fAlert.get("Alert Type"), fAlert.get("Alert Status"),
                fAlert.get("Alert level"), fAlert.get("Creation Time"), null);
        Apage.grid().waitForRowsToLoad();
        String afterSearchAlertType = Apage.grid().getRowInfo(0).get("Alert Type");
        soft.assertTrue(beforeSearchalertType.equals(afterSearchAlertType), "After and before search records are same");
        soft.assertAll();

    }

    //This method will do search operation using advance filters
    @Test(description = "ALRT-6", groups = {"multiTenancy", "singleTenancy"})
    public void SearchAdvanceFilters() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        LoginPage Lpage = new LoginPage(driver);
        String userName = Generator.randomAlphaNumeric(3);
        rest.createUser(userName, DRoles.USER, data.getDefaultTestPass(), null);
        Lpage.login(userName, data.getNewTestPass());
        soft.assertFalse(Lpage.getSandwichMenu().isLoggedIn(), "User not logged in");
        log.info("Alert Message shown : " + Lpage.getAlertArea().getAlertMessage());
        Lpage.login(data.getAdminUser());
        page.getSidebar().gGoToPage(PAGES.ALERTS);
        AlertPage Apage = new AlertPage(driver);
        log.info("Number of records : " + Apage.grid().getRowsNo());
        log.info("Getting all listed alert info");
        log.info("Alert type for top row : " + Apage.grid().getRowInfo(0).get("Alert Type"));
        String beforeSearchalertType = Apage.grid().getRowInfo(0).get("Alert Type");
        List<HashMap<String, String>> allRowInfo = Apage.grid().getAllRowInfo();
        HashMap<String, String> fAlert = allRowInfo.get(0);
        log.info("Advance filtering by " + fAlert);
        Apage.getFilters().advancedFilterBy(null, fAlert.get("Alert Type"), fAlert.get("Alert Status")
                , null, fAlert.get("Alert Level"), fAlert.get("Creation Time"), null,
                fAlert.get("Reporting Time"), null);
        Apage.grid().waitForRowsToLoad();
        String afterSearchAlertType = Apage.grid().getRowInfo(0).get("Alert Type");
        soft.assertTrue(beforeSearchalertType.equals(afterSearchAlertType), "After and before search records are same");
        soft.assertAll();
    }

    //This method will validate empty search result
    @Test(description = "ALRT-7", groups = {"multiTenancy", "singleTenancy"})
    public void EmptySearchResult() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        log.info("Login into application and navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.ALERTS);
        AlertPage Apage = new AlertPage(driver);
        log.info("Search using basic filters");
        Apage.getFilters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);
        Apage.grid().waitForRowsToLoad();
        log.info("Validate grid count as zero");
        soft.assertTrue(Apage.grid().getPagination().getTotalItems() == 0, "No search result exist");
        soft.assertAll();
    }

    //This method will validate presence of all records after deletion of all search criterias
    @Test(description = "ALRT-8", groups = {"multiTenancy", "singleTenancy"})
    public void DeleteSearchCriteria() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        log.info("Login into application and navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.ALERTS);
        AlertPage Apage = new AlertPage(driver);
        Apage.grid().waitForRowsToLoad();
        log.info("Search using basic filter");
        int prevCount = Apage.grid().getPagination().getTotalItems();
        log.info("Previous count of grid rows:" + prevCount);
        Apage.getFilters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);
        log.info("Validate Grid row count as zero ");
        soft.assertTrue(Apage.grid().getPagination().getTotalItems() == 0, "No search result exist");
        log.info("Refresh page");
        Apage.refreshPage();
        log.info("Wait for grid row to load ");
        Apage.grid().waitForRowsToLoad();
        log.info("Validate actual grid row count ");
        log.info("Current grid row count:" + Apage.grid().getPagination().getTotalItems());
        soft.assertTrue(Apage.grid().getPagination().getTotalItems() == prevCount, "All search result exist");
        soft.assertAll();
    }

    //This method will validate presence of show domain alert check box in case of super admin only
    @Test(description = "ALRT-11", groups = {"multiTenancy"})
    public void ShowDomainAlert() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        log.info("Login into application with super admin credentials and navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.ALERTS);
        AlertPage Apage = new AlertPage(driver);
        AlertFilters AFpage = new AlertFilters(driver);
        log.info("Check presence of Show domain checkbox");
        soft.assertTrue(AFpage.getShowDomainCheckbox().isPresent(), "CheckBox is  present in case of Admin User");
        log.info("Logout from application");
        logout();
        log.info("Login with admin credentials");
        login(data.getUser(DRoles.ADMIN)).getSidebar().gGoToPage(PAGES.ALERTS);
        log.info("Validate non availability of Show domain alert checkbox for Admin user");
        soft.assertFalse(AFpage.getShowDomainCheckbox().isPresent(), "CheckBox is not present in case of Admin User");
        soft.assertAll();
    }

    //This method will verify alert for message status change
    @Test(description = "ALRT-14", groups = {"multiTenancy", "singleTenancy"})
    public void MsgStatusChangeAlert() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        log.info("Upload pmode");
        rest.uploadPMode("pmodes/Edelivery-blue-NoRetry.xml", null);
        String user = Generator.randomAlphaNumeric(10);
        log.info("Create plugin users");
        rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(), null);
        log.info("Send message using plugin user credentials");
        String messID = messageSender.sendMessage(user, data.getDefaultTestPass(), null, null);
        log.info("Login into application");
        login(data.getAdminUser());
        MessagesPage Mpage = new MessagesPage(driver);
        log.info("Search data using message id");
        Mpage.getFilters().basicFilterBy(messID, null, null, null);
        Mpage.grid().waitForRowsToLoad();
        log.info("Select row 0");
        Mpage.grid().selectRow(0);
        log.info("Click on Resend button");
        Mpage.getResendButton().click();
        MessageResendModal modal = new MessageResendModal(driver);
        log.info("Click on Resend button on confirmation pop up");
        modal.getResendButton().click();
        log.info("Navigate to Alerts page");
        page.getSidebar().gGoToPage(PAGES.ALERTS);
        AlertPage Apage = new AlertPage(driver);
        log.info("Search data using Msg_status_changed alert type");
        Apage.getFilters().basicFilterBy(null, "MSG_STATUS_CHANGED", null, null, null, null);
        log.info("Check if Multidomain exists");
        if (data.isIsMultiDomain()) {
            log.info("Click on Show domain checkbox");
            Apage.getFilters().getShowDomainCheckbox().click();
            log.info("Click on search button");
            Apage.getFilters().getSearchButton().click();
        }
        log.info("Validate data for given message id,status ,alert type ,alert status and level");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Alert Type").contains("MSG_STATUS_CHANGED"), "Top row contains alert type as Msg_Status_Changed");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Alert Level").contains("HIGH"), "Top row contains alert level as High");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Top row contains alert Status as Success");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Parameters").contains(messID), "Top row contains alert for message status changed for :" + messID);
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Parameters").contains("SEND_FAILURE"), "Top row contains alert for message status as Send_failure");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Parameters").contains("SEND_ENQUEUED"), "Top row contains alert for message status as Send_Enqueued");
        soft.assertAll();

    }

    //This method will verify alert for user login failure case
    @Test(description = "ALRT-17", groups = {"multiTenancy", "singleTenancy"})
    public void UserLoginFailureAlert() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        LoginPage Lpage = new LoginPage(driver);
        String userName = Generator.randomAlphaNumeric(3);
        log.info("Create user");
        rest.createUser(userName, DRoles.USER, data.getDefaultTestPass(), null);
        log.info("Try to login with user with wrong password");
        Lpage.login(userName, data.getNewTestPass());
        log.info("Alert message shown :" + page.getAlertArea().getAlertMessage());
        log.info("Login into application");
        Lpage.login(data.getAdminUser());
        log.info("Navigate to Alerts page");
        page.getSidebar().gGoToPage(PAGES.ALERTS);
        AlertPage Apage = new AlertPage(driver);
        log.info("Search data using basic filter for user_login_failure alert type");
        Apage.getFilters().basicFilterBy(null, "USER_LOGIN_FAILURE", null, null, null, null);
        log.info("Check if multidomain exists");
        if (data.isIsMultiDomain()) {
            log.info("Select show domain check box");
            Apage.getFilters().getShowDomainCheckbox().click();
            log.info("Click on search button");
            Apage.getFilters().getSearchButton().click();
        }
        log.info("Validate pressence of alert data for user_login_failure alert type for given user");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Alert Type").contains("USER_LOGIN_FAILURE"), "Top row contains alert type as USER_LOGIN_FAILURE");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Alert Level").contains("LOW"), "Top row contains alert level as low");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Top row contains alert status as success");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Parameters").contains(userName), "Top row contains alert type as USER_LOGIN_FAILURE");
        soft.assertAll();
    }

    //This method will verify alert for user account disable after 5 attempts of login with wrong credentials
    @Test(description = "ALRT-18", groups = {"multiTenancy", "singleTenancy"})
    public void UserDisableAlert() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        String userName = Generator.randomAlphaNumeric(3);
        log.info("Create user");
        rest.createUser(userName, DRoles.USER, data.getDefaultTestPass(), null);
        LoginPage Lpage = new LoginPage(driver);
        log.info("Try to login with wrong password for 5 times so that user account gets disabled");
        for (int i = 0; i <= 5; i++) {
            Lpage.login(userName, "abc");
            log.info("Alert Message shown : " + Lpage.getAlertArea().getAlertMessage());
        }
        log.info("Login with Super/admin user");
        Lpage.login(data.getAdminUser());
        log.info("Navigate to Alerts page");
        page.getSidebar().gGoToPage(PAGES.ALERTS);
        AlertPage Apage = new AlertPage(driver);
        log.info("Search by basic filter for alert type : user account disabled");
        Apage.getFilters().basicFilterBy(null, "USER_ACCOUNT_DISABLED", null, null, null, null);
        log.info("Check if multi domain exists");
        if (data.isIsMultiDomain()) {
            log.info("Check show domain alert checkbox");
            Apage.getFilters().getShowDomainCheckbox().click();
            log.info("Click on search button");
            Apage.getFilters().getSearchButton().click();
        }
        log.info("Validate top row for user account disabled alert type for given user");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Alert Type").contains("USER_ACCOUNT_DISABLED"), "Alert for disabled account is shown ");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Alert Level").contains("HIGH"), "Disable account alert is of High level");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Account disabled alert has Success status");
        soft.assertTrue(Apage.grid().getRowInfo(0).get("Parameters").contains(userName), "Alert for user :" + userName + "disabled account is shown here");
        soft.assertAll();

    }


}











