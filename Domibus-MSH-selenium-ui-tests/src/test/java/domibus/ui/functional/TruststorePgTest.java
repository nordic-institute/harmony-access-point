package domibus.ui.functional;

import io.qameta.allure.*;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.truststore.TruststorePage;
import utils.DFileUtils;


@Epic("Domibus Truststore")
@Feature("Functional")
public class TruststorePgTest extends SeleniumTest {


	/*  This method wil verify Presence of error in case of random/file with wrong format */
	/*  TRST-4 - Upload random file  */
	@Description("TRST-4 - Upload random file")
	@Link(name = "EDELIVERY-5160", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5160")
	@AllureId("TRST-4")
	@Test(description = "TRST-4", groups = {"multiTenancy", "singleTenancy"})
	public void uploadRandomFile() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to Truststore page");
		log.info("Login into application and navigate to Truststore page");

		selectRandomDomain();

		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		Allure.step("Try to upload random file ");
		log.info("Try to upload random file ");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/rnd.xlsx");


		page.uploadFile(path, "test123", soft);
		Allure.step(page.getAlertArea().getAlertMessage());
		log.info(page.getAlertArea().getAlertMessage());
		Allure.step("Validate presence of Error in alert message");
		log.info("Validate presence of Error in alert message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("Error"), DMessages.TRUSTSTORE_REPLACE_ERROR);

		soft.assertAll();
	}

	/*This method will verify successful upload of valid truststore  */
	/*  TRST-5 - Upload valid file  */
	@Description("TRST-5 - Upload valid file")
	@Link(name = "EDELIVERY-5161", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5161")
	@AllureId("TRST-5")
	@Test(description = "TRST-5", groups = {"multiTenancy", "singleTenancy"})
	public void uploadValidFile() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to Truststore page");
		log.info("Login into application and navigate to Truststore page");
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		Allure.step("Try uploading correct truststore file");
		log.info("Try uploading correct truststore file");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");

		page.uploadFile(path, "test123", soft);
		Allure.step(page.getAlertArea().getAlertMessage() + "  - Message after upload event");
		log.info(page.getAlertArea().getAlertMessage(), " Message after upload event");

		Allure.step("Validate presence of successfully keyword in Alert message for default domain");
		log.info("Validate presence of successfully keyword in Alert message for default domain");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);

		soft.assertAll();

	}

	/*  This method will verify successful upload of truststore with no password for alias  */
	/*  TRST-12 - Upload  jks files with password but no password for keys  */
	@Description("TRST-12 - Upload  jks files with password but no password for keys")
	@Link(name = "EDELIVERY-5168", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5168")
	@AllureId("TRST-12")
	@Test(description = "TRST-12", groups = {"multiTenancy", "singleTenancy"})
	public void uploadJksWithNoAliasPass() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to truststore page");
		log.info("Login into application and navigate to truststore page");

		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		Allure.step("Try uploading truststore with no password for alias");
		log.info("Try uploading truststore with no password for alias");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/noAliasPass.jks");

		page.uploadFile(path, "test123", soft);

		Allure.step(page.getAlertArea().getAlertMessage() + "  - Message after upload event");
		log.info(page.getAlertArea().getAlertMessage(), " Message after upload event");

		Allure.step("Validate presence of successfully keyword in Alert message");
		log.info("Validate presence of successfully keyword in Alert message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);

		soft.assertAll();
	}

	/*  This method will verify successful upload of truststore with password protected alias  */
	/*  TRST-13 - Upload  jks files with password with password protected aliases  */
	@Description("TRST-13 - Upload  jks files with password with password protected aliases")
	@Link(name = "EDELIVERY-5169", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5169")
	@AllureId("TRST-13")
	@Test(description = "TRST-13", groups = {"multiTenancy", "singleTenancy"})
	public void uploadJksWithPassProAlias() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to Truststore page ");
		log.info("Login into application and navigate to Truststore page ");

		TruststorePage page = new TruststorePage(driver);
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		Allure.step("Try uploading truststore with password protected alias");
		log.info("Try uploading truststore with password protected alias");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/PassProAlias.jks");

		page.uploadFile(path, "test123", soft);

		Allure.step(page.getAlertArea().getAlertMessage() + "  - Message after upload event");
		log.info(page.getAlertArea().getAlertMessage(), " Message after upload event");
		Allure.step("Validate presence of successfully keyword in alert message");
		log.info("Validate presence of successfully keyword in alert message");

		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);

		soft.assertAll();
	}

	/*  This method will verify no uploading in case of valid file but without password  */
	/*  TRST-14 - Upload jks files without passowrd   */
	@Description("TRST-14 - Upload jks files without passowrd ")
	@Link(name = "EDELIVERY-5170", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5170")
	@AllureId("TRST-14")
	@Test(description = "TRST-14", groups = {"multiTenancy", "singleTenancy"})
	public void uploadFileWithoutPassword() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to truststore");
		log.info("Login into application and navigate to truststore");

		TruststorePage page = new TruststorePage(driver);
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);
		Allure.step("try uploading valid file without any password");
		log.info("try uploading valid file without any password");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");

		page.uploadFile(path, "", soft);
		page.refreshPage();
		page.waitForPageTitle();

		soft.assertAll();


	}

	/*  This method will verify no uploading in case of expired truststore certificate  */
	/*  TRST-15 - Upload expired jks files  */
	@Description("TRST-15 - Upload expired jks files")
	@Link(name = "EDELIVERY-5171", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5171")
	@AllureId("TRST-15")
	@Test(description = "TRST-15", groups = {"multiTenancy", "singleTenancy"})
	public void expiredCertificate() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to truststore");
		log.info("Login into application and navigate to truststore");

		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		Allure.step("Try uploading expired certificate");
		log.info("Try uploading expired certificate");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/expired.jks");

		page.uploadFile(path, "test123", soft);
		Allure.step(page.getAlertArea().getAlertMessage());
		log.info(page.getAlertArea().getAlertMessage());

		soft.assertFalse(page.getAlertArea().isError(), "Success message is shown");

		soft.assertAll();

	}

	/*  This method will verify successful upload of jks file with special char àøýßĉæãäħ */
	/*  TRST-18 - Verify upload of certificate on truststore page having some sPecial characters   */
	@Description("TRST-18 - Verify upload of certificate on truststore page having some sPecial characters ")
	@Link(name = "EDELIVERY-6372", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-6372")
	@AllureId("TRST-18")
	@Test(description = "TRST-18", groups = {"multiTenancy", "singleTenancy"})
	public void uploadTruststoreWithSpclChar() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to Truststore page");
		log.info("Login into application and navigate to Truststore page");
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

		String filePath = DFileUtils.getAbsolutePath("./src/main/resources/truststore/àøýßĉæãäħ.jks");

		Allure.step("Try to upload jks file for single tenancy");
		log.info("Try to upload jks file for single tenancy");
		page.uploadFile(filePath, "test123", soft);
		Allure.step("Validate success message");
		log.info("Validate success message");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.TRUSTSTORE_REPLACE_SUCCESS);
		page.getAlertArea().closeButton.click();

		soft.assertAll();
	}


}
