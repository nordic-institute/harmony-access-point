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
		
		int status = response.getStatus();
		String content = response.getEntity(String.class);
		
		log.debug("status = " + status);
		log.debug("content = " + content);
		
		if(status != 200){
			throw new Exception("Could not get ui replication count");
		}
		
		return sanitizeResponse(content);
	}
	
	public String sync(String domain) throws Exception {
		switchDomain(domain);
		
		ClientResponse response = requestGET(resource.path(RestServicePaths.UI_REPLICATION_SYNC), null);
		
		int status = response.getStatus();
		String content = response.getEntity(String.class);
		
		log.debug("status = " + status);
		log.debug("content = " + content);
		
		if(status != 200){
			throw new Exception("Could not get ui replication count");
		}
		
		return sanitizeResponse(content);
	}
	
	public int extractNoOfRecords(String mess){
		try {
			return Integer.valueOf(mess.replaceAll("\\D", ""));
		} catch (NumberFormatException e) {
		}
		
		return 0;
	}
	
	
}

