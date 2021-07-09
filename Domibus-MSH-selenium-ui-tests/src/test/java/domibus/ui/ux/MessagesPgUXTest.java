package domibus.ui.ux;

import io.qameta.allure.*;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DatePicker;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessageFilterArea;
import pages.messages.MessagesPage;
import rest.RestServicePaths;
import utils.Gen;
import utils.TestUtils;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
@Epic("")
@Feature("")
public class MessagesPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.MESSAGES);


	/*Login as system admin and open Messages page*/
	/*  MSG-1 - Login as super admin and open Messages page  */
	@Description("MSG-1 - Login as super admin and open Messages page")
	@Link(name = "EDELIVERY-5053", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5053")
	@AllureId("MSG-1")
	@Test(description = "MSG-1", groups = {"multiTenancy", "singleTenancy"})
	public void openMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		Allure.step("Checking page title");
		log.info("Checking page title");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

		Allure.step("checking basic filter presence");
		log.info("checking basic filter presence");
		basicFilterPresence(soft, page.getFilters(), descriptorObj.getJSONArray("filters"));

		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}

		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");

		testButtonPresence(soft, page, descriptorObj.getJSONArray("buttons"));

		soft.assertAll();
	}


	/*User clicks grid row*/
	/*  MSG-2 - User clicks grid row  */
	@Description("MSG-2 - User clicks grid row")
	@Link(name = "EDELIVERY-5054", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5054")
	@AllureId("MSG-2")
	@Test(description = "MSG-2", groups = {"multiTenancy", "singleTenancy"})
	public void messageRowSelect() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		Allure.step("selecting message with status SEND_FAILURE ");
		log.info("selecting message with status SEND_FAILURE ");
		page.grid().scrollToAndSelect("Message Status", "SEND_FAILURE");

		Allure.step("checking download button is enabled");
		log.info("checking download button is enabled");
		soft.assertTrue(page.getDownloadButton().isEnabled(), "After a row is selected the Download button");

		soft.assertAll();
	}

	/*User clicks another grid row*/
	/*  MSG-3 - User clicks another grid row  */
	@Description("MSG-3 - User clicks another grid row")
	@Link(name = "EDELIVERY-5055", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5055")
	@AllureId("MSG-3")
	@Test(description = "MSG-3", groups = {"multiTenancy", "singleTenancy"})
	public void selectAnotherRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> messIds = rest.getMessageIDs(null, 2, false);
		String messID1 = messIds.get(0);
		String messID2 = messIds.get(1);

		MessagesPage page = new MessagesPage(driver);
		page.refreshPage();
		page.getSidebar().goToPage(PAGES.MESSAGES);
		DGrid grid = page.grid();

		Allure.step("selecting mess with id " + messID1);
		log.info("selecting mess with id " + messID1);
		grid.scrollToAndSelect("Message Id", messID1);

		Allure.step("selecting mess with id " + messID2);
		log.info("selecting mess with id " + messID2);
		int index2 = grid.scrollToAndSelect("Message Id", messID2);

		Allure.step("checking selected message");
		log.info("checking selected message");
		int selectedRow = grid.getSelectedRowIndex();
		soft.assertEquals(index2, selectedRow, "Selected row index is correct");

		soft.assertAll();
	}

	/*Open advanced filters*/
	/*  MSG-6 - Open advanced filters  */
	@Description("MSG-6 - Open advanced filters")
	@Link(name = "EDELIVERY-5058", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5058")
	@AllureId("MSG-6")
	@Test(description = "MSG-6", groups = {"multiTenancy", "singleTenancy"})
	public void openAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.getFilters().expandArea();
		advancedFilterPresence(soft, page.getFilters(), descriptorObj.getJSONArray("filters"));
		soft.assertAll();
	}

	/* Download list of messages */
	/*  MSG-10 - Download list of messages  */
	@Description("MSG-10 - Download list of messages")
	@Link(name = "EDELIVERY-5062", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5062")
	@AllureId("MSG-10")
	@Test(description = "MSG-10", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		String fileName = rest.csv().downloadGrid(RestServicePaths.MESSAGE_LOG_CSV, null, null);
		Allure.step("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();


		Allure.step("set page size to 100");
		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		Allure.step("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.grid().relaxCheckCSVvsGridInfo(fileName, soft, "datetime");

		soft.assertAll();
	}

	/*Click Show columns link*/
	/*  MSG-17 - Click Show columns link  */
	@Description("MSG-17 - Click Show columns link")
	@Link(name = "EDELIVERY-5069", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5069")
	@AllureId("MSG-17")
	@Test(description = "MSG-17", groups = {"multiTenancy", "singleTenancy"})
	public void showColumnsLink() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}

	/*Check/Uncheck of fields on Show links*/
	/*  MSG-18 - CheckUncheck of fields on Show links  */
	@Description("MSG-18 - CheckUncheck of fields on Show links")
	@Link(name = "EDELIVERY-5070", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5070")
	@AllureId("MSG-18")
	@Test(description = "MSG-18", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		grid.checkModifyVisibleColumns(soft);

		soft.assertAll();
	}

	/*Click Hide link without any new selection*/
	/*  MSG-19 - Click Hide link without any new selection  */
	@Description("MSG-19 - Click Hide link without any new selection")
	@Link(name = "EDELIVERY-5071", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5071")
	@AllureId("MSG-19")
	@Test(description = "MSG-19", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.refreshPage();

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Allure.step("getting available columns before modification " + columnsPre);
		log.info("getting available columns before modification " + columnsPre);

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

	/*Click Hide link after selecting some new fields*/
	/*  MSG-20 - Click Hide link after selecting some new fields  */
	@Description("MSG-20 - Click Hide link after selecting some new fields")
	@Link(name = "EDELIVERY-5072", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5072")
	@AllureId("MSG-20")
	@Test(description = "MSG-20", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWithNewSelection() throws Exception {
		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		List<String> columnsPre = grid.getColumnNames();
		Allure.step("getting column list before new column is added: " + columnsPre);
		log.info("getting column list before new column is added: " + columnsPre);

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

	/*Click All None link*/
	/*  MSG-21 - Click All None link  */
	@Description("MSG-21 - Click All None link")
	@Link(name = "EDELIVERY-5073", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5073")
	@AllureId("MSG-21")
	@Test(description = "MSG-21", groups = {"multiTenancy", "singleTenancy"})
	public void clickAllNoneLink() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.refreshPage();

		DGrid grid = page.grid();
		grid.checkAllLink(soft);
		grid.checkNoneLink(soft);

		soft.assertAll();
	}

	/*Change Rows field data*/
	/*  MSG-22 - Change Rows field data  */
	@Description("MSG-22 - Change Rows field data")
	@Link(name = "EDELIVERY-5074", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5074")
	@AllureId("MSG-22")
	@Test(description = "MSG-22", groups = {"multiTenancy", "singleTenancy"})
	public void changeNumberOfRows() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.refreshPage();
		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/*Check sorting on the basis of Headers of Grid */
	/*  MSG-24 - Check sorting on the basis of Headers of Grid   */
	@Description("MSG-24 - Check sorting on the basis of Headers of Grid ")
	@Link(name = "EDELIVERY-5076", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5076")
	@AllureId("MSG-24")
	@Test(description = "MSG-24", groups = {"multiTenancy", "singleTenancy"})
	public void gridSorting() throws Exception {
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
//		grid.getPagination().getPageSizeSelect().selectOptionByText("25");
		grid.getGridCtrl().showAllColumns();
		grid.waitForRowsToLoad();

		for (int i = 0; i < 3; i++) {
			int index = Gen.randomNumber(colDescs.length() - 1);

//			this code is here because of bug EDELIVERY-6734
			if (index == 17) {
				continue;
			}

			JSONObject colDesc = colDescs.getJSONObject(index);
			Allure.step("checking sorting for column - " + colDesc.getString("name"));
			log.info("checking sorting for column - " + colDesc.getString("name"));
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}
		soft.assertAll();
	}

	/* Verify headers in downloaded CSV sheet */
	/*  MSG-25 - Verify headers in downloaded CSV sheet   */
	@Description("MSG-25 - Verify headers in downloaded CSV sheet ")
	@Link(name = "EDELIVERY-5077", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5077")
	@AllureId("MSG-25")
	@Test(description = "MSG-25", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("logged in");
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		String fileName = page.pressSaveCsvAndSaveFile();
		Allure.step("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Allure.step("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	/* verify the two headers, Message Fragments and Source Message are NOT present in csv if not available as grid column */
	/*  MSG-26 - Verify absence of Message fragment and Source message header in downaloded csv  */
	@Description("MSG-26 - Verify absence of Message fragment and Source message header in downaloded csv")
	@Link(name = "EDELIVERY-6363", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-6363")
	@AllureId("MSG-26")
	@Test(description = "MSG-26", groups = {"multiTenancy", "singleTenancy"})
	public void verifySplitAndJoinSpecificHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();

		String sourceMessageHeader = "Source Message";
		String fragmentMessageHeader = "Message Fragment";

		Allure.step("logged in");
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		String filename = page.pressSaveCsvAndSaveFile();
		Allure.step("downloaded file with name " + filename);
		log.info("downloaded file with name " + filename);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		List<String> gridHeaders = page.grid().getColumnNames();

		Allure.step("Getting headers in file");
		log.info("Getting headers in file");
		Reader reader = Files.newBufferedReader(Paths.get(filename));
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
		List<String> csvHeaders = new ArrayList<>();
		csvHeaders.addAll(csvParser.getHeaderMap().keySet());

		soft.assertEquals(csvHeaders.contains(sourceMessageHeader), gridHeaders.contains(sourceMessageHeader), "Source header is either present or not present in both grid and CSV");
		soft.assertEquals(csvHeaders.contains(fragmentMessageHeader), gridHeaders.contains(fragmentMessageHeader), "Fragment header is either present or not present in both grid and CSV");

		soft.assertAll();
	}

	/* MSG-23 - Verify max value for Received up to field */
	/*  MSG-23 - Verify max value for Received up to field   */
	@Description("MSG-23 - Verify max value for Received up to field ")
	@Link(name = "EDELIVERY-5075", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5075")
	@AllureId("MSG-23")
	@Test(description = "MSG-23", groups = {"multiTenancy", "singleTenancy"})
	public void receivedToMaxValue() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("logged in");
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);
		Calendar cal = Calendar.getInstance();

		Allure.step("Current date is : " + cal.get(Calendar.DAY_OF_MONTH));
		log.info("Current date is : " + cal.get(Calendar.DAY_OF_MONTH));
		Allure.step("Current hour is : " + cal.get(Calendar.HOUR_OF_DAY));
		log.info("Current hour is : " + cal.get(Calendar.HOUR_OF_DAY));
		Allure.step("Current Minute is : " + cal.get(Calendar.MINUTE));
		log.info("Current Minute is : " + cal.get(Calendar.MINUTE));

		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.getFilters().expandArea();
		Allure.step("Click on ReceivedTo field clock");
		log.info("Click on ReceivedTo field clock");
		page.receivedToClock.click();
		DatePicker datePicker = new DatePicker(driver, page.receivedTo);
		soft.assertTrue(datePicker.verifyMaxClockValue(soft, cal) > 0, "field is accepting correct value ,smaller than System's current date & time");

		soft.assertAll();
	}

	/*  MSG-28 - Resend message as USER  */
	@Description("MSG-28 - Resend message as USER")
	@Link(name = "EDELIVERY-7183", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7183")
	@AllureId("MSG-28")
	@Test(description = "MSG-28", groups = {"multiTenancy", "singleTenancy"})
	public void resendMsg() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);

		rest.users().createUser(username, DRoles.USER, data.defaultPass(), null);
		String pluginUser = Gen.randomAlphaNumeric(10);
		rest.pluginUsers().createPluginUser(pluginUser, DRoles.ADMIN, data.defaultPass(), null);

		String messID = messageSender.sendMessage(pluginUser, data.defaultPass(), null, null);
		logout();
		login(username, data.defaultPass());
		MessagesPage mPage = new MessagesPage(driver);
		soft.assertFalse(mPage.getResendButton().isPresent(), "Resend button is not present");
		soft.assertFalse(mPage.isActionIconPresent(0, "Resend"), "Icon is not present");
		soft.assertAll();
	}

	/* MSG-31 - Verify Message Envelop presence for all admin console users */
	@Description("MSG-31 - Verify Message Envelop presence for all admin console users")
	@Link(name = "EDELIVERY-8180", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-8180")
	@AllureId("MSG-31")
	@Test(description = "MSG-31", groups = {"multiTenancy", "singleTenancy"})
	public void checkMsgEnvPresence() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		int gridRow = page.grid().getPagination().getTotalItems();
		if (gridRow > 0) {
			soft.assertTrue(page.isActionIconPresent(0, "downloadEnvelopes") && page.getActionIconStatus(0, "downloadEnvelopes"), "downloadEnvelopes icon is shown and enabled");
			String user = Gen.randomAlphaNumeric(10);
			rest.users().createUser(user, DRoles.USER, data.defaultPass(), "Default");
			logout();
			login(user, data.defaultPass());
			soft.assertTrue(page.isActionIconPresent(0, "downloadEnvelopes") && page.getActionIconStatus(0, "downloadEnvelopes"), "downloadEnvelopes icon is shown and enabled");
			if (data.isMultiDomain()) {
				String adminUsr = Gen.randomAlphaNumeric(10);
				rest.users().createUser(adminUsr, DRoles.ADMIN, data.defaultPass(), "Default");
				logout();
				login(adminUsr, data.defaultPass());
				soft.assertTrue(page.isActionIconPresent(0, "downloadEnvelopes") && page.getActionIconStatus(0, "downloadEnvelopes"), "downloadEnvelopes icon is shown and enabled");
			}
		} else {
			throw new SkipException("Grid has no data present");
		}
		soft.assertAll();
	}


}

