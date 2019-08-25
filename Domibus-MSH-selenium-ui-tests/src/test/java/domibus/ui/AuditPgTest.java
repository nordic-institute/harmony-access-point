package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Audit.AuditPage;

import pages.login.LoginPage;
import rest.RestServicePaths;
import utils.Generator;



public class AuditPgTest extends BaseTest {
    /*
    This method verify navigation to Audit page and its elements status
     */
    @Test(description = "AP-1", groups = {"multiTenancy", "singleTenancy"})
    public void OpenAuditPage() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        soft.assertTrue(loginPage.isLoaded());
        log.info("Login into application with Admin user and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page and its elements");
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        soft.assertAll();
    }

    /*
    This method will search data on the basis of basic filters
     */
    @Test(description = "AP-2", groups = {"multiTenancy", "singleTenancy"})
    public void SearchBasicFilters() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        log.info("Validate login page");
        soft.assertTrue(loginPage.isLoaded(),"page is loaded successfully");
        log.info("Navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("generate random username for plugin user");
        String user = Generator.randomAlphaNumeric(10);
        log.info("Crete Plugin user with rest service");
        rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(), null);
        log.info("Upload pmode");
        rest.uploadPMode("pmodes/pmode-blue.xml", null);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        log.info("Total number of records in grid are: "+ Apage.getFilters().getPagination().getTotalItems());
        int totalCount = Apage.getFilters().getPagination().getTotalItems();
        log.info("Select data pmode in Table input field  as basic filter");
        Apage.getFilters().setFilterData("table", "Pmode");
        log.info("Select Admin in User input field as basic filter");
        Apage.getFilters().setFilterData("user", "admin");
        log.info("Click on Search button");
        Apage.getFilters().getSearchButton().click();
        log.info("Validate record on search is not greater than total records");
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems() <= totalCount, "Number of records are not greater than total records");
        soft.assertAll();
    }

    /*
    This method will open Advance filters and verify all filter presence
     */
    @Test(description = "AP-3", groups = {"multiTenancy", "singleTenancy"})
    public void ClickAdvanceFilterFilters() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Click on Advance search filters");
        Apage.getFilters().getAdvancedSearchExpandLnk().click();
        log.info("Validate all advance filters");
        soft.assertTrue(Apage.getFilters().advanceFiltersLoaded(), "Advanced filters ");
        soft.assertAll();
    }


    /*
    This method will download csv with all grid data
     */

    @Test(description = "AP-4", groups = {"multiTenancy", "singleTenancy"})
    public void DownloadCSV() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(),"page is loaded successfully");
        log.info("Download all grid record csv");
        rest.downloadGrid(RestServicePaths.AUDIT_CSV, null, null);
        soft.assertAll();
    }
    /*
    This method will check audit page record after move operation on JMS monitoring page
     */

    @Test(description = "AP-5", groups = {"multiTenancy", "singleTenancy"})
    public void JmsMove() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        DomibusPage page = new DomibusPage(driver);
        JMSMessPgTest jTest = new JMSMessPgTest();
        log.info("Calling test method corresponding to Move operation from JMSMessagePgTest class");
        jTest.moveMessage();
        log.info("Naviagte to Audit page");
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Set Jms message as input for Table filter");
        Apage.getFilters().setFilterData("Table", "Jms message");
        Apage.getFilters().setFilterData("Action", "Moved");
        log.info("Click on Search button");
        Apage.getFilters().getSearchButton().click();
        log.info("Audit page record details after jms move:" + Apage.grid().getRowInfo(0));
        soft.assertTrue(Apage.getPagination().getTotalItems()>0,"Search result is greater than 0");
        log.info("Search result has grid data >0");
        soft.assertAll();
    }
    /*
    This method will check record of audit pagewith action 'Deleted' after Delete operation on Jms Monitoring page
     */

    @Test(description = "AP-6", groups = {"multiTenancy", "singleTenancy"})
    public void JmsDel() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        DomibusPage page = new DomibusPage(driver);
        JMSMessPgTest jTest = new JMSMessPgTest();
        log.info("Calling test method corresponding to Delete operation from JMSMessagePgTest class");
        jTest.deleteJMSMessage();
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        Apage.getFilters().setFilterData("Table", "Jms message");
        Apage.getFilters().setFilterData("Action", "Deleted");
        Apage.getFilters().getSearchButton().click();
        log.info("Audit page record details after jms msg delete:" + Apage.grid().getRowInfo(0));
        soft.assertTrue(Apage.getPagination().getTotalItems()>0,"Search result is greater than 0");
        log.info("Search result has grid data >0");
        soft.assertAll();
    }
    /*
    This method will ensure record presence with action'Resent' after message resend operation on Message page
     */

    @Test(description = "AP-7", groups = {"multiTenancy", "singleTenancy"})
    public void MsgResend() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        DomibusPage page = new DomibusPage(driver);
        MessagesPgTest Mpage = new MessagesPgTest();
        log.info("Calling ResendMessage method from MessagePgTest class");
        Mpage.resendMessage();
        log.info("Navigate to Audit page");
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Select Resent as value in Action filter ");
        Apage.getFilters().setFilterData("Action", "Resent");
        log.info("Click on search button");
        Apage.getFilters().getSearchButton().click();
        log.info("Audit page record details after resend message action:" + Apage.grid().getRowInfo(0));
        soft.assertTrue(Apage.getPagination().getTotalItems()>0,"Search result is greater than 0");
        log.info("Search result has grid data >0");
        soft.assertAll();
    }


    @Test(description = "AP-8", groups = {"multiTenancy", "singleTenancy"})
    public void AddMsgFilter() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        DomibusPage page = new DomibusPage(driver);
        MessageFilterPgTest MFpage = new MessageFilterPgTest();
        log.info("Login into application with Admin credentials and navigate to Message filter page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.MESSAGE_FILTER);
        log.info("Calling test method corresponding to New Filter save operation from MessageFilterPgTest class");
        MFpage.newFilterSave();
        log.info("Refresh page");
        page.refreshPage();
        log.info("Navigate to Audit page");
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(),"page is loaded successfully");
        log.info("Select Create as Action filter value");
        Apage.getFilters().setFilterData("Action", "Created");
        log.info("Click on search button");
        Apage.getFilters().getSearchButton().click();
        log.info("Audit page record details after create message filter action:" + Apage.grid().getRowInfo(0));
        soft.assertTrue(Apage.grid().getRowInfo(0).containsValue("Created"), "Action is logged as Create event");
         soft.assertAll();
    }

    @Test(description = "AP-9", groups = {"multiTenancy", "singleTenancy"})
    public void EditMsgFilter() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        DomibusPage page = new DomibusPage(driver);
        MessageFilterPgTest MFpage = new MessageFilterPgTest();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.MESSAGE_FILTER);
        MFpage.editAndSave();
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Set value as Message filter in table input filter");
        Apage.getFilters().setFilterData("Table", "Message filter");
        log.info("Set value as Modified in Action input filter");
        Apage.getFilters().setFilterData("Action", "Modified");
        log.info("Click on Search button");
        Apage.getFilters().getSearchButton().click();
        log.info("Audit page record details after Edit message filter action:" + Apage.grid().getRowInfo(0));
        soft.assertTrue(Apage.getPagination().getTotalItems()>0,"Search result is greater than 0");
        log.info("Search result has grid data >0");
        soft.assertAll();
    }


}



