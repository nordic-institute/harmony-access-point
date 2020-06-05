package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.alerts.configuration.login.PluginLoginFailConfigurationManager;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to login failure configuration for plugin users
 */
@Service
public class AlertPluginLoginFailureConfigurationChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private PluginLoginFailConfigurationManager pluginLoginFailConfigurationManager;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        pluginLoginFailConfigurationManager.getConfiguration();
    }

}
