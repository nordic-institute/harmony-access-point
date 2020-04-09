package rest;


import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DomibusRestClient extends BaseRestClient{



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
}







