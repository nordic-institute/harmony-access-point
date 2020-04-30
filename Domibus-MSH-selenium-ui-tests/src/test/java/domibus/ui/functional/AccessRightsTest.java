package domibus.ui.functional;


import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import pages.messages.MessagesPage;
import utils.BaseTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import utils.Generator;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class AccessRightsTest extends BaseTest {

	/* Login with valid user with role ROLE_USER */
	@Test(description = "RGT-1", groups = {"multiTenancy", "singleTenancy"})
	public void userRights() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.USER, data.defaultPass(), "Default");
		log.info("Created user with username: " + username);

		login(username, data.defaultPass());
		log.info("Logged in with user: " + username);
		DomibusPage page= new DomibusPage(driver);
		page.waitForTitle();

		log.info("Checking rights for: " + username);
		soft.assertTrue(new DomibusPage(driver).getSidebar().isUserState(), "Options that should be available to an USER are present");

		soft.assertAll();
	}

	/* Login with valid user with role ROLE_USER */
	@Test(description = "RGT-2", groups = {"multiTenancy"})
	public void userAccessDomainSwitch() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.USER, data.defaultPass(), "Default");
		log.info("Created user with username: " + username);

		login(username, data.defaultPass());
		log.info("Logged in with user: " + username);

		log.info("Checking domain selector for: " + username);
		DomibusPage page = new DomibusPage(driver);
		try {
			soft.assertTrue(null == page.getDomainSelector().getSelectedValue(), "Domain selector is NOT present");
		} catch (Exception e) {
		}

		soft.assertAll();
	}

	/*Login with valid user with role ROLE_ADMIN*/
	@Test(description = "RGT-3", groups = {"multiTenancy", "singleTenancy"})
	public void adminRights() throws Exception {
		logout();
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.ADMIN, data.defaultPass(), "Default");
		log.info("Created admin with username: " + username);

		login(username, data.defaultPass());
		log.info("Logged in with admin: " + username);

		DomibusPage page = new DomibusPage(driver);
		page.waitForTitle();
		log.info("Checking rights for admin: " + username);
		soft.assertTrue(page.getSidebar().isAdminState(), "Options that should be available to an ADMIN are present");
		soft.assertAll();
	}

	/*Login with valid user with role ROLE_ADMIN*/
	@Test(description = "RGT-4", groups = {"multiTenancy"})
	public void adminDomainSwitch() throws Exception {
		logout();
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.ADMIN, data.defaultPass(), "Default");
		log.info("Created admin with username: " + username);

		login(username, data.defaultPass());
		log.info("Logged in with admin: " + username);

		DomibusPage page = new DomibusPage(driver);
		log.info("Checking domain selector for admin: " + username);
		try {
			soft.assertTrue(null == page.getDomainSelector().getSelectedValue(), "Domain selector is NOT present");
		} catch (Exception e) {
		}

		soft.assertAll();
	}

	/*Login with valid user with role ROLE_AP_ADMIN*/
	@Test(description = "RGT-5", groups = {"multiTenancy"} )
	public void superAdminRights() throws Exception {
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser());
		log.info("Logged in with super admin");

		DomibusPage page = new DomibusPage(driver);
		log.info("Checking rights for super admin");
		soft.assertTrue(page.getSidebar().isAdminState(), "Options that should be available to an ADMIN are present");

		log.info("Checking domain selector for super user");
		soft.assertTrue(null != page.getDomainSelector().getSelectedValue(), "Domain selector is present and selected value is not null");

		soft.assertAll();
	}

}





