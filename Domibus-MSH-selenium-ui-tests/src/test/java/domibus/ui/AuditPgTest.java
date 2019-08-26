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

import static org.reflections.Reflections.log;


public class AuditPgTest extends BaseTest {

    /*
    This method verify navigation to Audit page and its elements status
     */
    @Test(description = "AP-1", groups = {"multiTenancy", "singleTenancy"})
    public void OpenAuditPage() throws Exception {
        SoftAssert soft = new SoftAssert();
        LoginPage loginPage = new LoginPage(driver);
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page and its elements");
        soft.assertTrue(Apage.isLoaded(), "Audit Page is loaded successfully");
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
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        log.info("Total number of records in grid are: "+ Apage.getFilters().getPagination().getTotalItems());
        int totalCount = Apage.getFilters().getPagination().getTotalItems();
        System.out.println(totalCount);
        log.info("Select data pmode in Table input field  as basic filter");
        Apage.getFilters().setFilterData("table", "PluginUser");
        log.info("Select Admin in User input field as basic filter");
        Apage.getFilters().setFilterData("user", "admin");
        log.info("Click on Search button");
        Apage.getFilters().getSearchButton().click();
        log.info("Search result count:"+Apage.getFilters().getPagination().getTotalItems() );
        soft.assertTrue(Apage.getFilters().getPagination().getTotalItems()>0, "Search has some data");
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
        DomibusPage page = new DomibusPage(driver);

        AuditPage Apage = new AuditPage(driver);
        log.info("Validate Audit page");
        soft.assertTrue(Apage.isLoaded(),"page is loaded successfully");
        log.info("Download all grid record csv");
        rest.downloadGrid(RestServicePaths.AUDIT_CSV, null, null);
        soft.assertAll();
    }



}



