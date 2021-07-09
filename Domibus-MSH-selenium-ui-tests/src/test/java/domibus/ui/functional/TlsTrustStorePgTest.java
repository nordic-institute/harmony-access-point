package domibus.ui.functional;

import io.qameta.allure.*;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.tlsTrustStore.TlsTrustStorePage;
import utils.DFileUtils;

import java.util.HashMap;
import java.util.List;

/**
* @author Rupam
* @version 5.0
*/

@Epic("TLS Truststore")
@Feature("Functional")
public class TlsTrustStorePgTest extends SeleniumTest {

/* This method will verify upload certificate functionality when no tls config is done */
/*  TLS-3-Check upload certificate feature with no tls configuration  */
@Description("TLS-3-Check upload certificate feature with no tls configuration")
@Link(name = "EDELIVERY-8185", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8185")
@AllureId("TLS-3")
@Test(description = "TLS-3", groups = {"multiTenancy", "singleTenancy", "NoTlsConfig"})
public void uploadCertificate() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("Login into application and navigate to TlsTruststore page");
log.info("Login into application and navigate to TlsTruststore page");
selectRandomDomain();

TlsTrustStorePage page = new TlsTrustStorePage(driver);
page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
page.getAlertArea().closeButton.click();
String certFileName = "gateway_truststore.jks";
String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");

String currentDomain = page.getDomainFromTitle();
if (!data.isMultiDomain()) {
currentDomain = "default";
}

page.uploadAddCert(path, "test123", page.getUploadButton(), page.getPassInputField());

soft.assertTrue(page.getAlertArea().getAlertMessage().equals(String.format(DMessages.TlsTruststore.TLS_TRUSTSTORE_UPLOAD, certFileName, currentDomain)
), "Both error messages are same");
soft.assertAll();

}

/* This method will verify upload certificate functionality when tls config is done */
/*  TLS-4-Check upload certificate when tls configuration is already done  */
@Description("TLS-4-Check upload certificate when tls configuration is already done")
@Link(name = "EDELIVERY-8187", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8187")
@AllureId("TLS-4")
@Test(description = "TLS-4", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
public void uploadCert() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("Login into application and navigate to TlsTruststore page");
log.info("Login into application and navigate to TlsTruststore page");
selectRandomDomain();

TlsTrustStorePage page = new TlsTrustStorePage(driver);
page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");

page.uploadAddCert(path, "test123", page.getUploadButton(), page.getPassInputField());

soft.assertTrue(page.getAlertArea().getAlertMessage().equals(DMessages.TlsTruststore.TLS_TRUSTSTORE_SUCCESS_UPLOAD), "Success message is shown");
soft.assertAll();

}

/* This method will verify remove certificate functionality when tls config is done */
/*  TLS-5-Check remove certificate feature from Tls Truststore  */
@Description("TLS-5-Check remove certificate feature from Tls Truststore")
@Link(name = "EDELIVERY-8188", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8188")
@AllureId("TLS-5")
@Test(description = "TLS-5", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
public void removeCert() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("Login into application and navigate to TlsTruststore page");
log.info("Login into application and navigate to TlsTruststore page");
selectRandomDomain();

TlsTrustStorePage page = new TlsTrustStorePage(driver);
page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);

String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");
page.uploadAddCert(path, "test123", page.getUploadButton(), page.getPassInputField());
page.grid().waitForRowsToLoad();

int beforeCount = page.grid().getPagination().getTotalItems();
page.grid().selectRow(0);
String selectedAlias = page.grid().getRowInfo(0).get("Name");
soft.assertTrue(page.getRemoveCertButton().isEnabled(), "Remove Button is enabled");
page.getRemoveCertButton().click();
soft.assertTrue(page.getAlertArea().getAlertMessage().equals(String.format(DMessages.TlsTruststore.TLS_TRUSTSTORE_REMOVE_CERT, selectedAlias)
), "Both error messages are same");

page.grid().waitForRowsToLoad();
int afterCount = page.grid().getPagination().getTotalItems();
soft.assertTrue(beforeCount > afterCount, "Now grid has less certificate than before");

soft.assertAll();

}

/* This method will verify remove all certificate functionality when tls config is done */
/*  TLS-6-Remove all certificates  */
@Description("TLS-6-Remove all certificates")
@Link(name = "EDELIVERY-8189", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8189")
@AllureId("TLS-6")
@Test(description = "TLS-6", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
public void removeAllCert() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("Login into application and navigate to TlsTruststore page");
log.info("Login into application and navigate to TlsTruststore page");
selectRandomDomain();

TlsTrustStorePage page = new TlsTrustStorePage(driver);
page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");
page.uploadAddCert(path, "test123", page.getUploadButton(), page.getPassInputField());
page.grid().waitForRowsToLoad();


int beforeCount = page.grid().getPagination().getTotalItems();
for (int i = beforeCount - 1; i >= 0; i--) {
page.grid().selectRow(i);
String selectedAlias = page.grid().getRowInfo(i).get("Name");
soft.assertTrue(page.getRemoveCertButton().isEnabled(), "Remove Button is enabled");
page.getRemoveCertButton().click();
soft.assertTrue(page.getAlertArea().getAlertMessage().equals(String.format(DMessages.TlsTruststore.TLS_TRUSTSTORE_REMOVE_CERT, selectedAlias)
), "Both error messages are same");
page.getAlertArea().closeButton.click();

}

soft.assertTrue(page.grid().getPagination().getTotalItems() == 0, "Grid is empty now");
soft.assertTrue(page.getUploadButton().isEnabled(), "Upload button is enabled");
soft.assertTrue(page.getAddCertButton().isDisabled(), "Add Certificate button is disabled");
soft.assertTrue(page.getDownloadButton().isDisabled(), "Download button is disabled");
soft.assertTrue(page.getRemoveCertButton().isDisabled(), "Remove certificate button is disabled");
soft.assertAll();
}

/* This method will verify upload certificate functionality after all certificate removal */
/*  TLS-7-Upload certificate when all existing certificates are removed earlier  */
@Description("TLS-7-Upload certificate when all existing certificates are removed earlier")
@Link(name = "EDELIVERY-8190", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8190")
@AllureId("TLS-7")
@Test(description = "TLS-7", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
public void uploadCertfterRemove() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("Login into application and navigate to TlsTruststore page");
log.info("Login into application and navigate to TlsTruststore page");
selectRandomDomain();
TlsTrustStorePage page = new TlsTrustStorePage(driver);
page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
int gridRowDataCount = page.grid().gridRows.size();
if (gridRowDataCount > 0) {
int beforeCount = page.grid().getPagination().getTotalItems();
for (int i = beforeCount - 1; i >= 0; i--) {
page.grid().selectRow(i);
page.getRemoveCertButton().click();
}
}
soft.assertTrue(page.grid().getPagination().getTotalItems() == 0, "grid is empty now");
String path = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore_noRecCert.jks");
page.uploadAddCert(path, "test123", page.getUploadButton(), page.getPassInputField());
page.grid().waitForRowsToLoad();

soft.assertTrue(page.getAlertArea().getAlertMessage().equals(DMessages.TlsTruststore.TLS_TRUSTSTORE_SUCCESS_UPLOAD));
soft.assertTrue(page.grid().getPagination().getTotalItems() > 0, "Certificate data is present in grid");
soft.assertAll();

}

/*This test case will verify wrong file upload feature */
/*  TLS-21-Upload wrong format file using upload button  */
@Description("TLS-21-Upload wrong format file using upload button")
@Link(name = "EDELIVERY-8257", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8257")
@AllureId("TLS-21")
@Test(description = "TLS-21", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
public void wrongFileUpload() throws Exception {
SoftAssert soft = new SoftAssert();
TlsTrustStorePage page = new TlsTrustStorePage(driver);
page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
String certFileName = "Edelivery-blue.xml";
String path = "./src/main/resources/pmodes/" + certFileName;
String absolutePath = DFileUtils.getAbsolutePath(path);

page.uploadAddCert(absolutePath, "test123", page.getUploadButton(), page.getPassInputField());

soft.assertTrue(page.getAlertArea().getAlertMessage().equals(
String.format(DMessages.TlsTruststore.TLS_TRUSTSTORE_WRONGFILE_UPLOAD, certFileName)), "Error message is shown");

soft.assertAll();
}

/* This will verify wrong file add feature */
/*  TLS-22-Try to Add file with wrong format using Add certificate button  */
@Description("TLS-22-Try to Add file with wrong format using Add certificate button")
@Link(name = "EDELIVERY-8258", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8258")
@AllureId("TLS-22")
@Test(description = "TLS-22", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
public void wrongFileAdd() throws Exception {
SoftAssert soft = new SoftAssert();
TlsTrustStorePage page = new TlsTrustStorePage(driver);
page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
String certFileName = "Edelivery-blue.xml";
String path = "./src/main/resources/pmodes/" + certFileName;
String absolutePath = DFileUtils.getAbsolutePath(path);

page.uploadAddCert(absolutePath, "test123", page.getAddCertButton(), page.getAliasInputField());

soft.assertTrue(page.getAlertArea().getAlertMessage().equals(String.format(DMessages.TlsTruststore.TLS_TRUSTSTOE_WRONGFILE_ADD, certFileName)), "Correct error message is shown");
soft.assertAll();
}

/* This test method will verify upload of expired certificate*/
/*  TLS-20- Check expired Jks file upload  */
@Description("TLS-20- Check expired Jks file upload")
@Link(name = "EDELIVERY-8214", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8214")
@AllureId("TLS-20")
@Test(description = "TLS-20", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
public void uploadExpired() throws Exception {
SoftAssert soft = new SoftAssert();
TlsTrustStorePage page = new TlsTrustStorePage(driver);
page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
String certFileName = "expired.jks";
String path = "./src/main/resources/truststore/" + certFileName;
String absolutePath = DFileUtils.getAbsolutePath(path);

page.uploadAddCert(absolutePath, "test123", page.getUploadButton(), page.getPassInputField());
soft.assertTrue(page.getAlertArea().getAlertMessage().equals(DMessages.TlsTruststore.TLS_TRUSTSTORE_SUCCESS_UPLOAD), "Success message is shown");
soft.assertAll();

}

/* This test method will verify upload new certificate through upload button when old certificate exist*/
/*  TLS-10-Upload certificate when page has already some certificate present  */
@Description("TLS-10-Upload certificate when page has already some certificate present")
@Link(name = "EDELIVERY-8194", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8194")
@AllureId("TLS-10")
@Test(description = "TLS-10", groups = {"multiTenancy", "singleTenancy", "TlsConfig"})
public void uploadNewCert() throws Exception {
SoftAssert soft = new SoftAssert();
TlsTrustStorePage page = new TlsTrustStorePage(driver);
page.getSidebar().goToPage(PAGES.TRUSTSTORES_TLS);
List<HashMap<String, String>> beforeUploadData = page.grid().getAllRowInfo();
String certFileName = "expired.jks";
String path = "./src/main/resources/truststore/" + certFileName;
String absolutePath = DFileUtils.getAbsolutePath(path);

page.uploadAddCert(absolutePath, "test123", page.getUploadButton(), page.getPassInputField());
List<HashMap<String, String>> afterUploadData = page.grid().getAllRowInfo();
soft.assertFalse(beforeUploadData.equals(afterUploadData),"Before and after data are different");
soft.assertAll();

}


}
