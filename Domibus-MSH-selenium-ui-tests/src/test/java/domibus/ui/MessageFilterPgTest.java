package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import pages.msgFilter.MessageFilterGrid;
import pages.msgFilter.MessageFilterModal;
import pages.msgFilter.MessageFilterPage;
import utils.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class MessageFilterPgTest extends BaseTest {

	@BeforeMethod(alwaysRun = true)
	private void login() throws Exception {
		new LoginPage(driver)
				.login(data.getAdminUser());
		new DomibusPage(driver).getSidebar().gGoToPage(PAGES.MESSAGE_FILTER);
	}

	@Test(description = "MSGF-1", groups = {"multiTenancy", "singleTenancy"})
	public void openMessagesFilterPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);

		soft.assertTrue(page.isLoaded(), "All elements are loaded");
		soft.assertAll();

	}

	@Test(description = "MSGF-2", groups = {"multiTenancy", "singleTenancy"})
	public void newFilterSave() throws Exception {
		String actionName = Generator.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		soft.assertTrue(page.isLoaded(), "All elements are loaded");

		page.getNewBtn().click();
		MessageFilterModal popup = new MessageFilterModal(driver);
		popup.getPluginSelect().selectOptionByIndex(0);
		popup.actionInput.sendKeys(actionName);
		popup.clickOK();


		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is active after new Message Filter was created");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is active after new Message Filter was created");

		page.saveAndConfirmChanges();

		soft.assertTrue(page.grid().scrollTo("Action", actionName) > -1, "New filter is present in the grid");

		soft.assertAll();
	}

	@Test(description = "MSGF-3", groups = {"multiTenancy", "singleTenancy"})
	public void cancelNewFilter() throws Exception {
		String actionName = Generator.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		soft.assertTrue(page.isLoaded(), "All elements are loaded");

		page.getNewBtn().click();
		MessageFilterModal popup = new MessageFilterModal(driver);
		popup.getPluginSelect().selectOptionByIndex(0);
		popup.actionInput.sendKeys(actionName);
		popup.clickOK();


		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is active after new Message Filter was created");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is active after new Message Filter was created");

		page.cancelChangesAndConfirm();
		soft.assertTrue(page.grid().scrollTo("Action", actionName) == -1, "New filter is NOT present in the grid");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled after changes are canceled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled after changes are canceled");

		soft.assertAll();
	}

	@Test(description = "MSGF-5", groups = {"multiTenancy", "singleTenancy"})
	public void shuffleAndCancel() throws Exception {
		List<String> actionNames = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String actionName = Generator.randomAlphaNumeric(5);
			rest.createMessageFilter(actionName, null);
			actionNames.add(actionName);
		}

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();
		soft.assertTrue(page.isLoaded(), "All elements are loaded");

		page.grid().selectRow(0);
		soft.assertFalse(page.getMoveUpBtn().isEnabled(), "Button Move Up is not enabled if selected filter is already first");

		page.grid().selectRow(1);
		soft.assertTrue(page.getMoveUpBtn().isEnabled(), "Button Move Up is enabled for the second row");

		HashMap<String, String> row1 = page.grid().getRowInfo(1);
		HashMap<String, String> row0 = page.grid().getRowInfo(0);
		page.getMoveUpBtn().click();
		HashMap<String, String> newRow0 = page.grid().getRowInfo(0);
		soft.assertEquals(row1.get("Action"), newRow0.get("Action"), "The row that was previously on position 1 is now on first position");

		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled");

		page.cancelChangesAndConfirm();
		HashMap<String, String> oldRow0 = page.grid().getRowInfo(0);

		soft.assertEquals(row0.get("Action"), oldRow0.get("Action"),
				"The row that was previously on position 0 is now on first position again after Cancel");

		for (int i = 0; i < actionNames.size(); i++) {
			rest.deleteMessageFilter(actionNames.get(i), null);
		}
		soft.assertAll();
	}

	@Test(description = "MSGF-6", groups = {"multiTenancy", "singleTenancy"})
	public void shuffleAndSave() throws Exception {
		List<String> actionNames = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String actionName = Generator.randomAlphaNumeric(5);
			rest.createMessageFilter(actionName, null);
			actionNames.add(actionName);
		}

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();
		soft.assertTrue(page.isLoaded(), "All elements are loaded");

		page.grid().selectRow(0);
		soft.assertFalse(page.getMoveUpBtn().isEnabled(), "Button Move Up is not enabled if selected filter is already first");

		page.grid().selectRow(1);
		soft.assertTrue(page.getMoveUpBtn().isEnabled(), "Button Move Up is enabled for the second row");

		HashMap<String, String> row1 = page.grid().getRowInfo(1);
		page.getMoveUpBtn().click();
		HashMap<String, String> newRow0 = page.grid().getRowInfo(0);
		soft.assertEquals(row1.get("Action"), newRow0.get("Action"), "The row that was previously on position 1 is now on first position");

		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled");

		page.saveAndConfirmChanges();
		HashMap<String, String> oldRow0 = page.grid().getRowInfo(0);

		soft.assertEquals(oldRow0.get("Action"), row1.get("Action"),
				"The row that was previously on position 0 is now on first position again after Save");

		for (int i = 0; i < actionNames.size(); i++) {
			rest.deleteMessageFilter(actionNames.get(i), null);
		}
		soft.assertAll();
	}


	@Test(description = "MSGF-7", groups = {"multiTenancy", "singleTenancy"})
	public void editAndCancel() throws Exception {
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		HashMap<String, String> row0 = page.grid().getRowInfo(0);
		page.grid().selectRow(0);

		page.getEditBtn().click();
		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill("newActionValue");
		modal.clickOK();

		page.cancelChangesAndConfirm();

		HashMap<String, String> newRow0 = page.grid().getRowInfo(0);

		soft.assertEquals(row0.get("Action"), newRow0.get("Action"), "Edited values are reset after canceling changes");

//		Delete created filter
		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-8", groups = {"multiTenancy", "singleTenancy"})
	public void editAndSave() throws Exception {
		//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		String newActionValue = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();
		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		page.grid().selectRow(index);
		page.getEditBtn().click();

		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill(newActionValue);

//		necesary because somehow typing doesn't finish the word otherwise
		modal.wait.forXMillis(200);

		modal.clickOK();

		page.saveAndConfirmChanges();

		HashMap<String, String> row = page.grid().getRowInfo(index);
		soft.assertEquals(row.get("Action"), newActionValue, "Edited values are saved");

//		Delete created filter
		rest.deleteMessageFilter(newActionValue, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-9", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndCancel() throws Exception {
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		if (index < 0) {
			throw new RuntimeException("Could not find created filter");
		}

		page.grid().selectRow(index);
		page.getDeleteBtn().click();

		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index == -1, "Filter not found in grid after delete");

		page.cancelChangesAndConfirm();

		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index > -1, "Filter found in grid after Cancel");

//		Delete created filter
		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-10", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndSave() throws Exception {
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		if (index < 0) {
			throw new RuntimeException("Could not find created filter");
		}

		page.grid().selectRow(index);
		page.getDeleteBtn().click();

		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index == -1, "Filter not found in grid after delete");

		page.saveAndConfirmChanges();

		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index == -1, "Filter found in grid after Save");

		soft.assertAll();

	}

	@Test(description = "MSGF-11", groups = {"multiTenancy"})
	public void filtersNotVisibleOnWrongDomains() throws Exception {
//		Create a filter to check on Default domain
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);


		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		if(index<0){throw new RuntimeException("Could not find created filter");}

//		select whatever domain is on second position in the list
		page.getDomainSelector().selectOptionByIndex(1);
		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index<0, "Check if filter is still present in the grid (1)");

//		select default domain
		page.getDomainSelector().selectOptionByText("Default");

		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(!(index<0), "Check if filter is still present in the grid (2)");

//		Delete the created filter
		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-12", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickRow() throws Exception {
		//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();
		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		HashMap<String, String> rowInfo = page.grid().getRowInfo(index);
		page.grid().doubleClickRow(index);

		MessageFilterModal modal = new MessageFilterModal(driver);
		soft.assertTrue(modal.isLoaded(), "Double-clicking a row opens the edit message filter modal");

		soft.assertEquals(rowInfo.get("Plugin"), modal.getPluginSelect().getSelectedValue(), "Value for PLUGIN is the same in grid and modal");
		soft.assertEquals(rowInfo.get("From"), modal.getFromInput().getText(), "Value for FROM is the same in grid and modal");
		soft.assertEquals(rowInfo.get("To"), modal.getToInput().getText(), "Value for TO is the same in grid and modal");
		soft.assertEquals(rowInfo.get("Action"), modal.getActionInput().getText(), "Value for ACTION is the same in grid and modal");
		soft.assertEquals(rowInfo.get("Service"), modal.getServiceInput().getText(), "Value for SERVICE is the same in grid and modal");

//		Delete created filter
		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-13", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickEditAndCancel() throws Exception {
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();
		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		HashMap<String, String> rowInfo = page.grid().getRowInfo(index);
		page.grid().doubleClickRow(index);

		MessageFilterModal modal = new MessageFilterModal(driver);
		soft.assertTrue(modal.isLoaded(), "Double-clicking a row opens the edit message filter modal");

		String partyString = Generator.randomAlphaNumeric(5) +":"+ Generator.randomAlphaNumeric(5);
		modal.getFromInput().fill(partyString);
		modal.getToInput().fill(partyString);
		modal.getActionInput().fill(partyString);
		modal.getServiceInput().fill(partyString);

		modal.clickCancel();

		page.grid().waitForRowsToLoad();

		HashMap<String, String> rowInfo2 = page.grid().getRowInfo(index);

		soft.assertEquals(rowInfo.get("Plugin"), rowInfo2.get("Plugin"), "Value for PLUGIN is the same in grid and modal");
		soft.assertEquals(rowInfo.get("From"), rowInfo2.get("From"), "Value for FROM is the same in grid and modal");
		soft.assertEquals(rowInfo.get("To"), rowInfo2.get("To"), "Value for TO is the same in grid and modal");
		soft.assertEquals(rowInfo.get("Action"), rowInfo2.get("Action"), "Value for ACTION is the same in grid and modal");
		soft.assertEquals(rowInfo.get("Service"), rowInfo2.get("Service"), "Value for SERVICE is the same in grid and modal");


//		Delete created filter
		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-14", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickEditAndSave() throws Exception {
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();
		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		page.grid().doubleClickRow(index);

		MessageFilterModal modal = new MessageFilterModal(driver);
		soft.assertTrue(modal.isLoaded(), "Double-clicking a row opens the edit message filter modal");

		String partyString = Generator.randomAlphaNumeric(5) +":"+ Generator.randomAlphaNumeric(5);
		modal.getFromInput().fill(partyString);
		modal.getToInput().fill(partyString);
		modal.getActionInput().fill(partyString);
		modal.getServiceInput().fill(partyString);

		modal.clickOK();

		page.grid().waitForRowsToLoad();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		page.grid().waitForRowsToLoad();

		HashMap<String, String> rowInfo = page.grid().getRowInfo(index);

		soft.assertEquals(rowInfo.get("From"), partyString, "Value for FROM is changed");
		soft.assertEquals(rowInfo.get("To"), partyString, "Value for TO is changed");
		soft.assertEquals(rowInfo.get("Action"), partyString, "Value for ACTION is changed");
		soft.assertEquals(rowInfo.get("Service"), partyString, "Value for SERVICE is changed");


//		Delete created filter
		rest.deleteMessageFilter(partyString, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-15", groups = {"multiTenancy", "singleTenancy"})
	public void duplicateFilter() throws Exception {
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);


		page.getNewBtn().click();

		MessageFilterModal modal = new MessageFilterModal(driver);
		soft.assertTrue(modal.isLoaded(), "New button opens the new/edit message filter modal");

		String partyString = Generator.randomAlphaNumeric(5) +":"+ Generator.randomAlphaNumeric(5);
		modal.getFromInput().fill(partyString);
		modal.getToInput().fill(partyString);
		modal.getActionInput().fill(partyString);
		modal.getServiceInput().fill(partyString);

		modal.clickOK();

		page.grid().waitForRowsToLoad();
		page.getSaveBtn().click();
		new Dialog(driver).confirm();
		page.grid().waitForRowsToLoad();


		page.getNewBtn().click();

		modal = new MessageFilterModal(driver);
		soft.assertTrue(modal.isLoaded(), "New button opens the new/edit message filter modal");

		modal.getFromInput().fill(partyString);
		modal.getToInput().fill(partyString);
		modal.getActionInput().fill(partyString);
		modal.getServiceInput().fill(partyString);

		modal.clickOK();


		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.DUPLICATE_MESSAGE_FILTER_ERROR,  "Page shows error");

		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled");


//		Delete created filter
		rest.deleteMessageFilter(partyString, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-16", groups = {"multiTenancy", "singleTenancy"})
	public void duplicateEmptyFilter() throws Exception {
		SoftAssert soft = new SoftAssert();
		MessageFilterPage page = new MessageFilterPage(driver);


		page.getNewBtn().click();

		MessageFilterModal modal = new MessageFilterModal(driver);
		soft.assertTrue(modal.isLoaded(), "New button opens the new/edit message filter modal");

		modal.clickOK();

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.DUPLICATE_MESSAGE_FILTER_ERROR,  "Page shows error");

		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled");

		soft.assertAll();
	}

	@Test(description = "MSGF-17", groups = {"multiTenancy", "singleTenancy"})
	public void editToDuplicate() throws Exception {
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		String anotherActionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);
		rest.createMessageFilter(anotherActionName, null);

		SoftAssert soft = new SoftAssert();
		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		page.grid().selectRow(index);
		page.getEditBtn().click();

		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill(anotherActionName);

		modal.clickOK();

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.DUPLICATE_MESSAGE_FILTER_ERROR,  "Page shows error");

		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled");

		soft.assertAll();


//		Delete created filter
		rest.deleteMessageFilter(actionName, null);
		rest.deleteMessageFilter(anotherActionName, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-18", groups = {"multiTenancy"})
	public void editAndChangeDomain() throws Exception {
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		String anotherActionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		String domainName = rest.getDomainNames().get(1);

		SoftAssert soft = new SoftAssert();
		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		page.grid().selectRow(index);
		page.getEditBtn().click();

		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill(anotherActionName);
		modal.clickOK();

		page.getDomainSelector().selectOptionByText(domainName);

		Dialog dialog = new Dialog(driver);

		soft.assertTrue(dialog.isLoaded(), "Dialog is shown");
		soft.assertEquals(dialog.getMessage(), DMessages.DIALOG_CANCEL_ALL, "Dialog shows correct message");

		dialog.confirm();

		soft.assertEquals(page.getDomainSelector().getSelectedValue(), domainName, "Domain was changed");

		page.getDomainSelector().selectOptionByText("Default");

		String listedAction = page.grid().getRowInfo(index).get("Action");
		soft.assertEquals(actionName, listedAction, "Action is not changed after the user presses OK in the dialog");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Changes are canceled and save button is disabled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Changes are canceled and cancel button is disabled");



		page.grid().selectRow(index);
		page.getEditBtn().click();

		modal = new MessageFilterModal(driver);
		modal.getActionInput().fill(anotherActionName);
		modal.clickOK();
		page.getDomainSelector().selectOptionByText(domainName);

		dialog = new Dialog(driver);

		soft.assertTrue(dialog.isLoaded(), "Dialog is shown");
		soft.assertEquals(dialog.getMessage(), DMessages.DIALOG_CANCEL_ALL, "Dialog shows correct message");

		dialog.cancel();
		soft.assertEquals(page.getDomainSelector().getSelectedValue(), "Default", "Domain was NOT changed");

		listedAction = page.grid().getRowInfo(index).get("Action");
		soft.assertEquals(anotherActionName, listedAction, "Action is still changed after the user presses Cancel in the dialog");
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Changes are NOT canceled and save button is enabled");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Changes are NOT canceled and cancel button is enabled");

//		Delete created filter
		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();
	}


	@Test(description = "MSGF-19", groups = {"multiTenancy"})
	public void persistedCheckbox() throws Exception {
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);

		MessageFilterGrid grid = page.grid();
		for (int i = 0; i < grid.getRowsNo(); i++) {
			soft.assertTrue(!grid.getPersisted(i).isEnabled(), "Persisted checkbox is disabled for all rows " + i);
		}

		soft.assertAll();
	}



}
