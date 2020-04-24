package domibus.ui.rest;

import org.json.JSONArray;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class PModePartiesTest extends RestTest {

	@Test
	public void listParties() throws Exception{
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		for (String domain : domains) {
			rest.pmode().uploadPMode(uploadPath, domain);
			JSONArray parties = rest.pmodeParties().getParties();
			soft.assertTrue(parties.length() == 2, "Both parties are listed");
		}

		soft.assertAll();
	}

	@Test
	public void createParty() throws Exception{
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		for (String domain : domains) {
			rest.pmode().uploadPMode(uploadPath, domain);
			JSONArray parties = rest.pmodeParties().getParties();
			soft.assertTrue(parties.length() == 2, "Both parties are listed");
		}

		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void createPartiesNegativeTests(String evilStr) throws Exception{

	}

}
