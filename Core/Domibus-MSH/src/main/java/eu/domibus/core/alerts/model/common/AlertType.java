package eu.domibus.core.alerts.model.common;

import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfiguration;
import eu.domibus.core.earchive.alerts.DefaultAlertConfiguration;
import eu.domibus.core.earchive.alerts.DefaultConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

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
    PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX),
    PASSWORD_EXPIRED("password_expired.ftl", DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX),
    PLUGIN_PASSWORD_EXPIRED("password_expired.ftl", DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX),
    PLUGIN("plugin.ftl", null),
    ARCHIVING_NOTIFICATION_FAILED("archiving_notification_failed.ftl", DOMIBUS_ALERT_EARCHIVING_NOTIFICATION_FAILED_PREFIX),
    ARCHIVING_MESSAGES_NON_FINAL("archiving_messages_non_final.ftl", DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_PREFIX),
    ARCHIVING_START_DATE_STOPPED("archiving_start_date_stopped.ftl"),
    PARTITION_CHECK("partition_check.ftl", DOMIBUS_ALERT_PARTITION_CHECK_PREFIX),
    CONNECTION_MONITORING_FAILED("connection_monitoring_failed.ftl");

    //    public static ObjectProvider<DefaultConfigurationManager> defaultConfigurationManagerObjectProvider;
    public static ApplicationContext applicationContext;

    private String template;
    private String configurationProperty;
    private String title;
    public AlertConfigurationManager configurationManager;

    AlertType(String template) {
        this(template, null);
    }

    AlertType(String template, String configurationProperty) {
        setParams(template, configurationProperty);
    }

//    AlertType(String template, DefaultConfigurationManager configurationManager) {
//        setParams(template, null, configurationManager);
//    }

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
        if (configurationManager == null && StringUtils.isNotBlank(configurationProperty)) {
            configurationManager = (AlertConfigurationManager) applicationContext.getBean("defaultConfigurationManager", this, configurationProperty);
        }
//        if (configurationManager == null && StringUtils.isNotBlank(configurationProperty)) {
//            this.configurationManager = defaultConfigurationManagerObjectProvider.getObject(this, configurationProperty);
//        }
        return configurationManager;
    }

    public AlertModuleConfiguration getConfiguration() {
        AlertConfigurationManager confMan = getConfigurationManager();
        return confMan.getConfiguration();
    }

    private void setParams(String template, String configurationProperty) {
        this.template = template;
        this.configurationProperty = configurationProperty;
        this.configurationManager = null;
        this.title = this.name();
    }
}
