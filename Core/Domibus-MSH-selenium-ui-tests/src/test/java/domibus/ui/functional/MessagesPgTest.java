package domibus.ui.functional;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import domibus.ui.pojos.UIMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessageDetailsModal;
import pages.messages.MessageFilterArea;
import pages.messages.MessageResendModal;
import pages.messages.MessagesPage;
import utils.Gen;
import utils.TestRunData;
import utils.TestUtils;
import utils.soap_client.MessageConstants;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.testng.Assert.assertTrue;

public class MessagesPgTest extends SeleniumTest {


	private MessagesPage navigate() throws Exception {
		Reporter.log("logged in");
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		page.getFilters().showAllMessages();
		page.grid().waitForRowsToLoad();

		return page;
	}


	/* EDELIVERY-5056 - MSG-4 - Doubleclik on one message */
	@Test(description = "MSG-4", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickMessageRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		String messID = rest.getMessageIDs(null, 1, false).get(0);

		MessagesPage page = navigate();

		DGrid grid = page.grid();
		int index = grid.scrollTo("Message Id", messID);
		Reporter.log("Checking message with ID " + messID);
		log.info("Checking message with ID " + messID);

		HashMap<String, String> info = grid.getRowInfo(index);
		grid.doubleClickRow(index);

		MessageDetailsModal modal = new MessageDetailsModal(driver);

		for (String s : info.keySet()) {
			if (s.contains("Action")) {
				continue;
			}
			Reporter.log("Checking info in modal vs grid for field " + s);
			log.info("Checking info in modal vs grid for field " + s);
			soft.assertEquals(modal.getValue(s), info.get(s), "Checking info in grid vs modal " + s);
		}
		soft.assertAll();
	}


	/* EDELIVERY-5057 - MSG-5 - Filter messages using basic filters */
	@Test(description = "MSG-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterUsingBasicFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> messageIDs = rest.getMessageIDs(null, 5, false);

		Reporter.log("Login with admin");
		log.info("Login with admin");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage page = navigate();
		DGrid grid = page.grid();

		Reporter.log("Getting all listed message info");
		log.info("Getting all listed message info");
		List<HashMap<String, String>> allRowInfo = grid.getAllRowInfo();
		HashMap<String, String> fMessage = allRowInfo.get(0);

		Reporter.log("Basic filtering by " + fMessage);
		log.info("Basic filtering by " + fMessage);
		page.getFilters().basicFilterBy(fMessage.get("Message Id")
				, fMessage.get("Message Status")
				, fMessage.get("From Party Id")
				, fMessage.get("To Party Id"));
		page.grid().waitForRowsToLoad();

		Reporter.log("Getting all listed message info after filtering");
		log.info("Getting all listed message info after filtering");
		List<HashMap<String, String>> filteredRowInfo = grid.getAllRowInfo();

		List<HashMap<String, String>> expectedResult = allRowInfo.stream().filter(rowInfo -> rowInfo.get("Message Id").equals(messageIDs.get(0))).collect(Collectors.toList());
		soft.assertEquals(filteredRowInfo.size(), expectedResult.size(), "No of listed items in page matches expected");

		soft.assertAll();
	}

	/* EDELIVERY-5059 - MSG-7 - Filter messages using advanced filters */
	@Test(description = "MSG-7", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = navigate();

		JSONArray messages = rest.messages().getListOfMessages(null);
		UIMessage messInfo = mapper.readValue(messages.get(0).toString(), UIMessage.class);


		MessageFilterArea filters = page.getFilters();

		String receivedFromDate = TestRunData.DATEWIDGET_DATE_FORMAT.format(new Date(messInfo.getReceived() - 60000));
		String receivedToDate = TestRunData.DATEWIDGET_DATE_FORMAT.format(new Date(messInfo.getReceived() + 60000));


		Reporter.log("Filtering using advanced filters by " + messInfo);
		log.info("Filtering using advanced filters by " + messInfo);
		filters.advancedFilterBy(messInfo.getMessageId()
				, messInfo.getMessageStatus()
				, messInfo.getFromPartyId()
				, messInfo.getToPartyId()
				, messInfo.getConversationId()
				, messInfo.getMshRole()
				, messInfo.getMessageType()
				, messInfo.getNotificationStatus()
				, messInfo.getRefToMessageId()
				, messInfo.getOriginalSender()
				, messInfo.getFinalRecipient()
				, receivedFromDate
				, receivedToDate
		);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		grid.getGridCtrl().showAllColumns();

		List<HashMap<String, String>> listedResults = page.grid().getAllRowInfo();

		soft.assertTrue(listedResults.size() >= 1, "At least one result is listed");

		for (int i = 0; i < listedResults.size(); i++) {
			Reporter.log("checking result with number " + i);
			log.info("checking result with number " + i);
			HashMap<String, String> resultInfo = listedResults.get(i);
			String messID = resultInfo.get("Message Id");
			soft.assertEquals(resultInfo.get("Message Id"), messInfo.getMessageId(), "checked message id");
			soft.assertEquals(resultInfo.get("Conversation Id"), messInfo.getConversationId(), messID + " - check conversation id");
//			soft.assertTrue(StringUtils.equalsIgnoreCase(resultInfo.get("Ref To Message Id"), messInfo.getRefToMessageId()), messID + " - check Ref To Message Id");
			soft.assertEquals(resultInfo.get("From Party Id"), MessageConstants.From_Party_Id, messID + " - check From Party Id");
			soft.assertEquals(resultInfo.get("To Party Id"), MessageConstants.To_Party_Id, messID + " - check To Party Id");
			soft.assertEquals(resultInfo.get("Original Sender"), MessageConstants.Original_Sender, messID + " - check Original Sender");
			soft.assertEquals(resultInfo.get("Final Recipient"), MessageConstants.Final_Recipient, messID + " - check Final Recipient");
//			soft.assertEquals(resultInfo.get("AP Role"), MessageConstants.AP_Role, messID + " - check AP Role");
		}

		soft.assertAll();
	}

	/* EDELIVERY-5060 - MSG-8 - Filter messages so that there are no results */
	@Test(description = "MSG-8", groups = {"multiTenancy", "singleTenancy"})
	public void filterEmptyGrid() throws Exception {
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		Reporter.log("logged in");
		log.info("logged in");
		MessagesPage page = navigate();

		int gridRows = page.grid().getRowsNo();
		int allRows = page.grid().getPagination().getTotalItems();
		Reporter.log(String.format("Grid shows %s rows, pagination shows %s total items", gridRows, allRows));
		log.info(String.format("Grid shows %s rows, pagination shows %s total items", gridRows, allRows));

		Reporter.log("filtering so that grid shows 0 results");
		log.info("filtering so that grid shows 0 results");
		page.getFilters().basicFilterBy("invalidMessageId", null, null, null);
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.grid().getRowsNo(), 0, "The grid is empty after search with 0 matching messages");

		soft.assertAll();
	}

	/* EDELIVERY-5061 - MSG-9 - Filter messages so that there are no results then delete all filters and press Search */
	@Test(description = "MSG-9", groups = {"multiTenancy", "singleTenancy"})
	public void emptySearchAllResults() throws Exception {
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		Reporter.log("logged in");
		log.info("logged in");
		MessagesPage page = navigate();

		int gridRows = page.grid().getRowsNo();
		int allRows = page.grid().getPagination().getTotalItems();
		Reporter.log(String.format("Grid shows %s rows, pagination shows %s total items", gridRows, allRows));
		log.info(String.format("Grid shows %s rows, pagination shows %s total items", gridRows, allRows));

		Reporter.log("filtering so that grid shows 0 results");
		log.info("filtering so that grid shows 0 results");
		page.getFilters().basicFilterBy("invalidMessageId", null, null, null);
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.grid().getRowsNo(), 0, "The grid is empty after search with 0 matching messages");

//		refresh because entering empty string everywhere does not trigger change event
		page.refreshPage();
		page.getFilters().showAllMessages();
		page.grid().waitForRowsToLoad();

		Reporter.log("checking results after refresh");
		log.info("checking results after refresh");
		soft.assertEquals(page.grid().getRowsNo(), gridRows, "Empty search resets grid to original state (2)");
		soft.assertEquals(page.grid().getPagination().getTotalItems(), allRows, "Empty search resets grid to original state (2)");

		soft.assertAll();
	}

	/* EDELIVERY-5063 - MSG-11 - Download message */
	@Test(description = "MSG-11", groups = {"multiTenancy", "singleTenancy"})
	public void downloadMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessagesPage page = navigate();
		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		grid.getPagination().getPageSizeSelect().selectOptionByText("100");
		grid.waitForRowsToLoad();

		ArrayList<HashMap<String, String>> info = grid.getListedRowInfo();
		int toCheckIndex = -1;

		for (int i = 0; i < info.size(); i++) {
			String role = info.get(i).get("AP Role");
			String status = info.get(i).get("Message Status");
			List<String> actions = grid.getAvailableActionsForRow(i);

			if (role.equalsIgnoreCase("SENDING") && status.equalsIgnoreCase("SEND_FAILURE")) {
				soft.assertTrue(actions.contains("Resend"), "Message can be resent");
				soft.assertTrue(actions.contains("Download"), "Message can be Downloaded");
				toCheckIndex = i;
				break;
			}
		}

		if (toCheckIndex < 0) {
			throw new SkipException("Message with desired characteristics was not found");
		}

		String zipPath = page.downloadMessage(toCheckIndex);

		Reporter.log("downloaded message to zip with path " + zipPath);
		log.info("downloaded message to zip with path " + zipPath);

		HashMap<String, String> zipContent = TestUtils.unzip(zipPath);
		Reporter.log("checking zip for files message and message.xml");
		log.info("checking zip for files message and message.xml");
		boolean foundXMLfile = false;
		boolean foundMessfile = false;
		for (String fileName : zipContent.keySet()) {
			if (StringUtils.equalsIgnoreCase(fileName, "payloadName.extension")) {
				foundMessfile = true;
			}
			if (StringUtils.equalsIgnoreCase(fileName, "message.xml")) {
				foundXMLfile = true;
			}
		}

		soft.assertTrue(foundMessfile, "Found file containing message content");
		soft.assertTrue(foundXMLfile, "Found file containing message properties");
		Reporter.log("checking the message payload");
		log.info("checking the message payload");
//		soft.assertEquals(zipContent.get("message"), MessageConstants.Message_Content, "Correct message content is downloaded");

		String xmlString = zipContent.get("message.xml");

		grid.doubleClickRow(toCheckIndex);
		Reporter.log("double clicked message " + info.get(toCheckIndex));
		log.info("double clicked message " + info.get(toCheckIndex));

		Reporter.log("checking the message metadata");
		log.info("checking the message metadata");
		MessageDetailsModal modal = new MessageDetailsModal(driver);
		soft.assertEquals(modal.getValue("Message Id"),
				TestUtils.getValueFromXMLString(xmlString, "MessageId"), "MessageId - value matches");
		soft.assertEquals(modal.getValue("Conversation Id"),
				TestUtils.getValueFromXMLString(xmlString, "ConversationId"), "ConversationId - value matches");
		soft.assertEquals(modal.getValue("Ref To Message Id"),
				TestUtils.getValueFromXMLString(xmlString, "RefToMessageId"), "RefToMessageId - value matches");

		soft.assertTrue(xmlString.contains("name=\"originalSender\">" + modal.getValue("Original Sender"))
				, "Original Sender - value matches");

		soft.assertTrue(xmlString.contains("name=\"finalRecipient\">" + modal.getValue("Final Recipient"))
				, "Final Recipient - value matches");

		soft.assertAll();
	}

	/* EDELIVERY-5064 - MSG-12 - Resend message */
	@Test(description = "MSG-12", groups = {"multiTenancy", "singleTenancy"})
	public void resendMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("pmodes/doNothingInvalidRedRetry1.xml", null);
		List<String> messIds = rest.getMessageIDsWithStatus(null, "SEND_FAILURE");
		if (messIds.size() == 0) {
			throw new SkipException("Could not get messages with status SEND_FAILURE");
		}

		MessagesPage page = navigate();
		String messageID = messIds.get(0);

		int index = page.grid().scrollToAndSelect("Message Id", messageID);

		soft.assertTrue(page.getResendButton().isEnabled(), "Resend button is enabled");
		page.getResendButton().click();
		Reporter.log("clicked Resend button");
		log.info("clicked Resend button");

		MessageResendModal modal = new MessageResendModal(driver);
		modal.getResendButton().click();

		soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MESSAGES_RESEND_MESSAGE_SUCCESS, "Correct message is shown");

		boolean statusChanged = false;
		for (int i = 0; i < 20; i++) {
			Reporter.log("checking for status change");
			log.info("checking for status change");
			HashMap<String, String> info = page.grid().getRowInfo(index);
			Reporter.log(info.get("Message Status"));
			log.debug(info.get("Message Status"));
			if (StringUtils.equalsIgnoreCase(info.get("Message Status"), "SEND_ENQUEUED")
					|| StringUtils.equalsIgnoreCase(info.get("Message Status"), "WAITING_FOR_RETRY")) {
				statusChanged = true;
				break;
			}

		}

		soft.assertTrue(statusChanged, "Message changed");

		soft.assertAll();
	}

	/* EDELIVERY-5065 - MSG-13 - Domain admin logs in and views messages */
	@Test(description = "MSG-13", groups = {"multiTenancy"})
	public void messagesSegregatedByDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domainName = rest.getNonDefaultDomain();
		String domain = rest.getDomainCodeForName(domainName);
		Reporter.log(String.format("Domain name = %s", domainName));
		log.info(String.format("Domain name = %s", domainName));

		String userDomain = Gen.randomAlphaNumeric(10);
		rest.pluginUsers().createPluginUser(userDomain, DRoles.ADMIN, data.defaultPass(), domain);
		Reporter.log("created plugin user " + userDomain);
		log.info("created plugin user " + userDomain);
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", domain);
		String messageIDDomain = messageSender.sendMessage(userDomain, data.defaultPass(), null, null);
		Reporter.log("sent message with id " + messageIDDomain);
		log.info("sent message with id " + messageIDDomain);

		Reporter.log("Switching to default domain");
		log.info("Switching to default domain");
		String userDefault = Gen.randomAlphaNumeric(10);
		rest.pluginUsers().createPluginUser(userDefault, DRoles.ADMIN, data.defaultPass(), null);
		Reporter.log("created plugin user " + userDefault);
		log.info("created plugin user " + userDefault);
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		String messageIDDefault = messageSender.sendMessage(userDefault, data.defaultPass(), null, null);
		Reporter.log("sent message with id " + messageIDDefault);
		log.info("sent message with id " + messageIDDefault);

		String userAdmin = Gen.randomAlphaNumeric(10);
		rest.users().createUser(userAdmin, DRoles.ADMIN, data.defaultPass(), domain);
		Reporter.log("created admin with username " + userAdmin);
		log.info("created admin with username " + userAdmin);

		login(userAdmin, data.defaultPass()).getSidebar().goToPage(PAGES.MESSAGES);
		Reporter.log("logged in as created admin");
		log.info("logged in as created admin");
		MessagesPage page = navigate();
		page.grid().waitForRowsToLoad();

		Reporter.log("checking if new messages are visible");
		log.info("checking if new messages are visible");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain) >= 0, "Domain admin sees the domain message (1)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault) < 0, "Domain admin does NOT see the default domain message (2)");

		page.getSandwichMenu().logout();
		Reporter.log("logged out");
		log.info("logged out");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		Reporter.log("logged in as super admin");
		log.info("logged in as super admin");

		page.grid().waitForRowsToLoad();
		Reporter.log("checking on default domain if messages are visible");
		log.info("checking on default domain if messages are visible");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain) < 0, "Super admin does NOT see the domain message while on the default domain (3)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault) >= 0, "Super admin sees the default domain message when on default domain (4)");

		Reporter.log("switching to domain " + domainName);
		log.info("switching to domain " + domainName);
		page.getDomainSelector().selectOptionByText(domainName);
		page.grid().waitForRowsToLoad();

		Reporter.log("checking if messages are visible");
		log.info("checking if messages are visible");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain) >= 0, "Super admin sees the domain message while on the proper domain (5)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault) < 0, "Super admin doesn't see the default domain message when on domain (6)");

		rest.pluginUsers().deletePluginUser(userDefault, null);
		rest.pluginUsers().deletePluginUser(userDomain, domain);
		rest.pluginUsers().deletePluginUser(userAdmin, domain);
		Reporter.log("delete admin and plugin users");
		log.info("delete admin and plugin users");
		soft.assertAll();
	}

	/* EDELIVERY-5066 - MSG-14 - Super admin logs in and views messages for a selected domain, selects 1 message, and changes domain */
	@Test(description = "MSG-14", groups = {"multiTenancy"})
	public void superSelectMessageChangeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domainName = rest.getNonDefaultDomain();
		String domain = rest.getDomainCodeForName(domainName);

		String messageIDDomain = rest.getMessageIDs(domain, 1, false).get(0);
		String messageIDDefault = rest.getMessageIDs(null, 1, false).get(0);

		MessagesPage page = navigate();
		String defaultDomainName = page.getDomainSelector().getSelectedValue();
		page.grid().waitForRowsToLoad();

		page.grid().scrollToAndSelect("Message Id", messageIDDefault);
		Reporter.log("selected message from default domain");
		log.info("selected message from default domain");

		page.getDomainSelector().selectOptionByText(domainName);
		Reporter.log("switch domain to " + domainName);
		log.info("switch domain to " + domainName);
		page.getFilters().showAllMessages();
		page.grid().waitForRowsToLoad();


		Reporter.log("check Download and Resend buttons status");
		log.info("check Download and Resend buttons status");
		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is disabled after domain switch");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download message button is disabled after domain switch");


		page.grid().scrollToAndSelect("Message Id", messageIDDomain);
		Reporter.log("selected message from new domain");
		log.info("selected message from new domain");

		page.getDomainSelector().selectOptionByText(defaultDomainName);
		page.getFilters().showAllMessages();
		page.grid().waitForRowsToLoad();
		Reporter.log("switch domain to default");
		log.info("switch domain to default");


		Reporter.log("check Download and Resend buttons status");
		log.info("check Download and Resend buttons status");
		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is disabled after domain switch (2)");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download message button is disabled after domain switch (2)");

		soft.assertAll();
	}

	/* EDELIVERY-5067 - MSG-15 - Super admin logs in and views messages for a selected domain, navigates to second page of messages and changes domain */
	@Test(description = "MSG-15", groups = {"multiTenancy"})
	public void verifyDomainSpecificMsgs() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessagesPage page = navigate();

		Reporter.log("trying to go to page 2 if ti exists");
		log.info("trying to go to page 2 if ti exists");
		Pagination pag = page.grid().getPagination();
		if (pag.hasNextPage()) {
			Reporter.log("going to page 2");
			log.info("going to page 2");
			pag.goToNextPage();
		}

		Reporter.log("gathering listed ids on page 2");
		log.info("gathering listed ids on page 2");
		page.grid().waitForRowsToLoad();
		List<String> info_dom1 = page.grid().getListedValuesOnColumn("Message Id");


		Reporter.log("changing domain");
		log.info("changing domain");
		page.getDomainSelector().selectAnotherDomain();
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.grid().getPagination().getActivePage(), Integer.valueOf(1), "Pagination is set to first page");

		Reporter.log("gathering listed info");
		log.info("gathering listed info");
		List<String> info_dom2 = page.grid().getListedValuesOnColumn("Message Id");

		Reporter.log("checking listed message id are different");
		log.info("checking listed message id are different");
		for (String id : info_dom1) {
			soft.assertFalse(info_dom2.contains(id), "Message is found also in domain 2: " + id);
		}

		soft.assertAll();
	}

	/* EDELIVERY-5068 - MSG-16 - Download list of messages multitenancy */
	@Test(description = "MSG-16", groups = {"multiTenancy"})
	public void downloadCSV() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessagesPage page = navigate();

		Reporter.log("Click on download csv button");
		log.info("Click on download csv button");
		String completeFilePath = page.pressSaveCsvAndSaveFile();

		Reporter.log("Click on show link");
		log.info("Click on show link");
		page.grid().getGridCtrl().showCtrls();

		Reporter.log("Click on All link to show all available column headers");
		log.info("Click on All link to show all available column headers");
		page.grid().getGridCtrl().showAllColumns();

		page.grid().checkCSVvsGridHeaders(completeFilePath, soft);

		page.grid().relaxCheckCSVvsGridInfo(completeFilePath, soft, "datetime"); //checkCSVvsGridInfo(completeFilePath, soft);
		soft.assertAll();
	}

	/* EDELIVERY-8179 - MSG-29- Download message envelop */
	@Test(description = "MSG-29", groups = {"multiTenancy", "singleTenancy"})
	public void checkMsgEnvXML() throws Exception {

		String xmlFileNameUser = "user_message_envelope.xml";
		String xmlFileNameSignal = "signal_message_envelope.xml";

		MessagesPage page = navigate();

		log.info("locate message envelope to download");
		int index = -1;
		String[] statuses = {"ACKNOWLEDGED", "RECEIVED"};

		for (int i = 0; i < statuses.length; i++) {
			index = page.grid().scrollTo("Message Status", statuses[i]);
			if (index >= 0) {
				break;
			}
		}

		if (index < 0) {
			throw new SkipException("Could not find message with proper status");
		}

		String zipPath = page.downloadMessageEnvelop(index);
		HashMap<String, String> content = TestUtils.unzip(zipPath);

		assertTrue(content.containsKey(xmlFileNameSignal), xmlFileNameSignal+ " file found");
		assertTrue(content.containsKey(xmlFileNameUser), xmlFileNameUser+ " file found");

		assertTrue(containsIgnoreCase(content.get(xmlFileNameUser), MessageConstants.Final_Recipient));
		assertTrue(containsIgnoreCase(content.get(xmlFileNameUser), MessageConstants.Original_Sender));
		assertTrue(containsIgnoreCase(content.get(xmlFileNameUser), MessageConstants.From_Party_Id));
		assertTrue(containsIgnoreCase(content.get(xmlFileNameUser), MessageConstants.To_Party_Id));

	}


	/* EDELIVERY-9636 - MSG-36 - Filter by message type SIGNAL_MESSAGE */
	@Test(description = "MSG-36", groups = {"multiTenancy", "singleTenancy"})
	public void filterForSignalMessage() throws Exception {/**/
		SoftAssert soft = new SoftAssert();
		MessagesPage page = navigate();

		log.info("filter for SIGNAL_MESSAGES");
		MessageFilterArea filters = page.getFilters();
		filters.expandArea();
		filters.getMessageTypeSelect().selectOptionByText("SIGNAL_MESSAGE");
		filters.clickSearch();

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		soft.assertFalse(page.getAlertArea().isShown(), "No alert message is opened");

		List<HashMap<String, String>> listedResults = page.grid().getListedRowInfo();

		for (int i = 0; i < listedResults.size(); i++) {
			log.info("checking result with number " + i);
			soft.assertEquals(listedResults.get(i).get("Message Type"), "SIGNAL_MESSAGE");
		}

		soft.assertAll();
	}


	/* EDELIVERY-9637 - MSG-37 - Filter for test messages*/
	@Test(description = "MSG-37", groups = {"multiTenancy", "singleTenancy"})
	public void filterForTestMessage() throws Exception {/**/
		SoftAssert soft = new SoftAssert();
		MessagesPage page = navigate();

		log.info("filter for test messages");
		MessageFilterArea filters = page.getFilters();
		filters.expandArea();
		filters.getShowTestMessagesChk().check();
		filters.clickSearch();

		DGrid grid = page.grid();
		grid.getGridCtrl().showOnlyColumn("Action");
		grid.waitForRowsToLoad();

		soft.assertFalse(page.getAlertArea().isShown(), "No alert message is opened");

		List<HashMap<String, String>> listedResults = page.grid().getListedRowInfo();

		for (int i = 0; i < listedResults.size(); i++) {
			log.info("checking result with number " + i);

			soft.assertEquals(listedResults.get(i).get("Action"), "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/test");
		}

		soft.assertAll();
	}
	/* EDELIVERY-9638 - MSG-38 - Filter using search interval*/
	@Test(description = "MSG-38", groups = {"multiTenancy", "singleTenancy"})
	public void filterUsingReceivedInterval() throws Exception {/**/
		SoftAssert soft = new SoftAssert();
		MessagesPage page = navigate();

		log.info("filter for test messages");
		MessageFilterArea filters = page.getFilters();
		filters.expandArea();

		long timestamp = Calendar.getInstance().getTimeInMillis();
		filters.getMessageIntervalSelect().selectOptionByText("Last 4 hours");
		filters.clickSearch();

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		soft.assertFalse(page.getAlertArea().isShown(), "No alert message is opened");

		List<HashMap<String, String>> listedResults = page.grid().getListedRowInfo();

		for (int i = 0; i < listedResults.size(); i++) {
			log.info("checking result with number " + i);
			long messReceived = DateUtils.parseDate(listedResults.get(i).get("Received").replaceFirst("UTC\\+\\d+", ""), "dd-MM-yyy HH:mm:ss").getTime();
			soft.assertTrue(timestamp - messReceived <= 14400000, "Time difference is less than 30 minutes for mess: " + listedResults.get(i).get("Message Id"));
		}

		soft.assertAll();
	}
}

