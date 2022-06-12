package eu.domibus.core.alerts.configuration.partitions;

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

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_PARTITION_CHECK_PREFIX_FREQUENCY_DAYS;

/**
 * Manages the reading of partition alerts configuration
 *
 * @author idragusa
 * @since 5.0
 */
@Service
public class PartitionsConfigurationManager
        extends ReaderMethodAlertConfigurationManager<PartitionsModuleConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PartitionsConfigurationManager.class);

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected DomainContextProvider domainContextProvider;

    protected AlertConfigurationService alertConfigurationService;


    public PartitionsConfigurationManager(DomibusPropertyProvider domibusPropertyProvider,
                                          DomainContextProvider domainContextProvider,
                                          AlertConfigurationService alertConfigurationService) {
        super();
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainContextProvider = domainContextProvider;
        this.alertConfigurationService = alertConfigurationService;
    }

    @Override
    public AlertType getAlertType() {
        return AlertType.PARTITION_CHECK;
    }

    @Override
    protected ConfigurationReader<PartitionsModuleConfiguration> getReaderMethod() {
        return this::readConfiguration;
    }

    protected PartitionsModuleConfiguration readConfiguration() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertActive = alertConfigurationService.isAlertModuleEnabled();
            final Integer frequency = Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PARTITION_CHECK_PREFIX_FREQUENCY_DAYS));
            if (BooleanUtils.isFalse(alertActive)) {
                return new PartitionsModuleConfiguration();
            }
            return new PartitionsModuleConfiguration(AlertLevel.HIGH, null, frequency);
        } catch (Exception ex) {
            LOG.warn("Error while configuring alerts related to deleting partitions for domain:[{}].", currentDomain, ex);
            return new PartitionsModuleConfiguration();
        }
    }
}
