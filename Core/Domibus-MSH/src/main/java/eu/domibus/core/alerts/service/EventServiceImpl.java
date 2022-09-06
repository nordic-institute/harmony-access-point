package eu.domibus.core.alerts.service;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.user.UserEntityBase;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.*;
import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.alerts.model.service.Event;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.domibus.core.alerts.model.common.AccountEventKey.*;
import static eu.domibus.core.alerts.model.common.MessageEvent.*;
import static eu.domibus.jms.spi.InternalJMSConstants.ALERT_MESSAGE_QUEUE;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class EventServiceImpl implements EventService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EventServiceImpl.class);

    private static final String EVENT_ADDED_TO_THE_QUEUE = "Event:[{}] added to the queue";

    public static final int MAX_DESCRIPTION_LENGTH = 255;

    public static final String EVENT_IDENTIFIER = "EVENT_IDENTIFIER";

    @Autowired
    private EventDao eventDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    private ErrorLogService errorLogService;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private JMSManager jmsManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier(ALERT_MESSAGE_QUEUE)
    private Queue alertMessageQueue;

    @Autowired
    protected MpcService mpcService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueMessageEvent(final String messageId, final MessageStatus oldStatus, final MessageStatus newStatus, final MSHRole role) {
        Event event = new Event(EventType.MSG_STATUS_CHANGED);
        event.addStringKeyValue(OLD_STATUS.name(), oldStatus.name());
        event.addStringKeyValue(NEW_STATUS.name(), newStatus.name());
        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
        event.addStringKeyValue(ROLE.name(), role.name());

        enqueueEvent(event);
    }

    @Override
    // add from and to parties
    public void enqueueConnectionMonitoringEvent(String messageId, MSHRole role, MessageStatus status) {
        Event event = new Event(EventType.CONNECTION_MONITORING_FAILED);
        event.addStringKeyValue(ConnectionMonitoringFailedEventProperties.MESSAGE_ID.name(), messageId);
        event.addStringKeyValue(ConnectionMonitoringFailedEventProperties.ROLE.name(), role.name());
        event.addStringKeyValue(ConnectionMonitoringFailedEventProperties.STATUS.name(), status.name());

        enqueueEvent(event);
    }

    private void enqueueEvent(Event event) {
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, event.getType().getQueueSelector());
        LOG.debug(EVENT_ADDED_TO_THE_QUEUE, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueLoginFailureEvent(UserEntityBase.Type userType, final String userName, final Date loginTime, final boolean accountDisabled) {
        EventType eventType = userType == UserEntityBase.Type.CONSOLE ? EventType.USER_LOGIN_FAILURE : EventType.PLUGIN_USER_LOGIN_FAILURE;
        enqueueEvent(prepareAccountEvent(
                eventType, userName,
                userType.getName(),
                loginTime,
                Boolean.toString(accountDisabled),
                AccountEventKey.ACCOUNT_DISABLED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueAccountDisabledEvent(UserEntityBase.Type userType, final String userName, final Date accountDisabledTime) {
        EventType eventType = userType == UserEntityBase.Type.CONSOLE ? EventType.USER_ACCOUNT_DISABLED : EventType.PLUGIN_USER_ACCOUNT_DISABLED;
        enqueueEvent(prepareAccountEvent(
                eventType, userName,
                userType.getName(),
                accountDisabledTime,
                Boolean.toString(true),
                AccountEventKey.ACCOUNT_DISABLED));
    }

    @Override
    public void enqueueAccountEnabledEvent(UserEntityBase.Type userType, String userName, Date accountEnabledTime) {
        EventType eventType = userType == UserEntityBase.Type.CONSOLE ? EventType.USER_ACCOUNT_ENABLED : EventType.PLUGIN_USER_ACCOUNT_ENABLED;
        enqueueEvent(prepareAccountEvent(
                eventType, userName,
                userType.getName(),
                accountEnabledTime,
                Boolean.toString(true),
                AccountEventKey.ACCOUNT_ENABLED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueImminentCertificateExpirationEvent(final String accessPoint, final String alias, final Date expirationDate) {
        EventType eventType = EventType.CERT_IMMINENT_EXPIRATION;
        enqueueEvent(prepareCertificateEvent(accessPoint, alias, expirationDate, eventType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueuePartitionCheckEvent(String partitionName) {
        Event event = new Event(EventType.PARTITION_CHECK);
        event.addStringKeyValue(PartitionCheckEvent.PARTITION_NAME.name(), partitionName);
        enqueueEvent(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueCertificateExpiredEvent(final String accessPoint, final String alias, final Date expirationDate) {
        EventType eventType = EventType.CERT_EXPIRED;
        enqueueEvent(prepareCertificateEvent(accessPoint, alias, expirationDate, eventType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueEArchivingEvent(String batchId, EArchiveBatchStatus batchStatus) {
        Event event = new Event(EventType.ARCHIVING_NOTIFICATION_FAILED);
        event.addStringKeyValue(ArchivingEventProperties.BATCH_ID.name(), batchId);
        event.addStringKeyValue(ArchivingEventProperties.BATCH_STATUS.name(), batchStatus.name());
        enqueueEvent(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public eu.domibus.core.alerts.model.persist.Event persistEvent(final Event event) {
        final eu.domibus.core.alerts.model.persist.Event eventEntity = eventMapper.eventServiceToEventPersist(event);
        LOG.debug("Converting jms event [{}] to persistent event [{}]", event, eventEntity);
        eventEntity.enrichProperties();
        eventDao.create(eventEntity);
        event.setEntityId(eventEntity.getEntityId());
        return eventEntity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enrichMessageEvent(final Event event) {
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

    private Event prepareCertificateEvent(String accessPoint, String alias, Date expirationDate, EventType eventType) {
        Event event = new Event(eventType);
        event.addStringKeyValue(CertificateEvent.ACCESS_POINT.name(), accessPoint);
        event.addStringKeyValue(CertificateEvent.ALIAS.name(), alias);
        event.addDateKeyValue(CertificateEvent.EXPIRATION_DATE.name(), expirationDate);
        return event;
    }

    private Event prepareAccountEvent(
            final EventType eventType,
            final String userName,
            final String userType,
            final Date loginTime,
            final String value,
            final AccountEventKey key) {
        Event event = new Event(eventType);
        event.addAccountKeyValue(USER, userName);
        event.addAccountKeyValue(USER_TYPE, userType);
        event.addDateKeyValue(LOGIN_TIME.name(), loginTime);
        event.addAccountKeyValue(key, value);
        return event;
    }

    @Override
    public void enqueuePasswordExpirationEvent(EventType eventType, UserEntityBase user, Integer maxPasswordAgeInDays, PasswordExpirationAlertModuleConfiguration alertConfiguration) {
        Event event = preparePasswordEvent(user, eventType, maxPasswordAgeInDays);
        eu.domibus.core.alerts.model.persist.Event entity = getPersistedEvent(event, EVENT_IDENTIFIER);

        if (!shouldCreateAlert(entity, alertConfiguration)) {
            return;
        }

        entity.setLastAlertDate(LocalDate.now());
        eventDao.update(entity);

        jmsManager.convertAndSendToQueue(event, alertMessageQueue, eventType.getQueueSelector());

        LOG.securityInfo(eventType.getSecurityMessageCode(), user.getUserName(), event.findOptionalProperty(PasswordExpirationEventProperties.EXPIRATION_DATE.name()));
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

    private eu.domibus.core.alerts.model.persist.Event getPersistedEvent(Event event, String identifier) {
        String id = event.findStringProperty(identifier).orElse("");
        eu.domibus.core.alerts.model.persist.Event entity = eventDao.findWithTypeAndPropertyValue(event.getType(), identifier, id);

        if (entity == null) {
            entity = this.persistEvent(event);
        }

        return entity;
    }

    protected boolean shouldCreateAlert(eu.domibus.core.alerts.model.persist.Event entity, PasswordExpirationAlertModuleConfiguration alertConfiguration) {
        int frequency = alertConfiguration.getEventFrequency();
        return shouldCreateAlert(entity, frequency);
    }

    private Event preparePasswordEvent(UserEntityBase user, EventType eventType, Integer maxPasswordAgeInDays) {
        Event event = new Event(eventType);
        event.setReportingTime(new Date());

        event.addStringKeyValue(EVENT_IDENTIFIER, getUniqueIdentifier(user));
        event.addStringKeyValue(PasswordExpirationEventProperties.USER_TYPE.name(), user.getType().getName());
        event.addStringKeyValue(PasswordExpirationEventProperties.USER.name(), user.getUserName());

        LocalDate expDate = user.getPasswordChangeDate().plusDays(maxPasswordAgeInDays).toLocalDate();
        event.addDateKeyValue(PasswordExpirationEventProperties.EXPIRATION_DATE.name(), Date.from(expDate.atStartOfDay(ZoneOffset.UTC).toInstant()));

        return event;
    }

    private String getUniqueIdentifier(UserEntityBase user) {
        return user.getType().getCode() + "/" + user.getEntityId() + "/" + user.getPasswordChangeDate().toLocalDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueEArchivingMessageNonFinalEvent(final String messageId,
                                                      final MessageStatus status) {
        Event event = new Event(EventType.ARCHIVING_MESSAGES_NON_FINAL);
        event.addStringKeyValue(OLD_STATUS.name(), status.name());
        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
        enqueueEvent(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueEArchivingStartDateStopped() {
        Event event = new Event(EventType.ARCHIVING_START_DATE_STOPPED);
        enqueueEvent(event);
    }

}
