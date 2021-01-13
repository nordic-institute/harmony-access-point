package ddsl.enums;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DMessages {


	public static final String LOGIN_INVALID_CREDENTIALS = "The username/password combination you provided is not valid. Please try again or contact your administrator.";
	public static final String LOGIN_ACCOUNT_SUSPENDED = "Too many invalid attempts to log in. Access has been temporarily suspended. Please try again later with the right credentials.";
	public static final String LOGIN_ACCOUNT_SUSPENDED_1 = "The user is suspended. Please try again later or contact your administrator.";
	public static final String LOGIN_USER_INACTIVE = "The user is inactive. Please contact your administrator.";

	public static final String DIALOG_CANCEL_ALL = "Do you want to cancel all unsaved operations?";

	public static final String USER_CANNOT_EDIT_DELETED = "You cannot edit a deleted user.";
	public static final String USER_EMAIL_INVALID = "You should type a valid email";

	public static final String USER_USERNAME_NO_EMPTY = "You should type an username";
	public static final String USER_USERNAME_VALIDATION_SHORT = "You should type at least 4 characters";
	public static final String USER_USERNAME_VALIDATION_SPECIAL_CHR = "You should not use special characters";
	public static final String USER_DELETE_LOGGED_IN_USER = "You cannot delete the logged in user: %s";

	public static final String PLUGIN_USER_ORIGINAL_USER_INVALID = "You should follow the rule: urn:oasis:names:tc:ebcore:partyid-type:[unregistered]:[corner]";

	public static final String ROLE_EMPTY = "You should choose a role";
	public static final String PASS_POLICY_MESSAGE = "Password should follow all of these rules:";
	public static final String PASS_NO_MATCH_MESSAGE = "Passwords do not match";
	public static final String PASS_EMPTY_MESSAGE = "You should type a password";

	public static final String PLUGINUSER_MODAL_ORIGINAL_USER_ERR = "You should follow the rule: urn:oasis:names:tc:ebcore:partyid-type:[unregistered]:[corner]";
	public static final String PLUGINUSER_SAVE_SUCCESS = "The operation 'update plugin users' completed successfully.";
	public static final String PLUGINUSER_DUPLICATE_USERNAME = "The operation 'update plugin users' did not complete successfully. [DOM_001]:Cannot add user %s because it already exists in the %s domain.";
	public static final String PLUGINUSER_DUPLICATE_USERNAME_SAMEDOMAIN = "The operation 'update plugin users' did not complete successfully. [DOM_001]:Cannot add user %s because this name already exists.";
	public static final String DUPLICATE_CERT_PLUGINUSER_ = "The operation update plugin users completed with errors.  [DOM_001]:Cannot add user with certificate %s because this certificate already exists.";
	public static final String USER_DUPLICATE_USERNAME = "The operation 'update users' did not complete successfully. [DOM_001]:Cannot add user %s because it already exists in the %s domain.";


	public static final String CONN_MON_PMODE_CONFIG_ERR = "The Pmode is not properly configured.";

	public static final String MESSAGES_RESEND_MESSAGE_SUCCESS = "The operation resend message completed successfully";

	public static final String PMODE_UPDATE_SUCCESS = "PMode file has been successfully uploaded.";
	public static final String PMODE_PARTIES_UPDATE_SUCCESS = "PMode parties have been successfully updated.";
	public static final String PMODE_PARTIES_DELETE_OWN_PARTY_ERROR = "The operation 'update parties' did not complete successfully. [DOM_003]:Cannot delete the party describing the current system.";
	public static final String PMODE_ARCHIVE_UPDATE_SUCCESS = "The operation 'update pmodes' completed successfully.";
	public static final String PMODE_ARCHIVE_DELETE_SUCCESS = "PModes were deleted";
	
	public static final String TRUSTSTORE_REPLACE_SUCCESS = "Truststore file has been successfully replaced.";
	public static final String TRUSTSTORE_REPLACE_ERROR = "There is an error while uploading truststore.";

	public static final String JMS_MOVE_MESSAGE_SUCCESS = "The operation 'move messages' completed successfully.";
	public static final String JMS_INVALID_SELECTOR_ERROR = "An error occured while loading the JMS messages. In case you are using the Selector / JMS Type please follow the rules for Selector / JMS Type according to Help Page / Admin Guide Error Status: 400";


	public static final String MESSAGE_FILTER_DUPLICATE_FILTER = "Impossible to insert a duplicate entry";
	public static final String MESSAGE_FILTER_SUCCESS = "The operation 'update message filters' completed successfully.";
	
	public static final String CHANGEPASSWORD_WRONG_CURRENT_PASSWORD = "Password could not be changed. [DOM_001]:The current password does not match the provided one.";
	public static final String CHANGEPASSWORD_LAST_FIVE = "Password could not be changed. [DOM_001]:The password of %s user cannot be the same as the last 5";
	
	
	public static final String UI_REPLICATION_NOT_ENABLED = "UIReplication is disabled. No records will be count to be synced for TB_MESSAGE_UI table";
	
	
	public static final String ALERT_ID_INPUT_VALIDATION_MESSAGE = "Alert Id must be 19 digits long and must not start with a 0";
	public static final String ALERT_UPDATE_SUCCESS_MESSAGE = "The operation 'update alerts' completed successfully.";

	public static final String SESSION_EXPIRED_MESSAGE = "You have been logged out because of inactivity or missing access permissions.";


	public static final String PROPERTIES_UPDATE_ERROR_TYPE = "Could not update property: Value '%s' for property '%s' is not of type '%s'";

	
	
	
	
	public class Users {
		public static final String DUPLICATE_USERNAME_ERROR = "The operation 'update users' did not complete successfully. [DOM_001]:Cannot add user %s because it already exists in the %s domain.";
		public static final String DUPLICATE_USERNAME_SAMEDOMAIN_ERROR = "The operation 'update users' did not complete successfully. Duplicate user name for users: %s";
	}

}


