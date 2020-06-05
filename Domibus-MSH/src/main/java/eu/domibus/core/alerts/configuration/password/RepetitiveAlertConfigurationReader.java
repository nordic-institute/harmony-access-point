package eu.domibus.core.alerts.configuration.password;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.UserAuthenticationConfiguration;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_ACTIVE;

/**
 * Base code for reading of console and plugin password expiration alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
public abstract class RepetitiveAlertConfigurationReader implements UserAuthenticationConfiguration {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(RepetitiveAlertConfigurationReader.class);

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    protected abstract AlertType getAlertType();

    public RepetitiveAlertModuleConfiguration readConfiguration() {
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        final String moduleName = getAlertType().getTitle();
        final String property = getAlertType().getConfigurationProperty();
        try {
            if (shouldCheckExtAuthEnabled()) {
                LOG.debug("domain:[{}] [{}] module is inactive for the following reason: external authentication provider is enabled", domain, moduleName);
                return new RepetitiveAlertModuleConfiguration(getAlertType());
            }

            final Boolean alertModuleActive = isAlertModuleEnabled();
            final Boolean eventActive = Boolean.valueOf(domibusPropertyProvider.getProperty(property + ".active"));
            if (!alertModuleActive || !eventActive) {
                LOG.debug("domain:[{}] Alert {} module is inactive for the following reason: global alert module active[{}], event active[{}]",
                        domain, moduleName, alertModuleActive, eventActive);
                return new RepetitiveAlertModuleConfiguration(getAlertType());
            }

            final Integer delay = Integer.valueOf(domibusPropertyProvider.getProperty(property + ".delay_days"));
            final Integer frequency = Integer.valueOf(domibusPropertyProvider.getProperty(property + ".frequency_days"));
            final AlertLevel alertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(property + ".level"));
            final String mailSubject = domibusPropertyProvider.getProperty(property + ".mail.subject");

            LOG.info("Alert {} module activated for domain:[{}]", moduleName, domain);
            return new RepetitiveAlertModuleConfiguration(getAlertType(), delay, frequency, alertLevel, mailSubject);
        } catch (Exception e) {
            LOG.warn("An error occurred while reading {} alert module configuration for domain:[{}], ", moduleName, domain, e);
            return new RepetitiveAlertModuleConfiguration(getAlertType());
        }
    }

    private Boolean isAlertModuleEnabled() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
    }
}