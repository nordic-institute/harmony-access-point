package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.enums.DRoles;
import domibus.ui.RestTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.Generator;

public class UsersRestTest extends RestTest {
	
	@Test(description = "USR-x")
	public void searchUsersTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		JSONArray users = rest.users().getUsers(null);
		
		if (data.isMultiDomain()) {
			soft.assertTrue(users.toString().contains("super"), "Super user is present");
		} else {
			soft.assertTrue(users.toString().contains("admin"), "Admin user is present");
			soft.assertTrue(users.toString().contains("user"), "User user is present");
		}
		soft.assertAll();
	}
	
	@Test(description = "USR-6")
	public void createUserTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String username = Generator.randomAlphaNumeric(15);
		String email = "test@email.com";
		String role = DRoles.ADMIN;
		
		rest.users().createUser(username, role, data.defaultPass(), null);
		JSONArray users = rest.users().getUsers(null);
		
		soft.assertTrue(users.toString().contains(username), "New user in list of users");
		
		soft.assertAll();
	}
	
	@Test(description = "USR-8")
	public void editUserTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String email = "test@email.com";
		String role = DRoles.USER;
		boolean active = false;
		
		JSONObject user = rest.getUser(null, DRoles.ADMIN, true, false, false);
		
		user.put("status", "UPDATED");
		user.put("active", active);
		user.put("email", email);
		user.put("roles", role);
		
		JSONArray toUpdate = new JSONArray();
		toUpdate.put(user);
		
		ClientResponse response = rest.users().putUser(toUpdate, null);
		
		int status = response.getStatus();
		log.debug("Status: " + status);
		
		soft.assertTrue(status == 200, "Response status is success: " + status);
		
		boolean found = false;
		JSONArray users = rest.users().getUsers(null);
		for (int i = 0; i < users.length(); i++) {
			JSONObject curUser = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(curUser.getString("userName"), user.getString("userName"))) {
				found = true;
				soft.assertFalse(curUser.getBoolean("active"), "User is deactivated");
				soft.assertEquals(curUser.getString("roles"), role, "User role si updated");
				soft.assertEquals(curUser.getString("email"), email, "User email si updated");
			}
		}
		
		soft.assertTrue(found, "User still present in user list after edit");
		
		soft.assertAll();
	}
	
	@Test(description = "USR-10")
	public void deleteUserTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONObject user = rest.getUser(null, DRoles.USER, true, false, false);
		
		user.put("status", "REMOVED");
		
		JSONArray toDelete = new JSONArray();
		toDelete.put(user);
		
		ClientResponse response = rest.users().putUser(toDelete, null);
		
		int status = response.getStatus();
		log.debug("Status: " + status);
		
		soft.assertTrue(status == 200, "Response status is success: " + status);
		
		boolean found = false;
		JSONArray users = rest.users().getUsers(null);
		for (int i = 0; i < users.length(); i++) {
			JSONObject curUser = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(curUser.getString("userName"), user.getString("userName"))) {
				found = true;
				soft.assertTrue(curUser.getBoolean("deleted"), "User is marked as deleted");
			}
		}
		
		soft.assertTrue(found, "User still present in user list after edit");
		
		soft.assertAll();
	}
	
	
	@Test(description = "USR-8", dataProvider = "readInvalidStrings")
	public void editUserNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String email = evilStr;
		String role = evilStr;
		
		JSONObject user = rest.getUser(null, DRoles.ADMIN, true, false, false);
		
		user.put("status", "UPDATED");
		user.put("email", email);
		user.put("roles", role);
		
		JSONArray toUpdate = new JSONArray();
		toUpdate.put(user);
		
		ClientResponse response = rest.users().putUser(toUpdate, null);
		validateInvalidResponse(response, soft);
		
		soft.assertAll();
	}
	
	@Test(description = "USR-6", dataProvider = "readInvalidStrings")
	public void createUserNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONArray toCreate = createUserObj(null, evilStr, evilStr, evilStr, true);
		ClientResponse response = rest.users().putUser(toCreate, null);
		validateInvalidResponse(response, soft);
		
		soft.assertAll();
	}
	
	private JSONArray createUserObj(String domain, String username, String email, String role, Boolean active) throws Exception {
		String domainName = "";
		if (StringUtils.isEmpty(domain)) {
			domain = "default";
			domainName = "Default";
		} else {
			JSONArray domains = rest.getDomains();
			for (int i = 0; i < domains.length(); i++) {
				JSONObject curDom = domains.getJSONObject(i);
				if (curDom.toString().contains(domain)) {
					domain = curDom.getString("code");
					domainName = curDom.getString("name");
				}
			}
		}
		
		JSONObject user = new JSONObject();
		
		user.put("active", active);
		user.put("authorities", new JSONArray().toString());
		user.put("deleted", false);
		user.put("domain", domain);
		user.put("domainName", domainName);
		user.put("email", email);
		user.put("password", data.defaultPass());
		user.put("roles", role);
		user.put("status", "NEW");
		user.put("suspended", false);
		user.put("userName", username);
		
		JSONArray array = new JSONArray();
		array.put(user);
		
		return array;
	}
	
}
