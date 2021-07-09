package domibus.ui.ux;

import io.qameta.allure.*;
import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.errorLog.ErrorLogPage;
import pages.users.UserModal;
import pages.users.UsersPage;
import rest.RestServicePaths;
import utils.TestUtils;

import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


@Epic("Users")
@Feature("UX")
public class UsersPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.USERS);

	/* Login as super admin and open Users page */
	/*  USR-1 - Login as super admin and open Users page  */
	@Description("USR-1 - Login as super admin and open Users page")
	@Link(name = "EDELIVERY-5174", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5174")
	@AllureId("USR-1")
	@Test(description = "USR-1", groups = {"multiTenancy", "singleTenancy"})
	public void openWindow() throws Exception {
		SoftAssert soft = new SoftAssert();

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		Allure.step("checking page default state");
		log.info("checking page default state");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}

		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");

		soft.assertAll();
	}

	/* Doubleclick on one user (active) */
	/*  USR-2 - Doubleclick on one user active  */
	@Description("USR-2 - Doubleclick on one user active")
	@Link(name = "EDELIVERY-5175", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5175")
	@AllureId("USR-2")
	@Test(description = "USR-2", groups = {"multiTenancy", "singleTenancy"})
	public void openDoubleClickModal() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");
		Allure.step("found user " + username);
		log.info("found user " + username);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.refreshPage();
		page.grid().waitForRowsToLoad();

		Allure.step("double clicking on user");
		log.info("double clicking on user");
		page.grid().scrollToAndDoubleClick("Username", username);

		UserModal um = new UserModal(driver);
		soft.assertTrue(um.isLoaded(), "Doubleclick opens modal");

		soft.assertEquals(um.getUserNameInput().getText(), username, "Usernames match");
		soft.assertEquals(um.getRoleSelect().getSelectedValue(), DRoles.USER, "Roles match");

		if (data.isMultiDomain()) {
			soft.assertTrue(um.getDomainSelect().getSelectedValue().equalsIgnoreCase("Default"), "Domain matches selected domain in page header");
		}
		soft.assertAll();
	}

	/* Doubleclick on one user (deleted) */
	/*  USR-3 - Doubleclick on one user deleted  */
	@Description("USR-3 - Doubleclick on one user deleted")
	@Link(name = "EDELIVERY-5176", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5176")
	@AllureId("USR-3")
	@Test(description = "USR-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickDeletedUser() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, DRoles.USER, true, true, false).getString("userName");

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.grid().waitForRowsToLoad();

		page.getDeletedChk().check();
		page.getSearchBtn().click();
		page.grid().waitForRowsToLoad();

		Allure.step("double clicking on user");
		log.info("double clicking on user");
		page.grid().scrollToAndDoubleClick("Username", username);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.USER_CANNOT_EDIT_DELETED, "Page shows error message");

		page.grid().scrollToAndSelect("Username", username);
		soft.assertTrue(page.getEditBtn().isDisabled(), "Edit button is not enabled for deleted users!");
		soft.assertTrue(page.getDeleteBtn().isDisabled(), "Delete button is not enabled for deleted users!");

		soft.assertAll();
	}

	/* Admin wants to edit username */
	/*  USR-11 - Admin wants to edit username  */
	@Description("USR-11 - Admin wants to edit username")
	@Link(name = "EDELIVERY-5184", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5184")
	@AllureId("USR-11")
	@Test(description = "USR-11", groups = {"multiTenancy", "singleTenancy"})
	public void editUsername() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		String username = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");
		Allure.step("test for user " + username);
		log.info("test for user " + username);

		page.grid().scrollToAndDoubleClick("Username", username);
		Allure.step("modal opened");
		log.info("modal opened");

		soft.assertTrue(!new UserModal(driver).getUserNameInput().isEnabled(), "Username input is not available for editing");

		soft.assertAll();
	}

	/* Admin adds invalid email */
	/*  USR-13 - Admin adds invalid email also applies to user creation  */
	@Description("USR-13 - Admin adds invalid email also applies to user creation")
	@Link(name = "EDELIVERY-5186", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5186")
	@AllureId("USR-13")
	@Test(description = "USR-13", groups = {"multiTenancy", "singleTenancy"})
	public void addEditInvalidEmail() throws Exception {
		SoftAssert soft = new SoftAssert();

//		edit scenario
		String username = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");
		Allure.step("found user " + username);
		log.info("found user " + username);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.refreshPage();

		Allure.step("double clicking on user");
		log.info("double clicking on user");
		page.grid().scrollToAndDoubleClick("Username", username);

		Allure.step("adding invalid email");
		log.info("adding invalid email");
		UserModal modal = new UserModal(driver);
		modal.getEmailInput().click();
		modal.getEmailInput().fill("invalidEmail@");
		modal.getPasswordInput().click();
		soft.assertTrue(modal.getEmailErrMess().isPresent(), "When entering an invalid email an error message is shown.");
		soft.assertTrue(StringUtils.equalsIgnoreCase(modal.getEmailErrMess().getText(), DMessages.USER_EMAIL_INVALID), "When entering an invalid email the CORRECT error message is shown.");

		page.clickVoidSpace();

//		new user scenario

		page.getNewBtn().click();
		Allure.step("adding invalid email");
		log.info("adding invalid email");
		modal.getEmailInput().click();
		modal.getEmailInput().fill("invalidEmail@");
		modal.getPasswordInput().click();
		soft.assertTrue(modal.getEmailErrMess().isPresent(), "When entering an invalid email an error message is shown (new).");
		soft.assertTrue(StringUtils.equalsIgnoreCase(modal.getEmailErrMess().getText(), DMessages.USER_EMAIL_INVALID), "When entering an invalid email the CORRECT error message is shown (new)");

		soft.assertAll();
	}

	/* USR-37 - Verify headers in downloaded CSV sheet */
	/*  USR-37 - Verify headers in downloaded CSV sheet   */
	@Description("USR-37 - Verify headers in downloaded CSV sheet ")
	@Link(name = "EDELIVERY-5210", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5210")
	@AllureId("USR-37")
	@Test(description = "USR-37", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

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

	/* USR-30 - Download all lists of users */
	/*  USR-30 - Download all lists of users  */
	@Description("USR-30 - Download all lists of users")
	@Link(name = "EDELIVERY-5203", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5203")
	@AllureId("USR-30")
	@Test(description = "USR-30", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.grid().waitForRowsToLoad();

		String fileName = rest.csv().downloadGrid(RestServicePaths.USERS_CSV, null, null);
		Allure.step("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.includeDeletedUsers();
		page.grid().waitForRowsToLoad();
		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Allure.step("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.getUsersGrid().relaxCheckCSVvsGridInfo(fileName, soft, "text");

		soft.assertAll();
	}

	/* USR-31 - Change Rows field data */
	/*  USR-31 - Change Rows field data  */
	@Description("USR-31 - Change Rows field data")
	@Link(name = "EDELIVERY-5204", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5204")
	@AllureId("USR-31")
	@Test(description = "USR-31", groups = {"multiTenancy", "singleTenancy"})
	public void checkChangeNumberOfRows() throws Exception {

		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		Allure.step("checking grid");
		log.info("checking grid");
		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/* USR-29 - Click All None link */
	/*  USR-29 - Click All None link  */
	@Description("USR-29 - Click All None link")
	@Link(name = "EDELIVERY-5202", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5202")
	@AllureId("USR-29")
	@Test(description = "USR-29", groups = {"multiTenancy", "singleTenancy"})
	public void checkAllNoneLnk() throws Exception {

		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		Allure.step("checking grid");
		log.info("checking grid");
		page.grid().checkAllLink(soft);
		page.grid().checkNoneLink(soft);

		soft.assertAll();
	}

	/* USR-28 - Click Hide link after selecting some new fields */
	/*  USR-28 - Click Hide link after selecting some new fields  */
	@Description("USR-28 - Click Hide link after selecting some new fields")
	@Link(name = "EDELIVERY-5201", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5201")
	@AllureId("USR-28")
	@Test(description = "USR-28", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWNewSelection() throws Exception {
		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		Allure.step("getting list of columns: " + columnsPre);
		log.info("getting list of columns: " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		Allure.step("enable column with name " + colName);
		log.info("enable column with name " + colName);
		grid.getGridCtrl().checkBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		Allure.step("getting list of columns " + columnsPost);
		log.info("getting list of columns " + columnsPost);

		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() + 1 == columnsPost.size(), "One more column is shown");
		soft.assertTrue(columnsPost.contains(colName), "Correct column is now in the list of columns");

		soft.assertAll();
	}

	/* USR-27 - Click Hide link without any new selection */
	/*  USR-27 - Click Hide link without any new selection  */
	@Description("USR-27 - Click Hide link without any new selection")
	@Link(name = "EDELIVERY-5200", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5200")
	@AllureId("USR-27")
	@Test(description = "USR-27", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		DGrid grid = page.grid();
		Allure.step("get column names");
		log.info("get column names");
		List<String> columnsPre = grid.getColumnNames();

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		Allure.step("click show");
		log.info("click show");
		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		Allure.step("click hide");
		log.info("click hide");
		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		Allure.step("comparing columns");
		log.info("comparing columns");
		List<String> columnsPost = grid.getColumnNames();
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		soft.assertAll();
	}

	/* USR-26 - Check/Uncheck of fields on Show links */
	/*  USR-26 - CheckUncheck of fields on Show links  */
	@Description("USR-26 - CheckUncheck of fields on Show links")
	@Link(name = "EDELIVERY-5199", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5199")
	@AllureId("USR-26")
	@Test(description = "USR-26", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);


		DGrid grid = page.grid();
		grid.waitForRowsToLoad();
		grid.checkModifyVisibleColumns(soft);

		soft.assertAll();
	}

	/* USR-25 - Click Show columns link */
	/*  USR-25 - Click Show columns link  */
	@Description("USR-25 - Click Show columns link")
	@Link(name = "EDELIVERY-5198", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5198")
	@AllureId("USR-25")
	@Test(description = "USR-25", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		Allure.step("Checking visibility of All/None links");
		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");


		soft.assertAll();
	}

	/* USR-24 - Deleted user row selection on single click */
	/*  USR-24 - Deleted user row selection on single click  */
	@Description("USR-24 - Deleted user row selection on single click")
	@Link(name = "EDELIVERY-5197", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5197")
	@AllureId("USR-24")
	@Test(description = "USR-24", groups = {"multiTenancy", "singleTenancy"})
	public void selectDeletedUserRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUser(null, DRoles.USER, true, true, false).getString("userName");
		Allure.step("checking for username " + username);
		log.info("checking for username " + username);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.includeDeletedUsers();

		page.grid().waitForRowsToLoad();

		int index = page.grid().scrollTo("Username", username);
		page.grid().selectRow(index);
		Allure.step("selecting row " + index);
		log.info("selecting row " + index);

		soft.assertEquals(page.grid().getSelectedRowIndex(), index, "Selected row is the one expected");
		soft.assertFalse(page.getEditBtn().isEnabled(), "Cannot edit a deleted user");
		soft.assertFalse(page.getDeleteBtn().isEnabled(), "Cannot delete a deleted user");

		soft.assertAll();
	}

	/* USR-23 - Active user row selection on single click */
	/*  USR-23 - Active user row selection on single click  */
	@Description("USR-23 - Active user row selection on single click")
	@Link(name = "EDELIVERY-5196", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5196")
	@AllureId("USR-23")
	@Test(description = "USR-23", groups = {"multiTenancy", "singleTenancy"})
	public void selectUserRow() throws Exception {
		String username = rest.getUser(null, DRoles.USER, true, false, false).getString("userName");
		Allure.step("checking for username " + username);
		log.info("checking for username " + username);

		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		int index = page.grid().scrollTo("Username", username);
		page.grid().selectRow(index);
		Allure.step("selecting row " + index);
		log.info("selecting row " + index);

		soft.assertEquals(page.grid().getSelectedRowIndex(), index, "Selected row is the one expected");

		soft.assertAll();
	}

	/* USR-18 - Admin downloads user list (multitenancy)*/
	/*  USR-18 - Admin downloads user list  */
	@Description("USR-18 - Admin downloads user list")
	@Link(name = "EDELIVERY-5191", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5191")
	@AllureId("USR-18")
	@Test(description = "USR-18", groups = {"multiTenancy"}, enabled = true)
	public void csvFileDownloadDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		String domainName = rest.getNonDefaultDomain();
		String domainCode = rest.getDomainCodeForName(domainName);
		Allure.step("checking download for domain " + domainName);
		log.info("checking download for domain " + domainName);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.getDomainSelector().selectOptionByText(domainName);

		String fileName = rest.csv().downloadGrid(RestServicePaths.USERS_CSV, null, domainCode);
		Allure.step("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.includeDeletedUsers();
		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Allure.step("checking info in grid against the file");
		log.info("checking info in grid against the file");
//		page.getUsersGrid().checkCSVvsGridInfo(fileName, soft);
		page.getUsersGrid().relaxCheckCSVvsGridInfo(fileName, soft, "text");
		soft.assertAll();
	}


	/*EDELIVERY-5209 - USR-36 - Check sorting on the basis of Headers of Grid*/
	/*  USR-36 - Check sorting on the basis of Headers of Grid   */
	@Description("USR-36 - Check sorting on the basis of Headers of Grid ")
	@Link(name = "EDELIVERY-5209", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5209")
	@AllureId("USR-36")
	@Test(description = "USR-36", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		SoftAssert soft = new SoftAssert();

		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		DGrid grid = page.grid();
		grid.getPagination().getPageSizeSelect().selectOptionByText("25");

		for (int i = 0; i < colDescs.length(); i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}

		soft.assertAll();
	}

	// This test case verifies presence of deleted checkbox in enabled status on users page
	/*  USR-43 - Verify presence of Deleted check box in filters section  */
	@Description("USR-43 - Verify presence of Deleted check box in filters section")
	@Link(name = "EDELIVERY-7230", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7230")
	@AllureId("USR-43")
	@Test(description = "USR-43", groups = {"multiTenancy", "singleTenancy"})
	public void deletedCheckbox() throws Exception {
		SoftAssert soft = new SoftAssert();

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.grid().waitForRowsToLoad();
		soft.assertTrue(page.getDeletedChk().isPresent(), "Deleted checkbox is present");
		soft.assertFalse(page.getDeletedChk().isChecked(), "Checkbox is not checked by default");
		soft.assertFalse(page.getDeletedChk().isDisabled(), "Check box is enabled state");
		soft.assertAll();

	}

	// This test case verifies search functionality for active and deleted both users
	/*  USR-45 - Search all users active and deleted  */
	@Description("USR-45 - Search all users active and deleted")
	@Link(name = "EDELIVERY-7232", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7232")
	@AllureId("USR-45")
	@Test(description = "USR-45", groups = {"multiTenancy", "singleTenancy"})
	public void searchAllUsers() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.grid().waitForRowsToLoad();

		Allure.step("Get all users");
		log.info("Get all users");
		JSONArray userArray = rest.users().getUsers(page.getDomainFromTitle());
		int userCount = userArray.length();

		page.includeDeletedUsers();
		page.grid().waitForRowsToLoad();

		soft.assertTrue(userCount == page.grid().getPagination().getTotalItems(), "Grid count is same as total active and deleted user");

		for (int i = 0; i < Math.min(userCount, 3); i++) {
			Boolean isDeleted = userArray.getJSONObject(i).getBoolean("deleted");
			String userName = userArray.getJSONObject(i).getString("userName");
			String role = userArray.getJSONObject(i).getString("roles");

			int index = page.grid().scrollTo("Username", userName);
			soft.assertFalse(index < 0, "User is  visible in the grid");

			if (isDeleted.booleanValue()) {
				soft.assertTrue(page.getUsersGrid().isDeleted(userName, "Username"), "Username field value is strike through");
				soft.assertTrue(page.getUsersGrid().isDeleted(userName, "Role"), "Role field value is striked through");
			}
		}
		soft.assertAll();

	}

	//This test case verifies the functionality of single click on deleted checkbox
	/*  USR-44 - Filter using Deleted checkbox  */
	@Description("USR-44 - Filter using Deleted checkbox")
	@Link(name = "EDELIVERY-7231", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7231")
	@AllureId("USR-44")
	@Test(description = "USR-44", groups = {"multiTenancy", "singleTenancy"})
	public void searchDeletedUser() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.grid().waitForRowsToLoad();

		Allure.step("Get all users");
		log.info("Get all users");
		JSONArray userArray = rest.users().getUsers(page.getDomainFromTitle());
		int userCount = userArray.length();

		JSONArray deletedUserArray = new JSONArray();

		for (int i = 0; i < userCount; i++) {
			Boolean isDeleted = userArray.getJSONObject(i).getBoolean("deleted");

			if (isDeleted) {
				deletedUserArray.put(userArray.get(i));
			}
		}
		int deleteUserCount = deletedUserArray.length();

		page.getDeletedChk().click();
		page.getSearchBtn().click();
		page.grid().waitForRowsToLoad();
		soft.assertTrue(deleteUserCount == page.grid().getPagination().getTotalItems(), "Grid count is same as deleted user count");

		String userName = deletedUserArray.getJSONObject(0).get("userName").toString();
		Boolean isDeleted = userArray.getJSONObject(0).getBoolean("deleted");

		int index = page.grid().scrollTo("Username", userName);
		soft.assertFalse(index < 0, "User is  visible in the grid");

		if (isDeleted.booleanValue()) {
			soft.assertTrue(page.getUsersGrid().isDeleted(userName, "Username"), "Username field value is strike through");
			soft.assertTrue(page.getUsersGrid().isDeleted(userName, "Role"), "Role field value is striked through");
		}
		soft.assertAll();

	}

	//This test case verifies presence of domain column in downloaded csv
	/*  USR-38 - Verify Domain column in downloaded csv  */
	@Description("USR-38 - Verify Domain column in downloaded csv")
	@Link(name = "EDELIVERY-6359", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-6359")
	@AllureId("USR-38")
	@Test(description = "USR-38", groups = {"multiTenancy"})
	public void domainColPresenceInCsv() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.grid().waitForRowsToLoad();
		String fileName = rest.csv().downloadGrid(RestServicePaths.USERS_CSV, null, page.getDomainFromTitle());
		Allure.step("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		List<String> headers = page.grid().getCsvHeader(fileName);
		soft.assertTrue(headers.contains("Domain"), "Csv header has domain column present");

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Allure.step("Verifying info in CSV file against grid rows");
		log.info("Verifying info in CSV file against grid rows");
		page.grid().checkCSVvsGridHeaders(fileName, soft);

		soft.assertAll();
	}

	// This test case verifies user name in edit pop up opened after sorting data by username
	/*  USR-42 - Verify user name in edit pop up opened after sorting data by username  */
	@Description("USR-42 - Verify user name in edit pop up opened after sorting data by username")
	@Link(name = "EDELIVERY-6374", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-6374")
	@AllureId("USR-42")
	@Test(description = "USR-42", groups = {"multiTenancy", "singleTenancy"})
	public void checkUserName() throws Exception {
		SoftAssert soft = new SoftAssert();

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");
		for (int i = 0; i < colDescs.length(); i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (page.grid().getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, page.grid(), colDesc);
			}
		}
		Allure.step("get username for top row");
		log.info("get username for top row");
		page.grid().getRowSpecificColumnVal(1, "Username");
		String userName = page.grid().getRowSpecificColumnVal(1, "Username");
		Allure.step("double click on top row");
		log.info("double click on top row");
		page.grid().doubleClickRow(1);
		UserModal modal = new UserModal(driver);

		soft.assertTrue(userName.equals(modal.getUserNameInput().getText()), "Top row username is same as username from edit user pop up");
		soft.assertAll();
	}


}

