package rest;


import com.sun.jersey.api.client.ClientResponse;
import ddsl.enums.DRoles;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Gen;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DomibusRestClient extends BaseRestClient {
	
	public DomibusRestClient(String username, String password) {
		super(username, password);
	}
	
	public DomibusRestClient() {
		super();
	}
	
	// -------------------------------------------- UI Replication -----------------------------------------------------------
	public void syncRecord() throws Exception {
		ClientResponse response = requestGET(resource.path(RestServicePaths.UI_REPLICATION_SYNC), null);
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Data is not sync now ", response);
		} else {
			log.info("Data is synchronized now with response code:" + response.getStatus());
		}
	}
	
	// -------------------------------------------- get clients -----------------------------------------------------------
	public PmodePartiesClient pmodeParties() {
		return new PmodePartiesClient(username, password);
	}
	
	public PropertiesClient properties() {
		return new PropertiesClient(username, password);
	}
	
	public MessageClient messages() {
		return new MessageClient(username, password);
	}
	
	public ErrorsClient errors() {
		return new ErrorsClient(username, password);
	}
	
	
	public CSVClient csv() {
		return new CSVClient(username, password);
	}
	
	public PModeClient pmode() {
		return new PModeClient(username, password);
	}
	
	public JMSClient jms() {
		return new JMSClient(username, password);
	}
	
	public MessageFiltersClient messFilters() {
		return new MessageFiltersClient(username, password);
	}
	
	public PluginUsersClient pluginUsers() {
		return new PluginUsersClient(username, password);
	}
	
	public UsersClient users() {
		return new UsersClient(username, password);
	}
	
	public ConnectionMonitoringClient connMonitor() {
		return new ConnectionMonitoringClient(username, password);
	}
	
	public AlertsRestClient alerts() {
		return new AlertsRestClient(username, password);
	}
	
	public AuditRestClient audit() {
		return new AuditRestClient(username, password);
	}
	
	public LoggingClient logging() {
		return new LoggingClient(username, password);
	}
	
	public UIReplicationClient uiReplication() {
		return new UIReplicationClient(username, password);
	}
	
	
	public String getUsername(String domainCode, String role, boolean active, boolean deleted, boolean forceNew) throws Exception {
		return getUser(domainCode, role, active, deleted, forceNew).getString("userName");
	}
	
	public List<String> getMessageIDs(String domainCode, int noOfNecessaryMessages, boolean forceNew) throws Exception {
		JSONArray mess = messages().getListOfMessages(domainCode);
		List<String> messIDs = new ArrayList<>();
		
		if (forceNew) {
			return sendMessages(noOfNecessaryMessages, domainCode);
		}
		
		if (mess.length() < noOfNecessaryMessages) {
			List<String> sentMess = sendMessages(noOfNecessaryMessages - mess.length(), domainCode);
			messIDs.addAll(sentMess);
		}
		
		for (int i = 0; i < mess.length(); i++) {
			messIDs.add(mess.getJSONObject(i).getString("messageId"));
		}
		
		return messIDs;
	}
	
	public List<String> getMessageIDsWithStatus(String domainCode, String status) throws Exception {
		JSONArray mess = messages().getListOfMessages(domainCode);
		List<String> messIDs = new ArrayList<>();
		
		for (int i = 0; i < mess.length(); i++) {
			if (mess.getJSONObject(i).getString("messageStatus").equalsIgnoreCase(status))
				messIDs.add(mess.getJSONObject(i).getString("messageId"));
		}
		return messIDs;
	}
	
	public JSONObject getUser(String domainCode, String role, boolean active, boolean deleted, boolean forceNew) throws Exception {
		String username = Gen.randomAlphaNumeric(10);
		
		if (StringUtils.isEmpty(domainCode)) {
			domainCode = "default";
		}
		
		if (!forceNew) {
			log.info("trying to find existing user with desired config");
			JSONArray users = users().getUsers(domainCode);
			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(user.getString("userName"), "super")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "admin")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "user")
				) {
					log.info("skipping default users");
					continue;
				}
				
				if (StringUtils.equalsIgnoreCase(user.getString("domain"), domainCode)
						&& StringUtils.equalsIgnoreCase(user.getString("roles"), role)
						&& user.getBoolean("active") == active
						&& user.getBoolean("deleted") == deleted) {
					log.info("found user " + user.getString("userName"));
					return user;
				}
			}
		}
		
		users().createUser(username, role, data.defaultPass(), domainCode);
		log.info("created user " + username);
		
		if (!active) {
			users().blockUser(username, domainCode);
			log.info("deactivated user " + username);
		}
		if (deleted) {
			users().deleteUser(username, domainCode);
			log.info("deleted user " + username);
		}
		
		JSONArray users = users().getUsers(domainCode);
		log.info("searching for user in the system");
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(user.getString("userName"), username)) {
				log.info("user found and returned");
				return user;
			}
		}
		log.info("user not found .. returning null");
		return null;
	}
	
	public JSONObject getPluginUser(String domainCode, String role, boolean active, boolean forceNew) throws Exception {
		String username = Gen.randomAlphaNumeric(10);
		
		if (StringUtils.isEmpty(domainCode)) {
			domainCode = "default";
		}
		
		if (!forceNew) {
			log.info("trying to find existing user with desired config");
			JSONArray users = pluginUsers().getPluginUsers(domainCode, "BASIC");
			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(user.getString("userName"), "super")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "admin")
						|| StringUtils.equalsIgnoreCase(user.getString("userName"), "user")
				) {
					log.info("skipping default users");
					continue;
				}
				
				if (!StringUtils.equalsIgnoreCase(user.getString("userName"), "null")
						&& StringUtils.equalsIgnoreCase(user.getString("authRoles"), role)
						&& user.getBoolean("active") == active) {
					log.info("found user " + user.getString("userName"));
					return user;
				}
			}
		}
		
		pluginUsers().createPluginUser(username, role, data.defaultPass(), domainCode);
		log.info("created user " + username);
		
		if (!active) {
			users().blockUser(username, domainCode);
			log.info("deactivated user " + username);
		}
		
		JSONArray users = pluginUsers().getPluginUsers(domainCode, "BASIC");
		log.info("searching for user in the system");
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(user.getString("userName"), username)) {
				log.info("user found and returned");
				return user;
			}
		}
		log.info("user not found .. returning null");
		return null;
	}
	
	public JSONObject getPluginUser(String domainCode, String type, String role, boolean active, boolean forceNew) throws Exception {
		String username = Gen.randomAlphaNumeric(10);
		
		String identiKey = "userName";
		String authType = "BASIC";
		
		if (type.equalsIgnoreCase("CERTIFICATE")) {
			identiKey = "certificateId";
			authType = "CERTIFICATE";
			String partyName = Gen.randomAlphaNumeric(5);
			String id = Gen.randomAlphaNumeric(15);
			username = String.format("CN=%s,O=eDelivery,C=BE:%s", partyName, id);
		}
		
		if (StringUtils.isEmpty(domainCode)) {
			domainCode = "default";
		}
		
		if (!forceNew) {
			log.info("trying to find existing user with desired config");
			JSONArray users = pluginUsers().getPluginUsers(domainCode, authType);
			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject(i);
				if (!StringUtils.equalsIgnoreCase(user.getString(identiKey), "null")
						&& StringUtils.equalsIgnoreCase(user.getString("authRoles"), role)
						&& user.getBoolean("active") == active) {
					log.info("found user " + user.getString(identiKey));
					return user;
				}
			}
		}
		
		if (type.equalsIgnoreCase("CERTIFICATE")) {
			pluginUsers().createCertPluginUser(username, role, domainCode);
		} else {
			pluginUsers().createPluginUser(username, role, data.defaultPass(), domainCode);
		}
		
		log.info("created user " + username);
		
		JSONArray users = pluginUsers().getPluginUsers(domainCode, authType);
		log.info("searching for user in the system");
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(user.getString(identiKey), username)) {
				log.info("user found and returned");
				return user;
			}
		}
		log.info("user not found .. returning null");
		return null;
	}
	
	public String getNonDefaultDomain() {
		log.info("getting domains");
		List<String> domains = getDomainNames();
		String domain1 = "";
		for (String domain : domains) {
			if (!StringUtils.equalsIgnoreCase(domain, "Default")) {
				domain1 = domain;
				break;
			}
		}
		return domain1;
	}
	
	public List<String> sendMessages(int noOf, String domainCode) throws Exception {
		List<String> messIDs = new ArrayList<>();
		
		String user = Gen.randomAlphaNumeric(10);
		String messageRefID = Gen.randomAlphaNumeric(10);
		String conversationID = Gen.randomAlphaNumeric(10);
		
		pluginUsers().createPluginUser(user, DRoles.ADMIN, data.defaultPass(), domainCode);
		log.info("Created plugin user " + user + " on domain " + domainCode);
		
		log.info("Uploading PMODE ");
		pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		
		for (int i = 0; i < noOf; i++) {
			messIDs.add(messageSender.sendMessage(user, data.defaultPass(), messageRefID, conversationID));
		}
		log.info("Sent messages " + noOf);
		
		pluginUsers().deletePluginUser(user, domainCode);
		log.info("deleted plugin user" + user);
		return messIDs;
	}
}







