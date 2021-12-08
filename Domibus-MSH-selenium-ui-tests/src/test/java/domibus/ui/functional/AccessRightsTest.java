package domibus.ui.functional;


import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessagesPage;
import utils.Gen;




public class AccessRightsTest extends SeleniumTest {

    /* EDELIVERY-5048 - RGT-1 - Login with valid user with role ROLEUSER */
	@Test(description = "RGT-1", groups = {"multiTenancy", "singleTenancy"})
	public void userRights() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.USER, data.defaultPass(), "Default");
		log.info("Created user with username: " + username);

		login(username, data.defaultPass());
		log.info("Logged in with user: " + username);

		log.info("Checking rights for: " + username);
		soft.assertTrue(new DomibusPage(driver).getSidebar().isUserState(), "Options that should be available to an USER are present");

		soft.assertAll();
	}

    /* EDELIVERY-5049 - RGT-2 - Check domain switch for role ROLEUSER */
	@Test(description = "RGT-2", groups = {"multiTenancy"})
	public void userAccessDomainSwitch() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.USER, data.defaultPass(), "Default");
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

    /* EDELIVERY-5050 - RGT-3 - Login with valid user with role ROLEADMIN */
	@Test(description = "RGT-3", groups = {"multiTenancy", "singleTenancy"})
	public void adminRights() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), "Default");
		log.info("Created admin with username: " + username);

		login(username, data.defaultPass());
		log.info("Logged in with admin: " + username);

		DomibusPage page = new DomibusPage(driver);
		log.info("Checking rights for admin: " + username);
		soft.assertTrue(page.getSidebar().isAdminState(), "Options that should be available to an ADMIN are present");
		soft.assertAll();
	}

    /* EDELIVERY-5051 - RGT-4 - Check domain switch for role ROLEADMIN */
	@Test(description = "RGT-4", groups = {"multiTenancy"})
	public void adminDomainSwitch() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), "Default");
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

    /* EDELIVERY-5052 - RGT-5 - Login with valid user with role ROLEAPADMIN */
	@Test(description = "RGT-5", groups = {"multiTenancy"})
	public void superAdminRights() throws Exception {
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser());
		log.info("Logged in with super admin");

		DomibusPage page = new DomibusPage(driver);
		log.info("Checking rights for super admin");
		soft.assertTrue(page.getSidebar().isSuperState(), "Options that should be available to an SUPER ADMIN are present");

		log.info("Checking domain selector for super user");
		soft.assertTrue(null != page.getDomainSelector().getSelectedValue(), "Domain selector is present and selected value is not null");

		soft.assertAll();
	}


    /* EDELIVERY-7203 - RGT-6 - Resend message as ROLEUSER */
	@Test(description = "RGT-6", groups = {"multiTenancy", "singleTenancy"})
	public void userResendMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, true);
		login(username, data.defaultPass());
		log.info("Logged in with user " + username);

		MessagesPage page = new MessagesPage(driver);
		page.grid().waitForRowsToLoad();

		try {
			page.grid().scrollToAndSelect("Message Status", "SEND_FAILURE");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SkipException(e.getMessage());
		}

		boolean isPresent = false;
		try {
			isPresent = page.getResendButton().isPresent();
		} catch (Exception e) {
		}

		soft.assertFalse(isPresent, "Resend button is present");

		soft.assertAll();
	}

    /* EDELIVERY-7204 - RGT-7 - Access Logging page as ROLEADMIN */
	@Test(description = "RGT-7", groups = {"multiTenancy", "singleTenancy"})
	public void adminAccessLoggingPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.ADMIN, true, false, true);
		login(username, data.defaultPass());
		log.info("Logged in with admin " + username);

		boolean isPresent = false;

		try {
			new DomibusPage(driver).getSidebar().getPageLnk(PAGES.LOGGING).getLinkText();
			isPresent = true;
		} catch (Exception e) {
		}

		soft.assertFalse(isPresent, "Link is present");

		soft.assertAll();
	}

}





