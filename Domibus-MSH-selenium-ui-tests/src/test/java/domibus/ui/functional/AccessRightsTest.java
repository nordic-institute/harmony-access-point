package domibus.ui.functional;


import io.qameta.allure.*;
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


@Epic("Rights and Privileges")
@Feature("Functional")
public class AccessRightsTest extends SeleniumTest {

	/*  RGT-1 - Login with valid user with role ROLEUSER  */
	@Description("RGT-1 - Login with valid user with role ROLEUSER")
	@Link(name = "EDELIVERY-5048", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5048")
	@AllureId("RGT-1")
	@Test(description = "RGT-1", groups = {"multiTenancy", "singleTenancy"})
	public void userRights() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.USER, data.defaultPass(), "Default");
		Allure.step("Created user with username: " + username);
		log.info("Created user with username: " + username);

		login(username, data.defaultPass());
		Allure.step("Logged in with user: " + username);
		log.info("Logged in with user: " + username);

		Allure.step("Checking rights for: " + username);
		log.info("Checking rights for: " + username);
		soft.assertTrue(new DomibusPage(driver).getSidebar().isUserState(), "Options that should be available to an USER are present");

		soft.assertAll();
	}

	/*  RGT-2 - Check domain switch for role ROLEUSER  */
	@Description("RGT-2 - Check domain switch for role ROLEUSER")
	@Link(name = "EDELIVERY-5049", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5049")
	@AllureId("RGT-2")
	@Test(description = "RGT-2", groups = {"multiTenancy"})
	public void userAccessDomainSwitch() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.USER, data.defaultPass(), "Default");
		Allure.step("Created user with username: " + username);
		log.info("Created user with username: " + username);

		login(username, data.defaultPass());
		Allure.step("Logged in with user: " + username);
		log.info("Logged in with user: " + username);

		Allure.step("Checking domain selector for: " + username);
		log.info("Checking domain selector for: " + username);
		DomibusPage page = new DomibusPage(driver);
		try {
			soft.assertTrue(null == page.getDomainSelector().getSelectedValue(), "Domain selector is NOT present");
		} catch (Exception e) {
		}

		soft.assertAll();
	}

	/*  RGT-3 - Login with valid user with role ROLEADMIN  */
	@Description("RGT-3 - Login with valid user with role ROLEADMIN")
	@Link(name = "EDELIVERY-5050", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5050")
	@AllureId("RGT-3")
	@Test(description = "RGT-3", groups = {"multiTenancy", "singleTenancy"})
	public void adminRights() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), "Default");
		Allure.step("Created admin with username: " + username);
		log.info("Created admin with username: " + username);

		login(username, data.defaultPass());
		Allure.step("Logged in with admin: " + username);
		log.info("Logged in with admin: " + username);

		DomibusPage page = new DomibusPage(driver);
		Allure.step("Checking rights for admin: " + username);
		log.info("Checking rights for admin: " + username);
		soft.assertTrue(page.getSidebar().isAdminState(), "Options that should be available to an ADMIN are present");
		soft.assertAll();
	}

	/*  RGT-4 - Check domain switch for role ROLEADMIN  */
	@Description("RGT-4 - Check domain switch for role ROLEADMIN")
	@Link(name = "EDELIVERY-5051", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5051")
	@AllureId("RGT-4")
	@Test(description = "RGT-4", groups = {"multiTenancy"})
	public void adminDomainSwitch() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Gen.randomAlphaNumeric(10);
		rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), "Default");
		Allure.step("Created admin with username: " + username);
		log.info("Created admin with username: " + username);

		login(username, data.defaultPass());
		Allure.step("Logged in with admin: " + username);
		log.info("Logged in with admin: " + username);

		DomibusPage page = new DomibusPage(driver);
		Allure.step("Checking domain selector for admin: " + username);
		log.info("Checking domain selector for admin: " + username);
		try {
			soft.assertTrue(null == page.getDomainSelector().getSelectedValue(), "Domain selector is NOT present");
		} catch (Exception e) {
		}

		soft.assertAll();
	}

	/*  RGT-5 - Login with valid user with role ROLEAPADMIN  */
	@Description("RGT-5 - Login with valid user with role ROLEAPADMIN")
	@Link(name = "EDELIVERY-5052", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-5052")
	@AllureId("RGT-5")
	@Test(description = "RGT-5", groups = {"multiTenancy"})
	public void superAdminRights() throws Exception {
		SoftAssert soft = new SoftAssert();

		login(data.getAdminUser());
		Allure.step("Logged in with super admin");
		log.info("Logged in with super admin");

		DomibusPage page = new DomibusPage(driver);
		Allure.step("Checking rights for super admin");
		log.info("Checking rights for super admin");
		soft.assertTrue(page.getSidebar().isSuperState(), "Options that should be available to an SUPER ADMIN are present");

		Allure.step("Checking domain selector for super user");
		log.info("Checking domain selector for super user");
		soft.assertTrue(null != page.getDomainSelector().getSelectedValue(), "Domain selector is present and selected value is not null");

		soft.assertAll();
	}


	/*  RGT-6 - Resend message as ROLEUSER  */
	@Description("RGT-6 - Resend message as ROLEUSER")
	@Link(name = "EDELIVERY-7203", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7203")
	@AllureId("RGT-6")
	@Test(description = "RGT-6", groups = {"multiTenancy", "singleTenancy"})
	public void userResendMessage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.USER, true, false, true);
		login(username, data.defaultPass());
		Allure.step("Logged in with user " + username);
		log.info("Logged in with user " + username);

		MessagesPage page = new MessagesPage(driver);
		page.grid().waitForRowsToLoad();

		page.grid().scrollToAndSelect("Message Status", "SEND_FAILURE");

		boolean isPresent = false;
		try {
			isPresent = page.getResendButton().isPresent();
		} catch (Exception e) {
		}

		soft.assertFalse(isPresent, "Resend button is present");

		soft.assertAll();
	}

	/*  RGT-7 - Access Logging page as ROLEADMIN  */
	@Description("RGT-7 - Access Logging page as ROLEADMIN")
	@Link(name = "EDELIVERY-7204", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7204")
	@AllureId("RGT-7")
	@Test(description = "RGT-7", groups = {"multiTenancy", "singleTenancy"})
	public void adminAccessLoggingPage() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = rest.getUsername(null, DRoles.ADMIN, true, false, true);
		login(username, data.defaultPass());
		Allure.step("Logged in with admin " + username);
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





