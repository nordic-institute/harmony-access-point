package utils.soap_client;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import utils.Gen;
import utils.TestRunData;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.substringBetween;


public class DomibusC1 {

	Logger log = Logger.getAnonymousLogger();
	private final String DEFAULT_WEBSERVICE_LOCATION = new TestRunData().getUiBaseUrl() + "services/backend";

	public String sendMessage(String pluginU, String password, String messageRefID, String conversationID) throws Exception {

		log.info(String.format("Sending message using plugin user %s and pass %s", pluginU, password));
		String messTemplate = new String(Files.readAllBytes(Paths.get("src/main/resources/messages/messageTemplate.xml")));
		if (isEmpty(messageRefID)) {
			messageRefID = "";
		}
		if (isEmpty(conversationID)) {
			conversationID = "";
		}

		String messBody = messTemplate.replace("${mess_id}", Gen.randomAlphaNumeric(10) + "@selenium")
				.replace("${ref_id}", messageRefID)
				.replace("${convo_id}", conversationID);

		HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter(pluginU, password);

		Client client = Client.create();
		client.addFilter(authFilter);

		ClientResponse response = client.resource(DEFAULT_WEBSERVICE_LOCATION)
				.header("Content-Type", "text/xml; charset=utf-8")
				.post(ClientResponse.class, messBody);

		if (response.getStatus() == 200) {
			return substringBetween(response.getEntity(String.class), "<messageID>", "</messageID>").trim();
		} else {
			log.severe("Sending message to WS plugin resulted in error code " + response.getStatus());
			log.severe("Sending message to WS plugin resulted in error " + response.getEntity(String.class));
		}
		throw new Exception("Could not send message to WS plugin");
	}


}
