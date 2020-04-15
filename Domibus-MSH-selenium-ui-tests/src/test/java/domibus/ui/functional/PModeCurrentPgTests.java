package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import pages.pmode.current.PModeArchivePage;
import pages.pmode.parties.PModePartiesPage;
import utils.BaseTest;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;
import utils.DFileUtils;

import java.io.File;


/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class PModeCurrentPgTests extends BaseTest {

    /*	PMC-1 - Login as super admin and open PMode - Current page	*/
    @Test(description = "PMC-1", groups = {"multiTenancy", "singleTenancy"})
    public void openPModeCurrentWindow() throws Exception {

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

        PModeCurrentPage page = new PModeCurrentPage(driver);

        soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is not enabled when first opening the page");
        soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is not enabled when first opening the page");
        soft.assertTrue(page.getUploadBtn().isEnabled(), "Upload button is enabled when first opening the page");

        if (rest.isPmodeUploaded(null)) {
            soft.assertTrue(page.getTextArea().isEnabled(), "If at least one PMode file was uploaded, text area is present and enabled when first opening the page");
            soft.assertTrue(page.getDownloadBtn().isEnabled(), "Download button button is enabled when first opening the page");
        } else {
            soft.assertTrue(!page.getTextArea().isPresent(), "If no PMode was uploaded the text area is not present");
            soft.assertTrue(!page.getDownloadBtn().isEnabled(), "Download button button is NOT enabled if no file uploaded ever");
        }

        soft.assertAll();
    }


    /*PMC-5 - User edits PMode file using the text area available in the page to an invalid XML*/
    @Test(description = "PMC-5", groups = {"multiTenancy", "singleTenancy"})
    public void editPModeInvalidXML() throws Exception {

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
    public void editPModeValidXML() throws Exception {

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
    public void domainSegregationPMode() throws Exception {

        String domainName = rest.getDomainNames().get(1);
        String domaincode = rest.getDomainCodeForName(domainName);

        log.info("uploading different pmodes on 2 different domains");

        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        rest.uploadPMode("pmodes/multipleParties.xml", domaincode);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

        log.info("gettting pmodes listed for each domain");
        PModeCurrentPage page = new PModeCurrentPage(driver);

        String defaultPmode = page.getTextArea().getText();

        log.info("changing domain");
        page.getDomainSelector().selectOptionByText(domainName);
        String d1Pmode = page.getTextArea().getText();

        log.info("comparing pmodes");
        soft.assertTrue(!XMLUnit.compareXML(defaultPmode, d1Pmode).identical(), "The 2 pmodes are not identical");

        soft.assertAll();
    }
    /*  This method will upload pmode on pmode current page */

    @Test(description = "PMC-2", groups = {"multiTenancy", "singleTenancy"})
    public void uploadPmode() throws Exception {

        SoftAssert soft = new SoftAssert();
        log.info("Login into application");
        login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);
        log.info("Navigate to Pmode-Archive page");
        page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        PModeArchivePage paPage = new PModeArchivePage(driver);
        int totalCountBefore = paPage.grid().getPagination().getTotalItems();
        log.info("Total number of records  before upload : " + totalCountBefore);
        log.info("Navigate to Pmode current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        PModeCurrentPage pmodePage = new PModeCurrentPage(driver);

        log.info("Click on update button");
        pmodePage.getUploadBtn().click();

        PModeCofirmationModal pPage = new PModeCofirmationModal(driver);
        String path = DFileUtils.getAbsolutePath("pmodes/Edelivery-blue.xml");
        log.info("Upload valid Pmode");
        pPage.uploadPmodeFile(path,soft,"freshUpload");
        soft.assertTrue(pmodePage.getAlertArea().getAlertMessage().contains("successfully"), "Success message is shown");


        log.info("navigate to pmode archive page");
        page.getSidebar().goToPage(PAGES.PMODE_ARCHIVE);
        paPage.grid().waitForRowsToLoad();

        int totalCountAfter = paPage.grid().getPagination().getTotalItems();
        log.info("Total number of records after upload " + totalCountAfter);

        log.info("Compare before and after count on Archive page");
        soft.assertTrue(totalCountBefore < totalCountAfter, "After count is greater than before");
        log.info("Verify Description for first row");
        soft.assertTrue(paPage.grid().getRowInfo(0).containsValue("[CURRENT]: freshUpload"));
        soft.assertAll();
    }

    /*  This method will verify error message while uploading invalid pmode  or wrong  format file */
    @Test(description = "PMC-4", groups = {"multiTenancy", "singleTenancy"})
    public void uploadInvalidPmode() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application");
        login(data.getAdminUser());
        DomibusPage page = new DomibusPage(driver);

        log.info("Navigate to pmode current");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        PModeCurrentPage pmodePage = new PModeCurrentPage(driver);

        log.info("Click on upload button");
        pmodePage.getUploadBtn().click();

        PModeCofirmationModal pPage = new PModeCofirmationModal(driver);

        log.info("Upload invalid xml file");
        String path = DFileUtils.getAbsolutePath("pmodes/invalidPmode.xml");
        pPage.uploadPmodeFile(path, soft, "invalidPmodeUpload");

        log.info("Message shown " + pmodePage.getAlertArea().getAlertMessage());

        log.info("Validate presence of Error in alert message");
        soft.assertTrue(pmodePage.getAlertArea().getAlertMessage().contains("Error"), "Error message is shown");
        log.info("Refresh page ");
        pmodePage.refreshPage();
        log.info("Wait for page title");
        pmodePage.waitForTitle();

        soft.assertTrue(pmodePage.getUploadBtn().isEnabled(), "Upload button is enabled");
        log.info("Click on upload button");
        pmodePage.getUploadBtn().click();

        log.info("Try to upload invalid file with format other than xml");
        String randomFilePath = DFileUtils.getAbsolutePath("truststore/expired.jks");

        pPage.uploadPmodeFile(randomFilePath, soft, "invalidFileUpload");
        log.info("Message shown " + pmodePage.getAlertArea().getAlertMessage());
        soft.assertTrue(pmodePage.getAlertArea().getAlertMessage().contains("Error"), "Error message is shown");
        pmodePage.refreshPage();
        soft.assertAll();
    }

    /* This method will download current pmode to customized location and compare the downloaded pmode to current one  */
    @Test(description = "PMC-3", groups = {"multiTenancy", "singleTenancy"})
    public void downloadPmode() throws Exception {
        SoftAssert soft = new SoftAssert();
        log.info("Login into application");

        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

        log.info("Customized path location for download files");
        String filePath = System.getProperty("user.dir")+File.separator+"downloadFiles";

        log.info("Navigate to pmode current");
        PModeCurrentPage pmodePage = new PModeCurrentPage(driver);

        log.info("Delete existing files from given folder location");
        FileUtils.cleanDirectory(new File(filePath));

        log.info("Click on download button");
        pmodePage.getDownloadBtn().click();

        log.info("Wait to complete file download");
//        DWait wait = new DWait(driver);
//        wait.forXMillis(500);

        pmodePage.wait.forXMillis(500);

        log.info("Check presence of file in given folder location");
        soft.assertTrue(DFileUtils.isFileDownloaded(filePath));

        log.info("Extract doc from downloaded xml");
        Document doc = DFileUtils.getDocFromXML(filePath);

        PModePartiesPage ppPage = new PModePartiesPage(driver);
        log.info("Save xml file data into string variable");
        String downloadedPmode = ppPage.printPmode(doc);

        log.info("Extract xml data from pmode current page");
        String currentPmode = pmodePage.getTextArea().getText();

        log.info("Compare xml data from downloaded file and current pmode avaible in application");
        soft.assertTrue(XMLUnit.compareXML(downloadedPmode, currentPmode).identical(), "Both pmode are same");

        ppPage.refreshPage();
        soft.assertAll();

    }

}