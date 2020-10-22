package domibus.ui.functional;

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


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class MessageFilterPgTest extends SeleniumTest {
	
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
	
	/* Login as super admin and open Messages Filter page */
	@Test(description = "MSGF-1", groups = {"multiTenancy", "singleTenancy"})
	public void openMessagesFilterPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();
		
		log.info("checking that all expected elements appear");
		soft.assertTrue(page.isLoaded(), "All elements are loaded");
		soft.assertAll();
		
	}
	
	/* Create new filter and press Cancel */
	@Test(description = "MSGF-3", groups = {"multiTenancy", "singleTenancy"})
	public void newFilterCancel() throws Exception {
		String actionName = Gen.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();
		
		page.getNewBtn().click();
		MessageFilterModal popup = new MessageFilterModal(driver);
		popup.getPluginSelect().selectOptionByIndex(0);
		popup.actionInput.sendKeys(actionName);
		popup.clickOK();
		log.info("created new filter with action " + actionName);
		
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is active after new Message Filter was created");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is active after new Message Filter was created");
		
		page.cancelChangesAndConfirm();
		log.info("canceled the changes");
		
		soft.assertTrue(page.grid().scrollTo("Action", actionName) == -1, "New filter is NOT present in the grid");
		soft.assertTrue(page.getSaveBtn().isDisabled(), "Save button is disabled after changes are canceled");
		soft.assertTrue(page.getCancelBtn().isDisabled(), "Cancel button is disabled after changes are canceled");
		
		soft.assertAll();
	}
	
	/*User creates new filter and presses Save*/
	@Test(description = "MSGF-4", groups = {"multiTenancy", "singleTenancy"})
	public void newFilterSave() throws Exception {
		String actionName = Gen.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();
		
		page.getNewBtn().click();
		MessageFilterModal popup = new MessageFilterModal(driver);
		popup.getPluginSelect().selectOptionByIndex(0);
		popup.actionInput.sendKeys(actionName);
		popup.clickOK();
		log.info("created new filter with action " + actionName);
		
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is active after new Message Filter was created");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is active after new Message Filter was created");
		
		page.saveAndConfirmChanges();
		log.info("saved the changes");
		
		soft.assertTrue(page.grid().scrollTo("Action", actionName) > -1, "New filter is present in the grid");
		soft.assertTrue(page.getSaveBtn().isDisabled(), "Save button is disabled after changes are saved");
		soft.assertTrue(page.getCancelBtn().isDisabled(), "Cancel button is disabled after changes are saved");
		
		soft.assertAll();
	}
	
	/*User shuffles filters using Move Up and Move Down buttons and presses Cancel*/
	@Test(description = "MSGF-5", groups = {"multiTenancy", "singleTenancy"})
	public void shuffleAndCancel() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		List<String> actionNames = new ArrayList<>();
		log.info("create 5 filters for the shuffle");
		for (int i = 0; i < 5; i++) {
			String actionName = Gen.randomAlphaNumeric(5);
			rest.messFilters().createMessageFilter(actionName, null);
			actionNames.add(actionName);
		}
		
		
		MessageFilterPage page = navigateToPage();
		
		log.info("Switch row 0 and row 1");
		page.grid().selectRow(1);
		HashMap<String, String> row1 = page.grid().getRowInfo(1);
		HashMap<String, String> row0 = page.grid().getRowInfo(0);
		page.getMoveUpBtn().click();
		HashMap<String, String> newRow0 = page.grid().getRowInfo(0);
		soft.assertEquals(row1.get("Action"), newRow0.get("Action"), "The row that was previously on position 1 is now on first position");
		
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled");
		
		log.info("Cancel changes");
		page.cancelChangesAndConfirm();
		page.grid().waitForRowsToLoad();
		
		HashMap<String, String> oldRow0 = page.grid().getRowInfo(0);
		
		soft.assertEquals(row0.get("Action"), oldRow0.get("Action"),
				"The row that was previously on position 0 is now on first position again after Cancel");
		
		log.info("Delete the created filters");
		for (int i = 0; i < actionNames.size(); i++) {
			rest.messFilters().deleteMessageFilter(actionNames.get(i), null);
		}
		soft.assertAll();
	}
	
	/* User selects first row */
	@Test(description = "MSGF-7", groups = {"multiTenancy", "singleTenancy"})
	public void selectFirstRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();
		
		log.info("selecting row 0");
		page.grid().selectRow(0);
		
		log.info("checking buttons after row select");
		soft.assertTrue(!page.getMoveUpBtn().isEnabled(), "Move up button is NOT enabled for the first row");
		soft.assertTrue(page.getMoveDownBtn().isEnabled(), "Move down button is enabled for the first row");
		
		soft.assertTrue(page.getEditBtn().isEnabled(), "Edit button is enabled for the first row");
		soft.assertTrue(page.getDeleteBtn().isEnabled(), "Delete button is enabled for the first row");
		
		soft.assertAll();
	}
	
	/* User selects last row */
	@Test(description = "MSGF-8", groups = {"multiTenancy", "singleTenancy"})
	public void selectLastRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();
		
		log.info("selecting last row");
		int lastRowIndex = page.grid().getRowsNo() - 1;
		page.grid().selectRow(lastRowIndex);
		
		log.info("checking buttons after row select");
		soft.assertTrue(page.getMoveUpBtn().isEnabled(), "Move up button is NOT enabled for the last row");
		soft.assertTrue(!page.getMoveDownBtn().isEnabled(), "Move down button is enabled for the last row");
		
		soft.assertTrue(page.getEditBtn().isEnabled(), "Edit button is enabled for the last row");
		soft.assertTrue(page.getDeleteBtn().isEnabled(), "Delete button is enabled for the last row");
		
		soft.assertAll();
	}
	
	/* User selects row other than first and last */
	@Test(description = "MSGF-9", groups = {"multiTenancy", "singleTenancy"})
	public void selectMiddleRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		List<String> actionNames = new ArrayList<>();
		log.info("create 5 filters");
		for (int i = 0; i < 5; i++) {
			String actionName = Gen.randomAlphaNumeric(5);
			rest.messFilters().createMessageFilter(actionName, null);
			actionNames.add(actionName);
		}
		
		
		MessageFilterPage page = navigateToPage();
		
		log.info("selecting middle row");
		int rowIndex = page.grid().getRowsNo() / 2;
		page.grid().selectRow(rowIndex);
		
		log.info("checking buttons after row select");
		soft.assertTrue(page.getMoveUpBtn().isEnabled(), "Move up button is enabled for the middle row");
		soft.assertTrue(page.getMoveDownBtn().isEnabled(), "Move down button is enabled for the middle row");
		
		soft.assertTrue(page.getEditBtn().isEnabled(), "Edit button is enabled for the middle row");
		soft.assertTrue(page.getDeleteBtn().isEnabled(), "Delete button is enabled for middle last row");
		
		log.info("Delete the created filters");
		for (int i = 0; i < actionNames.size(); i++) {
			rest.messFilters().deleteMessageFilter(actionNames.get(i), null);
		}
		
		soft.assertAll();
	}
	
	/* User click on Move up for any row other than first */
	@Test(description = "MSGF-10", groups = {"multiTenancy", "singleTenancy"})
	public void moveUpAndDown() throws Exception {
		List<String> actionNames = new ArrayList<>();
		log.info("create 5 filters for the shuffle");
		for (int i = 0; i < 5; i++) {
			String actionName = Gen.randomAlphaNumeric(5);
			rest.messFilters().createMessageFilter(actionName, null);
			actionNames.add(actionName);
		}
		
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();

//		move up
		int index = page.grid().getRowsNo() / 2;
		HashMap<String, String> prevInfo = page.grid().getRowInfo(index - 1);
		HashMap<String, String> rowInfo = page.grid().getRowInfo(index);
		HashMap<String, String> nextInfo = page.grid().getRowInfo(index + 1);
		
		log.info("moving filter up");
		page.grid().selectRow(index);
		page.getMoveUpBtn().click();
		
		HashMap<String, String> newPrevInfo = page.grid().getRowInfo(index - 1);
		HashMap<String, String> newRowInfo = page.grid().getRowInfo(index);
		HashMap<String, String> newNextInfo = page.grid().getRowInfo(index + 1);
		
		log.info("checking that rows have swapped position as expected");
		soft.assertEquals(newPrevInfo, rowInfo, "Selected row has moved up one position");
		soft.assertEquals(newNextInfo, nextInfo, "Row bellow was not affected");
		soft.assertEquals(newRowInfo, prevInfo, "Row above has moved down one position");
		
		log.info("checking the row is still selected");
		soft.assertTrue(page.grid().getSelectedRowIndex() == index - 1, "Row is still selected at new position");
		log.info("checking buttons after row select");
		soft.assertTrue(page.getMoveUpBtn().isEnabled(), "Move up button is enabled for the middle row");
		soft.assertTrue(page.getMoveDownBtn().isEnabled(), "Move down button is enabled for the middle row");
		
		soft.assertTrue(page.getEditBtn().isEnabled(), "Edit button is enabled for the middle row");
		soft.assertTrue(page.getDeleteBtn().isEnabled(), "Delete button is enabled for middle last row");

//		move down
		prevInfo = page.grid().getRowInfo(index - 1);
		rowInfo = page.grid().getRowInfo(index);
		nextInfo = page.grid().getRowInfo(index + 1);
		
		log.info("moving filter down");
		page.grid().selectRow(index);
		page.getMoveDownBtn().click();
		
		newPrevInfo = page.grid().getRowInfo(index - 1);
		newRowInfo = page.grid().getRowInfo(index);
		newNextInfo = page.grid().getRowInfo(index + 1);
		
		log.info("checking that rows have swapped position as expected");
		soft.assertEquals(newNextInfo, rowInfo, "Selected row has moved down one position");
		soft.assertEquals(prevInfo, newPrevInfo, "Row above was not affected");
		soft.assertEquals(nextInfo, newRowInfo, "Row below has moved up one position");
		
		log.info("checking the row is still selected");
		soft.assertTrue(page.grid().getSelectedRowIndex() == index + 1, "Row is still selected at new position");
		log.info("checking buttons after row select");
		soft.assertTrue(page.getMoveUpBtn().isEnabled(), "Move up button is enabled for the middle row");
		soft.assertTrue(page.getMoveDownBtn().isEnabled(), "Move down button is enabled for the middle row");
		
		soft.assertTrue(page.getEditBtn().isEnabled(), "Edit button is enabled for the middle row");
		soft.assertTrue(page.getDeleteBtn().isEnabled(), "Delete button is enabled for middle last row");
		
		for (int i = 0; i < actionNames.size(); i++) {
			rest.messFilters().deleteMessageFilter(actionNames.get(i), null);
		}
		log.info("deleted the created filters");
		
		soft.assertAll();
	}
	
	/* User reshuffles filters using Move Up and Move Down buttons and presses Save */
	@Test(description = "MSGF-11", groups = {"multiTenancy", "singleTenancy"})
	public void shuffleAndSave() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		List<String> actionNames = new ArrayList<>();
		log.info("create 5 filters for the shuffle");
		for (int i = 0; i < 5; i++) {
			String actionName = Gen.randomAlphaNumeric(5);
			rest.messFilters().createMessageFilter(actionName, null);
			actionNames.add(actionName);
		}
		
		MessageFilterPage page = navigateToPage();
		
		int index = page.grid().scrollTo("Action", actionNames.get(0));
		
		if (index <= 0 || index == page.grid().getRowsNo()) {
			index = page.grid().scrollTo("Action", actionNames.get(1));
		}
		
		page.grid().selectRow(index);
		log.info("selected row with index " + index);
		
		HashMap<String, String> row = page.grid().getRowInfo(index);
		
		log.info("pressing moveUP button");
		page.getMoveUpBtn().click();
		
		log.info("check filter in position index-1");
		HashMap<String, String> newRow = page.grid().getRowInfo(index - 1);
		soft.assertEquals(row.get("Action"), newRow.get("Action"), "The row was moved up by one position");
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled");
		
		log.info("Saving");
		page.saveAndConfirmChanges();
		
		log.info("check filter in position index-1");
		HashMap<String, String> rowAfterSave = page.grid().getRowInfo(index - 1);
		soft.assertEquals(rowAfterSave.get("Action"), row.get("Action"),
				"After the save the row is still on one position higher than before");
		
		for (int i = 0; i < actionNames.size(); i++) {
			rest.messFilters().deleteMessageFilter(actionNames.get(i), null);
		}
		log.info("deleted the created filters");
		
		soft.assertAll();
	}
	
	/*User selects a filter and chooses to edit it then presses Cancel*/
	@Test(description = "MSGF-12", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void editAndCancel() throws Exception {
		log.info("Create a filter to edit");
		String actionName = Gen.randomAlphaNumeric(5);
		rest.messFilters().createMessageFilter(actionName, null);
		
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();
		
		
		MessageFilterGrid grid = page.grid();
		int index = grid.scrollTo("Action", actionName);
		HashMap<String, String> rowInfo = grid.getRowInfo(index);
		page.grid().selectRow(index);
		
		log.info("editing row");
		page.getEditBtn().click();
		MessageFilterModal modal = new MessageFilterModal(driver);
		log.info("editing action value");
		modal.getActionInput().fill("newActionValue");
		modal.clickOK();
		
		log.info("canceling changes");
		page.cancelChangesAndConfirm();
		
		log.info("action value after cancel is the same as the one before editing");
		HashMap<String, String> newRowInfo = grid.getRowInfo(index);
		soft.assertEquals(rowInfo.get("Action"), newRowInfo.get("Action"), "Edited values are reset after canceling changes");

//		Delete created filter
		log.info("delete created filter");
		rest.messFilters().deleteMessageFilter(actionName, null);
		
		soft.assertAll();
	}
	
	/* User selects a filter and chooses to edit it then press save */
	@Test(description = "MSGF-14", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void editAndSave() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("create a filter to edit");
		String actionName = Gen.randomAlphaNumeric(5);
		String newActionValue = Gen.randomAlphaNumeric(5);
		rest.messFilters().createMessageFilter(actionName, null);
		
		MessageFilterPage page = navigateToPage();
		
		log.info("editing filter");
		int index = page.grid().scrollTo("Action", actionName);
		page.grid().selectRow(index);
		page.getEditBtn().click();
		
		log.info("changing action value");
		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill(newActionValue);

//		necessary because somehow typing doesn't finish the word otherwise
		modal.wait.forXMillis(200);
		
		modal.clickOK();
		log.info("saving changes");
		page.saveAndConfirmChanges();
		
		HashMap<String, String> row = page.grid().getRowInfo(index);
		soft.assertEquals(row.get("Action"), newActionValue, "Edited values are saved");

//		Delete created filter
		log.info("delete created filter");
		rest.messFilters().deleteMessageFilter(newActionValue, null);
		
		soft.assertAll();
	}
	
	/* User chooses to delete a filter and presses Cancel */
	@Test(description = "MSGF-15", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndCancel() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Create a filter to delete");
		String actionName = Gen.randomAlphaNumeric(5);
		rest.messFilters().createMessageFilter(actionName, null);
		
		MessageFilterPage page = navigateToPage();
		
		
		int index = page.grid().scrollTo("Action", actionName);
		if (index < 0) {
			throw new SkipException("Could not find created filter");
		}
		
		log.info("deleting filter");
		page.grid().selectRow(index);
		page.getDeleteBtn().click();
		
		log.info("checking the row was deleted from the grid");
		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index == -1, "Filter not found in grid after delete");
		
		log.info("canceling changes");
		page.cancelChangesAndConfirm();
		
		log.info("checking the row was restored in the grid");
		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index > -1, "Filter found in grid after Cancel");

//		Delete created filter
		log.info("cleanup the filter");
		rest.messFilters().deleteMessageFilter(actionName, null);
		
		soft.assertAll();
	}
	
	/* User chooses to delete a filter and presses Save */
	@Test(description = "MSGF-17", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndSave() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Create a filter to edit");
		String actionName = Gen.randomAlphaNumeric(5);
		rest.messFilters().createMessageFilter(actionName, null);
		
		
		MessageFilterPage page = navigateToPage();
		
		int index = page.grid().scrollTo("Action", actionName);
		if (index < 0) {
			throw new SkipException("Could not find created filter");
		}
		
		log.info("deleting filter");
		page.grid().selectRow(index);
		page.getDeleteBtn().click();
		
		log.info("checking the row was removed from the grid");
		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index == -1, "Filter not found in grid after delete");
		
		log.info("saving changes");
		page.saveAndConfirmChanges();
		
		log.info("checking that row is still removed from the list after save");
		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index == -1, "Filter found in grid after Save");
		
		soft.assertAll();
		
	}
	
	/* Create new filter on default domain and change domains */
	@Test(description = "MSGF-18", groups = {"multiTenancy"})
	public void newFilterAndChangeDomains() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String actionName = Gen.randomAlphaNumeric(5);
		
		MessageFilterPage page = navigateToPage();
		
		page.getNewBtn().click();
		MessageFilterModal popup = new MessageFilterModal(driver);
//		popup.getPluginSelect().selectOptionByIndex(0);
		popup.actionInput.sendKeys(actionName);
		popup.serviceInput.sendKeys(actionName+":"+actionName);
		popup.clickOK();
		log.info("created new filter with action" + actionName);
		
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is active after new Message Filter was created");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is active after new Message Filter was created");
		
		page.saveAndConfirmChanges();
		log.info("saved the changes");
		soft.assertFalse(page.getAlertArea().isError(), "Success message is shown!!");
		
		log.info("check if filter is present");
		soft.assertTrue(page.grid().scrollTo("Action", actionName) > -1, "New filter is present in the grid");
		
		log.info("changing domain to 1");
		page.getDomainSelector().selectOptionByText(rest.getNonDefaultDomain());
		page.getDomainSelector().selectOptionByText(rest.getNonDefaultDomain());
		page.grid().waitForRowsToLoad();
		
		log.info("check if filter is NOT present");
		soft.assertTrue(page.grid().scrollTo("Action", actionName) == -1, "New filter is NOT present in the grid on other domains then default");
		
		log.info("delete created filter");
		rest.messFilters().deleteMessageFilter(actionName, null);
		soft.assertAll();
	}
	
	/* Operate a change in the list of filters and don't press Save or Cancel Change domain */
	@Test(description = "MSGF-19", groups = {"multiTenancy"}, enabled = false)
	public void editAndChangeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		log.info("Create a filter to edit");
		String actionName = Gen.randomAlphaNumeric(5);
		String anotherActionName = Gen.randomAlphaNumeric(5) + "mod";
		rest.messFilters().createMessageFilter(actionName, null);
		log.debug("filter with action " + actionName + " created");
		String domainName = rest.getNonDefaultDomain();
		
		MessageFilterPage page = navigateToPage();
		String defaultDomainName = page.getDomainFromTitle();
		
		int index = page.grid().scrollTo("Action", actionName);
		page.grid().selectRow(index);
		page.getEditBtn().click();
		
		log.info("editing the message filter");
		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill(anotherActionName);
		modal.wait.forXMillis(150);
		modal.clickOK();
		modal.wait.forXMillis(150);
		
		log.info("check that new value is present in grid");
		String listedAction = page.grid().getRowInfo(index).get("Action");
		soft.assertEquals(listedAction, anotherActionName, "Action is changed after edit form is closed");
		
		try {
			log.info("changing domain");
			page.getDomainSelector().selectOptionByText(domainName);
		} catch (Exception e) {
			soft.assertEquals(page.getDomainFromTitle().toLowerCase(), defaultDomainName, "Domain has not changed while changes are not saved");
		}
		
		log.info("check that cancel all changes dialog appears");
		Dialog dialog = new Dialog(driver);
		soft.assertTrue(dialog.isLoaded(), "Dialog is shown");
		soft.assertEquals(dialog.getMessage(), DMessages.DIALOG_CANCEL_ALL, "Dialog shows correct message");
		log.info("confirm cancel all changes");
		dialog.confirm();
		
		soft.assertEquals(page.getDomainSelector().getSelectedValue(), domainName, "Domain was changed");
		
		try {
			log.info("change domain back to default");
			page.getDomainSelector().selectOptionByText(defaultDomainName);
		} catch (Exception e) {
			soft.assertEquals(page.getDomainFromTitle().toLowerCase(), defaultDomainName, "Domain has not changed while changes are not saved");
		}
		
		log.info("check that changes were canceled");
		listedAction = page.grid().getRowInfo(index).get("Action");
		soft.assertEquals(listedAction, actionName, "Action is not changed after the user presses OK in the dialog");
		soft.assertTrue(page.getSaveBtn().isDisabled(), "Changes are canceled and save button is disabled");
		soft.assertTrue(page.getCancelBtn().isDisabled(), "Changes are canceled and cancel button is disabled");
		
		
		page.grid().selectRow(index);
		page.getEditBtn().click();
		log.info("edit the same filter again");
		modal = new MessageFilterModal(driver);
		modal.getActionInput().fill(anotherActionName);
		modal.wait.forXMillis(150);
		modal.clickOK();
		modal.wait.forXMillis(150);
		
		try {
			log.info("changing domain");
			page.getDomainSelector().selectOptionByText(domainName);
		} catch (Exception e) {
			soft.assertEquals(page.getDomainFromTitle().toLowerCase(), defaultDomainName, "Domain has not changed while changes are not saved");
		}
		
		log.info("check that cancel all changes dialog appears");
		dialog = new Dialog(driver);
		soft.assertTrue(dialog.isLoaded(), "Dialog is shown");
		soft.assertEquals(dialog.getMessage(), DMessages.DIALOG_CANCEL_ALL, "Dialog shows correct message");
		
		log.info("Press cancel in the dialog");
		dialog.cancel();
		log.info("check that the domain is not changed");
		soft.assertEquals(page.getDomainSelector().getSelectedValue(), defaultDomainName, "Domain was NOT changed");
		
		log.info("check info for filter is still updated");
		listedAction = page.grid().getRowInfo(index).get("Action");
		soft.assertEquals(listedAction, anotherActionName, "Action is still changed after the user presses Cancel in the dialog");
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Changes are NOT canceled and save button is enabled");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Changes are NOT canceled and cancel button is enabled");

//		Delete created filter
		log.info("cleanup the filter");
		rest.messFilters().deleteMessageFilter(actionName, null);
		
		soft.assertAll();
	}
	
	/* Download list of messages filters */
	@Test(description = "MSGF-20", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();
		
		String fileName = rest.csv().downloadGrid(RestServicePaths.MESSAGE_FILTERS_CSV, null, null);
		log.info("downloaded file " + fileName);
		page.grid().checkCSVvsGridInfo(fileName, soft);
		
		soft.assertAll();
	}
	
	/* Double click on one message filter */
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
	
	/* Perform two action and press cancel */
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
	
	/* Add duplicate Message Filter with blank From,To,Action & Service */
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
	
	/* Add duplicate message filter with data in all fields */
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
	
	
	/* Create a duplicate by editing another filter */
	@Test(description = "MSGF-28", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
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
	
	/* Try to uncheck Persisted Field check box for one Message filter */
	@Test(description = "MSGF-29", groups = {"multiTenancy"})
	public void persistedCheckbox() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();
		
		MessageFilterGrid grid = page.grid();
		log.info("check persisted checkbox cannot be edited by the user");
		for (int i = 0; i < grid.getRowsNo(); i++) {
			soft.assertTrue(!grid.getPersisted(i).isEnabled(), "Persisted checkbox is disabled for all rows " + i);
		}
		
		soft.assertAll();
	}
	
	/* Verify headers in downloaded CSV sheet  */
	@Test(description = "MSGF-31", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		MessageFilterPage page = navigateToPage();
		String fileName = rest.csv().downloadGrid(RestServicePaths.MESSAGE_FILTERS_CSV, null, null);
		log.info("downloaded file " + fileName);
		page.grid().checkCSVvsGridHeaders(fileName, soft);
		
		soft.assertAll();
	}
	
//	Delete a message filter and don’t press Save or Cancel,click on Export as CSV
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
			if(!DFileUtils.isFileDownloaded(filePath)){
				page.wait.forXMillis(500);
			}else{
				break;
			}
		}
		
		if(!DFileUtils.isFileDownloaded(filePath)){
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
	@Test(description = "MSGF-22", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
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
	
	/* This method will verify plugin change through edit pop up from Message filter page
			   in case of availability of multiple plugins  */
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
				if(!plugin.equalsIgnoreCase(info.get("Plugin"))){
					popup.getPluginSelect().selectOptionByText(plugin);
					break;
				}
			}
			
			log.info("Enter action field data");
			popup.getActionInput().fill(Gen.randomAlphaNumeric(3));
			
			log.info("Click on Ok button");
			popup.clickOK();
//			page.wait.forXMillis(300);
			
			log.info("Click on Save button");
			page.saveAndConfirmChanges();
			
			soft.assertFalse(page.getAlertArea().isError() , "Success message is shown");
			soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MESSAGE_FILTER_SUCCESS, "Correct message is displayed");
			
			soft.assertTrue(page.grid().scrollTo("Action", info.get("Action"))<0 , "Old action name is not present in the grid anymore");
			
		} else {
			throw new SkipException("Only one plugin found, this test is skipped");
		}
		soft.assertAll();
	}







	
}
