package domibus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ddsl.enums.DRoles;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rest.DomibusRestClient;
import utils.Generator;
import utils.TestRunData;
import utils.soap_client.DomibusC1;

import java.util.List;

public class BaseTest {
	
	public static WebDriver driver;
	public static TestRunData data = new TestRunData();
	public static DomibusRestClient rest = new DomibusRestClient();
	public static DomibusC1 messageSender = new DomibusC1();
	
	public ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	
	Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	public void generateTestData() throws Exception {
		
		log.info("GENERATING TEST DATA");
		
		String pass = data.defaultPass();
		
		int noOfMess = rest.messages().getListOfMessages(null).length();
		if (noOfMess < 15) {
			rest.pmode().uploadPMode("pmodes/pmode-dataSetupBlue.xml", null);
			String pluginUsername = rest.getPluginUser(null, DRoles.ADMIN, true, false).getString("userName");
			for (int i = noOfMess; i < 15; i++) {
				messageSender.sendMessage(pluginUsername, pass, Generator.randomAlphaNumeric(20), Generator.randomAlphaNumeric(20));
			}
		}
		
		JSONArray messageFilters = rest.messFilters().getMessageFilters(null);
		for (int i = 0; i < messageFilters.length(); i++) {
			JSONObject obj = messageFilters.getJSONObject(i);
			if (!obj.getBoolean("persisted")) {
				rest.messFilters().saveMessageFilters(messageFilters, null);
				break;
			}
		}
		
		waitForErrors();
		
		log.info("DONE GENERATING TEST DATA");
	}
	
	private void waitForErrors() {
		int noOfErrors = 0;
		int retries = 0;
		while (noOfErrors == 0 && retries < 120) {
			System.out.println("waiting for errors to be logged");
			try {
				noOfErrors = rest.errors().getErrors(null).length();
				retries++;
				Thread.sleep(1000);
			} catch (Exception e) {
				log.error("EXCEPTION: ", e);
			}
		}
	}

	public void cleanMessFilters() throws Exception {
		List<String> domains= rest.getDomainCodes();
		for (int i = 0; i < domains.size() ; i++) {
			rest.messFilters().saveMessageFilters(new JSONArray(), domains.get(i));
		}
	}
	
	
}
