package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.core.alerts.model.common.AlertType;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

public class PluginAccountDisabledConfigurationReader extends AccountDisabledConfigurationReader {

    @Override
    protected AlertType getAlertType() {
        return AlertType.PLUGIN_USER_ACCOUNT_DISABLED;
    }

    @Override
    protected String getModuleName() {
        return "Alert plugin account disabled";
    }

    @Override
    protected String getAlertActivePropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_ACTIVE;
    }

    @Override
    protected String getAlertLevelPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_LEVEL;
    }

    @Override
    protected String getAlertMomentPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_MOMENT;
    }

    @Override
    protected String getAlertEmailSubjectPropertyName() {
        return DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_SUBJECT;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return false;
    }
}