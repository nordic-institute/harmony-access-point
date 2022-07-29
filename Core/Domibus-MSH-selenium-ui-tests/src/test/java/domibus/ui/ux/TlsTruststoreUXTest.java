package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.tlsTrustStore.TlsTrustStorePage;
import pages.tlsTrustStore.TlsTruststoreModal;
import utils.DFileUtils;
import utils.TestUtils;

import java.util.HashMap;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.testng.Assert.assertTrue;


public class TlsTruststoreUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.TRUSTSTORES_TLS);

	// Need to handle messages separately for ST & MT after resolution of  EDELIVERY-8270
	/* EDELIVERY-8183 - TLS-1-Open Truststores-TLS page with no configuration */
	@Test(description = "TLS-1", groups = {"singleTenancy", "NoTlsConfig"})
	public void openTlsTrustorePg() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Login into application and navigate to TlsTruststore page");
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

	/* EDELIVERY-8184 - TLS-2-Open Truststores-TLS page with proper configuration */
	@Test(description = "TLS-2", groups = {"singleTenancy", "TlsConfig"})
	public void openPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Login into application and navigate to TlsTruststore page");
		log.info("Login into application and navigate to TlsTruststore page");
		selectRandomDomain();

		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		soft.assertTrue(page.isDefaultElmPresent(TRUE), "All default elements are present in default status");
		page.grid().getGridCtrl().showCtrls();

		soft.assertAll();
	}

	/* EDELIVERY-8197 - TLS-11-Verify Download CSV */
	@Test(description = "TLS-11", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void downloadCSV() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		Reporter.log("Click on download csv button");
		log.info("Click on download csv button");
		String fileName = page.pressSaveCsvAndSaveFile();
		Reporter.log("downloaded rows to file " + fileName);
		log.info("downloaded rows to file " + fileName);
		page.grid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	/* EDELIVERY-8198 - TLS-12-Change visible number of rows */
	@Test(description = "TLS-12", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void changeRowCount() throws Exception {

		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);
		soft.assertAll();
	}

	/* EDELIVERY-8201 - TLS-15-Check uncheck fields on Show Column links */
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

	/* EDELIVERY-8203 - TLS-17-Hide link after uncheckingchecking checkbox */
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

	/* EDELIVERY-8199 - TLS-13-Check Sorting */
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

	/* EDELIVERY-8202 - TLS-16-Check ALL None link feature */
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

	/* EDELIVERY-8204 - TLS-18-Open Tls TrustStore page with Super Admin */
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

	/* EDELIVERY-8205 - TLS-19-Open Tls Truststore page with Super Admin when no ssl configuration is done */
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

	/* EDELIVERY-8267 - TLS-23- Verify Single click on grid row */
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

	/* EDELIVERY-8268 - TLS-24-Verify Double click on grid row */
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

	/* EDELIVERY-8191 - TLS-8-Download Certificate */
	@Test(description = "TLS-8", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
	public void downloadCert() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		Reporter.log("Customized location for download");
		log.info("Customized location for download");
		String filePath = data.downloadFolderPath();

		page.pressDownloadCertAndSaveFile(filePath);
		soft.assertTrue(DFileUtils.getFileExtension(filePath).equals("jks"), "Downloaded cert file has jks extension");

		soft.assertAll();
	}

	/* EDELIVERY-8200 - TLS-14-Check Show Column link */
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

	/* EDELIVERY-8306 - TLS-25-Check Reflection of Upload Certificate feature on other domain */
	@Test(description = "TLS-25", groups = {"multiTenancy", "TlsConfig"})
	public void compareDomainDataAfterUpload() throws Exception {
		SoftAssert soft = new SoftAssert();
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
		int domainCount = rest.getDomainNames().size();
		for (int i = 0; i < domainCount; i++) {
			String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");
			page.uploadTruststore(path, "test123");
			page.waitForPageToLoad();
			List<HashMap<String, String>> firstDomainData = page.grid().getAllRowInfo();
			page.getDomainSelector().selectAnotherDomain();
			page.grid().waitForRowsToLoad();
			List<HashMap<String, String>> secondDomainData = page.grid().getAllRowInfo();
			soft.assertTrue(firstDomainData.equals(secondDomainData), "Both domain has same data after upload");
		}
		soft.assertAll();

	}

	/* EDELIVERY-8308 - TLS-27-Check reflection of remove certificate on other domain */
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

	/* EDELIVERY-9639 - TLS-29 - Add certificate button is enabled even if no certificate is present as long as the TLS Truststore has been uploaded at least once */
	@Test(description = "TLS-29", groups = {"multiTenancy"})
	public void removeAllAndCheckAddCertButton() throws Exception {
		TlsTrustStorePage page = new TlsTrustStorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

		try {
			if(page.getAlertArea().isError()){
				log.info("uploading truststore...");
				page.uploadTruststore(DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks"), "test123");
			}
		} catch (Exception e) {}

		log.info("remove all certificates");
		while (page.getRemoveCertButton().isEnabled()){
			page.grid().selectRow(0);
			page.getRemoveCertButton().click();
		}

		assertTrue(page.getAddCertButton().isEnabled(), "after all certificates are removed add cert button is still enabled");

	}
}




