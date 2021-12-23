package domibus.ui.functional;

import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.truststore.TruststorePage;
import utils.DFileUtils;


public class TruststorePgTest extends SeleniumTest {


    /* EDELIVERY-5160 - TRST-4 - Upload random file */
	@Test(description = "TRST-4", groups = {"multiTenancy", "singleTenancy"})
	public void uploadRandomFile() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Login into application and navigate to Truststore page");

		selectRandomDomain();

		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		log.info("Try to upload random file ");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/rnd.xlsx");


		page.uploadFile(path, "test123", soft);
		log.info(page.getAlertArea().getAlertMessage());
		log.info("Validate presence of Error in alert message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("Error"), DMessages.TRUSTSTORE_REPLACE_ERROR);

		soft.assertAll();
	}

    /* EDELIVERY-5161 - TRST-5 - Upload valid file */
	@Test(description = "TRST-5", groups = {"multiTenancy", "singleTenancy"})
	public void uploadValidFile() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Login into application and navigate to Truststore page");
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		log.info("Try uploading correct truststore file");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");

		page.uploadFile(path, "test123", soft);
		log.info(page.getAlertArea().getAlertMessage() + " Message after upload event");

		log.info("Validate presence of successfully keyword in Alert message for default domain");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);

		soft.assertAll();

	}

    /* EDELIVERY-5168 - TRST-12 - Upload  jks files with password but no password for keys */
	@Test(description = "TRST-12", groups = {"multiTenancy", "singleTenancy"})
	public void uploadJksWithNoAliasPass() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Login into application and navigate to truststore page");

		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		log.info("Try uploading truststore with no password for alias");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/noAliasPass.jks");

		page.uploadFile(path, "test123", soft);

		log.info(page.getAlertArea().getAlertMessage() + " Message after upload event");

		log.info("Validate presence of successfully keyword in Alert message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);

		soft.assertAll();
	}

    /* EDELIVERY-5169 - TRST-13 - Upload  jks files with password with password protected aliases */
	@Test(description = "TRST-13", groups = {"multiTenancy", "singleTenancy"})
	public void uploadJksWithPassProAlias() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Login into application and navigate to Truststore page ");

		TruststorePage page = new TruststorePage(driver);
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		log.info("Try uploading truststore with password protected alias");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/PassProAlias.jks");

		page.uploadFile(path, "test123", soft);

		log.info(page.getAlertArea().getAlertMessage() + " Message after upload event");
		log.info("Validate presence of successfully keyword in alert message");

		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);

		soft.assertAll();
	}

    /* EDELIVERY-5170 - TRST-14 - Upload jks files without passowrd  */
	@Test(description = "TRST-14", groups = {"multiTenancy", "singleTenancy"})
	public void uploadFileWithoutPassword() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Login into application and navigate to truststore");

		TruststorePage page = new TruststorePage(driver);
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);
		log.info("try uploading valid file without any password");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");

		page.uploadFile(path, "", soft);
		page.refreshPage();
		page.waitForPageTitle();

		soft.assertAll();


	}

    /* EDELIVERY-5171 - TRST-15 - Upload expired jks files */
	@Test(description = "TRST-15", groups = {"multiTenancy", "singleTenancy"})
	public void expiredCertificate() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Login into application and navigate to truststore");

		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		log.info("Try uploading expired certificate");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/expired.jks");

		page.uploadFile(path, "test123", soft);
		log.info(page.getAlertArea().getAlertMessage());

		soft.assertFalse(page.getAlertArea().isError(), "Success message is shown");

		soft.assertAll();

	}

    /* EDELIVERY-6372 - TRST-18 - Verify upload of certificate on truststore page having some sPecial characters  */
	@Test(description = "TRST-18", groups = {"multiTenancy", "singleTenancy"})
	public void uploadTruststoreWithSpclChar() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Login into application and navigate to Truststore page");
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		String filePath = DFileUtils.getAbsolutePath("./src/main/resources/truststore/àøýßĉæãäħ.jks");

		log.info("Try to upload jks file for single tenancy");
		page.uploadFile(filePath, "test123", soft);
		log.info("Validate success message");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.TRUSTSTORE_REPLACE_SUCCESS);
		page.getAlertArea().closeButton.click();

		soft.assertAll();
	}


}
