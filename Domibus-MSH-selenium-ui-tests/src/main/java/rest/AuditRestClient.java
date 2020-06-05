package rest;

import com.sun.jersey.api.client.ClientResponse;

import java.util.HashMap;

public class AuditRestClient extends BaseRestClient {
	
	public AuditRestClient(String username, String password) {
		super(username, password);
	}
	
	public ClientResponse getAuditLog(HashMap<String, String> params, String domain) throws Exception {
		switchDomain(domain);
		return requestGET(resource.path(RestServicePaths.AUDIT_LIST), params);
	}
}
