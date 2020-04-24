package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.enums.DRoles;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import domibus.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Lists;
import utils.Generator;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestTest extends BaseTest {

	public Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private String invalidStringsFile = "src/test/resources/rest_csv/invalidStrings.txt";

	List<String> domains = new ArrayList<>();
	List<String> messageFilterPlugins = new ArrayList<>();

	@BeforeSuite(alwaysRun = true)
	public void setup() throws Exception {
		getListOfDomains();
		getListOfPlugins();

		for (String domain : domains) {
			int noOfMess = rest.messages().getListOfMessages(domain).length();
			if (noOfMess < 15) {
				rest.pmode().uploadPMode("pmodes/pmode-dataSetupBlue.xml", domain);
				String pluginUsername = rest.getPluginUser(domain, DRoles.ADMIN, true, false).getString("userName");
				for (int i = noOfMess; i < 15; i++) {
					messageSender.sendMessage(pluginUsername
							,data.defaultPass()
							,Generator.randomAlphaNumeric(20)
							, Generator.randomAlphaNumeric(20));
				}
			}

			JSONArray messageFilters = rest.messFilters().getMessageFilters(domain);
			for (int i = 0; i < messageFilters.length(); i++) {
				JSONObject obj = messageFilters.getJSONObject(i);
				if (!obj.getBoolean("persisted")) {
					rest.messFilters().saveMessageFilters(messageFilters, domain);
					break;
				}
			}
		}

		log.info("DONE GENERATING TEST DATA");
	}

	public String getSanitizedStringResponse(ClientResponse response){
		return rest.sanitizeResponse(response.getEntity(String.class));
	}

	private void getListOfDomains(){
		List<String> codes = rest.getDomainCodes();
		if(null == codes || codes.size() == 0){domains.add("default");}
		domains.addAll(codes);
	}

	private void getListOfPlugins(){
		Set<String> uniqPluginNames = new HashSet<>();
		JSONArray msgfs = rest.messFilters().getMessageFilters(null);

		for (int i = 0; i < msgfs.length() ; i++) {
			JSONObject msgf = msgfs.getJSONObject(i);
			uniqPluginNames.add(msgf.getString("backendName"));
		}
		messageFilterPlugins.addAll(uniqPluginNames);

	}

	protected Object[][] readCSV(String filename) throws IOException {
		Reader reader = Files.newBufferedReader(Paths.get(filename));
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase()
				.withTrim());
		List<CSVRecord> records = csvParser.getRecords();

		Object[][] toRet = new Object[records.size()][1];

		for (int i = 0; i < records.size() ; i++) {
			toRet[i][0] = records.get(i).toMap();
		}

		return toRet;
	}

	@DataProvider
	protected Object[][] readInvalidStrings() throws IOException {

		List<String> strings = Files.readAllLines(Paths.get(invalidStringsFile));

		Object[][] toRet = new Object[strings.size()][1];

		for (int i = 0; i < strings.size() ; i++) {
			toRet[i][0] = strings.get(i);
		}

		return toRet;
	}

	protected void validateInvalidResponse(ClientResponse response, SoftAssert soft, Integer... acceptedResponseCodes){
		Integer status=  response.getStatus();
		String responseContent = getSanitizedStringResponse(response);

		log.debug("Response status: " + status);
		log.debug("Response content: " + responseContent);

		soft.assertTrue(Lists.newArrayList(acceptedResponseCodes).contains(status), "Response status not as expected, found: " + status);

		try {
			new JSONObject(responseContent);
		} catch (JSONException e) {
			soft.fail("Response is not in JSON format");
		}

	}


}
