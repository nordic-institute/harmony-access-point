package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.BaseTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Audit.AuditPage;
import pages.login.LoginPage;
import rest.RestServicePaths;
import utils.Generator;

import java.util.HashMap;



public class AuditPgTest extends BaseTest {

    /*
    This method verify navigation to Audit page and its elements status
     */
    @Test(description = "AU-1", groups = {"multiTenancy", "singleTenancy"})
    public void OpenAuditPage() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        DomibusPage page = new DomibusPage(driver);
        log.info("Login with Admin credential and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page and its elements");
        soft.assertTrue(Apage.isLoaded(), "Audit Page is loaded successfully");
        soft.assertAll();
    }
     /*
    This method confirms no row selection on double click event.
     */

    @Test(description = "AU-2", groups = {"multiTenancy", "singleTenancy"})
    public void DoubleClick() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        soft.assertTrue(!Apage.isGridEmpty(), "Grid has some data");
        Apage.grid().doubleClickRow(1);
        soft.assertTrue(Apage.isRowSelected(1), "Row is not selected on double click");
        soft.assertAll();
    }

    /*
    This method will search data on the basis of basic filters
     */
    @Test(description = "AU-3", groups = {"multiTenancy", "singleTenancy"})
    public void SearchBasicFilters() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("Login with Admin user and Navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Create Plugin user with rest service");
        rest.createPluginUser(Generator.randomAlphaNumeric(10), DRoles.ADMIN, data.getDefaultTestPass(), null);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        log.info("Total number of records in grid are: " + Apage.getFilters().getPagination().getTotalItems());
        log.info("Select data PluginUser in Table input field  as basic filter");
        Apage.getFilters().setFilterData("table", "PluginUser");
        log.info("Select Admin in User input field as basic filter");
        Apage.getFilters().setFilterData("Action", "Created");
        log.info("Click on Search button");
        Apage.getFilters().getSearchButton().click();
        log.info("Search result count:" + Apage.getFilters().getPagination().getTotalItems());
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems() > 0, "Search has some data");
        Boolean result = Apage.grid().getRowInfo(0).containsValue("PluginUser")
                && Apage.grid().getRowInfo(0).containsValue("Created");
        soft.assertTrue(result, "Top row has Table value as PluginUser, User value as Admin & Action as Created ");
        soft.assertAll();
    }

    /*
    This method will open Advance filters and verify all filter presence
     */
    @Test(description = "AU-4", groups = {"multiTenancy", "singleTenancy"})
    public void ClickAdvanceFilterFilters() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        Apage.waitForTitle();
        log.info("Click on Advance search filters");
        Apage.getFilters().getAdvancedSearchExpandLnk().click();
        log.info("Validate all advance filters");
        soft.assertTrue(Apage.getFilters().advanceFiltersLoaded(), "Advanced filters ");
        soft.assertAll();
    }

    /*
    This method will filter events with no results
     */
    @Test(description = "AU-6", groups = {"multiTenancy", "singleTenancy"})
    public void SearchWithNoData() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("Validate login page");
        soft.assertTrue(loginPage.isLoaded(), "page is loaded successfully");
        log.info("Generate Random string for Username");
        String user = Generator.randomAlphaNumeric(10);
        log.info("Create user with rest service");
        rest.createUser(user, DRoles.ADMIN, data.getDefaultTestPass(), null);
        log.info("Login with admin user");
        loginPage.login(data.getAdminUser());
        log.info("Navigate to Audit page");
        loginPage.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        Apage.getFilters().setFilterData("table", "User");
        log.info("Select logged in user username in User input filter");
        Apage.getFilters().setFilterData("user", user);
        log.info("Click on Search button");
        Apage.getFilters().getSearchButton().click();
        log.info("Search result count:" + Apage.getFilters().getPagination().getTotalItems());
        log.info("Validate no data presence for this user on audit page");
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems() == 0, "Search has no data");
        soft.assertAll();
    }

    /*
    This method will confirm presence of all grid data after deletion of all selected filter values
     */
    @Test(description = "AU-7", groups = {"multiTenancy", "singleTenancy"})
    public void DeleteSearchCriteria() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser());
        String user = Generator.randomAlphaNumeric(10);
        log.info("Create user with rest service");
        rest.createUser(user, DRoles.ADMIN, data.getDefaultTestPass(), null);
        DomibusPage page = new DomibusPage(driver);
        log.info("Navigate to Audit page");
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        int prevCount = Apage.grid().getPagination().getTotalItems();
        log.info("Set Table filter data as User");
        Apage.getFilters().setFilterData("table", "User");
        log.info("Set User filter data as created user");
        Apage.getFilters().setFilterData("user", user);
        log.info("Click on search button");
        Apage.getFilters().getSearchButton().click();
        log.info("Total search record is :" + Apage.grid().getPagination().getTotalItems());
        Apage.refreshPage();
        Apage.wait.forElementToBeVisible(Apage.auditPageHeader);
        soft.assertTrue(Apage.grid().getPagination().getTotalItems() == prevCount, "Page shows all records after deletion of all selected filter values");
        soft.assertAll();
    }
    /*
    This method will download csv with all grid data
     */

    @Test(description = "AU-9", groups = {"multiTenancy", "singleTenancy"})
    public void DownloadCSV() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        DomibusPage page = new DomibusPage(driver);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(), "page is loaded successfully");
        log.info("Download all grid record csv");
        String fileName = rest.downloadGrid(RestServicePaths.AUDIT_CSV, null, null);
        log.info("downloaded audit logs to file :" + fileName);
        System.out.println(Apage.grid().getRowsNo());
        if(Apage.grid().getRowsNo()>=10){
            log.info("comparing any random row data from downloaded csv and grid");
            Apage.grid().checkCSVvsGridDataForSpecificRow(fileName,soft,Generator.randomNumber(10));
            Apage.grid().checkCSVvsGridDataForSpecificRow(fileName,soft,Generator.randomNumber(10));
        }
       else{
           log.info("comparing all data from grid row and downloaded csv");
           Apage.grid().checkCSVvsGridInfo(fileName,soft);
        }
        soft.assertAll();
    }

    @Test(description = "AU-16", groups = {"multiTenancy", "singleTenancy"})
    public void MessageDownloadedLog() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        String user = Generator.randomAlphaNumeric(10);
        log.info("Create Plugin user with rest service");
        rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(), null);
        log.info("Upload pmode");
        rest.uploadPMode("pmodes/pmode-blue.xml", null);
        log.info("Send message");
        String messID = messageSender.sendMessage(user, data.getDefaultTestPass(), null, null);
        log.info("Download message");
        rest.downloadMessage(messID, null);
        log.info("Navigate to Audit page");
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Set Table data as Message");
        Apage.getFilters().setFilterData("table", "Message");
        log.info("Select Created as Action Field data");
        Apage.getFilters().setFilterData("Action", "Downloaded");
        Apage.getFilters().getSearchButton().click();
        log.info("Validate non zero Search result count ");
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
        log.info("Validate top record Action as Deleted");
        Boolean result = Apage.grid().getRowInfo(0).containsValue("Message")
                && Apage.grid().getRowInfo(0).containsValue("Downloaded")
                && Apage.grid().getRowInfo(0).containsValue(messID);
        soft.assertTrue(result, "Top row has Table value as Message, User value as Admin & Action as Downloaded ");
        soft.assertAll();
    }
    /*
    This method will check log on User creation
     */

    @Test(description = "AU-29", groups = {"multiTenancy", "singleTenancy"})
    public void CreateUserLog() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        log.info("Create user with rest call");
        String username = Generator.randomAlphaNumeric(10);
        rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
        DomibusPage page = new DomibusPage(driver);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(), "page is loaded successfully");
        log.info("Select User in Table input filter");
        Apage.getFilters().setFilterData("table", "User");
        log.info("Select Created as Action in filter");
        Apage.getFilters().setFilterData("Action", "Created");
        Apage.getFilters().getSearchButton().click();
        log.info("Validate non zero Search result count ");
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
        log.info("Validate top record Action as Created");
        Boolean result = Apage.grid().getRowInfo(0).containsValue("User")
                && Apage.grid().getRowInfo(0).containsValue("Created");
        soft.assertTrue(result, "Top row has Table value as User & Action as created ");
        soft.assertAll();
    }

    /*
   This method will verify log on Edit event for User
     */
    @Test(description = "AU-30", groups = {"multiTenancy", "singleTenancy"})
    public void EditUserLog() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        log.info("Create user with rest call");
        String username = Generator.randomAlphaNumeric(10);
        System.out.println(username);
        rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
        HashMap<String, String> params = new HashMap<>();
        params.put("password", data.getNewTestPass());
        rest.updateUser(username, params);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(), "page is loaded successfully");
        log.info("Select User in Table input filter");
        Apage.getFilters().setFilterData("table", "User");
        log.info("Select Created as Action in filter");
        Apage.getFilters().setFilterData("Action", "Modified");
        Apage.getFilters().getSearchButton().click();
        log.info("Validate non zero Search result count ");
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
        log.info("Validate top record Action as Modified");
        Boolean result = Apage.grid().getRowInfo(0).containsValue("User")
                && Apage.grid().getRowInfo(0).containsValue("Modified");
        soft.assertTrue(result, "Top row has Table value as User, User value as Admin & Action as Modified ");
        soft.assertAll();
    }

/*
This method will verify log for User on delete event
 */

    @Test(description = "AU-31", groups = {"multiTenancy", "singleTenancy"})
    public void DeleteUserLog() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        log.info("Create user with rest call");
        String username = Generator.randomAlphaNumeric(10);
        rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
        rest.deleteUser(username, null);
        DomibusPage page = new DomibusPage(driver);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(), "page is loaded successfully");
        log.info("Select User in Table input filter");
        Apage.getFilters().setFilterData("table", "User");
        log.info("Select Created as Action in filter");
        Apage.getFilters().setFilterData("Action", "Modified");
        Apage.getFilters().getSearchButton().click();
        log.info("Validate non zero Search result count ");
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
        log.info("Validate top record Action as Modified");
        Boolean result = Apage.grid().getRowInfo(0).containsValue("User")
                && Apage.grid().getRowInfo(0).containsValue("Modified");
        soft.assertTrue(result, "Top row has Table value as User, User value as Admin & Action as Modified ");
        soft.assertAll();
    }


    /*
    This method will check log on plugin user creation
     */
    @Test(description = "AU-40", groups = {"multiTenancy", "singleTenancy"})
    public void CreatePluginUserLog() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        log.info("Create user with rest call");
        String username = Generator.randomAlphaNumeric(10);
        rest.createPluginUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
        DomibusPage page = new DomibusPage(driver);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(), "page is loaded successfully");
        log.info("Select PluginUser as Table field data");
        Apage.getFilters().setFilterData("table", "PluginUser");
        log.info("Select Created as Action Field data");
        Apage.getFilters().setFilterData("Action", "Created");
        Apage.getFilters().getSearchButton().click();
        log.info("Validate non zero Search result count ");
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
        log.info("Validate top record Action as Created");
        Boolean result = Apage.grid().getRowInfo(0).containsValue("PluginUser")
                && Apage.grid().getRowInfo(0).containsValue("Created");
        soft.assertTrue(result, "Top row has Table value as PluginUser, User value as Admin & Action as created ");
        soft.assertAll();
    }
    /*
    This methods verifies log on Audit page on plugin user deletion event
     */

    @Test(description = "AU-41", groups = {"multiTenancy", "singleTenancy"})
    public void DeletePluginUserLog() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        log.info("Create user with rest call");
        String username = Generator.randomAlphaNumeric(10);
        System.out.println(username);
        rest.createPluginUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);
        rest.deletePluginUser(username, null);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(), "page is loaded successfully");
        log.info("Select PluginUser as Table field data");
        Apage.getFilters().setFilterData("table", "PluginUser");
        log.info("Select Created as Action Field data");
        Apage.getFilters().setFilterData("Action", "Deleted");
        Apage.getFilters().getSearchButton().click();
        log.info("Validate non zero Search result count ");
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems() > 0, "Search has records");
        log.info("Validate top record Action as Deleted");
        Boolean result = Apage.grid().getRowInfo(0).containsValue("PluginUser")
                && Apage.grid().getRowInfo(0).containsValue("Deleted");
        soft.assertTrue(result, "Top row has Table value as PluginUser, User value as Admin & Action as Deleted ");
        soft.assertAll();
    }
}







