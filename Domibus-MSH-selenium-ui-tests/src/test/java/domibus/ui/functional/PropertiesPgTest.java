package domibus.ui.functional;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.ChangePassword.ChangePasswordPage;
import pages.properties.PropGrid;
import pages.properties.PropertiesPage;
import pages.users.UserModal;
import pages.users.UsersGrid;
import pages.users.UsersPage;
import utils.Gen;
import utils.TestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PropertiesPgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PROPERTIES);

	String passExpirationDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	private String modifyProperty(String propertyName, Boolean isDomain, String newPropValue) throws Exception {

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("filtering for property");
		page.filters().filterBy(propertyName, null, null, null, isDomain);

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();

		log.info("setting property");
		String oldVal = (grid.getPropertyValue(propertyName));
		grid.setPropertyValue(propertyName, newPropValue);
		page.getAlertArea().waitForAlert();

		return oldVal;
	}

	/*EDELIVERY-7302 - PROP-1 - Verify presence of Domibus Properties page*/
	@Test(description = "PROP-1", groups = {"multiTenancy", "singleTenancy"})
	public void pageAvailability() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);
		log.info("checking if option is available for system admin");
		soft.assertTrue(page.getSidebar().isLinkPresent(PAGES.PROPERTIES), data.getAdminUser().get("username") + "has the option to access properties");

		if (data.isMultiDomain()) {
			String username = rest.getUsername(null, DRoles.ADMIN, true, false, true);
			login(username, data.defaultPass());
			log.info("checking if option is available for role ADMIN");
			soft.assertTrue(page.getSidebar().isLinkPresent(PAGES.PROPERTIES), username + "has the option to access properties");
		}

		String userUsername = rest.getUsername(null, DRoles.USER, true, false, true);
		login(userUsername, data.defaultPass());

		log.info("checking if option is available for role USER");
		soft.assertFalse(page.getSidebar().isLinkPresent(PAGES.PROPERTIES), userUsername + "has the option to access properties");

		soft.assertAll();
	}


	/*EDELIVERY-7303 - PROP-2 - Open Properties page as Super admin */
	@Test(description = "PROP-2", groups = {"multiTenancy"})
	public void openPageSuper() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("checking page elements are visible");
		soft.assertTrue(page.filters().getNameInput().isVisible(), "Name input is displayed");
		page.filters().expandArea();
		soft.assertTrue(page.filters().getTypeInput().isVisible(), "Type input is displayed");
		soft.assertTrue(page.filters().getModuleInput().isVisible(), "Module input is displayed");
		soft.assertTrue(page.filters().getValueInput().isVisible(), "Value input is displayed");
		soft.assertTrue(page.filters().getShowDomainChk().isVisible(), "Show domain checkbox is displayed");

		soft.assertTrue(page.grid().isPresent(), "Grid displayed");

		log.info("check at least one domain property id displayed");
		List<String> values = page.grid().getListedValuesOnColumn("Usage");
		soft.assertTrue(values.contains("Domain"), "at least one domain prop shown");

		soft.assertAll();
	}


	/*  EDELIVERY-7305 - PROP-3 - Open Properties page as Admin  */
	@Test(description = "PROP-3", groups = {"multiTenancy", "singleTenancy"})
	public void openPageAdmin() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.ADMIN, true, false, true);
		login(username, data.defaultPass());

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("checking page elements are visible");
		soft.assertTrue(page.filters().getNameInput().isVisible(), "Name input is displayed");
		page.filters().expandArea();
		soft.assertTrue(page.filters().getTypeInput().isVisible(), "Type input is displayed");
		soft.assertTrue(page.filters().getModuleInput().isVisible(), "Module input is displayed");
		soft.assertTrue(page.filters().getValueInput().isVisible(), "Value input is displayed");
		soft.assertFalse(page.filters().getShowDomainChk().isPresent(), "Show domain checkbox is NOT displayed");

		soft.assertTrue(page.grid().isPresent(), "Grid displayed");

		if (data.isMultiDomain()) {
			log.info(" checking if a global property can be viewed by admin");
			page.filters().filterBy("wsplugin.mtom.enabled", null, null, null, null);
			page.grid().waitForRowsToLoad();

			soft.assertEquals(page.grid().getRowsNo(), 0, "No rows displayed");
		}
		soft.assertAll();
	}


	/*  EDELIVERY-7306 - PROP-4 - Filter properties using available filters  */
	@Test(description = "PROP-4", groups = {"multiTenancy", "singleTenancy"})
	public void filterProperties() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info(" checking if a global property can be viewed by admin");
		page.filters().filterBy("wsplugin.mtom.enabled", null, null, null, null);
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.grid().getRowsNo(), 1, "1 rows displayed");

		HashMap<String, String> info = page.grid().getRowInfo(0);

		soft.assertEquals(info.get("Property Name"), "wsplugin.mtom.enabled", "correct property name is displayed");

		soft.assertAll();
	}

	/*  EDELIVERY-7307 - PROP-5 - Change number of rows visible  */
	@Test(description = "PROP-5", groups = {"multiTenancy", "singleTenancy"})
	public void changeNumberOfRows() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("check changing number of rows visible");
		page.grid().checkChangeNumberOfRows(soft);

		soft.assertAll();
	}

	/*  EDELIVERY-7308 - PROP-6 - Change visible columns  */
	@Test(description = "PROP-6", groups = {"multiTenancy", "singleTenancy"})
	public void changeVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("checking changing visible columns");
		page.propGrid().checkModifyVisibleColumns(soft);

		soft.assertAll();
	}

	/* EDELIVERY-7309 - PROP-7 - Sort grid  */
	@Test(description = "PROP-7", groups = {"multiTenancy", "singleTenancy"})
	public void checkSorting() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("checking sorting");

		JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");
		DGrid grid = page.propGrid();

		for (int i = 0; i < 3; i++) {
			JSONObject colDesc = colDescs.getJSONObject(i);
			if (grid.getColumnNames().contains(colDesc.getString("name"))) {
				TestUtils.testSortingForColumn(soft, page.propGrid(), colDesc);
			}
		}

		soft.assertAll();
	}

	/* EDELIVERY-7310 - PROP-8 - Change active domain  */
	@Test(description = "PROP-8", groups = {"multiTenancy"})
	public void changeDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("setting domaint title to empty string for default domain");
		rest.properties().updateDomibusProperty("domain.title", "", null);

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("filter for property domain.title");
		page.filters().filterBy("domain.title", null, null, null, true);
		page.propGrid().waitForRowsToLoad();

		page.propGrid().setPropertyValue("domain.title", page.getDomainFromTitle());

		String firstValue = page.propGrid().getPropertyValue("domain.title");
		log.info("got property value " + firstValue);

		log.info("changing domain");
		page.getDomainSelector().selectAnotherDomain();
		page.propGrid().waitForRowsToLoad();


		String newDomainValue = page.propGrid().getPropertyValue("domain.title");
		log.info("got value for new domain: " + newDomainValue);

		soft.assertNotEquals(firstValue, newDomainValue, "Values from the different domains are not equal");

		log.info("resetting value");
		rest.properties().updateDomibusProperty("domain.title", "", null);


		soft.assertAll();
	}

	/* EDELIVERY-7311 - PROP-9 - Update property value to valid value and press save  */
	@Test(description = "PROP-9", groups = {"multiTenancy", "singleTenancy"})
	public void updateAndSave() throws Exception {
		SoftAssert soft = new SoftAssert();

		String domainTitleVal = Gen.randomAlphaNumeric(15);

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("filter for property domain.title");
		page.filters().filterBy("domain.title", null, null, null, true);
		page.propGrid().waitForRowsToLoad();

		page.propGrid().setPropertyValue("domain.title", domainTitleVal);

		page.refreshPage();

		String value = page.propGrid().getPropertyValue("domain.title");
		log.info("got property value " + value);

		soft.assertEquals(value, domainTitleVal, "Set value is saved properly");

		log.info("resetting value");
		rest.properties().updateDomibusProperty("domain.title", "", null);


		soft.assertAll();
	}


	/* EDELIVERY-7312 - PROP-10 - Update property value to invalid value and press save  */
	@Test(description = "PROP-10", groups = {"multiTenancy", "singleTenancy"})
	public void updateInvalidValue() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.properties().updateDomibusProperty("domibus.property.validation.enabled", "true");

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("filter for boolean properties");
		page.filters().filterBy("", "BOOLEAN", null, null, true);
		page.propGrid().waitForRowsToLoad();

		log.info("getting info on row 0");
		HashMap<String, String> info = page.propGrid().getRowInfo(0);

		String toSetValue = Gen.randomAlphaNumeric(5);
		log.info("setting invalid value " + toSetValue);
		page.propGrid().setPropRowValueAndSave(0, toSetValue);

		log.info("checking for error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");

		log.info("check correct message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.PROPERTIES_UPDATE_ERROR_TYPE, toSetValue, info.get("Property Name"), "BOOLEAN"),
				"Correct error message is shown");

		page.refreshPage();
		page.propGrid().waitForRowsToLoad();

		String value = page.propGrid().getPropertyValue(info.get("Property Name"));
		log.info("getting value after refresh: " + value);

		soft.assertEquals(value, info.get("Property Value"), "Set value was not saved");

		soft.assertAll();
	}


	/* EDELIVERY-7313 - PROP-11 - Update property value and press revert  */
	@Test(description = "PROP-11", groups = {"multiTenancy", "singleTenancy"})
	public void updateAndRevert() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("getting info on row 0");
		HashMap<String, String> info = page.propGrid().getRowInfo(0);

		String toSetValue = Gen.randomAlphaNumeric(5);
		log.info("setting invalid value " + toSetValue);
		page.propGrid().setPropRowValueAndRevert(0, toSetValue);

		String value = page.propGrid().getPropertyValue(info.get("Property Name"));
		log.info("getting value after refresh: " + value);

		soft.assertEquals(value, info.get("Property Value"), "Set value was not saved");

		soft.assertAll();
	}


	/* EDELIVERY-7314 - PROP-12 - Update property value don't press save and move focus on another field  */
	@Test(description = "PROP-12", groups = {"multiTenancy", "singleTenancy"})
	public void fillAndDontSave() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("getting info on row 0");
		HashMap<String, String> info = page.propGrid().getRowInfo(0);

		String toSetValue = Gen.randomAlphaNumeric(5);
		log.info("setting invalid value " + toSetValue);
		page.propGrid().setPropRowValue(0, toSetValue);


		page.grid().getGridCtrl().showCtrls();
		page.wait.forXMillis(3000);

		String value = page.propGrid().getPropertyValue(info.get("Property Name"));
		log.info("getting value after refresh: " + value);

		soft.assertEquals(value, info.get("Property Value"), "Set value was not saved");

		soft.assertAll();
	}


	/* EDELIVERY-7315 - PROP-13 - Update property value don't press save and go to another page   */
	@Test(description = "PROP-13", groups = {"multiTenancy", "singleTenancy"})
	public void fillAndGoPage2() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("getting info on row 0");
		HashMap<String, String> info = page.propGrid().getRowInfo(0);

		String toSetValue = Gen.randomAlphaNumeric(5);
		log.info("setting invalid value " + toSetValue);
		page.propGrid().setPropRowValue(0, toSetValue);


		page.wait.forXMillis(1000);
		page.grid().getPagination().goToNextPage();

		String value = rest.properties().getDomibusPropertyDetail(info.get("Property Name"), null).getString("value");
		log.info("getting value after refresh: " + value);

		soft.assertEquals(value, info.get("Property Value"), "Set value was not saved");

		soft.assertAll();
	}

	/* EDELIVERY-7316 - PROP-14 - Export to CSV   */
	@Test(description = "PROP-14", groups = {"multiTenancy", "singleTenancy"})
	public void exportCSV() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		PropGrid grid = page.propGrid();
		grid.getGridCtrl().showAllColumns();
		grid.getPagination().getPageSizeSelect().selectOptionByText("100");

		String filename = page.pressSaveCsvAndSaveFile();

		page.propGrid().relaxCheckCSVvsGridInfo(filename, soft, "text");

		soft.assertAll();
	}


	/* EDELIVERY-7318 - PROP-16 - Update property domibus.console.login.maximum.attempt   */
	@Test(description = "PROP-16", groups = {"multiTenancy", "singleTenancy"})
	public void updateMaxLoginAttempts() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		page.filters().filterBy("domibus.console.login.maximum.attempt", null, null, null, null);
		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();


		grid.setPropertyValue("domibus.console.login.maximum.attempt", "1");

		String username = rest.getUsername(null, DRoles.USER, true, false, false);

		boolean userBlocked = false;
		int attempts = 0;

		while (!userBlocked && attempts < 10) {
			log.info("attempting login with wrong pass and user " + username);
			ClientResponse response = rest.callLogin(username, "wrong password");
			attempts++;

			log.info("checking error message for account suspended message");
			String errMessage = response.getEntity(String.class);
			userBlocked = errMessage.contains("Suspended");
		}

		log.info("verifying number of attempts");
		soft.assertEquals(attempts, 2, "User is blocked on the second attempt to login");

		soft.assertAll();
	}

	/* EDELIVERY-7319 - PROP-17 - Update property domibus.console.login.suspension.time  */
	@Test(description = "PROP-17", groups = {"multiTenancy", "singleTenancy"})
	public void updateSuspensionTime() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();


		grid.setPropertyValue("domibus.console.login.suspension.time", "10");
		grid.setPropertyValue("domibus.account.unlock.cron", "0 * * ? * *");

		String username = rest.getUsername(null, DRoles.USER, true, false, true);

		boolean userBlocked = false;
		int attempts = 0;

		while (!userBlocked && attempts < 10) {
			log.info("attempting login with wrong pass and user " + username);
			ClientResponse response = rest.callLogin(username, "wrong password");
			attempts++;

			log.info("checking error message for account suspended message");
			String errMessage = response.getEntity(String.class);
			userBlocked = errMessage.contains("Suspended");
		}

		page.wait.forXMillis(60000);
		ClientResponse response = rest.callLogin(username, data.defaultPass());
		soft.assertEquals(response.getStatus(), 200, "Login response is success");

		soft.assertAll();
	}


	/* EDELIVERY-7323 - PROP-18 - Update property domibus.file.upload.maxSize  */
	@Test(description = "PROP-18", groups = {"multiTenancy", "singleTenancy"})
	public void updateMaxUploadSize() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		page.filters().filterBy("domibus.file.upload.maxSize", null, null, null, false);

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();

		grid.setPropertyValue("domibus.file.upload.maxSize", "100");

		soft.assertFalse(page.getAlertArea().isError(), "Success message is shown");

		try {
			ClientResponse response = rest.pmode().uploadPMode("pmodes/pmode-dataSetupBlue.xml", "test comment", null);
			soft.assertEquals(response.getStatus() , 500, "500 error returned");
		} catch (Exception e) {
			e.printStackTrace();
		}

		rest.properties().updateGlobalProperty("domibus.file.upload.maxSize", "1000000");
		soft.assertAll();
	}


	/* EDELIVERY-7325 - PROP-20 - Update property domibus.passwordPolicy.checkDefaultPassword  */
	@Test(description = "PROP-20", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkDefaultPassword() throws Exception {
		SoftAssert soft = new SoftAssert();

		String role = DRoles.ADMIN;
		if (data.isMultiDomain()) {
			role = DRoles.SUPER;
		}

		String adminUserName = rest.getUsername(null, role, true, false, true);
		logout();
		login(adminUserName, data.defaultPass());

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("filtering for property");
		page.filters().filterBy("domibus.passwordPolicy.checkDefaultPassword", null, null, null, false);

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();

		log.info("setting property");
		grid.setPropertyValue("domibus.passwordPolicy.checkDefaultPassword", "true");
		page.getAlertArea().waitForAlert();

		logout();
		log.info("login with default user and default pass to check the effect of change");
		login(data.getAdminUser());

		log.info("checking that alert is shown, popup is shown, user is on change pass page");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown after login");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_DEFAULT_PASS, "Correct message is shown - ");

		soft.assertTrue(page.hasOpenDialog(), "Page has a open dialog warning");
		soft.assertEquals(new Dialog(driver).getMessage(), DMessages.LOGIN_DEFAULT_PASS, "Popup displays correct message");

		soft.assertTrue(driver.getCurrentUrl().contains("changePassword"), "URL contains changePassword");
		soft.assertTrue(new ChangePasswordPage(driver).isLoaded(), "Change password page is loaded");

		log.info("reseting property value and check effect");

		logout();
		login(adminUserName, data.defaultPass());

		log.info("going to properties page");
		page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("filtering for property");
		page.filters().filterBy("domibus.passwordPolicy.checkDefaultPassword", null, null, null, false);

		grid = page.propGrid();
		grid.waitForRowsToLoad();

		log.info("setting property value");
		grid.setPropertyValue("domibus.passwordPolicy.checkDefaultPassword", "false");
		page.getAlertArea().waitForAlert();

		logout();
		login(data.getAdminUser());

		log.info("checking the effect of property change");
		soft.assertNull(page.getAlertArea().getAlertMessage(), "After setting prop back to false no more error message appears after login");

		soft.assertAll();
	}


	/* EDELIVERY-7330 - PROP-21 - Update property domibus.passwordPolicy.defaultPasswordExpiration  */
	@Test(description = "PROP-21", groups = {"multiTenancy", "singleTenancy"})
	public void defaultPassExpiration() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("getting expiry date before change");
		String expiryDateDefAdmin = rest.users().getUser(null, data.getAdminUser().get("username")).getString("expirationDate");
		String rndUser = rest.getUsername(null, DRoles.USER, true, false, true);
		String rndUserExpiryDate = rest.users().getUser(null, rndUser).getString("expirationDate");

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("filtering for property");
		page.filters().filterBy("domibus.passwordPolicy.defaultPasswordExpiration", null, null, null, !data.isMultiDomain());

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();

		log.info("setting property");
		Integer propVal = Integer.valueOf(grid.getPropertyValue("domibus.passwordPolicy.defaultPasswordExpiration"));
		grid.setPropertyValue("domibus.passwordPolicy.defaultPasswordExpiration", "10");
		page.getAlertArea().waitForAlert();

		log.info("getting expiry date after change");
		String newExpiryDateDefAdmin = rest.users().getUser(null, data.getAdminUser().get("username")).getString("expirationDate");
		String rndUserNewExpiryDate = rest.users().getUser(null, rndUser).getString("expirationDate");

		log.info("comparing dates");
		Date defAdminBefore = DateUtils.parseDate(expiryDateDefAdmin, "yyyy-MM-dd'T'HH:mm:ss.SSS");
		Date defAdminAfter = DateUtils.parseDate(newExpiryDateDefAdmin, "yyyy-MM-dd'T'HH:mm:ss.SSS");

		Date rndExpBefore = DateUtils.parseDate(rndUserExpiryDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
		Date rndExpAfter = DateUtils.parseDate(rndUserNewExpiryDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");


		soft.assertEquals(DateUtils.addDays(defAdminAfter, propVal - 10), defAdminBefore, "Checking days difference between after and before dates for system admin");
		soft.assertEquals(rndExpBefore, rndExpAfter, "Checking days difference between after and before dates for rnd user");


		soft.assertAll();
	}


	/* EDELIVERY-7331 - PROP-22 - Update property domibus.passwordPolicy.dontReuseLast */
	@Test(description = "PROP-22", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void dontReuseLastPass() throws Exception {
		SoftAssert soft = new SoftAssert();

//		checking property at domain level
		String username = rest.getUsername(null, DRoles.USER, true, false, true);
		for (int i = 0; i <= 5; i++) {
			log.info("changing pass for user " + username);
			rest.users().changePassForUser(null, username, data.defaultPass() + i);
		}

		Integer propVal = Integer.valueOf(modifyProperty("domibus.passwordPolicy.dontReuseLast", true, "3"));

		boolean isErr = true;
		int count = 0;
		while (isErr && count < 5) {
			log.info("attempt " + count);
			try {
				rest.users().changePassForUser(null, username, data.defaultPass() + (5 - count));
				isErr = false;
			} catch (Exception e) {
				count++;
			}

		}

		soft.assertEquals(count, 3, "cannot reuse any of the last 3 passwords");


//		checking property at super level
		if (data.isMultiDomain()) {
			username = rest.getUsername(null, DRoles.SUPER, true, false, true);
			for (int i = 0; i <= 5; i++) {
				log.info("changing pass for user " + username);
				rest.users().changePassForUser(null, username, data.defaultPass() + i);
			}

			propVal = Integer.valueOf(modifyProperty("domibus.passwordPolicy.dontReuseLast", false, "2"));

			isErr = true;
			count = 0;
			while (isErr && count < 5) {
				log.info("attempt " + count);
				try {
					rest.users().changePassForUser(null, username, data.defaultPass() + (5 - count));
					isErr = false;
				} catch (Exception e) {
					count++;
				}

			}
			soft.assertEquals(count, 2, "cannot reuse any of the last 2 passwords");
		}


		soft.assertAll();
	}


	/* EDELIVERY-7332 - PROP-23 - Update property domibus.passwordPolicy.expiration */
	@Test(description = "PROP-23", groups = {"multiTenancy", "singleTenancy"})
	public void regularPassExpiration() throws Exception {
		SoftAssert soft = new SoftAssert();

//		checking property at domain level
		String username = rest.getUsername(null, DRoles.USER, true, false, true);
		log.info("getting expiry date before change");
		String oldExpiryDate = rest.users().getUser(null, username).getString("expirationDate");

		Integer oldPropVal = Integer.valueOf(modifyProperty("domibus.passwordPolicy.expiration", true, "10"));

		log.info("getting expiry date after change");
		String newExpiryDate = rest.users().getUser(null, username).getString("expirationDate");

		log.info("checking the new expiry date is correct");
		Date oldDate = DateUtils.parseDate(oldExpiryDate, passExpirationDatePattern);
		Date newDate = DateUtils.parseDate(newExpiryDate, passExpirationDatePattern);
		soft.assertEquals(oldDate, DateUtils.addDays(newDate, oldPropVal - 10), "date updated correctly");

		if (data.isMultiDomain()) {
			//		checking property at SUPER level
			username = rest.getUsername(null, DRoles.SUPER, true, false, true);
			log.info("getting expiry date before change");
			oldExpiryDate = rest.users().getUser(null, username).getString("expirationDate");

			oldPropVal = Integer.valueOf(modifyProperty("domibus.passwordPolicy.expiration", false, "10"));

			log.info("getting expiry date after change");
			newExpiryDate = rest.users().getUser(null, username).getString("expirationDate");

			log.info("checking the new expiry date is correct");
			oldDate = DateUtils.parseDate(oldExpiryDate, passExpirationDatePattern);
			newDate = DateUtils.parseDate(newExpiryDate, passExpirationDatePattern);
			soft.assertEquals(oldDate, DateUtils.addDays(newDate, oldPropVal - 10), "date updated correctly");
		}


		soft.assertAll();
	}


	/*     EDELIVERY-7333 - PROP-24 - Update property domibus.passwordPolicy.pattern */
	@Test(description = "PROP-24", groups = {"multiTenancy", "singleTenancy"})
	public void checkPolicyPattern() throws Exception {
		SoftAssert soft = new SoftAssert();

//		checking property at domain level
		String username = rest.getUsername(null, DRoles.USER, true, false, false);

		String oldPropVal = modifyProperty("domibus.passwordPolicy.pattern", true, "[0-9].{8,32}");

		try {
			rest.users().changePassForUser(null, username, Gen.randomNumberOfLen(10));
		} catch (Exception e) {
			soft.assertTrue(false, "Updating pass to only numbers failed for user");
		}

		rest.properties().updateDomibusProperty("domibus.passwordPolicy.pattern", oldPropVal, null);

		if (data.isMultiDomain()) {
//		checking property at super level
			String superUsername = rest.getUsername(null, DRoles.SUPER, true, false, true);

			modifyProperty("domibus.passwordPolicy.pattern", false, "[0-9].{8,32}");

			try {
				rest.users().changePassForUser(null, superUsername, Gen.randomNumberOfLen(10));
			} catch (Exception e) {
				soft.assertTrue(false, "Updating pass to only numbers failed for super");
			}
			rest.properties().updateGlobalProperty("domibus.passwordPolicy.pattern", oldPropVal);
		}


		soft.assertAll();
	}


	/* EDELIVERY-7334 - PROP-25 - Update property domibus.passwordPolicy.validationMessage */
	@Test(description = "PROP-25", groups = {"multiTenancy", "singleTenancy"})
	public void checkPolicyValidationMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

//		checking property at domain level
		String username = rest.getUsername(null, DRoles.USER, true, false, false);

		String newValidationMessage = "newUSERValidationMessage";
		String oldPropVal = modifyProperty("domibus.passwordPolicy.validationMessage", true, newValidationMessage);

		String newSuperValidationMessage = "newSUPERValidationMessage";

		if (data.isMultiDomain()) {
			String oldSuperPropVal = modifyProperty("domibus.passwordPolicy.validationMessage", false, newSuperValidationMessage);
		}

		UsersPage page = new UsersPage(driver);
		page.getSidebar().goToPage(PAGES.USERS);

		UsersGrid grid = page.getUsersGrid();
		grid.waitForRowsToLoad();

		page.getNewBtn().click();

		UserModal modal = new UserModal(driver);
		modal.getPasswordInput().fill("notGood");


		soft.assertEquals(modal.getPassErrMess().getText() , "Password should follow all of these rules:\n\n" + newValidationMessage , "User validation message changed according to property");

		if (data.isMultiDomain()) {

			page.refreshPage();
			grid.waitForRowsToLoad();

			page.getNewBtn().click();
			modal.getRoleSelect().selectOptionByText(DRoles.SUPER);

			modal.getPasswordInput().fill("notGood");
			soft.assertEquals(modal.getPassErrMess().getText() , "Password should follow all of these rules:\n\n" + newSuperValidationMessage , "Super validation message changed according to property");

			rest.properties().updateGlobalProperty("domibus.passwordPolicy.validationMessage", oldPropVal);
		}

		rest.properties().updateDomibusProperty("domibus.passwordPolicy.validationMessage", oldPropVal);
		soft.assertAll();
	}

	/* EDELIVERY-7336 - PROP-27 - Update property domibus.property.length.max */
	@Test(description = "PROP-27", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void checkPropertyLengthMax() throws Exception {
		SoftAssert soft = new SoftAssert();

		String expectedErrorTemplate = "Could not update property: Invalid property value [%s] for property [%s]. Maximum accepted length is: %s";
		String newMaxLength = "10";

		PropertiesPage page = new PropertiesPage(driver);

		String oldVal = modifyProperty("domibus.property.length.max", false, "10");

		try{
			String globalNewVal = Gen.randomAlphaNumeric(12);
			modifyProperty("domibus.instance.name", false, globalNewVal);
			soft.assertEquals(page.getAlertArea().getAlertMessage()
					, String.format(expectedErrorTemplate, globalNewVal, "domibus.instance.name", newMaxLength)
					, "Correct error message is shown (GLOBAL)");
		}catch (Exception e){ }
		try{

			String globalNewVal = Gen.randomAlphaNumeric(12);
			modifyProperty("domibus.ui.support.team.name", true, globalNewVal);
			soft.assertEquals(page.getAlertArea().getAlertMessage()
					, String.format(expectedErrorTemplate, globalNewVal, "domibus.ui.support.team.name", newMaxLength)
					, "Correct error message is shown (DOMAIN)");

		}catch (Exception e){ }

		rest.properties().updateGlobalProperty("domibus.property.length.max", oldVal);

		soft.assertAll();
	}


}
