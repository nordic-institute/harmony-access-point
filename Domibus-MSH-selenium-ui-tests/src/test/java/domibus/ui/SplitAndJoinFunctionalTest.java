package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.PAGES;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplitAndJoin.SplitAndJoinPage;
import pages.errorLog.ErrorModal;
import pages.messages.MessagesPage;
import pages.pmode.current.PModeCurrentPage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SplitAndJoinFunctionalTest extends SeleniumTest {
	//This method will verify presence of splitting configuration and along with its activation
	
	@Test(description = "SJ-1", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void splittingConfPresence() throws Exception {
		rest.pmode().uploadPMode("pmodes/splitPmode.xml", null);
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser());
		DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);
		PModeCurrentPage PCpage = new PModeCurrentPage(driver);
		SplitAndJoinPage SJpage = new SplitAndJoinPage(driver);
		String path = "./src/main/resources/pmodes/splitPmode.xml";
		log.info("Extract  Splitting Configuration details from Pmode");
		String name = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "name", "splitting", path);
		String fragmentSize = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "fragmentSize", "splitting", path);
		String compression = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "compression", "splitting", path);
		String joinInterval = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "joinInterval", "splitting", path);
		SJpage.checkSplitAndJoinActivation("legConfigurations", "legConfiguration", soft, "splitting",
				"legConfiguration", "name", path);
		soft.assertTrue(name != null, "Splitting conf has name attribute present");
		soft.assertTrue(fragmentSize != null, "Fragment size attribute is present with some valid data");
		soft.assertTrue(compression != null, "Compression attribute is present");
		soft.assertTrue(joinInterval != null, "join Interval has some data");
		soft.assertAll();
		
	}
//This method will specifically send message from localhost by putting file in out folder.Verify fragment count and its presence on admin console
	
	@Test(description = "SJ-2", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkFragmentCount() throws Exception {
		SoftAssert soft = new SoftAssert();
		rest.pmode().uploadPMode("pmodes/splitPmode.xml", null);
		login(data.getAdminUser());
		DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage Mpage = new MessagesPage(driver);
		SplitAndJoinPage SJpage = new SplitAndJoinPage(driver);
		int beforeMsgCount = Mpage.grid().getPagination().getTotalItems();
		log.info("Message count available : " + Mpage.grid().getPagination().getTotalItems());
		log.info("Path of file to be copied in OUT folder");
		String path = "./src/main/resources/splitAndJoinDoc/largeFile.xml";
		File afile = new File(path);
		log.info("Path of Out folder");
		String filePath = "C:\\Work\\4.1.1_23sep_red\\domibus\\fs_plugin_data\\MAIN\\OUT\\";
		log.info("Copy file to Out folder");
		SJpage.copyFile(afile, filePath);
		log.info("Extract size of copied file");
		Double filesize = SJpage.getFileSize(afile);
		String pmodePath = "./src/main/resources/pmodes/splitPmode.xml";
		log.info("Finding fragmnet size data from uploaded pmode");
		String fragmentSize = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "fragmentSize", "splitting", pmodePath);
		Integer fragSize = Integer.valueOf(fragmentSize);
		log.info("fragmentSize in configuration is : " + fragSize + "MB");
		log.info("Calculating Fragment Count");
		int expectedFragCount = SJpage.getFragCount(filesize, fragSize);
		log.info("Fragment count is :" + expectedFragCount);
		log.info("Getting message count after message sending through Fs plugin");
		for (; ; ) {
			Thread.sleep(100);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			log.info("Compare before & after total message count");
			if (beforeMsgCount != Mpage.grid().getPagination().getTotalItems()) {
				log.info("Message count is different from earlier count");
				break;
			}
		}
		log.info("Getting all fragments and source message availability on Admin console at sender side");
		for (; ; ) {
			Thread.sleep(1000);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			Mpage.grid().waitForRowsToLoad();
			log.info("Calculate actual fragment count");
			int actualFragCount = Mpage.grid().getPagination().getTotalItems() - beforeMsgCount - 1;
			if (actualFragCount == expectedFragCount) {
				log.info("All fragments are shown on Admin console on Sender side");
				break;
			}
		}
		soft.assertTrue(beforeMsgCount < Mpage.grid().getPagination().getTotalItems(), "Current message count is greater");
		soft.assertAll();
	}
	
	//This method with verify presence of al fragments at sender side along with its status
	@Test(description = "SJ-3", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkFragmentPresenceAndStatus() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/splitPmode.xml", null);
		log.info("Login into application");
		login(data.getAdminUser());
		DomibusPage page = new DomibusPage(driver);
		log.info("Navigate to Messages page");
		page.getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage Mpage = new MessagesPage(driver);
		SplitAndJoinPage SJpage = new SplitAndJoinPage(driver);
		log.info("Get total number of message count available");
		int beforeMsgCount = Mpage.grid().getPagination().getTotalItems();
		log.info("Message count available  before : " + Mpage.grid().getPagination().getTotalItems());
		log.info("Path of file to put in out folder");
		String path = "./src/main/resources/splitAndJoinDoc/largeFile.xml";
		File afile = new File(path);
		log.info("Path of Out folder");
		String filePath = "C:\\Work\\4.1.1_23sep_red\\domibus\\fs_plugin_data\\MAIN\\OUT\\";
		// find size of file
		SJpage.copyFile(afile, filePath);
		log.info("Extract Size of file ");
		Double filesize = SJpage.getFileSize(afile);
		String pmodePath = "./src/main/resources/pmodes/splitPmode.xml";
		log.info("Extract fragment size mentioned in splitting configuration");
		String fragmentSize = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "fragmentSize", "splitting", pmodePath);
		Integer fragSize = Integer.valueOf(fragmentSize);
		log.info("fragmentSize in configuration is : " + fragSize + "MB");
		log.info("Calculate expected fragment count ");
		int expectedFragCount = SJpage.getFragCount(filesize, fragSize);
		log.info("Fragment count is :" + expectedFragCount);
		log.info("Getting  message count after message sending through Fs plugin");
		for (; ; ) {
			Thread.sleep(100);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			log.info("Compare before message count to current count");
			if (beforeMsgCount != Mpage.grid().getPagination().getTotalItems()) {
				log.info("Message count is different from earlier count");
				break;
			}
		}
		log.info("verify presence of  all fragments and source message availability on Admin console at sender side.");
		Thread.sleep(1000);
		Mpage.refreshPage();
		Mpage.waitForPageToLoad();
		Mpage.grid().waitForRowsToLoad();
		int addtionalMsg = Mpage.grid().getPagination().getTotalItems() - beforeMsgCount;
		if (addtionalMsg == expectedFragCount + 1) {
			soft.assertTrue(addtionalMsg == expectedFragCount + 1, "Additional msg count is equal to expected frag count+1");
			log.info("All fragments  and source message are shown on Admin console on Sender side");
			
		} else {
			log.info("All fragment and source message are not shown on admin console yet");
		}
		log.info("total message count after" + Mpage.grid().getPagination().getTotalItems());
		int totalCount = expectedFragCount + 1;
		
		for (int k = 0; k < totalCount; k++) {
			String msgId = "Message Id";
			String msgStatus = "Message Status";
			log.info("For row : " + k + "Message status :" + Mpage.grid().getRowSpecificColumnVal(k, msgStatus) + " " + "Message id :" + Mpage.grid().getRowSpecificColumnVal(k, msgId));
			
			
		}
		soft.assertAll();
	}
	
	//This method will validate Download button status of Fragment or Source message row selection
	@Test(description = "SJ-6", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkDownloadButtonStatus() throws Exception {
		SoftAssert soft = new SoftAssert();
		rest.pmode().uploadPMode("pmodes/splitPmode_NoRetry.xml", null);
		login(data.getAdminUser());
		DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage Mpage = new MessagesPage(driver);
		SplitAndJoinPage SJpage = new SplitAndJoinPage(driver);
		int beforeMsgCount = Mpage.grid().getPagination().getTotalItems();
		log.info("Message count available  before : " + Mpage.grid().getPagination().getTotalItems());
		log.info("Path of file to be put in out folder");
		String path = "./src/main/resources/splitAndJoinDoc/largeFile.xml";
		File afile = new File(path);
		log.info("Location of Out folder");
		String filePath = "C:\\Work\\4.1.1_23sep_red\\domibus\\fs_plugin_data\\MAIN\\OUT\\";
		log.info("Copying file to Ot folder");
		SJpage.copyFile(afile, filePath);
		log.info("finding size of file");
		Double filesize = SJpage.getFileSize(afile);
		String pmodePath = "./src/main/resources/pmodes/splitPmode.xml";
		log.info("extracting fragment size from splitting configuration of pmode");
		String fragmentSize = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "fragmentSize", "splitting", pmodePath);
		Integer fragSize = Integer.valueOf(fragmentSize);
		log.info("fragmentSize in configuration is : " + fragSize + "MB");
		log.info("Caluclate expected fragment count");
		int expectedFragCount = SJpage.getFragCount(filesize, fragSize);
		log.info("Fragment count is :" + expectedFragCount);
		log.info("verify  message count after message sending through Fs plugin");
		for (; ; ) {
			Thread.sleep(100);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			log.info("Compare before message count with current count");
			if (beforeMsgCount != Mpage.grid().getPagination().getTotalItems()) {
				log.info("Message count is different from earlier count");
				break;
			}
		}
		log.info(" getting all fragments and source message availability on Admin console at sender side");
		Thread.sleep(1000);
		Mpage.refreshPage();
		Mpage.waitForPageToLoad();
		Mpage.grid().waitForRowsToLoad();
		int addtionalMsg = Mpage.grid().getPagination().getTotalItems() - beforeMsgCount;
		if (addtionalMsg == expectedFragCount + 1) {
			log.info("All fragments  and source message are shown on Admin console on Sender side");
			
		} else {
			log.info("All fragment and source message are not shown on admin console yet");
		}
		log.info("total message count after" + Mpage.grid().getPagination().getTotalItems());
		int totalCount = expectedFragCount + 1;
		String msgStatus = "Message Status";
		for (int k = 0; k < totalCount; k++) {
			Mpage.grid().selectRow(k);
			log.info("Message status:" + Mpage.grid().getRowSpecificColumnVal(k, msgStatus));
			soft.assertFalse(Mpage.getDownloadButton().isEnabled(), "Button is not enabled");
		}
		log.info("Download button is disabled  for source & fragment message row of current group with above status");
		soft.assertAll();
	}
	
	@Test(description = "SJ-9", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkResendButtonStatus() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Upload pmode");
		rest.pmode().uploadPMode("pmodes/splitPmode_NoRetry.xml", null);
		login(data.getAdminUser());
		DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage Mpage = new MessagesPage(driver);
		SplitAndJoinPage SJpage = new SplitAndJoinPage(driver);
		log.info("Calculating total message count on admin console");
		int beforeMsgCount = Mpage.grid().getPagination().getTotalItems();
		log.info("Message count available  before : " + Mpage.grid().getPagination().getTotalItems());
		log.info("Path of file to put in out folder");
		String path = "./src/main/resources/splitAndJoinDoc/largeFile.xml";
		File afile = new File(path);
		log.info("Path location of Out folder");
		String filePath = "C:\\Work\\4.1.1_23sep_red\\domibus\\fs_plugin_data\\MAIN\\OUT\\";
		log.info("Copy file in OUT folder");
		SJpage.copyFile(afile, filePath);
		log.info("Getting file size");
		Double filesize = SJpage.getFileSize(afile);
		String pmodePath = "./src/main/resources/pmodes/splitPmode.xml";
		log.info("Extract value of fragment size from uploaded pmode");
		String fragmentSize = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "fragmentSize", "splitting", pmodePath);
		Integer fragSize = Integer.valueOf(fragmentSize);
		log.info("fragmentSize in configuration is : " + fragSize + "MB");
		log.info("Calculate expected fragment count");
		int expectedFragCount = SJpage.getFragCount(filesize, fragSize);
		log.info("Fragment count is :" + expectedFragCount);
		log.info("getting message count after message sending through Fs plugin");
		for (; ; ) {
			Thread.sleep(100);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			if (beforeMsgCount != Mpage.grid().getPagination().getTotalItems()) {
				log.info("Message count is different from earlier count");
				break;
			}
		}
		log.info("verify presence of all fragments and source message availability on Admin console at sender side");
		Thread.sleep(1000);
		Mpage.refreshPage();
		Mpage.waitForPageToLoad();
		Mpage.grid().waitForRowsToLoad();
		int addtionalMsg = Mpage.grid().getPagination().getTotalItems() - beforeMsgCount;
		if (addtionalMsg == expectedFragCount + 1) {
			log.info("All fragments  and source message are shown on Admin console on Sender side");
			
		} else {
			log.info("All fragment and source message are not shown on admin console yet");
		}
		log.info("total message count after" + Mpage.grid().getPagination().getTotalItems());
		int totalCount = expectedFragCount + 1;
		String msgStatus = "Message Status";
		for (int k = 0; k < totalCount; k++) {
			Mpage.grid().selectRow(k);
			log.info("Message status:" + Mpage.grid().getRowSpecificColumnVal(k, msgStatus) + "for row" + k);
			soft.assertFalse(Mpage.getResendButton().isEnabled(), "Button is not enabled");
		}
		log.info("Resend button is disabled  for source & fragment message row of current group with above status");
		soft.assertAll();
	}
	
	@Test(description = "SJ-8", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkActionIconStatus() throws Exception {
		SoftAssert soft = new SoftAssert();
		rest.pmode().uploadPMode("pmodes/splitPmode_NoRetry.xml", null);
		login(data.getAdminUser());
		DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage Mpage = new MessagesPage(driver);
		SplitAndJoinPage SJpage = new SplitAndJoinPage(driver);
		int beforeMsgCount = Mpage.grid().getPagination().getTotalItems();
		log.info("Message count available  before : " + Mpage.grid().getPagination().getTotalItems());
		log.info("Path of file to put in out folder");
		String path = "./src/main/resources/splitAndJoinDoc/largeFile.xml";
		File afile = new File(path);
		String filePath = "C:\\Work\\4.1.1_23sep_red\\domibus\\fs_plugin_data\\MAIN\\OUT\\";
		log.info("finding size of file");
		SJpage.copyFile(afile, filePath);
		Double filesize = SJpage.getFileSize(afile);
		String pmodePath = "./src/main/resources/pmodes/splitPmode.xml";
		String fragmentSize = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "fragmentSize", "splitting", pmodePath);
		Integer fragSize = Integer.valueOf(fragmentSize);
		log.info("fragmentSize in configuration is : " + fragSize + "MB");
		int expectedFragCount = SJpage.getFragCount(filesize, fragSize);
		log.info("Fragment count is :" + expectedFragCount);
		log.info("message count after message sending through Fs plugin");
		for (; ; ) {
			Thread.sleep(100);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			if (beforeMsgCount != Mpage.grid().getPagination().getTotalItems()) {
				log.info("Message count is different from earlier count");
				break;
			}
		}
		log.info("Verify presence of all fragments and source message availability on Admin console at sender side");
		Thread.sleep(1000);
		Mpage.refreshPage();
		Mpage.waitForPageToLoad();
		Mpage.grid().waitForRowsToLoad();
		int addtionalMsg = Mpage.grid().getPagination().getTotalItems() - beforeMsgCount;
		if (addtionalMsg == expectedFragCount + 1) {
			log.info("All fragments  and source message are shown on Admin console on Sender side");
			
		} else {
			log.info("All fragment and source message are not shown on admin console yet");
		}
		log.info("total message count after" + Mpage.grid().getPagination().getTotalItems());
		int totalCount = expectedFragCount + 1;
		String msgStatus = "Message Status";
		for (int k = 0; k < totalCount; k++) {
			Mpage.grid().selectRow(k);
			soft.assertFalse(Mpage.getActionIconStatus(k, "Download"), "Download icon is disabled");
			soft.assertFalse(Mpage.getActionIconStatus(k, "Resend"), "Resend icon is disabled");
		}
		log.info("Download and resend icon is disabled for fragment/source message ");
		soft.assertAll();
	}
	
	//This method will verify fragment count when file size is smaller than fragment size in splitting configuration
	@Test(description = "SJ-12", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkfragForSmallSize() throws Exception {
		SoftAssert soft = new SoftAssert();
		rest.pmode().uploadPMode("pmodes/splitPmode.xml", null);
		login(data.getAdminUser());
		DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage Mpage = new MessagesPage(driver);
		SplitAndJoinPage SJpage = new SplitAndJoinPage(driver);
		int beforeMsgCount = Mpage.grid().getPagination().getTotalItems();
		log.info("Message count available : " + Mpage.grid().getPagination().getTotalItems());
		String path = "./src/main/resources/splitAndJoinDoc/smallFile.xml";
		File afile = new File(path);
		String filePath = "C:\\Work\\4.1.1_23sep_red\\domibus\\fs_plugin_data\\MAIN\\OUT\\";
		log.info("Copy file to Out folder");
		SJpage.copyFile(afile, filePath);
		log.info("Calculate file size");
		Double filesize = SJpage.getFileSize(afile);
		String pmodePath = "./src/main/resources/pmodes/splitPmode.xml";
		log.info("Extract value of fragment size from splitting configuration of Pmode");
		String fragmentSize = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "fragmentSize", "splitting", pmodePath);
		Integer fragSize = Integer.valueOf(fragmentSize);
		log.info("fragmentSize in configuration is : " + fragSize + "MB");
		int expectedFragCount = SJpage.getFragCount(filesize, fragSize);
		log.info("Fragment count is :" + expectedFragCount);
		log.info("getting message count after message sending through Fs plugin");
		for (; ; ) {
			Thread.sleep(100);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			if (beforeMsgCount != Mpage.grid().getPagination().getTotalItems()) {
				break;
			}
		}
		log.info("Verify presence of all fragments and source message availability on Admin console at sender side");
		for (; ; ) {
			Thread.sleep(1000);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			Mpage.grid().waitForRowsToLoad();
			int actualFragCount = Mpage.grid().getPagination().getTotalItems() - beforeMsgCount - 1;
			log.info("Actual Fragment count in case of file size smaller than configured fragment size :" + actualFragCount);
			if (actualFragCount == expectedFragCount) {
				soft.assertTrue(actualFragCount == expectedFragCount, "Actual fragment count is equal to expected count");
				log.info("All fragments are shown on Admin console on Sender side");
				break;
			}
		}
		soft.assertAll();
	}
	
	//This method will validate equality of conversation ids for fragments and source message at sender side
	@Test(description = "SJ-16", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkConservationId() throws Exception {
		SoftAssert soft = new SoftAssert();
		rest.pmode().uploadPMode("pmodes/splitPmode.xml", null);
		login(data.getAdminUser());
		DomibusPage page = new DomibusPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		MessagesPage Mpage = new MessagesPage(driver);
		SplitAndJoinPage SJpage = new SplitAndJoinPage(driver);
		log.info("Caluclate before message count");
		int beforeMsgCount = Mpage.grid().getPagination().getTotalItems();
		log.info("Message count available : " + Mpage.grid().getPagination().getTotalItems());
		String path = "./src/main/resources/splitAndJoinDoc/largeFile.xml";
		File afile = new File(path);
		log.info("Path of Out folder");
		String filePath = "C:\\Work\\4.1.1_23sep_red\\domibus\\fs_plugin_data\\MAIN\\OUT\\";
		log.info("copy file to out folder");
		SJpage.copyFile(afile, filePath);
		log.info("Calculate file size");
		Double filesize = SJpage.getFileSize(afile);
		String pmodePath = "./src/main/resources/pmodes/splitPmode.xml";
		log.info("Extract value of fragment size from uploaded pmode");
		String fragmentSize = SJpage.getSplittingConf("splittingConfigurations", "splitting", soft, "fragmentSize", "splitting", pmodePath);
		Integer fragSize = Integer.valueOf(fragmentSize);
		log.info("fragmentSize in configuration is : " + fragSize + "MB");
		log.info("Calculate expected no of fragment count");
		int expectedFragCount = SJpage.getFragCount(filesize, fragSize);
		log.info("Fragment count is :" + expectedFragCount);
		log.info("Verify message count after message sending through Fs plugin");
		for (; ; ) {
			Thread.sleep(100);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			if (beforeMsgCount != Mpage.grid().getPagination().getTotalItems()) {
				break;
			}
		}
		log.info("Verify presence of all fragments and source message availability on Admin console at sender side");
		for (; ; ) {
			Thread.sleep(1000);
			Mpage.refreshPage();
			Mpage.waitForPageToLoad();
			Mpage.grid().waitForRowsToLoad();
			int actualFragCount = Mpage.grid().getPagination().getTotalItems() - beforeMsgCount - 1;
			if (actualFragCount == expectedFragCount) {
				log.info("All fragments are shown on Admin console on Sender side");
				break;
			}
			
		}
		
		int totalCount = expectedFragCount + 1;
		List<String> convIds = new ArrayList<String>();
		
		for (int k = 0; k < totalCount; k++) {
			Mpage.grid().getGridCtrl().showAllColumns();
			HashMap<String, String> info = Mpage.grid().getRowInfo(k);
			Mpage.grid().doubleClickRow(k);
			HashMap<String, String> modalInfo = new ErrorModal(driver).getListedInfo();
			page.clickVoidSpace();
			
			if (modalInfo.keySet().contains("Conversation Id")) {
				convIds.add(modalInfo.get("Conversation Id"));
			}
		}
		for (int k = 0; k < totalCount; k++) {
			for (int i = k + 1; i < totalCount; i++) {
				soft.assertTrue(convIds.get(k).equals(convIds.get(i)), "All conversation ids are same");
				
			}
			soft.assertAll();
		}
	}
	
}




