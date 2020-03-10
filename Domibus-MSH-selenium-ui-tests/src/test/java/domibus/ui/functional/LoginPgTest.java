package domibus.ui.functional;


import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import utils.BaseTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import utils.Generator;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class LoginPgTest extends BaseTest {


	private void checkUserLogin(String role, SoftAssert soft) throws Exception {
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, role, data.defaultPass(), null);
		log.info(String.format("Created user %s with role %s", username, role));

		log.info(String.format("Login %s with role %s", username, role));
		login(username, data.defaultPass());

		DomibusPage page = new DomibusPage(driver);
		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");

		log.info("Logout");
		page.getSandwichMenu().logout();

//		rest.deleteUser(username, null);
		log.info("Deleted user: " + username);
	}

	/**Checks whether login as system admin works*/
	@Test(description = "LGN-1", groups = {"multiTenancy", "singleTenancy"})
	public void validLogin() throws Exception {
		log.info("Testing valid login with every type of user");
		SoftAssert soft = new SoftAssert();

		if(data.isMultiDomain()){
			checkUserLogin(DRoles.SUPER, soft);
		}
		checkUserLogin(DRoles.ADMIN, soft);
		checkUserLogin(DRoles.USER, soft);

		soft.assertAll();
	}

	/**Login using invalid username*/
	@Test(description = "LGN-2", groups = {"multiTenancy", "singleTenancy"})
	public void invalidUsername() throws Exception {
		log.info("Testing login using invalid username");
		SoftAssert soft = new SoftAssert();

		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.USER, data.defaultPass(), null);
		log.info(String.format("Created user %s with role %s", username, DRoles.USER));

		LoginPage page = new LoginPage(driver);

		page.login("invalidUserTest", data.defaultPass());
		log.info(String.format("Trying to login with user=%s and pass=%s", "invalidUserTest", data.defaultPass()));

		soft.assertFalse(page.getSandwichMenu().isLoggedIn(), "User not logged in");
		soft.assertTrue(page.isLoaded(), "User is still on Login page");

		log.info("Verifying correct error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_INVALID_CREDENTIALS, "Displayed message is correct");

		//rest.deleteUser(username, null);
		log.info("Deleted user: " + username);

		soft.assertAll();
	}

	/**Login using invalid password but valid username*/
	@Test(description = "LGN-3", groups = {"multiTenancy", "singleTenancy"})
	public void invalidPassword() throws Exception {
		log.info("Testing login using invalid password");
		SoftAssert soft = new SoftAssert();

		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.USER, data.defaultPass(), null);
		log.info(String.format("Created user %s with role %s", username, DRoles.USER));

		LoginPage page = new LoginPage(driver);

		page.login(username, "invalidPassword");
		log.info(String.format("Trying to login with user=%s and pass=%s", username, "invalidPassword"));

		soft.assertFalse(page.getSandwichMenu().isLoggedIn(), "User not logged in");
		soft.assertTrue(page.isLoaded(), "User is still on Login page");

		log.info("Verifying correct error message");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_INVALID_CREDENTIALS, "Displayed message is correct");

//		rest.deleteUser(username, null);
		log.info("Deleted user: " + username);

		soft.assertAll();
	}

	/**Try to login with valid username and invalid password more than 5 times*/
	@Test(description = "LGN-4", groups = {"multiTenancy", "singleTenancy"})
	public void blockUserAccountTest() throws Exception {
		log.info("Try to login with valid username and invalid password more than 5 times");
		SoftAssert soft = new SoftAssert();
		String username = "testBlockAcc_" + Generator.randomAlphaNumeric(3);
		rest.createUser(username, DRoles.USER, data.defaultPass(), null);
		log.info(String.format("Created user %s with role %s", username, DRoles.USER));


		LoginPage page = new LoginPage(driver);

		for (int i = 0; i < 5; i++) {
			log.info(String.format("Trying to login with user=%s and pass=%s", username, "password So Wrong"));
			page.login(username, "password So Wrong");

			soft.assertFalse(page.getSandwichMenu().isLoggedIn(), "User not logged in");
			soft.assertTrue(page.isLoaded(), "User is still on Login page");

			log.info("Verifying error is displayed");
			soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed");

			if (i <= 4) {
				log.info("Verifying LOGIN_INVALID_CREDENTIALS error message is displayed");
				soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_INVALID_CREDENTIALS, "Displayed message is correct");
			} else {
				log.info("Verifying LOGIN_ACCOUNT_SUSPENDED error message is displayed");
				soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_ACCOUNT_SUSPENDED, "Account blocked message displayed as expected");
			}
		}

		log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
		page.login(username, data.defaultPass());
		soft.assertTrue(page.isLoaded(), "User is still on Login page");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed (2)");
		log.info("Verifying LOGIN_ACCOUNT_SUSPENDED_1 error message is displayed");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_ACCOUNT_SUSPENDED_1, "Displayed message is correct (2)");

		rest.unblockUser(username, null);
		log.info("Unblocked user account");

//		wait required because the unlock is done trough REST API
		page.wait.forXMillis(500);

		log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
		page.login(username, data.defaultPass());
		soft.assertTrue(new DomibusPage(driver).getSandwichMenu().isLoggedIn(), "User is on Messages page, account is unblocked");

//		rest.deleteUser(username, null);
		log.info("Deleted user: " + username);

		soft.assertAll();
	}

	/**Admin unlocks account and user tries to login with valid username and password*/
	@Test(description = "LGN-5", groups = {"multiTenancy", "singleTenancy"})
	public void unblockedAccountCanLogin() throws Exception {
		log.info("Admin unlocks account and user tries to login with valid username and password");
		SoftAssert soft = new SoftAssert();
		String username = "testBlockAcc_" + Generator.randomAlphaNumeric(3);
		rest.createUser(username, DRoles.USER, data.defaultPass(), null);
		log.info(String.format("Created user %s with role %s", username, DRoles.USER));


		LoginPage page = new LoginPage(driver);

		for (int i = 0; i < 5; i++) {
			log.info(String.format("Trying to login with user=%s and pass=%s", username, "password So Wrong"));
			page.login(username, "password So Wrong");
		}

		log.info(String.format("Trying to login with user=%s and pass=%s", username, data.defaultPass()));
		page.login(username, data.defaultPass());
		log.info("Verifying LOGIN_ACCOUNT_SUSPENDED_1 error message is displayed");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.LOGIN_ACCOUNT_SUSPENDED_1, "User account blocked confirmed");

		rest.unblockUser(username, null);
		log.info("Unblocked user account");

//		wait required because the unlock is done trough REST API
		page.wait.forXMillis(500);

		log.info("Attempting login after account is unblocked");
		page.login(username, data.defaultPass());
		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User is on Messages page, account is unblocked");

//		rest.deleteUser(username, null);
		log.info("Deleted user: " + username);

		soft.assertAll();
	}


}





