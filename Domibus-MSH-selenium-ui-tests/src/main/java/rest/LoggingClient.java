package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class LoggingClient extends BaseRestClient {
	
	public LoggingClient(String username, String password) {
		super(username, password);
	}
	
	public JSONObject searchLogLevels(HashMap<String, String> params, String domain) throws Exception {
		switchDomain(domain);
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.LOGGING), params);
		
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Did not receive success status " , response);
		}
		
		return new JSONObject(response.getEntity(String.class));
	}
	
	public JSONArray getLogLevels(String name, boolean showClasses) throws Exception {
		
		HashMap<String, String> params = new HashMap<>();
		params.put("loggerName", name);
		params.put("showClasses", "" + showClasses);
		params.put("page", "0");
		params.put("pageSize", "10000");
		params.put("orderBy", "name");
		params.put("asc", "false");
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.LOGGING), params);
		
		
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Did not receive success status ", response);
		}
		
		return new JSONObject(response.getEntity(String.class)).getJSONArray("loggingEntries");
	}
	
	public JSONArray getLogLevels() throws Exception {
		return getLogLevels("eu.domibus", false);
	}
	
	public String setLogLevel(String level, String name) throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("level", level);
		ClientResponse response = requestPOST(resource.path(RestServicePaths.LOGGING), obj.toString());
		
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Did not receive success status ", response);
		}
		
		return response.getEntity(String.class);
	}
	
	public String resetLogLevel() throws Exception {
		ClientResponse response = requestPOST(resource.path(RestServicePaths.LOGGING_RESET), new JSONObject().toString());

		if (response.getStatus() != 200) {
			throw new DomibusRestException("Did not receive success status ", response);
		}
		
		return response.getEntity(String.class);
	}
	
	public ClientResponse searchLevels(HashMap<String, String> params, String domain) throws Exception {
		switchDomain(domain);
		return requestGET(resource.path(RestServicePaths.LOGGING), params);
	}
}
