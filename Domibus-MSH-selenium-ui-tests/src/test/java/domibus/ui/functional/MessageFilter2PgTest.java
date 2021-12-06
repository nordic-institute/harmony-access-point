package domibus.ui.functional;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.msgFilter.MessageFilterGrid;
import pages.msgFilter.MessageFilterModal;
import pages.msgFilter.MessageFilterPage;
import rest.RestServicePaths;
import utils.DFileUtils;
import utils.Gen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;




public class MessageFilter2PgTest extends SeleniumTest {

	private MessageFilterPage navigateToPage() throws Exception {
		MessageFilterPage page = new MessageFilterPage(driver);
		if (page.getTitle().contains("Filter")) {
			page.refreshPage();
		} else {
			page.getSidebar().goToPage(PAGES.MESSAGE_FILTER);
		}
		page.grid().waitForRowsToLoad();
		return page;
	}

    /* EDELIVERY-5097 - MSGF-24 - Double click on one message filter */
	@Test(description = "MSGF-24", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickRow() throws Exception {
		log.info("Create a filter to edit");
		SoftAssert soft = new SoftAssert();

		String actionName = Gen.randomAlphaNumeric(5);
		rest.messFilters().createMessageFilter(actionName, null);

		MessageFilterPage page = navigateToPage();


		int index = page.grid().scrollTo("Action", actionName);
		HashMap<String, String> rowInfo = page.grid().getRowInfo(index);
		log.info("double click the row");
		page.grid().doubleClickRow(index);

		MessageFilterModal modal = new MessageFilterModal(driver);
		soft.assertTrue(modal.isLoaded(), "Double-clicking a row opens the edit message filter modal");

		log.info("checking listed info");
		soft.assertEquals(rowInfo.get("Plugin"), modal.getPluginSelect().getSelectedValue(), "Value for PLUGIN is the same in grid and modal");
		soft.assertEquals(rowInfo.get("From"), modal.getFromInput().getText(), "Value for FROM is the same in grid and modal");
		soft.assertEquals(rowInfo.get("To"), modal.getToInput().getText(), "Value for TO is the same in grid and modal");
		soft.assertEquals(rowInfo.get("Action"), modal.getActionInput().getText(), "Value for ACTION is the same in grid and modal");
		soft.assertEquals(rowInfo.get("Service"), modal.getServiceInput().getText(), "Value for SERVICE is the same in grid and modal");

//		Delete created filter
		log.info("deleting the created filter");
		rest.messFilters().deleteMessageFilter(actionName, null);

		soft.assertAll();
	}

    /* EDELIVERY-5098 - MSGF-25 - Perform two action and press cancel */
	@Test(description = "MSGF-25", groups = {"multiTenancy", "singleTenancy"})
	public void twoActionsAndCancel() throws Exception {
		List<String> actionNames = new ArrayList<>();
		log.info("create 5 filters for the shuffle");
		for (int i = 0; i < 5; i++) {
			String actionName = Gen.randomAlphaNumeric(5);
			rest.messFilters().createMessageFilter(actionName, null);
			actionNames.add(actionName);
		}

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = navigateToPage();

		List<HashMap<String, String>> allRowInfo = page.grid().getAllRowInfo();

		log.info("Switch row 0 and row 1");
		page.grid().selectRow(1);
		page.getMoveUpBtn().click();

		log.info("Edit filter with action " + actionNames.get(0));
		page.grid().scrollToAndDoubleClick("Action", actionNames.get(0));
		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill("newActionValue1");
		modal.getOkBtn().click();

		log.info("Cancel changes");
		page.cancelChangesAndConfirm();

		log.info("Comparing the new data in the grid with data before the changes");
		List<HashMap<String, String>> newRowInfo = page.grid().getAllRowInfo();
		boolean eq = ListUtils.isEqualList(allRowInfo, newRowInfo);
		soft.assertTrue(eq, "Info before and after the changes is the same");

		log.info("Delete the created filters");
		for (int i = 0; i < actionNames.size(); i++) {
			rest.messFilters().deleteMessageFilter(actionNames.get(i), null);
		}
		soft.assertAll();
	}

    /* EDELIVERY-5099 - MSGF-26 - Add duplicate Message Filter with blank From,To,Action  Service */
	@Test(description = "MSGF-26", groups = {"multiTenancy", "singleTenancy"})
	public void duplicateEmptyFilter() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("checking if there are any empty message filters");
		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		String pluginName = "";
		for (int i = 0; i < msgfs.length(); i++) {
			JSONObject msgf = msgfs.getJSONObject(i);
			if (msgf.getJSONArray("routingCriterias").length() == 0) {
				pluginName = msgf.getString("backendName");
				break;
			}
		}

		MessageFilterPage page = navigateToPage();

		if (StringUtils.isEmpty(pluginName)) {
			log.info("Try to create empty filter");
			page.getNewBtn().click();
			MessageFilterModal modal = new MessageFilterModal(driver);
			pluginName = modal.getPluginSelect().getSelectedValue();
			modal.clickOK();
			page.saveAndConfirmChanges();
		}


		log.info("Try to create empty filter");
		page.getNewBtn().click();
		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getPluginSelect().selectOptionByText(pluginName);
		modal.clickOK();

		log.info("checking listed error");
		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MESSAGE_FILTER_DUPLICATE_FILTER, "Page shows error");

		log.info("checking button state");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled");

		soft.assertAll();
	}

    /* EDELIVERY-5100 - MSGF-27 - Add duplicate message filter with data in all fields */
	@Test(description = "MSGF-27", groups = {"multiTenancy", "singleTenancy"})
	public void duplicateFilter() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = navigateToPage();
		page.getNewBtn().click();

		MessageFilterModal modal = new MessageFilterModal(driver);
		log.info("creating filter");
		String generatedStr = Gen.randomAlphaNumeric(5) + ":" + Gen.randomAlphaNumeric(5);
		modal.getPluginSelect().selectOptionByIndex(0);
		modal.getFromInput().fill(generatedStr);
		modal.getToInput().fill(generatedStr);
		modal.getActionInput().fill(generatedStr);
		modal.getServiceInput().fill(generatedStr);

		modal.clickOK();

		page.grid().waitForRowsToLoad();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		page.grid().waitForRowsToLoad();


		page.getNewBtn().click();
		log.info("creating the same filter");
		modal = new MessageFilterModal(driver);
		soft.assertTrue(modal.isLoaded(), "New button opens the new/edit message filter modal");
		modal.getPluginSelect().selectOptionByIndex(0);
		modal.getFromInput().fill(generatedStr);
		modal.getToInput().fill(generatedStr);
		modal.getActionInput().fill(generatedStr);
		modal.getServiceInput().fill(generatedStr);

		modal.clickOK();

		log.info("checking listed error");
		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MESSAGE_FILTER_DUPLICATE_FILTER, "Page shows error");

		log.info("checking buttons state");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled");


//		Delete created filter
		log.info("deleting created filter");
		rest.messFilters().deleteMessageFilter(generatedStr, null);

		soft.assertAll();
	}


    /* EDELIVERY-5101 - MSGF-28 - Create a duplicate by editing another filter */
	@Test(description = "MSGF-28", groups = {"multiTenancy", "singleTenancy"})
	public void editToDuplicate() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Create 2 filters to edit");

		String actionName = Gen.randomAlphaNumeric(5);
		String anotherActionName = Gen.randomAlphaNumeric(5);
		rest.messFilters().createMessageFilter(actionName, null);
		rest.messFilters().createMessageFilter(anotherActionName, null);

		MessageFilterPage page = navigateToPage();

		int index = page.grid().scrollTo("Action", actionName);
		page.grid().selectRow(index);
		page.getEditBtn().click();

		log.info("editing first filter to match the second");
		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill(anotherActionName);

		modal.clickOK();

		log.info("checking listed error");
		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MESSAGE_FILTER_DUPLICATE_FILTER, "Page shows error");

		log.info("checking buttons state");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled");

		soft.assertAll();


//		Delete created filter
		log.info("deleting created filters");
		rest.messFilters().deleteMessageFilter(actionName, null);
		rest.messFilters().deleteMessageFilter(anotherActionName, null);

		soft.assertAll();
	}

    /* EDELIVERY-5102 - MSGF-29 - Try to uncheck Persisted Field check box for one Message filter */
	@Test(description = "MSGF-29", groups = {"multiTenancy"})
	public void persistedCheckbox() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = navigateToPage();

		MessageFilterGrid grid = page.grid();
		log.info("check persisted checkbox cannot be edited by the user");
		for (int i = 0; i < grid.getRowsNo(); i++) {
			soft.assertTrue(!grid.getPersistedChckElem(i).isEnabled(), "Persisted checkbox is disabled for all rows " + i);
		}

		soft.assertAll();
	}

    /* EDELIVERY-5104 - MSGF-31 - Verify headers in downloaded CSV sheet  */
	@Test(description = "MSGF-31", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = navigateToPage();
		String fileName = page.pressSaveCsvAndSaveFile();
		log.info("downloaded file " + fileName);
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	//	Delete a message filter and don’t press Save or Cancel,click on Export as CSV
    /* EDELIVERY-5094 - MSGF-21 - Delete a message filter and dont press Save or Cancel,click on Export as CSV */
	@Test(description = "MSGF-21", groups = {"multiTenancy", "singleTenancy"})
	public void exportCsvOnUnsavedFilterDelAction() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Customized location for download");
		String filePath = data.downloadFolderPath();

		log.info("Clean given directory");
		FileUtils.cleanDirectory(new File(filePath));

		String domain = selectRandomDomain();

		log.info("Create a filter to delete");
		String actionName = Gen.randomAlphaNumeric(5);
		rest.messFilters().createMessageFilter(actionName, domain);

		MessageFilterPage page = navigateToPage();

		int index = page.grid().scrollTo("Action", actionName);
		if (index < 0) {
			throw new RuntimeException("Could not find created filter");
		}

		log.info("deleting filter");
		page.grid().selectRow(index);
		page.getDeleteBtn().click();

		log.info("Click on download csv button");
		page.getSaveCSVButton().click();

		log.info("Confirm cancel all changes");
		new Dialog(driver).cancel();

		log.info("Wait for download to complete");
		log.info("Check if file is downloaded at given location");
		for (int i = 0; i < 10; i++) {
			if (!DFileUtils.isFileDownloaded(filePath)) {
				page.wait.forFileToBeDownloaded(filePath);
			} else {
				break;
			}
		}

		if (!DFileUtils.isFileDownloaded(filePath)) {
			throw new Exception("Could not find file");
		}

		String filename = DFileUtils.getCompleteFileName(data.downloadFolderPath());

		page.refreshPage();
		page.grid().waitForRowsToLoad();

		int csvRowCount = DFileUtils.getRowCount(filename) - 1;
		int gridRowCount = page.grid().getPagination().getTotalItems();

		soft.assertTrue(csvRowCount == gridRowCount, "Newly added record should be present at both places");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is not enabled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is not enabled");

		soft.assertAll();

	}

	//
    /* EDELIVERY-5095 - MSGF-22 - Click on Edit icon for row when selection is done for another row */
	@Test(description = "MSGF-22", groups = {"multiTenancy", "singleTenancy"})
	public void editWithDiffRowSelection() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		log.info("Generate value for action field");
		String actionName = Gen.randomAlphaNumeric(5);
		String actionName1 = Gen.randomAlphaNumeric(5);
		String actionName2 = Gen.randomAlphaNumeric(5);

		log.info("create new message filter using rest call");
		rest.messFilters().createMessageFilter(actionName, domain);
		rest.messFilters().createMessageFilter(actionName1, domain);

		MessageFilterPage page = navigateToPage();

		log.info("Identify row number for created filters");
		int index1 = page.grid().scrollTo("Action", actionName1);

		log.info("Select row for first filter");
		int index = page.grid().scrollToAndSelect("Action", actionName);

		log.info("Click on edit icon for second filter");
		page.grid().rowEdit(index1);
		MessageFilterModal modal = new MessageFilterModal(driver);

		log.info("Verify edit pop up field values as per second filter values");
		soft.assertTrue(modal.getActionInput().getText().equalsIgnoreCase(actionName1));
		log.info("editing action value");
		modal.getActionInput().fill(actionName2);
		modal.clickOK();
		log.info("Check status for save button");
		soft.assertTrue(page.getSaveBtn().isEnabled());
		soft.assertTrue(page.getCancelBtn().isEnabled());

		log.info("Click on save button then ok for confirmation");
		page.saveAndConfirmChanges();
		soft.assertTrue(page.grid().scrollTo("Action", actionName2) == index1);
		soft.assertAll();

	}

    /* EDELIVERY-5096 - MSGF-23 - Click on Move upmove down for row when selection is done for another row */
	@Test(description = "MSGF-23", groups = {"multiTenancy", "singleTenancy"})
	public void moveUpWithDiffRowSelection() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		log.info("Generate random values for action field");
		String actionName = Gen.randomAlphaNumeric(5);
		String actionName1 = Gen.randomAlphaNumeric(5);

		log.info("Create two filters using rest call");
		rest.messFilters().createMessageFilter(actionName, domain);
		rest.messFilters().createMessageFilter(actionName1, domain);

		MessageFilterPage page = navigateToPage();


		log.info("Identify row number for added filters");
		int index = page.grid().scrollTo("Action", actionName);
		int index1 = page.grid().scrollTo("Action", actionName1);

		log.info("Select first filter row");
		page.grid().selectRow(index);

		log.info("Click on move up icon for second row");
		page.grid().rowMoveUp(index1);

		log.info("Check status for move up and move down button for selected row");
		soft.assertTrue(page.getMoveUpBtn().isEnabled());
		soft.assertTrue(page.getMoveDownBtn().isEnabled());

		log.info("Click on save then ok ");
		page.saveAndConfirmChanges();
		page.grid().waitForRowsToLoad();

		log.info("Verify row number for both filters after move operation");
		soft.assertTrue(page.grid().scrollTo("Action", actionName) == index + 1);
		soft.assertTrue(page.grid().scrollTo("Action", actionName1) == index1 - 1);
		soft.assertAll();
	}


    /* EDELIVERY-6122 - MSGF-32 - Click on Delete icon for row when selection is done for another row */
	@Test(description = "MSGF-32", groups = {"multiTenancy", "singleTenancy"})
	public void delWithDiffRowSelection() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		log.info("Generate random values for action name");
		String actionName = Gen.randomAlphaNumeric(5);
		String actionName1 = Gen.randomAlphaNumeric(5);

		rest.messFilters().createMessageFilter(actionName, domain);
		rest.messFilters().createMessageFilter(actionName1, domain);

		MessageFilterPage page = navigateToPage();

		log.info("Identify row number for added filters");
		int index = page.grid().scrollTo("Action", actionName);
		int index1 = page.grid().scrollTo("Action", actionName1);

		log.info("select row for first filter");
		page.grid().selectRow(index);
		log.info("Click on delete icon of second filter");
		page.grid().rowDelete(index1);

		log.info("Check status for save button");
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is active after pressing delete");

		log.info("Check status for cancel button");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Delete button is active after pressing delete");

		log.info("save changes ");
		page.saveAndConfirmChanges();

		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MESSAGE_FILTER_SUCCESS, "Success messsage is displayed");

		log.info("Check non presence of second filter in grid");
		soft.assertTrue(page.grid().scrollTo("Action", actionName1) < 0);

		log.info("Check presence of first filter in grid");
		soft.assertTrue(page.grid().scrollTo("Action", actionName) > 0);
		soft.assertAll();

	}


    /* EDELIVERY-6123 - MSGF-33 - Click on Move down for row when selection is done for another row */
	@Test(description = "MSGF-33", groups = {"multiTenancy", "singleTenancy"})
	public void moveDownWithDiffRowSelection() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		log.info("Generate random values for action field");
		String actionName = Gen.randomAlphaNumeric(5);
		String actionName1 = Gen.randomAlphaNumeric(5);

		log.info("Create two filters using rest call");
		rest.messFilters().createMessageFilter(actionName, domain);
		rest.messFilters().createMessageFilter(actionName1, domain);

		MessageFilterPage page = navigateToPage();


		log.info("Identify row number for added filters");
		int index = page.grid().scrollTo("Action", actionName);
		int index1 = page.grid().scrollTo("Action", actionName1);

		MessageFilterGrid grid = page.grid();
		int rowCount = grid.getRowsNo();

		HashMap<String, String> nextToLast = page.grid().getRowInfo(rowCount - 2);
		HashMap<String, String> last = page.grid().getRowInfo(rowCount - 1);


		log.info("Select first filter row");
		page.grid().selectRow(rowCount - 1);

		log.info("Click on move up icon for second row");
		page.grid().rowMoveDown(rowCount - 2);

		log.info("Click on save then ok ");
		page.saveAndConfirmChanges();

		log.info("Verify row number for both filters after move operation");
		soft.assertEquals(nextToLast, page.grid().getRowInfo(rowCount - 1), "Next to last row is now last");
		soft.assertEquals(last, page.grid().getRowInfo(rowCount - 2), "Last row is now next to last");


		soft.assertAll();
	}

    /* EDELIVERY-5103 - MSGF-30 - Change Plugin on Message Filter Edit */
	@Test(description = "MSGF-30", groups = {"multiTenancy", "singleTenancy"})
	public void changeMsgFilterOnUpdate() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		String actionName = Gen.randomAlphaNumeric(5);
		rest.messFilters().createMessageFilter(actionName, domain);

		MessageFilterPage page = navigateToPage();

		int index = page.grid().scrollTo("Action", actionName);
		HashMap<String, String> info = page.grid().getRowInfo(index);

		log.info("Extract row number plugin having action name :" + actionName);
		page.grid().selectRow(index);

		soft.assertTrue(page.getEditBtn().isEnabled(), "On row selection, button gets enabled");
		log.info("Click on Edit button");
		page.getEditBtn().click();

		log.info("Check number of plugins available on edit pop in plugin drop down");
		MessageFilterModal popup = new MessageFilterModal(driver);

		List<String> plugins = popup.getPluginSelect().getOptionsTexts();
		if (plugins.size() > 1) {

			log.info("Select another plugin");
			for (String plugin : plugins) {
				if (!plugin.equalsIgnoreCase(info.get("Plugin"))) {
					popup.getPluginSelect().selectOptionByText(plugin);
					break;
				}
			}

			log.info("Enter action field data");
			popup.getActionInput().fill(Gen.randomAlphaNumeric(3));

			log.info("Click on Ok button");
			popup.clickOK();

			log.info("Click on Save button");
			page.saveAndConfirmChanges();

			soft.assertFalse(page.getAlertArea().isError(), "Success message is shown");
			soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MESSAGE_FILTER_SUCCESS, "Correct message is displayed");

			soft.assertTrue(page.grid().scrollTo("Action", info.get("Action")) < 0, "Old action name is not present in the grid anymore");

		} else {
			throw new SkipException("Only one plugin found, this test is skipped");
		}
		soft.assertAll();
	}


    /* EDELIVERY-7184 - MSGF-34 - Verify downloaded CSV file against data in the grid  */
	@Test(description = "MSGF-34", groups = {"multiTenancy", "singleTenancy"})
	public void downloadAsCSV() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = navigateToPage();

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		String filePath = page.pressSaveCsvAndSaveFile();

		log.info("Check if file is downloaded at given location");
		soft.assertTrue(new File(filePath).exists(), "File is downloaded successfully");

		log.info("Compare headers from downloaded csv and grid");
		page.grid().checkCSVvsGridHeaders(filePath, soft);


		soft.assertAll();
	}

    /* EDELIVERY-7185 - MSGF-35 - Sort the grid */
	@Test(description = "MSGF-35", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = navigateToPage();

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		log.info("Check default sorted column");
		soft.assertNull(grid.getSortedColumnName(), "Grid is not sortable and no column is marked as sorted by default");

		grid.sortBy("Plugin");

		log.info("Check sorted column name after sorting attempt");
		soft.assertNull(grid.getSortedColumnName(), "Grid is not sortable and no column is marked as sorted ");


		soft.assertAll();
	}


}
