package utils;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import pages.errorLog.ErrFilters;
import pages.jms.JMSFilters;
import pages.login.LoginPage;
import pages.messages.MessagesPage;
import pages.plugin_users.PluginUsersFilterArea;
import pages.pmode.PartiesFilters;
import rest.DomibusRestClient;
import utils.driver.DriverManager;
import utils.soap_client.DomibusC1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @version 4.1
 */
@Listeners(utils.customReporter.FailListener.class)
public class BaseTest {

	public static WebDriver driver;
	public static TestRunData data = new TestRunData();
	public static DomibusRestClient rest = new DomibusRestClient();
	public static DomibusC1 messageSender = new DomibusC1();

	public final Logger log = LoggerFactory.getLogger(this.getClass().getName());


	/**
	 * Starts the browser and navigates to the homepage. This happens once before the test
	 * suite and the browser window is reused for all tests in suite
	 */
	@BeforeSuite(alwaysRun = true)
	public void beforeClass() {
		log.info("-------- Starting -------");
		driver = DriverManager.getDriver();
		driver.get(data.getUiBaseUrl());
	}


	/** After the test suite is done we close the browser */
	@AfterSuite(alwaysRun = true)
	public void afterClassSuite() {
		log.info("-------- Quitting -------");
		try {
			driver.quit();
		} catch (Exception e) {
			log.warn("Closing the driver failed");
			e.printStackTrace();
		}
	}

	/**
	 * After each test method page is refreshed and logout is attempted
	 */
	@AfterMethod(alwaysRun = true)
	protected void logout() throws Exception {
		DomibusPage page = new DomibusPage(driver);

		/*refresh will close any remaining opened modals*/
		page.refreshPage();
		if (page.getSandwichMenu().isLoggedIn()) {
			log.info("Logging out");
			page.getSandwichMenu().logout();
		}
	}

	/**
	 * Before each test method we will log a separator to make logs more readable
	 */
	@BeforeMethod(alwaysRun = true)
	protected void logSeparator() throws Exception {
		log.info("---------------------------");
	}

	protected DomibusPage login(HashMap<String, String> user) {
		log.info("login started");
		LoginPage loginPage = new LoginPage(driver);

		try {
			loginPage.login(user);
			loginPage.waitForTitle();
		} catch (Exception e) {
			log.info("Login did not succeed!!!");
			log.debug(e.getMessage());
		}

		return new DomibusPage(driver);
	}

	protected DomibusPage login(String user, String pass) {

		HashMap<String, String> userInfo = new HashMap<>();
		userInfo.put("username", user);
		userInfo.put("pass", pass);

		login(userInfo);

		return new DomibusPage(driver);
	}


	public List<String> getMessageIDs(String domainCode, int noOfNecessaryMessages, boolean forceNew) throws Exception {
		JSONArray mess = rest.getListOfMessages(domainCode);
		List<String> messIDs = new ArrayList<>();

		if (forceNew) {
			return sendMessages(noOfNecessaryMessages, domainCode);
		}

		if (mess.length() < noOfNecessaryMessages) {
			List<String> sentMess = sendMessages(noOfNecessaryMessages - mess.length(), domainCode);
			messIDs.addAll(sentMess);
		}

		for (int i = 0; i < mess.length(); i++) {
			messIDs.add(mess.getJSONObject(i).getString("messageId"));
		}

		return messIDs;
	}

	public List<String> sendMessages(int noOf, String domainCode) throws Exception {
		List<String> messIDs = new ArrayList<>();

		String user = Generator.randomAlphaNumeric(10);
		String messageRefID = Generator.randomAlphaNumeric(10);
		String conversationID = Generator.randomAlphaNumeric(10);

		rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), domainCode);
		log.info("Created plugin user " + user + " on domain " + domainCode);

		log.info("Uploading PMODE ");
		rest.uploadPMode("pmodes/pmode-blue.xml", null);

		for (int i = 0; i < noOf; i++) {
			messIDs.add(messageSender.sendMessage(user, data.defaultPass(), messageRefID, conversationID));
		}
		log.info("Sent messages " + noOf);

		rest.deletePluginUser(user, domainCode);
		log.info("deleted plugin user" + user);
		return messIDs;
	}

	public JSONObject getUser(String domain, String role, boolean active, boolean deleted, boolean forceNew) throws Exception {
		String username = Generator.randomAlphaNumeric(10);

		if (StringUtils.isEmpty(domain)) {
			domain = "default";
		}

		if (!forceNew) {
			log.info("trying to find existing user with desired config");
			JSONArray users = rest.getUsers(domain);
			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(user.getString("userName"), "super")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "admin")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "user")
				) {
					log.info("skipping default users");
					continue;
				}

				if (StringUtils.equalsIgnoreCase(user.getString("domain"), domain)
						&& StringUtils.equalsIgnoreCase(user.getString("roles"), role)
						&& user.getBoolean("active") == active
						&& user.getBoolean("deleted") == deleted) {
					log.info("found user " + user.getString("userName"));
					return user;
				}
			}
		}

		rest.createUser(username, role, data.defaultPass(), domain);
		log.info("created user " + username);

		if (!active) {
			rest.blockUser(username, domain);
			log.info("deactivated user " + username);
		}
		if (deleted) {
			rest.deleteUser(username, domain);
			log.info("deleted user " + username);
		}

		JSONArray users = rest.getUsers(domain);
		log.info("searching for user in the system");
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(user.getString("userName"), username)) {
				log.info("user found and returned");
				return user;
			}
		}
		log.info("user not found .. returning null");
		return null;
	}

	public JSONObject getPluginUser(String domain, String role, boolean active, boolean forceNew) throws Exception {
		String username = Generator.randomAlphaNumeric(10);

		if (StringUtils.isEmpty(domain)) {
			domain = "default";
		}

		if (!forceNew) {
			log.info("trying to find existing user with desired config");
			JSONArray users = rest.getPluginUsers(domain, "BASIC");
			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(user.getString("userName"), "super")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "admin")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "user")
				) {
					log.info("skipping default users");
					continue;
				}

				if (!StringUtils.equalsIgnoreCase(user.getString("userName"), "null")
						&& StringUtils.equalsIgnoreCase(user.getString("authRoles"), role)
						&& user.getBoolean("active") == active) {
					log.info("found user " + user.getString("userName"));
					return user;
				}
			}
		}

		rest.createPluginUser(username, role, data.defaultPass(), domain);
		log.info("created user " + username);

		if (!active) {
			rest.blockUser(username, domain);
			log.info("deactivated user " + username);
		}

		JSONArray users = rest.getPluginUsers(domain, "BASIC");
		log.info("searching for user in the system");
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(user.getString("userName"), username)) {
				log.info("user found and returned");
				return user;
			}
		}
		log.info("user not found .. returning null");
		return null;
	}

	public String getNonDefaultDomain() throws Exception {
		log.info("getting domains");
		List<String> domains = rest.getDomainNames();
		String domain1 = "";
		for (String domain : domains) {
			if (!StringUtils.equalsIgnoreCase(domain, "Default")) {
				domain1 = domain;
				break;
			}
		}
		return domain1;
	}

	// This method will fill default black listed char in all input fields of different pages
	public  void fillBlackListedChar(PAGES page,String inputString) throws Exception {

		switch (page) {
			case MESSAGES:
				log.debug("Enter black listed char in input fields of Message page");
				MessagesPage mPage = new MessagesPage(driver);
				mPage.getFilters().advancedFilterBy(inputString, "ACKNOWLEDGED", inputString, inputString,
						inputString, "SENDING", "USER_MESSAGE", "NOTIFIED", inputString, inputString, inputString, null, null);
				break;

			case ERROR_LOG:
				log.debug("Enter black listed char in input fields of Error log page");
				ErrFilters eFilter = new ErrFilters(driver);
				eFilter.advancedSearch(inputString, inputString, null, null, inputString,
						"SENDING", "EBMS_0001", null, null);
				break;

			case PMODE_PARTIES:
				log.debug("Enter black listed char in input fields of Pmode parties page");
				PartiesFilters pPage = new PartiesFilters(driver);
				pPage.filter(inputString, inputString, inputString, inputString, PartiesFilters.PROCESS_ROLE.IR);
				break;

			case JMS_MONITORING:
				log.debug("Enter black listed char in input fields of JMS Monitoring page");
				JMSFilters jPage = new JMSFilters(driver);
				jPage.getJmsSelectorInput().fill(inputString);
				jPage.getJmsTypeInput().fill(inputString);
				jPage.getJmsSearchButton().click();
				break;

			case PLUGIN_USERS:
				log.debug("Enter black listed char in input fields of Plugin user page");
				PluginUsersFilterArea puPage = new PluginUsersFilterArea(driver);
				puPage.search(null, null, inputString, inputString);
				break;

		}

	}


	}
