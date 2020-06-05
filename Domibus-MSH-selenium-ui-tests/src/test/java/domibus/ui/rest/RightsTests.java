package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.enums.DRoles;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import rest.DomibusRestClient;
import rest.RestServicePaths;

public class RightsTests extends RestTest {
	
	@Test
	public void userRightsTests() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, DRoles.USER, true, false, true).getString("userName");
		
		DomibusRestClient userClient = new DomibusRestClient(username, data.defaultPass());
		
		
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_LOG_MESSAGES, userClient), 200, "Status does not match expectations MESSAGE_LOG_MESSAGES");
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_LOG_CSV, userClient), 200, "Status does not match expectations MESSAGE_LOG_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.ERRORS, userClient), 200, "Status does not match expectations ERRORS");
		soft.assertEquals(callGetPath(RestServicePaths.ERROR_LOG_CSV, userClient), 200, "Status does not match expectations ERROR_LOG_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_FILTERS, userClient), 403, "Status does not match expectations MESSAGE_FILTERS");
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_FILTERS_CSV, userClient), 403, "Status does not match expectations MESSAGE_FILTERS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_PROCESS_LIST, userClient), 403, "Status does not match expectations PMODE_PROCESS_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_RESTORE, userClient), 403, "Status does not match expectations PMODE_RESTORE");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_CURRENT_DOWNLOAD, userClient), 403, "Status does not match expectations PMODE_CURRENT_DOWNLOAD");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_ARCHIVE_CSV, userClient), 403, "Status does not match expectations PMODE_ARCHIVE_CSV");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_LIST, userClient), 403, "Status does not match expectations PMODE_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_PARTIES_CSV, userClient), 403, "Status does not match expectations PMODE_PARTIES_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.JMS_ACTION, userClient), 403, "Status does not match expectations JMS_ACTION");
		soft.assertEquals(callGetPath(RestServicePaths.JMS_SEARCH, userClient), 403, "Status does not match expectations JMS_SEARCH");
		soft.assertEquals(callGetPath(RestServicePaths.JMS_QUEUES, userClient), 403, "Status does not match expectations JMS_QUEUES");
		soft.assertEquals(callGetPath(RestServicePaths.JMS_MESSAGES_CSV, userClient), 403, "Status does not match expectations JMS_MESSAGES_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.TRUSTSTORE, userClient), 403, "Status does not match expectations TRUSTSTORE");
		soft.assertEquals(callGetPath(RestServicePaths.TRUSTSTORE_CSV, userClient), 403, "Status does not match expectations TRUSTSTORE_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.USERS, userClient), 403, "Status does not match expectations USERS");
		soft.assertEquals(callGetPath(RestServicePaths.USERS_CSV, userClient), 403, "Status does not match expectations USERS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.PLUGIN_USERS, userClient), 403, "Status does not match expectations PLUGIN_USERS");
		soft.assertEquals(callGetPath(RestServicePaths.PLUGIN_USERS_CSV, userClient), 403, "Status does not match expectations PLUGIN_USERS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.AUDIT_COUNT, userClient), 403, "Status does not match expectations AUDIT_COUNT");
		soft.assertEquals(callGetPath(RestServicePaths.AUDIT_LIST, userClient), 403, "Status does not match expectations AUDIT_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.AUDIT_CSV, userClient), 403, "Status does not match expectations AUDIT_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.ALERTS_LIST, userClient), 403, "Status does not match expectations ALERTS_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.ALERTS_CSV, userClient), 403, "Status does not match expectations ALERTS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.CON_MON_PARTIES_DETAILS, userClient), 403, "Status does not match expectations CON_MON_PARTIES_DETAILS");
		soft.assertEquals(callGetPath(RestServicePaths.CON_MON_PARTIES, userClient), 403, "Status does not match expectations CON_MON_PARTIES");
		
		soft.assertEquals(callGetPath(RestServicePaths.LOGGING_RESET, userClient), 403, "Status does not match expectations LOGGING_RESET");
		soft.assertEquals(callGetPath(RestServicePaths.LOGGING, userClient), 403, "Status does not match expectations LOGGING");
		
		soft.assertEquals(callGetPath(RestServicePaths.DOMIBUS_PROPERTIES, userClient), 403, "Status does not match expectations DOMIBUS_PROPERTIES");
		
		if (data.isMultiDomain()) {
			soft.assertEquals(callGetPath(RestServicePaths.DOMAINS, userClient), 200, "Status does not match expectations");
		}
		
		soft.assertAll();
	}
	
	@Test
	public void adminRightsTests() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName");
		
		DomibusRestClient adminClient = new DomibusRestClient(username, data.defaultPass());
		
		
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_LOG_MESSAGES, adminClient), 200, "Status does not match expectations MESSAGE_LOG_MESSAGES");
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_LOG_CSV, adminClient), 200, "Status does not match expectations MESSAGE_LOG_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.ERRORS, adminClient), 200, "Status does not match expectations ERRORS");
		soft.assertEquals(callGetPath(RestServicePaths.ERROR_LOG_CSV, adminClient), 200, "Status does not match expectations ERROR_LOG_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_FILTERS, adminClient), 200, "Status does not match expectations MESSAGE_FILTERS");
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_FILTERS_CSV, adminClient), 200, "Status does not match expectations MESSAGE_FILTERS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_PROCESS_LIST, adminClient), 200, "Status does not match expectations PMODE_PROCESS_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_ARCHIVE_CSV, adminClient), 200, "Status does not match expectations PMODE_ARCHIVE_CSV");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_LIST, adminClient), 200, "Status does not match expectations PMODE_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_PARTIES_CSV, adminClient), 200, "Status does not match expectations PMODE_PARTIES_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.JMS_QUEUES, adminClient), 200, "Status does not match expectations JMS_QUEUES");
		
		soft.assertEquals(callGetPath(RestServicePaths.TRUSTSTORE_CSV, adminClient), 200, "Status does not match expectations TRUSTSTORE_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.USERS, adminClient), 200, "Status does not match expectations USERS");
		soft.assertEquals(callGetPath(RestServicePaths.USERS_CSV, adminClient), 200, "Status does not match expectations USERS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.PLUGIN_USERS, adminClient), 200, "Status does not match expectations PLUGIN_USERS");
		soft.assertEquals(callGetPath(RestServicePaths.PLUGIN_USERS_CSV, adminClient), 200, "Status does not match expectations PLUGIN_USERS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.AUDIT_COUNT, adminClient), 200, "Status does not match expectations AUDIT_COUNT");
		soft.assertEquals(callGetPath(RestServicePaths.AUDIT_LIST, adminClient), 200, "Status does not match expectations AUDIT_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.AUDIT_CSV, adminClient), 200, "Status does not match expectations AUDIT_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.ALERTS_LIST, adminClient), 200, "Status does not match expectations ALERTS_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.ALERTS_CSV, adminClient), 200, "Status does not match expectations ALERTS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.CON_MON_PARTIES_DETAILS, adminClient), 200, "Status does not match expectations CON_MON_PARTIES_DETAILS");
		soft.assertEquals(callGetPath(RestServicePaths.CON_MON_PARTIES, adminClient), 200, "Status does not match expectations CON_MON_PARTIES");
		
		soft.assertEquals(callGetPath(RestServicePaths.LOGGING, adminClient), 200, "Status does not match expectations LOGGING");
		
		soft.assertEquals(callGetPath(RestServicePaths.DOMIBUS_PROPERTIES, adminClient), 200, "Status does not match expectations DOMIBUS_PROPERTIES");
		
		if (data.isMultiDomain()) {
			soft.assertEquals(callGetPath(RestServicePaths.DOMAINS, adminClient), 403, "Status does not match expectations");
		}
		
		soft.assertAll();
	}
	
	@Test(groups = "multiTenancy")
	public void superRightsTests() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, DRoles.SUPER, true, false, true).getString("userName");
		
		DomibusRestClient superClient = new DomibusRestClient(username, data.defaultPass());
		
		
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_LOG_MESSAGES, superClient), 200, "Status does not match expectations MESSAGE_LOG_MESSAGES");
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_LOG_CSV, superClient), 200, "Status does not match expectations MESSAGE_LOG_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.ERRORS, superClient), 200, "Status does not match expectations ERRORS");
		soft.assertEquals(callGetPath(RestServicePaths.ERROR_LOG_CSV, superClient), 200, "Status does not match expectations ERROR_LOG_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_FILTERS, superClient), 200, "Status does not match expectations MESSAGE_FILTERS");
		soft.assertEquals(callGetPath(RestServicePaths.MESSAGE_FILTERS_CSV, superClient), 200, "Status does not match expectations MESSAGE_FILTERS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_PROCESS_LIST, superClient), 200, "Status does not match expectations PMODE_PROCESS_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_ARCHIVE_CSV, superClient), 200, "Status does not match expectations PMODE_ARCHIVE_CSV");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_LIST, superClient), 200, "Status does not match expectations PMODE_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.PMODE_PARTIES_CSV, superClient), 200, "Status does not match expectations PMODE_PARTIES_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.JMS_QUEUES, superClient), 200, "Status does not match expectations JMS_QUEUES");
		
		soft.assertEquals(callGetPath(RestServicePaths.TRUSTSTORE_CSV, superClient), 200, "Status does not match expectations TRUSTSTORE_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.USERS, superClient), 200, "Status does not match expectations USERS");
		soft.assertEquals(callGetPath(RestServicePaths.USERS_CSV, superClient), 200, "Status does not match expectations USERS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.PLUGIN_USERS, superClient), 200, "Status does not match expectations PLUGIN_USERS");
		soft.assertEquals(callGetPath(RestServicePaths.PLUGIN_USERS_CSV, superClient), 200, "Status does not match expectations PLUGIN_USERS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.AUDIT_COUNT, superClient), 200, "Status does not match expectations AUDIT_COUNT");
		soft.assertEquals(callGetPath(RestServicePaths.AUDIT_LIST, superClient), 200, "Status does not match expectations AUDIT_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.AUDIT_CSV, superClient), 200, "Status does not match expectations AUDIT_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.ALERTS_LIST, superClient), 200, "Status does not match expectations ALERTS_LIST");
		soft.assertEquals(callGetPath(RestServicePaths.ALERTS_CSV, superClient), 200, "Status does not match expectations ALERTS_CSV");
		
		soft.assertEquals(callGetPath(RestServicePaths.CON_MON_PARTIES_DETAILS, superClient), 200, "Status does not match expectations CON_MON_PARTIES_DETAILS");
		soft.assertEquals(callGetPath(RestServicePaths.CON_MON_PARTIES, superClient), 200, "Status does not match expectations CON_MON_PARTIES");
		
		soft.assertEquals(callGetPath(RestServicePaths.LOGGING, superClient), 200, "Status does not match expectations LOGGING");
		
		soft.assertEquals(callGetPath(RestServicePaths.DOMIBUS_PROPERTIES, superClient), 200, "Status does not match expectations DOMIBUS_PROPERTIES");
		
		soft.assertEquals(callGetPath(RestServicePaths.DOMAINS, superClient), 200, "Status does not match expectations");
		
		soft.assertAll();
	}
	
	private int callGetPath(String path, DomibusRestClient client) throws Exception {
		ClientResponse response = client.getPath(path);
		
		log.debug(path + " returned status " + response.getStatus());
		log.debug(path + " returned content " + response.getEntity(String.class));
		
		return response.getStatus();
	}
	
	
}
