package domibus.ui.functional;


import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import domibus.BaseUXTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import utils.Generator;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class AccessRightsTest extends BaseUXTest {

	/*Login with valid user with role ROLE_AP_ADMIN*/
	@Test(description = "RGT-5", groups = {"multiTenancy"})
	public void superAdminRights() throws Exception {
		SoftAssert soft = new SoftAssert();
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login(data.getAdminUser());
		log.info("Logged in with super admin");

		DomibusPage page = new DomibusPage(driver);

		soft.assertTrue(page.getSidebar().isAdminState(), "Options that should be available to an ADMIN are present");
		soft.assertTrue(null != page.getDomainSelector().getSelectedValue(), "Domain selector is present and selected value is not null");

		soft.assertAll();
	}

	/*Login with valid user with role ROLE_ADMIN*/
	@Test(description = "RGT-3", groups = {"multiTenancy", "singleTenancy"})
	public void adminRights() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), "Default");

		LoginPage loginPage = new LoginPage(driver);
		loginPage.login(username, data.getDefaultTestPass());

		DomibusPage page = new DomibusPage(driver);

		soft.assertTrue(page.getSidebar().isAdminState(), "Options that should be available to an ADMIN are present");
		soft.assertAll();
	}

	/*Login with valid user with role ROLE_ADMIN*/
	@Test(description = "RGT-4", groups = {"multiTenancy"})
	public void adminDomainSwitch() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), "Default");

		LoginPage loginPage = new LoginPage(driver);
		loginPage.login(username, data.getDefaultTestPass());

		DomibusPage page = new DomibusPage(driver);

		try {
			soft.assertTrue(null == page.getDomainSelector().getSelectedValue(), "Domain selector is NOT present");
		} catch (Exception e) {

		}

		soft.assertAll();
	}

	/* Login with valid user with role ROLE_USER */
	@Test(description = "RGT-1", groups = {"multiTenancy", "singleTenancy"})
	public void userRights() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), "Default");

		LoginPage loginPage = new LoginPage(driver);
		loginPage.login(username, data.getDefaultTestPass());

		DomibusPage page = new DomibusPage(driver);

		soft.assertTrue(page.getSidebar().isUserState(), "Options that should be available to an ADMIN are present");
		soft.assertAll();
	}

	/* Login with valid user with role ROLE_USER */
	@Test(description = "RGT-2", groups = {"multiTenancy"})
	public void userAccessDomainSwitch() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), "Default");

		LoginPage loginPage = new LoginPage(driver);
		loginPage.login(username, data.getDefaultTestPass());

		DomibusPage page = new DomibusPage(driver);
		try {
			soft.assertTrue(null == page.getDomainSelector().getSelectedValue(), "Domain selector is NOT present");
		} catch (Exception e) { }

		soft.assertAll();
	}


}





