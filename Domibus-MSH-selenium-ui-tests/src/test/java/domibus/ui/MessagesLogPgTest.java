package domibus.ui;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DMessages;
import ddsl.enums.DOMIBUS_PAGES;
import ddsl.enums.DRoles;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.w3c.dom.Document;
import pages.messages.MessageDetailsModal;
import pages.messages.MessageResendModal;
import pages.messages.MessagesPage;
import pages.messages.SearchFilters;
import utils.Generator;
import utils.soap_client.MyMessageConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MessagesLogPgTest extends BaseTest {

	private HashMap<String, String> createdPluginUsers = new HashMap<>();

	@Test(description = "MSG-1", groups = {"multiTenancy", "singleTenancy"})
	public void openMessagesPage() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		soft.assertTrue(page.isLoaded(), "Page elements are loaded");
		soft.assertTrue(page.getFilters().basicFiltersLoaded(), "Basic filters are present");
		soft.assertTrue(!page.getFilters().advancedFiltersLoaded(), "Advanced filters are NOT present");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download button is not enabled");
		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is not enabled");

		soft.assertAll();
	}

	@Test(description = "MSG-2", groups = {"multiTenancy", "singleTenancy"})
	public void messageRowSelect() throws Exception{
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmodes/pmode-blue.xml", null);
		String messID = messageSender.sendMessage(user, data.getDefaultTestPass(),null, null).getMessageID().get(0);

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

//		page.wait.forXMillis(5000);

		page.refreshPage();
		page.grid().scrollToAndSelect("Message Id", messID);

		soft.assertTrue(page.getDownloadButton().isEnabled(), "After a row is selected the Download button");

		rest.deletePluginUser(user, null);
		soft.assertAll();
	}

	@Test(description = "MSG-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickMessageRow() throws Exception{
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmodes/pmode-blue.xml", null);
		String messID = messageSender.sendMessage(user, data.getDefaultTestPass(), null, null).getMessageID().get(0);

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		DGrid grid = page.grid();
		int index = grid.scrollTo("Message Id", messID);

		HashMap<String, String> info = grid.getRowInfo(index);
		grid.doubleClickRow(index);

		MessageDetailsModal modal = new MessageDetailsModal(driver);

		for (String s : info.keySet()) {
			if(s.contains("Action")){ continue;}
			soft.assertEquals(modal.getValue(s), info.get(s), "Checking info in grid vs modal " + s );
		}
		rest.deletePluginUser(user, null);
		soft.assertAll();
	}

	@Test(description = "MSG-4", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessages() throws Exception{
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmodes/pmode-blue.xml", null);
		List<String> messageIDs = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			messageIDs.add( messageSender.sendMessage(user, data.getDefaultTestPass(), null, null).getMessageID().get(0));
		}

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		DGrid grid = page.grid();

		List<HashMap<String, String>> allRowInfo = grid.getAllRowInfo();

		page.getFilters().getMessageIDInput().fill(messageIDs.get(0));
		page.getFilters().getSearchButton().click();

		List<HashMap<String, String>> filteredRowInfo = grid.getAllRowInfo();

		List<HashMap<String, String>> expectedResult = allRowInfo.stream().filter(rowInfo -> rowInfo.get("Message Id").equals(messageIDs.get(0))).collect(Collectors.toList());

		soft.assertTrue(filteredRowInfo.size() == expectedResult.size(), "No of listed items in page matches expected");


		rest.deletePluginUser(user, null);
		soft.assertAll();
	}

	@Test(description = "MSG-5", groups = {"multiTenancy", "singleTenancy"})
	public void openAdvancedMessageFilters() throws Exception{
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		page.getFilters().getAdvancedSearchExpandLnk().click();

		soft.assertTrue(page.getFilters().advancedFiltersLoaded(), "Advanced filters ");

		soft.assertAll();
	}


	@Test(description = "MSG-6", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesAdvancedFilters() throws Exception{
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		String messageRefID = Generator.randomAlphaNumeric(10);
		String conversationID = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmodes/pmode-blue.xml", null);
		List<String> messageIDs = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			messageIDs.add( messageSender.sendMessage(user, data.getDefaultTestPass(), messageRefID, conversationID).getMessageID().get(0));
		}

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		DGrid grid = page.grid();

		SearchFilters filters = page.getFilters();

		filters.getAdvancedSearchExpandLnk().click();
		filters.getFromPartyInput().fill(MyMessageConstants.From_Party_Id);
		filters.getToPartyInput().fill(MyMessageConstants.To_Party_Id);
		filters.getFinalRecipientInput().fill(MyMessageConstants.Final_Recipient);
		filters.getOriginalSenderInput().fill(MyMessageConstants.Original_Sender);
		filters.getConversationIDInput().fill(conversationID);
		filters.getReferenceMessageIDInput().fill(messageRefID);
		filters.getApRoleSelect().selectOptionByText(MyMessageConstants.AP_Role);

		filters.getSearchButton().click();

		for (int i = 0; i < page.grid().getRowsNo(); i++) {
			page.grid().doubleClickRow(i);
			MessageDetailsModal modal = new MessageDetailsModal(driver);
			String messID = modal.getValue("Message ID");
			soft.assertEquals(modal.getValue("Conversation Id"), conversationID, messID + " - check conversation id");
			soft.assertEquals(modal.getValue("Ref To Message Id"), messageRefID, messID + " - check Ref To Message Id");
			soft.assertEquals(modal.getValue("From Party Id"), MyMessageConstants.From_Party_Id, messID + " - check From Party Id");
			soft.assertEquals(modal.getValue("To Party Id"), MyMessageConstants.To_Party_Id, messID + " - check To Party Id");
			soft.assertEquals(modal.getValue("Original Sender"), MyMessageConstants.Original_Sender, messID + " - check Original Sender");
			soft.assertEquals(modal.getValue("Final Recipient"), MyMessageConstants.Final_Recipient, messID + " - check Final Recipient");
			soft.assertEquals(modal.getValue("AP Role"), MyMessageConstants.AP_Role, messID + " - check AP Role");

			page.clickVoidSpace();
		}


		rest.deletePluginUser(user, null);
		soft.assertAll();
	}


	@Test(description = "MSG-7", groups = {"multiTenancy", "singleTenancy"})
	public void filterEmptyGrid() throws Exception{
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		int gridRows = page.grid().getRowsNo();
		int allRows = page.grid().getPagination().getTotalItems();

		page.getFilters().getMessageIDInput().fill("testEmptyGrid");
		page.getFilters().getSearchButton().click();

		page.wait.forXMillis(300);

		soft.assertEquals(page.grid().getRowsNo(), 0, "The grid is empty after search with 0 matching messages");

//		--------------------------------------------------
//		TODO: when there is time replace this with an empty search
		page.refreshPage();

//		--------------------------------------------------

		soft.assertEquals(page.grid().getRowsNo(), gridRows, "Empty search resets grid to original state (2)");
		soft.assertEquals(page.grid().getPagination().getTotalItems(), allRows, "Empty search resets grid to original state (2)");

		soft.assertAll();
	}

	@Test(description = "MSG-8", groups = {"multiTenancy", "singleTenancy"})
	public void downloadMessage() throws Exception{
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		String messageRefID = Generator.randomAlphaNumeric(10);
		String conversationID = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		String messageID = messageSender.sendMessage(user, data.getDefaultTestPass(), messageRefID, conversationID).getMessageID().get(0);

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		page.grid().scrollToAndDoubleClick("Message Id", messageID);

		String zipPath = rest.downloadMessage(messageID, null);
		HashMap<String, String> zipContent = unzip(zipPath);

		boolean foundXMLfile = false;
		boolean foundMessfile = false;
		for (String fileName : zipContent.keySet()) {
			if(fileName.equalsIgnoreCase("message")){ foundMessfile= true;}
			if(fileName.equalsIgnoreCase("message.xml")){ foundXMLfile = true;	}
		}

		soft.assertTrue(foundMessfile, "Found file containing message content");
		soft.assertTrue(foundXMLfile, "Found file containing message properties");

		soft.assertEquals(zipContent.get("message"), MyMessageConstants.Message_Content, "Correct message content is downloaded");

		String xmlString = zipContent.get("message.xml");
		MessageDetailsModal modal = new MessageDetailsModal(driver);
		soft.assertEquals(modal.getValue("Message Id"),
				getValueFromXMLString(xmlString, "MessageId"), "MessageId - value matches");
		soft.assertEquals(modal.getValue("Conversation Id"),
				getValueFromXMLString(xmlString, "ConversationId"), "ConversationId - value matches");
		soft.assertEquals(modal.getValue("Ref To Message Id"),
				getValueFromXMLString(xmlString, "RefToMessageId"), "RefToMessageId - value matches");

		soft.assertTrue(xmlString.contains("name=\"originalSender\">"+modal.getValue("Original Sender"))
				, "Original Sender - value matches");

		soft.assertTrue(xmlString.contains("name=\"finalRecipient\">" + modal.getValue("Final Recipient"))
				, "Final Recipient - value matches");


		rest.deletePluginUser(user, null);
		soft.assertAll();
	}

	@Test(description = "MSG-9", groups = {"multiTenancy", "singleTenancy"})
	public void resendMessage() throws Exception{
		SoftAssert soft = new SoftAssert();
		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		String messageID =  messageSender.sendMessage(user, data.getDefaultTestPass(), null, null).getMessageID().get(0);

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		page.grid().scrollToAndSelect("Message Id", messageID);
		page.getResendButton().click();

		MessageResendModal modal = new MessageResendModal(driver);
		modal.getResendButton().click();

		boolean statusChanged = false;
		int c=0;
		while(c<20) {
			page.wait.forXMillis(100);
//			page.refreshPage();
			HashMap<String, String> info = page.grid().getRowInfo("Message Id", messageID);
			if(info.get("Message Status").equalsIgnoreCase("SEND_ENQUEUED")
			|| info.get("Message Status").equalsIgnoreCase("WAITING_FOR_RETRY")){
				statusChanged = true;
				break;
			}
			c++;
		}
		soft.assertTrue(statusChanged, "Message changed to SEND_ENQUEUED");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.RESEND_MESSAGE_SUCCESS, "Page shows corect success message");
		soft.assertTrue(!page.getAlertArea().isError(), "Page shows success message");


		soft.assertAll();
	}

	@Test(description = "MSG-10", groups = {"multiTenancy"})
	public void messagesSegregatedByDomain() throws Exception{
		SoftAssert soft = new SoftAssert();

		String domainName = rest.getDomainNames().get(1);
		String domain = rest.getDomainCodeForName(domainName);


		String userDomain = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(userDomain, DRoles.ADMIN, data.getDefaultTestPass(),domain);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", domain);
		String messageIDDomain =  messageSender.sendMessage(userDomain, data.getDefaultTestPass(), null, null).getMessageID().get(0);

		String userDefault = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(userDefault, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		String messageIDDefault =  messageSender.sendMessage(userDefault, data.getDefaultTestPass(), null, null).getMessageID().get(0);

		String userAdmin = Generator.randomAlphaNumeric(10);
		rest.createUser(userAdmin, DRoles.ADMIN, data.getDefaultTestPass(), domain);

		login(userAdmin, data.getDefaultTestPass()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain)>=0, "Domain admin sees the domain message (1)");
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault)<0, "Domain admin does NOT see the default domain message (2)");

		page.getSandwichMenu().logout();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);

		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain)<0, "Super admin does NOT see the domain message while on the default domain (3)" );
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault)>=0, "Super admin sees the default domain message when on default domain (4)");

		page.getDomainSelector().selectOptionByText(domainName);

		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDomain)>=0, "Super admin sees the domain message while on the proper domain (5)" );
		soft.assertTrue(page.grid().scrollTo("Message Id", messageIDDefault)<0, "Super admin doesn't see the default domain message when on domain (6)");

		rest.deletePluginUser(userDefault, null);
		rest.deletePluginUser(userDomain, domain);
		rest.deletePluginUser(userAdmin, domain);

		soft.assertAll();
	}


	@Test(description = "MSG-11", groups = {"multiTenancy"})
	public void superSelectMessageChangeDomain() throws Exception{
		SoftAssert soft = new SoftAssert();

		String domainName = rest.getDomainNames().get(1);
		String domain = rest.getDomainCodeForName(domainName);


		String userDomain = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(userDomain, DRoles.ADMIN, data.getDefaultTestPass(),domain);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", domain);
		String messageIDDomain =  messageSender.sendMessage(userDomain, data.getDefaultTestPass(), null, null).getMessageID().get(0);

		String userDefault = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(userDefault, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
		String messageIDDefault =  messageSender.sendMessage(userDefault, data.getDefaultTestPass(), null, null).getMessageID().get(0);

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);

		MessagesPage page = new MessagesPage(driver);
		page.grid().scrollToAndSelect("Message Id", messageIDDefault);

		page.getDomainSelector().selectOptionByText(domainName);

		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is disabled after domain switch");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download message button is disabled after domain switch");

		page.grid().scrollToAndSelect("Message Id", messageIDDomain);
		page.getDomainSelector().selectOptionByText("Default");

		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is disabled after domain switch (2)");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download message button is disabled after domain switch (2)");


		rest.deletePluginUser(userDefault, null);
		rest.deletePluginUser(userDomain, domain);

		soft.assertAll();
	}





	private HashMap<String, String> unzip(String zipFilePath) throws Exception {

		String destDir = zipFilePath.replaceAll(".zip", "");

		HashMap<String, String> zipContent = new HashMap<String, String>();

		File dir = new File(destDir);
		// create output directory if it doesn't exist
		if(!dir.exists()) dir.mkdirs();


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

	private String getValueFromXMLString(String xmlString, String key){
		String start = key + ">";
		String end = "<\\/eb:" + key ;

		Pattern p = Pattern.compile(start+"(.*?)"+end);
		Matcher m = p.matcher(xmlString);
		m.find();
		return m.group(1);
	}

}

