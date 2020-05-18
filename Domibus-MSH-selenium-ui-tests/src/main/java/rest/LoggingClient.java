package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class LoggingClient extends DomibusRestClient {


	public JSONObject searchLogLevels(HashMap<String, String> params) throws Exception {
		ClientResponse response = requestGET(resource.path(RestServicePaths.LOGGING), params);

		int status = response.getStatus();
		String content = sanitizeResponse(response.getEntity(String.class));

		log.debug("status: " + status);
		log.debug("content: " + content);

		if(status != 200){
			throw new Exception("Did not receive success status " + status);
		}

		return new JSONObject(content);
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

		int status = response.getStatus();
		String content = sanitizeResponse(response.getEntity(String.class));

		log.debug("status: " + status);
		log.debug("content: " + content);

		if(status != 200){
			throw new Exception("Did not receive success status " + status);
		}

		return new JSONObject(content).getJSONArray("loggingEntries");
	}

	public JSONArray getLogLevels() throws Exception {
		return getLogLevels("eu.domibus", false);
	}

	public String setLogLevel(String level, String name) throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("level", level);
		ClientResponse response = requestPOST(resource.path(RestServicePaths.LOGGING), obj.toString());

		int status = response.getStatus();
		String content = sanitizeResponse(response.getEntity(String.class));

		log.debug("status: " + status);
		log.debug("content: " + content);

		if(status != 200){
			throw new Exception("Did not receive success status " + status);
		}

		return content;
	}

	public String resetLogLevel() throws Exception {
		ClientResponse response = requestPOST(resource.path(RestServicePaths.LOGGING_RESET), new JSONObject().toString());

		int status = response.getStatus();
		String content = sanitizeResponse(response.getEntity(String.class));

		log.debug("status: " + status);
		log.debug("content: " + content);

		if(status != 200){
			throw new Exception("Did not receive success status " + status);
		}

		return content;
	}

}
