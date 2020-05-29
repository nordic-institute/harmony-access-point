package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.TestUtils;

public class JMSRestTest extends RestTest {


	@Test
	public void searchTest() {
		SoftAssert soft = new SoftAssert();

		String source = "domibus.DLQ";

		JSONArray queues = rest.jms().getQueues();
		for (int i = 0; i < queues.length(); i++) {
			if(queues.getJSONObject(i).getInt("numberOfMessages") > 0){
				source = queues.getJSONObject(i).getString("name");
			}
		}
		JSONArray messages = rest.jms().getQueueMessages(source);
		if(null == messages){
			throw new SkipException("No messages to filter");
		}

		JSONObject message = messages.getJSONObject(0);
		String jmsType = null;
//		if(StringUtils.isNotBlank(message.getString("type"))){
//			jmsType = message.getString("type");
//		}
		Long messTimestamp = message.getLong("timestamp");
		String fromDate = TestUtils.jmsDateStrFromTimestamp(messTimestamp - 10);
		String toDate = TestUtils.jmsDateStrFromTimestamp(messTimestamp + 10);

		String selector = String.format("MESSAGE_ID='%s'", message.getJSONObject("properties").getString("MESSAGE_ID"));

		ClientResponse response = rest.jms().searchMessages(source, jmsType, fromDate, toDate, selector);

		int status = response.getStatus();
		log.debug("Response status: " + status);

		soft.assertTrue(status == 200, "Response status is " + status);

		JSONArray results = new JSONObject(getSanitizedStringResponse(response)).getJSONArray("messages");

		soft.assertTrue(results.length() == 1, "only one message is returned");

		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void searchNegativeTest(String evilStr) {
		SoftAssert soft = new SoftAssert();

		String jmsType = evilStr;
		String source = evilStr;
		String fromDate = evilStr;
		String toDate = evilStr;
		String selector = evilStr;

		ClientResponse response = rest.jms().searchMessages(source, jmsType, fromDate, toDate, selector);

		validateInvalidResponse(response, soft);

		soft.assertAll();
	}

	@Test
	public void moveTest() {
		SoftAssert soft = new SoftAssert();

		String source = null;
		String destination = null;

		JSONArray queues = rest.jms().getQueues();
		for (int i = 0; i < queues.length(); i++) {
			if(queues.getJSONObject(i).getString("name").contains("DLQ")){
				destination = queues.getJSONObject(i).getString("name");
				continue;
			}else if(queues.getJSONObject(i).getInt("numberOfMessages") > 0){
				source = queues.getJSONObject(i).getString("name");
			}
		}

		if(null == source || null == destination){
			throw new SkipException("No messages found to move");
		}

		JSONArray messages = rest.jms().getQueueMessages(source);
		JSONObject message = messages.getJSONObject(0);

		ClientResponse response = rest.jms().moveMessages(source, destination, message.getString("id"));

		int status = response.getStatus();
		log.debug("Response status: " + status);

		soft.assertTrue(status == 200, "Response status is " + status);

		String responseContent = getSanitizedStringResponse(response);

		soft.assertTrue(new JSONObject(responseContent).getString("outcome").equalsIgnoreCase("success"), "success message returned");

		messages = rest.jms().getQueueMessages(destination);
		boolean found = false;
		for (int i = 0; i < messages.length(); i++) {
			JSONObject curMessage = messages.getJSONObject(i);
			if(StringUtils.equalsIgnoreCase(curMessage.getString("id"), message.getString("id"))){
				found = true;
				break;
			}
		}

		soft.assertTrue(found, "Message listed in the destination queue");

		soft.assertAll();
	}

	@Test
	public void deleteTest() {
		SoftAssert soft = new SoftAssert();

		String source = null;

		JSONArray queues = rest.jms().getQueues();
		for (int i = 0; i < queues.length(); i++) {
		 if(queues.getJSONObject(i).getInt("numberOfMessages") > 0){
				source = queues.getJSONObject(i).getString("name");
			}
		}

		if(null == source){
			throw new SkipException("No messages found to move");
		}

		JSONArray messages = rest.jms().getQueueMessages(source);
		JSONObject message = messages.getJSONObject(0);

		ClientResponse response = rest.jms().deleteMessages(source, message.getString("id"));

		int status = response.getStatus();
		log.debug("Response status: " + status);

		soft.assertTrue(status == 200, "Response status is " + status);

		String responseContent = getSanitizedStringResponse(response);

		soft.assertTrue(new JSONObject(responseContent).getString("outcome").equalsIgnoreCase("success"), "success message returned");

		messages = rest.jms().getQueueMessages(source);
		boolean found = false;
		for (int i = 0; i < messages.length(); i++) {
			JSONObject curMessage = messages.getJSONObject(i);
			if(StringUtils.equalsIgnoreCase(curMessage.getString("id"), message.getString("id"))){
				found = true;
				break;
			}
		}

		soft.assertFalse(found, "Message not listed in the source queue anymore");

		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void moveNegativeTest(String evilStr) {
		SoftAssert soft = new SoftAssert();

		String source = evilStr;
		String destination = evilStr;

		ClientResponse response = rest.jms().moveMessages(source, destination, evilStr);

		validateInvalidResponse(response, soft);

		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void deleteNegativeTest(String evilStr) {
		SoftAssert soft = new SoftAssert();
		ClientResponse response = rest.jms().deleteMessages(evilStr, evilStr);
		validateInvalidResponse(response, soft);
		soft.assertAll();
	}




}
