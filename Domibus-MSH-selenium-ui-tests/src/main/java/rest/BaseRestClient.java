package rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TestRunData;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseRestClient {

	protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	protected TestRunData data = new TestRunData();

	protected Client client = Client.create();
	public WebResource resource = client.resource(data.getUiBaseUrl());

	protected List<NewCookie> cookies;
	protected String token;

	//	---------------------------------------Default request methods -------------------------------------------------
	public ClientResponse requestGET(WebResource resource, HashMap<String, String> params) {

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

	public ClientResponse requestDEL(WebResource resource, HashMap<String, String> params) {

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

	public ClientResponse requestPOSTFile(WebResource resource, String filePath, HashMap<String, String> fields) {

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

	public ClientResponse jsonPUT(WebResource resource, String params) {
		return requestPUT(resource, params, MediaType.APPLICATION_JSON);
	}

	//Method is applicable when Media type is TEXT_PLAIN
	public ClientResponse textPUT(WebResource resource, String params) {
		return requestPUT(resource, params, MediaType.TEXT_PLAIN);
	}

	public ClientResponse requestPUT(WebResource resource, String params, String type) {

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

	public boolean isLoggedIn() {
		WebResource.Builder builder = decorateBuilder(resource.path(RestServicePaths.USERNAME));
		int response = builder.get(ClientResponse.class).getStatus();
		return (response == 200);
	}

	public WebResource.Builder decorateBuilder(WebResource resource) {

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




}
