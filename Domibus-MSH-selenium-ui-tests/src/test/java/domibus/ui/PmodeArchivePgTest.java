package domibus.ui;

import ddsl.enums.PAGES;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.PModeArchivePage;
import rest.RestServicePaths;

/**
 * @author Rupam
 * @description:
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
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        if(page.grid().getRowsNo()==0)
        {rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        page.getPage().getSidebar().gGoToPage(PAGES.PMODE_ARCHIVE);
        }
        page.getPmodeStatus();
        soft.assertTrue(page.isLoaded(), "Archive page is loaded correctly");

    }

    /*
    This testcase will ensure delete icon/button  is disable for current file.
     */
    @Test(description = "PMA-2", groups = {"multiTenancy", "singleTenancy"})
    public void DeleteRestoreCurrentFile() throws Exception {

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        soft.assertTrue(page.isLoaded(), "Archive page is loaded correctly");

        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        page.grid().scrollToAndSelect("Description", "[CURRENT]: automatic red");
        soft.assertTrue(!page.getDeleteButton().isEnabled(), "Button is disabled");
        soft.assertTrue(!page.getRestoreButton().isEnabled(), "Button is disabled");
        soft.assertTrue(!page.getCDeleteIcon().isEnabled(), "Icon is disabled");
        soft.assertTrue(!page.getCRestoreIcon().isEnabled(), "Icon is disabled");

    }

    /*
     *This testcase will download current file through download button and icon(pending)
     * Xml comparison is still remaining//work remaining
     */
    @Test(description = "PMA-3", groups = {"multiTenancy", "singleTenancy"})
    public void DownloadCurrentFile() throws Exception {
        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        soft.assertTrue(page.isLoaded(), "Archive page is loaded correctly");
        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        page.grid().scrollToAndSelect("Description", "[CURRENT]: automatic red");
        page.getDownloadButton().click();
    }

    /*
    This testcase will download Grid content
     */
    @Test(description = "PMA-4", groups = {"multiTenancy", "singleTenancy"})
    public void DownloadGridData() throws Exception {
        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage page = new PModeArchivePage(driver);
        String fileName = rest.downloadGrid(RestServicePaths.PMODE_ARCHIVE_CSV, null, null);
        page.getDownloadCSV().click();
        soft.assertAll();
    }


}
