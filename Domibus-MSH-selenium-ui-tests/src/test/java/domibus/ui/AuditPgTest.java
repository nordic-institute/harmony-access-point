package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.BaseTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Audit.AuditPage;
import pages.login.LoginPage;
import pages.msgFilter.MessageFilterModal;
import pages.msgFilter.MessageFilterPage;
import pages.pmode.*;
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
        if (Apage.grid().getRowsNo() >= 10) {
            log.info("comparing any random row data from downloaded csv and grid");
            Apage.grid().checkCSVvsGridDataForSpecificRow(fileName, soft, Generator.randomNumber(10));
            Apage.grid().checkCSVvsGridDataForSpecificRow(fileName, soft, Generator.randomNumber(10));
        } else {
            log.info("comparing all data from grid row and downloaded csv");
            Apage.grid().checkCSVvsGridInfo(fileName, soft);
        }
        soft.assertAll();
    }
    //This method will verify audit page log for Message Download event

    @Test(description = "AU-14", groups = {"multiTenancy", "singleTenancy"})
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
    //This method will verify audit page log for Message filter creation event

    @Test(description = "AU-15", groups = {"multiTenancy", "singleTenancy"})
    public void msgFilterCreation() throws Exception {
        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.MESSAGE_FILTER);
        DomibusPage page = new DomibusPage(driver);
        String actionName = Generator.randomAlphaNumeric(5);
        log.info("Create one message filter with action field value as :" + actionName);
        rest.createMessageFilter(actionName, null);
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded(), "Page is loaded ");
        log.info("Set all search filters");
        Apage.getFilters().setFilterData("table", "Message filter");
        log.info("Click on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate log presence on Audit page");
        soft.assertTrue(Apage.grid().getRowInfo(0).containsValue("Created"), "Message filter action is logged successfully");
        soft.assertAll();

    }
//This method will verify Audit page log for Message filter update event
    @Test(description = "AU-16", groups = {"multiTenancy", "singleTenancy"})
    public void msgFilterEdit() throws Exception {
        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.MESSAGE_FILTER);
        DomibusPage page = new DomibusPage(driver);
        MessageFilterPage Mpage = new MessageFilterPage(driver);
        String actionName = Generator.randomAlphaNumeric(5);
        if (Mpage.grid().getPagination().getTotalItems() > 0) {
            log.info("Select row with index 0 if total grid count >0");
            Mpage.grid().selectRow(0);
            log.info("Click on edit button");
            Mpage.getEditBtn().click();
            MessageFilterModal modal = new MessageFilterModal(driver);
            log.info("Update action field value");
            modal.getActionInput().fill(actionName);
            log.info("Click on ok ");
            modal.clickOK();
            log.info("saving changes");
            Mpage.saveAndConfirmChanges();
        }
        log.info("Navigate to Audit page");
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded());
        log.info("Set all data in search filters");
        Apage.getFilters().setFilterData("table", "Message filter");
        log.info("Click on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        soft.assertTrue(Apage.grid().getRowInfo(0).containsValue("Modified"), "Correct action is logged");
        soft.assertAll();

    }
//This method will verify audit page log for Message filter Move up/down action
    @Test(description = "AU-17", groups = {"multiTenancy", "singleTenancy"})
    public void msgFilterMoveAction() throws Exception {
        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.MESSAGE_FILTER);
        DomibusPage page = new DomibusPage(driver);
        MessageFilterPage Mpage = new MessageFilterPage(driver);
        if (Mpage.grid().getPagination().getTotalItems() == 1) {
            String actionName = Generator.randomAlphaNumeric(5);
            log.info("Create message filter");
            rest.createMessageFilter(actionName, null);
        }
        log.info("Select last row");
        Mpage.grid().selectRow(Mpage.grid().getPagination().getTotalItems() - 1);
        log.info("Select last row");
        Mpage.getMoveUpBtn().click();
        log.info("Click on save button then yes from confirmation pop up");
        Mpage.saveAndConfirmChanges();
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded());
        log.info("Select data in search filters");
        Apage.getFilters().setFilterData("table", "Message filter");
        log.info("Click on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate logs on Audit page");
        soft.assertTrue(Apage.grid().getRowInfo(0).containsValue("Modified"), "Correct action is logged");
        soft.assertTrue(Apage.grid().getRowInfo(1).containsValue("Modified"), "Correct action is logged");
        soft.assertAll();


    }

//This method will verify audit page log for Message filter deletion event
    @Test(description = "AU-18", groups = {"multiTenancy", "singleTenancy"})
    public void msgFilterDeletion() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application and navigate to Message filter page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.MESSAGE_FILTER);
        DomibusPage page = new DomibusPage(driver);
        String actionName = Generator.randomAlphaNumeric(5);
        log.info("Create one message filter");
        rest.createMessageFilter(actionName, null);
        MessageFilterPage Mpage = new MessageFilterPage(driver);
        log.info("Select last row");
        Mpage.grid().selectRow(Mpage.grid().getPagination().getTotalItems() - 1);
        log.info("Click on delete button");
        Mpage.getDeleteBtn().click();
        log.info("Click save button");
        Mpage.getSaveBtn().click();
        log.info("Click save button then yes button on confirmation pop up");
        Mpage.getConfirmation().confirm();
        log.info("Success message shown :" + page.getAlertArea().getAlertMessage());
        log.info("Navigate to Audit page");
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded());
        log.info("Set all search filter");
        Apage.getFilters().setFilterData("table", "Message filter");
        log.info("Click on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate presence of log on Audit page");
        soft.assertTrue(Apage.grid().getRowInfo(0).containsValue("Deleted"), "Correct action is logged");
        soft.assertAll();
    }

//This method will verify log on Audit page after text modification on current pmode

    @Test(description = "AU-19", groups = {"multiTenancy", "singleTenancy"})
    public void TxtUpdatePmode() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credentials and navigate to Pmode current page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        log.info("Upload pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        PModeCurrentPage Ppage = new PModeCurrentPage(driver);
        log.info("Extract data from current Pmode");
        String beforeEditPmode = Ppage.getTextArea().getText();
        log.info("Modify some text");
        String afterEditPmode = beforeEditPmode.replaceAll("\\t", " ").replaceAll("localhost", "mockhost");
        log.info("Fill pmode current area with updated pmode text");
        Ppage.getTextArea().fill(afterEditPmode);
        log.info("Click on save button");
        Ppage.getSaveBtn().click();
        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        log.info("Enter description");
        modal.getDescriptionTextArea().fill("Valid Modification");
        log.info("Click on ok button");
        modal.clickOK();
        DomibusPage page = new DomibusPage(driver);
        AuditPage Apage = new AuditPage(driver);
        log.info("Navigate to Audit page");
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        log.info("Select Pmode as Table field data");
        Apage.getFilters().setFilterData("table", "Pmode");
        log.info("click on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate data on Audit page");
        soft.assertTrue(Apage.getActionData("Pmode", 0) != null, "Proper action is logged");
        soft.assertTrue(Apage.getActionData("Pmode", 1) != null, "Proper action is logged");
        soft.assertAll();
    }

    //This method will verify log on Audit page after pmode upload action
    @Test(description = "AU-20", groups = {"multiTenancy", "singleTenancy"})
    public void PmodeUpload() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Upload pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        log.info("Login into application with Admin credentials and navigate to Audit page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.AUDIT);
        log.info("Select Pmode as Table field data");
        AuditPage Apage = new AuditPage(driver);
        Apage.getFilters().setFilterData("table", "Pmode");
        log.info("click on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate data on Audit page");
        soft.assertTrue(Apage.getActionData("Pmode", 0) != null, "Proper action is logged");
        soft.assertTrue(Apage.getActionData("Pmode", 1) != null, "Proper action is logged");
        soft.assertAll();
    }

//This method will verfiy audit page log for Create party event on Pmode parties page
    @Test(description = "AU-22", groups = {"multiTenancy", "singleTenancy"})
    public void createParty() throws Exception {
        log.info("Upload pmode");
        rest.uploadPMode("pmodes/multipleParties.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);
        SoftAssert soft = new SoftAssert();
        log.info("login into application and navigate to Pmode parties page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        log.info("Validate new button is enabled");
        soft.assertTrue(Ppage.getNewButton().isEnabled(), "New button is enabled");
        log.info("Click on New button");
        Ppage.getNewButton().click();
        PartyModal modal = new PartyModal(driver);
        log.info("Fill new party info");
        modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");
        log.info("Click ok button");
        modal.clickOK();
        Ppage.wait.forXMillis(1000);
        Ppage.getSaveButton().click();
        Ppage.wait.forXMillis(5000);
        log.info("validate presence of success message");
        soft.assertTrue(!Ppage.getAlertArea().isError(), "page shows success message");
        DomibusPage page = new DomibusPage(driver);
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        log.info("Set all search filter data");
        Apage.getFilters().setFilterData("table", "Pmode");
        log.info("Click in search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate data on Audit page");
        soft.assertTrue(Apage.getActionData("Pmode", 0) != null, "Proper action is logged");
        soft.assertTrue(Apage.getActionData("Pmode", 1) != null, "Proper action is logged");
        soft.assertAll();
    }

//This method will verify Audit page log for Edit party event on Pmode archive page
    @Test(description = "AU-23", groups = {"multiTenancy", "singleTenancy"})
    public void editParty() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);
        SoftAssert soft = new SoftAssert();
        log.info("Login and navigate to pmode parties page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        log.info("select row 0");
        Ppage.grid().selectRow(0);
        log.info("Click edit button");
        Ppage.getEditButton().click();
        PartyModal modal = new PartyModal(driver);
        log.info("Fill new party info");
        modal.getNameInput().fill(newPatyName);
        log.info("Fill endpint value");
        modal.getEndpointInput().fill("http://" + newPatyName.toLowerCase() + ".com");
        log.info("Click ok button");
        modal.clickOK();
        Ppage.wait.forXMillis(1000);
        Ppage.getSaveButton().click();
        Ppage.wait.forXMillis(5000);
        log.info("Validate presence of success message");
        DomibusPage page = new DomibusPage(driver);
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        log.info("Set all search filter data");
        Apage.getFilters().setFilterData("table", "Pmode");
        log.info("Click on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate data on Audit page");
        soft.assertTrue(Apage.getActionData("Pmode", 0) != null, "Proper action is logged");
        soft.assertTrue(Apage.getActionData("Pmode", 1) != null, "Proper action is logged");
        soft.assertAll();
    }
//This method will verfiy audit page log for Delete party event on Pmode Parties page
    @Test(description = "AU-24", groups = {"multiTenancy", "singleTenancy"})
    public void deleteParty() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);
        SoftAssert soft = new SoftAssert();
        log.info("Login and navigate to pmode parties page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        PModeCurrentPage PCpage = new PModeCurrentPage(driver);
        String defaultPmode = PCpage.getTextArea().getText();
        Boolean isPresent = defaultPmode.contains("party=\"blue_gw\"");
        log.info("System party name is blue_gw: " + isPresent);
        DomibusPage page = new DomibusPage(driver);
        page.getSidebar().gGoToPage(PAGES.PMODE_PARTIES);
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        int rowCount = Ppage.grid().getPagination().getTotalItems();
        log.info("Total row count" + rowCount);
        for (int i = 0; i < Ppage.grid().getPagination().getTotalItems(); i++) {
            if (Ppage.grid().getRowInfo(i).get("Party Name").equals("red_gw")) {
                Ppage.grid().selectRow(i);
                Ppage.getDeleteButton().click();
                Ppage.getSaveButton().click();
                log.info("Success message shown : " + Ppage.getAlertArea().getAlertMessage());
            }
        }
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        log.info("Set all search filter data");
        Apage.getFilters().setFilterData("table", "Pmode");
        log.info("Click on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate data on Audit page");
        soft.assertTrue(Apage.grid().getRowInfo(0).containsValue("Deleted"), "Proper action is logged");
        soft.assertAll();

    }

    //This method will verify log on Audit page after Pmode download action on Pmode archive page
    @Test(description = "AU-25", groups = {"multiTenancy", "singleTenancy"})
    public void PmodeDownload() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application and navigate to Pmode archive page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_ARCHIVE);
        DomibusPage page = new DomibusPage(driver);
        PModeArchivePage PApage = new PModeArchivePage(driver);
        log.info("Download pmode corresponding to current pmode row");
        rest.downloadGrid(PApage.getRestServicePath(), null, null);
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        Apage.getFilters().setFilterData("table", "Pmode");
        log.info("click on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate data on Audit page");
        Boolean result = Apage.grid().getRowInfo(0).containsValue("Downloaded") && Apage.grid().getRowInfo(0).containsValue("Pmode");
        System.out.println(result);
        soft.assertTrue(result, "Event is logged on audit page");
        soft.assertAll();

    }
//This method will verify audit page log for Restore pmode event on Pmode archive page
    @Test(description = "AU-26", groups = {"multiTenancy", "singleTenancy"})
    public void restorePmodeFromArchive() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);
        SoftAssert soft = new SoftAssert();
        log.info("Login and navigate to pmode parties page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage PApage = new PModeArchivePage(driver);
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        DomibusPage page = new DomibusPage(driver);
        if (PApage.grid().getRowsNo() == 1) {
            log.info("Upload pmode if grid row count is 1");
            rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        }
        log.info("Select row with index 1");
        PApage.grid().selectRow(1);
        log.info("Click on restore button");
        PApage.getRestoreButton().click();
        log.info("Click on save and then yes button on confirmation pop up");
        PApage.getConfirmation().confirm();
        log.info("Success message shown : " + Ppage.getAlertArea().getAlertMessage());
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        Apage.waitForTitle();
        log.info("Set all search filters");
        Apage.getFilters().setFilterData("table", "Pmode");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("click on search button");
        log.info("Validate log presence on Audit page");
        soft.assertTrue(Apage.getActionData("Pmode", 0) != null, "Proper action is logged");
        soft.assertTrue(Apage.getActionData("Pmode", 1) != null, "Proper action is logged");
        soft.assertAll();
    }

//This method will verify audit page log for Delete old pmode event on Pmode archive page
    @Test(description = "AU-27", groups = {"multiTenancy", "singleTenancy"})
    public void deletePmodeFromArchive() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);
        SoftAssert soft = new SoftAssert();
        log.info("Login and navigate to pmode parties page");
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage PApage = new PModeArchivePage(driver);
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        DomibusPage page = new DomibusPage(driver);
        if (PApage.grid().getRowsNo() == 1) {
            log.info("Upload pmode");
            rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        }
        log.info("Select row with index 1");
        PApage.grid().selectRow(1);
        log.info("Click on delete button");
        PApage.getDeleteButton().click();
        log.info("clcik on save button");
        PApage.getSaveButton().click();
        log.info("Click on yes button on confirmation pop up");
        PApage.getConfirmation().confirm();
        log.info("Success message shown : " + Ppage.getAlertArea().getAlertMessage());
        page.getSidebar().gGoToPage(PAGES.AUDIT);
        AuditPage Apage = new AuditPage(driver);
        soft.assertTrue(Apage.isLoaded(), "Page is loaded successfully");
        log.info("Set all searach filters");
        Apage.getFilters().setFilterData("table", "Pmode Archive");
        log.info("Clcik on search button");
        Apage.getFilters().getSearchButton().click();
        Apage.grid().waitForRowsToLoad();
        log.info("Validate data on Audit page");
        soft.assertTrue(Apage.grid().getRowInfo(0).containsValue("Deleted"), "Proper action is logged");
        soft.assertAll();
    }
     /*
    This method will check log on User creation
     */

    @Test(description = "AU-28", groups = {"multiTenancy", "singleTenancy"})
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
        Boolean result1 = Apage.grid().getRowInfo(0).containsValue("Created");
        Boolean result = Apage.grid().getRowInfo(0).containsValue("User");
        System.out.println(result);
        System.out.println(result1);
        //soft.assertTrue(result, "Top row has Table value as User & Action as created ");
        soft.assertAll();
    }

    /*
   This method will verify log on Edit event for User
     */
    @Test(description = "AU-29", groups = {"multiTenancy", "singleTenancy"})
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

    @Test(description = "AU-30", groups = {"multiTenancy", "singleTenancy"})
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
    @Test(description = "AU-39", groups = {"multiTenancy", "singleTenancy"})
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

    @Test(description = "AU-40", groups = {"multiTenancy", "singleTenancy"})
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









