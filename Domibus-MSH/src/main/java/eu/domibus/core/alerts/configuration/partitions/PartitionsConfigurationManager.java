package eu.domibus.core.alerts.configuration.partitions;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.model.MessageStatus;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.stream.IntStream;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Manages the reading of messaging alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PartitionsConfigurationManager
        extends ReaderMethodAlertConfigurationManager<PartitionsModuleConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PartitionsConfigurationManager.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    AlertConfigurationService alertConfigurationService;

    @Override
    public AlertType getAlertType() {
        return AlertType.PARTITION_EXPIRATION;
    }

    @Override
    protected ConfigurationReader<PartitionsModuleConfiguration> getReaderMethod() {
        return this::readConfiguration;
    }

    protected PartitionsModuleConfiguration readConfiguration() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertActive = alertConfigurationService.isAlertModuleEnabled();
            final Integer frequency = Integer.valueOf(domibusPropertyProvider.getProperty(DOMIBUS_ALERT_PARTITION_EXPIRATION_PREFIX_FREQUENCY_DAYS));
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
