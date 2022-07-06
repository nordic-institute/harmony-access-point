package eu.domibus.core.plugin.notification;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.*;
import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.core.plugin.BackendConnectorHelper;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.plugin.validation.SubmissionValidatorService;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import javax.management.NotificationListener;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE;
import static eu.domibus.common.NotificationType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class BackendNotificationServiceTest {

    public static final String FINAL_RECIPIENT = "finalRecipient";
    public static final String ORIGINAL_SENDER = "originalSender";
    public static final String MESSAGE_ID = "MessageId";
    long entityId = 1L;
    public static final Timestamp TIMESTAMP = new Timestamp(System.currentTimeMillis());
    public static final String ORIGINAL_FILENAME = "originalFilename";
    public static final String BACKEND_NAME = "backendName";
    public static final String MIME = "mime";
    public static final String HREF = "Href";

    @Tested
    BackendNotificationService backendNotificationService = new BackendNotificationService();

    @Injectable
    SubmissionValidatorService submissionValidatorService;

    @Injectable
    JMSManager jmsManager;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    private UserMessageDao userMessageDao;

    @Injectable
    RoutingService routingService;

    @Injectable
    AsyncNotificationConfigurationService asyncNotificationConfigurationService;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    PluginEventNotifierProvider pluginEventNotifierProvider;

    @Injectable
    UserMessageHandlerService userMessageHandlerService;

    @Injectable
    Queue unknownReceiverQueue;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    UserMessageServiceHelper userMessageServiceHelper;

    @Injectable
    private EventService eventService;

    @Injectable
    private MessagingConfigurationManager messagingConfigurationManager;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomainService domainService;

    @Injectable
    BackendConnectorProvider backendConnectorProvider;

    @Injectable
    BackendConnectorDelegate backendConnectorDelegate;

    @Injectable
    BackendConnectorHelper backendConnectorHelper;

    @Injectable
    MessagingModuleConfiguration messageCommunicationConfiguration;

    @Test
    public void testValidateAndNotify_propertyNull(@Mocked final UserMessage userMessage) {
        String backendName = "backendName";
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        new Expectations(backendNotificationService) {{
            backendNotificationService.notify(userMessage, backendName, notificationType, null);
        }};

        backendNotificationService.notify(userMessage, backendName, notificationType, null);

        new Verifications() {
        };
    }


    @Test
    public void testNotifyWithNoConfiguredNotificationListener(@Mocked final BackendConnector<?, ?> backendConnector,
                                                               @Mocked UserMessage userMessage) {
        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);
        HashMap<String, String> properties = new HashMap<>();
        long entityId = 1L;

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorHelper.getRequiredNotificationTypeList(backendConnector);
            result = requiredNotifications;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = null;

            userMessage.getMessageId();
            result = MESSAGE_ID;

            userMessage.getEntityId();
            this.result = entityId;

            backendNotificationService.notifySync(backendConnector, entityId, MESSAGE_ID, NotificationType.MESSAGE_RECEIVED, properties);
        }};

        backendNotificationService.notify(userMessage, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, properties);

        new FullVerifications() {
        };
    }


    @Test
    public void notify(
            @Injectable final AsyncNotificationConfiguration notificationListener,
            @Mocked final BackendConnector<?, ?> backendConnector,
            @Mocked UserMessage userMessage) {

        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);
        long entityId = 1L;

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorHelper.getRequiredNotificationTypeList(backendConnector);
            result = requiredNotifications;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = notificationListener;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = notificationListener;

            backendNotificationService.shouldNotifyAsync(notificationListener);
            result = true;

            userMessage.getMessageId();
            result = MESSAGE_ID;

            userMessage.getEntityId();
            this.result = entityId;

            backendNotificationService.notifyAsync(notificationListener, entityId, MESSAGE_ID, NotificationType.MESSAGE_RECEIVED, null);
        }};

        backendNotificationService.notify(userMessage, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {
        };
    }

    @Test
    public void notifySync_propertiesNotNull(
            @Mocked final AsyncNotificationConfiguration notificationListener,
            @Mocked final BackendConnector<?, ?> backendConnector,
            @Mocked UserMessage userMessage) {
        long entityId = 1L;

        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorHelper.getRequiredNotificationTypeList(backendConnector);
            result = requiredNotifications;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = notificationListener;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = notificationListener;

            backendNotificationService.shouldNotifyAsync(notificationListener);
            result = false;

            userMessage.getMessageId();
            result = MESSAGE_ID;

            userMessage.getEntityId();
            this.result = entityId;

            backendNotificationService.notifySync(backendConnector, entityId, MESSAGE_ID, NotificationType.MESSAGE_RECEIVED, null);
        }};

        backendNotificationService.notify(userMessage, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {
        };
    }

    @Test
    public void notify_NoNotification(@Mocked final BackendConnector<?, ?> backendConnector,
                                      @Mocked UserMessage userMessage) {

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorHelper.getRequiredNotificationTypeList(backendConnector);
            result = null;

            userMessage.getMessageId();
            result = MESSAGE_ID;

            userMessage.getEntityId();
            this.result = entityId;
        }};

        backendNotificationService.notify(userMessage, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {
        };
    }

    @Test
    public void notify_NoBackendConnector(@Mocked UserMessage userMessage) {

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = null;
        }};

        backendNotificationService.notify(userMessage, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new Verifications() {
        };
    }

    @Test
    public void notify_NotificationNotMatchType(@Mocked BackendConnector<?, ?> backendConnector, @Mocked UserMessage userMessage) {

        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(MESSAGE_STATUS_CHANGE);

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorHelper.getRequiredNotificationTypeList(backendConnector);
            result = requiredNotifications;

            userMessage.getMessageId();
            result = MESSAGE_ID;

            userMessage.getEntityId();
            this.result = entityId;
        }};

        backendNotificationService.notify(userMessage, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyOfMessageStatusChange(@Mocked final UserMessageLog messageLog,
                                                @Mocked final UserMessage userMessage) {
        MessageStatus status = MessageStatus.ACKNOWLEDGED;

        new Expectations(backendNotificationService) {{
            userMessageDao.findByMessageId(anyString);
            result = userMessage;

            backendNotificationService.notifyOfMessageStatusChange(userMessage, messageLog, status, TIMESTAMP);
        }};

        backendNotificationService.notifyOfMessageStatusChange(MESSAGE_ID, messageLog, status, TIMESTAMP);

        new FullVerifications() {
        };
    }


    @Test
    public void notifyOfMessageStatusChange_isPluginNotificationDisabled(
            @Mocked final UserMessageLog messageLog, @Mocked final UserMessage userMessage) {
        final String messageId = "1";
        final MSHRole role = MSHRole.SENDING;

        MessageStatus status = MessageStatus.ACKNOWLEDGED;
        final MessageStatus previousStatus = MessageStatus.SEND_ENQUEUED;
        new Expectations(backendNotificationService) {{
            messagingConfigurationManager.getConfiguration();
            result = messageCommunicationConfiguration;
            times = 1;

            messageCommunicationConfiguration.shouldMonitorMessageStatus(status);
            result = true;
            times = 1;

            backendNotificationService.isPluginNotificationDisabled();
            result = true;
            times = 1;

            messageLog.getMessageStatus();
            result = previousStatus;

            userMessage.getMessageId();
            result = messageId;

            messageLog.getMshRole().getRole();
            result = role;
        }};

        backendNotificationService.notifyOfMessageStatusChange(userMessage, messageLog, status, TIMESTAMP);

        new FullVerificationsInOrder() {{
            eventService.enqueueMessageEvent(messageId, previousStatus, status, role);
            times = 1;
        }};
    }


    @Test
    public void notifyOfMessageStatusChange_notFragment(@Mocked final UserMessageLog messageLog,
                                                        @Mocked final UserMessage userMessage) {
        final String messageId = "1";
        final String backend = "JMS";
        final MSHRole role = MSHRole.SENDING;

        MessageStatus status = MessageStatus.ACKNOWLEDGED;
        final MessageStatus previousStatus = MessageStatus.SEND_ENQUEUED;
        new Expectations(backendNotificationService) {{
            messagingConfigurationManager.getConfiguration();
            result = messageCommunicationConfiguration;
            times = 1;

            messageCommunicationConfiguration.shouldMonitorMessageStatus(status);
            result = true;
            times = 1;

            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            messageLog.getMessageStatus();
            result = previousStatus;

            userMessage.getMessageId();
            result = messageId;

            messageLog.getMshRole().getRole();
            result = role;

            messageLog.getBackend();
            result = backend;

            userMessage.isMessageFragment();
            result = false;

            backendNotificationService.getMessageProperties(messageLog, userMessage, status, TIMESTAMP);
            result = new HashMap<String, Object>();

            backendNotificationService.notify(userMessage, anyString, NotificationType.MESSAGE_STATUS_CHANGE, withAny(new HashMap<>()));
            times = 1;
        }};

        backendNotificationService.notifyOfMessageStatusChange(userMessage, messageLog, status, TIMESTAMP);

        new FullVerificationsInOrder() {{
            eventService.enqueueMessageEvent(messageId, previousStatus, status, role);
            times = 1;
        }};
    }

    @Test
    public void notifyOfMessageStatusChange_fragment(@Mocked final UserMessageLog messageLog,
                                                     @Mocked final UserMessage userMessage) {
        final String messageId = "1";
        final String backend = "JMS";
        final MSHRole role = MSHRole.SENDING;

        MessageStatus status = MessageStatus.ACKNOWLEDGED;
        final MessageStatus previousStatus = MessageStatus.SEND_ENQUEUED;
        new Expectations(backendNotificationService) {{
            messagingConfigurationManager.getConfiguration();
            result = messageCommunicationConfiguration;
            times = 1;

            messageCommunicationConfiguration.shouldMonitorMessageStatus(status);
            result = true;
            times = 1;

            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            messageLog.getMessageStatus();
            result = previousStatus;

            userMessage.getMessageId();
            result = messageId;

            messageLog.getMshRole().getRole();
            result = role;

            messageLog.getBackend();
            result = backend;

            userMessage.isMessageFragment();
            result = true;

            backendNotificationService.getMessageProperties(messageLog, userMessage, status, TIMESTAMP);
            result = new HashMap<String, Object>();

            backendNotificationService.notify(userMessage, anyString, NotificationType.MESSAGE_FRAGMENT_STATUS_CHANGE, withAny(new HashMap<>()));
            times = 1;
        }};

        backendNotificationService.notifyOfMessageStatusChange(userMessage, messageLog, status, TIMESTAMP);

        new FullVerificationsInOrder() {{
            eventService.enqueueMessageEvent(messageId, previousStatus, status, role);
            times = 1;
        }};
    }

    @Test
    public void notifyOfMessageStatusChange_SameState(@Mocked final UserMessageLog messageLog, @Mocked final UserMessage userMessage) {
        final String messageId = "1";
        final MSHRole role = MSHRole.SENDING;

        MessageStatus status = MessageStatus.ACKNOWLEDGED;
        final MessageStatus previousStatus = MessageStatus.ACKNOWLEDGED;
        new Expectations(backendNotificationService) {{
            messagingConfigurationManager.getConfiguration();
            result = messageCommunicationConfiguration;
            times = 1;

            messageCommunicationConfiguration.shouldMonitorMessageStatus(status);
            result = true;
            times = 1;

            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            messageLog.getMessageStatus();
            result = previousStatus;

            userMessage.getMessageId();
            result = messageId;

            messageLog.getMshRole().getRole();
            result = role;
        }};

        backendNotificationService.notifyOfMessageStatusChange(userMessage, messageLog, status, TIMESTAMP);

        new FullVerifications() {{
            eventService.enqueueMessageEvent(messageId, previousStatus, status, role);
            times = 1;
        }};
    }


    @Test
    public void createMessageDeleteBatchEventTest() {

        String backend = "ws";
        List<MessageDeletedEvent> messageIds = Stream
                .of("abc", "def")
                .map(this::getMessageDeletedEvent)
                .collect(Collectors.toList());

        new Expectations(backendNotificationService) {{
            backendConnectorDelegate.messageDeletedBatchEvent(backend, (MessageDeletedBatchEvent) any);

        }};

        backendNotificationService.createMessageDeleteBatchEvent(backend, messageIds);

        new FullVerifications() {
        };
    }

    private MessageDeletedEvent getMessageDeletedEvent(String s) {
        MessageDeletedEvent messageDeletedEvent = new MessageDeletedEvent();
        messageDeletedEvent.setMessageId(s);
        return messageDeletedEvent;
    }

    @Test
    public void getAllMessageIdsForBackendTest(@Mocked UserMessageLogDto uml1,
                                               @Mocked UserMessageLogDto uml2) {

        String backend = "ws";
        List<UserMessageLogDto> userMessageLogDtos = Arrays.asList(uml1, uml2);
        Map<String, String> props = new HashMap<>();
        props.put(FINAL_RECIPIENT, "f1");
        props.put(ORIGINAL_SENDER, "o1");

        new Expectations(backendNotificationService) {{
            uml1.getMessageId();
            result = "abc";

            uml2.getMessageId();
            result = "def";

            uml1.getBackend();
            result = backend;

            uml2.getBackend();
            result = backend;

            uml1.getProperties();
            result = props;

            uml2.getProperties();
            result = props;

        }};

        List<MessageDeletedEvent> result =
                backendNotificationService.getAllMessageIdsForBackend(backend, userMessageLogDtos);

        assertEquals(2, result.size());
        assertEquals("abc", result.get(0).getMessageId());
        assertEquals("def", result.get(1).getMessageId());

        new FullVerifications() {
        };
    }

    @Test
    public void getMessageDeletedEvent(@Mocked UserMessageLogDto uml1) {

        Map<String, String> props1 = new HashMap<>();
        props1.put(FINAL_RECIPIENT, "f1");
        props1.put(ORIGINAL_SENDER, "o1");
        new Expectations(backendNotificationService) {{
            uml1.getMessageId();
            result = "abc";

            uml1.getProperties();
            result = props1;

        }};

        MessageDeletedEvent result =
                backendNotificationService.getMessageDeletedEvent(uml1);

        assertEquals("abc", result.getMessageId());
        assertEquals("f1", result.getProps().get(FINAL_RECIPIENT));
        assertEquals("o1", result.getProps().get(ORIGINAL_SENDER));

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyMessageDeletedRemoveTestMessages(@Mocked UserMessageLogDto uml1, @Mocked UserMessageLogDto uml2) {

        String backend = "ws";
        List<UserMessageLogDto> userMessageLogDtos = Arrays.asList(uml1, uml2);
        List<UserMessageLogDto> userMessageLogDtosNoTest = Collections.singletonList(uml1);

        new Expectations(backendNotificationService) {{
            uml1.getBackend();
            result = backend;
            uml2.getBackend();
            times = 0;
            uml1.isTestMessage();
            result = false;
            uml2.isTestMessage();
            result = true;

            backendNotificationService.getAllMessageIdsForBackend(backend, userMessageLogDtosNoTest);
            times = 1;

            backendNotificationService.createMessageDeleteBatchEvent(backend, (List<MessageDeletedEvent>) any);

        }};

        backendNotificationService.notifyMessageDeleted(userMessageLogDtos);

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyMessage_allTestMessages(@Mocked UserMessageLogDto uml1, @Mocked UserMessageLogDto uml2) {

        String backend = "ws";
        List<UserMessageLogDto> userMessageLogDtos = Arrays.asList(uml1, uml2);
        List<UserMessageLogDto> userMessageLogDtosNoTest = Collections.singletonList(uml1);

        new Expectations(backendNotificationService) {{
            uml1.isTestMessage();
            result = true;
            uml2.isTestMessage();
            result = true;

            backendNotificationService.getAllMessageIdsForBackend(backend, userMessageLogDtosNoTest);
            times = 0;

            backendNotificationService.createMessageDeleteBatchEvent(backend, (List<MessageDeletedEvent>) any);
            times = 0;

        }};

        backendNotificationService.notifyMessageDeleted(userMessageLogDtos);

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyMessageDeletedEmptyList(@Mocked UserMessageLogDto uml1, @Mocked UserMessageLogDto uml2) {

        String backend = "ws";
        List<UserMessageLogDto> userMessageLogDtos = Collections.emptyList();

        new Expectations(backendNotificationService) {{
            uml1.getBackend();
            times = 0;
            uml2.getBackend();
            times = 0;

            backendNotificationService.getAllMessageIdsForBackend(backend, userMessageLogDtos);
            times = 0;
        }};

        backendNotificationService.notifyMessageDeleted(userMessageLogDtos);

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyMessageDeleted(
            @Mocked UserMessageLogDto uml1,
            @Mocked UserMessageLogDto uml2) {

        String backend = "ws";
        List<UserMessageLogDto> userMessageLogDtos = Arrays.asList(uml1, uml2);
        List<MessageDeletedEvent> messageIds = Stream
                .of("abc", "def")
                .map(this::getMessageDeletedEvent)
                .collect(Collectors.toList());

        List<List<MessageDeletedEvent>> calls = new ArrayList<>();

        new Expectations(backendNotificationService) {{
            uml1.getBackend();
            result = backend;
            uml2.getBackend();
            result = backend;
            uml1.isTestMessage();
            result = false;
            uml2.isTestMessage();
            result = false;

            backendNotificationService.getAllMessageIdsForBackend(backend, userMessageLogDtos);
            result = messageIds;

            backendNotificationService.createMessageDeleteBatchEvent(backend, withCapture(calls));
        }};

        backendNotificationService.notifyMessageDeleted(userMessageLogDtos);

        assertEquals(1, calls.size());
        assertEquals(2, calls.get(0).size());

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyMessageDeleted_noBackend(
            @Mocked UserMessageLogDto uml1,
            @Mocked UserMessageLogDto uml2) {

        String backend = "ws";
        List<UserMessageLogDto> userMessageLogDtos = Arrays.asList(uml1, uml2);

        List<List<MessageDeletedEvent>> calls = new ArrayList<>();

        new Expectations(backendNotificationService) {{
            uml1.getBackend();
            result = null;
            uml2.getBackend();
            result = null;
            uml1.isTestMessage();
            result = false;
            uml2.isTestMessage();
            result = false;

            backendNotificationService.getAllMessageIdsForBackend(backend, userMessageLogDtos);
            times = 0;

            backendNotificationService.createMessageDeleteBatchEvent(backend, withCapture(calls));
            times = 0;
        }};

        backendNotificationService.notifyMessageDeleted(userMessageLogDtos);

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyMessageReceivedFailure(@Mocked UserMessage userMessage,
                                                 @Mocked ErrorResult errorResult,
                                                 @Mocked List<PartInfo> partInfoList,
                                                 @Mocked ServiceEntity serviceEntity) {

        String errorCodeName = "errorCode";
        String errorDetail = "errorDetail";
        String service = "my service";
        String serviceType = "service type";
        String action = "my action";

        List<Map<String, String>> propertiesList = new ArrayList<>();

        new Expectations(backendNotificationService) {{
            errorResult.getErrorCode().getErrorCodeName();
            result = errorCodeName;

            errorResult.getErrorDetail();
            result = errorDetail;

            userMessage.isMessageFragment();
            result = true;

            userMessage.getService();
            result = serviceEntity;

            userMessage.getActionValue();
            result = action;

            serviceEntity.getValue();
            result = service;

            serviceEntity.getType();
            result = serviceType;

            backendNotificationService.isPluginNotificationDisabled();
            result = false;

            backendNotificationService.notifyOfIncoming(userMessage, MESSAGE_FRAGMENT_RECEIVED_FAILURE, withCapture(propertiesList));

        }};

        backendNotificationService.notifyMessageReceivedFailure(userMessage, errorResult);

        assertEquals(1, propertiesList.size());

        assertEquals(errorCodeName, propertiesList.get(0).get(MessageConstants.ERROR_CODE));
        assertEquals(errorDetail, propertiesList.get(0).get(MessageConstants.ERROR_DETAIL));
        assertEquals(service, propertiesList.get(0).get(MessageConstants.SERVICE));
        assertEquals(serviceType, propertiesList.get(0).get(MessageConstants.SERVICE_TYPE));
        assertEquals(action, propertiesList.get(0).get(MessageConstants.ACTION));

        new Verifications() {
        };
    }

    @Test
    public void testNotifyMessageReceivedFailure_PluginNotificationDisabled(@Mocked UserMessage userMessage,
                                                                            @Mocked ErrorResult errorResult,
                                                                            @Mocked List<PartInfo> partInfoList) {

        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = true;
        }};

        backendNotificationService.notifyMessageReceivedFailure(userMessage, errorResult);

        new FullVerifications() {
        };
    }

    @Test
    public void isPluginNotificationDisabled() {
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE);
            result = true;
        }};
        boolean pluginNotificationDisabled = backendNotificationService.isPluginNotificationDisabled();
        assertFalse(pluginNotificationDisabled);

        new FullVerifications() {
        };
    }

    @Test
    public void notifyOfSendSuccess(@Mocked UserMessageLog userMessageLog, @Mocked UserMessage userMessage) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = true;
            times = 1;
        }};
        backendNotificationService.notifyOfSendSuccess(userMessage, userMessageLog);
        new FullVerifications() {
        };
    }

    @Test
    public void notifyOfSendSuccess_notFragment(@Mocked UserMessageLog userMessageLog, @Mocked UserMessage userMessage) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            userMessage.isMessageFragment();
            result = false;
            times = 1;

            userMessageLog.getBackend();
            result = BACKEND_NAME;
            times = 1;

            backendNotificationService.notify(userMessage, BACKEND_NAME, MESSAGE_SEND_SUCCESS, (Map<String, String>) any);
            times = 1;

            userMessageLogDao.setAsNotified(userMessageLog);
            times = 1;
        }};
        backendNotificationService.notifyOfSendSuccess(userMessage, userMessageLog);
        new Verifications() {
        };
    }

    @Test
    public void notifyOfSendSuccess_fragment(@Mocked UserMessageLog userMessageLog, @Mocked UserMessage userMessage) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            userMessage.isMessageFragment();
            result = true;
            times = 1;

            userMessageLog.getBackend();
            result = BACKEND_NAME;
            times = 1;

            backendNotificationService.notify(userMessage, BACKEND_NAME, MESSAGE_FRAGMENT_SEND_SUCCESS, (Map<String, String>) any);
            times = 1;

            userMessageLogDao.setAsNotified(userMessageLog);
            times = 1;
        }};

        backendNotificationService.notifyOfSendSuccess(userMessage, userMessageLog);
        new Verifications() {
        };
    }

    @Test
    public void notifyMessageReceived_isPluginNotificationDisabled(
            @Mocked final BackendFilter matchingBackendFilter,
            @Mocked final UserMessage userMessage) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = true;
            times = 1;
        }};

        backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);

        new FullVerifications() {
        };
    }

    @Test
    public void notifyMessageReceived_fragment(
            @Mocked final BackendFilter matchingBackendFilter,
            @Mocked final UserMessage userMessage) {

        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            userMessage.isMessageFragment();
            result = true;
            times = 1;

            backendNotificationService.notifyOfIncoming(matchingBackendFilter, userMessage, MESSAGE_FRAGMENT_RECEIVED, withAny(new HashMap<>()));
            times = 1;
        }};

        backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);

        new Verifications() {
        };
    }

    @Test
    public void notifyMessageReceived_NotFragment(
            @Mocked final BackendFilter matchingBackendFilter,
            @Mocked final UserMessage userMessage) {

        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            userMessage.isMessageFragment();
            result = false;
            times = 1;

            backendNotificationService.notifyOfIncoming(matchingBackendFilter, userMessage, MESSAGE_RECEIVED, withAny(new HashMap<>()));
            times = 1;
        }};

        backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);

        new Verifications() {
        };
    }

    @Test
    public void notifyPayloadSubmitted(
            @Mocked UserMessage userMessage,
            @Mocked PartInfo partInfo,
            @Mocked BackendConnector<?, ?> backendConnector) {
        List<PayloadSubmittedEvent> valueHolderForMultipleInvocations = new ArrayList<>();

        new Expectations(backendNotificationService) {{
//            userMessageHandlerService.checkTestMessage(userMessage);
//            result = false;

            userMessage.getEntityId();
            result = 1L;

            userMessage.getMessageId();
            result = MESSAGE_ID;

            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            partInfo.getHref();
            result = HREF;

            partInfo.getMime();
            result = MIME;

            backendConnector.payloadSubmittedEvent(withCapture(valueHolderForMultipleInvocations));
            times = 1;
        }};

        backendNotificationService.notifyPayloadSubmitted(userMessage, ORIGINAL_FILENAME, partInfo, BACKEND_NAME);

        new FullVerifications() {
        };

        assertEquals(1, valueHolderForMultipleInvocations.size());
        assertEquals(HREF, valueHolderForMultipleInvocations.get(0).getCid());
        assertEquals(ORIGINAL_FILENAME, valueHolderForMultipleInvocations.get(0).getFileName());
        assertEquals(MESSAGE_ID, valueHolderForMultipleInvocations.get(0).getMessageId());
        assertEquals(MIME, valueHolderForMultipleInvocations.get(0).getMime());
    }

    @Test
    public void notifyPayloadSubmitted_test(
            @Mocked UserMessage userMessage,
            @Mocked PartInfo partInfo) {

        new Expectations() {{
//            userMessageHandlerService.checkTestMessage(userMessage);
//            result = true;
        }};

        backendNotificationService.notifyPayloadSubmitted(userMessage, ORIGINAL_FILENAME, partInfo, BACKEND_NAME);

        new Verifications() {
        };
    }

    @Test
    public void notifyPayloadProcessed(
            @Mocked UserMessage userMessage,
            @Mocked PartInfo partInfo,
            @Mocked BackendConnector<?, ?> backendConnector) {
        List<PayloadProcessedEvent> payloadList = new ArrayList<>();

        new Expectations(backendNotificationService) {{
//            userMessageHandlerService.checkTestMessage(userMessage);
//            result = false;

            userMessage.getEntityId();
            result = 1L;

            userMessage.getMessageId();
            result = MESSAGE_ID;

            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            partInfo.getHref();
            result = HREF;

            partInfo.getMime();
            result = MIME;

            backendConnector.payloadProcessedEvent(withCapture(payloadList));
            times = 1;
        }};

        backendNotificationService.notifyPayloadProcessed(userMessage, ORIGINAL_FILENAME, partInfo, BACKEND_NAME);

        new FullVerifications() {
        };

        assertEquals(1, payloadList.size());
        assertEquals(HREF, payloadList.get(0).getCid());
        assertEquals(ORIGINAL_FILENAME, payloadList.get(0).getFileName());
        assertEquals(MESSAGE_ID, payloadList.get(0).getMessageId());
        assertEquals(MIME, payloadList.get(0).getMime());
    }

    @Test
    public void notifyPayloadProcessed_test(
            @Mocked UserMessage userMessage,
            @Mocked PartInfo partInfo) {

        new Expectations() {{
//            userMessageHandlerService.checkTestMessage(userMessage);
//            result = true;
        }};

        backendNotificationService.notifyPayloadProcessed(userMessage, ORIGINAL_FILENAME, partInfo, BACKEND_NAME);

        new Verifications() {
        };

    }

    @Test
    public void notifyOfIncoming_matchingBackendFilterNull(
            @Mocked UserMessage userMessage,
            @Mocked NotificationType notificationType) {

        Map<String, String> properties = new HashMap<>();

        new Expectations() {{
            routingService.getMatchingBackendFilter(userMessage);
            result = null;

            userMessage.getMessageId();
            result = MESSAGE_ID;
        }};

        backendNotificationService.notifyOfIncoming(userMessage, notificationType, properties);

        //It's not fullVerification because it was raising an UnexpectedInvocation on Queue#toString() (for logging)
        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, unknownReceiverQueue);
            times = 1;
        }};
    }

    @Test
    public void notifyOfIncoming(
            @Mocked BackendFilter matchingBackendFilter,
            @Mocked UserMessage userMessage,
            @Mocked NotificationType notificationType) {
        Map<String, String> properties = new HashMap<>();
        new Expectations(backendNotificationService) {{
            matchingBackendFilter.getBackendName();
            result = BACKEND_NAME;

            backendNotificationService.notify(userMessage, BACKEND_NAME, notificationType, properties);
            times = 1;
        }};

        backendNotificationService.notifyOfIncoming(userMessage, notificationType, properties);

        new Verifications() {{
            routingService.getMatchingBackendFilter(userMessage);
        }};
    }


    @Test
    public void notifyOfIncoming(@Mocked UserMessage userMessage,
                                 @Mocked NotificationType notificationType,
                                 @Mocked Map<String, String> properties,
                                 @Mocked BackendFilter matchingBackendFilter) {
        new Expectations(backendNotificationService) {{
            routingService.getMatchingBackendFilter(userMessage);
            result = matchingBackendFilter;

            backendNotificationService.notifyOfIncoming(matchingBackendFilter, userMessage, notificationType, properties);
            times = 1;
        }};

        backendNotificationService.notifyOfIncoming(userMessage, notificationType, properties);

        new FullVerifications() {
        };
    }


    @Test
    public void notifyOfSendFailure_isPluginNotificationDisabled(@Mocked UserMessageLog userMessageLog, @Mocked UserMessage userMessage) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = true;
        }};
        backendNotificationService.notifyOfSendFailure(userMessage, userMessageLog);
        new FullVerifications() {
        };
    }

    @Test
    public void notifyOfSendFailure_fragment(@Mocked UserMessageLog userMessageLog, @Mocked UserMessage userMessage) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;

            userMessageLog.getBackend();
            result = BACKEND_NAME;

            userMessage.isMessageFragment();
            result = true;

            backendNotificationService.notify(userMessage, BACKEND_NAME, NotificationType.MESSAGE_FRAGMENT_SEND_FAILURE, (Map<String, String>) any);
        }};

        backendNotificationService.notifyOfSendFailure(userMessage, userMessageLog);

        new Verifications() {{
            userMessageLogDao.setAsNotified(userMessageLog);
            times = 1;
        }};
    }

    @Test
    public void notifyOfSendFailure_NoFragment(@Mocked UserMessageLog userMessageLog, @Mocked UserMessage userMessage) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;

            userMessageLog.getBackend();
            result = BACKEND_NAME;

            userMessage.isMessageFragment();
            result = false;

            backendNotificationService.notify(userMessage, BACKEND_NAME, NotificationType.MESSAGE_SEND_FAILURE, (Map<String, String>) any);
            times = 1;
        }};

        backendNotificationService.notifyOfSendFailure(userMessage, userMessageLog);

        new Verifications() {{
            userMessageLogDao.setAsNotified(userMessageLog);
            times = 1;
        }};
    }

    @Test
    public void getMessageProperties(@Mocked UserMessageLog messageLog,
                                     @Mocked UserMessage userMessage) {
        MessageStatus newStatus = MessageStatus.ACKNOWLEDGED;
        Map<String, String> props = new HashMap<>();
        props.put(MessageConstants.FINAL_RECIPIENT, FINAL_RECIPIENT);
        props.put(MessageConstants.ORIGINAL_SENDER, ORIGINAL_SENDER);
        new Expectations() {{
            messageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;

            userMessage.getService().getValue();
            result = "CollabInfoValue";

            userMessage.getService().getType();
            result = "CollabInfoType";

            userMessage.getActionValue();
            result = "CollabInfoAction";

            userMessageServiceHelper.getProperties(userMessage);
            result = props;

        }};

        Map<String, String> messageProperties = backendNotificationService.getMessageProperties(messageLog, userMessage, newStatus, TIMESTAMP);

        assertThat(messageProperties.size(), is(16));
        assertThat(messageProperties.get(MessageConstants.STATUS_FROM), is(MessageStatus.SEND_ENQUEUED.toString()));
        assertThat(messageProperties.get(MessageConstants.STATUS_TO), is(MessageStatus.ACKNOWLEDGED.toString()));
        assertThat(messageProperties.get(MessageConstants.CHANGE_TIMESTAMP), is(String.valueOf(TIMESTAMP.getTime())));
        assertThat(messageProperties.get(MessageConstants.SERVICE), is("CollabInfoValue"));
        assertThat(messageProperties.get(MessageConstants.SERVICE_TYPE), is("CollabInfoType"));
        assertThat(messageProperties.get(MessageConstants.ACTION), is("CollabInfoAction"));
        assertThat(messageProperties.get(MessageConstants.FINAL_RECIPIENT), is(FINAL_RECIPIENT));
        assertThat(messageProperties.get(MessageConstants.ORIGINAL_SENDER), is(ORIGINAL_SENDER));

        new Verifications() {
        };

    }

    @Test
    public void getMessageProperties_noMessage_NoUserMessage(@Mocked UserMessageLog messageLog) {
        MessageStatus newStatus = MessageStatus.ACKNOWLEDGED;

        new Expectations() {{
            messageLog.getMessageStatus();
            result = null;

        }};

        Map<String, String> messageProperties = backendNotificationService.getMessageProperties(messageLog, null, newStatus, TIMESTAMP);

        assertThat(messageProperties.size(), is(2));
        assertThat(messageProperties.get(MessageConstants.STATUS_TO), is(MessageStatus.ACKNOWLEDGED.toString()));
        assertThat(messageProperties.get(MessageConstants.CHANGE_TIMESTAMP), is(String.valueOf(TIMESTAMP.getTime())));

        new FullVerifications() {
        };
    }

    @Test
    public void notifyMessageDeleted_test(@Mocked UserMessageLog userMessageLog, @Mocked UserMessage userMessage) {
        new Expectations() {{
            userMessage.isTestMessage();
            result = true;
        }};

        backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);
        new Verifications() {
        };

    }

    @Test
    public void notifyMessageDeleted_noBackend(@Mocked UserMessageLog userMessageLog, @Mocked UserMessage userMessage) {
        new Expectations() {{
            userMessage.isTestMessage();
            result = false;

            userMessageLog.getBackend();
            result = "";

            userMessage.toString();
        }};

        backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);

        new Verifications() {
        };
    }

    @Test
    public void notifyMessageDeleted(@Mocked UserMessageLog userMessageLog,
                                     @Mocked UserMessage userMessage) {

        Map<String, String> props = new HashMap<>();
        long messageEntityId = 1L;
        new Expectations(backendNotificationService) {{
            userMessage.isTestMessage();
            result = false;

            userMessageLog.getBackend();
            result = "backend";

            userMessage.getMessageId();
            result = MESSAGE_ID;

            userMessageLog.getEntityId();
            result = messageEntityId;

            userMessageService.getProperties(messageEntityId);
            result = props;

            backendConnectorDelegate.messageDeletedEvent(
                    "backend",
                    (MessageDeletedEvent) any);
            times = 1;

        }};

        backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);

        new FullVerifications() {
        };
    }

    @Test
    public void notifyAsync(
            @Mocked AsyncNotificationConfiguration asyncNotificationConfiguration,
            @Mocked NotificationType notificationType,
            @Mocked Queue backendNotificationQueue) {
        Map<String, String> properties = new HashMap<>();
        new Expectations() {{
            asyncNotificationConfiguration.getBackendNotificationQueue();
            result = backendNotificationQueue;

            asyncNotificationConfiguration.getBackendConnector().getName();
            result = "BackEnd";

            jmsManager.sendMessageToQueue((JmsMessage) any, backendNotificationQueue);
            times = 1;
        }};

        long messageEntityId = 1L;
        backendNotificationService.notifyAsync(asyncNotificationConfiguration, messageEntityId, MESSAGE_ID, notificationType, properties);

        new FullVerifications() {
        };
    }

    @Test
    public void notifySync_noPluginEventNotifier(
            @Mocked BackendConnector<?, ?> backendConnector,
            @Mocked AsyncNotificationConfiguration asyncNotificationConfiguration,
            @Mocked NotificationType notificationType) {
        Map<String, String> properties = new HashMap<>();
        new Expectations() {{
            backendConnector.getName();
            result = "backend";

            pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
            result = null;
        }};

        backendNotificationService.notifySync(backendConnector, 1L, MESSAGE_ID, notificationType, properties);

        new FullVerifications() {
        };
    }

    @Test
    public void notifySync_noListener(
            @Mocked BackendConnector<?, ?> backendConnector,
            @Mocked AsyncNotificationConfiguration asyncNotificationConfiguration,
            @Mocked NotificationType notificationType,
            @Mocked PluginEventNotifier pluginEventNotifier) {
        Map<String, String> properties = new HashMap<>();
        new Expectations() {{
            backendConnector.getName();
            result = "backend";

            pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
            result = pluginEventNotifier;
        }};

        final long messageEntityId = 1L;
        backendNotificationService.notifySync(backendConnector, messageEntityId, MESSAGE_ID, notificationType, properties);

        new FullVerifications() {{
            pluginEventNotifier.notifyPlugin(backendConnector, messageEntityId, MESSAGE_ID, properties);
            times = 1;
        }};
    }

    @Test
    public void notifySync_ok(
            @Mocked BackendConnector<?, ?> backendConnector,
            @Mocked NotificationListener asyncNotificationConfiguration,
            @Mocked NotificationType notificationType,
            @Mocked PluginEventNotifier pluginEventNotifier) {
        Map<String, String> properties = new HashMap<>();
        new Expectations() {{
            backendConnector.getName();
            result = "backend";

            pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
            result = pluginEventNotifier;
        }};

        final long messageEntityId = 1L;
        backendNotificationService.notifySync(backendConnector, messageEntityId, MESSAGE_ID, notificationType, properties);

        new FullVerifications() {{
            pluginEventNotifier.notifyPlugin(backendConnector, messageEntityId, MESSAGE_ID, properties);
            times = 1;
        }};
    }

}
