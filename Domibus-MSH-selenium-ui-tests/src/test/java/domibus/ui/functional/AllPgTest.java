package domibus.ui.functional;

import ddsl.dcomponents.AlertArea;
import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DObject;
import ddsl.enums.PAGES;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Alert.AlertPage;
import pages.errorLog.ErrorLogPage;
import pages.jms.JMSMonitoringPage;
import pages.messages.MessagesPage;
import pages.plugin_users.PluginUsersPage;
import pages.pmode.PartiesFilters;
import pages.pmode.parties.PModePartiesPage;
import utils.BaseTest;
import utils.DFileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rupam
 * @version 4.1.5
 */

public class AllPgTest extends BaseTest {

    private static String blackListedString = "'\\u0022(){}[];,+=%&*#<>/\\\\";
    private static String messageStatus = "ACKNOWLEDGED";
    private static String apRole = "SENDING";
    private static String messageType = "USER_MESSAGE";
    private static String notificationStatus = "NOTIFIED";
    private static String errCode = "EBMS_0001";

    /**
     * This Test method will verify extension of downloaded csv from all pages of Admin Console
     */
    @Test(description = "ALLDOM-1", groups = {"multiTenancy", "singleTenancy"})
    public void checkFileExtension() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        login(data.getAdminUser());

        for (PAGES ppage : PAGES.values()) {

            List<PAGES> pages = Arrays.asList(PAGES.PMODE_CURRENT, PAGES.TEST_SERVICE, PAGES.LOGGING);
            if (pages.contains(ppage)) {
                log.debug("Pages not having download csv feature are skipped");
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

    /**
     * This Test method will check error in case of presence of forbidden characters in all input fields for all pages
     */
    @Test(description = "ALLDOM-5", groups = {"multiTenancy", "singleTenancy"})
    public void checkFilterIpData() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        login(data.getAdminUser());

        for (PAGES ppage : PAGES.values()) {
            List<PAGES> pages = Arrays.asList(PAGES.MESSAGE_FILTER, PAGES.PMODE_CURRENT, PAGES.PMODE_ARCHIVE, PAGES.TRUSTSTORE
                    , PAGES.USERS, PAGES.AUDIT, PAGES.TEST_SERVICE, PAGES.LOGGING);
            if (pages.contains(ppage)) {
                log.debug("Pages not having input field to enter forbidden char are skipped");
                continue;
            }
            page.getSidebar().goToPage(ppage);
            searchSpecificPage(ppage, blackListedString);

            if (ppage.equals(PAGES.PMODE_PARTIES)) {
                log.debug("No gui validation is present for Pmode parties page");
                soft.assertFalse(new DObject(driver, new AlertArea(driver).alertMessage).isPresent(), "No alert message is shown");
            } else if (ppage.equals(PAGES.ALERTS)) {
                log.debug("Search button is disabled in case of invalid data present in alert id");
                soft.assertFalse(new AlertPage(driver).filters().getSearchButton().isEnabled(), "Search button is not enabled");
            } else {
                log.debug("Error alert message is shown in case of forbidden char");
                soft.assertTrue(page.getAlertArea().isError(), "Error for forbidden char is shown");
            }
        }
        soft.assertAll();
    }

    /**
     * This method will verify changing the selected domain resets all selected search filters and results
     */
    @Test(description = "ALLDOM-3", groups = {"multiTenancy"})
    public void verifyresetSearch() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        login(data.getAdminUser());

        for (PAGES ppage : PAGES.values()) {
            page.getDomainSelector().selectOptionByIndex(0);
            System.out.println(ppage);

            List<PAGES> pages = Arrays.asList(PAGES.MESSAGE_FILTER, PAGES.PMODE_CURRENT, PAGES.PMODE_ARCHIVE, PAGES.TRUSTSTORE
                    , PAGES.USERS, PAGES.AUDIT, PAGES.TEST_SERVICE, PAGES.LOGGING);

            if (pages.contains(ppage)) {
                log.debug("Pages not having search filters are skipped");
                continue;
            }
            page.getSidebar().goToPage(ppage);

            String searchData = searchSpecificPage(ppage, RandomStringUtils.random(2, true, false));
            page.getDomainSelector().selectOptionByIndex(1);
            page.waitForTitle();
            switch (ppage) {
                case MESSAGES:
                    soft.assertFalse(searchData.equals(new MessagesPage(driver).getFilters().getMessageIDInput().getText()), "Grid has diff data for both domain");
                    break;
                case ERROR_LOG:
                    soft.assertFalse(searchData.equals(new ErrorLogPage(driver).filters().getSignalMessIDInput().getText()), "Grid has diff data for both domain");
                    break;
                case PMODE_PARTIES:
                    soft.assertFalse(searchData.equals(new PModePartiesPage(driver).filters().getPartyIDInput().getText()), "Grid has diff data for both domain");
                    break;
                case JMS_MONITORING:
                    soft.assertFalse(searchData.equals(new JMSMonitoringPage(driver).filters().getJmsTypeInput().getText()), "Grid has diff data for both domain");
                    break;
                case PLUGIN_USERS:
                    soft.assertFalse(searchData.equals(new PluginUsersPage(driver).filters().getUsernameInput().getText()), "Grid has diff data for both domain");
                    break;
                case ALERTS:
                    soft.assertFalse(searchData.equals(new AlertPage(driver).filters().getAlertId().getText()), "Grid has diff data for both domain");
                    break;
                default:
                    soft.assertTrue(searchData.equals(null), "something went wrong");
            }
        }
        soft.assertAll();
    }

    /**
     * This method will perform search on different pages with specific data i.e forbidden char or random string in all input text field
     * @param page      : Page on which search is performed
     * @param inputData : String to be passed as input data in filter
     * @return : Specific filter data for given page
     */
    public String searchSpecificPage(PAGES page, String inputData) throws Exception {

        switch (page) {
            case MESSAGES:
                log.debug("Enter user defined data in input fields of Message page");
                MessagesPage mPage = new MessagesPage(driver);
                mPage.getFilters().advancedFilterBy(inputData, messageStatus, inputData, inputData,
                        inputData, apRole, messageType, notificationStatus, inputData, inputData, inputData, null, null);
                return mPage.getFilters().getMessageIDInput().getText();

            case ERROR_LOG:
                log.debug("Enter user defined data in input fields of Error log page");
                ErrorLogPage errorLogPage = new ErrorLogPage(driver);
                errorLogPage.filters().advancedSearch(inputData, inputData, null, null, inputData,
                        apRole, errCode, null, null);
                return errorLogPage.filters().getSignalMessIDInput().getText();

            case PMODE_PARTIES:
                log.debug("Enter user defined data in input fields of Pmode parties page");
                pages.pmode.PModePartiesPage pModePartiesPage = new pages.pmode.PModePartiesPage(driver);
                pModePartiesPage.filters().filter(inputData, inputData, inputData, inputData, PartiesFilters.PROCESS_ROLE.IR);
                return pModePartiesPage.filters().getPartyIDInput().getText();

            case JMS_MONITORING:
                log.debug("Enter user defined data in input fields of JMS Monitoring page");
                JMSMonitoringPage jmsMonitoringPage = new JMSMonitoringPage(driver);
                jmsMonitoringPage.filters().getJmsSelectorInput().fill(inputData);
                jmsMonitoringPage.filters().getJmsTypeInput().fill(inputData);
                jmsMonitoringPage.filters().getJmsSearchButton().click();
                return jmsMonitoringPage.filters().getJmsTypeInput().getText();

            case PLUGIN_USERS:
                log.debug("Enter user defined data in fields of Plugin user page");
                PluginUsersPage pluginUsersPage = new PluginUsersPage(driver);
                pluginUsersPage.filters().search(null, null, inputData, inputData);
                return pluginUsersPage.filters().getUsernameInput().getText();

            case ALERTS:
                log.debug("Enter user defined data in input fields of Alert page");
                AlertPage aPage = new AlertPage(driver);
                aPage.filters().getAdvanceLink().click();
                aPage.filters().getAlertId().fill(inputData);
                return aPage.filters().alertIdValidation.getText();
        }
        return null;
    }

}
