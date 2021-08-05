
package eu.domibus.plugin.jms;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.DomibusJMSConstants;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.test.PModeUtil;
import eu.domibus.test.common.JMSMessageUtil;
import eu.domibus.test.common.SubmissionUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jms.ConnectionFactory;
import javax.jms.MapMessage;
import javax.jms.Message;
import java.io.IOException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;


/**
 * This class implements the test cases Receive Deliver Message-01
 *
 * @author martifp
 */
public class ReceiveDeliverMessageJMSIT extends AbstractBackendJMSIT {


    @Autowired
    private JMSPluginImpl jmsPluginImpl;

    @Autowired
    @Qualifier(DomibusJMSConstants.DOMIBUS_JMS_CONNECTION_FACTORY)
    private ConnectionFactory jmsConnectionFactory;


    @Autowired
    MessagingService messagingService;

    @Autowired
    UserMessageLogDefaultService userMessageLogService;

    @Autowired
    protected SubmissionUtil submissionUtil;

    @Autowired
    DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    JMSMessageTransformer jmsMessageTransformer;

    @Autowired
    JMSMessageUtil jmsMessageUtil;

    @Autowired
    JMSManager jmsManager;

    @Autowired
    PModeUtil pModeUtil;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @Before
    public void before() throws IOException, XmlProcessingException {
        pModeUtil.uploadPmode(wireMockRule.port());
    }

    /**
     * It tests the message reception by Domibus through the JMS channel.
     * It also checks that the messages are actually pushed on the right queues (dispatch and reply).
     * The message ID is cleaned to simulate the submission of the a new message.
     *
     * @throws Exception
     */
    @Test
    public void testReceiveMessage() throws Exception {
        final MapMessage mapMessage = createJMSMessageForReceive();

        pModeUtil.uploadPmode();

        System.out.println("MapMessage: " + mapMessage);
        String messageId = UUID.randomUUID().toString();
        mapMessage.setStringProperty(MESSAGE_ID, messageId); // Cleaning the message ID since it is supposed to submit a new message.
        mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_SUBMIT);
        // The downloaded MapMessage is used as input parameter for the real Test case here!
        jmsPluginImpl.receiveMessage(mapMessage);

        // Verifies that the message is really in the queue
        Message message = popMessageFromQueue();
        Assert.assertEquals(message.getStringProperty(JMSMessageConstants.MESSAGE_ID), messageId);
        Assert.assertNull(message.getStringProperty("ErrorMessage"));

    }

    private Message popMessageFromQueue() throws Exception {
        javax.jms.Connection connection = jmsConnectionFactory.createConnection("domibus", "changeit");
        connection.start();
        Message message = jmsMessageUtil.popQueueMessageWithTimeout(connection, JMS_BACKEND_REPLY_QUEUE_NAME, 2000);
        connection.close();
        return message;
    }

    protected MapMessage createJMSMessageForReceive() throws Exception {
        Submission submission = submissionUtil.createSubmission();
        final MapMessage mapMessage = new ActiveMQMapMessage();
        return jmsMessageTransformer.transformFromSubmission(submission, mapMessage);
    }

    /*
     *
     * Similar test to the previous one but this does not change the Message ID so that an exception is raised and handled with an JMS error message.
     * It tests that the message is actually into the REPLY queue.
     *
     * @throws Exception
     */
    @Test
    public void testDuplicateMessage() throws Exception {
        final MapMessage mapMessage = createJMSMessageForReceive();

        final String messageId = mapMessage.getStringProperty(MESSAGE_ID);

        System.out.println("MapMessage: " + mapMessage);
        mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_SUBMIT);
        // The downloaded MapMessage is used as input parameter for the real Test case here!
        jmsPluginImpl.receiveMessage(mapMessage);
        popMessageFromQueue();

        jmsPluginImpl.receiveMessage(mapMessage);
        // Verifies that the message is really in the queue
        Message message = popMessageFromQueue();
        Assert.assertEquals(message.getStringProperty(JMSMessageConstants.MESSAGE_ID), messageId);
        Assert.assertNotNull(message.getStringProperty("ErrorMessage"));
    }


}
