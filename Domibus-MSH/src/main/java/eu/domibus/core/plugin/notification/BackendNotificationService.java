package eu.domibus.core.plugin.notification;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.*;
import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.*;
import eu.domibus.core.plugin.routing.BackendFilterEntity;
import eu.domibus.core.plugin.routing.CriteriaFactory;
import eu.domibus.core.plugin.routing.IRoutingCriteria;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.plugin.routing.dao.BackendFilterDao;
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.core.plugin.validation.SubmissionValidatorListProvider;
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
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidator;
import eu.domibus.plugin.validation.SubmissionValidatorList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.jms.Queue;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;
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
    private BackendFilterDao backendFilterDao;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    protected SubmissionAS4Transformer submissionAS4Transformer;

    @Autowired
    protected SubmissionValidatorListProvider submissionValidatorListProvider;

    protected List<NotificationListener> notificationListenerServices;

    @Autowired
    protected List<CriteriaFactory> routingCriteriaFactories;

    @Autowired
    @Qualifier("unknownReceiverQueue")
    private Queue unknownReceiverQueue;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainCoreConverter coreConverter;

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
    protected List<BackendConnector> backendConnectors;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected MetricRegistry metricRegistry;

    //TODO move this into a dedicate provider(a different spring bean class)
    private Map<String, IRoutingCriteria> criteriaMap;

    protected Object backendFiltersCacheLock = new Object();
    protected volatile List<BackendFilter> backendFiltersCache;

    @PostConstruct
    public void init() {
        Map notificationListenerBeanMap = applicationContext.getBeansOfType(NotificationListener.class);
        if (notificationListenerBeanMap.isEmpty()) {
            throw new ConfigurationException("No Plugin available! Please configure at least one backend plugin in order to run domibus");
        } else {

            notificationListenerServices = new ArrayList<NotificationListener>(notificationListenerBeanMap.values());
            if (!domibusConfigurationService.isMultiTenantAware()) {
                LOG.debug("Creating plugin backend filters in Non MultiTenancy environment");
                createBackendFilters();
            } else {
                // Get All Domains
                final List<Domain> domains = domainService.getDomains();
                LOG.debug("Creating plugin backend filters for all the domains in MultiTenancy environment");
                for (Domain domain : domains) {
                    domainTaskExecutor.submit(() -> createBackendFilters(), domain);
                }
            }
        }
        criteriaMap = new HashMap<>();
        for (final CriteriaFactory routingCriteriaFactory : routingCriteriaFactories) {
            criteriaMap.put(routingCriteriaFactory.getName(), routingCriteriaFactory.getInstance());
        }
    }

    /**
     * Find the existing backend Filters from the db and create backend Filters of the plugins based on the available backend filters in the db.
     */
    protected void createBackendFilters() {
        List<BackendFilterEntity> backendFilterEntities = backendFilterDao.findAll();

        if (backendFilterEntities.isEmpty()) {
            LOG.info("No Plugins details available in database!");
            createBackendFiltersWithDefaultPriority();
        } else {
            createBackendFiltersBasedOnExistingUserPriority(backendFilterEntities);
        }
    }

    /**
     * Create Backend Filters of plugins in DB by checking the existing User Priority
     *
     * @param backendFilterEntities
     */
    protected void createBackendFiltersBasedOnExistingUserPriority(List<BackendFilterEntity> backendFilterEntities) {

        LOG.debug("Some Backend Plugins are available in database with user priority. So Creating backend filters for the other plugins by giving more priorities to the existing plugins.");

        List<String> notificationListenerPluginsList = notificationListenerServices.stream().map(NotificationListener::getBackendName).collect(Collectors.toList());
        LOG.debug("Total number of plugins configured in the application: [{}]", notificationListenerPluginsList.size());

        List<String> backendFilterPluginList = backendFilterEntities.stream().map(BackendFilterEntity::getBackendName).collect(Collectors.toList());
        LOG.debug("Number of Backend Plugins with user priority: [{}]", backendFilterPluginList.size());

        notificationListenerPluginsList.removeAll(backendFilterPluginList);
        LOG.debug("Number of Backend Plugins without user priority: [{}]", notificationListenerPluginsList.size());

        BackendFilterEntity backendFilterEntity = backendFilterEntities.stream().max(Comparator.comparing(BackendFilterEntity::getIndex)).orElseThrow(NoSuchElementException::new);
        LOG.debug("Lowest user defined priority of the existing Backend Plugins in the database: [{}]", backendFilterEntity.getIndex());

        if (!notificationListenerPluginsList.isEmpty()) {
            List<BackendFilterEntity> backendFilters = assignPriorityToPlugins(notificationListenerPluginsList, backendFilterEntity.getIndex());
            backendFilterDao.create(backendFilters);
        }
    }

    /**
     * Assigning priorities to the default plugin, which doesn't have any priority set by User
     *
     * @param pluginList
     * @param priority
     * @return backendFilters
     */
    protected List<BackendFilterEntity> assignPriorityToPlugins(List<String> pluginList, int priority) {

        List<BackendFilterEntity> backendFilters = new ArrayList<>();
        List<String> defaultPluginOrderList = Arrays.asList(BackendPluginEnum.WS_PLUGIN.getPluginName(), BackendPluginEnum.JMS_PLUGIN.getPluginName(), BackendPluginEnum.FS_PLUGIN.getPluginName());
        pluginList.sort(Comparator.comparing(defaultPluginOrderList::indexOf));
        LOG.debug("Assigning lower priorities to the backend plugins which doesn't have any existing priority set by User.");

        for (String pluginName : pluginList) {
            BackendFilterEntity filterEntity = new BackendFilterEntity();
            filterEntity.setBackendName(pluginName);
            BackendPluginEnum backEndPluginEnum = BackendPluginEnum.getBackendPluginEnum(pluginName);
            if (backEndPluginEnum != null)
                filterEntity.setIndex(++priority);
            LOG.debug("Assigning priority [{}] to the backend plugin [{}].", priority, pluginName);
            backendFilters.add(filterEntity);
        }
        return backendFilters;
    }

    /**
     * create BackendFilters With Priorities in the order of WS, JMS and FS when No Plugins priorities already set by User.
     */
    protected void createBackendFiltersWithDefaultPriority() {
        List<BackendFilterEntity> backendFilters = new ArrayList<>();
        LOG.debug("Creating Plugin backend filters in the default order of WS_PLUGIN, JMS_PLUGIN and FS_PLUGIN, because no other priorities are already set by User");
        for (NotificationListener notificationListener : notificationListenerServices) {
            BackendFilterEntity backendFilterEntity = new BackendFilterEntity();
            LOG.debug("Loading Plugin with BackendName [{}] to database.", notificationListener.getBackendName());
            backendFilterEntity.setBackendName(notificationListener.getBackendName());
            BackendPluginEnum backEndPluginEnum = BackendPluginEnum.getBackendPluginEnum(notificationListener.getBackendName());
            backendFilterEntity.setIndex(backEndPluginEnum.getPriority());
            backendFilters.add(backendFilterEntity);
        }
        backendFilterDao.create(backendFilters);
    }

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
        com.codahale.metrics.Timer.Context persistReceivedMessage = metricRegistry.timer(name(BackendNotificationService.class, "notifyMessageReceived")).time();
        if (isPluginNotificationDisabled()) {
            return;
        }
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        if (userMessage.isUserMessageFragment()) {
            notificationType = NotificationType.MESSAGE_FRAGMENT_RECEIVED;
        }

        notifyOfIncoming(matchingBackendFilter, userMessage, notificationType, new HashMap<String, Object>());
        persistReceivedMessage.stop();
    }

    public void notifyPayloadSubmitted(final UserMessage userMessage, String originalFilename, PartInfo partInfo, String backendName) {
        if (userMessageHandlerService.checkTestMessage(userMessage)) {
            LOG.debug("Payload submitted notifications are not enabled for test messages [{}]", userMessage);
            return;
        }

        final BackendConnector backendConnector = getBackendConnector(backendName);
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

        final BackendConnector backendConnector = getBackendConnector(backendName);
        PayloadProcessedEvent payloadProcessedEvent = new PayloadProcessedEvent();
        payloadProcessedEvent.setCid(partInfo.getHref());
        payloadProcessedEvent.setFileName(originalFilename);
        payloadProcessedEvent.setMessageId(userMessage.getMessageInfo().getMessageId());
        payloadProcessedEvent.setMime(partInfo.getMime());
        backendConnector.payloadProcessedEvent(payloadProcessedEvent);
    }

    public BackendFilter getMatchingBackendFilter(final UserMessage userMessage) {
        List<BackendFilter> backendFilters = getBackendFiltersWithCache();
        return getMatchingBackendFilter(backendFilters, criteriaMap, userMessage);
    }

    protected void notifyOfIncoming(final BackendFilter matchingBackendFilter, final UserMessage userMessage, final NotificationType notificationType, Map<String, Object> properties) {
        if (matchingBackendFilter == null) {
            LOG.error("No backend responsible for message [" + userMessage.getMessageInfo().getMessageId() + "] found. Sending notification to [" + unknownReceiverQueue + "]");
            String finalRecipient = userMessageServiceHelper.getFinalRecipient(userMessage);
            properties.put(MessageConstants.FINAL_RECIPIENT, finalRecipient);
            jmsManager.sendMessageToQueue(new NotifyMessageCreator(userMessage.getMessageInfo().getMessageId(), notificationType, properties).createMessage(), unknownReceiverQueue);
            return;
        }

        validateAndNotify(userMessage, matchingBackendFilter.getBackendName(), notificationType, properties);
    }

    protected void notifyOfIncoming(final UserMessage userMessage, final NotificationType notificationType, Map<String, Object> properties) {
        final BackendFilter matchingBackendFilter = getMatchingBackendFilter(userMessage);
        notifyOfIncoming(matchingBackendFilter, userMessage, notificationType, properties);
    }

    protected BackendFilter getMatchingBackendFilter(final List<BackendFilter> backendFilters, final Map<String, IRoutingCriteria> criteriaMap, final UserMessage userMessage) {
        LOG.debug("Getting the backend filter for message [" + userMessage.getMessageInfo().getMessageId() + "]");
        for (final BackendFilter filter : backendFilters) {
            final boolean backendFilterMatching = isBackendFilterMatching(filter, criteriaMap, userMessage);
            if (backendFilterMatching) {
                LOG.debug("Filter [" + filter + "] matched for message [" + userMessage.getMessageInfo().getMessageId() + "]");
                return filter;
            }
        }
        return null;
    }

    protected boolean isBackendFilterMatching(BackendFilter filter, Map<String, IRoutingCriteria> criteriaMap, final UserMessage userMessage) {
        if (filter.getRoutingCriterias() != null) {
            for (final RoutingCriteria routingCriteriaEntity : filter.getRoutingCriterias()) {
                final IRoutingCriteria criteria = criteriaMap.get(StringUtils.upperCase(routingCriteriaEntity.getName()));
                boolean matches = criteria.matches(userMessage, routingCriteriaEntity.getExpression());
                //if at least one criteria does not match it means the filter is not matching
                if (!matches) {
                    return false;
                }
            }
        }
        return true;
    }

    public void invalidateBackendFiltersCache() {
        LOG.debug("Invalidating the backend filter cache");

        this.backendFiltersCache = null;
    }

    protected List<BackendFilter> getBackendFiltersWithCache() {
        if (backendFiltersCache == null) {
            synchronized (backendFiltersCacheLock) {
                LOG.debug("Initializing backendFilterCache");
                if (backendFiltersCache == null) {
                    List<BackendFilter> backendFilters = getBackendFilters();
                    backendFiltersCache = backendFilters;
                }
            }
        }
        return backendFiltersCache;
    }

    protected List<BackendFilter> getBackendFilters() {
        List<BackendFilterEntity> backendFilterEntities = backendFilterDao.findAll();

        if (!backendFilterEntities.isEmpty()) {
            return coreConverter.convert(backendFilterEntities, BackendFilter.class);
        }

        List<BackendFilter> backendFilters = routingService.getBackendFilters();
        if (backendFilters.isEmpty()) {
            LOG.error("There are no backend plugins deployed on this server");
        }
        if (backendFilters.size() > 1) { //There is more than one unconfigured backend available. For security reasons we cannot send the message just to the first one
            LOG.error("There are multiple unconfigured backend plugins available. Please set up the configuration using the \"Message filter\" pannel of the administrative GUI.");
            backendFilters.clear(); // empty the list so its handled in the desired way.
        }
        //If there is only one backend deployed we send it to that as this is most likely the intent
        return backendFilters;
    }

    public void validateSubmission(UserMessage userMessage, String backendName, NotificationType notificationType) {
        if (NotificationType.MESSAGE_RECEIVED != notificationType) {
            LOG.debug("Validation is not configured to be done for notification of type [" + notificationType + "]");
            return;
        }

        SubmissionValidatorList submissionValidatorList = submissionValidatorListProvider.getSubmissionValidatorList(backendName);
        if (submissionValidatorList == null) {
            LOG.debug("No submission validators found for backend [" + backendName + "]");
            return;
        }
        LOG.info("Performing submission validation for backend [" + backendName + "]");
        Submission submission = submissionAS4Transformer.transformFromMessaging(userMessage);
        List<SubmissionValidator> submissionValidators = submissionValidatorList.getSubmissionValidators();
        for (SubmissionValidator submissionValidator : submissionValidators) {
            submissionValidator.validate(submission);
        }
    }

    public NotificationListener getNotificationListener(String backendName) {
        for (final NotificationListener notificationListenerService : notificationListenerServices) {
            if (notificationListenerService.getBackendName().equalsIgnoreCase(backendName)) {
                return notificationListenerService;
            }
        }
        return null;
    }

    public BackendConnector getBackendConnector(String backendName) {
        for (final BackendConnector backendConnector : backendConnectors) {
            if (backendConnector.getName().equalsIgnoreCase(backendName)) {
                return backendConnector;
            }
        }
        return null;
    }

    protected void validateAndNotify(UserMessage userMessage, String backendName, NotificationType notificationType, Map<String, Object> properties) {
        LOG.info("Notifying backend [{}] of message [{}] and notification type [{}]", backendName, userMessage.getMessageInfo().getMessageId(), notificationType);

        validateSubmission(userMessage, backendName, notificationType);
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
        NotificationListener notificationListener = getNotificationListener(backendName);
        if (notificationListener == null) {
            LOG.warn("No notification listeners found for backend [" + backendName + "]");
            return;
        }

        List<NotificationType> requiredNotificationTypeList = notificationListener.getRequiredNotificationTypeList();
        LOG.debug("Required notifications [{}] for backend [{}]", requiredNotificationTypeList, backendName);
        if (requiredNotificationTypeList == null || !requiredNotificationTypeList.contains(notificationType)) {
            LOG.debug("No plugin notification sent for message [{}]. Notification type [{}], mode [{}]", messageId, notificationType, notificationListener.getMode());
            return;
        }

        if (properties != null) {
            String finalRecipient = (String) properties.get(MessageConstants.FINAL_RECIPIENT);
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}] and finalRecipient [{}]", backendName, messageId, notificationType, finalRecipient);
        } else {
            LOG.info("Notifying plugin [{}] for message [{}] with notificationType [{}]", backendName, messageId, notificationType);
        }

        Queue backendNotificationQueue = notificationListener.getBackendNotificationQueue();
        if (backendNotificationQueue != null) {
            LOG.debug("Notifying plugin [{}] using queue", backendName);
            jmsManager.sendMessageToQueue(new NotifyMessageCreator(messageId, notificationType, properties).createMessage(), backendNotificationQueue);
        } else {
            LOG.debug("Notifying plugin [{}] using callback", backendName);
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
        if (userMessageLog.getMessageFragment()) {
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
        if (userMessageLog.getMessageFragment()) {
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
        if (messageLog.getMessageFragment()) {
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

    public List<NotificationListener> getNotificationListenerServices() {
        return notificationListenerServices;
    }

    protected boolean isPluginNotificationDisabled() {
        return !domibusPropertyProvider.getBooleanProperty(DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE);
    }
}
