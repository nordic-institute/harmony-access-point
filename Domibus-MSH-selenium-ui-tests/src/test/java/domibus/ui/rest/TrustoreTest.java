package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.testng.annotations.Test;
import rest.RestServicePaths;

import java.util.HashMap;

public class TrustoreTest extends RestTest {

	@Test
	public void theFirstTests() throws Exception{
		String filepath = "C:\\Users\\User\\Desktop\\test.jks";
		HashMap<String, String> params = new HashMap<>();
		params.put("password", "test123");

		ClientResponse response = rest.requestPOSTJKSFile(rest.resource.path(RestServicePaths.TRUSTSTORE), filepath, params);
		System.out.println("response.getStatus() = " + response.getStatus());
	
	}

}
