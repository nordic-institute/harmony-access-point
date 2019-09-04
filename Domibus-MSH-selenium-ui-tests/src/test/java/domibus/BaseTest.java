package domibus;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DRoles;
import org.json.JSONArray;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import pages.login.LoginPage;
import rest.DomibusRestClient;
import utils.DriverManager;
import utils.Generator;
import utils.TestRunData;
import utils.soap_client.DomibusC1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @version 4.1
*/

public class BaseTest {

	public static WebDriver driver;
	public static TestRunData data = new TestRunData();
	public static DomibusRestClient rest = new DomibusRestClient();
	public static DomibusC1 messageSender = new DomibusC1();

	protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());


	/**
	 * Starts the browser and navigates to the homepage. This happens once before the test
	 * suite and the browser window is reused for all tests in suite
	 */
	@BeforeSuite(alwaysRun = true)
	public void beforeClass(){
		log.info("-------- Starting -------");
		driver = DriverManager.getDriver();
		driver.get(data.getUiBaseUrl());
	}


	/**After the test suite is done we close the browser*/
	@AfterSuite(alwaysRun = true)
	public void afterClassSuite(){
		log.info("-------- Quitting -------");
		try {
			driver.quit();
		} catch (Exception e) {
			log.warn("Closing the driver failed");
			e.printStackTrace();
		}
	}

	/**After each test method page is refreshed and logout is attempted*/
	@AfterMethod(alwaysRun = true)
	protected void logout() throws Exception{
		DomibusPage page = new DomibusPage(driver);

		/*refresh will close any remaining opened modals*/
		page.refreshPage();
		if (page.getSandwichMenu().isLoggedIn()) {
			log.info("Logging out");
			page.getSandwichMenu().logout();
		}
	}

	/**Before each test method we will log a separator to make logs more readable*/
	@BeforeMethod(alwaysRun = true)
	protected void logSeparator() throws Exception{
		log.info("----------------------------------------------");
	}

	protected DomibusPage login(HashMap<String, String> user){
		log.info("login started");
		LoginPage loginPage = new LoginPage(driver);

		try {
			loginPage.login(user);
		} catch (Exception e) {
			e.printStackTrace();
		}

		loginPage.waitForTitle();

		return new DomibusPage(driver);
	}

	protected DomibusPage login(String user, String pass){

		HashMap<String, String> userInfo = new HashMap<>();
		userInfo.put("username", user);
		userInfo.put("pass", pass);

		login(userInfo);

		return new DomibusPage(driver);
	}


	public List<String> getMessageIDs(String domainCode, int noOfNecessaryMessages, boolean forceNew) throws Exception {
		JSONArray mess = rest.getListOfMessages(domainCode);
		List<String> messIDs = new ArrayList<>();

		if(forceNew){
			return sendMessages(noOfNecessaryMessages, domainCode);
		}

		if(mess.length() < noOfNecessaryMessages){
			List<String> sentMess = sendMessages(noOfNecessaryMessages-mess.length(), domainCode);
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

		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),domainCode);
		log.info("Created plugin user " + user + " on domain " + domainCode);

		log.info("Uploading PMODE ");
		rest.uploadPMode("pmodes/pmode-blue.xml", null);

		for (int i = 0; i < noOf; i++) {
			messIDs.add(messageSender.sendMessage(user, data.getDefaultTestPass(), messageRefID, conversationID));
		}
		log.info("Sent messages " + noOf);

		rest.deletePluginUser(user, domainCode);
		log.info("deleted plugin user" + user);
		return messIDs;
	}


}
