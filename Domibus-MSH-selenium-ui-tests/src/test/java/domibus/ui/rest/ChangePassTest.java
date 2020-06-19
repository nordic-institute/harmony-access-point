package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.enums.DRoles;
import domibus.ui.RestTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import rest.BaseRestClient;

public class ChangePassTest extends RestTest {
	
	@Test(description = "CP-3")
	public void changePass() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, DRoles.USER, true, false, true).getString("userName");
		
		BaseRestClient myUserRest = new BaseRestClient(username, data.defaultPass());
		ClientResponse response = myUserRest.changePassword(data.defaultPass(), data.getNewTestPass());
		
		soft.assertEquals(response.getStatus(), 204, "204 status");
		soft.assertTrue(rest.login(username, data.getNewTestPass()), "Login with new pass works");
		soft.assertFalse(rest.login(username, data.defaultPass()), "Login with old pass NOT works");
		
		soft.assertAll();
	}
	
	@Test(description = "CP-3", dataProvider = "readInvalidStrings")
	public void changePassNegativeTests(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = rest.getUser(null, DRoles.USER, true, false, true).getString("userName");
		
		BaseRestClient myUserRest = new BaseRestClient(username, data.defaultPass());
		ClientResponse response = myUserRest.changePassword(evilStr, evilStr);
		
		soft.assertTrue(response.getStatus() < 500, "correct status");
		soft.assertTrue(rest.login(username, data.defaultPass()), "Login with old pass works");
		
		soft.assertAll();
	}
	
}
