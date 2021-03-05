package domibus.ui.functional;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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


	private void validateDomainAlertInfo(HashMap<String, String> rowInfo, JSONArray userList, JSONArray messageList, JSONArray pluginuserList, SoftAssert soft) throws Exception {
		log.info("Validating alert: " + rowInfo.toString());
		String alertType = rowInfo.get("Alert Type");
		String entity = rowInfo.get("Parameters").split(",")[0];

		soft.assertFalse(entity.equalsIgnoreCase("super"), "super user is not present in the alert info");


		if (StringUtils.equalsIgnoreCase(alertType, "MSG_STATUS_CHANGED")) {
			soft.assertTrue(foundMessage(messageList, entity), "Could not find mesage in list of domain messages");
		} else if (StringUtils.equalsIgnoreCase(alertType, "USER_LOGIN_FAILURE")
				|| StringUtils.equalsIgnoreCase(alertType, "USER_ACCOUNT_DISABLED")
				|| StringUtils.equalsIgnoreCase(alertType, "USER_ACCOUNT_ENABLED")
				|| StringUtils.equalsIgnoreCase(alertType, "PASSWORD_EXPIRED")
				|| StringUtils.equalsIgnoreCase(alertType, "PASSWORD_IMMINENT_EXPIRATION")
		) {
			soft.assertTrue(foundUser(userList, entity), "Could not find user in list of domain users");
		} else if (StringUtils.equalsIgnoreCase(alertType, "PLUGIN_USER_LOGIN_FAILURE")
				|| StringUtils.equalsIgnoreCase(alertType, "PLUGIN_USER_ACCOUNT_DISABLED")
				|| StringUtils.equalsIgnoreCase(alertType, "PLUGIN_USER_ACCOUNT_ENABLED")
				|| StringUtils.equalsIgnoreCase(alertType, "PLUGIN_PASSWORD_IMMINENT_EXPIRATION")
				|| StringUtils.equalsIgnoreCase(alertType, "PLUGIN_PASSWORD_EXPIRED")) {
			soft.assertTrue(foundPluginUser(pluginuserList, entity), "Could not find plugin user in list of domain plugin users");
		}
	}

	private boolean foundUser(JSONArray users, String username) throws Exception {
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(user.getString("userName"), username)) {
				return true;
			}
		}
		return false;
	}

	private boolean foundMessage(JSONArray messages, String messID) throws Exception {
		for (int i = 0; i < messages.length(); i++) {
			JSONObject message = messages.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(message.getString("messageId"), messID)) {
				return true;
			}
		}

		return false;
	}

	private boolean foundPluginUser(JSONArray pluginUsers, String pluginUserId) throws Exception {
		for (int i = 0; i < pluginUsers.length(); i++) {
			JSONObject plu = pluginUsers.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(plu.getString("userName"), pluginUserId)) {
				return true;
			}
		}
		return false;
	}


	// EDELIVERY-5283 - ALRT-1 - Login as super admin and open Alerts page
	@Test(description = "ALRT-1", groups = {"multiTenancy", "singleTenancy"})
	public void openAlertsPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		log.info("Checking page title");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

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


	//This method will do Search using Basic filters
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

		log.info("Number of records : " + page.grid().getPagination().getTotalItems());
		log.info("Getting all listed alert info");

		HashMap<String, String> fAlert = page.grid().getRowInfo(0);

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

		log.info("Getting all listed alert info");

		HashMap<String, String> fAlert = page.grid().getRowInfo(0);
		String beforeSearchAlertType = fAlert.get("Alert Type");
		log.info("Alert type for top row : " + beforeSearchAlertType);

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
	@Test(description = "ALRT-7", groups = {"multiTenancy", "singleTenancy"})
	public void emptySearchResult() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Login into application and navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		log.info("Search using basic filters");
		page.filters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);
		page.grid().waitForRowsToLoad();

		log.info("Validate grid count as zero");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == 0, "No search result exist");
		soft.assertAll();
	}

	//This method will validate presence of all records after deletion of all search criteria
	@Test(description = "ALRT-8", groups = {"multiTenancy", "singleTenancy"})
	public void deleteSearchCriteria() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Login into application and navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		log.info("Wait for grid row to load ");
		page.grid().waitForRowsToLoad();

		log.info("Search using basic filter");
		int prevCount = page.grid().getPagination().getTotalItems();
		log.info("Previous count of grid rows:" + prevCount);

		page.filters().basicFilterBy(null, "CERT_EXPIRED", null, null, null, null);
		page.grid().waitForRowsToLoad();

		log.info("Validate Grid row count as zero ");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == 0, "No search result exist");

		log.info("Refresh page");
		page.refreshPage();
		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		log.info("Wait for grid row to load ");
		page.grid().waitForRowsToLoad();

		log.info("Validate actual grid row count ");
		log.info("Current grid row count:" + page.grid().getPagination().getTotalItems());
		soft.assertTrue(page.grid().getPagination().getTotalItems() == prevCount, "All search result exist");
		soft.assertAll();
	}

	//This method will validate presence of show domain alert check box in case of super admin only
	@Test(description = "ALRT-11", groups = {"multiTenancy"})
	public void showDomainAlert() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login into application with super admin credentials and navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
		AlertPage apage = new AlertPage(driver);
		AlertFilters filters = apage.filters();
		log.info("Check presence of Show domain checkbox");
		soft.assertTrue(filters.getShowDomainCheckbox().isPresent(), "CheckBox is  present in case of super User");
		log.info("Logout from application");
		logout();
		log.info("Login with admin credentials");
		login(rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName"), data.defaultPass())
				.getSidebar().goToPage(PAGES.ALERTS);
		log.info("Validate non availability of Show domain alert checkbox for Admin user");
		soft.assertFalse(filters.getShowDomainCheckbox().isPresent(), "CheckBox is not present in case of Admin User");
		soft.assertAll();
	}

	//This method will verify alert for message status change
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
		log.info("Navigate to Alerts page");
		page.grid().waitForRowsToLoad();

		log.info("Search data using Msg_status_changed alert type");
		page.filters().basicFilterBy(null, "MSG_STATUS_CHANGED", null, null, null, null);

		page.filters().getMsgIdInput().fill(messID);

		log.info("Check if Multidomain exists");
		if (data.isMultiDomain()) {
			log.info("Click on Show domain checkbox");
			page.filters().getShowDomainCheckbox().check();
		}

		log.info("Click on search button");
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		log.info("Validate data for given message id,status ,alert type ,alert status and level");
		List<String> allInfo = page.grid().getValuesOnColumn("Parameters");

		for (String info : allInfo) {
			soft.assertTrue(info.contains(messID), "Row contains alert for message status changed for :" + messID);
			soft.assertTrue(info.contains("SEND_FAILURE"), "Row contains alert for message status changed for :" + messID);
		}

		soft.assertAll();

	}

	//This method will verify alert for user login failure case
	@Test(description = "ALRT-17", groups = {"multiTenancy", "singleTenancy"})
	public void userLoginFailureAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");


		log.info("Login into application");
		log.info("Navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		log.info("Search data using basic filter for user_login_failure alert type");
		page.filters().basicFilterBy(null, "USER_LOGIN_FAILURE", null, null, null, null);
		page.grid().waitForRowsToLoad();

		log.info("Check if multidomain exists");
		if (data.isMultiDomain()) {
			log.info("Select show domain check box");
			page.filters().getShowDomainCheckbox().click();
		}

		log.info("Click on search button");
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		log.info("Validate presence of alert data for user_login_failure alert type for given user");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Type").contains("USER_LOGIN_FAILURE"), "Top row contains alert type as USER_LOGIN_FAILURE");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Level").contains("LOW"), "Top row contains alert level as low");
		soft.assertTrue(page.grid().getRowInfo(0).get("Parameters").contains(username), "Top row contains alert type as USER_LOGIN_FAILURE");
		soft.assertAll();
	}

	//This method will verify alert for user account disable after 5 attempts of login with wrong credentials
	@Test(description = "ALRT-18", groups = {"multiTenancy", "singleTenancy"})
	public void userDisableAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
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

		AlertPage apage = new AlertPage(driver);
		log.info("Login with Super/admin user");
		apage.getSidebar().goToPage(PAGES.ALERTS);
		log.info("Navigate to Alerts page");

		log.info("Search by basic filter for alert type : user account disabled");
		apage.filters().basicFilterBy(null, "USER_ACCOUNT_DISABLED", null, null, null, null);

		log.info("Check if multi domain exists");
		if (data.isMultiDomain()) {
			log.info("Check show domain alert checkbox");
			apage.filters().getShowDomainCheckbox().click();
			log.info("Click on search button");
			apage.filters().getSearchButton().click();
		}

		apage.grid().waitForRowsToLoad();

		log.info("Validate top row for user account disabled alert type for given user");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Type").contains("USER_ACCOUNT_DISABLED"), "Alert for disabled account is shown ");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Level").contains("HIGH"), "Disable account alert is of High level");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains(username), "Alert for user :" + username + "disabled account is shown here");
		soft.assertAll();

	}

	@Test(description = "ALRT-21", groups = {"multiTenancy", "singleTenancy"})
	public void pluginUserLoginFailure() throws Exception {
		SoftAssert soft = new SoftAssert();
		String user = Gen.randomAlphaNumeric(10);
		log.info("Create plugin user");
		rest.pluginUsers().createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);
		if (!data.isMultiDomain()) {
			log.info("Setting properties");
			String propName = "domibus.auth.unsecureLoginAllowed";
			String payload = "false";
			log.info("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			rest.properties().updateDomibusProperty(propName, payload);
			log.info("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
		}

		log.info("Send message using plugin user credentials");
		try {
			messageSender.sendMessage(user, data.getNewTestPass(), null, null);
		} catch (Exception e) {
			log.debug("Authentication exception" + e);
		}

		log.info("Login into application");
		log.info("Navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

		AlertPage page = new AlertPage(driver);
		log.info("Search data using basic filter for plugin_user_login_failure alert type");
		page.filters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);

		log.info("Check if multidomain exists");
		if (data.isMultiDomain()) {
			log.info("Select show domain check box");
			page.filters().showDomainAlert();
		}
		page.grid().waitForRowsToLoad();

		HashMap<String, String> info = page.grid().getRowInfo(0);

		log.info("Validate presence of alert with correct alert type, level ,status and plugin username in parameters");
		soft.assertTrue(info.get("Alert Type").contains("PLUGIN_USER_LOGIN_FAILURE"), "Alert for Plugin user login failure is shown ");
		soft.assertTrue(info.get("Alert Level").contains("LOW"), "Alert level is low ");
		soft.assertTrue(info.get("Alert Status").contains("SUCCESS"), "Alert status is success");
		soft.assertTrue(info.get("Parameters").contains(user), "Alert has plugin user name in parameters field");
		soft.assertAll();
	}

	@Test(description = "ALRT-22", groups = {"multiTenancy", "singleTenancy"})
	public void pluginUserDisabled() throws Exception {
		SoftAssert soft = new SoftAssert();

		String user = Gen.randomAlphaNumeric(10);
		log.info("Create plugin users");
		rest.pluginUsers().createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);

		if (!data.isMultiDomain()) {
			log.info("Setting properties");
			String propName = "domibus.auth.unsecureLoginAllowed";
			String payload = "false";
			log.info("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			rest.properties().updateDomibusProperty(propName, payload);
			log.info("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
		}

		log.info("Send message using plugin user credentials");
		for (int i = 0; i <= 5; i++) {
			try {
				messageSender.sendMessage(user, data.getNewTestPass(), null, null);
			} catch (Exception e) {
				log.debug("Authentication Exception " + e);
			}
		}

		log.info("Login into application");
		log.info("Navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

		AlertPage page = new AlertPage(driver);
		log.info("Search data using basic filter for plugin_user_account_disabled alert type");
		page.filters().basicFilterBy(null, "PLUGIN_USER_ACCOUNT_DISABLED", null, null, null, null);

		log.info("Check if multidomain exists");
		if (data.isMultiDomain()) {
			log.info("Select show domain check box");
			page.filters().showDomainAlert();
		}

		page.grid().waitForRowsToLoad();
		log.info("Validate presence of alert for plugin_user_account_disabled");
		HashMap<String, String> info = page.grid().getRowInfo(0);
		soft.assertTrue(info.get("Alert Type").contains("PLUGIN_USER_ACCOUNT_DISABLED"), "Top row alert is for Plugin user account disabled");
		soft.assertTrue(info.get("Alert Level").contains("HIGH"), "Proper alert level is shown");
		soft.assertTrue(info.get("Alert Status").contains("SUCCESS"), "Proper alert status is shown");
		soft.assertTrue(info.get("Parameters").contains(user), "Alert is shown for same user");
		soft.assertAll();
	}

	//This method will verfiy data after clicking show domain alerts checkbox for default domain
	@Test(description = "ALRT-2", groups = {"multiTenancy"})
	public void showDomainAlertChecked() throws Exception {
		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);

		log.info("Login into application and navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		log.info("wait for grid row to load");
		page.grid().waitForRowsToLoad();

		log.info("Click on show domain alert checkbox");
		aFilter.getShowDomainCheckbox().click();

		log.info("Click on search button");
		aFilter.getSearchButton().click();
		page.grid().waitForRowsToLoad();

		JSONArray userList = rest.users().getUsers(page.getDomainFromTitle());
		JSONArray messageList = rest.messages().getListOfMessages(page.getDomainFromTitle());
		JSONArray pluginuserList = rest.pluginUsers().getPluginUsers(page.getDomainFromTitle(), "BASIC");

		int rowCount = page.grid().getRowsNo();

		for (int i = 0; i < 3; i++) {
			validateDomainAlertInfo(page.grid().getRowInfo(Gen.randomNumber(rowCount)), userList, messageList, pluginuserList, soft);
		}

		soft.assertAll();
	}

	//This method will verify Alert page data for second domain with show domain alerts checked
	@Test(description = "ALRT-3", groups = {"multiTenancy"})
	public void showDomainAlertCheckedForSecDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);

		log.info("Login into application and navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		log.info("Change Domain");
		page.getDomainSelector().selectOptionByIndex(1);

		log.info("wait for grid row to load");
		page.grid().waitForRowsToLoad();
		log.info("Select show domain alert checkbox");
		aFilter.getShowDomainCheckbox().click();
		log.info("Click on search button");
		aFilter.getSearchButton().click();
		page.grid().waitForRowsToLoad();

		int rowCount = page.grid().getRowsNo();
		if (rowCount > 0) {
			String domain = page.getDomainFromTitle();
			JSONArray userList = rest.users().getUsers(domain);
			JSONArray messageList = rest.messages().getListOfMessages(domain);
			JSONArray pluginsuerList = rest.pluginUsers().getPluginUsers(domain, "BASIC");

			validateDomainAlertInfo(page.grid().getRowInfo(Gen.randomNumber(rowCount)), userList, messageList, pluginsuerList, soft);
		}
		soft.assertAll();
	}

	//This method will verify double click feature for Alerts page
	@Test(description = "ALRT-4", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickAlertRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();

		AlertPage page = new AlertPage(driver);
		log.info("Navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();


		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

		log.info("double click row 0");
		page.grid().doubleClickRow(0);

		log.info("checking the current selected row");
		soft.assertTrue(!page.hasOpenDialog(), "No dialog is visible on the page");

		soft.assertAll();
	}


	//This method will verify data of Alerts page after changing domains
	@Test(description = "ALRT-9", groups = {"multiTenancy"})
	public void changeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);
		log.info("Login into application and Navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		List<String> userName = new ArrayList<>();

		soft.assertTrue(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");
		soft.assertFalse(aFilter.getShowDomainCheckbox().isChecked(), "Check Box is not checked");

		int gridRowCount = page.grid().getRowsNo();
		ArrayList<String> superUsers = rest.users().getSuperUsernames();

		for (int i = 0; i < gridRowCount; i++) {
			String userNameStr = page.grid().getRowSpecificColumnVal(i, "Parameters").split(",")[0];
			log.info("Extract all user names available in parameters fields ");

			boolean isSuper = false;
			for (int j = 0; j < superUsers.size(); j++) {
				if (StringUtils.equalsIgnoreCase(superUsers.get(j), userNameStr)) {
					isSuper = true;
				}
			}
			soft.assertTrue(isSuper, "User is found to be super user");
		}

		log.info("Change domain");
		page.getDomainSelector().selectOptionByIndex(1);
		page.grid().waitForRowsToLoad();

		soft.assertTrue(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");
		soft.assertFalse(aFilter.getShowDomainCheckbox().isChecked(), "Check Box is not checked");

		log.info("Extract total number of count");

		int newgridRowCount = page.grid().getRowsNo();

		soft.assertEquals(gridRowCount, newgridRowCount, "Same number of rows shown");

		for (int i = 0; i < newgridRowCount; i++) {
			String userNameStr = page.grid().getRowSpecificColumnVal(i, "Parameters").split(",")[0];
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

	//	disabled due to bug EDELIVERY-4186
	//This method will download csv with/without show domain checkbox checked for all domains
	@Test(description = "ALRT-10", groups = {"multiTenancy", "singleTenancy"})
	public void downloadCsv() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();
		AlertPage page = new AlertPage(driver);

		log.info("Login with Super/Admin user and navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		HashMap<String, String> params = new HashMap<>();
		params.put("processed", "false");
		params.put("domainAlerts", "false");
		params.put("orderBy", "creationTime");
		params.put("asc", "false");

		String fileName = rest.csv().downloadGrid(RestServicePaths.ALERTS_CSV, params, null);
		log.info("downloaded file with name " + fileName);

		page.grid().waitForRowsToLoad();
		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		log.info("checking info in grid against the file");
		page.alertsGrid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}

	//This method will verify data for Admin user
	@Test(description = "ALRT-12", groups = {"multiTenancy", "singleTenancy"})
	public void dataForAdminUser() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = null;
		if (data.isMultiDomain()) {
			List<String> domains = rest.getDomainCodes();
			log.debug("got domains: " + domains);

			int index = new Random().nextInt(domains.size());

			domain = domains.get(index);
			log.info("will run for domain " + domain);
		}

		String user = Gen.randomAlphaNumeric(5);
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), domain);
		log.info("created user " + user);

		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);

		log.info("Login and navigate to Alert page");
		login(user, data.defaultPass()).getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();


		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();

		log.info("Check absence of Show Domain Alert check box");
		soft.assertFalse(aFilter.getShowDomainCheckbox().isPresent(), "Checkbox is not present for Admin user");

		int rowCount = page.grid().getRowsNo();


		if (rowCount > 0) {
			JSONArray userList = rest.users().getUsers(domain);
			JSONArray messageList = rest.messages().getListOfMessages(domain);
			JSONArray pluginuserList = rest.pluginUsers().getPluginUsers(domain, "BASIC");

			for (int i = 0; i < 2; i++) {
				validateDomainAlertInfo(page.grid().getRowInfo(Gen.randomNumber(rowCount)), userList, messageList, pluginuserList, soft);
			}
		}

		soft.assertAll();
	}

	//This method will verify absence of super admin records and present record belongs to current domain
	@Test(description = "ALRT-13", groups = {"multiTenancy"})
	public void superAdminrecordAbsenceForAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = null;
		if (data.isMultiDomain()) {
			log.info("selecting random domain");
			List<String> domains = rest.getDomainCodes();
			log.debug("got domains: " + domains);

			int index = new Random().nextInt(domains.size());

			domain = domains.get(index);
			log.info("will run for domain " + domain);
		}

		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);

		String user = Gen.randomAlphaNumeric(10);
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), domain);
		log.info("created user " + user);

		log.info("Login with created user and naviagte to Alerts page");
		login(user, data.defaultPass()).getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		soft.assertFalse(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");

		int recordCount = page.grid().getRowsNo();
		if (recordCount <= 0) {
			log.info("no records to verify, exiting now");
			return;
		}

		List<String> superList = rest.users().getSuperUsernames();
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
	@Test(description = "ALRT-28", groups = {"multiTenancy", "singleTenancy"})
	public void defaultDataInSearchFilter() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();

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
	@Test(description = "ALRT-29", groups = {"multiTenancy", "singleTenancy"})
	public void checkValidationForAlertId() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();

		log.info("Login into application and navigate to Alert page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		AlertFilters aFilter = new AlertFilters(driver);

		log.info("Click on advance link");
		aFilter.getAdvancedLink().click();

		log.info("Create list of correct and incorrect data");
		List<String> correctDataArray = Arrays.asList("1234567890123456789", "347362", "1");
		List<String> incorrectDataArray = Arrays.asList("random", "0random", "0000000000000000000", "12345678901234567890", "random1", "54 656", "$#%", "-989", "+787");

		for (int i = 0; i < correctDataArray.size(); i++) {
			log.info("Pass correct value :" + correctDataArray.get(i));
			aFilter.getAlertIdInput().fill(correctDataArray.get(i));

			soft.assertFalse(aFilter.isAlertIdValidationMessageVisible(), "Validation message is not visible for input " + correctDataArray.get(i));

			log.info("Verify status of search button as enabled");
			soft.assertTrue(aFilter.getSearchButton().isEnabled(), "Button is enabled");
		}

		for (int i = 0; i < incorrectDataArray.size(); i++) {
			log.info("Pass incorrect value :" + incorrectDataArray.get(i));
			aFilter.getAlertIdInput().fill(incorrectDataArray.get(i));

			log.info("Verify presence of validation message under alert id field");
			soft.assertTrue(aFilter.isAlertIdValidationMessageVisible(), "Validation message IS visible for input " + incorrectDataArray.get(i));
			soft.assertEquals(aFilter.getAlertValidationMess(), DMessages.ALERT_ID_INPUT_VALIDATION_MESSAGE, "Correct validation message is shown");

			soft.assertFalse(aFilter.getSearchButton().isEnabled(), "Button is not enabled");
		}

		soft.assertAll();
	}

	//This method will verfiy feature for Processed for Super alerts
	@Test(description = "ALRT-32", groups = {"multiTenancy"})
	public void checkProcessed() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		log.info("Login into application and navigate to Alert page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		if (data.isMultiDomain()) {
			log.info("Showing domain alerts");
			page.filters().showDomainAlert();
			page.grid().waitForRowsToLoad();
		}

		AlertFilters aFilter = new AlertFilters(driver);

		log.info("Check alert count when showDomain alert is false");
		int totalCount = rest.alerts().getAlerts(domain, false, true).length();

		if (totalCount <= 0) {
			throw new SkipException("No alerts present");
		}


		log.info("Verify disabled status of save and cancel button");
		soft.assertFalse(page.getSaveButton().isEnabled(), "Check Save button is disabled");
		soft.assertFalse(page.getCancelButton().isEnabled(), "Check Cancel button is disabled");
		soft.assertFalse(page.getDeleteButton().isEnabled(), "Check Delete button is disabled");

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().checkBoxWithLabel("Alert Id");

		log.info("Check processed checkbox for first row");
		page.alertsGrid().markAsProcessed(0);

		String alertId = page.grid().getRowSpecificColumnVal(0, "Alert Id");

		soft.assertTrue(page.getSaveButton().isEnabled(), "Check Save button is enabled");
		soft.assertTrue(page.getCancelButton().isEnabled(), "Check Cancel button is enabled");
		soft.assertTrue(page.getDeleteButton().isEnabled(), "Check Delete button is enabled");

		log.info("Click on save button and then ok from confirmation pop up");
		page.getSaveButton().click();
		new Dialog(driver).confirm();

		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.ALERT_UPDATE_SUCCESS_MESSAGE, "Correct update message is shown");
		page.grid().waitForRowsToLoad();


		log.info("Check total count as 1 less than before");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == totalCount - 1, "Check all alert size 1 less than before");

		log.info("Select processed in search filter ");
		aFilter.getProcessedSelect().selectOptionByText("PROCESSED");
		log.info("Click on search button");
		aFilter.getSearchButton().click();
		page.grid().waitForRowsToLoad();

		List<String> ids = page.grid().getValuesOnColumn("Alert Id");

		soft.assertTrue(ids.contains(alertId), "Processed record is present after event completion");

		soft.assertAll();
	}

	/* disabled because EDELIVERY-4186 */
	@Test(description = "ALRT-20", groups = {"multiTenancy", "singleTenancy"})
	public void verifyHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		log.info("Customized location for download");

		page.grid().waitForRowsToLoad();
		String filePath = page.pressSaveCsvAndSaveFile();


		log.info("Check if file is downloaded at given location");
		soft.assertTrue(new File(filePath).exists(), "File is downloaded successfully");

		log.info("Extract complete path for downloaded file");
		String completeFilePath = filePath;

		log.info("Click on show link");
		page.grid().getGridCtrl().showCtrls();

		log.info("Click on All link to show all available column headers");
		page.grid().getGridCtrl().showAllColumns();

		log.info("Compare headers from downloaded csv and grid");
		page.grid().checkCSVvsGridHeaders(completeFilePath, soft);
		soft.assertAll();
	}

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
	public void checkAditionalFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("Navigating to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		log.info("waiting for grid to load");
		page.grid().waitForRowsToLoad();

		AlertFilters filter = new AlertFilters(driver);
		log.info("iterating trough alert types");

		List<String> options = filter.getAlertTypeSelect().getOptionsTexts();

		for (String option : options) {
			log.info("checking alert type " + option);
			filter.getAlertTypeSelect().selectOptionByText(option);
			List<String> xFilters = filter.getXFilterNames();

			log.debug(xFilters.toString());

		}


		soft.assertAll();
	}

}