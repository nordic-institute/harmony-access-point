package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.FilterArea;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.*;
import domibus.BaseTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import utils.Gen;
import utils.driver.DriverManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class SeleniumTest extends BaseTest {


	static int methodCount = 1;
	public String logFilename;

	public static final Logger log = LoggerFactory.getLogger(SeleniumTest.class);


	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() throws Exception {

		log.info("Log file name is " + logFilename);
		log.info("-------- Starting -------");

//		deleteAllPluginUsers();
		generateTestData();
	}

	public void deleteAllPluginUsers() throws Exception {
		String[] pluginUserTypes = {"BASIC", "CERTIFICATE"};
		List<String> domains = rest.getDomainCodes();

		for (int i = 0; i < pluginUserTypes.length; i++) {
			String type = pluginUserTypes[i];
			for (String domain : domains) {
				ArrayList<String> plus = rest.pluginUsers().getPluginUsernameList(type,domain);
				for (String s : plus) {
					rest.pluginUsers().deletePluginUser(s, domain);
				}
			}
		}



	}



	@AfterSuite(alwaysRun = true)
	public void afterSuite() throws Exception {
	}

	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws Exception {
		log.info("--------Initialize test class-------");
		driver = DriverManager.getDriver();
	}

	@BeforeMethod(alwaysRun = true)
	protected void beforeMethod(Method method) throws Exception {

		MDC.put("logFileName", method.getDeclaringClass().getSimpleName());

		log.info("--------------------------- Running test number: " + methodCount);
		log.info("--------------------------- Running test method: " + method.getDeclaringClass().getSimpleName() + "." + method.getName());
		methodCount++;

		try {
			driver.get(data.getUiBaseUrl());
		} catch (Exception e) {
			driver = DriverManager.getDriver();
			driver.get(data.getUiBaseUrl());
		}
		new DomibusPage(driver).waitForPageToLoad();

		login(data.getAdminUser());
	}


	@AfterClass(alwaysRun = true)
	protected void afterClass() throws Exception {
		
		log.info("-------- Quitting driver after test class-------");
		try {
			driver.quit();
		} catch (Exception e) {
			log.warn("Closing the driver failed");
			log.error("EXCEPTION: ", e);
		}


	}


	protected DomibusPage login(HashMap<String, String> user) {
		log.info("login started");
		LoginPage loginPage = new LoginPage(driver);
		loginPage.refreshPage();

		try {

			if (loginPage.getSandwichMenu().isLoggedIn()) {
				String currentLoggedInUser = loginPage.getCurrentLoggedInUser();
				if (StringUtils.equalsIgnoreCase(currentLoggedInUser, user.get("username"))) {
					loginPage.refreshPage();
					loginPage.getDomainSelector().selectOptionByText("default");
					return new DomibusPage(driver);
				}
				logout();
			}

			loginPage.login(user);
			loginPage.waitForPageToLoad();
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

	protected void logout() throws Exception {
		DomibusPage page = new DomibusPage(driver);

		driver.manage().deleteAllCookies();
		((JavascriptExecutor) driver).executeScript("localStorage.clear();");

		page.refreshPage();
		if (page.getSandwichMenu().isLoggedIn()) {
			log.info("Logging out");
			page.getSandwichMenu().logout();
		}
	}

	protected String selectRandomDomain() throws Exception {
		if (!data.isMultiDomain()) {
			log.info("running in singletenancy mode, no domain to select");
			return null;
		}
		DomibusPage page = new DomibusPage(driver);
		return page.getDomainFromTitle();
	}

	protected <T extends FilterArea> void basicFilterPresence(SoftAssert soft, T filtersArea, JSONArray filtersDescription) throws Exception {

		log.info("checking basic filter presence");
		Field[] fields = filtersArea.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (!field.getType().toString().contains("WebElement")) {
				log.info(String.format("Skipping filed %s because it is not of type WebElement.", field.getName()));
				continue;
			}

			for (int i = 0; i < filtersDescription.length(); i++) {
				JSONObject currentNode = filtersDescription.getJSONObject(i);

				if (StringUtils.equalsIgnoreCase(currentNode.getString("name"), field.getName())) {

					log.info(String.format("Evaluating filter with description %s", currentNode.toString()));

					WebElement element = (WebElement) field.get(filtersArea);
					DObject object = new DObject(driver, element);

					soft.assertEquals(object.isPresent(), currentNode.getBoolean("isDefault"),
							String.format("Filter %s isChangePassLnkPresent = %s as expected", field.getName(), currentNode.getBoolean("isDefault")));
					if (currentNode.getBoolean("isDefault")) {
						String placeHolder = object.getAttribute("placeholder");
						if(!(null == placeHolder && StringUtils.isEmpty(currentNode.getString("placeholder")))){
							soft.assertEquals(object.getAttribute("placeholder"), currentNode.getString("placeholder"), "Placeholder text is correct - " + currentNode.getString("placeholder"));
						}
					}
					continue;
				}
			}

		}
	}

	protected void filterValuesTrimm(SoftAssert soft) throws Exception {

		String term = "  "+Gen.randomAlphaNumeric(5)+"  ";
		DomibusPage page = new DomibusPage(driver);
		page.waitForPageToLoad();

		try {
			WebElement filterFrom  = driver.findElement(By.name("filterForm"));

			try {
				filterFrom.findElement(By.id("advancedlink_id")).click();
			} catch (Exception e) {	}

			List<WebElement> inputs = filterFrom.findElements(By.cssSelector("input:not(input.md2-datepicker-value, input.mat-checkbox-input"));
			for (WebElement input : inputs) {
				new DInput(driver, input).fill(term);
			}

			driver.findElement(By.id("searchbutton_id")).click();

			page.waitForPageToLoad();

			for (WebElement input : inputs) {
				String newTerm = new DInput(driver, input).getText();
				soft.assertEquals(newTerm, term.trim(), "Input not properly trimmed in page " + page.getTitle());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected <T extends FilterArea> void advancedFilterPresence(SoftAssert soft, T filtersArea, JSONArray filtersDescription) throws Exception {

		log.info("checking advanced filter presence");

		Field[] fields = filtersArea.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (!field.getType().toString().contains("WebElement")) {
				log.info(String.format("Skipping filed %s because it is not of type WebElement.", field.getName()));
				continue;
			}

			for (int i = 0; i < filtersDescription.length(); i++) {
				JSONObject currentNode = filtersDescription.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(currentNode.getString("name"), field.getName())) {

					log.info(String.format("Evaluating filter with description %s", currentNode.toString()));

					WebElement element = (WebElement) field.get(filtersArea);
//					DObject object = new DObject(filtersArea.getDriver(), element);
					DObject object = new DObject(driver, element);

					soft.assertEquals(object.isPresent(), true,
							String.format("Filter %s as expected", field.getName()));

					String expected = currentNode.getString("placeholder");
					String actual = object.getAttribute("placeholder");

					if (StringUtils.isEmpty(expected) && StringUtils.isEmpty(actual)) {
						continue;
					}

					log.debug("placeholder: " + actual);
					soft.assertEquals(actual, expected, "Placeholder text is correct - " + expected);
				}
			}
		}
	}

	public <T extends DGrid> void testDefaultColumnPresence(SoftAssert soft, T grid, JSONArray gridDesc) throws Exception {
		log.info("Asserting grid default state");
		List<String> columns = new ArrayList<>();
		List<String> visibleColumns = grid.getColumnNames();

		for (int i = 0; i < gridDesc.length(); i++) {
			JSONObject colDesc = gridDesc.getJSONObject(i);
			if (colDesc.getBoolean("visibleByDefault")) {
				columns.add(colDesc.getString("name"));
			}
		}

		for (String column : columns) {
			soft.assertTrue(visibleColumns.contains(column), String.format("Column %s is found to be visible", column));
		}
	}

	public <T extends DomibusPage> void testButtonPresence(SoftAssert soft, T page, JSONArray buttons) throws Exception {
		log.info("Asserting button default state");

		Field[] fields = page.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (!field.getType().toString().contains("WebElement")) {
				log.info(String.format("Skipping filed %s because it is not of type WebElement.", field.getName()));
				continue;
			}

			for (int i = 0; i < buttons.length(); i++) {
				JSONObject curButton = buttons.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(curButton.getString("name"), field.getName())) {
					log.info(String.format("Evaluating button with description %s", curButton.toString()));

					WebElement element = (WebElement) field.get(page);
					DButton dButton = new DButton(driver, element);

					soft.assertEquals(dButton.isVisible(), curButton.getBoolean("visibleByDefault"), String.format("Button %s visibility is as described", curButton.getString("label")));
					soft.assertEquals(dButton.isEnabled(), curButton.getBoolean("enabledByDefault"), String.format("Button %s is enabled/disabled as described", curButton.getString("label")));
					soft.assertEquals(dButton.getText(), curButton.getString("label"), String.format("Button %s has expected label", curButton.getString("label")));
				}
			}

		}
	}

	protected <T extends DGrid> void testColumnControlsAvailableOptions(SoftAssert soft, T grid, JSONArray columns) throws Exception {
		log.info("checking column controls and available options");
		List<String> controlOptions = new ArrayList<>(grid.getGridCtrl().getAllCheckboxStatuses().keySet());
		for (int i = 0; i < columns.length(); i++) {
			String currentColumn = columns.getJSONObject(i).getString("name");
			log.info("Check option for " + currentColumn);
			soft.assertTrue(controlOptions.contains(currentColumn),
					String.format("Column %s present in the list of options in column controls", currentColumn));
		}
	}


}
