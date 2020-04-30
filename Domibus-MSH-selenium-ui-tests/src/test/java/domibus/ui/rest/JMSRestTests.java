package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.TestUtils;

public class JMSRestTests extends RestTest {


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

		validateInvalidResponse(response, soft, 400);

		soft.assertAll();
	}

}
