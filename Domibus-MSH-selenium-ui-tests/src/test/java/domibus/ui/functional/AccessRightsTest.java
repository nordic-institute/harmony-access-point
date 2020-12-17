package domibus.ui.functional;


import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessagesPage;
import utils.Gen;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class AccessRightsTest extends SeleniumTest {
	
	/* Login with valid user with role ROLE_USER */
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
	
	/* Login with valid user with role ROLE_USER */
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
	
	/*Login with valid user with role ROLE_ADMIN*/
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
	
	/*Login with valid user with role ROLE_ADMIN*/
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
	
	/*Login with valid user with role ROLE_AP_ADMIN*/
	@Test(description = "RGT-5", groups = {"multiTenancy"})
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


	/* RGT-6 - Resend message as ROLE_USER */
	@Test(description = "RGT-6", groups = {"multiTenancy", "singleTenancy"})
	public void userResendMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, true);
		login(username, data.defaultPass());
		log.info("Logged in with user " + username);

		MessagesPage page = new MessagesPage(driver);
		page.grid().waitForRowsToLoad();

		page.grid().scrollToAndSelect("Message Status", "SEND_FAILURE");

		boolean isPresent = false;
		try {
			isPresent = page.getResendButton().isPresent();
		} catch (Exception e) { }

		soft.assertFalse(isPresent, "Resend button is present");

		soft.assertAll();
	}

	/* RGT-7 - Access Logging page as ROLE_ADMIN */
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
		} catch (Exception e) { }

		soft.assertFalse(isPresent, "Link is present");

		soft.assertAll();
	}

}





