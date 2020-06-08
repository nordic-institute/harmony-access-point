package eu.domibus.core.alerts.configuration.certificate;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Manages the reading of expired certificate alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ExpiredCertificateConfigurationManager implements AlertConfigurationManager {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(ExpiredCertificateConfigurationManager.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private ConfigurationLoader<ExpiredCertificateModuleConfiguration> loader;

    @Autowired
    AlertConfigurationService alertConfigurationService;

    @Override
    public AlertType getAlertType() {
        return AlertType.CERT_EXPIRED;
    }

    @Override
    public ExpiredCertificateModuleConfiguration getConfiguration() {
        return loader.getConfiguration(this::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }

    protected ExpiredCertificateModuleConfiguration readConfiguration() {
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertActive = alertConfigurationService.isAlertModuleEnabled();
            final Boolean expiredActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE);
            if (!alertActive || !expiredActive) {
                LOG.debug("domain:[{}] Alert certificate expired module is inactive for the following reason:global alert module active[{}], certificate expired module active[{}]",
                        domain, alertActive, expiredActive);
                return new ExpiredCertificateModuleConfiguration();
            }
            final Integer revokedFrequency = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS);
            final Integer revokedDuration = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS);
            final AlertLevel revocationLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_LEVEL));
            final String expiredMailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT);

            LOG.info("Alert certificate expired activated for domain:[{}]", domain);
            return new ExpiredCertificateModuleConfiguration(
                    revokedFrequency,
                    revokedDuration,
                    revocationLevel,
                    expiredMailSubject);

        } catch (Exception e) {
            LOG.error("An error occurred while reading certificate scanner alert module configuration for domain:[{}], ", domain, e);
            return new ExpiredCertificateModuleConfiguration();
        }
    }

}
