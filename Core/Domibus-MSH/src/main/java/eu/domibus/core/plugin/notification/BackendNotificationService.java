package eu.domibus.core.plugin.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.*;
import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.TestMessageValidator;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageServiceHelper;
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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
    protected PluginEventNotifierProvider pluginEventNotifierProvider;

    @Autowired
    protected SubmissionValidatorService submissionValidatorService;

    @Autowired
    protected BackendConnectorProvider backendConnectorProvider;

    @Autowired
    protected BackendConnectorDelegate backendConnectorDelegate;

    @Autowired
    protected BackendConnectorHelper backendConnectorHelper;

    @Autowired
    @Qualifier("domibusJsonMapper")
    protected ObjectMapper objectMapper;

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

        MessageReceiveFailureEvent event = new MessageReceiveFailureEvent();

        event.addProperty(FINAL_RECIPIENT, properties.get(FINAL_RECIPIENT));
        event.addProperty(ORIGINAL_SENDER, properties.get(ORIGINAL_SENDER));
        event.setMessageId(userMessage.getMessageId());
        event.setMessageEntityId(userMessage.getEntityId());
        String service = properties.get(MessageConstants.SERVICE);
        event.setService(service);

        String serviceType = properties.get(MessageConstants.SERVICE_TYPE);
        event.setServiceType(serviceType);

        String action = properties.get(MessageConstants.ACTION);
        event.setAction(action);

        event.setErrorResult(errorResult);
        event.setEndpoint(properties.get(MessageConstants.ENDPOINT));

        addMessagePropertiesToEvent(event, userMessage);

        notifyOfIncoming(event, routingService.getMatchingBackendFilter(userMessage), notificationType);
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

        MessageReceivedEvent messageReceivedEvent = new MessageReceivedEvent();
        messageReceivedEvent.setMessageEntityId(userMessage.getEntityId());
        messageReceivedEvent.setMessageId(userMessage.getMessageId());
        addMessagePropertiesToEvent(messageReceivedEvent, userMessage);

        notifyOfIncoming(messageReceivedEvent, matchingBackendFilter, notificationType);
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
        notify(messageDeletedBatchEvent, backend, NotificationType.MESSAGE_DELETE_BATCH);
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
        return getMessageDeletedEvent(userMessageLogDto.getMessageId(), userMessageLogDto.getEntityId(), userMessageLogDto.getProperties());
    }

    protected MessageDeletedEvent getMessageDeletedEvent(String messageId, Long messageEntityId, Map<String, String> properties) {
        MessageDeletedEvent messageDeletedEvent = new MessageDeletedEvent();
        messageDeletedEvent.setMessageId(messageId);
        messageDeletedEvent.setMessageEntityId(messageEntityId);
        messageDeletedEvent.addProperty(FINAL_RECIPIENT, properties.get(FINAL_RECIPIENT));
        messageDeletedEvent.addProperty(ORIGINAL_SENDER, properties.get(ORIGINAL_SENDER));
        messageDeletedEvent.addProperty(MSH_ROLE, properties.get(MSH_ROLE));
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

        Map<String, String> properties = userMessageServiceHelper.getProperties(userMessage);
        fillEventProperties(userMessage, properties);
        MessageDeletedEvent messageDeletedEvent = getMessageDeletedEvent(
                userMessage.getMessageId(),
                userMessage.getEntityId(),
                properties);

        notify(messageDeletedEvent, backend, NotificationType.MESSAGE_DELETED);
    }


    public void notifyPayloadSubmitted(final UserMessage userMessage, String originalFilename, PartInfo partInfo, String backendName) {
        if (BooleanUtils.isTrue(testMessageValidator.checkTestMessage(userMessage))) {
            LOG.debug("Payload submitted notifications are not enabled for test messages [{}]", userMessage);
            return;
        }
        PayloadSubmittedEvent payloadSubmittedEvent = new PayloadSubmittedEvent();
        payloadSubmittedEvent.setCid(partInfo.getHref());
        payloadSubmittedEvent.setFileName(originalFilename);
        payloadSubmittedEvent.setMessageEntityId(userMessage.getEntityId());
        payloadSubmittedEvent.setMessageId(userMessage.getMessageId());
        payloadSubmittedEvent.setMime(partInfo.getMime());
        addMessagePropertiesToEvent(payloadSubmittedEvent, userMessage);

        notify(payloadSubmittedEvent, backendName, NotificationType.PAYLOAD_SUBMITTED);
    }


    public void notifyPayloadProcessed(final UserMessage userMessage, String originalFilename, PartInfo partInfo, String backendName) {
        if (BooleanUtils.isTrue(testMessageValidator.checkTestMessage(userMessage))) {
            LOG.debug("Payload processed notifications are not enabled for test messages [{}]", userMessage);
            return;
        }

        PayloadProcessedEvent payloadProcessedEvent = new PayloadProcessedEvent();
        payloadProcessedEvent.setCid(partInfo.getHref());
        payloadProcessedEvent.setFileName(originalFilename);
        payloadProcessedEvent.setMessageEntityId(userMessage.getEntityId());
        payloadProcessedEvent.setMessageId(userMessage.getMessageId());
        payloadProcessedEvent.setMime(partInfo.getMime());
        addMessagePropertiesToEvent(payloadProcessedEvent, userMessage);

        notify(payloadProcessedEvent, backendName, NotificationType.PAYLOAD_PROCESSED);
    }

    protected void notifyOfIncoming(MessageEvent messageEvent, final BackendFilter matchingBackendFilter, final NotificationType notificationType) {
        if (matchingBackendFilter == null) {
            LOG.error("No backend responsible for message [{}] found. Sending notification to [{}]", messageEvent.getMessageId(), unknownReceiverQueue);
            MSHRole role = null;
            String mshRole = messageEvent.getProps().get(MSH_ROLE);
            if (mshRole == null) {
                LOG.info("MSH role is null for message with messageId [{}]", messageEvent.getMessageId());
            } else {
                role = MSHRole.valueOf(mshRole);
            }

            jmsManager.sendMessageToQueue(new NotifyMessageCreator(role,
                    notificationType, messageEvent.getProps(), objectMapper).createMessage(messageEvent), unknownReceiverQueue);
            return;
        }

        notify(messageEvent, matchingBackendFilter.getBackendName(), notificationType);
    }

    public void fillEventProperties(final UserMessage userMessage, Map<String, String> target) {
        if (userMessage == null) {
            return;
        }

        Map<String, String> props = userMessageServiceHelper.getProperties(userMessage);
        target.putAll(props);

        target.put(REF_TO_MESSAGE_ID, userMessage.getRefToMessageId());
        target.put(CONVERSATION_ID, userMessage.getConversationId());

        final PartyId partyFrom = userMessageServiceHelper.getPartyFrom(userMessage);
        if (partyFrom != null) {
            target.put(FROM_PARTY_ID, partyFrom.getValue());
            target.put(FROM_PARTY_TYPE, partyFrom.getType());
        }
        target.put(FROM_PARTY_ROLE, userMessageServiceHelper.getPartyFromRole(userMessage));

        final PartyId partyTo = userMessageServiceHelper.getPartyTo(userMessage);
        if (partyTo != null) {
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

        MSHRoleEntity mshRole = userMessage.getMshRole();
        if (mshRole != null) {
            target.put(MSH_ROLE, mshRole.getRole().name());
        }
    }


    protected void notify(MessageEvent messageEvent, String backendName, NotificationType notificationType) {
        LOG.info("Notifying backend [{}] of message [{}] and notification type [{}]", backendName, messageEvent.getMessageId(), notificationType);

        BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backendName);
        if (backendConnector == null) {
            LOG.warn("No backend connector found for backend [{}]", backendName);
            return;
        }
        final String messageId = messageEvent.getMessageId();
        final long messageEntityId = messageEvent.getMessageEntityId();

        List<NotificationType> requiredNotificationTypeList = backendConnectorHelper.getRequiredNotificationTypeList(backendConnector);
        LOG.debug("Required notifications [{}] for backend [{}]", requiredNotificationTypeList, backendName);
        if (requiredNotificationTypeList == null || !requiredNotificationTypeList.contains(notificationType)) {
            LOG.debug("No plugin notification sent for message [{}]. Notification type [{}]]", messageId, notificationType);
            return;
        }

        Map<String,String> properties = messageEvent.getProps();
        if (properties != null) {
            String finalRecipient = properties.get(FINAL_RECIPIENT);
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}] and finalRecipient [{}]", backendName, messageId, notificationType, finalRecipient);
        } else {
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}]", backendName, messageId, notificationType);
        }

        AsyncNotificationConfiguration asyncNotificationConfiguration = asyncNotificationConfigurationService.getAsyncPluginConfiguration(backendName);
        if (shouldNotifyAsync(asyncNotificationConfiguration)) {
            MSHRole role = null;
            String mshRole = messageEvent.getProps().get(MSH_ROLE);
            if (mshRole == null) {
                LOG.info("MSH role is null for message with messageId [{}]", messageEvent.getMessageId());
            } else {
                role = MSHRole.valueOf(mshRole);
            }
            notifyAsync(messageEvent, asyncNotificationConfiguration, messageEntityId, messageId, role, notificationType, properties);
            return;
        }

        notifySync(messageEvent, backendConnector, notificationType);
    }

    protected boolean shouldNotifyAsync(AsyncNotificationConfiguration asyncNotificationConfiguration) {
        return asyncNotificationConfiguration != null && asyncNotificationConfiguration.getBackendNotificationQueue() != null;
    }

    protected void notifyAsync(MessageEvent messageEvent, AsyncNotificationConfiguration asyncNotificationConfiguration, Long messageEntityId,
                               String messageId, MSHRole mshRole, NotificationType notificationType, Map<String, String> properties) {
        Queue backendNotificationQueue = asyncNotificationConfiguration.getBackendNotificationQueue();
        LOG.debug("Notifying plugin [{}] using queue", asyncNotificationConfiguration.getBackendConnector().getName());
        NotifyMessageCreator notifyMessageCreator = new NotifyMessageCreator(mshRole, notificationType, properties, objectMapper);
        jmsManager.sendMessageToQueue(notifyMessageCreator.createMessage(messageEvent), backendNotificationQueue);
    }

    protected void notifySync(MessageEvent messageEvent, BackendConnector<?, ?> backendConnector,
                              NotificationType notificationType) {
        LOG.debug("Notifying plugin [{}] using callback", backendConnector.getName());
        PluginEventNotifier pluginEventNotifier = pluginEventNotifierProvider.getPluginEventNotifier(notificationType);
        if (pluginEventNotifier == null) {
            LOG.warn("Could not get plugin event notifier for notification type [{}]", notificationType);
            return;
        }

        pluginEventNotifier.notifyPlugin(messageEvent, backendConnector);
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

        MessageSendFailedEvent messageSendFailedEvent = new MessageSendFailedEvent(userMessage.getEntityId(), userMessage.getMessageId());
        addMessagePropertiesToEvent(messageSendFailedEvent, userMessage);

//        messageSendFailedEvent.addProperty(FINAL_RECIPIENT, properties.get(FINAL_RECIPIENT));
//        messageSendFailedEvent.addProperty(ORIGINAL_SENDER, properties.get(ORIGINAL_SENDER));

        notify(messageSendFailedEvent, backendName, notificationType);
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

        MessageSendSuccessEvent messageSendSuccessEvent = new MessageSendSuccessEvent();
        messageSendSuccessEvent.setMessageEntityId(userMessage.getEntityId());
        messageSendSuccessEvent.setMessageId(userMessage.getMessageId());
        addMessagePropertiesToEvent(messageSendSuccessEvent, userMessage);

        notify(messageSendSuccessEvent, userMessageLog.getBackend(), notificationType);
        userMessageLogDao.setAsNotified(userMessageLog);
    }


    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    @Transactional
    public void notifyOfMessageStatusChange(UserMessageLog messageLog, MessageStatus newStatus, Timestamp changeTimestamp) {
        UserMessage userMessage = userMessageDao.findByEntityId(messageLog.getEntityId());
        notifyOfMessageStatusChange(userMessage, messageLog, newStatus, changeTimestamp);
    }

    @Timer(clazz = BackendNotificationService.class, value = "notifyOfMessageStatusChange")
    @Counter(clazz = BackendNotificationService.class, value = "notifyOfMessageStatusChange")
    @Transactional
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public void notifyOfMessageStatusChange(UserMessage userMessage, UserMessageLog messageLog, MessageStatus newStatus, Timestamp changeTimestamp) {
        final MessagingModuleConfiguration messagingConfiguration = messagingConfigurationManager.getConfiguration();
        if (messagingConfiguration.shouldMonitorMessageStatus(newStatus)) {
            eventService.enqueueMessageEvent(userMessage.getMessageId(), messageLog.getMessageStatus(), newStatus, messageLog.getMshRole().getRole());
        }

        if (isPluginNotificationDisabled()) {
            return;
        }

        handleMDC(userMessage);
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

        MessageStatusChangeEvent messageStatusChangeEvent = new MessageStatusChangeEvent(messageProperties);
        messageStatusChangeEvent.setMessageId(userMessage.getMessageId());
        messageStatusChangeEvent.setMessageEntityId(userMessage.getEntityId());

        final String fromStatus = messageProperties.get(MessageConstants.STATUS_FROM);
        if (StringUtils.isNotEmpty(fromStatus)) {
            messageStatusChangeEvent.setFromStatus(eu.domibus.common.MessageStatus.valueOf(fromStatus));
        }
        messageStatusChangeEvent.setToStatus(eu.domibus.common.MessageStatus.valueOf(messageProperties.get(MessageConstants.STATUS_TO)));
        messageStatusChangeEvent.setChangeTimestamp(new Timestamp(NumberUtils.toLong(messageProperties.get(MessageConstants.CHANGE_TIMESTAMP))));
        addMessagePropertiesToEvent(messageStatusChangeEvent, userMessage);

        notify(messageStatusChangeEvent, messageLog.getBackend(), notificationType);
    }

    private void handleMDC(UserMessage userMessage) {
        final String messageId = userMessage.getMessageId();
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
            if (userMessage.getMshRole() != null && userMessage.getMshRole().getRole() != null) {
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ROLE, userMessage.getMshRole().getRole().name());
            } else {
                LOG.warn("No MshRole for message [{}]", userMessage);
            }
        }
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

    private void addMessagePropertiesToEvent(MessageEvent event, UserMessage userMessage) {
        Map<String, String> existingProperties = userMessageServiceHelper.getProperties(userMessage);
        final Map<String, String> properties = new HashMap<>();
        fillEventProperties(userMessage, properties);
        properties.putAll(existingProperties);

        if (MapUtils.isNotEmpty(properties)) {
            properties.forEach((key, value) -> event.addProperty(key, value));
        }
    }
}
