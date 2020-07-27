package domibus.ui.ux;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.PAGES;
import org.apache.commons.io.FileUtils;
import utils.BaseUXTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.parties.PModePartiesPage;
import utils.DFileUtils;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class PmodePartiesPgUXTest extends BaseUXTest {

    @Test(description = "PMP-1", groups = {"multiTenancy", "singleTenancy"})
    public void openPModePartiesPage() throws Exception {

        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

        SoftAssert soft = new SoftAssert();
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        Ppage.getSidebar().goToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        soft.assertTrue(page.filters().getNameInput().isEnabled(), "Page contains filter for party name");
        soft.assertTrue(page.filters().getEndpointInput().isEnabled(), "Page contains filter for party endpoint");
        soft.assertTrue(page.filters().getPartyIDInput().isEnabled(), "Page contains filter for party id");
        soft.assertTrue(page.filters().getProcessRoleSelect().isDisplayed(), "Page contains filter for process role");

        List<HashMap<String, String>> partyInfo = page.grid().getAllRowInfo();
        soft.assertTrue(partyInfo.size() == 2, "Grid contains both the parties described in PMode file");

        soft.assertTrue(!page.getCancelButton().isEnabled(), "Cancel button is NOT enabled");
        soft.assertTrue(!page.getSaveButton().isEnabled(), "Save button is NOT enabled");
        soft.assertTrue(!page.getEditButton().isEnabled(), "Edit button is NOT enabled");
        soft.assertTrue(!page.getDeleteButton().isEnabled(), "Delete button is NOT enabled");
        soft.assertTrue(page.getNewButton().isEnabled(), "New button is enabled");

        soft.assertAll();

    }

    @Test(description = "PMP-1.1", groups = {"multiTenancy", "singleTenancy"})
    public void selectRow() throws Exception {

        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

        SoftAssert soft = new SoftAssert();
        DomibusPage dPage = new DomibusPage(driver);
        dPage.getSidebar().goToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        soft.assertTrue(!page.getEditButton().isEnabled(), "Edit button is not enabled");
        soft.assertTrue(!page.getDeleteButton().isEnabled(), "Delete button is not enabled");

        page.grid().selectRow(0);

        soft.assertTrue(page.getEditButton().isEnabled(), "Edit button is enabled after select row");
        soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled after select row");

        soft.assertAll();

    }


    @Test(description = "PMP-3", groups = {"multiTenancy", "singleTenancy"},enabled = false)
    public void downloadParties() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Navigate to Pmode parties page");
        new DomibusPage(driver).getSidebar().goToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        String partyName = page.grid().getRowInfo(0).get("Party Name");

        log.info("Search for party with id " + partyName);
        page.getNameField().fill(partyName);
        log.info("Click on search button");
        page.getSearchButton().click();

        log.info("Customized location for download");
        String filePath = System.getProperty("user.dir")+File.separator +"downloadFiles";

        log.info("Clean given directory");
        FileUtils.cleanDirectory(new File(filePath));

        log.info("Click on download csv button");
        page.grid().waitForRowsToLoad();
        page.clickDownloadCsvButton(page.getDownloadCsvButton().element);

        log.info("Wait for download to complete");
        page.wait.forXMillis(1000);

        log.info("Check if file is downloaded at given location");
        soft.assertTrue(DFileUtils.isFileDownloaded(filePath), "File is downloaded successfully");

        log.info("Verify file extension as csv");
        soft.assertTrue(DFileUtils.getFileExtension(filePath).equals("csv"), "File extension is csv");
        log.info("Retrieve grid row count from Pmode parties page : " + page.grid().getPagination().getTotalItems());
        int gridRowCount = page.grid().getPagination().getTotalItems();

        String completeFilePath = filePath + File.separator + DFileUtils.getCompleteFileName(filePath);
        log.info("Retrieve row count from downloaded file along with header row");
        int csvFileRowCount = DFileUtils.getRowCount(completeFilePath);

        log.info("Compare grid row from Pmode parties page and downloaded file grid row");
        soft.assertTrue(csvFileRowCount - 1 == gridRowCount, "Grid row Count matches with csvFileRow Count excluding headers");
        page.grid().checkCSVvsGridInfo(completeFilePath, soft);

        soft.assertAll();
    }

    /* column Process from CSV doesn't match Process (I=Initiator, R=Responder, IR=Both) in grid */
    @Test(description = "PMP-16", groups = {"multiTenancy", "singleTenancy"},enabled =false)
    public void downloadAllList() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Navigate to Pmode parties page");
        new DomibusPage(driver).getSidebar().goToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);
        page.grid().waitForRowsToLoad();

        log.info("Customized location for download");
        String filePath = System.getProperty("user.dir")+File.separator +"downloadFiles";

        log.info("Clean given directory");
        FileUtils.cleanDirectory(new File(filePath));

        log.info("Click on download csv button");
        page.clickDownloadCsvButton(page.getDownloadCsvButton().element);

        log.info("Wait for download to complete");
        page.wait.forXMillis(1000);

        log.info("Check if file is downloaded at given location");
        soft.assertTrue(DFileUtils.isFileDownloaded(filePath), "File is downloaded successfully");

        log.info("Retrieve grid row count from Pmode parties page : " + page.grid().getPagination().getTotalItems());
        int gridRowCount = page.grid().getPagination().getTotalItems();

        String completeFilePath = filePath + File.separator + DFileUtils.getCompleteFileName(filePath);
        log.info("Retrieve row count from downloaded file along with header row");
        int csvFileRowCount = DFileUtils.getRowCount(completeFilePath);

        log.info("Compare grid row from Pmode parties page and downloaded file grid row");
        soft.assertTrue(csvFileRowCount - 1 == gridRowCount, "Grid row Count matches with csvFileRow Count excluding headers");

        log.info("Compare grid row data and downloaded ");
        page.grid().checkCSVvsGridInfo(completeFilePath, soft);
        soft.assertAll();
    }


    /* This method will verify grid element changes on row selection with single click */
    @Test(description = "PMP-18", groups = {"multiTenancy", "singleTenancy"})
    public void singleClick() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Navigate to Pmode parties page");
        new DomibusPage(driver).getSidebar().goToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);
        do{
        page.grid().waitForRowsToLoad();

        log.info("Check status of New, Edit & Delete button");
        soft.assertTrue(page.getNewButton().isEnabled() , " New button is enabled");
        soft.assertFalse(page.getEditButton().isEnabled(), "Edit button is enabled");
        soft.assertFalse(page.getDeleteButton().isEnabled(), "Delete button is enabled");

        log.info("Select first row");
        page.grid().selectRow(0);

        log.info("Check New, Edit & Delete button status after row selection");
        soft.assertTrue(page.getNewButton().isEnabled(), " New button is enabled");
        soft.assertTrue(page.getEditButton().isEnabled(), " Edit button is enabled");
        soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled");

        if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
            log.info("break from loop if current domain is other than default");
            break;
        }
        if (data.isMultiDomain()) {
            log.info("Change domain other than default");
            page.getDomainSelector().selectOptionByText(getNonDefaultDomain());
            page.waitForTitle();
        }
    } while (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));
        soft.assertAll();

    }


}

