package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.core.alerts.model.common.AlertType;

public class PluginPasswordImminentExpirationAlertConfigurationReader extends RepetitiveAlertConfigurationReader {
    @Override
    protected AlertType getAlertType() {
        return AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return false;
    }
}