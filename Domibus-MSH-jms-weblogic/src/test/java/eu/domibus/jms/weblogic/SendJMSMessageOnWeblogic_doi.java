package eu.domibus.jms.weblogic;

import org.apache.commons.io.IOUtils;
import weblogic.security.Security;

import javax.jms.*;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import java.io.InputStream;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.Map;

public class SendJMSMessageOnWeblogic_doi {

    private static final String PROVIDER_URL = "t3://localhost:7001";
    private static final String USER = "weblogic";
    private static final String PASSWORD = "weblogic1";
    private static final String CONNECTION_FACTORY_JNDI = "jms/ConnectionFactory";
    //private static final String FOREIGN_CONNECTION_FACTORY_JNDI = "jms/ForeignConnectionFactory";
    private static final String QUEUE = "jms/domibus.backend.jms.inQueue";
//    private static final String REMOTE_QUEUE = "jms/domibus.backend.jms.inQueue";

    public static void main(String[] args) throws Exception {
        try {
            Security.runAs(new Subject(), new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() {
                    new SendJMSMessageOnWeblogic_doi().run();
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw e;
        }


    }

    public void run() throws RuntimeException {
        try {
            InitialContext ic = getInitialContext(PROVIDER_URL, USER, PASSWORD);
            Queue queue = (Queue) ic.lookup(QUEUE);
            QueueConnectionFactory cf = (QueueConnectionFactory) ic.lookup(CONNECTION_FACTORY_JNDI);
            QueueConnection qc = cf.createQueueConnection();
            QueueSession qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender qsr = qs.createSender(queue);
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jms/etrustexJmsMessage.xml");
            final String message = IOUtils.toString(resourceAsStream);

            // Message testMessage = createTextMessage(qs, message, "", null);
            Message testMessage = createTestMessage(qs, message);
            qsr.send(testMessage);
            ic.close();
            System.out.println("Successfully sent message " + testMessage.getJMSMessageID());


            JMXServiceURL serviceURL = new JMXServiceURL("t3", "localhost", Integer.parseInt("7001"), JMXHelper.REMOTE_DOMAIN_RUNTIME_MBEANSERVER_JNDI);
            // Build security context to connect with
            String username = "jmsManager";
            String password = "jms_Manager1";
            Hashtable<String, String> ctx = new Hashtable<String, String>();
            ctx.put(Context.SECURITY_PRINCIPAL, username);
            ctx.put(Context.SECURITY_CREDENTIALS, password);
            ctx.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL, ctx);
            MBeanServerConnection mbs = connector.getMBeanServerConnection();
            mbs.getMBeanCount();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private Message createTestMessage(QueueSession session, String messageContent) throws Exception {
        MapMessage messageMap = session.createMapMessage();

        messageMap.setStringProperty("messageType", "submitMessage");
        messageMap.setStringProperty("service", "bdx:noprocess");
        messageMap.setStringProperty("serviceType", "tc1");
        messageMap.setStringProperty("action", "TC1Leg1");
        messageMap.setStringProperty("fromPartyId", "domibus-blue");
        messageMap.setStringProperty("fromPartyType", "urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        messageMap.setStringProperty("toPartyId", "domibus-blue");
        messageMap.setStringProperty("toPartyType", "urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        messageMap.setStringProperty("fromRole", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        messageMap.setStringProperty("toRole", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        messageMap.setStringProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
        messageMap.setStringProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        messageMap.setStringProperty("protocol", "AS4");

        // Optional
        // messageMap.setStringProperty("conversationId", "123");
        // messageMap.setStringProperty("refToMessageId", "11");
        messageMap.setStringProperty("messageId", "jms_" + Math.random() * 10000);
        messageMap.setJMSCorrelationID("12345");

        messageMap.setStringProperty("totalNumberOfPayloads", "1");
        messageMap.setStringProperty("payload_1_mimeContentId", "cid:message");
        messageMap.setStringProperty("payload_1_mimeType", "text/xml");
        messageMap.setStringProperty("payload_1_description", "message");
        String pay1 = messageContent;
        byte[] payload = pay1.getBytes();
        messageMap.setBytes("payload_1", payload);

        return messageMap;
    }

    InitialContext getInitialContext(String providerUrl, String userName, String password) throws Exception {
        InitialContext ic = null;
        if (providerUrl != null) {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.PROVIDER_URL, providerUrl);
            env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
            if (userName != null) {
                env.put(Context.SECURITY_PRINCIPAL, userName);
            }
            if (password != null) {
                env.put(Context.SECURITY_CREDENTIALS, password);
            }
            ic = new InitialContext(env);
        } else {
            ic = new InitialContext();
        }
        return ic;
    }

    TextMessage createTextMessage(Session session, String message, String messageType, Map<String, String> messageProperties) throws JMSException {
        TextMessage textMessage = session.createTextMessage();
        textMessage.setText(message != null ? message : "");
        if (messageType != null) {
            textMessage.setJMSType(messageType);
        }
        if (messageProperties != null) {
            for (String messageProperty : messageProperties.keySet()) {
                String value = messageProperties.get(messageProperty);
                textMessage.setStringProperty(messageProperty, value);
            }
        }
        return textMessage;
    }
}