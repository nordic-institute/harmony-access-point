package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.PAGES;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.truststore.TruststorePage;
import utils.BaseTest;
import utils.DFileUtils;

/**
 * @author Rupam
 **/
public class TruststorePgTest extends BaseTest {

    /*  This method wil verify Presence of error in case of random/file with wrong format */
    @Test(description = "TRST-4", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
    public void uploadRandomFile() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Login into application and navigate to Truststore page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORE);
        DomibusPage page = new DomibusPage(driver);
        TruststorePage tPage = new TruststorePage(driver);

        log.info("Try to upload random file ");
        String path = DFileUtils.getAbsolutePath("truststore/TestCase_Domibus-4.1.xlsx");


        tPage.uploadFile(path, "test123", soft);
        log.info(tPage.getAlertArea().getAlertMessage());
        log.info("Validate presence of Error in alert message");
        soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("Error"), "There is an error while uploading truststore.");

        if (data.isMultiDomain()) {
            page.getDomainSelector().selectOptionByText(getNonDefaultDomain());
            page.waitForTitle();

            log.info("Try to upload random file for domain " + getNonDefaultDomain());

            tPage.uploadFile(path, "test123", soft);
            log.info(tPage.getAlertArea().getAlertMessage());
            log.info("Validate presence of Error in alert message for domain" + getNonDefaultDomain());
            soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("Error"), "There is an error while uploading truststore.");

        }

        soft.assertAll();
    }

    /*This method will verify successful upload of valid truststore  */
    @Test(description = "TRST-5", groups = {"multiTenancy", "singleTenancy"})
    public void uploadValidFile() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Login into application and navigate to Truststore page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORE);
        TruststorePage tPage = new TruststorePage(driver);
        DomibusPage page = new DomibusPage(driver);
        log.info("Try uploading correct truststore file");
        String path = DFileUtils.getAbsolutePath("truststore/gateway_truststore.jks");

        tPage.uploadFile(path, "test123", soft);
        log.info(tPage.getAlertArea().getAlertMessage(), " Message after upload event");

        log.info("Validate presence of successfully keyword in Alert message for default domain");
        soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("successfully"), "Truststore file has been successfully replaced.");

        if (data.isMultiDomain()) {
            page.getDomainSelector().selectOptionByText(getNonDefaultDomain());
            page.waitForTitle();
            String pathForOtherDomain = DFileUtils.getAbsolutePath("truststore/gateway_truststoreDomain1.jks");

            log.info("Try uploading correct truststore file for other domain : " + getNonDefaultDomain());
            tPage.uploadFile(pathForOtherDomain, "test123", soft);
            log.info(tPage.getAlertArea().getAlertMessage(), " Message after upload event");

            log.info("Validate presence of successfully keyword in Alert message for domain :" + getNonDefaultDomain());
            soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("successfully"), "Truststore file has been successfully replaced.");
        }

        soft.assertAll();

    }

    /*  This method will verify successful upload of truststore with no password for alias  */
    @Test(description = "TRST-12", groups = {"multiTenancy", "singleTenancy"})
    public void uploadJksWithNoAliasPass() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Login into application and navigate to truststore page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORE);
        TruststorePage tPage = new TruststorePage(driver);
        DomibusPage page = new DomibusPage(driver);
        log.info("Try uploading truststore with no password for alias");
        String path = DFileUtils.getAbsolutePath("truststore/noAliasPass.jks");

        tPage.uploadFile(path, "test123", soft);

        log.info(tPage.getAlertArea().getAlertMessage(), " Message after upload event");

        log.info("Validate presence of successfully keyword in Alert message");
        soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("successfully"), "Truststore file has been successfully replaced.");

        if (data.isMultiDomain()) {
            page.getDomainSelector().selectOptionByText(getNonDefaultDomain());
            page.waitForTitle();
            log.info("Try uploading truststore with no password for alias for domain: " + getNonDefaultDomain());
            String pathForOtherDomain = DFileUtils.getAbsolutePath("truststore/noAliasPassDomain1.jks");

            tPage.uploadFile(pathForOtherDomain, "test123", soft);

            log.info(tPage.getAlertArea().getAlertMessage(), " Message after upload event");

            log.info("Validate presence of successfully keyword in Alert message");
            soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("successfully"), "Truststore file has been successfully replaced.");
        }
        soft.assertAll();
    }

    /*  This method will verify successful upload of truststore with password protected alias  */
    @Test(description = "TRST-13", groups = {"multiTenancy", "singleTenancy"})
    public void uploadJksWithPassProAlias() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Login into application and navigate to Truststore page ");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORE);
        DomibusPage page = new DomibusPage(driver);
        TruststorePage tPage = new TruststorePage(driver);

        log.info("Try uploading truststore with password protected alias");
        String path = DFileUtils.getAbsolutePath("truststore/PassProAlias.jks");

        tPage.uploadFile(path, "test123", soft);

        log.info(tPage.getAlertArea().getAlertMessage(), " Message after upload event");
        log.info("Validate presence of successfully keyword in alert message");

        soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("successfully"), "Truststore file has been successfully replaced.");

        if (data.isMultiDomain()) {
            page.getDomainSelector().selectOptionByText(getNonDefaultDomain());
            page.waitForTitle();
            log.info("Try uploading truststore with password protected alias for domain :" + getNonDefaultDomain());
            String pathForOtherDomain = DFileUtils.getAbsolutePath("truststore/PassProAliasDomain1.jks");

            tPage.uploadFile(pathForOtherDomain, "test123", soft);

            log.info(tPage.getAlertArea().getAlertMessage(), " Message after upload event");
            log.info("Validate presence of successfully keyword in alert message");

            soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("successfully"), "Truststore file has been successfully replaced.");

        }

        soft.assertAll();
    }

    /*  This method will verify no uploading in case of valid file but without password  */
    @Test(description = "TRST-14", groups = {"multiTenancy", "singleTenancy"})
    public void uploadFileWithoutPassword() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Login into application and navigate to truststore");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORE);
        DomibusPage page = new DomibusPage(driver);
        TruststorePage tPage = new TruststorePage(driver);
        log.info("try uploading valid file without any password");
        String path = DFileUtils.getAbsolutePath("truststore/gateway_truststore.jks");

        tPage.uploadFile(path, "", soft);
        page.refreshPage();
        page.waitForTitle();
        if (data.isMultiDomain()) {
            page.getDomainSelector().selectOptionByText(getNonDefaultDomain());
            page.waitForTitle();
            log.info("try uploading valid file without any password for domain :" + getNonDefaultDomain());
            String pathForOtherDomain = DFileUtils.getAbsolutePath("truststore/gateway_truststoreDomain1.jks");

            tPage.uploadFile(pathForOtherDomain, "", soft);
        }
        soft.assertAll();


    }

    /*  This method will verify no uploading in case of expired truststore certificate  */
    @Test(description = "TRST-15", groups = {"multiTenancy", "singleTenancy"})
    public void expiredCertificate() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("Login into application and navigate to truststore");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.TRUSTSTORE);
        DomibusPage page = new DomibusPage(driver);
        TruststorePage tPage = new TruststorePage(driver);
        log.info("Try uploading expired certificate");
        String path = DFileUtils.getAbsolutePath("truststore/expired.jks");

        tPage.uploadFile(path, "test123", soft);
        log.info(tPage.getAlertArea().getAlertMessage());
        soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("Error"), "Error while uploading expired certificate");


        if (data.isMultiDomain()) {
            page.getDomainSelector().selectOptionByText(getNonDefaultDomain());
            log.info("Try uploading expired certificate for domain :" + getNonDefaultDomain());
            String pathForOtherDomain = DFileUtils.getAbsolutePath("truststore/expiredDomain1.jks");
            tPage.uploadFile(pathForOtherDomain, "test123", soft);

            log.info(tPage.getAlertArea().getAlertMessage());
            log.info("Validate  presence of Error in alert message ");
            soft.assertTrue(tPage.getAlertArea().getAlertMessage().contains("Error"), "Error while uploading expired certificate");

        }
        soft.assertAll();

    }

}
