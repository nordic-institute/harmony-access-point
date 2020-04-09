package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Generator;

import java.util.HashMap;

public class PmodePartiesClient extends DomibusRestClient{
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

		ClientResponse updatePartiesResp = jsonPUT(resource.path(RestServicePaths.UPDATE_PARTIES), parties.toString());

		if(updatePartiesResp.getStatus() != 200){
			throw new Exception("delete party failed with status " + updatePartiesResp.getStatus() );
		}
	}

	public JSONArray getParties( ) throws Exception{
		HashMap<String, String> params = new HashMap<>();
		params.put("pageSize", "0");
		ClientResponse getPartiesResp = requestGET(resource.path(RestServicePaths.GET_PARTIES), params);

		if(getPartiesResp.getStatus() != 200){
			throw new Exception("delete party failed with status " + getPartiesResp.getStatus() );
		}
		return new JSONArray(sanitizeResponse(getPartiesResp.getEntity(String.class)));
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

		ClientResponse updatePartiesResp = jsonPUT(resource.path(RestServicePaths.UPDATE_PARTIES), parties.toString());

		if(updatePartiesResp.getStatus() != 200){
			throw new Exception("delete party failed with status " + updatePartiesResp.getStatus() );
		}
	}
}
