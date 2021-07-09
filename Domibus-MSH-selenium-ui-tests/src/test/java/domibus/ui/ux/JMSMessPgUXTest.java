package domibus.ui.ux;

import io.qameta.allure.*;
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

/**
 * @author Catalin Comanici
 * @since 4.1
 */
@Epic("")
@Feature("")
public class JMSMessPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.JMS_MONITORING);

	/*JMS-1 - Login as super admin and open JMS Monitoring page*/
	/*  JMS-1 - Login as super admin and open JMS Monitoring page  */
	@Description("JMS-1 - Login as super admin and open JMS Monitoring page")
	@Link(name = "EDELIVERY-5128", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5128")
	@AllureId("JMS-1")
	@Test(description = "JMS-1", groups = {"multiTenancy", "singleTenancy"})
	public void openJMSMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		Allure.step("checking page default state");
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
	/*  JMS-2 - Doubleclik on one message  */
	@Description("JMS-2 - Doubleclik on one message")
	@Link(name = "EDELIVERY-5129", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5129")
	@AllureId("JMS-2")
	@Test(description = "JMS-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {

			Allure.step("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");
			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			page.filters().getJmsQueueSelect().selectQueueWithMessages();

			page.grid().waitForRowsToLoad();

			Allure.step("getting info for row 0");
			log.info("getting info for row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			Allure.step("double click row 0");
			log.info("double click row 0");
			page.grid().doubleClickRow(0);

			JMSMessModal modal = new JMSMessModal(driver);
			Allure.step("getting info from modal");
			log.info("getting info from modal");
			HashMap<String, String> modalInfo = modal.getMessageInfo();

			Allure.step("checking modal info against row info");
			log.info("checking modal info against row info");
			soft.assertEquals(rowInfo.get("ID"), modalInfo.get("Id"), "Info from grid and modal is the same (1)");
			soft.assertEquals(rowInfo.get("Time"), modalInfo.get("Timestamp"), "Info from grid and modal is the same (2)");


			soft.assertEquals(rowInfo.get("Custom prop").replaceAll("\\s", ""), modalInfo.get("Custom Properties").replaceAll("\\s", ""), "Info from grid and modal is the same (3)");
		}

		soft.assertAll();
	}

	/*JMS-3 - Filter messages using the filters provided*/
	/*  JMS-3 - Filter messages using the filters provided  */
	@Description("JMS-3 - Filter messages using the filters provided")
	@Link(name = "EDELIVERY-5130", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5130")
	@AllureId("JMS-3")
	@Test(description = "JMS-3", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessages() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Allure.step("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");


			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();


			Allure.step("getting info from row 0");
			log.info("getting info from row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			Allure.step("setting \"From\" filter to: " + rowInfo.get("Time"));
			log.info("setting \"From\" filter to: " + rowInfo.get("Time"));
			page.filters().getJmsFromDatePicker().selectDate(data.fromUIToWidgetFormat(rowInfo.get("Time")));

			Allure.step("setting \"Selector\" filter to: " + getSelector(rowInfo));
			log.info("setting \"Selector\" filter to: " + getSelector(rowInfo));
			page.filters().getJmsSelectorInput().fill(getSelector(rowInfo));
			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			Allure.step("checking number of messages");
			log.info("checking number of messages");
			soft.assertTrue(page.grid().getRowsNo() == 1, "One message is listed, the one that was on first position before");

			Allure.step("checking message id");
			log.info("checking message id");
			HashMap<String, String> newRowInfo = page.grid().getRowInfo(0);
			soft.assertEquals(rowInfo.get("ID"), newRowInfo.get("ID"), "Result has expoected ID");
		}

		soft.assertAll();
	}

	/*	JMS-4 - Filter messages so that there are no results*/
	/*  JMS-4 - Filter messages so that there are no results  */
	@Description("JMS-4 - Filter messages so that there are no results")
	@Link(name = "EDELIVERY-5131", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5131")
	@AllureId("JMS-4")
	@Test(description = "JMS-4", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesNoResults() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Allure.step("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			String endDate = page.filters().getJmsToDatePicker().getSelectedDate();
			page.filters().getJmsFromDatePicker().selectDate(endDate);
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			Allure.step("checking the number of listed rows");
			log.info("checking the number of listed rows");
			soft.assertEquals(page.grid().getRowsNo(), 0, "No message is listed");
		}

		soft.assertAll();
	}

	/*	JMS-5 - Delete all criteria and press Search */
	/*  JMS-5 - Delete all criteria and press Search  */
	@Description("JMS-5 - Delete all criteria and press Search")
	@Link(name = "EDELIVERY-5132", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5132")
	@AllureId("JMS-5")
	@Test(description = "JMS-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesEmptySearch() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Allure.step("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			Allure.step("perform search that results in empty grid");
			log.info("perform search that results in empty grid");
			String endDate = page.filters().getJmsToDatePicker().getSelectedDate();
			page.filters().getJmsFromDatePicker().selectDate(endDate);
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			Allure.step("check number of rows");
			log.info("check number of rows");
			soft.assertEquals(page.grid().getRowsNo(), 0, "No message is listed");

			Allure.step("clearing filters");
			log.info("clearing filters");
			page.filters().getJmsFromDatePicker().clearSelectedDate();
			page.filters().getJmsSearchButton().click();
			page.grid().waitForRowsToLoad();

			JMSSelect select = page.filters().getJmsQueueSelect();
			String q = select.getSelectedValue();
			noOfMessages = select.getListedNoOfMessInQName(q);

			Allure.step("checking number of rows");
			log.info("checking number of rows");
			soft.assertEquals(page.grid().getAllRowInfo().size(), noOfMessages, "All messages are listed");

		}

		soft.assertAll();
	}

	/*JMS-22 - Click on single click*/
	/*  JMS-22 - Click on single click   */
	@Description("JMS-22 - Click on single click ")
	@Link(name = "EDELIVERY-5149", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5149")
	@AllureId("JMS-22")
	@Test(description = "JMS-22", groups = {"multiTenancy", "singleTenancy"})
	public void clickMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Allure.step("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			Allure.step("selecting row 0");
			log.info("selecting row 0");
			page.grid().selectRow(0);

			Allure.step("checking selected row");
			log.info("checking selected row");
			soft.assertTrue(page.grid().getSelectedRowIndex() == 0, "proper row selected");

			Allure.step("checking button state");
			log.info("checking button state");
			soft.assertTrue(page.getMoveButton().isEnabled(), "Move button is enabled after row select");
			soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled after row select");

		}

		soft.assertAll();
	}

	/*	JMS-27 - Check Valid expression for Selector field of Search filter */
	/*  JMS-27 - Check Valid expression for Selector field of Search filter  */
	@Description("JMS-27 - Check Valid expression for Selector field of Search filter")
	@Link(name = "EDELIVERY-5154", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5154")
	@AllureId("JMS-27")
	@Test(description = "JMS-27", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessagesBySelector() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Allure.step("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);
			page.grid().waitForRowsToLoad();

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			Allure.step("getting info from row 0");
			log.info("getting info from row 0");
			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);

			Allure.step("setting \"Selector\" filter to: " + getSelector(rowInfo));
			log.info("setting \"Selector\" filter to: " + getSelector(rowInfo));
			page.filters().getJmsSelectorInput().fill(getSelector(rowInfo));
			page.filters().getJmsSearchButton().click();

			page.grid().waitForRowsToLoad();

			Allure.step("checking number of messages");
			log.info("checking number of messages");
			soft.assertTrue(page.grid().getRowsNo() == 1, "One message is listed, the one that was on first position before");

			Allure.step("checking message id");
			log.info("checking message id");
			HashMap<String, String> newRowInfo = page.grid().getRowInfo(0);
			soft.assertEquals(rowInfo.get("ID"), newRowInfo.get("ID"), "Result has expoected ID");
		}

		soft.assertAll();
	}

	/*	JMS-6 - Download list of messages*/
	/*  JMS-6 - Download list of messages  */
	@Description("JMS-6 - Download list of messages")
	@Link(name = "EDELIVERY-5133", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5133")
	@AllureId("JMS-6")
	@Test(description = "JMS-6", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Allure.step("Navigate to JMS Messages page");
			log.info("Navigate to JMS Messages page");

			JMSMonitoringPage page = new JMSMonitoringPage(driver);
			page.getSidebar().goToPage(PAGES.JMS_MONITORING);

			int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
			page.grid().waitForRowsToLoad();

			String qName = page.filters().getJmsQueueSelect().getSelectedValue();
			Allure.step("verifying for queue " + qName);
			log.info("verifying for queue " + qName);

			String fileName = page.pressSaveCsvAndSaveFile();
			Allure.step("downloaded file " + fileName);
			log.info("downloaded file " + fileName);

			page.grid().getGridCtrl().showCtrls();
			page.grid().getGridCtrl().getAllLnk().click();
			page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

			Allure.step("checking file against info from the grid");
			log.info("checking file against info from the grid");
			page.grid().checkCSVvsGridInfo(fileName, soft);
		}

		soft.assertAll();
	}

	/*	JMS-13 - Download list of messages */
	/*  JMS-13 - Download list of messages  */
	@Description("JMS-13 - Download list of messages")
	@Link(name = "EDELIVERY-5140", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5140")
	@AllureId("JMS-13")
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

			Allure.step("checking domain " + domain);
			log.info("checking domain " + domain);
			page.getDomainSelector().selectOptionByText(domain);

			page.grid().waitForRowsToLoad();
			String qName = page.filters().getJmsQueueSelect().getSelectedValue().replace("[internal]", "").replaceAll("\\(\\d+\\)", "").trim();
			Allure.step("verifying for queue " + qName);
			log.info("verifying for queue " + qName);
			HashMap<String, String> params = new HashMap<>();
			params.put("source", qName);

			String fileName = rest.csv().downloadGrid(RestServicePaths.JMS_MESSAGES_CSV, params, domain);
			Allure.step("downloaded file " + fileName);
			log.info("downloaded file " + fileName);

			page.grid().getGridCtrl().showCtrls();
			page.grid().getGridCtrl().getAllLnk().click();

			Allure.step("checking file against info from the grid");
			log.info("checking file against info from the grid");
			page.grid().checkCSVvsGridInfo(fileName, soft);

			break;
		}

		soft.assertAll();
	}

	/*JMS-14 - Click Show columns link*/
	/*  JMS-14 - Click Show columns link  */
	@Description("JMS-14 - Click Show columns link")
	@Link(name = "EDELIVERY-5141", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5141")
	@AllureId("JMS-14")
	@Test(description = "JMS-14", groups = {"multiTenancy", "singleTenancy"})
	public void showColumnsLink() throws Exception {

		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		Allure.step("click show columns link");
		log.info("click show columns link");
		grid.getGridCtrl().showCtrls();

		Allure.step("checking available options");
		log.info("checking available options");
		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}

	/*JMS-15 - Check/Uncheck of fields on Show links*/
	/*  JMS-15 - CheckUncheck of fields on Show links  */
	@Description("JMS-15 - CheckUncheck of fields on Show links")
	@Link(name = "EDELIVERY-5142", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5142")
	@AllureId("JMS-15")
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

	/*JMS-16 - Click Hide link without any new selection*/
	/*  JMS-16 - Click Hide link without any new selection  */
	@Description("JMS-16 - Click Hide link without any new selection")
	@Link(name = "EDELIVERY-5143", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5143")
	@AllureId("JMS-16")
	@Test(description = "JMS-16", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {

		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Allure.step("getting available columns berfor modification " + columnsPre);
		log.info("getting available columns berfor modification " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		Allure.step("expand column controls");
		log.info("expand column controls");
		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		Allure.step("getting available columns after expanding " + columnsPost);
		log.info("getting available columns after expanding " + columnsPost);
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		soft.assertAll();
	}

	/*JMS-17 - Click Hide link after selecting some new fields*/
	/*  JMS-17 - Click Hide link after selecting some new fields  */
	@Description("JMS-17 - Click Hide link after selecting some new fields")
	@Link(name = "EDELIVERY-5144", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5144")
	@AllureId("JMS-17")
	@Test(description = "JMS-17", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWithNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Allure.step("getting column list before new column is added: " + columnsPre);
		log.info("getting column list before new column is added: " + columnsPre);

		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		Allure.step("enabling column with name " + colName);
		log.info("enabling column with name " + colName);
		grid.getGridCtrl().checkBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		Allure.step("getting column list after new column is added: " + columnsPost);
		log.info("getting column list after new column is added: " + columnsPost);
		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() + 1 == columnsPost.size(), "One more column is shown");
		soft.assertTrue(columnsPost.contains(colName), "Correct column is now in the list of columns");

		soft.assertAll();
	}

	/*	JMS-18 - Click All None link*/
	/*  JMS-18 - Click All None link  */
	@Description("JMS-18 - Click All None link")
	@Link(name = "EDELIVERY-5145", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5145")
	@AllureId("JMS-18")
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
	/*  JMS-19 - Change Rows field data  */
	@Description("JMS-19 - Change Rows field data")
	@Link(name = "EDELIVERY-5146", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5146")
	@AllureId("JMS-19")
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
	/*  JMS-28 - Check sorting on the basis of Headers of Grid   */
	@Description("JMS-28 - Check sorting on the basis of Headers of Grid ")
	@Link(name = "EDELIVERY-5155", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5155")
	@AllureId("JMS-28")
	@Test(description = "JMS-28", groups = {"multiTenancy", "singleTenancy"})
	public void gridSorting() throws Exception {
		SoftAssert soft = new SoftAssert();

		String qWMess = rest.jms().getRandomQNameWithMessages();
		if (StringUtils.isEmpty(qWMess)) {
			throw new SkipException("No queue has messages");
		} else {
			Allure.step("Navigate to JMS Messages page");
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

	/* JMS-29 - Verify headers in downloaded CSV sheet */
	/*  JMS-29 - Verify headers in downloaded CSV sheet   */
	@Description("JMS-29 - Verify headers in downloaded CSV sheet ")
	@Link(name = "EDELIVERY-5156", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5156")
	@AllureId("JMS-29")
	@Test(description = "JMS-29", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		JMSMonitoringPage page = new JMSMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.JMS_MONITORING);

		String qName = page.filters().getJmsQueueSelect().getSelectedValue().replace("[internal]", "").replaceAll("\\(\\d+\\)", "").trim();
		Allure.step("verifying for queue " + qName);
		log.info("verifying for queue " + qName);
		HashMap<String, String> params = new HashMap<>();
		params.put("source", qName);

		String fileName = rest.csv().downloadGrid(RestServicePaths.JMS_MESSAGES_CSV, params, null);
		Allure.step("downloaded file " + fileName);
		log.info("downloaded file " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Allure.step("checking file against info from the grid");
		log.info("checking file against info from the grid");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	private String getSelector(HashMap<String, String> messInfo) throws JSONException {

		JSONObject customProp = new JSONObject(messInfo.get("Custom prop"));

		String messageId = customProp.getString("messageId");
		String fromPartyId = customProp.getString("fromPartyId");

		String selector = String.format("messageId='%s' AND fromPartyId='%s'", messageId, fromPartyId);
		return selector;
	}


}
