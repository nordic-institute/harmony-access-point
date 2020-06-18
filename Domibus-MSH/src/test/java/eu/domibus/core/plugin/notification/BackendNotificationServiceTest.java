package eu.domibus.core.plugin.notification;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationType;
import eu.domibus.core.alerts.configuration.messaging.MessagingConfigurationManager;
import eu.domibus.core.alerts.configuration.messaging.MessagingModuleConfiguration;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.converter.DomainCoreConverter;
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
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidator;
import eu.domibus.plugin.validation.SubmissionValidatorList;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import javax.jms.Queue;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by baciuco on 08/08/2016.
 */
@RunWith(JMockit.class)
public class BackendNotificationServiceTest {

    @Injectable
    JMSManager jmsManager;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    BackendFilterDao backendFilterDao;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    RoutingService routingService;

    @Injectable
    UserMessageLogDao messageLogDao;

    @Injectable
    SubmissionAS4Transformer submissionAS4Transformer;

    @Injectable
    SubmissionValidatorListProvider submissionValidatorListProvider;

    @Injectable
    UserMessageHandlerService userMessageHandlerService;

    List<NotificationListener> notificationListenerServices;

    @Injectable
    List<CriteriaFactory> routingCriteriaFactories;

    @Injectable
    Queue unknownReceiverQueue;

    @Injectable
    ApplicationContext applicationContext;

    @Injectable
    Map<String, IRoutingCriteria> criteriaMap;

    @Injectable
    DomainCoreConverter coreConverter;

    @Injectable
    MessageExchangeService messageExchangeService;

    @Tested
    BackendNotificationService backendNotificationService = new BackendNotificationService();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
    protected List<BackendConnector> backendConnectors;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomainService domainService;

    @Mock
    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Captor
    ArgumentCaptor argCaptor;

    @Test
    public void testValidateSubmissionForUnsupportedNotificationType(@Injectable final Submission submission, @Injectable final UserMessage userMessage) throws Exception {
        final String backendName = "customPlugin";
        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED_FAILURE);

        new Verifications() {{
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            times = 0;
        }};
    }

    @Test
    public void testValidateSubmissionWhenFirstValidatorThrowsException(@Injectable final Submission submission,
                                                                        @Injectable final UserMessage userMessage,
                                                                        @Injectable final SubmissionValidatorList submissionValidatorList,
                                                                        @Injectable final SubmissionValidator validator1,
                                                                        @Injectable final SubmissionValidator validator2) throws Exception {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionAS4Transformer.transformFromMessaging(userMessage);
            result = submission;
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = submissionValidatorList;
            submissionValidatorList.getSubmissionValidators();
            result = Arrays.asList(new SubmissionValidator[]{validator1, validator2});
            validator1.validate(submission);
            result = new SubmissionValidationException("Exception in the validator1");
        }};

        thrown.expect(SubmissionValidationException.class);
        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new Verifications() {{
            validator2.validate(submission);
            times = 0;
        }};
    }

    @Test
    public void testValidateSubmissionWithAllValidatorsCalled(@Injectable final Submission submission,
                                                              @Injectable final UserMessage userMessage,
                                                              @Injectable final SubmissionValidatorList submissionValidatorList,
                                                              @Injectable final SubmissionValidator validator1,
                                                              @Injectable final SubmissionValidator validator2) throws Exception {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionAS4Transformer.transformFromMessaging(userMessage);
            result = submission;
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = submissionValidatorList;
            submissionValidatorList.getSubmissionValidators();
            result = Arrays.asList(new SubmissionValidator[]{validator1, validator2});
        }};

        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new Verifications() {{
            validator1.validate(submission);
            times = 1;
            validator2.validate(submission);
            times = 1;
        }};
    }

    @Test
    public void testGetNotificationListener(@Injectable final NotificationListener notificationListener1,
                                            @Injectable final NotificationListener notificationListener2) throws Exception {
        final String backendName = "customPlugin";
        new Expectations() {{
            notificationListener1.getBackendName();
            result = "anotherPlugin";
            notificationListener2.getBackendName();
            result = backendName;
        }};

        List<NotificationListener> notificationListeners = new ArrayList<>();
        notificationListeners.add(notificationListener1);
        notificationListeners.add(notificationListener2);
        backendNotificationService.notificationListenerServices = notificationListeners;

        NotificationListener notificationListener = backendNotificationService.getNotificationListener(backendName);
        assertEquals(backendName, notificationListener.getBackendName());

    }

    @Test
    public void testValidateAndNotify(@Injectable final UserMessage userMessage) throws Exception {
        String backendName = "backendName";
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        new Expectations(backendNotificationService) {{
            backendNotificationService.validateSubmission(userMessage, backendName, notificationType);
            result = null;
            backendNotificationService.notify(anyString, backendName, notificationType, null);
            result = null;
        }};

        Map<String, Object> properties = new HashMap<>();
        backendNotificationService.validateAndNotify(userMessage, backendName, notificationType, properties);

        new Verifications() {{
            backendNotificationService.validateSubmission(userMessage, backendName, notificationType);
            times = 1;
            backendNotificationService.notify(anyString, backendName, notificationType, null);
            times = 1;
        }};
    }

    @Test
    public void testNotifyWithNoConfiguredNoficationListener(
            @Injectable final NotificationType notificationType,
            @Injectable final Queue queue) throws Exception {
        final String backendName = "customPlugin";
        new Expectations(backendNotificationService) {{
            backendNotificationService.getNotificationListener(backendName);
            result = null;
        }};

        backendNotificationService.notify("messageId", backendName, notificationType);

        new Verifications() {{
            jmsManager.sendMessageToQueue(withAny(new JmsMessage()), withAny(queue));
            times = 0;
        }};
    }

    @Test
    public void testNotifyWithConfiguredNotificationListener(
            @Injectable final NotificationListener notificationListener,
            @Injectable final Queue queue) throws Exception {

        final String backendName = "customPlugin";
        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);
        new Expectations(backendNotificationService) {{
            backendNotificationService.getNotificationListener(backendName);
            result = notificationListener;

            notificationListener.getRequiredNotificationTypeList();
            result = requiredNotifications;

            notificationListener.getBackendNotificationQueue();
            result = queue;
        }};

        final String messageId = "123";
        final NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        backendNotificationService.notify(messageId, backendName, notificationType);

        new Verifications() {{
            JmsMessage jmsMessage = null;
            jmsManager.sendMessageToQueue(jmsMessage = withCapture(), queue);
            times = 1;

            assertEquals(jmsMessage.getProperty(MessageConstants.MESSAGE_ID), messageId);
            assertEquals(jmsMessage.getProperty(MessageConstants.NOTIFICATION_TYPE), notificationType.name());
        }};
    }

    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromAndActionMatching(@Injectable final BackendFilter filter,
                                                                                 @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                 @Injectable final UserMessage userMessage,
                                                                                 @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                                 @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                                 @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                 @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            // TODO: Criteria Operator is not used.
            /*filter.getCriteriaOperator();
            result = LogicalOperator.AND;*/

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            actionRoutingCriteria.getName();
            result = actionCriteriaName;

            actionRoutingCriteria.getExpression();
            result = "myAction";

            criteriaMap.get(actionCriteriaName);
            result = actionRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = true;

            actionRoutingCriteriaConfiguration.matches(userMessage, actionRoutingCriteria.getExpression());
            result = true;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertTrue(backendFilterMatching);
    }


    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromMatchingAndActionNotMatching(@Injectable final BackendFilter filter,
                                                                                            @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                            @Injectable final UserMessage userMessage,
                                                                                            @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                                            @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                                            @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                            @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.AND;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            actionRoutingCriteria.getName();
            result = actionCriteriaName;

            actionRoutingCriteria.getExpression();
            result = "myAction";

            criteriaMap.get(actionCriteriaName);
            result = actionRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = true;

            actionRoutingCriteriaConfiguration.matches(userMessage, actionRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);
    }


    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromNotMatching(@Injectable final BackendFilter filter,
                                                                           @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                           @Injectable final UserMessage userMessage,
                                                                           @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                           @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                           @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                           @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.AND;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);

        new Verifications() {{
            criteriaMap.get(actionCriteriaName);
            times = 0;

            actionRoutingCriteriaConfiguration.matches(userMessage, anyString);
            times = 0;
        }};
    }

    @Test
    public void testIsBackendFilterMatchingWithFromMatchingAndActionNotMatching(@Injectable final BackendFilter filter,
                                                                                @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                @Injectable final UserMessage userMessage,
                                                                                @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                                @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                                @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.OR;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = true;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);

        new Verifications() {{
            criteriaMap.get(actionCriteriaName);
            times = 0;

            actionRoutingCriteriaConfiguration.matches(userMessage, anyString);
            times = 1;
        }};
    }

    @Test
    public void testIsBackendFilterMatchingWithNoRoutingCriteriaDefined(@Injectable final BackendFilter filter,
                                                                        @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                        @Injectable final UserMessage userMessage,
                                                                        @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                        @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                        @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                        @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        new Expectations() {{
            filter.getRoutingCriterias();
            result = null;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertTrue(backendFilterMatching);

        new Verifications() {{
            criteriaMap.get(anyString);
            times = 0;
        }};
    }

    @Test
    public void testIsBackendFilterMatchingANDOperationWithFromNotMatchingAndActionMatching(@Injectable final BackendFilter filter,
                                                                                            @Injectable final Map<String, IRoutingCriteria> criteriaMap,
                                                                                            @Injectable final UserMessage userMessage,
                                                                                            @Injectable final IRoutingCriteria fromRoutingCriteriaConfiguration,
                                                                                            @Injectable final IRoutingCriteria actionRoutingCriteriaConfiguration,
                                                                                            @Injectable final RoutingCriteria fromRoutingCriteria, //contains the FROM filter defined by the user
                                                                                            @Injectable final RoutingCriteria actionRoutingCriteria) { //contains the ACTION filter defined by the user

        // these 2 filters are defined by the user in the Message Filter screen
        final List<RoutingCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(fromRoutingCriteria);
        criteriaList.add(actionRoutingCriteria);

        final String fromCriteriaName = "FROM";
        final String actionCriteriaName = "ACTION";

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

            //filter.getCriteriaOperator();
            //result = LogicalOperator.OR;

            fromRoutingCriteria.getName();
            result = fromCriteriaName;

            fromRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(fromCriteriaName);
            result = fromRoutingCriteriaConfiguration;

            fromRoutingCriteriaConfiguration.matches(userMessage, fromRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);
    }

    @Test
    public void testGetMatchingBackendFilter(@Injectable final UserMessage userMessage, @Injectable final List<BackendFilter> backendFilters) throws Exception {
        new Expectations(backendNotificationService) {{
            backendNotificationService.getBackendFiltersWithCache();
            result = backendFilters;
        }};

        backendNotificationService.getMatchingBackendFilter(userMessage);

        new Verifications() {{
            backendNotificationService.getMatchingBackendFilter(backendFilters, withAny(new HashMap<String, IRoutingCriteria>()), userMessage);
        }};
    }

    @Test
    public void testNotifyOfMessageStatusChange(@Injectable final UserMessageLog messageLog,
                                                @Injectable final MessagingModuleConfiguration messageCommunicationConfiguration) throws Exception {
        final String messageId = "1";
        final String backend = "JMS";
        final MSHRole role = MSHRole.SENDING;

        MessageStatus status = MessageStatus.ACKNOWLEDGED;
        final MessageStatus previousStatus = MessageStatus.SEND_ENQUEUED;
        new Expectations(backendNotificationService) {{
            backendNotificationService.isPluginNotificationDisabled();
            result = false;

            messageLog.getMessageStatus();
            result = previousStatus;

            messageLog.getMessageId();
            result = messageId;

            messageLog.getMshRole();
            result = role;

            messageLog.getBackend();
            result = backend;

            messagingConfigurationManager.getConfiguration();
            result = messageCommunicationConfiguration;

            messageCommunicationConfiguration.shouldMonitorMessageStatus(status);
            result = true;

            backendNotificationService.notify(anyString, anyString, NotificationType.MESSAGE_STATUS_CHANGE, withAny(new HashMap<String, Object>()));
        }};


        backendNotificationService.notifyOfMessageStatusChange(messageLog, status, new Timestamp(System.currentTimeMillis()));

        new VerificationsInOrder() {{
            eventService.enqueueMessageEvent(messageId, previousStatus, status, role);
            String capturedMessageId = null;
            String capturedBackend = null;
            Map<String, Object> properties = null;
            backendNotificationService.notify(capturedMessageId = withCapture(), capturedBackend = withCapture(), NotificationType.MESSAGE_STATUS_CHANGE, properties = withCapture());

            assertEquals(messageId, capturedMessageId);
            assertEquals(capturedBackend, backend);
        }};
    }

    @Test
    public void testInitWithOutEmptyBackendFilter(@Injectable NotificationListener notificationListener,
                                                  @Injectable CriteriaFactory criteriaFactory,
                                                  @Injectable BackendFilterEntity backendFilterEntity) {

        Map notificationListenerBeanMap = new HashMap();
        routingCriteriaFactories.add(criteriaFactory);
        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();
        notificationListenerBeanMap.put("1", notificationListener);
        backendFilterEntities.add(backendFilterEntity);

        new Expectations(backendNotificationService) {{

            applicationContext.getBeansOfType(NotificationListener.class);
            result = notificationListenerBeanMap;

            backendFilterDao.findAll();
            result = backendFilterEntity;

            backendNotificationService.createBackendFiltersBasedOnExistingUserPriority(backendFilterEntities);
            times = 1;
        }};

        backendNotificationService.init();

        new Verifications() {{
            backendNotificationService.createBackendFiltersWithDefaultPriority();
            times = 0;
        }};
    }

    @Test
    public void testInitWithBackendFilterInMultitenancyEnv(@Injectable NotificationListener notificationListener,
                                                           @Injectable CriteriaFactory routingCriteriaFactory,
                                                           @Injectable BackendFilterEntity backendFilterEntity,
                                                           @Injectable Domain domain,
                                                           @Injectable IRoutingCriteria iRoutingCriteria) {

        Map notificationListenerBeanMap = new HashMap();
        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(routingCriteriaFactory);
        backendNotificationService.routingCriteriaFactories = routingCriteriaFactories;
        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();
        backendFilterEntities.add(backendFilterEntity);
        notificationListenerBeanMap.put("1", notificationListener);
        List<Domain> domains = new ArrayList<>();
        domains.add(domain);

        new Expectations(backendNotificationService) {{

            applicationContext.getBeansOfType(NotificationListener.class);
            result = notificationListenerBeanMap;

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domainService.getDomains();
            result = domains;

            routingCriteriaFactory.getName();
            result = anyString;

            routingCriteriaFactory.getInstance();
            result = iRoutingCriteria;
        }};

        backendNotificationService.init();

        new FullVerifications() {{
            domainTaskExecutor.submit((Runnable) any, domain);
            minTimes = 1;
        }};

    }

    @Test
    public void testCreateBackendFilters(@Injectable BackendFilterEntity backendFilterEntity) {
        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();
        backendFilterEntities.add(backendFilterEntity);

        new Expectations(backendNotificationService) {{
            backendFilterDao.findAll();
            result = backendFilterEntities;
            backendNotificationService.createBackendFiltersBasedOnExistingUserPriority(backendFilterEntities);
            times = 1;
        }};
        backendNotificationService.createBackendFilters();
        new Verifications() {{
            backendNotificationService.createBackendFiltersWithDefaultPriority();
            times = 0;
        }};
    }

    @Test
    public void testInitWithEmptyBackendFilter(@Injectable NotificationListener notificationListener,
                                               @Injectable CriteriaFactory routingCriteriaFactory,
                                               @Injectable BackendFilterEntity backendFilterEntity) {

        Map notificationListenerBeanMap = new HashMap();
        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(routingCriteriaFactory);
        backendNotificationService.routingCriteriaFactories = routingCriteriaFactories;
        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();
        notificationListenerBeanMap.put("1", notificationListener);

        new Expectations(backendNotificationService) {{

            applicationContext.getBeansOfType(NotificationListener.class);
            result = notificationListenerBeanMap;

            backendFilterDao.findAll();
            result = backendFilterEntities;

            backendNotificationService.createBackendFiltersWithDefaultPriority();
            times = 1;
        }};

        backendNotificationService.init();

        new Verifications() {{
            backendNotificationService.createBackendFiltersBasedOnExistingUserPriority(backendFilterEntities);
            times = 0;
        }};
    }

    @Test
    public void testCreateBackendFiltersBasedOnExistingUserPriority(@Injectable BackendFilterEntity backendFilterEntity,
                                                                    @Injectable NotificationListener notificationListener) {

        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();
        List<NotificationListener> notificationListenerServices = new ArrayList<>();
        int priority = 0;
        List<String> notificationListenerPluginsList = new ArrayList<>();
        List<String> backendFilterPluginList = new ArrayList<>();
        backendFilterEntities.add(backendFilterEntity);
        notificationListenerServices.add(notificationListener);
        notificationListenerPluginsList.add(BackendPluginEnum.WS_PLUGIN.getPluginName());
        notificationListenerPluginsList.add(BackendPluginEnum.JMS_PLUGIN.getPluginName());
        backendFilterPluginList.add(BackendPluginEnum.WS_PLUGIN.getPluginName());
        backendNotificationService.notificationListenerServices = notificationListenerServices;

        new Expectations(backendNotificationService) {{
            backendFilterEntity.getBackendName();
            result = BackendPluginEnum.WS_PLUGIN.getPluginName();

            notificationListener.getBackendName();
            result = BackendPluginEnum.JMS_PLUGIN.getPluginName();

            notificationListenerServices.stream().map(NotificationListener::getBackendName).collect(Collectors.toList());
            result = notificationListenerPluginsList;

            backendFilterEntities.stream().map(BackendFilterEntity::getBackendName).collect(Collectors.toList());
            result = backendFilterPluginList;

            notificationListenerPluginsList.removeAll(backendFilterPluginList);
            times = 1;

            backendFilterEntities.stream().max(Comparator.comparing(BackendFilterEntity::getIndex)).orElseThrow(NoSuchElementException::new);
            result = priority;
        }};

        backendNotificationService.createBackendFiltersBasedOnExistingUserPriority(backendFilterEntities);

        new Verifications() {{
            List<String> capturedList = null;
            int capturedPriority;
            backendNotificationService.assignPriorityToPlugins(capturedList = withCapture(), capturedPriority = withCapture());
            times = 1;

            assertEquals(capturedList, notificationListenerPluginsList);
            assertEquals(capturedPriority, priority);

            backendFilterDao.create(backendFilterEntities);
            times = 0;
        }};
    }

    @Test
    public void testAssignPriorityToPlugins(@Injectable BackendFilterEntity backendFilterEntity) {
        List<String> pluginList = new ArrayList<>();
        int priority = 0;
        pluginList.add(BackendPluginEnum.FS_PLUGIN.getPluginName());
        pluginList.add(BackendPluginEnum.JMS_PLUGIN.getPluginName());
        pluginList.add(BackendPluginEnum.WS_PLUGIN.getPluginName());
        List<String> defaultPluginOrderList = Arrays.asList(BackendPluginEnum.WS_PLUGIN.getPluginName(), BackendPluginEnum.JMS_PLUGIN.getPluginName(), BackendPluginEnum.FS_PLUGIN.getPluginName());

        new Expectations(backendNotificationService) {{
            pluginList.sort(Comparator.comparing(defaultPluginOrderList::indexOf));
        }};

        List<BackendFilterEntity> backendFilters = backendNotificationService.assignPriorityToPlugins(pluginList, priority);

        assertEquals(backendFilters.get(0).getBackendName(), BackendPluginEnum.WS_PLUGIN.getPluginName());
        assertEquals(backendFilters.get(1).getBackendName(), BackendPluginEnum.JMS_PLUGIN.getPluginName());
        assertEquals(backendFilters.get(2).getBackendName(), BackendPluginEnum.FS_PLUGIN.getPluginName());
    }

    @Test
    public void testCreateWSBackendFiltersWithDefaultPriority(@Injectable NotificationListener notificationListener,
                                                              @Injectable BackendFilterEntity backendFilterEntity) {

        List<NotificationListener> notificationListenerServices = new ArrayList<>();
        List<BackendFilterEntity> backendFilters = new ArrayList<>();
        notificationListenerServices.add(notificationListener);
        backendNotificationService.notificationListenerServices = notificationListenerServices;
        backendFilters.add(backendFilterEntity);

        new Expectations() {{
            notificationListener.getBackendName();
            result = BackendPluginEnum.WS_PLUGIN.getPluginName();
        }};

        backendNotificationService.createBackendFiltersWithDefaultPriority();

        new Verifications() {{
            backendFilterDao.create(backendFilters);
            times = 1;
        }};
    }

    @Test
    public void testGetBackendFiltersWithCache(@Injectable List<BackendFilter> backendFilters) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.getBackendFilters();
            result = backendFilters;
        }};

        backendNotificationService.backendFiltersCache = null;
        backendNotificationService.getBackendFiltersWithCache();
        backendNotificationService.getBackendFiltersWithCache();

        new FullVerifications() {{
            backendNotificationService.getBackendFilters();
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
        }};

        backendNotificationService.notifyMessageReceivedFailure(userMessage, errorResult);

        new Verifications() {{
            Map<String, Object> properties = null;
            backendNotificationService.notifyOfIncoming(userMessage, NotificationType.MESSAGE_FRAGMENT_RECEIVED_FAILURE, properties = withCapture());

            assertEquals(errorCodeName, properties.get(MessageConstants.ERROR_CODE));
            assertEquals(errorDetail, properties.get(MessageConstants.ERROR_DETAIL));
            assertEquals(service, properties.get(MessageConstants.SERVICE));
            assertEquals(serviceType, properties.get(MessageConstants.SERVICE_TYPE));
            assertEquals(action, properties.get(MessageConstants.ACTION));
        }};
    }
}
