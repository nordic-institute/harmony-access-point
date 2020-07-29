package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MessageFiltersClient extends BaseRestClient {
	
	String[] criteriaOrder = {"from", "to", "action", "service"};
	
	
	public MessageFiltersClient(String username, String password) {
		super(username, password);
	}
	
	// -------------------------------------------- Message Filters ----------------------------------------------------
	public void createMessageFilter(String actionName, String domain) throws Exception {
		
		String payload = provider.createMessageFilterObj(actionName);
		
		switchDomain(domain);
		
		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null).getEntity(String.class);
		JSONArray currentMSGF = null;
		try {
			currentMSGF = new JSONObject(sanitizeResponse(currentMSGFRaw)).getJSONArray("messageFilterEntries");
			currentMSGF.put(new JSONObject(payload));
		} catch (JSONException e) {
			log.error("EXCEPTION: ", e);
		}
		
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), currentMSGF.toString());
		if (response.getStatus() != 200) {
			throw new Exception("Could not get message filter");
		}
	}
	
	public int createMessageFilter(String plugin, String from, String to, String action, String service, String domain) throws Exception {
		
		switchDomain(domain);
		
		JSONArray currentFilters = getMessageFilters(domain);
		JSONArray preppedFilters = prepExistingList(currentFilters);
		preppedFilters.put(createMsgFilterEntity(plugin, from, to, action, service));
		
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), preppedFilters.toString());
		
		if (response.getStatus() != 200) {
			throw new Exception("Could not get message filter");
		}
		
		JSONArray newFilters = getMessageFilters(domain);
		for (int i = 0; i < newFilters.length(); i++) {
			String nF = newFilters.getJSONObject(i).toString();
			
			if (!nF.contains(plugin)) {
				continue;
			}
			
			if (StringUtils.isNotEmpty(from)) {
				if (!nF.contains(from)) {
					continue;
				}
			} else {
				if (nF.contains("from")) {
					continue;
				}
			}
			if (StringUtils.isNotEmpty(to)) {
				if (!nF.contains(to)) {
					continue;
				}
			} else {
				if (nF.contains("to")) {
					continue;
				}
			}
			if (StringUtils.isNotEmpty(action)) {
				if (!nF.contains(action)) {
					continue;
				}
			} else {
				if (nF.contains("action")) {
					continue;
				}
			}
			if (StringUtils.isNotEmpty(service)) {
				if (!nF.contains(service)) {
					continue;
				}
			} else {
				if (nF.contains("service")) {
					continue;
				}
			}
			return newFilters.getJSONObject(i).getInt("entityId");
		}
		return -1;
	}
	
	public void saveMessageFilters(JSONArray filters, String domain) throws Exception {
		switchDomain(domain);
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), filters.toString());
		
		if (response.getStatus() != 200) {
			throw new Exception("Could not get message filter " + response.getStatus());
		}
	}
	
	public void deleteMessageFilter(String actionName, String domain) throws Exception {
		
		switchDomain(domain);
		
		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null).getEntity(String.class);
		JSONArray currentMSGF;
		JSONArray deletedL = new JSONArray();
		
		try {
			currentMSGF = new JSONObject(sanitizeResponse(currentMSGFRaw)).getJSONArray("messageFilterEntries");
			
			for (int i = 0; i < currentMSGF.length(); i++) {
				JSONObject filter = currentMSGF.getJSONObject(i);
				if (!filter.toString().contains(actionName)) {
					deletedL.put(filter);
				}
			}
		} catch (JSONException e) {
			log.error("EXCEPTION: ", e);
		}
		
		
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), deletedL.toString());
		if (response.getStatus() != 200) {
			log.debug(String.valueOf(response.getStatus()));
			log.debug(response.getEntity(String.class));
			throw new Exception("Could not delete message filter");
		}
	}
	
	public JSONArray getMessageFilters(String domain) throws Exception {
		
		switchDomain(domain);
		
		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null).getEntity(String.class);
		JSONArray currentMSGF = new JSONArray();
		
		try {
			currentMSGF = new JSONObject(sanitizeResponse(currentMSGFRaw)).getJSONArray("messageFilterEntries");
		} catch (JSONException e) {
			log.error("EXCEPTION: ", e);
		}
		return currentMSGF;
	}
	
	public ClientResponse updateFilterList(JSONArray toSendMSGFS, String domain) throws Exception {
		switchDomain(domain);
		return jsonPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), toSendMSGFS.toString());
	}
	
	public ClientResponse updateFilterList(String toSend, String domain) throws Exception {
		switchDomain(domain);
		return jsonPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), toSend);
	}
	
	
	private JSONObject createMsgFilterEntity(String plugin, String from, String to, String action, String service) {
		HashMap<String, String> toCreate = new HashMap<>();
		if (StringUtils.isNotEmpty(plugin)) {
			toCreate.put("plugin", plugin);
		}
		if (StringUtils.isNotEmpty(from)) {
			toCreate.put("from", from);
		}
		if (StringUtils.isNotEmpty(to)) {
			toCreate.put("to", to);
		}
		if (StringUtils.isNotEmpty(action)) {
			toCreate.put("action", action);
		}
		if (StringUtils.isNotEmpty(service)) {
			toCreate.put("service", service);
		}
		return createMsgFilterEntity(toCreate);
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
			if (!filterInfo.containsKey(cuCriteria)) {
				continue;
			}
			
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
	
	private JSONArray prepExistingList(JSONArray array) {
		
		for (int i = 0; i < array.length(); i++) {
			JSONObject msgf = array.getJSONObject(i);
			JSONArray rtCrt = msgf.getJSONArray("routingCriterias");
			
			for (int j = 0; j < criteriaOrder.length; j++) {
				String key = criteriaOrder[j];
				msgf.put(key, JSONObject.NULL);
				
				for (int k = 0; k < rtCrt.length(); k++) {
					JSONObject crt = rtCrt.getJSONObject(k);
					if (crt.getString("name").equalsIgnoreCase(key)) {
						msgf.put(key, crt);
					}
				}
				
			}
			
			
		}
		
		
		return array;
	}
	
	
}
