package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.tlsTrustStore.TlsTrustStorePage;
import rest.RestServicePaths;
import utils.TestUtils;

import java.util.List;

import static java.lang.Boolean.*;

/**
 * @author Rupam
 * @version 5.0
 */

public class TlsTruststoreUXTest extends SeleniumTest {

    JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.TRUSTSTORES_TLS);


    /* This method will verify page navigation and components when tls config is not done */
    @Test(description = "TLS-1", groups = {"multiTenancy", "singleTenancy", "NoTlsConfig"})
    public void openTlsTrustorePg() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Login into application and navigate to TlsTruststore page");

        selectRandomDomain();

        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

        soft.assertTrue(page.getAlertArea().isError(), "Error message appears");
        String currentDomain = page.getDomainFromTitle();
        if (!data.isMultiDomain()) {
            currentDomain = "default";
        }
        soft.assertTrue(page.getAlertArea().getAlertMessage().equals(String.format(DMessages.TlsTruststore.TLS_TRUSTSTORE_NOCONFIG, currentDomain)), "same");
        soft.assertTrue(page.isDefaultElmPresent(FALSE),"All default elements are present in default status");

        soft.assertAll();
    }

    /* This method will verfiy page navigation with components when tls configuration is done */
    @Test(description = "TLS-2", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
    public void openPage() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Login into application and navigate to TlsTruststore page");
        selectRandomDomain();

        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
        soft.assertTrue(page.isDefaultElmPresent(TRUE),"All default elements are present in default status");

        soft.assertAll();
    }

    /* This method will verify download csv feature */
    @Test(description = "TLS-11", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
    public void downloadCSV() throws Exception {
        SoftAssert soft = new SoftAssert();
        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

        String fileName = rest.csv().downloadGrid(RestServicePaths.TLS_TRUSTSTORE_CSV, null, null);
        log.info("downloaded rows to file " + fileName);
        page.grid().checkCSVvsGridInfo(fileName, soft);

        soft.assertAll();
    }

    /* This method will verify grid data by changing rows */
    @Test(description = "TLS-12", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
    public void changeRowCount() throws Exception {

        SoftAssert soft = new SoftAssert();
        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

        DGrid grid = page.grid();
        grid.checkChangeNumberOfRows(soft);
        soft.assertAll();
    }

    /* Check/Uncheck of fields on Show links */
    @Test(description = "TLS-15", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
    public void changeVisibleColumns() throws Exception {

        SoftAssert soft = new SoftAssert();
        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

        DGrid grid = page.grid();
        grid.waitForRowsToLoad();
        grid.checkModifyVisibleColumns(soft);

        soft.assertAll();
    }

    /* This method will verify hide show link functionality */
    @Test(description = "TLS-17", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
    public void hideWithoutSelection() throws Exception {
        SoftAssert soft = new SoftAssert();
        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

        DGrid grid = page.grid();
        List<String> columnsPre = grid.getColumnNames();

        soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before clicking Show link,checkboxes are not visible");

        grid.getGridCtrl().showCtrls();
        soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After clicking Show link,checkboxes are visible");

        grid.getGridCtrl().hideCtrls();
        soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After clicking Hide link,checkboxes are not visible");

        List<String> columnsPost = grid.getColumnNames();
        soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after event are same");

        soft.assertAll();
    }

    /* This test case will verify sorting on the basis of all sortable columns */
    @Test(description = "TLS-13", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
    public void verifySorting() throws Exception {
        SoftAssert soft = new SoftAssert();
        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

        page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

        JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");
        for (int i = 0; i < colDescs.length(); i++) {
            JSONObject colDesc = colDescs.getJSONObject(i);
            if (page.grid().getColumnNames().contains(colDesc.getString("name"))) {
                TestUtils.testSortingForColumn(soft, page.grid(), colDesc);
            }
        }

        soft.assertAll();
    }

    /* This method will verify show column link presence along with all column checkboxes*/
    @Test(description = "TLS-14", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
    public void verifyShowLinkFeature() throws Exception {
        SoftAssert soft = new SoftAssert();
        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
        page.getGridctrls().showCtrls();
        soft.assertTrue(page.getGridctrls().areCheckboxesVisible(), "All check boxes are visible");
        soft.assertTrue(page.getGridctrls().getAllLnk().isPresent() && page.getGridctrls().getAllLnk().isEnabled(), "All link is present & enabled");
        soft.assertTrue(page.getGridctrls().getNoneLnk().isPresent() && page.getGridctrls().getNoneLnk().isEnabled(), "None link is present & enabled");

        soft.assertAll();

    }

    /* This method will verify Page navigation and default element present on both domains when tls config is done*/
    @Test(description = "TLS-18", groups = {"multiTenancy", "TlsConfig"})
    public void openPageForSuperAdmin() throws Exception {
        SoftAssert soft = new SoftAssert();
        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
        int domainCount = rest.getDomainNames().size();
        for (int i = 0; i <= domainCount - 1; i++) {
            page.getDomainSelector().selectOptionByIndex(i);
            page.grid().waitForRowsToLoad();
            soft.assertTrue(page.isDefaultElmPresent(TRUE),"All Default elements are present in default state");
        }
        soft.assertAll();
    }

    /* This test case will verify Page navigation and element on both domains when no tls config is done*/
    @Test(description = "TLS-19", groups = {"multiTenancy", "NoTlsConfig"})
    public void openPageSuperAdmin() throws Exception {
        SoftAssert soft = new SoftAssert();
        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
        int domainCount = rest.getDomainNames().size();
        for (int i = 0; i <= domainCount - 1; i++) {
            soft.assertTrue(page.getAlertArea().isShown(), "Error message is shown");
            page.getDomainSelector().selectOptionByIndex(i);
            page.grid().waitForRowsToLoad();
            soft.assertTrue(page.isDefaultElmPresent(FALSE),"All default elements are present in default state");
        }
        soft.assertAll();
    }

   /* This test case will verify Single click on grid row functionality */
    @Test(description = "TLS-23", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
    public void singleClick() throws Exception {
        SoftAssert soft = new SoftAssert();
        TlsTrustStorePage page = new TlsTrustStorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
        soft.assertFalse(page.grid().gridRows.get(0).getAttribute("class").contains("active"),"Grid is not selected yet");
        page.grid().selectRow(0);
        soft.assertTrue(page.grid().gridRows.get(0).getAttribute("class").contains("active"),"Grid is selected now");
        soft.assertTrue(page.getRemoveCertButton().isEnabled(),"Remove button is enabled on selection");
        soft.assertAll();
    }


}

