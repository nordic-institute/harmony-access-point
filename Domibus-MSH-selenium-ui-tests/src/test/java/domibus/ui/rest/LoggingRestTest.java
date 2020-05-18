package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import rest.RestServicePaths;
import utils.Generator;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class LoggingRestTest extends RestTest {

	private String[] logLevels = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF", "ALL"};

	@DataProvider
	private Object[][] validSearches() throws IOException {
		return readCSV("src/test/resources/rest_csv/loggingValidSearches.csv");
	}

	@Test(dataProvider = "validSearches")
	public void search(HashMap<String, String> params) throws Exception {
		SoftAssert soft = new SoftAssert();

		JSONObject logging = rest.logging().searchLogLevels(params);

		soft.assertTrue(logging.has("levels"), "response has levels listed");

		for (int i = 0; i < logLevels.length; i++) {
			String logLevel = logLevels[i];
			soft.assertTrue(logging.getJSONArray("levels").toString().contains(logLevel), logLevel + "present in list of levels");
		}

		soft.assertEquals(params.get("loggerName"), logging.getJSONObject("filter").getString("loggerName"), "Correct filter logername is returned");

		boolean showCls = false;
		if (params.containsKey("showClasses")) {
			showCls = Boolean.valueOf(params.get("showClasses"));
		}

		soft.assertEquals(showCls, logging.getJSONObject("filter").getBoolean("showClasses"), "Correct filter showClasses is returned");
		soft.assertEquals(params.get("page"), ""+logging.getInt("page"), "Correct page is returned");
		soft.assertEquals(params.get("pageSize"), ""+logging.getInt("pageSize"), "Correct page size is returned");

		JSONArray entries = logging.getJSONArray("loggingEntries");
		for (int i = 0; i < entries.length(); i++) {
			soft.assertTrue(entries.getJSONObject(i).getString("name").contains(params.get("loggerName")) , "entries have correct name");
			soft.assertTrue(entries.getJSONObject(i).has("level") , "Key level is present");
			soft.assertTrue(Arrays.asList(logLevels).contains(entries.getJSONObject(i).getString("level")) , "level is one of the approved levels");
		}
		
		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void searchNegativeTests(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();

		HashMap<String, String> params = new HashMap<>();
		params.put("loggerName", evilStr);
		params.put("showClasses", evilStr);
		params.put("page", evilStr);
		params.put("pageSize", evilStr);
		params.put("orderBy", evilStr);
		params.put("asc", evilStr);

		ClientResponse response = rest.requestGET(rest.resource.path(RestServicePaths.LOGGING), params);

		validateInvalidResponse(response, soft, 400);

		soft.assertAll();
	}

	@Test
	public void setLogLevels() throws Exception {
		SoftAssert soft = new SoftAssert();

		JSONArray loggers = rest.logging().getLogLevels();
		String name = loggers.getJSONObject(Generator.randomNumber(loggers.length()-2)).getString("name");
		String currentLevel = loggers.getJSONObject(0).getString("level");

		for (int i = 0; i < logLevels.length; i++) {
			String logLevel = logLevels[i];
			if(logLevel.equalsIgnoreCase(currentLevel)){
				continue;
			}
			String response = rest.logging().setLogLevel(logLevel, name);
			soft.assertTrue(response.contains(name) , "Success message returned");

			JSONArray arr = rest.logging().getLogLevels(name, true);
			for (int j = 0; j < arr.length(); j++) {
				JSONObject lob = arr.getJSONObject(j);
				soft.assertEquals(logLevel, lob.getString("level"), "Level properly updated");
			}
			currentLevel = logLevel;
		}

		soft.assertAll();
	}

	@Test
	public void resetLogLevels() throws Exception {
		SoftAssert soft = new SoftAssert();

		JSONArray loggers = rest.logging().getLogLevels();
			String response = rest.logging().resetLogLevel();
			soft.assertTrue(response.contains("successfully") , "Success message returned");

			JSONArray arr = rest.logging().getLogLevels();
			for (int j = 0; j < arr.length(); j++) {
				JSONObject lob = arr.getJSONObject(j);
				soft.assertEquals("DEBUG", lob.getString("level"), "Level properly updated");
			}


		soft.assertAll();
	}



}