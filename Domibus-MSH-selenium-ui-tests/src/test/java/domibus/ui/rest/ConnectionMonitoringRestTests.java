package domibus.ui.rest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;

public class ConnectionMonitoringRestTests extends RestTest {

	@Test //(dataProvider = "readInvalidStrings")
	public void getPartiesTest() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("rest_pmodes/pmode-blue.xml", null);

		JSONArray pmodeParties = rest.pmodeParties().getParties(null);
		List<String> pmode_ids = extracPartyIDs(pmodeParties);
		log.debug(pmode_ids.toString());

		JSONArray connParties = rest.connMonitor().getConnectionMonitoringParties(null);
		log.debug(connParties.toString());
		soft.assertTrue(connParties.length() == pmode_ids.size(), "Same number of parties listed");
		for (int i = 0; i < connParties.length(); i++) {
			soft.assertTrue(pmode_ids.contains(connParties.getString(i)),
					"Conn party is present in pmode parties " + connParties.getString(i));
		}

		soft.assertAll();
	}
	@Test
	public void enableMonitoringTest() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("rest_pmodes/pmode-blue.xml", null);
		JSONArray connParties = rest.connMonitor().getConnectionMonitoringParties(null);
		log.debug(connParties.toString());

		String partyID = connParties.getString(0);
		soft.assertTrue(rest.connMonitor().monitorParty(partyID, null), "Monitoring party" + partyID);;

		JSONObject partyDetail = rest.connMonitor().getMonitoringPartiesDetails(partyID, null);
		soft.assertTrue(partyDetail.getBoolean("monitored"), "party "+partyID+" is monitored");

		soft.assertAll();
	}

	@Test
	public void disableMonitoringTest() throws Exception {
		SoftAssert soft = new SoftAssert();

		rest.pmode().uploadPMode("rest_pmodes/pmode-blue.xml", null);
		JSONArray connParties = rest.connMonitor().getConnectionMonitoringParties(null);
		log.debug(connParties.toString());

		String partyID = connParties.getString(0);
		soft.assertTrue(rest.connMonitor().disableMonitorParty(partyID, null), "Disable monitoring for party" + partyID);;

		JSONObject partyDetail = rest.connMonitor().getMonitoringPartiesDetails(partyID, null);
		soft.assertFalse(partyDetail.getBoolean("monitored"), "party "+partyID+" is monitored");

		soft.assertAll();
	}


	@Test(dataProvider = "readInvalidStrings")
	public void enableMonitoringNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();

		String partyID = evilStr;

		try {
			rest.connMonitor().monitorParty(partyID, null);
			soft.fail("no error thrown");
		} catch (Exception e) {

		}

		JSONObject partyDetail = rest.connMonitor().getMonitoringPartiesDetails(partyID, null);
		soft.assertTrue(partyDetail.getBoolean("monitored"), "party "+partyID+" is monitored");

		soft.assertAll();
	}

	private List<String> extracPartyIDs(JSONArray parties) {
		List<String> ids = new ArrayList<>();
		String partiesStr = parties.toString();
		for (int i = 0; i < parties.length(); i++) {
			JSONObject party = parties.getJSONObject(i);
			JSONArray identifiers = party.getJSONArray("identifiers");
			for (int j = 0; j < identifiers.length(); j++) {
				ids.add(identifiers.getJSONObject(j).getString("partyId"));
			}
		}
		return ids;
	}

}
