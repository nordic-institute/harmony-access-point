package domibus.ui;

import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.BaseUXTest;
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
public class JMSMessPgTest extends BaseUXTest {

	@Test(description = "JMS-1", groups = {"multiTenancy", "singleTenancy"})
	public void openJMSMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		soft.assertTrue(page.isLoaded());

		soft.assertAll();
	}

	@Test(description = "JMS-2.2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
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
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
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
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
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
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
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
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
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
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
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
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
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
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
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
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		HashMap<String, String> params = new HashMap<>();
		params.put("source", "DomibusDLQ");

		String fileName = rest.downloadGrid(RestServicePaths.JMS_MESSAGES_CSV, params, null);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		page.grid().checkCSVAgainstGridInfo(fileName, soft);

		soft.assertAll();
	}

	@Test(description = "JMS-9", groups = {"multiTenancy", "singleTenancy"})
	public void gridSelfAssert() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.JMS_MONITORING);
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


}
