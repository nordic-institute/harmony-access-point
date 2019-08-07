package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.model.service.*;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_ALERT_ACTIVE;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert enabled property
 */
@Service
public class AlertActiveChangeListener implements PluginPropertyChangeListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertActiveChangeListener.class);

    @Autowired
    private MailSender mailSender;

    @Autowired
    protected DomainService domainService;

    @Autowired
    private ConfigurationLoader<MessagingModuleConfiguration> messagingConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> accountDisabledConfigurationLoader;

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> pluginAccountDisabledConfigurationLoader;

    @Autowired
    private ConfigurationLoader<LoginFailureModuleConfiguration> loginFailureConfigurationLoader;

    @Autowired
    private ConfigurationLoader<LoginFailureModuleConfiguration> pluginLoginFailureConfigurationLoader;

    @Autowired
    private ConfigurationLoader<ImminentExpirationCertificateModuleConfiguration> imminentExpirationCertificateConfigurationLoader;

    @Autowired
    private ConfigurationLoader<ExpiredCertificateModuleConfiguration> expiredCertificateConfigurationLoader;

    @Autowired
    private ConfigurationLoader<CommonConfiguration> commonConfigurationConfigurationLoader;

    @Autowired
    private RepetitiveAlertConfigurationHolder passwordExpirationAlertsConfigurationHolder;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName, DOMIBUS_ALERT_ACTIVE, "domibus.alert.super.active");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.debug("Global active property for alerts has cjanged for domain [{}]. Reseting all configuration loaders.", domainCode);

        mailSender.reset();

        final Domain domain = domainService.getDomain(domainCode);
        messagingConfigurationLoader.resetConfiguration(domain);
        accountDisabledConfigurationLoader.resetConfiguration(domain);
        pluginAccountDisabledConfigurationLoader.resetConfiguration(domain);
        loginFailureConfigurationLoader.resetConfiguration(domain);
        pluginLoginFailureConfigurationLoader.resetConfiguration(domain);
        imminentExpirationCertificateConfigurationLoader.resetConfiguration(domain);
        expiredCertificateConfigurationLoader.resetConfiguration(domain);
        commonConfigurationConfigurationLoader.resetConfiguration(domain);
        passwordExpirationAlertsConfigurationHolder.resetConfiguration(domain);
    }

}
