package rest;

import com.sun.jersey.api.client.ClientResponse;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class CSVClient extends DomibusRestClient {
	
	public CSVClient(String username, String password) {
		super(username, password);
	}
	
	// -------------------------------------------- Get Grid -----------------------------------------------------------
	public String downloadGrid(String path, HashMap<String, String> params, String domain) throws Exception {
		switchDomain(domain);
		
		ClientResponse clientResponse = requestGET(resource.path(path), params);
		
		if (clientResponse.getStatus() == 200) {
			InputStream in = clientResponse.getEntity(InputStream.class);
			
			File file = File.createTempFile("domibus", ".csv");
			Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
			in.close();
			return file.getAbsolutePath();
		} else {
			log.debug(clientResponse.getEntity(String.class));
			throw new Exception("Could not download file. Request status is " + clientResponse.getStatus());
		}
	}
}
