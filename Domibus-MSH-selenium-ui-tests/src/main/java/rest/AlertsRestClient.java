package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import rest.utilPojo.Param;

import java.util.ArrayList;
import java.util.HashMap;

public class AlertsRestClient extends BaseRestClient{

	public AlertsRestClient(String username, String password) {
		super(username, password);
	}

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

	public ClientResponse filterAlerts(ArrayList<Param> params, String domain) {
		switchDomain(domain);
		return multivalueGET(resource.path(RestServicePaths.ALERTS_LIST), params);
	}

	public ClientResponse markAlert(JSONArray alerts, String domain) {
		switchDomain(domain);
		return jsonPUT(resource.path(RestServicePaths.ALERTS_LIST), alerts.toString());
	}
}
