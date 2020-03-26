package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DWait;
import ddsl.enums.PAGES;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Alert.AlertPage;
import utils.BaseUXTest;
import utils.DFileUtils;
import utils.TestUtils;

import java.io.File;

public class AlertPgUXTest extends BaseUXTest {

    JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.ALERTS);

    /* disabled because EDELIVERY-4186 */
    @Test(description = "ALRT-20", groups = {"multiTenancy", "singleTenancy"})
    public void verifyHeaders() throws Exception {
        SoftAssert soft = new SoftAssert();
        AlertPage page = new AlertPage(driver);
        page.getSidebar().goToPage(PAGES.ALERTS);
        log.info("Customized location for download");
        String filePath = System.getProperty("user.dir") + File.separator + "downloadFiles";

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

    @Test(description = "ALRT-19", groups = {"multiTenancy", "singleTenancy"})
    public void checkSorting() throws Exception {
        JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

        SoftAssert soft = new SoftAssert();
        AlertPage page = new AlertPage(driver);
        page.getSidebar().goToPage(PAGES.ALERTS);
        do {
            DGrid grid = page.grid();
            log.info("Change page selector value to 25");
            grid.getPagination().getPageSizeSelect().selectOptionByText("25");

            for (int i = 0; i < colDescs.length(); i++) {
                JSONObject colDesc = colDescs.getJSONObject(i);
                if (grid.getColumnNames().contains(colDesc.getString("name"))) {
                    TestUtils.testSortingForColumn(soft, grid, colDesc);
                }
            }
            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                break;
            }
            if (data.isMultiDomain()) {
                log.info("chnage domain");
                page.getDomainSelector().selectOptionByIndex(1);
            }
            page.refreshPage();
            page.grid().waitForRowsToLoad();
        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));

        soft.assertAll();
    }
}
