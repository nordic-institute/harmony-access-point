package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.core.alerts.model.common.AlertType;

public class PluginPasswordExpiredAlertConfigurationReader extends RepetitiveAlertConfigurationReader {
    @Override
    protected AlertType getAlertType() {
        return AlertType.PLUGIN_PASSWORD_EXPIRED;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return false;
    }
}
