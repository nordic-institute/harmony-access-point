package domibus.ui.functional;

import ddsl.dcomponents.AlertArea;
import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DObject;
import ddsl.enums.PAGES;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessagesPage;
import utils.BaseTest;
import utils.DFileUtils;
import java.io.File;


public class AllPgTest  extends BaseTest {

    String blackListedString = "'\\u0022(){}[];,+=%&*#<>/\\\\";

    /*Check extension of downloaded file on all pages*/
    @Test(description = "ALLDOM-1", groups = {"multiTenancy", "singleTenancy"})
    public void checkFileExtension() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        login(data.getAdminUser());

        for (PAGES ppage : PAGES.values()) {

            if (ppage.equals(PAGES.PMODE_CURRENT) || ppage.equals(PAGES.TEST_SERVICE) || ppage.equals(PAGES.LOGGING)) {
                //skipping these pages as they dont have download csv feature available
                continue;
            }

            page.getSidebar().goToPage(ppage);
            log.info("Customized location for download");
            String filePath = DFileUtils.downloadFolderPath();

            log.info("Clean given directory");
            FileUtils.cleanDirectory(new File(filePath));

            log.info("Click on download csv button");
            page.clickDownloadCsvButton(page.getDownloadCsvButton().element);
            log.info("Wait for download to complete");
            page.wait.forXMillis(3000);

            log.info("Check if file is downloaded at given location");
            soft.assertTrue(DFileUtils.isFileDownloaded(filePath), "File is downloaded successfully");
            soft.assertTrue(DFileUtils.getFileExtension(filePath).equals("csv"), "Downloaded file extension is csv");
            soft.assertAll();
        }
    }

    /*Check non acceptance of forbidden characters in input filters of all pages*/
    @Test(description = "ALLDOM-5", groups = {"multiTenancy", "singleTenancy"})
    public void checkFilterIpData() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        login(data.getAdminUser());

        for (PAGES ppage : PAGES.values()) {

            if (ppage.equals(PAGES.MESSAGE_FILTER) || ppage.equals(PAGES.PMODE_CURRENT) || ppage.equals(PAGES.PMODE_ARCHIVE)
            || ppage.equals(PAGES.TRUSTSTORE) || ppage.equals(PAGES.USERS)|| ppage.equals(PAGES.AUDIT)
                    || ppage.equals(PAGES.ALERTS)|| ppage.equals(PAGES.TEST_SERVICE)|| ppage.equals(PAGES.LOGGING)) {

                //skipping these pages as they dont have filter area available
                continue;
            }
            page.getSidebar().goToPage(ppage);
            fillBlackListedChar(ppage, blackListedString);

            if (ppage.equals(PAGES.PMODE_PARTIES)) {
                //Skipping Pmode parties page as Validation is handled at backend only
                soft.assertFalse(new DObject(driver, new AlertArea(driver).alertMessage).isPresent(),"No alert message is shown");
            } else {
                soft.assertTrue(page.getAlertArea().isError(),"Error for forbidden char is shown ");
            }
        }
        soft.assertAll();
    }

    /* Check presence of confirmation pop up on  all pages on page navigation/new search operation when unsaved data exists */
    @Test(description = "ALLDOM-2", groups = {"multiTenancy", "singleTenancy"})
    public void verifyConfPopUp() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        login(data.getAdminUser());
        page.getSidebar().goToPage(PAGES.MESSAGES);



    }

}
