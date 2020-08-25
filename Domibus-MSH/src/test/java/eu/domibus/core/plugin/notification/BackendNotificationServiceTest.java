package eu.domibus.core.plugin.notification;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
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
import eu.domibus.core.message.*;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.BackendConnectorService;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.plugin.validation.SubmissionValidatorService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.CollaborationInfo;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE;
import static eu.domibus.common.NotificationType.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class BackendNotificationServiceTest {

    public static final String FINAL_RECIPIENT = "finalRecipient";
    public static final String MESSAGE_ID = "MessageId";
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
    private MessagingDao messagingDao;

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
    MessageExchangeService messageExchangeService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    UserMessageServiceHelper userMessageServiceHelper;

    @Injectable
    private EventService eventService;

    @Injectable
    private MessagingConfigurationManager messagingConfigurationManager;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

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
    BackendConnectorService backendConnectorService;

    @Test
    public void testValidateAndNotify_propertyNull(@Injectable final UserMessage userMessage) {
        String backendName = "backendName";
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        new Expectations(backendNotificationService) {{
            submissionValidatorService.validateSubmission(userMessage, backendName, notificationType);

            userMessageServiceHelper.getFinalRecipient(userMessage);
            result = "finalRecipient";

            userMessage.getMessageInfo().getMessageId();
            result = "messageId";

            backendNotificationService.notify(anyString, backendName, notificationType, null);
        }};

        backendNotificationService.validateAndNotify(userMessage, backendName, notificationType, null);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateAndNotify(@Injectable final UserMessage userMessage) {
        Map<String, Object> properties = new HashMap<>();

        String backendName = "backendName";
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        new Expectations(backendNotificationService) {{
            submissionValidatorService.validateSubmission(userMessage, backendName, notificationType);

            userMessageServiceHelper.getFinalRecipient(userMessage);
            result = FINAL_RECIPIENT;

            userMessage.getMessageInfo().getMessageId();
            result = "messageId";

            backendNotificationService.notify(anyString, backendName, notificationType, properties);
        }};


        backendNotificationService.validateAndNotify(userMessage, backendName, notificationType, properties);

        assertThat(properties.size(), is(1));
        assertThat(properties.get(MessageConstants.FINAL_RECIPIENT), is(FINAL_RECIPIENT));
        new FullVerifications() {
        };
    }

    @Test
    public void notifyParent(@Injectable NotificationType notificationType) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, notificationType, null);
            times = 1;
        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, notificationType);

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyWithNoConfiguredNotificationListener(
            @Injectable final NotificationType notificationType,
            @Injectable final BackendConnector backendConnector) {
        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);


        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorService.getRequiredNotificationTypeList(backendConnector);
            result = requiredNotifications;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = null;

            backendNotificationService.notifySync(backendConnector, null, MESSAGE_ID, NotificationType.MESSAGE_RECEIVED, null);
        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {
        };
    }

    @Test
    public void notify(
            @Injectable final AsyncNotificationConfiguration notificationListener,
            @Injectable final BackendConnector backendConnector,
            @Injectable final Queue queue) {

        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorService.getRequiredNotificationTypeList(backendConnector);
            result = requiredNotifications;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = notificationListener;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = notificationListener;

            backendNotificationService.shouldNotifyAsync(notificationListener);
            result = true;

            backendNotificationService.notifyAsync(notificationListener, MESSAGE_ID, NotificationType.MESSAGE_RECEIVED, null);
        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {{

        }};
    }

    @Test
    public void notifySync_propertiesNotNull(
            @Injectable final AsyncNotificationConfiguration notificationListener,
            @Injectable final BackendConnector backendConnector) {

        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorService.getRequiredNotificationTypeList(backendConnector);
            result = requiredNotifications;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = notificationListener;

            asyncNotificationConfigurationService.getAsyncPluginConfiguration(BACKEND_NAME);
            result = notificationListener;

            backendNotificationService.shouldNotifyAsync(notificationListener);
            result = false;

            backendNotificationService.notifySync(backendConnector, notificationListener, MESSAGE_ID, NotificationType.MESSAGE_RECEIVED, null);
        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {{

        }};
    }

    @Test
    public void notify_NoNotification(
            @Injectable final AsyncNotificationConfiguration notificationListener,
            @Injectable final BackendConnector backendConnector) {

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorService.getRequiredNotificationTypeList(backendConnector);
            result = null;

            backendConnector.getMode();
            result = BackendConnector.Mode.PUSH;

        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {
        };
    }

    @Test
    public void notify_NotificationNotMatchType(
            @Injectable final AsyncNotificationConfiguration notificationListener,
            @Injectable final Queue queue,
            @Injectable BackendConnector backendConnector
    ) {

        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(MESSAGE_STATUS_CHANGE);

        new Expectations(backendNotificationService) {{
            backendConnectorProvider.getBackendConnector(BACKEND_NAME);
            result = backendConnector;

            backendConnectorService.getRequiredNotificationTypeList(backendConnector);
            result = requiredNotifications;

            backendConnector.getMode();
            result = BackendConnector.Mode.PUSH;


        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyOfMessageStatusChange(@Injectable final UserMessageLog messageLog) {
        MessageStatus status = MessageStatus.ACKNOWLEDGED;

        new Expectations(backendNotificationService) {{
            backendNotificationService.notifyOfMessageStatusChange(null, messageLog, status, TIMESTAMP);
        }};

        backendNotificationService.notifyOfMessageStatusChange(messageLog, status, TIMESTAMP);

        new FullVerifications() {
        };
    }

    @Test
    public void notifyOfMessageStatusChange_isPluginNotificationDisabled(
            @Injectable final UserMessageLog messageLog,
            @Injectable final MessagingModuleConfiguration messageCommunicationConfiguration) {
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

            messageLog.getMessageId();
            result = messageId;

            messageLog.getMshRole();
            result = role;
        }};

        backendNotificationService.notifyOfMessageStatusChange(null, messageLog, status, TIMESTAMP);

        new FullVerificationsInOrder() {{
            eventService.enqueueMessageEvent(messageId, previousStatus, status, role);
            times = 1;
        }};
    }

    @Test
    public void notifyOfMessageStatusChange_notFragment(@Injectable final UserMessageLog messageLog,
                                                        @Injectable final MessagingModuleConfiguration messageCommunicationConfiguration,
                                                        @Injectable final UserMessage userMessage) {
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

            messageLog.getMessageId();
            result = messageId;

            messageLog.getMshRole();
            result = role;

            messageLog.getBackend();
            result = backend;

            messageLog.getMessageFragment();
            result = false;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            backendNotificationService.getMessageProperties(messageLog, userMessage, status, TIMESTAMP);
            result = new HashMap<String, Object>();

            backendNotificationService.notify(anyString, anyString, NotificationType.MESSAGE_STATUS_CHANGE, withAny(new HashMap<>()));
            times = 1;
        }};

        backendNotificationService.notifyOfMessageStatusChange(null, messageLog, status, TIMESTAMP);

        new FullVerificationsInOrder() {{
            eventService.enqueueMessageEvent(messageId, previousStatus, status, role);
            times = 1;
        }};
    }

    @Test
    public void notifyOfMessageStatusChange_fragment(@Injectable final UserMessageLog messageLog,
                                                     @Injectable final MessagingModuleConfiguration messageCommunicationConfiguration,
                                                     @Injectable final UserMessage userMessage) {
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

            messageLog.getMessageId();
            result = messageId;

            messageLog.getMshRole();
            result = role;

            messageLog.getBackend();
            result = backend;

            messageLog.getMessageFragment();
            result = true;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            backendNotificationService.getMessageProperties(messageLog, userMessage, status, TIMESTAMP);
            result = new HashMap<String, Object>();

            backendNotificationService.notify(anyString, anyString, NotificationType.MESSAGE_FRAGMENT_STATUS_CHANGE, withAny(new HashMap<>()));
            times = 1;
        }};

        backendNotificationService.notifyOfMessageStatusChange(null, messageLog, status, TIMESTAMP);

        new FullVerificationsInOrder() {{
            eventService.enqueueMessageEvent(messageId, previousStatus, status, role);
            times = 1;
        }};
    }

    @Test
    public void notifyOfMessageStatusChange_SameState(@Injectable final UserMessageLog messageLog,
                                                      @Injectable final MessagingModuleConfiguration messageCommunicationConfiguration,
                                                      @Injectable final UserMessage userMessage) {
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

            messageLog.getMessageId();
            result = messageId;

            messageLog.getMshRole();
            result = role;
        }};

        backendNotificationService.notifyOfMessageStatusChange(null, messageLog, status, TIMESTAMP);

        new FullVerifications() {{
            eventService.enqueueMessageEvent(messageId, previousStatus, status, role);
            times = 1;
        }};
    }


    @Test
    public void testNotifyMessageReceivedFailure(@Injectable UserMessage userMessage,
                                                 @Injectable ErrorResult errorResult,
                                                 @Injectable CollaborationInfo collaborationInfo) {

        String errorCodeName = "errorCode";
        String errorDetail = "errorDetail";
        String service = "my service";
        String serviceType = "service type";
        String action = "my action";

        List<Map<String, Object>> propertiesList = new ArrayList<>();

        new Expectations(backendNotificationService) {{
            errorResult.getErrorCode().getErrorCodeName();
            result = errorCodeName;

            errorResult.getErrorDetail();
            result = errorDetail;

            userMessage.isUserMessageFragment();
            result = true;

            userMessage.getCollaborationInfo();
            result = collaborationInfo;

            collaborationInfo.getService();
            result = service;

            collaborationInfo.getAction();
            result = action;

            collaborationInfo.getService().getValue();
            result = service;

            collaborationInfo.getService().getType();
            result = serviceType;

            collaborationInfo.getAction();
            result = action;

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

        new FullVerifications() {
        };
    }

    @Test
    public void testNotifyMessageReceivedFailure_PluginNotificationDisabled(@Injectable UserMessage userMessage,
                                                                            @Injectable ErrorResult errorResult,
                                                                            @Injectable CollaborationInfo collaborationInfo) {

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
    public void notifyOfSendSuccess(@Injectable UserMessageLog userMessageLog) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = true;
            times = 1;
        }};
        backendNotificationService.notifyOfSendSuccess(userMessageLog);
        new FullVerifications() {
        };
    }

    @Test
    public void notifyOfSendSuccess_notFragment(@Injectable UserMessageLog userMessageLog) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            userMessageLog.getMessageId();
            result = MESSAGE_ID;
            times = 1;

            userMessageLog.getMessageFragment();
            result = false;
            times = 1;

            userMessageLog.getBackend();
            result = BACKEND_NAME;
            times = 1;

            backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, MESSAGE_SEND_SUCCESS);
            times = 1;

            userMessageLogDao.setAsNotified(userMessageLog);
            times = 1;

            uiReplicationSignalService.messageChange(MESSAGE_ID);
            times = 1;
        }};
        backendNotificationService.notifyOfSendSuccess(userMessageLog);
        new FullVerifications() {
        };
    }

    @Test
    public void notifyOfSendSuccess_fragment(@Injectable UserMessageLog userMessageLog) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            userMessageLog.getMessageId();
            result = MESSAGE_ID;
            times = 1;

            userMessageLog.getMessageFragment();
            result = true;
            times = 1;

            userMessageLog.getBackend();
            result = BACKEND_NAME;
            times = 1;

            backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, MESSAGE_FRAGMENT_SEND_SUCCESS);
            times = 1;

            userMessageLogDao.setAsNotified(userMessageLog);
            times = 1;

            uiReplicationSignalService.messageChange(MESSAGE_ID);
            times = 1;
        }};

        backendNotificationService.notifyOfSendSuccess(userMessageLog);
        new FullVerifications() {
        };
    }

    @Test
    public void notifyMessageReceived_isPluginNotificationDisabled(
            @Injectable final BackendFilter matchingBackendFilter,
            @Injectable final UserMessage userMessage) {
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
            @Injectable final BackendFilter matchingBackendFilter,
            @Injectable final UserMessage userMessage) {

        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            userMessage.isUserMessageFragment();
            result = true;
            times = 1;

            backendNotificationService.notifyOfIncoming(matchingBackendFilter, userMessage, MESSAGE_FRAGMENT_RECEIVED, withAny(new HashMap<>()));
            times = 1;
        }};

        backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);

        new FullVerifications() {
        };
    }

    @Test
    public void notifyMessageReceived_NotFragment(
            @Injectable final BackendFilter matchingBackendFilter,
            @Injectable final UserMessage userMessage) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;
            times = 1;

            userMessage.isUserMessageFragment();
            result = false;
            times = 1;

            backendNotificationService.notifyOfIncoming(matchingBackendFilter, userMessage, MESSAGE_RECEIVED, withAny(new HashMap<>()));
            times = 1;
        }};

        backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);

        new FullVerifications() {
        };
    }

    @Test
    public void notifyPayloadSubmitted(
            @Injectable UserMessage userMessage,
            @Injectable PartInfo partInfo,
            @Injectable BackendConnector<?, ?> backendConnector) {
        List<PayloadSubmittedEvent> valueHolderForMultipleInvocations = new ArrayList<>();

        new Expectations(backendNotificationService) {{
            userMessageHandlerService.checkTestMessage(userMessage);
            result = false;

            userMessage.getMessageInfo().getMessageId();
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
            @Injectable UserMessage userMessage,
            @Injectable PartInfo partInfo) {

        new Expectations() {{
            userMessageHandlerService.checkTestMessage(userMessage);
            result = true;
        }};

        backendNotificationService.notifyPayloadSubmitted(userMessage, ORIGINAL_FILENAME, partInfo, BACKEND_NAME);

        new FullVerifications() {
        };
    }

    @Test
    public void notifyPayloadProcessed(
            @Injectable UserMessage userMessage,
            @Injectable PartInfo partInfo,
            @Injectable BackendConnector<?, ?> backendConnector) {
        List<PayloadProcessedEvent> payloadList = new ArrayList<>();

        new Expectations(backendNotificationService) {{
            userMessageHandlerService.checkTestMessage(userMessage);
            result = false;

            userMessage.getMessageInfo().getMessageId();
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
            @Injectable UserMessage userMessage,
            @Injectable PartInfo partInfo) {

        new Expectations() {{
            userMessageHandlerService.checkTestMessage(userMessage);
            result = true;
        }};

        backendNotificationService.notifyPayloadProcessed(userMessage, ORIGINAL_FILENAME, partInfo, BACKEND_NAME);

        new FullVerifications() {
        };

    }



    @Test
    public void notifyOfIncoming_matchingBackendFilterNull(
            @Injectable UserMessage userMessage,
            @Injectable NotificationType notificationType) {

        Map<String, Object> properties = new HashMap<>();

        new Expectations() {{
            userMessageServiceHelper.getFinalRecipient(userMessage);
            result = FINAL_RECIPIENT;

            userMessage.getMessageInfo().getMessageId();
            result = MESSAGE_ID;
        }};

        backendNotificationService.notifyOfIncoming(
                null,
                userMessage,
                notificationType,
                properties);

        //It's not fullVerification because it was raising an UnexpectedInvocation on Queue#toString() (for logging)
        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, unknownReceiverQueue);
            times = 1;
        }};
    }

    @Test
    public void notifyOfIncoming(
            @Injectable BackendFilter matchingBackendFilter,
            @Injectable UserMessage userMessage,
            @Injectable NotificationType notificationType,
            @Injectable Map<String, Object> properties) {

        new Expectations(backendNotificationService) {{
            matchingBackendFilter.getBackendName();
            result = BACKEND_NAME;

            backendNotificationService.validateAndNotify(userMessage, BACKEND_NAME, notificationType, properties);
            times = 1;
        }};

        backendNotificationService.notifyOfIncoming(
                matchingBackendFilter,
                userMessage,
                notificationType,
                properties);

        new FullVerifications() {
        };
    }

    @Test
    public void notifyOfIncoming(@Injectable UserMessage userMessage,
                                 @Injectable NotificationType notificationType,
                                 @Injectable Map<String, Object> properties,
                                 @Injectable BackendFilter matchingBackendFilter) {
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
    public void notifyOfSendFailure_isPluginNotificationDisabled(@Injectable UserMessageLog userMessageLog) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = true;
        }};
        backendNotificationService.notifyOfSendFailure(userMessageLog);
        new FullVerifications() {
        };
    }

    @Test
    public void notifyOfSendFailure_fragment(@Injectable UserMessageLog userMessageLog) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;

            userMessageLog.getMessageId();
            result = MESSAGE_ID;

            userMessageLog.getBackend();
            result = BACKEND_NAME;

            userMessageLog.getMessageFragment();
            result = true;

            backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_FRAGMENT_SEND_FAILURE);
        }};

        backendNotificationService.notifyOfSendFailure(userMessageLog);

        new FullVerifications() {{
            userMessageLogDao.setAsNotified(userMessageLog);
            times = 1;

            uiReplicationSignalService.messageChange(MESSAGE_ID);
            times = 1;
        }};
    }

    @Test
    public void notifyOfSendFailure_NoFragment(@Injectable UserMessageLog userMessageLog) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;

            userMessageLog.getMessageId();
            result = MESSAGE_ID;

            userMessageLog.getBackend();
            result = BACKEND_NAME;

            userMessageLog.getMessageFragment();
            result = false;

            backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_SEND_FAILURE);
            times = 1;
        }};

        backendNotificationService.notifyOfSendFailure(userMessageLog);

        new FullVerifications() {{
            userMessageLogDao.setAsNotified(userMessageLog);
            times = 1;

            uiReplicationSignalService.messageChange(MESSAGE_ID);
            times = 1;
        }};
    }

    @Test
    public void getMessageProperties(@Injectable MessageLog messageLog,
                                     @Injectable UserMessage userMessage) {
        MessageStatus newStatus = MessageStatus.ACKNOWLEDGED;

        new Expectations() {{
            messageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;

            messageLog.getMessageId();
            result = MESSAGE_ID;

            userMessage.getCollaborationInfo().getService().getValue();
            result = "CollabInfoValue";

            userMessage.getCollaborationInfo().getService().getType();
            result = "CollabInfoType";

            userMessage.getCollaborationInfo().getAction();
            result = "CollabInfoAction";

        }};

        Map<String, Object> messageProperties = backendNotificationService.getMessageProperties(messageLog, userMessage, newStatus, TIMESTAMP);

        assertThat(messageProperties.size(), is(6));
        assertThat(messageProperties.get(MessageConstants.STATUS_FROM), is(MessageStatus.SEND_ENQUEUED.toString()));
        assertThat(messageProperties.get(MessageConstants.STATUS_TO), is(MessageStatus.ACKNOWLEDGED.toString()));
        assertThat(messageProperties.get(MessageConstants.CHANGE_TIMESTAMP), is(TIMESTAMP.getTime()));
        assertThat(messageProperties.get(MessageConstants.SERVICE), is("CollabInfoValue"));
        assertThat(messageProperties.get(MessageConstants.SERVICE_TYPE), is("CollabInfoType"));
        assertThat(messageProperties.get(MessageConstants.ACTION), is("CollabInfoAction"));

        new FullVerifications() {
        };

    }

    @Test
    public void getMessageProperties_noMessage_NoUserMessage(@Injectable MessageLog messageLog,
                                                             @Injectable UserMessage userMessage) {
        MessageStatus newStatus = MessageStatus.ACKNOWLEDGED;

        new Expectations() {{
            messageLog.getMessageStatus();
            result = null;

        }};

        Map<String, Object> messageProperties = backendNotificationService.getMessageProperties(messageLog, null, newStatus, TIMESTAMP);

        assertThat(messageProperties.size(), is(2));
        assertThat(messageProperties.get(MessageConstants.STATUS_TO), is(MessageStatus.ACKNOWLEDGED.toString()));
        assertThat(messageProperties.get(MessageConstants.CHANGE_TIMESTAMP), is(TIMESTAMP.getTime()));

        new FullVerifications() {
        };
    }


}
