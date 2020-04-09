package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class PModeClient extends DomibusRestClient {
	// -------------------------------------------- PMode --------------------------------------------------------------
	public void uploadPMode(String pmodeFilePath, String domain) throws Exception {
		switchDomain(domain);

		HashMap<String, String> fields = new HashMap<>();
		fields.put("description", "automatic red");
		ClientResponse response = requestPOSTFile(resource.path(RestServicePaths.PMODE), pmodeFilePath, fields);
		if (response.getStatus() != 200) {
			log.debug(String.valueOf(response.getStatus()));
			log.debug(response.getEntity(String.class));
			throw new Exception("Could not upload PMODE file!!!");
		}
	}

	public boolean isPmodeUploaded(String domain) {
		JSONArray entries = getPmodesList(domain);
		return entries.length() > 0;
	}

	JSONArray getPmodesList(String domain) {
		switchDomain(domain);
		String getResponse = requestGET(resource.path(RestServicePaths.PMODE_LIST), null).getEntity(String.class);

		JSONArray entries = new JSONArray();
		try {
			entries = new JSONArray(sanitizeResponse(getResponse));
		} catch (JSONException e) {
		}

		return entries;
	}

	public Integer getLatestPModeID(String domain) {
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
}
