package rest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONArray;
import org.json.JSONException;
import utils.Gen;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;

public class PModeClient extends BaseRestClient {
	
	public PModeClient(String username, String password) {
		super(username, password);
	}
	
	// -------------------------------------------- PMode --------------------------------------------------------------
	public void uploadPMode(String pmodeFilePath, String domain) throws Exception {
		switchDomain(domain);
		
		HashMap<String, String> fields = new HashMap<>();
		fields.put("description", Gen.randomAlphaNumeric(10));
		ClientResponse response = requestPOSTFile(resource.path(RestServicePaths.PMODE), pmodeFilePath, fields);
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Could not upload PMODE file!!!", response);
		}
	}
	
	public ClientResponse uploadPMode(String pmodeFilePath, String comment, String domain) throws Exception {
		switchDomain(domain);
		
		HashMap<String, String> fields = new HashMap<>();
		fields.put("description", comment);
		ClientResponse response = requestPOSTFile(resource.path(RestServicePaths.PMODE), pmodeFilePath, fields);
		return response;
	}
	
	public boolean isPmodeUploaded(String domain) throws Exception {
		JSONArray entries = getPmodesList(domain);
		return entries.length() > 0;
	}
	
	public JSONArray getPmodesList(String domain) throws Exception {
		switchDomain(domain);
		String getResponse = requestGET(resource.path(RestServicePaths.PMODE_LIST), null).getEntity(String.class);
		
		JSONArray entries = new JSONArray();
		try {
			entries = new JSONArray(sanitizeResponse(getResponse));
		} catch (JSONException e) {
		}
		
		return entries;
	}
	
	public Integer getLatestPModeID(String domain) throws Exception {
		switchDomain(domain);
		
		JSONArray entries = getPmodesList(domain);
		int pmodeID = 0;
		for (int i = 0; i < entries.length(); i++) {
			pmodeID = Math.max(pmodeID, entries.getJSONObject(i).getInt("id"));
		}
		return pmodeID;
	}
	
	public String downloadPmode(String domain, Integer pmodeID) throws Exception {
		switchDomain(domain);
		
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.PMODE_CURRENT_DOWNLOAD + pmodeID), null);
		
		InputStream in = clientResponse.getEntity(InputStream.class);
		File file = File.createTempFile("pmode", ".xml");
		Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		in.close();
		return file.getAbsolutePath();
	}
	
	public ClientResponse downloadPmode(String domain, String pmodeID) throws Exception {
		switchDomain(domain);
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.PMODE_CURRENT_DOWNLOAD + pmodeID), null);
		return clientResponse;
	}
	
	public ClientResponse deletePmode(String domain, List<String> pmodeIDs) throws DomibusRestException {
		switchDomain(domain);
		
		WebResource myRes = resource.path(RestServicePaths.PMODE);
		
		for (int i = 0; i < pmodeIDs.size(); i++) {
			String pmodeID = pmodeIDs.get(i);
			myRes = myRes.queryParam("ids", pmodeID);
		}
		ClientResponse clientResponse = decorateBuilder(myRes).delete(ClientResponse.class);
		return clientResponse;
	}
	
	public ClientResponse restorePmode(String domain, String pmodeId) throws Exception {
		switchDomain(domain);
		
		ClientResponse clientResponse = jsonPUT(resource.path(RestServicePaths.PMODE_RESTORE + pmodeId), "");
		return clientResponse;
	}
	
	
}
