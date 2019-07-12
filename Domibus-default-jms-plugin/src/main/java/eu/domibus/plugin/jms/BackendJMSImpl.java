package eu.domibus.plugin.jms;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class BackendJMSImpl extends AbstractBackendConnector<MapMessage, MapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSImpl.class);

    protected static final String JMSPLUGIN_QUEUE_REPLY = "jmsplugin.queue.reply";
    protected static final String JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR = "jmsplugin.queue.consumer.notification.error";
    protected static final String JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR = "jmsplugin.queue.producer.notification.error";
    protected static final String JMSPLUGIN_QUEUE_OUT = "jmsplugin.queue.out";


    //added for the stress test.
    public static final String PAYLOAD_ENDPOINT_PROPERTY_NAME = "domibus.c4.rest.payload.endpoint";
    public static final String DOMIBUS_TAXUD_REST_TIMEOUT = "domibus.taxud.rest.timeout";
    public static final String DOMIBUS_TAXUD_REST_CONNECTIONS_TOTAL = "domibus.taxud.rest.connections";
    public static final String DO_SEND_TO_C4 = "domibus.do.send.to.c4";

    private org.springframework.web.client.RestTemplate istTemplate;

    @Autowired
    protected JMSExtService jmsExtService;

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    protected DomainContextExtService domainContextExtService;

    @Autowired
    @Qualifier(value = "mshToBackendTemplate")
    private JmsOperations mshToBackendTemplate;

    private MessageRetrievalTransformer<MapMessage> messageRetrievalTransformer;
    private MessageSubmissionTransformer<MapMessage> messageSubmissionTransformer;


    @PostConstruct
    protected void init() {
        int timeout = Integer.parseInt(domibusPropertyExtService.getProperty(DOMIBUS_TAXUD_REST_TIMEOUT));
        int connections = Integer.parseInt(domibusPropertyExtService.getProperty(DOMIBUS_TAXUD_REST_CONNECTIONS_TOTAL));

        istTemplate = new RestTemplate(getClientHttpRequestFactory(timeout, connections));
        istTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory(final int timeout, final int connections) {

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setMaxConnTotal(connections)
                .setMaxConnPerRoute(connections)
                .setDefaultRequestConfig(config)
                .build();
        return new HttpComponentsClientHttpRequestFactory(client);
    }

    public BackendJMSImpl(String name) {
        super(name);
    }

    @Override
    public MessageSubmissionTransformer<MapMessage> getMessageSubmissionTransformer() {
        return this.messageSubmissionTransformer;
    }

    public void setMessageSubmissionTransformer(MessageSubmissionTransformer<MapMessage> messageSubmissionTransformer) {
        this.messageSubmissionTransformer = messageSubmissionTransformer;
    }

    @Override
    public MessageRetrievalTransformer<MapMessage> getMessageRetrievalTransformer() {
        return this.messageRetrievalTransformer;
    }

    public void setMessageRetrievalTransformer(MessageRetrievalTransformer<MapMessage> messageRetrievalTransformer) {
        this.messageRetrievalTransformer = messageRetrievalTransformer;
    }

    /**
     * This method is called when a message was received at the incoming queue
     *
     * @param map The incoming JMS Message
     */
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Transactional
    public void receiveMessage(final MapMessage map) {
        try {
            String messageID = map.getStringProperty(MESSAGE_ID);
            if (StringUtils.isNotBlank(messageID)) {
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageID);
            }
            final String jmsCorrelationID = map.getJMSCorrelationID();
            final String messageType = map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);

            LOG.info("Received message with messageId [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]");

            if (!MESSAGE_TYPE_SUBMIT.equals(messageType)) {
                String wrongMessageTypeMessage = getWrongMessageTypeErrorMessage(messageID, jmsCorrelationID, messageType);
                LOG.error(wrongMessageTypeMessage);
                sendReplyMessage(messageID, wrongMessageTypeMessage, jmsCorrelationID);
                return;
            }

            String errorMessage = null;
            try {
                //in case the messageID is not sent by the user it will be generated
                messageID = submit(map);
            } catch (final MessagingProcessingException e) {
                LOG.error("Exception occurred receiving message [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]", e);
                errorMessage = e.getMessage() + ": Error Code: " + (e.getEbms3ErrorCode() != null ? e.getEbms3ErrorCode().getErrorCodeName() : " not set");
            }

            sendReplyMessage(messageID, errorMessage, jmsCorrelationID);

            LOG.info("Submitted message with messageId [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]");
        } catch (Exception e) {
            LOG.error("Exception occurred while receiving message [" + map + "]", e);
            throw new DefaultJmsPluginException("Exception occurred while receiving message [" + map + "]", e);
        }
    }

    protected String getWrongMessageTypeErrorMessage(String messageID, String jmsCorrelationID, String messageType) {
        return MessageFormat.format("Illegal messageType [{0}] on message with JMSCorrelationId [{1}] and messageId [{2}]. Only [{3}] messages are accepted on this queue",
                messageType, jmsCorrelationID, messageID, MESSAGE_TYPE_SUBMIT);
    }

    protected void sendReplyMessage(final String messageId, final String errorMessage, final String correlationId) {
        LOG.debug("Sending reply message");
        final JmsMessageDTO jmsMessageDTO = new ReplyMessageCreator(messageId, errorMessage, correlationId).createMessage();
        sendJmsMessage(jmsMessageDTO, JMSPLUGIN_QUEUE_REPLY);
    }

    @Override
    public void deliverMessage(final String messageId) {
        LOG.info("Deliver message:[{}]", messageId);
        Submission submission;
        try {
            submission = this.messageRetriever.downloadMessage(messageId);
        } catch (MessageNotFoundException e) {
            LOG.error(e.getMessage(), e);
            return;
        }

        if (domibusPropertyExtService.getProperty(DO_SEND_TO_C4).equals("true")) {
            sendPayload(submission);
        }
        /*LOG.debug("Delivering message");
        final DomainDTO currentDomain = domainContextExtService.getCurrentDomain();
        final String queueValue = domibusPropertyExtService.getDomainProperty(currentDomain, JMSPLUGIN_QUEUE_OUT);
        if (StringUtils.isEmpty(queueValue)) {
            throw new DomibusPropertyExtException("Error getting the queue [" + JMSPLUGIN_QUEUE_OUT + "]");
        }
        LOG.info("Sending message to queue [{}]", queueValue);
        mshToBackendTemplate.send(queueValue, new DownloadMessageCreator(messageId));*/
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        LOG.debug("Handling messageReceiveFailed");
        final JmsMessageDTO jmsMessageDTO = new ErrorMessageCreator(messageReceiveFailureEvent.getErrorResult(),
                messageReceiveFailureEvent.getEndpoint(),
                NotificationType.MESSAGE_RECEIVED_FAILURE).createMessage();
        sendJmsMessage(jmsMessageDTO, JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR);
    }

    @Override
    public void messageSendFailed(final String messageId) {
        List<ErrorResult> errors = super.getErrorsForMessage(messageId);
        final JmsMessageDTO jmsMessageDTO = new ErrorMessageCreator(errors.get(errors.size() - 1), null, NotificationType.MESSAGE_SEND_FAILURE).createMessage();
        sendJmsMessage(jmsMessageDTO, JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR);
    }

    @Override
    public void messageSendSuccess(String messageId) {
        LOG.debug("Handling messageSendSuccess");
        final JmsMessageDTO jmsMessageDTO = new SignalMessageCreator(messageId, NotificationType.MESSAGE_SEND_SUCCESS).createMessage();
        sendJmsMessage(jmsMessageDTO, JMSPLUGIN_QUEUE_REPLY);
    }

    protected void sendJmsMessage(JmsMessageDTO message, String queueProperty) {
        final DomainDTO currentDomain = domainContextExtService.getCurrentDomain();
        final String queueValue = domibusPropertyExtService.getDomainProperty(currentDomain, queueProperty);
        if (StringUtils.isEmpty(queueValue)) {
            throw new DomibusPropertyExtException("Error getting the queue [" + queueProperty + "]");
        }
        LOG.info("Sending message to queue [{}]", queueValue);
        jmsExtService.sendMapMessageToQueue(message, queueValue);
    }

    @Override
    public MapMessage downloadMessage(String messageId, MapMessage target) throws MessageNotFoundException {
        LOG.debug("Downloading message [{}]", messageId);
        try {
            MapMessage result = this.getMessageRetrievalTransformer().transformFromSubmission(this.messageRetriever.downloadMessage(messageId), target);

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

    private void sendPayload(Submission submission) {
        JsonSubmission jsonSubmission = new JsonSubmission();
        jsonSubmission.setAction(submission.getAction());
        jsonSubmission.setAgreementRef(submission.getAgreementRef());
        jsonSubmission.setAgreementRefType(submission.getAgreementRefType());
        jsonSubmission.setConversationId(submission.getConversationId());
        jsonSubmission.setMessageId(submission.getMessageId());
        jsonSubmission.setRefToMessageId(submission.getRefToMessageId());
        jsonSubmission.setService(submission.getService());
        jsonSubmission.setServiceType(submission.getServiceType());
        Set<Submission.Party> toParties = submission.getToParties();
        jsonSubmission.setFromRole(submission.getFromRole());
        jsonSubmission.setToRole(submission.getToRole());

        for (Submission.Party toParty : toParties) {
            jsonSubmission.addToParty(toParty.getPartyId(), toParty.getPartyIdType());
        }

        Set<Submission.Party> fromParties = submission.getFromParties();
        for (Submission.Party fromParty : fromParties) {
            jsonSubmission.addFromParty(fromParty.getPartyId(), fromParty.getPartyIdType());
        }
        Collection<Submission.TypedProperty> messageProperties = submission.getMessageProperties();
        for (Submission.TypedProperty messageProperty : messageProperties) {
            jsonSubmission.getMessageProperties().add(new JsonSubmission.TypedProperty(messageProperty.getKey(), messageProperty.getValue(), messageProperty.getType()));
        }

        LOG.info("Message submission:\n  [{}]", jsonSubmission);
        byte[] bytes = buildMultiPartRequestFromPayload(submission.getPayloads());
        jsonSubmission.setPayload(bytes);

        String payloadEndPointUrl = domibusPropertyExtService.getProperty(PAYLOAD_ENDPOINT_PROPERTY_NAME);

        LOG.trace("Sending payload to:[{}]", payloadEndPointUrl);
        istTemplate.postForLocation(payloadEndPointUrl, jsonSubmission);
    }

    private byte[] buildMultiPartRequestFromPayload(Set<Submission.Payload> payloads) {
        for (Submission.Payload payload : payloads) {
            try (InputStream inputStream = payload.getPayloadDatahandler().getInputStream();) {

                int available = inputStream.available();
                if (available == 0) {
                    LOG.warn("Payload skipped because it is empty");
                    return new byte[]{0};
                }
                byte[] payloadContent = new byte[available];
                inputStream.read(payloadContent);
                return Base64.encodeBase64(payloadContent);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return new byte[]{0};
    }
}
