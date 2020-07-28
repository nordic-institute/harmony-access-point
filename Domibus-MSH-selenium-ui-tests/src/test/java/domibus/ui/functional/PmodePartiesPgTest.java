package domibus.ui.functional;

import ddsl.dcomponents.AlertArea;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DObject;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import pages.pmode.PartiesFilters;
import utils.BaseTest;
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

public class PmodePartiesPgTest extends BaseTest {

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

    @Test(priority=1,description = "PMP-2", groups = {"multiTenancy", "singleTenancy"})
    public void filterParties() throws Exception {

        log.info("upload pmode");
        rest.uploadPMode("pmodes/multipleParties.xml", null);
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
        log.info("Click on serach button");
        page.filters().getSearchButton().click();
        log.info("Wait for rows to load");
        page.grid().waitForRowsToLoad();
        soft.assertEquals(page.grid().getRowsNo(), 1, "1 rows returned");
        soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "first party is returned");
        soft.assertAll();
    }

    @Test(priority=2,description = "PMP-4", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
    public void doubleClickRow() throws Exception {

        log.info("uploade pmode");
        rest.uploadPMode("pmodes/multipleParties.xml", null);

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

    @Test(priority=3,description = "PMP-5", groups = {"multiTenancy", "singleTenancy"})
    public void deleteParty() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/multipleParties.xml", null);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);
        log.info("Extract data for row 0");
        HashMap<String, String> firstParty = page.grid().getRowInfo(0);

        log.info("Select row0");
        page.grid().selectRow(0);

        log.info("Click delete button");
        page.getDeleteButton().click();

        soft.assertTrue(page.getSaveButton().isEnabled(), "Save button is active");
        soft.assertTrue(page.getCancelButton().isEnabled(), "Cancel button is active");

        log.info("Click on Cancel button");
        page.getCancelButton().click();

        log.info("validate presence of first party after cancellation");
        soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "After cancel party is still present in grid");

        log.info("select row0");
        page.grid().selectRow(0);

        log.info("Click delete button");
        page.getDeleteButton().click();

        log.info("Click on Save button");
        page.getSaveButton().click();
        page.grid().waitForRowsToLoad();

        log.info("Validate absence of first party from grid data");
        soft.assertTrue(page.grid().scrollTo(partyName, firstParty.get(partyName)) == -1, "After save party is NOT present in grid");
        soft.assertAll();
    }

    @Test(priority=4,description = "PMP-6", groups = {"multiTenancy", "singleTenancy"})
    public void createParty() throws Exception {
        log.info("Upload pmode");
        rest.uploadPMode("pmodes/multipleParties.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);
        SoftAssert soft = new SoftAssert();
        log.info("login into application and navigate to Pmode parties page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
        PModePartiesPage page = new PModePartiesPage(driver);
        log.info("Validate new button is enabled");
        soft.assertTrue(page.getNewButton().isEnabled(), "New button is enabled");
        log.info("Click on New button");
        page.getNewButton().click();
        PartyModal modal = new PartyModal(driver);
        log.info("Fill new party info");
        modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");
        log.info("Click ok button");
        modal.clickOK();
        page.wait.forXMillis(1000);
        page.getSaveButton().click();
        page.wait.forXMillis(5000);
        log.info("validate presence of success message");
        soft.assertTrue(!page.getAlertArea().isError(), "page shows success message");
        soft.assertTrue(StringUtils.equalsIgnoreCase(page.getAlertArea().getAlertMessage(),
                DMessages.PMODE_PARTIES_UPDATE_SUCCESS), "page shows correct success message");
        soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) >= 0, "party is shown in grid");
        soft.assertAll();
    }

    @Test(priority=5,description = "PMP-7", groups = {"multiTenancy", "singleTenancy"})
    public void editParty() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/multipleParties.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);
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
        modal.getNameInput().fill(newPatyName);
        log.info("Fill endpint value");
        modal.getEndpointInput().fill("http://" + newPatyName.toLowerCase() + ".com");
        log.info("Click ok button");
        modal.clickOK();
        page.wait.forXMillis(1000);
        page.getSaveButton().click();
        page.wait.forXMillis(5000);
        log.info("Validate presence of success message");
        soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");
        log.info("Validate visbility of new party");
        soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) >= 0, "New name is visible in grid");
        soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPatyName + ".com") >= 0, "New endpoint is visible in grid");
        soft.assertAll();
    }

    @Test(priority=6,description = "PMP-9", groups = {"multiTenancy", "singleTenancy"})
    public void editPartyAndCancel() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/multipleParties.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);
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
        modal.getNameInput().fill(newPatyName);
        log.info("fill end point ");
        modal.getEndpointInput().fill("http://" + newPatyName.toLowerCase() + ".com");
        log.info("click ok button");
        modal.clickOK();
        log.info("Click Cancel button ");
        page.getCancelButton().click();
        log.info("Validate non visibility of new party after cancellation");
        soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) < 0, "New name is NOT visible in grid");
        soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPatyName + ".com") < 0, "New endpoint is NOT visible in grid");
        soft.assertAll();
    }

    @Test(priority=7,description = "PMP-10", groups = {"multiTenancy"})
    public void domainSegregation() throws Exception {
        String domainName = rest.getDomainNames().get(1);
        String domainCode = rest.getDomainCodeForName(domainName);
        rest.uploadPMode("pmodes/multipleParties.xml", null);
        rest.uploadPMode("pmodes/doNothingSelfSending.xml", domainCode);
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


    @Test(priority=8,description = "PMP-21", groups = {"multiTenancy", "singleTenancy"})
    public void partyAdditionCurrentPmode() throws Exception {
        log.info("Navigate to Pmode current page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModePartiesPage pPage = new PModePartiesPage(driver);
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
        pmPage.waitForTitle();
        log.info("Edit current text");
        PCpage.getTextArea().fill(updatedpmode);
        log.info("Click on save button");
        PCpage.getSaveBtn().click();
        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        log.info("Enter comment");
        pPage.getModal().getDescriptionTextArea().fill("Red party is deleted");
        log.info("Click on Ok button");
        modal.clickOK();
        soft.assertFalse(pPage.getPage().getTextArea().getText().contains(oldPartyId), "Domibus-red party is deleted");
        soft.assertTrue(pPage.getPage().getTextArea().getText().contains(newPartyId), "black party still exists");
        page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
        soft.assertTrue(pPage.grid().getIndexOf(0, oldPartyName) < 0, "Red_gw party is not available");
        TestServicePage tPage = new TestServicePage(driver);
        log.info("Navigate to Test service page");
        page.getSidebar().goToPage(PAGES.TEST_SERVICE);
        tPage.waitForTitle();
        log.info("Get all options from Responder drop down");
        List<String> options = tPage.getPartySelector().getOptionsTexts();
        log.info("Validate absence of Domibus-red");
        soft.assertFalse(options.contains(oldPartyId), "Red party is not present");
        soft.assertAll();

    }

    @Test(priority=9,description = "PMP-22", groups = {"multiTenancy", "singleTenancy"})
    public void partyRemovalCurrentPmode() throws Exception {
        log.info("Navigate to Pmode current page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModePartiesPage pPage = new PModePartiesPage(driver);

        String defaultPmode = pPage.getPage().getTextArea().getText();
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File("./src/main/resources/pmodes/Edelivery-blue.xml"));
        NodeList nodes = doc.getElementsByTagName("parties");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element partyId = (Element) nodes.item(i);
            Element name = (Element) partyId.getElementsByTagName("party").item(0);
            System.out.println(name.getAttribute("name"));
            if (name.getAttribute("name").equals(oldPartyName)) {
                name.getParentNode().removeChild(name);
            }
        }
        doc.normalize();
        NodeList nodes1 = doc.getElementsByTagName("party");
        log.info("Party count :" + nodes.getLength());
        PModePartiesPage pmPage = new PModePartiesPage(driver);
        PModeCurrentPage PCpage = new PModeCurrentPage(driver);
        log.info("Pmode after red_gw party deletion:" + pmPage.printPmode(doc));
        String updatedpmode = pmPage.printPmode(doc).replaceAll("\\t", " ");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        pmPage.waitForTitle();
        log.info("Edit current text");
        PCpage.getTextArea().fill(updatedpmode);
        log.info("Click on save button");
        PCpage.getSaveBtn().click();
        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        log.info("Enter comment");
        pPage.getModal().getDescriptionTextArea().fill("Red party is deleted");
        log.info("Click on Ok button");
        modal.clickOK();
        soft.assertFalse(pPage.getPage().getTextArea().getText().contains(oldPartyId), "Domibus-red party is deleted");
        soft.assertTrue(pPage.getPage().getTextArea().getText().contains("domibus-blue"), "Blue party still exists");
        page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
        soft.assertTrue(pPage.grid().getIndexOf(0, oldPartyName) < 0, "Red_gw party is not available");
        TestServicePage tPage = new TestServicePage(driver);
        log.info("Navigate to Test service page");
        page.getSidebar().goToPage(PAGES.TEST_SERVICE);
        tPage.waitForTitle();
        log.info("Get all options from Responder drop down");
        List<String> options = tPage.getPartySelector().getOptionsTexts();
        log.info("Validate absence of Domibus-red");
        soft.assertFalse(options.contains(oldPartyId), "Red party is not present");
        soft.assertAll();
    }

    @Test(priority=10,description = "PMP-23", groups = {"multiTenancy", "singleTenancy"})
    public void partyAdditionOnPartiesPage() throws Exception {
        log.info("Navigate to Pmode parties page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
        log.info("upload pmode");
        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
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
        pPage.wait.forXMillis(1000);
        log.info("Click on Save button");
        pPage.getSaveButton().click();
        pPage.wait.forXMillis(5000);
        log.info("Validate Success Message");
        soft.assertTrue(!pPage.getAlertArea().isError(), "page shows success message");
        log.info("Validate presence of New Party in Grid");
        soft.assertTrue(pPage.grid().scrollTo(partyName, newPatyName) >= 0, "party is shown in grid");
        log.info("Navigate to Pmode current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        page.waitForTitle();
        soft.assertTrue(Apage.getPage().getTextArea().isPresent(), "Current pmode is available");
        String UpdatedPmode = Apage.getPage().getTextArea().getText();
        log.info("Current Pmode is :" + UpdatedPmode);
        log.info("Validate presence of new party name in Current Pmode");
        soft.assertTrue(UpdatedPmode.contains(newPatyName), "New party is shown in Current pmode");
        page.refreshPage();
        soft.assertAll();

    }

    @Test(priority=11,description = "PMP-24", groups = {"multiTenancy", "singleTenancy"})
    public void partyRemovalFromPartiesPage() throws Exception {
        log.info("Navigate to Current Pmode");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
        log.info("upload pmode");
        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
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
        log.info(page.getAlertArea().getAlertMessage());
        log.info("Navigate to Pmode Current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        page.waitForTitle();
        soft.assertTrue(Apage.getPage().getTextArea().isPresent(), "Current pmode is available");
        String UpdatedPmode = Apage.getPage().getTextArea().getText();
        log.info("Current Pmode is :" + UpdatedPmode);
        log.info("Validate absence of party name :red_gw ");
        soft.assertFalse(UpdatedPmode.contains(oldPartyName), "red_gw party is not shown in Current pmode");
        soft.assertAll();

    }

    @Test(priority=12,description = "PMP-25", groups = {"multiTenancy", "singleTenancy"})
    public void responderInitiatorRemovalFromPartiesPage() throws Exception {
        log.info("Navigate to Pmode Current page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModeArchivePage Apage = new PModeArchivePage(driver);
        String defaultPmode = Apage.getPage().getTextArea().getText();
        log.info("Validate presence of red_gw in current pmode");
        PModePartiesPage pPage = new PModePartiesPage(driver);
        PModeCurrentPage Cpage = new PModeCurrentPage(driver);
        log.info("Navigate to Pmode parties page");
        page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
        log.info("Find index of row having party name red_gw on Pmode parties page and select row");
        pPage.grid().selectRow(pPage.grid().getIndexOf(0, oldPartyName));
        log.info("Click on Edit button");
        pPage.getEditButton().click();
        PartyModal pmPage = new PartyModal((driver));
        log.info("Validate Ok button is enabled");
        soft.assertTrue(pmPage.getOkButton().isEnabled());
        log.info("Uncheck Initiator & Responder checkbox");
        pmPage.clickIRCheckboxes();
        log.info("Click on Save button");
        pPage.getSaveButton().click();
        System.out.println(pPage.getAlertArea().getAlertMessage());
        page.waitForTitle();
        log.info("Naviagte to Pmode Current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        String updatedPmode = Apage.getPage().getTextArea().getText();
        log.info("Validate absence of red_gw as Initiator party");
        soft.assertFalse(updatedPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is not present in pmode");
       log.info("Validate absence of red_gw as Responder party");
        soft.assertFalse(updatedPmode.contains("<responderParty name=\"red_gw\"/>"), "red_gw responder party is not present in pmode");
        page.getSidebar().goToPage(PAGES.TEST_SERVICE);
        log.info("Navigating to Test Service page");
        TestServicePage tPage = new TestServicePage(driver);
        tPage.waitForTitle();
        log.info("Get all options from Responder drop down");
        List<String> options = tPage.getPartySelector().getOptionsTexts();
        log.info("Validate presence of Domibus-blue");
        soft.assertTrue(options.contains(defaultPartyId), "blue party is present");
        log.info("Validate absence of Domibus-red");
        soft.assertFalse(options.contains(oldPartyId), "Red party is not present");
        soft.assertAll();
    }

    @Test(priority=13,description = "PMP-26", groups = {"multiTenancy", "singleTenancy"})
    public void responderInitiatorAdditionOnPartiesPage() throws Exception {
        log.info("Nvaiagte to Pmode current page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
        log.info("upload pmode");
        rest.uploadPMode("pmodes/NoResponderInitiator.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModeArchivePage Apage = new PModeArchivePage(driver);
        String defaultPmode = Apage.getPage().getTextArea().getText();
        log.info("Validate absence of red_gw as Initiator party");
        soft.assertFalse(defaultPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is present in pmode");
        log.info("Validate absence of red_gw as Responder party");
        soft.assertFalse(defaultPmode.contains("<responderParty name=\"red_gw\"/>\n"), "red_gw responder party is present in pmode");
        PModePartiesPage pPage = new PModePartiesPage(driver);
        PModeCurrentPage Cpage = new PModeCurrentPage(driver);
        log.info("Naviagte to Pmode Parties page");
        page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
        log.info("Find row number for party with nae red_gw and select it");
        pPage.grid().selectRow(pPage.grid().getIndexOf(0, oldPartyName));
        log.info("Click on Edit button");
        pPage.getEditButton().click();
        PartyModal pmPage = new PartyModal((driver));
        log.info("Validate Ok button is enabled");
        soft.assertTrue(pmPage.getOkButton().isEnabled());
        log.info("select checkbox for Initiator & Responder");
        pmPage.clickIRCheckboxes();
        log.info("Click on Save button");
        pPage.getSaveButton().click();
        log.info(pPage.getAlertArea().getAlertMessage());
        page.waitForTitle();
        log.info("Naviagte to Pmode current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        String updatedPmode = Apage.getPage().getTextArea().getText();
        log.info("Validate presence of red_gw as Initiator party ");
        soft.assertTrue(updatedPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is  present in pmode");
        log.info("Validate presence of red_gw as responder party");
        soft.assertTrue(updatedPmode.contains("<responderParty name=\"red_gw\"/>"), "red_gw responder party is  present in pmode");
        page.getSidebar().goToPage(PAGES.TEST_SERVICE);
        log.info("Navigating to Test Service page");
        TestServicePage tPage = new TestServicePage(driver);
        tPage.waitForTitle();
        log.info("Get all options from Responder drop down");
        List<String> options = tPage.getPartySelector().getOptionsTexts();
        log.info("Validate presence of Domibus-blue");
        soft.assertTrue(options.contains(defaultPartyId), "blue party is present");
        log.info("Validate absence of Domibus-red");
        soft.assertTrue(options.contains(oldPartyId), "Red party is  present");
        soft.assertAll();
    }

    @Test(priority=14,description = "PMP-27", groups = {"multiTenancy", "singleTenancy"})
    public void initiatorResponderRemovalCurrentPmode() throws Exception {
        log.info("Navigate to Pmode Current page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModePartiesPage pPage = new PModePartiesPage(driver);
        PModeCurrentPage PCpage = new PModeCurrentPage(driver);

        String defaultPmode = pPage.getPage().getTextArea().getText();
        log.info("Replace initiator from red to green");
        String updatedPmodeInit = defaultPmode.replaceAll("\\t", " ").replaceAll("<initiatorParty name=\"red_gw\"/>", "<initiatorParty name=\"green_gw\"/>")
                .replaceAll("<responderParty name=\"red_gw\"/>", "<responderParty name=\"green_gw\"/>");
        log.info("Edit current text");
        PCpage.getTextArea().fill(updatedPmodeInit);
        log.info("Click on save button");
        PCpage.getSaveBtn().click();
        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        log.info("Enter comment");
        pPage.getModal().getDescriptionTextArea().fill("Initiator and Responder party name are updated");
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
        log.info("Navigate to Test service page");
        page.getSidebar().goToPage(PAGES.TEST_SERVICE);
        tPage.waitForTitle();
        log.info("Get all options from Responder drop down");
        List<String> options = tPage.getPartySelector().getOptionsTexts();
        log.info("Validate presence of Domibus-blue");
        soft.assertTrue(options.contains(defaultPartyId), "blue party is present");
        log.info("Validate absence of Domibus-red");
        soft.assertFalse(options.contains(oldPartyId), "Red party is  present");
        soft.assertAll();
    }

    @Test(priority=15,description = "PMP-28", groups = {"multiTenancy", "singleTenancy"})
    public void IRRemovalCurrentPmode() throws Exception {
        log.info("Navigate to Pmode Current page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModePartiesPage pPage = new PModePartiesPage(driver);
        PModeCurrentPage PCpage = new PModeCurrentPage(driver);
        String defaultPmode = pPage.getPage().getTextArea().getText();
        log.info("Replace initiator from red to green");
        String updatedPmodeInit = defaultPmode.replaceAll("\\t", " ").replaceAll("<initiatorParty name=\"red_gw\"/>", "<initiatorParty name=\"green_gw\"/>")
                .replaceAll("<responderParty name=\"red_gw\"/>", "<responderParty name=\"green_gw\"/>");
        log.info("Edit current text");
        PCpage.getTextArea().fill(updatedPmodeInit);
        log.info("Click on save button");
        PCpage.getSaveBtn().click();
        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        log.info("Enter comment");
        pPage.getModal().getDescriptionTextArea().fill("Initiator And Responder  party name are updated");
        log.info("Click on Ok button");
        modal.clickOK();
        log.info("Validate non presence of red_gw");
        soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<initiatorParty name=\"red_gw\"/>"));
        log.info("Validate non presence of responder red_gw");
        soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<responderParty name=\"red_gw\"/>"), "red_gw is not present as Responder");
        log.info("Navigate to Pmode parties");
        TestServicePage tPage = new TestServicePage(driver);
        log.info("Navigate to Test service page");
        page.getSidebar().goToPage(PAGES.TEST_SERVICE);
        tPage.waitForTitle();
        log.info("Get all options from Responder drop down");
        List<String> options = tPage.getPartySelector().getOptionsTexts();
        log.info("Validate presence of Domibus-blue");
        soft.assertTrue(options.contains(defaultPartyId), "blue party is present");
        log.info("Validate absence of Domibus-red");
        soft.assertFalse(options.contains(oldPartyId), "Red party is  present");
        soft.assertAll();
    }

    @Test(priority=16,description = "PMP-29", groups = {"multiTenancy", "singleTenancy"})
    public void initiatorResponderAdditionCurrentPmode() throws Exception {
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
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
        pPage.getModal().getDescriptionTextArea().fill("Initiator and responder  party name and party id are updated");
        log.info("Click on Ok button");
        modal.clickOK();
        log.info("Validate non presence of red_gw");
        soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<initiatorParty name=\"red_gw\"/>"));
        soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<responderParty name=\"red_gw\"/>"));
        soft.assertTrue(pPage.getPage().getTextArea().getText().contains("<initiatorParty name=\"black_gw\"/>"));
        soft.assertTrue(pPage.getPage().getTextArea().getText().contains("<responderParty name=\"black_gw\"/>"));
        soft.assertTrue(pPage.getPage().getTextArea().getText().contains(newPartyId));
        log.info("navigate to Pmode parties page");
        page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
        page.waitForTitle();
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
        pmPage.refreshPage();
        TestServicePage tPage = new TestServicePage(driver);
        log.info("Navigate to Test service page");
        page.getSidebar().goToPage(PAGES.TEST_SERVICE);
        tPage.waitForTitle();
        log.info("Get all options from Responder drop down");
        List<String> options = tPage.getPartySelector().getOptionsTexts();
        log.info("Validate presence of Domibus-blue");
        soft.assertTrue(options.contains(defaultPartyId), "Blue party is present");
        log.info("Validate absence of Domibus-black");
        soft.assertTrue(options.contains(newPartyId), "Black party is  present");
        soft.assertAll();
    }

    @Test(priority=17,description = "PMP-30", groups = {"multiTenancy", "singleTenancy"})
    public void IRAdditionCurrentPmode() throws Exception {
        log.info("Navigate to Pmode Current page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
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
        pPage.getModal().getDescriptionTextArea().fill("Initiator and responder  party name and party id are updated");
        log.info("Click on Ok button");
        modal.clickOK();
        log.info("Validate non presence of red_gw");
        soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<initiatorParty name=\"red_gw\"/>"));
        soft.assertFalse(pPage.getPage().getTextArea().getText().contains("<responderParty name=\"red_gw\"/>"));
        soft.assertTrue(pPage.getPage().getTextArea().getText().contains("<initiatorParty name=\"black_gw\"/>"));
        soft.assertTrue(pPage.getPage().getTextArea().getText().contains("<responderParty name=\"black_gw\"/>"));
        soft.assertTrue(pPage.getPage().getTextArea().getText().contains(newPartyId));
        log.info("navigate to Pmode parties page");
        TestServicePage tPage = new TestServicePage(driver);
        log.info("Navigate to Test service page");
        page.getSidebar().goToPage(PAGES.TEST_SERVICE);
        tPage.waitForTitle();
        log.info("Get all options from Responder drop down");
        List<String> options = tPage.getPartySelector().getOptionsTexts();
        log.info("Validate presence of Domibus-blue");
        soft.assertTrue(options.contains(defaultPartyId), "Blue party is present");
        log.info("Validate absence of Domibus-black");
        soft.assertTrue(options.contains(newPartyId), "Black party is  present");
        soft.assertAll();
    }

    /* This method will check successful deletion of all parties except the one which defines system */
    @Test(description = "PMP-32", groups = {"multiTenancy", "singleTenancy"})
    public void deleteAllParty() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login and Navigate to Pmode Current page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);
        DomibusPage page = new DomibusPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_PARTIES);
        PModePartiesPage pPage = new PModePartiesPage(driver);
        log.info("Extract system party name from current pmode");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().
                parse(new File("./src/main/resources/pmodes/Edelivery-blue.xml"));

        String systemParty = doc.getDocumentElement().getAttribute("party");
        String path = "pmodes/Edelivery-blue.xml";
        //for multitenancy
        if (data.isMultiDomain()) {
            List<String> domains = rest.getDomainNames();
            for (String domain : domains) {
                log.info("verify and select current domain :" + domain);
                page.getDomainSelector().selectOptionByText(domain);
                page.waitForTitle();
                log.info("upload Pmode");
                rest.uploadPMode("pmodes/Edelivery-blue.xml", pPage.getDomainFromTitle());
                log.info("Total number of available pmode parties");
                int count = pPage.grid().getPagination().getTotalItems();
                partyDelAlertMsg(soft, pPage, systemParty,count);
            }
        }
        //single tenancy
        else {
            log.info("upload Pmode");
            rest.uploadPMode("pmodes/Edelivery-blue.xml", pPage.getDomainFromTitle());
            log.info("Total number of available pmode parties");
            int count = pPage.grid().getPagination().getTotalItems();
            partyDelAlertMsg(soft, pPage, systemParty,count);
        }
        soft.assertAll();

    }

    /* This method will verify search with forbidden char  */
    @Test(description = "PMP-31", groups = {"multiTenancy", "singleTenancy"})
    public void searchWithForbiddenChar() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login and Navigate to Pmode Parties page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_PARTIES);
        PModePartiesPage page= new PModePartiesPage(driver);
        PartiesFilters pfPage= new PartiesFilters(driver);
        String searchData="'\\u0022(){}[];,+=%&*#<>/\\\\";
        AlertArea apage = new AlertArea(driver);

        //for multitenancy
        if (data.isMultiDomain()) {
            List<String> domains = rest.getDomainNames();
            for (String domain : domains) {
                log.info("verify and select current domain :" + domain);
                page.getDomainSelector().selectOptionByText(domain);
                page.waitForTitle();
                log.info("Validate searching" + page.getDomainFromTitle());
                soft.assertTrue(validateSearchForForbiddenChar(page, searchData)==0,"Blank grid is shown");
                soft.assertFalse(new DObject(driver, apage.alertMessage).isPresent(), "No alert message is shown for forbidden char");

            }
        }
        //single tenancy
        else {
            log.info("Validate searching for single tenancy ");
            soft.assertTrue(validateSearchForForbiddenChar(page, searchData)==0,"Blank grid is shown");
            soft.assertFalse(new DObject(driver, apage.alertMessage).isPresent(), "No alert message is shown for forbidden char");

        }

        soft.assertAll();


    }
    private int validateSearchForForbiddenChar(PModePartiesPage page,String data) throws Exception{
        log.info("Search with default forbidden char");
        page.filters().getNameInput().fill(data);
        page.filters().getEndpointInput().fill(data);
        page.filters().getPartyIDInput().fill(data);
        page.filters().getProcessInput().fill(data);

        log.info("Click on search button");
        page.filters().getSearchButton().click();
        return page.grid().getPagination().getTotalItems();
    }

    private void partyDelAlertMsg(SoftAssert soft, PModePartiesPage pPage,String systemParty,int partyCount) throws Exception {

        for (int i = partyCount - 1; i >= 0; i--) {

            String selectedParty = pPage.grid().getRowInfo(i).get("Party Name");
            log.info("party name" + selectedParty + "for row " + i + pPage.getDomainFromTitle());

            pPage.grid().selectRow(i);
            pPage.getDeleteButton().click();
            pPage.getSaveButton().click();

            soft.assertTrue(new DObject(driver, new AlertArea(driver).alertMessage).isPresent(), "Alert is present on deletion");
            if (selectedParty.equals(systemParty)) {
                soft.assertTrue(pPage.getAlertArea().alertMessage.getText().contains("Party update error"));
            } else {
                soft.assertTrue(pPage.getAlertArea().alertMessage.getText().contains("Parties saved successfully"));
            }
            pPage.refreshPage();
            pPage.grid().waitForRowsToLoad();
        }
    }


}



