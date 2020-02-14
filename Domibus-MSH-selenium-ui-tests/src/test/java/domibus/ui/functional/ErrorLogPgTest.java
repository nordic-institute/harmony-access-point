package domibus.ui.functional;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DWait;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pages.errorLog.ErrorLogPage;
import pages.errorLog.ErrorModal;
import pages.messages.MessagesPage;
import pages.pmode.current.PModeCurrentPage;
import rest.RestServicePaths;
import utils.BaseUXTest;
import utils.DFileUtils;
import utils.Generator;
import utils.TestUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Rupam
 * @version 4.1
 */


public class ErrorLogPgTest extends BaseUXTest {


	/* This method will verify domain specific error messages on each domain by changing domain through domain
     selector by Super user */

    @Test(description = "ERR-8", groups = {"multiTenancy"})
    public void openErrorLogPage() throws Exception {
        SoftAssert soft = new SoftAssert();

        ErrorLogPage page = new ErrorLogPage(driver);
        MessagesPage mPage = new MessagesPage(driver);
        page.getSidebar().goToPage(PAGES.ERROR_LOG);
        log.info("Current Domain Name is " + page.getDomainFromTitle());

        log.info("Compare grid data for both domain");
        mPage.compareGridData(soft, "Message Id");
        soft.assertAll();

    }

    @Test(description = "ERR-18", groups = {"multiTenancy", "singleTenancy"})
    public void errorLogForFailedMsg() throws Exception {
        SoftAssert soft = new SoftAssert();
        MessagesPage mPage = new MessagesPage(driver);
        ErrorLogPage page = new ErrorLogPage(driver);
        log.info("uploading pmode");
        rest.uploadPMode("pmodes/Edelivery-blue-lessRetryTimeout.xml", null);
        String user = Generator.randomAlphaNumeric(10);

        log.info("Create plugin user");
        rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);

        log.info("send message ");
        String messageID = messageSender.sendMessage(user, data.defaultPass(), null, null);
        log.info("Message id " + messageID);

        log.info("Navigate to Message page");
        page.getSidebar().goToPage(PAGES.MESSAGES);
        mPage.refreshPage();

        log.info("Wait for grid row to load");
        mPage.grid().waitForRowsToLoad();

        log.info("Check Message status for first row");
        soft.assertTrue(mPage.grid().getRowInfo(0).containsValue("WAITING_FOR_RETRY"));

        log.info("Navigate to Error log page");
        page.getSidebar().goToPage(PAGES.ERROR_LOG);

        log.info("Wait for grid row to load");
        page.grid().waitForRowsToLoad();
        log.info("Verify message id for first row");
        soft.assertTrue(page.grid().getRowInfo(0).containsValue(messageID));

        log.info("Extract all message ids from error log page:message id column");
        List<String> msgIds = page.grid().getValuesOnColumn("Message Id");

        soft.assertTrue(msgIds.contains(messageID), "Error log page has record present");

        if (data.isIsMultiDomain()) {
            log.info("uploading pmode");
            String domainName = rest.getDomainNames().get(1);
            String domain = rest.getDomainCodeForName(domainName);

            log.info("Upload pmode for second domain ");
            rest.uploadPMode("pmodes/Edelivery-blue-lessRetryTimeout.xml", domain);
            String userr = Generator.randomAlphaNumeric(10);

            log.info("Create plugin user");
            rest.createPluginUser(userr, DRoles.ADMIN, data.defaultPass(), domain);

            log.info("Change domain from admin console");
            page.getDomainSelector().selectOptionByIndex(1);

            log.info("Send message from second domain");
            String messageeID = messageSender.sendMessage(userr, data.defaultPass(), null, null);
            log.info("Message id " + messageID);

            //page.getSidebar().goToPage(PAGES.MESSAGES);
            mPage.refreshPage();

            log.info("Wait for grid row to load");
            mPage.grid().waitForRowsToLoad();
            log.info("Verify message status for first row");
            soft.assertTrue(mPage.grid().getRowInfo(0).containsValue("WAITING_FOR_RETRY"));

            log.info("Navigate to error log");
            page.getSidebar().goToPage(PAGES.ERROR_LOG);

            log.info("Wait for grid row to load");
            page.grid().waitForRowsToLoad();

            log.info("Verify presence of message id in first row");
            soft.assertTrue(page.grid().getRowInfo(0).containsValue(messageeID));

            log.info("Extract all message ids from Error log page");
            List<String> msggIds = page.grid().getValuesOnColumn("Message Id");
            soft.assertTrue(msggIds.contains(messageeID), "Error log page has record present");
        }
        soft.assertAll();
    }

    @Test(description = "ERR-19", groups = {"multiTenancy", "singleTenancy"})
    public void totalErrorLogs() throws Exception {
        SoftAssert soft = new SoftAssert();
        MessagesPage mPage = new MessagesPage(driver);
        ErrorLogPage page = new ErrorLogPage(driver);

        log.info("uploading pmode");
        rest.uploadPMode("pmodes/Edelivery-blue-lessRetryTimeout.xml", null);

        log.info("Navigate to Pmode Current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        log.info("Find retryTimeout & retryCount from pmode");
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File("./src/main/resources/pmodes/Edelivery-blue-lessRetryTimeout.xml"));
        NodeList nodes = doc.getElementsByTagName("as4");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element party = (Element) nodes.item(i);
            Element name = (Element) party.getElementsByTagName("receptionAwareness").item(0);
            log.info(name.getAttribute("retry"));
            String retryParameters[] = name.getAttribute("retry").split(";");
            int retryCount = Integer.parseInt(retryParameters[1]);
            int retryTimeout = Integer.parseInt(retryParameters[0]);

            log.info("Create plugin user");
            String user = Generator.randomAlphaNumeric(10);
            rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);
            log.info("Send message id ");
            String messageID = messageSender.sendMessage(user, data.defaultPass(), null, null);

            log.info("Navigate to Messages page");
            page.getSidebar().goToPage(PAGES.MESSAGES);
            for (; ; ) {
                log.info("execute loop till message status chnages from WAITING_FOR_RETRY to SEND_FAILURE");
                if (mPage.grid().getRowInfo(0).containsValue(messageID)) {
                    mPage.refreshPage();

                    log.info("Wait for grid row to load");
                    mPage.grid().waitForRowsToLoad();

                    log.info("Break from loop if Message status is SEND_FAILURE");
                    if (mPage.grid().getRowInfo(0).containsValue("SEND_FAILURE")) {
                        break;
                    }
                } else {
                    page.wait.forXMillis(2000);
                }

            }

            log.info("Navigate to error log");
            page.getSidebar().goToPage(PAGES.ERROR_LOG);

            log.info("verify message id for all rows logged after retry");
            for (int j = 0; j < retryCount; j++) {
                soft.assertTrue(page.grid().getRowInfo(j).containsValue(messageID));
            }
        }
        soft.assertAll();
    }


    @Test(description = "ERR-20", groups = {"multiTenancy", "singleTenancy"})
    public void checkTimestamp() throws Exception {
        SoftAssert soft = new SoftAssert();
        MessagesPage mPage = new MessagesPage(driver);
        ErrorLogPage page = new ErrorLogPage(driver);
        log.info("uploading pmode");
        rest.uploadPMode("pmodes/Edelivery-blue-lessRetryTimeout.xml", null);

        log.info("Navigate to Pmode current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        log.info("Extract value of retryTimeout and retryCount");
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File("./src/main/resources/pmodes/Edelivery-blue-lessRetryTimeout.xml"));
        NodeList nodes = doc.getElementsByTagName("as4");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element party = (Element) nodes.item(i);
            Element name = (Element) party.getElementsByTagName("receptionAwareness").item(0);
            log.info(name.getAttribute("retry"));
            String retryParameters[] = name.getAttribute("retry").split(";");
            int retryCount = Integer.parseInt(retryParameters[1]);
            int retryTimeout = Integer.parseInt(retryParameters[0]);

            log.info("Create plugin user");
            String user = Generator.randomAlphaNumeric(10);
            rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);

            log.info("Send message");
            String messageID = messageSender.sendMessage(user, data.defaultPass(), null, null);

            log.info("Navigate to message page");
            page.getSidebar().goToPage(PAGES.MESSAGES);
            for (; ; ) {
                log.info("Execute loop until message status changes to SEND_FAILURE");
                if (mPage.grid().getRowInfo(0).containsValue(messageID)) {
                    mPage.refreshPage();

                    log.info("Wait for grid row to load");
                    mPage.grid().waitForRowsToLoad();

                    log.info("Break loop if Message status chnage to SEND_FAILURE");
                    if (mPage.grid().getRowInfo(0).containsValue("SEND_FAILURE")) {
                        soft.assertTrue(mPage.grid().getRowInfo(0).containsValue("SEND_FAILURE"), "Message status changed to SEND_FAILURE after all retries");
                        break;
                    }
                } else {
                    page.wait.forXMillis(2000);
                }

            }

            log.info("Navigate to Error log page");
            page.getSidebar().goToPage(PAGES.ERROR_LOG);
            ArrayList<String> dateStr = new ArrayList<String>();

            log.info("Find timestamp for each retry event");
            for (int j = 0; j <= retryCount; j++) {
                soft.assertTrue(page.grid().getRowInfo(j).containsValue(messageID));
                String str[] = page.grid().getRowInfo(j).get("Timestamp").split("GMT");
                String str1[] = str[0].split(" ");
                dateStr.add(str1[1]);
                log.info(" Retry happens at" + dateStr.get(j));
            }

            log.info("Find time difference between each retry");
            for (int k = 0; k < retryCount; k++) {
                Date d1 = new SimpleDateFormat("HH:mm:ss").parse(dateStr.get(k + 1));
                Date d2 = new SimpleDateFormat("HH:mm:ss").parse(dateStr.get(k));

                long difference = d2.getTime() - d1.getTime();
                soft.assertTrue((difference / 1000 / 60) == (retryTimeout / retryCount), "Retry happens at correct interval");

            }

        }


        soft.assertAll();
    }

    @Test(description = "ERR-21", groups = {"multiTenancy", "singleTenancy"})
    public void checkErrorCode() throws Exception {
        SoftAssert soft = new SoftAssert();
        MessagesPage mPage = new MessagesPage(driver);
        ErrorLogPage page = new ErrorLogPage(driver);

        log.info("uploading pmode");
        rest.uploadPMode("pmodes/Edelivery-blue-lessRetryTimeout.xml", null);

        log.info("Naviagte to Pmode current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

        log.info("Extract retry count and timeout value from pmode configuration");
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File("./src/main/resources/pmodes/Edelivery-blue-lessRetryTimeout.xml"));
        NodeList nodes = doc.getElementsByTagName("as4");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element party = (Element) nodes.item(i);
            Element name = (Element) party.getElementsByTagName("receptionAwareness").item(0);
            log.info(name.getAttribute("retry"));
            String retryParameters[] = name.getAttribute("retry").split(";");
            int retryCount = Integer.parseInt(retryParameters[1]);

            log.info("Create plugin users");
            String user = Generator.randomAlphaNumeric(10);
            rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);

            log.info("Send message");
            String messageID = messageSender.sendMessage(user, data.defaultPass(), null, null);

            log.info("Navigate to Message page");
            page.getSidebar().goToPage(PAGES.MESSAGES);
            for (; ; ) {
                log.info("Verify presence of message id for first row");
                if (mPage.grid().getRowInfo(0).containsValue(messageID)) {
                    mPage.refreshPage();

                    log.info("Wait for grid row to load");
                    mPage.grid().waitForRowsToLoad();

                    log.info("Break from loop if message status is SEND_FAILURE");
                    if (mPage.grid().getRowInfo(0).containsValue("SEND_FAILURE")) {
                        break;
                    }
                } else {
                    page.wait.forXMillis(2000);
                }

            }

            log.info("Navigate to Error log page");
            page.getSidebar().goToPage(PAGES.ERROR_LOG);

            log.info("Extract error code of first row");
            String errorCode = page.grid().getRowInfo(0).get("Error Code");

            log.info("Verify message id and error code for all row corresponding to retry events");
            for (int j = 0; j < retryCount; j++) {
                soft.assertTrue(page.grid().getRowInfo(j).containsValue(messageID), "Row has message id same as expected");
                soft.assertTrue(page.grid().getRowInfo(j).get("Error Code").equals(errorCode), "Error Code for all records are same");
            }
        }
        soft.assertAll();
    }


}






