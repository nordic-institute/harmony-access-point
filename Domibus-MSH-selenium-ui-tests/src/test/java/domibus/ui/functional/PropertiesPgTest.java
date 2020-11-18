package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.properties.PropertiesPage;

import java.util.List;

public class PropertiesPgTest extends SeleniumTest {

	/*EDELIVERY-7302 - PROP-1 - Verify presence of Domibus Properties page*/
	@Test(description = "PROP-1", groups = {"multiTenancy", "singleTenancy"})
	public void pageAvailability() throws Exception {
		SoftAssert soft = new SoftAssert();

		DomibusPage page = new DomibusPage(driver);
		log.info("checking if option is available for system admin");
		soft.assertTrue( page.getSidebar().isLinkPresent(PAGES.PROPERTIES), data.getAdminUser().get("username") + "has the option to access properties");

		if(data.isMultiDomain()){
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



}
