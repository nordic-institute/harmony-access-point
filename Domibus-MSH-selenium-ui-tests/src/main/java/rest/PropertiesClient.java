package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class PropertiesClient extends DomibusRestClient{


	// -------------------------------------------- Domibus Properties -----------------------------------------------------------
	public JSONArray getDomibusPropertyDetail(HashMap<String, String> params) throws Exception {
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.DOMIBUS_PROPERTIES), params);
		if (clientResponse.getStatus() != 200) {
			throw new RuntimeException("Could not get properties ");
		}
		return new JSONObject(sanitizeResponse(clientResponse.getEntity(String.class))).getJSONArray("items");

	}

	public void updateDomibusProperty(String propertyName, HashMap<String, String> params, String payload) throws Exception {

		String RestServicePathForPropertyUpdate = RestServicePaths.DOMIBUS_PROPERTIES + "/" + propertyName;
		ClientResponse clientResponse = textPUT(resource.path(RestServicePathForPropertyUpdate), payload);
		if (clientResponse.getStatus() != 200) {
			throw new RuntimeException("Could not update " + propertyName + " property");
		}
	}
}
