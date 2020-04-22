package domibus.ui.ux;

import ddsl.dcomponents.DomainSelector;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.Select;
import ddsl.enums.PAGES;
import org.apache.commons.lang3.StringUtils;
import utils.BaseTest;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.jms.JMSMessModal;
import pages.jms.JMSMonitoringPage;
import pages.jms.JMSSelect;
import rest.RestServicePaths;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class JMSMessPgUXTest extends BaseTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.JMS_MONITORING);

	/*JMS-1 - Login as super admin and open JMS Monitoring page*/
	@Test(description = "JMS-1", groups = {"multiTenancy", "singleTenancy"})
	public void openJMSMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		log.info("checking page default state");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");
		soft.assertTrue(page.filters().isLoaded(), "Filters are loaded and visible");
		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}

		testButtonPresence(soft, page, descriptorObj.getJSONArray("buttons"));

		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");


		soft.assertAll();
	}

	/*JMS-2 - Double-click on one message*/
	@Test(description = "JMS-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		int noOfMessages = 0;
		try {
			noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		} catch (Exception e) {
			throw new SkipException(e.getMessage());
		}

		if (noOfMessages > 0) {

			log.info("getting info for row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			log.info("double click row 0");
			page.grid().doubleClickRow(0);

			JMSMessModal modal = new JMSMessModal(driver);
			log.info("getting info from modal");
			HashMap<String, String> modalInfo = modal.getMessageInfo();

			log.info("checking modal info against row info");
			soft.assertEquals(rowInfo.get("ID"), modalInfo.get("Id"), "Info from grid and modal is the same (1)");
			soft.assertEquals(rowInfo.get("Time"), modalInfo.get("Timestamp"), "Info from grid and modal is the same (2)");


			soft.assertEquals(rowInfo.get("Custom prop").replaceAll("\\s", ""), modalInfo.get("Custom Properties").replaceAll("\\s", ""), "Info from grid and modal is the same (3)");
		}

		soft.assertAll();
	}

	/*JMS-3 - Filter messages using the filters provided*/
	@Test(description = "JMS-3", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void filterMessages() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		int noOfMessages = 0;
		try {
			noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		} catch (Exception e) {
			throw new SkipException(e.getMessage());
		}
		if (noOfMessages > 0) {
			log.info("getting info from row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			log.info("setting \"From\" filter to: " + rowInfo.get("Time"));
			page.filters().getJmsFromDatePicker().selectDate(data.fromUIToWidgetFormat(rowInfo.get("Time")));

			log.info("setting \"Selector\" filter to: " + getSelector(rowInfo));
			page.filters().getJmsSelectorInput().fill(getSelector(rowInfo));
			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			log.info("checking number of messages");
			soft.assertTrue(page.grid().getRowsNo() == 1, "One message is listed, the one that was on first position before");

			log.info("checking message id");
			HashMap<String, String> newRowInfo = page.grid().getRowInfo(0);
			soft.assertEquals(rowInfo.get("ID"), newRowInfo.get("ID"), "Result has expoected ID");
		}

		soft.assertAll();
	}

	/*	JMS-4 - Filter messages so that there are no results*/
	@Test(description = "JMS-4", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesNoResults() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		int noOfMessages = 0;
		try {
			noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		} catch (Exception e) {
			throw new SkipException(e.getMessage());
		}
		if (noOfMessages > 0) {
			log.info("setting the same from and to date");
			String endDate = page.filters().getJmsToDatePicker().getSelectedDate();
			page.filters().getJmsFromDatePicker().selectDate(endDate);
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			log.info("checking the number of listed rows");
			soft.assertEquals(page.grid().getRowsNo(), 0, "No message is listed");
		}

		soft.assertAll();
	}

	/*	JMS-5 - Delete all criteria and press Search */
	@Test(description = "JMS-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesEmptySearch() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		int noOfMessages = 0;
		try {
			noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		} catch (Exception e) {
			throw new SkipException(e.getMessage());
		}
		if (noOfMessages > 0) {
			page.grid().waitForRowsToLoad();
			log.info("perform search that results in empty grid");
			String endDate = page.filters().getJmsToDatePicker().getSelectedDate();
			page.filters().getJmsFromDatePicker().selectDate(endDate);
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			log.info("check number of rows");
			soft.assertEquals(page.grid().getRowsNo(), 0, "No message is listed");

			log.info("clearing filters");
			page.filters().getJmsFromDatePicker().clearSelectedDate();
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			JMSSelect select = page.filters().getJmsQueueSelect();
			String q = select.getSelectedValue();
			noOfMessages = select.getListedNoOfMessInQName(q);

			log.info("checking number of rows");
			soft.assertEquals(page.grid().getAllRowInfo().size(), noOfMessages, "All messages are listed");

		}

		soft.assertAll();
	}

	/*JMS-22 - Click on single click*/
	@Test(description = "JMS-22", groups = {"multiTenancy", "singleTenancy"})
	public void clickMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		int noOfMessages = 0;
		try {
			noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		} catch (Exception e) {
			throw new SkipException(e.getMessage());
		}
		if (noOfMessages > 0) {
			log.info("selecting row 0");
			page.grid().selectRow(0);

			log.info("checking selected row");
			soft.assertTrue(page.grid().getSelectedRowIndex() == 0, "proper row selected");

			log.info("checking button state");
			soft.assertTrue(page.getMoveButton().isEnabled(), "Move button is enabled after row select");
			soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled after row select");

		} else {
			throw new SkipException("Not enough messages in any of the queues to run test");
		}

		soft.assertAll();
	}

	/*	JMS-27 - Check Valid expression for Selector field of Search filter */
	@Test(description = "JMS-27", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void filterMessagesBySelector() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		int noOfMessages = 0;
		try {
			noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
		} catch (Exception e) {
			throw new SkipException(e.getMessage());
		}
		if (noOfMessages > 0) {
			log.info("getting info from row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			log.info("setting \"Selector\" filter to: " + getSelector(rowInfo));
			page.filters().getJmsSelectorInput().fill(getSelector(rowInfo));
			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			log.info("checking number of messages");
			soft.assertTrue(page.grid().getRowsNo() == 1, "One message is listed, the one that was on first position before");

			log.info("checking message id");
			HashMap<String, String> newRowInfo = page.grid().getRowInfo(0);
			soft.assertEquals(rowInfo.get("ID"), newRowInfo.get("ID"), "Result has expoected ID");
		}

		soft.assertAll();
	}

	/*	JMS-6 - Download list of messages*/
	@Test(description = "JMS-6", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
		page.grid().waitForRowsToLoad();

		String qName = page.filters().getJmsQueueSelect().getSelectedValue().replace("[internal]", "").replaceAll("\\(\\d+\\)", "").trim();
		log.info("verifying for queue " + qName);
		HashMap<String, String> params = new HashMap<>();
		params.put("source", qName);

		String fileName = rest.downloadGrid(RestServicePaths.JMS_MESSAGES_CSV, params, null);
		log.info("downloaded file " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		log.info("checking file against info from the grid");
		page.grid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

/*	JMS-13 - Download list of messages */
	@Test(description = "JMS-13", groups = {"multiTenancy"})
	public void csvFileDownloadMultiDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
//		page.grid().waitForRowsToLoad();

		List<String> domains = rest.getDomainNames();

		for (String domain : domains) {

			if(StringUtils.equalsIgnoreCase(domain, "default")){
				continue;
			}

			log.info("checking domain " + domain);
			page.getDomainSelector().selectOptionByText(domain);


			String qName = page.filters().getJmsQueueSelect().getSelectedValue().replace("[internal]", "").replaceAll("\\(\\d+\\)", "").trim();
			log.info("verifying for queue " + qName);
			HashMap<String, String> params = new HashMap<>();
			params.put("source", qName);

			String fileName = rest.downloadGrid(RestServicePaths.JMS_MESSAGES_CSV, params, domain);
			log.info("downloaded file " + fileName);

			page.grid().getGridCtrl().showCtrls();
			page.grid().getGridCtrl().getAllLnk().click();

			log.info("checking file against info from the grid");
			page.grid().checkCSVvsGridInfo(fileName, soft);

			break;
		}

		soft.assertAll();
	}

	/*JMS-14 - Click Show columns link*/
	@Test(description = "JMS-14", groups = {"multiTenancy", "singleTenancy"})
	public void showColumnsLink() throws Exception {

		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		log.info("click show columns link");
		grid.getGridCtrl().showCtrls();

		log.info("checking available options");
		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}

	/*JMS-15 - Check/Uncheck of fields on Show links*/
	@Test(description = "JMS-15", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {

		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		log.info("expanding controls");
		grid.getGridCtrl().showCtrls();

		List<String> columnList = new ArrayList<>(grid.getGridCtrl().getAllCheckboxStatuses().keySet());
		grid.checkModifyVisibleColumns(soft, columnList);
		soft.assertAll();
	}

	/*JMS-16 - Click Hide link without any new selection*/
	@Test(description = "JMS-16", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {

		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		log.info("getting available columns berfor modification " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		log.info("expand column controls");
		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		log.info("getting available columns after expanding " +columnsPost);
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		soft.assertAll();
	}

	/*JMS-17 - Click Hide link after selecting some new fields*/
	@Test(description = "JMS-17", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWithNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		log.info("getting column list before new column is added: " + columnsPre);

		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		log.info("enabling column with name " + colName);
		grid.getGridCtrl().checkBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		log.info("getting column list after new column is added: " + columnsPost);
		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() + 1 == columnsPost.size(), "One more column is shown");
		soft.assertTrue(columnsPost.contains(colName), "Correct column is now in the list of columns");

		soft.assertAll();
	}

	/*	JMS-18 - Click All None link*/
	@Test(description = "JMS-18", groups = {"multiTenancy", "singleTenancy"})
	public void clickAllNoneLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		grid.checkAllLink(soft);
		grid.checkNoneLink(soft);

		soft.assertAll();
	}

	/*JMS-19 - Change Rows field data*/
	@Test(description = "JMS-19", groups = {"multiTenancy", "singleTenancy"})
	public void changeNumberOfRows() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/* JMS-28 - Check sorting on the basis of Headers of Grid */
	@Test(description = "JMS-28", groups = {"multiTenancy", "singleTenancy"})
	public void gridSorting() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		DGrid grid = page.grid();
		grid.getPagination().getPageSizeSelect().selectOptionByText("100");

		for (int i = 0; i < 3; i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}
		soft.assertAll();
	}

	/* JMS-29 - Verify headers in downloaded CSV sheet */
	@Test(description = "JMS-29", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		String qName = page.filters().getJmsQueueSelect().getSelectedValue().replace("[internal]", "").replaceAll("\\(\\d+\\)", "").trim();
		log.info("verifying for queue " + qName);
		HashMap<String, String> params = new HashMap<>();
		params.put("source", qName);

		String fileName = rest.downloadGrid(RestServicePaths.JMS_MESSAGES_CSV, params, null);
		log.info("downloaded file " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		log.info("checking file against info from the grid");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	private String getSelector(HashMap<String, String> messInfo) throws JSONException {

		String jmsProp = messInfo.get("JMS prop");
		String jmsMessageID = new JSONObject(jmsProp).getString("JMSMessageID");

		String custProp = messInfo.get("Custom prop");
		if (custProp.contains("MESSAGE_ID")) {
			String selectorTemplate = "MESSAGE_ID='%s' AND JMSMessageID='%s'";
			String messageId = new JSONObject(custProp).getString("MESSAGE_ID");
			return String.format(selectorTemplate, messageId, jmsMessageID);
		} else {
			// some legit jms messages don't contain the MESSAGE_ID prop
			String selectorTemplate = "JMSMessageID='%s'";
			return String.format(selectorTemplate, jmsMessageID);
		}
	}


}
