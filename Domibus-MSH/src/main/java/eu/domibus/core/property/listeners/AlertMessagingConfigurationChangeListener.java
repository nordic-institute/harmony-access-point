package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyMetadataManager;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.model.service.MessagingModuleConfiguration;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to messaging configuration
 */
@Service
public class AlertMessagingConfigurationChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Autowired
    private ConfigurationLoader<MessagingModuleConfiguration> messagingConfigurationLoader;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManager.DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);

        messagingConfigurationLoader.resetConfiguration(domain);
    }

}
