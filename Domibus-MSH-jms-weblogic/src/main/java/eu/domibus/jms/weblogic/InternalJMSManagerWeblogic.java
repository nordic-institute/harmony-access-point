package eu.domibus.jms.weblogic;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.jms.spi.InternalJMSDestination;
import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.jms.spi.InternalJMSManager;
import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import eu.domibus.jms.spi.helper.JmsMessageCreator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import weblogic.messaging.runtime.MessageInfo;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.Topic;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * @author Cosmin Baciu, Catalin Enache
 * @since 3.2
 */
@Component
public class InternalJMSManagerWeblogic implements InternalJMSManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(InternalJMSManagerWeblogic.class);

    private static final String PROPERTY_OBJECT_NAME = "ObjectName";

    protected static final String PROPERTY_JNDI_NAME = "Jndi";

    private static final String JMS_TYPE = "JMSType";

    private static final String FAILED_TO_BUILD_JMS_DEST_MAP = "Failed to build JMS destination map";

    /**
     * JMX attributes
     */
    private static final String ATTR_SERVER_RUNTIMES = "ServerRuntimes";
    private static final String ATTR_JMS_RUNTIME = "JMSRuntime";
    private static final String ATTR_JMS_SERVERS = "JMSServers";
    private static final String ATTR_DESTINATIONS = "Destinations";
    private static final String ATTR_NAME = "Name";
    private static final String ATTR_ADMIN_SERVER = "AdminServer";
    private static final String ATTR_JNDI_NAME = "JNDIName";
    private static final String ATTR_DOMAIN_CONFIGURATION = "DomainConfiguration";
    private static final String ATTR_JMS_SYSTEM_RESOURCES = "JMSSystemResources";
    private static final String ATTR_JMS_RESOURCE = "JMSResource";
    private static final String ATTR_QUEUES = "Queues";
    private static final String ATTR_DISTRIBUTED_QUEUES = "DistributedQueues";
    private static final String ATTR_UNIFORM_DISTRIBUTED_QUEUES = "UniformDistributedQueues";

    private static final String LOG_INTERNAL_DESTINATION_FOUND_FOR_SOURCE = "Internal destination found for source [{}]";

    /**
     * queues map including the distributed ones
     */
    protected Map<String, ObjectName> queueMap;

    protected volatile Map<String, String> jndiMap = new HashMap<>();

    protected final Object lock = new Object();

    /**
     * managed servers names
     */
    protected List<String> managedServerNames;

    @Autowired
    private JMXHelper jmxHelper;

    @Autowired
    private JMXTemplate jmxTemplate;

    @Resource(name = "jmsSender")
    private JmsOperations jmsSender;

    @Autowired
    private JMSDestinationHelper jmsDestinationHelper;

    @Autowired
    private JMSSelectorUtil jmsSelectorUtil;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private CommandService commandService;

    @Autowired
    private ServerInfoService serverInfoService;

    @Autowired
    private JmsDestinationCache jmsDestinationCache;

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, InternalJMSDestination> findDestinationsGroupedByFQName() {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public Map<String, InternalJMSDestination> execute(MBeanServerConnection mbsc) {
                        return findDestinationsGroupedByFQName(mbsc);
                    }
                }
        );
    }

    protected Map<String, InternalJMSDestination> findDestinationsGroupedByFQName(MBeanServerConnection mbsc) {
        Map<String, InternalJMSDestination> destinationsMap = new TreeMap<>();
        try {
            for (Map.Entry<String, ObjectName> mapEntry : getJmsDestinations(mbsc).entrySet()) {
                final String destinationFQName = mapEntry.getKey();
                final ObjectName jmsDestination = mapEntry.getValue();

                InternalJMSDestination destination = createInternalJMSDestination(mbsc, jmsDestination, destinationFQName);
                if (destination == null) continue; //topic
                destinationsMap.put(removeJmsModuleAndServer(destinationFQName), destination);
            }
            return destinationsMap;
        } catch (Exception e) {
            throw new InternalJMSException(FAILED_TO_BUILD_JMS_DEST_MAP, e);
        }
    }

    /**
     * It returns the JMS Destinations (Queues, Topics, etc) for all servers
     * Key of the map is destinationFQName
     *
     * @param mbsc MBeanServerConnection
     * @return JMSDestinations as {@code ObjectName}
     */
    protected Map<String, ObjectName> getJmsDestinations(MBeanServerConnection mbsc) {
        Map<String, ObjectName> jmsDestinationsMap = new LinkedHashMap<>(); //keep order of insertion
        try {
            ObjectName drs = jmxHelper.getDomainRuntimeService();
            ObjectName[] servers = (ObjectName[]) mbsc.getAttribute(drs, ATTR_SERVER_RUNTIMES);
            for (ObjectName server : servers) {
                LOG.debug("Server [{}]", server);
                ObjectName jmsRuntime = (ObjectName) mbsc.getAttribute(server, ATTR_JMS_RUNTIME);
                ObjectName[] jmsServers = (ObjectName[]) mbsc.getAttribute(jmsRuntime, ATTR_JMS_SERVERS);
                for (ObjectName jmsServer : jmsServers) {
                    LOG.debug("JMS Server [{}]", jmsServer);
                    ObjectName[] objectNames = (ObjectName[]) mbsc.getAttribute(jmsServer, ATTR_DESTINATIONS);
                    for (ObjectName objectName : objectNames) {
                        String destinationFQName = (String) mbsc.getAttribute(objectName, ATTR_NAME);
                        jmsDestinationsMap.put(destinationFQName, objectName);
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalJMSException(FAILED_TO_BUILD_JMS_DEST_MAP, e);
        }
        return jmsDestinationsMap;
    }

    /**
     * It builds {@code InternalJMSDestination} based on JMX attributes
     *
     * @param mbsc              JMX MBeanServerConnection
     * @param jmsDestination    JMX ObjectName corresponding to JMS Destinations
     * @param destinationFQName JMS Destination fully qualified name
     * @return {@code InternalJMSDestination} internal JMSDestination object
     */
    protected InternalJMSDestination createInternalJMSDestination(MBeanServerConnection mbsc, ObjectName jmsDestination, final String destinationFQName) {
        LOG.debug("JMS Destination [{}]", jmsDestination);

        try {
            // The name must be the queueName in a single server or serverName@queueName in a cluster.
            String destName = getShortDestName(destinationFQName);

            //getting the destination from queues map
            ObjectName configQueue = getQueueMap(mbsc).get(getOnlyDestName(destName));

            // We do not manage Topic at the moment
            if (configQueue == null) return null;

            InternalJMSDestination destination = new InternalJMSDestination();
            destination.setName(destName);
            destination.setFullyQualifiedName(removeJmsModule(destinationFQName));
            destination.setType(QUEUE);
            destination.setProperty(PROPERTY_OBJECT_NAME, jmsDestination);
            String configQueueJndiName = (String) mbsc.getAttribute(configQueue, ATTR_JNDI_NAME);
            destination.setProperty(PROPERTY_JNDI_NAME, configQueueJndiName);
            destination.setInternal(jmsDestinationHelper.isInternal(configQueueJndiName));
            destination.setNumberOfMessages(getMessagesTotalCount(mbsc, jmsDestination));

            return destination;
        } catch (Exception e) {
            throw new InternalJMSException("failed to build JMSInternalDestination ", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param internalJMSDestination internal representation of a JMS Destination
     * @return
     */
    @Override
    public long getDestinationCount(InternalJMSDestination internalJMSDestination) {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public Long execute(MBeanServerConnection mbsc) {
                        return getDestinationCount(mbsc, internalJMSDestination);
                    }
                }
        );
    }

    protected Long getDestinationCount(MBeanServerConnection mbsc, InternalJMSDestination internalJMSDestination) {
        ObjectName jmsDestination = internalJMSDestination.getProperty(PROPERTY_OBJECT_NAME);
        try {
            return getMessagesTotalCount(mbsc, jmsDestination);
        } catch (Exception e) {
            throw new InternalJMSException("Exception while getting count for destination: " + internalJMSDestination.getName(), e);
        }
    }

    protected List<String> getManagedServerNames() {
        if (managedServerNames == null) {
            managedServerNames = jmxTemplate.query(
                    new JMXOperation() {
                        @Override
                        public List<String> execute(MBeanServerConnection mbsc) {
                            return getManagedServerNames(mbsc);
                        }
                    }
            );
            return managedServerNames;
        }
        return managedServerNames;
    }

    protected List<String> getManagedServerNames(MBeanServerConnection mbsc) {
        List<String> result = new ArrayList<>();
        try {
            ObjectName drs = jmxHelper.getDomainRuntimeService();
            ObjectName[] servers = (ObjectName[]) mbsc.getAttribute(drs, ATTR_SERVER_RUNTIMES);
            for (ObjectName server : servers) {
                final Boolean isAdminServer = (Boolean) mbsc.getAttribute(server, ATTR_ADMIN_SERVER);
                //we want only the managed server names
                if (isAdminServer) {
                    continue;
                }
                String serverName = (String) mbsc.getAttribute(server, ATTR_NAME);
                LOG.debug("Found managed server [{}]", serverName);
                result.add(serverName);
            }
            return result;
        } catch (Exception e) {
            throw new InternalJMSException("Failed getting managed server names", e);
        }
    }


    protected Map<String, InternalJMSDestination> findDestinationsGroupedByName() {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public Map<String, InternalJMSDestination> execute(MBeanServerConnection mbsc) {
                        return findDestinationsGroupedByName(mbsc);
                    }
                }
        );
    }

    protected Map<String, InternalJMSDestination> findDestinationsGroupedByName(MBeanServerConnection mbsc) {
        Map<String, InternalJMSDestination> destinationsMap = new HashMap<>();
        try {
            for (Map.Entry<String, ObjectName> mapEntry : getJmsDestinations(mbsc).entrySet()) {
                final String destinationFQName = mapEntry.getKey();
                final ObjectName jmsDestination = mapEntry.getValue();

                InternalJMSDestination destination = createInternalJMSDestination(mbsc, jmsDestination, destinationFQName);
                if (destination == null) continue; //topic
                destinationsMap.put(destinationFQName, destination);
            }
            return destinationsMap;
        } catch (Exception e) {
            throw new InternalJMSException(FAILED_TO_BUILD_JMS_DEST_MAP, e);
        }
    }

    private String getOnlyDestName(String destName) {
        if (destName.contains("@")) {
            destName = StringUtils.substringAfterLast(destName, "@");
        }
        return destName;
    }

    protected long getMessagesTotalCount(MBeanServerConnection mbsc, ObjectName jmsDestination) {
        if (domibusConfigurationService.isMultiTenantAware() && !authUtils.isSuperAdmin()) {
            //in multi-tenancy mode we show the number of messages only to super admin
            return NB_MESSAGES_ADMIN;
        }
        long result = 0;

        try {
            Long messagesCurrentCount = (Long) mbsc.getAttribute(jmsDestination, "MessagesCurrentCount");
            if (messagesCurrentCount != null) {
                result += messagesCurrentCount;
            }
            Long messagesPendingCount = (Long) mbsc.getAttribute(jmsDestination, "MessagesPendingCount");
            if (messagesPendingCount != null) {
                result += messagesPendingCount;
            }

            return result;
        } catch (Exception e) {
            throw new InternalJMSException("Failed getting the count for jmsDestination", e);
        }
    }

    protected String removeJmsModuleAndServer(String destinationFQName) {
        String destName = StringUtils.substringAfter(destinationFQName, "@");
        return (StringUtils.isEmpty(destName)) ? destinationFQName : destName;
    }

    protected String getShortDestName(String destinationName) {
        String result = destinationName;
        if (result.contains("!")) {
            result = result.substring(result.lastIndexOf('!') + 1);
        }
        if (result.contains(".")) {
            result = result.substring(result.lastIndexOf('.') + 1);
        }
        if (result.contains("@")) {
            result = StringUtils.substringAfter(destinationName, "@");
        }
        return result;
    }

    private String removeJmsModule(String destination) {
        String destName = StringUtils.substringAfter(destination, "!");
        return (StringUtils.isEmpty(destName)) ? destination : destName;
    }

    protected Map<String, ObjectName> getQueueMap(MBeanServerConnection mbsc) throws IOException, AttributeNotFoundException, InstanceNotFoundException, MBeanException,
            ReflectionException {
        if (queueMap != null) {
            return queueMap;
        }
        ObjectName drs = jmxHelper.getDomainRuntimeService();
        ObjectName config = (ObjectName) mbsc.getAttribute(drs, ATTR_DOMAIN_CONFIGURATION);
        queueMap = new HashMap<>();
        ObjectName[] configJmsSystemResources = (ObjectName[]) mbsc.getAttribute(config, ATTR_JMS_SYSTEM_RESOURCES);
        for (ObjectName configJmsSystemResource : configJmsSystemResources) {
            ObjectName configJmsResource = (ObjectName) mbsc.getAttribute(configJmsSystemResource, ATTR_JMS_RESOURCE);
            ObjectName[] configQueues = (ObjectName[]) mbsc.getAttribute(configJmsResource, ATTR_QUEUES);
            for (ObjectName configQueue : configQueues) {
                String configQueueName = (String) mbsc.getAttribute(configQueue, ATTR_NAME);
                queueMap.put(configQueueName, configQueue);
            }
            ObjectName[] configDDQueues = (ObjectName[]) mbsc.getAttribute(configJmsResource, ATTR_DISTRIBUTED_QUEUES);
            for (ObjectName configQueue : configDDQueues) {
                String configQueueName = (String) mbsc.getAttribute(configQueue, ATTR_NAME);
                queueMap.put(configQueueName, configQueue);
            }
            ObjectName[] configUDDQueues = (ObjectName[]) mbsc.getAttribute(configJmsResource, ATTR_UNIFORM_DISTRIBUTED_QUEUES);
            for (ObjectName configQueue : configUDDQueues) {
                String configQueueName = (String) mbsc.getAttribute(configQueue, ATTR_NAME);
                queueMap.put(configQueueName, configQueue);
            }
        }
        return queueMap;
    }

    protected Queue getQueue(String queueName) throws NamingException {
        return (Queue) lookupDestination(queueName);
    }

    protected Topic getTopic(String topicName) throws NamingException {
        return (Topic) lookupDestination(topicName);
    }

    protected Destination lookupDestination(String destJndiName) throws NamingException {
        LOG.debug("Retrieving destination with JNDI name [{}] ", destJndiName);
        return jmsDestinationCache.getByJndiName(destJndiName);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, String destName) {
        sendMessage(message, destName, jmsSender);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, String destName, JmsOperations jmsOperations) {
        try {
            JmsMessageCreator messageCreator = new JmsMessageCreator(message);
            jmsOperations.send(lookupDestination(destName), messageCreator);
        } catch (NamingException e) {
            throw new InternalJMSException("Error performing lookup for [" + destName + "]", e);
        }
    }

    @Override
    public void sendMessage(InternalJmsMessage message, Destination destination) {
        sendMessage(message, destination, jmsSender);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, Destination destination, JmsOperations jmsOperations) {
        jmsOperations.send(destination, new JmsMessageCreator(message));
    }

    @Override
    public void sendMessageToTopic(InternalJmsMessage internalJmsMessage, Topic destination) {
        sendMessageToTopic(internalJmsMessage, destination, false);
    }

    protected void sendMessage(InternalJmsMessage internalJmsMessage, Topic destination) {
        final boolean isClusterDeployment = domibusConfigurationService.isClusterDeployment();
        if (!isClusterDeployment) {
            LOG.debug("Sending JMS message to topic");
            sendMessage(internalJmsMessage, destination);
            return;
        }
        //the uniform distributed topics do not work correctly in WebLogic 12.1.3
        // the JMS message is not correctly replicated to all managed servers when the cluster is composed of more than 2 managed servers
        LOG.debug("Cluster deployment: using command signaling via database instead of uniform distributed topic");
        String command = (String) internalJmsMessage.getProperty(Command.COMMAND);
        String domain = (String) internalJmsMessage.getProperty(MessageConstants.DOMAIN);
        String originServer = (String) internalJmsMessage.getProperty(CommandProperty.ORIGIN_SERVER);

        final List<String> managedServerNamesList = getManagedServerNames();
        LOG.debug("Found managed servers [{}]", managedServerNamesList);

        if (StringUtils.isNotBlank(originServer)) {
            managedServerNamesList.remove(originServer);
            LOG.debug("Managed servers [{}] after exclusion of origin server [{}]", managedServerNamesList, originServer);
        }

        for (String managedServerName : managedServerNamesList) {
            commandService.createClusterCommand(command, domain, managedServerName, internalJmsMessage.getCustomProperties());
        }
    }

    @Override
    public void sendMessageToTopic(InternalJmsMessage internalJmsMessage, Topic destination, boolean excludeOrigin) {
        if (excludeOrigin) {
            internalJmsMessage.setProperty(CommandProperty.ORIGIN_SERVER, serverInfoService.getServerName());
        }
        sendMessage(internalJmsMessage, destination);
    }

    protected ObjectName getMessageDestinationName(String source) {
        InternalJMSDestination intJmsDest = getInternalJMSDestination(source);
        return intJmsDest.getProperty(PROPERTY_OBJECT_NAME);
    }

    @Override
    public int deleteMessages(String source, String[] messageIds) {
        String selector = jmsSelectorUtil.getSelector(messageIds);
        return deleteMessages(getMessageDestinationName(removeJmsModule(source)), selector);
    }

    @Override
    public InternalJmsMessage getMessage(String source, String messageId) {
        InternalJmsMessage internalJmsMessage = null;
        for (InternalJMSDestination internalJmsDestination : getInternalJMSDestinations(removeJmsModule(source))) {
            try {
                ObjectName destination = internalJmsDestination.getProperty(PROPERTY_OBJECT_NAME);
                internalJmsMessage = getMessageFromDestination(destination, messageId);
                if (internalJmsMessage != null) break;
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
        return internalJmsMessage;
    }

    protected InternalJmsMessage getMessageFromDestination(ObjectName destination, String messageId) {
        List<InternalJmsMessage> messages = getMessagesFromDestination(destination, jmsSelectorUtil.getSelector(messageId));
        if (!messages.isEmpty()) {
            return messages.get(0);
        }
        return null;
    }

    protected InternalJmsMessage getMessageFromDestinationUsingCustomSelector(ObjectName destination, String customSelector) {
        List<InternalJmsMessage> messages = getMessagesFromDestination(destination, customSelector);
        if (!messages.isEmpty()) {
            return messages.get(0);
        }
        return null;
    }

    protected List<InternalJmsMessage> getMessagesFromDestination(final ObjectName destination, final String selectorString) {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public List<InternalJmsMessage> execute(MBeanServerConnection mbsc) {
                        return doGetMessagesFromDestination(mbsc, selectorString, destination);
                    }
                }
        );
    }

    protected List<InternalJmsMessage> doGetMessagesFromDestination(MBeanServerConnection mbsc, String selectorString, ObjectName destination) {
        try {
            List<InternalJmsMessage> messages = new ArrayList<>();
            String selector = selectorString;
            if (selector == null) {
                selector = "true";
            }
            Integer timeout = 0;
            // Visible and available for consumption
            Integer stateMask = MessageInfo.STATE_VISIBLE;

            String messageCursor = (String) mbsc.invoke(
                    destination,
                    "getMessages",
                    new Object[]{selector, timeout, stateMask},
                    new String[]{String.class.getName(), Integer.class.getName(), Integer.class.getName()});

            Long totalAmountOfMessages = (Long) mbsc.invoke(
                    destination,
                    "getCursorSize",
                    new Object[]{messageCursor}, new String[]{String.class.getName()});

            Integer maxBrowseSize = totalAmountOfMessages.intValue();
            final Integer configuredMaxBrowseCount = NumberUtils.toInt(domibusPropertyProvider.getProperty(PROP_MAX_BROWSE_SIZE));
            if (configuredMaxBrowseCount > 0) {
                LOG.debug("Setting JMS maxBrowse size to [{}]", configuredMaxBrowseCount);
                maxBrowseSize = configuredMaxBrowseCount;
            }

            CompositeData[] allMessageMetaData = (CompositeData[]) mbsc.invoke(
                    destination,
                    "getItems",
                    new Object[]{messageCursor, Long.valueOf(0), maxBrowseSize},
                    new String[]{String.class.getName(), Long.class.getName(), Integer.class.getName()});

            if (allMessageMetaData != null) {
                for (CompositeData compositeData : allMessageMetaData) {
                    try {
                        InternalJmsMessage message = getInternalJmsMessage(destination, mbsc, messageCursor, compositeData);
                        messages.add(message);
                    } catch (Exception e) {
                        LOG.error("Error converting message [" + compositeData + "]", e);
                    }
                }
            }
            return messages;
        } catch (Exception e) {
            throw new InternalJMSException("Error getting messages from destination: " + destination, e);
        }
    }

    protected InternalJmsMessage getInternalJmsMessage(ObjectName destination, MBeanServerConnection mbsc, String messageCursor, CompositeData messageMetaData) throws ParserConfigurationException, SAXException, IOException, MBeanException, InstanceNotFoundException, ReflectionException {
        InternalJmsMessage message = convertMessage(messageMetaData);
        String messageId = message.getId();
        CompositeData messageDataDetails = (CompositeData) mbsc.invoke(destination, "getMessage", new Object[]{messageCursor, messageId}, new String[]{
                String.class.getName(), String.class.getName()});
        message = convertMessage(messageDataDetails);
        return message;
    }

    /**
     * Finds the InternalJMSDestination corresponding to the source parameter.
     *
     * @param source name of the JMS source
     * @return a list of InternalJMSDestination (one per each managed server)
     */
    protected List<InternalJMSDestination> getInternalJMSDestinations(String source) {
        if (StringUtils.isEmpty(source)) {
            throw new InternalJMSException("Source has not been specified");
        }
        List<InternalJMSDestination> destinations = new ArrayList<>();
        Map<String, InternalJMSDestination> internalDestinations = findDestinationsGroupedByName();
        for (Map.Entry<String, InternalJMSDestination> entry : internalDestinations.entrySet()) {
            if (entry.getKey().contains(source)) {
                LOG.debug(LOG_INTERNAL_DESTINATION_FOUND_FOR_SOURCE, source);
                destinations.add(entry.getValue());
            }
        }
        if (destinations.isEmpty()) {
            throw new InternalJMSException("Could not find any destination for source[" + source + "]");
        }
        return destinations;
    }

    /**
     * Finds the InternalJMSDestination corresponding to the source parameter.
     *
     * @param source name of the JMS source including the managed server.
     * @return an InternalJMSDestination
     */
    protected InternalJMSDestination getInternalJMSDestination(String source) {
        if (StringUtils.isEmpty(source)) {
            throw new InternalJMSException("Source has not been specified");
        }
        Map<String, InternalJMSDestination> internalDestinations = findDestinationsGroupedByFQName();
        for (Map.Entry<String, InternalJMSDestination> entry : internalDestinations.entrySet()) {
            if (entry.getKey().contains(source)) {
                LOG.debug(LOG_INTERNAL_DESTINATION_FOUND_FOR_SOURCE, source);
                return entry.getValue();
            }
        }
        throw new InternalJMSException("Could not find internal destination for [" + source + "]");
    }

    /**
     * To be used to browse all the messages of a specific queue. It works also in a cluster.
     *
     * @param source
     * @return
     */
    @Override
    public List<InternalJmsMessage> browseClusterMessages(String source, String selector) {
        List<InternalJmsMessage> internalJmsMessages = new ArrayList<>();
        final String sourceWithoutJMSModule = removeJmsModule(source);
        List<InternalJMSDestination> destinations = getInternalJMSDestinations(sourceWithoutJMSModule);
        for (InternalJMSDestination destination : destinations) {
            String destinationType = destination.getType();
            if (QUEUE.equals(destinationType)) {
                try {
                    ObjectName jmsDestination = destination.getProperty(PROPERTY_OBJECT_NAME);
                    internalJmsMessages.addAll(getMessagesFromDestination(jmsDestination, selector));
                } catch (Exception e) {
                    throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
                }
            } else {
                throw new InternalJMSException("Unrecognized destination type [" + destinationType + "]");
            }
        }
        return internalJmsMessages;
    }

    /**
     * To be used to browse messages for a specific queue and respecting certain criteria.
     *
     * @param source
     * @param jmsType
     * @param fromDate
     * @param toDate
     * @param selectorClause
     * @return
     */
    @Override
    public List<InternalJmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {

        List<InternalJmsMessage> internalJmsMessages = new ArrayList<>();
        InternalJMSDestination destination = getInternalJMSDestination(removeJmsModule(source));
        String destinationType = destination.getType();
        if (QUEUE.equals(destinationType)) {
            Map<String, Object> criteria = new HashMap<>();
            if (jmsType != null) {
                criteria.put(JMS_TYPE, jmsType);
            }
            if (fromDate != null) {
                criteria.put("JMSTimestamp_from", fromDate.getTime());
            }
            if (toDate != null) {
                criteria.put("JMSTimestamp_to", toDate.getTime());
            }
            if (selectorClause != null) {
                criteria.put("selectorClause", selectorClause);
            }
            String selector = jmsSelectorUtil.getSelector(criteria);
            try {
                ObjectName jmsDestination = destination.getProperty(PROPERTY_OBJECT_NAME);
                internalJmsMessages.addAll(getMessagesFromDestination(jmsDestination, selector));
            } catch (Exception e) {
                throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
            }
        } else {
            throw new InternalJMSException("Unrecognized destination type [" + destinationType + "]");
        }
        return internalJmsMessages;
    }

    protected int deleteMessages(final ObjectName destination, final String selector) {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public Integer execute(MBeanServerConnection mbsc) {
                        return doDeleteMessages(mbsc, destination, selector);
                    }
                }
        );
    }

    protected Integer doDeleteMessages(MBeanServerConnection mbsc, ObjectName destination, String selector) {
        try {
            return (Integer) mbsc.invoke(destination, "deleteMessages", new Object[]{selector}, new String[]{String.class.getName()});
        } catch (Exception e) {
            throw new InternalJMSException("Failed to delete messages", e);
        }
    }

    @Override
    public int moveMessages(String sourceFrom, String sourceTo, String[] messageIds) {

        ObjectName fromDestination = getMessageDestinationName(removeJmsModule(sourceFrom));
        ObjectName toDestination = getMessageDestinationName(removeJmsModule(sourceTo));
        String selector = jmsSelectorUtil.getSelector(messageIds);
        return moveMessages(fromDestination, toDestination, selector);
    }

    protected int moveMessages(final ObjectName from, final ObjectName to, final String selector) {
        return jmxTemplate.query(
                new JMXOperation() {
                    @Override
                    public Integer execute(MBeanServerConnection mbsc) {
                        return doMoveMessages(mbsc, to, from, selector);
                    }
                }
        );
    }

    protected Integer doMoveMessages(MBeanServerConnection mbsc, ObjectName to, ObjectName from, String selector) {
        try {
            CompositeData toDestinationInfo = (CompositeData) mbsc.getAttribute(to, "DestinationInfo");
            return (Integer) mbsc.invoke(from, "moveMessages", new Object[]{selector, toDestinationInfo}, new String[]{String.class.getName(),
                    CompositeData.class.getName()});
        } catch (Exception e) {
            throw new InternalJMSException("Error moving messages", e);
        }
    }

    /**
     * Implements the consume operation.
     * The message is browsed, deleted and returned to caller.
     *
     * @param source          name of the JMS queue
     * @param customMessageId ID of the message present in the custom properties.
     * @return
     */
    @Override
    public InternalJmsMessage consumeMessage(String source, String customMessageId) {

        String selector = "MESSAGE_ID = '" + customMessageId + "' AND NOTIFICATION_TYPE ='MESSAGE_RECEIVED'";

        InternalJmsMessage internalJmsMessage = null;
        ObjectName destinationName = null;
        try {
            for (InternalJMSDestination internalJmsDestination : getInternalJMSDestinations(removeJmsModule(source))) {
                destinationName = internalJmsDestination.getProperty(PROPERTY_OBJECT_NAME);
                internalJmsMessage = getMessageFromDestinationUsingCustomSelector(destinationName, selector);
                if (internalJmsMessage != null) break;
            }
            if (internalJmsMessage != null) {
                deleteMessages(destinationName, selector);
            }
        } catch (Exception ex) {
            throw new InternalJMSException("Failed to consume message [" + customMessageId + "] from source [" + source + "]", ex);
        }
        return internalJmsMessage;
    }


    protected InternalJmsMessage convertMessage(CompositeData messageData) throws IOException, SAXException, ParserConfigurationException {
        InternalJmsMessage message = new InternalJmsMessage();
        String xmlMessage = String.valueOf(messageData.get("MessageXMLText"));
        Document xmlDocument = parseXML(xmlMessage);
        Element root = xmlDocument.getDocumentElement();
        Element header = getChildElement(root, "Header");
        Element jmsMessageId = getChildElement(header, "JMSMessageID");
        String id = jmsMessageId.getTextContent();
        message.setId(id);
        message.getProperties().put("JMSMessageID", id);
        Element jmsTimestamp = getChildElement(header, "JMSTimestamp");
        String timestamp = jmsTimestamp.getTextContent();
        message.setTimestamp(new Date(Long.parseLong(timestamp)));
        message.getProperties().put("JMSTimestamp", timestamp);
        Element jmsType = getChildElement(header, JMS_TYPE);
        if (jmsType != null) {
            String type = jmsType.getTextContent();
            message.setType(type);
            message.getProperties().put(JMS_TYPE, type);
        }
        Element jmsPriority = getChildElement(header, JMS_PRIORITY);
        if (jmsPriority != null) {
            String priorityString = jmsPriority.getTextContent();
            message.getProperties().put(JMS_PRIORITY, priorityString);
            try {
                int priority = Integer.parseInt(priorityString);
                message.setPriority(priority);
            } catch (NumberFormatException e) {
                LOG.error("Could not parse priority [" + priorityString + "]", e);
            }
        }


        Element propertiesRoot = getChildElement(header, "Properties");
        List<Element> properties = getChildElements(propertiesRoot, "property");
        for (Element property : properties) {
            String key = property.getAttribute("name");
            final Element firstChildElement = getFirstChildElement(property);
            String value = null;
            if (firstChildElement != null) {
                value = firstChildElement.getTextContent();
            }
            message.getProperties().put(key, value);
        }
        Element jmsBody = getChildElement(root, "Body");
        if (jmsBody != null) {
            message.setContent(StringUtils.trim(jmsBody.getTextContent()));
        }
        return message;
    }

    protected Document parseXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        factory.setNamespaceAware(true);
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    protected Element getChildElement(Element parent, String name) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) parent.getChildNodes().item(i);
                String localName = child.getLocalName();
                if (localName == null) {
                    localName = child.getNodeName();
                    if (localName != null && localName.contains(":")) {
                        localName = localName.substring(localName.indexOf(':') + 1);
                    }
                }
                if (StringUtils.equals(localName, name)) {
                    return child;
                }
            }
        }
        return null;
    }

    protected List<Element> getChildElements(Element parent, String name) {
        List<Element> childElements = new ArrayList<>();
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) parent.getChildNodes().item(i);
                String localName = child.getLocalName();
                if (localName == null) {
                    localName = child.getNodeName();
                    if (localName != null && localName.contains(":")) {
                        localName = localName.substring(localName.indexOf(':') + 1);
                    }
                }
                if (StringUtils.equals(localName, name)) {
                    childElements.add(child);
                }
            }
        }
        return childElements;
    }

    protected Element getFirstChildElement(Element parent) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                return (Element) parent.getChildNodes().item(i);
            }
        }
        return null;
    }

}