package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import domibus.ui.RestTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class PModePartiesTest extends RestTest {
	
	@Test(description = "PMP-1")
	public void listParties() throws Exception {
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		for (String domain : domains) {
			rest.pmode().uploadPMode(uploadPath, domain);
			JSONArray parties = rest.pmodeParties().getParties();
			soft.assertTrue(parties.length() == 2, "Both parties are listed");
		}
		
		soft.assertAll();
	}
	
	@Test(description = "PMP-6")
	public void createParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		String uploadPath = "rest_pmodes/pmode-blue.xml";

		for (String domain : domains) {
			rest.pmode().uploadPMode(uploadPath, domain);
			JSONArray parties = rest.pmodeParties().getParties();
			soft.assertTrue(parties.length() == 2, "Both parties are listed");
			
			ClientResponse response = rest.pmodeParties().createParty(null, null, null, null, null, null, null, null);
			
			int status = response.getStatus();
			log.debug("status= " + status);
			
			soft.assertTrue(status == 200, "Response status is success");
			
			try {
				new JSONObject(getSanitizedStringResponse(response));
			} catch (JSONException e) {
				soft.fail("response content is not in JSON format");
			}
			
			soft.assertTrue(rest.pmodeParties().getParties().length() == 3, "3 parties listed");
			
		}
		
		soft.assertAll();
	}
	
	@Test(description = "PMP-6", dataProvider = "readInvalidStrings")
	public void createPartiesNegativeTests(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		
		for (String domain : domains) {
			rest.pmode().uploadPMode(uploadPath, domain);
			JSONArray parties = rest.pmodeParties().getParties();
			soft.assertTrue(parties.length() == 2, "Both parties are listed");
			
			String[] evilProcs = {evilStr};
			
			ClientResponse response = rest.pmodeParties().createParty(null, evilStr, evilStr, evilProcs, evilProcs, evilStr, evilStr, evilStr);
			validateInvalidResponse(response, soft);
			
			soft.assertTrue(rest.pmodeParties().getParties().length() == 2, "2 parties listed");
			
		}
		
		soft.assertAll();
	}
	
}
