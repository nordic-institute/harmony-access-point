package eu.domibus.core.plugin.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.*;
import eu.domibus.api.plugin.BackendConnectorService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.*;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.plugin.BackendConnectorHelper;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.routing.RoutingService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.api.property.DomibusGeneralConstants.JSON_MAPPER_BEAN;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGE_TEST_DELIVERY;
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

    protected final JMSManager jmsManager;

    protected final RoutingService routingService;

    protected final AsyncNotificationConfigurationService asyncNotificationConfigurationService;

    protected final UserMessageLogDao userMessageLogDao;

    @Qualifier(UNKNOWN_RECEIVER_QUEUE)
    protected final Queue unknownReceiverQueue;

    protected final UserMessageDao userMessageDao;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final EventService eventService;

    private final UserMessageServiceHelper userMessageServiceHelper;

    protected final PluginEventNotifierProvider pluginEventNotifierProvider;

    protected final BackendConnectorProvider backendConnectorProvider;

    protected final BackendConnectorHelper backendConnectorHelper;

    protected final BackendConnectorService backendConnectorService;

    @Qualifier(JSON_MAPPER_BEAN)
    protected final ObjectMapper objectMapper;

    public BackendNotificationService(JMSManager jmsManager, RoutingService routingService, AsyncNotificationConfigurationService asyncNotificationConfigurationService,
                                      UserMessageLogDao userMessageLogDao, Queue unknownReceiverQueue, UserMessageDao userMessageDao,
                                      DomibusPropertyProvider domibusPropertyProvider, EventService eventService,
                                      UserMessageServiceHelper userMessageServiceHelper, PluginEventNotifierProvider pluginEventNotifierProvider,
                                      BackendConnectorProvider backendConnectorProvider, BackendConnectorHelper backendConnectorHelper,
                                      BackendConnectorService backendConnectorService, ObjectMapper objectMapper) {
        this.jmsManager = jmsManager;
        this.routingService = routingService;
        this.asyncNotificationConfigurationService = asyncNotificationConfigurationService;
        this.userMessageLogDao = userMessageLogDao;
        this.unknownReceiverQueue = unknownReceiverQueue;
        this.userMessageDao = userMessageDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.eventService = eventService;
        this.userMessageServiceHelper = userMessageServiceHelper;
        this.pluginEventNotifierProvider = pluginEventNotifierProvider;
        this.backendConnectorProvider = backendConnectorProvider;
        this.backendConnectorHelper = backendConnectorHelper;
        this.backendConnectorService = backendConnectorService;
        this.objectMapper = objectMapper;
    }

    @Timer(clazz = BackendNotificationService.class, value = "notifyMessageReceivedFailure")
    @Counter(clazz = BackendNotificationService.class, value = "notifyMessageReceivedFailure")
    public void notifyMessageReceivedFailure(final UserMessage userMessage, ErrorResult errorResult) {
        LOG.debug("Notify message receive failure");
        BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(userMessage);

        if (!shouldNotify(userMessage, matchingBackendFilter)) {
            return;
        }

        final Map<String, String> errorProperties = new HashMap<>();
        if (errorResult.getErrorCode() != null) {
            errorProperties.put(MessageConstants.ERROR_CODE, errorResult.getErrorCode().getErrorCodeName());
        }
        errorProperties.put(MessageConstants.ERROR_DETAIL, errorResult.getErrorDetail());
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED_FAILURE;
        if (userMessage.isMessageFragment()) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED_FAILURE;
        }

        MessageReceiveFailureEvent event = new MessageReceiveFailureEvent();

        event.setMessageId(userMessage.getMessageId());
        event.setMessageEntityId(userMessage.getEntityId());

        addMessagePropertiesToEvent(event, userMessage, errorProperties);

        ServiceEntity service = userMessage.getService();
        if (service != null) {
            event.setService(service.getValue());
            event.setServiceType(service.getType());
        }

        event.setAction(userMessage.getActionValue());
        event.setErrorResult(errorResult);
        event.setEndpoint(errorProperties.get(MessageConstants.ENDPOINT));

        notifyOfIncoming(event, matchingBackendFilter, notificationType);
    }

    @Timer(clazz = BackendNotificationService.class, value = "notifyMessageReceived")
    @Counter(clazz = BackendNotificationService.class, value = "notifyMessageReceived")
    public void notifyMessageReceived(final BackendFilter matchingBackendFilter, final UserMessage userMessage) {
        if (!shouldNotify(userMessage, matchingBackendFilter)) {
            return;
        }

        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        if (userMessage.isMessageFragment()) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED;
        }

        DeliverMessageEvent deliverMessageEvent = new DeliverMessageEvent();
        deliverMessageEvent.setMessageEntityId(userMessage.getEntityId());
        deliverMessageEvent.setMessageId(userMessage.getMessageId());
        addMessagePropertiesToEvent(deliverMessageEvent, userMessage, null);

        notifyOfIncoming(deliverMessageEvent, matchingBackendFilter, notificationType);
    }

    @Timer(clazz = BackendNotificationService.class, value = "notifyMessageResponseSent")
    @Counter(clazz = BackendNotificationService.class, value = "notifyMessageResponseSent")
    public void notifyMessageResponseSent(BackendFilter matchingBackendFilter, UserMessage userMessage) {
        if (!shouldNotify(userMessage, matchingBackendFilter)) {
            return;
        }

        if (userMessage.isMessageFragment()) {
            LOG.debug("No MessageResponseSent event for message fragments.");
            return;
        }

        NotificationType notificationType = NotificationType.MESSAGE_RESPONSE_SENT;
        MessageResponseSentEvent messageResponseSentEvent = new MessageResponseSentEvent(userMessage.getMessageId());
        messageResponseSentEvent.setMessageEntityId(userMessage.getEntityId());
        addMessagePropertiesToEvent(messageResponseSentEvent, userMessage, null);
        notifyOfIncoming(messageResponseSentEvent, matchingBackendFilter, notificationType);
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
            List<MessageDeletedEvent> individualMessageDeletedEvents =
                    getMessageDeletedEventsForBackend(backend, userMessageLogsToNotify);
            createMessageDeleteBatchEvent(backend, individualMessageDeletedEvents);
        });
    }

    public void notifyMessageDeleted(UserMessage userMessage, UserMessageLog userMessageLog) {
        String backend = userMessageLog.getBackend();

        if (!shouldNotify(userMessage, backend)) {
            return;
        }

        if (StringUtils.isEmpty(backend)) {
            LOG.warn("Could not find backend for message with id [{}]", userMessage);
            return;
        }

        Map<String, String> properties = userMessageServiceHelper.getProperties(userMessage);
        fillEventProperties(userMessage, properties);
        UserMessageLogDto userMessageLogDto = new UserMessageLogDto(userMessageLog.getEntityId(), userMessageLog.getUserMessage().getMessageId(), userMessageLog.getBackend());
        userMessageLogDto.setProperties(properties);
        MessageDeletedEvent messageDeletedEvent = getMessageDeletedEvent(userMessageLogDto);

        notify(messageDeletedEvent, backend, NotificationType.MESSAGE_DELETED);
    }

    public void notifyPayloadSubmitted(final UserMessage userMessage, String originalFilename, PartInfo partInfo, String backendName) {
        if (!shouldNotify(userMessage, backendName)) {
            return;
        }

        PayloadSubmittedEvent payloadSubmittedEvent = new PayloadSubmittedEvent();
        payloadSubmittedEvent.setCid(partInfo.getHref());
        payloadSubmittedEvent.setFileName(originalFilename);
        payloadSubmittedEvent.setMessageEntityId(userMessage.getEntityId());
        payloadSubmittedEvent.setMessageId(userMessage.getMessageId());
        payloadSubmittedEvent.setMime(partInfo.getMime());
        addMessagePropertiesToEvent(payloadSubmittedEvent, userMessage, null);

        notify(payloadSubmittedEvent, backendName, NotificationType.PAYLOAD_SUBMITTED);
    }

    public void notifyPayloadProcessed(final UserMessage userMessage, String originalFilename, PartInfo partInfo, String backendName) {
        if (!shouldNotify(userMessage, backendName)) {
            return;
        }

        PayloadProcessedEvent payloadProcessedEvent = new PayloadProcessedEvent();
        payloadProcessedEvent.setCid(partInfo.getHref());
        payloadProcessedEvent.setFileName(originalFilename);
        payloadProcessedEvent.setMessageEntityId(userMessage.getEntityId());
        payloadProcessedEvent.setMessageId(userMessage.getMessageId());
        payloadProcessedEvent.setMime(partInfo.getMime());
        addMessagePropertiesToEvent(payloadProcessedEvent, userMessage, null);

        notify(payloadProcessedEvent, backendName, NotificationType.PAYLOAD_PROCESSED);
    }

    @Transactional
    public void notifyOfSendFailure(final UserMessage userMessage, UserMessageLog userMessageLog) {
        final String backendName = userMessageLog.getBackend();
        if (!shouldNotify(userMessage, backendName)) {
            return;
        }

        NotificationType notificationType = NotificationType.MESSAGE_SEND_FAILURE;
        if (BooleanUtils.isTrue(userMessage.isMessageFragment())) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_SEND_FAILURE;
        }

        Map<String, String> properties = new HashMap<>();
        fillEventProperties(userMessage, properties);

        MessageSendFailedEvent messageSendFailedEvent = new MessageSendFailedEvent(userMessage.getEntityId(), userMessage.getMessageId());
        addMessagePropertiesToEvent(messageSendFailedEvent, userMessage, null);

        notify(messageSendFailedEvent, backendName, notificationType);
        userMessageLogDao.setAsNotified(userMessageLog);
    }

    @Timer(clazz = BackendNotificationService.class, value = "notifyOfSendSuccess")
    @Counter(clazz = BackendNotificationService.class, value = "notifyOfSendSuccess")
    @Transactional
    public void notifyOfSendSuccess(final UserMessage userMessage, final UserMessageLog userMessageLog) {
        String backend = userMessageLog.getBackend();
        if (!shouldNotify(userMessage, backend)) {
            return;
        }

        NotificationType notificationType = NotificationType.MESSAGE_SEND_SUCCESS;
        if (BooleanUtils.isTrue(userMessage.isMessageFragment())) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_SEND_SUCCESS;
        }

        MessageSendSuccessEvent messageSendSuccessEvent = new MessageSendSuccessEvent();
        messageSendSuccessEvent.setMessageEntityId(userMessage.getEntityId());
        messageSendSuccessEvent.setMessageId(userMessage.getMessageId());
        addMessagePropertiesToEvent(messageSendSuccessEvent, userMessage, null);

        notify(messageSendSuccessEvent, backend, notificationType);
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
        String backend = messageLog.getBackend();
        if (!shouldNotify(userMessage, backend)) {
            return;
        }

        eventService.enqueueMessageStatusChangedEvent(userMessage.getMessageId(), messageLog.getMessageStatus(), newStatus, userMessage.getMshRole().getRole());

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
        addMessagePropertiesToEvent(messageStatusChangeEvent, userMessage, null);

        notify(messageStatusChangeEvent, backend, notificationType);
    }

    protected boolean shouldNotify(UserMessage userMessage, BackendFilter backendFilter) {
        return backendFilter != null && shouldNotify(userMessage, backendFilter.getBackendName());
    }

    protected boolean shouldNotify(UserMessage userMessage, String backendName) {
        if (isPluginNotificationDisabled()) {
            LOG.debug("Plugin notification is disabled.");
            return false;
        }

        if (userMessage == null) {
            LOG.warn("User message is null");
            return false;
        }

        if (!backendConnectorService.isBackendConnectorEnabled(backendName)) {
            LOG.info("Backend connector [{}] is disabled so exiting notification", backendName);
            return false;
        }

        if (userMessage.isTestMessage()) {
            final Boolean testMessageNotification = domibusPropertyProvider.getBooleanProperty(DOMIBUS_MESSAGE_TEST_DELIVERY);
            LOG.debug("Notification status [{}] for Test message [{}]", testMessageNotification, userMessage);
            return testMessageNotification;
        }

        return true;
    }

    protected void createMessageDeleteBatchEvent(String backend, List<MessageDeletedEvent> messageDeletedEvents) {
        MessageDeletedBatchEvent messageDeletedBatchEvent = new MessageDeletedBatchEvent();
        messageDeletedBatchEvent.setMessageDeletedEvents(messageDeletedEvents);
        notify(messageDeletedBatchEvent, backend, NotificationType.MESSAGE_DELETE_BATCH);
    }

    protected List<MessageDeletedEvent> getMessageDeletedEventsForBackend(String backend, final List<UserMessageLogDto> userMessageLogs) {
        List<MessageDeletedEvent> individualMessageDeletedEvents = userMessageLogs
                .stream()
                .filter(userMessageLog -> userMessageLog.getBackend().equals(backend))
                .map(this::getMessageDeletedEvent)
                .collect(toList());
        LOG.debug("There are [{}] delete messages to notify for backend [{}]", individualMessageDeletedEvents.size(), backend);
        return individualMessageDeletedEvents;
    }

    protected MessageDeletedEvent getMessageDeletedEvent(UserMessageLogDto userMessageLogDto) {
        MessageDeletedEvent messageDeletedEvent = new MessageDeletedEvent();
        messageDeletedEvent.setMessageId(userMessageLogDto.getMessageId());
        messageDeletedEvent.setMessageEntityId(userMessageLogDto.getEntityId());
        messageDeletedEvent.addProperty(FINAL_RECIPIENT, userMessageLogDto.getProperties().get(FINAL_RECIPIENT));
        messageDeletedEvent.addProperty(ORIGINAL_SENDER, userMessageLogDto.getProperties().get(ORIGINAL_SENDER));
        messageDeletedEvent.addProperty(MSH_ROLE, userMessageLogDto.getProperties().get(MSH_ROLE));
        return messageDeletedEvent;
    }

    protected void notifyOfIncoming(MessageEvent messageEvent, final BackendFilter matchingBackendFilter, final NotificationType notificationType) {
        if (matchingBackendFilter == null) {
            LOG.error("No backend responsible for message [{}] found. Sending notification to [{}]", messageEvent.getMessageId(), unknownReceiverQueue);
            MSHRole role = getMshRole(messageEvent);
            jmsManager.sendMessageToQueue(new NotifyMessageCreator(role,
                    notificationType, messageEvent.getProps(), objectMapper).createMessage(messageEvent), unknownReceiverQueue);
            return;
        }

        notify(messageEvent, matchingBackendFilter.getBackendName(), notificationType);
    }

    private MSHRole getMshRole(MessageEvent messageEvent) {
        MSHRole role = null;
        Map<String, String> props = messageEvent.getProps();
        if (MapUtils.isEmpty(props)) {
            LOG.info("No properties in MessageEvent object of type [{}]", messageEvent.getClass());
            return role;
        }
        String mshRole = messageEvent.getProps().get(MSH_ROLE);
        if (mshRole == null) {
            LOG.info("MSH role is null for message with messageId [{}]", messageEvent.getMessageId());
        } else {
            role = MSHRole.valueOf(mshRole);
        }
        return role;
    }

    public void fillEventProperties(final UserMessage userMessage, Map<String, String> target) {
        if (userMessage == null) {
            return;
        }

        Map<String, String> props = userMessageServiceHelper.getProperties(userMessage);
        target.putAll(props);

        target.put(ORIGINAL_SENDER, props.get(ORIGINAL_SENDER));
        target.put(FINAL_RECIPIENT, props.get(FINAL_RECIPIENT));
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

        List<NotificationType> requiredNotificationTypeList = backendConnectorHelper.getRequiredNotificationTypeList(backendConnector);
        LOG.debug("Required notifications [{}] for backend [{}]", requiredNotificationTypeList, backendName);
        if (requiredNotificationTypeList == null || !requiredNotificationTypeList.contains(notificationType)) {
            if (notificationType != NotificationType.MESSAGE_DELETE_BATCH) {
                LOG.debug("No plugin notification sent for message [{}]. Notification type [{}]]", messageId, notificationType);
            }
            return;
        }

        Map<String, String> properties = messageEvent.getProps();
        if (properties != null && notificationType != NotificationType.MESSAGE_DELETE_BATCH) {
            String finalRecipient = properties.get(FINAL_RECIPIENT);
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}] and finalRecipient [{}]", backendName, messageId, notificationType, finalRecipient);
        } else {
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}]", backendName, messageId, notificationType);
        }

        AsyncNotificationConfiguration asyncNotificationConfiguration = asyncNotificationConfigurationService.getAsyncPluginConfiguration(backendName);
        if (shouldNotifyAsync(asyncNotificationConfiguration)) {
            MSHRole role = getMshRole(messageEvent);
            notifyAsync(messageEvent, asyncNotificationConfiguration, role, notificationType, properties);
            return;
        }

        notifySync(messageEvent, backendConnector, notificationType);
    }

    protected boolean shouldNotifyAsync(AsyncNotificationConfiguration asyncNotificationConfiguration) {
        return asyncNotificationConfiguration != null && asyncNotificationConfiguration.getBackendNotificationQueue() != null;
    }

    protected void notifyAsync(MessageEvent messageEvent, AsyncNotificationConfiguration asyncNotificationConfiguration,
                               MSHRole mshRole, NotificationType notificationType, Map<String, String> properties) {
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

    private void addMessagePropertiesToEvent(MessageEvent event, UserMessage userMessage, Map<String, String> otherProperties) {
        Map<String, String> existingProperties = userMessageServiceHelper.getProperties(userMessage);
        final Map<String, String> allProps = new HashMap<>();
        if (MapUtils.isNotEmpty(otherProperties)) {
            allProps.putAll(otherProperties);
        }
        fillEventProperties(userMessage, allProps);
        allProps.putAll(existingProperties);

        if (MapUtils.isNotEmpty(allProps)) {
            allProps.forEach((key, value) -> event.addProperty(key, value));
        }
    }
}
