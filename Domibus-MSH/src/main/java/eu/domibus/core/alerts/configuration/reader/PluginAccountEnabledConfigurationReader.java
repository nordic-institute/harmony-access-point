package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.core.alerts.model.common.AlertType;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

public class PluginAccountEnabledConfigurationReader extends AccountEnabledConfigurationReader {

    @Override
    protected AlertType getAlertType() {
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
