package domibus.ui.ux;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DWait;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import utils.BaseUXTest;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.plugin_users.PluginUserModal;
import pages.plugin_users.PluginUsersPage;
import utils.DFileUtils;
import utils.Generator;
import utils.TestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class PluginUsersPgUXTest extends BaseUXTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PLUGIN_USERS);

	/*	PU-1 - Login as super admin and open Plugin Users page	*/
	@Test(description = "PU-1", groups = {"multiTenancy", "singleTenancy"})
	public void openWindow() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		log.info("checking page default state");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");
		basicFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));
		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}

		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");


		soft.assertAll();
	}

	/*	PU-2 - Doubleclick on one user	*/
	@Test(description = "PU-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickRow() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		if (page.grid().getRowsNo() == 0) {
			getPluginUser(null, DRoles.USER, true, true);
			page.refreshPage();
		}

		log.info("getting user info");
		HashMap<String, String> row = page.grid().getRowInfo(0);

		log.info("double click user");
		page.grid().doubleClickRow(0);

		PluginUserModal pum = new PluginUserModal(driver);

		log.info("checking user info");
		soft.assertEquals(row.get("User Name"), pum.getUserNameInput().getText(), "Correct username is displayed");
		soft.assertEquals(row.get("Role"), pum.getRolesSelect().getSelectedValue(), "Correct role is displayed");
		soft.assertEquals(row.get("Original User"), pum.getOriginalUserInput().getText(), "Correct orig user is displayed");

		soft.assertAll();
	}

	/*	PU-12 - Admin changes password (also applies to user creation)	*/
	@Test(description = "PU-12", groups = {"multiTenancy", "singleTenancy"})
	public void editPassErrMess() throws Exception {
		String username = getPluginUser(null, DRoles.USER, true, false).getString("userName");
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		log.info("editing user");
		page.grid().scrollToAndDoubleClick("User Name", username);

		PluginUserModal pum = new PluginUserModal(driver);

		log.info("setting invalid password");
		pum.getPasswordInput().fill("tst");
		String errMess = pum.getPassErrMess().getText();

		log.info("check error message");
		soft.assertEquals(errMess, DMessages.PASS_POLICY_MESSAGE, "Password policy clearly displayed when wrong pass is entered");

		log.info("setting passord and confirmation not to match");
		pum.getPasswordInput().fill(data.defaultPass());
		pum.getConfirmationInput().fill("lksjdlkfdskj");
		pum.wait.forElementToBeVisible(pum.getConfirmationErrMess().element);
		log.info("check error message");
		errMess = pum.getConfirmationErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_NO_MATCH_MESSAGE, "Password and confirmation should match");

		page.clickVoidSpace();

		soft.assertAll();
	}

	/*	PU-10 - Admin wants to edit username	*/
	@Test(description = "PU-10", groups = {"multiTenancy", "singleTenancy"})
	public void editUsername() throws Exception {
		String username = getPluginUser(null, DRoles.USER, true, false).getString("userName");
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		log.info("opening edit modal");
		page.grid().scrollToAndDoubleClick("User Name", username);

		PluginUserModal pum = new PluginUserModal(driver);
		log.info("check that username field is disabled");
		soft.assertTrue(!pum.getUserNameInput().isEnabled(), "Username is disabled");

		page.clickVoidSpace();

		soft.assertAll();

	}

	/*	PU-11 - Admin wants to edit certificate ID	*/
	@Test(description = "PU-11", groups = {"multiTenancy", "singleTenancy"})
	public void editCertificateID() throws Exception {
		String certName = Generator.randomAlphaNumeric(5);
		String id = Generator.randomAlphaNumeric(5);
		String username = String.format("CN=%s,O=eDelivery,C=BE:%s", certName, id);
		rest.createCertPluginUser(username, DRoles.USER, null);
		log.info("testing for user " + username);

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		log.info("changing to auth type CERTIFICATE");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		log.info("opening edit modal");
		page.grid().scrollToAndDoubleClick("Certificate Id", username);

		PluginUserModal pum = new PluginUserModal(driver);
		log.info("check that username field is disabled");
		soft.assertTrue(!pum.getUserNameInput().isEnabled(), "Username is disabled");

		page.clickVoidSpace();

		soft.assertAll();
	}


	@Test(description = "PU-11", groups = {"multiTenancy", "singleTenancy"})
	public void createPluginUserFieldValidations() throws Exception {
		String username = Generator.randomAlphaNumeric(10);
		SoftAssert soft = new SoftAssert();

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		log.info("opening new plugin user modal");
		page.getNewBtn().click();
		PluginUserModal pum = new PluginUserModal(driver);

		pum.getUserNameInput().clear();
		String errMess = pum.getUsernameErrMess().getText();
		soft.assertEquals(errMess, DMessages.USER_USERNAME_NO_EMPTY, "Username should not be empty");

		pum.getUserNameInput().fill(username);

		pum.getOriginalUserInput().fill("kdsjflksjfsldjk");
		soft.assertEquals(pum.getOriginalUserErrMess().getText(), DMessages.PLUGIN_USER_ORIGINAL_USER_INVALID, "Original user is not valid");
		pum.getOriginalUserInput().clear();

		soft.assertEquals(pum.getRoleErrMess().getText(), DMessages.ROLE_EMPTY, "Role cannot be empty");
		pum.getRolesSelect().selectOptionByIndex(0);


		errMess = pum.getPassErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_EMPTY_MESSAGE, "Password should NOT empty");

		errMess = pum.getConfirmationErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_EMPTY_MESSAGE, "Password should NOT empty");

		pum.getPasswordInput().fill("tst");

		errMess = pum.getPassErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_POLICY_MESSAGE, "Password policy clearly displayed when wrong pass is entered");

		pum.getPasswordInput().fill(data.defaultPass());
		pum.getConfirmationInput().fill("lksjdlkfdskj");
		errMess = pum.getConfirmationErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_NO_MATCH_MESSAGE, "Password and confirmation should match");

		page.clickVoidSpace();

		soft.assertAll();
	}

	@Test(description = "PU-18", groups = {"multiTenancy", "singleTenancy"})
	public void filterPluginUserList() throws Exception {
		List<String> usernames = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String username = Generator.randomAlphaNumeric(10);
			rest.createPluginUser(username, DRoles.USER, data.defaultPass(), null);
			usernames.add(username);
		}

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		page.filters().search(null, null, null, usernames.get(0));
		soft.assertEquals(page.grid().getRowInfo(0).get("User Name"), usernames.get(0), "Search by username return correct result");
		soft.assertEquals(page.grid().getRowsNo(), 1, "Search by username return only one result");

		page.filters().getUsernameInput().clear();
		page.filters().search(null, DRoles.USER, null, null);
		DGrid grid = page.grid();
		for (int i = 0; i < grid.getRowsNo(); i++) {
			soft.assertEquals(grid.getRowInfo(i).get("Role"), DRoles.USER, "Result has correct role");
		}

		for (int i = 0; i < usernames.size(); i++) {
			rest.deletePluginUser(usernames.get(i), null);
		}
		soft.assertAll();
	}

	/* PU-15 - Admin tries to create new user with username less than 3 letters long */
//	known failure, was decided it will not be fixed
	@Test(description = "PU-15", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void pluginUsernameTooShort() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("testing username with only 2 letters");

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		log.info("click New");
		page.getNewBtn().click();

		PluginUserModal modal = new PluginUserModal(driver);
		log.info("fill username with aa");
		modal.getUserNameInput().fill("aa");
		modal.getRolesSelect().selectOptionByText(DRoles.ADMIN);
		modal.getPasswordInput().fill(data.defaultPass());
		modal.getConfirmationInput().fill(data.defaultPass());

		log.info("checking the ok button to be disabled");
		soft.assertTrue(!modal.getOkBtn().isEnabled(), "OK button is disabled until user enters username with more than 3 characters");

		soft.assertAll();
	}

	/* PU-21 - Click Show columns link for Basic authetication type */
	@Test(description = "PU-21", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLinkBasicAuth() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.grid().waitForRowsToLoad();

		log.info("checking show columns link");
		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}

	/* PU-22 - Click Show columns link for Certificate authetication type */
	@Test(description = "PU-22", groups = {"multiTenancy", "singleTenancy"})
	public void clickShowColumnsLinkCertAuth() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		log.info("checking show columns link");
		DGrid grid = page.grid();
		grid.getGridCtrl().showCtrls();

		testColumnControlsAvailableOptions(soft, grid, descriptorObj.getJSONObject("grid").getJSONArray("cert_columns"));

		log.info("Checking visibility of All/None links");
		soft.assertTrue(grid.getGridCtrl().getAllLnk().isVisible(), "All link is visible");
		soft.assertTrue(grid.getGridCtrl().getNoneLnk().isVisible(), "None link is visible");

		soft.assertAll();
	}

	/* PU-23 - Check/Uncheck of fields on Show links */
	@Test(description = "PU-23", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		log.info("modifying column visibility on basic auth view");

		page.grid().getGridCtrl().showCtrls();
		List<String> columnList = new ArrayList<>(page.grid().getGridCtrl().getAllCheckboxStatuses().keySet());
		page.grid().checkModifyVisibleColumns(soft, columnList);

		page.refreshPage();
		log.info("modifying column visibility on certificate auth view");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		page.grid().getGridCtrl().showCtrls();
		columnList = new ArrayList<>(page.grid().getGridCtrl().getAllCheckboxStatuses().keySet());
		page.grid().checkModifyVisibleColumns(soft, columnList);


		soft.assertAll();
	}

	/* PU-24 - Click Hide link without any new selection */
	@Test(description = "PU-24", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkNoNewSelection() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		page.refreshPage();
		log.info("modifying column visibility on certificate auth view");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		page.grid().waitForRowsToLoad();

		grid = page.grid();
		columnsPre = grid.getColumnNames();

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		columnsPost = grid.getColumnNames();
		soft.assertTrue(ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");

		soft.assertAll();
	}

	/* PU-25 - Click Hide link after selecting some new fields */
	@Test(description = "PU-25", groups = {"multiTenancy", "singleTenancy"})
	public void checkHideLinkWNewSelection() throws Exception {
		String colName = "Role";
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();
		List<String> columnsPre = grid.getColumnNames();
		log.info("getting list of columns: " + columnsPre);

		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "Before Show link is clicked the checkboxes are not visible");

		grid.getGridCtrl().showCtrls();
		soft.assertTrue(grid.getGridCtrl().areCheckboxesVisible(), "After Show link is clicked the checkboxes are visible");

		log.info("disable column with name " + colName);
		grid.getGridCtrl().uncheckBoxWithLabel(colName);

		grid.getGridCtrl().hideCtrls();
		soft.assertTrue(!grid.getGridCtrl().areCheckboxesVisible(), "After Hide link is clicked the checkboxes are not visible");

		List<String> columnsPost = grid.getColumnNames();
		log.info("getting list of columns " + columnsPost);

		soft.assertTrue(!ListUtils.isEqualList(columnsPre, columnsPost), "List of columns before and after hiding the controls is the same");
		soft.assertTrue(columnsPre.size() - 1 == columnsPost.size(), "One less column is shown");
		soft.assertTrue(!columnsPost.contains(colName), "Correct column is now out of the list of columns");

		soft.assertAll();
	}

	/* PU-26 - Click All/None link */
	@Test(description = "PU-25", groups = {"multiTenancy", "singleTenancy"})
	public void checkAllNoneLnk() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();

		grid.checkAllLink(soft);
		grid.checkNoneLink(soft);

		soft.assertAll();
	}

	/* PU-28 - Change Rows field data */
	@Test(description = "PU-28", groups = {"multiTenancy", "singleTenancy"})
	public void checkChangeNumberOfRows() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();
		grid.checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/* PU-29 - Check sorting on the basis of Headers of Grid  */
	@Test(description = "PU-29", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkSorting() throws Exception {
		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		DGrid grid = page.grid();

		for (int i = 0; i < colDescs.length(); i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}

		colDescs = descriptorObj.getJSONObject("grid").getJSONArray("cert_columns");

		log.info("switching to CERT users");
		page.filters().getAuthTypeSelect().selectOptionByText("CERTIFICATE");
		grid.waitForRowsToLoad();

		for (int i = 0; i < colDescs.length(); i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}

		soft.assertAll();
	}

	/* This method will verify data of downloaded csv in case of Certificate authentication type*/
	@Test(description = "PU-27-1", groups = {"multiTenancy", "singleTenancy"})
	public void verifyCsvDataForCert() throws Exception {
		SoftAssert soft = new SoftAssert();
		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		log.info("Search plugin user for CERTIFICATE authentication type ");
		page.filters().search("CERTIFICATE", null, null, null);

		log.info("Customized location for download");
		String filePath = DFileUtils.downloadFolderPath();

		log.info("Clean given directory");
		FileUtils.cleanDirectory(new File(filePath));

		log.info("Click on download csv button");
		page.grid().waitForRowsToLoad();
		page.clickDownloadCsvButton(page.getDownloadCsvButton().element);

		log.info("Wait for download to complete");
		DWait wait = new DWait(driver);
		wait.forXMillis(1000);
		log.info("Check if file is downloaded at given location");
		soft.assertTrue(DFileUtils.isFileDownloaded(filePath), "File is downloaded successfully");
		String completeFilePath = filePath + File.separator + DFileUtils.getCompleteFileName(filePath);

		log.info("Verify headers from Downloaded csv and Grid Headers");
		page.grid().checkCSVvsGridHeadersWithAdditionalHeaders(completeFilePath, soft, "CERTIFICATE");
		page.grid().checkCSVvsGridInfo(completeFilePath, soft);

		soft.assertAll();

	}

	/**
	 * This method will verify presence of confirmation pop up on clicking save button for Basic and Certificate
	 * authentication type plugin user
	 */
	@Test(description = "PU-35", groups = {"multiTenancy", "singleTenancy"})
	public void verifyConfPopUp() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(5);

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		List<String> authType = page.filters.getAuthTypeSelect().getOptionsTexts();
		for (String auth : authType) {
			// check scenario for both types of plugin users
			page.filters.getAuthTypeSelect().selectOptionByText(auth);
			log.info("checking Cancel button state");
			soft.assertFalse(page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");

			if (page.filters.getAuthTypeSelect().getSelectedValue().equals("BASIC")) {

				log.info("filling form for new user  for BASIC auth type" + username);
				page.newUser(username, DRoles.ADMIN, data.defaultPass(), data.defaultPass());
			} else {
				String certUserName = "CN=" + Generator.randomAlphaNumeric(1) + "," + "O=" + Generator.randomAlphaNumeric(2) +
						"," + "C=" + RandomStringUtils.random(2, true, false) + ":" + Generator.randomNumber(2);

				log.info("filling form for new cert user for CERTIFICATION auth type :" + certUserName);
				page.newCertUser(certUserName, DRoles.ADMIN);
			}
			page.grid().waitForRowsToLoad();
			log.info("checking Cancel button state");
			soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");
			page.getSaveBtn().click();
			soft.assertTrue(new Dialog(driver).confirmationPopUp.isDisplayed(), "Confirmation pop up is shown");

			soft.assertTrue(new DButton(driver, new Dialog(driver).yesBtn).isPresent(), "Yes button is present");
			soft.assertTrue(new DButton(driver, new Dialog(driver).noBtn).isPresent(), "No button is present");
			new DButton(driver, new Dialog(driver).yesBtn).click();
			page.grid().waitForRowsToLoad();
		}
		soft.assertAll();

	}

	/**
	 * PU-34 - This method will verify addition of Plugin user with username "SUPER" and error for user with "super"
	 * as username
	 */
	@Test(description = "PU-34", groups = {"multiTenancy"})
	public void addSUPERPluginUsr() throws Exception {
		SoftAssert soft = new SoftAssert();
		List<String> usernames = Arrays.asList(new String[]{"super", "SUPER"});

		PluginUsersPage page = new PluginUsersPage(driver);
		page.getSidebar().goToPage(PAGES.PLUGIN_USERS);

		for (String userToAdd : usernames) {
			log.info("Username to be added" + userToAdd);
			page.newUser(userToAdd, DRoles.ADMIN, data.defaultPass(), data.defaultPass());
			page.getSaveBtn().click();
			new DButton(driver, new Dialog(driver).yesBtn).click();

			if (userToAdd.equals("super")) {
				soft.assertTrue(page.getAlertArea().getAlertMessage().contains("name already exists"), "Error message is shown");
				page.getCancelBtn().click();
				new DButton(driver, new Dialog(driver).yesBtn).click();
			} else {
				soft.assertTrue(page.getAlertArea().getAlertMessage().contains("success"), "Success message is shown");

				log.info("Logout and login again to confirm super user details remain same after addition of SUPER plugin user");
				logout();
				login(data.getAdminUser());
			}
			page.refreshPage();
			page.grid().waitForRowsToLoad();
			soft.assertAll();
		}
	}
}
