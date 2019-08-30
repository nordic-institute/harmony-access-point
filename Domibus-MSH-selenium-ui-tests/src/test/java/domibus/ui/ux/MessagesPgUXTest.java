package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import ddsl.enums.DRoles;
import domibus.BaseUXTest;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessagesPage;
import utils.Generator;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MessagesPgUXTest extends BaseUXTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.MESSAGES);

	/*Login as system admin and open Messages page*/
	@Test(description = "MSG-1", groups = {"multiTenancy", "singleTenancy"})
	public void openMessagesPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().gGoToPage(PAGES.MESSAGES);

		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

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

		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(), null);
		rest.uploadPMode("pmodes/pmode-blue.xml", null);
		String messID = messageSender.sendMessage(user, data.getDefaultTestPass(), null, null);

		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().gGoToPage(PAGES.MESSAGES);


		page.refreshPage();
		page.grid().scrollToAndSelect("Message Id", messID);

		soft.assertTrue(page.getDownloadButton().isEnabled(), "After a row is selected the Download button");

		rest.deletePluginUser(user, null);
		soft.assertAll();
	}

	/*User clicks another grid row*/
	@Test(description = "MSG-3", groups = {"multiTenancy", "singleTenancy"})
	public void selectAnotherRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(), null);
		rest.uploadPMode("pmodes/pmode-blue.xml", null);
		String messID1 = messageSender.sendMessage(user, data.getDefaultTestPass(), null, null);
		String messID2 = messageSender.sendMessage(user, data.getDefaultTestPass(), null, null);

		MessagesPage page = new MessagesPage(driver);
		page.refreshPage();
		page.getSidebar().gGoToPage(PAGES.MESSAGES);
		DGrid grid = page.grid();

		int index1 = grid.scrollTo("Message Id", messID1);
		int index2 = grid.scrollTo("Message Id", messID2);

		grid.selectRow(index1);
		grid.selectRow(index2);

		int selectedRow = grid.getSelectedRowIndex();
		soft.assertEquals(index2, selectedRow, "Selected row index is correct");


		rest.deletePluginUser(user, null);
		soft.assertAll();
	}

	/*Open advanced filters*/
	@Test(description = "MSG-6", groups = {"multiTenancy", "singleTenancy"})
	public void openAdvancedFilters() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().gGoToPage(PAGES.MESSAGES);
		page.getFilters().expandArea();
		advancedFilterPresence(soft, page.getFilters(), descriptorObj.getJSONArray("filters"));
		soft.assertAll();
	}

	/*Click Show columns link*/
	@Test(description = "MSG-17", groups = {"multiTenancy", "singleTenancy"})
	public void showColumnsLink() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().gGoToPage(PAGES.MESSAGES);

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
		page.getSidebar().gGoToPage(PAGES.MESSAGES);

		DGrid grid = page.grid();
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
		page.getSidebar().gGoToPage(PAGES.MESSAGES);
		page.refreshPage();

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		soft.assertAll();
	}

	/*Click Hide link after selecting some new fields*/
	@Test(description = "MSG-20", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWithNewSelection() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().gGoToPage(PAGES.MESSAGES);
		page.refreshPage();

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		grid.getGridCtrl().checkBoxWithLabel("Send Attempts");

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() + 1 == columnsPost.size(), "One more column is shown");
		soft.assertTrue(columnsPost.contains("Send Attempts"), "Correct column is now in the list of columns");

		soft.assertAll();
	}

	/*Click All None link*/
	@Test(description = "MSG-21", groups = {"multiTenancy", "singleTenancy"})
	public void clickAllNoneLink() throws Exception {

		SoftAssert soft = new SoftAssert();
		MessagesPage page = new MessagesPage(driver);
		page.getSidebar().gGoToPage(PAGES.MESSAGES);
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
		page.getSidebar().gGoToPage(PAGES.MESSAGES);
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
		page.getSidebar().gGoToPage(PAGES.MESSAGES);

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




}

