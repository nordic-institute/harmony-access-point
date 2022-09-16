package eu.domibus.core.alerts.configuration.connectionMonitpring;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.ReaderMethodAlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.alerts.service.ConfigurationReader;
import eu.domibus.core.monitoring.ConnectionMonitoringService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Manages the reading of connection monitoring notification failure properties
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Service
public class ConnectionMonitoringConfigurationManager
        extends ReaderMethodAlertConfigurationManager<ConnectionMonitoringModuleConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringConfigurationManager.class);

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final DomainContextProvider domainContextProvider;

    protected final AlertConfigurationService alertConfigurationService;

    public ConnectionMonitoringConfigurationManager(DomibusPropertyProvider domibusPropertyProvider,
                                                    DomainContextProvider domainContextProvider,
                                                    AlertConfigurationService alertConfigurationService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainContextProvider = domainContextProvider;
        this.alertConfigurationService = alertConfigurationService;
    }

    @Override
    public AlertType getAlertType() {
        return AlertType.CONNECTION_MONITORING_FAILED;
    }

    @Override
    protected ConfigurationReader<ConnectionMonitoringModuleConfiguration> getReaderMethod() {
        return this::readConfiguration;
    }

    protected ConnectionMonitoringModuleConfiguration readConfiguration() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertsActive = alertConfigurationService.isAlertModuleEnabled();
            if (BooleanUtils.isNotTrue(alertsActive)) {
                LOG.info("No connection monitoring notifications for domain:[{}] because the alerts are not enabled", currentDomain);
                return new ConnectionMonitoringModuleConfiguration();
            }

            List<String> enabledParties = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES);
            if (CollectionUtils.isEmpty(enabledParties)) {
                LOG.info("No connection monitoring notifications for domain:[{}] because the enabled parties list is empty.", currentDomain);
                return new ConnectionMonitoringModuleConfiguration();
            }

            final int frequency = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_FREQUENCY_DAYS);
            final AlertLevel alertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_LEVEL));
            final String mailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_MAIL_SUBJECT);

            return new ConnectionMonitoringModuleConfiguration(frequency, alertLevel, mailSubject, enabledParties);
        } catch (Exception ex) {
            LOG.warn("Error while configuring alerts related to connection monitoring notifications for domain:[{}].", currentDomain, ex);
            return new ConnectionMonitoringModuleConfiguration();
        }
    }
}
