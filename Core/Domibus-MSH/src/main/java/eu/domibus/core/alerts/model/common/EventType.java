package eu.domibus.core.alerts.model.common;

import eu.domibus.logging.DomibusMessageCode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Dussart,
 * @author Ion Perpegel
 * @since 4.0
 */
public enum EventType {
    MSG_STATUS_CHANGED(AlertType.MSG_STATUS_CHANGED, Arrays.asList("MESSAGE_ID", "OLD_STATUS", "NEW_STATUS", "FROM_PARTY", "TO_PARTY", "ROLE", "DESCRIPTION")),

    CERT_IMMINENT_EXPIRATION(AlertType.CERT_IMMINENT_EXPIRATION, QueueSelectors.DEFAULT, EventProperties.CERTIFICATE),
    CERT_EXPIRED(AlertType.CERT_EXPIRED, QueueSelectors.DEFAULT, EventProperties.CERTIFICATE),

    USER_LOGIN_FAILURE(AlertType.USER_LOGIN_FAILURE, EventProperties.USER_ACCOUNT_DISABLED, true),
    USER_ACCOUNT_DISABLED(AlertType.USER_ACCOUNT_DISABLED, EventProperties.USER_ACCOUNT_DISABLED, true),
    USER_ACCOUNT_ENABLED(AlertType.USER_ACCOUNT_ENABLED, EventProperties.USER_ACCOUNT_ENABLED, true),
    PLUGIN_USER_LOGIN_FAILURE(AlertType.PLUGIN_USER_LOGIN_FAILURE, EventProperties.USER_ACCOUNT_DISABLED),
    PLUGIN_USER_ACCOUNT_DISABLED(AlertType.PLUGIN_USER_ACCOUNT_DISABLED, EventProperties.USER_ACCOUNT_DISABLED),
    PLUGIN_USER_ACCOUNT_ENABLED(AlertType.PLUGIN_USER_ACCOUNT_ENABLED, EventProperties.USER_ACCOUNT_ENABLED),

    PASSWORD_EXPIRED(AlertType.PASSWORD_EXPIRED, EventProperties.PASSWORD_EXPIRATION, true, DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PASSWORD_IMMINENT_EXPIRATION(AlertType.PASSWORD_IMMINENT_EXPIRATION, EventProperties.PASSWORD_EXPIRATION, true, DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),
    PLUGIN_PASSWORD_EXPIRED(AlertType.PLUGIN_PASSWORD_EXPIRED, EventProperties.PASSWORD_EXPIRATION, DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, EventProperties.PASSWORD_EXPIRATION, DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),

    PLUGIN(AlertType.PLUGIN, QueueSelectors.PLUGIN_EVENT, Collections.emptyList(), DomibusMessageCode.PLUGIN_DEFAULT),

    ARCHIVING_NOTIFICATION_FAILED(AlertType.ARCHIVING_NOTIFICATION_FAILED, Arrays.asList("BATCH_ID", "BATCH_STATUS")),
    ARCHIVING_MESSAGES_NON_FINAL(AlertType.ARCHIVING_MESSAGES_NON_FINAL, Arrays.asList("MESSAGE_ID", "OLD_STATUS")),
    ARCHIVING_START_DATE_STOPPED(AlertType.ARCHIVING_START_DATE_STOPPED, Collections.emptyList()),

    PARTITION_CHECK(AlertType.PARTITION_CHECK, Arrays.asList("PARTITION_NAME")),
    CONNECTION_MONITORING_FAILED(AlertType.CONNECTION_MONITORING_FAILED, Arrays.asList("MESSAGE_ID", "ROLE", "STATUS", "FROM_PARTY", "TO_PARTY"));

    private AlertType defaultAlertType;

    private String queueSelector;

    private boolean userRelated;

    private DomibusMessageCode securityMessageCode;

    List<String> properties;

    EventType(AlertType defaultAlertType, List<String> properties, boolean isUserRelated) {
        setParams(defaultAlertType, properties, null, isUserRelated, null);
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
        setParams(defaultAlertType, properties, queueSelector, false, securityMessageCode);
    }


    public AlertType geDefaultAlertType() {
        return this.defaultAlertType;
    }

    public List<String> getProperties() {
        return properties;
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
        this.securityMessageCode = securityMessageCode;
        this.userRelated = isUserRelated;

        setQueueSelector(defaultAlertType, queueSelector);
    }

    private void setQueueSelector(AlertType defaultAlertType, String queueSelector) {
        if (queueSelector == null) {
            this.queueSelector = getQueueSelector(defaultAlertType);
        } else {
            this.queueSelector = queueSelector;
        }
    }

    private String getQueueSelector(AlertType alertType) {
        String queueSelector;
        if (alertType.getCategory() == AlertCategory.DEFAULT) {
            queueSelector = QueueSelectors.DEFAULT;
        } else if (alertType.getCategory() == AlertCategory.REPETITIVE) {
            queueSelector = QueueSelectors.REPETITIVE;
        } else {
            queueSelector = QueueSelectors.FREQUENCY;
        }
        return queueSelector;
    }

    public static class QueueSelectors {
        public static final String DEFAULT = "defaultSelector";
        public static final String REPETITIVE = "repetitiveSelector";
        public static final String FREQUENCY = "frequencySelector";

        public static final String PLUGIN_EVENT = "PLUGIN_EVENT";
    }

    private static class EventProperties {
        public static final List<String> USER_ACCOUNT_DISABLED = Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_DISABLED");
        public static final List<String> USER_ACCOUNT_ENABLED = Arrays.asList("USER", "USER_TYPE", "LOGIN_TIME", "ACCOUNT_ENABLED");
        public static final List<String> CERTIFICATE = Arrays.asList("ACCESS_POINT", "ALIAS", "EXPIRATION_DATE");
        public static final List<String> PASSWORD_EXPIRATION = Arrays.asList("USER", "USER_TYPE", "EXPIRATION_DATE");
    }
}
