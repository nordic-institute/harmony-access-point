package domibus.ui.functional;

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

public class PmodeParties2PgTest extends SeleniumTest {

	private static String partyName = "Party Name";
	private static String endpoint = "End Point";
	private static String partyID = "Party Id";
	private static String oldPartyName = "red_gw";

	@Test(description = "PMP-26", groups = {"multiTenancy", "singleTenancy"})
	public void responderInitiatorAdditionOnPartiesPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/NoResponderInitiator.xml", null);

		PModePartiesPage page = navigateToPage();
		log.info("Find row number for party with name red_gw and select it");
		page.grid().scrollToAndSelect(partyName, oldPartyName);

		log.info("Click on Edit button");
		page.getEditButton().click();

		PartyModal modal = new PartyModal((driver));
		log.info("select checkbox for Initiator & Responder");
		modal.clickIRCheckboxes();

		log.info("Click on Save button");
		page.getSaveButton().click();
		new Dialog(driver).confirm();

		log.info(page.getAlertArea().getAlertMessage());

		log.info("Navigate to Pmode current page");
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		PModeCurrentPage pModeCurrentPage = new PModeCurrentPage(driver);
		page.waitForPageToLoad();

		String updatedPmode = pModeCurrentPage.getTextArea().getText();
		log.info("Validate presence of red_gw as Initiator party ");
		soft.assertTrue(updatedPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is  present in pmode");
		log.info("Validate presence of red_gw as responder party");
		soft.assertTrue(updatedPmode.contains("<responderParty name=\"red_gw\"/>"), "red_gw responder party is  present in pmode");

		soft.assertFalse(rest.connMonitor().getMonitoredParties(null).contains(oldPartyName), oldPartyName + " is not in the connection monitor party list");

		soft.assertAll();
	}

	@Test(description = "PMP-27", groups = {"multiTenancy", "singleTenancy"})
	public void initiatorResponderRemovalCurrentPmode() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("upload Pmode");
		String pmodePath = "pmodes/Edelivery-blue.xml";
		rest.pmode().uploadPMode(pmodePath, null);

		PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(new File(getClass().getClassLoader().getResource(pmodePath).getFile()));
		pModeXMLUtils.removePartyFromAllProcesses(oldPartyName);
		String updatedPmodeInit = pModeXMLUtils.printDoc();

		log.info("Navigate to Pmode Current page");
		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
		pmcPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		log.info("Edit current text");
		pmcPage.getTextArea().fill(updatedPmodeInit);

		log.info("Click on save button");
		pmcPage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		log.info("Enter comment");
		modal.getDescriptionTextArea().fill("Initiator and Responder party name are updated");
		log.info("Click on Ok button");
		modal.clickOK();

		soft.assertTrue(!pmcPage.getAlertArea().isError(), "Success message is shown");
		soft.assertEquals(pmcPage.getAlertArea().getAlertMessage(), DMessages.PMODE_UPDATE_SUCCESS, "Correct message is shown");

		log.info("Validate non presence of red_gw");
		soft.assertFalse(pmcPage.getTextArea().getText().contains("<initiatorParty name=\"red_gw\"/>"));

		log.info("Validate non presence of responder red_gw");
		soft.assertFalse(pmcPage.getTextArea().getText().contains("<responderParty name=\"red_gw\"/>"), "red_gw is not present as Responder");

		log.info("Get index of row  with party detail red_gw");

		PModePartiesPage pPage = navigateToPage();
		pPage.grid().scrollToAndSelect(partyName, oldPartyName);

		log.info("Click on Edit button");
		pPage.getEditButton().click();

		PartyModal pmPage = new PartyModal((driver));

		log.info("Validate initiator checkbox status");
		soft.assertFalse(pmPage.getCheckboxStatus("Initiator"), "Initiator Checkbox is unchecked");

		log.info("Validate checkbox status of responder");
		soft.assertFalse(pmPage.getCheckboxStatus("Responder"), "Responder checkbox is unchecked");

		String monitoredParties = rest.connMonitor().getConnectionMonitoringParties(null).toString();
		soft.assertTrue(!monitoredParties.contains(oldPartyName), oldPartyName + " is not available for monitoring in Connection Monitoring page ");
		soft.assertAll();
	}

	@Test(description = "PMP-28", groups = {"multiTenancy", "singleTenancy"})
	public void IRRemovalCurrentPmode() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("upload Pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		PModePartiesPage page = navigateToPage();

		page.grid().getGridCtrl().showOnlyColumn("Party Name");

		page.grid().scrollToAndDoubleClick("Party Name", "red_gw");

		PPartyModal modal = new PPartyModal(driver);

		modal.processSection.editForProcess("tc1Process", false, false);
		modal.clickOK();

		page.getSaveButton().click();
		new Dialog(driver).confirm();

		soft.assertTrue(!page.getAlertArea().isError(), "Success message shown");

		String monitoredParties = rest.connMonitor().getConnectionMonitoringParties(null).toString();
		soft.assertTrue(!monitoredParties.contains(oldPartyName), oldPartyName + " is not available for monitoring in Connection Monitoring page ");

		soft.assertAll();
	}

	@Test(description = "PMP-29", groups = {"multiTenancy", "singleTenancy"})
	public void initiatorResponderAdditionCurrentPmode() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("upload Pmode");
		String pmodePath = "pmodes/Edelivery-blue.xml";
		rest.pmode().uploadPMode(pmodePath, null);

		PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(getClass().getClassLoader().getResource(pmodePath).getFile());
		String mypartyName = pModeXMLUtils.addPartyToPmode();
		pModeXMLUtils.addPartyToProcessInitiators(mypartyName, null);
		pModeXMLUtils.addPartyToProcessResponders(mypartyName, null);
		String updatedPmodeInit = pModeXMLUtils.printDoc();

		PModeCurrentPage pcPage = new PModeCurrentPage(driver);
		log.info("Navigate to Pmode Current page");
		pcPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		log.info("Replace initiator from red to green");
		log.info("Edit current text");
		pcPage.getTextArea().fill(updatedPmodeInit);
		log.info("Click on save button");
		pcPage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		log.info("Enter comment");
		modal.getDescriptionTextArea().fill("Initiator and responder  party name and party id are updated");
		log.info("Click on Ok button");
		modal.clickOK();

		soft.assertTrue(!pcPage.getAlertArea().isError(), "Success message is shown");

		PModePartiesPage ppPage = navigateToPage();

		log.info("Get index of row  with party detail " + mypartyName);
		int index = ppPage.grid().scrollTo(partyName, mypartyName);
		String myPartId = ppPage.grid().getRowInfo(index).get("Party Id");
		ppPage.grid().selectRow(index);

		log.info("Click on Edit button");
		ppPage.getEditButton().click();

		PartyModal modal1 = new PartyModal((driver));
		log.info("Validate initiator checkbox status");
		soft.assertFalse(modal1.getCheckboxStatus("Initiator"), "Initiator Checkbox is unchecked");
		soft.assertFalse(modal1.getCheckboxStatus("Responder"), "Responder Checkbox is unchecked");

		modal1.clickCancel();


		log.info("Validate presence of " + myPartId);
		String monitoredParties = rest.connMonitor().getConnectionMonitoringParties(null).toString();
		soft.assertTrue(monitoredParties.contains(myPartId), myPartId + " is  present");
		soft.assertAll();
	}

	/* This method will verify search with forbidden characters  */
	@Test(description = "PMP-31", groups = {"multiTenancy", "singleTenancy"})
	public void searchWithForbiddenChar() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login and Navigate to Pmode Parties page");
		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		String searchData = "'\\u0022(){}[];,+=%&*#<>/\\\\";
		log.info("search string is: " + searchData);

		String domain = selectRandomDomain();

		page.filters().getNameInput().fill(searchData);
		page.filters().getEndpointInput().fill(searchData);
		page.filters().getPartyIDInput().fill(searchData);
		page.filters().getProcessInput().fill(searchData);

		log.info("Click on search button");
		page.filters().getSearchButton().click();

		page.grid().waitForRowsToLoad();

		int noOfRes = page.grid().getPagination().getTotalItems();

		soft.assertTrue(noOfRes == 0, "Blank grid is shown");
		soft.assertFalse(page.getAlertArea().isShown(), "No alert message is shown for forbidden char");

		soft.assertAll();
	}

	/* This method will check successful deletion of all parties except the one which defines system */
	@Test(description = "PMP-32", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAllParties() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login and Navigate to Pmode Current page");

		String domain = selectRandomDomain();

		log.info("Extract system party name from current pmode");
		String pmodePath = "./src/main/resources/pmodes/Edelivery-blue.xml";
		String systemParty = new PModeXMLUtils(new File(pmodePath)).getCurrentPartyName();

		String path = "pmodes/Edelivery-blue.xml";
		log.info("upload Pmode");
		rest.pmode().uploadPMode(path, domain);

		PModePartiesPage page = navigateToPage();

		log.info("Total number of available pmode parties");
		int count = page.grid().getPagination().getTotalItems();

		for (int i = 0; i < count; i++) {
			page.grid().selectRow(0);
			int index = page.grid().scrollTo("Party Name", systemParty);

			page.getDeleteButton().click();
			page.getSaveButton().click();
			new Dialog(driver).confirm();

			if (index == 0) {
				soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.PMODE_PARTIES_DELETE_OWN_PARTY_ERROR, "Proper alert message is displayed alerting user he cannot delete party describing own system");
				soft.assertTrue(page.getAlertArea().isError(), "Message is error message");
			} else {
				soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.PMODE_PARTIES_UPDATE_SUCCESS, "Proper successs message is displayed");
				soft.assertFalse(page.getAlertArea().isError(), "When not deleting own party success message is shown");
			}
			page.refreshPage();
		}

		soft.assertAll();

	}

	/* EDELIVERY-7255 - PMP-33 -Delete party defining current system from Pmode Parties and Pmode Current page */
	@Test(description = "PMP-33", groups = {"multiTenancy", "singleTenancy"})
	public void deleteCurrentSystemParty() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login and Navigate to Pmode Current page");

		log.info("Extract system party name from current pmode");
		String pmodePath = "./src/main/resources/pmodes/Edelivery-blue.xml";
		String systemParty = new PModeXMLUtils(new File(pmodePath)).getCurrentPartyName();

		String path = "pmodes/Edelivery-blue.xml";
		log.info("upload Pmode");
		rest.pmode().uploadPMode(path, null);

		PModePartiesPage page = navigateToPage();


		page.grid().scrollToAndSelect("Party Name", systemParty);

		page.getDeleteButton().click();
		page.getSaveButton().click();
		new Dialog(driver).confirm();

		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.PMODE_PARTIES_DELETE_OWN_PARTY_ERROR, "Proper alert message is displayed alerting user he cannot delete party describing own system");
		soft.assertTrue(page.getAlertArea().isError(), "Message is error message");

		soft.assertAll();

	}


	/* EDELIVERY-7257 - PMP-35 -Create identifier with duplicate partyId */
	@Test(description = "PMP-35", groups = {"multiTenancy", "singleTenancy"})
	public void newIdentifierDuplicatePartyId() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login and Navigate to Pmode Current page");

		String path = "pmodes/Edelivery-blue.xml";
		log.info("upload Pmode");
		rest.pmode().uploadPMode(path, null);

		PModePartiesPage page = navigateToPage();
		page.grid().waitForRowsToLoad();

		log.info("edit party at row 0");
		page.grid().selectRow(0);
		page.getEditButton().click();

		PartyModal modal = new PartyModal(driver);
		log.info("getting identifier at row 0");
		HashMap<String, String> identifierInfo = modal.getIdentifierTable().getRowInfo(0);

		modal.getNewIdentifierButton().click();
		log.info("create new identifier with the same info as old one");

		PartyIdentifierModal pidModal = new PartyIdentifierModal(driver);

		pidModal.getPartyIdInput().fill(identifierInfo.get("Party Id"));
		pidModal.getPartyIdTypeInput().fill(identifierInfo.get("Party Id Type"));
		pidModal.getPartyIdValueInput().fill(identifierInfo.get("Party Id Value"));

		log.info("saving...");
		pidModal.clickOK();
		modal.clickOK();

		page.getSaveButton().click();
		new Dialog(driver).confirm();

		String expectedErrStr1 = "The operation 'update parties' did not complete successfully. [DOM_003]:PMode validation failed";
		String expectedErrStr2 = "ERROR: Duplicate party identifier";

		log.info("checking error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is present");
		String actualMess = page.getAlertArea().getAlertMessage();
		soft.assertTrue(actualMess.contains(expectedErrStr1), "Error message is correct(1)");
		soft.assertTrue(actualMess.contains(expectedErrStr2), "Error message is correct(2)");

		soft.assertAll();
	}


	/* EDELIVERY-6153 - PMP-34 - User can add multiple party identifiers */
	@Test(description = "PMP-34", groups = {"multiTenancy", "singleTenancy"})
	public void multipleIdentifiers() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login and Navigate to Pmode Current page");

		String path = "pmodes/Edelivery-blue.xml";
		log.info("upload Pmode");
		rest.pmode().uploadPMode(path, null);

		PModePartiesPage page = navigateToPage();
		page.grid().waitForRowsToLoad();

		log.info("edit party at row 0");
		page.grid().selectRow(0);
		page.getEditButton().click();

		PartyModal modal = new PartyModal(driver);

		modal.getNewIdentifierButton().click();
		log.info("create new identifier");

		PartyIdentifierModal pidModal = new PartyIdentifierModal(driver);

		pidModal.getPartyIdInput().fill(Gen.rndStr(5));
		pidModal.getPartyIdTypeInput().fill(Gen.rndStr(5));
		pidModal.getPartyIdValueInput().fill(Gen.randomPartyIdValStr());

		log.info("saving...");
		pidModal.clickOK();
		modal.clickOK();

		page.getSaveButton().click();
		new Dialog(driver).confirm();


		log.info("checking error message");
		soft.assertFalse(page.getAlertArea().isError(), "Error message is present");

		soft.assertAll();
	}

	/* EDELIVERY-7261 - PMP-37 - Create party with duplicate party name */
	@Test(description = "PMP-37", groups = {"multiTenancy", "singleTenancy"})
	public void duplicatePartyName() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login and Navigate to Pmode Current page");

		String path = "pmodes/Edelivery-blue.xml";
		log.info("upload Pmode");
		rest.pmode().uploadPMode(path, null);

		PModePartiesPage page = navigateToPage();
		page.grid().waitForRowsToLoad();

		log.info("getting info from row 0");
		HashMap<String, String> partyInfo = page.grid().getRowInfo(0);

		log.info("click new party..");
		page.getNewButton().click();

		PartyModal modal = new PartyModal(driver);

		modal.getNameInput().fill(partyInfo.get("Party Name"));
		modal.getEndpointInput().fill(Gen.randomEndpoint());


		modal.getNewIdentifierButton().click();
		log.info("create new identifier");

		PartyIdentifierModal pidModal = new PartyIdentifierModal(driver);

		pidModal.getPartyIdInput().fill(Gen.rndStr(5));
		pidModal.getPartyIdTypeInput().fill(Gen.rndStr(5));
		pidModal.getPartyIdValueInput().fill(Gen.randomPartyIdValStr());

		log.info("saving...");
		pidModal.clickOK();
		modal.clickOK();

		page.getSaveButton().click();
		new Dialog(driver).confirm();


		log.info("checking error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is present");

		soft.assertAll();
	}


	/* EDELIVERY-7268 - PMP-38 - Create party with unique partyIdType but existing partyIdValue */
	@Test(description = "PMP-38", groups = {"multiTenancy", "singleTenancy"})
	public void partyIdValueDuplicate() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login and Navigate to Pmode Current page");

		String path = "pmodes/Edelivery-blue.xml";
		log.info("upload Pmode");
		rest.pmode().uploadPMode(path, null);

		PModePartiesPage page = navigateToPage();
		page.grid().waitForRowsToLoad();

		log.info("edit party at row 0");
		page.grid().selectRow(0);
		page.getEditButton().click();

		PartyModal modal = new PartyModal(driver);

		HashMap<String, String> identifierInfo = modal.getIdentifierTable().getRowInfo(0);

		modal.getNewIdentifierButton().click();
		log.info("create new identifier");

		PartyIdentifierModal pidModal = new PartyIdentifierModal(driver);

		pidModal.getPartyIdInput().fill(Gen.rndStr(5));
		pidModal.getPartyIdTypeInput().fill(Gen.rndStr(5));
		pidModal.getPartyIdValueInput().fill(identifierInfo.get("Party Id Value"));

		log.info("saving...");
		pidModal.clickOK();
		modal.clickOK();

		page.getSaveButton().click();
		new Dialog(driver).confirm();


		log.info("checking error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is present");

		soft.assertAll();
	}



	private PModePartiesPage navigateToPage() throws Exception {
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


