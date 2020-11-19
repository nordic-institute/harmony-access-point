package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.properties.PropertiesPage;

import java.util.HashMap;
import java.util.List;

public class PropertiesPgTest extends SeleniumTest {

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

}
