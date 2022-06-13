package domibus.ui.ux;

import org.testng.Reporter;
import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
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

import java.util.HashMap;
import java.util.List;

public class JMSMessPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.JMS_MONITORING);

	/* EDELIVERY-5128 - JMS-1 - Login as super admin and open JMS Monitoring page */
	@Test(description = "JMS-1", groups = {"multiTenancy", "singleTenancy"})
	public void openJMSMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		Reporter.log("checking page default state");
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

	/* EDELIVERY-5129 - JMS-2 - Doubleclik on one message */
	@Test(description = "JMS-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {

			Reporter.log("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");
			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			page.filters().getJmsQueueSelect().selectQueueWithMessages();

			page.grid().waitForRowsToLoad();

			Reporter.log("getting info for row 0");
			log.info("getting info for row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			Reporter.log("double click row 0");
			log.info("double click row 0");
			page.grid().doubleClickRow(0);

			JMSMessModal modal = new JMSMessModal(driver);
			Reporter.log("getting info from modal");
			log.info("getting info from modal");
			HashMap<String, String> modalInfo = modal.getMessageInfo();

			Reporter.log("checking modal info against row info");
			log.info("checking modal info against row info");
			soft.assertEquals(rowInfo.get("ID"), modalInfo.get("Id"), "Info from grid and modal is the same (1)");
			soft.assertEquals(rowInfo.get("Time"), modalInfo.get("Timestamp"), "Info from grid and modal is the same (2)");


			soft.assertEquals(rowInfo.get("Custom prop").replaceAll("\\s", ""), modalInfo.get("Custom Properties").replaceAll("\\s", ""), "Info from grid and modal is the same (3)");
		}

		soft.assertAll();
	}

	/* EDELIVERY-5130 - JMS-3 - Filter messages using the filters provided */
	@Test(description = "JMS-3", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessages() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Reporter.log("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");


			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();


			Reporter.log("getting info from row 0");
			log.info("getting info from row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			Reporter.log("setting \"From\" filter to: " + rowInfo.get("Time"));
			log.info("setting \"From\" filter to: " + rowInfo.get("Time"));
			page.filters().getJmsFromDatePicker().selectDate(data.fromUIToWidgetFormat(rowInfo.get("Time")));

			Reporter.log("setting \"Selector\" filter to: " + getSelector(rowInfo));
			log.info("setting \"Selector\" filter to: " + getSelector(rowInfo));
			page.filters().getJmsSelectorInput().fill(getSelector(rowInfo));
			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			Reporter.log("checking message id");
			log.info("checking message id");
			HashMap<String, String> newRowInfo = page.grid().getRowInfo(0);
			soft.assertEquals(rowInfo.get("ID"), newRowInfo.get("ID"), "Result has expoected ID");
		}

		soft.assertAll();
	}

	/* EDELIVERY-5131 - JMS-4 - Filter messages so that there are no results */
	@Test(description = "JMS-4", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesNoResults() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Reporter.log("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			String endDate = page.filters().getJmsToDatePicker().getSelectedDate();
			page.filters().getJmsFromDatePicker().selectDate(endDate);
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			Reporter.log("checking the number of listed rows");
			log.info("checking the number of listed rows");
			soft.assertEquals(page.grid().getRowsNo(), 0, "No message is listed");
		}

		soft.assertAll();
	}

	/* EDELIVERY-5132 - JMS-5 - Delete all criteria and press Search */
	@Test(description = "JMS-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesEmptySearch() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Reporter.log("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			Reporter.log("perform search that results in empty grid");
			log.info("perform search that results in empty grid");
			String endDate = page.filters().getJmsToDatePicker().getSelectedDate();
			page.filters().getJmsFromDatePicker().selectDate(endDate);
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			Reporter.log("check number of rows");
			log.info("check number of rows");
			soft.assertEquals(page.grid().getRowsNo(), 0, "No message is listed");

			Reporter.log("clearing filters");
			log.info("clearing filters");
			page.filters().getJmsFromDatePicker().clearSelectedDate();
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			JMSSelect select = page.filters().getJmsQueueSelect();
			String q = select.getSelectedValue();
			noOfMessages = select.getListedNoOfMessInQName(q);

			Reporter.log("checking number of rows");
			log.info("checking number of rows");
			soft.assertEquals(page.grid().getAllRowInfo().size(), noOfMessages, "All messages are listed");

		}

		soft.assertAll();
	}

	/* EDELIVERY-5149 - JMS-22 - Click on single click  */
	@Test(description = "JMS-22", groups = {"multiTenancy", "singleTenancy"})
	public void clickMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Reporter.log("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			Reporter.log("selecting row 0");
			log.info("selecting row 0");
			page.grid().selectRow(0);

			Reporter.log("checking selected row");
			log.info("checking selected row");
			soft.assertTrue(page.grid().getSelectedRowIndex() == 0, "proper row selected");

			Reporter.log("checking button state");
			log.info("checking button state");
			soft.assertTrue(page.getMoveButton().isEnabled(), "Move button is enabled after row select");
			soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled after row select");

		}

		soft.assertAll();
	}

	/* EDELIVERY-5154 - JMS-27 - Check Valid expression for Selector field of Search filter */
	@Test(description = "JMS-27", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesBySelector() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Reporter.log("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);
			page.grid().waitForRowsToLoad();

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			Reporter.log("getting info from row 0");
			log.info("getting info from row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			Reporter.log("setting \"Selector\" filter to: " + getSelector(rowInfo));
			log.info("setting \"Selector\" filter to: " + getSelector(rowInfo));
			page.filters().getJmsSelectorInput().fill(getSelector(rowInfo));
			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			Reporter.log("checking number of messages");
			log.info("checking number of messages");
			soft.assertTrue(page.grid().getRowsNo() >= 1, "At least one message is listed");

			Reporter.log("checking message id");
			log.info("checking message id");
			HashMap<String, String> newRowInfo = page.grid().getRowInfo(0);
			soft.assertEquals(rowInfo.get("ID"), newRowInfo.get("ID"), "Result has expoected ID");
		}

		soft.assertAll();
	}

	/* EDELIVERY-5133 - JMS-6 - Download list of messages */
	@Test(description = "JMS-6", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Reporter.log("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			String qName = page.filters().getJmsQueueSelect().getSelectedValue();
			Reporter.log("verifying for queue " + qName);
			log.info("verifying for queue " + qName);

			String fileName = page.pressSaveCsvAndSaveFile();
			Reporter.log("downloaded file " + fileName);
			log.info("downloaded file " + fileName);

			page.grid().getGridCtrl().showCtrls();
			page.grid().getGridCtrl().getAllLnk().click();
			page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

			Reporter.log("checking file against info from the grid");
			log.info("checking file against info from the grid");
			page.grid().checkCSVvsGridInfo(fileName, soft);
		}

		soft.assertAll();
	}

	/* EDELIVERY-5140 - JMS-13 - Download list of messages */
	@Test(description = "JMS-13", groups = {"multiTenancy"})
	public void csvFileDownloadMultiDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);
//		page.grid().waitForRowsToLoad();

		List<String> domains = rest.getDomainNames();

		for (String domain : domains) {

			if (StringUtils.equalsIgnoreCase(domain, "default")) {
				continue;
			}

			Reporter.log("checking domain " + domain);
			log.info("checking domain " + domain);
			page.getDomainSelector().selectOptionByText(domain);

			page.grid().waitForRowsToLoad();
			String qName = page.filters().getJmsQueueSelect().getSelectedValue().replace("[internal]", "").replaceAll("\\(\\d+\\)", "").trim();
			Reporter.log("verifying for queue " + qName);
			log.info("verifying for queue " + qName);
			HashMap<String, String> params = new HashMap<>();
			params.put("source", qName);

			String fileName = rest.csv().downloadGrid(RestServicePaths.JMS_MESSAGES_CSV, params, domain);
			Reporter.log("downloaded file " + fileName);
			log.info("downloaded file " + fileName);

			page.grid().getGridCtrl().showCtrls();
			page.grid().getGridCtrl().getAllLnk().click();

			Reporter.log("checking file against info from the grid");
			log.info("checking file against info from the grid");
			page.grid().checkCSVvsGridInfo(fileName, soft);

			break;
		}

		soft.assertAll();
	}

	/* EDELIVERY-5141 - JMS-14 - Click Show columns link */
	@Test(description = "JMS-14", groups = {"multiTenancy", "singleTenancy"})
	public void showColumnsLink() throws Exception {

		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		Reporter.log("click show columns link");
		log.info("click show columns link");
		grid.getGridCtrl().showCtrls();

		Reporter.log("checking available options");
		log.info("checking available options");
		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}

	/* EDELIVERY-5142 - JMS-15 - CheckUncheck of fields on Show links */
	@Test(description = "JMS-15", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {

		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);


		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		grid.checkModifyVisibleColumns(soft);
		soft.assertAll();
	}

	/* EDELIVERY-5143 - JMS-16 - Click Hide link without any new selection */
	@Test(description = "JMS-16", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {

		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Reporter.log("getting available columns berfor modification " + columnsPre);
		log.info("getting available columns berfor modification " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		Reporter.log("expand column controls");
		log.info("expand column controls");
		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		Reporter.log("getting available columns after expanding " + columnsPost);
		log.info("getting available columns after expanding " + columnsPost);
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		soft.assertAll();
	}

	/* EDELIVERY-5144 - JMS-17 - Click Hide link after selecting some new fields */
	@Test(description = "JMS-17", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWithNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Reporter.log("getting column list before new column is added: " + columnsPre);
		log.info("getting column list before new column is added: " + columnsPre);

		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		Reporter.log("enabling column with name " + colName);
		log.info("enabling column with name " + colName);
		grid.getGridCtrl().checkBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		Reporter.log("getting column list after new column is added: " + columnsPost);
		log.info("getting column list after new column is added: " + columnsPost);
		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() + 1 == columnsPost.size(), "One more column is shown");
		soft.assertTrue(columnsPost.contains(colName), "Correct column is now in the list of columns");

		soft.assertAll();
	}

	/* EDELIVERY-5145 - JMS-18 - Click All None link */
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

	/* EDELIVERY-5146 - JMS-19 - Change Rows field data */
	@Test(description = "JMS-19", groups = {"multiTenancy", "singleTenancy"})
	public void changeNumberOfRows() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/* EDELIVERY-5155 - JMS-28 - Check sorting on the basis of Headers of Grid  */
	@Test(description = "JMS-28", groups = {"multiTenancy", "singleTenancy"})
	public void gridSorting() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Reporter.log("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

			DGrid grid = page.grid();
			grid.getPagination().getPageSizeSelect().selectOptionByText("100");
			grid.waitForRowsToLoad();

			for (int i = 0; i < 3; i++) {
				JSONObject colDesc = colDescs.getJSONObject(i);
				if (grid.getColumnNames().contains(colDesc.getString("name"))) {
					TestUtils.testSortingForColumn(soft, grid, colDesc);
				}
			}
		}
		soft.assertAll();
	}

	/* EDELIVERY-5156 - JMS-29 - Verify headers in downloaded CSV sheet  */
	@Test(description = "JMS-29", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		String qName = page.filters().getJmsQueueSelect().getSelectedValue().replace("[internal]", "").replaceAll("\\(\\d+\\)", "").trim();
		Reporter.log("verifying for queue " + qName);
		log.info("verifying for queue " + qName);
		HashMap<String, String> params = new HashMap<>();
		params.put("source", qName);

		String fileName = rest.csv().downloadGrid(RestServicePaths.JMS_MESSAGES_CSV, params, null);
		Reporter.log("downloaded file " + fileName);
		log.info("downloaded file " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Reporter.log("checking file against info from the grid");
		log.info("checking file against info from the grid");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	private String getSelector(HashMap<String, String> messInfo) throws JSONException {

		JSONObject customProp = new JSONObject(messInfo.get("Custom prop"));

		String key1 = customProp.keySet().toArray()[0].toString();
		String key2 = customProp.keySet().toArray()[1].toString();

		String val1 = customProp.getString(key1);
		String val2 = customProp.getString(key2);

		String selector = String.format("%s='%s' AND %s='%s'", key1, val1, key2, val2);
		return selector;
	}


}
