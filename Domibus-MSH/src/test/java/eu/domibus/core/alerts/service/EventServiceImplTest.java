package eu.domibus.core.alerts.service;

import com.google.common.collect.Lists;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.persist.AbstractEventProperty;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import static eu.domibus.core.alerts.model.common.AccountEventKey.*;
import static eu.domibus.core.alerts.model.common.CertificateEvent.*;
import static eu.domibus.core.alerts.model.common.MessageEvent.*;
import static eu.domibus.core.alerts.service.EventServiceImpl.MESSAGE_EVENT_SELECTOR;

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
    private MessagingDao messagingDao;

    @Injectable
    private ErrorLogDao errorLogDao;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private Queue alertMessageQueue;

    @Injectable
    protected MpcService mpcService;

    @Test
    public void enqueueMessageEvent() {
        final String messageId = "messageId";
        final MessageStatus oldMessageStatus = MessageStatus.SEND_ENQUEUED;
        final MessageStatus newMessageStatus = MessageStatus.ACKNOWLEDGED;
        final MSHRole mshRole = MSHRole.SENDING;
        eventService.enqueueMessageEvent(messageId, oldMessageStatus, newMessageStatus, mshRole);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, MESSAGE_EVENT_SELECTOR);
            times = 1;
            Assert.assertEquals(oldMessageStatus.name(), event.getProperties().get(OLD_STATUS.name()).getValue());
            Assert.assertEquals(newMessageStatus.name(), event.getProperties().get(NEW_STATUS.name()).getValue());
            Assert.assertEquals(messageId, event.getProperties().get(MESSAGE_ID.name()).getValue());
            Assert.assertEquals(mshRole.name(), event.getProperties().get(ROLE.name()).getValue());
        }};
    }

    @Test
    public void enqueueLoginFailureEvent() throws ParseException {
        final String userName = "thomas";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date loginTime = parser.parse("25/10/1977 00:00:00");
        final boolean accountDisabled = false;
        eventService.enqueueLoginFailureEvent(UserEntityBase.Type.CONSOLE, userName, loginTime, accountDisabled);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventServiceImpl.LOGIN_FAILURE);
            times = 1;
            Assert.assertEquals(userName, event.getProperties().get(USER.name()).getValue());
            Assert.assertEquals(loginTime, event.getProperties().get(LOGIN_TIME.name()).getValue());
            Assert.assertEquals("false", event.getProperties().get(ACCOUNT_DISABLED.name()).getValue());
        }};

    }

    @Test
    public void enqueueAccountDisabledEvent() throws ParseException {
        final String userName = "thomas";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date loginTime = parser.parse("25/10/1977 00:00:00");

        eventService.enqueueAccountDisabledEvent(UserEntityBase.Type.CONSOLE, userName, loginTime);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventServiceImpl.ACCOUNT_DISABLED);
            times = 1;
            Assert.assertEquals(userName, event.getProperties().get(USER.name()).getValue());
            Assert.assertEquals(loginTime, event.getProperties().get(LOGIN_TIME.name()).getValue());
            Assert.assertEquals("true", event.getProperties().get(ACCOUNT_DISABLED.name()).getValue());
        }};
    }

    @Test
    public void enqueueAccountEnabledEvent() throws ParseException {
        final String userName = "thomas";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date loginTime = parser.parse("25/10/1977 00:00:00");

        eventService.enqueueAccountEnabledEvent(UserEntityBase.Type.CONSOLE, userName, loginTime);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventServiceImpl.ACCOUNT_ENABLED);
            times = 1;
            Assert.assertEquals(userName, event.getProperties().get(USER.name()).getValue());
            Assert.assertEquals(loginTime, event.getProperties().get(LOGIN_TIME.name()).getValue());
            Assert.assertEquals("true", event.getProperties().get(ACCOUNT_ENABLED.name()).getValue());
        }};
    }

    @Test
    public void enqueueImminentCertificateExpirationEvent() throws ParseException {
        final String accessPoint = "red_gw";
        final String alias = "blue_gw";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date expirationDate = parser.parse("25/10/1977 00:00:00");
        eventService.enqueueImminentCertificateExpirationEvent(accessPoint, alias, expirationDate);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventServiceImpl.CERTIFICATE_IMMINENT_EXPIRATION);
            times = 1;
            Assert.assertEquals(accessPoint, event.getProperties().get(ACCESS_POINT.name()).getValue());
            Assert.assertEquals(alias, event.getProperties().get(ALIAS.name()).getValue());
            Assert.assertEquals(expirationDate, event.getProperties().get(EXPIRATION_DATE.name()).getValue());
        }};
    }

    @Test
    public void enqueueCertificateExpiredEvent() throws ParseException {
        final String accessPoint = "red_gw";
        final String alias = "blue_gw";
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date expirationDate = parser.parse("25/10/1977 00:00:00");
        eventService.enqueueCertificateExpiredEvent(accessPoint, alias, expirationDate);
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventServiceImpl.CERTIFICATE_EXPIRED);
            times = 1;
            Assert.assertEquals(accessPoint, event.getProperties().get(ACCESS_POINT.name()).getValue());
            Assert.assertEquals(alias, event.getProperties().get(ALIAS.name()).getValue());
            Assert.assertEquals(expirationDate, event.getProperties().get(EXPIRATION_DATE.name()).getValue());
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
            domainConverter.convert(event, eu.domibus.core.alerts.model.persist.Event.class);
            result = persistedEvent;

        }};
        eventService.persistEvent(event);
        new Verifications() {{
            eu.domibus.core.alerts.model.persist.Event capture;
            eventDao.create(capture = withCapture());
            final AbstractEventProperty stringEventProperty1 = capture.getProperties().get(key);
            Assert.assertEquals(key, stringEventProperty1.getKey());
            Assert.assertEquals(persistedEvent, stringEventProperty1.getEvent());
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
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = userMessageExchangeContext;

            userMessageExchangeContext.getPmodeKey();
            result = "pmodekey";

            pModeProvider.getSenderParty(userMessageExchangeContext.getPmodeKey()).getName();

            result = fromParty;

            pModeProvider.getReceiverParty(userMessageExchangeContext.getPmodeKey()).getName();

            result = toParty;

            errorLogDao.getErrorsForMessage(messageId);
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
    public void enqueuePasswordExpirationEvent(@Mocked PasswordExpirationAlertModuleConfiguration passwordExpirationAlertModuleConfiguration) throws ParseException {
        int maxPasswordAge = 15;
        LocalDateTime passwordDate = LocalDateTime.of(2018, 10, 1, 21, 58, 59);
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        final Date expirationDate = parser.parse("16/10/2018 00:00:00");
        User user = initPasswordTestUser(passwordDate);
        eu.domibus.core.alerts.model.persist.Event persistedEvent = new eu.domibus.core.alerts.model.persist.Event();
        persistedEvent.setEntityId(1);
        persistedEvent.setType(EventType.PASSWORD_EXPIRED);

        new Expectations() {{
            eventDao.findWithTypeAndPropertyValue((EventType) any, anyString, anyString);
            result = null;
            domainConverter.convert(any, eu.domibus.core.alerts.model.persist.Event.class);
            result = persistedEvent;
        }};

        eventService.enqueuePasswordExpirationEvent(EventType.PASSWORD_EXPIRED, user, maxPasswordAge, passwordExpirationAlertModuleConfiguration);

        new VerificationsInOrder() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, anyString);
            times = 1;
            Assert.assertEquals(user.getUserName(), event.getProperties().get("USER").getValue());
            Assert.assertEquals(expirationDate, event.getProperties().get("EXPIRATION_DATE").getValue());
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