package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to login failure configuration for console users
 */
@Service
public class AlertAccountDisabledConfigurationChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> accountDisabledConfigurationLoader;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithAny(propertyName,
                "domibus.alert.user.account_disabled.",
                "domibus.alert.super.user.account_disabled.");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);

        accountDisabledConfigurationLoader.resetConfiguration(domain);
    }
}
