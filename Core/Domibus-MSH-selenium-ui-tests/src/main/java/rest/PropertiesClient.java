package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
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


	
	public JSONArray searchDomainProperties(String name) throws Exception {
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
	
	public ClientResponse searchDomainProperties(HashMap<String, String> params) throws Exception {
		ClientResponse response = requestGET(resource.path(RestServicePaths.DOMIBUS_PROPERTIES), params);
		return response;
	}
	
	public JSONArray getAllProperties() throws Exception {
		return searchDomainProperties("");
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
	




//	-----------------------------------------------------------------------------------------------------

	private JSONArray getAllProperties(String domain, boolean showDomain) throws Exception{

		HashMap<String, String> params = new HashMap<>();
		params.put("page", "0");
		params.put("pageSize", "1000");
		params.put("showDomain", ""+showDomain);

		if(showDomain){
			switchDomain(domain);
		}

		ClientResponse response = requestGET(resource.path(RestServicePaths.DOMIBUS_PROPERTIES), params);

		if(response.getStatus() != 200){
			throw new DomibusRestException("could not get properties", response);
		}

		String respText = sanitizeResponse(response.getEntity(String.class));

		return new JSONObject(respText).getJSONArray("items");
	}

	public JSONArray getAllDomainProperties(String domain) throws Exception{
		log.info("getting domain properties");
		return getAllProperties(domain, true);
	}

	public JSONArray getAllGlobalProperties() throws Exception {
		log.info("getting global properties");
		return getAllProperties(null, false);
	}



	public String getPropertyValue(String propName, boolean isDomain, String domain) throws Exception {

		JSONArray allProps;
		if(isDomain) {
			allProps = getAllDomainProperties(domain);
		}else {
			allProps = getAllGlobalProperties();
		}

		for (int i = 0; i < allProps.length(); i++) {
			JSONObject prop = allProps.getJSONObject(i);

			if(StringUtils.equalsIgnoreCase(propName, prop.getString("name"))){
				return prop.get("value").toString();
			}
		}
		return null;
	}



}
