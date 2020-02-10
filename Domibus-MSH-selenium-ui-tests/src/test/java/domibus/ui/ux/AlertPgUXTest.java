package domibus.ui.ux;

import ddsl.dobjects.DWait;
import ddsl.enums.PAGES;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Alert.AlertPage;
import utils.BaseUXTest;
import utils.DFileUtils;

import java.io.File;

public class AlertPgUXTest extends BaseUXTest {

    /* disabled because EDELIVERY-4186 */
    @Test(description = "ALRT-20", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
    public void verifyHeaders() throws Exception {
        SoftAssert soft = new SoftAssert();
        AlertPage page = new AlertPage(driver);
        page.getSidebar().goToPage(PAGES.ALERTS);
        log.info("Customized location for download");
        String filePath = System.getProperty("user.dir")+ File.separator +"downloadFiles";

        log.info("Clean given directory");
        FileUtils.cleanDirectory(new File(filePath));

        log.info("Click on download csv button");
        page.clickDownloadCsvButton(page.getDownloadCsvButton().element);

        log.info("Wait for download to complete");
        DWait wait = new DWait(driver);
        wait.forXMillis(1000);

        log.info("Check if file is downloaded at given location");
        soft.assertTrue(DFileUtils.isFileDownloaded(filePath), "File is downloaded successfully");

        log.info("Extract complete path for downloaded file");
        String completeFilePath = filePath + File.separator + DFileUtils.getCompleteFileName(filePath);

        log.info("Click on show link");
        page.gridControl().showCtrls();

        log.info("Click on All link to show all available column headers");
        page.gridControl().showAllColumns();

        log.info("Compare headers from downloaded csv and grid");
        page.grid().checkCSVvsGridHeaders(completeFilePath, soft);
        soft.assertAll();
    }

}
