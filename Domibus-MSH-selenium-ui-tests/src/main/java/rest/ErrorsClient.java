package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class ErrorsClient extends BaseRestClient {
	
	public ErrorsClient(String username, String password) {
		super(username, password);
	}
	
	public JSONArray getErrors(String domain) throws Exception {
		switchDomain(domain);
		
		HashMap<String, String> params = new HashMap<>();
		params.put("asc", "true");
		params.put("orderBy", "timestamp");
		params.put("page", "0");
		params.put("pageSize", "100");
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.ERRORS), params);
		if (response.getStatus() != 200) {
			throw new Exception("Error getting errors " + response.getStatus());
		}
		
		return new JSONObject(sanitizeResponse(response.getEntity(String.class))).getJSONArray("errorLogEntries");
	}
	
	public ClientResponse getErrors(String domain, HashMap<String, String> params) throws Exception {
		switchDomain(domain);
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.ERRORS), params);
		
		return response;
	}
	
	
}
