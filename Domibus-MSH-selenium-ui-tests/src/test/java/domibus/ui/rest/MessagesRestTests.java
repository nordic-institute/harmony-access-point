package domibus.ui.rest;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import rest.clients.MessagesRestClient;
import utils.TestRunData;
import utils.soap_client.DomibusC1;

import java.util.ArrayList;
import java.util.List;

public class MessagesRestTests extends RestTest {


	private MessagesRestClient messagesClient= new MessagesRestClient();
	public static TestRunData data = new TestRunData();
	public static DomibusC1 messageSender = new DomibusC1();

	public Logger log = LoggerFactory.getLogger(this.getClass().getName());

	JSONArray messages;
	List<String> domains = new ArrayList<>();


	/*Filter messages using basic filters */
	@Test(description = "MSG-5", groups = {"multiTenancy", "singleTenancy"})
	public void filterUsingBasicFilters() throws Exception{
		SoftAssert soft = new SoftAssert();

		soft.assertAll();
	}




}
