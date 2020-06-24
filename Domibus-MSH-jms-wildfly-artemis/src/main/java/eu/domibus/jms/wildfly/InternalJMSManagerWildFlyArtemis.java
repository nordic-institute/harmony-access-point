package eu.domibus.jms.wildfly;

import eu.domibus.api.cluster.CommandProperty;
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
import org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.management.ActiveMQServerControl;
import org.apache.activemq.artemis.api.core.management.AddressControl;
import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.core.management.QueueControl;
import org.apache.activemq.artemis.utils.SelectorTranslator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.jms.*;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_ARTEMIS_BROKER;
import static org.apache.activemq.artemis.api.core.SimpleString.toSimpleString;

/**
 * JMSManager implementation for ActiveMQ Artemis (Wildfly 12)
 *
 * @author Catalin Enache
 * @since 4.0
 */
@Component
public class InternalJMSManagerWildFlyArtemis implements InternalJMSManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(InternalJMSManagerWildFlyArtemis.class);

    private static final String PROPERTY_OBJECT_NAME = "ObjectName";
    private static final String PROPERTY_JNDI_NAME = "Jndi";

    /**
     * The old Artemis 1.x JMS prefix.
     *
     * @see org.apache.activemq.artemis.core.protocol.core.impl.PacketImpl#OLD_QUEUE_PREFIX
     */
    public static final String JMS_QUEUE_PREFIX = "jms.queue.";

    protected Map<String, ObjectName> queueMap;

    protected Map<String, ObjectName> topicMap;

    protected MBeanServer mBeanServer;

    protected ActiveMQServerControl activeMQServerControl;

    protected JmsOperations jmsSender;

    protected JMSDestinationHelper jmsDestinationHelper;

    protected JMSSelectorUtil jmsSelectorUtil;

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected AuthUtils authUtils;

    protected DomibusConfigurationService domibusConfigurationService;

    protected ServerInfoService serverInfoService;

    public InternalJMSManagerWildFlyArtemis(MBeanServer mBeanServer,
                                            @Qualifier("activeMQServerControl") ActiveMQServerControl activeMQServerControl,
                                            @Qualifier("jmsSender") JmsOperations jmsSender,
                                            JMSDestinationHelper jmsDestinationHelper,
                                            JMSSelectorUtil jmsSelectorUtil,
                                            DomibusPropertyProvider domibusPropertyProvider,
                                            AuthUtils authUtils,
                                            DomibusConfigurationService domibusConfigurationService,
                                            ServerInfoService serverInfoService) {
        this.mBeanServer = mBeanServer;
        this.activeMQServerControl = activeMQServerControl;
        this.jmsSender = jmsSender;
        this.jmsDestinationHelper = jmsDestinationHelper;
        this.jmsSelectorUtil = jmsSelectorUtil;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.authUtils = authUtils;
        this.domibusConfigurationService = domibusConfigurationService;
        this.serverInfoService = serverInfoService;
    }

    /**
     * Returns null if the string is null or empty
     */
    protected String createFilterFromJMSSelector(final String selectorStr) {
        if(StringUtils.isBlank(selectorStr)){
            return null;
        }
        return SelectorTranslator.convertToActiveMQFilterString(selectorStr);
    }

    @Override
    public Map<String, InternalJMSDestination> findDestinationsGroupedByFQName() {
        Map<String, InternalJMSDestination> destinationMap = new TreeMap<>();

        try {
            for (ObjectName objectName : getQueueMap().values()) {
                QueueControl queueControl = getQueueControl(objectName);
                InternalJMSDestination internalJmsDestination = createInternalJMSDestination(objectName, queueControl);
                destinationMap.put(queueControl.getName(), internalJmsDestination);
            }
            return destinationMap;
        } catch (Exception e) {
            throw new InternalJMSException("Failed to build JMS destination map", e);
        }
    }

    protected InternalJMSDestination createInternalJMSDestination(ObjectName objectName, QueueControl queueControl) {
        InternalJMSDestination internalJmsDestination = new InternalJMSDestination();
        internalJmsDestination.setName(queueControl.getName());
        internalJmsDestination.setType(InternalJMSDestination.QUEUE_TYPE);
        internalJmsDestination.setNumberOfMessages(getMessagesTotalCount(queueControl));
        internalJmsDestination.setProperty(PROPERTY_OBJECT_NAME, objectName);
        internalJmsDestination.setProperty(PROPERTY_JNDI_NAME, queueControl.getAddress());
        internalJmsDestination.setInternal(jmsDestinationHelper.isInternal(queueControl.getAddress()));
        return internalJmsDestination;
    }

    protected long getMessagesTotalCount(QueueControl queueControl) {
        if (domibusConfigurationService.isMultiTenantAware() && !authUtils.isSuperAdmin()) {
            //in multi-tenancy mode we show the number of messages only to super admin
            return NB_MESSAGES_ADMIN;
        }
        long result;
        try {
            result = queueControl.getMessageCount();
        } catch (Exception e) {
            throw new InternalJMSException("Failed to get messages count on JMS destination: " + queueControl.getName(), e);
        }
        return result;
    }

    protected Map<String, ObjectName> getQueueMap() {
        if (queueMap != null) {
            return queueMap;
        }
        queueMap = new HashMap<>();
        queueMap.putAll(getQueueMap(RoutingType.ANYCAST));
        return queueMap;
    }

    protected Map<String, ObjectName> getTopicMap() {
        if (topicMap != null) {
            return topicMap;
        }
        topicMap = new HashMap<>();
        topicMap.putAll(getQueueMap(RoutingType.MULTICAST));
        return topicMap;
    }

    protected Map<String, ObjectName> getQueueMap(RoutingType routingType) {
        final Map<String, ObjectName> queues = new HashMap<>();
        LOG.debug("Retrieving the {} map from the server", routingType == RoutingType.ANYCAST ? "queue" : "topic");

        ObjectNameBuilder objectNameBuilder = ObjectNameBuilder.create(ActiveMQDefaultConfiguration.getDefaultJmxDomain(),
                domibusPropertyProvider.getProperty(ACTIVE_MQ_ARTEMIS_BROKER), true);

        String[] addressNames = activeMQServerControl.getAddressNames();
        LOG.debug("Address names: [{}]", Arrays.toString(addressNames));

        Arrays.stream(addressNames).forEach(addressName -> {
            try {
                ObjectName addressObjectName = objectNameBuilder.getAddressObjectName(toSimpleString(addressName));

                String[] queueNames = getAddressControl(addressObjectName).getQueueNames();
                LOG.debug("Address to queue names mapping: [{} -> {}]", addressName, Arrays.toString(queueNames));

                queues.putAll(getAddressQueueMap(addressName, queueNames, routingType, objectNameBuilder));
            } catch (Exception e) {
                // Just log the error and continue with the next address name
                LOG.error("Error creating object name for address [" + addressName + "]", e);
            }
        });

        return queues;
    }

    protected Map<String, ObjectName> getAddressQueueMap(String addressName, String[] queueNames, RoutingType routingType, ObjectNameBuilder objectNameBuilder) {
        Map<String, ObjectName> queueMap = Arrays.stream(queueNames).collect(Collectors.toMap(
                Function.identity(),
                queueName -> {
                    try {
                        return objectNameBuilder.getQueueObjectName(
                                toSimpleString(addressName),
                                toSimpleString(queueName),
                                routingType);
                    } catch (Exception e) {
                        throw new DomibusJMXException("Error creating object name for queue [" + queueName + "]", e);
                    }
                }));

        // Add corresponding entries for the non-qualified names so lookups without the Artemis 1.x JMS prefix will work
        // (using the ActiveMQJMSClient.enable1xPrefixes Artemis 2.x parameter doesn't seem to work)
        Map<String, ObjectName> nonFQNObjectMap = new HashMap<>();
        queueMap.entrySet().stream().forEach(queueEntry -> {
            String keyNonFQN = StringUtils.removeStart(queueEntry.getKey(), JMS_QUEUE_PREFIX);
            nonFQNObjectMap.put(keyNonFQN, queueEntry.getValue());
        });
        queueMap.putAll(nonFQNObjectMap);

        return queueMap;
    }

    protected QueueControl getQueueControl(ObjectName objectName) {
        return MBeanServerInvocationHandler.newProxyInstance(mBeanServer, objectName, QueueControl.class, false);
    }

    protected AddressControl getAddressControl(ObjectName objectName) {
        return MBeanServerInvocationHandler.newProxyInstance(mBeanServer, objectName, AddressControl.class, false);
    }

    protected QueueControl getQueueControl(String destName) {
        ObjectName objectName = getQueueMap().get(destName);
        if (objectName == null) {
            throw new InternalJMSException("Queue [" + destName + "] does not exists");
        }
        return getQueueControl(objectName);
    }

    protected QueueControl getTopicControl(String destName) {
        ObjectName objectName = getTopicMap().get(destName);
        if (objectName == null) {
            throw new InternalJMSException("Topic [" + destName + "] does not exists");
        }
        return getQueueControl(objectName);
    }

    protected Queue getQueue(String queueName) throws NamingException {
        return lookupQueue(queueName);
    }

    protected Topic getTopic(String topicName) throws NamingException {
        return lookupTopic(topicName);
    }

    protected String getJndiName(String destJndiName) {
        return "java:/" + StringUtils.replace(destJndiName, ".", "/");
    }

    protected String getJndiName(InternalJMSDestination internalJmsDestination) {
        String destinationJndi = internalJmsDestination.getProperty(PROPERTY_JNDI_NAME);
        return "java:/" + StringUtils.replace(destinationJndi, ".", "/");
    }

    protected Queue lookupQueue(String destName) throws NamingException {
        String destinationJndi = destName;

        ObjectName objectName = getQueueMap().get(destName);
        if (objectName != null) {
            destinationJndi = getJndiName(getQueueControl(objectName).getAddress());
        }

        LOG.debug("Found JNDI [{}] for queue [{}]", destinationJndi, destName);
        return InitialContext.doLookup(destinationJndi);
    }

    protected Topic lookupTopic(String destName) throws NamingException {
        String destinationJndi = getJndiName(getTopicControl(destName).getAddress());
        LOG.debug("Found JNDI [{}] for topic [{}]", destinationJndi, destName);
        return InitialContext.doLookup(destinationJndi);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, String destName) {
        sendMessage(message, destName, jmsSender);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, String destName, JmsOperations jmsOperations) {
        try {
            jmsOperations.send(lookupQueue(destName), new JmsMessageCreator(message));
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

    @Override
    public void sendMessageToTopic(InternalJmsMessage internalJmsMessage, Topic destination, boolean excludeOrigin) {
        if (excludeOrigin) {
            internalJmsMessage.setProperty(CommandProperty.ORIGIN_SERVER, serverInfoService.getServerName());
        }
        sendMessage(internalJmsMessage, destination);
    }

    @Override
    public int deleteMessages(String source, String[] messageIds) {
        QueueControl queue = getQueueControl(source);
        try {
            String filterFromJMSSelector = createFilterFromJMSSelector(jmsSelectorUtil.getSelector(messageIds));
            return queue.removeMessages(filterFromJMSSelector);
        } catch (Exception e) {
            throw new InternalJMSException("Failed to delete messages from source [" + source + "]:" + Arrays.toString(messageIds), e);
        }
    }

    @Override
    public InternalJmsMessage getMessage(String source, String messageId) {
        String selector = jmsSelectorUtil.getSelector(messageId);

        try {
            List<InternalJmsMessage> messages = getMessagesFromDestination(source, selector);
            if (!messages.isEmpty()) {
                return messages.get(0);
            }
        } catch (Exception e) {
            throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
        }
        return null;
    }

    @Override
    public List<InternalJmsMessage> browseClusterMessages(String source, String selector) {
        return browseMessages(source, null, null, null, selector);
    }

    @Override
    public List<InternalJmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {
        if (StringUtils.isEmpty(source)) {
            throw new InternalJMSException("Source has not been specified");
        }

        source = StringUtils.prependIfMissing(source, JMS_QUEUE_PREFIX);
        InternalJMSDestination destination = findDestinationsGroupedByFQName().get(source);
        if (destination == null) {
            throw new InternalJMSException("Could not find destination for [" + source + "]");
        }
        List<InternalJmsMessage> internalJmsMessages = new ArrayList<>();
        String destinationType = destination.getType();
        if ("Queue".equals(destinationType)) {
            Map<String, Object> criteria = new HashMap<>();
            if (jmsType != null) {
                criteria.put("JMSType", jmsType);
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
                internalJmsMessages.addAll(getMessagesFromDestination(source, selector));
            } catch (Exception e) {
                throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
            }
        } else {
            throw new InternalJMSException("Unrecognized destination type [" + destinationType + "]");
        }
        return internalJmsMessages;
    }

    private List<InternalJmsMessage> getMessagesFromDestination(String destination, String selector) throws NamingException {
        Queue queue = getQueue(destination);
        return jmsSender.browseSelected(queue, selector, new BrowserCallback<List<InternalJmsMessage>>() {
            @Override
            public List<InternalJmsMessage> doInJms(Session session, QueueBrowser browser) throws JMSException {
                List<InternalJmsMessage> result = new ArrayList<>();
                Enumeration enumeration = browser.getEnumeration();
                while (enumeration.hasMoreElements()) {
                    Object message = enumeration.nextElement();
                    try {
                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            result.add(convert(textMessage));
                        } else if (message instanceof MapMessage) {
                            MapMessage mapMessage = (MapMessage) message;
                            result.add(convert(mapMessage));
                        }
                    } catch (Exception e) {
                        LOG.error("Error converting message [" + message + "]", e);
                    }

                }
                return result;
            }
        });
    }

    protected InternalJmsMessage convert(TextMessage textMessage) throws JMSException {
        InternalJmsMessage result = new InternalJmsMessage();
        result.setContent(textMessage.getText());
        result.setId(textMessage.getJMSMessageID());
        result.setTimestamp(new Date(textMessage.getJMSTimestamp()));
        result.setType(textMessage.getJMSType());
        Enumeration propertyNames = textMessage.getPropertyNames();

        Map<String, Object> properties = new HashMap<>();
        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            Object objectProperty = textMessage.getObjectProperty(name);
            properties.put(name, objectProperty);
        }
        result.setProperties(properties);
        return result;
    }

    protected InternalJmsMessage convert(MapMessage mapMessage) throws JMSException {
        InternalJmsMessage result = new InternalJmsMessage();

        result.setType(mapMessage.getJMSType());
        Long jmsTimestamp = mapMessage.getJMSTimestamp();
        if (jmsTimestamp != null) {
            result.setTimestamp(new Date(jmsTimestamp));
        }
        result.setId(mapMessage.getJMSMessageID());

        Map<String, Object> properties = new HashMap<>();
        Enumeration<String> propertyNames = mapMessage.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String mapKey = propertyNames.nextElement();
            properties.put(mapKey, mapMessage.getObjectProperty(mapKey));
        }
        result.setProperties(properties);
        return result;
    }

    @Override
    public int moveMessages(String source, String destination, String[] messageIds) {
        try {
            QueueControl queue = getQueueControl(source);
            return queue.moveMessages(createFilterFromJMSSelector(jmsSelectorUtil.getSelector(messageIds)), destination);
        } catch (Exception e) {
            throw new InternalJMSException("Failed to move messages from source [" + source + "] to destination [" + destination + "]:" + Arrays.toString(messageIds), e);
        }
    }

    @Override
    public InternalJmsMessage consumeMessage(String source, String customMessageId) {

        InternalJmsMessage intJmsMsg = null;
        String selector = "MESSAGE_ID='" + customMessageId + "' AND NOTIFICATION_TYPE ='MESSAGE_RECEIVED'";

        try {
            List<InternalJmsMessage> messages = getMessagesFromDestination(source, selector);
            if (!messages.isEmpty()) {
                intJmsMsg = messages.get(0);
                // Deletes it
                QueueControl queue = getQueueControl(source);
                int removeMessages = queue.removeMessages(selector);
                LOG.debug("{} Jms Messages Id [{}] deleted from the source queue [{}] ", removeMessages, customMessageId, source);
            }
        } catch (Exception ex) {
            throw new InternalJMSException("Failed to consume message [" + customMessageId + "] from source [" + source + "]", ex);
        }
        return intJmsMsg;
    }

    @Override
    public long getDestinationCount(InternalJMSDestination internalJMSDestination) {
        final ObjectName objectName = internalJMSDestination.getProperty(PROPERTY_OBJECT_NAME);
        final QueueControl queueControl = getQueueControl(objectName);
        return getMessagesTotalCount(queueControl);
    }

}
