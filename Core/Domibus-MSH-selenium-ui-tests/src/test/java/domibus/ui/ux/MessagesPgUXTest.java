package domibus.ui.ux;

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
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessagesPage;
import utils.Gen;
import utils.TestUtils;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MessagesPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.MESSAGES);

	/* EDELIVERY-5053 - MSG-1 - Login as super admin and open Messages page */
	@Test(description = "MSG-1", groups = {"multiTenancy", "singleTenancy"})
	public void openMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		Reporter.log("Checking page title");
		log.info("Checking page title");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

		Reporter.log("checking basic filter presence");
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


	/* EDELIVERY-5054 - MSG-2 - User clicks grid row */
	@Test(description = "MSG-2", groups = {"multiTenancy", "singleTenancy"})
	public void messageRowSelect() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		Reporter.log("selecting message with status SEND_FAILURE ");
		log.info("selecting message with status SEND_FAILURE ");
		page.grid().scrollToAndSelect("Message Status", "SEND_FAILURE");

		Reporter.log("checking download button is enabled");
		log.info("checking download button is enabled");
		soft.assertTrue(page.getDownloadButton().isEnabled(), "After a row is selected the Download button");

		soft.assertAll();
	}

	/* EDELIVERY-5055 - MSG-3 - User clicks another grid row */
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

		Reporter.log("selecting mess with id " + messID1);
		log.info("selecting mess with id " + messID1);
		grid.scrollToAndSelect("Message Id", messID1);

		Reporter.log("selecting mess with id " + messID2);
		log.info("selecting mess with id " + messID2);
		int index2 = grid.scrollToAndSelect("Message Id", messID2);

		Reporter.log("checking selected message");
		log.info("checking selected message");
		int selectedRow = grid.getSelectedRowIndex();
		soft.assertEquals(index2, selectedRow, "Selected row index is correct");

		soft.assertAll();
	}

	/* EDELIVERY-5058 - MSG-6 - Open advanced filters */
	@Test(description = "MSG-6", groups = {"multiTenancy", "singleTenancy"})
	public void openAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.getFilters().expandArea();
		advancedFilterPresence(soft, page.getFilters(), descriptorObj.getJSONArray("filters"));
		soft.assertAll();
	}

	/* EDELIVERY-5062 - MSG-10 - Download list of messages */
	@Test(description = "MSG-10", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		String fileName = page.pressSaveCsvAndSaveFile();
		Reporter.log("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();


		Reporter.log("set page size to 100");
		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		Reporter.log("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.grid().relaxCheckCSVvsGridInfo(fileName, soft, "datetime");

		soft.assertAll();
	}

	/* EDELIVERY-5069 - MSG-17 - Click Show columns link */
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

	/* EDELIVERY-5070 - MSG-18 - CheckUncheck of fields on Show links */
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

	/* EDELIVERY-5071 - MSG-19 - Click Hide link without any new selection */
	@Test(description = "MSG-19", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.refreshPage();

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Reporter.log("getting available columns before modification " + columnsPre);
		log.info("getting available columns before modification " + columnsPre);

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

	/* EDELIVERY-5072 - MSG-20 - Click Hide link after selecting some new fields */
	@Test(description = "MSG-20", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWithNewSelection() throws Exception {
		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		List<String> columnsPre = grid.getColumnNames();
		Reporter.log("getting column list before new column is added: " + columnsPre);
		log.info("getting column list before new column is added: " + columnsPre);

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

	/* EDELIVERY-5073 - MSG-21 - Click All None link */
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

	/* EDELIVERY-5074 - MSG-22 - Change Rows field data */
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

	/* EDELIVERY-5076 - MSG-24 - Check sorting on the basis of Headers of Grid  */
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
			Reporter.log("checking sorting for column - " + colDesc.getString("name"));
			log.info("checking sorting for column - " + colDesc.getString("name"));
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}
		soft.assertAll();
	}

	/* EDELIVERY-5077 - MSG-25 - Verify headers in downloaded CSV sheet  */
	@Test(description = "MSG-25", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("logged in");
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		String fileName = page.pressSaveCsvAndSaveFile();
		Reporter.log("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Reporter.log("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	/* EDELIVERY-6363 - MSG-26 - Verify absence of Message fragment and Source message header in downaloded csv */
	@Test(description = "MSG-26", groups = {"multiTenancy", "singleTenancy"})
	public void verifySplitAndJoinSpecificHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();

		String sourceMessageHeader = "Source Message";
		String fragmentMessageHeader = "Message Fragment";

		Reporter.log("logged in");
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		String filename = page.pressSaveCsvAndSaveFile();
		Reporter.log("downloaded file with name " + filename);
		log.info("downloaded file with name " + filename);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		List<String> gridHeaders = page.grid().getColumnNames();

		Reporter.log("Getting headers in file");
		log.info("Getting headers in file");
		Reader reader = Files.newBufferedReader(Paths.get(filename));
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
		List<String> csvHeaders = new ArrayList<>();
		csvHeaders.addAll(csvParser.getHeaderMap().keySet());

		soft.assertEquals(csvHeaders.contains(sourceMessageHeader), gridHeaders.contains(sourceMessageHeader), "Source header is either present or not present in both grid and CSV");
		soft.assertEquals(csvHeaders.contains(fragmentMessageHeader), gridHeaders.contains(fragmentMessageHeader), "Fragment header is either present or not present in both grid and CSV");

		soft.assertAll();
	}

	/* EDELIVERY-5075 - MSG-23 - Verify max value for Received up to field  */
	@Test(description = "MSG-23", groups = {"multiTenancy", "singleTenancy"})
	public void receivedToMaxValue() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("logged in");
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);
		Calendar cal = Calendar.getInstance();

		Reporter.log("Current date is : " + cal.get(Calendar.DAY_OF_MONTH));
		log.info("Current date is : " + cal.get(Calendar.DAY_OF_MONTH));
		Reporter.log("Current hour is : " + cal.get(Calendar.HOUR_OF_DAY));
		log.info("Current hour is : " + cal.get(Calendar.HOUR_OF_DAY));
		Reporter.log("Current Minute is : " + cal.get(Calendar.MINUTE));
		log.info("Current Minute is : " + cal.get(Calendar.MINUTE));

		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.getFilters().expandArea();
		Reporter.log("Click on ReceivedTo field clock");
		log.info("Click on ReceivedTo field clock");
		page.receivedToClock.click();
		DatePicker datePicker = new DatePicker(driver, page.receivedTo);
		soft.assertTrue(datePicker.verifyMaxClockValue(soft, cal) > 0, "field is accepting correct value ,smaller than System's current date & time");

		soft.assertAll();
	}

	/* EDELIVERY-7183 - MSG-28 - Resend message as USER */
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

	@Test(description = "MSG-31", groups = {"multiTenancy", "singleTenancy"})
	public void checkMsgEnvPresence() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		List<String> messIds = rest.getMessageIDsWithStatus(null, "ACKNOWLEDGED");
		messIds.addAll(rest.getMessageIDsWithStatus(null, "DELETED"));

		if (messIds.size() == 0) {
			throw new SkipException("There are no messages with needed status");
		}

		int rowindex = page.grid().scrollTo("Message Id", messIds.get(0));

		soft.assertTrue(page.isActionIconPresent(rowindex, "downloadEnvelopes") && page.getActionIconStatus(rowindex, "downloadEnvelopes"), "downloadEnvelopes icon is shown and enabled");
		String user = Gen.randomAlphaNumeric(10);
		rest.users().createUser(user, DRoles.USER, data.defaultPass(), "default");
		logout();
		login(user, data.defaultPass());

		rowindex = page.grid().scrollTo("Message Id", messIds.get(0));
		soft.assertTrue(page.isActionIconPresent(rowindex, "downloadEnvelopes") && page.getActionIconStatus(rowindex, "downloadEnvelopes"), "downloadEnvelopes icon is shown and enabled");
		if (data.isMultiDomain()) {
			String adminUsr = Gen.randomAlphaNumeric(10);
			rest.users().createUser(adminUsr, DRoles.ADMIN, data.defaultPass(), "default");
			logout();
			login(adminUsr, data.defaultPass());

			rowindex = page.grid().scrollTo("Message Id", messIds.get(0));
			soft.assertTrue(page.isActionIconPresent(rowindex, "downloadEnvelopes") && page.getActionIconStatus(rowindex, "downloadEnvelopes"), "downloadEnvelopes icon is shown and enabled");
		}

		soft.assertAll();
	}


}

