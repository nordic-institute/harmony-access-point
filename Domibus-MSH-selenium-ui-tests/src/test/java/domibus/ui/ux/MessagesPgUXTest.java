package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import utils.BaseTest;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessagesPage;
import rest.RestServicePaths;
import utils.Generator;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Catalin Comanici

 * @since 4.1
 */
public class MessagesPgUXTest extends BaseTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.MESSAGES);

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

		testButtonPresence(soft, page, descriptorObj.getJSONArray("buttons"));

		soft.assertAll();
	}


	/*User clicks grid row*/
	@Test(description = "MSG-2", groups = {"multiTenancy", "singleTenancy"})
	public void messageRowSelect() throws Exception {
		SoftAssert soft = new SoftAssert();

		String messID = restUtils.getMessageIDs(null, 1, false).get(0);

		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		log.info("selecting message with id " + messID);
		page.refreshPage();
		page.grid().scrollToAndSelect("Message Id", messID);

		log.info("checking download button is enabled");
		soft.assertTrue(page.getDownloadButton().isEnabled(), "After a row is selected the Download button");

		soft.assertAll();

		//TODO: this test fails if the found message happen to be ACKNOWLEDGED or another non-downloadable state;
		//      we should locate by status=SEND_FAILURE the message (and generate one if not found)
	}

	/*User clicks another grid row*/
	@Test(description = "MSG-3", groups = {"multiTenancy", "singleTenancy"})
	public void selectAnotherRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> messIds = restUtils.getMessageIDs(null, 2, false);
		String messID1 = messIds.get(0);
		String messID2 = messIds.get(1);

		MessagesPage page = new MessagesPage(driver);
		page.refreshPage();
		page.getSidebar().goToPage(PAGES.MESSAGES);
		DGrid grid = page.grid();

		log.info("selecting mess with id " +messID1);
		grid.scrollToAndSelect("Message Id", messID1);

		log.info("selecting mess with id " +messID2);
		int index2 = grid.scrollToAndSelect("Message Id", messID2);

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

	/* Download list of messages */
	@Test(description = "MSG-10", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception{
		SoftAssert soft = new SoftAssert();

		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		String fileName = rest.csv().downloadGrid(RestServicePaths.MESSAGE_LOG_CSV, null, null);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		log.info("sorting after column Received");
		page.grid().sortBy("Received");

		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		log.info("checking info in grid against the file");
		page.grid().relaxCheckCSVvsGridInfo(fileName, soft, "datetime");

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
	@Test(description = "MSG-24", groups = {"multiTenancy", "singleTenancy"})
	public void gridSorting() throws Exception {
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().goToPage(PAGES.MESSAGES);

		DGrid grid = page.grid();
		grid.getPagination().getPageSizeSelect().selectOptionByText("100");

		for (int i = 0; i < 3; i++) {
			int index = Generator.randomNumber(colDescs.length()-1);
			JSONObject colDesc = colDescs.getJSONObject(index);
			log.info("checking sorting for column" + colDesc.getString("name"));
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

		String fileName = rest.csv().downloadGrid(RestServicePaths.MESSAGE_LOG_CSV, null, null);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		log.info("sorting after column Received");
		page.grid().sortBy("Received");

		log.info("set page size to 100");
		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

}

