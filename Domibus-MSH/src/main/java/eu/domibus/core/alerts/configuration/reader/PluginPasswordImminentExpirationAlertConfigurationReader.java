package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.stereotype.Service;

/**
 * Reader of plugin user password imminent expiration alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginPasswordImminentExpirationAlertConfigurationReader extends RepetitiveAlertConfigurationReader {
    @Override
    public AlertType getAlertType() {
        return AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return false;
    }
}