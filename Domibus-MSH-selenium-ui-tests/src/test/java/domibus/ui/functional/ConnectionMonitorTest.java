package domibus.ui.functional;

import io.qameta.allure.*;
import ddsl.dobjects.DWait;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.connectionMon.ConMonGrid;
import pages.connectionMon.ConnectionMonitoringPage;
import pages.connectionMon.TestMessDetailsModal;
import pages.pmode.parties.PModePartiesPage;
import pages.truststore.TruststorePage;
import utils.DFileUtils;
import utils.PModeXMLUtils;

import java.io.File;



/**
* @author Catalin Comanici
* @version 4.1
*/


@Epic("")
@Feature("")
public class ConnectionMonitorTest extends SeleniumTest {
private static String receiverEndPoint = "http://localhost:8181/domibus/services/msh";

/* CM-2 - Login as super admin and open Connections Monitoring page */
/*  CM-2 - Login as system admin and open Connection Monitoring page  */
@Description("CM-2 - Login as system admin and open Connection Monitoring page")
@Link(name = "EDELIVERY-5303", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5303")
@AllureId("CM-2")
@Test(description = "CM-2", groups = {"multiTenancy", "singleTenancy"})
public void openWindow() throws Exception {
SoftAssert soft = new SoftAssert();

ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

if (!rest.pmode().isPmodeUploaded(null)) {
Allure.step("checking error message when no pmode is uploaded");
log.info("checking error message when no pmode is uploaded");
soft.assertTrue(page.invalidConfigurationState(), "Page shows invalid configuration state");
}

Allure.step("checking page ..");
log.info("checking page ..");
soft.assertTrue(page.isLoaded(), "Page shows all desired elements");
soft.assertAll();
}

/* CM-1 - Login as system admin and open Connection Monitoring page without proper Pmode */
/*  CM-1 - Login as system admin and open Connection Monitoring page without proper Pmode  */
@Description("CM-1 - Login as system admin and open Connection Monitoring page without proper Pmode")
@Link(name = "EDELIVERY-7240", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7240")
@AllureId("CM-1")
@Test(description = "CM-1", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
public void openWindowNoPmode() throws Exception {
SoftAssert soft = new SoftAssert();

ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

if (!rest.pmode().isPmodeUploaded(null)) {
Allure.step("checking error message when no pmode is uploaded");
log.info("checking error message when no pmode is uploaded");
soft.assertTrue(page.invalidConfigurationState(), "Page shows invalid configuration state");
} else {
throw new SkipException("Pmode already uploaded, test could not be executed");
}
soft.assertAll();
}

/* CM-3 - Open details view for party that has never been tested or monitored */
/*  CM-3 - Open details view for party that has never been tested or monitored  */
@Description("CM-3 - Open details view for party that has never been tested or monitored")
@Link(name = "EDELIVERY-7241", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7241")
@AllureId("CM-3")
@Test(description = "CM-3", groups = {"multiTenancy", "singleTenancy"})
public void partyNotTested() throws Exception {
SoftAssert soft = new SoftAssert();

ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
rest.pmode().uploadPMode("pmodes/selfSending8080.xml", page.getDomainFromTitle());

page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

int size = page.grid().getPagination().getTotalItems();
for (int i = 0; i < size; i++) {

if (page.grid().connectionStatusIcons.get(i).getText().equals("indeterminate_check_box")) {
String partyName = page.grid().getRowSpecificColumnVal(i, "Party");
Allure.step("party" + partyName);
log.info("party" + partyName);

page.grid().getActionButton("Details", i).click();
TestMessDetailsModal modalTest = new TestMessDetailsModal(driver);

soft.assertTrue(modalTest.getTestbutton().isEnabled(), "Test button is enabled");
soft.assertFalse(modalTest.getUpdateBtn().isEnabled(), "Update button is disabled");
String expectedError =
String.format("Error retrieving Last Sent Test Message for %s [DOM_001]:No User message found for party [%s]", partyName, partyName);
soft.assertEquals(expectedError, page.getAlertArea().getAlertMessage(), "Correct error message is shown");
Allure.step("test" + modalTest.getSentMessInfo().get("party"));
log.info("test" + modalTest.getSentMessInfo().get("party"));

soft.assertTrue(modalTest.isMessInfoPresent("send"), "Sent Message info field have no data");
soft.assertTrue(modalTest.isMessInfoPresent("receive"), "Received message info fields have no data");
soft.assertTrue(modalTest.getCloseBtn().isEnabled(), "Enabled close button is shown");
modalTest.getCloseBtn().click();
page.grid().waitForRowsToLoad();
}


}

soft.assertAll();
}

/* CM-4 - Open details view for party that has never been tested or monitored and push Test button */
/*  CM-4 - Open details view for party that has never been tested or monitored and push Test button  */
@Description("CM-4 - Open details view for party that has never been tested or monitored and push Test button")
@Link(name = "EDELIVERY-7242", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7242")
@AllureId("CM-4")
@Test(description = "CM-4", groups = {"multiTenancy", "singleTenancy"})
public void sendTestMsg() throws Exception {
SoftAssert soft = new SoftAssert();
ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

ConMonGrid grid = page.grid();

if (grid.connectionStatusIcons.get(0).getText().equals("indeterminate_check_box")
|| grid.connectionStatusIcons.get(0).getText().equals("error")) {

String partyName = grid.getRowSpecificColumnVal(0, "Party");
Allure.step("party " + partyName);
log.info("party " + partyName);
TestMessDetailsModal modalTest = new TestMessDetailsModal(driver);
grid.getActionButton("Details", 0).click();

page.getAlertArea().closeButton.click();
modalTest.getTestbutton().click();

page.getAlertArea().isShown();
soft.assertFalse(modalTest.isMessInfoPresent("send"), "Sent message Info fields have data present");

if (page.getAlertArea().isError()) {
soft.assertTrue(modalTest.isMessInfoPresent("receive"), "Received info fields are blank");
} else {
soft.assertFalse(modalTest.isMessInfoPresent("receive"), "Received info fields are not blank");
}
modalTest.getCloseBtn().click();
}
soft.assertAll();
}

/* CM-5 - Open details view for party and push Update button */
/*  CM-5 - Open details view for party and push Update button  */
@Description("CM-5 - Open details view for party and push Update button")
@Link(name = "EDELIVERY-7243", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7243")
@AllureId("CM-5")
@Test(description = "CM-5", groups = {"multiTenancy", "singleTenancy"})
public void checkUpdateFeature() throws Exception {
SoftAssert soft = new SoftAssert();
ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);

page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

int noOfParties = rest.connMonitor().getConnectionMonitoringParties(page.getDomainFromTitle()).length();

if (noOfParties > 0) {

TestMessDetailsModal modalTest = new TestMessDetailsModal(driver);

getAlrtForTestMsg(page, 0, "Details", modalTest);
if (page.getAlertArea().isShown()) {
soft.assertTrue(modalTest.isMessInfoPresent("receive"), "Response is not received for test message");
} else {
soft.assertFalse(modalTest.isMessInfoPresent("receive"), "Test Message is sent successfully");
}
modalTest.getCloseBtn().click();
}

}

/*CM-6 - Push Refresh button */
/*  CM-6 - Push Refresh button   */
@Description("CM-6 - Push Refresh button ")
@Link(name = "EDELIVERY-7244", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7244")
@AllureId("CM-6")
@Test(description = "CM-6", groups = {"multiTenancy", "singleTenancy"})
public void checkRefreshFeature() throws Exception {
SoftAssert soft = new SoftAssert();
ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);

rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", page.getDomainFromTitle());

page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

page.grid().getActionButton("Send", 0).click();
new DWait(driver).forXMillis(100);
page.grid().getActionButton("Refresh", 0).click();
new DWait(driver).forXMillis(100);

String afterSentData = page.grid().getSendRecStatus("Send", 0);

soft.assertTrue(afterSentData.contains("a few seconds ago"), "After sent data contains time difference in seconds");

soft.assertAll();


}

/*CM-7 - Push Send button */
/*  CM-7 - Push Send button   */
@Description("CM-7 - Push Send button ")
@Link(name = "EDELIVERY-7245", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7245")
@AllureId("CM-7")
@Test(description = "CM-7", groups = {"multiTenancy", "singleTenancy"})
public void checkSendFeature() throws Exception {
SoftAssert soft = new SoftAssert();
ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
rest.pmode().uploadPMode("pmodes/selfSending8080.xml", page.getDomainFromTitle());

page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);


page.grid().getActionButton("Send", 0).click();
new DWait(driver).forXMillis(1000);
page.grid().getActionButton("Refresh", 0).click();
new DWait(driver).forXMillis(1000);

String afterSentData = page.grid().sentRecvStatusDetail.get(0).getText();
soft.assertTrue(afterSentData.contains("a few seconds ago"), "After sent data contains time difference in seconds");
String afterConnectionStatus = page.grid().connectionStatusIcons.get(0).getText();
soft.assertTrue(afterConnectionStatus.equals("check_circle"), "Success symbol is shown");
soft.assertTrue(page.grid().connectionStatusIcons.get(0).getAttribute("style").contains("color: green"), "Symbol is shown with green color");
soft.assertAll();
}

/* CM-11 - Remove destination party as a responder in connection testing process */
/*  CM-11 - Remove destination party as a responder in connection testing process  */
@Description("CM-11 - Remove destination party as a responder in connection testing process")
@Link(name = "EDELIVERY-7249", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7249")
@AllureId("CM-11")
@Test(description = "CM-11", groups = {"multiTenancy", "singleTenancy"})
public void currentSystemNotAsInitiator() throws Exception {
SoftAssert soft = new SoftAssert();
ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);

Allure.step("upload pmode");
log.info("upload pmode");
String filepath = "pmodes/NoResponderInitiator.xml";
rest.pmode().uploadPMode(filepath, null);
page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
PModePartiesPage pPage = new PModePartiesPage(driver);
int noOfParties = page.grid().getPagination().getTotalItems();
String partyId = pPage.getNoResIniPartyId(noOfParties);

page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

for (int i = 0; i < noOfParties; i++) {
if (page.grid().getRowSpecificColumnVal(i, "Party").equals(partyId)) {
soft.assertTrue(page.grid().getActionButton("Send", i) == null, "Send icon is present");
soft.assertTrue(page.grid().getActionButton("Refresh", i) == null, "Refresh icon is present");
soft.assertTrue(page.grid().getActionButton("Details", i) == null, "Detail icon is present");
soft.assertTrue(page.grid().getRowSpecificColumnVal(i, "Monitoring").equals("N/A"), "Monitoring is not enabled");
}
}
soft.assertAll();
}

/*CM-12 - Make sure destination party is not responding and test connection*/
/*  CM-12 - Make sure destination party is not responding and test connection  */
@Description("CM-12 - Make sure destination party is not responding and test connection")
@Link(name = "EDELIVERY-7250", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7250")
@AllureId("CM-12")
@Test(description = "CM-12", groups = {"multiTenancy", "singleTenancy"})
public void recPartyInactive() throws Exception {
SoftAssert soft = new SoftAssert();
ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);

Allure.step("upload pmode");
log.info("upload pmode");
String filepath = "pmodes/Edelivery-blue.xml";
rest.pmode().uploadPMode(filepath, null);
File file = new File(getClass().getClassLoader().getResource(filepath).getFile());
PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(file);
String currentParty = pModeXMLUtils.getCurrentPartyName();

page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
String partyToMonitor = page.grid().getRowSpecificColumnVal(0, "Party");
TestMessDetailsModal modal = new TestMessDetailsModal(driver);
if (!partyToMonitor.equalsIgnoreCase(currentParty)) {

String actualErrMsg = getAlrtForTestMsg(page, 0, "Details", modal);
String error = String.format(DMessages.CONNECTION_MONITORING_ERROR_RESPONDER_NOTUP, partyToMonitor, receiverEndPoint);
soft.assertEquals(actualErrMsg, error, "Correct error message is shown");

modal.getCloseBtn().click();
page.grid().waitForRowsToLoad();
soft.assertTrue(page.grid().connectionStatusIcons.get(0).getText().equals("error"), "Test Message is not sent");
}
soft.assertAll();

}

/* CM-13 - Test connection when destination party certificate is invalid or expired*/
/*  CM-13 - Test connection when destination party certificate is invalid or expired  */
@Description("CM-13 - Test connection when destination party certificate is invalid or expired")
@Link(name = "EDELIVERY-7251", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7251")
@AllureId("CM-13")
@Test(description = "CM-13", groups = {"multiTenancy", "singleTenancy"})
public void wrongRecCert() throws Exception {
SoftAssert soft = new SoftAssert();
ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);

String filepath = "pmodes/Edelivery-blue.xml";
rest.pmode().uploadPMode(filepath, null);
File file = new File(getClass().getClassLoader().getResource(filepath).getFile());
PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(file);
String currentParty = pModeXMLUtils.getCurrentPartyName();
page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);
TruststorePage tPage = new TruststorePage(driver);
String pathToInvalidCert = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore_noRecCert.jks");

tPage.uploadFile(pathToInvalidCert, "test123", soft);
Allure.step(page.getAlertArea().getAlertMessage() + "   -  Message after upload event");
log.info(page.getAlertArea().getAlertMessage(), " Message after upload event");

page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
TestMessDetailsModal modal = new TestMessDetailsModal(driver);
String partyName = page.grid().getRowSpecificColumnVal(0, "Party");

if (partyName != currentParty) {
String actualErrMsg = getAlrtForTestMsg(page, 0, "Details", modal);
String certError = String.format(DMessages.CONNECTION_MONITORING_CERT_ERROR, partyName, "red_gw");
soft.assertTrue(actualErrMsg.equals(certError), "Correct error is shown");
modal.getCloseBtn().click();

}
page.getSidebar().goToPage(PAGES.TRUSTSTORES_DOMIBUS);

String pathToCorrectCert = DFileUtils.getAbsolutePath("./src/main/resources/truststore/gateway_truststore.jks");
tPage.uploadFile(pathToCorrectCert, "test123", soft);
soft.assertAll();

}

public String getAlrtForTestMsg(ConnectionMonitoringPage page, int i, String actionBtnName, TestMessDetailsModal modal) throws Exception {
page.grid().getActionButton(actionBtnName, i).click();

modal.getTestbutton().click();
new DWait(driver).forXMillis(100);
modal.getUpdateBtn().click();
return page.getAlertArea().getAlertMessage();
}

}
