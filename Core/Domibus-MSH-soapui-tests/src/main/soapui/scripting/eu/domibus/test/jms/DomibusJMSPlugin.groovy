package eu.domibus.test.jms

import eu.domibus.test.utils.DomibusSoapUIConstants
import eu.domibus.test.utils.LogUtils
import eu.domibus.test.utils.SoapUIPropertyUtils
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.jmx.QueueViewMBean

import javax.jms.*
import javax.management.MBeanServerConnection
import javax.management.MBeanServerInvocationHandler
import javax.management.ObjectName
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL
import javax.naming.Context
import javax.naming.InitialContext

class DomibusJMSPlugin {

    static def defaultPluginAdminC2Default = "pluginAdminC2Default"
    static def defaultAdminDefaultPassword = "adminDefaultPassword"

    def allJMSProperties
    def log
    def context

    //START: JMS communication - specific properties
    def jmsSender = null
    def jmsConnectionHandler = null
    // END: JMS communication - specific properties

    /**
     * Constructor set the logger and groovy context.
     * From the groovy the parameter  allJMSDomainsProperties is parsed
     *
     * @param log - the logger
     * @param context - the soapui parameter with allJMSDomainsProperties
     */
    DomibusJMSPlugin(log, context) {
        this.log = log
        this.context = context
        this.allJMSProperties = SoapUIPropertyUtils.parseJMSDomainProperties(
                context.expand('${#Project#' + DomibusSoapUIConstants.PROP_GLOBAL_JMS_ALL_PROPERTIES + '}'), this.log)
    }

    // Class destructor
    void finalize() {
        log.debug "Domibus class not needed longer."
    }

//---------------------------------------------------------------------------------------------------------------------------------
// This methods support JMS project
//---------------------------------------------------------------------------------------------------------------------------------
    static void addPluginCredentialsIfNeeded(context, log, messageMap, String propPluginUsername = defaultPluginAdminC2Default, String propPluginPassword = defaultAdminDefaultPassword) {
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

    static InitialContext getInitialContext(String providerUrl, String userName, String password, String initialContextFactory) throws Exception {
        InitialContext ic
        if (providerUrl != null) {
            Hashtable<String, String> env = new Hashtable<String, String>()
            env.put(Context.PROVIDER_URL, providerUrl)
            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory)
            if (userName != null) {
                env.put(Context.SECURITY_PRINCIPAL, userName)
            }
            if (password != null) {
                env.put(Context.SECURITY_CREDENTIALS, password)
            }
            ic = new InitialContext(env)
        } else {
            ic = new InitialContext()
        }
        return ic
    }

    def connectUsingJMSApi(String PROVIDER_URL, String USER, String PASSWORD, String CONNECTION_FACTORY_JNDI, String QUEUE, String initialContextFactory, String applicationServerType) {
        log.info "Connect using JMS API: PROVIDER_URL:$PROVIDER_URL, USER=$USER, PASSWORD=$PASSWORD, " +
                "CONNECTION_FACTORY_JNDI=$CONNECTION_FACTORY_JNDI, QUEUE=$QUEUE, " +
                "initialContextFactory=$initialContextFactory, applicationServerType=$applicationServerType"

        MapMessage messageMap = null
        try {
            jmsConnectionHandler = getInitialContext(PROVIDER_URL, USER, PASSWORD, initialContextFactory)

            QueueConnectionFactory cf = (QueueConnectionFactory) jmsConnectionHandler.lookup(CONNECTION_FACTORY_JNDI)
            QueueConnection qc = cf.createQueueConnection(USER, PASSWORD)
            QueueSession session = (QueueSession) qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE)

            Queue queue = (Queue) jmsConnectionHandler.lookup(QUEUE)
            jmsSender = session.createSender(queue)

            messageMap = session.createMapMessage()
        } catch (Exception ex) {
            log.error "jmsConnectionHandlerInitialize    [][]  Connection to JMS queue in $applicationServerType deployment failed. " +
                    "PROVIDER_URL: $PROVIDER_URL | USER: $USER | PASSWORD: $PASSWORD | " +
                    "CONNECTION_FACTORY_JNDI: $CONNECTION_FACTORY_JNDI | QUEUE: $QUEUE"
            assert 0, "Exception occurred when trying to connect: " + ex
        }
        return messageMap
    }

    def connectToActiveMQ(String FACTORY_URL, String USER, String PASSWORD, String QUEUE) {
        MapMessage messageMap = null
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(FACTORY_URL)
            jmsConnectionHandler = (Connection) connectionFactory.createConnection(USER, PASSWORD)
            //username and password of the default JMS broker
            QueueSession session = jmsConnectionHandler.createSession(false, Session.AUTO_ACKNOWLEDGE) as QueueSession
            Destination destination = session.createQueue(QUEUE)
            jmsSender = (MessageProducer) session.createProducer(destination)
            jmsSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT)
            messageMap = session.createMapMessage()
        } catch (Exception ex) {
            log.error "jmsConnectionHandlerInitialize    [][]  Connection to JMS queue in Tomcat deployment failed. " +
                    "FACTORY_URL: $FACTORY_URL | USER: $USER | PASSWORD: $PASSWORD | " +
                    "QUEUE: $QUEUE"
            assert 0, "Exception occurred when trying to connect: " + ex
        }
        return messageMap
    }

    /**
     * Method returns activeMQ QueueViewMBean for managing the queue. Example of using the
     * QueueViewMBean is to pause, resume queue, or to manipulate messages in the queue as retrieve, remove message from the queue
     *
     * https://activemq.apache.org/maven/apidocs/org/apache/activemq/broker/jmx/QueueViewMBean.html
     *
     *
     * @param queueName - queue name as example  domibus.internal.earchive.queue
     * @param jmxHost - jmx host / ip. Examples: tomcatC2 or 172.20.0.5
     * @param jmxPort - jmx port / example: 1199
     * @return  QueueViewMBean
     */
    def getActiveMQJMXQueueBean(def queueName, def jmxHost,  def jmxPort="1199") {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+jmxHost+":"+jmxPort+"/jmxrmi");
        JMXConnector jmxc = JMXConnectorFactory.connect(url);
        MBeanServerConnection conn = jmxc.getMBeanServerConnection();
        ObjectName queue = new ObjectName("org.apache.activemq:type=Broker,brokerName=domibusActiveMQBroker,destinationType=Queue,destinationName="+queueName);
        return MBeanServerInvocationHandler.newProxyInstance(conn, queue, QueueViewMBean.class, true);
    }

    def jmsConnectionHandlerInitializeC2() {
        jmsConnectionHandlerInitialize("C2Default")
    }

    def jmsConnectionHandlerInitializeC3() {
        jmsConnectionHandlerInitialize("C3Default")
    }


    def getJMSDomainProperty(String domain, String property) {
        return SoapUIPropertyUtils.getDomainProperty(this.context, this.allJMSProperties, domain, property, this.log)
    }

    def jmsConnectionHandlerInitialize(String domain, String queue) {
        MapMessage messageMap = null

        log.info "Starting JMS message sending"
        String jmsClientType = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMS_TYPE)
        String jmsURL = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMS_URL)
        String serverUser = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMS_SRV_USERNAME)
        String serverPassword = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMS_SRV_PASSWORD)
        String jmsConnectionFactory = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMS_CF_JNDI)

        switch (jmsClientType) {
            case "weblogic":
                messageMap = connectUsingJMSApi(jmsURL, serverUser, serverPassword, jmsConnectionFactory, queue, "weblogic.jndi.WLInitialContextFactory", "Weblogic")
                break
            case "tomcat":
                log.info("JmsServer Tomcat. Reading connection details.")
                messageMap = connectToActiveMQ(jmsURL, serverUser, serverPassword, queue)
                break
            case "wildfly":
                log.info("JmsServer Wildfly. Reading connection details.")
                messageMap = connectUsingJMSApi(jmsURL, serverUser, serverPassword, jmsConnectionFactory, queue, "org.jboss.naming.remote.client.InitialContextFactory", "Wildfly")
                break

            default:
                log.error("Incorrect or not supported jms server type, jmsServer=" + jmsClientType)
                assert 0, "Properties value error, check jmsServer value."
                break
        }
        return messageMap
    }

    def manageQueue(String domain, String queue, String action) {
        log.info "manageQueue ["+action+"] for queue ["+queue+"]"
        String jmsClientType = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMS_TYPE)
        String jmxPort = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMX_PORT)
        String hostname = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_HOSTNAME)
        switch (jmsClientType) {
            case "tomcat":
                log.info("Pause queue ["+queue+"] on Tomcat.")
                manageActiveMQQueue(queue, action, hostname, jmxPort)
                break
            case "weblogic":
            case "wildfly":
                log.error("Pause is not yest supported for jmsServer=" + jmsClientType)
                assert 0, "Properties value error, check jmsServer value."
                break
            default:
                log.error("Incorrect or not supported jms server type, jmsServer=" + jmsClientType)
                assert 0, "Properties value error, check jmsServer value."
                break
        }
    }

    def getDataForQueue(String domain, String queue, String dataType) {
        log.info "getDataForQueue ["+dataType+"] for queue ["+queue+"]"


        String jmsClientType = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMS_TYPE)
        String jmxPort = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMX_PORT)
        String hostname = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_HOSTNAME)
        switch (jmsClientType) {
            case "tomcat":
                log.info("Pause queue ["+queue+"] on Tomcat.")
                return getDataActiveMQQueue(queue, dataType, hostname, jmxPort)
            case "weblogic":
            case "wildfly":
                log.error("Pause is not yest supported for jmsServer=" + jmsClientType)
                assert 0, "Properties value error, check jmsServer value."
                break
            default:
                log.error("Incorrect or not supported jms server type, jmsServer=" + jmsClientType)
                assert 0, "Properties value error, check jmsServer value."
                break
        }
        return -1
    }

    def manageActiveMQQueue(String queue, String action,hostname, jmxPort){
        QueueViewMBean queueBean = getActiveMQJMXQueueBean(queue, hostname, jmxPort)
        switch (action) {
            case JMSConstants.QUEUE_ACTION_PAUSE:
                queueBean.pause()
                break;
            case JMSConstants.QUEUE_ACTION_RESUME:
                queueBean.resume()
                break;
            case JMSConstants.QUEUE_ACTION_PURGE:
                queueBean.purge()
                break;
            default:
                log.error("Action: ["+action+"] is not not supported on ActiveMQ!")
                assert 0, "Action ["+action+"]  is not not supported on ActiveMQ!."
        }
    }

    def getDataActiveMQQueue(String queue, String dataType,hostname, jmxPort){
        QueueViewMBean queueBean = getActiveMQJMXQueueBean(queue, hostname, jmxPort)
        switch (dataType) {
            case JMSConstants.QUEUE_DATA_ENQUEUE_COUNT:
                return queueBean.enqueueCount
            case JMSConstants.QUEUE_DATA_DISPATCH_COUNT:
                return queueBean.dispatchCount
            case JMSConstants.QUEUE_DATA_DEQUEUE_COUNT:
                return queueBean.dequeueCount
            case JMSConstants.QUEUE_DATA_QUEUE_COUNT:
                return queueBean.queueSize

            default:
                log.error("Data type: ["+dataType+"] is not not supported on ActiveMQ!")
                assert 0, "Data type ["+dataType+"]  is not not supported on ActiveMQ!."
        }
    }


    def jmsConnectionHandlerInitialize(String domain) {
        String queue = getJMSDomainProperty(domain, DomibusSoapUIConstants.JSON_JMS_QUEUE)
        return jmsConnectionHandlerInitialize(domain, queue)
    }

    def sendMessageAndClean(messageMap) {
        log.info "sending message"
        try {
            jmsSender.send(messageMap)
        } catch (Exception ex) {
            log.error "sendMessageAndClean    [][]  Sending message"
            assert 0, "Exception occurred when trying to send: " + ex
        }
        log.info "message sent"

        log.info "cleaning up"
        try {
            jmsConnectionHandler.close()
        } catch (Exception ex) {
            log.error "sendMessageAndClean    [][]  Closing connection to JMS queue"
            assert 0, "Exception occurred when trying to close the connection: " + ex
        }
        log.info "cleaned up"
    }

}

