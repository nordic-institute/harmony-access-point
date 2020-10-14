package domibus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ddsl.enums.DRoles;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rest.DomibusRestClient;
import utils.Gen;
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
		
		List<String> domains = rest.getDomainCodes();
		for (int i = 0; i < domains.size(); i++) {
			
			String domain = domains.get(i);
			
			rearrangeMessageFilters(domain);
			
			rest.users().createUser(Gen.randomAlphaNumeric(10), DRoles.ADMIN, pass, domain);
			rest.users().createUser(Gen.randomAlphaNumeric(10), DRoles.USER, pass, domain);
			
			rest.pmode().uploadPMode("pmodes/pmode-dataSetupBlue.xml", domain);
			
			String pluser = Gen.randomAlphaNumeric(10);
			rest.pluginUsers().createPluginUser(pluser, DRoles.ADMIN, pass, domain);
			
			int noOfMess = rest.messages().getListOfMessages(domain).length();
			if (noOfMess < 15) {
				for (int j = noOfMess; j < 15; j++) {
					messageSender.sendMessage(pluser, pass, Gen.randomAlphaNumeric(20), Gen.randomAlphaNumeric(20));
				}
			}
			
			rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", domain);
			for (int j = noOfMess; j < 5; j++) {
				messageSender.sendMessage(pluser, pass, Gen.randomAlphaNumeric(20), Gen.randomAlphaNumeric(20));
			}
			
		}
		
		waitForErrors();
		
		log.info("DONE GENERATING TEST DATA");
	}
	
	private void rearrangeMessageFilters(String domain) throws Exception {
		JSONArray msf = rest.messFilters().getMessageFilters(domain);
		JSONArray msfTS = new JSONArray();
		
		for (int i = 0; i < msf.length(); i++) {
			if(StringUtils.equalsIgnoreCase(msf.getJSONObject(i).getString("backendName"), "Jms")){
				msfTS.put(msf.get(i));
				msf.remove(i);
				break;
			}
		}
		for (int i = 0; i < msf.length(); i++) {
			msfTS.put(msf.getJSONObject(i));
		}
		System.out.println("msfTS = " + msfTS);
		
		rest.messFilters().updateFilterList(msfTS, domain);
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
	
}
