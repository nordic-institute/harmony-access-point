package eu.domibus.core.earchive.alerts;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.ReaderMethodAlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.alerts.service.ConfigurationReader;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Manages the reading of e-archiving message non-final alert configuration
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class ArchivingMessagesNonFinalStatusConfigurationManager
        extends ReaderMethodAlertConfigurationManager<ArchivingMessagesNonFinalModuleConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ArchivingMessagesNonFinalStatusConfigurationManager.class);

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected DomainContextProvider domainContextProvider;

    protected AlertConfigurationService alertConfigurationService;

    public ArchivingMessagesNonFinalStatusConfigurationManager(DomibusPropertyProvider domibusPropertyProvider,
                                                               DomainContextProvider domainContextProvider,
                                                               AlertConfigurationService alertConfigurationService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainContextProvider = domainContextProvider;
        this.alertConfigurationService = alertConfigurationService;
    }

    @Override
    public AlertType getAlertType() {
        return AlertType.ARCHIVING_MESSAGES_NON_FINAL;
    }

    @Override
    protected ConfigurationReader<ArchivingMessagesNonFinalModuleConfiguration> getReaderMethod() {
        return this::readConfiguration;
    }

    protected ArchivingMessagesNonFinalModuleConfiguration readConfiguration() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertsActive = alertConfigurationService.isAlertModuleEnabled();
            final Boolean earchiveAlertsActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_ACTIVE);
            if (BooleanUtils.isNotTrue(alertsActive) || BooleanUtils.isNotTrue(earchiveAlertsActive)) {
                return new ArchivingMessagesNonFinalModuleConfiguration();
            }
            final AlertLevel alertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_LEVEL));
            final String mailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_MAIL_SUBJECT);

            return new ArchivingMessagesNonFinalModuleConfiguration(alertLevel, mailSubject);
        } catch (Exception ex) {
            LOG.warn("Error while configuring alerts related to e-archiving message non-final for domain:[{}].", currentDomain, ex);
            return new ArchivingMessagesNonFinalModuleConfiguration();
        }
    }
}