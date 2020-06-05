package rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Scanner;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class ObjectProvider {
	
	private static final String TEMPLATE_FILE_PATH = "src/main/resources/restRequestTemplates.json";
	private static JSONObject templates;
	
	public ObjectProvider() throws Exception {
		String content = new Scanner(new File(TEMPLATE_FILE_PATH)).useDelimiter("\\Z").next();
		templates = new JSONObject(content);
	}
	
	public String createUserObj(String username, String role, String pass, String domain) throws JSONException {
		JSONObject template = templates.getJSONObject("createUserTemplate");
		template.put("roles", role);
		template.put("domain", domain);
		template.put("userName", username);
		template.put("password", pass);
		
		return new JSONArray().put(template).toString();
	}
	
	public String createPluginUserObj(String username, String role, String pass) throws JSONException {
		JSONObject template = templates.getJSONObject("createPluginUser");
		template.put("userName", username);
		template.put("authRoles", role);
		template.put("password", pass);
		
		return new JSONArray().put(template).toString();
	}
	
	public String createCertPluginUserObj(String username, String role) throws JSONException {
		JSONObject template = templates.getJSONObject("createPluginCertUser");
		template.put("certificateId", username);
		template.put("authRoles", role);
		
		return new JSONArray().put(template).toString();
	}
	
	public String createMessageFilterObj(String action) throws JSONException {
		JSONObject template = templates.getJSONObject("createMessageFilter");
		
		template.getJSONArray("routingCriterias").getJSONObject(0).put("expression", action);
		template.getJSONObject("action").put("expression", action);
		
		return template.toString();
	}
}
