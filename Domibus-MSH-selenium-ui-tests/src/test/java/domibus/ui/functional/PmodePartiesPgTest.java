package domibus.ui.functional;

import io.qameta.allure.*;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;
import pages.pmode.parties.PModePartiesPage;
import pages.pmode.parties.PartyIdentifierModal;
import pages.pmode.parties.PartyModal;
import pages.pmode.parties.modal.PPartyModal;
import utils.Gen;
import utils.PModeXMLUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Epic("")
@Feature("")
public class PmodePartiesPgTest extends SeleniumTest {

private static String partyName = "Party Name";
private static String endpoint = "End Point";
private static String partyID = "Party Id";
private static String oldPartyName = "red_gw";

/*  PMP-2 - Filter parties by one or multiple criteria  */
@Description("PMP-2 - Filter parties by one or multiple criteria")
@Link(name = "EDELIVERY-5324", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5324")
@AllureId("PMP-2")
@Test(description = "PMP-2", groups = {"multiTenancy", "singleTenancy"})
public void filterParties() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload pmode");
log.info("upload pmode");
rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);

Allure.step("Login with Admin credentials and navigate to Pmode parties page");
log.info("Login with Admin credentials and navigate to Pmode parties page");
PModePartiesPage page = navigateToPage();

Allure.step("Validate presence of filter party name");
log.info("Validate presence of filter party name");
soft.assertTrue(page.filters().getNameInput().isEnabled(), "Page contains filter for party name");

HashMap<String, String> firstParty = page.grid().getRowInfo(0);

Allure.step("Enter party name");
log.info("Enter party name");
page.filters().getNameInput().fill(firstParty.get(partyName));

Allure.step("Enter Endpoint");
log.info("Enter Endpoint");
page.filters().getEndpointInput().fill(firstParty.get(endpoint));

Allure.step("Enter party Id");
log.info("Enter party Id");
page.filters().getPartyIDInput().fill(firstParty.get(partyID));

Allure.step("Click on search button");
log.info("Click on search button");
page.filters().getSearchButton().click();
page.grid().waitForRowsToLoad();

soft.assertEquals(page.grid().getRowsNo(), 1, "1 rows returned");
soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "first party is returned");
soft.assertAll();
}

/*  PMP-4 - User doubleclicks on a row  */
@Description("PMP-4 - User doubleclicks on a row")
@Link(name = "EDELIVERY-5326", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5326")
@AllureId("PMP-4")
@Test(description = "PMP-4", groups = {"multiTenancy", "singleTenancy"})
public void doubleClickRow() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload pmode");
log.info("upload pmode");
if (!rest.pmode().isPmodeUploaded(null)) {
rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
}

Allure.step("Login into application and navigate to Pmode parties page");
log.info("Login into application and navigate to Pmode parties page");
PModePartiesPage page = navigateToPage();

Allure.step("Extract row info for 0th row");
log.info("Extract row info for 0th row");
HashMap<String, String> firstParty = page.grid().getRowInfo(0);

Allure.step("Double click 0th row");
log.info("Double click 0th row");
page.grid().doubleClickRow(0);

PartyModal modal = new PartyModal(driver);
soft.assertEquals(firstParty.get(partyName), modal.getNameInput().getText(), "Listed party name is correct");
soft.assertEquals(firstParty.get(endpoint), modal.getEndpointInput().getText(), "Listed party endpoint is correct");

List<String> toCompare = new ArrayList<>();
Allure.step("Validate presence of partyId");
log.info("Validate presence of partyId");

for (HashMap<String, String> info : modal.getIdentifierTable().getListedRowInfo()) {
soft.assertTrue(firstParty.get(partyID).contains(info.get("Party Id")), "id is listed");
}

soft.assertAll();
}

/*  PMP-5 - User chooses to delete a party  */
@Description("PMP-5 - User chooses to delete a party")
@Link(name = "EDELIVERY-5327", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5327")
@AllureId("PMP-5")
@Test(description = "PMP-5", groups = {"multiTenancy", "singleTenancy"})
public void deleteParty() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload pmode");
log.info("upload pmode");
String filepath = "pmodes/multipleParties.xml";
rest.pmode().uploadPMode(filepath, null);

File file = new File(getClass().getClassLoader().getResource(filepath).getFile());
PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(file);
String currentParty = pModeXMLUtils.getCurrentPartyName();

PModePartiesPage page = navigateToPage();

List<HashMap<String, String>> partyInfo = page.grid().getListedRowInfo();
HashMap<String, String> partyToDelete = null;
int rowIndex = -1;
for (int i = 0; i < partyInfo.size(); i++) {
HashMap<String, String> party = partyInfo.get(i);
if (!StringUtils.equalsIgnoreCase(party.get(partyName), currentParty)) {
rowIndex = i;
partyToDelete = party;
break;
}
}

Allure.step("party to delete: " + partyToDelete.get(partyName));
log.debug("party to delete: " + partyToDelete.get(partyName));
Allure.step("Select row " + rowIndex);
log.info("Select row " + rowIndex);
page.grid().selectRow(rowIndex);

Allure.step("Click delete button");
log.info("Click delete button");
page.getDeleteButton().click();

soft.assertTrue(page.getSaveButton().isEnabled(), "Save button is active");
soft.assertTrue(page.getCancelButton().isEnabled(), "Cancel button is active");

Allure.step("Click on Cancel button");
log.info("Click on Cancel button");
page.getCancelButton().click();
new Dialog(driver).confirm();

Allure.step("validate presence of first party after cancellation");
log.info("validate presence of first party after cancellation");
soft.assertEquals(page.grid().getRowInfo(rowIndex).get(partyName), partyToDelete.get(partyName), "After cancel party is still present in grid");

Allure.step("select row " + rowIndex);
log.info("select row " + rowIndex);
page.grid().selectRow(rowIndex);

Allure.step("Click delete button");
log.info("Click delete button");
page.getDeleteButton().click();

Allure.step("Click on Save button");
log.info("Click on Save button");
page.getSaveButton().click();
new Dialog(driver).confirm();

Allure.step("Validate absence of first party from grid data");
log.info("Validate absence of first party from grid data");
soft.assertTrue(page.grid().scrollTo(partyName, partyToDelete.get(partyName)) == -1, "After save party is NOT present in grid");

soft.assertAll();
}

/*  PMP-6 - User chooses to add a new party  */
@Description("PMP-6 - User chooses to add a new party")
@Link(name = "EDELIVERY-5328", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5328")
@AllureId("PMP-6")
@Test(description = "PMP-6", groups = {"multiTenancy", "singleTenancy"})
public void createParty() throws Exception {
SoftAssert soft = new SoftAssert();

if (!rest.pmode().isPmodeUploaded(null)) {
Allure.step("Upload pmode");
log.info("Upload pmode");
rest.pmode().uploadPMode("pmodes/pmode-red.xml", null);
}
String newPatyName = Gen.randomAlphaNumeric(5);

Allure.step("login into application and navigate to Pmode parties page");
log.info("login into application and navigate to Pmode parties page");
PModePartiesPage page = navigateToPage();

Allure.step("Click on New button");
log.info("Click on New button");
page.getNewButton().click();

PartyModal modal = new PartyModal(driver);
Allure.step("Fill new party info");
log.info("Fill new party info");
modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");

Allure.step("Click ok button");
log.info("Click ok button");
modal.clickOK();

Allure.step("Save and confirm");
log.info("Save and confirm");
page.getSaveButton().click();
new Dialog(driver).confirm();

Allure.step("validate presence of success message");
log.info("validate presence of success message");
soft.assertFalse(page.getAlertArea().isError(), "page shows success message");
soft.assertTrue(StringUtils.equalsIgnoreCase(page.getAlertArea().getAlertMessage(),
DMessages.PMODE_PARTIES_UPDATE_SUCCESS), "page shows correct success message");

soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) >= 0, "party is shown in grid");

soft.assertAll();
}

/*  PMP-7 - User chooses to edit a party  */
@Description("PMP-7 - User chooses to edit a party")
@Link(name = "EDELIVERY-5329", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5329")
@AllureId("PMP-7")
@Test(description = "PMP-7", groups = {"multiTenancy", "singleTenancy"})
public void editParty() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload pmode");
log.info("upload pmode");
String filepath = "pmodes/Edelivery-blue.xml";
rest.pmode().uploadPMode(filepath, null);

File file = new File(getClass().getClassLoader().getResource(filepath).getFile());
PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(file);
String currentParty = pModeXMLUtils.getCurrentPartyName();

Allure.step("Login and navigate to pmode parties page");
log.info("Login and navigate to pmode parties page");
PModePartiesPage page = navigateToPage();

String newEndpoint = "http://" + Gen.randomAlphaNumeric(10).toLowerCase() + ".com";


Allure.step("select row for current system party");
log.info("select row for current system party");
page.grid().scrollToAndSelect("Party Name", currentParty);

Allure.step("Click edit button");
log.info("Click edit button");
page.getEditButton().click();
PartyModal modal = new PartyModal(driver);

Allure.step("Fill endpoint value");
log.info("Fill endpoint value");
modal.getEndpointInput().fill(newEndpoint);

Allure.step("Click ok button");
log.info("Click ok button");
modal.clickOK();

page.getSaveButton().click();
new Dialog(driver).confirm();

Allure.step("Validate presence of success message");
log.info("Validate presence of success message");
soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");

Allure.step("Validate visibility of new party endpoint");
log.info("Validate visibility of new party endpoint");
soft.assertTrue(page.grid().scrollTo(endpoint, newEndpoint) >= 0, "New endpoint is visible in grid.");
soft.assertAll();
}

/*  PMP-9 - User makes changes and Cancels them  */
@Description("PMP-9 - User makes changes and Cancels them")
@Link(name = "EDELIVERY-5331", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5331")
@AllureId("PMP-9")
@Test(description = "PMP-9", groups = {"multiTenancy", "singleTenancy"})
public void editPartyAndCancel() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload pmode");
log.info("upload pmode");
rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);

String newPartyName = Gen.randomAlphaNumeric(5);

PModePartiesPage page = navigateToPage();
Allure.step("select row 0");
log.info("select row 0");
page.grid().selectRow(0);

Allure.step("Click edit button");
log.info("Click edit button");
page.getEditButton().click();

PartyModal modal = new PartyModal(driver);
Allure.step("Fill new party name");
log.info("Fill new party name");
modal.getNameInput().fill(newPartyName);
Allure.step("fill end point ");
log.info("fill end point ");
modal.getEndpointInput().fill("http://" + newPartyName.toLowerCase() + ".com");

Allure.step("click ok button");
log.info("click ok button");
modal.clickOK();

Allure.step("Click Cancel button ");
log.info("Click Cancel button ");
page.getCancelButton().click();
new Dialog(driver).confirm();

Allure.step("Validate non visibility of new party after cancellation");
log.info("Validate non visibility of new party after cancellation");
soft.assertTrue(page.grid().scrollTo(partyName, newPartyName) < 0, "New name is NOT visible in grid");
soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPartyName + ".com") < 0, "New endpoint is NOT visible in grid");

soft.assertAll();
}

/*  PMP-10 - User changes domains  */
@Description("PMP-10 - User changes domains")
@Link(name = "EDELIVERY-5332", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5332")
@AllureId("PMP-10")
@Test(description = "PMP-10", groups = {"multiTenancy"})
public void domainSegregation() throws Exception {
SoftAssert soft = new SoftAssert();

String domainName = rest.getNonDefaultDomain();
String domainCode = rest.getDomainCodeForName(domainName);
rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
rest.pmode().uploadPMode("pmodes/doNothingSelfSending.xml", domainCode);

PModePartiesPage page = navigateToPage();

Allure.step("Count total no of parties present");
log.info("Count total no of parties present");
int noOfParties = page.grid().getRowsNo();

Allure.step("select given domain in domain selector drop down");
log.info("select given domain in domain selector drop down");
page.getDomainSelector().selectOptionByText(domainName);

Allure.step("Find no of parties present");
log.info("Find no of parties present");
int domainNoOfParties = page.grid().getRowsNo();

Allure.step("compare no of parties of both domain");
log.info("compare no of parties of both domain");
soft.assertTrue(noOfParties != domainNoOfParties, "Number of parties doesn't coincide");

soft.assertAll();
}

/*  PMP-21 - Check impact when new party is added on Pmode-Current page  */
@Description("PMP-21 - Check impact when new party is added on Pmode-Current page")
@Link(name = "EDELIVERY-5343", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5343")
@AllureId("PMP-21")
@Test(description = "PMP-21", groups = {"multiTenancy", "singleTenancy"})
public void partyAdditionCurrentPmode() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload Pmode");
log.info("upload Pmode");
String pmodePath = "pmodes/Edelivery-blue.xml";
rest.pmode().uploadPMode(pmodePath, null);

Allure.step("Navigate to Pmode current page");
log.info("Navigate to Pmode current page");

PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(new File(getClass().getClassLoader().getResource(pmodePath).getFile()));
String myPartyName = pModeXMLUtils.addPartyToPmode();
String newPmodeStr = pModeXMLUtils.printDoc();

PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
pmcPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);
pmcPage.waitForPageToLoad();

Allure.step("Edit current text");
log.info("Edit current text");
pmcPage.getTextArea().fill(newPmodeStr);

Allure.step("Click on save button");
log.info("Click on save button");
pmcPage.getSaveBtn().click();
PModeCofirmationModal modal = new PModeCofirmationModal(driver);

Allure.step("Enter comment");
log.info("Enter comment");
modal.getDescriptionTextArea().fill("New party is added " + myPartyName);

Allure.step("Click on Ok button");
log.info("Click on Ok button");
modal.clickOK();

soft.assertFalse(pmcPage.getAlertArea().isError(), "Success mess is shown");

PModePartiesPage page = navigateToPage();

soft.assertTrue(page.grid().scrollTo(partyName, myPartyName) >= 0, "New party is present in parties page");

soft.assertFalse(rest.connMonitor().getConnectionMonitoringParties(null).toString().contains(myPartyName), "New party listed in connection parties");

soft.assertAll();

}

/*  PMP-22 - Check impact when party is removed from Pmode-Current   */
@Description("PMP-22 - Check impact when party is removed from Pmode-Current ")
@Link(name = "EDELIVERY-5344", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5344")
@AllureId("PMP-22")
@Test(description = "PMP-22", groups = {"multiTenancy", "singleTenancy"})
public void partyRemovalCurrentPmode() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload Pmode");
log.info("upload Pmode");
String pmodePath = "pmodes/Edelivery-blue.xml";
rest.pmode().uploadPMode(pmodePath, null);

PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(new File(getClass().getClassLoader().getResource(pmodePath).getFile()));
pModeXMLUtils.removeParty(oldPartyName);
String newPmodeStr = pModeXMLUtils.printDoc();

PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
pmcPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);
pmcPage.waitForPageToLoad();

Allure.step("Edit current text");
log.info("Edit current text");
pmcPage.getTextArea().fill(newPmodeStr);

Allure.step("Click on save button");
log.info("Click on save button");
pmcPage.getSaveBtn().click();
PModeCofirmationModal modal = new PModeCofirmationModal(driver);

Allure.step("Enter comment");
log.info("Enter comment");
modal.getDescriptionTextArea().fill("Party deleted " + oldPartyName);

Allure.step("Click on Ok button");
log.info("Click on Ok button");
modal.clickOK();

soft.assertFalse(pmcPage.getAlertArea().isError(), "Success mess is shown");


PModePartiesPage page = navigateToPage();

soft.assertTrue(page.grid().scrollTo("Party Name", oldPartyName) < 0, "Red_gw party is not available");

soft.assertFalse(rest.connMonitor().getConnectionMonitoringParties(null).toString().contains(oldPartyName), "Red party is not present");
soft.assertAll();
}

/*  PMP-23 - Check impact of new party added on Pmode-Parties page  */
@Description("PMP-23 - Check impact of new party added on Pmode-Parties page")
@Link(name = "EDELIVERY-5345", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5345")
@AllureId("PMP-23")
@Test(description = "PMP-23", groups = {"multiTenancy", "singleTenancy"})
public void partyAdditionOnPartiesPage() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload pmode");
log.info("upload pmode");
rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

PModePartiesPage page = navigateToPage();

Allure.step("Validate whether New button is enabled ");
log.info("Validate whether New button is enabled ");
soft.assertTrue(page.getNewButton().isEnabled(), "New button is enabled");

Allure.step("Click on New button");
log.info("Click on New button");
page.getNewButton().click();

Allure.step("Generate random New Party Name");
log.info("Generate random New Party Name");
String newPatyName = Gen.randomAlphaNumeric(5);
PartyModal modal = new PartyModal(driver);

Allure.step("Fill New Party Form");
log.info("Fill New Party Form");
modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");

Allure.step("Click On Ok Button");
log.info("Click On Ok Button");
modal.clickOK();

Allure.step("Click on Save button");
log.info("Click on Save button");
page.getSaveButton().click();
new Dialog(driver).confirm();

Allure.step("Validate Success Message");
log.info("Validate Success Message");
soft.assertTrue(!page.getAlertArea().isError(), "page shows success message");

Allure.step("Validate presence of New Party in Grid");
log.info("Validate presence of New Party in Grid");
soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) >= 0, "party is shown in grid");

Allure.step("Navigate to Pmode current page");
log.info("Navigate to Pmode current page");

page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

PModeCurrentPage pModeCurrentPage = new PModeCurrentPage(driver);

soft.assertTrue(pModeCurrentPage.getTextArea().isPresent(), "Current pmode is available");
String UpdatedPmode = pModeCurrentPage.getTextArea().getText();
Allure.step("Current Pmode is :" + UpdatedPmode);
log.info("Current Pmode is :" + UpdatedPmode);
Allure.step("Validate presence of new party name in Current Pmode");
log.info("Validate presence of new party name in Current Pmode");

soft.assertTrue(UpdatedPmode.contains(newPatyName), "New party is shown in Current pmode");

soft.assertAll();
}

/*  PMP-24 - Check impact of removing party from Parties page  */
@Description("PMP-24 - Check impact of removing party from Parties page")
@Link(name = "EDELIVERY-5346", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5346")
@AllureId("PMP-24")
@Test(description = "PMP-24", groups = {"multiTenancy", "singleTenancy"})
public void partyRemovalFromPartiesPage() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload pmode");
log.info("upload pmode");
rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

PModePartiesPage page = navigateToPage();

Allure.step("Select row for red_gw other than current system party name");
log.info("Select row for red_gw other than current system party name");
page.grid().scrollToAndSelect(partyName, "red_gw");

Allure.step("Click on Delete button");
log.info("Click on Delete button");
page.getDeleteButton().click();

Allure.step("Click on Save button");
log.info("Click on Save button");
page.getSaveButton().click();
new Dialog(driver).confirm();

Allure.step(page.getAlertArea().getAlertMessage());
log.info(page.getAlertArea().getAlertMessage());
Allure.step("Navigate to Pmode Current page");
log.info("Navigate to Pmode Current page");

page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
page.waitForPageToLoad();

PModeCurrentPage pModeCurrentPage = new PModeCurrentPage(driver);

soft.assertTrue(pModeCurrentPage.getTextArea().isPresent(), "Current pmode is available");

String updatedPmode = pModeCurrentPage.getTextArea().getText();

Allure.step("Validate absence of party name :red_gw ");
log.info("Validate absence of party name :red_gw ");
soft.assertFalse(updatedPmode.contains(oldPartyName), "red_gw party is not shown in Current pmode");

soft.assertAll();
}

/*  PMP-25 - Check impact when party removed as InitiatorResponder from Pmode-Parties update popup  */
@Description("PMP-25 - Check impact when party removed as InitiatorResponder from Pmode-Parties update popup")
@Link(name = "EDELIVERY-5347", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5347")
@AllureId("PMP-25")
@Test(description = "PMP-25", groups = {"multiTenancy", "singleTenancy"})
public void responderInitiatorRemovalFromPartiesPage() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("upload Pmode");
log.info("upload Pmode");
rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

PModePartiesPage page = navigateToPage();

Allure.step("Find index of row having party name red_gw on Pmode parties page and select row");
log.info("Find index of row having party name red_gw on Pmode parties page and select row");
page.grid().scrollToAndSelect(partyName, oldPartyName);

Allure.step("Click on Edit button");
log.info("Click on Edit button");
page.getEditButton().click();

PartyModal modal = new PartyModal((driver));

Allure.step("Uncheck Initiator & Responder checkbox");
log.info("Uncheck Initiator & Responder checkbox");
modal.participationInProcess("tc1Process", false, false);

Allure.step("Validate Ok button is enabled");
log.info("Validate Ok button is enabled");
soft.assertTrue(modal.getOkBtn().isEnabled(), "Modal OK button is enabled");

Allure.step("Click on Ok button");
log.info("Click on Ok button");
modal.getOkBtn().click();


Allure.step("Click on Save button");
log.info("Click on Save button");
page.getSaveButton().click();
new Dialog(driver).confirm();


soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");

Allure.step("Navigate to Pmode Current page");
log.info("Navigate to Pmode Current page");
page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
PModeCurrentPage pModeCurrentPage = new PModeCurrentPage(driver);
pModeCurrentPage.waitForPageToLoad();

String updatedPmode = pModeCurrentPage.getTextArea().getText();
Allure.step("Validate absence of red_gw as Initiator party");
log.info("Validate absence of red_gw as Initiator party");
soft.assertFalse(updatedPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is not present in pmode");

Allure.step("Validate absence of red_gw as Responder party");
log.info("Validate absence of red_gw as Responder party");
soft.assertFalse(updatedPmode.contains("<responderParty name=\"red_gw\"/>"), "red_gw responder party is not present in pmode");

soft.assertTrue(!rest.connMonitor().getMonitoredParties(null).contains(oldPartyName), "Party is not present in connection monitor page anymore");

soft.assertAll();
}



private PModePartiesPage navigateToPage() throws Exception {
Allure.step("Navigate to Pmode Parties page");
log.info("Navigate to Pmode Parties page");
PModePartiesPage page = new PModePartiesPage(driver);
if (page.getTitle().contains("Parties")) {
page.refreshPage();
}
if (!page.getTitle().contains("Parties")) {
page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
}
page.grid().waitForRowsToLoad();
return page;
}

}


