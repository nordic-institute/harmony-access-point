package rest;

import com.sun.jersey.api.client.ClientResponse;
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


}
