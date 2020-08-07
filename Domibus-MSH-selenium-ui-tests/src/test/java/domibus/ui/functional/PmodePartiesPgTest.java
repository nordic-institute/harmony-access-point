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
import pages.pmode.parties.PartyModal;
import pages.pmode.parties.modal.PPartyModal;
import utils.Generator;
import utils.PModeXMLUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PmodePartiesPgTest extends SeleniumTest {
	
	private static String partyName = "Party Name";
	private static String endpoint = "End Point";
	private static String partyID = "Party Id";
	private static String oldPartyName = "red_gw";
	
	@Test(description = "PMP-2", groups = {"multiTenancy", "singleTenancy"})
	public void filterParties() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
		
		log.info("Login with Admin credentials and navigate to Pmode parties page");
		PModePartiesPage page = navigateToPage();
		
		log.info("Validate presence of filter party name");
		soft.assertTrue(page.filters().getNameInput().isEnabled(), "Page contains filter for party name");
		
		HashMap<String, String> firstParty = page.grid().getRowInfo(0);
		
		log.info("Enter party name");
		page.filters().getNameInput().fill(firstParty.get(partyName));
		
		log.info("Enter Endpoint");
		page.filters().getEndpointInput().fill(firstParty.get(endpoint));
		
		log.info("Enter party Id");
		page.filters().getPartyIDInput().fill(firstParty.get(partyID));
		
		log.info("Click on search button");
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();
		
		soft.assertEquals(page.grid().getRowsNo(), 1, "1 rows returned");
		soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "first party is returned");
		soft.assertAll();
	}
	
	@Test(description = "PMP-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("upload pmode");
		if (!rest.pmode().isPmodeUploaded(null)) {
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}
		
		log.info("Login into application and navigate to Pmode parties page");
		PModePartiesPage page = navigateToPage();
		
		log.info("Extract row info for 0th row");
		HashMap<String, String> firstParty = page.grid().getRowInfo(0);
		
		log.info("Double click 0th row");
		page.grid().doubleClickRow(0);
		
		PartyModal modal = new PartyModal(driver);
		soft.assertEquals(firstParty.get(partyName), modal.getNameInput().getText(), "Listed party name is correct");
		soft.assertEquals(firstParty.get(endpoint), modal.getEndpointInput().getText(), "Listed party endpoint is correct");
		
		List<String> toCompare = new ArrayList<>();
		log.info("Validate presence of partyId");
		
		for (HashMap<String, String> info : modal.getIdentifierTable().getListedRowInfo()) {
			soft.assertTrue(firstParty.get(partyID).contains(info.get("Party Id")), "id is listed");
		}
		
		soft.assertAll();
	}
	
	@Test(description = "PMP-4", groups = {"multiTenancy", "singleTenancy"})
	public void deleteParty() throws Exception {
		SoftAssert soft = new SoftAssert();
		
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
		
		log.debug("party to delete: " + partyToDelete.get(partyName));
		log.info("Select row " + rowIndex);
		page.grid().selectRow(rowIndex);
		
		log.info("Click delete button");
		page.getDeleteButton().click();
		
		soft.assertTrue(page.getSaveButton().isEnabled(), "Save button is active");
		soft.assertTrue(page.getCancelButton().isEnabled(), "Cancel button is active");
		
		log.info("Click on Cancel button");
		page.getCancelButton().click();
		new Dialog(driver).confirm();
		
		log.info("validate presence of first party after cancellation");
		soft.assertEquals(page.grid().getRowInfo(rowIndex).get(partyName), partyToDelete.get(partyName), "After cancel party is still present in grid");
		
		log.info("select row " + rowIndex);
		page.grid().selectRow(rowIndex);
		
		log.info("Click delete button");
		page.getDeleteButton().click();
		
		log.info("Click on Save button");
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		
		log.info("Validate absence of first party from grid data");
		soft.assertTrue(page.grid().scrollTo(partyName, partyToDelete.get(partyName)) == -1, "After save party is NOT present in grid");
		
		soft.assertAll();
	}
	
	@Test(description = "PMP-5", groups = {"multiTenancy", "singleTenancy"})
	public void createParty() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		if (!rest.pmode().isPmodeUploaded(null)) {
			log.info("Upload pmode");
			rest.pmode().uploadPMode("pmodes/pmode-red.xml", null);
		}
		String newPatyName = Generator.randomAlphaNumeric(5);
		
		log.info("login into application and navigate to Pmode parties page");
		PModePartiesPage page = navigateToPage();
		
		log.info("Click on New button");
		page.getNewButton().click();
		
		PartyModal modal = new PartyModal(driver);
		log.info("Fill new party info");
		modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");
		
		log.info("Click ok button");
		modal.clickOK();
		
		log.info("Save and confirm");
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		
		log.info("validate presence of success message");
		soft.assertFalse(page.getAlertArea().isError(), "page shows success message");
		soft.assertTrue(StringUtils.equalsIgnoreCase(page.getAlertArea().getAlertMessage(),
				DMessages.PMODE_PARTIES_UPDATE_SUCCESS), "page shows correct success message");
		
		soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) >= 0, "party is shown in grid");
		
		soft.assertAll();
	}
	
	@Test(description = "PMP-6", groups = {"multiTenancy", "singleTenancy"})
	public void editParty() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("upload pmode");
		String filepath = "pmodes/multipleParties.xml";
		rest.pmode().uploadPMode(filepath, null);
		
		File file = new File(getClass().getClassLoader().getResource(filepath).getFile());
		PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(file);
		String currentParty = pModeXMLUtils.getCurrentPartyName();
		
		log.info("Login and navigate to pmode parties page");
		PModePartiesPage page = navigateToPage();
		
		int index = page.grid().scrollTo(partyName, currentParty);
		String newPartyName = Generator.randomAlphaNumeric(5);
		int toEditIndex = 0;
		if (toEditIndex == index) {
			toEditIndex = 1;
		}
		
		
		log.info("select row " + toEditIndex);
		page.grid().selectRow(toEditIndex);
		
		log.info("Click edit button");
		page.getEditButton().click();
		PartyModal modal = new PartyModal(driver);
		
		log.info("Fill new party info");
		modal.getNameInput().fill(newPartyName);
		
		log.info("Fill endpoint value");
		modal.getEndpointInput().fill("http://" + newPartyName.toLowerCase() + ".com");
		
		log.info("Click ok button");
		modal.clickOK();
		
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		
		log.info("Validate presence of success message");
		soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");
		
		log.info("Validate visibility of new party: " + newPartyName);
		soft.assertTrue(page.grid().scrollTo(partyName, newPartyName) >= 0, "New name is visible in grid.");
		soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPartyName + ".com") >= 0, "New endpoint is visible in grid.");
		soft.assertAll();
	}
	
	@Test(description = "PMP-7", groups = {"multiTenancy", "singleTenancy"})
	public void editPartyAndCancel() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
		
		String newPartyName = Generator.randomAlphaNumeric(5);
		
		PModePartiesPage page = navigateToPage();
		log.info("select row 0");
		page.grid().selectRow(0);
		
		log.info("Click edit button");
		page.getEditButton().click();
		
		PartyModal modal = new PartyModal(driver);
		log.info("Fill new party name");
		modal.getNameInput().fill(newPartyName);
		log.info("fill end point ");
		modal.getEndpointInput().fill("http://" + newPartyName.toLowerCase() + ".com");
		log.info("click ok button");
		modal.clickOK();
		
		log.info("Click Cancel button ");
		page.getCancelButton().click();
		new Dialog(driver).confirm();
		
		log.info("Validate non visibility of new party after cancellation");
		soft.assertTrue(page.grid().scrollTo(partyName, newPartyName) < 0, "New name is NOT visible in grid");
		soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPartyName + ".com") < 0, "New endpoint is NOT visible in grid");
		
		soft.assertAll();
	}
	
	@Test(description = "PMP-8", groups = {"multiTenancy"})
	public void domainSegregation() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String domainName = rest.getNonDefaultDomain();
		String domainCode = rest.getDomainCodeForName(domainName);
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
		rest.pmode().uploadPMode("pmodes/doNothingSelfSending.xml", domainCode);
		
		PModePartiesPage page = navigateToPage();
		
		log.info("Count total no of parties present");
		int noOfParties = page.grid().getRowsNo();
		
		log.info("select given domain in domain selector drop down");
		page.getDomainSelector().selectOptionByText(domainName);
		
		log.info("Find no of parties present");
		int domainNoOfParties = page.grid().getRowsNo();
		
		log.info("compare no of parties of both domain");
		soft.assertTrue(noOfParties != domainNoOfParties, "Number of parties doesn't coincide");
		
		soft.assertAll();
	}
	
	@Test(description = "PMP-21", groups = {"multiTenancy", "singleTenancy"})
	public void partyAdditionCurrentPmode() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("upload Pmode");
		String pmodePath = "pmodes/Edelivery-blue.xml";
		rest.pmode().uploadPMode(pmodePath, null);
		
		log.info("Navigate to Pmode current page");
		
		PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(new File(getClass().getClassLoader().getResource(pmodePath).getFile()));
		String myPartyName = pModeXMLUtils.addPartyToPmode();
		String newPmodeStr = pModeXMLUtils.printDoc();
		
		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
		pmcPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		pmcPage.waitForPageToLoad();
		
		log.info("Edit current text");
		pmcPage.getTextArea().fill(newPmodeStr);
		
		log.info("Click on save button");
		pmcPage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		
		log.info("Enter comment");
		modal.getDescriptionTextArea().fill("New party is added " + myPartyName);
		
		log.info("Click on Ok button");
		modal.clickOK();
		
		soft.assertFalse(pmcPage.getAlertArea().isError(), "Success mess is shown");
		
		PModePartiesPage page = navigateToPage();
		
		soft.assertTrue(page.grid().scrollTo(partyName, myPartyName) >= 0, "New party is present in parties page");
		
		soft.assertFalse(rest.connMonitor().getConnectionMonitoringParties(null).toString().contains(myPartyName), "New party listed in connection parties");
		
		soft.assertAll();
		
	}
	
	@Test(description = "PMP-22", groups = {"multiTenancy", "singleTenancy"})
	public void partyRemovalCurrentPmode() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("upload Pmode");
		String pmodePath = "pmodes/Edelivery-blue.xml";
		rest.pmode().uploadPMode(pmodePath, null);
		
		PModeXMLUtils pModeXMLUtils = new PModeXMLUtils(new File(getClass().getClassLoader().getResource(pmodePath).getFile()));
		pModeXMLUtils.removeParty(oldPartyName);
		String newPmodeStr = pModeXMLUtils.printDoc();
		
		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
		pmcPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		pmcPage.waitForPageToLoad();
		
		log.info("Edit current text");
		pmcPage.getTextArea().fill(newPmodeStr);
		
		log.info("Click on save button");
		pmcPage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		
		log.info("Enter comment");
		modal.getDescriptionTextArea().fill("Party deleted " + oldPartyName);
		
		log.info("Click on Ok button");
		modal.clickOK();
		
		soft.assertFalse(pmcPage.getAlertArea().isError(), "Success mess is shown");
		
		
		PModePartiesPage page = navigateToPage();
		
		soft.assertTrue(page.grid().scrollTo("Party Name", oldPartyName) < 0, "Red_gw party is not available");
		
		soft.assertFalse(rest.connMonitor().getConnectionMonitoringParties(null).toString().contains(oldPartyName), "Red party is not present");
		soft.assertAll();
	}
	
	@Test(description = "PMP-23", groups = {"multiTenancy", "singleTenancy"})
	public void partyAdditionOnPartiesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		
		PModePartiesPage page = navigateToPage();
		
		log.info("Validate whether New button is enabled ");
		soft.assertTrue(page.getNewButton().isEnabled(), "New button is enabled");
		
		log.info("Click on New button");
		page.getNewButton().click();
		
		log.info("Generate random New Party Name");
		String newPatyName = Generator.randomAlphaNumeric(5);
		PartyModal modal = new PartyModal(driver);
		
		log.info("Fill New Party Form");
		modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");
		
		log.info("Click On Ok Button");
		modal.clickOK();
		
		log.info("Click on Save button");
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		
		page.wait.forXMillis(5000);
		log.info("Validate Success Message");
		soft.assertTrue(!page.getAlertArea().isError(), "page shows success message");
		
		log.info("Validate presence of New Party in Grid");
		soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) >= 0, "party is shown in grid");
		
		log.info("Navigate to Pmode current page");
		
		PModeCurrentPage pModeCurrentPage = new PModeCurrentPage(driver);
		pModeCurrentPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		
		soft.assertTrue(pModeCurrentPage.getTextArea().isPresent(), "Current pmode is available");
		String UpdatedPmode = pModeCurrentPage.getTextArea().getText();
		log.info("Current Pmode is :" + UpdatedPmode);
		log.info("Validate presence of new party name in Current Pmode");
		
		soft.assertTrue(UpdatedPmode.contains(newPatyName), "New party is shown in Current pmode");
		
		soft.assertAll();
	}
	
	@Test(description = "PMP-24", groups = {"multiTenancy", "singleTenancy"})
	public void partyRemovalFromPartiesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		
		PModePartiesPage page = navigateToPage();
		
		log.info("Select row for red_gw other than current system party name");
		page.grid().scrollToAndSelect(partyName, "red_gw");
		
		log.info("Click on Delete button");
		page.getDeleteButton().click();
		
		log.info("Click on Save button");
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		
		log.info(page.getAlertArea().getAlertMessage());
		log.info("Navigate to Pmode Current page");
		
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		page.waitForPageToLoad();
		
		PModeCurrentPage pModeCurrentPage = new PModeCurrentPage(driver);
		
		soft.assertTrue(pModeCurrentPage.getTextArea().isPresent(), "Current pmode is available");
		
		String updatedPmode = pModeCurrentPage.getTextArea().getText();
		
		log.info("Validate absence of party name :red_gw ");
		soft.assertFalse(updatedPmode.contains(oldPartyName), "red_gw party is not shown in Current pmode");
		
		soft.assertAll();
	}
	
	@Test(description = "PMP-25", groups = {"multiTenancy", "singleTenancy"})
	public void responderInitiatorRemovalFromPartiesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("upload Pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
		
		PModePartiesPage page = navigateToPage();
		
		log.info("Find index of row having party name red_gw on Pmode parties page and select row");
		page.grid().scrollToAndSelect(partyName, oldPartyName);
		
		log.info("Click on Edit button");
		page.getEditButton().click();
		
		PartyModal modal = new PartyModal((driver));
		log.info("Validate Ok button is enabled");
		soft.assertTrue(modal.getOkBtn().isEnabled());
		
		log.info("Uncheck Initiator & Responder checkbox");
		modal.participationInProcess("tc1Process", false, false);
		
		log.info("Click on Save button");
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		
		
		soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");
		
		log.info("Navigate to Pmode Current page");
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		PModeCurrentPage pModeCurrentPage = new PModeCurrentPage(driver);
		pModeCurrentPage.waitForPageToLoad();
		
		String updatedPmode = pModeCurrentPage.getTextArea().getText();
		log.info("Validate absence of red_gw as Initiator party");
		soft.assertFalse(updatedPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is not present in pmode");
		
		log.info("Validate absence of red_gw as Responder party");
		soft.assertFalse(updatedPmode.contains("<responderParty name=\"red_gw\"/>"), "red_gw responder party is not present in pmode");
		
		soft.assertTrue(!rest.connMonitor().getMonitoredParties(null).contains(oldPartyName), "Party is not present in connection monitor page anymore");
		
		soft.assertAll();
	}
	
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
		soft.assertEquals(pmcPage.getAlertArea().getAlertMessage(),DMessages.PMODE_UPDATE_SUCCESS , "Correct message is shown");
		
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
	
	private PModePartiesPage navigateToPage() throws Exception {
		log.info("Navigate to Pmode Parties page");
		PModePartiesPage page = new PModePartiesPage(driver);
		if (page.getTitle().contains("Parties")) {
			page.refreshPage();
		} else {
			page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		}
		page.grid().waitForRowsToLoad();
		return page;
	}
	
}


