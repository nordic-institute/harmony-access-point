package eu.domibus.core.alerts.service;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.*;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static eu.domibus.core.alerts.model.common.AccountEventKey.*;
import static eu.domibus.core.alerts.model.common.AccountEventKey.LOGIN_TIME;
import static eu.domibus.core.alerts.model.common.MessageEvent.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class EventServiceImpl implements EventService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EventServiceImpl.class);

    static final String MESSAGE_EVENT_SELECTOR = "message";

    static final String LOGIN_FAILURE = "loginFailure";

    static final String ACCOUNT_DISABLED = "accountDisabled";

    static final String ACCOUNT_ENABLED = "accountEnabled";

    static final String CERTIFICATE_EXPIRED = "certificateExpired";

    static final String CERTIFICATE_IMMINENT_EXPIRATION = "certificateImminentExpiration";

    private static final String EVENT_ADDED_TO_THE_QUEUE = "Event:[{}] added to the queue";

    private static final int MAX_DESCRIPTION_LENGTH = 255;

    public static final String EVENT_IDENTIFIER = "EVENT_IDENTIFIER";

    @Autowired
    private EventDao eventDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private JMSManager jmsManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("alertMessageQueue")
    private Queue alertMessageQueue;

    @Autowired
    protected MpcService mpcService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueMessageEvent(
            final String messageId,
            final MessageStatus oldStatus,
            final MessageStatus newStatus,
            final MSHRole role) {
        Event event = new Event(EventType.MSG_STATUS_CHANGED);
        event.addStringKeyValue(OLD_STATUS.name(), oldStatus.name());
        event.addStringKeyValue(NEW_STATUS.name(), newStatus.name());
        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
        event.addStringKeyValue(ROLE.name(), role.name());
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, MESSAGE_EVENT_SELECTOR);
        LOG.debug(EVENT_ADDED_TO_THE_QUEUE, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueLoginFailureEvent(UserEntityBase.Type userType, final String userName, final Date loginTime, final boolean accountDisabled) {
        EventType eventType = userType == UserEntityBase.Type.CONSOLE ? EventType.USER_LOGIN_FAILURE : EventType.PLUGIN_USER_LOGIN_FAILURE;
        enqueueEvent(LOGIN_FAILURE, prepareAccountEvent(
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
        enqueueEvent(ACCOUNT_DISABLED, prepareAccountEvent(
                eventType, userName,
                userType.getName(),
                accountDisabledTime,
                Boolean.toString(true),
                AccountEventKey.ACCOUNT_DISABLED));
    }

    @Override
    public void enqueueAccountEnabledEvent(UserEntityBase.Type userType, String userName, Date accountEnabledTime) {
        EventType eventType = userType == UserEntityBase.Type.CONSOLE ? EventType.USER_ACCOUNT_ENABLED : EventType.PLUGIN_USER_ACCOUNT_ENABLED;
        enqueueEvent(ACCOUNT_ENABLED, prepareAccountEvent(
                eventType, userName,
                userType.getName(),
                accountEnabledTime,
                Boolean.toString(true),
                AccountEventKey.ACCOUNT_ENABLED));
    }

    private void enqueueEvent(String selector, Event event) {
        jmsManager.convertAndSendToQueue(event, alertMessageQueue, selector);
        LOG.debug(EVENT_ADDED_TO_THE_QUEUE, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueImminentCertificateExpirationEvent(final String accessPoint, final String alias, final Date expirationDate) {
        EventType eventType = EventType.CERT_IMMINENT_EXPIRATION;
        enqueueEvent(CERTIFICATE_IMMINENT_EXPIRATION, prepareCertificateEvent(accessPoint, alias, expirationDate, eventType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueCertificateExpiredEvent(final String accessPoint, final String alias, final Date expirationDate) {
        EventType eventType = EventType.CERT_EXPIRED;
        enqueueEvent(CERTIFICATE_EXPIRED, prepareCertificateEvent(accessPoint, alias, expirationDate, eventType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public eu.domibus.core.alerts.model.persist.Event persistEvent(final Event event) {
        final eu.domibus.core.alerts.model.persist.Event eventEntity = domainConverter.convert(event, eu.domibus.core.alerts.model.persist.Event.class);
        LOG.debug("Converting jms event\n[{}] to persistent event\n[{}]", event, eventEntity);
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
        final String role = roleProperty.get();
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        final MessageExchangeConfiguration userMessageExchangeContext;
        try {
            StringBuilder errors = new StringBuilder();
            errorLogDao.
                    getErrorsForMessage(messageId).
                    stream().
                    map(ErrorLogEntry::getErrorDetail).forEach(errors::append);
            if (!errors.toString().isEmpty()) {
                event.addStringKeyValue(DESCRIPTION.name(), StringUtils.truncate(errors.toString(), MAX_DESCRIPTION_LENGTH));
            }

            String receiverPartyName = null;
            if (mpcService.forcePullOnMpc(userMessage.getMpc())) {
                LOG.debug("Find UserMessage exchange context (pull context)");
                userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true);
                LOG.debug("Extract receiverPartyName from mpc");
                receiverPartyName = mpcService.extractInitiator(userMessage.getMpc());
            } else {
                LOG.debug("Find UserMessage exchange context");
                userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.valueOf(role));
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
        eu.domibus.core.alerts.model.persist.Event entity = getPersistedEvent(event);

        if (!shouldCreateAlert(entity, alertConfiguration)) {
            return;
        }

        entity.setLastAlertDate(LocalDate.now());
        eventDao.update(entity);

        jmsManager.convertAndSendToQueue(event, alertMessageQueue, eventType.getQueueSelector());

        LOG.securityInfo(eventType.getSecurityMessageCode(), user.getUserName(), event.findOptionalProperty(PasswordExpirationEventProperties.EXPIRATION_DATE.name()));
    }

    private eu.domibus.core.alerts.model.persist.Event getPersistedEvent(Event event) {
        String id = event.findStringProperty(EVENT_IDENTIFIER).orElse("");
        eu.domibus.core.alerts.model.persist.Event entity = eventDao.findWithTypeAndPropertyValue(event.getType(), EVENT_IDENTIFIER, id);

        if (entity == null) {
            entity = this.persistEvent(event);
        }

        return entity;
    }

    protected boolean shouldCreateAlert(eu.domibus.core.alerts.model.persist.Event entity, PasswordExpirationAlertModuleConfiguration alertConfiguration) {
        int frequency = alertConfiguration.getEventFrequency();

        LocalDate lastAlertDate = entity.getLastAlertDate();
        LocalDate notificationDate = LocalDate.now().minusDays(frequency);

        if (lastAlertDate == null) {
            LOG.debug("Should create alert for event [{}] because lastAlertDate == null", entity);
            return true;
        }

        if (lastAlertDate.isBefore(notificationDate)) {
            LOG.debug("Should create alert for event [{}] because lastAlertDate is old enough", entity);
            return true; // last alert is old enough to send another one
        }

        LOG.debug("Should NOT create alert for event [{}] because lastAlertDate is not old enough", entity);
        return false;
    }

    private Event preparePasswordEvent(UserEntityBase user, EventType eventType, Integer maxPasswordAgeInDays) {
        Event event = new Event(eventType);
        event.setReportingTime(new Date());

        event.addStringKeyValue(EVENT_IDENTIFIER, getUniqueIdentifier(user));
        event.addStringKeyValue(PasswordExpirationEventProperties.USER_TYPE.name(), user.getType().getName());
        event.addStringKeyValue(PasswordExpirationEventProperties.USER.name(), user.getUserName());

        LocalDate expDate = user.getPasswordChangeDate().plusDays(maxPasswordAgeInDays).toLocalDate();
        event.addDateKeyValue(PasswordExpirationEventProperties.EXPIRATION_DATE.name(), Date.from(expDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        return event;
    }

    private String getUniqueIdentifier(UserEntityBase user) {
        return user.getType().getCode() + "/" + user.getEntityId() + "/" + user.getPasswordChangeDate().toLocalDate();
    }

}
