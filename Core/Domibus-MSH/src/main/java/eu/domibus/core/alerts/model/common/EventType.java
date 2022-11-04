package eu.domibus.core.alerts.model.common;

import eu.domibus.logging.DomibusMessageCode;

import java.util.*;

/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 4.0
 */
public enum EventType {
    MSG_STATUS_CHANGED(AlertType.MSG_STATUS_CHANGED, Arrays.asList("MESSAGE_ID", "OLD_STATUS", "NEW_STATUS", "FROM_PARTY", "TO_PARTY", "ROLE", "DESCRIPTION")),

    CERT_IMMINENT_EXPIRATION(AlertType.CERT_IMMINENT_EXPIRATION, QuerySelectors.DEFAULT, Constants.CERTIFICATE_PROPS),
    CERT_EXPIRED(AlertType.CERT_EXPIRED, QuerySelectors.DEFAULT, Constants.CERTIFICATE_PROPS),

    USER_LOGIN_FAILURE(AlertType.USER_LOGIN_FAILURE, Constants.USER_ACCOUNT_DISABLED_PROPS, true),
    USER_ACCOUNT_DISABLED(AlertType.USER_ACCOUNT_DISABLED, Constants.USER_ACCOUNT_DISABLED_PROPS, true),
    USER_ACCOUNT_ENABLED(AlertType.USER_ACCOUNT_ENABLED, Constants.USER_ACCOUNT_ENABLED_PROPS, true),
    PLUGIN_USER_LOGIN_FAILURE(AlertType.PLUGIN_USER_LOGIN_FAILURE, Constants.USER_ACCOUNT_DISABLED_PROPS),
    PLUGIN_USER_ACCOUNT_DISABLED(AlertType.PLUGIN_USER_ACCOUNT_DISABLED, Constants.USER_ACCOUNT_DISABLED_PROPS),
    PLUGIN_USER_ACCOUNT_ENABLED(AlertType.PLUGIN_USER_ACCOUNT_ENABLED, Constants.USER_ACCOUNT_ENABLED_PROPS),

    PASSWORD_EXPIRED(AlertType.PASSWORD_EXPIRED, Constants.PASSWORD_EXPIRATION_PROPS, true, DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PASSWORD_IMMINENT_EXPIRATION(AlertType.PASSWORD_IMMINENT_EXPIRATION, Constants.PASSWORD_EXPIRATION_PROPS, true, DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),
    PLUGIN_PASSWORD_EXPIRED(AlertType.PLUGIN_PASSWORD_EXPIRED, Constants.PASSWORD_EXPIRATION_PROPS, DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, Constants.PASSWORD_EXPIRATION_PROPS, DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),

    PLUGIN(AlertType.PLUGIN, QuerySelectors.PLUGIN_EVENT, Collections.emptyList(), DomibusMessageCode.PLUGIN_DEFAULT),

    ARCHIVING_NOTIFICATION_FAILED(AlertType.ARCHIVING_NOTIFICATION_FAILED, Arrays.asList("BATCH_ID", "BATCH_STATUS")),
    ARCHIVING_MESSAGES_NON_FINAL(AlertType.ARCHIVING_MESSAGES_NON_FINAL, Arrays.asList("MESSAGE_ID", "OLD_STATUS")),
    ARCHIVING_START_DATE_STOPPED(AlertType.ARCHIVING_START_DATE_STOPPED, Collections.emptyList()),

    PARTITION_CHECK(AlertType.PARTITION_CHECK, Arrays.asList("PARTITION_NAME")),
    CONNECTION_MONITORING_FAILED(AlertType.CONNECTION_MONITORING_FAILED, Arrays.asList("MESSAGE_ID", "ROLE", "STATUS", "FROM_PARTY", "TO_PARTY", "DESCRIPTION"));


    private AlertType defaultAlertType;

    private String queueSelector;

    private boolean userRelated;

    private DomibusMessageCode securityMessageCode;

    List<String> properties;

    EventType(AlertType defaultAlertType, List<String> properties, boolean isUserRelated) {
        setParams(defaultAlertType, properties,null, isUserRelated, null);
    }

    EventType(AlertType defaultAlertType, List<String> properties) {
        setParams(defaultAlertType, properties, null, false, null);
    }

    EventType(AlertType defaultAlertType, List<String> properties, boolean isUserRelated, DomibusMessageCode securityMessageCode) {
        setParams(defaultAlertType, properties, null, isUserRelated, securityMessageCode);
    }

    EventType(AlertType defaultAlertType, List<String> properties, DomibusMessageCode securityMessageCode) {
        setParams(defaultAlertType, properties, null, false, securityMessageCode);
    }

    EventType(AlertType defaultAlertType, String queueSelector, List<String> properties) {
        setParams(defaultAlertType, properties, queueSelector, false, null);
    }

    EventType(AlertType defaultAlertType, String queueSelector, List<String> properties, DomibusMessageCode securityMessageCode) {
        setParams(defaultAlertType, properties, queueSelector, false, null);
    }


    public AlertType geDefaultAlertType() {
        return this.defaultAlertType;
    }

    public List<String> getProperties() {
        return properties;

//        if (properties != null) {
//            return properties;
//        }
//        ArrayList<String> list = new ArrayList<>();
//        if (this.propertiesEnumClass != null) {
//            EnumSet.allOf(this.propertiesEnumClass).forEach(x -> list.add(((Enum) x).name()));
//        }
//        return list;
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

    private void setParams(AlertType defaultAlertType, List<String> properties, String queueSelector, boolean isUserRelated, DomibusMessageCode securityMessageCode) {
        this.defaultAlertType = defaultAlertType;
        this.properties = properties;
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
        this.userRelated = isUserRelated;
    }

    public static class QuerySelectors {
        public static final String DEFAULT = "defaultSelector";
        public static final String REPETITIVE = "repetitiveSelector";
        public static final String FREQUENCY = "frequencySelector";

        public static final String PLUGIN_EVENT = "PLUGIN_EVENT";
    }

    private static class Constants {
        public static final List<String> USER_ACCOUNT_DISABLED_PROPS = Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_DISABLED");
        public static final List<String> USER_ACCOUNT_ENABLED_PROPS = Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_ENABLED");
        public static final List<String> CERTIFICATE_PROPS = Arrays.asList("ACCESS_POINT", "ALIAS", "EXPIRATION_DATE");
        public static final List<String> PASSWORD_EXPIRATION_PROPS = Arrays.asList("USER", "USER_TYPE", "EXPIRATION_DATE");
    }
}
