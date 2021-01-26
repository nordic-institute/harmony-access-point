package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.truststore.TruststorePage;
import utils.DFileUtils;

import java.io.File;

public class TrustoreTest extends SeleniumTest {
	
	
	/*  This method wil verify Presence of error in case of random/file with wrong format */
	@Test(description = "TRST-4", groups = {"multiTenancy", "singleTenancy"})
	public void uploadRandomFile() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login into application and navigate to Truststore page");
		
		selectRandomDomain();
		
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		log.info("Try to upload random file ");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/rnd.xlsx");
		
		
		page.uploadFile(path, "test123", soft);
		log.info(page.getAlertArea().getAlertMessage());
		log.info("Validate presence of Error in alert message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("Error"), DMessages.TRUSTSTORE_REPLACE_ERROR);
		
		soft.assertAll();
	}
	
	/*This method will verify successful upload of valid truststore  */
	@Test(description = "TRST-5", groups = {"multiTenancy", "singleTenancy"})
	public void uploadValidFile() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login into application and navigate to Truststore page");
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		log.info("Try uploading correct truststore file");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");
		
		page.uploadFile(path, "test123", soft);
		log.info(page.getAlertArea().getAlertMessage(), " Message after upload event");
		
		log.info("Validate presence of successfully keyword in Alert message for default domain");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);
		
		soft.assertAll();
		
	}
	
	/*  This method will verify successful upload of truststore with no password for alias  */
	@Test(description = "TRST-12", groups = {"multiTenancy", "singleTenancy"})
	public void uploadJksWithNoAliasPass() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login into application and navigate to truststore page");
		
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		log.info("Try uploading truststore with no password for alias");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/noAliasPass.jks");
		
		page.uploadFile(path, "test123", soft);
		
		log.info(page.getAlertArea().getAlertMessage(), " Message after upload event");
		
		log.info("Validate presence of successfully keyword in Alert message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);
		
		soft.assertAll();
	}
	
	/*  This method will verify successful upload of truststore with password protected alias  */
	@Test(description = "TRST-13", groups = {"multiTenancy", "singleTenancy"})
	public void uploadJksWithPassProAlias() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login into application and navigate to Truststore page ");

		TruststorePage page = new TruststorePage(driver);
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		log.info("Try uploading truststore with password protected alias");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/PassProAlias.jks");
		
		page.uploadFile(path, "test123", soft);
		
		log.info(page.getAlertArea().getAlertMessage(), " Message after upload event");
		log.info("Validate presence of successfully keyword in alert message");
		
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("successfully"), DMessages.TRUSTSTORE_REPLACE_SUCCESS);
		
		soft.assertAll();
	}
	
	/*  This method will verify no uploading in case of valid file but without password  */
	@Test(description = "TRST-14", groups = {"multiTenancy", "singleTenancy"})
	public void uploadFileWithoutPassword() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login into application and navigate to truststore");

		TruststorePage page = new TruststorePage(driver);
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORE);
		log.info("try uploading valid file without any password");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");
		
		page.uploadFile(path, "", soft);
		page.refreshPage();
		page.waitForPageTitle();
		
		soft.assertAll();
		
		
	}
	
	/*  This method will verify no uploading in case of expired truststore certificate  */
	@Test(description = "TRST-15", groups = {"multiTenancy", "singleTenancy"} )
	public void expiredCertificate() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login into application and navigate to truststore");
		
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		log.info("Try uploading expired certificate");
		String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/expired.jks");
		
		page.uploadFile(path, "test123", soft);
		log.info(page.getAlertArea().getAlertMessage());

		soft.assertFalse(page.getAlertArea().isError(), "Success message is shown");

		soft.assertAll();
		
	}
	
	/*  This method will verify successful upload of jks file with special char àøýßĉæãäħ */
	@Test(description = "TRST-18", groups = {"multiTenancy", "singleTenancy"})
	public void uploadTruststoreWithSpclChar() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Login into application and navigate to Truststore page");
		TruststorePage page = new TruststorePage(driver);
		page.getSidebar().goToPage(PAGES.TRUSTSTORE);
		
		String filePath = DFileUtils.getAbsolutePath("./src/main/resources/truststore/àøýßĉæãäħ.jks");
		
		log.info("Try to upload jks file for single tenancy");
		page.uploadFile(filePath, "test123", soft);
		log.info("Validate success message");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.TRUSTSTORE_REPLACE_SUCCESS);
		page.getAlertArea().closeButton.click();
		
		soft.assertAll();
	}
	
	
	
	
	
	
}
