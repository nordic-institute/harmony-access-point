package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.util.HashMap;

public class ErrorLogRestTest extends RestTest {
	
	@DataProvider
	private Object[][] searchFilterCombinations() throws IOException {
		return readCSV("src/test/resources/rest_csv/errorsValidSearches.csv");
	}
	
	@Test(dataProvider = "searchFilterCombinations")
	public void searchErrors(HashMap<String, String> params) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONObject err;
		try {
			err = rest.errors().getErrors(null).getJSONObject(0);
		} catch (Exception e) {
			log.error("EXCEPTION: ", e);
			throw new SkipException("Could not get error info");
		}
		
		for (String key : params.keySet()) {
			if (params.get(key).equalsIgnoreCase("<FILL>")) {
				String value = err.getString(key);
				params.put(key, value);
			}
		}
		log.debug("Using filters: " + params.toString());
		
		ClientResponse response = rest.errors().getErrors(null, params);
		soft.assertTrue(response.getStatus() == 200, "Response status was " + response.getStatus());
		
		JSONObject obj = null;
		try {
			obj = new JSONObject(getSanitizedStringResponse(response));
		} catch (JSONException e) {
			soft.fail("response not in json format");
		}
		
		if (null != obj) {
			soft.assertTrue(obj.has("filter"), "response contains filter node");
			soft.assertTrue(obj.has("errorLogEntries"), "response contains errorLogEntries node");
			soft.assertTrue(obj.has("mshRoles"), "response contains mshRoles node");
			soft.assertTrue(obj.has("errorCodes"), "response contains errorCodes node");
			soft.assertTrue(obj.has("count"), "response contains count node");
			soft.assertTrue(obj.has("page"), "response contains page node");
			soft.assertTrue(obj.has("pageSize"), "response contains pageSize node");
			
			int respPageSize = obj.getInt("pageSize");
			soft.assertTrue(respPageSize == 10 || respPageSize == Integer.valueOf(params.get("pageSize")), "Correct page size in response");
			
			int count = obj.getInt("count");
			
			if (count < respPageSize) {
				soft.assertTrue(obj.getJSONArray("errorLogEntries").length() == count, "Count is correct");
			} else {
				soft.assertTrue(obj.getJSONArray("errorLogEntries").length() == respPageSize, "Entries length is page size");
			}
		}
		
		soft.assertAll();
	}
	
	@Test(dataProvider = "readInvalidStrings")
	public void searchErrorsNegativeTests(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		String[] keys = {"asc", "errorCode", "errorDetail", "errorSignalMessageId", "messageInErrorId", "mshRole", "notifiedFrom", "notifiedTo", "orderBy", "page", "pageSize", "timestampFrom", "timestampTo"};
		HashMap<String, String> params = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			params.put(key, evilStr);
		}
		
		log.debug("used params" + params.toString());
		ClientResponse response = rest.errors().getErrors(null, params); //requestGET(rest.resource.path(RestServicePaths.ERRORS), params);
		validateInvalidResponse(response, soft);
		
		soft.assertAll();
	}
}
