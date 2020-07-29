package domibus.ui.functional;

import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.jms.JMSMonitoringPage;
import pages.jms.JMSMoveMessageModal;

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
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		page.grid().waitForRowsToLoad();
		page.filters().getJmsSearchButton().isEnabled();
		
		
		int noOfMessages = -1;
		try {
			noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		} catch (Exception e) {
			throw new SkipException(e.getMessage());
		}
		if (noOfMessages > 0) {
			
			
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
	
//	This cannot run reliable because messages are pulled from the queues as the tests run
	/*JMS-8 - Move message*/
	@Test(description = "JMS-8", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void moveMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		rest.sendMessages(5, null);
		
		
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		
		log.info("checking no of messages");
		page.grid().waitForRowsToLoad();
		int noOfMessInDQL = page.grid().getPagination().getTotalItems();
		
		int noOfMessages;
		try {
			noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessagesNotDLQ();
		} catch (Exception e) {
			throw new SkipException(e.getMessage());
		}
		page.grid().waitForRowsToLoad();
		
		String queuename = page.filters().getJmsQueueSelect().getSelectedValue();
		
		if (noOfMessages > 0) {
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
		} else {
			throw new SkipException("Not enough messages in any of the queues to run test");
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
	
	



}
