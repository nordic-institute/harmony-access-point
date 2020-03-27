package rest;

import ddsl.enums.DRoles;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Generator;
import utils.TestRunData;
import utils.soap_client.DomibusC1;

import java.util.ArrayList;
import java.util.List;

public class RestUtils {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private static TestRunData data = new TestRunData();
	public static DomibusRestClient rest = new DomibusRestClient();
	public static DomibusC1 messageSender = new DomibusC1();



	public String getUsername(String domainCode, String role, boolean active, boolean deleted, boolean forceNew) throws Exception {
		return getUser(domainCode, role, active, deleted, forceNew).getString("userName");
	}

	public List<String> getMessageIDs(String domainCode, int noOfNecessaryMessages, boolean forceNew) throws Exception {
		JSONArray mess = rest.getListOfMessages(domainCode);
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
		JSONArray mess = rest.getListOfMessages(domainCode);
		List<String> messIDs = new ArrayList<>();

		for (int i = 0; i < mess.length(); i++) {
			if(mess.getJSONObject(i).getString("messageStatus").equalsIgnoreCase(status))
			messIDs.add(mess.getJSONObject(i).getString("messageId"));
		}

		return messIDs;
	}

	public List<String> sendMessages(int noOf, String domainCode) throws Exception {
		List<String> messIDs = new ArrayList<>();

		String user = Generator.randomAlphaNumeric(10);
		String messageRefID = Generator.randomAlphaNumeric(10);
		String conversationID = Generator.randomAlphaNumeric(10);

		rest.createPluginUser(user, DRoles.ADMIN, data.defaultPass(), domainCode);
		log.info("Created plugin user " + user + " on domain " + domainCode);

		log.info("Uploading PMODE ");
		rest.uploadPMode("pmodes/pmode-blue.xml", null);

		for (int i = 0; i < noOf; i++) {
			messIDs.add(messageSender.sendMessage(user, data.defaultPass(), messageRefID, conversationID));
		}
		log.info("Sent messages " + noOf);

		rest.deletePluginUser(user, domainCode);
		log.info("deleted plugin user" + user);
		return messIDs;
	}

	public JSONObject getUser(String domainCode, String role, boolean active, boolean deleted, boolean forceNew) throws Exception {
		String username = Generator.randomAlphaNumeric(10);

		if (StringUtils.isEmpty(domainCode)) {
			domainCode = "default";
		}

		if (!forceNew) {
			log.info("trying to find existing user with desired config");
			JSONArray users = rest.getUsers(domainCode);
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

		rest.createUser(username, role, data.defaultPass(), domainCode);
		log.info("created user " + username);

		if (!active) {
			rest.blockUser(username, domainCode);
			log.info("deactivated user " + username);
		}
		if (deleted) {
			rest.deleteUser(username, domainCode);
			log.info("deleted user " + username);
		}

		JSONArray users = rest.getUsers(domainCode);
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
		String username = Generator.randomAlphaNumeric(10);

		if (StringUtils.isEmpty(domainCode)) {
			domainCode = "default";
		}

		if (!forceNew) {
			log.info("trying to find existing user with desired config");
			JSONArray users = rest.getPluginUsers(domainCode, "BASIC");
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

		rest.createPluginUser(username, role, data.defaultPass(), domainCode);
		log.info("created user " + username);

		if (!active) {
			rest.blockUser(username, domainCode);
			log.info("deactivated user " + username);
		}

		JSONArray users = rest.getPluginUsers(domainCode, "BASIC");
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

	public String getPluginUsername(String domainCode, String role, boolean active, boolean forceNew) throws Exception {
		return getPluginUser(domainCode, role, active, forceNew).getString("userName");
	}

	public String getNonDefaultDomain() throws Exception {
		log.info("getting domains");
		List<String> domains = rest.getDomainNames();
		String domain1 = "";
		for (String domain : domains) {
			if (!StringUtils.equalsIgnoreCase(domain, "Default")) {
				domain1 = domain;
				break;
			}
		}
		return domain1;
	}
}
