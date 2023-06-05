package domibus.ui.ux;

import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.certificates.CertificatePage;
import utils.DFileUtils;
import utils.TestUtils;

public class KeystorePgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.KEYSTORE);

	/* EDELIVERY-10241 - KST-1 - Login as admin and open KeyStore page */
	@Test(description = "KST-1", groups = {"multiTenancy", "singleTenancy"})
	public void openKeyStorePage() throws Exception {

		SoftAssert soft = new SoftAssert();
		CertificatePage page = new CertificatePage(driver);
		page.getSidebar().goToPage(PAGES.KEYSTORE);

		log.info("checking page default state");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}

		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");

		soft.assertTrue(page.getUploadButton().isEnabled(), "Upload button is enabled");
		soft.assertTrue(page.getDownloadButton().isEnabled(), "Download button is enabled");
		soft.assertTrue(page.getReloadButton().isEnabled(), "Reload button is enabled");

		soft.assertAll();
	}

	/*     EDELIVERY-10242 - KST-2 - KeyStore page grid controls */
	@Test(description = "KST-2", groups = {"multiTenancy", "singleTenancy"})
	public void griControls() throws Exception {

		SoftAssert soft = new SoftAssert();
		CertificatePage page = new CertificatePage(driver);
		page.getSidebar().goToPage(PAGES.KEYSTORE);

		page.grid().checkChangeNumberOfRows(soft);

		page.grid().checkShowLink(soft);
		page.grid().checkHideLink(soft);
		page.grid().checkAllLink(soft);
		page.grid().checkNoneLink(soft);
		page.grid().checkModifyVisibleColumns(soft);

		soft.assertAll();
	}


	/*     EDELIVERY-10243 - KST-3 - Download CSV with grid content */
	@Test(description = "KST-3", groups = {"multiTenancy", "singleTenancy"})
	public void csvFile() throws Exception {

		SoftAssert soft = new SoftAssert();
		CertificatePage page = new CertificatePage(driver);
		page.getSidebar().goToPage(PAGES.KEYSTORE);

		page.grid().getGridCtrl().showAllColumns();

		String file = page.pressSaveCsvAndSaveFile();
		page.grid().checkCSVvsGridHeaders(file, soft);
		page.grid().checkCSVvsGridInfo(file, soft);
		soft.assertAll();

	}

	/* EDELIVERY-10244 - KST-4 - Upload valid file */
	@Test(description = "KST-4", groups = {"multiTenancy", "singleTenancy"})
	public void uploadValidFile() throws Exception {

		SoftAssert soft = new SoftAssert();
		CertificatePage page = new CertificatePage(driver);
		page.getSidebar().goToPage(PAGES.KEYSTORE);


		log.info("Try uploading correct truststore file");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");

		page.uploadFile(path, "test123");
		log.info(page.getAlertArea().getAlertMessage() + " Message after upload event");

		log.info("Validate presence of successfully keyword in Alert message for default domain");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);


		soft.assertAll();

	}


	/* EDELIVERY-10248 - KST-6 - Upload valid file containing keys with non alphanumeric characters in the key name */
	@Test(description = "KST-6", groups = {"multiTenancy", "singleTenancy"})
	public void uploadNonAlphaNumFile() throws Exception {

		SoftAssert soft = new SoftAssert();
		CertificatePage page = new CertificatePage(driver);
		page.getSidebar().goToPage(PAGES.KEYSTORE);


		log.info("Try uploading correct truststore file");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/àøýßĉæãäħ.jks");

		page.uploadFile(path, "test123");
		log.info(page.getAlertArea().getAlertMessage() + " Message after upload event");

		log.info("Validate presence of successfully keyword in Alert message for default domain");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);

		soft.assertAll();

	}

	/*     EDELIVERY-10249 - KST-7 - Reload keystore */
	@Test(description = "KST-7", groups = {"multiTenancy", "singleTenancy"})
	public void reloadKeystore() throws Exception {

		SoftAssert soft = new SoftAssert();
		CertificatePage page = new CertificatePage(driver);
		page.getSidebar().goToPage(PAGES.KEYSTORE);


		log.info("Try uploading correct truststore file");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/àøýßĉæãäħ.jks");

		page.uploadFile(path, "test123");
		log.info(page.getAlertArea().getAlertMessage() + " Message after upload event");


		page.getReloadButton().click();
		log.info(page.getAlertArea().getAlertMessage() + " Message after reload event");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.KEYSTORE_RESET);

		soft.assertEquals(page.grid().getRowsNo(), 2, "Grid has 2 row after reload");
		soft.assertEquals(page.grid().getRowInfo(0).get("Name"), "blue_gw", "Grid has default blue_gw certificate");


		soft.assertAll();

	}

	/* EDELIVERY-10246 - KST-10 - Upload valid file containing expired keys       */
	@Test(description = "KST-10", groups = {"multiTenancy", "singleTenancy"})
	public void uploadExpiredKeys() throws Exception {

		SoftAssert soft = new SoftAssert();
		CertificatePage page = new CertificatePage(driver);
		page.getSidebar().goToPage(PAGES.KEYSTORE);

		log.info("Try uploading expired truststore file");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/expired.jks");

		page.uploadFile(path, "test123");
		log.info(page.getAlertArea().getAlertMessage() + " Message after upload event");

		log.info("Check expired key is listed in red color");
		soft.assertTrue(page.grid().getRowElement(0).getAttribute("class").contains("highlighted-row"), "Expired certificate is highlighted");


		soft.assertAll();
	}

	/*       EDELIVERY-10250 - KST-8 - Download keystore */
	@Test(description = "KST-8", groups = {"multiTenancy", "singleTenancy"})
	public void downloadKeystore() throws Exception {

		SoftAssert soft = new SoftAssert();
		CertificatePage page = new CertificatePage(driver);
		page.getSidebar().goToPage(PAGES.KEYSTORE);

		page.pressDownloadAndSaveFile(data.downloadFolderPath());
		soft.assertTrue(DFileUtils.isFileDownloaded(data.downloadFolderPath()), "File is downloaded");
		String fileName = DFileUtils.getCompleteFileName(data.downloadFolderPath());

		soft.assertTrue(fileName.contains("KeyStore.jks"), "KeyStore.jks file is downloaded");

		soft.assertAll();

	}


	/*     EDELIVERY-10245 - KST-5 - Upload invalid file */
	@Test(description = "KST-5", groups = {"multiTenancy", "singleTenancy"})
	public void uploadInvalidFile() throws Exception {

		SoftAssert soft = new SoftAssert();
		CertificatePage page = new CertificatePage(driver);
		page.getSidebar().goToPage(PAGES.KEYSTORE);


		log.info("Try uploading correct truststore file");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/rnd.xlsx");

		page.uploadFile(path, "test123");
		log.info(page.getAlertArea().getAlertMessage() + " Message after upload event");

		log.info("Validate presence of error Alert message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message shown");

		soft.assertAll();

	}
}
