package domibus.ui.functional;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Alert.AlertFilters;
import pages.Alert.AlertPage;
import rest.RestServicePaths;
import utils.Gen;
import utils.TestUtils;

import java.io.File;
import java.util.*;

public class AlertPgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.ALERTS);

	/* EDELIVERY-5283 - ALRT-1 - Login as super admin and open Alerts page */
	@Test(description = "ALRT-1", groups = {"multiTenancy", "singleTenancy"})
	public void openAlertsPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		Reporter.log("Checking page title");
		log.info("Checking page title");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

		Reporter.log("checking basic filter presence");
		log.info("checking basic filter presence");
		basicFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));

		testDefaultColumnPresence(soft, page.grid(), descriptorObj.getJSONObject("grid").getJSONArray("columns"));

		if (page.grid().getRowsNo() > 0) {
			soft.assertTrue(page.grid().getPagination().getActivePage() == 1, "Default page shown in pagination is 1");
		}

		soft.assertTrue(page.grid().getPagination().getPageSizeSelect().getSelectedValue().equals("10"), "10 is selected by default in the page size select");

		testButtonPresence(soft, page, descriptorObj.getJSONArray("buttons"));


		soft.assertAll();

	}


	/* EDELIVERY-5287 - ALRT-5 - Filter alerts using basic filters */
	@Test(description = "ALRT-5", groups = {"multiTenancy", "singleTenancy"})
	public void searchBasicFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}
		page.grid().waitForRowsToLoad();

		Reporter.log("Number of records : " + page.grid().getPagination().getTotalItems());
		log.info("Number of records : " + page.grid().getPagination().getTotalItems());
		Reporter.log("Getting all listed alert info");
		log.info("Getting all listed alert info");

		HashMap<String, String> fAlert = page.grid().getRowInfo(0);

		Reporter.log("Basic filtering by " + fAlert);
		log.info("Basic filtering by " + fAlert);
		page.filters().basicFilterBy(null, fAlert.get("Alert Type"), fAlert.get("Alert Status"),
				fAlert.get("Alert level"), fAlert.get("Creation Time"), null);

		page.grid().waitForRowsToLoad();

		List<HashMap<String, String>> allResultInfo = page.grid().getAllRowInfo();

		soft.assertTrue(allResultInfo.size() >= 1, "At least one result is returned");

		for (HashMap<String, String> currentAlert : allResultInfo) {
			soft.assertEquals(currentAlert.get("Alert Type"), fAlert.get("Alert Type"), "Result has the same value as initial alert for: " + "Alert Type");
			soft.assertEquals(currentAlert.get("Alert Status"), fAlert.get("Alert Status"), "Result has the same value as initial alert for: " + "Alert Status");
			soft.assertEquals(currentAlert.get("Alert level"), fAlert.get("Alert level"), "Result has the same value as initial alert for: " + "Alert level");
		}


		soft.assertAll();

	}

	//This method will do search operation using advance filters
	/* EDELIVERY-5288 - ALRT-6 - Filter alerts using advanced filters */
	@Test(description = "ALRT-6", groups = {"multiTenancy", "singleTenancy"})
	public void searchAdvanceFilters() throws Exception {
		SoftAssert soft = new SoftAssert();


		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		Reporter.log("Getting all listed alert info");
		log.info("Getting all listed alert info");

		HashMap<String, String> fAlert = page.grid().getRowInfo(0);
		String beforeSearchAlertType = fAlert.get("Alert Type");
		Reporter.log("Alert type for top row : " + beforeSearchAlertType);
		log.info("Alert type for top row : " + beforeSearchAlertType);

		Reporter.log("Advance filtering by " + fAlert);
		log.info("Advance filtering by " + fAlert);
		page.filters().advancedFilterBy(null, fAlert.get("Alert Type"), fAlert.get("Alert Status")
				, null, fAlert.get("Alert Level"), fAlert.get("Creation Time"), null,
				fAlert.get("Reporting Time"), null);
		page.grid().waitForRowsToLoad();

		String afterSearchAlertType = page.grid().getRowInfo(0).get("Alert Type");
		soft.assertEquals(beforeSearchAlertType, afterSearchAlertType, "Alert type after filter is correct");

		soft.assertAll();
	}

	//This method will validate empty search result
	/* EDELIVERY-5289 - ALRT-7 - Filter alerts so that there are no results */
	@Test(description = "ALRT-7", groups = {"multiTenancy", "singleTenancy"})
	public void emptySearchResult() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Login into application and navigate to Alerts page");
		log.info("Login into application and navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		Reporter.log("Search using basic filters");
		log.info("Search using basic filters");
		page.filters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate grid count as zero");
		log.info("Validate grid count as zero");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == 0, "No search result exist");
		soft.assertAll();
	}

	//This method will validate presence of all records after deletion of all search criteria
	/* EDELIVERY-5290 - ALRT-8 - Delete all criteria and press Search */
	@Test(description = "ALRT-8", groups = {"multiTenancy", "singleTenancy"})
	public void deleteSearchCriteria() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Login into application and navigate to Alerts page");
		log.info("Login into application and navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		Reporter.log("Wait for grid row to load ");
		log.info("Wait for grid row to load ");
		page.grid().waitForRowsToLoad();

		Reporter.log("Search using basic filter");
		log.info("Search using basic filter");
		int prevCount = page.grid().getPagination().getTotalItems();
		Reporter.log("Previous count of grid rows:" + prevCount);
		log.info("Previous count of grid rows:" + prevCount);

		page.filters().basicFilterBy(null, "CERT_EXPIRED", null, null, null, null);
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate Grid row count as zero ");
		log.info("Validate Grid row count as zero ");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == 0, "No search result exist");

		Reporter.log("Refresh page");
		log.info("Refresh page");
		page.refreshPage();
		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		Reporter.log("Wait for grid row to load ");
		log.info("Wait for grid row to load ");
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate actual grid row count ");
		log.info("Validate actual grid row count ");
		Reporter.log("Current grid row count:" + page.grid().getPagination().getTotalItems());
		log.info("Current grid row count:" + page.grid().getPagination().getTotalItems());
		soft.assertTrue(page.grid().getPagination().getTotalItems() == prevCount, "All search result exist");
		soft.assertAll();
	}

	//This method will validate presence of show domain alert check box in case of super admin only
	/* EDELIVERY-5293 - ALRT-11 - Admin opens Alerts page */
	@Test(description = "ALRT-11", groups = {"multiTenancy"})
	public void showDomainAlert() throws Exception {
		SoftAssert soft = new SoftAssert();
		Reporter.log("Login into application with super admin credentials and navigate to Alerts page");
		log.info("Login into application with super admin credentials and navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
		AlertPage apage = new AlertPage(driver);
		AlertFilters filters = apage.filters();
		Reporter.log("Check presence of Show domain checkbox");
		log.info("Check presence of Show domain checkbox");
		soft.assertTrue(filters.getShowDomainCheckbox().isPresent(), "CheckBox is  present in case of super User");
		Reporter.log("Logout from application");
		log.info("Logout from application");
		logout();
		Reporter.log("Login with admin credentials");
		log.info("Login with admin credentials");
		login(rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName"), data.defaultPass())
				.getSidebar().goToPage(PAGES.ALERTS);
		Reporter.log("Validate non availability of Show domain alert checkbox for Admin user");
		log.info("Validate non availability of Show domain alert checkbox for Admin user");
		soft.assertFalse(filters.getShowDomainCheckbox().isPresent(), "CheckBox is not present in case of Admin User");
		soft.assertAll();
	}

	//This method will verify alert for message status change
	/* EDELIVERY-5296 - ALRT-14 - Check data listed for MSGSTATUSCHANGED alert */
	@Test(description = "ALRT-14", groups = {"multiTenancy", "singleTenancy"})
	public void msgStatusChangeAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> ids = rest.getMessageIDsWithStatus(null, "SEND_FAILURE");
		if (ids.size() < 1) {
			throw new SkipException("no messages in SEND_FAILURE state");
		}
		String messID = ids.get(0);

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		Reporter.log("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		page.grid().waitForRowsToLoad();

		Reporter.log("Search data using Msg_status_changed alert type");
		log.info("Search data using Msg_status_changed alert type");
		page.filters().basicFilterBy(null, "MSG_STATUS_CHANGED", null, null, null, null);

		page.filters().getMsgIdInput().fill(messID);

		Reporter.log("Check if Multidomain exists");
		log.info("Check if Multidomain exists");
		if (data.isMultiDomain()) {
			Reporter.log("Click on Show domain checkbox");
			log.info("Click on Show domain checkbox");
			page.filters().getShowDomainCheckbox().check();
		}

		Reporter.log("Click on search button");
		log.info("Click on search button");
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate data for given message id,status ,alert type ,alert status and level");
		log.info("Validate data for given message id,status ,alert type ,alert status and level");
		List<String> allInfo = page.grid().getValuesOnColumn("Parameters");

		for (String info : allInfo) {
			soft.assertTrue(info.contains(messID), "Row contains alert for message status changed for :" + messID);
			soft.assertTrue(info.contains("SEND_FAILURE"), "Row contains alert for message status changed for :" + messID);
		}

		soft.assertAll();

	}

	/* EDELIVERY-5299 - ALRT-17 - Check data for USERLOGINFAILURE alert */
	@Test(description = "ALRT-17", groups = {"multiTenancy", "singleTenancy"})
	public void userLoginFailureAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");


		Reporter.log("Login into application");
		log.info("Login into application");
		Reporter.log("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		Reporter.log("Search data using basic filter for user_login_failure alert type");
		log.info("Search data using basic filter for user_login_failure alert type");
		page.filters().basicFilterBy(null, "USER_LOGIN_FAILURE", null, null, null, null);
		page.grid().waitForRowsToLoad();

		Reporter.log("Check if multidomain exists");
		log.info("Check if multidomain exists");
		if (data.isMultiDomain()) {
			Reporter.log("Select show domain check box");
			log.info("Select show domain check box");
			page.filters().getShowDomainCheckbox().check();
		}

		Reporter.log("Click on search button");
		log.info("Click on search button");
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Reporter.log("Validate presence of alert data for user_login_failure alert type for given user");
		log.info("Validate presence of alert data for user_login_failure alert type for given user");
		ArrayList<HashMap<String, String>> listedInfo = page.grid().getListedRowInfo();

		boolean found = false;
		for (HashMap<String, String> map : listedInfo) {
			if (map.get("Parameters").contains(username)) {
				found = true;
				soft.assertTrue(map.get("Alert Type").contains("USER_LOGIN_FAILURE"), "Alert for disabled account is shown ");
				soft.assertTrue(map.get("Alert Level").contains("LOW"), "Disable account alert is of High level");
				soft.assertTrue(map.get("Parameters").contains(username), "Alert for user : " + username + "disabled account is shown here");
			}
		}

		soft.assertTrue(found, "Alert for the blocked user was found");


		soft.assertAll();
	}

	/* EDELIVERY-5300 - ALRT-18 - Check data for USERACCOUNTDISABLED */
	@Test(description = "ALRT-18", groups = {"multiTenancy", "singleTenancy"})
	public void userDisableAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		Reporter.log("Try to login with wrong password for 5 times so that user account gets disabled");
		log.info("Try to login with wrong password for 5 times so that user account gets disabled");
		for (int i = 0; i < 6; i++) {
			rest.login(username, "wrong");
		}

		JSONArray users = rest.users().getUsers(null);
		for (int i = 0; i < users.length(); i++) {
			JSONObject obj = users.getJSONObject(i);
			if (obj.getString("userName").equalsIgnoreCase(username)) {
				soft.assertFalse(obj.getBoolean("active"), "User has been disabled");
			}
		}

		AlertPage page = new AlertPage(driver);
		Reporter.log("Login with Super/admin user");
		log.info("Login with Super/admin user");
		page.getSidebar().goToPage(PAGES.ALERTS);
		Reporter.log("Navigate to Alerts page");
		log.info("Navigate to Alerts page");

		Reporter.log("Search by basic filter for alert type : user account disabled");
		log.info("Search by basic filter for alert type : user account disabled");
		page.filters().basicFilterBy(null, "USER_ACCOUNT_DISABLED", null, null, null, null);

		Reporter.log("Check if multi domain exists");
		log.info("Check if multi domain exists");
		if (data.isMultiDomain()) {
			Reporter.log("Check show domain alert checkbox");
			log.info("Check show domain alert checkbox");
			page.filters().getShowDomainCheckbox().check();
			Reporter.log("Click on search button");
			log.info("Click on search button");
			page.filters().getSearchButton().click();
		}

		page.grid().waitForRowsToLoad();

		Reporter.log("Validate row for user account disabled alert type for given user");
		log.info("Validate row for user account disabled alert type for given user");
		ArrayList<HashMap<String, String>> listedInfo = page.grid().getListedRowInfo();

		boolean found = false;
		for (HashMap<String, String> map : listedInfo) {
			if (map.get("Parameters").contains(username)) {
				found = true;
				soft.assertTrue(map.get("Alert Type").contains("USER_ACCOUNT_DISABLED"), "Alert for disabled account is shown ");
				soft.assertTrue(map.get("Alert Level").contains("HIGH"), "Disable account alert is of High level");
				soft.assertTrue(map.get("Parameters").contains(username), "Alert for user : " + username + "disabled account is shown here");
			}
		}

		soft.assertTrue(found, "Alert for the blocked user was found");

		soft.assertAll();

	}

	/* EDELIVERY-5459 - ALRT-21 - Check data for PLUGINUSERLOGINFAILURE */
	@Test(description = "ALRT-21", groups = {"multiTenancy", "singleTenancy"})
	public void pluginUserLoginFailure() throws Exception {
		SoftAssert soft = new SoftAssert();

		String user = rest.getPluginUser(null, "BASIC", DRoles.ADMIN, true, true).getString("userName");
		Reporter.log("Using plugin user " + user);
		log.info("Using plugin user " + user);

		if (!data.isMultiDomain()) {
			Reporter.log("Setting properties");
			log.info("Setting properties");
			String propName = "domibus.auth.unsecureLoginAllowed";
			String payload = "false";
			Reporter.log("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			log.info("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			rest.properties().updateDomibusProperty(propName, payload);
			Reporter.log("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
			log.info("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
		}

		Reporter.log("Send message using plugin user credentials");
		log.info("Send message using plugin user credentials");
		try {
			messageSender.sendMessage(user, "WrongPassword", null, null);
		} catch (Exception e) {
			Reporter.log("Authentication exception" + e);
			log.debug("Authentication exception" + e);
		}

		Reporter.log("Login into application");
		log.info("Login into application");
		Reporter.log("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

		AlertPage page = new AlertPage(driver);
		Reporter.log("Search data using basic filter for plugin_user_login_failure alert type");
		log.info("Search data using basic filter for plugin_user_login_failure alert type");
		page.filters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);

		Reporter.log("Check if multidomain exists");
		log.info("Check if multidomain exists");
		if (data.isMultiDomain()) {
			Reporter.log("Select show domain check box");
			log.info("Select show domain check box");
			page.filters().showDomainAlert();
		}

		page.grid().waitForRowsToLoad();

		HashMap<String, String> info = page.grid().getRowInfo(0);

		Reporter.log("Validate presence of alert with correct alert type, level ,status and plugin username in parameters");
		log.info("Validate presence of alert with correct alert type, level ,status and plugin username in parameters");
		soft.assertTrue(info.get("Alert Type").contains("PLUGIN_USER_LOGIN_FAILURE"), "Alert for Plugin user login failure is shown ");
		soft.assertTrue(info.get("Alert Level").contains("LOW"), "Alert level is low ");
		soft.assertTrue(info.get("Alert Status").contains("SUCCESS"), "Alert status is success");
		soft.assertTrue(info.get("Parameters").contains(user), "Alert has plugin user name in parameters field");
		soft.assertAll();
	}

	/* EDELIVERY-5460 - ALRT-22 - Check data for PLUGINUSERACCOUNTDISABLED */
	@Test(description = "ALRT-22", groups = {"multiTenancy", "singleTenancy"})
	public void pluginUserDisabled() throws Exception {
		SoftAssert soft = new SoftAssert();

		String user = Gen.randomAlphaNumeric(10);
		Reporter.log("Create plugin users");
		log.info("Create plugin users");
		rest.pluginUsers().createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);

		if (!data.isMultiDomain()) {
			Reporter.log("Setting properties");
			log.info("Setting properties");
			String propName = "domibus.auth.unsecureLoginAllowed";
			String payload = "false";
			Reporter.log("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			log.info("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			rest.properties().updateDomibusProperty(propName, payload);
			Reporter.log("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
			log.info("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
		}

		Reporter.log("Send message using plugin user credentials");
		log.info("Send message using plugin user credentials");
		for (int i = 0; i <= 5; i++) {
			try {
				messageSender.sendMessage(user, data.getNewTestPass(), null, null);
			} catch (Exception e) {
				Reporter.log("Authentication Exception " + e);
				log.debug("Authentication Exception " + e);
			}
		}

		Reporter.log("Login into application");
		log.info("Login into application");
		Reporter.log("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

		AlertPage page = new AlertPage(driver);
		Reporter.log("Search data using basic filter for plugin_user_account_disabled alert type");
		log.info("Search data using basic filter for plugin_user_account_disabled alert type");
		page.filters().basicFilterBy(null, "PLUGIN_USER_ACCOUNT_DISABLED", null, null, null, null);

		Reporter.log("Check if multidomain exists");
		log.info("Check if multidomain exists");
		if (data.isMultiDomain()) {
			Reporter.log("Select show domain check box");
			log.info("Select show domain check box");
			page.filters().showDomainAlert();
		}

		page.grid().waitForRowsToLoad();
		Reporter.log("Validate presence of alert for plugin_user_account_disabled");
		log.info("Validate presence of alert for plugin_user_account_disabled");
		HashMap<String, String> info = page.grid().getRowInfo(0);
		soft.assertTrue(info.get("Alert Type").contains("PLUGIN_USER_ACCOUNT_DISABLED"), "Top row alert is for Plugin user account disabled");
		soft.assertTrue(info.get("Alert Level").contains("HIGH"), "Proper alert level is shown");
		soft.assertTrue(info.get("Alert Status").contains("SUCCESS"), "Proper alert status is shown");
		soft.assertTrue(info.get("Parameters").contains(user), "Alert is shown for same user");
		soft.assertAll();
	}


	/* EDELIVERY-5285 - ALRT-3 - Super admin changes domain and ticks Show Domain alerts */
	@Test(description = "ALRT-3", groups = {"multiTenancy"})
	public void showDomainAlertCheckedForSecDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);

		Reporter.log("Login into application and navigate to Alerts page");
		log.info("Login into application and navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		Reporter.log("Change Domain");
		log.info("Change Domain");
		page.getDomainSelector().selectOptionByIndex(1);

		Reporter.log("wait for grid row to load");
		log.info("wait for grid row to load");
		page.grid().waitForRowsToLoad();

		Reporter.log("Select show domain alert checkbox");
		log.info("Select show domain alert checkbox");
		aFilter.getShowDomainCheckbox().click();

		aFilter.basicFilterBy(null, "USER_LOGIN_FAILURE", null, null, null, null);

		Reporter.log("Click on search button");
		log.info("Click on search button");
//		aFilter.getSearchButton().click();
		page.grid().waitForRowsToLoad();

		int rowCount = page.grid().getRowsNo();
		if (rowCount <= 0) {
			throw new SkipException("not enough USER_LOGIN_FAILURE alerts to verify");
		}
		String domain = page.getDomainFromTitle();
		JSONArray userList = rest.users().getUsers(domain);

		String alertUser = page.grid().getRowSpecificColumnVal(0, "Parameters").split(",")[0].trim();

		boolean found = false;
		for (int i = 0; i < userList.length(); i++) {
			String user = userList.getJSONObject(i).getString("userName");
			String role = userList.getJSONObject(i).getString("roles");
			if (StringUtils.equalsIgnoreCase(user, alertUser) && !StringUtils.equalsIgnoreCase(role, "ROLE_AP_ADMIN")) {
				found = true;
			}
		}

		soft.assertTrue(found, "User was found and was not SUPER user");


		soft.assertAll();
	}

	//This method will verify double click feature for Alerts page
	/* EDELIVERY-5286 - ALRT-4 - Doubleclik on one alert */
	@Test(description = "ALRT-4", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickAlertRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();

		AlertPage page = new AlertPage(driver);
		Reporter.log("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		Reporter.log("Wait for grid row to load");
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();


		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

		Reporter.log("double click row 0");
		log.info("double click row 0");
		page.grid().doubleClickRow(0);

		Reporter.log("checking the current selected row");
		log.info("checking the current selected row");
		soft.assertTrue(!page.hasOpenDialog(), "No dialog is visible on the page");

		soft.assertAll();
	}


	//This method will verify data of Alerts page after changing domains
	/* EDELIVERY-5291 - ALRT-9 - Super admin changes current domain */
	@Test(description = "ALRT-9", groups = {"multiTenancy"})
	public void changeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);
		Reporter.log("Login into application and Navigate to Alerts page");
		log.info("Login into application and Navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		List<String> userName = new ArrayList<>();

		soft.assertTrue(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");
		soft.assertFalse(aFilter.getShowDomainCheckbox().isChecked(), "Check Box is not checked");

		int gridRowCount = page.grid().getRowsNo();
		ArrayList<String> superUsers = rest.users().getSuperUsernames();

		for (int i = 0; i < gridRowCount; i++) {
			String userNameStr = page.grid().getRowSpecificColumnVal(i, "Parameters").split(",")[0];
			Reporter.log("Extract all user names available in parameters fields ");
			log.info("Extract all user names available in parameters fields ");

			boolean isSuper = false;
			for (int j = 0; j < superUsers.size(); j++) {
				if (StringUtils.equalsIgnoreCase(superUsers.get(j), userNameStr)) {
					isSuper = true;
				}
			}
			soft.assertTrue(isSuper, "User is found to be super user");
		}

		Reporter.log("Change domain");
		log.info("Change domain");
		page.getDomainSelector().selectOptionByIndex(1);
		page.grid().waitForRowsToLoad();

		soft.assertTrue(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");
		soft.assertFalse(aFilter.getShowDomainCheckbox().isChecked(), "Check Box is not checked");

		Reporter.log("Extract total number of count");
		log.info("Extract total number of count");

		int newgridRowCount = page.grid().getRowsNo();

		soft.assertEquals(gridRowCount, newgridRowCount, "Same number of rows shown");

		for (int i = 0; i < newgridRowCount; i++) {
			String userNameStr = page.grid().getRowSpecificColumnVal(i, "Parameters").split(",")[0];
			Reporter.log("Extract all user names available in parameters fields ");
			log.info("Extract all user names available in parameters fields ");

			boolean isSuper = false;
			for (int j = 0; j < superUsers.size(); j++) {
				if (StringUtils.equalsIgnoreCase(superUsers.get(j), userNameStr)) {
					isSuper = true;
				}
			}
			soft.assertTrue(isSuper, "User is found to be super user after domain change");
		}


		soft.assertAll();

	}

//This method will download csv with/without show domain checkbox checked for all domains
	/* EDELIVERY-5292 - ALRT-10 - Admin downloads list of alerts */
	@Test(description = "ALRT-10", groups = {"multiTenancy", "singleTenancy"})
	public void downloadCsv() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();
		AlertPage page = new AlertPage(driver);

		Reporter.log("Login with Super/Admin user and navigate to Alerts page");
		log.info("Login with Super/Admin user and navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		String fileName = page.pressSaveCsvAndSaveFile();

		Reporter.log("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().waitForRowsToLoad();
		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Reporter.log("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.alertsGrid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}


	//This method will verify absence of super admin records and present record belongs to current domain
	/* EDELIVERY-5295 - ALRT-13 - Super admin checks Show domain alerts checkbox  */
	@Test(description = "ALRT-13", groups = {"multiTenancy"})
	public void superAdminrecordAbsenceForAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = null;
		if (data.isMultiDomain()) {
			Reporter.log("selecting random domain");
			log.info("selecting random domain");
			List<String> domains = rest.getDomainCodes();
			Reporter.log("got domains: " + domains);
			log.debug("got domains: " + domains);

			int index = new Random().nextInt(domains.size());

			domain = domains.get(index);
			Reporter.log("will run for domain " + domain);
			log.info("will run for domain " + domain);
		}

		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);

		String user = Gen.randomAlphaNumeric(10);
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), domain);
		Reporter.log("created user " + user);
		log.info("created user " + user);

		Reporter.log("Login with created user and naviagte to Alerts page");
		log.info("Login with created user and naviagte to Alerts page");
		login(user, data.defaultPass()).getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		soft.assertFalse(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");

		int recordCount = page.grid().getRowsNo();
		if (recordCount <= 0) {
			Reporter.log("no records to verify, exiting now");
			log.info("no records to verify, exiting now");
			return;
		}

		List<String> superList = rest.users().getSuperUsernames();
		Reporter.log("Super user list: " + superList);
		log.debug("Super user list: " + superList);
		ArrayList<HashMap<String, String>> pageInfo = page.grid().getListedRowInfo();

		for (int j = 0; j < pageInfo.size(); j++) {

			HashMap<String, String> info = pageInfo.get(j);

			if (info.containsValue("USER_LOGIN_FAILURE") || info.containsValue("USER_ACCOUNT_DISABLED")) {
				String userNameStr = info.get("Parameters").split(",")[0];
				soft.assertTrue(!superList.contains(userNameStr), userNameStr + " is not present in the list of super admins");
			}
		}

		soft.assertAll();


	}


	//This method will verify default data in all search filters drop downs
	/* EDELIVERY-6352 - ALRT-28 - Verify data in drop downs for search filters on both domains and for both admin and super */
	@Test(description = "ALRT-28", groups = {"multiTenancy", "singleTenancy"})
	public void defaultDataInSearchFilter() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		AlertFilters aFilter = new AlertFilters(driver);

		soft.assertTrue(aFilter.verifyDropdownValues("Processed"), "Processed dropdown values are not as expected");
		soft.assertTrue(aFilter.verifyDropdownValues("Alert Type"), "Alert Type dropdown values are not as expected");
		soft.assertTrue(aFilter.verifyDropdownValues("Alert Status"), "Alert Status dropdown values are not as expected");
		soft.assertTrue(aFilter.verifyDropdownValues("Alert Level"), "Alert Level dropdown values are not as expected");

		soft.assertAll();
	}

	//This method will verify validation applicable for alert id
	/* EDELIVERY-6353 - ALRT-29 - Verify validation for data for Alert id field */
	@Test(description = "ALRT-29", groups = {"multiTenancy", "singleTenancy"})
	public void checkValidationForAlertId() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();

		Reporter.log("Login into application and navigate to Alert page");
		log.info("Login into application and navigate to Alert page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		AlertFilters aFilter = new AlertFilters(driver);

		Reporter.log("Click on advance link");
		log.info("Click on advance link");
		aFilter.getAdvancedLink().click();

		Reporter.log("Create list of correct and incorrect data");
		log.info("Create list of correct and incorrect data");
		List<String> correctDataArray = Arrays.asList("1234567890123456789", "347362", "1");
		List<String> incorrectDataArray = Arrays.asList("random", "0random", "0000000000000000000", "12345678901234567890", "random1", "54 656", "$#%", "-989", "+787");

		for (int i = 0; i < correctDataArray.size(); i++) {
			Reporter.log("Pass correct value :" + correctDataArray.get(i));
			log.info("Pass correct value :" + correctDataArray.get(i));
			aFilter.getAlertIdInput().fill(correctDataArray.get(i));

			soft.assertFalse(aFilter.isAlertIdValidationMessageVisible(), "Validation message is not visible for input " + correctDataArray.get(i));

			Reporter.log("Verify status of search button as enabled");
			log.info("Verify status of search button as enabled");
			soft.assertTrue(aFilter.getSearchButton().isEnabled(), "Button is enabled");
		}

		for (int i = 0; i < incorrectDataArray.size(); i++) {
			Reporter.log("Pass incorrect value :" + incorrectDataArray.get(i));
			log.info("Pass incorrect value :" + incorrectDataArray.get(i));
			aFilter.getAlertIdInput().fill(incorrectDataArray.get(i));

			Reporter.log("Verify presence of validation message under alert id field");
			log.info("Verify presence of validation message under alert id field");
			soft.assertTrue(aFilter.isAlertIdValidationMessageVisible(), "Validation message IS visible for input " + incorrectDataArray.get(i));
			soft.assertEquals(aFilter.getAlertValidationMess(), DMessages.ALERT_ID_INPUT_VALIDATION_MESSAGE, "Correct validation message is shown");

			soft.assertFalse(aFilter.getSearchButton().isEnabled(), "Button is not enabled");
		}

		soft.assertAll();
	}

	/* EDELIVERY-6371 - ALRT-32 - Super admin marks super alert as processed  */
	@Test(description = "ALRT-32", groups = {"multiTenancy"})
	public void checkProcessed() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		Reporter.log("Login into application and navigate to Alert page");
		log.info("Login into application and navigate to Alert page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		if (data.isMultiDomain()) {
			Reporter.log("Showing domain alerts");
			log.info("Showing domain alerts");
			page.filters().showDomainAlert();
			page.grid().waitForRowsToLoad();
		}

		AlertFilters aFilter = new AlertFilters(driver);

		Reporter.log("Check alert count when showDomain alert is false");
		log.info("Check alert count when showDomain alert is false");
		int totalCount = rest.alerts().getAlerts(domain, false, true).length();

		if (totalCount <= 0) {
			throw new SkipException("No alerts present");
		}


		Reporter.log("Verify disabled status of save and cancel button");
		log.info("Verify disabled status of save and cancel button");
		soft.assertFalse(page.getSaveButton().isEnabled(), "Check Save button is disabled");
		soft.assertFalse(page.getCancelButton().isEnabled(), "Check Cancel button is disabled");
		soft.assertFalse(page.getDeleteButton().isEnabled(), "Check Delete button is disabled");

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().checkBoxWithLabel("Alert Id");

		Reporter.log("Check processed checkbox for first row");
		log.info("Check processed checkbox for first row");
		page.alertsGrid().markAsProcessed(0);

		String alertId = page.grid().getRowSpecificColumnVal(0, "Alert Id");

		soft.assertTrue(page.getSaveButton().isEnabled(), "Check Save button is enabled");
		soft.assertTrue(page.getCancelButton().isEnabled(), "Check Cancel button is enabled");
		soft.assertTrue(page.getDeleteButton().isEnabled(), "Check Delete button is enabled");

		Reporter.log("Click on save button and then ok from confirmation pop up");
		log.info("Click on save button and then ok from confirmation pop up");
		page.getSaveButton().click();
		new Dialog(driver).confirm();

		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.ALERT_UPDATE_SUCCESS_MESSAGE, "Correct update message is shown");
		page.grid().waitForRowsToLoad();


		Reporter.log("Check total count as 1 less than before");
		log.info("Check total count as 1 less than before");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == totalCount - 1, "Check all alert size 1 less than before");

		Reporter.log("Select processed in search filter ");
		log.info("Select processed in search filter ");
		aFilter.getProcessedSelect().selectOptionByText("PROCESSED");
		Reporter.log("Click on search button");
		log.info("Click on search button");
		aFilter.getSearchButton().click();
		page.grid().waitForRowsToLoad();

		List<String> ids = page.grid().getValuesOnColumn("Alert Id");

		soft.assertTrue(ids.contains(alertId), "Processed record is present after event completion");

		soft.assertAll();
	}

	/* EDELIVERY-5302 - ALRT-20 - Verify headers in downloaded CSV file */
	@Test(description = "ALRT-20", groups = {"multiTenancy", "singleTenancy"})
	public void verifyHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		Reporter.log("Customized location for download");
		log.info("Customized location for download");

		page.grid().waitForRowsToLoad();
		String filePath = page.pressSaveCsvAndSaveFile();


		Reporter.log("Check if file is downloaded at given location");
		log.info("Check if file is downloaded at given location");
		soft.assertTrue(new File(filePath).exists(), "File is downloaded successfully");

		Reporter.log("Extract complete path for downloaded file");
		log.info("Extract complete path for downloaded file");
		String completeFilePath = filePath;

		Reporter.log("Click on show link");
		log.info("Click on show link");
		page.grid().getGridCtrl().showCtrls();

		Reporter.log("Click on All link to show all available column headers");
		log.info("Click on All link to show all available column headers");
		page.grid().getGridCtrl().showAllColumns();

		Reporter.log("Compare headers from downloaded csv and grid");
		log.info("Compare headers from downloaded csv and grid");
		page.grid().checkCSVvsGridHeaders(completeFilePath, soft);
		soft.assertAll();
	}

	/* EDELIVERY-5301 - ALRT-19 - Check sorting */
	@Test(description = "ALRT-19", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		SoftAssert soft = new SoftAssert();

		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		AlertFilters aFilter = new AlertFilters(driver);
		aFilter.getProcessedSelect().selectOptionByIndex(1);
		aFilter.getSearchButton().click();
		page.grid().waitForRowsToLoad();

		DGrid grid = page.grid();
		for (int i = 0; i < colDescs.length(); i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, grid, colDesc);
			}
		}


		soft.assertAll();
	}

	/* EDELIVERY-5471 - ALRT-23 - Check additional filters section for each alert type */
	@Test(description = "ALRT-23", groups = {"multiTenancy", "singleTenancy"})
	public void checkAdditionalFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("Navigating to Alerts page");
		log.info("Navigating to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		Reporter.log("waiting for grid to load");
		log.info("waiting for grid to load");
		page.grid().waitForRowsToLoad();

		AlertFilters filter = new AlertFilters(driver);
		Reporter.log("iterating trough alert types");
		log.info("iterating trough alert types");

		List<String> options = filter.getAlertTypeSelect().getOptionsTexts();

		for (String option : options) {

			if (StringUtils.isEmpty(option.trim())) {
				continue;
			}

			Reporter.log("checking alert type " + option);
			log.info("checking alert type " + option);
			filter.getAlertTypeSelect().selectOptionByText(option);

			soft.assertEquals(option, filter.getXFilterSectionName(), "Alert type and section name have the same value");
			List<String> xFilters = filter.getXFilterNames();

//			soft.assertEquals(xFilters , descriptorObj.getJSONObject("extraFilters").getJSONArray(option).toList(), "Filters listed are as expected");
			soft.assertTrue(
					CollectionUtils.isEqualCollection(xFilters, descriptorObj.getJSONObject("extraFilters").getJSONArray(option).toList()),
					"Filters listed are as expected");
		}


		soft.assertAll();
	}


	/*     EDELIVERY-8215 - ALRT-53 - Search Alert for "Plugin" category */
	@Test(description = "ALRT-53", groups = {"multiTenancy", "singleTenancy"})
	public void filterForPluginAlertType() throws Exception {
		SoftAssert soft = new SoftAssert();


		log.info("Navigating to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		log.info("waiting for grid to load");
		page.grid().waitForRowsToLoad();

		log.info("Select alert type PLUGIN");
		page.filters().getAlertTypeSelect().selectOptionByText("PLUGIN");
		page.filters().getSearchButton().click();

		soft.assertFalse(page.getAlertArea().isShown(), "No alert message is shown");
		soft.assertTrue(page.grid().isPresent(), "No alert message is shown");

		soft.assertAll();
	}


}
