package domibus.ui.functional;

import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.archive.PModeArchivePage;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;
import utils.DFileUtils;
import utils.Gen;
import utils.PModeXMLUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class PModeCurrentPgTests extends SeleniumTest {

	/*	PMC-1 - Login as super admin and open PMode - Current page	*/
	@Test(description = "PMC-1", groups = {"multiTenancy", "singleTenancy"})
	public void openPModeCurrentWindow() throws Exception {

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

		PModeCurrentPage page = new PModeCurrentPage(driver);

		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is not enabled when first opening the page");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is not enabled when first opening the page");
		soft.assertTrue(page.getUploadBtn().isEnabled(), "Upload button is enabled when first opening the page");

		if (rest.pmode().isPmodeUploaded(null)) {
			soft.assertTrue(page.getTextArea().isEnabled(), "If at least one PMode file was uploaded, text area is present and enabled when first opening the page");
			soft.assertTrue(page.getDownloadBtn().isEnabled(), "Download button button is enabled when first opening the page");
		} else {
			soft.assertTrue(!page.getTextArea().isPresent(), "If no PMode was uploaded the text area is not present");
			soft.assertTrue(!page.getDownloadBtn().isEnabled(), "Download button button is NOT enabled if no file uploaded ever");
		}

		soft.assertAll();
	}

	/*  This method will verify message while uploading Pmode and Pmode archive record */
	@Test(description = "PMC-2", groups = {"multiTenancy", "singleTenancy"})
	public void uploadPmode() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("pmodes/pmode-red.xml", null);
		int archivePgCount = rest.pmode().getPmodesList(null).length();

		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
		pmcPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		log.info("Click on upload button");
		pmcPage.getUploadBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);

		log.info("Upload pmode file");
		String path = DFileUtils.getAbsolutePath("src/main/resources/pmodes/Edelivery-blue.xml");
		String oldPmode = pmcPage.getTextArea().getText();

		String pmodeMessage = Gen.rndStr(50);
		modal.uploadPmodeFile(path, pmodeMessage);
		log.info("upload message is " + pmodeMessage);

		soft.assertTrue(pmcPage.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_SUCCESS));

		String newPmode = pmcPage.getTextArea().getText();

		log.info("checking number of pomodes in archive");
		int archivePgNewCount = rest.pmode().getPmodesList(null).length();
		soft.assertTrue(archivePgCount + 1 == archivePgNewCount, "Archive page has one new record present");

		log.info("comparing pmodes");
		soft.assertFalse(XMLUnit.compareXML(oldPmode, newPmode).identical(), "Both pmodes are not identical");

		soft.assertAll();

	}

	/*  This method will verify error message while uploading invalid pmode  or wrong  format file */
	@Test(description = "PMC-4", groups = {"multiTenancy", "singleTenancy"})
	public void uploadInvalidPmode() throws Exception {
		SoftAssert soft = new SoftAssert();


		log.info("Login into application");
		log.info("Navigate to pmode current");
		PModeCurrentPage page = new PModeCurrentPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		String beforeUpdatePmode = page.getTextArea().getText();

		log.info("Click on upload button");
		page.getUploadBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		log.info("Upload invalid xml file");
		String path = DFileUtils.getAbsolutePath("src/main/resources/pmodes/invalidPmode.xml");
		modal.uploadPmodeFile(path, "invalidPmodeUpload");

		log.info("Message shown " + page.getAlertArea().getAlertMessage());
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("Error"), "Error message is shown");
		page.refreshPage();
		page.waitForPageTitle();
		page.getUploadBtn().click();

		log.info("Upload wrong file");
		String pathh = DFileUtils.getAbsolutePath("src/main/resources/myLocal.properties");
		modal.uploadPmodeFile(pathh, "invalidPmodeUpload");

		log.info("Message shown " + page.getAlertArea().getAlertMessage());

		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_ERROR), "Error for wrong file format is shown");
		page.refreshPage();
		String afterUpdatePmode = page.getTextArea().getText();
		soft.assertTrue(beforeUpdatePmode.equals(afterUpdatePmode), "Both pmodes are equal");

		soft.assertAll();
	}

	/*PMC-5 - User edits PMode file using the text area available in the page to an invalid XML*/
	@Test(description = "PMC-5", groups = {"multiTenancy", "singleTenancy"})
	public void editPModeInvalidXML() throws Exception {

		String expectedErrorMess = "Failed to upload the PMode file due to: WstxUnexpectedCharException: Unexpected character";

		log.info("uploading pmode");
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

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
	public void editPModeValidXML() throws Exception {

		log.info("uploading pmode");
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

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
	public void domainSegregationPMode() throws Exception {

		String domainName = rest.getNonDefaultDomain();
		String domaincode = rest.getDomainCodeForName(domainName);

		log.info("uploading different pmodes on 2 different domains");

		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", domaincode);

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

	/*  EDELIVERY-7288 - PMC-12 - PMode validations - boolean attributes don't accept other values  */
	@Test(description = "PMC-12", groups = {"multiTenancy", "singleTenancy"})
	public void validationsBooleans() throws Exception {
		SoftAssert soft = new SoftAssert();

		String currentPmode = rest.pmode().getCurrentPmode(null);
		String newPmode = currentPmode.replaceAll("true", Gen.rndStr(5)).replaceAll("false", Gen.rndStr(5));

		PModeCurrentPage page = modifyListedPmode(newPmode);
		soft.assertTrue(page.getAlertArea().isError(), "Error message shown");
		soft.assertAll();
	}

	/*    EDELIVERY-7291 - PMC-13 - PMode validations - party describing current system must be present */
	@Test(description = "PMC-13", groups = {"multiTenancy", "singleTenancy"})
	public void currentPmodeNoCurrentParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		if (!rest.pmode().isPmodeUploaded(null)) {
			log.info("uploading pmode to modify");
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		String currentPmode = rest.pmode().getCurrentPmode(null);

		PModeXMLUtils xmlutils = new PModeXMLUtils(currentPmode);
		String currentParty = xmlutils.getCurrentPartyName();
		xmlutils.removeParty(currentParty);

		String newPmode = xmlutils.printDoc();

		PModeCurrentPage page = modifyListedPmode(newPmode);
		soft.assertTrue(page.getAlertArea().isError(), "Error message shown");
		soft.assertAll();
	}


	/*     EDELIVERY-7292 - PMC-14 - PMode validations - <> are considered invalid in any value */
	@Test(description = "PMC-14", groups = {"multiTenancy", "singleTenancy"})
	public void invalidCharacters() throws Exception {
		SoftAssert soft = new SoftAssert();

		if (!rest.pmode().isPmodeUploaded(null)) {
			log.info("uploading pmode to modify");
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		String currentPmode = rest.pmode().getCurrentPmode(null);

		PModeXMLUtils xmlutils = new PModeXMLUtils(currentPmode);
		String currentParty = xmlutils.getCurrentPartyName();

		String newPmode = currentPmode.replaceAll(currentParty, currentParty + "<>");
		PModeCurrentPage page = modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");

		newPmode = currentPmode.replaceAll("name=\"", "name=\"<>");
		page = modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");

		soft.assertAll();
	}


	/*     EDELIVERY-7293 - PMC-15 - PMode validations - all listed URLs are valid */
	@Test(description = "PMC-15", groups = {"multiTenancy", "singleTenancy"})
	public void invalidURLs() throws Exception {
		SoftAssert soft = new SoftAssert();

		if (!rest.pmode().isPmodeUploaded(null)) {
			log.info("uploading pmode to modify");
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		String currentPmode = rest.pmode().getCurrentPmode(null);

		String newPmode = currentPmode.replaceAll("http://", Gen.rndStr(5));
		PModeCurrentPage page = modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");

		soft.assertAll();
	}

/*        EDELIVERY-7294 - PMC-16 - PMode validations - attributes with integer values are validated as integers */
	@Test(description = "PMC-16", groups = {"multiTenancy", "singleTenancy"})
	public void integerAttributesValidations() throws Exception {
		SoftAssert soft = new SoftAssert();

		if (!rest.pmode().isPmodeUploaded(null)) {
			log.info("uploading pmode to modify");
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		String currentPmode = rest.pmode().getCurrentPmode(null);

		String newPmode = currentPmode.replaceAll("\\d+", Gen.rndStr(5));
		PModeCurrentPage page = modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");

		soft.assertAll();
	}


	private PModeCurrentPage modifyListedPmode(String newPmode) throws Exception {
		if (!rest.pmode().isPmodeUploaded(null)) {
			log.info("uploading pmode to modify");
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

		log.info("getting current pmode");
		PModeCurrentPage page = new PModeCurrentPage(driver);

		page.getTextArea().fill(newPmode);

		log.info("saving");
		page.saveAndConfirm("");


		return page;
	}

}
