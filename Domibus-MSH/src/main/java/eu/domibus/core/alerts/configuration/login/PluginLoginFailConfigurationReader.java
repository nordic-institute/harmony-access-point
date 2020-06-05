package eu.domibus.core.alerts.configuration.login;

import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Reader of plugin user login fail alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginLoginFailConfigurationReader extends LoginFailConfigurationReader {
    @Override
    public AlertType getAlertType() {
        return AlertType.PLUGIN_USER_LOGIN_FAILURE;
    }

    @Override
    protected String getModuleName() {
        return "Alert Plugin Login failure";
    }

    @Override
    protected String getAlertActivePropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE;
    }

    @Override
    protected String getAlertLevelPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_LEVEL;
    }

    @Override
    protected String getAlertEmailSubjectPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_MAIL_SUBJECT;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return false;
    }
}
