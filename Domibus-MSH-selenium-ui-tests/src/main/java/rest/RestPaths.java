package rest;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class RestPaths {

	public class LOGIN{
		public static final String LOGIN = "/rest/security/authentication";
		public static final String USERNAME = "/rest/security/username";
		public static final String DOMAINS = "/rest/application/domains";
		public static final String SESSION_DOMAIN = "/rest/security/user/domain";
	}
	public class PMODE{
		public static final String PMODE = "/rest/pmode";
		public static final String PMODE_LIST = "/rest/pmode/list";
		public static final String PMODE_CURRENT_DOWNLOAD = "/rest/pmode/";
		public static final String PMODE_ARCHIVE_CSV = "/rest/pmode/csv";
		public static final String PMODE_PARTIES_CSV = "/rest/party/csv";
	}
	public class USERS{
		public static final String USERS = "/rest/user/users";
		public static final String USERS_CSV = "/rest/user/csv";
	}
	public class PLUGIN_USERS{
		public static final String PLUGIN_USERS = "/rest/plugin/users";
		public static final String PLUGIN_USERS_CSV = "/rest/plugin/csv";
	}
	public class MESSAGES{
		public static final String MESSAGE_LOG_CSV = "/rest/messagelog/csv";
		public static final String MESSAGE_LOG_MESSAGE = "/rest/message/download";
		public static final String MESSAGE_LOG_MESSAGES = "rest/messagelog";
	}

	public class MESSAGES_FILTERS{
		public static final String MESSAGE_FILTERS = "/rest/messagefilters";
		public static final String MESSAGE_FILTERS_CSV = "/rest/messagefilters/csv";
	}

	public class ERROR_LOG{
		public static final String ERROR_LOG_CSV = "/rest/errorlogs/csv";
	}
	public class TRUSTORE{
		public static final String TRUSTSTORE = "/rest/truststore/save";
		public static final String TRUSTSTORE_CSV = "/rest/truststore/csv";
	}
	public class JMSMESSEGES{
		public static final String JMS_MESSAGES_CSV = "/rest/jms/csv";
	}
	public class AUDIT{
		public static final String AUDIT_CSV = "/rest/audit/csv";
	}
	public class ALERTS{
		public static final String ALERTS_CSV = "/rest/alerts/csv";
	}
	public class PROPERTIES{
		public static final String DOMIBUS_PROPERTIES="rest/configuration/properties";
	}

	public static final String UI_REPLICATION_SYNC= "/rest/uireplication/sync";


}
