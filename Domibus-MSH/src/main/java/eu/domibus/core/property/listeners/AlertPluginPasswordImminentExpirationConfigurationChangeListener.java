package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.alerts.configuration.password.imminent.plugin.PluginPasswordImminentExpirationAlertConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to configuration of plugin password imminent expiration alerts
 */
@Service
public class AlertPluginPasswordImminentExpirationConfigurationChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private PluginPasswordImminentExpirationAlertConfigurationManager pluginPasswordImminentExpirationAlertConfigurationManager;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        pluginPasswordImminentExpirationAlertConfigurationManager.reset();
    }
}

