package eu.domibus.core.alerts.model.common;

import eu.domibus.logging.DomibusMessageCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 4.0
 */
public enum EventType {

    MSG_STATUS_CHANGED(AlertType.MSG_STATUS_CHANGED, Arrays.asList("MESSAGE_ID", "OLD_STATUS", "NEW_STATUS", "FROM_PARTY", "TO_PARTY", "ROLE", "DESCRIPTION")),

    CERT_IMMINENT_EXPIRATION(AlertType.CERT_IMMINENT_EXPIRATION, QuerySelectors.DEFAULT, CertificateEvent.class),
    CERT_EXPIRED(AlertType.CERT_EXPIRED, QuerySelectors.DEFAULT, CertificateEvent.class),

    USER_LOGIN_FAILURE(AlertType.USER_LOGIN_FAILURE, Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_DISABLED"), true),
    USER_ACCOUNT_DISABLED(AlertType.USER_ACCOUNT_DISABLED, Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_DISABLED"), true),
    USER_ACCOUNT_ENABLED(AlertType.USER_ACCOUNT_ENABLED, Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_ENABLED"), true),
    PLUGIN_USER_LOGIN_FAILURE(AlertType.PLUGIN_USER_LOGIN_FAILURE, Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_DISABLED")),
    PLUGIN_USER_ACCOUNT_DISABLED(AlertType.PLUGIN_USER_ACCOUNT_DISABLED, Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_DISABLED")),
    PLUGIN_USER_ACCOUNT_ENABLED(AlertType.PLUGIN_USER_ACCOUNT_ENABLED, Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_ENABLED")),

    PASSWORD_EXPIRED(AlertType.PASSWORD_EXPIRED, PasswordExpirationEventProperties.class, true,
            DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PASSWORD_IMMINENT_EXPIRATION(AlertType.PASSWORD_IMMINENT_EXPIRATION, PasswordExpirationEventProperties.class, true,
            DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),
    PLUGIN_PASSWORD_EXPIRED(AlertType.PLUGIN_PASSWORD_EXPIRED, PasswordExpirationEventProperties.class,
            DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, PasswordExpirationEventProperties.class,
            DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),

    PLUGIN(AlertType.PLUGIN, QuerySelectors.PLUGIN_EVENT, null, DomibusMessageCode.PLUGIN_DEFAULT),

    ARCHIVING_NOTIFICATION_FAILED(AlertType.ARCHIVING_NOTIFICATION_FAILED, ArchivingEventProperties.class),
    ARCHIVING_MESSAGES_NON_FINAL(AlertType.ARCHIVING_MESSAGES_NON_FINAL, MessageEvent.class),
    ARCHIVING_START_DATE_STOPPED(AlertType.ARCHIVING_START_DATE_STOPPED, (Class<? extends Enum>) null),

    PARTITION_CHECK(AlertType.PARTITION_CHECK, Arrays.asList("PARTITION_NAME")),
    CONNECTION_MONITORING_FAILED(AlertType.CONNECTION_MONITORING_FAILED, Arrays.asList("MESSAGE_ID", "ROLE", "STATUS", "FROM_PARTY", "TO_PARTY", "DESCRIPTION"));


    private AlertType defaultAlertType;

    private String queueSelector;

    private Class<? extends Enum> propertiesEnumClass;

    private boolean userRelated;

    private DomibusMessageCode securityMessageCode;

    List<String> properties;

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass, boolean isUserRelated, DomibusMessageCode securityMessageCode) {
        setParams(defaultAlertType, queueSelector, propertiesEnumClass, isUserRelated, securityMessageCode);
    }

    EventType(AlertType defaultAlertType, Class<? extends Enum> propertiesEnumClass, boolean isUserRelated, DomibusMessageCode securityMessageCode) {
        setParams(defaultAlertType, null, propertiesEnumClass, isUserRelated, securityMessageCode);
    }

    EventType(AlertType defaultAlertType, Class<? extends Enum> propertiesEnumClass, boolean isUserRelated) {
        setParams(defaultAlertType, null, propertiesEnumClass, isUserRelated, null);
    }

    EventType(AlertType defaultAlertType, List<String> properties, boolean isUserRelated) {
        setParams(defaultAlertType, null, null, isUserRelated, null);
        this.properties = properties;
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass, DomibusMessageCode securityMessageCode) {
        setParams(defaultAlertType, queueSelector, propertiesEnumClass, false, securityMessageCode);
    }

    EventType(AlertType defaultAlertType, Class<? extends Enum> propertiesEnumClass, DomibusMessageCode securityMessageCode) {
        setParams(defaultAlertType, null, propertiesEnumClass, false, securityMessageCode);
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass) {
        setParams(defaultAlertType, queueSelector, propertiesEnumClass, false, null);
    }

    EventType(AlertType defaultAlertType, Class<? extends Enum> propertiesEnumClass) {
        setParams(defaultAlertType, null, propertiesEnumClass, false, null);
    }

    EventType(AlertType defaultAlertType, List<String> properties) {
        this(defaultAlertType, null, null, false, null);
        this.properties = properties;
    }


    public AlertType geDefaultAlertType() {
        return this.defaultAlertType;
    }

    public List<String> getProperties() {
        if (properties != null) {
            return properties;
        }
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

    private void setParams(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass, boolean isUserRelated, DomibusMessageCode securityMessageCode) {
        this.defaultAlertType = defaultAlertType;
        if (queueSelector == null) {
            if (defaultAlertType.getCategory() == AlertCategory.DEFAULT) {
                this.queueSelector = QuerySelectors.DEFAULT;
            } else if (defaultAlertType.getCategory() == AlertCategory.REPETITIVE) {
                this.queueSelector = QuerySelectors.REPETITIVE;
            } else {
                this.queueSelector = QuerySelectors.FREQUENCY;
            }
        } else {
            this.queueSelector = queueSelector;
        }
        this.securityMessageCode = securityMessageCode;
        this.propertiesEnumClass = propertiesEnumClass;
        this.userRelated = isUserRelated;
    }

    public static class QuerySelectors {
        public static final String DEFAULT = "defaultSelector";
        public static final String REPETITIVE = "repetitiveSelector";
        public static final String FREQUENCY = "frequencySelector";

        public static final String PLUGIN_EVENT = "PLUGIN_EVENT";
        public static final String PARTITION_CHECK = "PARTITION_CHECK";
    }

}
