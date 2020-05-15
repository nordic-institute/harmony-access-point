package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.Generator;

import java.util.HashMap;

public class PropertiesTest extends RestTest {


	@Test
	public void searchProperties() throws Exception {
		SoftAssert soft = new SoftAssert();

		JSONArray props = rest.properties().getAllProperties();
		for (int i = 0; i < props.length(); i++) {
			JSONObject prop = props.getJSONObject(i);
			JSONArray results = rest.properties().searchProperties(prop.getString("name"));
			for (int j = 0; j < results.length(); j++) {
				JSONObject result = results.getJSONObject(j);
				soft.assertTrue(result.getString("name").contains(prop.getString("name")), "result contains search term");
			}
		}
		soft.assertAll();
	}

	@Test
	public void updateProperty() throws Exception {
		SoftAssert soft = new SoftAssert();

		JSONArray props = rest.properties().searchProperties("plugin");
			JSONObject prop = props.getJSONObject(5);
			log.debug("" + prop);

			String name = prop.getString("name");
			String value = prop.optString("value");
			String newValue = Generator.randomAlphaNumeric(20);

			ClientResponse response = rest.properties().updateDomibusProperty(name, newValue);
			soft.assertTrue(response.getStatus() == 200, "Updated successfully");

			JSONArray arr = rest.properties().searchProperties(name);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject curProp = arr.getJSONObject(i);
				if(StringUtils.equalsIgnoreCase(name, curProp.getString("name"))){
					soft.assertEquals(curProp.optString("value"), newValue, "Property has updated value");
					break;
				}
			}

			response = rest.properties().updateDomibusProperty(name, value);
			soft.assertTrue(response.getStatus() == 200, "Reset successfully");

			arr = rest.properties().searchProperties(name);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject curProp = arr.getJSONObject(i);
				if(StringUtils.equalsIgnoreCase(name, curProp.getString("name"))){
					soft.assertEquals(curProp.optString("value"), value, "Property was reset");
					break;
				}
			}


		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void updatePropertyNegativeTests(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();

		JSONArray props = rest.properties().searchProperties("plugin");
			JSONObject prop = props.getJSONObject(5);
			log.debug("" + prop);

			String name = prop.getString("name");
			String value = prop.optString("value");
			String newValue = evilStr;

			ClientResponse response = rest.properties().updateDomibusProperty(name, newValue);
			validateInvalidResponse(response, soft, 400);

		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void searchPropertiesNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		HashMap<String, String> params = new HashMap<>();
		params.put("showDomain", "true");
		params.put("name", evilStr);
		params.put("page", "0");
		params.put("pageSize", "10000");

		ClientResponse response = rest.properties().searchProperties(params);
		validateInvalidResponse(response, soft, 400);

		soft.assertAll();
	}


}
