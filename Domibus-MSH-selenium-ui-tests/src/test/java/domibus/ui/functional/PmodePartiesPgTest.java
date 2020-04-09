package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import org.json.JSONArray;
import org.json.JSONObject;
import pages.pmode.parties.modal.PPartyModal;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pages.TestServicePage;
import pages.pmode.current.PModeArchivePage;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;
import pages.pmode.parties.PModePartiesPage;
import pages.pmode.parties.PartyModal;
import utils.Generator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PmodePartiesPgTest extends SeleniumTest {

	private static String partyName = "Party Name";
	private static String endpoint = "End Point";
	private static String partyID = "Party Id";
	private static String process = "Process (I=Initiator, R=Responder, IR=Both)";
	private static String partyElement = "party";
	private static String oldPartyName="red_gw";
	private static String newPartyName="black_gw";
	private static String oldPartyId="domibus-red";
	private static String newPartyId="domibus-black";
	private static String defaultPartyId="domibus-blue";
	private static String defaultPartyName="blue-gw";

	@Test(priority=1,description = "PMP-2", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void filterParties() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);

		SoftAssert soft = new SoftAssert();
		log.info("Login with Admin credentials and navigate to Pmode parties page");
		login(data.getAdminUser());
		DomibusPage Dpage=new DomibusPage(driver);
		Dpage.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage page = new PModePartiesPage(driver);
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

		log.info("Wait for rows to load");
		page.grid().waitForRowsToLoad();
		soft.assertEquals(page.grid().getRowsNo(), 1, "1 rows returned");
		soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "first party is returned");
		soft.assertAll();
	}

	@Test(priority=2,description = "PMP-3", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void doubleClickRow() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);

		SoftAssert soft = new SoftAssert();
		log.info("Login into application and navigate to Pmode parties page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage page = new PModePartiesPage(driver);
		log.info("Extract row info for 0th row");
		HashMap<String, String> firstParty = page.grid().getRowInfo(0);
		log.info("Double click 0th row");
		page.grid().doubleClickRow(0);
		PartyModal modal = new PartyModal(driver);
		soft.assertEquals(firstParty.get(partyName), modal.getNameInput().getText(), "Listed party name is correct");
		soft.assertEquals(firstParty.get(endpoint), modal.getEndpointInput().getText(), "Listed party endpoint is correct");
		List<String> toCompare = new ArrayList<>();
		log.info("Validate presence of partyId");
		for (HashMap<String, String> info : modal.getIdentifierTable().getAllRowInfo()) {
			soft.assertTrue(firstParty.get(partyID).contains(info.get("Party Id")), "id is listed");
		}
		soft.assertAll();
	}

	@Test(priority=3,description = "PMP-4", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void deleteParty() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage page = new PModePartiesPage(driver);
		log.info("Extract data for row 0");
		HashMap<String, String> firstParty = page.grid().getRowInfo(0);
		log.debug("party to delete: " + firstParty.get(partyName));
		log.info("Select row0");
		page.grid().selectRow(0);
		log.info("Click delete button");
		page.getDeleteButton().click();
		soft.assertTrue(page.getSaveButton().isEnabled(), "Save button is active");
		soft.assertTrue(page.getCancelButton().isEnabled(), "Cancel button is active");
		log.info("Click on Cancel button");
		page.getCancelButton().click();
		new Dialog(driver).confirm();
		log.info("validate presence of first party after cancellation");
		soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "After cancel party is still present in grid");
		log.info("select row0");
		page.grid().selectRow(0);
		log.info("Click delete button");
		page.getDeleteButton().click();
		log.info("Click on Save button");
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		log.info("Validate absence of first party from grid data");
		soft.assertTrue(page.grid().scrollTo(partyName, firstParty.get(partyName)) == -1, "After save party is NOT present in grid");
		soft.assertAll();
	}

	@Test(priority=4,description = "PMP-5", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void createParty() throws Exception {
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/pmode-red.xml", null);

		String newPatyName = Generator.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();

		log.info("login into application and navigate to Pmode parties page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage page = new PModePartiesPage(driver);

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

	@Test(priority=5,description = "PMP-6", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void editParty() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);

		String newPartyName = Generator.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();
		log.info("Login and navigate to pmode parties page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage page = new PModePartiesPage(driver);
		log.info("select row 0");
		page.grid().selectRow(0);
		log.info("Click edit button");
		page.getEditButton().click();
		PartyModal modal = new PartyModal(driver);
		log.info("Fill new party info");
		modal.getNameInput().fill(newPartyName);
		log.info("Fill endpoint value");
		modal.getEndpointInput().fill("http://" + newPartyName.toLowerCase() + ".com");
		log.info("Click ok button");
		modal.clickOK();
		page.wait.forXMillis(500);
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		page.wait.forXMillis(5000);
		log.info("Validate presence of success message");
		soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");
		log.info("Validate visibility of new party: " + newPartyName);
		soft.assertTrue(page.grid().scrollTo(partyName, newPartyName) >= 0, "New name is visible in grid.");
		soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPartyName + ".com") >= 0, "New endpoint is visible in grid.");
		soft.assertAll();
	}

	@Test(priority=6,description = "PMP-7", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void editPartyAndCancel() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);

		String newPartyName = Generator.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();
		log.info("login into application and navigate to pmode partie spage");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage page = new PModePartiesPage(driver);
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

	@Test(priority=7,description = "PMP-8", groups = {"multiTenancy"}, enabled = false)
	public void domainSegregation() throws Exception {
		String domainName = restUtils.getNonDefaultDomain();
		String domainCode = rest.getDomainCodeForName(domainName);
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", null);
		rest.pmode().uploadPMode("pmodes/doNothingSelfSending.xml", domainCode);

		SoftAssert soft = new SoftAssert();
		log.info("login into application and navigate to pmode parties page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage page = new PModePartiesPage(driver);
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


	@Test(priority=8,description = "PMP-21", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void partyAdditionCurrentPmode() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("upload Pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		log.info("Navigate to Pmode current page");
		new DomibusPage(driver).getSidebar().goToPage(PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new File("./src/main/resources/pmodes/Edelivery-blue.xml"));
		NodeList nodes = doc.getElementsByTagName("parties");

		for (int i = 0; i < nodes.getLength(); i++) {
			Element party = (Element) nodes.item(i);
			Element name = (Element) party.getElementsByTagName("party").item(0);
			if (name.getAttribute("name").equals(oldPartyName)) {
				log.info("Name attribute before update:" + name.getAttribute("name"));
				log.info("Endpoint attribute before update:" + name.getAttribute("endpoint"));
				name.setAttribute("name", newPartyName);
				name.setAttribute("endpoint", "http://localhost:8383/domibus/services/msh");
				log.info("Name attribute after update:" + name.getAttribute("name"));
				log.info("Endpoint attribute after update:" + name.getAttribute("endpoint"));
			}

			NodeList subchild = doc.getElementsByTagName("identifier");
			for (int j = 0; j < subchild.getLength(); j++) {
				Element identifier = (Element) nodes.item(i);
				Element partyId = (Element) identifier.getElementsByTagName("identifier").item(0);
				if (partyId.getAttribute("partyId").equals(oldPartyId)) {
					partyId.setAttribute("partyId", newPartyId);
				}
			}
		}
		doc.normalize();
		PModePartiesPage pmPage = new PModePartiesPage(driver);
		PModeCurrentPage PCpage = new PModeCurrentPage(driver);
		String updatedpmode = pmPage.printPmode(doc).replaceAll("\\t", " ");
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		pmPage.waitForPageToLoad();
		log.info("Edit current text");
		PCpage.getTextArea().fill(updatedpmode);
		log.info("Click on save button");
		PCpage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		log.info("Enter comment");
		modal.getDescriptionTextArea().fill("Red party is deleted");
		log.info("Click on Ok button");
		modal.clickOK();
		soft.assertFalse(page.getPage().getTextArea().getText().contains(oldPartyId), "Domibus-red party is deleted");
		soft.assertTrue(page.getPage().getTextArea().getText().contains(newPartyId), "black party still exists");
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		soft.assertTrue(page.grid().getIndexOf(0, oldPartyName) < 0, "Red_gw party is not available");

		TestServicePage tPage = new TestServicePage(driver);
		log.info("Navigate to Connections Monitoring page");
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
		tPage.waitForPageToLoad();
		log.info("Get all options from Responder drop down");
		List<String> options = tPage.getPartySelector().getOptionsTexts();
		log.info("Validate absence of Domibus-red");
		soft.assertFalse(options.contains(oldPartyId), "Red party is not present");
		soft.assertAll();

	}

	@Test(priority=9,description = "PMP-22", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void partyRemovalCurrentPmode() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("upload Pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		log.info("delete party");
		rest.pmodeParties().deleteParty("red_gw");

		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.grid().scrollTo("Party Name", oldPartyName) < 0, "Red_gw party is not available");

		TestServicePage tPage = new TestServicePage(driver);
		log.info("Navigate to Connections Monitoring page");
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

		log.info("Get all options from Responder drop down");
		List<String> options = tPage.getPartySelector().getOptionsTexts();
		log.info("Validate absence of domibus-red");
		soft.assertFalse(options.contains(oldPartyId), "Red party is not present");
		soft.assertAll();
	}

	@Test(priority=10,description = "PMP-23", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void partyAdditionOnPartiesPage() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		log.info("Navigate to Pmode parties page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
		SoftAssert soft = new SoftAssert();
		DomibusPage page = new DomibusPage(driver);
		PModePartiesPage pPage = new PModePartiesPage(driver);
		PModeArchivePage Apage = new PModeArchivePage(driver);
		PModeCurrentPage Cpage = new PModeCurrentPage(driver);

		log.info("Validate whether New button is enabled ");
		soft.assertTrue(pPage.getNewButton().isEnabled(), "New button is enabled");
		log.info("Click on New button");
		pPage.getNewButton().click();
		log.info("Generate random New Party Name");
		String newPatyName = Generator.randomAlphaNumeric(5);
		PartyModal modal = new PartyModal(driver);
		log.info("Fill New Party Form");
		modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");
		log.info("Click On Ok Button");
		modal.clickOK();
		pPage.wait.forXMillis(500);
		log.info("Click on Save button");
		pPage.getSaveButton().click();
		new Dialog(driver).confirm();
		pPage.wait.forXMillis(5000);
		log.info("Validate Success Message");
		soft.assertTrue(!pPage.getAlertArea().isError(), "page shows success message");
		log.info("Validate presence of New Party in Grid");
		soft.assertTrue(pPage.grid().scrollTo(partyName, newPatyName) >= 0, "party is shown in grid");
		log.info("Navigate to Pmode current page");
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		page.waitForPageToLoad();
		soft.assertTrue(Apage.getPage().getTextArea().isPresent(), "Current pmode is available");
		String UpdatedPmode = Apage.getPage().getTextArea().getText();
		log.info("Current Pmode is :" + UpdatedPmode);
		log.info("Validate presence of new party name in Current Pmode");
		soft.assertTrue(UpdatedPmode.contains(newPatyName), "New party is shown in Current pmode");
		page.refreshPage();
		soft.assertAll();

	}

	@Test(priority=11,description = "PMP-24", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void partyRemovalFromPartiesPage() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		log.info("Navigate to Current Pmode");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
		SoftAssert soft = new SoftAssert();
		DomibusPage page = new DomibusPage(driver);
		PModePartiesPage pPage = new PModePartiesPage(driver);
		PModeArchivePage Apage = new PModeArchivePage(driver);
		PModeCurrentPage Cpage = new PModeCurrentPage(driver);

		String DefaultPmode = Apage.getPage().getTextArea().getText();
		log.info("validate presence of Current system party name");
		soft.assertTrue(DefaultPmode.contains("<ns2:configuration xmlns:ns2=\"http://domibus.eu/configuration\" party=\"blue_gw\">"));
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		log.info("Select row for red_gw other than current system party name");
		for (int i = 0; i < pPage.grid().getRowsNo(); i++) {
			if (!pPage.grid().getRowInfo(i).containsValue(defaultPartyName)) {
				if (pPage.grid().getRowInfo(i).containsValue(oldPartyName)) {
					pPage.grid().selectRow(i);
				}
			}
		}
		log.info("Click on Delete button");
		pPage.getDeleteButton().click();
		log.info("Click on Save button");
		pPage.getSaveButton().click();
		new Dialog(driver).confirm();
		log.info(page.getAlertArea().getAlertMessage());
		log.info("Navigate to Pmode Current page");
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		page.waitForPageToLoad();
		soft.assertTrue(Apage.getPage().getTextArea().isPresent(), "Current pmode is available");
		String UpdatedPmode = Apage.getPage().getTextArea().getText();
		log.info("Current Pmode is :" + UpdatedPmode);
		log.info("Validate absence of party name :red_gw ");
		soft.assertFalse(UpdatedPmode.contains(oldPartyName), "red_gw party is not shown in Current pmode");
		soft.assertAll();

	}

	@Test(priority=12,description = "PMP-25", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void responderInitiatorRemovalFromPartiesPage() throws Exception {
		log.info("upload Pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		log.info("Navigate to Pmode Current page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
		DomibusPage page = new DomibusPage(driver);
		SoftAssert soft = new SoftAssert();

		log.info("Validate presence of red_gw in current pmode");
		PModePartiesPage pPage = new PModePartiesPage(driver);
		log.info("Navigate to Pmode parties page");
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		log.info("Find index of row having party name red_gw on Pmode parties page and select row");
		pPage.grid().selectRow(pPage.grid().getIndexOf(0, oldPartyName));
		log.info("Click on Edit button");
		pPage.getEditButton().click();
		PartyModal pmPage = new PartyModal((driver));
		log.info("Validate Ok button is enabled");
		soft.assertTrue(pmPage.getOkBtn().isEnabled());
		log.info("Uncheck Initiator & Responder checkbox");
		pmPage.clickIRCheckboxes();
		log.info("Click on Save button");
		pPage.getSaveButton().click();
		new Dialog(driver).confirm();
		System.out.println(pPage.getAlertArea().getAlertMessage());
		page.waitForPageToLoad();
		log.info("Navigate to Pmode Current page");
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		PModeCurrentPage Cpage = new PModeCurrentPage(driver);
		Cpage.waitForPageToLoad();
		String updatedPmode = Cpage.getTextArea().getText();
		log.info("Validate absence of red_gw as Initiator party");
		soft.assertFalse(updatedPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is not present in pmode");
		log.info("Validate absence of red_gw as Responder party");
		soft.assertFalse(updatedPmode.contains("<responderParty name=\"red_gw\"/>"), "red_gw responder party is not present in pmode");
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
		log.info("Navigating to Connections Monitoring page");
		TestServicePage tPage = new TestServicePage(driver);
		tPage.waitForPageToLoad();
		log.info("Get all options from Responder drop down");
		List<String> options = tPage.getPartySelector().getOptionsTexts();
		log.info("Validate presence of Domibus-blue");
		soft.assertTrue(options.contains(defaultPartyId), "blue party is present");
		log.info("Validate absence of Domibus-red");
		soft.assertFalse(options.contains(oldPartyId), "Red party is not present");
		soft.assertAll();
	}

	@Test(priority=13,description = "PMP-26", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void responderInitiatorAdditionOnPartiesPage() throws Exception {
		log.info("upload pmode");
		rest.pmode().uploadPMode("pmodes/NoResponderInitiator.xml", null);

		log.info("Navigate to Pmode current page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
		DomibusPage page = new DomibusPage(driver);
		SoftAssert soft = new SoftAssert();
		PModeCurrentPage Cpage = new PModeCurrentPage(driver);
		Cpage.waitForPageToLoad();
		String defaultPmode = Cpage.getTextArea().getText();
		log.info("Validate absence of red_gw as Initiator party");
		soft.assertFalse(defaultPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is present in pmode");
		log.info("Validate absence of red_gw as Responder party");
		soft.assertFalse(defaultPmode.contains("<responderParty name=\"red_gw\"/>\n"), "red_gw responder party is present in pmode");

		log.info("Navigate to Pmode Parties page");
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		PModePartiesPage pPage = new PModePartiesPage(driver);
		pPage.waitForPageToLoad();
		log.info("Find row number for party with name red_gw and select it");
		pPage.grid().selectRow(pPage.grid().getIndexOf(0, oldPartyName));
		log.info("Click on Edit button");
		pPage.getEditButton().click();
		PartyModal pmPage = new PartyModal((driver));
		log.info("Validate Ok button is enabled");
		soft.assertTrue(pmPage.getOkBtn().isEnabled());
		log.info("select checkbox for Initiator & Responder");
		pmPage.clickIRCheckboxes();
		log.info("Click on Save button");
		if (!pPage.getSaveButton().isEnabled()) log.warn("Save button not enabled");
		pPage.getSaveButton().click();
		new Dialog(driver).confirm();
		log.info(pPage.getAlertArea().getAlertMessage());
		page.waitForPageToLoad();
		log.info("Navigate to Pmode current page");
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		Cpage = new PModeCurrentPage(driver);
		Cpage.waitForPageToLoad();
		String updatedPmode = Cpage.getTextArea().getText();
		log.info("Validate presence of red_gw as Initiator party ");
		soft.assertTrue(updatedPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is  present in pmode");
		log.info("Validate presence of red_gw as responder party");
		soft.assertTrue(updatedPmode.contains("<responderParty name=\"red_gw\"/>"), "red_gw responder party is  present in pmode");
		log.info("Navigating to Connections Monitoring page");
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
		TestServicePage tPage = new TestServicePage(driver);
		tPage.waitForPageToLoad();
		log.info("Get all options from Responder drop down");
		List<String> options = tPage.getPartySelector().getOptionsTexts();
		log.info("Validate presence of Domibus-blue");
		soft.assertTrue(options.contains(defaultPartyId), "blue party is present");
		log.info("Validate absence of Domibus-red");
		soft.assertTrue(options.contains(oldPartyId), "Red party is  present");
		soft.assertAll();
	}

	@Test(priority=14,description = "PMP-27", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void initiatorResponderRemovalCurrentPmode() throws Exception {
		log.info("upload Pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		log.info("Navigate to Pmode Current page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
		DomibusPage page = new DomibusPage(driver);
		SoftAssert soft = new SoftAssert();
		PModePartiesPage pPage = new PModePartiesPage(driver);
		PModeCurrentPage PCpage = new PModeCurrentPage(driver);

		String defaultPmode = PCpage.getTextArea().getText();
		log.info("Replace initiator from red to green");
		String updatedPmodeInit = defaultPmode.replaceAll("\\t", " ").replaceAll("<initiatorParty name=\"red_gw\"/>", "<initiatorParty name=\"green_gw\"/>")
				.replaceAll("<responderParty name=\"red_gw\"/>", "<responderParty name=\"green_gw\"/>");
		log.info("Edit current text");
		PCpage.getTextArea().fill(updatedPmodeInit);
		log.info("Click on save button");
		PCpage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		log.info("Enter comment");
		modal.getDescriptionTextArea().fill("Initiator and Responder party name are updated");
		log.info("Click on Ok button");
		modal.clickOK();
		log.info("Validate non presence of red_gw");
		soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<initiatorParty name=\"red_gw\"/>"));
		log.info("Validate non presence of responder red_gw");
		soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<responderParty name=\"red_gw\"/>"), "red_gw is not present as Responder");
		log.info("navigate to Pmode parties page");
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
		log.info("Get index of row  with party detail red_gw");
		pPage.grid().selectRow(pPage.grid().getIndexOf(0, oldPartyName));
		log.info("Click on Edit button");
		pPage.getEditButton().click();
		PartyModal pmPage = new PartyModal((driver));
		log.info("Validate initiator checkbox status");
		soft.assertFalse(pmPage.getCheckboxStatus("Initiator"), "Initiator Checkbox is unchecked");
		log.info("Validate checkbox status of responder");
		soft.assertFalse(pmPage.getCheckboxStatus("Responder"), "Responder checkbox is unchecked");
		page.refreshPage();
		TestServicePage tPage = new TestServicePage(driver);
		log.info("Navigate to Connections Monitoring page");
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
		tPage.waitForPageToLoad();
		log.info("Get all options from Responder drop down");
		List<String> options = tPage.getPartySelector().getOptionsTexts();
		log.info("Validate presence of Domibus-blue");
		soft.assertTrue(options.contains(defaultPartyId), "blue party is present");
		log.info("Validate absence of Domibus-red");
		soft.assertFalse(options.contains(oldPartyId), "Red party is  present");
		soft.assertAll();
	}

	@Test(priority=15,description = "PMP-28", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void IRRemovalCurrentPmode() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("upload Pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		log.info("Navigate to Pmode Parties page");
		PModePartiesPage page = new PModePartiesPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		page.grid().waitForRowsToLoad();
		page.grid().scrollToAndDoubleClick("Party Name" , "red_gw");

		PPartyModal modal = new PPartyModal(driver);

		modal.processSection.editForProcess("tc1Process", false, false);
		modal.clickOK();

		page.getSaveButton().click();
		new Dialog(driver).confirm();

		TestServicePage tPage = new TestServicePage(driver);
		log.info("Navigate to Connections Monitoring page");
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

		log.info("Get all options from Responder drop down");
		List<String> options = tPage.getPartySelector().getOptionsTexts();
		log.info("Validate presence of Domibus-blue");
		soft.assertTrue(options.contains(defaultPartyId), "blue party is present");
		log.info("Validate absence of Domibus-red");
		soft.assertFalse(options.contains(oldPartyId), "Red party is  present");
		soft.assertAll();
	}

	@Test(priority=16,description = "PMP-29", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void initiatorResponderAdditionCurrentPmode() throws Exception {
		log.info("upload Pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		DomibusPage page = new DomibusPage(driver);
		SoftAssert soft = new SoftAssert();
		PModePartiesPage pPage = new PModePartiesPage(driver);
		PModeCurrentPage PCpage = new PModeCurrentPage(driver);
		log.info("Navigate to Pmode Current page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
		String defaultPmode = pPage.getPage().getTextArea().getText();
		log.info("Replace initiator from red to green");
		String updatedPmodeInit = defaultPmode.replaceAll("\\t", " ").replaceAll(oldPartyName, newPartyName).replaceAll(oldPartyId, newPartyId);
		log.info("Edit current text");
		PCpage.getTextArea().fill(updatedPmodeInit);
		log.info("Click on save button");
		PCpage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		log.info("Enter comment");
		modal.getDescriptionTextArea().fill("Initiator and responder  party name and party id are updated");
		log.info("Click on Ok button");
		modal.clickOK();

		log.info("navigate to Pmode parties page");
		page.getSidebar().goToPage(PAGES.PMODE_PARTIES);

		log.info("Get index of row  with party detail green_gw");
		log.info("Select row other than blue_gw");
		for (int i = 0; i < pPage.grid().getRowsNo(); i++) {
			if (!pPage.grid().getRowInfo(i).containsValue(defaultPartyName)) {
				pPage.grid().selectRow(i);
			}
		}
		log.info("Click on Edit button");
		pPage.getEditButton().click();
		PartyModal pmPage = new PartyModal((driver));
		log.info("Validate initiator checkbox status");
		soft.assertFalse(pmPage.getCheckboxStatus("Initiator"), "Initiator Checkbox is unchecked");
		soft.assertFalse(pmPage.getCheckboxStatus("Responder"), "Responder Checkbox is unchecked");
		pPage.refreshPage();

		log.info("Validate presence of Domibus-black");
		soft.assertTrue(isPartyPresentInConMon(newPartyName), "Black party is  present");
		soft.assertAll();
	}

	@Test(priority=17,description = "PMP-30", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void IRAdditionCurrentPmode() throws Exception {
		log.info("upload Pmode");
		rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);

		log.info("Navigate to Pmode Current page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
		DomibusPage page = new DomibusPage(driver);
		SoftAssert soft = new SoftAssert();
		PModePartiesPage pPage = new PModePartiesPage(driver);
		PModeCurrentPage PCpage = new PModeCurrentPage(driver);
		String defaultPmode = pPage.getPage().getTextArea().getText();
		log.info("Replace initiator from red to green");
		String updatedPmodeInit = defaultPmode.replaceAll("\\t", " ").replaceAll(oldPartyName, newPartyName)
				.replaceAll(oldPartyId, newPartyId);
		log.info("Edit current text");
		PCpage.getTextArea().fill(updatedPmodeInit);
		log.info("Click on save button");
		PCpage.getSaveBtn().click();
		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		log.info("Enter comment");
		modal.getDescriptionTextArea().fill("Initiator and responder  party name and party id are updated");
		log.info("Click on Ok button");
		modal.clickOK();
		log.info("Validate non presence of red_gw");
		soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<initiatorParty name=\"red_gw\"/>"));
		soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<responderParty name=\"red_gw\"/>"));
		soft.assertTrue(pPage.getPage().getTextArea().getText().contains("<initiatorParty name=\"black_gw\"/>"));
		soft.assertTrue(pPage.getPage().getTextArea().getText().contains("<responderParty name=\"black_gw\"/>"));
		soft.assertTrue(pPage.getPage().getTextArea().getText().contains(newPartyId));

		log.info("Check parties on Connection Monitoring page");
		soft.assertTrue(isPartyPresentInConMon(newPartyName), "New party is present in Connection Monitoring page");
		soft.assertFalse(isPartyPresentInConMon(oldPartyName), "Old party is NOT present in Connection Monitoring page");


		soft.assertAll();
	}

	private boolean isPartyPresentInConMon(String partyName) throws Exception{
		log.info("Searching for party with name" + partyName);
		JSONArray parties = rest.connMonitor().getConnectionMonitoringParties();

		for (int i = 0; i < parties.length(); i++) {
			JSONObject party = parties.getJSONObject(i);
			if(StringUtils.equalsIgnoreCase(party.getString("name"), partyName)){
				log.info("Party found!");
				return true;
			}
		}
		log.info("Party not found!");
		return false;
	}



}


