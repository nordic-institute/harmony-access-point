package eu.domibus.core.alerts.configuration.account.enabled;

import eu.domibus.core.alerts.configuration.account.enabled.AccountEnabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Reader of plugin user account enabled alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginAccountEnabledConfigurationReader extends AccountEnabledConfigurationReader {

    @Override
    public AlertType getAlertType() {
        return AlertType.PLUGIN_USER_ACCOUNT_ENABLED;
    }

    @Override
    protected String getModuleName() {
        return "Alert plugin account enabled";
    }

    @Override
    protected String getAlertActivePropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_ACTIVE;
    }

    @Override
    protected String getAlertLevelPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_LEVEL;
    }

    @Override
    protected String getAlertEmailSubjectPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_SUBJECT;
    }
}
