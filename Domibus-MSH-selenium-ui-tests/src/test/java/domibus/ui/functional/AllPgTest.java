package domibus.ui.functional;

import ddsl.dcomponents.AlertArea;
import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DObject;
import ddsl.enums.PAGES;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.errorLog.ErrorLogPage;
import pages.jms.JMSMonitoringPage;
import pages.messages.MessagesPage;
import pages.plugin_users.PluginUsersPage;
import pages.pmode.PartiesFilters;
import pages.pmode.parties.PModePartiesPage;
import utils.BaseTest;
import utils.DFileUtils;

import java.io.File;


public class AllPgTest extends BaseTest {

    private static String blackListedString = "'\\u0022(){}[];,+=%&*#<>/\\\\";
    private static String messageStatus = "ACKNOWLEDGED";
    private static String apRole = "SENDING";
    private static String messageType = "USER_MESSAGE";
    private static String notificationStatus = "NOTIFIED";
    private static String errCode = "EBMS_0001";

    /*Check extension of downloaded file on all pages*/
    @Test(description = "ALLDOM-1", groups = {"multiTenancy", "singleTenancy"})
    public void checkFileExtension() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        login(data.getAdminUser());

        for (PAGES ppage : PAGES.values()) {

            if (ppage.equals(PAGES.PMODE_CURRENT) || ppage.equals(PAGES.TEST_SERVICE) || ppage.equals(PAGES.LOGGING)) {
                //skipping these pages as they dont have download csv feature available
                continue;
            }

            page.getSidebar().goToPage(ppage);
            log.info("Customized location for download");
            String filePath = DFileUtils.downloadFolderPath();

            log.info("Clean given directory");
            FileUtils.cleanDirectory(new File(filePath));

            log.info("Click on download csv button");
            page.clickDownloadCsvButton(page.getDownloadCsvButton().element);
            log.info("Wait for download to complete");
            page.wait.forXMillis(3000);

            log.info("Check if file is downloaded at given location");
            soft.assertTrue(DFileUtils.isFileDownloaded(filePath), "File is downloaded successfully");
            soft.assertTrue(DFileUtils.getFileExtension(filePath).equals("csv"), "Downloaded file extension is csv");
            soft.assertAll();
        }
    }

    /*Check non acceptance of forbidden characters in input filters of all pages*/
    @Test(description = "ALLDOM-5", groups = {"multiTenancy", "singleTenancy"})
    public void checkFilterIpData() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        login(data.getAdminUser());

        for (PAGES ppage : PAGES.values()) {

            if (ppage.equals(PAGES.MESSAGE_FILTER) || ppage.equals(PAGES.PMODE_CURRENT) || ppage.equals(PAGES.PMODE_ARCHIVE)
                    || ppage.equals(PAGES.TRUSTSTORE) || ppage.equals(PAGES.USERS) || ppage.equals(PAGES.AUDIT)
                    || ppage.equals(PAGES.ALERTS) || ppage.equals(PAGES.TEST_SERVICE) || ppage.equals(PAGES.LOGGING)) {

                //skipping these pages as they dont have filter area available
                continue;
            }
            page.getSidebar().goToPage(ppage);
            searchSpecificPage(ppage, blackListedString);

            if (ppage.equals(PAGES.PMODE_PARTIES)) {
                //Skipping Pmode parties page as Validation is handled at backend only
                soft.assertFalse(new DObject(driver, new AlertArea(driver).alertMessage).isPresent(), "No alert message is shown");
            } else {
                soft.assertTrue(page.getAlertArea().isError(), "Error for forbidden char is shown ");
            }
        }
        soft.assertAll();
    }

    /* Verify that changing the selected domain resets all selected search filters and results */
    @Test(description = "ALLDOM-3", groups = {"multiTenancy"})
    public void verifyresetSearch() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        login(data.getAdminUser());

        for (PAGES ppage : PAGES.values()) {
            page.getDomainSelector().selectOptionByIndex(0);

            if (ppage.equals(PAGES.MESSAGE_FILTER) || ppage.equals(PAGES.PMODE_CURRENT) || ppage.equals(PAGES.PMODE_ARCHIVE)
                    || ppage.equals(PAGES.TRUSTSTORE) || ppage.equals(PAGES.USERS) || ppage.equals(PAGES.AUDIT)
                    || ppage.equals(PAGES.ALERTS) || ppage.equals(PAGES.TEST_SERVICE) || ppage.equals(PAGES.LOGGING)) {

                //skipping these pages as they dont have filter area available
                continue;
            }
            page.getSidebar().goToPage(ppage);

            String searchData = searchSpecificPage(ppage, RandomStringUtils.random(2, true, false));
            page.getDomainSelector().selectOptionByIndex(1);
            page.waitForTitle();

            if (PAGES.MESSAGES.equals(ppage)) {
                soft.assertFalse(searchData.equals(new MessagesPage(driver).getFilters().getMessageIDInput().getText()), "Grid has diff data for both domain");

            } else if (PAGES.ERROR_LOG.equals(ppage)) {
                soft.assertFalse(searchData.equals(new ErrorLogPage(driver).filters().getSignalMessIDInput().getText()), "Grid has diff data for both domain");
            } else if (PAGES.PMODE_PARTIES.equals(ppage)) {
                soft.assertFalse(searchData.equals(new PModePartiesPage(driver).filters().getPartyIDInput().getText()), "Grid has diff data for both domain");

            } else if (PAGES.JMS_MONITORING.equals(ppage)) {
                soft.assertFalse(searchData.equals(new JMSMonitoringPage(driver).filters().getJmsTypeInput().getText()), "Grid has diff data for both domain");
            } else if (PAGES.PLUGIN_USERS.equals(ppage)) {
                soft.assertFalse(searchData.equals(new PluginUsersPage(driver).filters().getUsernameInput().getText()), "Grid has diff data for both domain");
            } else {
                soft.assertTrue(searchData.equals(null), "something went wrong");
            }
        }
        soft.assertAll();
    }

    // This method will perform search on different pages with specific data i.e forbidden char or random string in all input text field
    public String searchSpecificPage(PAGES page, String inputData) throws Exception {

        switch (page) {
            case MESSAGES:
                log.debug("Enter black listed char in input fields of Message page");
                MessagesPage mPage = new MessagesPage(driver);
                mPage.getFilters().advancedFilterBy(inputData, messageStatus, inputData, inputData,
                        inputData, apRole, messageType, notificationStatus, inputData, inputData, inputData, null, null);
                return mPage.getFilters().getMessageIDInput().getText();

            case ERROR_LOG:
                log.debug("Enter black listed char in input fields of Error log page");
                ErrorLogPage errorLogPage = new ErrorLogPage(driver);
                errorLogPage.filters().advancedSearch(inputData, inputData, null, null, inputData,
                        apRole, errCode, null, null);
                return errorLogPage.filters().getSignalMessIDInput().getText();

            case PMODE_PARTIES:
                log.debug("Enter in input fields of Pmode parties page");
                pages.pmode.PModePartiesPage pModePartiesPage = new pages.pmode.PModePartiesPage(driver);
                pModePartiesPage.filters().filter(inputData, inputData, inputData, inputData, PartiesFilters.PROCESS_ROLE.IR);
                return pModePartiesPage.filters().getPartyIDInput().getText();

            case JMS_MONITORING:
                log.debug("Enter in input fields of JMS Monitoring page");
                JMSMonitoringPage jmsMonitoringPage = new JMSMonitoringPage(driver);
                jmsMonitoringPage.filters().getJmsSelectorInput().fill(inputData);
                jmsMonitoringPage.filters().getJmsTypeInput().fill(inputData);
                jmsMonitoringPage.filters().getJmsSearchButton().click();
                return jmsMonitoringPage.filters().getJmsTypeInput().getText();

            case PLUGIN_USERS:
                log.debug("Enter in input fields of Plugin user page");
                PluginUsersPage pluginUsersPage = new PluginUsersPage(driver);
                pluginUsersPage.filters().search(null, null, inputData, inputData);
                return pluginUsersPage.filters().getUsernameInput().getText();

        }
        return null;
    }

}
