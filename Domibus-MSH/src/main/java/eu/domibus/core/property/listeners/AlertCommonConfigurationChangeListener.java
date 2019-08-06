package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.alerts.model.service.CommonConfiguration;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to common configuration
 */
@Service
public class AlertCommonConfigurationChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Autowired
    private ConfigurationLoader<CommonConfiguration> commonConfigurationConfigurationLoader;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName,
                "domibus.alert.mail.sending.active",
                "domibus.alert.super.mail.sending.active",
                "domibus.alert.sender.email", "domibus.alert.super.sender.email",
                "domibus.alert.receiver.email", "domibus.alert.super.receiver.email",
                "domibus.alert.cleaner.alert.lifetime",
                "domibus.alert.super.cleaner.alert.lifetime");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);

        commonConfigurationConfigurationLoader.resetConfiguration(domain);
    }

}
