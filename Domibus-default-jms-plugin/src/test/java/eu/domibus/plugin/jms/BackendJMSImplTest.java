package eu.domibus.plugin.jms;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.ext.services.MessageExtService;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.jms.MapMessage;

import static eu.domibus.plugin.jms.JMSMessageConstants.JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR;
import static eu.domibus.plugin.jms.JMSMessageConstants.JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR_ROUTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class BackendJMSImplTest {

    @Injectable
    protected MessageRetriever messageRetriever;

    @Injectable
    protected MessageSubmitter messageSubmitter;

    @Injectable
    protected MessagePuller messagePuller;

    @Injectable
    private JmsOperations replyJmsTemplate;

    @Injectable
    private JmsOperations mshToBackendTemplate;

    @Injectable
    private JmsOperations errorNotifyConsumerTemplate;

    @Injectable
    private JmsOperations errorNotifyProducerTemplate;

    @Injectable
    protected JMSExtService jmsService;

    @Injectable
    protected DomibusPropertyExtService domibusPropertyService;

    @Injectable
    protected DomainContextExtService domainContextService;

    @Injectable
    private MessageExtService messageExtService;

    @Injectable
    protected BackendJMSQueueService backendJMSQueueService;

    @Injectable
    protected JMSMessageTransformer jmsMessageTransformer;

    @Injectable
    String name = "myjmsplugin";

    @Tested
    BackendJMSImpl backendJMS;

    @Test
    public void testReceiveMessage(@Injectable final MapMessage map,
                                   @Injectable QueueContext queueContext) throws Exception {
        final String messageId = "1";
        final String jmsCorrelationId = "2";
        final String messageTypeSubmit = JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

        new Expectations(backendJMS) {{
            map.getStringProperty(JMSMessageConstants.MESSAGE_ID);
            result = messageId;

            messageExtService.cleanMessageIdentifier(messageId);
            result = messageId;

            map.getJMSCorrelationID();
            result = jmsCorrelationId;

            map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);
            result = messageTypeSubmit;

            jmsMessageTransformer.getQueueContext(messageId, map);
            result = queueContext;

            backendJMS.submit(withAny(new ActiveMQMapMessage()));
            result = messageId;

            backendJMS.sendReplyMessage(queueContext, anyString, jmsCorrelationId);
        }};

        backendJMS.receiveMessage(map);

        new Verifications() {{
            backendJMS.submit(map);

            String capturedMessageId = null;
            String capturedJmsCorrelationId = null;
            backendJMS.sendReplyMessage(queueContext, null, capturedJmsCorrelationId = withCapture());

            assertEquals(capturedJmsCorrelationId, jmsCorrelationId);
        }};
    }

    @Test
    public void testReceiveMessage_MessageId_WithEmptySpaces(@Injectable final MapMessage map,
                                                             @Injectable QueueContext queueContext) throws Exception {
        final String messageId = " test123 ";
        final String messageIdTrimmed = "test123";
        final String jmsCorrelationId = "2";
        final String messageTypeSubmit = JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

        new Expectations(backendJMS) {{
            map.getStringProperty(JMSMessageConstants.MESSAGE_ID);
            result = messageId;

            messageExtService.cleanMessageIdentifier(messageId);
            result = messageIdTrimmed;

            map.getJMSCorrelationID();
            result = jmsCorrelationId;

            map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);
            result = messageTypeSubmit;

            backendJMS.submit(withAny(new ActiveMQMapMessage()));
            result = messageIdTrimmed;

            backendJMS.sendReplyMessage((QueueContext) any, anyString, jmsCorrelationId);
        }};

        backendJMS.receiveMessage(map);

        new Verifications() {{
            backendJMS.submit(map);

            String capturedJmsCorrelationId;
            String capturedErrorMessage;
            backendJMS.sendReplyMessage(queueContext, capturedErrorMessage = withCapture(), capturedJmsCorrelationId = withCapture());

            assertEquals(capturedJmsCorrelationId, jmsCorrelationId);
            assertNull(capturedErrorMessage);
        }};
    }

    @Test
    public void testReceiveMessageWithUnacceptedMessage(@Injectable final MapMessage map,
                                                        @Injectable QueueContext queueContext) throws Exception {
        final String messageId = "1";
        final String jmsCorrelationId = "2";
        final String unacceptedMessageType = "unacceptedMessageType";

        new Expectations(backendJMS) {{
            map.getStringProperty(JMSMessageConstants.MESSAGE_ID);
            result = messageId;

            messageExtService.cleanMessageIdentifier(messageId);
            result = messageId;

            map.getJMSCorrelationID();
            result = jmsCorrelationId;

            map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);
            result = unacceptedMessageType;

            jmsMessageTransformer.getQueueContext(messageId, map);
            result = queueContext;

            backendJMS.sendReplyMessage((QueueContext) any, anyString, jmsCorrelationId);
        }};

        backendJMS.receiveMessage(map);

        new Verifications() {{
            QueueContext queueContext = null;
            String capturedJmsCorrelationId = null;
            String capturedErrorMessage = null;
            backendJMS.sendReplyMessage(queueContext, capturedErrorMessage = withCapture(), capturedJmsCorrelationId = withCapture());

            assertEquals(jmsCorrelationId, capturedJmsCorrelationId);
        }};
    }

    @Test
    public void testMessageReceiveFailed(@Injectable MessageReceiveFailureEvent messageReceiveFailureEvent,
                                         @Injectable ErrorResult errorResult) throws Exception {
        final String myEndpoint = "myEndpoint";
        final String messageId = "1";
        final ErrorCode errorCode = ErrorCode.EBMS_0010;
        final String errorDetail = "myError";

        new Expectations(backendJMS) {{
            messageReceiveFailureEvent.getErrorResult();
            result = errorResult;

            errorResult.getErrorCode();
            result = errorCode;

            errorResult.getErrorDetail();
            result = errorDetail;

            messageReceiveFailureEvent.getEndpoint();
            result = myEndpoint;

            messageReceiveFailureEvent.getMessageId();
            result = messageId;
        }};

        backendJMS.messageReceiveFailed(messageReceiveFailureEvent);

        new Verifications() {{
            JmsMessageDTO jmsMessageDTO = null;
            QueueContext queueContext = null;
            backendJMS.sendJmsMessage(jmsMessageDTO = withCapture(), queueContext = withCapture(), JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR, JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR_ROUTING);

            Assert.assertEquals(errorCode.getErrorCodeName(), jmsMessageDTO.getStringProperty(JMSMessageConstants.ERROR_CODE));
            Assert.assertEquals(errorDetail, jmsMessageDTO.getStringProperty(JMSMessageConstants.ERROR_DETAIL));
            Assert.assertEquals(messageId, queueContext.getMessageId());
        }};
    }
}
