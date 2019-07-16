package domibus.ui;

import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.PModeCofirmationModal;
import pages.pmode.PModeCurrentPage;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class PModeCurrentPgTests extends BaseTest {

	@Test(description = "PMC-1", groups = {"multiTenancy", "singleTenancy"})
	public void openPModeCurrentWindow() throws Exception{

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_CURRENT);

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

	@Test(description = "PMC-2", groups = {"multiTenancy", "singleTenancy"})
	public void editPModeInvalidXML() throws Exception{

		String expectedErrorMess = "Failed to upload the PMode file due to: WstxUnexpectedCharException: Unexpected character";

		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_CURRENT);

		PModeCurrentPage page = new PModeCurrentPage(driver);

		String beforeEdit = page.getTextArea().getText();

		page.getTextArea().fill("THIS IS MY INVALID XML!!!!");
		page.getSaveBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		modal.getDescriptionTextArea().fill("This modification is invalid!!");
		modal.clickOK();

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(expectedErrorMess), "Page shows correct message");

		page.refreshPage();
		String afterEdit = page.getTextArea().getText();

		soft.assertEquals(beforeEdit, afterEdit, "Current PMode is not changed");

		soft.assertAll();
	}


	@Test(description = "PMC-3", groups = {"multiTenancy", "singleTenancy"})
	public void editPModeValidXML() throws Exception{

		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_CURRENT);

		PModeCurrentPage page = new PModeCurrentPage(driver);

		String beforeEdit = page.getTextArea().getText();

		String afterEdit = beforeEdit.replaceAll("\\t", " ").replaceAll("localhost", "mockhost");
//		afterEdit;

		page.getTextArea().fill(afterEdit);
		page.getSaveBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		modal.getDescriptionTextArea().fill("This modification is valid!!");
		modal.clickOK();

		soft.assertTrue(!page.getAlertArea().isError(), "Page shows success message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_SUCCESS), "Page shows correct message");

		page.refreshPage();
		String afterRefresh = page.getTextArea().getText();

		soft.assertEquals(afterEdit, afterRefresh, "Current PMode is updated changed");

		soft.assertAll();
	}

	@Test(description = "PMC-4", groups = {"multiTenancy"})
	public void domainSegregationPMode() throws Exception{

		String domainName = rest.getDomainNames().get(1);
		String domaincode = rest.getDomainCodeForName(domainName);

		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", domaincode);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_CURRENT);

		PModeCurrentPage page = new PModeCurrentPage(driver);

		String defaultPMode = page.getTextArea().getText();
		String alteredPMode = defaultPMode.replaceAll("\\t", " ").replaceAll("localhost", "alteredhost");

		page.getDomainSelector().selectOptionByText(domainName);

		page.getTextArea().fill(alteredPMode);
		page.getSaveBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		modal.getDescriptionTextArea().fill("Modification for domain " + domainName);
		modal.clickOK();

		soft.assertTrue(!page.getAlertArea().isError(), "Page shows success message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_SUCCESS), "Page shows correct message");

		page.refreshPage();
		String afterRefresh = page.getTextArea().getText();

		soft.assertEquals(alteredPMode, afterRefresh, "Current PMode is updated changed");

		page.getDomainSelector().selectOptionByText("Default");

		String defaultAfterRefresh = page.getTextArea().getText();
		soft.assertEquals(defaultPMode, defaultAfterRefresh, "On default domain the PMode is not changed");

		soft.assertAll();
	}


}
