package utils;

import ddsl.dcomponents.DomainSelector;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.FilterArea;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @version 4.1
 */

public class BaseUXTest extends BaseTest {

	/**
	 * Starts the browser and navigates to the homepage. This happens once before the test
	 * suite and the browser window is reused for all tests in suite
	 */
	@BeforeTest(alwaysRun = true)
	public void beforeTest() throws Exception {
		log.info("-------- Starting -------");
		DomibusPage page = new DomibusPage(driver);
		if (!page.getSandwichMenu().isLoggedIn()) {
			login(data.getAdminUser());
		}
	}


	/**After UX test page is refreshed and logout is attempted*/
	@AfterTest(alwaysRun = true)
	protected void logout() throws Exception{
		DomibusPage page = new DomibusPage(driver);

		/*refresh will close any remaining opened modals*/
		page.refreshPage();
		if (page.getSandwichMenu().isLoggedIn()) {
			log.info("Logging out");
			page.getSandwichMenu().logout();
		}
	}

	@AfterMethod(alwaysRun = true)
	public void refresh() throws Exception {
		try {
			if (data.isIsMultiDomain()) {
				DomainSelector ds = new DomibusPage(driver).getDomainSelector();
				log.info("reseting to default domain");
				if(!StringUtils.equals(ds.getSelectedValue(), "Default")){
					ds.selectOptionByText("Default");
				}
			}
		} catch (Exception e) {	}

		log.info("-------- Refreshing -------");
		driver.navigate().refresh();
	}



	protected DomibusPage login(HashMap<String, String> user) {
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

	protected DomibusPage login(String user, String pass) {

		HashMap<String, String> userInfo = new HashMap<>();
		userInfo.put("username", user);
		userInfo.put("pass", data.defaultPass());

		login(userInfo);

		return new DomibusPage(driver);
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

					log.info(String.format("Evaualting filter with description %s", currentNode.toString()));

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

					log.info(String.format("Evaualting filter with description %s", currentNode.toString()));

					WebElement element = (WebElement) field.get(filtersArea);
					DObject object = new DObject(filtersArea.getDriver(), element);

					soft.assertEquals(object.isPresent(), true,
							String.format("Filter %s isChangePassLnkPresent as expected", field.getName()));

					log.info(object.getAttribute("placeholder"));
					soft.assertEquals(object.getAttribute("placeholder"), currentNode.getString("placeholder"), "Placeholder text is correct - " + currentNode.getString("placeholder"));

					continue;
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

	public <T extends DomibusPage> void testButonPresence(SoftAssert soft, T page, JSONArray buttons) throws Exception {
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
					log.info(String.format("Evaualting button with description %s", curButton.toString()));

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
		log.info("checking column controls and avaialable options");
		List<String> controlOptions = new ArrayList<>(grid.getGridCtrl().getAllCheckboxStatuses().keySet());
		for (int i = 0; i < columns.length(); i++) {
			String currentColumn = columns.getJSONObject(i).getString("name");
			log.info("Check option for " + currentColumn);
			soft.assertTrue(controlOptions.contains(currentColumn),
					String.format("Column %s present in the list of options in column controls", currentColumn));
		}
	}

}
