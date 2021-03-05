package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class PropertiesClient extends BaseRestClient {
	
	public PropertiesClient(String username, String password) {
		super(username, password);
	}
	
	// -------------------------------------------- Domibus Properties -----------------------------------------------------------
	public JSONArray getDomibusPropertyDetail(String propName) throws Exception {
		HashMap<String, String> params = new HashMap<>();
		params.put("name", propName);
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.DOMIBUS_PROPERTIES), params);
		if (clientResponse.getStatus() != 200) {
			throw new DomibusRestException("Could not get properties ", clientResponse);
		}
		return new JSONObject(sanitizeResponse(clientResponse.getEntity(String.class))).getJSONArray("items");
		
	}

	public JSONObject getDomibusPropertyDetail(String propName, String domain) throws Exception {

		switchDomain(domain);

		HashMap<String, String> params = new HashMap<>();
		params.put("name", propName);
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.DOMIBUS_PROPERTIES), params);
		if (clientResponse.getStatus() != 200) {
			throw new DomibusRestException("Could not get properties ", clientResponse);
		}
		return new JSONObject(sanitizeResponse(clientResponse.getEntity(String.class))).getJSONArray("items").getJSONObject(0);

	}
	
	
	public JSONArray searchProperties(String name) throws Exception {
		HashMap<String, String> params = new HashMap<>();
		params.put("showDomain", "true");
		params.put("name", name);
		params.put("page", "0");
		params.put("pageSize", "10000");
		
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.DOMIBUS_PROPERTIES), params);
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Could not get properties ", response);
		}
		return new JSONObject(sanitizeResponse(response.getEntity(String.class))).getJSONArray("items");
	}
	
	public ClientResponse searchProperties(HashMap<String, String> params) throws Exception {
		ClientResponse response = requestGET(resource.path(RestServicePaths.DOMIBUS_PROPERTIES), params);
		return response;
	}
	
	public JSONArray getAllProperties() throws Exception {
		return searchProperties("");
	}
	
	public ClientResponse updateDomibusProperty(String propertyName, String value) throws Exception {
		
		String path = RestServicePaths.DOMIBUS_PROPERTIES + "/" + propertyName;
		ClientResponse response = textPUT(resource.path(path), value);
		return response;
	}

	public ClientResponse updateDomibusProperty(String propertyName, String value, String domain) throws Exception {

		switchDomain(domain);

		String path = RestServicePaths.DOMIBUS_PROPERTIES + "/" + propertyName;
		ClientResponse response = textPUT(resource.path(path), value);
		return response;
	}

	public ClientResponse updateGlobalProperty(String propertyName, String value) throws Exception {

		switchDomain(null);

		String path = RestServicePaths.DOMIBUS_PROPERTIES + "/" + propertyName;



		ClientResponse response = textPUT(resource.path(path).queryParam("isDomain", "false"), value);
		return response;
	}
	
	
}
