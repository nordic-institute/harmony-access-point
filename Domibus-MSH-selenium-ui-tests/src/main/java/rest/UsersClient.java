package rest;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.enums.DRoles;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersClient extends BaseRestClient {
	
	public UsersClient(String username, String password) {
		super(username, password);
	}
	
	// -------------------------------------------- Users --------------------------------------------------------------
	public JSONArray getUsers(String domain) throws Exception {
		
		switchDomain(domain);
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.USERS), null);
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Could not get users ", response);
		}
		
		try {
			String rawResp = response.getEntity(String.class);
			return new JSONArray(sanitizeResponse(rawResp));
		} catch (JSONException e) {
			log.error("EXCEPTION: ", e);
		}
		return null;
	}
	
	public List<String> getUsernameList(String domain) throws Exception {
		List<String> usernameList = new ArrayList<>();
		
		JSONArray users = getUsers(domain);
		for (int i = 0; i < users.length(); i++) {
			JSONObject  user= users.getJSONObject(i);
			usernameList.add(user.getString("userName"));
		}
		return usernameList;
	}
	
	public int getNoOfAdmins(String domain) throws Exception {
		JSONArray users = getUsers(domain);
		int adminNo = 0;
		for (int i = 0; i < users.length(); i++) {
			if (DRoles.ADMIN.equalsIgnoreCase(users.getJSONObject(i).getJSONArray("authorities").getString(0))) {
				adminNo++;
			}
		}
		return adminNo;
	}
	
	public ArrayList<String> getSuperUsernames() throws Exception {
		JSONArray users = getUsers("default");
		ArrayList<String> supers = new ArrayList<>();
		
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (DRoles.SUPER.equalsIgnoreCase(user.getJSONArray("authorities").getString(0))) {
				supers.add(user.getString("userName"));
			}
		}
		return supers;
	}
	
	public void createUser(String username, String role, String pass, String domain) throws Exception {
		switchDomain(domain);
		if (null == domain || domain.isEmpty()) {
			domain = "default";
		}
		
		String payload = provider.createUserObj(username, role, pass, domain);
		
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.USERS), payload);
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Could not create user", response);
		}
	}
	
	public void deleteUser(String username, String domain) throws Exception {
		switchDomain(domain);
		
		String getResponse = requestGET(resource.path(RestServicePaths.USERS), null).getEntity(String.class);
		
		JSONArray pusers = new JSONArray(sanitizeResponse(getResponse));
		JSONArray toDelete = new JSONArray();
		for (int i = 0; i < pusers.length(); i++) {
			if (StringUtils.equalsIgnoreCase(pusers.getJSONObject(i).getString("userName"), username)) {
				JSONObject tmpUser = pusers.getJSONObject(i);
				tmpUser.put("status", "REMOVED");
				tmpUser.put("deleted", true);
				tmpUser.put("$$index", 0);
				toDelete.put(tmpUser);
			}
		}
		
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.USERS), toDelete.toString());
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Could not delete user", response);
		}
	}
	
	public void updateUser(String username, HashMap<String, String> toUpdate, String domain) {
		JSONObject user = null;
		
		try {
			JSONArray array = getUsers(domain);
			for (int i = 0; i < array.length(); i++) {
				JSONObject tmpUser = array.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(tmpUser.getString("userName"), username)) {
					user = tmpUser;
				}
			}
			
			if (null == user) {
				return;
			}
			
			for (Map.Entry<String, String> entry : toUpdate.entrySet()) {
				user.put(entry.getKey(), entry.getValue());
			}
			
			user.put("status", "UPDATED");
			
			ClientResponse response = jsonPUT(resource.path(RestServicePaths.USERS), "[" + user.toString() + "]");
			if (response.getStatus() != 200) {
				throw new DomibusRestException("Could not UPDATE user", response);
			}
		} catch (Exception e) {
			log.error("EXCEPTION: ", e);
		}
	}
	
	public void activate(String username, String domain) {
		HashMap<String, String> toUpdate = new HashMap<>();
		toUpdate.put("active", "true");
		updateUser(username, toUpdate, domain);
	}
	
	public void deactivate(String username, String domain) {
		HashMap<String, String> toUpdate = new HashMap<>();
		toUpdate.put("active", "false");
		updateUser(username, toUpdate, domain);
	}
	
	public ClientResponse putUser(JSONArray toUpdate, String domain) throws Exception {
		switchDomain(domain);
		return jsonPUT(resource.path(RestServicePaths.USERS), toUpdate.toString());
	}
}

