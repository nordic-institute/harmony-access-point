package domibus.ui.functional;

import io.qameta.allure.*;
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

@Epic("Alerts")
@Feature("Functional")
public class AlertPgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.ALERTS);


	private void validateDomainAlertInfo(HashMap<String, String> rowInfo, JSONArray userList, JSONArray messageList, JSONArray pluginuserList, SoftAssert soft) throws Exception {
		Allure.step("Validating alert: " + rowInfo.toString());
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
	/*  ALRT-1 - Login as super admin and open Alerts page  */
	@Description("ALRT-1 - Login as super admin and open Alerts page")
	@Link(name = "EDELIVERY-5283", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5283")
	@AllureId("ALRT-1")
	@Test(description = "ALRT-1", groups = {"multiTenancy", "singleTenancy"})
	public void openAlertsPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		Allure.step("Checking page title");
		log.info("Checking page title");
		soft.assertEquals(page.getTitle(), descriptorObj.getString("title"), "Page title is correct");

		Allure.step("checking basic filter presence");
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
	/*  ALRT-5 - Filter alerts using basic filters  */
	@Description("ALRT-5 - Filter alerts using basic filters")
	@Link(name = "EDELIVERY-5287", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5287")
	@AllureId("ALRT-5")
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

		Allure.step("Number of records : " + page.grid().getPagination().getTotalItems());
		log.info("Number of records : " + page.grid().getPagination().getTotalItems());
		Allure.step("Getting all listed alert info");
		log.info("Getting all listed alert info");

		HashMap<String, String> fAlert = page.grid().getRowInfo(0);

		Allure.step("Basic filtering by " + fAlert);
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
	/*  ALRT-6 - Filter alerts using advanced filters  */
	@Description("ALRT-6 - Filter alerts using advanced filters")
	@Link(name = "EDELIVERY-5288", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5288")
	@AllureId("ALRT-6")
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

		Allure.step("Getting all listed alert info");
		log.info("Getting all listed alert info");

		HashMap<String, String> fAlert = page.grid().getRowInfo(0);
		String beforeSearchAlertType = fAlert.get("Alert Type");
		Allure.step("Alert type for top row : " + beforeSearchAlertType);
		log.info("Alert type for top row : " + beforeSearchAlertType);

		Allure.step("Advance filtering by " + fAlert);
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
	/*  ALRT-7 - Filter alerts so that there are no results  */
	@Description("ALRT-7 - Filter alerts so that there are no results")
	@Link(name = "EDELIVERY-5289", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5289")
	@AllureId("ALRT-7")
	@Test(description = "ALRT-7", groups = {"multiTenancy", "singleTenancy"})
	public void emptySearchResult() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to Alerts page");
		log.info("Login into application and navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		Allure.step("Search using basic filters");
		log.info("Search using basic filters");
		page.filters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);
		page.grid().waitForRowsToLoad();

		Allure.step("Validate grid count as zero");
		log.info("Validate grid count as zero");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == 0, "No search result exist");
		soft.assertAll();
	}

	//This method will validate presence of all records after deletion of all search criteria
	/*  ALRT-8 - Delete all criteria and press Search  */
	@Description("ALRT-8 - Delete all criteria and press Search")
	@Link(name = "EDELIVERY-5290", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5290")
	@AllureId("ALRT-8")
	@Test(description = "ALRT-8", groups = {"multiTenancy", "singleTenancy"})
	public void deleteSearchCriteria() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Login into application and navigate to Alerts page");
		log.info("Login into application and navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		Allure.step("Wait for grid row to load ");
		log.info("Wait for grid row to load ");
		page.grid().waitForRowsToLoad();

		Allure.step("Search using basic filter");
		log.info("Search using basic filter");
		int prevCount = page.grid().getPagination().getTotalItems();
		Allure.step("Previous count of grid rows:" + prevCount);
		log.info("Previous count of grid rows:" + prevCount);

		page.filters().basicFilterBy(null, "CERT_EXPIRED", null, null, null, null);
		page.grid().waitForRowsToLoad();

		Allure.step("Validate Grid row count as zero ");
		log.info("Validate Grid row count as zero ");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == 0, "No search result exist");

		Allure.step("Refresh page");
		log.info("Refresh page");
		page.refreshPage();
		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		Allure.step("Wait for grid row to load ");
		log.info("Wait for grid row to load ");
		page.grid().waitForRowsToLoad();

		Allure.step("Validate actual grid row count ");
		log.info("Validate actual grid row count ");
		Allure.step("Current grid row count:" + page.grid().getPagination().getTotalItems());
		log.info("Current grid row count:" + page.grid().getPagination().getTotalItems());
		soft.assertTrue(page.grid().getPagination().getTotalItems() == prevCount, "All search result exist");
		soft.assertAll();
	}

	//This method will validate presence of show domain alert check box in case of super admin only
	/*  ALRT-11 - Admin opens Alerts page  */
	@Description("ALRT-11 - Admin opens Alerts page")
	@Link(name = "EDELIVERY-5293", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5293")
	@AllureId("ALRT-11")
	@Test(description = "ALRT-11", groups = {"multiTenancy"})
	public void showDomainAlert() throws Exception {
		SoftAssert soft = new SoftAssert();
		Allure.step("Login into application with super admin credentials and navigate to Alerts page");
		log.info("Login into application with super admin credentials and navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);
		AlertPage apage = new AlertPage(driver);
		AlertFilters filters = apage.filters();
		Allure.step("Check presence of Show domain checkbox");
		log.info("Check presence of Show domain checkbox");
		soft.assertTrue(filters.getShowDomainCheckbox().isPresent(), "CheckBox is  present in case of super User");
		Allure.step("Logout from application");
		log.info("Logout from application");
		logout();
		Allure.step("Login with admin credentials");
		log.info("Login with admin credentials");
		login(rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName"), data.defaultPass())
				.getSidebar().goToPage(PAGES.ALERTS);
		Allure.step("Validate non availability of Show domain alert checkbox for Admin user");
		log.info("Validate non availability of Show domain alert checkbox for Admin user");
		soft.assertFalse(filters.getShowDomainCheckbox().isPresent(), "CheckBox is not present in case of Admin User");
		soft.assertAll();
	}

	//This method will verify alert for message status change
	/*  ALRT-14 - Check data listed for MSGSTATUSCHANGED alert  */
	@Description("ALRT-14 - Check data listed for MSGSTATUSCHANGED alert")
	@Link(name = "EDELIVERY-5296", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5296")
	@AllureId("ALRT-14")
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
		Allure.step("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		page.grid().waitForRowsToLoad();

		Allure.step("Search data using Msg_status_changed alert type");
		log.info("Search data using Msg_status_changed alert type");
		page.filters().basicFilterBy(null, "MSG_STATUS_CHANGED", null, null, null, null);

		page.filters().getMsgIdInput().fill(messID);

		Allure.step("Check if Multidomain exists");
		log.info("Check if Multidomain exists");
		if (data.isMultiDomain()) {
			Allure.step("Click on Show domain checkbox");
			log.info("Click on Show domain checkbox");
			page.filters().getShowDomainCheckbox().check();
		}

		Allure.step("Click on search button");
		log.info("Click on search button");
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Allure.step("Validate data for given message id,status ,alert type ,alert status and level");
		log.info("Validate data for given message id,status ,alert type ,alert status and level");
		List<String> allInfo = page.grid().getValuesOnColumn("Parameters");

		for (String info : allInfo) {
			soft.assertTrue(info.contains(messID), "Row contains alert for message status changed for :" + messID);
			soft.assertTrue(info.contains("SEND_FAILURE"), "Row contains alert for message status changed for :" + messID);
		}

		soft.assertAll();

	}

	//This method will verify alert for user login failure case
	/*  ALRT-17 - Check data for USERLOGINFAILURE alert  */
	@Description("ALRT-17 - Check data for USERLOGINFAILURE alert")
	@Link(name = "EDELIVERY-5299", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5299")
	@AllureId("ALRT-17")
	@Test(description = "ALRT-17", groups = {"multiTenancy", "singleTenancy"})
	public void userLoginFailureAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		rest.login(username, "wrong");


		Allure.step("Login into application");
		log.info("Login into application");
		Allure.step("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		Allure.step("Search data using basic filter for user_login_failure alert type");
		log.info("Search data using basic filter for user_login_failure alert type");
		page.filters().basicFilterBy(null, "USER_LOGIN_FAILURE", null, null, null, null);
		page.grid().waitForRowsToLoad();

		Allure.step("Check if multidomain exists");
		log.info("Check if multidomain exists");
		if (data.isMultiDomain()) {
			Allure.step("Select show domain check box");
			log.info("Select show domain check box");
			page.filters().getShowDomainCheckbox().click();
		}

		Allure.step("Click on search button");
		log.info("Click on search button");
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Allure.step("Validate presence of alert data for user_login_failure alert type for given user");
		log.info("Validate presence of alert data for user_login_failure alert type for given user");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Type").contains("USER_LOGIN_FAILURE"), "Top row contains alert type as USER_LOGIN_FAILURE");
		soft.assertTrue(page.grid().getRowInfo(0).get("Alert Level").contains("LOW"), "Top row contains alert level as low");
		soft.assertTrue(page.grid().getRowInfo(0).get("Parameters").contains(username), "Top row contains alert type as USER_LOGIN_FAILURE");
		soft.assertAll();
	}

	//This method will verify alert for user account disable after 5 attempts of login with wrong credentials
	/*  ALRT-18 - Check data for USERACCOUNTDISABLED  */
	@Description("ALRT-18 - Check data for USERACCOUNTDISABLED")
	@Link(name = "EDELIVERY-5300", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5300")
	@AllureId("ALRT-18")
	@Test(description = "ALRT-18", groups = {"multiTenancy", "singleTenancy"})
	public void userDisableAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, false);
		Allure.step("Try to login with wrong password for 5 times so that user account gets disabled");
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
		Allure.step("Login with Super/admin user");
		log.info("Login with Super/admin user");
		apage.getSidebar().goToPage(PAGES.ALERTS);
		Allure.step("Navigate to Alerts page");
		log.info("Navigate to Alerts page");

		Allure.step("Search by basic filter for alert type : user account disabled");
		log.info("Search by basic filter for alert type : user account disabled");
		apage.filters().basicFilterBy(null, "USER_ACCOUNT_DISABLED", null, null, null, null);

		Allure.step("Check if multi domain exists");
		log.info("Check if multi domain exists");
		if (data.isMultiDomain()) {
			Allure.step("Check show domain alert checkbox");
			log.info("Check show domain alert checkbox");
			apage.filters().getShowDomainCheckbox().click();
			Allure.step("Click on search button");
			log.info("Click on search button");
			apage.filters().getSearchButton().click();
		}

		apage.grid().waitForRowsToLoad();

		Allure.step("Validate top row for user account disabled alert type for given user");
		log.info("Validate top row for user account disabled alert type for given user");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Type").contains("USER_ACCOUNT_DISABLED"), "Alert for disabled account is shown ");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Alert Level").contains("HIGH"), "Disable account alert is of High level");
		soft.assertTrue(apage.grid().getRowInfo(0).get("Parameters").contains(username), "Alert for user :" + username + "disabled account is shown here");
		soft.assertAll();

	}

	/*  ALRT-21 - Check data for PLUGINUSERLOGINFAILURE  */
	@Description("ALRT-21 - Check data for PLUGINUSERLOGINFAILURE")
	@Link(name = "EDELIVERY-5459", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5459")
	@AllureId("ALRT-21")
	@Test(description = "ALRT-21", groups = {"multiTenancy", "singleTenancy"})
	public void pluginUserLoginFailure() throws Exception {
		SoftAssert soft = new SoftAssert();
		String user = Gen.randomAlphaNumeric(10);
		Allure.step("Create plugin user");
		log.info("Create plugin user");
		rest.pluginUsers().createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);
		if (!data.isMultiDomain()) {
			Allure.step("Setting properties");
			log.info("Setting properties");
			String propName = "domibus.auth.unsecureLoginAllowed";
			String payload = "false";
			Allure.step("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			log.info("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			rest.properties().updateDomibusProperty(propName, payload);
			Allure.step("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
			log.info("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
		}

		Allure.step("Send message using plugin user credentials");
		log.info("Send message using plugin user credentials");
		try {
			messageSender.sendMessage(user, data.getNewTestPass(), null, null);
		} catch (Exception e) {
			Allure.step("Authentication exception" + e);
			log.debug("Authentication exception" + e);
		}

		Allure.step("Login into application");
		log.info("Login into application");
		Allure.step("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

		AlertPage page = new AlertPage(driver);
		Allure.step("Search data using basic filter for plugin_user_login_failure alert type");
		log.info("Search data using basic filter for plugin_user_login_failure alert type");
		page.filters().basicFilterBy(null, "PLUGIN_USER_LOGIN_FAILURE", null, null, null, null);

		Allure.step("Check if multidomain exists");
		log.info("Check if multidomain exists");
		if (data.isMultiDomain()) {
			Allure.step("Select show domain check box");
			log.info("Select show domain check box");
			page.filters().showDomainAlert();
		}
		page.grid().waitForRowsToLoad();

		HashMap<String, String> info = page.grid().getRowInfo(0);

		Allure.step("Validate presence of alert with correct alert type, level ,status and plugin username in parameters");
		log.info("Validate presence of alert with correct alert type, level ,status and plugin username in parameters");
		soft.assertTrue(info.get("Alert Type").contains("PLUGIN_USER_LOGIN_FAILURE"), "Alert for Plugin user login failure is shown ");
		soft.assertTrue(info.get("Alert Level").contains("LOW"), "Alert level is low ");
		soft.assertTrue(info.get("Alert Status").contains("SUCCESS"), "Alert status is success");
		soft.assertTrue(info.get("Parameters").contains(user), "Alert has plugin user name in parameters field");
		soft.assertAll();
	}

	/*  ALRT-22 - Check data for PLUGINUSERACCOUNTDISABLED  */
	@Description("ALRT-22 - Check data for PLUGINUSERACCOUNTDISABLED")
	@Link(name = "EDELIVERY-5460", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5460")
	@AllureId("ALRT-22")
	@Test(description = "ALRT-22", groups = {"multiTenancy", "singleTenancy"})
	public void pluginUserDisabled() throws Exception {
		SoftAssert soft = new SoftAssert();

		String user = Gen.randomAlphaNumeric(10);
		Allure.step("Create plugin users");
		log.info("Create plugin users");
		rest.pluginUsers().createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);

		if (!data.isMultiDomain()) {
			Allure.step("Setting properties");
			log.info("Setting properties");
			String propName = "domibus.auth.unsecureLoginAllowed";
			String payload = "false";
			Allure.step("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			log.info("Property details before modification" + rest.properties().getDomibusPropertyDetail(propName));
			rest.properties().updateDomibusProperty(propName, payload);
			Allure.step("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
			log.info("Property details after modification" + rest.properties().getDomibusPropertyDetail(propName));
		}

		Allure.step("Send message using plugin user credentials");
		log.info("Send message using plugin user credentials");
		for (int i = 0; i <= 5; i++) {
			try {
				messageSender.sendMessage(user, data.getNewTestPass(), null, null);
			} catch (Exception e) {
				Allure.step("Authentication Exception " + e);
				log.debug("Authentication Exception " + e);
			}
		}

		Allure.step("Login into application");
		log.info("Login into application");
		Allure.step("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.ALERTS);

		AlertPage page = new AlertPage(driver);
		Allure.step("Search data using basic filter for plugin_user_account_disabled alert type");
		log.info("Search data using basic filter for plugin_user_account_disabled alert type");
		page.filters().basicFilterBy(null, "PLUGIN_USER_ACCOUNT_DISABLED", null, null, null, null);

		Allure.step("Check if multidomain exists");
		log.info("Check if multidomain exists");
		if (data.isMultiDomain()) {
			Allure.step("Select show domain check box");
			log.info("Select show domain check box");
			page.filters().showDomainAlert();
		}

		page.grid().waitForRowsToLoad();
		Allure.step("Validate presence of alert for plugin_user_account_disabled");
		log.info("Validate presence of alert for plugin_user_account_disabled");
		HashMap<String, String> info = page.grid().getRowInfo(0);
		soft.assertTrue(info.get("Alert Type").contains("PLUGIN_USER_ACCOUNT_DISABLED"), "Top row alert is for Plugin user account disabled");
		soft.assertTrue(info.get("Alert Level").contains("HIGH"), "Proper alert level is shown");
		soft.assertTrue(info.get("Alert Status").contains("SUCCESS"), "Proper alert status is shown");
		soft.assertTrue(info.get("Parameters").contains(user), "Alert is shown for same user");
		soft.assertAll();
	}

	//This method will verfiy data after clicking show domain alerts checkbox for default domain
	/*  ALRT-20 - Verify headers in downloaded CSV file  */
	@Description("ALRT-20 - Verify headers in downloaded CSV file")
	@Link(name = "EDELIVERY-5302", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5302")
	@AllureId("ALRT-2")
	@Test(description = "ALRT-2", groups = {"multiTenancy"})
	public void showDomainAlertChecked() throws Exception {
		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);

		Allure.step("Login into application and navigate to Alerts page");
		log.info("Login into application and navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		Allure.step("wait for grid row to load");
		log.info("wait for grid row to load");
		page.grid().waitForRowsToLoad();

		Allure.step("Click on show domain alert checkbox");
		log.info("Click on show domain alert checkbox");
		aFilter.getShowDomainCheckbox().click();

		Allure.step("Click on search button");
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
	/*  ALRT-3 - Super admin changes domain and ticks Show Domain alerts  */
	@Description("ALRT-3 - Super admin changes domain and ticks Show Domain alerts")
	@Link(name = "EDELIVERY-5285", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5285")
	@AllureId("ALRT-3")
	@Test(description = "ALRT-3", groups = {"multiTenancy"})
	public void showDomainAlertCheckedForSecDomain() throws Exception {
		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);

		Allure.step("Login into application and navigate to Alerts page");
		log.info("Login into application and navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		Allure.step("Change Domain");
		log.info("Change Domain");
		page.getDomainSelector().selectOptionByIndex(1);

		Allure.step("wait for grid row to load");
		log.info("wait for grid row to load");
		page.grid().waitForRowsToLoad();
		Allure.step("Select show domain alert checkbox");
		log.info("Select show domain alert checkbox");
		aFilter.getShowDomainCheckbox().click();
		Allure.step("Click on search button");
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
	/*  ALRT-4 - Doubleclik on one alert  */
	@Description("ALRT-4 - Doubleclik on one alert")
	@Link(name = "EDELIVERY-5286", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5286")
	@AllureId("ALRT-4")
	@Test(description = "ALRT-4", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickAlertRow() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();

		AlertPage page = new AlertPage(driver);
		Allure.step("Navigate to Alerts page");
		log.info("Navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		if (data.isMultiDomain()) {
			page.filters().showDomainAlert();
		}

		Allure.step("Wait for grid row to load");
		log.info("Wait for grid row to load");
		page.grid().waitForRowsToLoad();


		if (page.grid().getRowsNo() < 1) {
			throw new SkipException("Not enough rows");
		}

		Allure.step("double click row 0");
		log.info("double click row 0");
		page.grid().doubleClickRow(0);

		Allure.step("checking the current selected row");
		log.info("checking the current selected row");
		soft.assertTrue(!page.hasOpenDialog(), "No dialog is visible on the page");

		soft.assertAll();
	}


	//This method will verify data of Alerts page after changing domains
	/*  ALRT-9 - Super admin changes current domain  */
	@Description("ALRT-9 - Super admin changes current domain")
	@Link(name = "EDELIVERY-5291", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5291")
	@AllureId("ALRT-9")
	@Test(description = "ALRT-9", groups = {"multiTenancy"})
	public void changeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);
		Allure.step("Login into application and Navigate to Alerts page");
		log.info("Login into application and Navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);

		List<String> userName = new ArrayList<>();

		soft.assertTrue(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");
		soft.assertFalse(aFilter.getShowDomainCheckbox().isChecked(), "Check Box is not checked");

		int gridRowCount = page.grid().getRowsNo();
		ArrayList<String> superUsers = rest.users().getSuperUsernames();

		for (int i = 0; i < gridRowCount; i++) {
			String userNameStr = page.grid().getRowSpecificColumnVal(i, "Parameters").split(",")[0];
			Allure.step("Extract all user names available in parameters fields ");
			log.info("Extract all user names available in parameters fields ");

			boolean isSuper = false;
			for (int j = 0; j < superUsers.size(); j++) {
				if (StringUtils.equalsIgnoreCase(superUsers.get(j), userNameStr)) {
					isSuper = true;
				}
			}
			soft.assertTrue(isSuper, "User is found to be super user");
		}

		Allure.step("Change domain");
		log.info("Change domain");
		page.getDomainSelector().selectOptionByIndex(1);
		page.grid().waitForRowsToLoad();

		soft.assertTrue(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");
		soft.assertFalse(aFilter.getShowDomainCheckbox().isChecked(), "Check Box is not checked");

		Allure.step("Extract total number of count");
		log.info("Extract total number of count");

		int newgridRowCount = page.grid().getRowsNo();

		soft.assertEquals(gridRowCount, newgridRowCount, "Same number of rows shown");

		for (int i = 0; i < newgridRowCount; i++) {
			String userNameStr = page.grid().getRowSpecificColumnVal(i, "Parameters").split(",")[0];
			Allure.step("Extract all user names available in parameters fields ");
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
	/*  ALRT-10 - Admin downloads list of alerts  */
	@Description("ALRT-10 - Admin downloads list of alerts")
	@Link(name = "EDELIVERY-5292", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5292")
	@AllureId("ALRT-10")
	@Test(description = "ALRT-10", groups = {"multiTenancy", "singleTenancy"})
	public void downloadCsv() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();
		AlertPage page = new AlertPage(driver);

		Allure.step("Login with Super/Admin user and navigate to Alerts page");
		log.info("Login with Super/Admin user and navigate to Alerts page");
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		HashMap<String, String> params = new HashMap<>();
		params.put("processed", "false");
		params.put("domainAlerts", "false");
		params.put("orderBy", "creationTime");
		params.put("asc", "false");

		String fileName = rest.csv().downloadGrid(RestServicePaths.ALERTS_CSV, params, null);
		Allure.step("downloaded file with name " + fileName);
		log.info("downloaded file with name " + fileName);

		page.grid().waitForRowsToLoad();
		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		Allure.step("checking info in grid against the file");
		log.info("checking info in grid against the file");
		page.alertsGrid().checkCSVvsGridInfo(fileName, soft);

		soft.assertAll();
	}


	//This method will verify absence of super admin records and present record belongs to current domain
	/*  ALRT-13 - Super admin checks Show domain alerts checkbox   */
	@Description("ALRT-13 - Super admin checks Show domain alerts checkbox ")
	@Link(name = "EDELIVERY-5295", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5295")
	@AllureId("ALRT-13")
	@Test(description = "ALRT-13", groups = {"multiTenancy"})
	public void superAdminrecordAbsenceForAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = null;
		if (data.isMultiDomain()) {
			Allure.step("selecting random domain");
			log.info("selecting random domain");
			List<String> domains = rest.getDomainCodes();
			Allure.step("got domains: " + domains);
			log.debug("got domains: " + domains);

			int index = new Random().nextInt(domains.size());

			domain = domains.get(index);
			Allure.step("will run for domain " + domain);
			log.info("will run for domain " + domain);
		}

		AlertPage page = new AlertPage(driver);
		AlertFilters aFilter = new AlertFilters(driver);

		String user = Gen.randomAlphaNumeric(10);
		rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), domain);
		Allure.step("created user " + user);
		log.info("created user " + user);

		Allure.step("Login with created user and naviagte to Alerts page");
		log.info("Login with created user and naviagte to Alerts page");
		login(user, data.defaultPass()).getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		soft.assertFalse(aFilter.getShowDomainCheckbox().isPresent(), "Check Box is present");

		int recordCount = page.grid().getRowsNo();
		if (recordCount <= 0) {
			Allure.step("no records to verify, exiting now");
			log.info("no records to verify, exiting now");
			return;
		}

		List<String> superList = rest.users().getSuperUsernames();
		Allure.step("Super user list: " + superList);
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
	/*  ALRT-28 - Verify data in drop downs for search filters on both domains and for both admin and super  */
	@Description("ALRT-28 - Verify data in drop downs for search filters on both domains and for both admin and super")
	@Link(name = "EDELIVERY-6352", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-6352")
	@AllureId("ALRT-28")
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
	/*  ALRT-29 - Verify validation for data for Alert id field  */
	@Description("ALRT-29 - Verify validation for data for Alert id field")
	@Link(name = "EDELIVERY-6353", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-6353")
	@AllureId("ALRT-29")
	@Test(description = "ALRT-29", groups = {"multiTenancy", "singleTenancy"})
	public void checkValidationForAlertId() throws Exception {
		SoftAssert soft = new SoftAssert();

		selectRandomDomain();

		Allure.step("Login into application and navigate to Alert page");
		log.info("Login into application and navigate to Alert page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		AlertFilters aFilter = new AlertFilters(driver);

		Allure.step("Click on advance link");
		log.info("Click on advance link");
		aFilter.getAdvancedLink().click();

		Allure.step("Create list of correct and incorrect data");
		log.info("Create list of correct and incorrect data");
		List<String> correctDataArray = Arrays.asList("1234567890123456789", "347362", "1");
		List<String> incorrectDataArray = Arrays.asList("random", "0random", "0000000000000000000", "12345678901234567890", "random1", "54 656", "$#%", "-989", "+787");

		for (int i = 0; i < correctDataArray.size(); i++) {
			Allure.step("Pass correct value :" + correctDataArray.get(i));
			log.info("Pass correct value :" + correctDataArray.get(i));
			aFilter.getAlertIdInput().fill(correctDataArray.get(i));

			soft.assertFalse(aFilter.isAlertIdValidationMessageVisible(), "Validation message is not visible for input " + correctDataArray.get(i));

			Allure.step("Verify status of search button as enabled");
			log.info("Verify status of search button as enabled");
			soft.assertTrue(aFilter.getSearchButton().isEnabled(), "Button is enabled");
		}

		for (int i = 0; i < incorrectDataArray.size(); i++) {
			Allure.step("Pass incorrect value :" + incorrectDataArray.get(i));
			log.info("Pass incorrect value :" + incorrectDataArray.get(i));
			aFilter.getAlertIdInput().fill(incorrectDataArray.get(i));

			Allure.step("Verify presence of validation message under alert id field");
			log.info("Verify presence of validation message under alert id field");
			soft.assertTrue(aFilter.isAlertIdValidationMessageVisible(), "Validation message IS visible for input " + incorrectDataArray.get(i));
			soft.assertEquals(aFilter.getAlertValidationMess(), DMessages.ALERT_ID_INPUT_VALIDATION_MESSAGE, "Correct validation message is shown");

			soft.assertFalse(aFilter.getSearchButton().isEnabled(), "Button is not enabled");
		}

		soft.assertAll();
	}

	//This method will verfiy feature for Processed for Super alerts
	/*  ALRT-32 - Super admin marks super alert as processed   */
	@Description("ALRT-32 - Super admin marks super alert as processed ")
	@Link(name = "EDELIVERY-6371", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-6371")
	@AllureId("ALRT-32")
	@Test(description = "ALRT-32", groups = {"multiTenancy"})
	public void checkProcessed() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domain = selectRandomDomain();

		Allure.step("Login into application and navigate to Alert page");
		log.info("Login into application and navigate to Alert page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		if (data.isMultiDomain()) {
			Allure.step("Showing domain alerts");
			log.info("Showing domain alerts");
			page.filters().showDomainAlert();
			page.grid().waitForRowsToLoad();
		}

		AlertFilters aFilter = new AlertFilters(driver);

		Allure.step("Check alert count when showDomain alert is false");
		log.info("Check alert count when showDomain alert is false");
		int totalCount = rest.alerts().getAlerts(domain, false, true).length();

		if (totalCount <= 0) {
			throw new SkipException("No alerts present");
		}


		Allure.step("Verify disabled status of save and cancel button");
		log.info("Verify disabled status of save and cancel button");
		soft.assertFalse(page.getSaveButton().isEnabled(), "Check Save button is disabled");
		soft.assertFalse(page.getCancelButton().isEnabled(), "Check Cancel button is disabled");
		soft.assertFalse(page.getDeleteButton().isEnabled(), "Check Delete button is disabled");

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().checkBoxWithLabel("Alert Id");

		Allure.step("Check processed checkbox for first row");
		log.info("Check processed checkbox for first row");
		page.alertsGrid().markAsProcessed(0);

		String alertId = page.grid().getRowSpecificColumnVal(0, "Alert Id");

		soft.assertTrue(page.getSaveButton().isEnabled(), "Check Save button is enabled");
		soft.assertTrue(page.getCancelButton().isEnabled(), "Check Cancel button is enabled");
		soft.assertTrue(page.getDeleteButton().isEnabled(), "Check Delete button is enabled");

		Allure.step("Click on save button and then ok from confirmation pop up");
		log.info("Click on save button and then ok from confirmation pop up");
		page.getSaveButton().click();
		new Dialog(driver).confirm();

		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.ALERT_UPDATE_SUCCESS_MESSAGE, "Correct update message is shown");
		page.grid().waitForRowsToLoad();


		Allure.step("Check total count as 1 less than before");
		log.info("Check total count as 1 less than before");
		soft.assertTrue(page.grid().getPagination().getTotalItems() == totalCount - 1, "Check all alert size 1 less than before");

		Allure.step("Select processed in search filter ");
		log.info("Select processed in search filter ");
		aFilter.getProcessedSelect().selectOptionByText("PROCESSED");
		Allure.step("Click on search button");
		log.info("Click on search button");
		aFilter.getSearchButton().click();
		page.grid().waitForRowsToLoad();

		List<String> ids = page.grid().getValuesOnColumn("Alert Id");

		soft.assertTrue(ids.contains(alertId), "Processed record is present after event completion");

		soft.assertAll();
	}

	/* disabled because EDELIVERY-4186 */
	/*  ALRT-20 - Verify headers in downloaded CSV file  */
	@Description("ALRT-20 - Verify headers in downloaded CSV file")
	@Link(name = "EDELIVERY-5302", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5302")
	@AllureId("ALRT-20")
	@Test(description = "ALRT-20", groups = {"multiTenancy", "singleTenancy"})
	public void verifyHeaders() throws Exception {
		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		Allure.step("Customized location for download");
		log.info("Customized location for download");

		page.grid().waitForRowsToLoad();
		String filePath = page.pressSaveCsvAndSaveFile();


		Allure.step("Check if file is downloaded at given location");
		log.info("Check if file is downloaded at given location");
		soft.assertTrue(new File(filePath).exists(), "File is downloaded successfully");

		Allure.step("Extract complete path for downloaded file");
		log.info("Extract complete path for downloaded file");
		String completeFilePath = filePath;

		Allure.step("Click on show link");
		log.info("Click on show link");
		page.grid().getGridCtrl().showCtrls();

		Allure.step("Click on All link to show all available column headers");
		log.info("Click on All link to show all available column headers");
		page.grid().getGridCtrl().showAllColumns();

		Allure.step("Compare headers from downloaded csv and grid");
		log.info("Compare headers from downloaded csv and grid");
		page.grid().checkCSVvsGridHeaders(completeFilePath, soft);
		soft.assertAll();
	}

	/*  ALRT-19 - Check sorting  */
	@Description("ALRT-19 - Check sorting")
	@Link(name = "EDELIVERY-5301", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5301")
	@AllureId("ALRT-19")
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
	/*  ALRT-23 - Check additional filters section for each alert type  */
	@Description("ALRT-23 - Check additional filters section for each alert type")
	@Link(name = "EDELIVERY-5471", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5471")
	@AllureId("ALRT-23")
	@Test(description = "ALRT-23", groups = {"multiTenancy", "singleTenancy"})
	public void checkAditionalFilters() throws Exception {
		SoftAssert soft = new SoftAssert();

		Allure.step("Navigating to Alerts page");
		log.info("Navigating to Alerts page");
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		Allure.step("waiting for grid to load");
		log.info("waiting for grid to load");
		page.grid().waitForRowsToLoad();

		AlertFilters filter = new AlertFilters(driver);
		Allure.step("iterating trough alert types");
		log.info("iterating trough alert types");

		List<String> options = filter.getAlertTypeSelect().getOptionsTexts();

		for (String option : options) {

			if (StringUtils.isEmpty(option.trim())) {
				continue;
			}

			Allure.step("checking alert type " + option);
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

}
