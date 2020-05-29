package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectionMonitoringClient extends BaseRestClient {

	public ConnectionMonitoringClient(String username, String password) {
		super(username, password);
	}

	private String propName = "domibus.monitoring.connection.party.enabled";

	public JSONArray getConnectionMonitoringParties(String domain) throws Exception {

		switchDomain(domain);

		HashMap<String, String> params = new HashMap<>();
		params.put("pageSize", "0");
		ClientResponse getPartiesResp = requestGET(resource.path(RestServicePaths.CON_MON_PARTIES), params);

		if (getPartiesResp.getStatus() != 200) {
			throw new Exception("get connection monitoring parties failed with status " + getPartiesResp.getStatus());
		}

		JSONArray parties = new JSONArray(sanitizeResponse(getPartiesResp.getEntity(String.class)));
		return parties;
	}

	public JSONObject getMonitoringPartiesDetails(String partyID, String domain) throws Exception {

		switchDomain(domain);

		HashMap<String, String> params = new HashMap<>();
		params.put("partyIds", partyID);
		ClientResponse getPartiesResp = requestGET(resource.path(RestServicePaths.CON_MON_PARTIES_DETAILS), params);

		if (getPartiesResp.getStatus() != 200) {
			throw new Exception("get connection monitoring parties details failed with status " + getPartiesResp.getStatus());
		}
		JSONObject raw = new  JSONObject(sanitizeResponse(getPartiesResp.getEntity(String.class)));
		return raw.getJSONObject(partyID);
	}

	public boolean monitorParty(String partyID, String domain) throws Exception {

		switchDomain(domain);

		String monitoredParties = getMonitoredPartiesStr(domain);

		if(monitoredParties.contains(partyID)){
			return true;
		}

		ClientResponse getPartiesResp = textPUT(resource.path(RestServicePaths.DOMIBUS_PROPERTIES).path(propName), monitoredParties + "," + partyID);

		if (getPartiesResp.getStatus() != 200) {
			throw new Exception("Enable monitoring for party " + partyID + " failed with status " + getPartiesResp.getStatus());
		}
		return getMonitoredPartiesStr(domain).contains(partyID);
	}

	public boolean disableMonitorParty(String partyID, String domain) throws Exception {

		switchDomain(domain);

		String monitoredParties = getMonitoredPartiesStr(domain);

		if(!monitoredParties.contains(partyID)){
			return true;
		}
		else {
			monitoredParties = monitoredParties.replaceAll(","+partyID, "");
		}
		ClientResponse getPartiesResp = textPUT(resource.path(RestServicePaths.DOMIBUS_PROPERTIES).path(propName), monitoredParties);

		if (getPartiesResp.getStatus() != 200) {
			throw new Exception("Enable monitoring for party " + partyID + " failed with status " + getPartiesResp.getStatus());
		}
		return !getMonitoredPartiesStr(domain).contains(partyID);
	}

	private String getMonitoredPartiesStr(String domain) throws Exception {
		switchDomain(domain);

		HashMap<String, String> params = new HashMap<>();
		params.put("name", propName);
		params.put("showDomain", "true");
		ClientResponse response = requestGET(resource.path(RestServicePaths.DOMIBUS_PROPERTIES), params);

		int status = response.getStatus();
		String entity = response.getEntity(String.class);

		log.debug("status = " + status);
		log.debug("content = " + entity);


		if (status != 200) {
			throw new Exception("Could not get monitored parties " + status);
		}

		JSONObject prop = new JSONObject(sanitizeResponse(entity)).getJSONArray("items").getJSONObject(0);
		String value = prop.optString("value");
		return value;
	}

	public List<String> getMonitoredParties(String domain) throws Exception {
		List<String> toret = new ArrayList<>();

		String value = getMonitoredPartiesStr(domain);

		if (StringUtils.isNotEmpty(value)) {
			String[] ids = value.split(",");
			for (int i = 0; i < ids.length; i++) {
				String id = ids[i].trim();
				if (StringUtils.isNotEmpty(id)) {
					toret.add(id);
				}
			}
		}
		return toret;
	}

}
