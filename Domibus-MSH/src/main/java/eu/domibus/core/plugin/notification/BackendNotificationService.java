package eu.domibus.core.plugin.notification;

import eu.domibus.api.jms.JMSManager;
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
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.PluginEventNotifier;
import eu.domibus.plugin.PluginEventNotifierProvider;
import eu.domibus.plugin.notification.AsyncNotificationListener;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@Service("backendNotificationService")
public class BackendNotificationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendNotificationService.class);

    @Autowired
    JMSManager jmsManager;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    @Qualifier("unknownReceiverQueue")
    protected Queue unknownReceiverQueue;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private EventService eventService;

    @Autowired
    private MessagingConfigurationManager messagingConfigurationManager;

    @Autowired
    private UserMessageServiceHelper userMessageServiceHelper;

    @Autowired
    private UserMessageHandlerService userMessageHandlerService;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected PluginEventNotifierProvider pluginEventNotifierProvider;

    @Autowired
    protected SubmissionValidatorService submissionValidatorService;

    @Autowired
    protected BackendConnectorProvider backendConnectorProvider;

    @Autowired
    protected BackendConnectorDelegate backendConnectorDelegate;

    @Autowired
    protected BackendConnectorService backendConnectorService;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyMessageReceivedFailure(final UserMessage userMessage, ErrorResult errorResult) {
        if (isPluginNotificationDisabled()) {
            return;
        }
        final Map<String, Object> properties = new HashMap<>();
        if (errorResult.getErrorCode() != null) {
            properties.put(MessageConstants.ERROR_CODE, errorResult.getErrorCode().getErrorCodeName());
        }
        properties.put(MessageConstants.ERROR_DETAIL, errorResult.getErrorDetail());
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED_FAILURE;
        if (userMessage.isUserMessageFragment()) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED_FAILURE;
        }
        CollaborationInfo collaborationInfo = userMessage.getCollaborationInfo();
        if (collaborationInfo != null) {
            properties.put(MessageConstants.SERVICE, collaborationInfo.getService().getValue());
            properties.put(MessageConstants.SERVICE_TYPE, collaborationInfo.getService().getType());
            properties.put(MessageConstants.ACTION, collaborationInfo.getAction());
        }
        notifyOfIncoming(userMessage, notificationType, properties);
    }

    public void notifyMessageReceived(final BackendFilter matchingBackendFilter, final UserMessage userMessage) {
        if (isPluginNotificationDisabled()) {
            return;
        }
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        if (userMessage.isUserMessageFragment()) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED;
        }

        notifyOfIncoming(matchingBackendFilter, userMessage, notificationType, new HashMap<>());
    }

    public void notifyMessageDeleted(String messageId, UserMessageLog userMessageLog) {
        if (userMessageLog == null) {
            LOG.warn("Could not find message with id [{}]", messageId);
            return;
        }
        if (userMessageLog.isTestMessage()) {
            return;
        }
        String backend = userMessageLog.getBackend();
        if (StringUtils.isEmpty(backend)) {
            LOG.warn("Could not find backend for message with id [{}]", messageId);
            return;
        }

        MessageDeletedEvent messageDeletedEvent = new MessageDeletedEvent();
        messageDeletedEvent.setMessageId(messageId);
        backendConnectorDelegate.messageDeletedEvent(backend, messageDeletedEvent);

    }

    public void notifyPayloadSubmitted(final UserMessage userMessage, String originalFilename, PartInfo partInfo, String backendName) {
        if (userMessageHandlerService.checkTestMessage(userMessage)) {
            LOG.debug("Payload submitted notifications are not enabled for test messages [{}]", userMessage);
            return;
        }

        final BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backendName);
        PayloadSubmittedEvent payloadSubmittedEvent = new PayloadSubmittedEvent();
        payloadSubmittedEvent.setCid(partInfo.getHref());
        payloadSubmittedEvent.setFileName(originalFilename);
        payloadSubmittedEvent.setMessageId(userMessage.getMessageInfo().getMessageId());
        payloadSubmittedEvent.setMime(partInfo.getMime());
        backendConnector.payloadSubmittedEvent(payloadSubmittedEvent);
    }

    public void notifyPayloadProcessed(final UserMessage userMessage, String originalFilename, PartInfo partInfo, String backendName) {
        if (userMessageHandlerService.checkTestMessage(userMessage)) {
            LOG.debug("Payload processed notifications are not enabled for test messages [{}]", userMessage);
            return;
        }

        final BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backendName);
        PayloadProcessedEvent payloadProcessedEvent = new PayloadProcessedEvent();
        payloadProcessedEvent.setCid(partInfo.getHref());
        payloadProcessedEvent.setFileName(originalFilename);
        payloadProcessedEvent.setMessageId(userMessage.getMessageInfo().getMessageId());
        payloadProcessedEvent.setMime(partInfo.getMime());
        backendConnector.payloadProcessedEvent(payloadProcessedEvent);
    }

    protected void notifyOfIncoming(final BackendFilter matchingBackendFilter, final UserMessage userMessage, final NotificationType notificationType, Map<String, Object> properties) {
        if (matchingBackendFilter == null) {
            LOG.error("No backend responsible for message [{}] found. Sending notification to [{}]", userMessage.getMessageInfo().getMessageId(), unknownReceiverQueue);
            String finalRecipient = userMessageServiceHelper.getFinalRecipient(userMessage);
            properties.put(MessageConstants.FINAL_RECIPIENT, finalRecipient);
            jmsManager.sendMessageToQueue(new NotifyMessageCreator(userMessage.getMessageInfo().getMessageId(), notificationType, properties).createMessage(), unknownReceiverQueue);
            return;
        }

        validateAndNotify(userMessage, matchingBackendFilter.getBackendName(), notificationType, properties);
    }

    protected void notifyOfIncoming(final UserMessage userMessage, final NotificationType notificationType, Map<String, Object> properties) {
        final BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(userMessage);
        notifyOfIncoming(matchingBackendFilter, userMessage, notificationType, properties);
    }

    protected void validateAndNotify(UserMessage userMessage, String backendName, NotificationType notificationType, Map<String, Object> properties) {
        LOG.info("Notifying backend [{}] of message [{}] and notification type [{}]", backendName, userMessage.getMessageInfo().getMessageId(), notificationType);

        submissionValidatorService.validateSubmission(userMessage, backendName, notificationType);
        String finalRecipient = userMessageServiceHelper.getFinalRecipient(userMessage);
        if (properties != null) {
            properties.put(MessageConstants.FINAL_RECIPIENT, finalRecipient);
        }
        notify(userMessage.getMessageInfo().getMessageId(), backendName, notificationType, properties);
    }

    protected void notify(String messageId, String backendName, NotificationType notificationType) {
        notify(messageId, backendName, notificationType, null);
    }

    protected void notify(String messageId, String backendName, NotificationType notificationType, Map<String, Object> properties) {
        BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backendName);
        if (backendConnector == null) {
            LOG.warn("No backend connector found for backend [{}]", backendName);
            return;
        }

        List<NotificationType> requiredNotificationTypeList = backendConnectorService.getRequiredNotificationTypeList(backendConnector);
        LOG.debug("Required notifications [{}] for backend [{}]", requiredNotificationTypeList, backendName);
        if (requiredNotificationTypeList == null || !requiredNotificationTypeList.contains(notificationType)) {
            LOG.debug("No plugin notification sent for message [{}]. Notification type [{}], mode [{}]", messageId, notificationType, backendConnector.getMode());
            return;
        }

        if (properties != null) {
            String finalRecipient = (String) properties.get(MessageConstants.FINAL_RECIPIENT);
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}] and finalRecipient [{}]", backendName, messageId, notificationType, finalRecipient);
        } else {
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}]", backendName, messageId, notificationType);
        }

        AsyncNotificationListener notificationListener = routingService.getNotificationListener(backendName);
        if (shouldNotifyAsync(notificationListener)) {
            notifyAsync(notificationListener, messageId, notificationType, properties);
            return;
        }

        notifySync(backendConnector, notificationListener, messageId, notificationType, properties);
    }

    protected boolean shouldNotifyAsync(AsyncNotificationListener asyncNotificationListener) {
        return asyncNotificationListener != null && asyncNotificationListener.getBackendNotificationQueue() != null;
    }

    protected void notifyAsync(AsyncNotificationListener asyncNotificationListener, String messageId, NotificationType notificationType, Map<String, Object> properties) {
        Queue backendNotificationQueue = asyncNotificationListener.getBackendNotificationQueue();
        if (backendNotificationQueue == null) {
            LOG.debug("Could not async notify for connector [{}]", asyncNotificationListener.getBackendConnector().getName());
            return;
        }
        LOG.debug("Notifying plugin [{}] using queue", asyncNotificationListener.getBackendConnector().getName());
        jmsManager.sendMessageToQueue(new NotifyMessageCreator(messageId, notificationType, properties).createMessage(), backendNotificationQueue);
    }

    protected void notifySync(BackendConnector<?, ?> backendConnector,
                              AsyncNotificationListener asyncNotificationListener,
                              String messageId,
                              NotificationType notificationType,
                              Map<String, Object> properties) {
        LOG.debug("Notifying plugin [{}] using callback", backendConnector.getName());
        PluginEventNotifier pluginEventNotifier = pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
        if (pluginEventNotifier == null) {
            LOG.warn("Could not get plugin event notifier for notification type [{}]", notificationType);
            return;
        }

        pluginEventNotifier.notifyPlugin(backendConnector, messageId, properties);

        //for backward compatibility
        if (asyncNotificationListener != null && asyncNotificationListener instanceof NotificationListener) {
            NotificationListener notificationListener = (NotificationListener) asyncNotificationListener;
            notificationListener.notify(messageId, notificationType, properties);
        }
    }

    public void notifyOfSendFailure(UserMessageLog userMessageLog) {
        if (isPluginNotificationDisabled()) {
            return;
        }
        final String messageId = userMessageLog.getMessageId();
        final String backendName = userMessageLog.getBackend();
        NotificationType notificationType = NotificationType.MESSAGE_SEND_FAILURE;
        if (BooleanUtils.isTrue(userMessageLog.getMessageFragment())) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_SEND_FAILURE;
        }

        notify(messageId, backendName, notificationType);
        userMessageLogDao.setAsNotified(userMessageLog);

        uiReplicationSignalService.messageChange(messageId);
    }

    public void notifyOfSendSuccess(final UserMessageLog userMessageLog) {
        if (isPluginNotificationDisabled()) {
            return;
        }
        String messageId = userMessageLog.getMessageId();
        NotificationType notificationType = NotificationType.MESSAGE_SEND_SUCCESS;
        if (BooleanUtils.isTrue(userMessageLog.getMessageFragment())) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_SEND_SUCCESS;
        }

        notify(messageId, userMessageLog.getBackend(), notificationType);
        userMessageLogDao.setAsNotified(userMessageLog);

        uiReplicationSignalService.messageChange(messageId);
    }

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void notifyOfMessageStatusChange(UserMessageLog messageLog, MessageStatus newStatus, Timestamp changeTimestamp) {
        notifyOfMessageStatusChange(null, messageLog, newStatus, changeTimestamp);
    }

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void notifyOfMessageStatusChange(UserMessage userMessage, UserMessageLog messageLog, MessageStatus newStatus, Timestamp changeTimestamp) {
        final MessagingModuleConfiguration messagingConfiguration = messagingConfigurationManager.getConfiguration();
        if (messagingConfiguration.shouldMonitorMessageStatus(newStatus)) {
            eventService.enqueueMessageEvent(messageLog.getMessageId(), messageLog.getMessageStatus(), newStatus, messageLog.getMshRole());
        }

        if (isPluginNotificationDisabled()) {
            return;
        }
        final String messageId = messageLog.getMessageId();
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        if (messageLog.getMessageStatus() == newStatus) {
            LOG.debug("Notification not sent: message status has not changed [{}]", newStatus);
            return;
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_STATUS_CHANGED, messageLog.getMessageStatus(), newStatus);

        //TODO check if it is needed
        if (userMessage == null) {
            LOG.debug("Getting UserMessage with id [{}]", messageId);
            userMessage = messagingDao.findUserMessageByMessageId(messageId);
        }

        final Map<String, Object> messageProperties = getMessageProperties(messageLog, userMessage, newStatus, changeTimestamp);
        NotificationType notificationType = NotificationType.MESSAGE_STATUS_CHANGE;
        if (BooleanUtils.isTrue(messageLog.getMessageFragment())) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_STATUS_CHANGE;
        }

        notify(messageLog.getMessageId(), messageLog.getBackend(), notificationType, messageProperties);
    }

    protected Map<String, Object> getMessageProperties(MessageLog messageLog, UserMessage userMessage, MessageStatus newStatus, Timestamp changeTimestamp) {
        Map<String, Object> properties = new HashMap<>();
        if (messageLog.getMessageStatus() != null) {
            properties.put(MessageConstants.STATUS_FROM, messageLog.getMessageStatus().toString());
        }
        properties.put(MessageConstants.STATUS_TO, newStatus.toString());
        properties.put(MessageConstants.CHANGE_TIMESTAMP, changeTimestamp.getTime());

        if (userMessage != null) {
            LOG.debug("Adding the service and action properties for message [{}]", messageLog.getMessageId());

            properties.put(MessageConstants.SERVICE, userMessage.getCollaborationInfo().getService().getValue());
            properties.put(MessageConstants.SERVICE_TYPE, userMessage.getCollaborationInfo().getService().getType());
            properties.put(MessageConstants.ACTION, userMessage.getCollaborationInfo().getAction());
        }
        return properties;
    }

    protected boolean isPluginNotificationDisabled() {
        return !domibusPropertyProvider.getBooleanProperty(DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE);
    }
}
