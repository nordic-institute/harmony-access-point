package domibus.ui;


import ddsl.enums.PAGES;
import domibus.BaseTest;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.PModeArchivePage;
import rest.RestServicePaths;


/**
 * @author Rupam

 * @since 4.1
 */
public class PmodeArchivePgTest extends BaseTest {

    /*
    This testcase  will check status of Pmode status whether no upload,
    multiple upload exists in the system.

     */
    @Test(description = "PMA-1", groups = {"multiTenancy", "singleTenancy"})
    public void OpenPmodeArchivePage() throws Exception {

        SoftAssert soft = new SoftAssert();
        log.info("Login into applicatin with Admin credentialsand navigate to Pmode_archive page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        log.info("Total no of rows in archive grid" + page.grid().getRowsNo());
        if (page.grid().getRowsNo() == 0) {
            log.info("Upload pmode through rest service");
            rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
            log.info("Navigate to Pmode Archive page");
            page.getPage().getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        }
        log.info("Check pmode upload status in application");
        page.getPmodeStatus();
        log.info("Validate Archive page is loaded successfully");
        soft.assertTrue(page.isLoaded(), "Archive page is loaded correctly");
        soft.assertAll();
    }

    /*
    This testcase will ensure delete icon/button  is disable for current file.
     */
    @Test(description = "PMA-2", groups = {"multiTenancy", "singleTenancy"})
    public void DeleteRestoreCurrentFile() throws Exception {

        SoftAssert soft = new SoftAssert();
        log.info("Login with Admin credentail and navigate to Pmode archive page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        log.info("Validate Archive page");
        soft.assertTrue(page.isLoaded(), "Archive page is loaded correctly");
        log.info("Upload pmode using rest service");
        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        log.info("Select row corresponding to current pmode");
        page.grid().scrollToAndSelect("Description", "[CURRENT]: automatic red");
        log.info("Validate Delete button is disabled");
        soft.assertTrue(!page.getDeleteButton().isEnabled(), "Button is disabled");
        log.info("Validate Restore button is disabled");
        soft.assertTrue(!page.getRestoreButton().isEnabled(), "Button is disabled");
        log.info("Validate row specific delete icon is disabled");
        soft.assertTrue(!page.getCDeleteIcon().isEnabled(), "Icon is disabled");
        log.info("Validate row specificrestore icon is disabled");
        soft.assertTrue(!page.getCRestoreIcon().isEnabled(), "Icon is disabled");

    }

    /*
     *This testcase will compare Current pmode From Archive page and current page and download it

     */
    @Test(description = "PMA-3", groups = {"multiTenancy", "singleTenancy"})
    public void ComparePmodesAndDownload() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login with Admin credentials and navigate to Pmode archive page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        log.info("Validate Pmode Archive page");
        soft.assertTrue(page.isLoaded(), "Archive page is loaded correctly");
        log.info("Upload pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        log.info("Selecting top row");
        page.grid().doubleClickRow(0);
        String ArchiveCurrentPmode = page.getXml().getText();
        log.info("Printing archive page current pmode: \r\n" + ArchiveCurrentPmode);
        page.refreshPage();
        log.info("navigate to Pmode_current page");
        page.getPage().getSidebar().goToPage(PAGES.PMODE_CURRENT);
        String DefaultPmode = page.getPage().getTextArea().getText();
        System.out.println("printing current page pmode" + DefaultPmode);
        log.info("Ignore white space while xml comparison");
        XMLUnit.setIgnoreWhitespace(true);
        log.info("Ignore white attribute order while xml comparison");
        XMLUnit.setIgnoreAttributeOrder(true);
        log.info("Compare Current pmode from Pmode archive page & current page ");
        XMLAssert.assertXMLEqual(DefaultPmode, ArchiveCurrentPmode);
        log.info("Navigate to Pmode archive page");
        page.getPage().getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        log.info("Validate Archive page");
        soft.assertTrue(page.isLoaded(), "Archive page is loaded correctly");
        log.info("Download current pmode from Archive page");
        rest.downloadGrid(page.getRestServicePath(), null, null);
        soft.assertAll();
    }

    /*
    This testcase will download Grid content
     */
    @Test(description = "PMA-4", groups = {"multiTenancy", "singleTenancy"})
    public void DownloadGridData() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("login into application with Admin user and navigate to Pmode archive page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        log.info("Download grid data");
        rest.downloadGrid(RestServicePaths.PMODE_ARCHIVE_CSV, null, null);
        soft.assertAll();
    }

    /*
     * This testcase will delete any older file
     */
    @Test(description = "PMA-5", groups = {"multiTenancy", "singleTenancy"})
    public void DeleteOldFile() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin user and navigate to Pmode archive");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        log.info("Grid row count is : " + page.grid().getRowsNo());
        if (page.grid().getRowsNo() <= 1) {
            log.info("Upload pmode");
            rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
            rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
            log.info("Navigate to Pmode Archive page");
            page.getPage().getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        }
        log.info("Validate Pmode archive page");
        soft.assertTrue(page.isLoaded(), "page is loaded successfully");
        log.info("Select First row");
        page.grid().selectRow(1);
        log.info("Click on delete button");
        page.getDeleteButton().click();
        log.info("click on Save button");
        page.getSaveButton().click();
        log.info("Click ok for confirmation");
        page.getConfirmation().confirm();
        log.info("Validate Pmode archive page");
        soft.assertTrue(page.isLoaded(), "page is loaded successfully");
        log.info("Grid data after delete operation: " + page.getpagination().getTotalItems());
        soft.assertTrue(page.getpagination().getTotalItems() != 0, "Grid has data left after delete action");
        soft.assertAll();
    }

    /*
     *This testcase  will restore any older file and increment of one row corresponding new current file with description
     * starts with [CURRENT]: Restored
     */
    @Test(description = "PMA-6", groups = {"multiTenancy", "singleTenancy"})
    public void RestoreOldFile() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin credential and navigate to Pmode archive page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        log.info("Validate Pmode archive page");
        soft.assertTrue(page.isLoaded(), "Archive page is loaded correctly");
        log.info("Number of records in Archive grid :" + page.grid().getRowsNo());
        if (page.grid().getRowsNo() <= 1) {
            log.info("Upload pmode");
            rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
            rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
            log.info("Validate Pmode Archive");
            page.getPage().getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        }
        int x = page.grid().getRowsNo();
        log.info("Grid count before restore operation: " + x);
        log.info("Select first row");
        page.grid().selectRow(1);
        log.info("Click restore Button");
        page.getRestoreButton().click();
        log.info("Click on on Confirmation pop up");
        page.getConfirmation().confirm();
        log.info("Validate Current pmode row as restored version");
        soft.assertTrue(page.getCurDescTxt(), "Current row is restored version");
        soft.assertAll();
    }

    /* This testcase will check double click feature
     */
    @Test(description = "PMA-7", groups = {"multiTenancy", "singleTenancy"})
    public void DoubleClick() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application with Admin user and Navigate to pmode archive");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        log.info("Validate Archive page");
        soft.assertTrue(page.isLoaded(), "Archive page is loaded correctly");
        log.info("Double click on Grid row ");
        page.DoubleClickRow();
        soft.assertTrue(page.isLoaded(), "page is loaded successfully");
        soft.assertAll();
    }

}
