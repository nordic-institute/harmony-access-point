package ddsl.enums;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DMessages {


	public static final String MSG_1 = "The username/password combination you provided is not valid. Please try again or contact your administrator.";
	public static final String MSG_2 = "Too many invalid attempts to log in. Access has been temporarily suspended. Please try again later with the right credentials.";
	public static final String MSG_2_1 = "The user is suspended. Please try again later or contact your administrator.";
	public static final String MSG_2_2 = "The user is inactive. Please contact your administrator.";

	public static final String DIALOG_CANCEL_ALL = "Do you want to cancel all unsaved operations?";

	public static final String EMAIL_INVALID_MESSAGE = "You should type an email";

	public static final String USERNAME_NO_EMPTY_MESSAGE = "You should type an username";
	public static final String USERNAME_VALIDATION_MESSAGE = "Username can only contain alphanumeric characters (letters A-Z, numbers 0-9) and must have from 4 to 32 characters";
	public static final String DELETE_LOGGED_IN_ERROR = "You cannot delete the logged in user: %s";

	public static final String ORIGINAL_USER_NOTVALID = "You should follow the rule: urn:oasis:names:tc:ebcore:partyid-type:[unregistered]:[corner]";

	public static final String ROLE_NOTEMPTY = "You need to choose at least one role for this user";


	public static final String PASS_POLICY_MESSAGE = "Password should follow all of these rules:\n" +
			"            - Minimum length: 8 characters- Maximum length: 32 characters- At least one letter in lowercase- At least one letter in uppercase- At least one digit- At least one special character";

	public static final String PASS_NO_MATCH_MESSAGE = "Passwords do not match";

	public static final String PASS_NO_EMPTY_MESSAGE = "You should type a password";

	public static final String DUPLICATE_PLUGINUSER_ = "The operation update plugin users completed with errors.  [DOM_001]:Cannot add user %s because this name already exists.";
	public static final String DUPLICATE_CERT_PLUGINUSER_ = "The operation update plugin users completed with errors.  [DOM_001]:Cannot add user with certificate %s because this certificate already exists.";
	public static final String DUPLICATE_USER_ = "The operation update users not completed successfully. [DOM_001]:Cannot add user %s because this name already exists in the %s domain.";


	public static final String TESTSERVICE_NOTCONFIGURED = "The test service is not properly configured.";

	public static final String RESEND_MESSAGE_SUCCESS = "The operation resend message completed successfully";

	public static final String PMODE_UPDATE_SUCCESS = "PMode file has been successfully uploaded";
	public static final String PMODE_PARTIES_UPDATE_SUCCESS = "Parties saved successfully.";

	public static final String JMS_MOVE_MESSAGE_SUCCESS = "The operation move messages completed successfully.";
	public static final String JMS_INVALID_SELECTOR_ERROR = "An error occured while loading the JMS messages. In case you are using the Selector / JMS Type please follow the rules for Selector / JMS Type according to Help Page / Admin Guide Error Status: 0";


	public static final String DUPLICATE_MESSAGE_FILTER_ERROR = "Impossible to insert a duplicate entry";

}


