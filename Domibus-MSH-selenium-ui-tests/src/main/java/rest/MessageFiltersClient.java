package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageFiltersClient extends DomibusRestClient{
	// -------------------------------------------- Message Filters ----------------------------------------------------
	public void createMessageFilter(String actionName, String domain) throws JSONException {

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
			throw new RuntimeException("Could not get message filter");
		}
	}

	public void saveMessageFilters(JSONArray filters, String domain) throws JSONException {
		switchDomain(domain);
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), filters.toString());
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not get message filter");
		}
	}

	public void deleteMessageFilter(String actionName, String domain) {

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
			throw new RuntimeException("Could not delete message filter");
		}
	}

	public JSONArray getMessageFilters(String domain) {

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
}