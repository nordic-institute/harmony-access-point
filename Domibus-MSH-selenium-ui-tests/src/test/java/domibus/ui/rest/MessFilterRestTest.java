package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import rest.RestServicePaths;
import utils.Generator;

import java.util.HashMap;

public class MessFilterRestTest extends RestTest {

	String[] criteriaOrder = {"from", "to", "action", "service"};

	@Test
	public void createNewFilter() {
		SoftAssert soft = new SoftAssert();

		String rndStr = Generator.randomAlphaNumeric(5);

		HashMap<String, String> newFilterInfo = new HashMap<>();
		newFilterInfo.put("plugin", messageFilterPlugins.get(0));
		newFilterInfo.put("from", "from:" + rndStr);
		newFilterInfo.put("to", "to:" + rndStr);
		newFilterInfo.put("action", "action" + rndStr);
		newFilterInfo.put("service", "service:" + rndStr);

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		JSONObject newFilter = createMsgFilterEntity(newFilterInfo);
		JSONArray toSendMSGFS = msgfs;
		toSendMSGFS.put(newFilter);
		System.out.println("msgfs = " + toSendMSGFS);
		rest.messFilters().updateFilterList(toSendMSGFS, null);

		JSONArray newMsgfs = rest.messFilters().getMessageFilters(null);

		boolean found = false;
		for (int i = 0; i < newMsgfs.length(); i++) {
			JSONObject msgf = newMsgfs.getJSONObject(i);

			boolean isEqual = true;

			for (String value : newFilterInfo.values()) {
				if (!msgf.toString().contains(value)) {
					isEqual = false;
				}
			}

			if (isEqual) {
				found = true;
				break;
			}
		}

		soft.assertTrue(found, "New filter is created and found in the list");
		soft.assertAll();
	}

	@Test
	public void createNewFilterNoPlugin() {
		SoftAssert soft = new SoftAssert();

		String rndStr = Generator.randomAlphaNumeric(5);

		HashMap<String, String> newFilterInfo = new HashMap<>();

		newFilterInfo.put("from", "from:" + rndStr);
		newFilterInfo.put("to", "to:" + rndStr);
		newFilterInfo.put("action", "action" + rndStr);
		newFilterInfo.put("service", "service:" + rndStr);

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		JSONObject newFilter = createMsgFilterEntity(newFilterInfo);
		JSONArray toSendMSGFS = msgfs;
		toSendMSGFS.put(newFilter);
		System.out.println("msgfs = " + toSendMSGFS);
		ClientResponse response = rest.messFilters().updateFilterList(toSendMSGFS, null);

		Integer status=  response.getStatus();
		String responseContent = getSanitizedStringResponse(response);
		log.debug("Response status: " + status);
		log.debug("Response content: " + responseContent);

		soft.assertTrue(status == 400, "Expected response status is 400 but found " + status);

		try {
			new JSONObject(responseContent);
		} catch (JSONException e) {
			soft.fail("Response is not in JSON format");
		}
		soft.assertAll();
	}

	@Test
	public void createNewFilterMalformedJSON() {
		SoftAssert soft = new SoftAssert();

		String rndStr = Generator.randomAlphaNumeric(5);

		HashMap<String, String> newFilterInfo = new HashMap<>();
		newFilterInfo.put("plugin", messageFilterPlugins.get(0));
		newFilterInfo.put("from", "from:" + rndStr);
		newFilterInfo.put("to", "to:" + rndStr);
		newFilterInfo.put("action", "action" + rndStr);
		newFilterInfo.put("service", "service:" + rndStr);

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		JSONObject newFilter = createMsgFilterEntity(newFilterInfo);
		JSONArray toSendMSGFS = msgfs;
		toSendMSGFS.put(newFilter);
		System.out.println("msgfs = " + toSendMSGFS);
		String toSend = StringUtils.substring(toSendMSGFS.toString(), 0, -5);
		ClientResponse response = rest.messFilters().updateFilterList(toSend, null); //jsonPUT(rest.resource.path(RestServicePaths.MESSAGE_FILTERS), toSend);

		Integer status=  response.getStatus();
		String responseContent = getSanitizedStringResponse(response);
		log.debug("Response status: " + status);
		log.debug("Response content: " + responseContent);

		soft.assertTrue(status == 400, "Expected response status is 400 but found " + status);

		try {
			new JSONObject(responseContent);
		} catch (JSONException e) {
			soft.fail("Response is not in JSON format");
		}

		soft.assertAll();
	}

	@Test
	public void createNewFilterDuplicate() {
		SoftAssert soft = new SoftAssert();

		String rndStr = Generator.randomAlphaNumeric(5);

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		JSONObject duplicato = msgfs.getJSONObject(msgfs.length()-1);

		duplicato.put("persisted", false);
		duplicato.remove("entityId");
		duplicato.remove("index");

		msgfs.put(duplicato);

		ClientResponse response = rest.messFilters().updateFilterList(msgfs, null); //jsonPUT(rest.resource.path(RestServicePaths.MESSAGE_FILTERS), msgfs.toString());

		Integer status=  response.getStatus();
		String responseContent = getSanitizedStringResponse(response);
		log.debug("Response status: " + status);
		log.debug("Response content: " + responseContent);

		soft.assertTrue(status == 400, "Expected response status is 400 but found " + status);

		try {
			new JSONObject(responseContent);
		} catch (JSONException e) {
			soft.fail("Response is not in JSON format");
		}

		soft.assertAll();
	}

	@Test
	public void createNewFilterRoutingCriteriasOrder() {
		SoftAssert soft = new SoftAssert();

		String rndStr = Generator.randomAlphaNumeric(5);

		HashMap<String, String> newFilterInfo = new HashMap<>();
		newFilterInfo.put("plugin", messageFilterPlugins.get(0));
		newFilterInfo.put("from", "from:" + rndStr);
		newFilterInfo.put("to", "to:" + rndStr);
		newFilterInfo.put("action", "action" + rndStr);
		newFilterInfo.put("service", "service:" + rndStr);

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		JSONObject newFilter = createMsgFilterEntity(newFilterInfo);

		JSONObject crit = (JSONObject) newFilter.getJSONArray("routingCriterias").remove(0);
		newFilter.getJSONArray("routingCriterias").put(crit);

		JSONArray toSendMSGFS = msgfs;
		toSendMSGFS.put(newFilter);
		System.out.println("msgfs = " + toSendMSGFS);
		rest.messFilters().updateFilterList(toSendMSGFS, null); //jsonPUT(rest.resource.path(RestServicePaths.MESSAGE_FILTERS), toSendMSGFS.toString());

		JSONArray newMsgfs = rest.messFilters().getMessageFilters(null);

		boolean found = false;
		for (int i = 0; i < newMsgfs.length(); i++) {
			JSONObject msgf = newMsgfs.getJSONObject(i);
			boolean isEqual = true;
			for (String value : newFilterInfo.values()) {
				if (!msgf.toString().contains(value)) {
					isEqual = false;
				}
			}

			if (isEqual) {
				found = true;
				break;
			}
		}
		for (int i = 0; i < newMsgfs.length(); i++) {
			JSONObject currentFilter = newMsgfs.getJSONObject(i);
			if(currentFilter.toString().contains(rndStr)){
				JSONArray rcrits = currentFilter.getJSONArray("routingCriterias");
				for (int j = 0; j < rcrits.length(); j++) {
					JSONObject rcrit = rcrits.getJSONObject(j);
					if(rcrit.getString("name").equalsIgnoreCase("action")){
						newMsgfs.getJSONObject(i).getJSONArray("routingCriterias").getJSONObject(j).put("expression", Generator.randomAlphaNumeric(11));
						Object action = newMsgfs.getJSONObject(i).getJSONArray("routingCriterias").remove(j);
						newMsgfs.getJSONObject(i).getJSONArray("routingCriterias").put(action);
					}
				}
			}
		}
		rest.messFilters().saveMessageFilters(newMsgfs, null);

		soft.assertTrue(found, "New filter is created and found in the list");



		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void createNewFilterNegativeTests(String evilStr) {
		SoftAssert soft = new SoftAssert();

		HashMap<String, String> newFilterInfo = new HashMap<>();
		newFilterInfo.put("plugin", messageFilterPlugins.get(0));
		newFilterInfo.put("from", evilStr);
		newFilterInfo.put("to", evilStr);
		newFilterInfo.put("action", evilStr);
		newFilterInfo.put("service", evilStr);

		JSONArray msgfs = rest.messFilters().getMessageFilters(null);
		JSONObject newFilter = createMsgFilterEntity(newFilterInfo);
		JSONArray toSendMSGFS = msgfs;
		toSendMSGFS.put(newFilter);
		System.out.println("msgfs = " + toSendMSGFS);
		ClientResponse response = rest.messFilters().updateFilterList(toSendMSGFS, null); //jsonPUT(rest.resource.path(RestServicePaths.MESSAGE_FILTERS), toSendMSGFS.toString());

		Integer status=  response.getStatus();
		String responseContent = getSanitizedStringResponse(response);

		log.debug("Response status: " + status);
		log.debug("Response content: " + responseContent);

		soft.assertTrue(status == 400, "Expected response status is 400 but found " + status);

		try {
			new JSONObject(responseContent);
		} catch (JSONException e) {
			soft.fail("Response is not in JSON format");
		}

		JSONArray newMsgfs = rest.messFilters().getMessageFilters(null);
		boolean found = false;
		for (int i = 0; i < newMsgfs.length(); i++) {
			JSONObject msgf = newMsgfs.getJSONObject(i);
			boolean isEqual = true;
			for (String value : newFilterInfo.values()) {
				if (!msgf.toString().contains(value)) {
					isEqual = false;
				}
			}
			if (isEqual) {
				found = true;
				break;
			}
		}

		soft.assertFalse(found, "New filter should not be created");
		soft.assertAll();
	}

	@Test
	public void deleteFilterTest() {
		SoftAssert soft = new SoftAssert();
		String rndActionName = Generator.randomAlphaNumeric(20);
		rest.messFilters().createMessageFilter(rndActionName, null);

		JSONArray arr = rest.messFilters().getMessageFilters(null);
		soft.assertTrue(arr.toString().contains(rndActionName), "New filter was created");

		rest.messFilters().deleteMessageFilter(rndActionName, null);

		arr = rest.messFilters().getMessageFilters(null);
		soft.assertFalse(arr.toString().contains(rndActionName), "New filter was deleted");

		soft.assertAll();
	}






	private JSONObject createMsgFilterEntity(HashMap<String, String> filterInfo) {
		JSONObject obj = new JSONObject();
		obj.put("entityId", 0);
		obj.put("index", 0);
		obj.put("persisted", false);

		String plugin = StringUtils.EMPTY;
		if (filterInfo.containsKey("plugin")) {
			plugin = filterInfo.get("plugin");
		}
		obj.put("backendName", plugin);


		JSONArray routingCriterias = new JSONArray();
		for (int i = 0; i < criteriaOrder.length; i++) {
			String cuCriteria = criteriaOrder[i];
			if(!filterInfo.containsKey(cuCriteria)){continue;}

			JSONObject crit = createRoutingCritEntity(cuCriteria, filterInfo.get(cuCriteria));
			obj.put(cuCriteria, crit);
			routingCriterias.put(crit);
		}

		obj.put("routingCriterias", routingCriterias);

		return obj;
	}

	private JSONObject createRoutingCritEntity(String name, String expression) {
		JSONObject obj = new JSONObject();
		obj.put("entityId", JSONObject.NULL);
		obj.put("name", name);
		obj.put("expression", expression);
		return obj;
	}

}
