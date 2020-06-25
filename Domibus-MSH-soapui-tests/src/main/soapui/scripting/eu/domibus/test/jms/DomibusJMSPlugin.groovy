package eu.domibus.test.jms

import eu.domibus.test.utils.LogUtils
import eu.domibus.test.utils.PropertyParser
import eu.domibus.test.utils.DomibusSoapUIConstants


import org.apache.activemq.ActiveMQConnectionFactory

import javax.jms.Connection
import javax.jms.DeliveryMode
import javax.jms.Destination
import javax.jms.MapMessage
import javax.jms.MessageProducer
import javax.jms.QueueConnection
import javax.jms.QueueConnectionFactory
import javax.jms.QueueSession
import javax.jms.Session
import javax.naming.Context
import javax.naming.InitialContext


class DomibusJMSPlugin {

    static def DEFAULT_LOG_LEVEL = 0;
    static def defaultPluginAdminC2Default = "pluginAdminC2Default"
    static def defaultAdminDefaultPassword = "adminDefaultPassword"

    def allJMSProperties;
    def log;
    def context;

    //START: JMS communication - specific properties
    def jmsSender = null
    def jmsConnectionHandler = null
    // END: JMS communication - specific properties

    DomibusJMSPlugin(log, context) {
        this.log = log
        this.context = context

        this.allJMSProperties = PropertyParser.parseJMSDomainProperties(
                context.expand('${#Project#'+DomibusSoapUIConstants.PROP_GLOBAL_JMS_ALL_PROPERTIES+'}'), this.log)
    }

    // Class destructor
    void finalize() {
        log.debug "Domibus class not needed longer."
    }

//---------------------------------------------------------------------------------------------------------------------------------
// This methods support JMS project
//---------------------------------------------------------------------------------------------------------------------------------
    static def void addPluginCredentialsIfNeeded(context, log, messageMap, String propPluginUsername = defaultPluginAdminC2Default, String propPluginPassword = defaultAdminDefaultPassword) {
        LogUtils.debugLog("  ====  Calling \"addPluginCredentialsIfNeeded\".", log)
        def unsecureLoginAllowed = context.expand("\${#Project#unsecureLoginAllowed}").toLowerCase()
        if (unsecureLoginAllowed == "false" || unsecureLoginAllowed == 0) {
            LogUtils.debugLog("  addPluginCredentialsIfNeeded  [][]  passed values are propPluginUsername=${propPluginUsername} propPluginPasswor=${propPluginPassword} ", log)
            def u = context.expand("\${#Project#${propPluginUsername}}")
            def p = context.expand("\${#Project#${propPluginPassword}}")
            LogUtils.debugLog("  addPluginCredentialsIfNeeded  [][]  Username|Password=" + u + "|" + p, log)
            messageMap.setStringProperty("username", u)
            messageMap.setStringProperty("password", p)
        }
    }

    def InitialContext getInitialContext(String providerUrl, String userName, String password, String initialContextFactory) throws Exception {
        InitialContext ic = null;
        if (providerUrl != null) {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.PROVIDER_URL, providerUrl);
            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
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

    def connectUsingJMSApi(String PROVIDER_URL, String USER, String PASSWORD, String CONNECTION_FACTORY_JNDI, String QUEUE,  String initialContextFactory) {

        def MapMessage messageMap = null
        try {
            jmsConnectionHandler = getInitialContext(PROVIDER_URL, USER, PASSWORD, initialContextFactory);

            QueueConnectionFactory cf = jmsConnectionHandler.lookup(CONNECTION_FACTORY_JNDI);
            QueueConnection qc = cf.createQueueConnection(USER, PASSWORD);
            QueueSession session = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = jmsConnectionHandler.lookup(QUEUE);
            jmsSender = session.createSender(queue);

            messageMap = session.createMapMessage();
        } catch (Exception ex) {
            log.error "jmsConnectionHandlerInitialize    [][]  Connection to JMS queue in Weblogic deployment failed. " +
                    "PROVIDER_URL: $PROVIDER_URL | USER: $USER | PASSWORD: $PASSWORD | " +
                    "CONNECTION_FACTORY_JNDI: $CONNECTION_FACTORY_JNDI | QUEUE: $QUEUE"
            assert 0, "Exception occurred when trying to connect: " + ex;
        }
        return messageMap
    }

    def connectUsingJMSArtemis(String PROVIDER_URL, String USER, String PASSWORD, String CONNECTION_FACTORY_JNDI, String QUEUE,  String initialContextFactory) {

        def MapMessage messageMap = null
        try {
            jmsConnectionHandler = getInitialContext(PROVIDER_URL, USER, PASSWORD, initialContextFactory);
            log.info "init context created " +jmsConnectionHandler
            QueueConnectionFactory cf = jmsConnectionHandler.lookup(CONNECTION_FACTORY_JNDI);
            log.info "got connection factory  " +cf + " name " + CONNECTION_FACTORY_JNDI
            QueueConnection qc = cf.createQueueConnection(USER, PASSWORD);
            log.info "got connection   " +qc + " for user  " + USER
            QueueSession session = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            log.info "got session   " +session + " for user  " + USER
            log.info "get queue   "  + QUEUE
            Queue queue = jmsConnectionHandler.lookup(QUEUE);
            log.info "create sender "  + QUEUE
            jmsSender = session.createSender(queue);

            messageMap = session.createMapMessage();
        } catch (Exception ex) {
            log.error "jmsConnectionHandlerInitialize    [][]  Connection to JMS queue. " +
                    "PROVIDER_URL: $PROVIDER_URL | USER: $USER | PASSWORD: $PASSWORD | " +
                    "CONNECTION_FACTORY_JNDI: $CONNECTION_FACTORY_JNDI | QUEUE: $QUEUE"
            assert 0, "Exception occurred when trying to connect: " + ex;
        }
        return messageMap
    }



    def connectToActiveMQ(String FACTORY_URL, String USER, String PASSWORD, String QUEUE) {
        def MapMessage messageMap = null
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(FACTORY_URL)
            jmsConnectionHandler = (Connection) connectionFactory.createConnection(USER, PASSWORD)
            //username and password of the default JMS broker
            QueueSession session = jmsConnectionHandler.createSession(false, Session.AUTO_ACKNOWLEDGE)
            Destination destination = session.createQueue(QUEUE)
            jmsSender = (MessageProducer) session.createProducer(destination)
            jmsSender.setDeliveryMode(DeliveryMode  .NON_PERSISTENT)
            messageMap = session.createMapMessage()
        } catch (Exception ex) {
            log.error "jmsConnectionHandlerInitialize    [][]  Connection to JMS queue in Tomcat deployment failed. " +
                    "FACTORY_URL: $FACTORY_URL | USER: $USER | PASSWORD: $PASSWORD | " +
                    "QUEUE: $QUEUE"
            assert 0, "Exception occurred when trying to connect: " + ex;
        }
        return messageMap
    }

    def jmsConnectionHandlerInitializeC2() {
        jmsConnectionHandlerInitialize("C2Default")
    }

    def jmsConnectionHandlerInitializeC3() {
        jmsConnectionHandlerInitialize("C3Default")
    }

    def jmsConnectionHandlerInitialize(String domain) {
        def MapMessage messageMap = null

        log.info "Starting JMS message sending"

        String jmsClientType = this.allJMSProperties[domain][DomibusSoapUIConstants.JSON_JMS_TYPE];
        String jmsURL = this.allJMSProperties[domain][DomibusSoapUIConstants.JSON_JMS_URL];
        String serverUser = this.allJMSProperties[domain][DomibusSoapUIConstants.JSON_JMS_SRV_USERNAME];
        String serverPasswd =this.allJMSProperties[domain][DomibusSoapUIConstants.JSON_JMS_SRV_PASSWORD];
        String jmsConnectionFactory = this.allJMSProperties[domain][DomibusSoapUIConstants.JSON_JMS_CF_JNDI];
        String queue = this.allJMSProperties[domain][DomibusSoapUIConstants.JSON_JMS_QUEUE];

        switch (jmsClientType) {
            case "weblogic":
                 messageMap = connectUsingJMSApi(jmsURL, serverUser, serverPasswd, jmsConnectionFactory, queue,"org.jboss.naming.remote.client.InitialContextFactory")
                break

            case "tomcat":
                log.info("JmsServer Tomcat. Reading connection details.")
                messageMap = connectToActiveMQ(jmsURL, serverUser, serverPasswd, queue)
                break
            case "wildfly":
                log.info("JmsServer Tomcat. Reading connection details.")
                messageMap = connectUsingJMSArtemis(jmsURL, serverUser, serverPasswd, jmsConnectionFactory, queue,"org.wildfly.naming.client.WildFlyInitialContextFactory")
                break

            default:
                log.error("Incorrect or not supported jms server type, jmsServer="+jmsClientType);
                assert 0, "Properties value error, check jmsServer value."
                break

        }
        return messageMap
    }

    def sendMessageAndClean(messageMap) {

        log.info "sending message"
        try {
            jmsSender.send(messageMap);
            jmsConnectionHandler.close();
        } catch (Exception ex) {
            log.error "sendMessageAndClean    [][]  Sending and closing connection  to JMS queue"
            assert 0, "Exception occurred when trying to connect: " + ex;
        }
        log.info "message sent"
    }

}

