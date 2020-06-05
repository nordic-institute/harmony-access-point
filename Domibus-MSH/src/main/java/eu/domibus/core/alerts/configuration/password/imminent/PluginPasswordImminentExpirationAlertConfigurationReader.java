package eu.domibus.core.alerts.configuration.password.imminent;

import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.stereotype.Service;

/**
 * Reader of plugin user password imminent expiration alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginPasswordImminentExpirationAlertConfigurationReader extends PasswordExpirationAlertConfigurationReader {
    @Override
    public AlertType getAlertType() {
        return AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return false;
    }
}