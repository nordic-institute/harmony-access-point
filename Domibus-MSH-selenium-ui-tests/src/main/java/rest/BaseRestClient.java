package rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.tool.Extension;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Entity;
import rest.utilPojo.Param;
import utils.TestRunData;
import utils.soap_client.DomibusC1;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseRestClient {

	public static DomibusC1 messageSender = new DomibusC1();
	protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	protected TestRunData data = new TestRunData();

	protected Client client = Client.create();
	public WebResource resource = client.resource(data.getUiBaseUrl());

	protected List<NewCookie> cookies;
	protected String token;
	protected ObjectProvider provider = new ObjectProvider();

	protected String username;
	protected String password;

	public BaseRestClient(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public BaseRestClient() {
		this.username = data.getAdminUser().get("username");
		this.password = data.getAdminUser().get("pass");;
	}

	//	---------------------------------------Default request methods -------------------------------------------------
	protected ClientResponse requestGET(WebResource resource, HashMap<String, String> params) {

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

	protected ClientResponse multivalueGET(WebResource resource, ArrayList<Param> params) {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		if (params != null) {
			for (Param param : params) {
				resource = resource.queryParam(param.getKey(), param.getValue());
			}
		}

		WebResource.Builder builder = decorateBuilder(resource);
		return builder.get(ClientResponse.class);
	}

	protected ClientResponse requestDEL(WebResource resource, HashMap<String, String> params) {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		if (params != null) {
			for (Map.Entry<String, String> param : params.entrySet()) {
				resource = resource.queryParam(param.getKey(), param.getValue());
			}
		}

		WebResource.Builder builder = decorateBuilder(resource);
		return builder.delete(ClientResponse.class);
	}

	protected ClientResponse requestPOSTFile(WebResource resource, String filePath, HashMap<String, String> fields) {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		WebResource.Builder builder = decorateBuilder(resource);

		File file = new File(getClass().getClassLoader().getResource(filePath).getFile());
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

	protected ClientResponse requestPOSTJKSFile(WebResource resource, String filePath, HashMap<String, String> fields) throws IOException {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		WebResource.Builder builder = decorateBuilder(resource);

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(filePath).getFile());
		FileDataBodyPart filePart = new FileDataBodyPart("truststore", file);

		FormDataMultiPart multiPartEntity = new FormDataMultiPart();
		multiPartEntity.field("password", "test123");
		multiPartEntity.bodyPart(filePart);

		return builder
				.type(MediaType.MULTIPART_FORM_DATA_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.TEXT_PLAIN_TYPE)
				.post(ClientResponse.class,
						multiPartEntity);
	}

	protected ClientResponse requestPOST(WebResource resource, String content) {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		WebResource.Builder builder = decorateBuilder(resource);

		return builder
				.type(MediaType.APPLICATION_JSON)
				.post(ClientResponse.class, content);
	}

	protected ClientResponse jsonPUT(WebResource resource, String params) {
		return requestPUT(resource, params, MediaType.APPLICATION_JSON);
	}

	//Method is applicable when Media type is TEXT_PLAIN
	protected ClientResponse textPUT(WebResource resource, String params) {
		return requestPUT(resource, params, MediaType.TEXT_PLAIN);
	}

	protected ClientResponse requestPUT(WebResource resource, String params, String type) {

		if (!isLoggedIn()) {
			refreshCookies();
		}

		WebResource.Builder builder = decorateBuilder(resource);

		return builder
				.type(type)
				.put(ClientResponse.class, params);
	}


	// -------------------------------------------- Login --------------------------------------------------------------
	public void refreshCookies() {
		if (isLoggedIn()) {
			return;
		}

		cookies = login();

		if (null != cookies) {
			token = extractToken();
		} else {
			throw new RuntimeException("Could not login, tests will not be able to generate necessary data!");
		}

		if (null == token) {
			throw new RuntimeException("Could not obtain XSRF token, tests will not be able to generate necessary data!");
		}
	}

	private String extractToken(){
		String mytoken = null;
		for (NewCookie cookie : cookies) {
			if (StringUtils.equalsIgnoreCase(cookie.getName(), "XSRF-TOKEN")) {
				mytoken = cookie.getValue();
			}
		}
		return mytoken;
	}

	public boolean isLoggedIn() {
		WebResource.Builder builder = decorateBuilder(resource.path(RestServicePaths.USERNAME));
		int response = builder.get(ClientResponse.class).getStatus();
		return (response == 200);
	}

	protected WebResource.Builder decorateBuilder(WebResource resource) {

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

	protected List<NewCookie> login() {
		log.debug("Rest client using to login: " + this.username);
		HashMap<String, String> params = new HashMap<>();
		params.put("username", this.username);
		params.put("password", this.password);

		ClientResponse response = resource.path(RestServicePaths.LOGIN)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, new JSONObject(params).toString());

		if (response.getStatus() == 200) {
			return response.getCookies();
		}
		return null;
	}

	public boolean login(String username, String pass) {
		return callLogin(username, pass).getStatus() == 200;
	}

	public ClientResponse callLogin(String username, String pass) {
		HashMap<String, String> params = new HashMap<>();
		params.put("username", username);
		params.put("password", pass);

		ClientResponse response = resource.path(RestServicePaths.LOGIN)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, new JSONObject(params).toString());

		return response;
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
	public JSONArray getDomains() {
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

	public ClientResponse getPath(String path){
		return requestGET(resource.path(path), null);
	}

	public ClientResponse changePassword(String oldPass, String newPass){
		JSONObject params = new JSONObject();
		params.put("currentPassword", oldPass);
		params.put("newPassword", newPass);
		return jsonPUT(resource.path(RestServicePaths.PASSWORD), params.toString());
	}
}
