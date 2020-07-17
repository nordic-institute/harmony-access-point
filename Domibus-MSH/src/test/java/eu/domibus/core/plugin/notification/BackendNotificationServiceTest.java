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
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import javax.jms.Queue;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE;
import static eu.domibus.common.NotificationType.*;
import static eu.domibus.core.plugin.notification.BackendPluginEnum.*;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by baciuco on 08/08/2016.
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
    public static final String UNKNOWN_RECEIVER_QUEUE = "unknownReceiverQueue";
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
    UserMessageLogDao userMessageLogDao;

    @Injectable
    SubmissionAS4Transformer submissionAS4Transformer;

    @Injectable
    SubmissionValidatorListProvider submissionValidatorListProvider;

    @Injectable
    UserMessageHandlerService userMessageHandlerService;

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
    protected List<BackendConnector<?, ?>> backendConnectors;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomainService domainService;

    @Mock
    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Test
    public void testValidateSubmissionForUnsupportedNotificationType(@Injectable final Submission submission,
                                                                     @Injectable final UserMessage userMessage) {
        final String backendName = "customPlugin";
        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED_FAILURE);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateSubmissionWhenFirstValidatorThrowsException(@Injectable final Submission submission,
                                                                        @Injectable final UserMessage userMessage,
                                                                        @Injectable final SubmissionValidatorList submissionValidatorList,
                                                                        @Injectable final SubmissionValidator validator1,
                                                                        @Injectable final SubmissionValidator validator2) {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionAS4Transformer.transformFromMessaging(userMessage);
            result = submission;
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = submissionValidatorList;
            submissionValidatorList.getSubmissionValidators();
            result = new SubmissionValidator[]{validator1, validator2};
            validator1.validate(submission);
            result = new SubmissionValidationException("Exception in the validator1");
        }};

        thrown.expect(SubmissionValidationException.class);
        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateSubmissionWithAllValidatorsCalled(@Injectable final Submission submission,
                                                              @Injectable final UserMessage userMessage,
                                                              @Injectable final SubmissionValidatorList submissionValidatorList,
                                                              @Injectable final SubmissionValidator validator1,
                                                              @Injectable final SubmissionValidator validator2) {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionAS4Transformer.transformFromMessaging(userMessage);
            result = submission;
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = submissionValidatorList;
            submissionValidatorList.getSubmissionValidators();
            result = new SubmissionValidator[]{validator1, validator2};
        }};

        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new FullVerifications() {{
            validator1.validate(submission);
            times = 1;
            validator2.validate(submission);
            times = 1;
        }};
    }

    @Test
    public void testValidateSubmission_noValidator(@Injectable final Submission submission,
                                                   @Injectable final UserMessage userMessage,
                                                   @Injectable final SubmissionValidatorList submissionValidatorList,
                                                   @Injectable final SubmissionValidator validator1,
                                                   @Injectable final SubmissionValidator validator2) {
        final String backendName = "customPlugin";
        new Expectations() {{
            submissionValidatorListProvider.getSubmissionValidatorList(backendName);
            result = null;
        }};

        backendNotificationService.validateSubmission(userMessage, backendName, NotificationType.MESSAGE_RECEIVED);

        new FullVerifications() {
        };
    }

    @Test
    public void testGetNotificationListener(@Injectable final NotificationListener notificationListener1,
                                            @Injectable final NotificationListener notificationListener2) {
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
    public void testGetNotificationListener_empty(@Injectable final NotificationListener notificationListener1,
                                                  @Injectable final NotificationListener notificationListener2) {
        final String backendName = "customPlugin";

        backendNotificationService.notificationListenerServices = new ArrayList<>();

        NotificationListener notificationListener = backendNotificationService.getNotificationListener(backendName);
        assertNull(notificationListener);

    }

    @Test
    public void testValidateAndNotify_propertyNull(@Injectable final UserMessage userMessage) {
        String backendName = "backendName";
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;
        new Expectations(backendNotificationService) {{
            backendNotificationService.validateSubmission(userMessage, backendName, notificationType);

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
            backendNotificationService.validateSubmission(userMessage, backendName, notificationType);

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
        new Expectations(backendNotificationService){{
            backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, notificationType, null);
            times = 1;
        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, notificationType);

        new FullVerifications(){};
    }

    @Test
    public void testNotifyWithNoConfiguredNotificationListener(
            @Injectable final NotificationType notificationType,
            @Injectable final Queue queue) {
        final String backendName = "customPlugin";
        new Expectations(backendNotificationService) {{
            backendNotificationService.getNotificationListener(backendName);
            result = null;
        }};

        backendNotificationService.notify("messageId", backendName, notificationType, null);

        new FullVerifications() {};
    }

    @Test
    public void notify(
            @Injectable final NotificationListener notificationListener,
            @Injectable final Queue queue) {

        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);

        new Expectations(backendNotificationService) {{
            backendNotificationService.getNotificationListener(BACKEND_NAME);
            result = notificationListener;

            notificationListener.getRequiredNotificationTypeList();
            result = requiredNotifications;

            notificationListener.getBackendNotificationQueue();
            result = queue;
        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {{
            JmsMessage jmsMessage;
            jmsManager.sendMessageToQueue(jmsMessage = withCapture(), queue);
            times = 1;

            assertEquals(MESSAGE_ID, jmsMessage.getProperty(MessageConstants.MESSAGE_ID));
            assertEquals(NotificationType.MESSAGE_RECEIVED.name(), jmsMessage.getProperty(MessageConstants.NOTIFICATION_TYPE));
        }};
    }

    @Test
    public void notify_propertiesNotNull_backendNotificationQueueNull(
            @Injectable final NotificationListener notificationListener,
            @Injectable final Queue queue) {

        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);

        new Expectations(backendNotificationService) {{
            backendNotificationService.getNotificationListener(BACKEND_NAME);
            result = notificationListener;

            notificationListener.getRequiredNotificationTypeList();
            result = requiredNotifications;

            notificationListener.getBackendNotificationQueue();
            result = null;
        }};

        HashMap<String, Object> properties = new HashMap<>();
        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, properties);

        new FullVerifications() {{
            notificationListener.notify(MESSAGE_ID, NotificationType.MESSAGE_RECEIVED, properties);
            times = 1;
        }};
    }

    @Test
    public void notify_NoNotification(
            @Injectable final NotificationListener notificationListener,
            @Injectable final Queue queue) {

        new Expectations(backendNotificationService) {{
            backendNotificationService.getNotificationListener(BACKEND_NAME);
            result = notificationListener;

            notificationListener.getRequiredNotificationTypeList();
            result = null;

            notificationListener.getMode();
            result = BackendConnector.Mode.PUSH;

        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {};
    }

    @Test
    public void notify_NotificationNotMatchType(
            @Injectable final NotificationListener notificationListener,
            @Injectable final Queue queue) {

        List<NotificationType> requiredNotifications = new ArrayList<>();
        requiredNotifications.add(MESSAGE_STATUS_CHANGE);

        new Expectations(backendNotificationService) {{
            backendNotificationService.getNotificationListener(BACKEND_NAME);
            result = notificationListener;

            notificationListener.getRequiredNotificationTypeList();
            result = requiredNotifications;

            notificationListener.getMode();
            result = BackendConnector.Mode.PUSH;

        }};

        backendNotificationService.notify(MESSAGE_ID, BACKEND_NAME, NotificationType.MESSAGE_RECEIVED, null);

        new FullVerifications() {};
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

        new FullVerifications() {{
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

            actionRoutingCriteria.getName();
            result = actionCriteriaName;

            actionRoutingCriteria.getExpression();
            result = "domibus-blue:partyType";

            criteriaMap.get(actionCriteriaName);
            result = actionRoutingCriteriaConfiguration;

            actionRoutingCriteriaConfiguration.matches(userMessage, actionRoutingCriteria.getExpression());
            result = false;
        }};

        final boolean backendFilterMatching = backendNotificationService.isBackendFilterMatching(filter, criteriaMap, userMessage);
        Assert.assertFalse(backendFilterMatching);

        new FullVerifications() {{
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

        new FullVerifications() {{
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

        new Expectations() {{
            filter.getRoutingCriterias();
            result = criteriaList;

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
    public void testGetMatchingBackendFilter(@Injectable final UserMessage userMessage,
                                             @Injectable final List<BackendFilter> backendFilters) {
        new Expectations(backendNotificationService) {{
            backendNotificationService.getBackendFiltersWithCache();
            result = backendFilters;
            backendNotificationService.getMatchingBackendFilter(backendFilters, withAny(new HashMap<>()), userMessage);
        }};

        backendNotificationService.getMatchingBackendFilter(userMessage);

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
    public void testInitWithOutEmptyBackendFilter(@Injectable NotificationListener notificationListener,
                                                  @Injectable CriteriaFactory criteriaFactory,
                                                  @Injectable BackendFilterEntity backendFilterEntity,
                                                  @Injectable IRoutingCriteria iRoutingCriteria) {

        Map<String, NotificationListener> notificationListenerBeanMap = new HashMap<>();
        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(criteriaFactory);
        backendNotificationService.routingCriteriaFactories = routingCriteriaFactories;
        notificationListenerBeanMap.put("1", notificationListener);

        new Expectations(backendNotificationService) {{

            applicationContext.getBeansOfType(NotificationListener.class);
            result = notificationListenerBeanMap;

            domibusConfigurationService.isMultiTenantAware();
            result = false;

            criteriaFactory.getName();
            result = "Name criteriaFactory";

            criteriaFactory.getInstance();
            result = iRoutingCriteria;

            backendNotificationService.createBackendFilters();
            times = 1;
        }};

        backendNotificationService.init();

        new FullVerifications() {};
    }

    @Test
    public void testInitWithBackendFilterInMultitenancyEnv(@Injectable NotificationListener notificationListener,
                                                           @Injectable CriteriaFactory routingCriteriaFactory,
                                                           @Injectable BackendFilterEntity backendFilterEntity,
                                                           @Injectable Domain domain,
                                                           @Injectable IRoutingCriteria iRoutingCriteria) {

        Map<String, NotificationListener> notificationListenerBeanMap = new HashMap<>();
        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(routingCriteriaFactory);
        backendNotificationService.routingCriteriaFactories = routingCriteriaFactories;
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
        List<BackendFilterEntity> toBeCreated = new ArrayList<>();

        new Expectations(backendNotificationService) {{
            backendFilterDao.findAll();
            result = backendFilterEntities;

            backendNotificationService.createMissingBackendFilters(backendFilterEntities);
            result = toBeCreated;
            times = 1;
        }};

        backendNotificationService.createBackendFilters();
        new FullVerifications() {{
            backendFilterDao.create(toBeCreated);
            times = 1;
        }};
    }

    @Test
    public void testCreateBackendFilters_empty(@Injectable BackendFilterEntity backendFilterEntity,
                                               @Injectable List<BackendFilterEntity> entities) {
        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();

        new Expectations(backendNotificationService) {{
            backendFilterDao.findAll();
            result = backendFilterEntities;
            backendNotificationService.createAllBackendFilters();
            times = 1;
            result = entities;
        }};
        backendNotificationService.createBackendFilters();
        new FullVerifications() {{
            backendFilterDao.create(entities);
            times = 1;
        }};
    }

    @Test
    public void testInit_noNotificationListenerBeanMap(@Injectable NotificationListener notificationListener,
                                                       @Injectable CriteriaFactory routingCriteriaFactory,
                                                       @Injectable BackendFilterEntity backendFilterEntity) {

        Map<String, NotificationListener> notificationListenerBeanMap = new HashMap<>();
        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(routingCriteriaFactory);
        backendNotificationService.routingCriteriaFactories = routingCriteriaFactories;

        new Expectations(backendNotificationService) {{

            applicationContext.getBeansOfType(NotificationListener.class);
            result = notificationListenerBeanMap;
        }};

        thrown.expect(ConfigurationException.class);
        backendNotificationService.init();

        new FullVerifications() {
        };
    }

    @Test
    public void testInitMultiAware(@Injectable NotificationListener notificationListener,
                                   @Injectable CriteriaFactory routingCriteriaFactory,
                                   @Injectable BackendFilterEntity backendFilterEntity,
                                   @Injectable Domain domain) {

        Map<String, NotificationListener> notificationListenerBeanMap = new HashMap<>();
        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(routingCriteriaFactory);
        backendNotificationService.routingCriteriaFactories = routingCriteriaFactories;
        notificationListenerBeanMap.put("1", notificationListener);

        new Expectations(backendNotificationService) {{

            applicationContext.getBeansOfType(NotificationListener.class);
            result = notificationListenerBeanMap;

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domainService.getDomains();
            result = Collections.singletonList(domain);

            routingCriteriaFactory.getName();
            result = "routingCriteriaFactory";

            routingCriteriaFactory.getInstance();
            result = null;
        }};

        backendNotificationService.init();

        new FullVerifications() {{
            domainTaskExecutor.submit((Runnable) any, domain);
            times = 1;
        }};
    }

    @Test
    public void testInit_NonMultiTenancy(@Injectable NotificationListener notificationListener,
                                         @Injectable CriteriaFactory routingCriteriaFactory,
                                         @Injectable BackendFilterEntity backendFilterEntity,
                                         @Injectable Domain domain) {

        Map<String, NotificationListener> notificationListenerBeanMap = new HashMap<>();
        List<CriteriaFactory> routingCriteriaFactories = new ArrayList<>();
        routingCriteriaFactories.add(routingCriteriaFactory);
        backendNotificationService.routingCriteriaFactories = routingCriteriaFactories;
        notificationListenerBeanMap.put("1", notificationListener);

        new Expectations(backendNotificationService) {{

            applicationContext.getBeansOfType(NotificationListener.class);
            result = notificationListenerBeanMap;

            domibusConfigurationService.isMultiTenantAware();
            result = false;

            routingCriteriaFactory.getName();
            result = "routingCriteriaFactory";

            routingCriteriaFactory.getInstance();
            result = null;

            backendNotificationService.createBackendFilters();
        }};

        backendNotificationService.init();

        new FullVerifications() {
        };
    }

    @Test
    public void testCreateBackendFiltersBasedOnExistingUserPriority(@Injectable BackendFilterEntity backendFilterEntity,
                                                                    @Injectable NotificationListener notificationListener) {

        List<BackendFilterEntity> backendFilterEntities = new ArrayList<>();
        List<NotificationListener> notificationListenerServices = new ArrayList<>();
        List<String> notificationListenerPluginsList = new ArrayList<>();
        List<String> backendFilterPluginList = new ArrayList<>();
        backendFilterEntities.add(backendFilterEntity);
        notificationListenerServices.add(notificationListener);
        notificationListenerPluginsList.add(WS_PLUGIN.getPluginName());
        notificationListenerPluginsList.add(JMS_PLUGIN.getPluginName());
        backendFilterPluginList.add(WS_PLUGIN.getPluginName());
        backendNotificationService.notificationListenerServices = notificationListenerServices;

        new Expectations(backendNotificationService) {{
            backendFilterEntity.getBackendName();
            result = WS_PLUGIN.getPluginName();

            notificationListener.getBackendName();
            result = JMS_PLUGIN.getPluginName();

            notificationListenerServices.stream().map(NotificationListener::getBackendName).collect(Collectors.toList());
            result = notificationListenerPluginsList;

            backendFilterEntities.stream().map(BackendFilterEntity::getBackendName).collect(Collectors.toList());
            result = backendFilterPluginList;

            notificationListenerPluginsList.removeAll(backendFilterPluginList);
            times = 1;

            backendNotificationService.getMaxIndex(backendFilterEntities);
            result = 1;

            backendNotificationService.createBackendFilterEntities(notificationListenerPluginsList, 2);
            result = backendFilterEntities;
        }};

        List<BackendFilterEntity> missingBackendFilters = backendNotificationService.createMissingBackendFilters(backendFilterEntities);

        assertEquals(backendFilterEntities, missingBackendFilters);

        new FullVerifications(){};
    }

    @Test
    public void createBackendFilterEntity_empty(@Injectable BackendFilterEntity backendFilterEntity) {
        List<BackendFilterEntity> backendFilters = backendNotificationService.createBackendFilterEntities(null, 1);
        assertEquals(0, backendFilters.size());
    }

    @Test
    public void createBackendFilterEntity(@Injectable BackendFilterEntity backendFilterEntity) {
        List<String> pluginList = new ArrayList<>();
        int priority = 4;
        pluginList.add(FS_PLUGIN.getPluginName());
        pluginList.add("TEST2");
        pluginList.add(JMS_PLUGIN.getPluginName());
        pluginList.add("TEST1");
        pluginList.add(WS_PLUGIN.getPluginName());
        pluginList.add("TEST3");

        List<BackendFilterEntity> backendFilters = backendNotificationService.createBackendFilterEntities(pluginList, priority);

        assertEquals("TEST2", backendFilters.get(0).getBackendName());
        assertEquals(4, backendFilters.get(0).getIndex());
        assertEquals("TEST1", backendFilters.get(1).getBackendName());
        assertEquals(5, backendFilters.get(1).getIndex());
        assertEquals("TEST3", backendFilters.get(2).getBackendName());
        assertEquals(6, backendFilters.get(2).getIndex());
        assertEquals(WS_PLUGIN.getPluginName(), backendFilters.get(3).getBackendName());
        assertEquals(7, backendFilters.get(3).getIndex());
        assertEquals(JMS_PLUGIN.getPluginName(), backendFilters.get(4).getBackendName());
        assertEquals(8, backendFilters.get(4).getIndex());
        assertEquals(FS_PLUGIN.getPluginName(), backendFilters.get(5).getBackendName());
        assertEquals(9, backendFilters.get(5).getIndex());
    }

    @Test
    public void testCreateWSBackendFiltersWithDefaultPriority(@Injectable NotificationListener notificationListener1,
                                                              @Injectable NotificationListener notificationListener2,
                                                              @Injectable NotificationListener notificationListener3,
                                                              @Injectable BackendFilterEntity backendFilterEntity) {

        List<NotificationListener> notificationListenerServices = new ArrayList<>();
        notificationListenerServices.add(notificationListener1);
        notificationListenerServices.add(notificationListener2);
        notificationListenerServices.add(notificationListener3);
        backendNotificationService.notificationListenerServices = notificationListenerServices;

        new Expectations() {{
            notificationListener1.getBackendName();
            result = FS_PLUGIN.getPluginName();
            notificationListener2.getBackendName();
            result = JMS_PLUGIN.getPluginName();
            notificationListener3.getBackendName();
            result = WS_PLUGIN.getPluginName();
        }};

        List<BackendFilterEntity> allBackendFilters = backendNotificationService.createAllBackendFilters();

        new FullVerifications() {};

        assertThat(allBackendFilters.size(), is(3));
        assertThat(allBackendFilters.get(2).getBackendName(), is(FS_PLUGIN.getPluginName()));
        assertThat(allBackendFilters.get(2).getIndex(), is(2));
        assertThat(allBackendFilters.get(1).getBackendName(), is(JMS_PLUGIN.getPluginName()));
        assertThat(allBackendFilters.get(1).getIndex(), is(1));
        assertThat(allBackendFilters.get(0).getBackendName(), is(WS_PLUGIN.getPluginName()));
        assertThat(allBackendFilters.get(0).getIndex(), is(0));
    }


    @Test
    public void testCreateWSBackendFiltersWithDefaultPriority2(@Injectable NotificationListener default1,
                                                              @Injectable NotificationListener default2,
                                                              @Injectable NotificationListener default3,
                                                              @Injectable NotificationListener other1,
                                                              @Injectable NotificationListener other2,
                                                              @Injectable BackendFilterEntity backendFilterEntity) {

        List<NotificationListener> notificationListenerServices = new ArrayList<>();
        notificationListenerServices.add(other1);
        notificationListenerServices.add(default1);
        notificationListenerServices.add(default2);
        notificationListenerServices.add(default3);
        notificationListenerServices.add(other2);
        backendNotificationService.notificationListenerServices = notificationListenerServices;

        new Expectations() {{
            other1.getBackendName();
            result = "OTHER1";
            default1.getBackendName();
            result = FS_PLUGIN.getPluginName();
            default2.getBackendName();
            result = JMS_PLUGIN.getPluginName();
            default3.getBackendName();
            result = WS_PLUGIN.getPluginName();
            other2.getBackendName();
            result = "OTHER2";
        }};

        List<BackendFilterEntity> allBackendFilters = backendNotificationService.createAllBackendFilters();

        new FullVerifications() {};

        assertThat(allBackendFilters.size(), is(5));
        assertThat(allBackendFilters.get(0).getBackendName(), is("OTHER1"));
        assertThat(allBackendFilters.get(0).getIndex(), is(0));
        assertThat(allBackendFilters.get(1).getBackendName(), is("OTHER2"));
        assertThat(allBackendFilters.get(1).getIndex(), is(1));
        assertThat(allBackendFilters.get(2).getBackendName(), is(WS_PLUGIN.getPluginName()));
        assertThat(allBackendFilters.get(2).getIndex(), is(2));
        assertThat(allBackendFilters.get(3).getBackendName(), is(JMS_PLUGIN.getPluginName()));
        assertThat(allBackendFilters.get(3).getIndex(), is(3));
        assertThat(allBackendFilters.get(4).getBackendName(), is(FS_PLUGIN.getPluginName()));
        assertThat(allBackendFilters.get(4).getIndex(), is(4));
    }

    @Test
    public void testGetBackendFiltersWithCache(@Injectable List<BackendFilter> backendFilters) {
        new Expectations(backendNotificationService) {{
            domainContextProvider.getCurrentDomain();
            result = new Domain();

            backendNotificationService.getBackendFilters();
            result = backendFilters;
        }};

        backendNotificationService.backendFiltersCache = new HashMap<>();
        List<BackendFilter> backendFiltersWithCache = backendNotificationService.getBackendFiltersWithCache();
        List<BackendFilter> backendFiltersWithCache1 = backendNotificationService.getBackendFiltersWithCache();

        assertNotNull(backendFiltersWithCache);
        assertNotNull(backendFiltersWithCache1);

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
    public void invalidateBackendFiltersCache() {
        Domain domain1 = new Domain("D1", "DOMAIN1");
        Domain domain2 = new Domain("D2", "DOMAIN2");
        backendNotificationService.backendFiltersCache = new HashMap<>();
        backendNotificationService.backendFiltersCache.put(domain1, new ArrayList<>());
        backendNotificationService.backendFiltersCache.put(domain2, new ArrayList<>());

        assertEquals(2, backendNotificationService.backendFiltersCache.size());

        new Expectations(){{
            domainContextProvider.getCurrentDomain();
            result = domain1;
        }};

        backendNotificationService.invalidateBackendFiltersCache();

        assertEquals(1, backendNotificationService.backendFiltersCache.size());
        assertNull(backendNotificationService.backendFiltersCache.get(domain1));
        assertNotNull(backendNotificationService.backendFiltersCache.get(domain2));
    }

    @Test
    public void getBackendFilters_backendFilterNotEmptyInDao() {
        ArrayList<BackendFilterEntity> backendFilterEntityList = new ArrayList<>();
        backendFilterEntityList.add(new BackendFilterEntity());

        ArrayList<BackendFilter> backendFilters = new ArrayList<>();

        new Expectations() {{
            backendFilterDao.findAll();
            result = backendFilterEntityList;

            coreConverter.convert(backendFilterEntityList, BackendFilter.class);
            result = backendFilters;
        }};
        List<BackendFilter> actual = backendNotificationService.getBackendFilters();

        assertEquals(backendFilters, actual);

        new FullVerifications() {
        };
    }

    @Test
    public void getBackendFilters_empty() {
        ArrayList<BackendFilterEntity> backendFilterEntityList = new ArrayList<>();

        ArrayList<BackendFilter> backendFilters = new ArrayList<>();

        new Expectations() {{
            backendFilterDao.findAll();
            result = backendFilterEntityList;

            routingService.getBackendFilters();
            result = backendFilters;
        }};

        List<BackendFilter> actual = backendNotificationService.getBackendFilters();

        assertTrue(actual.isEmpty());

        new FullVerifications() {
        };
    }

    @Test
    public void getBackendFilters_return1() {
        ArrayList<BackendFilterEntity> backendFilterEntityList = new ArrayList<>();

        ArrayList<BackendFilter> backendFilters = new ArrayList<>();
        backendFilters.add(new BackendFilter());

        new Expectations() {{
            backendFilterDao.findAll();
            result = backendFilterEntityList;

            routingService.getBackendFilters();
            result = backendFilters;
        }};

        List<BackendFilter> actual = backendNotificationService.getBackendFilters();

        assertEquals(backendFilters, actual);

        new FullVerifications() {
        };
    }

    @Test
    public void getBackendFilters_return2() {
        ArrayList<BackendFilterEntity> backendFilterEntityList = new ArrayList<>();

        ArrayList<BackendFilter> backendFilters = new ArrayList<>();
        backendFilters.add(new BackendFilter());
        backendFilters.add(new BackendFilter());

        new Expectations() {{
            backendFilterDao.findAll();
            result = backendFilterEntityList;

            routingService.getBackendFilters();
            result = backendFilters;
        }};

        List<BackendFilter> actual = backendNotificationService.getBackendFilters();

        assertTrue(actual.isEmpty());

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
    public void notifyMessageReceived_Notfragment(
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

            backendNotificationService.getBackendConnector(BACKEND_NAME);
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

            backendNotificationService.getBackendConnector(BACKEND_NAME);
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
    public void getBackendConnector_empty() {
        backendNotificationService.backendConnectors = new ArrayList<>();

        BackendConnector<?, ?> backendConnector = backendNotificationService.getBackendConnector(BACKEND_NAME);

        assertNull(backendConnector);
    }

    @Test
    public void getBackendConnector(@Injectable BackendConnector<?, ?> b1,
                                    @Injectable BackendConnector<?, ?> b2,
                                    @Injectable BackendConnector<?, ?> b3) {
        backendNotificationService.backendConnectors = asList(b1, b2, b3);
        new Expectations() {{
            b1.getName();
            result = "b1";
            b2.getName();
            result = "b2";
            b3.getName();
            result = BACKEND_NAME;
        }};

        BackendConnector<?, ?> backendConnector = backendNotificationService.getBackendConnector(BACKEND_NAME);

        assertEquals(b3, backendConnector);
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
            backendNotificationService.getMatchingBackendFilter(userMessage);
            result = matchingBackendFilter;

            backendNotificationService.notifyOfIncoming(matchingBackendFilter, userMessage, notificationType, properties);
            times = 1;
        }};

        backendNotificationService.notifyOfIncoming(userMessage, notificationType, properties);

        new FullVerifications() {
        };
    }

    @Test
    public void getMatchingBackendFilter(
            @Injectable Map<String, IRoutingCriteria> criteriaMap,
            @Injectable UserMessage userMessage,
            @Injectable BackendFilter backendFilter1,
            @Injectable BackendFilter backendFilter2) {
        List<BackendFilter> backendFilters = asList(backendFilter1, backendFilter2);

        new Expectations(backendNotificationService) {{
            userMessage.getMessageInfo().getMessageId();
            result = MESSAGE_ID;

            backendNotificationService.isBackendFilterMatching(backendFilter1, criteriaMap, userMessage);
            result = false;

            backendNotificationService.isBackendFilterMatching(backendFilter2, criteriaMap, userMessage);
            result = true;
        }};

        BackendFilter matchingBackendFilter = backendNotificationService.getMatchingBackendFilter(backendFilters, criteriaMap, userMessage);

        assertEquals(backendFilter2, matchingBackendFilter);

        //No fullVerifications because of UnexpectedInvocation BackendFilter#toString()
        new Verifications() {
        };
    }

    @Test
    public void getMatchingBackendFilter_noFilters(
            @Injectable Map<String, IRoutingCriteria> criteriaMap,
            @Injectable UserMessage userMessage) {
        List<BackendFilter> backendFilters = new ArrayList<>();

        new Expectations() {{
            userMessage.getMessageInfo().getMessageId();
            result = MESSAGE_ID;
        }};

        BackendFilter matchingBackendFilter = backendNotificationService.getMatchingBackendFilter(backendFilters, criteriaMap, userMessage);

        assertNull(matchingBackendFilter);

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
    public void notifyOfSendFailure_Nofragment(@Injectable UserMessageLog userMessageLog) {
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

        new Expectations(){{
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

        new FullVerifications(){};

    }

    @Test
    public void getMessageProperties_noMessage_NoUserMessage(@Injectable MessageLog messageLog,
                                     @Injectable UserMessage userMessage) {
        MessageStatus newStatus = MessageStatus.ACKNOWLEDGED;

        new Expectations(){{
            messageLog.getMessageStatus();
            result = null;

        }};

        Map<String, Object> messageProperties = backendNotificationService.getMessageProperties(messageLog, null, newStatus, TIMESTAMP);

        assertThat(messageProperties.size(), is(2));
        assertThat(messageProperties.get(MessageConstants.STATUS_TO), is(MessageStatus.ACKNOWLEDGED.toString()));
        assertThat(messageProperties.get(MessageConstants.CHANGE_TIMESTAMP), is(TIMESTAMP.getTime()));

        new FullVerifications(){};
    }
}
