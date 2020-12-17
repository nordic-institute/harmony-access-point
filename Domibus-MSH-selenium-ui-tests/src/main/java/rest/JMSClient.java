package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class JMSClient extends BaseRestClient {
	
	public JMSClient(String username, String password) {
		super(username, password);
	}
	
	public JSONArray getQueues() throws Exception {
		JSONArray queues = new JSONArray();
		ClientResponse response = requestGET(resource.path(RestServicePaths.JMS_QUEUES), null);
		
		log.debug("Got status: " + response.getStatus());
		String content = response.getEntity(String.class);
//		log.debug("Got content: " + content);
		
		if (response.getStatus() == 200) {
			JSONObject object = new JSONObject(sanitizeResponse(content)).getJSONObject("jmsDestinations");
			Iterator<String> it = object.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				JSONObject rawq = object.getJSONObject(key);
				JSONObject q = new JSONObject();
				q.put("name", rawq.getString("name"));
				q.put("numberOfMessages", rawq.getInt("numberOfMessages"));
				q.put("internal", rawq.getBoolean("internal"));
				queues.put(q);
			}
			
		} else {
			throw new DomibusRestException("Could not get queues", response);
		}
		return queues;
	}
	
	public String getRandomQNameWithMessages() throws Exception {
		JSONArray queues = getQueues();
		
		for (int i = 0; i < queues.length(); i++) {
			if (queues.getJSONObject(i).getInt("numberOfMessages") > 0) {
				return queues.getJSONObject(i).getString("name");
			}
		}
		return null;
	}
	
	public ClientResponse searchMessages(String source, String jmsType, String fromDate, String toDate, String selector) throws Exception {
		
		HashMap<String, String> params = new HashMap<>();
		if (StringUtils.isNotEmpty(source)) {
			params.put("source", source);
		}
		if (StringUtils.isNotEmpty(jmsType)) {
			params.put("jmsType", jmsType);
		}
		if (StringUtils.isNotEmpty(fromDate)) {
			params.put("fromDate", fromDate);
		}
		if (StringUtils.isNotEmpty(toDate)) {
			params.put("toDate", toDate);
		}
		if (StringUtils.isNotEmpty(selector)) {
			params.put("selector", selector);
		}
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.JMS_SEARCH), params);
		
		return response;
	}
	
	public JSONArray getQueueMessages(String source) throws Exception {
		
		HashMap<String, String> params = new HashMap<>();
		if (StringUtils.isNotEmpty(source)) {
			params.put("source", source);
		}
		ClientResponse response = requestGET(resource.path(RestServicePaths.JMS_SEARCH), params);
		
		if (response.getStatus() == 200) {
			return new JSONObject(sanitizeResponse(response.getEntity(String.class))).getJSONArray("messages");
		} else {
			log.debug("Got response: " + response.getStatus());
			log.debug("Got response content: " + response.getEntity(String.class));
		}
		return null;
	}
	
	
	public ClientResponse moveMessages(String source, String destination, String... messages) throws Exception {
		
		JSONArray array = new JSONArray();
		for (String message : messages) {
			array.put(message);
		}
		
		JSONObject params = new JSONObject();
		params.put("action", "MOVE");
		params.put("source", source);
		params.put("destination", destination);
		params.put("selectedMessages", array);
		
		ClientResponse response = requestPOST(resource.path(RestServicePaths.JMS_ACTION), params.toString());
		
		return response;
	}
	
	public ClientResponse deleteMessages(String source, String... messages) throws Exception {
		
		JSONArray array = new JSONArray();
		for (String message : messages) {
			array.put(message);
		}
		
		JSONObject params = new JSONObject();
		params.put("action", "REMOVE");
		params.put("source", source);
//		params.put("destination", destination);
		params.put("selectedMessages", array);
		
		ClientResponse response = requestPOST(resource.path(RestServicePaths.JMS_ACTION), params.toString());
		
		return response;
	}
	
	
}
