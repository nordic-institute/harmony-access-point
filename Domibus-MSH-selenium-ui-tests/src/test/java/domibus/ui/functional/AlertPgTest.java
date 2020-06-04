package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import utils.BaseTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Alert.AlertFilters;
import pages.Alert.AlertPage;
import utils.Generator;

import java.util.HashMap;
import java.util.List;

public class AlertPgTest extends BaseTest {

	//This method will do Search using Basic filters
	@Test(description = "ALRT-5", groups = {"multiTenancy", "singleTenancy"})
	public void searchBasicFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
		AlertPage apage = new AlertPage(driver);
		if (data.isIsMultiDomain()) {
			apage.filters().showDomainAlert();
		}

		log.info("Number of records : " + apage.grid().getRowsNo());
		log.info("Getting all listed alert info");
		log.info("Alert type for top row : " + apage.grid().getRowInfo(0).get("Alert Type"));
		String beforeSearchalertType = apage.grid().getRowInfo(0).get("Alert Type");
		List<HashMap<String, String>> allRowInfo = apage.grid().getAllRowInfo();
		HashMap<String, String> fAlert = allRowInfo.get(0);
		log.info("Basic filtering by " + fAlert);
		apage.filters().basicFilterBy(null, fAlert.get("Alert Type"), fAlert.get("Alert Status"),
				fAlert.get("Alert level"), fAlert.get("Creation Time"), null);
		apage.grid().waitForRowsToLoad();
		String afterSearchAlertType = apage.grid().getRowInfo(0).get("Alert Type");
		soft.assertTrue(beforeSearchalertType.equals(afterSearchAlertType), "After and before search records are same");
		soft.assertAll();

	}

	//This method will do search operation using advance filters
	@Test(description = "ALRT-6", groups = {"multiTenancy", "singleTenancy"})
	public void searchAdvanceFilters() throws Exception {
		SoftAssert soft = new SoftAssert();


		String username = getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		if (data.isIsMultiDomain()) {
			page.filters().showDomainAlert();
			page.grid().waitForRowsToLoad();
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
		DomibusPage page = new DomibusPage(driver);
		log.info("Login into application and navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
		AlertPage apage = new AlertPage(driver);
		log.info("Search using basic filters");
		apage.filters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);
		apage.grid().waitForRowsToLoad();
		log.info("Validate grid count as zero");
		soft.assertTrue(apage.grid().getPagination().getTotalItems() == 0, "No search result exist");
		soft.assertAll();
	}

	//This method will validate presence of all records after deletion of all search criteria
	@Test(description = "ALRT-8", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void deleteSearchCriteria() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("Login into application and navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		if (data.isIsMultiDomain()) {
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
		if (data.isIsMultiDomain()) {
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
		login(getUser(null, DRoles.ADMIN, true, false, true).getString("userName"), data.defaultPass())
				.getSidebar().goToPage(PAGES.ALERTS);
		log.info("Validate non availability of Show domain alert checkbox for Admin user");
		soft.assertFalse(filters.getShowDomainCheckbox().isPresent(), "CheckBox is not present in case of Admin User");
		soft.assertAll();
	}

	//This method will verify alert for message status change
	@Test(description = "ALRT-14", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void msgStatusChangeAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> ids = getMessageIDsWithStatus(null, "SEND_FAILURE");
		if (ids.size() < 1) {
			throw new SkipException("no messages in SEND_FAILURE state");
		}
		String messID = ids.get(0);

		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
		log.info("Navigate to Alerts page");

		AlertPage apage = new AlertPage(driver);
		log.info("Search data using Msg_status_changed alert type");
		apage.filters().basicFilterBy(null, "MSG_STATUS_CHANGED", null, null, null, null);

		apage.filters().getMsgIdInput().fill(messID);

		log.info("Check if Multidomain exists");
		if (data.isIsMultiDomain()) {
			log.info("Click on Show domain checkbox");
			apage.filters().getShowDomainCheckbox().click();
		}

		log.info("Click on search button");
		apage.filters().getSearchButton().click();
		apage.grid().waitForRowsToLoad();

		log.info("Validate data for given message id,status ,alert type ,alert status and level");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Type").contains("MSG_STATUS_CHANGED"), "Top row contains alert type as Msg_Status_Changed");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Level").contains("HIGH"), "Top row contains alert level as High");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains(messID), "Top row contains alert for message status changed for :" + messID);
		soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains("SEND_FAILURE"), "Top row contains alert for message status as Send_failure");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains("SEND_ENQUEUED"), "Top row contains alert for message status as Send_Enqueued");
		soft.assertAll();

	}

	//This method will verify alert for user login failure case
	@Test(description = "ALRT-17", groups = {"multiTenancy", "singleTenancy"})
	public void userLoginFailureAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");

		log.info("Login into application");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
		log.info("Navigate to Alerts page");

		AlertPage apage = new AlertPage(driver);
		log.info("Search data using basic filter for user_login_failure alert type");
		apage.filters().basicFilterBy(null, "USER_LOGIN_FAILURE", null, null, null, null);
		log.info("Check if multidomain exists");
		if (data.isIsMultiDomain()) {
			log.info("Select show domain check box");
			apage.filters().getShowDomainCheckbox().click();
		}

		log.info("Click on search button");
		apage.filters().getSearchButton().click();
		apage.grid().waitForRowsToLoad();

		log.info("Validate presence of alert data for user_login_failure alert type for given user");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Type").contains("USER_LOGIN_FAILURE"), "Top row contains alert type as USER_LOGIN_FAILURE");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Level").contains("LOW"), "Top row contains alert level as low");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains(username), "Top row contains alert type as USER_LOGIN_FAILURE");
		soft.assertAll();
	}

	//This method will verify alert for user account disable after 5 attempts of login with wrong credentials
	@Test(description = "ALRT-18", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void userDisableAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = getUsername(null, DRoles.USER, true, false, false);
		log.info("Try to login with wrong password for 5 times so that user account gets disabled");
		for (int i = 0; i < 6; i++) {
			rest.login(username, "wrong");
		}

		JSONArray users = rest.getUsers(null);
		for (int i = 0; i < users.length(); i++) {
			JSONObject obj = users.getJSONObject(i);
			if (obj.getString("userName").equalsIgnoreCase(username)) {
				soft.assertFalse(obj.getBoolean("active"), "User has been disabled");
			}
		}

		log.info("Login with Super/admin user");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
		log.info("Navigate to Alerts page");

		AlertPage apage = new AlertPage(driver);
		log.info("Search by basic filter for alert type : user account disabled");
		apage.filters().basicFilterBy(null, "USER_ACCOUNT_DISABLED", null, null, null, null);

		log.info("Check if multi domain exists");
		if (data.isIsMultiDomain()) {
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

	@Test(description = "ALRT-21", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void pluginUserLoginFailure() throws Exception {
		SoftAssert soft = new SoftAssert();
		String user = Generator.randomAlphaNumeric(10);
		log.info("Create plugin user");
		rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);
		if (!data.isIsMultiDomain()) {
			log.info("Setting properties");
			HashMap<String, String> params = new HashMap<>();
			String propName = "domibus.auth.unsecureLoginAllowed";
			String payload = "false";
			params.put("name", propName);
			log.info("Property details before modification" + rest.getDomibusPropertyDetail(params));
			rest.updateDomibusProperty(propName, params, payload);
			log.info("Property details after modification" + rest.getDomibusPropertyDetail(params));
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
		if (data.isIsMultiDomain()) {
			log.info("Select show domain check box");
			page.filters().showDomainAlert();
		}
		page.grid().waitForRowsToLoad();
		log.info("Validate presence of alert with correct alert type, level ,status and plugin username in parameters");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Type").contains("PLUGIN_USER_LOGIN_FAILURE"), "Alert for Plugin user login failure is shown ");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Level").contains("LOW"), "Alert level is low ");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Alert status is success");
		soft.assertTrue(page.grid().getRowInfo(0).get("Parameters").contains(user), "Alert has plugin user name in parameters field");
		soft.assertAll();
	}

	@Test(description = "ALRT-22", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void pluginUserDisabled() throws Exception {
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		log.info("Create plugin users");
		rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);

		if (!data.isIsMultiDomain()) {
			log.info("Setting properties");
			HashMap<String, String> params = new HashMap<>();
			String propName = "domibus.auth.unsecureLoginAllowed";
			String payload = "false";
			params.put("name", propName);
			log.info("Property details before modification" + rest.getDomibusPropertyDetail(params));
			rest.updateDomibusProperty(propName, params, payload);
			log.info("Property details after modification" + rest.getDomibusPropertyDetail(params));
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
		if (data.isIsMultiDomain()) {
			log.info("Select show domain check box");
			page.filters().showDomainAlert();
		}

		page.grid().waitForRowsToLoad();
		log.info("Validate presence of alert for plugin_user_account_disabled");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Type").contains("PLUGIN_USER_ACCOUNT_DISABLED"), "Top row alert is for Plugin user account disabled");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Level").contains("HIGH"), "Proper alert level is shown");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Status").contains("SUCCESS"), "Proper alert status is shown");
		soft.assertTrue(page.grid().getRowInfo(0).get("Parameters").contains(user), "Alert is shown for same user");
		soft.assertAll();
	}

}
