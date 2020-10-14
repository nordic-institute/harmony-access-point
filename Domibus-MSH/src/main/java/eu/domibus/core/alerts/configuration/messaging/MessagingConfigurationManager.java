package eu.domibus.core.alerts.configuration.messaging;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.ReaderMethodAlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.alerts.service.ConfigurationReader;
import eu.domibus.logging.DomibusLoggerFactory;
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
public class MessagingConfigurationManager
        extends ReaderMethodAlertConfigurationManager<MessagingModuleConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(MessagingConfigurationManager.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    AlertConfigurationService alertConfigurationService;

    @Override
    public AlertType getAlertType() {
        return AlertType.MSG_STATUS_CHANGED;
    }

    @Override
    protected ConfigurationReader<MessagingModuleConfiguration> getReaderMethod() {
        return this::readConfiguration;
    }

    protected MessagingModuleConfiguration readConfiguration() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertActive = alertConfigurationService.isAlertModuleEnabled();
            final Boolean messageAlertActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE);
            final String mailSubject = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT);

            if (!alertActive || !messageAlertActive) {
                LOG.debug("domain:[{}] Alert message status change module is inactive for the following reason:global alert module active[{}], message status change module active[{}]",
                        currentDomain, alertActive, messageAlertActive);
                return new MessagingModuleConfiguration(mailSubject);
            }
            final String messageCommunicationStates = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES);
            final String messageCommunicationLevels = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL);

            if (StringUtils.isEmpty(messageCommunicationStates) || StringUtils.isEmpty(messageCommunicationLevels)) {
                LOG.warn("Message status change alert module misconfiguration -> states[{}], levels[{}]", messageCommunicationStates, messageCommunicationLevels);
                return new MessagingModuleConfiguration();
            }
            final String[] states = messageCommunicationStates.split(",");
            final String[] trimmedStates = Arrays.stream(states).filter(state -> StringUtils.isNotBlank(state)).map(state -> StringUtils.trim(state)).distinct().toArray(String[]::new);

            final String[] levels = messageCommunicationLevels.split(",");
            final String[] trimmedLevels = Arrays.stream(levels).filter(level -> StringUtils.isNotBlank(level)).map(level -> StringUtils.trim(level)).distinct().toArray(String[]::new);

            final boolean eachStatusHasALevel = (states.length == levels.length);
            LOG.debug("Each message status has his own level[{}]", eachStatusHasALevel);

            MessagingModuleConfiguration messagingConfiguration = new MessagingModuleConfiguration(mailSubject);
            IntStream.
                    range(0, trimmedStates.length).
                    mapToObj(i -> new AbstractMap.SimpleImmutableEntry<>(MessageStatus.valueOf(trimmedStates[i]), AlertLevel.valueOf(trimmedLevels[eachStatusHasALevel ? i : 0]))).
                    forEach(entry -> messagingConfiguration.addStatusLevelAssociation(entry.getKey(), entry.getValue())); //NOSONAR
            LOG.info("Alert message status change module activated for domain:[{}]", currentDomain);
            return messagingConfiguration;
        } catch (Exception ex) {
            LOG.warn("Error while configuring message communication alerts for domain:[{}], message alert module will be discarded.", currentDomain, ex);
            return new MessagingModuleConfiguration();
        }

    }
}
