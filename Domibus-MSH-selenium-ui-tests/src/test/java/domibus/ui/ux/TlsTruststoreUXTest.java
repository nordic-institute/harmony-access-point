package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.tlsTrustStore.TlsTrustStorePage;
import rest.RestServicePaths;
import java.util.List;

/**
 * @author Rupam
 * @version 5.0
 */

public class TlsTruststoreUXTest extends SeleniumTest {

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
        soft.assertTrue(page.getUploadButton().isEnabled(), "Upload button is enabled");
        soft.assertTrue(page.getDownloadButton().isDisabled(), "Download button is disabled");
        soft.assertTrue(page.getAddCertButton().isDisabled(), "Add Certificate button is disabled");
        soft.assertTrue(page.getRemoveCertButton().isDisabled(), "Remove Certificate button is disabled");
        soft.assertTrue(page.grid().getPagination().getTotalItems() == 0, "Grid is empty");
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
        soft.assertTrue(!page.getAlertArea().isShown(), "Alert area with error/success message is not shown on page landing");
        soft.assertTrue(page.getUploadButton().isEnabled(), "Upload button is enabled");
        soft.assertTrue(page.getDownloadButton().isEnabled(), "Download button is enabled");
        soft.assertTrue(page.getAddCertButton().isEnabled(), "Add Certificate button is enabled");
        soft.assertTrue(page.getRemoveCertButton().isDisabled(), "Remove Certificate button is disabled");
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
    }
