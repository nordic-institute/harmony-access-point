package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageFiltersClient extends BaseRestClient {
	
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
			e.printStackTrace();
		}
		
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), currentMSGF.toString());
		if (response.getStatus() != 200) {
			throw new Exception("Could not get message filter");
		}
	}
	
	public void saveMessageFilters(JSONArray filters, String domain) throws Exception {
		switchDomain(domain);
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), filters.toString());
		if (response.getStatus() != 200) {
			throw new Exception("Could not get message filter");
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
			e.printStackTrace();
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
			e.printStackTrace();
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
}
