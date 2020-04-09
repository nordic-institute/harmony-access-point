package domibus.ui.rest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
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
		rest.jsonPUT(rest.resource.path(RestServicePaths.MESSAGE_FILTERS), toSendMSGFS.toString());

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

		soft.assertTrue(found);
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
