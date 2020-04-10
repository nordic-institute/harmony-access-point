package domibus.ui.functional;

import ddsl.dobjects.DWait;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pages.errorLog.ErrorLogPage;
import pages.messages.MessagesPage;
import pages.truststore.TruststorePage;
import utils.BaseTest;
import utils.BaseUXTest;
import utils.DFileUtils;
import utils.Generator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Rupam
 * @version 4.1
 */


public class ErrorLogPgTest extends BaseTest {


	/* This method will verify domain specific error messages on each domain by changing domain through domain
     selector by Super user */

    @Test(description = "ERR-8", groups = {"multiTenancy"})
    public void openErrorLogPage() throws Exception {
        SoftAssert soft = new SoftAssert();

        ErrorLogPage page = new ErrorLogPage(driver);
        MessagesPage mPage = new MessagesPage(driver);
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.ERROR_LOG);
        log.info("Current Domain Name is " + page.getDomainFromTitle());

        log.info("Compare grid data for both domain");
        mPage.compareMsgIDsOfDomains(soft);
        soft.assertAll();

    }

    @Test(description = "ERR-18", groups = {"multiTenancy", "singleTenancy"})
    public void errorLogForFailedMsg() throws Exception {
        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser());
        MessagesPage mPage = new MessagesPage(driver);
        ErrorLogPage page = new ErrorLogPage(driver);
        TruststorePage tPage = new TruststorePage(driver);
        tPage.waitForTitle();

        do {
            log.info("Send message for domain " + page.getDomainFromTitle());
            rest.uploadPMode("pmodes/Edelivery-blue-lessRetryTimeout.xml", page.getDomainFromTitle());
            String user = Generator.randomAlphaNumeric(10);

            log.info("Create plugin user for" + page.getDomainFromTitle());
            rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), page.getDomainFromTitle());

            log.info("send message for " + page.getDomainFromTitle());
            String messageID = messageSender.sendMessage(user, data.defaultPass(), null, null);
            page.getSidebar().goToPage(PAGES.TRUSTSTORE);

            String path = DFileUtils.getAbsolutePath("truststore/gateway_truststore.jks");

            tPage.uploadFile(path, "test123", soft);
            log.info("Navigate to Message page");
            page.getSidebar().goToPage(PAGES.MESSAGES);
            mPage.refreshPage();

            log.info("Wait for grid row to load");
            mPage.grid().waitForRowsToLoad();

            soft.assertTrue(mPage.grid().getRowInfo(0).containsValue(messageID), "Check presence of message id ");
            DWait wait = new DWait(driver);

            for (; ; ) {
                mPage.refreshPage();
                mPage.grid().waitForRowsToLoad();
                if (mPage.grid().getRowInfo(0).containsValue("SEND_ENQUEUED")) {
                    log.info("Wait for some time");
                    wait.forXMillis(100);

                } else if (mPage.grid().getRowInfo(0).containsValue("WAITING_FOR_RETRY")
                        || mPage.grid().getRowInfo(0).containsValue("SEND_FAILURE")) {
                    log.info("Break if message status changes to Waiting for retry or Send failure");
                    break;

                } else {
                    log.info("Row has message status other than waiting for retry or send failure");
                }
            }

            log.info("Navigate to Error log page");
            page.getSidebar().goToPage(PAGES.ERROR_LOG);

            log.info("Wait for grid row to load");
            page.grid().waitForRowsToLoad();
            soft.assertTrue(page.grid().getRowInfo(0).containsValue(messageID), "compare message id ");

            if (page.getDomainFromTitle() == null || page.getDomainFromTitle().equals(rest.getDomainNames().get(1))) {
                log.info("Break if it is single tenancy or current domain is other than default");
                break;
            }
            if (data.isMultiDomain()) {
                log.info("Change domain");
                page.getDomainSelector().selectOptionByIndex(1);
            }
            page.getSidebar().goToPage(PAGES.MESSAGES);
        } while (page.getDomainFromTitle().equals(rest.getDomainNames().get(1)));

        soft.assertAll();

    }


    @Test(description = "ERR-19", groups = {"multiTenancy", "singleTenancy"})
    public void checkTotalErrorLogs() throws Exception {
        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser());
        MessagesPage mPage = new MessagesPage(driver);
        ErrorLogPage page = new ErrorLogPage(driver);
        TruststorePage tPage = new TruststorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORE);
        tPage.waitForTitle();
        tPage.grid().waitForRowsToLoad();
        String path = DFileUtils.getAbsolutePath("truststore/gateway_truststore.jks");

        tPage.uploadFile(path, "test123", soft);
        log.info("uploading pmode");
        rest.uploadPMode("pmodes/Edelivery-blue-lessRetryTimeout.xml", null);

        log.info("Navigate to Pmode Current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        page.waitForTitle();

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
            page.grid().waitForRowsToLoad();
            for (; ; ) {
                mPage.refreshPage();
                log.info("Wait for grid row to load");
                mPage.grid().waitForRowsToLoad();
                log.info("execute loop till message status changes from WAITING_FOR_RETRY to SEND_FAILURE");
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
            page.refreshPage();
            page.grid().waitForRowsToLoad();
            int msgCount = 0;

            log.info("verify message id for all rows logged after retry");
            for (int j = 0; j < retryCount; j++) {
                soft.assertTrue(page.grid().getRowInfo(j).containsValue(messageID), "Message id is present");
            }
        }
        soft.assertAll();
    }


    @Test(priority=0,description = "ERR-20", groups = {"multiTenancy", "singleTenancy"})
    public void checkTimestamp() throws Exception {
        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser());
        MessagesPage mPage = new MessagesPage(driver);
        ErrorLogPage page = new ErrorLogPage(driver);
        TruststorePage tPage = new TruststorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORE);
        tPage.waitForTitle();
        tPage.grid().waitForRowsToLoad();
        String path = DFileUtils.getAbsolutePath("truststore/gateway_truststore.jks");

        tPage.uploadFile(path, "test123", soft);
        log.info("uploading pmode");
        rest.uploadPMode("pmodes/Edelivery-blue-lessRetryTimeout.xml", null);

        log.info("Navigate to Pmode current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        page.waitForTitle();

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
            page.grid().waitForRowsToLoad();
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
            page.grid().waitForRowsToLoad();

            ArrayList<String> dateStr = new ArrayList<String>();

            log.info("Find timestamp for each retry event");
            for (int j = 0; j <= retryCount; j++) {
                soft.assertTrue(page.grid().getRowInfo(j).containsValue(messageID), "Check row info has message id present");
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
        login(data.getAdminUser());
        MessagesPage mPage = new MessagesPage(driver);
        ErrorLogPage page = new ErrorLogPage(driver);
        TruststorePage tPage = new TruststorePage(driver);
        page.getSidebar().goToPage(PAGES.TRUSTSTORE);
        tPage.waitForTitle();
        tPage.grid().waitForRowsToLoad();

        String path = DFileUtils.getAbsolutePath("truststore/gateway_truststore.jks");

        tPage.uploadFile(path, "test123", soft);

        log.info("uploading pmode");
        rest.uploadPMode("pmodes/Edelivery-blue-lessRetryTimeout.xml", null);

        log.info("Naviagte to Pmode current page");
        page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
        page.grid().waitForRowsToLoad();

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
            page.grid().waitForRowsToLoad();
            for (; ; ) {
                mPage.grid().waitForRowsToLoad();
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
            page.grid().waitForRowsToLoad();

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






