package domibus.ui.ux;

import io.qameta.allure.*;
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
import pages.tlsTrustStore.TlsTruststoreModal;
import utils.DFileUtils;
import utils.TestUtils;

import java.util.HashMap;
import java.util.List;

import static java.lang.Boolean.*;

/**
 * @author Rupam
 * @version 5.0
 */

@Epic("TLS Truststore")
@Feature("UX")
public class TlsTruststoreUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.TRUSTSTORES_TLS);

	// Need to handle messages separately for ST & MT after resolution of  EDELIVERY-8270
	/* This method will verify page navigation and components when tls config is not done */
	/*  TLS-1-Open Truststores-TLS page with no configuration  */
	@Description("TLS-1-Open Truststores-TLS page with no configuration")
	@Link(name = "EDELIVERY-8183", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8183")
	@AllureId("TLS-1")
	@Test(description = "TLS-1", groups = {"singleTenancy", "NoTlsConfig"})
	public void openTlsTrustorePg() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to TlsTruststore page");
		log.info("Login into application and navigate to TlsTruststore page");

		selectRandomDomain();

		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		soft.assertTrue(page.getAlertArea().isError(), "Error message appears");
		String currentDomain = page.getDomainFromTitle();
		if (!data.isMultiDomain()) {
			currentDomain = "default";
		}
		soft.assertTrue(page.getAlertArea().getAlertMessage().equals(String.format(DMessages.TlsTruststore.TLS_TRUSTSTORE_NOCONFIG, currentDomain)), "Correct message is shown");
		soft.assertTrue(page.isDefaultElmPresent(FALSE), "All default elements are present in default status");
		page.grid().getGridCtrl().showCtrls();

		soft.assertAll();
	}

	/* This method will verfiy page navigation with components when tls configuration is done */
	/*  TLS-2-Open Truststores-TLS page with proper configuration  */
	@Description("TLS-2-Open Truststores-TLS page with proper configuration")
	@Link(name = "EDELIVERY-8184", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8184")
	@AllureId("TLS-2")
	@Test(description = "TLS-2", groups = {"singleTenancy", "TlsConfig"})
	public void openPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to TlsTruststore page");
		log.info("Login into application and navigate to TlsTruststore page");
		selectRandomDomain();

		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		soft.assertTrue(page.isDefaultElmPresent(TRUE), "All default elements are present in default status");
		page.grid().getGridCtrl().showCtrls();

		soft.assertAll();
	}

	/* This method will verify download csv feature */
	/*  TLS-11-Verify Download CSV  */
	@Description("TLS-11-Verify Download CSV")
	@Link(name = "EDELIVERY-8197", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8197")
	@AllureId("TLS-11")
	@Test(description = "TLS-11", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void downloadCSV() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		Allure.step("Click on download csv button");
		log.info("Click on download csv button");
		String fileName = page.pressSaveCsvAndSaveFile();
		Allure.step("downloaded rows to file " + fileName);
		log.info("downloaded rows to file " + fileName);
		page.grid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	/* This method will verify grid data by changing rows */
	/*  TLS-12-Change visible number of rows  */
	@Description("TLS-12-Change visible number of rows")
	@Link(name = "EDELIVERY-8198", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8198")
	@AllureId("TLS-12")
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
	/*  TLS-15-Check uncheck fields on Show Column links  */
	@Description("TLS-15-Check uncheck fields on Show Column links")
	@Link(name = "EDELIVERY-8201", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8201")
	@AllureId("TLS-15")
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
	/*  TLS-17-Hide link after uncheckingchecking checkbox  */
	@Description("TLS-17-Hide link after uncheckingchecking checkbox")
	@Link(name = "EDELIVERY-8203", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8203")
	@AllureId("TLS-17")
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
	/*  TLS-13-Check Sorting  */
	@Description("TLS-13-Check Sorting")
	@Link(name = "EDELIVERY-8199", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8199")
	@AllureId("TLS-13")
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
	/*  TLS-16-Check ALL None link feature  */
	@Description("TLS-16-Check ALL None link feature")
	@Link(name = "EDELIVERY-8202", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8202")
	@AllureId("TLS-16")
	@Test(description = "TLS-16", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void verifyAllNoneLinkFeature() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		page.grid().checkShowLink(soft);
		page.grid().checkAllLink(soft);
		page.grid().checkNoneLink(soft);

		soft.assertAll();

	}

	/* This method will verify Page navigation and default element present on both domains when tls config is done*/
	/*  TLS-18-Open Tls TrustStore page with Super Admin  */
	@Description("TLS-18-Open Tls TrustStore page with Super Admin")
	@Link(name = "EDELIVERY-8204", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8204")
	@AllureId("TLS-18")
	@Test(description = "TLS-18", groups = {"multiTenancy", "TlsConfig"})
	public void openPageForSuperAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		int domainCount = rest.getDomainNames().size();
		for (int i = 0; i < domainCount; i++) {
			page.getDomainSelector().selectOptionByIndex(i);
			page.grid().waitForRowsToLoad();
			soft.assertTrue(page.isDefaultElmPresent(TRUE), "All Default elements are present in default state");
			page.grid().getGridCtrl().showCtrls();
		}
		soft.assertAll();
	}

	/* This test case will verify Page navigation and element on both domains when no tls config is done*/
	/*  TLS-19-Open Tls Truststore page with Super Admin when no ssl configuration is done  */
	@Description("TLS-19-Open Tls Truststore page with Super Admin when no ssl configuration is done")
	@Link(name = "EDELIVERY-8205", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8205")
	@AllureId("TLS-19")
	@Test(description = "TLS-19", groups = {"multiTenancy", "NoTlsConfig"})
	public void openPageSuperAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		int domainCount = rest.getDomainNames().size();
		for (int i = 0; i < domainCount; i++) {
			soft.assertTrue(page.getAlertArea().isShown(), "Error message is shown");
			page.getDomainSelector().selectOptionByIndex(i);
			page.grid().waitForRowsToLoad();
			soft.assertTrue(page.isDefaultElmPresent(FALSE), "All default elements are present in default state");
			page.grid().getGridCtrl().showCtrls();

		}
		soft.assertAll();
	}

	/* This test case will verify Single click on grid row functionality */
	/*  TLS-23- Verify Single click on grid row  */
	@Description("TLS-23- Verify Single click on grid row")
	@Link(name = "EDELIVERY-8267", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8267")
	@AllureId("TLS-23")
	@Test(description = "TLS-23", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void singleClick() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		soft.assertFalse(page.grid().gridRows.get(0).getAttribute("class").contains("active"), "Grid is not selected yet");
		page.grid().selectRow(0);
		soft.assertTrue(page.grid().gridRows.get(0).getAttribute("class").contains("active"), "Grid is selected now");
		soft.assertTrue(page.getRemoveCertButton().isEnabled(), "Remove button is enabled on selection");
		soft.assertAll();
	}

	/* This test case will verify Double click on grid row functionality */
	/*  TLS-24-Verify Double click on grid row  */
	@Description("TLS-24-Verify Double click on grid row")
	@Link(name = "EDELIVERY-8268", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8268")
	@AllureId("TLS-24")
	@Test(description = "TLS-24", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void doubleClick() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		HashMap<String, String> entryInfo = page.grid().getRowInfo(0);

		page.grid().doubleClickRow(0);
		TlsTruststoreModal tModal = new TlsTruststoreModal(driver);

		soft.assertTrue(entryInfo.get("Valid from").equals(tModal.getValidFromInput().getText()), " Valid from data is same at both places ");
		soft.assertTrue(entryInfo.get("Issuer").equals(tModal.getIssuerInput().getText()), "Issuer data is same at both places");
		soft.assertTrue(entryInfo.get("Valid until").equals(tModal.getValidToInput().getText()), "Valid until data is same at both places");
		soft.assertTrue(entryInfo.get("Subject").equals(tModal.getSubjectInput().getText()), "Subject data is same at both places");
		soft.assertAll();
	}

	/* This test method will verify download feature*/
	/*  TLS-8-Download Certificate  */
	@Description("TLS-8-Download Certificate")
	@Link(name = "EDELIVERY-8191", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8191")
	@AllureId("TLS-8")
	@Test(description = "TLS-8", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void downloadCert() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		Allure.step("Customized location for download");
		log.info("Customized location for download");
		String filePath = data.downloadFolderPath();

		page.pressDownloadCertAndSaveFile(filePath);
		soft.assertTrue(DFileUtils.getFileExtension(filePath).equals("jks"), "Downloaded cert file has jks extension");

		soft.assertAll();
	}

	/*This test method will verify presence of Show Link along additional section and checkboxes on click event*/
	/*  TLS-14-Check Show Column link  */
	@Description("TLS-14-Check Show Column link")
	@Link(name = "EDELIVERY-8200", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8200")
	@AllureId("TLS-14")
	@Test(description = "TLS-14", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void checkShowLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		page.grid().checkShowLink(soft);
		page.grid().getGridCtrl().showCtrls();
		soft.assertTrue(page.showHideAdditionalArea.isDisplayed(), "ShowHide additional area is displayed");
		soft.assertTrue(page.grid().columnsVsCheckboxes(), "Visible columns and grid columns are same");
		soft.assertAll();
	}

	/* This test method will verify certificate grid data on tls truststore page for both domain on upload event */
	/*  TLS-25-Check Reflection of Upload Certificate feature on other domain  */
	@Description("TLS-25-Check Reflection of Upload Certificate feature on other domain")
	@Link(name = "EDELIVERY-8306", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8306")
	@AllureId("TLS-25")
	@Test(description = "TLS-25", groups = {"multiTenancy", "TlsConfig"})
	public void compareDomainDataAfterUpload() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		int domainCount = rest.getDomainNames().size();
		for (int i = 0; i < domainCount; i++) {
			String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");
			page.uploadAddCert(path, "test123", page.getUploadButton(), page.getPassInputField());
			page.waitForPageToLoad();
			List<HashMap<String, String>> firstDomainData = page.grid().getAllRowInfo();
			page.getDomainSelector().selectAnotherDomain();
			page.grid().waitForRowsToLoad();
			List<HashMap<String, String>> secondDomainData = page.grid().getAllRowInfo();
			soft.assertTrue(firstDomainData.equals(secondDomainData), "Both domain has same data after upload");
		}
		soft.assertAll();

	}

	/* This test method will verify Tls truststore certificate data on both domain after remove oeration */
	/*  TLS-27-Check reflection of remove certificate on other domain  */
	@Description("TLS-27-Check reflection of remove certificate on other domain")
	@Link(name = "EDELIVERY-8308", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8308")
	@AllureId("TLS-27")
	@Test(description = "TLS-27", groups = {"multiTenancy", "TlsConfig"})
	public void compareDomainDataAfterRemove() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		int domainCount = rest.getDomainNames().size();
		for (int i = 0; i < domainCount; i++) {
			page.grid().selectRow(0);
			page.getRemoveCertButton().click();
			page.grid().waitForRowsToLoad();
			List<HashMap<String, String>> firstDomainData = page.grid().getAllRowInfo();
			page.getDomainSelector().selectAnotherDomain();
			page.grid().waitForRowsToLoad();
			List<HashMap<String, String>> secondDomainData = page.grid().getAllRowInfo();
			soft.assertTrue(firstDomainData.equals(secondDomainData), "Same domain has same data after one cert removal");

		}
		soft.assertAll();

	}

	/* This test method will verify blank tlstruststore page grid on both domain after remove all operation*/
	/*  TLS-28-Check reflection on other domain data after removal of all certificate from tls truststore page  */
	@Description("TLS-28-Check reflection on other domain data after removal of all certificate from tls truststore page")
	@Link(name = "EDELIVERY-8309", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8309")
	@AllureId("TLS-28")
	@Test(description = "TLS-28", groups = {"multiTenancy", "TlsConfig"})
	public void compareDomainDataAfterRemoveAll() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		int domainCount = rest.getDomainNames().size();
		for (int i = 0; i < domainCount; i++) {
			int beforeCount = page.grid().getPagination().getTotalItems();
			if (beforeCount == 0) {
				String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");
				page.uploadAddCert(path, "test123", page.getUploadButton(), page.getPassInputField());
				page.grid().waitForRowsToLoad();
			}
			int afterCount = page.grid().getPagination().getTotalItems();
			for (int j = afterCount - 1; j >= 0; j--) {
				page.grid().selectRow(j);
				page.getRemoveCertButton().click();
			}
			page.grid().waitForRowsToLoad();
			List<HashMap<String, String>> firstDomainData = page.grid().getAllRowInfo();
			page.getDomainSelector().selectAnotherDomain();
			page.grid().waitForRowsToLoad();
			List<HashMap<String, String>> secondDomainData = page.grid().getAllRowInfo();
			soft.assertTrue(firstDomainData.equals(secondDomainData), "Same domain has same data after one cert removal");
		}
		soft.assertAll();

	}
}




