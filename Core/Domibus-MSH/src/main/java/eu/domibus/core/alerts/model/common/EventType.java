package eu.domibus.core.alerts.model.common;

import eu.domibus.logging.DomibusMessageCode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 4.0
 */
public enum EventType {

    MSG_STATUS_CHANGED(AlertType.MSG_STATUS_CHANGED, QuerySelectors.MESSAGE, MessageEvent.class),

    CONNECTION_MONITORING_FAILED(AlertType.CONNECTION_MONITORING_FAILED, QuerySelectors.CONNECTION_MONITORING_FAILURE, ConnectionMonitoringFailedEventProperties.class),

    CERT_IMMINENT_EXPIRATION(AlertType.CERT_IMMINENT_EXPIRATION, QuerySelectors.CERTIFICATE_IMMINENT_EXPIRATION, CertificateEvent.class),
    CERT_EXPIRED(AlertType.CERT_EXPIRED, QuerySelectors.CERTIFICATE_EXPIRED, CertificateEvent.class),

    USER_LOGIN_FAILURE(AlertType.USER_LOGIN_FAILURE, QuerySelectors.LOGIN_FAILURE, UserLoginFailedEventProperties.class, true),
    USER_ACCOUNT_DISABLED(AlertType.USER_ACCOUNT_DISABLED, QuerySelectors.ACCOUNT_DISABLED, UserAccountDisabledEventProperties.class, true),
    USER_ACCOUNT_ENABLED(AlertType.USER_ACCOUNT_ENABLED, QuerySelectors.ACCOUNT_ENABLED, UserAccountEnabledEventProperties.class, true),
    PLUGIN_USER_LOGIN_FAILURE(AlertType.PLUGIN_USER_LOGIN_FAILURE, QuerySelectors.LOGIN_FAILURE, UserLoginFailedEventProperties.class),
    PLUGIN_USER_ACCOUNT_DISABLED(AlertType.PLUGIN_USER_ACCOUNT_DISABLED, QuerySelectors.ACCOUNT_DISABLED, UserAccountDisabledEventProperties.class),
    PLUGIN_USER_ACCOUNT_ENABLED(AlertType.PLUGIN_USER_ACCOUNT_ENABLED, QuerySelectors.ACCOUNT_ENABLED, UserAccountEnabledEventProperties.class),

    PASSWORD_EXPIRED(AlertType.PASSWORD_EXPIRED, QuerySelectors.PASSWORD_EXPIRATION, PasswordExpirationEventProperties.class, true,
            DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PASSWORD_IMMINENT_EXPIRATION(AlertType.PASSWORD_IMMINENT_EXPIRATION, QuerySelectors.PASSWORD_EXPIRATION, PasswordExpirationEventProperties.class, true,
            DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),
    PLUGIN_PASSWORD_EXPIRED(AlertType.PLUGIN_PASSWORD_EXPIRED, QuerySelectors.PASSWORD_EXPIRATION, PasswordExpirationEventProperties.class,
            DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, QuerySelectors.PASSWORD_EXPIRATION, PasswordExpirationEventProperties.class,
            DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),
    PLUGIN(AlertType.PLUGIN, QuerySelectors.PLUGIN_EVENT, null, DomibusMessageCode.PLUGIN_DEFAULT),
    ARCHIVING_NOTIFICATION_FAILED(AlertType.ARCHIVING_NOTIFICATION_FAILED, QuerySelectors.ARCHIVING_NOTIFICATION_FAILED, ArchivingEventProperties.class),
    ARCHIVING_MESSAGES_NON_FINAL(AlertType.ARCHIVING_MESSAGES_NON_FINAL, QuerySelectors.ARCHIVING_MESSAGES_NON_FINAL, MessageEvent.class),
    ARCHIVING_START_DATE_STOPPED(AlertType.ARCHIVING_START_DATE_STOPPED, QuerySelectors.ARCHIVING_START_DATE_STOPPED, null),
    PARTITION_CHECK(AlertType.PARTITION_CHECK, QuerySelectors.PARTITION_CHECK,  PartitionCheckEvent.class);


    private AlertType defaultAlertType;
    private final String queueSelector;
    private Class<? extends Enum> propertiesEnumClass;
    private boolean userRelated;
    private final DomibusMessageCode securityMessageCode;

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass,
              boolean isUserRelated, DomibusMessageCode securityMessageCode) {
        this.defaultAlertType = defaultAlertType;
        this.queueSelector = queueSelector;
        this.securityMessageCode = securityMessageCode;
        this.propertiesEnumClass = propertiesEnumClass;
        this.userRelated = isUserRelated;
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass,
              boolean isUserRelated) {
        this(defaultAlertType, queueSelector, propertiesEnumClass, isUserRelated, null);
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass,
              DomibusMessageCode securityMessageCode) {
        this(defaultAlertType, queueSelector, propertiesEnumClass, false, securityMessageCode);
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass) {
        this(defaultAlertType, queueSelector, propertiesEnumClass, false, null);
    }


    public AlertType geDefaultAlertType() {
        return this.defaultAlertType;
    }

    public List<String> getProperties() {
        ArrayList<String> list = new ArrayList<>();
        if (this.propertiesEnumClass != null) {
            EnumSet.allOf(this.propertiesEnumClass).forEach(x -> list.add(((Enum) x).name()));
        }
        return list;
    }

    public String getQueueSelector() {
        return this.queueSelector;
    }

    public DomibusMessageCode getSecurityMessageCode() {
        return this.securityMessageCode;
    }

    public boolean isUserRelated() {
        return this.userRelated;
    }

    public static class QuerySelectors {
        public static final String MESSAGE = "message";
        public static final String CERTIFICATE_IMMINENT_EXPIRATION = "certificateImminentExpiration";
        public static final String CERTIFICATE_EXPIRED = "certificateExpired";
        public static final String CONNECTION_MONITORING_FAILURE = "connectionMonitoringFailure";
        public static final String LOGIN_FAILURE = "loginFailure";
        public static final String ACCOUNT_DISABLED = "accountDisabled";
        public static final String ACCOUNT_ENABLED = "accountEnabled";
        public static final String PASSWORD_EXPIRATION = "PASSWORD_EXPIRATION";
        public static final String PLUGIN_EVENT = "PLUGIN_EVENT";
        public static final String ARCHIVING_NOTIFICATION_FAILED = "ARCHIVING_NOTIFICATION_FAILED";
        public static final String ARCHIVING_MESSAGES_NON_FINAL = "ARCHIVING_MESSAGES_NON_FINAL";
        public static final String ARCHIVING_START_DATE_STOPPED = "ARCHIVING_START_DATE_STOPPED";
        public static final String PARTITION_CHECK = "PARTITION_CHECK";
    }
}
