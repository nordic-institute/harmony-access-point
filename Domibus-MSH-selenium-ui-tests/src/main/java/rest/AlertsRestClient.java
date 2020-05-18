package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class AlertsRestClient extends DomibusRestClient{

	public JSONArray getAlerts(String domain, boolean processed, boolean showDomain) throws Exception {

		switchDomain(domain);

		HashMap<String, String> params = new HashMap<>();
		params.put("processed", "" + processed);
		params.put("domainAlerts", "" + showDomain);
		params.put("orderBy", "creationTime");
		params.put("asc", "false");
		params.put("page", "0");
		params.put("pageSize", "10000");

		ClientResponse response = requestGET(resource.path(RestServicePaths.ALERTS_LIST), params);
		int status = response.getStatus();
		String content = response.getEntity(String.class);
		log.debug("status " + status);
//		log.debug("content " + content);

		if(status != 200){
			throw new Exception("Could't get alerts. Got status " + status);
		}

		JSONObject object = new JSONObject(sanitizeResponse(content));

		return object.getJSONArray("alertsEntries");
	}

}
