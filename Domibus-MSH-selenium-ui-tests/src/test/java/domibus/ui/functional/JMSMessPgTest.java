package domibus.ui.functional;

import ddsl.dcomponents.grid.Pagination;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.jms.JMSMonitoringPage;
import pages.jms.JMSMoveMessageModal;
import utils.Gen;
import utils.TestRunData;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class JMSMessPgTest extends SeleniumTest {
	
	/* JMS-7 - Delete message */
	@Test(description = "JMS-7", groups = {"multiTenancy", "singleTenancy"})
	public void deleteJMSMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			log.info("Navigate to JMS Messages page");
			
			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);
			
			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();
			
			log.info("deleting first message listed");
			
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
			page.grid().selectRow(0);
			page.getDeleteButton().click();
			log.info("cancel delete");
			page.getCancelButton().click();
			new Dialog(driver).confirm();
			page.wait.forXMillis(500);
			
			soft.assertTrue(page.grid().scrollTo("ID", rowInfo.get("ID")) >= 0, "Message still present in the grid after user cancels delete operation");
			
			log.info("deleting first message listed");
			HashMap<String, String> rowInfo2 = page.grid().getRowInfo(0);
			page.grid().selectRow(0);
			log.info("click delete");
			page.getDeleteButton().click();
			log.info("saving ");
			page.getSaveButton().click();
			new Dialog(driver).confirm();
			
			soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");
			
			page.grid().waitForRowsToLoad();
			log.info("check message is deleted from grid");
			soft.assertTrue(page.grid().scrollTo("ID", rowInfo2.get("ID")) < 0, "Message NOT present in the grid after delete operation");
			
		}
		
		soft.assertAll();
	}
	
	//	Disabled because functionality change and it needs to be updated
	/*JMS-8 - Move message*/
	@Test(description = "JMS-8", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void moveMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			log.info("Navigate to JMS Messages page");
			
			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);
			page.grid().waitForRowsToLoad();
			int noOfMessInDQL = page.grid().getPagination().getTotalItems();
			
			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();
			
			
			String queuename = page.filters().getJmsQueueSelect().getSelectedValue();
			
			
			log.info("moving the first message");
			page.grid().selectRow(0);
			page.getMoveButton().click();
			
			log.info("canceling");
			JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
			modal.getQueueSelect().selectDLQQueue();
			modal.clickCancel();
			
			log.info("checking the number of messages");
			soft.assertEquals(noOfMessages, page.grid().getPagination().getTotalItems(), "Number of messages in current queue is not changed");
			
			page.filters().getJmsQueueSelect().selectDLQQueue();
			page.grid().waitForRowsToLoad();
			
			log.info("getting no of messages in DLQ queue");
			soft.assertEquals(noOfMessInDQL, page.grid().getPagination().getTotalItems(), "Number of messages in DLQ message queue is not changed");
			
			log.info("selecting queue " + queuename);
			page.filters().getJmsQueueSelect().selectOptionByText(queuename);
			page.grid().waitForRowsToLoad();
			
			log.info("getting info on row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
			page.grid().selectRow(0);
			log.info("moving message on row 0 to DLQ queue");
			page.getMoveButton().click();
			
			modal.getQueueSelect().selectDLQQueue();
			modal.clickOK();
			
			page.grid().waitForRowsToLoad();
			
			log.info("checking success message");
			soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");
			soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.JMS_MOVE_MESSAGE_SUCCESS, "Correct message is shown");
			
			log.info("checking number of listed messages for this queue");
			soft.assertTrue(page.grid().getPagination().getTotalItems() == noOfMessages - 1, "Queue has one less message");
			
			log.info("selecting DLQ queue");
			page.filters().getJmsQueueSelect().selectDLQQueue();
			page.grid().waitForRowsToLoad();
			
			log.info("checking no of messages in DLQ queue");
			soft.assertEquals(noOfMessInDQL + 1, page.grid().getPagination().getTotalItems(), "DQL queue has one more message after the move");
			
			int index = page.grid().scrollTo("ID", rowInfo.get("ID"));
			log.info("checking the moved message is present in the grid");
			soft.assertTrue(index > -1, "DQL queue contains the new message");
		}
		
		soft.assertAll();
	}
	
	/*JMS-9 - Domain admin logs in and views messages*/
	@Test(description = "JMS-9", groups = {"multiTenancy"})
	public void adminOpenJMSMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		String domainName = rest.getNonDefaultDomain();
		String domainCode = rest.getDomainCodeForName(domainName);
		log.info("checking for domain " + domainCode);
		JSONObject user = rest.getUser(domainCode, DRoles.ADMIN, true, false, false);
		
		login(user.getString("userName"), data.defaultPass());
		log.info("logging in with admin " + user.getString("userName"));
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		
		log.info("checking domain name in the title");
		soft.assertEquals(page.getDomainFromTitle(), domainName, "Page title shows correct domain");
		soft.assertTrue(page.filters().isLoaded(), "Filters are loaded and visible");
		
		List<String> sources = page.filters().getJmsQueueSelect().getOptionsTexts();
		log.info("checking message numbers are missing from queue source names");
		for (String source : sources) {
			soft.assertTrue(!source.matches("\\(\\d\\)"), "Message numbers are not shown when admin is logged in");
		}
		
		List<HashMap<String, String>> allInfo = page.grid().getAllRowInfo();
		log.info("checking messages contain domain name in Custom prop field");
		for (HashMap<String, String> info : allInfo) {
			soft.assertTrue(info.get("Custom prop").contains(domainCode));
		}
		
		soft.assertAll();
	}
	
	private String getQWithMessages() throws Exception {

//		destination = queues.getJSONObject(i).getString("name");
		
		String source = null;
		
		JSONArray queues = rest.jms().getQueues();
		for (int i = 0; i < queues.length(); i++) {
			if (queues.getJSONObject(i).getString("name").contains("DLQ")) {
				continue;
			} else if (queues.getJSONObject(i).getInt("numberOfMessages") > 0) {
				source = queues.getJSONObject(i).getString("name");
			}
		}
		
		if (null == source) {
			throw new SkipException("No messages found to move");
		}
		
		return source;
	}
	
	private String getDLQName() throws Exception {
		
		String destination = null;
		
		JSONArray queues = rest.jms().getQueues();
		for (int i = 0; i < queues.length(); i++) {
			if (queues.getJSONObject(i).getString("name").contains("DLQ")) {
				destination = queues.getJSONObject(i).getString("name");
			}
		}
		
		if (null == destination) {
			throw new SkipException("Could not find DLQ Q");
		}
		return destination;
	}
	
	/* This method will verify scenario of changing domain after row selection*/
	@Test(description = "JMS-10", groups = {"multiTenancy"})
	public void changeDomainAfterSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String q = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(q)) {
			throw new SkipException("no queue has messages");
		}
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		log.info("Login into application and navigate to JMS Monitoring page");
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		page.grid().waitForRowsToLoad();
		
		log.info("Choose domain name from page title");
		String domain = selectRandomDomain();
		
		page.filters().getJmsQueueSelect().selectQueueByName(q);
		
		log.info("select any message queue having some messages");
		log.info("wait for grid row to load");
		page.grid().waitForRowsToLoad();
		
		log.info("select first row");
		page.grid().selectRow(0);
		
		log.info("Confirm status of Move button and Delete button");
		soft.assertTrue(page.moveButton.isEnabled(), "Move button is enabled on row selection");
		soft.assertTrue(page.deleteButton.isEnabled(), "Delete button is enabled on row selection");
		
		log.info("select other domain from domain selector");
		String otherDomain = page.getDomainSelector().selectAnotherDomain();
		
		log.info("Wait for page title");
		page.waitForPageTitle();
		
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();
		
		log.info("Compare old and new domain name");
		soft.assertTrue(page.getDomainFromTitle().equals(otherDomain), "Current domain differs from old domain");
		
		log.info("Check status of move button and delete button");
		soft.assertFalse(page.moveButton.isEnabled(), "Move button is not enabled");
		soft.assertFalse(page.deleteButton.isEnabled(), "Delete button is not enabled");
		
		
		soft.assertAll();
	}
	
	/* This method will verify scenario of changing domain from second page of default domain*/
	@Test(description = "JMS-11", groups = {"multiTenancy"})
	public void changeDomainFromSecondPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String q = rest.jms().getRandomQNameWithMessages();
		log.debug("found queue: {}", q);
		if (StringUtils.isEmpty(q)) {
			throw new SkipException("no queue has messages");
		}
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		log.info("Login into application and navigate to JMS Monitoring page");
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		page.grid().waitForRowsToLoad();
		
		String domain = page.getDomainFromTitle();
		
//		page.filters().getJmsQueueSelect().selectOptionByText(q);
		page.filters().getJmsQueueSelect().selectQueueByName(q);
		
		log.info("select any message queue having some messages");
		log.info("wait for grid row to load");
		page.grid().waitForRowsToLoad();
		
		log.info("going to next page");
		Pagination p = page.grid().getPagination();
		if (!p.hasNextPage()) {
			throw new SkipException("not enough messages to have another page");
		}
		p.goToNextPage();
		
		
		log.info("select other domain from domain selector");
		String otherDomain = page.getDomainSelector().selectAnotherDomain();
		
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();
		
		soft.assertTrue(p.getActivePage() == 1, "Pagination reset to first page");
		
		soft.assertAll();
	}
	
	
	/* This method will verify scenario of jms message deletion on domain change*/
	@Test(description = "JMS-12", groups = {"multiTenancy"})
	public void jmsMsgDelOnDomainChange() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String q = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(q)) {
			throw new SkipException("no queue has messages");
		}
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		log.info("Login into application and navigate to JMS Monitoring page");
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		
		String domain = page.getDomainFromTitle();
		page.grid().waitForRowsToLoad();
		page.filters().getJmsQueueSelect().selectQueueByName(q);
		
		log.info("select any message queue having some messages");
		log.info("wait for grid row to load");
		page.grid().waitForRowsToLoad();
		page.grid().selectRow(0);
		
		log.info("Verify status of Move button and Delete button");
		soft.assertTrue(page.getMoveButton().isEnabled(), "Move button is enabled on selection");
		soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled on selection");
		
		String otherDomain = page.getDomainSelector().selectAnotherDomain();
		
		log.info("Check message count in queue");
		page.grid().waitForRowsToLoad();
		
		page.filters().getJmsQueueSelect().selectQueueByName(q);
		
		log.info("After domain chenge select any message queue having some messages");
		log.info("wait for grid row to load");
		page.grid().waitForRowsToLoad();
		
		int totalCount = page.grid().getPagination().getTotalItems();
		log.info("Current message count is " + totalCount);
		
		log.info("Select first row");
		page.grid().selectRow(0);
		
		log.info("Click on delete button");
		page.getDeleteButton().click();
		
		log.info("Click on save button");
		page.getSaveButton().click();
		new Dialog(driver).confirm();
		
		log.info("Check presence of success message on deletion");
		soft.assertTrue(page.getAlertArea().getAlertMessage().contains("success"), "Success message is shown on deletion");
		
		log.info("Verify queue message count as 1 less than before");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == totalCount - 1, "Queue message count is 1 less");
		
		soft.assertAll();
	}
	
	/* This method will verify data of Received Upto field*/
	@Test(description = "JMS-21", groups = {"multiTenancy", "singleTenancy"})
	public void checkReceivedUpTo() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String domain = selectRandomDomain();
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		page.grid().waitForRowsToLoad();
		
		log.info("getting expectyed date");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		Date date = cal.getTime();
		String expectedDateStr = TestRunData.DATEWIDGET_DATE_FORMAT.format(date).trim();
		log.debug("expected date = " + expectedDateStr);
		
		String pageDateStr = page.filters().getJmsToDatePicker().getSelectedDate();
		log.info("Got date str from page: " + pageDateStr);
		
		log.info("checking dates");
		soft.assertEquals(pageDateStr, expectedDateStr, "Date string is as expected");
		
		soft.assertAll();
		
	}
	
	
	/* This method will verify jms message queue count on Input filter*/
	@Test(description = "JMS-23", groups = {"multiTenancy", "singleTenancy"})
	public void queueMsgCountOnInputFilter() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("navigate to JMS Monitoring page");
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		
		List<String> queues = page.filters().getJmsQueueSelect().getOptionsTexts();
		verifyQueueHasMessageCount(queues, true, soft);
		
		if(data.isMultiDomain()){
			
			String domain = selectRandomDomain();
			
			String username = Gen.randomAlphaNumeric(10);
			rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), domain);
			log.info("login with admin and navigate to JMS Monitoring page");
			login(username, data.defaultPass()).getSidebar().goToPage(PAGES.JMS_MONITORING);
			page.grid().waitForRowsToLoad();
			
			queues = page.filters().getJmsQueueSelect().getOptionsTexts();
			verifyQueueHasMessageCount(queues, false, soft);
		}
		
		soft.assertAll();
	}
	
	/* This method will verify jms message count in queue on Move pop up*/
	@Test(description = "JMS-24", groups = {"multiTenancy", "singleTenancy"})
	public void queueMsgCountOnMovePopUp() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String q = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(q)) {
			throw new SkipException("no queue has messages");
		}
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		log.info("Login into application and navigate to JMS Monitoring page");
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		
		log.info("selecting queue with name " + q);
		page.grid().waitForRowsToLoad();
		page.filters().getJmsQueueSelect().selectQueueByName(q);
		
		log.info("Selecting row 0");
		page.grid().selectRow(0);
		
		log.info("push move button");
		page.getMoveButton().click();
		
		JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
		List<String> queues = modal.getQueueSelect().getOptionsTexts();
		
		verifyQueueHasMessageCount(queues, true, soft);
		
		if(data.isMultiDomain()){
			log.info("Create Admin user for default domain");
			String user = Gen.randomAlphaNumeric(10);
			rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), null);
			
			log.info("Login into application with domain admin and navigate to JMS Monitoring page");
			login(user, data.defaultPass());
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);
			
			log.info("selecting queue with name " + q);
			page.grid().waitForRowsToLoad();
			page.filters().getJmsQueueSelect().selectQueueByName(q);
			
			log.info("Selecting row 0");
			page.grid().selectRow(0);
			
			log.info("push move button");
			page.getMoveButton().click();
			
			modal = new JMSMoveMessageModal(driver);
			queues = modal.getQueueSelect().getOptionsTexts();
			
			verifyQueueHasMessageCount(queues, false, soft);
		}
		soft.assertAll();
	}
	
	/* This method will verify jms message count on queue in case of super admin*/
	@Test(description = "JMS-25", groups = {"multiTenancy"} )
	public void queueMsgCountForSuperAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();
	
		selectRandomDomain();
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		log.info("Login into application and navigate to Jms Monitoring Page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		
		
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();
		
		
		List<String> queues = page.filters().getJmsQueueSelect().getOptionsTexts();
		verifyQueueHasMessageCount(queues, true, soft);
		
		soft.assertAll();
	}

	/* This method will verify message count on move pop up in case of super admin */
	@Test(description = "JMS-26", groups = {"multiTenancy"} )
	public void msgCountOnMoveForSuperAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String q = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(q)) {
			throw new SkipException("no queue has messages");
		}
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		log.info("Login into application and navigate to JMS Monitoring page");
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		
		log.info("selecting queue with name " + q);
		page.grid().waitForRowsToLoad();
		page.filters().getJmsQueueSelect().selectQueueByName(q);
		page.grid().waitForRowsToLoad();
		
		log.info("Selecting row 0");
		page.grid().selectRow(0);
		
		log.info("push move button");
		page.getMoveButton().click();
		
		JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
		List<String> queues = modal.getQueueSelect().getOptionsTexts();
		
		verifyQueueHasMessageCount(queues, true, soft);
		
		soft.assertAll();
	}
	
	private void verifyQueueHasMessageCount(List<String> queues, Boolean expectCount, SoftAssert soft){
		log.info("Checking message count in queue names is present");
		log.info("Expecting count = " + expectCount);
		
		for (String queue : queues) {
			soft.assertTrue(expectCount == queue.matches(".+\\(\\d+\\)$") ,
					String.format("Expecting count = %s not as expected for queue %s name: ", expectCount, queue));
		}
	}
	
	
	
	
}

