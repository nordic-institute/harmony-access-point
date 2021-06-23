package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import domibus.ui.RestTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Lists;
import utils.TestUtils;

import java.io.IOException;
import java.util.*;

public class MessagesRestTest extends RestTest {
	
	List<String> filterNames = Lists.newArrayList(new String[]{"notificationStatus", "fromPartyId", "originalSender", "conversationId", "messageId", "refToMessageId", "finalRecipient", "messageStatus", "messageType", "messageSubtype", "toPartyId", "mshRole"});
	List<String> dateFilterNames = Lists.newArrayList(new String[]{"receivedTo", "receivedFrom"});
	
	@DataProvider
	private Object[][] basicFilterCombinations() throws IOException {
		return readCSV("src/test/resources/rest_csv/messagesBasicValidSearches.csv");
	}
	
	@DataProvider
	private Object[][] advancedFilterCombinations() throws IOException {
		return readCSV("src/test/resources/rest_csv/messagesAdvancedValidSearches.csv");
	}
	
	
	@Test(description = "MSG-5", groups = {"multiTenancy", "singleTenancy"}, dataProvider = "basicFilterCombinations")
	public void filterUsingBasicFilters(HashMap<String, String> params) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		if (params.containsKey("messageId")
				&& StringUtils.isNotEmpty(params.get("messageId"))
				&& StringUtils.equalsIgnoreCase(params.get("messageId"), "<FILL>")) {
			
			JSONArray messages = rest.messages().getListOfMessages(null);
			params.put("messageId", messages.getJSONObject(0).getString("messageId"));
		}
		
		eliminateEmptyValues(params);
		
		ClientResponse response = rest.messages().getMessages(params, ""); //requestGET(rest.resource.path(RestServicePaths.MESSAGE_LOG_MESSAGES), params);
		
		assertPositiveResponse(response, soft, params);
		soft.assertAll();
	}
	
	@Test(description = "MSG-5", groups = {"multiTenancy", "singleTenancy"}, dataProvider = "readInvalidStrings")
	public void filterUsingBasicFiltersNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String[] keys = {"fromPartyId", "toPartyId", "messageId", "messageStatus", "messageType", "isTestMessage", "page", "pageSize", "orderBy", "asc"};
		HashMap<String, String> params = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			params.put(key, evilStr);
		}
		
		ClientResponse response = rest.messages().getMessages(params, "");
		validateInvalidResponse(response, soft);
		
		soft.assertAll();
	}
	
	@Test(description = "MSG-7", groups = {"multiTenancy", "singleTenancy"}, dataProvider = "readInvalidStrings")
	public void filterUsingAdvancedFiltersNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String[] keys = {"fromPartyId", "toPartyId", "originalSender", "finalRecipient", "messageSubtype", "receivedFrom", "receivedTo", "notificationStatus", "messageStatus", "messageType", "mshRole", "isTestMessage", "page", "pageSize", "orderBy", "asc"};
		HashMap<String, String> params = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			params.put(key, evilStr);
		}
		
		ClientResponse response = rest.messages().getMessages(params, "");
		validateInvalidResponse(response, soft);
		soft.assertAll();
	}
	
	
	@Test(description = "MSG-7", groups = {"multiTenancy", "singleTenancy"}, dataProvider = "advancedFilterCombinations")
	public void filterUsingAdvancedFilters(HashMap<String, String> params) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		eliminateEmptyValues(params);
		
		log.debug("------------" + params.toString());
		
		ClientResponse response = rest.messages().getMessages(params, "");
		
		assertPositiveResponse(response, soft, params);
		
		soft.assertAll();
	}
	
	@Test(description = "MSG-11", groups = {"multiTenancy", "singleTenancy"})
	public void downloadMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		for (String domain : domains) {
			
			JSONObject messToDownload = null;
			
			JSONArray messages = rest.messages().getListOfMessages(domain);
			for (int i = 0; i < messages.length(); i++) {
				JSONObject message = messages.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(message.getString("messageStatus"), "SEND_FAILURE")) {
					messToDownload = message;
				}
			}
			
			if (null == messToDownload) {
				log.info("no message found");
				continue;
			}
			
			String zipPath = rest.messages().downloadMessage(messToDownload.getString("messageId"), domain);
			
			HashMap<String, String> zipContent = TestUtils.unzip(zipPath);
			log.info("checking zip for files message and message.xml");
			boolean foundXMLfile = false;
			boolean foundMessfile = false;
			for (String fileName : zipContent.keySet()) {
				if (StringUtils.equalsIgnoreCase(fileName, "message")) {
					foundMessfile = true;
				}
				if (StringUtils.equalsIgnoreCase(fileName, "message.xml")) {
					foundXMLfile = true;
				}
			}
			
			soft.assertTrue(foundMessfile, "Found file containing message content");
			soft.assertTrue(foundXMLfile, "Found file containing message properties");
			log.info("checking the message payload");
			
			String xmlString = zipContent.get("message.xml");
			
			log.info("checking the message metadata");
			
			soft.assertEquals(messToDownload.getString("messageId"),
					TestUtils.getValueFromXMLString(xmlString, "MessageId"), "MessageId - value matches");
			soft.assertEquals(messToDownload.getString("conversationId"),
					TestUtils.getValueFromXMLString(xmlString, "ConversationId"), "ConversationId - value matches");
			soft.assertEquals(messToDownload.getString("refToMessageId"),
					TestUtils.getValueFromXMLString(xmlString, "RefToMessageId"), "RefToMessageId - value matches");
			
			soft.assertTrue(xmlString.contains("name=\"originalSender\">" + messToDownload.getString("originalSender"))
					, "Original Sender - value matches");
			
			soft.assertTrue(xmlString.contains("name=\"finalRecipient\">" + messToDownload.getString("finalRecipient"))
					, "Final Recipient - value matches");
			
			
		}
		
		
		soft.assertAll();
	}
	
	/* Resend message */
	@Test(description = "MSG-12", groups = {"multiTenancy", "singleTenancy"})
	public void resendMessage() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		
		for (String domain : domains) {
			
			JSONObject messToResend = null;
			
			JSONArray messages = rest.messages().getListOfMessages(domain);
			for (int i = 0; i < messages.length(); i++) {
				JSONObject message = messages.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(message.getString("messageStatus"), "SEND_FAILURE")) {
					messToResend = message;
				}
			}
			
			if (null == messToResend) {
				log.info("no message found");
				continue;
			}
			
			rest.messages().resendMessage(messToResend.getString("messageId"), domain);
			
			
			JSONObject object = rest.messages().searchMessage(messToResend.getString("messageId"), domain);
			soft.assertTrue(StringUtils.equalsAnyIgnoreCase(object.getString("messageStatus"), "SEND_ENQUEUED", "SEND_IN_PROGRESS", "WAITING_FOR_RECEIPT", "ACKNOWLEDGED", "ACKNOWLEDGED_WITH_WARNING", "WAITING_FOR_RETRY"),
					"Status has changed!"
			);
			
			
		}
		soft.assertAll();
	}
	
	/* Resend message */
	@Test(description = "MSG-12", groups = {"multiTenancy", "singleTenancy"}, dataProvider = "readInvalidStrings")
	public void resendMessageNegativeTest(String evilId) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		ClientResponse response = rest.messages().resendMessage(evilId);
		validateInvalidResponse(response, soft);
		
		soft.assertAll();
	}
	
	@Test(description = "MSG-11", groups = {"multiTenancy", "singleTenancy"}, dataProvider = "readInvalidStrings")
	public void downloadMessageNegativeTest(String evilId) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		for (String domain : domains) {
			rest.switchDomain(domain);
			
			HashMap<String, String> params = new HashMap<>();
			params.put("messageId", evilId);
			
			ClientResponse response = rest.messages().getMessages(params, ""); //requestGET(rest.resource.path(RestServicePaths.MESSAGE_LOG_MESSAGE), params);
			int status = response.getStatus();
			log.debug("Response status is " + status);
			
			validateInvalidResponse(response, soft);
		}
		
		soft.assertAll();
	}
	
	private void assertPositiveResponse(ClientResponse response, SoftAssert soft, HashMap<String, String> params) throws Exception {
		
		soft.assertEquals(response.getStatus(), 200, "Success");
		String responseContent = getSanitizedStringResponse(response);
		if (response.getStatus() == 200) {
			JSONObject responseObj = new JSONObject(responseContent);
			
			assertResponseForm(responseObj, params, soft);
			assertAppliedFilter(responseObj.getJSONArray("messageLogEntries"), params, soft);
			assertAppliedDateFilter(responseObj.getJSONArray("messageLogEntries"), params, soft);
		} else {
			log.debug("Params: " + params.toString());
			log.debug("Response code: " + response.getStatus());
			log.debug("Response content: " + responseContent);
		}
		
	}
	
	private void assertResponseForm(JSONObject object, HashMap<String, String> params, SoftAssert soft) {
		JSONArray entries = object.getJSONArray("messageLogEntries");
		
		int pageSize = 10;
		if (params.containsKey("pageSize")) {
			pageSize = Integer.valueOf(params.get("pageSize"));
		}
		int count = Math.min(object.getInt("count"), pageSize);
		
		soft.assertEquals(count, entries.length(), "Count value is correct");
		
		
		for (String key : params.keySet()) {
			if (filterNames.contains(key)) {
				soft.assertTrue(object.getJSONObject("filter").has(key), "Filter name is present in filter node of response");
				soft.assertFalse(object.getJSONObject("filter").isNull(key), "Value resent under key");
				soft.assertTrue(StringUtils.equalsIgnoreCase(params.get(key), object.getJSONObject("filter").getString(key)), "correct value is present ");
			}
			
		}
	}
	
	private void assertAppliedFilter(JSONArray entries, HashMap<String, String> params, SoftAssert soft) {
		
		for (String key : params.keySet()) {
			if (filterNames.contains(key)) {
				for (int i = 0; i < entries.length(); i++) {
					String entryStr = entries.getJSONObject(i).toString();
					soft.assertTrue(StringUtils.containsIgnoreCase(entryStr, params.get(key)), "Param applied correctly");
				}
			}
		}
	}
	
	private void assertAppliedDateFilter(JSONArray entries, HashMap<String, String> params, SoftAssert soft) throws Exception {
		String from = "";
		String to = "";
		long fromMillis = 0;
		long toMillis = 0;
		
		if (params.containsKey("receivedTo")) {
			to = params.get("receivedTo");
		}
		if (params.containsKey("receivedFrom")) {
			from = params.get("receivedFrom");
		}
		if (StringUtils.isNotEmpty(from)) {
			fromMillis = data.REST_DATE_FORMAT.parse(from).getTime();
		}
		if (StringUtils.isNotEmpty(to)) {
			toMillis = data.REST_DATE_FORMAT.parse(to).getTime();
		} else {
			toMillis = Calendar.getInstance().getTimeInMillis();
		}
		
		for (int i = 0; i < entries.length(); i++) {
			long received = entries.getJSONObject(i).getLong("received");
			soft.assertTrue(received >= fromMillis, "received is after from");
			soft.assertTrue(received <= toMillis, "received is before to");
		}
		
	}
	
	private void eliminateEmptyValues(HashMap<String, String> params) {
		List<String> torem = new ArrayList<>();
		
		Iterator<String> it = params.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = params.get(key).trim();
			if (StringUtils.isEmpty(value)) {
				torem.add(key);
			}
		}
		for (int i = 0; i < torem.size(); i++) {
			params.remove(torem.get(i));
		}
	}
	
}