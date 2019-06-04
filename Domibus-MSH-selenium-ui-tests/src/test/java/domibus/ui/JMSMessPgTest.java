package domibus.ui;

import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DOMIBUS_PAGES;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.jms.JMSMessModal;
import pages.jms.JMSMonitoringPage;
import pages.jms.JMSMoveMessageModal;

import java.util.HashMap;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class JMSMessPgTest extends BaseTest {

	@Test(description = "JMS-1", groups = {"multiTenancy", "singleTenancy"})
	public void openJMSMessagesPage() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		soft.assertTrue(page.isLoaded());

		soft.assertAll();
	}

	@Test(description = "JMS-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickMessage() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().selectQueueWithMessages();
		if(noOfMessages>0) {
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
			page.grid().doubleClickRow(0);

			JMSMessModal modal = new JMSMessModal(driver);
			HashMap<String, String> modalInfo = modal.getMessageInfo();

			soft.assertEquals(rowInfo.get("ID"), modalInfo.get("Id"), "Info from grid and modal is the same (1)");
			soft.assertEquals(rowInfo.get("Time"), modalInfo.get("Timestamp"), "Info from grid and modal is the same (2)");
			soft.assertEquals(rowInfo.get("Custom prop"), modalInfo.get("Custom Properties"), "Info from grid and modal is the same (3)");
		}

		soft.assertAll();
	}

	@Test(description = "JMS-3", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessages() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().selectQueueWithMessages();
		if(noOfMessages>0) {
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			page.filters().getJmsFromDatePicker().selectDate(rowInfo.get("Time"));
			page.filters().getJmsSelectorInput().fill(getSelector(rowInfo));
			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			soft.assertTrue(page.grid().getRowsNo()==1, "One message is listed, the one that was on first position before");

		}

		soft.assertAll();
	}

	@Test(description = "JMS-4", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesNoResults() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().selectQueueWithMessages();
		if(noOfMessages>0) {
			String endDate = page.filters().getJmsToDatePicker().getSelectedDate();
			page.filters().getJmsFromDatePicker().selectDate(endDate);

			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			soft.assertEquals(page.grid().getRowsNo(), 0, "No message is listed");
		}

		soft.assertAll();
	}

	@Test(description = "JMS-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesEmptySearch() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().selectQueueWithMessages();
		if(noOfMessages>0) {
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
	public void deleteJMSMessage() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().selectQueueWithMessages();
		if(noOfMessages>0) {
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
			page.grid().selectRow(0);
			page.getDeleteButton().click();
			page.getCancelButton().click();
			new Dialog(driver).confirm();
			soft.assertTrue(page.grid().scrollTo("ID", rowInfo.get("ID"))>=0, "Message still present in the grid after user cancels delete operation");


			HashMap<String, String> rowInfo2 = page.grid().getRowInfo(0);
			page.grid().selectRow(0);
			page.getDeleteButton().click();
			page.getSaveButton().click();

			soft.assertTrue(page.grid().scrollTo("ID", rowInfo2.get("ID"))<0, "Message NOT present in the grid after delete operation");

		}

		soft.assertAll();
	}

	@Test(description = "JMS-7", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void moveMessage() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.JMS_MONITORING);
		JMSMonitoringPage page = new JMSMonitoringPage(driver);

		int noOfMessages = page.filters().selectQueueWithMessagesNotDLQ();
		if(noOfMessages>0) {
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
			page.grid().selectRow(0);
			page.getMoveButton().click();

			JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
			modal.getQueueSelect().selectOptionByIndex(5);

			modal.clickCancel();

		}

		page.wait.forXMillis(10000);

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
