package domibus.ui.functional;

import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.custommonkey.xmlunit.XMLUnit;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;
import utils.DFileUtils;
import utils.Gen;
import utils.PModeXMLUtils;

public class PModeCurrentPgTests extends SeleniumTest {

    /* EDELIVERY-5310 - PMC-1 - Login as super admin and open PMode - Current page */
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

    /* EDELIVERY-5311 - PMC-2 - User chooses to upload new file */
    @Test(description = "PMC-2", groups = {"multiTenancy", "singleTenancy"})
    public void uploadPmode() throws Exception {
        SoftAssert soft = new SoftAssert();

        rest.pmode().uploadPMode("pmodes/pmode-red.xml", null);
        int archivePgCount = rest.pmode().getPmodesList(null).length();

        PModeCurrentPage pmcPage = new PModeCurrentPage(driver);
        pmcPage.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        Reporter.log("Click on upload button");
        log.info("Click on upload button");
        pmcPage.getUploadBtn().click();

        PModeCofirmationModal modal = new PModeCofirmationModal(driver);

        Reporter.log("Upload pmode file");
        log.info("Upload pmode file");
        String path = DFileUtils.getAbsolutePath("src/main/resources/pmodes/Edelivery-blue.xml");
        String oldPmode = pmcPage.getTextArea().getText();

        String pmodeMessage = Gen.rndStr(50);
        modal.uploadPmodeFile(path, pmodeMessage);
        Reporter.log("upload message is " + pmodeMessage);
        log.info("upload message is " + pmodeMessage);

        soft.assertTrue(pmcPage.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_SUCCESS));

        String newPmode = pmcPage.getTextArea().getText();

        pmcPage.wait.forXMillis(1000);

        Reporter.log("checking number of pomodes in archive");
        log.info("checking number of pomodes in archive");
        int archivePgNewCount = rest.pmode().getPmodesList(null).length();
        soft.assertTrue(archivePgCount + 1 == archivePgNewCount, "Archive page has one new record present");

        Reporter.log("comparing pmodes");
        log.info("comparing pmodes");
        soft.assertFalse(XMLUnit.compareXML(oldPmode, newPmode).identical(), "Both pmodes are not identical");

        soft.assertAll();

    }

    /* EDELIVERY-5313 - PMC-4 - User chooses to upload new INVALID file */
    @Test(description = "PMC-4", groups = {"multiTenancy", "singleTenancy"})
    public void uploadInvalidPmode() throws Exception {
        SoftAssert soft = new SoftAssert();


        Reporter.log("Login into application");
        log.info("Login into application");
        Reporter.log("Navigate to pmode current");
        log.info("Navigate to pmode current");
        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        String beforeUpdatePmode = page.getTextArea().getText();

        Reporter.log("Click on upload button");
        log.info("Click on upload button");
        page.getUploadBtn().click();

        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        Reporter.log("Upload invalid xml file");
        log.info("Upload invalid xml file");
        String path = DFileUtils.getAbsolutePath("src/main/resources/pmodes/invalidPmode.xml");
        modal.uploadPmodeFile(path, "invalidPmodeUpload");

        Reporter.log("Message shown " + page.getAlertArea().getAlertMessage());
        log.info("Message shown " + page.getAlertArea().getAlertMessage());
        soft.assertTrue(page.getAlertArea().getAlertMessage().contains("Error"), "Error message is shown");
        page.refreshPage();
        page.waitForPageTitle();
        page.getUploadBtn().click();

        Reporter.log("Upload wrong file");
        log.info("Upload wrong file");
        String pathh = DFileUtils.getAbsolutePath("src/main/resources/myLocal.properties");
        modal.uploadPmodeFile(pathh, "invalidPmodeUpload");

        Reporter.log("Message shown " + page.getAlertArea().getAlertMessage());
        log.info("Message shown " + page.getAlertArea().getAlertMessage());

        soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_ERROR), "Error for wrong file format is shown");
        page.refreshPage();
        String afterUpdatePmode = page.getTextArea().getText();
        soft.assertTrue(beforeUpdatePmode.equals(afterUpdatePmode), "Both pmodes are equal");

        soft.assertAll();
    }

    /* EDELIVERY-5314 - PMC-5 - User edits PMode file using the text area available in the page to an invalid XML */
    @Test(description = "PMC-5", groups = {"multiTenancy", "singleTenancy"})
    public void editPModeInvalidXML() throws Exception {

        String expectedErrorMess = "Failed to upload the PMode file due to: WstxUnexpectedCharException: Unexpected character";

        Reporter.log("uploading pmode");
        log.info("uploading pmode");
        rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

        PModeCurrentPage page = new PModeCurrentPage(driver);
        Reporter.log("getting listed pmode");
        log.info("getting listed pmode");
        String beforeEdit = page.getTextArea().getText();

        Reporter.log("editing pmode");
        log.info("editing pmode");
        page.getTextArea().fill("THIS IS MY INVALID XML");
        Reporter.log("saving pmode");
        log.info("saving pmode");
        page.getSaveBtn().click();

        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        modal.getDescriptionTextArea().fill("This modification is invalid");
        modal.clickOK();

        Reporter.log("checking error messages");
        log.info("checking error messages");
        soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
        soft.assertTrue(page.getAlertArea().getAlertMessage().contains(expectedErrorMess), "Page shows correct message");

        page.refreshPage();
        String afterEdit = page.getTextArea().getText();
        Reporter.log("checking the listed pmode was not affected");
        log.info("checking the listed pmode was not affected");
        soft.assertEquals(beforeEdit, afterEdit, "Current PMode is not changed");

        soft.assertAll();
    }

    /* EDELIVERY-5315 - PMC-6 - User edits PMode file using the text area available in the page so that it is valid */
    @Test(description = "PMC-6", groups = {"multiTenancy", "singleTenancy"})
    public void editPModeValidXML() throws Exception {

        Reporter.log("uploading pmode");
        log.info("uploading pmode");
        rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

        Reporter.log("getting current pmode");
        log.info("getting current pmode");
        PModeCurrentPage page = new PModeCurrentPage(driver);
        String beforeEdit = page.getTextArea().getText();
        String afterEdit = beforeEdit.replaceAll("\\t", " ").replaceAll("localhost", "mockhost");

//		afterEdit;
        Reporter.log("editing and saving new pmode");
        log.info("editing and saving new pmode");
        page.getTextArea().fill(afterEdit);
        page.getSaveBtn().click();

        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        modal.getDescriptionTextArea().fill("This modification is valid");
        modal.clickOK();

        Reporter.log("checking success message");
        log.info("checking success message");
        soft.assertTrue(!page.getAlertArea().isError(), "Page shows success message");
        soft.assertTrue(page.getAlertArea().getAlertMessage().contains(DMessages.PMODE_UPDATE_SUCCESS), "Page shows correct message");

        page.refreshPage();
        String afterRefresh = page.getTextArea().getText();
        Reporter.log("checking the new edited pmode");
        log.info("checking the new edited pmode");
        soft.assertEquals(afterEdit, afterRefresh, "Current PMode is updated changed");

        soft.assertAll();
    }

    /* EDELIVERY-5504 - PMC-7 - Domain segregation */
    @Test(description = "PMC-7", groups = {"multiTenancy"})
    public void domainSegregationPMode() throws Exception {

        String domainName = rest.getNonDefaultDomain();
        String domaincode = rest.getDomainCodeForName(domainName);

        Reporter.log("uploading different pmodes on 2 different domains");
        log.info("uploading different pmodes on 2 different domains");

        rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        rest.pmode().uploadPMode("pmodes/multipleParties.xml", domaincode);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.PMODE_CURRENT);

        Reporter.log("getting pmodes listed for each domain");
        log.info("getting pmodes listed for each domain");
        PModeCurrentPage page = new PModeCurrentPage(driver);

        String defaultPmode = page.getTextArea().getText();

        Reporter.log("changing domain");
        log.info("changing domain");
        page.getDomainSelector().selectOptionByText(domainName);

        String d1Pmode = page.getTextArea().getText();

        Reporter.log("comparing pmodes");
        log.info("comparing pmodes");
        soft.assertTrue(!XMLUnit.compareXML(defaultPmode, d1Pmode).identical(), "The 2 pmodes are not identical");

        soft.assertAll();
    }


    /*     EDELIVERY-9669 - PMC-24 - Downloaded PMode should contain domain name in filename */
    @Test(description = "PMC-24", groups = {"multiTenancy", "singleTenancy"})
    public void domainNameInDownloadedFileName() throws Exception {

        SoftAssert soft = new SoftAssert();

        log.info("getting current pmode");
        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        DFileUtils.cleanDownloadFolder(data.downloadFolderPath());

        page.getDownloadBtn().click();

        String downloadedFileName = DFileUtils.getCompleteFileName(data.downloadFolderPath());
        String domainName = page.getDomainFromTitle();
        soft.assertTrue(downloadedFileName.contains(domainName), "Downloaded file name contains domain name");

        soft.assertAll();
    }

    /*         EDELIVERY-6368 - PMC-8 - Verify save button is disabled when user deleted all text in pmode text area */
    @Test(description = "PMC-8", groups = {"multiTenancy", "singleTenancy"})
    public void saveBtnDisabledOnEmptyPMode() throws Exception {
        SoftAssert soft = new SoftAssert();

        log.info("getting current pmode");
        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        page.getTextArea().clear();
        ((JavascriptExecutor) driver).executeScript("document.querySelector('#pmodetextarea_id').dispatchEvent(new Event('input'))");

        page.wait.forXMillis(500);

        soft.assertTrue(page.getSaveBtn().isDisabled(), "Save button is disabled when pmode is empty");


        soft.assertAll();
    }


    /*  PMC-19 - PMode duplicate detection enabled */
    @Test(description = "PMC-19", groups = {"multiTenancy", "singleTenancy"})
    public void duplicateDetectionIsOn() throws Exception {
        SoftAssert soft = new SoftAssert();

        String[] nodeToCheck = {"mpc", "role", "partyIdType", "party", "mep", "binding", "property", "propertySet", "payload", "payloadProfile", "security", "errorHandling", "agreement", "service", "action", "receptionAwareness", "reliability", "legConfiguration", "process", "initiatorParty", "responderParty", "leg"};

        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        for (String nodeName : nodeToCheck) {

            page.refreshPage();

            PModeXMLUtils xmlUtils = new PModeXMLUtils(page.getTextArea().getText());

            soft.assertTrue(xmlUtils.isDuplicateDetectionEnabled(), "Duplicate detection is on");
            xmlUtils.duplicateNode(nodeName, false);

            page.getTextArea().fill(xmlUtils.printDoc());

            page.saveAndConfirm("This modification is invalid because of duplicate detection for node: " + nodeName);

            soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
        }

        soft.assertAll();
    }

    /*  PMC-20 - PMode duplicate detection CANNOT be disabled */
    @Test(description = "PMC-20", groups = {"multiTenancy", "singleTenancy"})
    public void duplicateDetectionCannotBeDisabled() throws Exception {
        SoftAssert soft = new SoftAssert();

        String[] nodeToCheck = {"mpc", "role"};

        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        for (String nodeName : nodeToCheck) {

            page.refreshPage();

            PModeXMLUtils xmlUtils = new PModeXMLUtils(page.getTextArea().getText());

            soft.assertTrue(xmlUtils.isDuplicateDetectionEnabled(), "Duplicate detection is on");
            xmlUtils.duplicateNode(nodeName, false);
            xmlUtils.setDuplicateDetection(false);

            page.getTextArea().fill(xmlUtils.printDoc());

            page.saveAndConfirm("This modification is invalid because of duplicate detection for node: " + nodeName);

            soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
        }

        soft.assertAll();
    }

    /*     EDELIVERY-9666 - PMC-21 - PMode duplicate detection is not case sensitive */
    @Test(description = "PMC-20", groups = {"multiTenancy", "singleTenancy"})
    public void duplicateDetectionNotCaseSensitive() throws Exception {
        SoftAssert soft = new SoftAssert();

        String[] nodeToCheck = {"mpc", "role"};

        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        for (String nodeName : nodeToCheck) {

            page.refreshPage();

            PModeXMLUtils xmlUtils = new PModeXMLUtils(page.getTextArea().getText());

            soft.assertTrue(xmlUtils.isDuplicateDetectionEnabled(), "Duplicate detection is on");
            xmlUtils.duplicateNode(nodeName, true);
            xmlUtils.setDuplicateDetection(false);

            page.getTextArea().fill(xmlUtils.printDoc());

            page.saveAndConfirm("This modification is invalid because of duplicate detection for node: " + nodeName);

            soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
        }

        soft.assertAll();
    }


    /*    EDELIVERY-9670 - PMC-25 - PMode should not pass validation with 2 parties with the same partId */
    @Test(description = "PMC-25", groups = {"multiTenancy", "singleTenancy"})
    public void duplicatePartyId() throws Exception {
        SoftAssert soft = new SoftAssert();

        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        PModeXMLUtils xmlUtils = new PModeXMLUtils(page.getTextArea().getText());
        xmlUtils.duplicatePartyIdNewName();

        page.getTextArea().fill(xmlUtils.printDoc());
        page.saveAndConfirm("This modification is invalid because of duplicate party id");

        soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");

        soft.assertAll();
    }

    /*   EDELIVERY-9668 - PMC-23 - PMode should pass validation even if it contains parties without partyIdType */
    @Test(description = "PMC-23", groups = {"multiTenancy", "singleTenancy"})
    public void noPartyIdType() throws Exception {
        SoftAssert soft = new SoftAssert();

        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        PModeXMLUtils xmlUtils = new PModeXMLUtils(page.getTextArea().getText());
        xmlUtils.removePartyIdType();

        page.getTextArea().fill(xmlUtils.printDoc());
        page.saveAndConfirm("This modification is valid");

        soft.assertFalse(page.getAlertArea().isError(), "Page shows error message");

        soft.assertAll();
    }

    /*  EDELIVERY-9667 - PMC-22 - PMode signature method should only contain valid values */
    @Test(description = "PMC-22", groups = {"multiTenancy", "singleTenancy"})
    public void validSignatureMethod() throws Exception {
        SoftAssert soft = new SoftAssert();

        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        PModeXMLUtils xmlUtils = new PModeXMLUtils(page.getTextArea().getText());
        xmlUtils.setAttributeValueForNode("security", "signatureMethod", "invalidValue");

        page.getTextArea().fill(xmlUtils.printDoc());
        page.saveAndConfirm("This modification is invalid due to erroneous signature method");

        soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");

        soft.assertAll();
    }

    /* EDELIVERY-7296 - PMC-18 - PMode validations - empty entities don't pass validations */
    @Test(description = "PMC-18", groups = {"multiTenancy", "singleTenancy"})
    public void emptyEntities() throws Exception {
        SoftAssert soft = new SoftAssert();

        String[] nodeNames = {"legConfiguration"
                , "service"
                , "property"
                , "party"
                , "role"};

        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        for (String nodeName : nodeNames) {

            PModeXMLUtils xmlUtils = new PModeXMLUtils(page.getTextArea().getText());

            xmlUtils.makeEmptyNode(nodeName);

            page.getTextArea().fill(xmlUtils.printDoc());
            page.saveAndConfirm("This modification is invalid due to empy entity " + nodeName);

            soft.assertTrue(page.getAlertArea().isError(), "Page shows error message for empty entity " + nodeName);

            page.refreshPage();
        }

        soft.assertAll();
    }


    /* EDELIVERY-7192 - PMC-11 - PMode validations - referenced entities are defined */
    @Test(description = "PMC-11", groups = {"multiTenancy", "singleTenancy"})
    public void referencedEntitiesExist() throws Exception {
        SoftAssert soft = new SoftAssert();

        String[] nodeNames = {"party"
                , "role"
                , "mep"
                , "binding"
                , "service"
                , "action"
                , "mpc"
                , "property"
                , "payloadProfile"
                , "security"
                , "errorHandling"
                , "legConfiguration"};

        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        for (String nodeName : nodeNames) {

            PModeXMLUtils xmlUtils = new PModeXMLUtils(page.getTextArea().getText());

            xmlUtils.setAttributeValueForNode(nodeName, "name", "invalidValue");

            page.getTextArea().fill(xmlUtils.printDoc());
            page.saveAndConfirm("This modification is invalid due to referenced name issue for entity " + nodeName);

            soft.assertTrue(page.getAlertArea().isError(), "Page shows error due to referenced name issue for entity " + nodeName);

            page.refreshPage();
        }

        soft.assertAll();
    }


    /* EDELIVERY-7295 - PMC-17 - PMode validations - validations are NOT case sensitive */
    @Test(description = "PMC-17", groups = {"multiTenancy", "singleTenancy"})
    public void switchCaseNames() throws Exception {
        SoftAssert soft = new SoftAssert();

        String[] nodeNames = {"party"
                , "role"
                , "mep"
                , "binding"
                , "service"
                , "action"
                , "mpc"
                , "property"
                , "payloadProfile"
                , "security"
                , "errorHandling"
                , "legConfiguration"};

        PModeCurrentPage page = new PModeCurrentPage(driver);
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        for (String nodeName : nodeNames) {

            PModeXMLUtils xmlUtils = new PModeXMLUtils(page.getTextArea().getText());

            xmlUtils.uppercaseAllAttributes(nodeName);

            page.getTextArea().fill(xmlUtils.printDoc());
            page.saveAndConfirm("This modification is valid for entity " + nodeName);

            soft.assertFalse(page.getAlertArea().isError(), "Page shows success for entity " + nodeName);

            if (page.getAlertArea().isError()) {
                log.debug(page.getAlertArea().getAlertMessage());
            }

            page.refreshPage();
        }

        soft.assertAll();
    }


}
