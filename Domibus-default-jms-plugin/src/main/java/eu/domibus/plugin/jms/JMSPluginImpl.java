package eu.domibus.plugin.jms;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.common.*;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.text.MessageFormat;
import java.util.List;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public class JMSPluginImpl extends AbstractBackendConnector<MapMessage, MapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSPluginImpl.class);

    public static final String PLUGIN_NAME = "Jms";

    protected JMSExtService jmsExtService;
    protected DomainContextExtService domainContextExtService;
    protected JMSPluginQueueService jmsPluginQueueService;
    protected JmsOperations mshToBackendTemplate;
    protected JMSMessageTransformer jmsMessageTransformer;
    protected MetricRegistry metricRegistry;

    public JMSPluginImpl(MetricRegistry metricRegistry,
                         JMSExtService jmsExtService,
                         DomainContextExtService domainContextExtService,
                         JMSPluginQueueService jmsPluginQueueService,
                         JmsOperations mshToBackendTemplate,
                         JMSMessageTransformer jmsMessageTransformer) {
        super(PLUGIN_NAME);
        this.jmsExtService = jmsExtService;
        this.domainContextExtService = domainContextExtService;
        this.jmsPluginQueueService = jmsPluginQueueService;
        this.mshToBackendTemplate = mshToBackendTemplate;
        this.jmsMessageTransformer = jmsMessageTransformer;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public MessageSubmissionTransformer<MapMessage> getMessageSubmissionTransformer() {
        return this.jmsMessageTransformer;
    }

    @Override
    public MessageRetrievalTransformer<MapMessage> getMessageRetrievalTransformer() {
        return this.jmsMessageTransformer;
    }

    /**
     * This method is called when a message was received at the incoming queue
     *
     * @param map The incoming JMS Message
     */
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Transactional
    @Timer(clazz = JMSPluginImpl.class, value = "receiveMessage")
    @Counter(clazz = JMSPluginImpl.class, value = "receiveMessage")
    public void receiveMessage(final MapMessage map) {
        try {
            String messageID = map.getStringProperty(MESSAGE_ID);
            if (StringUtils.isNotBlank(messageID)) {
                //trim the empty space
                messageID = messageExtService.cleanMessageIdentifier(messageID);
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageID);
            }
            final String jmsCorrelationID = map.getJMSCorrelationID();
            final String messageType = map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);

            LOG.info("Received message with messageId [{}], jmsCorrelationID [{}]", messageID, jmsCorrelationID);

            QueueContext queueContext = jmsMessageTransformer.getQueueContext(messageID, map);
            LOG.debug("Extracted queue context [{}]", queueContext);

            if (!MESSAGE_TYPE_SUBMIT.equals(messageType)) {
                String wrongMessageTypeMessage = getWrongMessageTypeErrorMessage(messageID, jmsCorrelationID, messageType);
                LOG.error(wrongMessageTypeMessage);
                sendReplyMessage(queueContext, wrongMessageTypeMessage, jmsCorrelationID);
                return;
            }

            String errorMessage = null;
            try {
                //in case the messageID is not sent by the user it will be generated
                messageID = submit(map);
            } catch (final MessagingProcessingException e) {
                LOG.error("Exception occurred receiving message [{}}], jmsCorrelationID [{}}]", messageID, jmsCorrelationID, e);
                errorMessage = e.getMessage() + ": Error Code: " + (e.getEbms3ErrorCode() != null ? e.getEbms3ErrorCode().getErrorCodeName() : " not set");
            }

            sendReplyMessage(queueContext, errorMessage, jmsCorrelationID);

            LOG.info("Submitted message with messageId [{}], jmsCorrelationID [{}}]", messageID, jmsCorrelationID);
        } catch (Exception e) {
            LOG.error("Exception occurred while receiving message [" + map + "]", e);
            throw new DefaultJmsPluginException("Exception occurred while receiving message [" + map + "]", e);
        }
    }

    protected String getWrongMessageTypeErrorMessage(String messageID, String jmsCorrelationID, String messageType) {
        return MessageFormat.format("Illegal messageType [{0}] on message with JMSCorrelationId [{1}] and messageId [{2}]. Only [{3}] messages are accepted on this queue",
                messageType, jmsCorrelationID, messageID, MESSAGE_TYPE_SUBMIT);
    }

    protected void sendReplyMessage(QueueContext queueContext, final String errorMessage, final String correlationId) {
        String messageId = queueContext.getMessageId();
        LOG.info("Sending reply message with message id [{}], error message [{}] and correlation id [{}]", messageId, errorMessage, correlationId);

        final JmsMessageDTO jmsMessageDTO = new ReplyMessageCreator(messageId, errorMessage, correlationId).createMessage();
        sendJmsMessage(jmsMessageDTO, queueContext, JMSPLUGIN_QUEUE_REPLY, JMSPLUGIN_QUEUE_REPLY_ROUTING);
    }

    @Override
    @Timer(clazz = JMSPluginImpl.class, value = "deliverMessage")
    @Counter(clazz = JMSPluginImpl.class, value = "deliverMessage")
    public void deliverMessage(final DeliverMessageEvent event) {
        String messageId = event.getMessageId();
        LOG.debug("Delivering message [{}] for final recipient [{}]", messageId, event.getFinalRecipient());

        final String queueValue = jmsPluginQueueService.getJMSQueue(messageId, JMSPLUGIN_QUEUE_OUT, JMSPLUGIN_QUEUE_OUT_ROUTING);
        LOG.info("Sending message to queue [{}]", queueValue);
        mshToBackendTemplate.send(queueValue, new DownloadMessageCreator(messageId));
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        LOG.debug("Handling messageReceiveFailed");
        final JmsMessageDTO jmsMessageDTO = new ErrorMessageCreator(messageReceiveFailureEvent.getErrorResult(),
                messageReceiveFailureEvent.getEndpoint(),
                NotificationType.MESSAGE_RECEIVED_FAILURE).createMessage();
        QueueContext queueContext = new QueueContext(messageReceiveFailureEvent.getMessageId(), messageReceiveFailureEvent.getService(), messageReceiveFailureEvent.getAction());
        sendJmsMessage(jmsMessageDTO, queueContext, JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR, JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR_ROUTING);
    }

    @Override
    public void messageSendFailed(final MessageSendFailedEvent event) {
        List<ErrorResult> errors = super.getErrorsForMessage(event.getMessageId());
        final JmsMessageDTO jmsMessageDTO = new ErrorMessageCreator(errors.get(errors.size() - 1), null, NotificationType.MESSAGE_SEND_FAILURE).createMessage();
        sendJmsMessage(jmsMessageDTO, event.getMessageId(), JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR, JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR_ROUTING);
    }

    @Override
    public void messageSendSuccess(MessageSendSuccessEvent event) {
        LOG.debug("Handling messageSendSuccess");
        final JmsMessageDTO jmsMessageDTO = new SignalMessageCreator(event.getMessageId(), NotificationType.MESSAGE_SEND_SUCCESS).createMessage();
        sendJmsMessage(jmsMessageDTO, event.getMessageId(), JMSPLUGIN_QUEUE_REPLY, JMSPLUGIN_QUEUE_REPLY_ROUTING);
    }

    protected void sendJmsMessage(JmsMessageDTO message, String messageId, String defaultQueueProperty, String routingQueuePrefixProperty) {
        String queueValue = jmsPluginQueueService.getJMSQueue(messageId, defaultQueueProperty, routingQueuePrefixProperty);

        LOG.info("Sending message [{}] to queue [{}]", message, queueValue);
        jmsExtService.sendMapMessageToQueue(message, queueValue, mshToBackendTemplate);
    }

    protected void sendJmsMessage(JmsMessageDTO message, QueueContext queueContext, String defaultQueueProperty, String routingQueuePrefixProperty) {
        final String queueValue = jmsPluginQueueService.getJMSQueue(queueContext, defaultQueueProperty, routingQueuePrefixProperty);

        LOG.info("Sending message with message id [{}] to queue [{}]", queueContext.getMessageId(), queueValue);
        jmsExtService.sendMapMessageToQueue(message, queueValue, mshToBackendTemplate);
    }

    @Override
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public MapMessage downloadMessage(String messageId, MapMessage target) throws MessageNotFoundException {
        LOG.debug("Downloading message [{}]", messageId);
        try {
            Submission submission = messageRetriever.downloadMessage(messageId);
            MapMessage result = getMessageRetrievalTransformer().transformFromSubmission(submission, target);

            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RETRIEVED);
            return result;
        } catch (Exception ex) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RETRIEVE_FAILED, ex);
            throw ex;
        }
    }

    private class DownloadMessageCreator implements MessageCreator {
        private String messageId;

        public DownloadMessageCreator(final String messageId) {
            this.messageId = messageId;
        }

        @Override
        public Message createMessage(final Session session) throws JMSException {
            final MapMessage mapMessage = session.createMapMessage();
            try {
                downloadMessage(messageId, mapMessage);
            } catch (final MessageNotFoundException e) {
                throw new DefaultJmsPluginException("Unable to create push message", e);
            }
            mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_INCOMING);
            final DomainDTO currentDomain = domainContextExtService.getCurrentDomain();
            mapMessage.setStringProperty(MessageConstants.DOMAIN, currentDomain.getCode());
            return mapMessage;
        }
    }
}
