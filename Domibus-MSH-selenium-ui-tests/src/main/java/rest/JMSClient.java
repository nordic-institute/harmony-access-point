package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class JMSClient extends DomibusRestClient {

	public JSONArray getQueues() throws JSONException {
		JSONArray queues = new JSONArray();
		ClientResponse response = requestGET(resource.path(RestServicePaths.JMS_QUEUES), null);

		log.debug("Got status: " + response.getStatus());
		String content = response.getEntity(String.class);
		log.debug("Got content: " + content);

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
			throw new RuntimeException("Could not get queues");
		}
		return queues;
	}

	public ClientResponse searchMessages(String source, String jmsType, String fromDate, String toDate, String selector) throws JSONException {

		HashMap<String, String> params = new HashMap<>();
		if(StringUtils.isNotEmpty(source)){params.put("source", source);}
		if(StringUtils.isNotEmpty(jmsType)){params.put("jmsType", jmsType);}
		if(StringUtils.isNotEmpty(fromDate)){params.put("fromDate", fromDate);}
		if(StringUtils.isNotEmpty(toDate)){params.put("toDate", toDate);}
		if(StringUtils.isNotEmpty(selector)){params.put("selector", selector);}

		ClientResponse response = requestGET(resource.path(RestServicePaths.JMS_SEARCH), params);

		return response;
	}

	public JSONArray getQueueMessages(String source) throws JSONException {

		HashMap<String, String> params = new HashMap<>();
		if(StringUtils.isNotEmpty(source)){params.put("source", source);}
		ClientResponse response = requestGET(resource.path(RestServicePaths.JMS_SEARCH), params);

		int status = response.getStatus();
		String content = response.getEntity(String.class);

		if(status == 200){
			return new JSONObject(sanitizeResponse(content)).getJSONArray("messages");
		}else {
			log.debug("Got response: " + status);
			log.debug("Got response content: " + content);
		}
		return null;
	}


}
