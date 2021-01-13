package domibus.ui.functional;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.properties.PropGrid;
import pages.properties.PropertiesPage;
import utils.Gen;
import utils.TestUtils;

import java.util.HashMap;
import java.util.List;

public class PropertiesPgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PROPERTIES);


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
	@Test(description = "PROP-2", groups = {"multiTenancy", "singleTenancy"})
	public void openPageSuper() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		log.info("checking page elements are visible");
		soft.assertTrue(page.filters().getNameInput().isVisible(), "Name input is displayed");
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
		soft.assertTrue(page.filters().getTypeInput().isVisible(), "Type input is displayed");
		soft.assertTrue(page.filters().getModuleInput().isVisible(), "Module input is displayed");
		soft.assertTrue(page.filters().getValueInput().isVisible(), "Value input is displayed");
		soft.assertFalse(page.filters().getShowDomainChk().isPresent(), "Show domain checkbox is NOT displayed");

		soft.assertTrue(page.grid().isPresent(), "Grid displayed");

		log.info(" checking if a global property can be viewed by admin");
		page.filters().filterBy("wsplugin.mtom.enabled", null, null, null, null);
		page.grid().waitForRowsToLoad();

		soft.assertEquals(page.grid().getRowsNo(), 0, "No rows displayed");

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
		page.filters().filterBy("wsplugin.mtom.enabled", null, null, null, false);
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

		String value = page.propGrid().getPropertyValue(info.get("Property Name"));
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
			log.info("attempting login with wring pass and user " + username);
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
			log.info("attempting login with wring pass and user " + username);
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

		ClientResponse response = null;
		try {
			response = rest.pmode().uploadPMode("pmodes/pmode-dataSetupBlue.xml", "test comment", null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		soft.assertTrue(StringUtils.containsIgnoreCase(response.getEntity(String.class), "Maximum upload size of 100 bytes exceeded"), "error message contains mention of file size exceeded");

		rest.properties().updateGlobalProperty("domibus.file.upload.maxSize", "1000000");
		soft.assertAll();
	}


	/* EDELIVERY-7325 - PROP-20 - Update property domibus.passwordPolicy.checkDefaultPassword  */
	@Test(description = "PROP-20", groups = {"multiTenancy", "singleTenancy"})
	public void checkDefaultPassword() throws Exception {
		SoftAssert soft = new SoftAssert();

		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		page.filters().filterBy("domibus.passwordPolicy.checkDefaultPassword", null, null, null, false);

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();

		grid.setPropertyValue("domibus.passwordPolicy.checkDefaultPassword", "true");

		logout();

		login(data.getAdminUser());



		rest.properties().updateGlobalProperty("domibus.passwordPolicy.checkDefaultPassword", "false");
		soft.assertAll();
	}


}
