package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import rest.utilPojo.Param;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlertsRestTest extends RestTest{


	List<String> excludedKeys = Arrays.asList(new String[]{"creationFrom", "creationTo", "reportingFrom", "reportingTo", "alertStatus", "alertId", "orderBy", "asc", "page", "pageSize", "dynamicFrom", "dynamicTo"});
	List<String> allKeys = Arrays.asList(new String[]{"processed", "domainAlerts", "alertType", "alertLevel", "creationFrom", "creationTo", "reportingFrom", "reportingTo", "alertStatus", "alertId", "orderBy", "asc", "page", "pageSize", "dynamicFrom", "dynamicTo", "parameters", "parameters", "parameters", "parameters"});


	@DataProvider
	public Object[][] multiValueProvider()throws Exception{
		return readCSVMultiValued("src/test/resources/rest_csv/alertsValidSearches.csv");
	}

	@Test(dataProvider = "multiValueProvider")
	public void searchTest(ArrayList<Param> params) throws Exception {
		SoftAssert soft = new SoftAssert();

		boolean showDomain = data.isMultiDomain();
		int validID = rest.alerts().getAlerts(null, false, showDomain).getJSONObject(0).getInt("entityId");


		for (Param param : params) {
			if(param.getValue().equalsIgnoreCase("<FILL>")) {
				param.setValue(String.valueOf(validID));
			}
		}

		ClientResponse response = rest.alerts().filterAlerts(params, null); //multivalueGET(rest.resource.path(RestServicePaths.ALERTS_LIST), params);

		int status = response.getStatus();
		String content = response.getEntity(String.class);
		log.debug("status " + status);
		log.debug("content " + content);

		soft.assertTrue(status == 200, "Success status received");

		try {
			JSONObject obj = new JSONObject(rest.sanitizeResponse(content));
			JSONArray entries = obj.getJSONArray("alertsEntries");
			soft.assertTrue(obj.getInt("count") == entries.length(), "Count is correct");
			for (int i = 0; i < entries.length(); i++) {
				validateAlertVsParams(entries.getJSONObject(i), params, soft);
			}
		} catch (JSONException e) {
			soft.fail("not in JSON format");
		}

		soft.assertAll();
	}

	@Test
	public void markAlertAsProcessedTest() throws Exception {
		SoftAssert soft = new SoftAssert();

		boolean showDomain = data.isMultiDomain();
		JSONArray alerts = rest.alerts().getAlerts(null, false, showDomain);

		JSONObject alert = alerts.getJSONObject(0);
		alerts.getJSONObject(0).put("processed", true);
		ClientResponse response = rest.alerts().markAlert(alerts, null);

		JSONArray processedAlerts = rest.alerts().getAlerts(null, true, showDomain);

		boolean found = false;
		for (int i = 0; i < processedAlerts.length(); i++) {
			JSONObject tmpAlert = processedAlerts.getJSONObject(i);
			if(tmpAlert.getInt("entityId") == alert.getInt("entityId")){
				found = true;
			}
		}
		soft.assertTrue(found, "alert marked as processed");
		soft.assertAll();
	}

	@Test(dependsOnMethods = "markAlertAsProcessedTest")
	public void markAlertAsNOTProcessedTest() throws Exception {
		SoftAssert soft = new SoftAssert();

		boolean showDomain = data.isMultiDomain();
		JSONArray processedAlerts = rest.alerts().getAlerts(null, true, showDomain);

		JSONObject alert = processedAlerts.getJSONObject(0);
		processedAlerts.getJSONObject(0).put("processed", false);
		ClientResponse response = rest.alerts().markAlert(processedAlerts, null);

		JSONArray rawAlerts = rest.alerts().getAlerts(null, false, showDomain);

		boolean found = false;
		for (int i = 0; i < rawAlerts.length(); i++) {
			JSONObject tmpAlert = rawAlerts.getJSONObject(i);
			if(tmpAlert.getInt("entityId") == alert.getInt("entityId")){
				found = true;
			}
		}
		soft.assertTrue(found, "alert marked as processed");
		soft.assertAll();
	}



	@Test(dataProvider = "readInvalidStrings")
	public void searchTest(String evilStr) throws Exception {
		log.debug("evilStr= " + evilStr);
		SoftAssert soft = new SoftAssert();
		ArrayList<Param> params = new ArrayList<>();
		for (String allKey : allKeys) {
			params.add(new Param(allKey, evilStr));
		}
		ClientResponse response = rest.alerts().filterAlerts(params, null);

		validateInvalidResponse(response, soft);

		soft.assertAll();
	}

	private void validateAlertVsParams(JSONObject alert, ArrayList<Param> params, SoftAssert soft){
		String alertStr = alert.toString();
		for (Param param : params) {
			if(excludedKeys.contains(param.getKey())){continue;}
			soft.assertTrue(alertStr.contains(param.getKey()) && alertStr.contains(param.getValue()), "Not found param key or value: " + param.toString());
		}
	}




}
