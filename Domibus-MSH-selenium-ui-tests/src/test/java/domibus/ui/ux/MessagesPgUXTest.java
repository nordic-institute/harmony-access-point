package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DWait;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FileUtils;
import utils.BaseUXTest;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessagesPage;
import rest.RestServicePaths;
import utils.DFileUtils;
import utils.Generator;
import utils.TestUtils;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici

 * @since 4.1
 */
public class MessagesPgUXTest extends BaseUXTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.MESSAGES);
	private static String sourceMessageHeader = "Source Message";
	private static String fragmentMessageHeader = "Message Fragment";

	/*Login as system admin and open Messages page*/
	@Test(description = "MSG-1", groups = {"multiTenancy", "singleTenancy"})
	public void openMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		log.info("Checking page title");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

		log.info("checking basic filter presence");
		basicFilterPresence(soft, page.getFilters(), descriptorObj.getJSONArray("filters"));

		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}

		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");

		testButonPresence(soft, page, descriptorObj.getJSONArray("buttons"));

		soft.assertAll();
	}


	/*User clicks grid row*/
	@Test(description = "MSG-2", groups = {"multiTenancy", "singleTenancy"})
	public void messageRowSelect() throws Exception {
		SoftAssert soft = new SoftAssert();

		String messID = getMessageIDs(null, 1, false).get(0);

		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		log.info("selecting message with id " + messID);
		page.refreshPage();
		page.grid().scrollToAndSelect("Message Id", messID);

		log.info("checking download button is enabled");
		soft.assertTrue(page.getDownloadButton().isEnabled(), "After a row is selected the Download button");

		soft.assertAll();
	}

	/*User clicks another grid row*/
	@Test(description = "MSG-3", groups = {"multiTenancy", "singleTenancy"})
	public void selectAnotherRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> messIds = getMessageIDs(null, 2, false);
		String messID1 = messIds.get(0);
		String messID2 = messIds.get(1);

		MessagesPage page = new MessagesPage(driver);
		page.refreshPage();
		page.getSidebar().goToPage(PAGES.MESSAGES);
		DGrid grid = page.grid();

		log.info("selecting mess with id " +messID1);
		grid.scrollToAndSelect("Message Id", messID1);

		log.info("selecting mess with id " +messID2);
		int index2 = grid.scrollTo("Message Id", messID2);
		grid.selectRow(index2);

		log.info("checking selected message");
		int selectedRow = grid.getSelectedRowIndex();
		soft.assertEquals(index2, selectedRow, "Selected row index is correct");

		soft.assertAll();
	}

	/*Open advanced filters*/
	@Test(description = "MSG-6", groups = {"multiTenancy", "singleTenancy"})
	public void openAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.getFilters().expandArea();
		advancedFilterPresence(soft, page.getFilters(), descriptorObj.getJSONArray("filters"));
		soft.assertAll();
	}

	/*rules need to be adapted to account for messages received in the same second that mess the order..*/
	/* Download list of messages */ /* issue posted EDELIVERY-6118*/
	@Test(description = "MSG-10", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void csvFileDownload() throws Exception{
		SoftAssert soft = new SoftAssert();

		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		String fileName = rest.downloadGrid(RestServicePaths.MESSAGE_LOG_CSV, null, null);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		log.info("sorting after column Received");
		page.grid().sortBy("Received");

		log.info("se page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	/*Click Show columns link*/
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
	@Test(description = "MSG-18", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		DGrid grid = page.grid();
		log.info("expanding controls");
		grid.getGridCtrl().showCtrls();

		List<String> columnList = new ArrayList<>(grid.getGridCtrl().getAllCheckboxStatuses().keySet());
		grid.checkModifyVisibleColumns(soft, columnList);

		soft.assertAll();
	}

	/*Click Hide link without any new selection*/
	@Test(description = "MSG-19", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.refreshPage();

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

	/*Click Hide link after selecting some new fields*/
	@Test(description = "MSG-20", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWithNewSelection() throws Exception {
		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);
		page.refreshPage();

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		log.info("getting column list before new column is added: " + columnsPre);

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

	/*Click All None link*/
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
	@Test(description = "MSG-24", groups = {"multiTenancy", "singleTenancy"} ,enabled = false)
	public void gridSorting() throws Exception {
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		DGrid grid = page.grid();
		grid.getPagination().getPageSizeSelect().selectOptionByText("100");

		for (int i = 0; i < colDescs.length(); i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}
		soft.assertAll();
	}

	/* Verify headers in downloaded CSV sheet */
	@Test(description = "MSG-25", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.MESSAGES);
		log.info("logged in");
		MessagesPage page = new MessagesPage(driver);

		String fileName = rest.downloadGrid(RestServicePaths.MESSAGE_LOG_CSV, null, null);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		log.info("sorting after column Received");
		page.grid().sortBy("Received");

		log.info("se page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}
/* This method will verify non presence of two headers Message Fragments and Source Message in csv if not available as grid column */
	@Test(description = "MSG-26", groups = {"multiTenancy", "singleTenancy"})
	public void verifySpecificHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);

		String pluginUser = Generator.randomAlphaNumeric(5);
		rest.createPluginUser(pluginUser, DRoles.ADMIN, data.getNewTestPass(), page.getDomainFromTitle());

		String messageID = messageSender.sendMessage(pluginUser, data.getNewTestPass(), Generator.randomAlphaNumeric(10), Generator.randomAlphaNumeric(10));
		page.getSidebar().goToPage(PAGES.MESSAGES);

		page.refreshPage();
		page.grid().waitForRowsToLoad();

		page.grid().checkAllLink(soft);
		page.grid().waitForRowsToLoad();

		HashMap<String, String> fMessage = page.grid().getRowInfo("Message Id", messageID);

		page.getFilters().basicFilterBy(fMessage.get("Message Id"), fMessage.get("Message Status")
				, fMessage.get("From Party Id"), fMessage.get("To Party Id"));
		page.grid().waitForRowsToLoad();

		log.info("Clean given directory");
		FileUtils.cleanDirectory(new File(DFileUtils.downloadFolderPath()));

		log.info("Click on download csv button");
		page.clickDownloadCsvButton(page.getDownloadCsvButton().element);

		log.info("Wait for download to complete");
		DWait wait = new DWait(driver);
		wait.forXMillis(1000);

		log.info("Check if file is downloaded at given location");
		soft.assertTrue(DFileUtils.isFileDownloaded(DFileUtils.downloadFolderPath()), "File is downloaded successfully");
		String completeFilePath = DFileUtils.downloadFolderPath() + File.separator + DFileUtils.getCompleteFileName(DFileUtils.downloadFolderPath());

		log.info("Verify headers from Downloaded csv and Grid Headers");
		page.grid().checkCSVvsGridHeaders(completeFilePath, soft);

		List<String> columnNames = page.grid().getColumnNames();

		CSVParser csvParser = new CSVParser(Files.newBufferedReader(Paths.get(completeFilePath)), CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase()
				.withTrim());
		List<String> csvFileHeaders = new ArrayList<>();
		csvFileHeaders.addAll(csvParser.getHeaderMap().keySet());

		if (columnNames.equals(fragmentMessageHeader) && columnNames.equals(sourceMessageHeader)) {
			soft.assertTrue(csvFileHeaders.equals(fragmentMessageHeader) && csvFileHeaders.equals(sourceMessageHeader),
					"Message Fragment  & Source Message header are shown in csv");
		} else {
			soft.assertFalse(csvFileHeaders.equals(fragmentMessageHeader) && csvFileHeaders.equals(sourceMessageHeader),
					"Message Fragment  & Source Message header are  not shown in csv");
		}
		soft.assertAll();
	}

}

