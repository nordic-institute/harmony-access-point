package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.stereotype.Service;

@Service
public class PluginPasswordExpiredAlertConfigurationReader extends RepetitiveAlertConfigurationReader {
    @Override
    public AlertType getAlertType() {
        return AlertType.PLUGIN_PASSWORD_EXPIRED;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return false;
    }
}
