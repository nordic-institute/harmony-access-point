package eu.domibus.core.alerts.service;

import com.google.common.collect.Lists;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.user.UserEntityBase;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfiguration;
import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.alerts.model.persist.AbstractEventProperty;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.user.ui.User;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static eu.domibus.core.alerts.model.common.AccountEventKey.*;
import static eu.domibus.core.alerts.model.common.MessageEvent.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class EventServiceImplTest {

    @Tested
    private EventServiceImpl eventService;

    @Injectable
    private EventDao eventDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private UserMessageDao userMessageDao;

    @Injectable
    private ErrorLogService errorLogService;

    @Injectable
    private EventMapper eventMapper;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private Queue alertMessageQueue;

    @Injectable
    protected MpcService mpcService;

    @Injectable
    private AlertConfigurationService alertConfigurationService;

    @Test
    public void enqueueMessageEvent(@Injectable MessagingModuleConfiguration configuration) {
        final String messageId = "messageId";
        final MessageStatus oldMessageStatus = MessageStatus.SEND_ENQUEUED;
        final MessageStatus newMessageStatus = MessageStatus.ACKNOWLEDGED;
        final MSHRole mshRole = MSHRole.SENDING;

        new Expectations() {{
            alertConfigurationService.getConfiguration(AlertType.MSG_STATUS_CHANGED);
            result = configuration;

            configuration.isActive();
            result = true;

            configuration.shouldMonitorMessageStatus(newMessageStatus);
            result = true;
        }};

        eventService.enqueueMessageStatusChangedEvent(messageId, oldMessageStatus, newMessageStatus, mshRole);

        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventType.MSG_STATUS_CHANGED.getQueueSelector());
            times = 1;
            Assert.assertEquals(oldMessageStatus.name(), event.getProperties().get(OLD_STATUS.name()).getValue());
            Assert.assertEquals(newMessageStatus.name(), event.getProperties().get(NEW_STATUS.name()).getValue());
            Assert.assertEquals(messageId, event.getProperties().get(MESSAGE_ID.name()).getValue());
            Assert.assertEquals(mshRole.name(), event.getProperties().get(ROLE.name()).getValue());
        }};
    }

    @Test
    public void enqueueLoginFailureEvent(@Injectable AlertModuleConfiguration configuration) throws ParseException {
        final String userName = "thomas";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date loginTime = parser.parse("25/10/1977 00:00:00");
        final boolean accountDisabled = false;

        new Expectations() {{
            configuration.isActive();
            result = true;
            alertConfigurationService.getConfiguration(AlertType.USER_LOGIN_FAILURE);
            result = configuration;
        }};

        eventService.enqueueEvent(EventType.USER_LOGIN_FAILURE, userName, new EventProperties(userName, UserEntityBase.Type.CONSOLE, loginTime, accountDisabled));

        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventType.USER_LOGIN_FAILURE.getQueueSelector());
            times = 1;
            Assert.assertEquals(userName, event.getProperties().get(USER.name()).getValue());
            Assert.assertEquals(loginTime, event.getProperties().get(LOGIN_TIME.name()).getValue());
            Assert.assertEquals("false", event.getProperties().get(ACCOUNT_DISABLED.name()).getValue());
        }};

    }

    @Test
    public void enqueueAccountDisabledEvent(@Injectable AlertModuleConfiguration configuration) throws ParseException {
        final String userName = "thomas";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date loginTime = parser.parse("25/10/1977 00:00:00");

        new Expectations() {{
            configuration.isActive();
            result = true;
            alertConfigurationService.getConfiguration(AlertType.USER_ACCOUNT_DISABLED);
            result = configuration;
        }};

        eventService.enqueueEvent(EventType.USER_ACCOUNT_DISABLED, userName, new EventProperties(userName, UserEntityBase.Type.CONSOLE, loginTime, true));

        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventType.USER_ACCOUNT_DISABLED.getQueueSelector());
            times = 1;
            Assert.assertEquals(userName, event.getProperties().get(USER.name()).getValue());
            Assert.assertEquals(loginTime, event.getProperties().get(LOGIN_TIME.name()).getValue());
            Assert.assertEquals("true", event.getProperties().get(ACCOUNT_DISABLED.name()).getValue());
        }};
    }

    @Test
    public void enqueueAccountEnabledEvent(@Injectable AlertModuleConfiguration configuration) throws ParseException {
        final String userName = "thomas";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date loginTime = parser.parse("25/10/1977 00:00:00");

        new Expectations() {{
            configuration.isActive();
            result = true;
            alertConfigurationService.getConfiguration(AlertType.USER_ACCOUNT_ENABLED);
            result = configuration;
        }};

        eventService.enqueueEvent(EventType.USER_ACCOUNT_ENABLED, userName, new EventProperties(userName, UserEntityBase.Type.CONSOLE, loginTime, true));

        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventType.USER_ACCOUNT_ENABLED.getQueueSelector());
            times = 1;
            Assert.assertEquals(userName, event.getProperties().get(USER.name()).getValue());
            Assert.assertEquals(loginTime, event.getProperties().get(LOGIN_TIME.name()).getValue());
            Assert.assertEquals("true", event.getProperties().get(ACCOUNT_ENABLED.name()).getValue());
        }};
    }

    @Test
    public void enqueueImminentCertificateExpirationEvent(@Injectable AlertModuleConfiguration configuration) throws ParseException {
        final String alias = "blue_gw";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date expirationDate = parser.parse("25/10/1977 00:00:00");

        new Expectations() {{
            configuration.isActive();
            result = true;
            alertConfigurationService.getConfiguration(AlertType.CERT_IMMINENT_EXPIRATION);
            result = configuration;
        }};

        eventService.enqueueImminentCertificateExpirationEvent(alias, expirationDate);

        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventType.CERT_IMMINENT_EXPIRATION.getQueueSelector());
            times = 1;
            Assert.assertEquals(alias, event.getProperties().get("ALIAS").getValue());
            Assert.assertEquals(expirationDate, event.getProperties().get("EXPIRATION_DATE").getValue());
        }};
    }

    @Test
    public void enqueueCertificateExpiredEvent(@Injectable AlertModuleConfiguration configuration) throws ParseException {
        final String alias = "blue_gw";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date expirationDate = parser.parse("25/10/1977 00:00:00");

        new Expectations() {{
            configuration.isActive();
            result = true;
            alertConfigurationService.getConfiguration(AlertType.CERT_EXPIRED);
            result = configuration;
        }};

        eventService.enqueueCertificateExpiredEvent(alias, expirationDate);

        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventType.CERT_EXPIRED.getQueueSelector());
            times = 1;
            Assert.assertEquals(alias, event.getProperties().get("ALIAS").getValue());
            Assert.assertEquals(expirationDate, event.getProperties().get("EXPIRATION_DATE").getValue());
        }};
    }

    @Test
    public void persistEvent() {
        Event event = new Event();

        eu.domibus.core.alerts.model.persist.Event persistedEvent = new eu.domibus.core.alerts.model.persist.Event();
        persistedEvent.setEntityId(1);
        final String key = "key";
        final StringEventProperty stringEventProperty = new StringEventProperty();
        stringEventProperty.setStringValue("value");
        stringEventProperty.setKey(key);
        persistedEvent.getProperties().put(key, stringEventProperty);

        new Expectations() {{
            eventMapper.eventServiceToEventPersist(event);
            result = persistedEvent;
        }};
        eventService.persistEvent(event);

        new Verifications() {{
            eu.domibus.core.alerts.model.persist.Event capture;
            eventDao.create(capture = withCapture());
            final AbstractEventProperty stringEventProperty1 = capture.getProperties().get(key);
            Assert.assertEquals(key, stringEventProperty1.getKey());
            Assert.assertEquals(persistedEvent.getType(), stringEventProperty1.getEvent().getType());
            Assert.assertEquals(1, event.getEntityId());
        }};
    }

    @Test
    public void enrichMessageEvent(@Mocked final UserMessage userMessage,
                                   @Mocked final MessageExchangeConfiguration userMessageExchangeContext) throws EbMS3Exception {
        final Event event = new Event();
        final String messageId = "messageId";
        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
        event.addStringKeyValue(ROLE.name(), "SENDING");
        final ErrorLogEntry errorLogEntry = new ErrorLogEntry();
        final String error_detail = "Error detail";
        errorLogEntry.setErrorDetail(error_detail);
        final String fromParty = "blue_gw";
        final String toParty = "red_gw";

        new Expectations() {{
            userMessageDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessage;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = userMessageExchangeContext;

            userMessageExchangeContext.getPmodeKey();
            result = "pmodekey";

            pModeProvider.getSenderParty(userMessageExchangeContext.getPmodeKey()).getName();

            result = fromParty;

            pModeProvider.getReceiverParty(userMessageExchangeContext.getPmodeKey()).getName();

            result = toParty;

            errorLogService.getErrorsForMessage(messageId, MSHRole.SENDING);
            result = Lists.newArrayList(errorLogEntry);
        }};
        eventService.enrichMessageEvent(event);
        Assert.assertEquals(fromParty, event.getProperties().get(FROM_PARTY.name()).getValue());
        Assert.assertEquals(toParty, event.getProperties().get(TO_PARTY.name()).getValue());
        Assert.assertEquals(error_detail, event.getProperties().get(DESCRIPTION.name()).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void enrichMessageEventWithIllegalArgumentExcption() {
        final Event event = new Event();
        final String messageId = "messageId";
        event.addStringKeyValue(MESSAGE_ID.name(), messageId);
        eventService.enrichMessageEvent(event);
    }

    @Test
    public void enqueuePasswordExpirationEvent(@Mocked RepetitiveAlertConfiguration configuration) throws ParseException {
        int maxPasswordAge = 15;
        LocalDateTime passwordDate = LocalDateTime.of(2018, 10, 1, 21, 58, 59);
        final Date expirationDate = Date.from(LocalDateTime.of(2018, 10, 16, 0, 0, 0).atZone(ZoneOffset.UTC).toInstant());
        User user = initPasswordTestUser(passwordDate);
        eu.domibus.core.alerts.model.persist.Event persistedEvent = new eu.domibus.core.alerts.model.persist.Event();
        persistedEvent.setEntityId(1);
        persistedEvent.setType(EventType.PASSWORD_EXPIRED);

        new Expectations() {{
            alertConfigurationService.getConfiguration(AlertType.PASSWORD_EXPIRED);
            result = configuration;
            configuration.isActive();
            result = true;
            eventDao.findWithTypeAndPropertyValue((EventType) any, anyString, anyString);
            result = null;
            eventMapper.eventServiceToEventPersist((Event) any);
            result = persistedEvent;
        }};

        eventService.enqueuePasswordExpirationEvent(EventType.PASSWORD_EXPIRED, user, maxPasswordAge);

        new VerificationsInOrder() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, anyString);
            times = 1;
            Assert.assertEquals(user.getUserName(), event.getProperties().get("USER").getValue());
        }};
    }

    private User initPasswordTestUser(LocalDateTime passwordDate) {
        User user = new User();
        user.setEntityId(1234);
        user.setUserName("testuser1");
        user.setPasswordChangeDate(passwordDate);
        return user;
    }
}
