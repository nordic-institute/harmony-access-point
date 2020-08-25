package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PmodePartiesClient extends BaseRestClient {
	public PmodePartiesClient(String username, String password) {
		super(username, password);
	}
	
	// -------------------------------------------- PMODE PARTIES -----------------------------------------------------------
	public void deleteParty(String name) throws Exception {
		JSONArray parties = getParties();
		
		for (int i = 0; i < parties.length(); i++) {
			JSONObject party = parties.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(name, party.getString("name"))) {
				parties.remove(i);
				break;
			}
		}
		
		ClientResponse updatePartiesResp = jsonPUT(resource.path(RestServicePaths.UPDATE_PARTIES), parties.toString());
		
		if (updatePartiesResp.getStatus() != 200) {
			throw new DomibusRestException("delete party failed!!", updatePartiesResp);
		}
	}
	
	public JSONArray getParties() throws Exception {
		return getParties(null);
	}
	
	public JSONArray getParties(String domain) throws Exception {
		
		switchDomain(domain);
		HashMap<String, String> params = new HashMap<>();
		params.put("pageSize", "0");
		ClientResponse getPartiesResp = requestGET(resource.path(RestServicePaths.GET_PARTIES), params);
		
		if (getPartiesResp.getStatus() != 200) {
			throw new DomibusRestException("delete party failed!!", getPartiesResp);
		}
		return new JSONArray(sanitizeResponse(getPartiesResp.getEntity(String.class)));
	}
	
	public void updatePartyURL(String name) throws Exception {
		JSONArray parties = getParties();
		String generatedURL = String.format("http://testhost.com/%s", Generator.randomAlphaNumeric(10));
		
		for (int i = 0; i < parties.length(); i++) {
			JSONObject party = parties.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(name, party.getString("name"))) {
				parties.getJSONObject(i).put("endpoint", generatedURL);
				break;
			}
		}
		
		ClientResponse updatePartiesResp = jsonPUT(resource.path(RestServicePaths.UPDATE_PARTIES), parties.toString());
		
		if (updatePartiesResp.getStatus() != 200) {
			throw new DomibusRestException("delete party failed!!" , updatePartiesResp);
		}
	}
	
	public ClientResponse createParty(String domain, String name, String endpoint, String[] initiatorsProc, String[] respondersProc, String partyID, String partyType, String partyIdVal) throws Exception {
		if (StringUtils.isEmpty(name)) {
			name = Generator.randomAlphaNumeric(10);
		}
		if (StringUtils.isEmpty(endpoint)) {
			endpoint = "http://" + Generator.randomAlphaNumeric(10) + ".com";
		}
		if (StringUtils.isEmpty(partyID)) {
			partyID = Generator.randomAlphaNumeric(10);
		}
		if (StringUtils.isEmpty(partyType)) {
			partyType = Generator.randomAlphaNumeric(10);
		}
		if (StringUtils.isEmpty(partyIdVal)) {
			partyIdVal = Generator.randomAlphaNumeric(10);
		}
//		if(null == initiatorsProc || initiatorsProc.length == 0){ initiatorsProc = {Generator.randomAlphaNumeric(10)};}
//		if(null == respondersProc || respondersProc.length == 0){ respondersProc = {Generator.randomAlphaNumeric(10)};}
		
		JSONObject newParty = createPartyObject(name, endpoint, initiatorsProc, respondersProc, partyID, partyType, partyIdVal);
		
		JSONArray parties = getParties(domain);
		parties.put(newParty);
		
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.UPDATE_PARTIES), parties.toString());
		return response;
	}
	
	public JSONArray getProcesses(String domain) throws Exception {
		ClientResponse response = requestGET(resource.path(RestServicePaths.PMODE_PROCESS_LIST), null);
		if (response.getStatus() == 200) {
			return new JSONArray(sanitizeResponse(response.getEntity(String.class)));
		}
		return null;
	}
	
	
	private JSONObject createPartyObject(String name, String endpoint, String[] initiatorsProc, String[] respondersProc, String partyID, String partyType, String partyIdVal) {
		JSONObject party = new JSONObject();
		party.put("entityId", 0);
		party.put("name", name);
		party.put("userName", JSONObject.NULL);
		party.put("certificateContent", JSONObject.NULL);
		party.put("endpoint", endpoint);
		
		
		addProcessNodes(party, initiatorsProc, respondersProc);
		addIdentifier(party, partyID, partyType, partyIdVal);
		
		return party;
	}
	
	private void addProcessNodes(JSONObject party, String[] initiators, String[] responders) {
		if (null == initiators) {
			initiators = new String[]{};
		}
		if (null == responders) {
			responders = new String[]{};
		}
		
		List<String> joinedProcesses = new ArrayList<>();
		for (int i = 0; i < initiators.length; i++) {
			String initiator = initiators[i];
			String particle = "I";
			for (int j = 0; j < responders.length; j++) {
				String respoder = responders[j];
				if (respoder.equalsIgnoreCase(initiator)) {
					particle = "IR";
				}
			}
			joinedProcesses.add(String.format("%s(%s)", initiator, particle));
		}
		
		for (int i = 0; i < responders.length; i++) {
			String responder = responders[i];
			if (Arrays.asList(initiators).contains(responder)) {
				continue;
			} else {
				joinedProcesses.add(String.format("%s(%s)", responder, "R"));
			}
		}
		
		party.put("processesWithPartyAsInitiator", createProcessNodes(initiators));
		party.put("processesWithPartyAsResponder", createProcessNodes(responders));
	}
	
	private JSONArray createProcessNodes(String[] processNames) {
		JSONArray arr = new JSONArray();
		
		for (int i = 0; i < processNames.length; i++) {
			String processName = processNames[i];
			
			JSONObject innerObj = new JSONObject();
			innerObj.put("entityId", 0);
			innerObj.put("name", processName);
			
			arr.put(innerObj);
		}
		
		return arr;
	}
	
	private void addIdentifier(JSONObject party, String partyID, String partyType, String partyIdVal) {
		String partyIdent;
		
		if (party.has("joinedIdentifiers")) {
			partyIdent = party.getString("joinedIdentifiers") + ", " + partyID;
		} else {
			partyIdent = partyID;
		}
		party.put("joinedIdentifiers", partyIdent);
		
		JSONArray identifiers = new JSONArray();
		if (party.has("identifiers")) {
			identifiers = party.getJSONArray("identifiers");
		}
		
		JSONObject obj = new JSONObject();
		obj.put("name", partyType);
		obj.put("value", partyIdVal);
		
		JSONObject identifier = new JSONObject();
		identifier.put("partyId", partyID);
		identifier.put("partyIdType", obj);
		identifiers.put(identifier);
		
		
		party.put("identifiers", identifiers);
	}
	
	
}
