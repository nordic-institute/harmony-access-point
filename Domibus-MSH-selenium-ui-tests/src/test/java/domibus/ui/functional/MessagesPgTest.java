package domibus.ui.functional;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import domibus.ui.pojos.UIMessage;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessageDetailsModal;
import pages.messages.MessageFilters;
import pages.messages.MessageResendModal;
import pages.messages.MessagesPage;
import utils.Gen;
import utils.TestRunData;
import utils.TestUtils;
import utils.soap_client.MessageConstants;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class MessagesPgTest extends SeleniumTest {
	
	
	private MessagesPage navigate() throws Exception {
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.grid().waitForRowsToLoad();
		return page;
	}
	
	
	/*Doubleclik on one message*/
	@Test(description = "MSG-4", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickMessageRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String messID = rest.getMessageIDs(null, 1, false).get(0);
		
		MessagesPage page = navigate();
		
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
	
	@Test
	public void sendSomeMess() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String messID = messageSender.sendMessage("qweqwe", "QW!@qw12", null, null);
		
		soft.assertAll();
	}
	
	/*Filter messages using basic filters */
	@Test(description = "MSG-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterUsingBasicFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		List<String> messageIDs = rest.getMessageIDs(null, 5, false);
		
		log.info("Login with admin");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);
		DGrid grid = page.grid();
		
		log.info("Getting all listed message info");
		List<HashMap<String, String>> allRowInfo = grid.getAllRowInfo();
		HashMap<String, String> fMessage = allRowInfo.get(0);
		
		log.info("Basic filtering by " + fMessage);
		page.getFilters().basicFilterBy(fMessage.get("Message Id")
				, fMessage.get("Message Status")
				, fMessage.get("From Party Id")
				, fMessage.get("To Party Id"));
		page.grid().waitForRowsToLoad();
		
		log.info("Getting all listed message info after filtering");
		List<HashMap<String, String>> filteredRowInfo = grid.getAllRowInfo();
		
		List<HashMap<String, String>> expectedResult = allRowInfo.stream().filter(rowInfo -> rowInfo.get("Message Id").equals(messageIDs.get(0))).collect(Collectors.toList());
		soft.assertEquals(filteredRowInfo.size(), expectedResult.size(), "No of listed items in page matches expected");
		
		soft.assertAll();
	}
	
	/*Filter messages using advanced filters */
	@Test(description = "MSG-7", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = navigate();
		
		JSONArray messages = rest.messages().getListOfMessages(null);
		UIMessage messInfo = mapper.readValue(messages.get(0).toString(), UIMessage.class);
		
		
		MessageFilters filters = page.getFilters();
		
		String receivedFromDate = TestRunData.DATEWIDGET_DATE_FORMAT.format(new Date(messInfo.getReceived() - 60000));
		String receivedToDate = TestRunData.DATEWIDGET_DATE_FORMAT.format(new Date(messInfo.getReceived() + 60000));
		
		
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

		soft.assertTrue(listedResults.size()>=1 , "At least one result is listed");
		
		for (int i = 0; i < listedResults.size(); i++) {
			log.info("checking result with number " + i);
			HashMap<String, String> resultInfo = listedResults.get(i);
			String messID = resultInfo.get("Message Id");
			soft.assertEquals(resultInfo.get("Message Id"), messInfo.getMessageId(), "checked message id");
			soft.assertEquals(resultInfo.get("Conversation Id"), messInfo.getConversationId(), messID + " - check conversation id");
			soft.assertEquals(resultInfo.get("Ref To Message Id"), messInfo.getRefToMessageId(), messID + " - check Ref To Message Id");
			soft.assertEquals(resultInfo.get("From Party Id"), MessageConstants.From_Party_Id, messID + " - check From Party Id");
			soft.assertEquals(resultInfo.get("To Party Id"), MessageConstants.To_Party_Id, messID + " - check To Party Id");
			soft.assertEquals(resultInfo.get("Original Sender"), MessageConstants.Original_Sender, messID + " - check Original Sender");
			soft.assertEquals(resultInfo.get("Final Recipient"), MessageConstants.Final_Recipient, messID + " - check Final Recipient");
			soft.assertEquals(resultInfo.get("AP Role"), MessageConstants.AP_Role, messID + " - check AP Role");
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
		page.getFilters().basicFilterBy("invalidMessageId", null, null, null);
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
		page.getFilters().basicFilterBy("invalidMessageId", null, null, null);
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
	@Test(description = "MSG-11", groups = {"multiTenancy", "singleTenancy"})
	public void downloadMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("uploading self sending pmode");
		
		String newMessId = null;
		
		JSONArray messages = rest.messages().getListOfMessages(null);
		for (int i = 0; i < messages.length(); i++) {
			JSONObject mess = messages.getJSONObject(i);
			if(StringUtils.equalsIgnoreCase(mess.getString("messageStatus"), "SEND_FAILURE")
			|| StringUtils.equalsIgnoreCase(mess.getString("messageStatus"), "RECEIVED")){
				newMessId = mess.getString("messageId");
				break;
			}
		}
		
		if(StringUtils.isEmpty(newMessId)){
			throw new SkipException("Could not find message to download");
		}
		
		MessagesPage page = new MessagesPage(driver);
		page.grid().waitForRowsToLoad();
		
		page.grid().scrollToAndDoubleClick("Message Id", newMessId);
		log.info("double clicked message with id " + newMessId);
		
		String zipPath = rest.messages().downloadMessage(newMessId, null);
		log.info("downloaded message to zip with path " + zipPath);
		
		HashMap<String, String> zipContent = TestUtils.unzip(zipPath);
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
//		soft.assertEquals(zipContent.get("message"), MessageConstants.Message_Content, "Correct message content is downloaded");
		
		String xmlString = zipContent.get("message.xml");
		
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
	
	/* Resend message */
	@Test(description = "MSG-12", groups = {"multiTenancy", "singleTenancy"})
	public void resendMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRedRetry1.xml", null);
		List<String> messIds = rest.getMessageIDsWithStatus(null, "SEND_FAILURE");
		if(messIds.size() == 0){
			throw new SkipException("Could not get messages with status SEND_FAILURE");
		}
		
		MessagesPage page = navigate();
		String messageID = messIds.get(0);

		int index = page.grid().scrollToAndSelect("Message Id", messageID);

		soft.assertTrue(page.getResendButton().isEnabled() , "Resend button is enabled");
		page.getResendButton().click();
		log.info("clicked Resend button");

		MessageResendModal modal = new MessageResendModal(driver);
		modal.getResendButton().click();

		soft.assertTrue(!page.getAlertArea().isError() , "Success message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),DMessages.MESSAGES_RESEND_MESSAGE_SUCCESS , "Correct message is shown");
		
		boolean statusChanged = false;
		for (int i = 0; i < 20; i++) {
			log.info("checking for status change");
			HashMap<String, String> info = page.grid().getRowInfo(index);
			log.debug(info.get("Message Status"));
			if (StringUtils.equalsIgnoreCase(info.get("Message Status"), "SEND_ENQUEUED")
			|| StringUtils.equalsIgnoreCase(info.get("Message Status"), "WAITING_FOR_RETRY")) {
				statusChanged = true;
				break;
			}
			page.wait.forXMillis(1000);
		}
		
		soft.assertTrue(statusChanged, "Message changed");
		
		soft.assertAll();
	}
	
	/* Domain admin logs in and views messages */
	@Test(description = "MSG-13", groups = {"multiTenancy"})
	public void messagesSegregatedByDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String domainName = rest.getNonDefaultDomain();
		String domain = rest.getDomainCodeForName(domainName);
		log.info(String.format("Domain name = %s", domainName));
		
		String userDomain = Gen.randomAlphaNumeric(10);
		rest.pluginUsers().createPluginUser(userDomain, DRoles.ADMIN, data.defaultPass(), domain);
		log.info("created plugin user " + userDomain);
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", domain);
		String messageIDDomain = messageSender.sendMessage(userDomain, data.defaultPass(), null, null);
		log.info("sent message with id " + messageIDDomain);
		
		log.info("Switching to default domain");
		String userDefault = Gen.randomAlphaNumeric(10);
		rest.pluginUsers().createPluginUser(userDefault, DRoles.ADMIN, data.defaultPass(), null);
		log.info("created plugin user " + userDefault);
		rest.pmode().uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		String messageIDDefault = messageSender.sendMessage(userDefault, data.defaultPass(), null, null);
		log.info("sent message with id " + messageIDDefault);
		
		String userAdmin = Gen.randomAlphaNumeric(10);
		rest.users().createUser(userAdmin, DRoles.ADMIN, data.defaultPass(), domain);
		log.info("created admin with username " + userAdmin);
		
		login(userAdmin, data.defaultPass()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in as created admin");
		MessagesPage page = new MessagesPage(driver);
		page.grid().waitForRowsToLoad();
		
		log.info("checking if new messages are visible");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain) >= 0, "Domain admin sees the domain message (1)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault) < 0, "Domain admin does NOT see the default domain message (2)");
		
		page.getSandwichMenu().logout();
		log.info("logged out");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in as super admin");
		
		page.grid().waitForRowsToLoad();
		log.info("checking on default domain if messages are visible");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain) < 0, "Super admin does NOT see the domain message while on the default domain (3)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault) >= 0, "Super admin sees the default domain message when on default domain (4)");
		
		log.info("switching to domain " + domainName);
		page.getDomainSelector().selectOptionByText(domainName);
		page.grid().waitForRowsToLoad();
		
		log.info("checking if messages are visible");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain) >= 0, "Super admin sees the domain message while on the proper domain (5)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault) < 0, "Super admin doesn't see the default domain message when on domain (6)");
		
		rest.pluginUsers().deletePluginUser(userDefault, null);
		rest.pluginUsers().deletePluginUser(userDomain, domain);
		rest.pluginUsers().deletePluginUser(userAdmin, domain);
		log.info("delete admin and plugin users");
		soft.assertAll();
	}
	
	/* Super admin logs in and views messages for a selected domain, selects 1 message, and changes domain */
	@Test(description = "MSG-14", groups = {"multiTenancy"})
	public void superSelectMessageChangeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String domainName = rest.getNonDefaultDomain();
		String domain = rest.getDomainCodeForName(domainName);
		
		String messageIDDomain = rest.getMessageIDs(domain, 1, false).get(0);
		String messageIDDefault = rest.getMessageIDs(null, 1, false).get(0);
		
		MessagesPage page = new MessagesPage(driver);
		
		String defaultDomainName = page.getDomainSelector().getSelectedValue();
		
		page.refreshPage();
		page.grid().waitForRowsToLoad();
		
		page.grid().scrollToAndSelect("Message Id", messageIDDefault);
		log.info("selected message from default domain");
		
		page.getDomainSelector().selectOptionByText(domainName);
		log.info("switch domain to " + domainName);
		
		log.info("check Download and Resend buttons status");
		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is disabled after domain switch");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download message button is disabled after domain switch");
		
		
		page.grid().scrollToAndSelect("Message Id", messageIDDomain);
		log.info("selected message from new domain");
		
		page.getDomainSelector().selectOptionByText(defaultDomainName);
		log.info("switch domain to default");
		
		log.info("check Download and Resend buttons status");
		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is disabled after domain switch (2)");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download message button is disabled after domain switch (2)");
		
		soft.assertAll();
	}
	
	 /* MSG-15 - Super admin logs in and views messages for a selected domain, navigates to second page of messages and changes domain */
	@Test(description = "MSG-15", groups = {"multiTenancy"} )
	public void verifyDomainSpecificMsgs() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		MessagesPage page = navigate();
		
		log.info("trying to go to page 2 if ti exists");
		Pagination pag = page.grid().getPagination();
		if(pag.hasNextPage()){
			log.info("going to page 2");
			pag.goToNextPage();
		}
		
		log.info("gathering listed ids on page 2");
		page.grid().waitForRowsToLoad();
		List<String> info_dom1 = page.grid().getListedValuesOnColumn("Message Id");
		
		
		log.info("changing domain");
		page.getDomainSelector().selectAnotherDomain();
		page.grid().waitForRowsToLoad();
		
		soft.assertEquals(page.grid().getPagination().getActivePage(), Integer.valueOf(1), "Pagination is set to first page");
		
		log.info("gathering listed info");
		List<String> info_dom2 = page.grid().getListedValuesOnColumn("Message Id");
		
		log.info("checking listed message id are different");
		for (String id : info_dom1) {
			soft.assertFalse(info_dom2.contains(id), "Message is found also in domain 2: " + id);
		}
		
		soft.assertAll();
	}
	
	/* MSG-16 - Download list of messages (multitenancy)*/
	@Test(description = "MSG-16", groups = {"multiTenancy"})
	public void downloadMsgs() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		MessagesPage page = navigate();
		String domain = selectRandomDomain();
		
		log.info("Click on download csv button");
		String completeFilePath = page.pressSaveCsvAndSaveFile();
		
		log.info("Click on show link");
		page.grid().getGridCtrl().showCtrls();
		
		log.info("Click on All link to show all available column headers");
		page.grid().getGridCtrl().showAllColumns();
		
		page.grid().checkCSVvsGridHeaders(completeFilePath, soft);
		int maxMess = page.grid().getRowsNo();
		
		page.grid().relaxCheckCSVvsGridInfo(completeFilePath, soft, "datetime"); //checkCSVvsGridInfo(completeFilePath, soft);
		soft.assertAll();
	}
	
	
}

