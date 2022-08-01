package ddsl.enums;


import java.util.Arrays;
import java.util.List;



public class DRoles {

	public static final String SUPER = "ROLE_AP_ADMIN";
	public static final String ADMIN = "ROLE_ADMIN";
	public static final String USER = "ROLE_USER";

	public static List<String> userRoleValues() {
		return Arrays.asList(new String[]{"ROLE_AP_ADMIN", "ROLE_ADMIN", "ROLE_USER"});
	}

	public static List<String> pluginUserRoleValues() {
		return Arrays.asList(new String[]{"ROLE_ADMIN", "ROLE_USER"});
	}


}
