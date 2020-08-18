package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.FilterArea;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import domibus.BaseTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import utils.driver.DriverManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


/**
 * @author Catalin Comanici
 * @version 4.1
 */
public class SeleniumTest extends BaseTest {
	
	static int methodCount = 1;
	public Logger log = LoggerFactory.getLogger(this.getClass().getName());
	public String logFilename;
//	private void makeLoggerLog() throws Exception{
//		PatternLayout layout = new PatternLayout("%d{ISO8601} [%C.%M] - %m%n");
//
//		Path file = Files.createTempFile("autoTests", ".txt");
//		this.logFilename = file.toAbsolutePath().toString();
//
//		FileAppender fileAppender = new FileAppender();
//		fileAppender.setFile(file.toAbsolutePath().toString());
//		fileAppender.setAppend(true);
//		fileAppender.setImmediateFlush(true);
//		fileAppender.setLayout(layout);
//		fileAppender.setName("CustomAppender");
//		fileAppender.setWriter(new BufferedWriter(new FileWriter(logFilename)));
//		log.addAppender(fileAppender);
//	}
	
	
	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() throws Exception {
//		makeLoggerLog();
		log.info("Log file name is " + logFilename);
		log.info("-------- Starting -------");
		cleanMessFilters();
		generateTestData();
	}
	
	@BeforeClass(alwaysRun = true)
	public void beforeClass() throws Exception {
		log.info("--------Initialize test class-------");
		
		driver = DriverManager.getDriver();
		driver.get(data.getUiBaseUrl());
		
	}
	
	@BeforeMethod(alwaysRun = true)
	protected void logSeparator(Method method) throws Exception {
		
		log.info("--------------------------- Running test number: " + methodCount);
		log.info("--------------------------- Running test method: " + method.getDeclaringClass().getSimpleName() + "." + method.getName());
		methodCount++;
		login(data.getAdminUser());
	}
	
	
	@AfterMethod(alwaysRun = true)
	protected void logout() throws Exception {
		DomibusPage page = new DomibusPage(driver);
		
		
		driver.manage().deleteAllCookies();
		((JavascriptExecutor) driver).executeScript("localStorage.clear();");
		
		/*refresh will close any remaining opened modals*/
		page.refreshPage();
		if (page.getSandwichMenu().isLoggedIn()) {
			log.info("Logging out");
			page.getSandwichMenu().logout();
		}
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
		
		try {
			
			if (loginPage.getSandwichMenu().isLoggedIn()) {
				String currentLoggedInUser = loginPage.getSandwichMenu().getCurrentUserID();
				if (StringUtils.equalsIgnoreCase(currentLoggedInUser, user.get("username"))) {
					loginPage.refreshPage();
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
	
	protected String selectRandomDomain() throws Exception {
		if(!data.isMultiDomain()){
			log.info("running in singletenancy mode, no domain to select");
			return null;
		}
		
		List<String> domains = rest.getDomainCodes();
		log.debug("got domains: " + domains);
		
		int index = new Random().nextInt(domains.size());
		
		String domain = domains.get(index);
		log.info("will select domain " +domain);
		
		DomibusPage page = new DomibusPage(driver);
		page.getDomainSelector().selectOptionByText(domain);
		page.wait.forXMillis(500);
		
		return domain;
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
					DObject object = new DObject(filtersArea.getDriver(), element);
					
					soft.assertEquals(object.isPresent(), currentNode.getBoolean("isDefault"),
							String.format("Filter %s isChangePassLnkPresent = %s as expected", field.getName(), currentNode.getBoolean("isDefault")));
					if (currentNode.getBoolean("isDefault")) {
						log.info(object.getAttribute("placeholder"));
						soft.assertEquals(object.getAttribute("placeholder"), currentNode.getString("placeholder"), "Placeholder text is correct - " + currentNode.getString("placeholder"));
					}
					continue;
				}
			}
			
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
					DObject object = new DObject(filtersArea.getDriver(), element);
					
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
