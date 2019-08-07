package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.model.service.LoginFailureModuleConfiguration;
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
public class AlertLoginFailureConfigurationChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Autowired
    private ConfigurationLoader<LoginFailureModuleConfiguration> loginFailureConfigurationLoader;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithAny(propertyName,
                "domibus.alert.user.login_failure.",
                "domibus.alert.super.user.login_failure.");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);

        loginFailureConfigurationLoader.resetConfiguration(domain);
    }

}
