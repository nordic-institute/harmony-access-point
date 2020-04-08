package domibus.ui.rest;

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

		JSONArray msgfs = rest.getMessageFilters(null);
		JSONObject newFilter = createMsgFilterEntity(newFilterInfo, getIndex(msgfs));
		JSONArray toSendMSGFS = msgfs; //transformFiltersArr(msgfs);
		toSendMSGFS.put(newFilter);
		System.out.println("msgfs = " + toSendMSGFS);
		rest.jsonPUT(rest.resource.path(RestServicePaths.MESSAGE_FILTERS), toSendMSGFS.toString());

		JSONArray newMsgfs = rest.getMessageFilters(null);

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


	private JSONObject createMsgFilterEntity(HashMap<String, String> filterInfo, int index) {
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

	private int getIndex(JSONArray arr) {
		int index = 0;
		for (int i = 0; i < arr.length(); i++) {
			index = Math.max(index, arr.getJSONObject(i).getInt("index"));
		}
		return index+2;
	}

	private JSONArray transformFiltersArr(JSONArray arr){



		for (int i = 0; i < arr.length(); i++) {
			JSONObject msgf = arr.getJSONObject(i);

			JSONArray routingCriterias = msgf.getJSONArray("routingCriterias");

			for (int j = 0; j < criteriaOrder.length; j++) {
				String crit = criteriaOrder[j];
				boolean found = false;
				for (int k = 0; k < routingCriterias.length(); k++) {
					JSONObject rCrit = routingCriterias.getJSONObject(k);
					if(rCrit.getString("name").equalsIgnoreCase(crit)){
						msgf.put(crit, rCrit);
						found = true;
					}
				}
				if (!found){
					msgf.put(crit, JSONObject.NULL);
				}
			}
		}
		return arr;
	}



}
