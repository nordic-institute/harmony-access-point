package eu.domibus.core.earchive.alerts;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.ReaderMethodAlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.service.ConfigurationReader;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Manages the reading of e-archiving start date stopped alert configuration
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class ArchivingStartDateStoppedConfigurationManager
        extends ReaderMethodAlertConfigurationManager<ArchivingStartDateStoppedModuleConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ArchivingStartDateStoppedConfigurationManager.class);

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected DomainContextProvider domainContextProvider;

    public ArchivingStartDateStoppedConfigurationManager(DomibusPropertyProvider domibusPropertyProvider,
                                                         DomainContextProvider domainContextProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainContextProvider = domainContextProvider;
    }

    @Override
    public AlertType getAlertType() {
        return AlertType.ARCHIVING_START_DATE_STOPPED;
    }

    @Override
    protected ConfigurationReader<ArchivingStartDateStoppedModuleConfiguration> getReaderMethod() {
        return this::readConfiguration;
    }

    protected ArchivingStartDateStoppedModuleConfiguration readConfiguration() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertsActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            final Boolean earchiveAlertsActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_EARCHIVING_START_DATE_STOPPED_ACTIVE);
            if (BooleanUtils.isNotTrue(alertsActive) || BooleanUtils.isNotTrue(earchiveAlertsActive)) {
                return new ArchivingStartDateStoppedModuleConfiguration();
            }
            final AlertLevel alertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_EARCHIVING_START_DATE_STOPPED_LEVEL));
            final String mailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_EARCHIVING_START_DATE_STOPPED_MAIL_SUBJECT);

            return new ArchivingStartDateStoppedModuleConfiguration(alertLevel, mailSubject);
        } catch (Exception ex) {
            LOG.warn("Error while configuring alerts related to e-archiving continuous start date stopped for domain:[{}].", currentDomain, ex);
            return new ArchivingStartDateStoppedModuleConfiguration();
        }
    }
}