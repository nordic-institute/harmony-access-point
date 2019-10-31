package domibus.ui.toref;

import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.BaseTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.jms.JMSMessModal;
import pages.jms.JMSMonitoringPage;
import pages.jms.JMSMoveMessageModal;
import rest.RestServicePaths;

import java.util.HashMap;

/**
 * @author Catalin Comanici

 * @since 4.1
 */
public class JMSMessPgTest extends BaseTest {

	/*JMS-1 - Login as super admin and open JMS Monitoring page*/
	@Test(description = "JMS-1", groups = {"multiTenancy", "singleTenancy"})
	public void openJMSMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		soft.assertTrue(page.isLoaded());

		soft.assertAll();
	}

	@Test(description = "JMS-2.2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		if (noOfMessages > 0) {
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
			page.grid().doubleClickRow(0);

			JMSMessModal modal = new JMSMessModal(driver);
			HashMap<String, String> modalInfo = modal.getMessageInfo();

			soft.assertEquals(rowInfo.get("ID"), modalInfo.get("Id"), "Info from grid and modal is the same (1)");
			soft.assertEquals(rowInfo.get("Time"), modalInfo.get("Timestamp"), "Info from grid and modal is the same (2)");


			soft.assertEquals(rowInfo.get("Custom prop").replaceAll("\\s", ""), modalInfo.get("Custom Properties").replaceAll("\\s", ""), "Info from grid and modal is the same (3)");
		}

		soft.assertAll();
	}

	@Test(description = "JMS-2.1", groups = {"multiTenancy", "singleTenancy"})
	public void clickMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		if (noOfMessages > 0) {
			page.grid().selectRow(0);

			soft.assertTrue(page.getMoveButton().isEnabled(), "Move button is enabled after row select");
			soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled after row select");

		} else {
			throw new SkipException("Not enough messages in any of the queues to run test");
		}

		soft.assertAll();
	}

	@Test(description = "JMS-3", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessages() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		if (noOfMessages > 0) {
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			page.filters().getJmsFromDatePicker().selectDate(rowInfo.get("Time"));
			page.filters().getJmsSelectorInput().fill(getSelector(rowInfo));
			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			soft.assertTrue(page.grid().getRowsNo() == 1, "One message is listed, the one that was on first position before");

		}

		soft.assertAll();
	}

	@Test(description = "JMS-3.1", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesBySelector() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		page.filters().getJmsSelectorInput().fill("totally invalid selector");
		page.filters().getJmsSearchButton().click();

		soft.assertTrue(page.getAlertArea().isError());
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.JMS_INVALID_SELECTOR_ERROR, "Correct message is displayed");

		int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		if (noOfMessages > 0) {
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			page.filters().getJmsSelectorInput().fill(getSelector(rowInfo));
			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			soft.assertTrue(page.grid().getRowsNo() == 1, "One message is listed, the one that was on first position before");

		} else {
			throw new SkipException("Not enough messages in any of the queues to run test");
		}

		soft.assertAll();
	}

	@Test(description = "JMS-4", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesNoResults() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		if (noOfMessages > 0) {
			String endDate = page.filters().getJmsToDatePicker().getSelectedDate();
			page.filters().getJmsFromDatePicker().selectDate(endDate);

			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			soft.assertEquals(page.grid().getRowsNo(), 0, "No message is listed");
		}

		soft.assertAll();
	}

	@Test(description = "JMS-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesEmptySearch() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		if (noOfMessages > 0) {
			String endDate = page.filters().getJmsToDatePicker().getSelectedDate();
			page.filters().getJmsFromDatePicker().selectDate(endDate);

			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			soft.assertEquals(page.grid().getRowsNo(), 0, "No message is listed");

			page.filters().getJmsFromDatePicker().clearSelectedDate();
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			soft.assertEquals(page.grid().getAllRowInfo().size(), noOfMessages, "All messages are listed");

		}

		soft.assertAll();
	}

	@Test(description = "JMS-6", groups = {"multiTenancy", "singleTenancy"})
	public void deleteJMSMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		if (noOfMessages > 0) {
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
			page.grid().selectRow(0);
			page.getDeleteButton().click();
			page.getCancelButton().click();
			new Dialog(driver).confirm();
			soft.assertTrue(page.grid().scrollTo("ID", rowInfo.get("ID")) >= 0, "Message still present in the grid after user cancels delete operation");


			HashMap<String, String> rowInfo2 = page.grid().getRowInfo(0);
			page.grid().selectRow(0);
			page.getDeleteButton().click();
			page.getSaveButton().click();

			soft.assertTrue(page.grid().scrollTo("ID", rowInfo2.get("ID")) < 0, "Message NOT present in the grid after delete operation");

		}

		soft.assertAll();
	}

	@Test(description = "JMS-7", groups = {"multiTenancy", "singleTenancy"})
	public void moveMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		page.grid().waitForRowsToLoad();
		int noOfMessInDQL = page.grid().getPagination().getTotalItems();

		int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessagesNotDLQ();
		page.grid().waitForRowsToLoad();

		String queuename = page.filters().getJmsQueueSelect().getSelectedValue();

		if (noOfMessages > 0) {
			page.grid().selectRow(0);
			page.getMoveButton().click();

			JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
			modal.getQueueSelect().selectDLQQueue();
			modal.clickCancel();

			soft.assertEquals(noOfMessages, page.grid().getPagination().getTotalItems(), "Number of messages in current queue is not changed");

			page.filters().getJmsQueueSelect().selectDLQQueue();
			page.grid().waitForRowsToLoad();

			soft.assertEquals(noOfMessInDQL, page.grid().getPagination().getTotalItems(), "Number of messages in DLQ message queue is not changed");

			page.filters().getJmsQueueSelect().selectOptionByText(queuename);
			page.grid().waitForRowsToLoad();

			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
			page.grid().selectRow(0);
			page.getMoveButton().click();

			modal.getQueueSelect().selectDLQQueue();
			modal.clickOK();

			page.grid().waitForRowsToLoad();

			soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");
			soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.JMS_MOVE_MESSAGE_SUCCESS, "Correct message is shown");

			soft.assertTrue(page.grid().getPagination().getTotalItems() == noOfMessages - 1, "Queue has one less message");

			page.filters().getJmsQueueSelect().selectDLQQueue();
			page.grid().waitForRowsToLoad();

			soft.assertEquals(noOfMessInDQL + 1, page.grid().getPagination().getTotalItems(), "DQL queue has one more message after the move");

			int index = page.grid().scrollTo("ID", rowInfo.get("ID"));

			soft.assertTrue(index > -1, "DQL queue contains the new message");
		} else {
			throw new SkipException("Not enough messages in any of the queues to run test");
		}

		soft.assertAll();
	}

	@Test(description = "JMS-8", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		HashMap<String, String> params = new HashMap<>();
		params.put("source", "DomibusDLQ");

		String fileName = rest.downloadGrid(RestServicePaths.JMS_MESSAGES_CSV, params, null);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		page.grid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	@Test(description = "JMS-9", groups = {"multiTenancy", "singleTenancy"})
	public void gridSelfAssert() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		page.grid().assertControls(soft);

		soft.assertAll();
	}






	private String getSelector(HashMap<String, String> messInfo) throws JSONException {
		String selectorTemplate = "MESSAGE_ID='%s' AND JMSMessageID='%s'";

		String custProp = messInfo.get("Custom prop");
		String jmsProp = messInfo.get("JMS prop");

		String messageId = new JSONObject(custProp).getString("MESSAGE_ID");
		String jmsMessageID = new JSONObject(jmsProp).getString("JMSMessageID");

		return String.format(selectorTemplate, messageId, jmsMessageID);

	}


/*
	JMS-1 - Login as super admin and open JMS Monitoring page
	JMS-2 - Doubleclik on one message
	JMS-3 - Filter messages using the filters provided
	JMS-4 - Filter messages so that there are no results
	JMS-5 - Delete all criteria and press Search
	JMS-6 - Download list of messages
	JMS-7 - Delete message
	JMS-8 - Move message
	JMS-9 - Domain admin logs in and views messages
	JMS-10 - Super admin logs in and views messages for a selected domain, selects 1 message, and changes domain
	JMS-11 - Super admin logs in and views messages for a selected domain, navigates to second page of messages and changes domain
	JMS-12 - Super admin selects a message and chooses to delete it
	JMS-13 - Download list of messages
	JMS-14 - Click Show columns link
	JMS-15 - Check/Uncheck of fields on Show links
	JMS-16 - Click Hide link without any new selection
	JMS-17 - Click Hide link after selecting some new fields
	JMS-18 - Click All None link
	JMS-19 - Change Rows field data
	JMS-20 - Download list as CSV
	JMS-21 - Max date for Received Up To field
	JMS-22 - Click on single click
	JMS-23 - Check queue message count against each JMS queue in Search filter Source field in case of Admin
	JMS-24 - Check queue message count against each queue in destination on Move pop up in case of admin
	JMS-25 - Check queue message count against each JMS queue in Search filter Source field in case of Super Admin
	JMS-26 - Check queue message count against each queue in destination on Move pop up in case of Super admin
	JMS-27 - Check Valid expression for Selector field of Search filter
	JMS-28 - Check sorting on the basis of Headers of Grid
	JMS-29 - Verify headers in downloaded CSV sheet
	*/





}
