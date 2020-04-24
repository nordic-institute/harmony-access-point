package rest;


import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import ddsl.enums.DRoles;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Generator;
import utils.soap_client.DomibusC1;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DomibusRestClient extends BaseRestClient{


	public static DomibusC1 messageSender = new DomibusC1();
	protected ObjectProvider provider = new ObjectProvider();


	public DomibusRestClient() {
		refreshCookies();
	}

	public String sanitizeResponse(String response) {
		return response.replaceFirst("\\)]}',\n", "");
	}


	public void switchDomain(String domainCode) {
		if (StringUtils.isEmpty(domainCode)) {
			domainCode = "default";
		}

		if (getDomainCodes().contains(domainCode)) {
			WebResource.Builder builder = decorateBuilder(resource.path(RestServicePaths.SESSION_DOMAIN));

			builder.accept(MediaType.TEXT_PLAIN_TYPE).type(MediaType.TEXT_PLAIN_TYPE)
					.put(ClientResponse.class, domainCode);
		}

	}


	// -------------------------------------------- Domains -----------------------------------------------------a-------
	private JSONArray getDomains() {
		JSONArray domainArray = null;
		ClientResponse response = requestGET(resource.path(RestServicePaths.DOMAINS), null);
		try {
			if (response.getStatus() == 200) {
				String rawStringResponse = response.getEntity(String.class);
				domainArray = new JSONArray(sanitizeResponse(rawStringResponse));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return domainArray;
	}

	public List<String> getDomainNames() {
		List<String> toReturn = new ArrayList<>();
		try {
			JSONArray domainArray = getDomains();
			for (int i = 0; i < domainArray.length(); i++) {
				toReturn.add(domainArray.getJSONObject(i).getString("name"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	public List<String> getDomainCodes() {

		List<String> toReturn = new ArrayList<>();

		try {
			JSONArray domainArray = getDomains();
			if (null != domainArray) {
				for (int i = 0; i < domainArray.length(); i++) {
					toReturn.add(domainArray.getJSONObject(i).getString("code"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	public String getDomainCodeForName(String name) {
		try {
			JSONArray domainArray = getDomains();
			for (int i = 0; i < domainArray.length(); i++) {
				String currentName = domainArray.getJSONObject(i).getString("name");
				if (StringUtils.equalsIgnoreCase(currentName, name)) {
					return domainArray.getJSONObject(i).getString("code");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}


	// -------------------------------------------- UI Replication -----------------------------------------------------------
	public void syncRecord() {
		ClientResponse response = requestGET(resource.path(RestServicePaths.UI_REPLICATION_SYNC), null);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Data is not sync now ");
		} else {
			System.out.println("Data is synchronized now with response code:" + response.getStatus());
		}
	}


	public PmodePartiesClient pmodeParties() {
		return new PmodePartiesClient();
	}

	public PropertiesClient properties() {
		return new PropertiesClient();
	}

	public MessageClient messages() {
		return new MessageClient();
	}

	public CSVClient csv() {
		return new CSVClient();
	}

	public PModeClient pmode() {
		return new PModeClient();
	}

	public MessageFiltersClient messFilters() {
		return new MessageFiltersClient();
	}

	public PluginUsersClient pluginUsers() {
		return new PluginUsersClient();
	}

	public UsersClient users() {
		return new UsersClient();
	}

	public ConnectionMonitoringClient connMonitor() {
		return new ConnectionMonitoringClient();
	}

	public String getUsername(String domainCode, String role, boolean active, boolean deleted, boolean forceNew) {
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

	public List<String> getMessageIDsWithStatus(String domainCode, String status) {
		JSONArray mess = messages().getListOfMessages(domainCode);
		List<String> messIDs = new ArrayList<>();

		for (int i = 0; i < mess.length(); i++) {
			if(mess.getJSONObject(i).getString("messageStatus").equalsIgnoreCase(status))
			messIDs.add(mess.getJSONObject(i).getString("messageId"));
		}

		return messIDs;
	}

	public JSONObject getUser(String domainCode, String role, boolean active, boolean deleted, boolean forceNew) {
		String username = Generator.randomAlphaNumeric(10);

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

	public JSONObject getPluginUser(String domainCode, String role, boolean active, boolean forceNew) {
		String username = Generator.randomAlphaNumeric(10);

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

	public String getPluginUsername(String domainCode, String role, boolean active, boolean forceNew) {
		return getPluginUser(domainCode, role, active, forceNew).getString("userName");
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

		String user = Generator.randomAlphaNumeric(10);
		String messageRefID = Generator.randomAlphaNumeric(10);
		String conversationID = Generator.randomAlphaNumeric(10);

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







