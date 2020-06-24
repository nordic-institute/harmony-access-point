package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.enums.DRoles;
import domibus.ui.RestTest;
import domibus.ui.pojos.AuthResp;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class LoginTests extends RestTest {
	
	
	@Test(description = "LGN-z", dataProvider = "readInvalidStrings")
	public void loginNegativeTest(String evilStr) {
		log.debug("param=" + evilStr);
		SoftAssert soft = new SoftAssert();
		ClientResponse response = rest.callLogin(evilStr, evilStr);
		validateInvalidResponse(response, soft);
		soft.assertAll();
	}
	
	@DataProvider
	public Object[][] userProvider() {
		Object[][] toret = null;
		
		if (data.isMultiDomain()) {
			toret = new Object[][]{{DRoles.USER}, {DRoles.ADMIN}, {DRoles.SUPER}};
		} else {
			toret = new Object[][]{{DRoles.USER}, {DRoles.ADMIN}};
		}
		return toret;
	}
	
	@Test(description = "LGN-1", dataProvider = "userProvider")
	public void loginValidUser(String role) throws Exception {
		
		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, role, true, false, true).getString("userName");
		
		ClientResponse response = rest.callLogin(username, data.defaultPass());
		
		soft.assertEquals(response.getStatus(), 200, "Success status returned");
		
		String entityStr = getSanitizedStringResponse(response);
		log.debug(entityStr);
		AuthResp entityObj = mapper.readValue(entityStr, AuthResp.class);
		
		soft.assertEquals(entityObj.getUsername(), username, "Returned username is correct");
		soft.assertEquals(entityObj.getAuthorities().size(), 1, "Only one role listed");
		soft.assertEquals(entityObj.getAuthorities().get(0), role, "User role listed");
		
		soft.assertEquals(entityObj.getDefaultPasswordUsed(), Boolean.FALSE, "Not using default pass");
		soft.assertEquals(entityObj.getExternalAuthProvider(), Boolean.FALSE, "Using normal auth");
		
		soft.assertAll();
	}
	
	@Test(description = "LGN-x", dataProvider = "userProvider")
	public void loginDeletedUser(String role) throws Exception {

		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, role, true, true, true).getString("userName");

		ClientResponse response = rest.callLogin(username, data.defaultPass());

		soft.assertEquals(response.getStatus(), 403, "403 status returned");

		String entityStr = getSanitizedStringResponse(response);
		log.debug(entityStr);

		JSONObject object = new JSONObject(entityStr);
		soft.assertEquals(object.get("message"), "Bad credentials", "Proper message in response");

		soft.assertAll();
	}

	@Test(description = "LGN-x", dataProvider = "userProvider")
	public void loginInactiveUser(String role) throws Exception {

		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, role, false, false, true).getString("userName");

		ClientResponse response = rest.callLogin(username, data.defaultPass());

		soft.assertEquals(response.getStatus(), 403, "403 status returned");

		String entityStr = getSanitizedStringResponse(response);
		log.debug(entityStr);

		JSONObject object = new JSONObject(entityStr);
		soft.assertEquals(object.get("message"), "Inactive", "Proper message in response");

		soft.assertAll();
	}

	@Test(description = "LGN-X", dataProvider = "userProvider")
	public void loginInactiveAndDeletedUser(String role) throws Exception {

		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, role, false, true, true).getString("userName");

		ClientResponse response = rest.callLogin(username, data.defaultPass());

		soft.assertEquals(response.getStatus(), 403, "403 status returned");

		String entityStr = getSanitizedStringResponse(response);
		log.debug(entityStr);

		JSONObject object = new JSONObject(entityStr);
		soft.assertEquals(object.get("message"), "Bad credentials", "Proper message in response");

		soft.assertAll();
	}

	@Test(description = "LGN-5", dataProvider = "userProvider")
	public void loginBlockedAccount(String role) throws Exception {

		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, role, true, false, true).getString("userName");

		for (int i = 0; i < 10; i++) {
			rest.callLogin(username, data.getNewTestPass());
		}

		ClientResponse response = rest.callLogin(username, data.defaultPass());

		soft.assertEquals(response.getStatus(), 403, "403 status returned");

		String entityStr = getSanitizedStringResponse(response);
		log.debug(entityStr);

		JSONObject object = new JSONObject(entityStr);
		soft.assertEquals(object.get("message"), "Suspended", "Proper message in response");

		soft.assertAll();
	}

	@Test(description = "LGN-x", dataProvider = "userProvider")
	public void loginBlockedInactiveAndDeleted(String role) throws Exception {

		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, role, true, false, true).getString("userName");

		for (int i = 0; i < 10; i++) {
			rest.callLogin(username, data.getNewTestPass());
		}

		rest.users().blockUser(username, null);
		rest.users().deleteUser(username, null);

		ClientResponse response = rest.callLogin(username, data.defaultPass());

		soft.assertEquals(response.getStatus(), 403, "403 status returned");

		String entityStr = getSanitizedStringResponse(response);
		log.debug(entityStr);

		JSONObject object = new JSONObject(entityStr);
		soft.assertEquals(object.get("message"), "Bad credentials", "Proper message in response");

		soft.assertAll();
	}

	
}
