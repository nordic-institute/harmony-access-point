package domibus.ui.functional;

import org.testng.Reporter;
import com.sun.jersey.api.client.ClientResponse;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
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

import java.io.File;
import java.util.*;

public class Properties2PgTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PROPERTIES);

	String passExpirationDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	private String modifyProperty(String propertyName, Boolean isDomain, String newPropValue) throws Exception {

		Reporter.log("going to properties page");
		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		Reporter.log("waiting for grid to load");
		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		Reporter.log("filtering for property");
		log.info("filtering for property");
		page.filters().filterBy(propertyName, null, null, null, isDomain);

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();

		Reporter.log("setting property");
		log.info("setting property");
		String oldVal = (grid.getPropertyValue(propertyName));
		grid.setPropertyValue(propertyName, newPropValue);
		page.getAlertArea().waitForAlertSucces();


		return oldVal;
	}

	/* EDELIVERY-7323 - PROP-18 - Update property domibusfileuploadmaxSize */
	@Test(description = "PROP-18", groups = {"multiTenancy", "singleTenancy"})
	public void updateMaxUploadSize() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("going to properties page");
		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		Reporter.log("waiting for grid to load");
		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		page.filters().filterBy("domibus.file.upload.maxSize", null, null, null, false);

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();

		grid.setPropertyValue("domibus.file.upload.maxSize", "100");

		soft.assertFalse(page.getAlertArea().isError(), "Success message is shown");

		try {
			ClientResponse response = rest.pmode().uploadPMode("pmodes/pmode-dataSetupBlue.xml", "test comment", null);
			soft.assertEquals(response.getStatus(), 500, "500 error returned");
		} catch (Exception e) {
			e.printStackTrace();
		}

		rest.properties().updateGlobalProperty("domibus.file.upload.maxSize", "1000000");
		soft.assertAll();
	}


	/* EDELIVERY-7325 - PROP-20 - Update property domibuspasswordPolicycheckDefaultPassword */
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

		Reporter.log("going to properties page");
		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		Reporter.log("waiting for grid to load");
		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		Reporter.log("filtering for property");
		log.info("filtering for property");
		page.filters().filterBy("domibus.passwordPolicy.checkDefaultPassword", null, null, null, false);

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();

		Reporter.log("setting property");
		log.info("setting property");
		grid.setPropertyValue("domibus.passwordPolicy.checkDefaultPassword", "true");
		page.getAlertArea().waitForAlert();

		logout();
		Reporter.log("login with default user and default pass to check the effect of change");
		log.info("login with default user and default pass to check the effect of change");
		login(data.getAdminUser());

		Reporter.log("checking that alert is shown, popup is shown, user is on change pass page");
		log.info("checking that alert is shown, popup is shown, user is on change pass page");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown after login");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_DEFAULT_PASS, "Correct message is shown - ");

		soft.assertTrue(page.hasOpenDialog(), "Page has a open dialog warning");
		soft.assertEquals(new Dialog(driver).getMessage(), DMessages.LOGIN_DEFAULT_PASS, "Popup displays correct message");

		soft.assertTrue(driver.getCurrentUrl().contains("changePassword"), "URL contains changePassword");
		soft.assertTrue(new ChangePasswordPage(driver).isLoaded(), "Change password page is loaded");

		Reporter.log("reseting property value and check effect");
		log.info("reseting property value and check effect");

		logout();
		login(adminUserName, data.defaultPass());

		Reporter.log("going to properties page");
		log.info("going to properties page");
		page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		Reporter.log("waiting for grid to load");
		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		Reporter.log("filtering for property");
		log.info("filtering for property");
		page.filters().filterBy("domibus.passwordPolicy.checkDefaultPassword", null, null, null, false);

		grid = page.propGrid();
		grid.waitForRowsToLoad();

		Reporter.log("setting property value");
		log.info("setting property value");
		grid.setPropertyValue("domibus.passwordPolicy.checkDefaultPassword", "false");
		page.getAlertArea().waitForAlert();

		logout();
		login(data.getAdminUser());

		Reporter.log("checking the effect of property change");
		log.info("checking the effect of property change");
		soft.assertNull(page.getAlertArea().getAlertMessage(), "After setting prop back to false no more error message appears after login");

		soft.assertAll();
	}


	/* EDELIVERY-7330 - PROP-21 - Update property domibuspasswordPolicydefaultPasswordExpiration */
	@Test(description = "PROP-21", groups = {"multiTenancy", "singleTenancy"})
	public void defaultPassExpiration() throws Exception {
		SoftAssert soft = new SoftAssert();

		Reporter.log("going to properties page");
		log.info("going to properties page");
		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		Reporter.log("getting expiry date before change");
		log.info("getting expiry date before change");
		String expiryDateDefAdmin = rest.users().getUser(null, data.getAdminUser().get("username")).getString("expirationDate");
		String rndUser = rest.getUsername(null, DRoles.USER, true, false, true);
		String rndUserExpiryDate = rest.users().getUser(null, rndUser).getString("expirationDate");

		Reporter.log("waiting for grid to load");
		log.info("waiting for grid to load");
		page.propGrid().waitForRowsToLoad();

		Reporter.log("filtering for property");
		log.info("filtering for property");
		page.filters().filterBy("domibus.passwordPolicy.defaultPasswordExpiration", null, null, null, !data.isMultiDomain());

		PropGrid grid = page.propGrid();
		grid.waitForRowsToLoad();

		Reporter.log("setting property");
		log.info("setting property");
		Integer propVal = Integer.valueOf(grid.getPropertyValue("domibus.passwordPolicy.defaultPasswordExpiration"));
		grid.setPropertyValue("domibus.passwordPolicy.defaultPasswordExpiration", "10");
		page.getAlertArea().waitForAlert();

		Reporter.log("getting expiry date after change");
		log.info("getting expiry date after change");
		String newExpiryDateDefAdmin = rest.users().getUser(null, data.getAdminUser().get("username")).getString("expirationDate");
		String rndUserNewExpiryDate = rest.users().getUser(null, rndUser).getString("expirationDate");

		Reporter.log("comparing dates");
		log.info("comparing dates");
		Date defAdminBefore = DateUtils.parseDate(expiryDateDefAdmin, "yyyy-MM-dd'T'HH:mm:ss.SSS");
		Date defAdminAfter = DateUtils.parseDate(newExpiryDateDefAdmin, "yyyy-MM-dd'T'HH:mm:ss.SSS");

		Date rndExpBefore = DateUtils.parseDate(rndUserExpiryDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");
		Date rndExpAfter = DateUtils.parseDate(rndUserNewExpiryDate, "yyyy-MM-dd'T'HH:mm:ss.SSS");


		soft.assertEquals(DateUtils.addDays(defAdminAfter, propVal - 10), defAdminBefore, "Checking days difference between after and before dates for system admin");
		soft.assertEquals(rndExpBefore, rndExpAfter, "Checking days difference between after and before dates for rnd user");


		soft.assertAll();
	}


	/* EDELIVERY-7331 - PROP-22 - Update property domibuspasswordPolicydontReuseLast */
	@Test(description = "PROP-22", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void dontReuseLastPass() throws Exception {
		SoftAssert soft = new SoftAssert();

//		checking property at domain level
		String username = rest.getUsername(null, DRoles.USER, true, false, true);
		for (int i = 0; i <= 5; i++) {
			Reporter.log("changing pass for user " + username);
			log.info("changing pass for user " + username);
			rest.users().changePassForUser(null, username, data.defaultPass() + i);
		}

		Integer propVal = Integer.valueOf(modifyProperty("domibus.passwordPolicy.dontReuseLast", true, "3"));

		boolean isErr = true;
		int count = 0;
		while (isErr && count < 5) {
			Reporter.log("attempt " + count);
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
				Reporter.log("changing pass for user " + username);
				log.info("changing pass for user " + username);
				rest.users().changePassForUser(null, username, data.defaultPass() + i);
			}

			propVal = Integer.valueOf(modifyProperty("domibus.passwordPolicy.dontReuseLast", false, "2"));

			isErr = true;
			count = 0;
			while (isErr && count < 5) {
				Reporter.log("attempt " + count);
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


	/* EDELIVERY-7332 - PROP-23 - Update property domibuspasswordPolicyexpiration */
	@Test(description = "PROP-23", groups = {"multiTenancy", "singleTenancy"})
	public void regularPassExpiration() throws Exception {
		SoftAssert soft = new SoftAssert();

//		checking property at domain level
		String username = rest.getUsername(null, DRoles.USER, true, false, true);
		Reporter.log("getting expiry date before change");
		log.info("getting expiry date before change");
		String oldExpiryDate = rest.users().getUser(null, username).getString("expirationDate");

		Integer oldPropVal = Integer.valueOf(modifyProperty("domibus.passwordPolicy.expiration", true, "10"));

		Reporter.log("getting expiry date after change");
		log.info("getting expiry date after change");
		String newExpiryDate = rest.users().getUser(null, username).getString("expirationDate");

		Reporter.log("checking the new expiry date is correct");
		log.info("checking the new expiry date is correct");
		Date oldDate = DateUtils.parseDate(oldExpiryDate, passExpirationDatePattern);
		Date newDate = DateUtils.parseDate(newExpiryDate, passExpirationDatePattern);
		soft.assertEquals(oldDate, DateUtils.addDays(newDate, oldPropVal - 10), "date updated correctly");

		if (data.isMultiDomain()) {
//		checking property at SUPER level
			username = rest.getUsername(null, DRoles.SUPER, true, false, true);
			Reporter.log("getting expiry date before change");
			log.info("getting expiry date before change");
			oldExpiryDate = rest.users().getUser(null, username).getString("expirationDate");

			oldPropVal = Integer.valueOf(modifyProperty("domibus.passwordPolicy.expiration", false, "10"));

			Reporter.log("getting expiry date after change");
			log.info("getting expiry date after change");
			newExpiryDate = rest.users().getUser(null, username).getString("expirationDate");

			Reporter.log("checking the new expiry date is correct");
			log.info("checking the new expiry date is correct");
			oldDate = DateUtils.parseDate(oldExpiryDate, passExpirationDatePattern);
			newDate = DateUtils.parseDate(newExpiryDate, passExpirationDatePattern);
			soft.assertEquals(oldDate, DateUtils.addDays(newDate, oldPropVal - 10), "date updated correctly");
		}


		soft.assertAll();
	}


	/* EDELIVERY-7333 - PROP-24 - Update property domibuspasswordPolicypattern */
	@Test(description = "PROP-24", groups = {"multiTenancy", "singleTenancy"})
	public void checkPolicyPattern() throws Exception {
		SoftAssert soft = new SoftAssert();

		PropertiesPage page = new PropertiesPage(driver);


//		checking property at domain level
		String username = rest.getUsername(null, DRoles.USER, true, false, true);

		String oldPropVal = modifyProperty("domibus.passwordPolicy.pattern", true, "[0-9].{8,32}");
		Reporter.log("property modified");
		log.info("property modified");

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


	/* EDELIVERY-7334 - PROP-25 - Update property domibuspasswordPolicyvalidationMessage */
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


		soft.assertEquals(modal.getPassErrMess().getText(), "Password should follow all of these rules:\n\n" + newValidationMessage, "User validation message changed according to property");

		if (data.isMultiDomain()) {

			page.refreshPage();
			grid.waitForRowsToLoad();

			page.getNewBtn().click();
			modal.getRoleSelect().selectOptionByText(DRoles.SUPER);

			modal.getPasswordInput().fill("notGood");
			soft.assertEquals(modal.getPassErrMess().getText(), "Password should follow all of these rules:\n\n" + newSuperValidationMessage, "Super validation message changed according to property");

			rest.properties().updateGlobalProperty("domibus.passwordPolicy.validationMessage", oldPropVal);
		}

		rest.properties().updateDomibusProperty("domibus.passwordPolicy.validationMessage", oldPropVal);
		soft.assertAll();
	}

	/* EDELIVERY-7336 - PROP-27 - Update property domibuspropertylengthmax */
	@Test(description = "PROP-27", groups = {"multiTenancy", "singleTenancy"})
	public void checkPropertyLengthMax() throws Exception {
		SoftAssert soft = new SoftAssert();

		String expectedErrorTemplate = "Could not update property: Invalid property value [%s] for property [%s]. Maximum accepted length is: %s";
		String newMaxLength = "10";

		PropertiesPage page = new PropertiesPage(driver);

		String oldVal = modifyProperty("domibus.property.length.max", false, "10");

		if (page.getAlertArea().isError()) {
			soft.fail("Could not update property domibus.property.length.max");
		}

		try {
			String globalNewVal = Gen.randomAlphaNumeric(12);
//			modifyProperty("domibus.instance.name", false, globalNewVal);
//			soft.assertEquals(page.getAlertArea().getAlertMessage()
//					, String.format(expectedErrorTemplate, globalNewVal, "domibus.instance.name", newMaxLength)
//					, "Correct error message is shown (GLOBAL)");
			String global_oldVal = rest.properties().getPropertyValue("domibus.instance.name", false, null);
			ClientResponse response = rest.properties().updateGlobalProperty("domibus.instance.name", globalNewVal);
			soft.assertFalse(response.getStatus() == 200, "Response is not success");

			rest.properties().updateGlobalProperty("domibus.instance.name", global_oldVal);
		} catch (Exception e) {
		}
		try {

			String globalNewVal = Gen.randomAlphaNumeric(12);
			modifyProperty("domibus.ui.support.team.name", true, globalNewVal);
			soft.assertEquals(page.getAlertArea().getAlertMessage()
					, String.format(expectedErrorTemplate, globalNewVal, "domibus.ui.support.team.name", newMaxLength)
					, "Correct error message is shown (DOMAIN)");

		} catch (Exception e) {
		}

		rest.properties().updateGlobalProperty("domibus.property.length.max", oldVal);

		soft.assertAll();
	}


	/* EDELIVERY-7337 - PROP-28 - Update property domibuspropertyvalidationenabled */
	@Test(description = "PROP-28", groups = {"multiTenancy", "singleTenancy"})
	public void propertyValidation() throws Exception {
		SoftAssert soft = new SoftAssert();

		List<String> toUdateDomain = Arrays.asList("domibus.alert.cert.expired.active"
				, "domibus.passwordPolicy.expiration"
				, "domibus.sendMessage.messageIdPattern"
//				,"domibus.dispatcher.concurency"
//				,"domibus.pull.retrry.cron"
//				,"domibus.alert.sender.email"
//				,"domibus.attachment.temp.storage.location"
		);

		List<String> toUdateGlobal = Arrays.asList(
				"domibus.auth.unsecureLoginAllowed"
				, "domibus.alert.cleaner.cron"
//				, "domibus.alert.sender.email"
//				, "domibus.passwordPolicy.expiration"
//				, "wsplugin.dispatcher.worker.cronExpression"
//				, "domibus.alert.sender.smtp.url"
//				, "domibus.alert.sender.smtp.url"
		);

		PropertiesPage page = new PropertiesPage(driver);

		String oldVal = modifyProperty("domibus.property.validation.enabled", false, "false");

		if (data.isMultiDomain()) {
			for (String prop : toUdateGlobal) {
				soft.assertTrue(isPropUpdateSuccess(prop, Gen.randomAlphaNumeric(5), false, null), "Global properties can be updated to invalid values when validation is turned off");
			}
		}

		for (String domProp : toUdateDomain) {
			soft.assertTrue(isPropUpdateSuccess(domProp, Gen.randomAlphaNumeric(5), true, null), "Domain properties can be updated to invalid values when validation is turned off");
		}

		rest.properties().updateGlobalProperty("domibus.property.validation.enabled", oldVal);
		soft.assertAll();
	}

	private boolean isPropUpdateSuccess(String propname, String propval, boolean isDomain, String domain) throws Exception {
		String currentVal = rest.properties().getPropertyValue(propname, isDomain, domain);
		boolean toreturn = false;
		try {
			ClientResponse response = null;
			if (isDomain) {
				response = rest.properties().updateDomibusProperty(propname, currentVal, domain);
			} else {
				response = rest.properties().updateGlobalProperty(propname, currentVal);
			}

			if (response.getStatus() == 200) {
				toreturn = true;
			}
			Reporter.log(response.getEntity(String.class));
			log.debug(response.getEntity(String.class));

		} catch (Exception e) {
		} finally {
			if (isDomain) {
				rest.properties().updateDomibusProperty(propname, currentVal, domain);
			} else {
				rest.properties().updateGlobalProperty(propname, currentVal);
			}

		}
		return toreturn;
	}


	/* EDELIVERY-7340 - PROP-31 - Update domain property domaintitle */
	@Test(description = "PROP-31", groups = {"multiTenancy"})
	public void domainTitle() throws Exception {
		SoftAssert soft = new SoftAssert();

		String newDomainTitle = Gen.randomAlphaNumeric(5);
		PropertiesPage page = new PropertiesPage(driver);

		String oldVal = modifyProperty("domain.title", true, newDomainTitle);

		page.refreshPage();

		soft.assertEquals(page.getDomainFromTitle(), newDomainTitle, "new domain title has taken effect");

		rest.properties().updateDomibusProperty("domain.title", oldVal);
		soft.assertAll();
	}

	/* EDELIVERY-7341 - PROP-32 - Update domain property domibusUItitlename */
	@Test(description = "PROP-32", groups = {"multiTenancy", "singleTenancy"})
	public void uiTitleName() throws Exception {
		SoftAssert soft = new SoftAssert();

		String newUITitle = Gen.randomAlphaNumeric(5);
		PropertiesPage page = new PropertiesPage(driver);

		String oldVal = modifyProperty("domibus.UI.title.name", true, newUITitle);

		page.refreshPage();

		soft.assertEquals(driver.getTitle(), newUITitle, "new ui title has taken effect");

		rest.properties().updateDomibusProperty("domibus.UI.title.name", oldVal);
		soft.assertAll();
	}

	/* EDELIVERY-7344 - PROP-35 - Update domain property domibusuicsvrowsmax */
	@Test(description = "PROP-35", groups = {"multiTenancy", "singleTenancy"})
	public void csvRowsMax() throws Exception {
		SoftAssert soft = new SoftAssert();

		String expetedErrorMessage = "The number of elements to export [%s] exceeds the maximum allowed [%s].";

		PropertiesPage page = new PropertiesPage(driver);

		String oldVal = modifyProperty("domibus.ui.csv.rows.max", true, "5");
		page.refreshPage();

		page.grid().waitForRowsToLoad();

		int totalItems = page.grid().getPagination().getTotalItems();

		page.getSaveCSVButton().click();
		soft.assertTrue(page.getAlertArea().isError(), "error message present");

		soft.assertEquals(page.getAlertArea().getAlertMessage(), String.format(expetedErrorMessage, totalItems, 5), "correct error message listed");

		rest.properties().updateDomibusProperty("domibus.ui.csv.rows.max", oldVal);
		soft.assertAll();
	}

	/* EDELIVERY-7343 - PROP-34 - Update domain property domibusmonitoringconnectionpartyenabled */
	@Test(description = "PROP-34", groups = {"multiTenancy", "singleTenancy"})
	public void monitoringParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		String partyID = rest.pmodeParties().getParties(null).getJSONObject(0).getString("joinedIdentifiers");
		String oldVal = modifyProperty("domibus.monitoring.connection.party.enabled", true, partyID);

		List<String> monitored = rest.connMonitor().getMonitoredParties(null);

		soft.assertTrue(monitored.size() == 1, "Only one monitored party");
		soft.assertEquals(monitored.get(0), partyID, "monitored party is correct");

		rest.properties().updateDomibusProperty("domibus.monitoring.connection.party.enabled", oldVal);
		soft.assertAll();
	}

	/* EDELIVERY-7352 - PROP-42 - Admin modifies properties */
	@Test(description = "PROP-42", groups = {"multiTenancy"})
	public void adminAccessToProperties() throws Exception {
		SoftAssert soft = new SoftAssert();

		String adminUsername = rest.getUsername(null, DRoles.ADMIN, true, false, true);

		login(adminUsername, data.defaultPass());

		PropertiesPage page = new PropertiesPage(driver);
		page.getSidebar().goToPage(PAGES.PROPERTIES);

		String filename = page.pressSaveCsvAndSaveFile();

		Scanner scanner = new Scanner(new File(filename));
		while (scanner.hasNextLine()) {
			if (scanner.nextLine().contains("GLOBAL")) {
				soft.fail("Global property found");
			}
		}
		scanner.close();


		soft.assertAll();
	}

	/* EDELIVERY-7351 - PROP-41 - Check properties value for each domain */
	@Test(description = "PROP-41", groups = {"multiTenancy"})
	public void propertyValuesSegragatedByDomain() throws Exception {
		SoftAssert soft = new SoftAssert();

		String oldVal = modifyProperty("domibus.UI.title.name", true, "testDefault");

		PropertiesPage page = new PropertiesPage(driver);
		page.getDomainSelector().selectAnotherDomain();

		String anotherDomVal = page.propGrid().getPropertyValue("domibus.UI.title.name");

		soft.assertNotEquals(anotherDomVal, "testDefault", "Property value differs on the other domain");

		rest.properties().updateDomibusProperty("domibus.UI.title.name", oldVal, null);

		soft.assertAll();
	}

	/* EDELIVERY-7351 - PROP-41 - Check properties value for each domain */
	@Test(description = "PROP-41", groups = {"multiTenancy"})
	public void supportTeamData() throws Exception {
		SoftAssert soft = new SoftAssert();

		modifyProperty("domibus.ui.support.team.email", true, "test@email.com");
		modifyProperty("domibus.ui.support.team.name", true, "Test support Team");

		String newEmail = rest.properties().getPropertyValue("domibus.ui.support.team.email", true, null);
		String newTeamName = rest.properties().getPropertyValue("domibus.ui.support.team.name", true, null);

		soft.assertEquals(newEmail, "test@email.com", "");
		soft.assertEquals(newTeamName, "Test support Team", "");


		soft.assertAll();
	}


}
