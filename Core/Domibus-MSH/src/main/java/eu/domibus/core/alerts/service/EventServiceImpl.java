package eu.domibus.core.alerts.service;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.user.UserEntityBase;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.DomibusAlertException;
import eu.domibus.core.alerts.configuration.connectionMonitoring.ConnectionMonitoringModuleConfiguration;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfiguration;
import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.AlertCategory;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.domibus.core.alerts.model.common.MessageEvent.*;
import static eu.domibus.jms.spi.InternalJMSConstants.ALERT_MESSAGE_QUEUE;

/**
 * @author Thomas Dussart
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class EventServiceImpl implements EventService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EventServiceImpl.class);

    public static final int MAX_DESCRIPTION_LENGTH = 255;

    public static final String EVENT_IDENTIFIER = "EVENT_IDENTIFIER";

    public static final String ALERT_JMS_LISTENER_CONTAINER_FACTORY = "alertJmsListenerContainerFactory";

    private final EventDao eventDao;

    private final PModeProvider pModeProvider;

    private final UserMessageDao userMessageDao;

    private final ErrorLogService errorLogService;

    private final EventMapper eventMapper;

    private final JMSManager jmsManager;

    private final Queue alertMessageQueue;

    protected final MpcService mpcService;

    private final AlertConfigurationService alertConfigurationService;

    public EventServiceImpl(EventDao eventDao, PModeProvider pModeProvider, UserMessageDao userMessageDao,
                            ErrorLogService errorLogService, EventMapper eventMapper, JMSManager jmsManager,
                            @Qualifier(ALERT_MESSAGE_QUEUE) Queue alertMessageQueue,
                            MpcService mpcService, AlertConfigurationService alertConfigurationService) {
        this.eventDao = eventDao;
        this.pModeProvider = pModeProvider;
        this.userMessageDao = userMessageDao;
        this.errorLogService = errorLogService;
        this.eventMapper = eventMapper;
        this.jmsManager = jmsManager;
        this.alertMessageQueue = alertMessageQueue;
        this.mpcService = mpcService;
        this.alertConfigurationService = alertConfigurationService;
    }

    @Override
    public void enqueueEvent(EventType eventType, String eventIdentifier, EventProperties eventProperties) {
        Event event = getEvent(eventType, eventIdentifier, eventProperties);
        if (event == null) {
            return;
        }

        enqueueEvent(event);
    }

    @Override
    public void enqueueMessageStatusChangedEvent(final String messageId, final MessageStatus oldStatus, final MessageStatus newStatus, final MSHRole role) {
        AlertType alertType = EventType.MSG_STATUS_CHANGED.geDefaultAlertType();
        MessagingModuleConfiguration configuration = (MessagingModuleConfiguration) alertConfigurationService.getConfiguration(alertType);
        if (!configuration.isActive()) {
            LOG.info("Messaging alerts module is not enabled, no alert will be created.");
            return;
        }

        if (!configuration.shouldMonitorMessageStatus(newStatus)) {
            LOG.info("[{}] status is not monitored, no alert will be created.", newStatus);
            return;
        }

        Event event = new Event(EventType.MSG_STATUS_CHANGED);
        event.addStringKeyValue(OLD_STATUS.name(), oldStatus.name());
        event.addStringKeyValue(NEW_STATUS.name(), newStatus.name());
        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
        event.addStringKeyValue(ROLE.name(), role.name());

        enrichMessageEvent(event);

        enqueueEvent(event);
    }

    @Override
    public void enqueueImminentCertificateExpirationEvent(final String alias, final Date expirationDate) {
        AlertType alertType = EventType.CERT_IMMINENT_EXPIRATION.geDefaultAlertType();
        AlertModuleConfiguration configuration = alertConfigurationService.getConfiguration(alertType);
        if (!configuration.isActive()) {
            LOG.info("[{}] alerts module is not enabled, no alert will be created.", alertType);
            return;
        }

        Event event = prepareCertificateEvent(EventType.CERT_IMMINENT_EXPIRATION, alias, expirationDate);
        enqueueEvent(event);
    }

    @Override
    public void enqueueCertificateExpiredEvent(final String alias, final Date expirationDate) {
        AlertType alertType = EventType.CERT_EXPIRED.geDefaultAlertType();
        AlertModuleConfiguration configuration = alertConfigurationService.getConfiguration(alertType);
        if (!configuration.isActive()) {
            LOG.info("[{}] alerts module is not enabled, no alert will be created.", alertType);
            return;
        }

        Event event = prepareCertificateEvent(EventType.CERT_EXPIRED, alias, expirationDate);
        enqueueEvent(event);
    }

    @Override
    public void enqueuePasswordExpirationEvent(EventType eventType, UserEntityBase user, Integer maxPasswordAgeInDays) {
        LocalDate expDate = user.getPasswordChangeDate().plusDays(maxPasswordAgeInDays).toLocalDate();
        EventProperties eventProperties = new EventProperties(user.getUserName(), user.getType().getName(), expDate);
        String eventIdentifier = getUniqueIdentifier(user);

        Event event = getEvent(eventType, eventIdentifier, eventProperties);
        if (event == null) {
            return;
        }

        enqueueEvent(event);

        LOG.securityInfo(eventType.getSecurityMessageCode(), user.getUserName(), event.findOptionalProperty("EXPIRATION_DATE"));
    }

    @Override
    public eu.domibus.core.alerts.model.persist.Event persistEvent(final Event event) {
        final eu.domibus.core.alerts.model.persist.Event eventEntity = eventMapper.eventServiceToEventPersist(event);
        LOG.debug("Converting jms event [{}] to persistent event [{}]", event, eventEntity);
        eventEntity.enrichProperties();
        eventDao.create(eventEntity);
        event.setEntityId(eventEntity.getEntityId());
        return eventEntity;
    }

    protected void enrichMessageEvent(final Event event) {
        final Optional<String> messageIdProperty = event.findStringProperty(MESSAGE_ID.name());
        final Optional<String> roleProperty = event.findStringProperty(ROLE.name());
        if (!messageIdProperty.isPresent() || !roleProperty.isPresent()) {
            LOG.error("Message id and role are mandatory for message event[{}].", event);
            throw new IllegalStateException("Message id and role are mandatory for message event.");
        }
        final String messageId = messageIdProperty.get();
        final MSHRole role = MSHRole.valueOf(roleProperty.get());
        final UserMessage userMessage = userMessageDao.findByMessageId(messageId, role);
        final MessageExchangeConfiguration userMessageExchangeContext;
        try {
            String errors = errorLogService
                    .getErrorsForMessage(messageId, role)
                    .stream()
                    .map(ErrorLogEntry::getErrorDetail)
                    .distinct()
                    .collect(Collectors.joining(" "));

            if (StringUtils.isNotBlank(errors)) {
                event.addStringKeyValue(DESCRIPTION.name(), StringUtils.truncate(errors, MAX_DESCRIPTION_LENGTH));
            }

            String receiverPartyName = null;
            if (mpcService.forcePullOnMpc(userMessage.getMpcValue())) {
                LOG.debug("Find UserMessage exchange context (pull context)");
                userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true);
                LOG.debug("Extract receiverPartyName from mpc");
                receiverPartyName = mpcService.extractInitiator(userMessage.getMpcValue());
            } else {
                LOG.debug("Find UserMessage exchange context");
                userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(userMessage, role);
                LOG.debug("Get receiverPartyName from exchange context pModeKey");
                receiverPartyName = pModeProvider.getReceiverParty(userMessageExchangeContext.getPmodeKey()).getName();
            }

            final Party senderParty = pModeProvider.getSenderParty(userMessageExchangeContext.getPmodeKey());
            LOG.debug("Create message event with receiverParty name: [{}], senderParty name: [{}]", receiverPartyName, senderParty.getName());
            event.addStringKeyValue(FROM_PARTY.name(), senderParty.getName());
            event.addStringKeyValue(TO_PARTY.name(), receiverPartyName);
        } catch (EbMS3Exception e) {
            LOG.error("Message:[{}] Errors while enriching message event", messageId, e);
        }
    }

    @Override
    public boolean shouldCreateAlert(eu.domibus.core.alerts.model.persist.Event entity, int frequency) {
        if (entity == null) {
            LOG.debug("Should create alert because the event was not previously persisted");
            return true;
        }

        LocalDate lastAlertDate = entity.getLastAlertDate();
        LocalDate notificationDate = LocalDate.now().minusDays(frequency);

        if (lastAlertDate == null) {
            LOG.debug("Should create alert for event [{}] because lastAlertDate == null", entity.getType());
            return true;
        }

        if (lastAlertDate.isBefore(notificationDate)) {
            LOG.debug("Should create alert for event [{}] because lastAlertDate is old enough [{}]", entity.getType(), entity.getLastAlertDate());
            return true; // last alert is old enough to send another one
        }

        LOG.debug("Should NOT create alert for event [{}] because lastAlertDate is not old enough [{}]", entity.getType(), entity.getLastAlertDate());
        return false;
    }

    @Override
    public eu.domibus.core.alerts.model.persist.Event getOrCreatePersistedEvent(Event event) {
        String id = event.findStringProperty(EVENT_IDENTIFIER).orElse("");
        eu.domibus.core.alerts.model.persist.Event entity = eventDao.findWithTypeAndPropertyValue(event.getType(), EVENT_IDENTIFIER, id);
        if (entity == null) {
            entity = this.persistEvent(event);
        }

        return entity;
    }

    @Override
    public void enqueueMonitoringEvent(String messageId, MSHRole role, MessageStatus messageStatus, MessageStatus newStatus, String fromParty, String toParty) {
        final ConnectionMonitoringModuleConfiguration connMonitorConfig = (ConnectionMonitoringModuleConfiguration) alertConfigurationService.getConfiguration(AlertType.CONNECTION_MONITORING_FAILED);
        if (!connMonitorConfig.shouldGenerateAlert(newStatus, toParty)) {
            LOG.debug("According to configuration, no event will be enqueued for status [{}] and party [{}]", newStatus, toParty);
            return;
        }

        enqueueEvent(EventType.CONNECTION_MONITORING_FAILED, toParty,
                new EventProperties(messageId, role.name(), messageStatus.name(), fromParty, toParty));
    }

    private void enqueueEvent(Event event) {
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, event.getType().getQueueSelector());
        LOG.debug("Event:[{}] added to the queue", event);
    }

    private Event prepareCertificateEvent(EventType eventType, String alias, Date expirationDate) {
        return createEventWithProperties(eventType, new EventProperties(alias, expirationDate));
    }

    private String getUniqueIdentifier(UserEntityBase user) {
        return user.getType().getCode() + "/" + user.getEntityId() + "/" + user.getPasswordChangeDate().toLocalDate();
    }

    private Event getEvent(EventType eventType, String eventIdentifier, EventProperties eventProperties) {
        AlertType alertType = eventType.geDefaultAlertType();
        AlertModuleConfiguration configuration = alertConfigurationService.getConfiguration(alertType);
        if (!configuration.isActive()) {
            LOG.info("[{}] alerts module is not enabled, no alert will be created.", alertType);
            return null;
        }

        Event event = createEventWithProperties(eventType, eventProperties);
        event.addStringKeyValue(EVENT_IDENTIFIER, eventIdentifier);

        if (alertType.getCategory() == AlertCategory.REPETITIVE) {
            eu.domibus.core.alerts.model.persist.Event entity = getOrCreatePersistedEvent(event);
            RepetitiveAlertConfiguration repetitiveConfiguration = (RepetitiveAlertConfiguration) configuration;
            if (!shouldCreateAlert(entity, repetitiveConfiguration.getFrequency())) {
                LOG.debug("Based on alert configuration [{}], the repetitive alert [{}] identified by event [{}] should not be fired now.",
                        configuration, alertType, eventIdentifier);
                return null;
            }

            entity.setLastAlertDate(LocalDate.now());
            eventDao.update(entity);
        }
        return event;
    }

    private Event createEventWithProperties(EventType eventType, EventProperties eventProperties) {
        Event event = new Event(eventType);

        if (eventProperties == null || eventProperties.get() == null
                || eventType.getProperties().size() != eventProperties.get().length) {
            throw new DomibusAlertException(String.format("List of actual params [%s] does not correspond to declared params [%s]",
                    Arrays.toString(Optional.ofNullable(eventProperties).map(EventProperties::get).orElse(null)), eventType.getProperties()));
        }
        for (int i = 0; i < eventProperties.get().length; i++) {
            String prop = eventType.getProperties().get(i);
            Object propValue = eventProperties.get()[i];
            if (propValue == null) {
                LOG.info("Property [{}] is null; skipping", prop);
                continue;
            }
            if (propValue instanceof String) {
                event.addStringKeyValue(prop, (String) propValue);
            } else if (propValue instanceof Date) {
                event.addDateKeyValue(prop, (Date) propValue);
            } else if (propValue instanceof Boolean) {
                event.addStringKeyValue(prop, Boolean.toString((Boolean) propValue));
            } else {
                LOG.info("Unexpected parameter [{}] value [{}] for event [{}]; stringify-ing it.", prop, propValue, event);
                event.addStringKeyValue(prop, propValue.toString());
            }
        }
        return event;
    }
}
