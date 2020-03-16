package rest;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import ddsl.enums.DRoles;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Generator;
import utils.TestRunData;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DomibusRestClient {

	protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private Client client = Client.create();
	private TestRunData data = new TestRunData();
	private ObjectProvider provider = new ObjectProvider();
	private WebResource resource = client.resource(data.getUiBaseUrl());

	private List<NewCookie> cookies;
	private String token;

	public DomibusRestClient() {
		refreshCookies();
	}

	private String sanitizeResponse(String response) {
		return response.replaceFirst("\\)]}',\n", "");
	}

	// -------------------------------------------- Login --------------------------------------------------------------
	private void refreshCookies() {
		if (isLoggedIn()) {
			return;
		}

		cookies = login();

		if (null != cookies) {
			for (NewCookie cookie : cookies) {
				if (StringUtils.equalsIgnoreCase(cookie.getName(), "XSRF-TOKEN")) {
					token = cookie.getValue();
				}
			}
		} else {
			throw new RuntimeException("Could not login, tests will not be able to generate necessary data!");
		}

		if (null == token) {
			throw new RuntimeException("Could not obtain XSRF token, tests will not be able to generate necessary data!");
		}
	}

	private boolean isLoggedIn() {
		WebResource.Builder builder = decorateBuilder(resource.path(RestServicePaths.USERNAME));
		int response = builder.get(ClientResponse.class).getStatus();
		return (response == 200);
	}

	private WebResource.Builder decorateBuilder(WebResource resource) {

		WebResource.Builder builder = resource.getRequestBuilder();

		if (null != cookies) {
			for (NewCookie cookie : cookies) {
				builder = builder.cookie(
						new Cookie(cookie.getName(),
								cookie.getValue(),
								"/",
								""
						)
				);

			}
		}

		if (null != token) {
			builder = builder.header("X-XSRF-TOKEN", token);
		}
		return builder;
	}

	public List<NewCookie> login() {
		HashMap<String, String> adminUser = data.getAdminUser();
		log.debug("Rest client using to login: " + adminUser.toString());
		HashMap<String, String> params = new HashMap<>();
		params.put("username", adminUser.get("username"));
		params.put("password", adminUser.get("pass"));

		ClientResponse response = resource.path(RestServicePaths.LOGIN)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, new JSONObject(params).toString());

		if (response.getStatus() == 200) {
			return response.getCookies();
		}
		return null;
	}

	public boolean login(String username, String pass) {
		HashMap<String, String> params = new HashMap<>();
		params.put("username", username);
		params.put("password", pass);

		ClientResponse response = resource.path(RestServicePaths.LOGIN)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, new JSONObject(params).toString());

		return (response.getStatus() == 200);
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

	//	---------------------------------------Default request methods -------------------------------------------------
	private ClientResponse requestGET(WebResource resource, HashMap<String, String> params) {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		if (params != null) {
			for (Map.Entry<String, String> param : params.entrySet()) {
				resource = resource.queryParam(param.getKey(), param.getValue());
			}
		}

		WebResource.Builder builder = decorateBuilder(resource);
		return builder.get(ClientResponse.class);
	}

	private ClientResponse requestPOSTFile(WebResource resource, String filePath, HashMap<String, String> fields) {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		WebResource.Builder builder = decorateBuilder(resource);

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(filePath).getFile());
		FileDataBodyPart filePart = new FileDataBodyPart("file", file);
		FormDataMultiPart multipartEntity = new FormDataMultiPart();
		for (String s : fields.keySet()) {
			multipartEntity.field(s, fields.get(s));
		}
		MultiPart multipart = multipartEntity.bodyPart(filePart);

		return builder.type(MediaType.MULTIPART_FORM_DATA_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.TEXT_PLAIN_TYPE)
				.post(ClientResponse.class, multipartEntity);
	}

	private ClientResponse requestPUT(WebResource resource, String params) {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		WebResource.Builder builder = decorateBuilder(resource);

		return builder
				.type(MediaType.APPLICATION_JSON)
				.put(ClientResponse.class, params);
	}

	//Method is applicable when Media type is TEXT_PLAIN
	private ClientResponse requesttPUT(WebResource resource, String params) {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		WebResource.Builder builder = decorateBuilder(resource);

		return builder
				.type(MediaType.TEXT_PLAIN)
				.put(ClientResponse.class, params);
	}

	// -------------------------------------------- Users --------------------------------------------------------------
	public JSONArray getUsers(String domain) {

		switchDomain(domain);

		ClientResponse response = requestGET(resource.path(RestServicePaths.USERS), null);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not get users ");
		}

		try {
			String rawResp = response.getEntity(String.class);
			return new JSONArray(sanitizeResponse(rawResp));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getNoOfAdmins(String domain) throws Exception {
		JSONArray users = getUsers(domain);
		int adminNo = 0;
		for (int i = 0; i < users.length(); i++) {
			if (DRoles.ADMIN.equalsIgnoreCase(users.getJSONObject(i).getJSONArray("authorities").getString(0))) {
				adminNo++;
			}
		}
		return adminNo;
	}

	public void createUser(String username, String role, String pass, String domain) throws JSONException {
		switchDomain(domain);
		if (null == domain || domain.isEmpty()) {
			domain = "default";
		}

		String payload = provider.createUserObj(username, role, pass, domain);

		ClientResponse response = requestPUT(resource.path(RestServicePaths.USERS), payload);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not create user");
		}
	}


	public void deleteUser(String username, String domain) throws Exception {
		switchDomain(domain);

		String getResponse = requestGET(resource.path(RestServicePaths.USERS), null).getEntity(String.class);

		JSONArray pusers = new JSONArray(sanitizeResponse(getResponse));
		JSONArray toDelete = new JSONArray();
		for (int i = 0; i < pusers.length(); i++) {
			if (StringUtils.equalsIgnoreCase(pusers.getJSONObject(i).getString("userName"), username)) {
				JSONObject tmpUser = pusers.getJSONObject(i);
				tmpUser.put("status", "REMOVED");
				tmpUser.put("deleted", true);
				tmpUser.put("$$index", 0);
				toDelete.put(tmpUser);
			}
		}

		ClientResponse response = requestPUT(resource.path(RestServicePaths.USERS), toDelete.toString());
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not delete user");
		}
	}

	public void updateUser(String username, HashMap<String, String> toUpdate, String domain) {
		HashMap<String, String> adminUser = data.getAdminUser();
		JSONObject user = null;

		try {
			JSONArray array = getUsers(domain);
			for (int i = 0; i < array.length(); i++) {
				JSONObject tmpUser = array.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(tmpUser.getString("userName"), username)) {
					user = tmpUser;
				}
			}

			if (null == user) {
				return;
			}

			for (Map.Entry<String, String> entry : toUpdate.entrySet()) {
				user.put(entry.getKey(), entry.getValue());
			}

			user.put("status", "UPDATED");

			ClientResponse response = requestPUT(resource.path(RestServicePaths.USERS), "[" + user.toString() + "]");
			if (response.getStatus() != 200) {
				throw new RuntimeException("Could not UPDATE user");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void unblockUser(String username, String domain) {
		HashMap<String, String> toUpdate = new HashMap<>();
		toUpdate.put("active", "true");
		updateUser(username, toUpdate, domain);
	}

	public void blockUser(String username, String domain) {
		HashMap<String, String> toUpdate = new HashMap<>();
		toUpdate.put("active", "false");
		updateUser(username, toUpdate, domain);
	}

	// ----------------------------------------- Plugin Users ----------------------------------------------------------

	public void createPluginUser(String username, String role, String pass, String domain) throws JSONException {
		String payload = provider.createPluginUserObj(username, role, pass);

		switchDomain(domain);
		ClientResponse response = requestPUT(resource.path(RestServicePaths.PLUGIN_USERS), payload);
		if (response.getStatus() != 204) {
			throw new RuntimeException("Could not create plugin user");
		}
	}

	public void createCertPluginUser(String username, String role, String domain) throws JSONException {
		String payload = provider.createCertPluginUserObj(username, role);

		switchDomain(domain);
		ClientResponse response = requestPUT(resource.path(RestServicePaths.PLUGIN_USERS), payload);
		if (response.getStatus() != 204) {
			throw new RuntimeException("Could not create plugin user");
		}
	}

	public void deletePluginUser(String username, String domain) throws Exception {

		switchDomain(domain);

		String getResponse = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), null).getEntity(String.class);

		JSONArray pusers = new JSONObject(sanitizeResponse(getResponse)).getJSONArray("entries");
		JSONArray toDelete = new JSONArray();


		for (int i = 0; i < pusers.length(); i++) {
			JSONObject puser = pusers.getJSONObject(i);
			if (!puser.has("userName") || puser.isNull("userName")) {
				continue;
			}
			if (StringUtils.equalsIgnoreCase(puser.getString("userName"), username)) {
				puser.put("status", "REMOVED");
				toDelete.put(puser);
			}
		}

		ClientResponse response = requestPUT(resource.path(RestServicePaths.PLUGIN_USERS), toDelete.toString());
		if (response.getStatus() != 204) {
			throw new RuntimeException("Could not delete plugin user");
		}
	}

	public JSONArray getPluginUsers(String domain, String authType) {

		switchDomain(domain);

		HashMap<String, String> params = new HashMap<>();
		params.put("authType", authType);
		params.put("page", "0");
		params.put("pageSize", "10000");

		ClientResponse response = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), params);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not get users ");
		}

		try {
			String rawResp = response.getEntity(String.class);
			return new JSONObject(sanitizeResponse(rawResp)).getJSONArray("entries");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	// -------------------------------------------- Domains ------------------------------------------------------------
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

	// -------------------------------------------- Message Filters ----------------------------------------------------
	public void createMessageFilter(String actionName, String domain) throws JSONException {

		String payload = provider.createMessageFilterObj(actionName);

		switchDomain(domain);

		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null).getEntity(String.class);
		JSONArray currentMSGF = null;
		try {
			currentMSGF = new JSONObject(sanitizeResponse(currentMSGFRaw)).getJSONArray("messageFilterEntries");
			currentMSGF.put(new JSONObject(payload));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ClientResponse response = requestPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), currentMSGF.toString());
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not get message filter");
		}
	}

	public void saveMessageFilters(JSONArray filters, String domain) throws JSONException {
		switchDomain(domain);
		ClientResponse response = requestPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), filters.toString());
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not get message filter");
		}
	}

	public void deleteMessageFilter(String actionName, String domain) {

		switchDomain(domain);

		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null).getEntity(String.class);
		JSONArray currentMSGF;
		JSONArray deletedL = new JSONArray();

		try {
			currentMSGF = new JSONObject(sanitizeResponse(currentMSGFRaw)).getJSONArray("messageFilterEntries");

			for (int i = 0; i < currentMSGF.length(); i++) {
				JSONObject filter = currentMSGF.getJSONObject(i);
				if (!filter.toString().contains(actionName)) {
					deletedL.put(filter);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}


		ClientResponse response = requestPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), deletedL.toString());
		if (response.getStatus() != 200) {
			log.debug(String.valueOf(response.getStatus()));
			log.debug(response.getEntity(String.class));
			throw new RuntimeException("Could not delete message filter");
		}
	}

	public JSONArray getMessageFilters(String domain) {

		switchDomain(domain);

		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null).getEntity(String.class);
		JSONArray currentMSGF = new JSONArray();

		try {
			currentMSGF = new JSONObject(sanitizeResponse(currentMSGFRaw)).getJSONArray("messageFilterEntries");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return currentMSGF;
	}

	// -------------------------------------------- PMode --------------------------------------------------------------
	public void uploadPMode(String pmodeFilePath, String domain) throws Exception {
		switchDomain(domain);

		HashMap<String, String> fields = new HashMap<>();
		fields.put("description", "automatic red");
		ClientResponse response = requestPOSTFile(resource.path(RestServicePaths.PMODE), pmodeFilePath, fields);
		if (response.getStatus() != 200) {
			log.debug(String.valueOf(response.getStatus()));
			log.debug(response.getEntity(String.class));
			throw new Exception("Could not upload PMODE file!!!");
		}
	}

	public boolean isPmodeUploaded(String domain) throws Exception {
		JSONArray entries = getPmodesList(domain);
		return entries.length() > 0;
	}

	private JSONArray getPmodesList(String domain) throws Exception {
		switchDomain(domain);
		String getResponse = requestGET(resource.path(RestServicePaths.PMODE_LIST), null).getEntity(String.class);

		JSONArray entries = new JSONArray();
		try {
			entries = new JSONArray(sanitizeResponse(getResponse));
		} catch (JSONException e) {
		}

		return entries;
	}

	public Integer getLatestPModeID(String domain) throws Exception {
		switchDomain(domain);

		JSONArray entries = getPmodesList(domain);
		int pmodeID = 0;
		for (int i = 0; i < entries.length(); i++) {
			pmodeID = Math.max(pmodeID, entries.getJSONObject(i).getInt("id"));
		}
		return pmodeID;
	}

	public String downloadPmode(String domain, Integer pmodeID) throws Exception {
		switchDomain(domain);

		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.PMODE_CURRENT_DOWNLOAD + pmodeID), null);

		InputStream in = clientResponse.getEntity(InputStream.class);
		File file = File.createTempFile("pmode", ".xml");
		Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

		in.close();
		return file.getAbsolutePath();
	}


	// -------------------------------------------- Get Grid -----------------------------------------------------------
	public String downloadGrid(String path, HashMap<String, String> params, String domain) throws Exception {
		switchDomain(domain);

		ClientResponse clientResponse = requestGET(resource.path(path), params);

		if (clientResponse.getStatus() == 200) {
			InputStream in = clientResponse.getEntity(InputStream.class);

			File file = File.createTempFile("domibus", ".csv");
			Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

			in.close();
			return file.getAbsolutePath();
		} else {
			log.debug(clientResponse.getEntity(String.class));
			throw new Exception("Could not download file. Request status is " + clientResponse.getStatus());
		}
	}

	// -------------------------------------------- Message ------------------------------------------------------------
	public String downloadMessage(String id, String domain) throws Exception {
		switchDomain(domain);

		HashMap<String, String> params = new HashMap<>();
		params.put("messageId", id);

		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.MESSAGE_LOG_MESSAGE), params);
		InputStream in = clientResponse.getEntity(InputStream.class);

		File file = File.createTempFile("message", ".zip");
		Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

		in.close();

		return file.getAbsolutePath();
	}

	public boolean resendMessage(String id, String domain) throws Exception {
		switchDomain(domain);

		ClientResponse response = requestPUT(resource.path(
				String.format(RestServicePaths.MESSAGE_LOG_RESEND, id)),
				"{}");

		if (response.getStatus() == 200) {
			return true;
		}

		log.error("Resending of message failed with status: " + response.getStatus());
		return false;
	}

	public JSONArray getListOfMessages(String domain) throws Exception {
		switchDomain(domain);
		HashMap<String, String> par = new HashMap<>();
		par.put("pageSize", "100");
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.MESSAGE_LOG_MESSAGES), par);
		if (clientResponse.getStatus() != 200) {
			return new JSONArray();
		}

		return new JSONObject(sanitizeResponse(clientResponse.getEntity(String.class))).getJSONArray("messageLogEntries");
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

	// -------------------------------------------- Domibus Properties -----------------------------------------------------------
	public JSONArray getDomibusPropertyDetail(HashMap<String, String> params) throws Exception {
		ClientResponse clientResponse = requestGET(resource.path(RestServicePaths.DOMIBUS_PROPERTIES), params);
		if (clientResponse.getStatus() != 200) {
			throw new RuntimeException("Could not get properties ");
		}
		return new JSONObject(sanitizeResponse(clientResponse.getEntity(String.class))).getJSONArray("items");

	}

	public void updateDomibusProperty(String propertyName, HashMap<String, String> params, String payload) throws Exception {

		String RestServicePathForPropertyUpdate = RestServicePaths.DOMIBUS_PROPERTIES + "/" + propertyName;
		ClientResponse clientResponse = requesttPUT(resource.path(RestServicePathForPropertyUpdate), payload);
		if (clientResponse.getStatus() != 200) {
			throw new RuntimeException("Could not update " + propertyName + " property");
		}
	}

	// -------------------------------------------- PMODE PARTIES -----------------------------------------------------------

	public void deleteParty(String name) throws Exception{
		JSONArray parties = getParties();

		for (int i = 0; i < parties.length(); i++) {
			JSONObject party = parties.getJSONObject(i);
			if(StringUtils.equalsIgnoreCase(name, party.getString("name"))){
				parties.remove(i);
				break;
			}
		}

		ClientResponse updatePartiesResp = requestPUT(resource.path(RestServicePaths.UPDATE_PARTIES), parties.toString());

		if(updatePartiesResp.getStatus() != 200){
			throw new Exception("delete party failed with status " + updatePartiesResp.getStatus() );
		}
	}

	public JSONArray getParties() throws Exception{
		HashMap<String, String> params = new HashMap<>();
		params.put("pageSize", "0");
		ClientResponse getPartiesResp = requestGET(resource.path(RestServicePaths.GET_PARTIES), params);

		if(getPartiesResp.getStatus() != 200){
			throw new Exception("delete party failed with status " + getPartiesResp.getStatus() );
		}
		JSONArray parties = new JSONArray(sanitizeResponse(getPartiesResp.getEntity(String.class)));
		return parties;
	}

	public void updatePartyURL(String name) throws Exception{
		JSONArray parties = getParties();
		String generatedURL = String.format("http://testhost.com/%s", Generator.randomAlphaNumeric(10));

		for (int i = 0; i < parties.length(); i++) {
			JSONObject party = parties.getJSONObject(i);
			if(StringUtils.equalsIgnoreCase(name, party.getString("name"))){
				parties.getJSONObject(i).put("endpoint", generatedURL);
				break;
			}
		}

		ClientResponse updatePartiesResp = requestPUT(resource.path(RestServicePaths.UPDATE_PARTIES), parties.toString());

		if(updatePartiesResp.getStatus() != 200){
			throw new Exception("delete party failed with status " + updatePartiesResp.getStatus() );
		}
	}



	// -------------------------------------------- CONNECTION MONITORING -----------------------------------------------------------

	public JSONArray getConnectionMonitoringParties() throws Exception{
		HashMap<String, String> params = new HashMap<>();
		params.put("pageSize", "0");
		ClientResponse getPartiesResp = requestGET(resource.path(RestServicePaths.CON_MON_PARTIES), params);

		if(getPartiesResp.getStatus() != 200){
			throw new Exception("get connection monitoring parties failed with status " + getPartiesResp.getStatus() );
		}

		JSONArray parties = new JSONArray(sanitizeResponse(getPartiesResp.getEntity(String.class)));
		return parties;
	}



}







