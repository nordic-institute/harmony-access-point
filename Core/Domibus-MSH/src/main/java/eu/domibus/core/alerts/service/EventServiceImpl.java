package eu.domibus.core.alerts.service;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.user.UserEntityBase;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.common.DomibusAlertException;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfiguration;
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

//    @Override
//    // de sters?? param de identif default???
//    public void enqueueEvent(EventType eventType, EventProperties eventProperties) {
//        Event event = createEventWithProperties(eventType, eventProperties);
//        enqueueEvent(event);
//    }

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
        Event event = createEventWithProperties(EventType.MSG_STATUS_CHANGED, new EventProperties(messageId, oldStatus.name(), newStatus.name(), role.name()));

//        Event event = new Event(EventType.MSG_STATUS_CHANGED);
//        event.addStringKeyValue(OLD_STATUS.name(), oldStatus.name());
//        event.addStringKeyValue(NEW_STATUS.name(), newStatus.name());
//        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
//        event.addStringKeyValue(ROLE.name(), role.name());

        enrichMessageEvent(event);

        enqueueEvent(event);
    }

//    @Override
//    public void enqueueConnectionMonitoringEvent(String messageId, MSHRole role, MessageStatus status, String fromParty, String toParty, int frequency) {
//        Event event = new Event(EventType.CONNECTION_MONITORING_FAILED);
//        event.setReportingTime(new Date());
//        event.addStringKeyValue(EVENT_IDENTIFIER, toParty);
//
//        event.addStringKeyValue(ConnectionMonitoringFailedEventProperties.MESSAGE_ID.name(), messageId);
//        event.addStringKeyValue(ConnectionMonitoringFailedEventProperties.ROLE.name(), role.name());
//        event.addStringKeyValue(ConnectionMonitoringFailedEventProperties.FROM_PARTY.name(), fromParty);
//        event.addStringKeyValue(ConnectionMonitoringFailedEventProperties.TO_PARTY.name(), toParty);
//        event.addStringKeyValue(ConnectionMonitoringFailedEventProperties.STATUS.name(), status.name());
//
//        eu.domibus.core.alerts.model.persist.Event entity = getPersistedEvent(event, EVENT_IDENTIFIER);
//        if (!shouldCreateAlert(entity, frequency)) {
//            return;
//        }
//
//        entity.setLastAlertDate(LocalDate.now());
//        eventDao.update(entity);
//
//        enqueueEvent(event);
//    }
//    @Override
//    public void enqueueLoginFailureEvent(UserEntityBase.Type userType, final String userName, final Date loginTime, final boolean accountDisabled) {
//        EventType eventType = userType == UserEntityBase.Type.CONSOLE ? EventType.USER_LOGIN_FAILURE : EventType.PLUGIN_USER_LOGIN_FAILURE;
//        enqueueEvent(prepareAccountEvent(eventType, userName, userType.getName(), loginTime, Boolean.toString(accountDisabled), AccountEventKey.ACCOUNT_DISABLED));
//    }
//    @Override
//    public void enqueueAccountDisabledEvent(UserEntityBase.Type userType, final String userName, final Date accountDisabledTime) {
//        EventType eventType = userType == UserEntityBase.Type.CONSOLE ? EventType.USER_ACCOUNT_DISABLED : EventType.PLUGIN_USER_ACCOUNT_DISABLED;
//        enqueueEvent(prepareAccountEvent(eventType, userName, userType.getName(), accountDisabledTime, Boolean.toString(true), AccountEventKey.ACCOUNT_DISABLED));
//    }
//    @Override
//    public void enqueueAccountEnabledEvent(UserEntityBase.Type userType, String userName, Date accountEnabledTime) {
//        EventType eventType = userType == UserEntityBase.Type.CONSOLE ? EventType.USER_ACCOUNT_ENABLED : EventType.PLUGIN_USER_ACCOUNT_ENABLED;
//        enqueueEvent(prepareAccountEvent(eventType, userName, userType.getName(), accountEnabledTime, Boolean.toString(true), AccountEventKey.ACCOUNT_ENABLED));
//    }

    @Override
    public void enqueueImminentCertificateExpirationEvent(final String accessPoint, final String alias, final Date expirationDate) {
        Event event = prepareCertificateEvent(EventType.CERT_IMMINENT_EXPIRATION, accessPoint, alias, expirationDate);
        enqueueEvent(event);
    }

    @Override
    public void enqueueCertificateExpiredEvent(final String accessPoint, final String alias, final Date expirationDate) {
        Event event = prepareCertificateEvent(EventType.CERT_EXPIRED, accessPoint, alias, expirationDate);
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

//        Event event = preparePasswordEvent(user, eventType, maxPasswordAgeInDays);
//
//        eu.domibus.core.alerts.model.persist.Event entity = getPersistedEvent(event);
//        if (!shouldCreateAlert(entity, frequency)) {
//            return;
//        }
//
//        entity.setLastAlertDate(LocalDate.now());
//        eventDao.update(entity);
//
//        jmsManager.convertAndSendToQueue(event, alertMessageQueue, eventType.getQueueSelector());

        enqueueEvent(event);

        LOG.securityInfo(eventType.getSecurityMessageCode(), user.getUserName(), event.findOptionalProperty("EXPIRATION_DATE"));
    }

//    @Override
//    public void enqueuePartitionCheckEvent(String partitionName) {
//        Event event = new Event(EventType.PARTITION_CHECK);
//        event.addStringKeyValue(PartitionCheckEvent.PARTITION_NAME.name(), partitionName);
//        enqueueEvent(event);
//    }
//    @Override
//    public void enqueueEArchivingEvent(String batchId, EArchiveBatchStatus batchStatus) {
//        Event event = new Event(EventType.ARCHIVING_NOTIFICATION_FAILED);
//        event.addStringKeyValue(ArchivingEventProperties.BATCH_ID.name(), batchId);
//        event.addStringKeyValue(ArchivingEventProperties.BATCH_STATUS.name(), batchStatus.name());
//        enqueueEvent(event);
//    }

    @Override
    public eu.domibus.core.alerts.model.persist.Event persistEvent(final Event event) {
        final eu.domibus.core.alerts.model.persist.Event eventEntity = eventMapper.eventServiceToEventPersist(event);
        LOG.debug("Converting jms event [{}] to persistent event [{}]", event, eventEntity);
        eventEntity.enrichProperties();
        eventDao.create(eventEntity);
        event.setEntityId(eventEntity.getEntityId());
        return eventEntity;
    }

    private void enrichMessageEvent(final Event event) {
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

    private void enqueueEvent(Event event) {
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, event.getType().getQueueSelector());
        LOG.debug("Event:[{}] added to the queue", event);
    }

    private Event prepareCertificateEvent(EventType eventType, String accessPoint, String alias, Date expirationDate) {
        Event event = createEventWithProperties(eventType, new EventProperties(accessPoint, alias, expirationDate));

//        Event event = new Event(eventType);
//        event.addStringKeyValue(CertificateEvent.ACCESS_POINT.name(), accessPoint);
//        event.addStringKeyValue(CertificateEvent.ALIAS.name(), alias);
//        event.addDateKeyValue(CertificateEvent.EXPIRATION_DATE.name(), expirationDate);
        return event;
    }

//    private Event prepareAccountEvent(final EventType eventType, final String userName, final String userType, final Date loginTime, final String value, final AccountEventKey key) {
//        Event event = new Event(eventType);
//        event.addAccountKeyValue(USER, userName);
//        event.addAccountKeyValue(USER_TYPE, userType);
//        event.addDateKeyValue(LOGIN_TIME.name(), loginTime);
//        event.addAccountKeyValue(key, value);
//        return event;
//    }

    @Override
    public eu.domibus.core.alerts.model.persist.Event getOrCreatePersistedEvent(Event event) {
        String id = event.findStringProperty(EVENT_IDENTIFIER).orElse("");
        eu.domibus.core.alerts.model.persist.Event entity = eventDao.findWithTypeAndPropertyValue(event.getType(), EVENT_IDENTIFIER, id);
        if (entity == null) {
            entity = this.persistEvent(event);
        }

        return entity;
    }

//    private Event preparePasswordEvent(UserEntityBase user, EventType eventType, Integer maxPasswordAgeInDays) {
//        Event event = new Event(eventType);
//        event.setReportingTime(new Date());
//
//        event.addStringKeyValue(EVENT_IDENTIFIER, getUniqueIdentifier(user));
//        event.addStringKeyValue(PasswordExpirationEventProperties.USER_TYPE.name(), user.getType().getName());
//        event.addStringKeyValue(PasswordExpirationEventProperties.USER.name(), user.getUserName());
//
//        LocalDate expDate = user.getPasswordChangeDate().plusDays(maxPasswordAgeInDays).toLocalDate();
//        event.addDateKeyValue(PasswordExpirationEventProperties.EXPIRATION_DATE.name(), Date.from(expDate.atStartOfDay(ZoneOffset.UTC).toInstant()));
//
//        return event;
//    }
//    @Override
//    public void enqueueEArchivingMessageNonFinalEvent(final String messageId, final MessageStatus status) {
//        Event event = new Event(EventType.ARCHIVING_MESSAGES_NON_FINAL);
//        event.addStringKeyValue(OLD_STATUS.name(), status.name());
//        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
//        enqueueEvent(event);
//    }
//    @Override
//    public void enqueueEArchivingStartDateStopped() {
//        Event event = new Event(EventType.ARCHIVING_START_DATE_STOPPED);
//        enqueueEvent(event);
//    }

    private String getUniqueIdentifier(UserEntityBase user) {
        return user.getType().getCode() + "/" + user.getEntityId() + "/" + user.getPasswordChangeDate().toLocalDate();
    }

    // revise and rename??
    private Event getEvent(EventType eventType, String eventIdentifier, EventProperties eventProperties) {
        Event event = createEventWithProperties(eventType, eventProperties);
        event.addStringKeyValue(EVENT_IDENTIFIER, eventIdentifier);

        AlertType alertType = eventType.geDefaultAlertType();
        if (alertType.getCategory() == AlertCategory.REPETITIVE) {
            eu.domibus.core.alerts.model.persist.Event entity = getOrCreatePersistedEvent(event);
            RepetitiveAlertConfiguration configuration = (RepetitiveAlertConfiguration) alertConfigurationService.getConfiguration(alertType);
            if (!shouldCreateAlert(entity, configuration.getFrequency())) {
                LOG.debug("Based on alert configuration [{}], the repetitive alert [{}] identified by event [{}] should not be fired now.", configuration, alertType, eventIdentifier);
                return null;
            }

            entity.setLastAlertDate(LocalDate.now());
            eventDao.update(entity);
        }
        return event;
    }

    private Event createEventWithProperties(EventType eventType, EventProperties eventProperties) {
        Event event = new Event(eventType);

        if (eventType.getProperties().size() != eventProperties.get().length) {
            throw new DomibusAlertException(String.format("List of actual params [%s] does not correspond to declared params [%s]",
                    Arrays.toString(eventProperties.get()), eventType.getProperties()));
        }
        for (int i = 0; i < eventType.getProperties().size(); i++) {
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
