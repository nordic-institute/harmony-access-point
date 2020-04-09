package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;

import java.util.HashMap;

public class ConnectionMonitoringClient extends DomibusRestClient{

	public JSONArray getConnectionMonitoringParties() throws Exception{
		HashMap<String, String> params = new HashMap<>();
		params.put("pageSize", "0");
		ClientResponse getPartiesResp = requestGET(resource.path(RestServicePaths.CON_MON_PARTIES), params);

		if(getPartiesResp.getStatus() != 200){
			throw new Exception("get connection monitoring parties failed with status " + getPartiesResp.getStatus() );
		}

		JSONArray parties = new JSONArray(sanitizeResponse(getPartiesResp.getEntity(String.class)));
		return parties;
	}

}
