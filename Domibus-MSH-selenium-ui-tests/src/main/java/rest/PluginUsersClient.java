package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PluginUsersClient extends BaseRestClient {
	
	public PluginUsersClient(String username, String password) {
		super(username, password);
	}
	
	public void createPluginUser(String username, String role, String pass, String domain) throws Exception {
		String payload = provider.createPluginUserObj(username, role, pass);
		
		switchDomain(domain);
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.PLUGIN_USERS), payload);
		if (response.getStatus() != 204) {
			throw new Exception("Could not create plugin user");
		}
	}
	
	public void createCertPluginUser(String username, String role, String domain) throws Exception {
		String payload = provider.createCertPluginUserObj(username, role);
		
		switchDomain(domain);
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.PLUGIN_USERS), payload);
		if (response.getStatus() != 204) {
			throw new Exception("Could not create plugin user");
		}
	}
	
	public void deletePluginUser(String username, String domain) throws Exception {
		
		switchDomain(domain);
		
		String getResponse = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), null).getEntity(String.class);
		
		JSONArray pusers = new JSONObject(sanitizeResponse(getResponse)).getJSONArray("entries");
		JSONArray toDelete = new JSONArray();
		
		
		for (int i = 0; i < pusers.length(); i++) {
			JSONObject puser = pusers.getJSONObject(i);
			if (!puser.has("userName") || puser.isNull("userName")) {
				continue;
			}
			if (StringUtils.equalsIgnoreCase(puser.getString("userName"), username)) {
				puser.put("status", "REMOVED");
				toDelete.put(puser);
			}
		}
		
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.PLUGIN_USERS), toDelete.toString());
		if (response.getStatus() != 204) {
			throw new Exception("Could not delete plugin user");
		}
	}
	
	// ----------------------------------------- Plugin Users ----------------------------------------------------------
	public JSONArray getPluginUsers(String domain, String authType) throws Exception {
		
		switchDomain(domain);
		
		HashMap<String, String> params = new HashMap<>();
		params.put("authType", authType);
		params.put("page", "0");
		params.put("pageSize", "10000");
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), params);
		if (response.getStatus() != 200) {
			throw new Exception("Could not get users ");
		}
		
		try {
			String rawResp = response.getEntity(String.class);
			return new JSONObject(sanitizeResponse(rawResp)).getJSONArray("entries");
		} catch (JSONException e) {
			log.error("EXCEPTION: ", e);
		}
		return null;
	}
	
	public ClientResponse searchPluginUsers(String domain, String authType, String role, String username, String originalUser, String page, String pageSize) throws Exception {
		
		switchDomain(domain);
		
		HashMap<String, String> params = new HashMap<>();
		HashMap<String, String> paramsNotEmpty = new HashMap<>();
		params.put("authType", authType);
		params.put("authRole", role);
		params.put("originalUser", originalUser);
		params.put("username", username);
		params.put("authType", authType);
		params.put("page", page);
		params.put("pageSize", pageSize);
		
		params.keySet().removeIf(k -> StringUtils.isEmpty(params.get(k)));
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), params);
		return response;
	}
	
	public ClientResponse updatePluginUserList(JSONArray array, String domain) throws Exception {
		switchDomain(domain);
		return jsonPUT(resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
	}
}
