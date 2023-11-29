package eu.domibus.core.alerts.model.common;

import eu.domibus.core.alerts.configuration.account.AccountEnabledConfigurationManager;
import eu.domibus.core.alerts.configuration.account.ConsoleAccountDisabledConfigurationManager;
import eu.domibus.core.alerts.configuration.account.PluginAccountDisabledConfigurationManager;
import eu.domibus.core.alerts.configuration.certificate.CertificateExpiredAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.connectionMonitoring.ConnectionMonitoringFailedConfigurationManager;
import eu.domibus.core.alerts.configuration.login.ConsoleUserLoginFailAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
import eu.domibus.core.alerts.configuration.partitions.PartitionConfigurationManager;
import eu.domibus.core.alerts.configuration.password.ConsoleUserPasswordExpirationAlertConfigurationManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Thomas Dussart
 * @author Ion Perpegel
 * @since 4.0
 */
public enum AlertType {
    MSG_STATUS_CHANGED("message.ftl", DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_PREFIX, MessagingConfigurationManager.class),

    CERT_IMMINENT_EXPIRATION("cert_imminent_expiration.ftl", DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX, AlertCategory.REPETITIVE),
    CERT_EXPIRED("cert_expired.ftl", DOMIBUS_ALERT_CERT_EXPIRED_PREFIX, AlertCategory.REPETITIVE, CertificateExpiredAlertConfigurationManager.class),

    USER_LOGIN_FAILURE("login_failure.ftl", DOMIBUS_ALERT_USER_LOGIN_FAILURE_PREFIX, ConsoleUserLoginFailAlertConfigurationManager.class),
    USER_ACCOUNT_DISABLED("account_disabled.ftl", DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX, ConsoleAccountDisabledConfigurationManager.class),
    USER_ACCOUNT_ENABLED("account_enabled.ftl", DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_PREFIX, AccountEnabledConfigurationManager.class),
    PLUGIN_USER_LOGIN_FAILURE("login_failure.ftl", DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_PREFIX),
    PLUGIN_USER_ACCOUNT_DISABLED("account_disabled.ftl", DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_PREFIX, PluginAccountDisabledConfigurationManager.class),
    PLUGIN_USER_ACCOUNT_ENABLED("account_enabled.ftl", DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_PREFIX, AccountEnabledConfigurationManager.class),

    PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX,
            AlertCategory.REPETITIVE, ConsoleUserPasswordExpirationAlertConfigurationManager.class),
    PASSWORD_EXPIRED("password_expired.ftl", DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX, AlertCategory.REPETITIVE,
            ConsoleUserPasswordExpirationAlertConfigurationManager.class),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX, AlertCategory.REPETITIVE),
    PLUGIN_PASSWORD_EXPIRED("password_expired.ftl", DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX, AlertCategory.REPETITIVE),

    PLUGIN("plugin.ftl"),

    ARCHIVING_NOTIFICATION_FAILED("archiving_notification_failed.ftl", DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_PREFIX),
    ARCHIVING_MESSAGES_NON_FINAL("archiving_messages_non_final.ftl", DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_PREFIX),
    ARCHIVING_START_DATE_STOPPED("archiving_start_date_stopped.ftl", DOMIBUS_ALERT_EARCHIVING_START_DATE_STOPPED_PREFIX),
    ARCHIVING_MESSAGE_EXPORT_FAILED("archiving_message_export_failed.ftl", DOMIBUS_ALERT_EARCHIVING_EXPORT_FAILED_PREFIX),

    PARTITION_CHECK("partition_check.ftl", DOMIBUS_ALERT_PARTITION_CHECK_PREFIX, AlertCategory.WITH_FREQUENCY, PartitionConfigurationManager.class),
    OLD_ONGOING_MESSAGES("plugin.ftl", "NA"), // TODO Francois GAUTIER 21/06/2023 EDELIVERY-11342 title "Ongoing messages not processed anymore"

    CONNECTION_MONITORING_FAILED("connection_monitoring_failed.ftl", DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PREFIX, AlertCategory.WITH_FREQUENCY,
            ConnectionMonitoringFailedConfigurationManager.class);


    //in the future an alert might not have one to one mapping.
    public static AlertType getByEventType(EventType eventType) {
        return eventType.geDefaultAlertType();
    }

    private String template;

    private String configurationProperty;

    private AlertCategory category;

    private Class configurationManagerClass;

    AlertType(String template) {
        setParams(template, null, null, null);
    }

    AlertType(String template, String configurationProperty) {
        setParams(template, configurationProperty, null, null);
    }

    AlertType(String template, String configurationProperty, AlertCategory category) {
        setParams(template, configurationProperty, category, null);
    }

    AlertType(String template, String configurationProperty, Class configurationManagerClass) {
        setParams(template, configurationProperty, null, configurationManagerClass);
    }

    AlertType(String template, String configurationProperty, AlertCategory category, Class configurationManagerClass) {
        setParams(template, configurationProperty, category, configurationManagerClass);
    }


    public String getTemplate() {
        return template;
    }

    public String getConfigurationProperty() {
        return configurationProperty;
    }

    public String getTitle() {
        return this.name();
    }

    public AlertCategory getCategory() {
        return category;
    }

    public List<EventType> getSourceEvents() {
        return Arrays.stream(EventType.values()).filter(el -> el.geDefaultAlertType() == this).collect(Collectors.toList());
    }

    public Class getConfigurationManagerClass() {
        return configurationManagerClass;
    }

    private void setParams(String template, String configurationProperty, AlertCategory category, Class configurationManagerClass) {
        this.template = template;
        this.configurationProperty = configurationProperty;
        this.category = category == null ? AlertCategory.DEFAULT : category;
        this.configurationManagerClass = configurationManagerClass;
    }

}
