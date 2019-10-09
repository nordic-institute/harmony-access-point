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
        log.info("Search using basic filter");
        int prevCount=Apage.grid().getPagination().getTotalItems();
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
        soft.assertTrue(Apage.grid().getPagination().getTotalItems() ==prevCount, "All search result exist");
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


}











