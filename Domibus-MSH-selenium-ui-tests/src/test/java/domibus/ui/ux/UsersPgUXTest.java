package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import utils.BaseUXTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.users.UserModal;
import pages.users.UsersPage;
import rest.RestServicePaths;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class UsersPgUXTest extends BaseUXTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.USERS);

	/* Login as super admin and open Users page */
	@Test(description = "USR-1", groups = {"multiTenancy", "singleTenancy"})
	public void openWindow() throws Exception {
		SoftAssert soft = new SoftAssert();

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

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
	@Test(description = "USR-2", groups = {"multiTenancy", "singleTenancy"})
	public void openDoubleClickModal() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = getUser(null, DRoles.USER, true, false, false).getString("userName");
		log.info("found user " + username);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.refreshPage();

		log.info("double clicking on user");
		page.grid().scrollToAndDoubleClick("Username", username);

		UserModal um = new UserModal(driver);
		soft.assertTrue(um.isLoaded(), "Doubleclick opens modal");

		soft.assertEquals(username, um.getUserNameInput().getText(), "Usernames match");
		soft.assertEquals(DRoles.USER, um.getRoleSelect().getSelectedValue(), "Roles match");

		if (data.isMultiDomain()) {
			soft.assertEquals(um.getDomainSelect().getSelectedValue(), "Default", "Domain matches selected domain in page header");
		}
		soft.assertAll();
	}

	/* Doubleclick on one user (deleted) */
	@Test(description = "USR-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickDeletedUser() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = getUser(null, DRoles.USER, true, true, false).getString("userName");

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.refreshPage();

		log.info("double clicking on user");
		page.grid().scrollToAndDoubleClick("Username", username);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error message");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.USER_CANNOT_EDIT_DELETED, "Page shows error message");

		soft.assertTrue(!page.getEditBtn().isEnabled(), "Edit button is not enabled for deleted users!");
		soft.assertTrue(!page.getDeleteBtn().isEnabled(), "Delete button is not enabled for deleted users!");

		soft.assertAll();
	}

	/* Admin wants to edit username */
	@Test(description = "USR-11", groups = {"multiTenancy", "singleTenancy"})
	public void editUsername() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = getUser(null, DRoles.USER, true, false, false).getString("userName");

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		log.info("test for user " + username);

		page.grid().scrollToAndDoubleClick("Username", username);
		log.info("modal opened");

		soft.assertTrue(!new UserModal(driver).getUserNameInput().isEnabled(), "Username input is not available for editing");

		soft.assertAll();
	}

	/* Admin adds invalid email */
	@Test(description = "USR-13", groups = {"multiTenancy", "singleTenancy"})
	public void addEditInvalidEmail() throws Exception {
		SoftAssert soft = new SoftAssert();

//		edit scenario
		String username = getUser(null, DRoles.USER, true, false, false).getString("userName");
		log.info("found user " + username);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		page.refreshPage();

		log.info("double clicking on user");
		page.grid().scrollToAndDoubleClick("Username", username);

		log.info("adding invalid email");
		UserModal modal = new UserModal(driver);
		modal.getEmailInput().fill("invalidEmail@");

		soft.assertTrue(modal.getEmailErrMess().isPresent(), "When entering an invalid email an error message is shown");
		soft.assertTrue(StringUtils.equalsIgnoreCase(modal.getEmailErrMess().getText(), DMessages.USER_EMAIL_INVALID), "When entering an invalid email the CORRECT error message is shown");

		page.clickVoidSpace();

//		new user scenario

		page.getNewBtn().click();
		log.info("adding invalid email");
		modal.getEmailInput().fill("invalidEmail@");

		soft.assertTrue(modal.getEmailErrMess().isPresent(), "When entering an invalid email an error message is shown (new)");
		soft.assertTrue(StringUtils.equalsIgnoreCase(modal.getEmailErrMess().getText(), DMessages.USER_EMAIL_INVALID), "When entering an invalid email the CORRECT error message is shown (new)");

		soft.assertAll();
	}

	/* USR-37 - Verify headers in downloaded CSV sheet */
	@Test(description = "USR-37", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownloadHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		String fileName = rest.downloadGrid(RestServicePaths.USERS_CSV, null, null);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();
		page.grid().getGridCtrl().uncheckBoxWithLabel("Password");

		log.info("checking info in grid against the file");
		page.grid().checkCSVvsGridHeaders(fileName, soft);
		soft.assertAll();
	}

	/* USR-30 - Download all lists of users */
	@Test(description = "USR-30", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		String fileName = rest.downloadGrid(RestServicePaths.USERS_CSV, null, null);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();
		page.grid().getGridCtrl().uncheckBoxWithLabel("Password");

		log.info("checking info in grid against the file");
		page.getUsersGrid().checkCSVvsGridInfo(fileName, soft);
		soft.assertAll();
	}

	/* USR-31 - Change Rows field data */
	@Test(description = "USR-31", groups = {"multiTenancy", "singleTenancy"})
	public void checkChangeNumberOfRows() throws Exception {

		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		log.info("checking grid");
		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/* USR-29 - Click All None link */
	@Test(description = "USR-29", groups = {"multiTenancy", "singleTenancy"})
	public void checkAllNoneLnk() throws Exception {

		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		log.info("checking grid");
		page.grid().checkAllLink(soft);
		page.grid().checkNoneLink(soft);

		soft.assertAll();
	}

	/* USR-28 - Click Hide link after selecting some new fields */
	@Test(description = "USR-28", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWNewSelection() throws Exception {
		String colName = TestUtils.getNonDefaultColumn(descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		log.info("getting list of columns: " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		log.info("enable column with name " + colName);
		grid.getGridCtrl().checkBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		log.info("getting list of columns " + columnsPost);

		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() + 1 == columnsPost.size(), "One more column is shown");
		soft.assertTrue(columnsPost.contains(colName), "Correct column is now in the list of columns");

		soft.assertAll();
	}

	/* USR-27 - Click Hide link without any new selection */
	@Test(description = "USR-27", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		DGrid grid = page.grid();
		log.info("get column names");
		List<String> columnsPre = grid.getColumnNames();

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		log.info("click show");
		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		log.info("click hide");
		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		log.info("comparing columns");
		List<String> columnsPost = grid.getColumnNames();
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		soft.assertAll();
	}

	/* USR-26 - Check/Uncheck of fields on Show links */
	@Test(description = "USR-26", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		List<String> columnList = new ArrayList<>(grid.getGridCtrl().getAllCheckboxStatuses().keySet());
		grid.checkModifyVisibleColumns(soft, columnList);

		soft.assertAll();
	}

	/* USR-25 - Click Show columns link */
	@Test(description = "USR-25", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLink() throws Exception {
		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");


		soft.assertAll();
	}

	/* USR-24 - Deleted user row selection on single click */
	@Test(description = "USR-24", groups = {"multiTenancy", "singleTenancy"})
	public void selectDeletedUserRow() throws Exception {
		String username = getUser(null, DRoles.USER, true, true, false).getString("userName");
		log.info("checking for username " + username);

		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		int index = page.grid().scrollTo("Username", username);
		page.grid().selectRow(index);
		log.info("selecting row " + index);

		soft.assertEquals(page.grid().getSelectedRowIndex(), index, "Selected row is the one expected");

		soft.assertAll();
	}

	/* USR-23 - Active user row selection on single click */
	@Test(description = "USR-23", groups = {"multiTenancy", "singleTenancy"})
	public void selectUserRow() throws Exception {
		String username = getUser(null, DRoles.USER, true, false, false).getString("userName");
		log.info("checking for username " + username);

		SoftAssert soft = new SoftAssert();
		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);
		int index = page.grid().scrollTo("Username", username);
		page.grid().selectRow(index);
		log.info("selecting row " + index);

		soft.assertEquals(page.grid().getSelectedRowIndex(), index, "Selected row is the one expected");

		soft.assertAll();
	}

	/* USR-18 - Admin downloads user list (multitenancy)*/
	@Test(description = "USR-18", groups = {"multiTenancy"})
	public void csvFileDownloadDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		String domain = rest.getDomainNames().get(1);
		log.info("checking download for domain " + domain);

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		String fileName = rest.downloadGrid(RestServicePaths.USERS_CSV, null, null);
		log.info("downloaded file with name " + fileName);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();
		page.grid().getGridCtrl().uncheckBoxWithLabel("Password");

		log.info("checking info in grid against the file");
		page.getUsersGrid().checkCSVvsGridInfo(fileName, soft);
		soft.assertAll();
	}






}
