package eu.domibus.core.plugin.notification;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.*;
import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.*;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.plugin.BackendConnectorHelper;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.plugin.validation.SubmissionValidatorService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import org.apache.commons.collections.CollectionUtils;
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
import static eu.domibus.jms.spi.InternalJMSConstants.UNKNOWN_RECEIVER_QUEUE;
import static eu.domibus.messaging.MessageConstants.*;
import static java.util.stream.Collectors.toList;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@Service("backendNotificationService")
public class BackendNotificationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendNotificationService.class);

    @Autowired
    protected JMSManager jmsManager;

    @Autowired
    protected RoutingService routingService;

    @Autowired
    protected AsyncNotificationConfigurationService asyncNotificationConfigurationService;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    @Qualifier(UNKNOWN_RECEIVER_QUEUE)
    protected Queue unknownReceiverQueue;

    @Autowired
    protected UserMessageDao userMessageDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected EventService eventService;

    @Autowired
    protected MessagingConfigurationManager messagingConfigurationManager;

    @Autowired
    private UserMessageServiceHelper userMessageServiceHelper;

    @Autowired
    protected TestMessageValidator testMessageValidator;

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
    protected BackendConnectorHelper backendConnectorHelper;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyMessageReceivedFailure(final UserMessage userMessage, ErrorResult errorResult) {
        LOG.debug("Notify message receive failure");

        if (isPluginNotificationDisabled()) {
            LOG.debug("Plugin notification is disabled.");
            return;
        }
        final Map<String, String> properties = new HashMap<>();
        if (errorResult.getErrorCode() != null) {
            properties.put(MessageConstants.ERROR_CODE, errorResult.getErrorCode().getErrorCodeName());
        }
        properties.put(MessageConstants.ERROR_DETAIL, errorResult.getErrorDetail());
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED_FAILURE;
        if (userMessage.isMessageFragment()) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED_FAILURE;
        }

        fillEventProperties(userMessage, properties);
        notifyOfIncoming(userMessage, notificationType, properties);
    }


    @Timer(clazz = BackendNotificationService.class, value = "notifyMessageReceived")
    @Counter(clazz = BackendNotificationService.class, value = "notifyMessageReceived")
    public void notifyMessageReceived(final BackendFilter matchingBackendFilter, final UserMessage userMessage) {
        if (isPluginNotificationDisabled()) {
            return;
        }
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        if (userMessage.isMessageFragment()) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED;
        }

        final Map<String, String> properties = new HashMap<>();
        fillEventProperties(userMessage, properties);
        notifyOfIncoming(matchingBackendFilter, userMessage, notificationType, properties);
    }

    public void notifyMessageDeleted(List<UserMessageLogDto> userMessageLogs) {
        if (CollectionUtils.isEmpty(userMessageLogs)) {
            LOG.warn("Empty notification list of userMessageLogs");
            return;
        }

        final List<UserMessageLogDto> userMessageLogsToNotify = userMessageLogs
                .stream()
                .filter(userMessageLog -> !userMessageLog.isTestMessage())
                .collect(toList());

        if (CollectionUtils.isEmpty(userMessageLogsToNotify)) {
            LOG.info("No more delete message notifications.");
            return;
        }
        List<String> backends = userMessageLogsToNotify
                .stream()
                .map(UserMessageLogDto::getBackend)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(toList());

        LOG.debug("Following backends will be notified with message delete events [{}]", backends);

        if (CollectionUtils.isEmpty(backends)) {
            LOG.warn("Could not find any backend for batch delete notification");
            return;
        }

        backends.forEach(backend ->
        {
            List<MessageDeletedEvent> allMessageIdsForBackend =
                    getAllMessageIdsForBackend(backend, userMessageLogsToNotify);
            createMessageDeleteBatchEvent(backend, allMessageIdsForBackend);
        });
    }

    protected void createMessageDeleteBatchEvent(String backend, List<MessageDeletedEvent> messageDeletedEvents) {
        MessageDeletedBatchEvent messageDeletedBatchEvent = new MessageDeletedBatchEvent();
        messageDeletedBatchEvent.setMessageDeletedEvents(messageDeletedEvents);
        backendConnectorDelegate.messageDeletedBatchEvent(backend, messageDeletedBatchEvent);
    }

    protected List<MessageDeletedEvent> getAllMessageIdsForBackend(String backend, final List<UserMessageLogDto> userMessageLogs) {
        List<MessageDeletedEvent> messageIds = userMessageLogs
                .stream()
                .filter(userMessageLog -> userMessageLog.getBackend().equals(backend))
                .map(this::getMessageDeletedEvent)
                .collect(toList());
        LOG.debug("There are [{}] delete messages to notify for backend [{}]", messageIds.size(), backend);
        return messageIds;
    }

    protected MessageDeletedEvent getMessageDeletedEvent(UserMessageLogDto userMessageLogDto) {
        return getMessageDeletedEvent(userMessageLogDto.getMessageId(), userMessageLogDto.getProperties());
    }

    protected MessageDeletedEvent getMessageDeletedEvent(String messageId, Map<String, String> properties) {
        MessageDeletedEvent messageDeletedEvent = new MessageDeletedEvent();
        messageDeletedEvent.setMessageId(messageId);
        messageDeletedEvent.addProperty(FINAL_RECIPIENT, properties.get(FINAL_RECIPIENT));
        messageDeletedEvent.addProperty(ORIGINAL_SENDER, properties.get(ORIGINAL_SENDER));
        return messageDeletedEvent;
    }

    public void notifyMessageDeleted(UserMessage userMessage, UserMessageLog userMessageLog) {
        if (userMessageLog == null) {
            LOG.warn("Could not find message with id [{}]", userMessage);
            return;
        }
        if (userMessage.isTestMessage()) {
            LOG.debug("Message [{}] is of type test: no notification for message deleted", userMessage);
            return;
        }
        String backend = userMessageLog.getBackend();
        if (StringUtils.isEmpty(backend)) {
            LOG.warn("Could not find backend for message with id [{}]", userMessage);
            return;
        }

        Map<String, String> properties = userMessageService.getProperties(userMessageLog.getEntityId());
        MessageDeletedEvent messageDeletedEvent = getMessageDeletedEvent(
                userMessage.getMessageId(),
                properties);
        backendConnectorDelegate.messageDeletedEvent(
                backend,
                messageDeletedEvent);
    }


    public void notifyPayloadSubmitted(final UserMessage userMessage, String originalFilename, PartInfo partInfo, String backendName) {
        if (BooleanUtils.isTrue(testMessageValidator.checkTestMessage(userMessage))) {
            LOG.debug("Payload submitted notifications are not enabled for test messages [{}]", userMessage);
            return;
        }

        final BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backendName);
        PayloadSubmittedEvent payloadSubmittedEvent = new PayloadSubmittedEvent();
        payloadSubmittedEvent.setCid(partInfo.getHref());
        payloadSubmittedEvent.setFileName(originalFilename);
        payloadSubmittedEvent.setMessageEntityId(userMessage.getEntityId());
        payloadSubmittedEvent.setMessageId(userMessage.getMessageId());
        payloadSubmittedEvent.setMime(partInfo.getMime());
        backendConnector.payloadSubmittedEvent(payloadSubmittedEvent);
    }


    public void notifyPayloadProcessed(final UserMessage userMessage, String originalFilename, PartInfo partInfo, String backendName) {
        if (BooleanUtils.isTrue(testMessageValidator.checkTestMessage(userMessage))) {
            LOG.debug("Payload processed notifications are not enabled for test messages [{}]", userMessage);
            return;
        }

        final BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backendName);
        PayloadProcessedEvent payloadProcessedEvent = new PayloadProcessedEvent();
        payloadProcessedEvent.setCid(partInfo.getHref());
        payloadProcessedEvent.setFileName(originalFilename);
        payloadProcessedEvent.setMessageEntityId(userMessage.getEntityId());
        payloadProcessedEvent.setMessageId(userMessage.getMessageId());
        payloadProcessedEvent.setMime(partInfo.getMime());
        backendConnector.payloadProcessedEvent(payloadProcessedEvent);
    }

    protected void notifyOfIncoming(final BackendFilter matchingBackendFilter, final UserMessage userMessage, final NotificationType notificationType, Map<String, String> properties) {
        if (matchingBackendFilter == null) {
            LOG.error("No backend responsible for message [{}] found. Sending notification to [{}]", userMessage.getMessageId(), unknownReceiverQueue);
            jmsManager.sendMessageToQueue(new NotifyMessageCreator(userMessage.getEntityId(), userMessage.getMessageId(), notificationType, properties).createMessage(), unknownReceiverQueue);
            return;
        }

        notify(userMessage, matchingBackendFilter.getBackendName(), notificationType, properties);
    }

    protected void fillEventProperties(final UserMessage userMessage, Map<String, String> target) {
        if(userMessage == null) {
            return;
        }

        Map<String, String> props = userMessageServiceHelper.getProperties(userMessage);
        target.putAll(props);

        target.put(REF_TO_MESSAGE_ID, userMessage.getRefToMessageId());
        target.put(CONVERSATION_ID, userMessage.getConversationId());

        final PartyId partyFrom = userMessageServiceHelper.getPartyFrom(userMessage);
        if(partyFrom != null) {
            target.put(FROM_PARTY_ID, partyFrom.getValue());
            target.put(FROM_PARTY_TYPE, partyFrom.getType());
        }
        target.put(FROM_PARTY_ROLE, userMessageServiceHelper.getPartyFromRole(userMessage));

        final PartyId partyTo = userMessageServiceHelper.getPartyTo(userMessage);
        if(partyTo != null) {
            target.put(TO_PARTY_ID, partyTo.getValue());
            target.put(TO_PARTY_TYPE, partyTo.getType());
        }
        target.put(TO_PARTY_ROLE, userMessageServiceHelper.getPartyToRole(userMessage));

        ServiceEntity service = userMessage.getService();
        if (service != null) {
            target.put(MessageConstants.SERVICE_TYPE, service.getType());
            target.put(MessageConstants.SERVICE, service.getValue());
        }
        String actionValue = userMessage.getActionValue();
        target.put(MessageConstants.ACTION, actionValue);
    }

    protected void notifyOfIncoming(final UserMessage userMessage, final NotificationType notificationType, Map<String, String> properties) {
        final BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(userMessage);
        notifyOfIncoming(matchingBackendFilter, userMessage, notificationType, properties);
    }

    protected void notify(UserMessage userMessage, String backendName, NotificationType notificationType, Map<String, String> properties) {
        LOG.info("Notifying backend [{}] of message [{}] and notification type [{}]", backendName, userMessage.getMessageId(), notificationType);

        BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backendName);
        if (backendConnector == null) {
            LOG.warn("No backend connector found for backend [{}]", backendName);
            return;
        }
        final String messageId = userMessage.getMessageId();
        final long messageEntityId = userMessage.getEntityId();

        List<NotificationType> requiredNotificationTypeList = backendConnectorHelper.getRequiredNotificationTypeList(backendConnector);
        LOG.debug("Required notifications [{}] for backend [{}]", requiredNotificationTypeList, backendName);
        if (requiredNotificationTypeList == null || !requiredNotificationTypeList.contains(notificationType)) {
            LOG.debug("No plugin notification sent for message [{}]. Notification type [{}]]", messageId, notificationType);
            return;
        }

        if (properties != null) {
            String finalRecipient = properties.get(FINAL_RECIPIENT);
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}] and finalRecipient [{}]", backendName, messageId, notificationType, finalRecipient);
        } else {
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}]", backendName, messageId, notificationType);
        }

        AsyncNotificationConfiguration asyncNotificationConfiguration = asyncNotificationConfigurationService.getAsyncPluginConfiguration(backendName);
        if (shouldNotifyAsync(asyncNotificationConfiguration)) {
            notifyAsync(asyncNotificationConfiguration, messageEntityId, messageId, notificationType, properties);
            return;
        }

        notifySync(backendConnector, messageEntityId, messageId, notificationType, properties);
    }

    protected boolean shouldNotifyAsync(AsyncNotificationConfiguration asyncNotificationConfiguration) {
        return asyncNotificationConfiguration != null && asyncNotificationConfiguration.getBackendNotificationQueue() != null;
    }

    protected void notifyAsync(AsyncNotificationConfiguration asyncNotificationConfiguration, Long messageEntityId, String messageId, NotificationType notificationType, Map<String, String> properties) {
        Queue backendNotificationQueue = asyncNotificationConfiguration.getBackendNotificationQueue();
        LOG.debug("Notifying plugin [{}] using queue", asyncNotificationConfiguration.getBackendConnector().getName());
        jmsManager.sendMessageToQueue(new NotifyMessageCreator(messageEntityId, messageId, notificationType, properties).createMessage(), backendNotificationQueue);
    }

    protected void notifySync(BackendConnector<?, ?> backendConnector,
                              long messageEntityId,
                              String messageId,
                              NotificationType notificationType,
                              Map<String, String> properties) {
        LOG.debug("Notifying plugin [{}] using callback", backendConnector.getName());
        PluginEventNotifier pluginEventNotifier = pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
        if (pluginEventNotifier == null) {
            LOG.warn("Could not get plugin event notifier for notification type [{}]", notificationType);
            return;
        }

        pluginEventNotifier.notifyPlugin(backendConnector, messageEntityId, messageId, properties);
    }

    @Transactional
    public void notifyOfSendFailure(final UserMessage userMessage, UserMessageLog userMessageLog) {
        if (isPluginNotificationDisabled()) {
            return;
        }
        final String backendName = userMessageLog.getBackend();
        NotificationType notificationType = NotificationType.MESSAGE_SEND_FAILURE;
        if (BooleanUtils.isTrue(userMessage.isMessageFragment())) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_SEND_FAILURE;
        }

        Map<String, String> properties = new HashMap<>();
        fillEventProperties(userMessage, properties);
        notify(userMessage, backendName, notificationType, properties);
        userMessageLogDao.setAsNotified(userMessageLog);
    }

    @Timer(clazz = BackendNotificationService.class, value = "notifyOfSendSuccess")
    @Counter(clazz = BackendNotificationService.class, value = "notifyOfSendSuccess")
    @Transactional
    public void notifyOfSendSuccess(final UserMessage userMessage, final UserMessageLog userMessageLog) {
        if (isPluginNotificationDisabled()) {
            return;
        }
        NotificationType notificationType = NotificationType.MESSAGE_SEND_SUCCESS;
        if (BooleanUtils.isTrue(userMessage.isMessageFragment())) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_SEND_SUCCESS;
        }

        Map<String, String> properties = new HashMap<>();
        fillEventProperties(userMessage, properties);
        notify(userMessage, userMessageLog.getBackend(), notificationType, properties);
        userMessageLogDao.setAsNotified(userMessageLog);
    }


    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    @Transactional
    public void notifyOfMessageStatusChange(String messageId, UserMessageLog messageLog, MessageStatus newStatus, Timestamp changeTimestamp) {
//        UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        UserMessage userMessage = userMessageDao.findByEntityId(messageLog.getEntityId());
        notifyOfMessageStatusChange(userMessage, messageLog, newStatus, changeTimestamp);
    }

    @Timer(clazz = BackendNotificationService.class, value = "notifyOfMessageStatusChange")
    @Counter(clazz = BackendNotificationService.class, value = "notifyOfMessageStatusChange")
    @Transactional
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public void notifyOfMessageStatusChange(UserMessage userMessage, UserMessageLog messageLog, MessageStatus newStatus, Timestamp changeTimestamp) {
        final MessagingModuleConfiguration messagingConfiguration = messagingConfigurationManager.getConfiguration();
        if (messagingConfiguration.shouldMonitorMessageStatus(newStatus)) {
            eventService.enqueueMessageEvent(userMessage.getMessageId(), messageLog.getMessageStatus(), newStatus, messageLog.getMshRole().getRole());
        }

        if (isPluginNotificationDisabled()) {
            return;
        }
        final String messageId = userMessage.getMessageId();
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        if (messageLog.getMessageStatus() == newStatus) {
            LOG.debug("Notification not sent: message status has not changed [{}]", newStatus);
            return;
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_STATUS_CHANGED, messageLog.getMessageStatus(), newStatus);

        final Map<String, String> messageProperties = getMessageProperties(messageLog, userMessage, newStatus, changeTimestamp);
        NotificationType notificationType = NotificationType.MESSAGE_STATUS_CHANGE;
        if (BooleanUtils.isTrue(userMessage.isMessageFragment())) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_STATUS_CHANGE;
        }

        notify(userMessage, messageLog.getBackend(), notificationType, messageProperties);
    }

    protected Map<String, String> getMessageProperties(UserMessageLog messageLog, UserMessage userMessage, MessageStatus newStatus, Timestamp changeTimestamp) {
        Map<String, String> properties = new HashMap<>();
        if (messageLog.getMessageStatus() != null) {
            properties.put(MessageConstants.STATUS_FROM, messageLog.getMessageStatus().toString());
        }
        properties.put(MessageConstants.STATUS_TO, newStatus.toString());
        properties.put(MessageConstants.CHANGE_TIMESTAMP, String.valueOf(changeTimestamp.getTime()));

        fillEventProperties(userMessage, properties);
        return properties;
    }

    protected boolean isPluginNotificationDisabled() {
        return !domibusPropertyProvider.getBooleanProperty(DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE);
    }
}
