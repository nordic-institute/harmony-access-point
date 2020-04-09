package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Lists;
import rest.RestServicePaths;
import utils.TestUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MessagesRestTests extends RestTest {

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


	@Test(groups = {"multiTenancy", "singleTenancy"}, dataProvider = "basicFilterCombinations")
	public void filterUsingBasicFilters(HashMap<String, String> params) throws Exception {
		SoftAssert soft = new SoftAssert();
		ClientResponse response = rest.requestGET(
				rest.resource.path(RestServicePaths.MESSAGE_LOG_MESSAGES)
				, params);

		soft.assertEquals(response.getStatus(), 200, "Success");
		JSONObject responseObj = new JSONObject(getSanitizedStringResponse(response));

		assertResponseForm(responseObj, params, soft);
		assertAppliedFilter(responseObj.getJSONArray("messageLogEntries"), params, soft);
		assertAppliedDateFilter(responseObj.getJSONArray("messageLogEntries"), params, soft);

		soft.assertAll();
	}


	@Test(groups = {"multiTenancy", "singleTenancy"}, dataProvider = "advancedFilterCombinations")
	public void filterUsingAdvancedFilters(HashMap<String, String> params) throws Exception {
		SoftAssert soft = new SoftAssert();
		ClientResponse response = rest.requestGET(
				rest.resource.path(RestServicePaths.MESSAGE_LOG_MESSAGES)
				, params);

		soft.assertEquals(response.getStatus(), 200, "Success");
		JSONObject responseObj = new JSONObject(getSanitizedStringResponse(response));

		assertResponseForm(responseObj, params, soft);
		assertAppliedFilter(responseObj.getJSONArray("messageLogEntries"), params, soft);
		assertAppliedDateFilter(responseObj.getJSONArray("messageLogEntries"), params, soft);

		soft.assertAll();
	}

	@Test(groups = {"multiTenancy", "singleTenancy"})
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
				if(StringUtils.equalsIgnoreCase(fileName, "message")){ foundMessfile= true;}
				if(StringUtils.equalsIgnoreCase(fileName, "message.xml")){ foundXMLfile = true;	}
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

			soft.assertTrue(xmlString.contains("name=\"originalSender\">"+messToDownload.getString("originalSender"))
					, "Original Sender - value matches");

			soft.assertTrue(xmlString.contains("name=\"finalRecipient\">" + messToDownload.getString("finalRecipient"))
					, "Final Recipient - value matches");


		}


		soft.assertAll();
	}

	/* Resend message */
	@Test(description = "MSG-12", groups = {"multiTenancy", "singleTenancy"})
	public void resendMessage() throws Exception{
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

}