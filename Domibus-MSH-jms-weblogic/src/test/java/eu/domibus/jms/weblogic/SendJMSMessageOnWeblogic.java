package eu.domibus.jms.weblogic;

import org.apache.commons.collections.map.HashedMap;
import weblogic.security.Security;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.Map;

public class SendJMSMessageOnWeblogic {

    private static final String PROVIDER_URL = "t3://localhost:7001";
    private static final String USER = "weblogic";
    private static final String PASSWORD = "weblogic1";
    private static final String CONNECTION_FACTORY_JNDI = "jms/ConnectionFactory";
    private static final String QUEUE = "jms/domibus.DLQ";

    public static void main(String[] args) throws Exception {
        SendJMSMessageOnWeblogic sendJMSMessageOnWeblogic = new SendJMSMessageOnWeblogic();

        Thread threadHigh = new Thread(() -> {
            try {
                sendJMSMessageOnWeblogic.sendMessages("10");
            } catch (PrivilegedActionException e) {
                e.printStackTrace();
            }
        });
        Thread threadMedium = new Thread(() -> {
            try {
                sendJMSMessageOnWeblogic.sendMessages("5");
            } catch (PrivilegedActionException e) {
                e.printStackTrace();
            }
        });
        Thread threadLow = new Thread(() -> {
            try {
                sendJMSMessageOnWeblogic.sendMessages("1");
            } catch (PrivilegedActionException e) {
                e.printStackTrace();
            }
        });
        Thread defaultThread = new Thread(() -> {
            try {
                sendJMSMessageOnWeblogic.sendMessages("2");
            } catch (PrivilegedActionException e) {
                e.printStackTrace();
            }
        });

        defaultThread.start();
        threadHigh.start();
        threadMedium.start();
        threadLow.start();

        System.out.println("Joining thread high");
        threadHigh.join();

        System.out.println("Joining thread medium");
        threadMedium.join();

        System.out.println("Joining thread low");
        threadLow.join();

        System.out.println("Joining thread default");
        defaultThread.join();

        System.out.println("Finished sending messages");

    }

    public void sendMessages(String priority) throws PrivilegedActionException {
        try {
            Security.runAs(new Subject(), new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() {
                    sendMessagesWithPriority(priority, 5);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw e;
        }
    }

    //    @Test
    public void sendMessagesWithPriority(String priority, int limit) throws RuntimeException {
        System.out.println("Sending " + priority + " priority messages: " + LocalDateTime.now());
        try {
            InitialContext ic = getInitialContext(PROVIDER_URL, USER, PASSWORD);
            Queue queue = (Queue) ic.lookup(QUEUE);
            QueueConnectionFactory cf = (QueueConnectionFactory) ic.lookup(CONNECTION_FACTORY_JNDI);
            QueueConnection qc = cf.createQueueConnection();
            QueueSession qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender qsr = qs.createSender(queue);
//            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jms/etrustexJmsMessage.xml");
//            final String message = IOUtils.toString(resourceAsStream);

            for (int index = 0; index < limit; index++) {
                Map<String, String> properties = new HashedMap();
                properties.put("messagePriority", priority);
                properties.put("DOMAIN", "default");
                properties.put("MESSAGE_ID", System.currentTimeMillis() + "");
                TextMessage textMessage = createTextMessage(qs, null, "", properties);
                qsr.send(textMessage);
            }

//            TextMessage textMessage1 = createTextMessage(qs, null, "", properties);

//            qsr.send(textMessage1);
            qsr.close();
            qs.close();
            qc.close();
            ic.close();
            System.out.println("Successfully sent messages [" + limit + "]");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("Finished sending " + priority + " priority messages: " + LocalDateTime.now());
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
//        textMessage.setText(message != null ? message : "");
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