package eu.domibus.core.alerts.model.common;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfiguration;
import eu.domibus.core.earchive.alerts.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

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
    MSG_STATUS_CHANGED("message.ftl"),

    CERT_IMMINENT_EXPIRATION("cert_imminent_expiration.ftl", DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX, true),
    CERT_EXPIRED("cert_expired.ftl", DOMIBUS_ALERT_CERT_EXPIRED_PREFIX, true, CertificateExpiredAlertConfigurationManager.class),

    USER_LOGIN_FAILURE("login_failure.ftl", DOMIBUS_ALERT_USER_LOGIN_FAILURE_PREFIX, ConsoleUserLoginFailAlertConfigurationManager.class),
    USER_ACCOUNT_DISABLED("account_disabled.ftl", DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX, ConsoleAccountDisabledConfigurationManager.class),
    USER_ACCOUNT_ENABLED("account_enabled.ftl", DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_PREFIX),
    PLUGIN_USER_LOGIN_FAILURE("login_failure.ftl", DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_PREFIX),
    PLUGIN_USER_ACCOUNT_DISABLED("account_disabled.ftl", DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_PREFIX, PluginAccountDisabledConfigurationManager.class),
    PLUGIN_USER_ACCOUNT_ENABLED("account_enabled.ftl", DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_PREFIX),

    PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX,
            true, ConsoleUserPasswordExpirationAlertConfigurationManager.class),
    PASSWORD_EXPIRED("password_expired.ftl", DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX, true,
            ConsoleUserPasswordExpirationAlertConfigurationManager.class),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX, true),
    PLUGIN_PASSWORD_EXPIRED("password_expired.ftl", DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX, true),

    PLUGIN("plugin.ftl"),

    ARCHIVING_NOTIFICATION_FAILED("archiving_notification_failed.ftl", DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_PREFIX),
    ARCHIVING_MESSAGES_NON_FINAL("archiving_messages_non_final.ftl", DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_PREFIX),
    ARCHIVING_START_DATE_STOPPED("archiving_start_date_stopped.ftl", DOMIBUS_ALERT_EARCHIVING_START_DATE_STOPPED_PREFIX),

    PARTITION_CHECK("partition_check.ftl", DOMIBUS_ALERT_PARTITION_CHECK_PREFIX),

    CONNECTION_MONITORING_FAILED("connection_monitoring_failed.ftl", DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PREFIX,
            ConnectionMonitoringFailedConfigurationManager.class);

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContextParam) {
        applicationContext = applicationContextParam;
    }

    //in the future an alert will not have one to one mapping.
    public static AlertType getByEventType(EventType eventType) {
        return eventType.geDefaultAlertType();
    }

    private String template;

    private String configurationProperty;

    private boolean repetitive;

    private Class configurationManagerClass;

    private AlertConfigurationManager configurationManager;

    AlertType(String template) {
        setParams(template, null, false, configurationManagerClass);
    }

    AlertType(String template, String configurationProperty) {
        setParams(template, configurationProperty, false, configurationManagerClass);
    }

    AlertType(String template, String configurationProperty, boolean repetitive) {
        setParams(template, configurationProperty, repetitive, configurationManagerClass);
    }

    AlertType(String template, String configurationProperty, Class configurationManagerClass) {
        setParams(template, configurationProperty, false, configurationManagerClass);
    }

    AlertType(String template, String configurationProperty, boolean repetitive, Class configurationManagerClass) {
        setParams(template, configurationProperty, repetitive, configurationManagerClass);
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
        return this.name();
    }

    public boolean isRepetitive() {
        return repetitive;
    }

    public AlertModuleConfiguration getConfiguration() {
        AlertConfigurationManager confMan = getConfigurationManager();
        return confMan.getConfiguration();
    }

    private void setParams(String template, String configurationProperty, boolean repetitive, Class configurationManagerClass) {
        this.template = template;
        this.configurationProperty = configurationProperty;
        this.repetitive = repetitive;
        this.configurationManagerClass = configurationManagerClass;
    }

    public AlertConfigurationManager getConfigurationManager() {
        if (configurationManager != null) {
            return configurationManager;
        }

        if (configurationManagerClass != null) {
            configurationManager = (AlertConfigurationManager) applicationContext.getBean(configurationManagerClass, this, configurationProperty);
        } else if (StringUtils.isNotBlank(configurationProperty)) {
            if (!repetitive) {
                configurationManager = applicationContext.getBean(DefaultConfigurationManager.class, this, configurationProperty);
            } else {
                configurationManager = applicationContext.getBean(RepetitiveAlertConfigurationManager.class, this, configurationProperty);
            }
        }

        DomibusPropertyChangeListener propertyChangeListener = applicationContext.getBean(DefaultAlertConfigurationChangeListener.class, this);

        return configurationManager;
    }

}
