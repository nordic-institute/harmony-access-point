package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

public class MessageClient extends BaseRestClient {
	
	public MessageClient(String username, String password) {
		super(username, password);
	}
	
	// -------------------------------------------- Low level methods ------------------------------------------------------------
	
	public ClientResponse callGetListOfMessages(String domain) throws Exception {
		switchDomain(domain);
		HashMap<String, String> par = new HashMap<>();
		par.put("pageSize", "10000");
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.MESSAGE_LOG_MESSAGES), par);
		return clientResponse;
	}
	
	public ClientResponse callDownloadMessage(String id, String domain) throws Exception {
		switchDomain(domain);
		
		HashMap<String, String> params = new HashMap<>();
		params.put("messageId", id);
		
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.MESSAGE_LOG_MESSAGE), params);
		return clientResponse;
	}
	
	public ClientResponse callResendMessage(String id, String domain) throws Exception {
		switchDomain(domain);
		
		ClientResponse response = jsonPUT(resource.path(
				RestServicePaths.MESSAGE_LOG_RESEND).queryParam("messageId", id),
				"{}");
		return response;
	}
	
	// -------------------------------------------- High level methods ------------------------------------------------------------
	
	public String downloadMessage(String id, String domain) throws Exception {
		switchDomain(domain);
		
		HashMap<String, String> params = new HashMap<>();
		params.put("messageId", id);
		
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.MESSAGE_LOG_MESSAGE), params);
		InputStream in = clientResponse.getEntity(InputStream.class);
		
		File file = File.createTempFile("message", ".zip");
		Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		in.close();
		
		return file.getAbsolutePath();
	}
	
	public boolean resendMessage(String id, String domain) throws Exception {
		switchDomain(domain);
		
		ClientResponse response = jsonPUT(resource.path(
				RestServicePaths.MESSAGE_LOG_RESEND).queryParam("messageId", id),
				"{}");
		
		if (response.getStatus() == 200) {
			return true;
		}
		
		log.error("Resending of message failed with status: " + response.getStatus());
		return false;
	}
	
	public ClientResponse resendMessage(String id) throws Exception {
		ClientResponse response = jsonPUT(resource.path(
				RestServicePaths.MESSAGE_LOG_RESEND).queryParam("messageId", id),
				"{}");
		return response;
	}
	
	public JSONArray getListOfMessages(String domain) throws Exception {
		ClientResponse clientResponse = callGetListOfMessages(domain);
		if (clientResponse.getStatus() != 200) {
			throw new DomibusRestException("Could not get messages", clientResponse);
		}
		return new JSONObject(sanitizeResponse(clientResponse.getEntity(String.class))).getJSONArray("messageLogEntries");
	}
	
	public JSONObject searchMessage(String id, String domain) throws Exception {
		switchDomain(domain);
		HashMap<String, String> par = new HashMap<>();
		par.put("pageSize", "100");
		par.put("messageId", id);
		
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.MESSAGE_LOG_MESSAGES), par);
		if (clientResponse.getStatus() != 200) {
			return null;
		}
		
		return new JSONObject(sanitizeResponse(clientResponse.getEntity(String.class))).getJSONArray("messageLogEntries").getJSONObject(0);
	}
	
	public ClientResponse getMessages(HashMap<String, String> params, String domain) throws Exception {
		switchDomain(domain);
		return requestGET(
				resource.path(RestServicePaths.MESSAGE_LOG_MESSAGES)
				, params);
	}
}
