package domibus.ui.functional;

import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import utils.BaseTest;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;

/**
 * @author Catalin Comanici

 * @since 4.1
 */
public class PModeCurrentPgTests extends BaseTest {

	/*	PMC-1 - Login as super admin and open PMode - Current page	*/
	@Test(description = "PMC-1", groups = {"multiTenancy", "singleTenancy"})
	public void openPModeCurrentWindow() throws Exception{

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

		PModeCurrentPage page = new PModeCurrentPage(driver);

		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is not enabled when first opening the page");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is not enabled when first opening the page");
		soft.assertTrue(page.getUploadBtn().isEnabled(), "Upload button is enabled when first opening the page");

		if(rest.isPmodeUploaded(null)){
			soft.assertTrue(page.getTextArea().isEnabled(), "If at least one PMode file was uploaded, text area is present and enabled when first opening the page");
			soft.assertTrue(page.getDownloadBtn().isEnabled(), "Download button button is enabled when first opening the page");
		}else {
			soft.assertTrue(!page.getTextArea().isPresent(), "If no PMode was uploaded the text area is not present");
			soft.assertTrue(!page.getDownloadBtn().isEnabled(), "Download button button is NOT enabled if no file uploaded ever");
		}

		soft.assertAll();
	}


	/*PMC-5 - User edits PMode file using the text area available in the page to an invalid XML*/
	@Test(description = "PMC-5", groups = {"multiTenancy", "singleTenancy"})
	public void editPModeInvalidXML() throws Exception{

		String expectedErrorMess = "Failed to upload the PMode file due to: WstxUnexpectedCharException: Unexpected character";

		log.info("uploading pmode");
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

		PModeCurrentPage page = new PModeCurrentPage(driver);
		log.info("getting listed pmode");
		String beforeEdit = page.getTextArea().getText();

		log.info("editing pmode");
		page.getTextArea().fill("THIS IS MY INVALID XML");
		log.info("saving pmode");
		page.getSaveBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		modal.getDescriptionTextArea().fill("This modification is invalid");
		modal.clickOK();

		log.info("checking error messages");
		soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(expectedErrorMess), "Page shows correct message");

		page.refreshPage();
		String afterEdit = page.getTextArea().getText();
		log.info("checking the listed pmode was not affected");
		soft.assertEquals(beforeEdit, afterEdit, "Current PMode is not changed");

		soft.assertAll();
	}

	/*PMC-6 - User edits PMode file using the text area available in the page so that it is valid*/
	@Test(description = "PMC-6", groups = {"multiTenancy", "singleTenancy"})
	public void editPModeValidXML() throws Exception{

		log.info("uploading pmode");
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

		log.info("getting current pmode");
		PModeCurrentPage page = new PModeCurrentPage(driver);
		String beforeEdit = page.getTextArea().getText();
		String afterEdit = beforeEdit.replaceAll("\\t", " ").replaceAll("localhost", "mockhost");

//		afterEdit;
		log.info("editing and saving new pmode");
		page.getTextArea().fill(afterEdit);
		page.getSaveBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		modal.getDescriptionTextArea().fill("This modification is valid");
		modal.clickOK();

		log.info("checking success message");
		soft.assertTrue(!page.getAlertArea().isError(), "Page shows success message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_SUCCESS), "Page shows correct message");

		page.refreshPage();
		String afterRefresh = page.getTextArea().getText();
		log.info("checking the new edited pmode");
		soft.assertEquals(afterEdit, afterRefresh, "Current PMode is updated changed");

		soft.assertAll();
	}

	/*PMC-7 - Domain segregation*/
	@Test(description = "PMC-7", groups = {"multiTenancy"})
	public void domainSegregationPMode() throws Exception{

		String domainName = getNonDefaultDomain();
		String domainCode = rest.getDomainCodeForName(domainName);

		log.info("uploading different pmodes on 2 different domains");

		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		rest.uploadPMode("pmodes/multipleParties.xml", domainCode);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

		log.info("getting pmodes listed for each domain");
		PModeCurrentPage page = new PModeCurrentPage(driver);

		String defaultPmode = page.getTextArea().getText();

		log.info("changing domain");
		page.getDomainSelector().selectOptionByText(domainName);

		page.wait.forXMillis(1000);
		String d1Pmode = page.getTextArea().getText();

		log.info("comparing pmodes");
		soft.assertTrue(!XMLUnit.compareXML(defaultPmode, d1Pmode).identical(), "The 2 pmodes are not identical");

		soft.assertAll();
	}


}
