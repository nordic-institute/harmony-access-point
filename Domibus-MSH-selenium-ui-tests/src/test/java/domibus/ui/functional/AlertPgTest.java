package domibus.ui.functional;


import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.testng.SkipException;
import pages.Audit.AuditPage;
import pages.users.UsersPage;
import utils.BaseTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Alert.AlertFilters;
import pages.Alert.AlertPage;
import pages.login.LoginPage;
import pages.messages.MessageResendModal;
import pages.messages.MessagesPage;
import utils.Generator;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class AlertPgTest extends BaseTest {

    //This method will do Search using Basic filters
    @Test(description = "ALRT-5", groups = {"multiTenancy", "singleTenancy"})
    public void searchBasicFilters() throws Exception {
        SoftAssert soft = new SoftAssert();
        String userName = Generator.randomAlphaNumeric(3);
        rest.createUser(userName, DRoles.USER, data.defaultPass(), null);
        log.info("created user " + userName);

        log.info("invalid login with " + userName);
        DomibusPage page = login(userName, data.getNewTestPass());
        soft.assertFalse(page.getSandwichMenu().isLoggedIn(), "User not logged in");
        log.info("Alert Message shown : " + page.getAlertArea().getAlertMessage());

        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        AlertPage apage = new AlertPage(driver);
        if (data.isMultiDomain()) {
            apage.filters().showDomainAlert();
        }

        log.info("Number of records : " + apage.grid().getRowsNo());
        log.info("Getting all listed alert info");
        log.info("Alert type for top row : " + apage.grid().getRowInfo(0).get("Alert Type"));
        String beforeSearchalertType = apage.grid().getRowInfo(0).get("Alert Type");
        List<HashMap<String, String>> allRowInfo = apage.grid().getAllRowInfo();
        HashMap<String, String> fAlert = allRowInfo.get(0);
        log.info("Basic filtering by " + fAlert);
        apage.filters().basicFilterBy(null, fAlert.get("Alert Type"), fAlert.get("Alert Status"),
                fAlert.get("Alert level"), fAlert.get("Creation Time"), null);
        apage.grid().waitForRowsToLoad();
        String afterSearchAlertType = apage.grid().getRowInfo(0).get("Alert Type");
        soft.assertTrue(beforeSearchalertType.equals(afterSearchAlertType), "After and before search records are same");
        soft.assertAll();

    }

    //This method will do search operation using advance filters
    @Test(description = "ALRT-6", groups = {"multiTenancy", "singleTenancy"})
    public void searchAdvanceFilters() throws Exception {
        SoftAssert soft = new SoftAssert();
        String userName = Generator.randomAlphaNumeric(3);
        rest.createUser(userName, DRoles.USER, data.defaultPass(), null);
        log.info("created user " + userName);

        log.info("invalid login with " + userName);
        DomibusPage page = login(userName, data.getNewTestPass());
        soft.assertFalse(page.getSandwichMenu().isLoggedIn(), "User not logged in");
        log.info("Alert Message shown : " + page.getAlertArea().getAlertMessage());

        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        AlertPage apage = new AlertPage(driver);
        if (data.isMultiDomain()) {
            apage.filters().showDomainAlert();
        }

        log.info("Number of records : " + apage.grid().getRowsNo());
        log.info("Getting all listed alert info");
        log.info("Alert type for top row : " + apage.grid().getRowInfo(0).get("Alert Type"));
        String beforeSearchalertType = apage.grid().getRowInfo(0).get("Alert Type");
        List<HashMap<String, String>> allRowInfo = apage.grid().getAllRowInfo();
        HashMap<String, String> fAlert = allRowInfo.get(0);
        log.info("Advance filtering by " + fAlert);
        apage.filters().advancedFilterBy(null, fAlert.get("Alert Type"), fAlert.get("Alert Status")
                , null, fAlert.get("Alert Level"), fAlert.get("Creation Time"), null,
                fAlert.get("Reporting Time"), null);
        apage.grid().waitForRowsToLoad();
        String afterSearchAlertType = apage.grid().getRowInfo(0).get("Alert Type");
        soft.assertTrue(beforeSearchalertType.equals(afterSearchAlertType), "After and before search records are same");
        soft.assertAll();
    }

    //This method will validate empty search result
    @Test(description = "ALRT-7", groups = {"multiTenancy", "singleTenancy"})
    public void emptySearchResult() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        log.info("Login into application and navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        AlertPage apage = new AlertPage(driver);
        log.info("Search using basic filters");
        apage.filters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);
        apage.grid().waitForRowsToLoad();
        log.info("Validate grid count as zero");
        soft.assertTrue(apage.grid().getPagination().getTotalItems() == 0, "No search result exist");
        soft.assertAll();
    }

    //This method will validate presence of all records after deletion of all search criterias
    @Test(description = "ALRT-8", groups = {"multiTenancy", "singleTenancy"})
    public void deleteSearchCriteria() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application and navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        AlertPage apage = new AlertPage(driver);
        if (data.isMultiDomain()) {
            apage.filters().showDomainAlert();
        }
        log.info("Wait for grid row to load ");
        apage.grid().waitForRowsToLoad();

        log.info("Search using basic filter");
        int prevCount = apage.grid().getPagination().getTotalItems();
        log.info("Previous count of grid rows:" + prevCount);
        apage.filters().basicFilterBy(null, "CERT_EXPIRED", null, null, null, null);
        log.info("Validate Grid row count as zero ");
        soft.assertTrue(apage.grid().getPagination().getTotalItems() < prevCount, "No search result exist");
        log.info("Refresh page");

        apage.refreshPage();
        if (data.isMultiDomain()) {
            apage.filters().showDomainAlert();
        }

        log.info("Wait for grid row to load ");
        apage.grid().waitForRowsToLoad();
        log.info("Validate actual grid row count ");
        log.info("Current grid row count:" + apage.grid().getPagination().getTotalItems());
        soft.assertTrue(apage.grid().getPagination().getTotalItems() == prevCount, "All search result exist");
        soft.assertAll();
    }

    //This method will validate presence of show domain alert check box in case of super admin only
    @Test(description = "ALRT-11", groups = {"multiTenancy"})
    public void showDomainAlert() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with super admin credentials and navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        AlertPage apage = new AlertPage(driver);
        AlertFilters filters = apage.filters();
        log.info("Check presence of Show domain checkbox");
        soft.assertTrue(filters.getShowDomainCheckbox().isPresent(), "CheckBox is  present in case of super User");
        log.info("Logout from application");
        logout();
        log.info("Login with admin credentials");
        login(getUser(null, DRoles.ADMIN, true, false, true).getString("userName"), data.defaultPass())
                .getSidebar().goToPage(PAGES.ALERTS);
        log.info("Validate non availability of Show domain alert checkbox for Admin user");
        soft.assertFalse(filters.getShowDomainCheckbox().isPresent(), "CheckBox is not present in case of Admin User");
        soft.assertAll();
    }

    //This method will verify alert for message status change
    @Test(description = "ALRT-14", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
    public void msgStatusChangeAlert() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        log.info("Upload pmode");
        rest.uploadPMode("pmodes/Edelivery-blue-NoRetry.xml", null);
        String user = Generator.randomAlphaNumeric(10);
        log.info("Create plugin users");
        rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);
        log.info("Send message using plugin user credentials");
        String messID = messageSender.sendMessage(user, data.defaultPass(), null, null);
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
        page.getSidebar().goToPage(PAGES.ALERTS);
        AlertPage apage = new AlertPage(driver);
        log.info("Search data using Msg_status_changed alert type");
        apage.filters().basicFilterBy(null, "MSG_STATUS_CHANGED", null, null, null, null);
        log.info("Check if Multidomain exists");
        if (data.isMultiDomain()) {
            log.info("Click on Show domain checkbox");
            apage.filters().getShowDomainCheckbox().click();
            log.info("Click on search button");
            apage.filters().getSearchButton().click();
        }
        log.info("Validate data for given message id,status ,alert type ,alert status and level");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Type").contains("MSG_STATUS_CHANGED"), "Top row contains alert type as Msg_Status_Changed");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Level").contains("HIGH"), "Top row contains alert level as High");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Top row contains alert Status as Success");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains(messID), "Top row contains alert for message status changed for :" + messID);
        soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains("SEND_FAILURE"), "Top row contains alert for message status as Send_failure");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains("SEND_ENQUEUED"), "Top row contains alert for message status as Send_Enqueued");
        soft.assertAll();

    }

    //This method will verify alert for user login failure case
    @Test(description = "ALRT-17", groups = {"multiTenancy", "singleTenancy"})
    public void userLoginFailureAlert() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        LoginPage lpage = new LoginPage(driver);
        String userName = Generator.randomAlphaNumeric(3);
        log.info("Create user");
        rest.createUser(userName, DRoles.USER, data.defaultPass(), null);
        log.info("Try to login with user with wrong password");
        lpage.login(userName, data.getNewTestPass());
        log.info("Alert message shown :" + page.getAlertArea().getAlertMessage());
        log.info("Login into application");
        lpage.login(data.getAdminUser());
        log.info("Navigate to Alerts page");
        page.getSidebar().goToPage(PAGES.ALERTS);
        AlertPage apage = new AlertPage(driver);
        log.info("Search data using basic filter for user_login_failure alert type");
        apage.filters().basicFilterBy(null, "USER_LOGIN_FAILURE", null, null, null, null);
        log.info("Check if multidomain exists");
        if (data.isMultiDomain()) {
            log.info("Select show domain check box");
            apage.filters().getShowDomainCheckbox().click();
            log.info("Click on search button");
            apage.filters().getSearchButton().click();
        }
        log.info("Validate pressence of alert data for user_login_failure alert type for given user");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Type").contains("USER_LOGIN_FAILURE"), "Top row contains alert type as USER_LOGIN_FAILURE");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Level").contains("LOW"), "Top row contains alert level as low");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Top row contains alert status as success");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains(userName), "Top row contains alert type as USER_LOGIN_FAILURE");
        soft.assertAll();
    }

    //This method will verify alert for user account disable after 5 attempts of login with wrong credentials
    @Test(description = "ALRT-18", groups = {"multiTenancy", "singleTenancy"})
    public void userDisableAlert() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        String userName = Generator.randomAlphaNumeric(3);
        log.info("Create user");
        rest.createUser(userName, DRoles.USER, data.defaultPass(), null);
        LoginPage lpage = new LoginPage(driver);
        log.info("Try to login with wrong password for 5 times so that user account gets disabled");
        for (int i = 0; i < 5; i++) {
            lpage.login(userName, "abc");
            log.info("Alert Message shown : " + lpage.getAlertArea().getAlertMessage());
        }
        log.info("Login with Super/admin user");
        lpage.login(data.getAdminUser());
        log.info("Navigate to Alerts page");
        page.getSidebar().goToPage(PAGES.ALERTS);
        AlertPage apage = new AlertPage(driver);
        log.info("Search by basic filter for alert type : user account disabled");
        apage.filters().basicFilterBy(null, "USER_ACCOUNT_DISABLED", null, null, null, null);
        log.info("Check if multi domain exists");
        if (data.isMultiDomain()) {
            log.info("Check show domain alert checkbox");
            apage.filters().getShowDomainCheckbox().click();
            log.info("Click on search button");
            apage.filters().getSearchButton().click();
        }
        log.info("Validate top row for user account disabled alert type for given user");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Type").contains("USER_ACCOUNT_DISABLED"), "Alert for disabled account is shown ");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Level").contains("HIGH"), "Disable account alert is of High level");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Account disabled alert has Success status");
        soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains(userName), "Alert for user :" + userName + "disabled account is shown here");
        soft.assertAll();

    }

    @Test(description = "ALRT-21", groups = {"multiTenancy", "singleTenancy"})
    public void pluginUserLoginFailure() throws Exception {
        SoftAssert soft = new SoftAssert();
        String user = Generator.randomAlphaNumeric(10);
        log.info("Create plugin user");
        rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);
        if (!data.isMultiDomain()) {
            log.info("Setting properties");
            HashMap<String, String> params = new HashMap<>();
            String propName = "domibus.auth.unsecureLoginAllowed";
            String payload = "false";
            params.put("name", propName);
            log.info("Property details before modification" + rest.getDomibusPropertyDetail(params));
            rest.updateDomibusProperty(propName, params, payload);
            log.info("Property details after modification" + rest.getDomibusPropertyDetail(params));
        }

        log.info("Send message using plugin user credentials");
        try {
            messageSender.sendMessage(user, data.getNewTestPass(), null, null);
        } catch (Exception e) {
            log.debug("Authentication exception" + e);
        }

        log.info("Login into application");
        log.info("Navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

        AlertPage page = new AlertPage(driver);
        log.info("Search data using basic filter for plugin_user_login_failure alert type");
        page.filters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);

        log.info("Check if multidomain exists");
        if (data.isMultiDomain()) {
            log.info("Select show domain check box");
            page.filters().showDomainAlert();
        }
        page.grid().waitForRowsToLoad();
        log.info("Validate presence of alert with correct alert type, level ,status and plugin username in parameters");
        soft.assertTrue(page.grid().getRowInfo(0).get("Alert Type").contains("PLUGIN_USER_LOGIN_FAILURE"), "Alert for Plugin user login failure is shown ");
        soft.assertTrue(page.grid().getRowInfo(0).get("Alert Level").contains("LOW"), "Alert level is low ");
        soft.assertTrue(page.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Alert status is success");
        soft.assertTrue(page.grid().getRowInfo(0).get("Parameters").contains(user), "Alert has plugin user name in parameters field");
        soft.assertAll();
    }

    @Test(description = "ALRT-22", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
    public void pluginUserDisabled() throws Exception {
        SoftAssert soft = new SoftAssert();

        String user = Generator.randomAlphaNumeric(10);
        log.info("Create plugin users");
        rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);

        if (!data.isMultiDomain()) {
            log.info("Setting properties");
            HashMap<String, String> params = new HashMap<>();
            String propName = "domibus.auth.unsecureLoginAllowed";
            String payload = "false";
            params.put("name", propName);
            log.info("Property details before modification" + rest.getDomibusPropertyDetail(params));
            rest.updateDomibusProperty(propName, params, payload);
            log.info("Property details after modification" + rest.getDomibusPropertyDetail(params));
        }

        log.info("Send message using plugin user credentials");
        for (int i = 0; i <= 5; i++) {
            try {
                messageSender.sendMessage(user, data.getNewTestPass(), null, null);
            } catch (Exception e) {
                log.debug("Authentication Exception " + e);
            }
        }

        log.info("Login into application");
        log.info("Navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

        AlertPage page = new AlertPage(driver);
        log.info("Search data using basic filter for plugin_user_account_disabled alert type");
        page.filters().basicFilterBy(null, "PLUGIN_USER_ACCOUNT_DISABLED", null, null, null, null);

        log.info("Check if multidomain exists");
        if (data.isMultiDomain()) {
            log.info("Select show domain check box");
            page.filters().showDomainAlert();
        }

        page.grid().waitForRowsToLoad();
        log.info("Validate presence of alert for plugin_user_account_disabled");
        soft.assertTrue(page.grid().getRowInfo(0).get("Alert Type").contains("PLUGIN_USER_ACCOUNT_DISABLED"), "Top row alert is for Plugin user account disabled");
        soft.assertTrue(page.grid().getRowInfo(0).get("Alert Level").contains("HIGH"), "Proper alert level is shown");
        soft.assertTrue(page.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Proper alert status is shown");
        soft.assertTrue(page.grid().getRowInfo(0).get("Parameters").contains(user), "Alert is shown for same user");
        soft.assertAll();
    }


    //This method will verfiy data after clicking show domain alerts checkbox for default domain
    @Test(description = "ALRT-2", groups = {"multiTenancy"})
    public void showDomainAlertChecked() throws Exception {
        SoftAssert soft = new SoftAssert();
        AlertPage page = new AlertPage(driver);
        AlertFilters aFilter = new AlertFilters(driver);
        log.info("Login into application and navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

        log.info("wait for grid row to load");
        page.grid().waitForRowsToLoad();

        log.info("Click on show domain alert checkbox");
        aFilter.getShowDomainCheckbox().click();
        log.info("Click on search button");
        aFilter.getSearchButton().click();
        page.grid().waitForRowsToLoad();

        log.info("Extract total number of records from Alerts page");
        int totalCount = rest.getAllAlerts(page.getDomainFromTitle(), "true").length();
        JSONArray userList = rest.getUsers(page.getDomainFromTitle());
        JSONArray messageList = rest.getListOfMessages(page.getDomainFromTitle());

        if (rest.getAllAlerts(page.getDomainFromTitle(), "true").length() > 10) {
            log.info("Set count as 10, if total count >10");
            totalCount = Math.min(10, page.grid().getPagination().getTotalItems());
        }
        for (int j = 0; j < totalCount; j++) {
            compareParamData(j, userList, messageList,page);
        }
        soft.assertAll();
    }

    //This method will verify Alert page data for second domain with show domain alerts checked
    @Test(description = "ALRT-3", groups = {"multiTenancy"})
    public void showDomainAlertCheckedForSecDomain() throws Exception {
        SoftAssert soft = new SoftAssert();
        AlertPage page = new AlertPage(driver);
        AlertFilters aFilter = new AlertFilters(driver);

        log.info("Login into application and navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

        log.info("Change Domain");
        page.getDomainSelector().selectOptionByIndex(1);

        log.info("wait for grid row to load");
        page.grid().waitForRowsToLoad();
        log.info("Select show domain alert checkbox");
        aFilter.getShowDomainCheckbox().click();
        log.info("Click on search button");
        aFilter.getSearchButton().click();
        page.grid().waitForRowsToLoad();

        log.info("Extract total record from Alerts page");
        int totalCount = rest.getAllAlerts(page.getDomainFromTitle(), "true").length();

        if (rest.getAllAlerts(page.getDomainFromTitle(), "true").length() > 10) {
            log.info("Set grid count=10 if total count>10");
            totalCount = Math.min(10, page.grid().getPagination().getTotalItems());
        }
        JSONArray userList = rest.getUsers(page.getDomainFromTitle());
        JSONArray messageList = rest.getListOfMessages(page.getDomainFromTitle());
        for (int j = 0; j < totalCount; j++) {

            compareParamData(j, userList, messageList,page);

        }
        soft.assertAll();
    }

    //This method will verify double click feature for Alerts page
    @Test(description = "ALRT-4", groups = {"multiTenancy", "singleTenancy"})
    public void doubleClickAlertRow() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Login into application");
        login(data.getAdminUser());
        AlertPage page = new AlertPage(driver);

        log.info("Navigate to Alerts page");
        page.getSidebar().goToPage(PAGES.ALERTS);

        log.info("Wait for grid row to load");
        page.grid().waitForRowsToLoad();
        do {
            if (page.grid().getRowsNo() < 1) {
                throw new SkipException("Not enough rows");
            }

            log.info("double click row 0");
            page.grid().doubleClickRow(0);

            log.info("checking the current selected row");
            soft.assertTrue(page.grid().getSelectedRowIndex() == -1, "Rows are not selectable in Alert page");
            soft.assertTrue(!page.hasOpenDialog(), "No dialog is visible on the page");

            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                log.info("Break from loop if current domain is null or equal to second domain in multitenancy");
                break;
            }
            log.info("Change domain");
            page.getDomainSelector().selectOptionByIndex(1);

            log.info("Wait for grid row to load");
            page.grid().waitForRowsToLoad();

        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));
        soft.assertAll();
    }

    //This method will verify data of Alerts page after changing domains
    @Test(description = "ALRT-9", groups = {"multiTenancy"})
    public void changeDomain() throws Exception {
        SoftAssert soft = new SoftAssert();
        AlertPage page = new AlertPage(driver);
        AlertFilters aFilter = new AlertFilters(driver);
        log.info("Login into application and Navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

        List<String> userName = new ArrayList<>();

        soft.assertTrue(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");
        soft.assertFalse(aFilter.getShowDomainCheckbox().isChecked(), "Check Box is not checked");
        int gridRowCount = Math.min(10, page.grid().getPagination().getTotalItems());

        for (int i = 0; i < gridRowCount; i++) {

            String userNameStr = page.grid().getRowSpecificColumnVal(i, "Parameters").split(",")[0];
            log.info("Extract all user names available in parameters fields ");
            userName.add(userNameStr);
        }

        log.info("Remove all duplicate username");
        List<String> userNameWithoutDuplicates = userName.stream().distinct().collect(Collectors.toList());

        log.info("Navigate to users page");
        page.getSidebar().goToPage(PAGES.USERS);
        UsersPage uPage = new UsersPage(driver);

        for (int j = 0; j < userNameWithoutDuplicates.size(); j++) {
            log.info("Verify role of all users as ROLE_AP_ROLE");
            soft.assertTrue(uPage.grid().getRowInfo("Username", userNameWithoutDuplicates.get(j)).get("Role").equals(DRoles.SUPER));
        }
        log.info("Navigate to Alerts page");
        uPage.getSidebar().goToPage(PAGES.ALERTS);
        page.grid().waitForRowsToLoad();

        log.info("Extract total number of count for default domain");
        int defaultDomainCount = rest.getAllAlerts(page.getDomainFromTitle(), "false").length();

        log.info("Extract all row infos");
        List<HashMap<String, String>> defaultDomainRowsInfo = page.grid().getAllRowInfo();

        log.info("Change domain");
        page.getDomainSelector().selectOptionByIndex(1);
        page.grid().waitForRowsToLoad();

        soft.assertTrue(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");
        soft.assertFalse(aFilter.getShowDomainCheckbox().isChecked(), "Check Box is not checked");

        log.info("Extract total number of count");
        int secDomainCount = rest.getAllAlerts(page.getDomainFromTitle(), "false").length();
        log.info("Extract all row infos");
        List<HashMap<String, String>> secDomainRowsInfo = page.grid().getAllRowInfo();

        log.info("Compare row count and row infos for both domains");
        soft.assertTrue(defaultDomainCount == secDomainCount, "Both domains have same number of data");
        soft.assertTrue(defaultDomainRowsInfo.equals(secDomainRowsInfo), "Both domains have same records");

        soft.assertAll();

    }

    //This method will download csv with/without show domain checkbox checked for all domains
    @Test(description = "ALRT-10", groups = {"multiTenancy", "singleTenancy"})
    public void downloadCsv() throws Exception {
        SoftAssert soft = new SoftAssert();
        AlertPage page = new AlertPage(driver);
        AlertFilters aFilter = new AlertFilters(driver);
        log.info("Login with Super/Admin user and navigate to Alerts page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        do {
            if (data.isMultiDomain()) {
                log.info("Check presence of Show domain alert and checkbox is not checked");
                soft.assertTrue(aFilter.getShowDomainCheckbox().isPresent() && !aFilter.getShowDomainCheckbox().isChecked());
            }

            log.info("Click on download csv button");
            File file = page.downloadCsv();

            log.info("Click on show link");
            page.gridControl().showCtrls();

            log.info("Click on All link to show all available column headers");
            page.gridControl().showAllColumns();

            String completeFilePath = file.getAbsolutePath();

            log.info("Check csv header and grid header");
            page.grid().checkCSVvsGridHeaders(completeFilePath, soft);

            int maxAlert = page.grid().getRowsNo();

            log.info("Check specific row data from grid and csv");
            page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxAlert));
            page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxAlert));

            if (data.isMultiDomain()) {
                log.info("Click show domain alert checkbox");
                aFilter.getShowDomainCheckbox().click();
                aFilter.getSearchButton().click();

                log.info("Wait for grid row to load");
                page.grid().waitForRowsToLoad();

                log.info("Click on download csv button");
                File filee = page.downloadCsv();

                log.info("Click on show link");
                page.gridControl().showCtrls();

                log.info("Click on All link to show all available column headers");
                page.gridControl().showAllColumns();

                String completeFilePathh = filee.getAbsolutePath();


                log.info("Check csv and grid headers");
                page.grid().checkCSVvsGridHeaders(completeFilePathh, soft);

                int maxAlertt = page.grid().getRowsNo();

                log.info("Check csv data and grid row data for specifc row");
                page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxAlertt));
                page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxAlertt));

            }
            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                log.info("Break from loop if current domain in null or equal to second domain");
                break;
            }
            log.info("Check if multidomain is true");
            if (data.isMultiDomain()) {
                log.info("Change domain");
                page.getDomainSelector().selectOptionByIndex(1);
                log.info("Wait for grid row to load");
                page.grid().waitForRowsToLoad();
            }

        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));
        soft.assertAll();
    }

    //This method will verify data for Admin user
    @Test(description = "ALRT-12", groups = {"multiTenancy", "singleTenancy"})
    public void dataForAdminUser() throws Exception {
        SoftAssert soft = new SoftAssert();
        AlertPage page = new AlertPage(driver);
        AlertFilters aFilter = new AlertFilters(driver);

        String user = Generator.randomAlphaNumeric(3);
        rest.createUser(user, DRoles.ADMIN, data.defaultPass(), null);
        log.info("created user " + user);

        log.info("Login and navigate to Alert page");
        login(user, data.defaultPass()).getSidebar().goToPage(PAGES.ALERTS);
        do {

            log.info("Wait for grid row to load");
            page.grid().waitForRowsToLoad();
            log.info("Check absence of Show Domain Alert check box");
            soft.assertFalse(aFilter.getShowDomainCheckbox().isPresent(), "Checkbox is not present for Admin user");

            log.info("If total count >10 ,set count value is 10");
            int totalCount = Math.min(10, page.grid().getPagination().getTotalItems());
            JSONArray userList = rest.getUsers(page.getDomainFromTitle());
            JSONArray messageList = rest.getListOfMessages(page.getDomainFromTitle());
            for (int j = 0; j < totalCount; j++) {
                compareParamData(j, userList, messageList,page);
            }
            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                log.info("Break from loop if current domain is null or second domain in case of multitenancy");
                break;
            }
            if (data.isMultiDomain()) {
                log.info("Check if multidomain is true");

                String userSecDomain = Generator.randomAlphaNumeric(3);
                rest.createUser(userSecDomain, DRoles.ADMIN, data.defaultPass(), rest.getDomainNames().get(1));
                log.info("created user for second domain" + userSecDomain);
                log.info("logout from application");
                logout();
                log.info("Login with user from second domain and navigate to Alerts page");
                login(userSecDomain, data.defaultPass()).getSidebar().goToPage(PAGES.ALERTS);

            }
        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));
        soft.assertAll();


    }

    //This method will verify absence of super admin records and present record belongs to current domain
    @Test(description = "ALRT-13", groups = {"multiTenancy"})
    public void superAdminrecordAbsenceForAdmin() throws Exception {

        SoftAssert soft = new SoftAssert();
        AlertPage page = new AlertPage(driver);
        AlertFilters aFilter = new AlertFilters(driver);

        String user = Generator.randomAlphaNumeric(3);
        rest.createUser(user, DRoles.ADMIN, data.defaultPass(), null);
        log.info("created user " + user);

        log.info("Login with created user and naviagte to Alerts page");
        login(user, data.defaultPass()).getSidebar().goToPage(PAGES.ALERTS);
        do {
            log.info("Extract total number of users");
            int totalUsers = rest.getUsers(page.getDomainFromTitle()).length();

            List<String> userName = new ArrayList<>();

            soft.assertFalse(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");

            log.info("if total alert count >10 then set count as 10");
            int recordCount = Math.min(10, page.grid().getPagination().getTotalItems());

            for (int j = 0; j < recordCount; j++) {
                if (page.grid().getRowInfo(j).containsValue("USER_LOGIN_FAILURE")
                        || page.grid().getRowInfo(j).containsValue("USER_ACCOUNT_DISABLED")) {

                    String userNameStr = page.grid().getRowSpecificColumnVal(j, "Parameters").split(",")[0];
                    log.info("Extract all user names available in parameters fields ");
                    userName.add(userNameStr);
                }
            }
            log.info("Remove all duplicate username");
            List<String> userNameWithoutDuplicates = userName.stream().distinct().collect(Collectors.toList());
            JSONArray userNames = rest.getUsers(page.getDomainFromTitle());
            log.info("Navigate to users page");
            page.getSidebar().goToPage(PAGES.USERS);
            UsersPage uPage = new UsersPage(driver);

            for (int j = 0; j < userNameWithoutDuplicates.size(); j++) {

                soft.assertFalse(uPage.grid().getRowInfo("Username", userName.get(j)).get("Role").equals(DRoles.SUPER));
                for (int k = 0; k < totalUsers; k++) {
                    if (userNames.getJSONObject(k).getString("userName").equals(userNameWithoutDuplicates.get(j))) {
                        log.info("Shown user is from current domain");
                    }
                }
            }

            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                log.info("Break from loop if current domain is null for Single tenancy or equal to second domain");

                break;
            }
            if (data.isMultiDomain()) {
                log.info("Check if multi domain is true");
                String userr = Generator.randomAlphaNumeric(3);

                rest.createUser(userr, DRoles.ADMIN, data.defaultPass(), rest.getDomainNames().get(1));
                log.info("created user for sec domain " + userr);

                log.info("Logout from application");
                logout();
                log.info("Login and navigate to Alerts page");
                login(userr, data.defaultPass()).getSidebar().goToPage(PAGES.ALERTS);
            }
        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));
        soft.assertAll();


    }

    //This method will verify default data in all search filters drop downs
    @Test(description = "ALRT-28", groups = {"multiTenancy", "singleTenancy"})
    public void defaultDataInSearchFilter() throws Exception {

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        AlertPage page = new AlertPage(driver);
        AlertFilters aFilter = new AlertFilters(driver);

        do {
            soft.assertTrue(aFilter.getProcessedSelect().getOptionsTexts().size() > 0, "Drop down in not blank");
            soft.assertTrue(aFilter.getAlertTypeSelect().getOptionsTexts().size() > 0, "Drop down in not blank");
            soft.assertTrue(aFilter.getAlertStatusSelect().getOptionsTexts().size() > 0, "Drop down in not blank");
            soft.assertTrue(aFilter.getAlertLevelSelect().getOptionsTexts().size() > 0, "Drop down in not blank");

            aFilter.verifyDropDownOptions(aFilter.ProcessedContainer, "Processed", soft);
            aFilter.verifyDropDownOptions(aFilter.alertTypeContainer, "Alert Type", soft);
            aFilter.verifyDropDownOptions(aFilter.alertStatusContainer, "Alert Status", soft);
            aFilter.verifyDropDownOptions(aFilter.alertLevelContainer, "Alert Level", soft);

            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                log.info("Break from loop in case domain is null (for single tenancy) or second domain (for multi tenancy ");
                break;
            }
            if (data.isMultiDomain()) {
                log.info("Change domain if it is multitenant");
                page.getDomainSelector().selectOptionByIndex(1);
                page.waitForTitle();
                page.grid().waitForRowsToLoad();
            }
        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));
        soft.assertAll();
    }

    //This method will verify validation applicable for alert id
    @Test(description = "ALRT-29", groups = {"multiTenancy", "singleTenancy"})
    public void checkValidationForAlertId() throws Exception {

        SoftAssert soft = new SoftAssert();
        log.info("Login into application and navigate to Alert page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        AlertPage page = new AlertPage(driver);
        AlertFilters aFilter = new AlertFilters(driver);
        log.info("Click on advance link");
        aFilter.getAdvanceLink().click();
        do {
            log.info("Create list of correct and incorrect data");
            List<String> correctDataArray = Arrays.asList("1234567890123456789", "347362", "1");
            List<String> incorrectDataArray = Arrays.asList("random", "0random", "0000000000000000000", "12345678901234567890", "random1"
                    , "54 656", "$#%", "-989", "+787");

            for (int i = 0; i < correctDataArray.size(); i++) {
                log.info("Pass correct value :" + correctDataArray.get(i));
                aFilter.getAlertId().fill(correctDataArray.get(i));
                log.info("Verify status of search button as enabled");
                soft.assertTrue(aFilter.getSearchButton().isEnabled(), "Button is enabled");
            }
            for (int i = 0; i < incorrectDataArray.size(); i++) {
                log.info("Pass incorrect value :" + incorrectDataArray.get(i));
                aFilter.getAlertId().fill(incorrectDataArray.get(i));
                log.info("Verify presence of validation message under alert id field");
                soft.assertTrue(aFilter.alertIdValidation.isDisplayed());
                log.info("Validation message is : " + aFilter.alertIdValidation.getText());
                log.info("Verify status of Search button as Disabled ");
                soft.assertFalse(aFilter.getSearchButton().isEnabled(), "Button is not enabled");
            }
            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                break;
            }

            if (data.isMultiDomain()) {
                log.info("Change domain if set up is multitenant");
                page.getDomainSelector().selectOptionByIndex(1);
                page.waitForTitle();
            }
        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));
        soft.assertAll();
    }

    //This method will verfiy feature for Processed for Super alerts
    @Test(description = "ALRT-32", groups = {"multiTenancy", "singleTenancy"})
    public void checkProcessed() throws Exception {

        SoftAssert soft = new SoftAssert();
        log.info("Login into application and navigate to Alert page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        AlertPage page = new AlertPage(driver);
        page.waitForTitle();
        page.grid().waitForRowsToLoad();
        AlertFilters aFilter = new AlertFilters(driver);
        do {
            log.info("Check alert count when showDomain alert is false");
            if (rest.getAllAlerts(page.getDomainFromTitle(), "false").length() > 0) {
                int totalCount = rest.getAllAlerts(page.getDomainFromTitle(), "false").length();
                HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
                log.info("Verify disabled status of save and cancel button");
                soft.assertTrue(page.getSaveButton().isEnabled() && page.getCancelButton().isEnabled());
                log.info("Check processed checkbox for first row");
                page.getCssForProcessedCheckBox(0).click();

                log.info("Click on save button and then ok from confirmation pop up");
                page.getSaveButton().click();
                page.confirmationPopup().confirm();
                log.info("Check total count as 1 less than before");
                soft.assertTrue(rest.getAllAlerts(page.getDomainFromTitle(), "false").length() == totalCount - 1);

                log.info("Select processed in search filter ");
                aFilter.getProcessedSelect().selectOptionByIndex(1);
                log.info("Click on search button");
                aFilter.getSearchButton().click();
                page.grid().waitForRowsToLoad();
                List<HashMap<String, String>> allRowInfo = page.grid().getAllRowInfo();
                for (int i = 0; i < allRowInfo.size(); i++) {
                    if (allRowInfo.get(i).equals(rowInfo)) {
                        log.info("Row is present ");
                    }
                }
            } else {
                log.info("There is no record on page to  verify this feature");
            }

            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                break;
            }
            if (data.isMultiDomain()) {
                log.info("Change domain");
                page.getDomainSelector().selectOptionByIndex(1);
                page.grid().waitForRowsToLoad();
            }
        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));
    }

    //This method will verify Processed feature for Domain specific data
    @Test(description = "ALRT-33", groups = {"multiTenancy", "singleTenancy"})
    public void checkProcessedForDomainData() throws Exception {

        SoftAssert soft = new SoftAssert();
        log.info("Login into application and navigate to Alert page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
        AlertPage page = new AlertPage(driver);
        AlertFilters aFilter = new AlertFilters(driver);

        aFilter.getShowDomainCheckbox().click();
        aFilter.getSearchButton().click();

        page.waitForTitle();
        page.grid().waitForRowsToLoad();
        do {
            log.info("Check alert count when showDomain alert is true");
            if (rest.getAllAlerts(page.getDomainFromTitle(), "true").length() > 0) {
                int totalCount = rest.getAllAlerts(page.getDomainFromTitle(), "false").length();
                HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
                log.info("Verify disabled status of save and cancel button");
                soft.assertTrue(page.getSaveButton().isEnabled() && page.getCancelButton().isEnabled());
                log.info("Check processed checkbox for first row");
                page.getCssForProcessedCheckBox(0).click();

                log.info("Click on save button and then ok from confirmation pop up");
                page.getSaveButton().click();
                page.confirmationPopup().confirm();
                log.info("Check total count as 1 less than before");
                soft.assertTrue(rest.getAllAlerts(page.getDomainFromTitle(), "false").length() == totalCount - 1);

                log.info("Select processed in search filter ");
                aFilter.getProcessedSelect().selectOptionByIndex(1);
                log.info("Click on search button");
                aFilter.getSearchButton().click();
                page.grid().waitForRowsToLoad();
                List<HashMap<String, String>> allRowInfo = page.grid().getAllRowInfo();
                for (int i = 0; i < allRowInfo.size(); i++) {
                    if (allRowInfo.get(i).equals(rowInfo)) {
                        log.info("Row is present ");
                    }
                }
            } else {
                log.info("There is no data present to verify this feature");
            }
            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                break;
            }
            if (data.isMultiDomain()) {
                log.info("Change domain");
                page.getDomainSelector().selectOptionByIndex(1);
                page.grid().waitForRowsToLoad();
            }
        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));
    }

    private void compareParamData(int rowNumber, JSONArray userList, JSONArray messageList,AlertPage page) throws Exception {
        if (page.grid().getRowInfo(rowNumber).containsValue("USER_LOGIN_FAILURE") || page.grid().getRowInfo(rowNumber).containsValue("USER_ACCOUNT_DISABLED")) {
            log.info("Check Alert type is USER_LOGIN_FAILURE or USER_ACCOUNT_DISABLED");

            String user = page.grid().getRowSpecificColumnVal(rowNumber, "Parameters").split(",")[0];
            log.info("Extract userName from Parameters ");
            for (int k = 0; k < userList.length(); k++) {
                if (userList.getJSONObject(k).getString("userName").equals(user)) {
                    log.info("Shown user is from current domain");
                }

            }
        } else if (page.grid().getRowInfo(rowNumber).containsValue("MSG_STATUS_CHANGED")) {
            log.info("Check if Alert Type is MSG_STATUS_CHANGED");
            String messageId = page.grid().getRowSpecificColumnVal(rowNumber, "Parameters").split(",")[0];
            log.info("Extract message id from parameters field");

            for (int k = 0; k < messageList.length(); k++) {
                if (messageList.getJSONObject(k).getString("messageId").equals(messageId)) {
                    log.info("Message belongs to current domain");
                }
            }

        }
    }

    public List<String> getCSVSpecificColumnData(String filename, String columnName) throws Exception {

        Reader reader = Files.newBufferedReader(Paths.get(filename));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase()
                .withTrim());
        List<CSVRecord> records = csvParser.getRecords();
        List<String> columnValue= new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            columnValue.add(records.get(i).get(columnName));
        }

        return columnValue;

    }

}







