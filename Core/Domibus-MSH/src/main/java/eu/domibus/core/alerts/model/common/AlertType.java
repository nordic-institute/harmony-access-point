package eu.domibus.core.alerts.model.common;

import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.earchive.alerts.DefaultConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public enum AlertType {
    MSG_STATUS_CHANGED("message.ftl", "Message status changed"),
    CERT_IMMINENT_EXPIRATION("cert_imminent_expiration.ftl"),
    CERT_EXPIRED("cert_expired.ftl"),
    USER_LOGIN_FAILURE("login_failure.ftl"),
    USER_ACCOUNT_DISABLED("account_disabled.ftl"),
    USER_ACCOUNT_ENABLED("account_enabled.ftl"),
    PLUGIN_USER_LOGIN_FAILURE("login_failure.ftl"),
    PLUGIN_USER_ACCOUNT_DISABLED("account_disabled.ftl"),
    PLUGIN_USER_ACCOUNT_ENABLED("account_enabled.ftl"),
    PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX, "Password imminent expiration"),
    PASSWORD_EXPIRED("password_expired.ftl", DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX, "Password expired"),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX, "Plugin password imminent expiration"),
    PLUGIN_PASSWORD_EXPIRED("password_expired.ftl", DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX, "Plugin password expired"),
    PLUGIN("plugin.ftl", null, "Plugin Alert"),
    ARCHIVING_NOTIFICATION_FAILED("archiving_notification_failed.ftl", DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_PREFIX),
    ARCHIVING_MESSAGES_NON_FINAL("archiving_messages_non_final.ftl"),
    ARCHIVING_START_DATE_STOPPED("archiving_start_date_stopped.ftl"),
    PARTITION_CHECK("partition_check.ftl", DOMIBUS_ALERT_PARTITION_CHECK_PREFIX, "Partition needs verification."),
    CONNECTION_MONITORING_FAILED("connection_monitoring_failed.ftl", "Connection monitoring failed");

//    public static ObjectProvider<DefaultConfigurationManager> defaultConfigurationManagerObjectProvider;

    private String template;
    private String configurationProperty;
    private String title;
    public AlertConfigurationManager configurationManager;

    AlertType(String template, String configurationProperty, String title) {
        setParams(template, configurationProperty, null, title);
    }

    AlertType(String template) {
        this(template, null, null);
    }

    AlertType(String template, String configurationProperty) {
        setParams(template, configurationProperty, null, this.name());
    }

    AlertType(String template, AlertConfigurationManager configurationManager) {
        setParams(template, null, configurationManager, this.name());
    }

    //in the future an alert will not have one to one mapping.
    public static AlertType getByEventType(EventType eventType) {
        return eventType.geDefaultAlertType();
    }

    public List<EventType> getSourceEvents() {
        return Arrays.stream(EventType.values()).filter(el -> el.geDefaultAlertType() == this).collect(Collectors.toList());
    }

    public String getTemplate() {
        return template;
    }

    public String getConfigurationProperty() {
        return configurationProperty;
    }

    public String getTitle() {
        return title;
    }

    public AlertConfigurationManager getConfigurationManager() {
//        if (configurationManager == null && StringUtils.isNotBlank(configurationProperty)) {
//            this.configurationManager = defaultConfigurationManagerObjectProvider.getObject(this, configurationProperty);
//        }
        return configurationManager;
    }

    private void setParams(String template, String configurationProperty, AlertConfigurationManager alertConfigurationManager, String title) {
        this.template = template;
        this.configurationProperty = configurationProperty;
        this.configurationManager = alertConfigurationManager;
        this.title = title;
    }
}
