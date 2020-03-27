package domibus.ui.rest;

import ddsl.enums.DRoles;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import rest.DomibusRestClient;
import rest.RestUtils;
import utils.Generator;
import utils.TestRunData;
import utils.soap_client.DomibusC1;

import java.util.ArrayList;
import java.util.List;

public class RestTest {

	public static TestRunData data = new TestRunData();
	public static DomibusC1 messageSender = new DomibusC1();
	public Logger log = LoggerFactory.getLogger(this.getClass().getName());
	public static DomibusRestClient rest = new DomibusRestClient();
	public static RestUtils restUtils = new RestUtils();


	List<String> domains = new ArrayList<>();

	@BeforeSuite(alwaysRun = true)
	public void setup() throws Exception {
		getListOfDomains();

		for (String domain : domains) {
			int noOfMess = rest.getListOfMessages(domain).length();
			if (noOfMess < 15) {
				rest.uploadPMode("pmodes/pmode-dataSetupBlue.xml", domain);
				String pluginUsername = restUtils.getPluginUser(domain, DRoles.ADMIN, true, false).getString("userName");
				for (int i = noOfMess; i < 15; i++) {
					messageSender.sendMessage(pluginUsername
							,data.defaultPass()
							,Generator.randomAlphaNumeric(20)
							, Generator.randomAlphaNumeric(20));
				}
			}

			JSONArray messageFilters = rest.getMessageFilters(domain);
			for (int i = 0; i < messageFilters.length(); i++) {
				JSONObject obj = messageFilters.getJSONObject(i);
				if (!obj.getBoolean("persisted")) {
					rest.saveMessageFilters(messageFilters, domain);
					break;
				}
			}
		}

		log.info("DONE GENERATING TEST DATA");
	}


	private void getListOfDomains(){
		List<String> codes = rest.getDomainCodes();
		if(null == codes || codes.size() == 0){domains.add("default");}
		domains.addAll(codes);
	}

}
