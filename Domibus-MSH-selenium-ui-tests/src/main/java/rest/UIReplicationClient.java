package rest;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.enums.DRoles;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UIReplicationClient extends BaseRestClient {
	
	public UIReplicationClient(String username, String password){super(username, password);}
	
	// -------------------------------------------- Users --------------------------------------------------------------
	public String getCount(String domain) throws Exception {
		switchDomain(domain);
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.UI_REPLICATION_COUNT), null);
		
		if(response.getStatus() != 200){
			throw new DomibusRestException("Could not get ui replication count", response);
		}
		
		return sanitizeResponse(response.getEntity(String.class));
	}
	
	public String sync(String domain) throws Exception {
		switchDomain(domain);
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.UI_REPLICATION_SYNC), null);
		
		if(response.getStatus() != 200){
			throw new DomibusRestException("Could not get ui replication count", response);
		}
		
		return sanitizeResponse(response.getEntity(String.class));
	}
	
	public int extractNoOfRecords(String mess){
		try {
			return Integer.valueOf(mess.replaceAll("\\D", ""));
		} catch (NumberFormatException e) {
		}
		
		return 0;
	}
	
	
}

