package domibus.ui.functional;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.apache.commons.io.FileUtils;
import utils.BaseTest;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessageDetailsModal;
import pages.messages.MessageResendModal;
import pages.messages.MessagesPage;
import pages.messages.MessageFilters;
import utils.DFileUtils;
import utils.Generator;
import utils.soap_client.MessageConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class MessagesPgTest extends BaseTest {


	/*Doubleclik on one message*/
	@Test(description = "MSG-4", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickMessageRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		String messID = getMessageIDs(null, 1, false).get(0);

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);

		DGrid grid = page.grid();
		int index = grid.scrollTo("Message Id", messID);
		log.info("Checking message with ID " + messID);

		HashMap<String, String> info = grid.getRowInfo(index);
		grid.doubleClickRow(index);

		MessageDetailsModal modal = new MessageDetailsModal(driver);

		for (String s : info.keySet()) {
			if (s.contains("Action")) {
				continue;
			}
			log.info("Checking info in modal vs grid for field " + s);
			soft.assertEquals(modal.getValue(s), info.get(s), "Checking info in grid vs modal " + s);
		}
		soft.assertAll();
	}

	/*Filter messages using basic filters */
	@Test(description = "MSG-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterUsingBasicFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> messageIDs = getMessageIDs(null, 5, false);

		log.info("Login with admin");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);
		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		log.info("Getting all listed message info");
		List<HashMap<String, String>> allRowInfo = grid.getAllRowInfo();
		HashMap<String, String> fMessage = allRowInfo.get(0);
		//HashMap<String, String> fMessage = grid.getRowInfo(0);

		log.info("Basic filtering by " + fMessage);
		page.getFilters().basicFilterBy(fMessage.get("Message Id")
				, fMessage.get("Message Status")
				, fMessage.get("From Party Id")
				, fMessage.get("To Party Id"));
		page.grid().waitForRowsToLoad();

		log.info("Getting all listed message info after filtering");
		List<HashMap<String, String>> filteredRowInfo = grid.getAllRowInfo();
		//HashMap<String, String> filteredRowInfo = grid.getRowInfo(0);

		List<HashMap<String, String>> expectedResult = allRowInfo.stream().filter(rowInfo -> rowInfo.get("Message Id").equals(messageIDs.get(0))).collect(Collectors.toList());
		soft.assertTrue(filteredRowInfo.size() == expectedResult.size(), "No of listed items in page matches expected");
//		soft.assertTrue(fMessage.get("Message Id").equals(filteredRowInfo.get("Message Id")),"compare message id");
//		soft.assertTrue(fMessage.size()==filteredRowInfo.size(),"compare size");
		soft.assertAll();
	}

	/*Filter messages using advanced filters */
	@Test(description = "MSG-7", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		List<String> messageIDs = getMessageIDs(null, 5, false);

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();
		grid.getGridCtrl().getAllLnk().click();
		HashMap<String, String> messInfo = grid.getRowInfo(0);

		MessageFilters filters = page.getFilters();
		log.info("Filtering using advanced filters by " + messInfo);
		filters.advancedFilterBy(messInfo.get("Message Id")
				, messInfo.get("Message Status")
				, messInfo.get("From Party Id")
				, messInfo.get("To Party Id")
				, messInfo.get("Conversation Id")
				, messInfo.get("AP Role")
				, messInfo.get("Message Type")
				, messInfo.get("Notification Status")
				, messInfo.get("Ref To Message Id")
				, messInfo.get("Original Sender")
				, messInfo.get("Final Recipient")
				, null
				, null
		);

		page.grid().waitForRowsToLoad();

		for (int i = 0; i < page.grid().getRowsNo(); i++) {
			log.info("checking result with number " + i);
			page.grid().doubleClickRow(i);
			MessageDetailsModal modal = new MessageDetailsModal(driver);
			String messID = modal.getValue("Message ID");
			soft.assertEquals(modal.getValue("Conversation Id"), messInfo.get("Conversation Id"), messID + " - check conversation id");
			soft.assertEquals(modal.getValue("Ref To Message Id"), messInfo.get("Ref To Message Id"), messID + " - check Ref To Message Id");
			soft.assertEquals(modal.getValue("From Party Id"), MessageConstants.From_Party_Id, messID + " - check From Party Id");
			soft.assertEquals(modal.getValue("To Party Id"), MessageConstants.To_Party_Id, messID + " - check To Party Id");
			soft.assertEquals(modal.getValue("Original Sender"), MessageConstants.Original_Sender, messID + " - check Original Sender");
			soft.assertEquals(modal.getValue("Final Recipient"), MessageConstants.Final_Recipient, messID + " - check Final Recipient");
			soft.assertEquals(modal.getValue("AP Role"), MessageConstants.AP_Role, messID + " - check AP Role");

			page.clickVoidSpace();
		}

		soft.assertAll();
	}

	/* Filter messages so that there are no results */
	@Test(description = "MSG-8", groups = {"multiTenancy", "singleTenancy"})
	public void filterEmptyGrid() throws Exception {
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);

		int gridRows = page.grid().getRowsNo();
		int allRows = page.grid().getPagination().getTotalItems();
		log.info(String.format("Grid shows %s rows, pagination shows %s total items", gridRows, allRows));

		log.info("filtering so that grid shows 0 results");
		page.getFilters().basicFilterBy("invalidMessageId", "SEND_FAILURE", "domibus-blue", "domibus-blue");
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.grid().getRowsNo(), 0, "The grid is empty after search with 0 matching messages");

		soft.assertAll();
	}

	/* Filter messages so that there are no results then delete all criteria and press Search in case of Basic /Advance Search both */
	@Test(description = "MSG-9", groups = {"multiTenancy", "singleTenancy"})
	public void emptySearchAllResults() throws Exception {
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);

		int gridRows = page.grid().getRowsNo();
		int allRows = page.grid().getPagination().getTotalItems();
		log.info(String.format("Grid shows %s rows, pagination shows %s total items", gridRows, allRows));

		log.info("filtering so that grid shows 0 results");
		page.getFilters().basicFilterBy("invalidMessageId", "SEND_FAILURE", "domibus-blue", "domibus-blue");
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.grid().getRowsNo(), 0, "The grid is empty after search with 0 matching messages");

//		refresh because entering empty string everywhere does not trigger change event
		page.refreshPage();
		page.grid().waitForRowsToLoad();

		log.info("checking results after refresh");
		soft.assertEquals(page.grid().getRowsNo(), gridRows, "Empty search resets grid to original state (2)");
		soft.assertEquals(page.grid().getPagination().getTotalItems(), allRows, "Empty search resets grid to original state (2)");

		soft.assertAll();
	}

	/* Download message */
	// TODO - Need to add download feature availability criteria
	@Test(description = "MSG-11", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void downloadMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String pluginUsername = getPluginUser(null, DRoles.ADMIN, true, true).getString("userName");
		String messageID = messageSender.sendMessage(pluginUsername, data.defaultPass(), Generator.randomAlphaNumeric(10), Generator.randomAlphaNumeric(10));

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);

		page.grid().scrollToAndDoubleClick("Message Id", messageID);
		log.info("double clicked messag with id " + messageID);

		String zipPath = rest.downloadMessage(messageID, null);
		log.info("downloaded message to zip with path " + zipPath);

		HashMap<String, String> zipContent = unzip(zipPath);
		log.info("checking zip for files message and message.xml");
		boolean foundXMLfile = false;
		boolean foundMessfile = false;
		for (String fileName : zipContent.keySet()) {
			if (StringUtils.equalsIgnoreCase(fileName, "message")) {
				foundMessfile = true;
			}
			if (StringUtils.equalsIgnoreCase(fileName, "message.xml")) {
				foundXMLfile = true;
			}
		}

		soft.assertTrue(foundMessfile, "Found file containing message content");
		soft.assertTrue(foundXMLfile, "Found file containing message properties");
		log.info("checking the message payload");
		soft.assertEquals(zipContent.get("message"), MessageConstants.Message_Content, "Correct message content is downloaded");

		String xmlString = zipContent.get("message.xml");

		log.info("checking the message metadata");
		MessageDetailsModal modal = new MessageDetailsModal(driver);
		soft.assertEquals(modal.getValue("Message Id"),
				getValueFromXMLString(xmlString, "MessageId"), "MessageId - value matches");
		soft.assertEquals(modal.getValue("Conversation Id"),
				getValueFromXMLString(xmlString, "ConversationId"), "ConversationId - value matches");
		soft.assertEquals(modal.getValue("Ref To Message Id"),
				getValueFromXMLString(xmlString, "RefToMessageId"), "RefToMessageId - value matches");

		soft.assertTrue(xmlString.contains("name=\"originalSender\">" + modal.getValue("Original Sender"))
				, "Original Sender - value matches");

		soft.assertTrue(xmlString.contains("name=\"finalRecipient\">" + modal.getValue("Final Recipient"))
				, "Final Recipient - value matches");

		soft.assertAll();
	}

	/* Resend message */
	@Test(description = "MSG-12", groups = {"multiTenancy", "singleTenancy"})
	public void resendMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		String messageID = messageSender.sendMessage(user, data.defaultPass(), null, null);

		rest.uploadPMode("pmodes/doNothingInvalidRedRetry1.xml", null);

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);

		page.grid().scrollToAndSelect("Message Id", messageID);

		page.wait.forElementToBeEnabled(page.getResendButton().element);
		page.getResendButton().click();
		log.info("clicked Resend button");

		MessageResendModal modal = new MessageResendModal(driver);
		modal.getResendButton().click();

		boolean statusChanged = false;
		int c = 0;
		while (c < 20) {
			log.info("checking for status change");
			HashMap<String, String> info = page.grid().getRowInfo("Message Id", messageID);

			if (StringUtils.equalsIgnoreCase(info.get("Message Status"), "SEND_ENQUEUED")
					|| StringUtils.equalsIgnoreCase(info.get("Message Status"), "WAITING_FOR_RETRY")) {
				statusChanged = true;
				break;
			}
			c++;
		}

		soft.assertTrue(statusChanged, "Message changed to SEND_ENQUEUED");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MESSAGES_RESEND_MESSAGE_SUCCESS, "Page shows corect success message");
		soft.assertTrue(!page.getAlertArea().isError(), "Page shows success message");

		soft.assertAll();
	}

	/* Domain admin logs in and views messages */
	@Test(description = "MSG-13", groups = {"multiTenancy"} )
	public void messagesSegregatedByDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domainName = rest.getDomainNames().get(1);
		String domain = rest.getDomainCodeForName(domainName);
		log.info(String.format("Domain name = %s", domainName));

		String userDomain = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(userDomain, DRoles.ADMIN, data.defaultPass(), domain);
		log.info("created plugin user " + userDomain);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", domain);
		String messageIDDomain = messageSender.sendMessage(userDomain, data.defaultPass(), null, null);
		log.info("sent message with id " + messageIDDomain);

		log.info("Switching to default domain");
		String userDefault = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(userDefault, DRoles.ADMIN, data.defaultPass(), null);
		log.info("created plugin user " + userDefault);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		String messageIDDefault = messageSender.sendMessage(userDefault, data.defaultPass(), null, null);
		log.info("sent message with id " + messageIDDefault);

		String userAdmin = Generator.randomAlphaNumeric(10);
		rest.createUser(userAdmin, DRoles.ADMIN, data.defaultPass(), domain);
		log.info("created admin with username " + userAdmin);

		login(userAdmin, data.defaultPass()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in as created admin");
		MessagesPage page = new MessagesPage(driver);

		log.info("checking if new messages are visible");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain) >= 0, "Domain admin sees the domain message (1)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault) < 0, "Domain admin does NOT see the default domain message (2)");

		page.getSandwichMenu().logout();
		log.info("logged out");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in as super admin");

		log.info("checking on default domain if messages are visible");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain) < 0, "Super admin does NOT see the domain message while on the default domain (3)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault) >= 0, "Super admin sees the default domain message when on default domain (4)");

		log.info("switching to domain " + domainName);
		page.getDomainSelector().selectOptionByText(domainName);

		log.info("checking if messages are visible");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain) >= 0, "Super admin sees the domain message while on the proper domain (5)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault) < 0, "Super admin doesn't see the default domain message when on domain (6)");

		rest.deletePluginUser(userDefault, null);
		rest.deletePluginUser(userDomain, domain);
		rest.deletePluginUser(userAdmin, domain);
		log.info("delete admin and plugin users");
		soft.assertAll();
	}

	/* Super admin logs in and views messages for a selected domain, selects 1 message, and changes domain */
	@Test(description = "MSG-14", groups = {"multiTenancy"} )
	public void superSelectMessageChangeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domainName = rest.getDomainNames().get(1);
		String domain = rest.getDomainCodeForName(domainName);

		String messageIDDomain = getMessageIDs(domain, 1, false).get(0);
		String messageIDDefault = getMessageIDs(null, 1, false).get(0);

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in");

		MessagesPage page = new MessagesPage(driver);
		page.grid().scrollToAndSelect("Message Id", messageIDDefault);
		log.info("selected message from default domain");

		page.getDomainSelector().selectOptionByText(domainName);
		log.info("switch domain to " + domainName);

		log.info("check Download and Resend buttons status");
		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is disabled after domain switch");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download message button is disabled after domain switch");


		page.grid().scrollToAndSelect("Message Id", messageIDDomain);
		log.info("selected message from new domain");

		page.getDomainSelector().selectOptionByText("Default");
		log.info("switch domain to default");

		log.info("check Download and Resend buttons status");
		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is disabled after domain switch (2)");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download message button is disabled after domain switch (2)");

		soft.assertAll();
	}


	private HashMap<String, String> unzip(String zipFilePath) throws Exception {

		String destDir = zipFilePath.replaceAll(".zip", "");

		HashMap<String, String> zipContent = new HashMap<>();

		File dir = new File(destDir);
		// create output directory if it doesn't exist
		if (!dir.exists()) dir.mkdirs();


		FileInputStream fis;
		//buffer for read and write data to file
		byte[] buffer = new byte[1024];

		fis = new FileInputStream(zipFilePath);
		ZipInputStream zis = new ZipInputStream(fis);
		ZipEntry ze = zis.getNextEntry();
		while (ze != null) {

			String fileName = ze.getName();
			File newFile = new File(destDir + File.separator + fileName);

			System.out.println("Unzipping to " + newFile.getAbsolutePath());
			//create directories for sub directories in zip
			new File(newFile.getParent()).mkdirs();
			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			//close this ZipEntry
			zis.closeEntry();

			String fileContent = new String(Files.readAllBytes(Paths.get(newFile.getAbsolutePath())));
			zipContent.put(fileName, fileContent);

			ze = zis.getNextEntry();
		}

		//close last ZipEntry
		zis.closeEntry();
		zis.close();
		fis.close();

		return zipContent;
	}

	private String getValueFromXMLString(String xmlString, String key) {
		String start = key + ">";
		String end = "<\\/eb:" + key;

		Pattern p = Pattern.compile(start + "(.*?)" + end);
		Matcher m = p.matcher(xmlString);
		m.find();
		return m.group(1);
	}


	@Test(description = "MSG-25", groups = {"multiTenancy", "singleTenancy"})
	public void verifyHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);
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

		log.info("Extract complete path for downloaded file");
		String completeFilePath = filePath + File.separator + DFileUtils.getCompleteFileName(filePath);

		log.info("Click On show Link");
		page.gridControls().showCtrls();

		log.info("Click on All link");
		page.gridControls().showAllColumns();

		log.info("Verify headers from downloaded csv and grid");
		page.grid().checkCSVvsGridHeaders(completeFilePath, soft);
		soft.assertAll();

	}

    /* This method will verify domain specific messages on each domain by changing domain through domain
     selector by Super user */

	@Test(description = "MSG-15", groups = {"multiTenancy"} )
	public void verifyDomainSpecificMsgs() throws Exception {

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		log.info("Current Domain Name is " + page.getDomainFromTitle());
		log.info("Compare message ids data for both domain");
		page.compareMsgIDsOfDomains(soft);
		soft.assertAll();
	}

	@Test(description = "MSG-16", groups = {"multiTenancy"},enabled = false)
	public void downloadMsgs() throws Exception {
		SoftAssert soft = new SoftAssert();


		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

        log.info("Click on download csv button");
        File file = page.downloadCsv();

        log.info("Click on show link");
        page.gridControls().showCtrls();

        log.info("Click on All link to show all available column headers");
        page.gridControls().showAllColumns();

        String completeFilePath = file.getAbsolutePath();

        page.grid().checkCSVvsGridHeaders(completeFilePath, soft);
        int maxMess = page.grid().getRowsNo();

        page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxMess));
        page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxMess));
        page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxMess));
        page.getDomainSelector().selectOptionByIndex(1);
        page.grid().waitForRowsToLoad();

        maxMess = page.grid().getRowsNo();


        log.info("Click on download csv button");
        file = page.downloadCsv();

        log.info("Click on show link");
        page.gridControls().showCtrls();

        log.info("Click on All link to show all available column headers");
        page.gridControls().showAllColumns();

        completeFilePath = file.getAbsolutePath();

        page.grid().checkCSVvsGridHeaders(completeFilePath, soft);

        page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxMess));
        page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxMess));
        page.grid().checkCSVvsGridDataForSpecificRow(completeFilePath, soft, Generator.randomNumber(maxMess));

		soft.assertAll();
	}


}