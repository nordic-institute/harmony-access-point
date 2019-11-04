package eu.domibus.sti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.jms.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class STIAs4MessConsumer implements MessageListener{

    private static final Logger LOG = LoggerFactory.getLogger(STIAs4MessConsumer.class);

    public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";

    public final static String JMS_FACTORY = "jms/ConnectionFactory";

    public final static String QUEUE = "jms/domibus.backend.jms.outQueue";

    private QueueConnectionFactory qconFactory;

    private QueueConnection qcon;

    private QueueSession qsession;

    private QueueReceiver qreceiver;

    private Queue queue;

    private boolean quit = false;


    @PostConstruct
    public void init(){
        InitialContext ic = null;
        try {
            ic = getInitialContext();
            initJmsReceiver(ic,QUEUE);
            LOG.info("JMS Ready To Receive Messages on queue:[{}]",QUEUE);

        } catch (NamingException | JMSException e) {
            System.out.println(e);
        }
        /*STIAs4MessConsumer qr = new STIAs4MessConsumer();
        qr.init(ic, QUEUE);
        System.out.println("JMS Ready To Receive Messages (To quit, send a \"quit\" message).");
        synchronized (qr) {
            while (!qr.quit) {
                try {
                    qr.wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        qr.close();*/

    }

    public void onMessage(Message msg)     {
        try {
            if (msg instanceof MapMessage) {
                MapMessage mess = ((MapMessage) msg);
//                Enumeration enumeration = mess.getMapNames();
//                while (enumeration.hasMoreElements()) {
//                    System.out.println(enumeration.nextElement());
//                }
                System.out.println("Room On Fire: " + mess.getString("RoomId"));
                System.out.println("Last Measured Temperature: " + mess.getFloat("AverageTemperature"));
            }
        } catch (JMSException jmse) {
            System.err.println("An exception occurred: " + jmse.getMessage());
        }
    }

    public void initJmsReceiver(Context ctx, String queueName)
            throws NamingException, JMSException     {
        qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);
        qcon = qconFactory.createQueueConnection();
        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) ctx.lookup(queueName);
        qreceiver = qsession.createReceiver(queue);
        qreceiver.setMessageListener((MessageListener) this);
        qcon.start();
    }

    public void close() throws JMSException    {
        qreceiver.close();
        qsession.close();
        qcon.close();
    }

   /* public static void main(String[] args) throws Exception {
        InitialContext ic = getInitialContext();
        STIAs4MessConsumer qr = new STIAs4MessConsumer();
        qr.init(ic, QUEUE);
        System.out.println("JMS Ready To Receive Messages (To quit, send a \"quit\" message).");
        synchronized (qr) {
            while (!qr.quit) {
                try {
                    qr.wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        qr.close();
    }*/

    private static InitialContext getInitialContext()
            throws NamingException
    {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        env.put(Context.PROVIDER_URL, "t3://localhost:7002/");
        return new InitialContext(env);
    }
}
