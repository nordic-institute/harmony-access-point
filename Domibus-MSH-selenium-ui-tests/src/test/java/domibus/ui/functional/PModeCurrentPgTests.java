package domibus.ui.functional;

import io.qameta.allure.*;
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
@Epic("Pmode Current")
@Feature("Functional")
public class PModeCurrentPgTests extends SeleniumTest {

	/*	PMC-1 - Login as super admin and open PMode - Current page	*/
	/*  PMC-1 - Login as super admin and open PMode - Current page  */
	@Description("PMC-1 - Login as super admin and open PMode - Current page")
	@Link(name = "EDELIVERY-5310", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5310")
	@AllureId("PMC-1")
	@Test(description = "PMC-1", groups = {"multiTenancy", "singleTenancy"})
	public void openPModeCurrentWindow() throws Exception {

		SoftAssert soft = new SoftAssert();

		PModeCurrentPage page = new PModeCurrentPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

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
	/*  PMC-2 - User chooses to upload new file  */
	@Description("PMC-2 - User chooses to upload new file")
	@Link(name = "EDELIVERY-5311", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5311")
	@AllureId("PMC-2")
	@Test(description = "PMC-2", groups = {"multiTenancy", "singleTenancy"})
	public void uploadPmode() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("pmodes/pmode-red.xml", null);
		int archivePgCount = rest.pmode().getPmodesList(null).length();

		PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
		pmcPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		Allure.step("Click on upload button");
		log.info("Click on upload button");
		pmcPage.getUploadBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);

		Allure.step("Upload pmode file");
		log.info("Upload pmode file");
		String path = DFileUtils.getAbsolutePath("src/main/resources/pmodes/Edelivery-blue.xml");
		String oldPmode = pmcPage.getTextArea().getText();

		String pmodeMessage = Gen.rndStr(50);
		modal.uploadPmodeFile(path, pmodeMessage);
		Allure.step("upload message is " + pmodeMessage);
		log.info("upload message is " + pmodeMessage);

		soft.assertTrue(pmcPage.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_SUCCESS));

		String newPmode = pmcPage.getTextArea().getText();

		Allure.step("checking number of pomodes in archive");
		log.info("checking number of pomodes in archive");
		int archivePgNewCount = rest.pmode().getPmodesList(null).length();
		soft.assertTrue(archivePgCount + 1 == archivePgNewCount, "Archive page has one new record present");

		Allure.step("comparing pmodes");
		log.info("comparing pmodes");
		soft.assertFalse(XMLUnit.compareXML(oldPmode, newPmode).identical(), "Both pmodes are not identical");

		soft.assertAll();

	}

	/*  This method will verify error message while uploading invalid pmode  or wrong  format file */
	/*  PMC-4 - User chooses to upload new INVALID file  */
	@Description("PMC-4 - User chooses to upload new INVALID file")
	@Link(name = "EDELIVERY-5313", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5313")
	@AllureId("PMC-4")
	@Test(description = "PMC-4", groups = {"multiTenancy", "singleTenancy"})
	public void uploadInvalidPmode() throws Exception {
		SoftAssert soft = new SoftAssert();


		Allure.step("Login into application");
		log.info("Login into application");
		Allure.step("Navigate to pmode current");
		log.info("Navigate to pmode current");
		PModeCurrentPage page = new PModeCurrentPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		String beforeUpdatePmode = page.getTextArea().getText();

		Allure.step("Click on upload button");
		log.info("Click on upload button");
		page.getUploadBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		Allure.step("Upload invalid xml file");
		log.info("Upload invalid xml file");
		String path = DFileUtils.getAbsolutePath("src/main/resources/pmodes/invalidPmode.xml");
		modal.uploadPmodeFile(path, "invalidPmodeUpload");

		Allure.step("Message shown " + page.getAlertArea().getAlertMessage());
		log.info("Message shown " + page.getAlertArea().getAlertMessage());
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("Error"), "Error message is shown");
		page.refreshPage();
		page.waitForPageTitle();
		page.getUploadBtn().click();

		Allure.step("Upload wrong file");
		log.info("Upload wrong file");
		String pathh = DFileUtils.getAbsolutePath("src/main/resources/myLocal.properties");
		modal.uploadPmodeFile(pathh, "invalidPmodeUpload");

		Allure.step("Message shown " + page.getAlertArea().getAlertMessage());
		log.info("Message shown " + page.getAlertArea().getAlertMessage());

		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_ERROR), "Error for wrong file format is shown");
		page.refreshPage();
		String afterUpdatePmode = page.getTextArea().getText();
		soft.assertTrue(beforeUpdatePmode.equals(afterUpdatePmode), "Both pmodes are equal");

		soft.assertAll();
	}

	/*PMC-5 - User edits PMode file using the text area available in the page to an invalid XML*/
	/*  PMC-5 - User edits PMode file using the text area available in the page to an invalid XML  */
	@Description("PMC-5 - User edits PMode file using the text area available in the page to an invalid XML")
	@Link(name = "EDELIVERY-5314", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5314")
	@AllureId("PMC-5")
	@Test(description = "PMC-5", groups = {"multiTenancy", "singleTenancy"})
	public void editPModeInvalidXML() throws Exception {

		String expectedErrorMess = "Failed to upload the PMode file due to: WstxUnexpectedCharException: Unexpected character";

		Allure.step("uploading pmode");
		log.info("uploading pmode");
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

		PModeCurrentPage page = new PModeCurrentPage(driver);
		Allure.step("getting listed pmode");
		log.info("getting listed pmode");
		String beforeEdit = page.getTextArea().getText();

		Allure.step("editing pmode");
		log.info("editing pmode");
		page.getTextArea().fill("THIS IS MY INVALID XML");
		Allure.step("saving pmode");
		log.info("saving pmode");
		page.getSaveBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		modal.getDescriptionTextArea().fill("This modification is invalid");
		modal.clickOK();

		Allure.step("checking error messages");
		log.info("checking error messages");
		soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(expectedErrorMess), "Page shows correct message");

		page.refreshPage();
		String afterEdit = page.getTextArea().getText();
		Allure.step("checking the listed pmode was not affected");
		log.info("checking the listed pmode was not affected");
		soft.assertEquals(beforeEdit, afterEdit, "Current PMode is not changed");

		soft.assertAll();
	}

	/*PMC-6 - User edits PMode file using the text area available in the page so that it is valid*/
	/*  PMC-6 - User edits PMode file using the text area available in the page so that it is valid  */
	@Description("PMC-6 - User edits PMode file using the text area available in the page so that it is valid")
	@Link(name = "EDELIVERY-5315", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5315")
	@AllureId("PMC-6")
	@Test(description = "PMC-6", groups = {"multiTenancy", "singleTenancy"})
	public void editPModeValidXML() throws Exception {

		Allure.step("uploading pmode");
		log.info("uploading pmode");
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

		Allure.step("getting current pmode");
		log.info("getting current pmode");
		PModeCurrentPage page = new PModeCurrentPage(driver);
		String beforeEdit = page.getTextArea().getText();
		String afterEdit = beforeEdit.replaceAll("\\t", " ").replaceAll("localhost", "mockhost");

//		afterEdit;
		Allure.step("editing and saving new pmode");
		log.info("editing and saving new pmode");
		page.getTextArea().fill(afterEdit);
		page.getSaveBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);
		modal.getDescriptionTextArea().fill("This modification is valid");
		modal.clickOK();

		Allure.step("checking success message");
		log.info("checking success message");
		soft.assertTrue(!page.getAlertArea().isError(), "Page shows success message");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_SUCCESS), "Page shows correct message");

		page.refreshPage();
		String afterRefresh = page.getTextArea().getText();
		Allure.step("checking the new edited pmode");
		log.info("checking the new edited pmode");
		soft.assertEquals(afterEdit, afterRefresh, "Current PMode is updated changed");

		soft.assertAll();
	}

	/*PMC-7 - Domain segregation*/
	/*  PMC-7 - Domain segregation  */
	@Description("PMC-7 - Domain segregation")
	@Link(name = "EDELIVERY-5504", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5504")
	@AllureId("PMC-7")
	@Test(description = "PMC-7", groups = {"multiTenancy"})
	public void domainSegregationPMode() throws Exception {

		String domainName = rest.getNonDefaultDomain();
		String domaincode = rest.getDomainCodeForName(domainName);

		Allure.step("uploading different pmodes on 2 different domains");
		log.info("uploading different pmodes on 2 different domains");

		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		rest.pmode().uploadPMode("pmodes/multipleParties.xml", domaincode);

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

		Allure.step("getting pmodes listed for each domain");
		log.info("getting pmodes listed for each domain");
		PModeCurrentPage page = new PModeCurrentPage(driver);

		String defaultPmode = page.getTextArea().getText();

		Allure.step("changing domain");
		log.info("changing domain");
		page.getDomainSelector().selectOptionByText(domainName);

		page.wait.forXMillis(1000);
		String d1Pmode = page.getTextArea().getText();

		Allure.step("comparing pmodes");
		log.info("comparing pmodes");
		soft.assertTrue(!XMLUnit.compareXML(defaultPmode, d1Pmode).identical(), "The 2 pmodes are not identical");

		soft.assertAll();
	}



}
