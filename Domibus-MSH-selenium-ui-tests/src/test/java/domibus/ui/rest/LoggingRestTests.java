package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import rest.RestServicePaths;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class LoggingRestTests extends RestTest {

	private String[] logLevels = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF", "ALL"};


	@Test
	public void search() throws Exception {
		SoftAssert soft = new SoftAssert();

		HashMap<String, String> params = new HashMap<>();
		params.put("loggerName", "eu.domibus");
		params.put("page", "0");
		params.put("pageSize", "10000");
		params.put("orderBy", "name");
		params.put("asc", "false");

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


}