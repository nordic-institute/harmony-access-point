package domibus.ui;

import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DOMIBUS_PAGES;
import org.apache.poi.util.StringUtil;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.PModeCurrentPage;
import pages.pmode.PModePartiesPage;
import pages.pmode.PartyModal;
import utils.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class PModePartiesPgTest extends BaseTest {

	private static String partyName = "Party Name";
	private static String endpoint = "End Point";
	private static String partyID = "Party Id";
	private static String process = "Process (I=Initiator, R=Responder, IR=Both)";


	@Test(description = "PMP-1", groups = {"multiTenancy", "singleTenancy"})
	public void openPModePartiesPage() throws Exception {

		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);

		soft.assertTrue(page.filters().getNameInput().isEnabled(), "Page contains filter for party name");
		soft.assertTrue(page.filters().getEndpointInput().isEnabled(), "Page contains filter for party endpoint");
		soft.assertTrue(page.filters().getPartyIDInput().isEnabled(), "Page contains filter for party id");
		soft.assertTrue(page.filters().getProcessRoleSelect().isDisplayed(), "Page contains filter for process role");

		List<HashMap<String, String>> partyInfo = page.grid().getAllRowInfo();
		soft.assertTrue(partyInfo.size()==2, "Grid contains both the parties described in PMode file");
//		TODO: add checks that the info listed is the same as in file

		soft.assertTrue(!page.getCancelButton().isEnabled(), "Cancel button is NOT enabled");
		soft.assertTrue(!page.getSaveButton().isEnabled(), "Save button is NOT enabled");
		soft.assertTrue(!page.getEditButton().isEnabled(), "Edit button is NOT enabled");
		soft.assertTrue(!page.getDeleteButton().isEnabled(), "Delete button is NOT enabled");
		soft.assertTrue(page.getNewButton().isEnabled(), "New button is enabled");

		soft.assertAll();

	}

	@Test(description = "PMP-1.1", groups = {"multiTenancy", "singleTenancy"})
	public void selectRow() throws Exception {

		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);

		soft.assertTrue(!page.getEditButton().isEnabled(), "Edit button is not enabled");
		soft.assertTrue(!page.getDeleteButton().isEnabled(), "Delete button is not enabled");

		page.grid().selectRow(0);

		soft.assertTrue(page.getEditButton().isEnabled(), "Edit button is enabled after select row");
		soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled after select row");

		soft.assertAll();

	}

	@Test(description = "PMP-2", groups = {"multiTenancy", "singleTenancy"})
	public void filterParties() throws Exception {

		rest.uploadPMode("pmodes/multipleParties.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);

		soft.assertTrue(page.filters().getNameInput().isEnabled(), "Page contains filter for party name");

		HashMap<String, String> firstParty = page.grid().getRowInfo(0);

		page.filters().getNameInput().fill(firstParty.get(partyName));
		page.filters().getEndpointInput().fill(firstParty.get(endpoint));
		page.filters().getPartyIDInput().fill(firstParty.get(partyID));
		page.filters().getSearchButton().click();

		soft.assertEquals(page.grid().getRowsNo(), 1, "1 rows returned");
		soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "first party is returned");


		soft.assertAll();
	}

	@Test(description = "PMP-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickRow() throws Exception {

		rest.uploadPMode("pmodes/multipleParties.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);

		HashMap<String, String> firstParty = page.grid().getRowInfo(0);
		page.grid().doubleClickRow(0);

		PartyModal modal = new PartyModal(driver);

		soft.assertEquals(firstParty.get(partyName), modal.getNameInput().getText(), "Listed party name is correct");
		soft.assertEquals(firstParty.get(endpoint), modal.getEndpointInput().getText(), "Listed party endpoint is correct");

		List<String> toCompare = new ArrayList<>();
		for (HashMap<String, String> info : modal.getIdentifierTable().getAllRowInfo()) {
			soft.assertTrue(firstParty.get(partyID).contains(info.get("Party Id")), "id is listed" );
		}

		soft.assertAll();
	}

	@Test(description = "PMP-4", groups = {"multiTenancy", "singleTenancy"})
	public void deleteParty() throws Exception {

		rest.uploadPMode("pmodes/multipleParties.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);

		HashMap<String, String> firstParty = page.grid().getRowInfo(0);
		page.grid().selectRow(0);
		page.getDeleteButton().click();

		soft.assertTrue(page.getSaveButton().isEnabled(), "Save button is active");
		soft.assertTrue(page.getCancelButton().isEnabled(), "Cancel button is active");

		page.getCancelButton().click();
		soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "After cancel party is still present in grid");

		page.grid().selectRow(0);
		page.getDeleteButton().click();
		page.getSaveButton().click();

		soft.assertTrue(page.grid().scrollTo(partyName, firstParty.get(partyName))== -1, "After save party is NOT present in grid");


		soft.assertAll();
	}

	@Test(description = "PMP-5", groups = {"multiTenancy", "singleTenancy"})
	public void createParty() throws Exception {

		rest.uploadPMode("pmodes/multipleParties.xml", null);
		String newPatyName = Generator.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);

		soft.assertTrue(page.getNewButton().isEnabled(), "New button is enabled");
		page.getNewButton().click();

		PartyModal modal = new PartyModal(driver);
		modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");
		modal.clickOK();

		page.getSaveButton().click();

		soft.assertTrue(!page.getAlertArea().isError(), "page shows success message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().equalsIgnoreCase(DMessages.PMODE_PARTIES_UPDATE_SUCCESS), "page shows correct success message");


		soft.assertTrue(page.grid().scrollTo(partyName, newPatyName)>=0, "party is shown in grid");

		soft.assertAll();
	}

	@Test(description = "PMP-6", groups = {"multiTenancy", "singleTenancy"})
	public void editParty() throws Exception {

		rest.uploadPMode("pmodes/multipleParties.xml", null);
		String newPatyName = Generator.randomAlphaNumeric(5);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);

		page.grid().selectRow(0);

		page.getEditButton().click();

		PartyModal modal = new PartyModal(driver);

		modal.getNameInput().fill(newPatyName);
		modal.getEndpointInput().fill("http://" + newPatyName.toLowerCase()+".com");
		modal.clickOK();
		page.getSaveButton().click();

		soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");

		soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) >= 0, "New name is visible in grid");
		soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPatyName+".com") >= 0, "New endpoint is visible in grid");

		soft.assertAll();
	}

	@Test(description = "PMP-7", groups = {"multiTenancy", "singleTenancy"})
	public void editPartyAndCancel() throws Exception {

		rest.uploadPMode("pmodes/multipleParties.xml", null);
		String newPatyName = Generator.randomAlphaNumeric(5);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);

		page.grid().selectRow(0);

		page.getEditButton().click();

		PartyModal modal = new PartyModal(driver);

		modal.getNameInput().fill(newPatyName);
		modal.getEndpointInput().fill("http://" + newPatyName.toLowerCase()+".com");
		modal.clickOK();
		page.getCancelButton().click();

//		new Dialog(driver).confirm();

		soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) < 0, "New name is NOT visible in grid");
		soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPatyName+".com") < 0, "New endpoint is NOT visible in grid");

		soft.assertAll();
	}

	@Test(description = "PMP-8", groups = {"multiTenancy"})
	public void domainSegregation() throws Exception {

		String domainName = rest.getDomainNames().get(1);
		String domainCode = rest.getDomainCodeForName(domainName);

		rest.uploadPMode("pmodes/multipleParties.xml", null);
		rest.uploadPMode("pmodes/doNothingSelfSending.xml", domainCode);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.PMODE_PARTIES);

		PModePartiesPage page = new PModePartiesPage(driver);

		int noOfParties = page.grid().getRowsNo();

		page.getDomainSelector().selectOptionByText(domainName);

		int domainNoOfParties = page.grid().getRowsNo();
		soft.assertTrue(noOfParties != domainNoOfParties, "Number of parties doesn't coincide");


		soft.assertAll();
	}

}
