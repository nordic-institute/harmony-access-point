package eu.domibus.jms.wildfly;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.management.ActiveMQServerControl;
import org.apache.activemq.artemis.api.core.management.AddressControl;
import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.jms.MapMessage;
import javax.management.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_ARTEMIS_BROKER;
import static org.apache.activemq.artemis.api.core.SimpleString.toSimpleString;
import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@RunWith(JMockit.class)
public class InternalJMSManagerWildFlyArtemisTest {

    @Tested
    InternalJMSManagerWildFlyArtemis jmsManager;

    @Injectable
    MBeanServer mBeanServer;

    @Injectable
    ActiveMQServerControl activeMQServerControl;

    @Injectable
    private JmsOperations jmsOperations;

    @Injectable
    JMSDestinationHelper jmsDestinationHelper;

    @Injectable
    JMSSelectorUtil jmsSelectorUtil;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private ServerInfoService serverInfoService;

    @Test
    public void createFilterFromJMSSelector_null() {
        assertNull(jmsManager.createFilterFromJMSSelector(null));
    }

    @Test
    public void createFilterFromJMSSelector_empty() {
        assertNull(jmsManager.createFilterFromJMSSelector(""));
    }

    @Test
    public void createFilterFromJMSSelector_OK() {
        assertEquals("AMQUserID",jmsManager.createFilterFromJMSSelector("JMSMessageID"));
    }

    @Test
    public void testConvertMapMessage(final @Injectable MapMessage mapMessage) throws Exception {
        final String jmsType = "mytype";
        final Date jmsTimestamp = new Date();
        final String jmsId1 = "jmsId1";
        final List<String> allPropertyNames = Arrays.asList("JMSProp1", "totalNumberOfPayloads");
        final List<String> mapNames = Arrays.asList("payload_1");

        new NonStrictExpectations() {
            {
                mapMessage.getJMSType();
                result = jmsType;

                mapMessage.getJMSTimestamp();
                result = jmsTimestamp.getTime();

                mapMessage.getJMSMessageID();
                result = jmsId1;

                mapMessage.getPropertyNames();
                result = new Vector(allPropertyNames).elements();

                mapMessage.getObjectProperty("JMSProp1");
                result = "JMSValue1";

                mapMessage.getObjectProperty("totalNumberOfPayloads");
                result = 5;

                mapMessage.getMapNames();
                result = new Vector(mapNames).elements();

                mapMessage.getObject("payload_1");
                result = "payload";
            }
        };

        InternalJmsMessage internalJmsMessage = jmsManager.convert(mapMessage);

        assertEquals(internalJmsMessage.getType(), jmsType);
        assertEquals(internalJmsMessage.getTimestamp(), jmsTimestamp);
        assertEquals(internalJmsMessage.getId(), jmsId1);

        Map<String, String> properties = internalJmsMessage.getProperties();
        assertEquals(properties.size(), 2);
        assertEquals(properties.get("JMSProp1"), "JMSValue1");
        assertEquals(properties.get("totalNumberOfPayloads"), "5");
        assertEquals(properties.get("payload_1"), null);
    }

    @Test
    public void testGetQueueMap(@Injectable AddressControl sendAddressControl,
                                @Injectable AddressControl splitAndJoinAddressControl) throws Exception {
        final String[] addressNames = {"jms.queue.DomibusSendMessageQueue", "jms.queue.DomibusSplitAndJoinQueue"};
        final String[] sendQueueNames = {"jms.queue.DomibusSendMessageQueue1", "jms.queue.DomibusSendMessageQueue2"};
        final String[] splitAndJoinQueueNames = {"jms.queue.DomibusSplitAndJoinQueue1"};

        // GIVEN
        new MockUp<MBeanServerInvocationHandler>() {
            @Mock
            public <T> T newProxyInstance(MBeanServerConnection connection,
                                          ObjectName objectName,
                                          Class<T> interfaceClass,
                                          boolean notificationBroadcaster) {
                if (AddressControl.class.isAssignableFrom(interfaceClass)) {
                    if (objectName.getCanonicalName().contains("address=\"jms.queue.DomibusSendMessageQueue\"")) {
                        return (T) sendAddressControl;
                    } else if (objectName.getCanonicalName().contains("address=\"jms.queue.DomibusSplitAndJoinQueue\"")) {
                        return (T) splitAndJoinAddressControl;
                    } else {
                        throw new IllegalArgumentException("Unknown AddressControl object name: " + objectName);
                    }
                }
                throw new IllegalArgumentException("Unknown proxy interface argument: " + interfaceClass);
            }
        };

        new Expectations(jmsManager) {{
            domibusPropertyProvider.getProperty(ACTIVE_MQ_ARTEMIS_BROKER);
            result = "localhost";
            activeMQServerControl.getAddressNames();
            result = addressNames;

            sendAddressControl.getQueueNames();
            result = sendQueueNames;
            jmsManager.getAddressQueueMap("jms.queue.DomibusSendMessageQueue", sendQueueNames, RoutingType.ANYCAST, (ObjectNameBuilder) any);
            result = Arrays.stream(sendQueueNames).collect(Collectors.toMap(Function.identity(),
                    queueName -> getQueueObjectName("jms.queue.DomibusSendMessageQueue", queueName)));

            splitAndJoinAddressControl.getQueueNames();
            result = splitAndJoinQueueNames;
            jmsManager.getAddressQueueMap("jms.queue.DomibusSplitAndJoinQueue", splitAndJoinQueueNames, RoutingType.ANYCAST, (ObjectNameBuilder) any);
            result = Arrays.stream(splitAndJoinQueueNames).collect(Collectors.toMap(Function.identity(),
                    queueName -> getQueueObjectName("jms.queue.DomibusSplitAndJoinQueue", queueName)));
        }};

        // WHEN
        Map<String, ObjectName> queues = jmsManager.getQueueMap(RoutingType.ANYCAST);

        // THEN
        new Verifications() {{
            Assert.assertEquals("Should have built the map of queue object names from the provided addresses",
                    new HashSet(Arrays.asList(
                            "jms.queue.DomibusSendMessageQueue1->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSendMessageQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSendMessageQueue1\"",
                            "jms.queue.DomibusSplitAndJoinQueue1->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSplitAndJoinQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSplitAndJoinQueue1\"",
                            "jms.queue.DomibusSendMessageQueue2->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSendMessageQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSendMessageQueue2\"")),
                    queues.entrySet().stream()
                            .map(entry -> entry.getKey() + "->" + entry.getValue().toString())
                            .collect(Collectors.toSet())
            );
        }};
    }

    @Test
    public void testGetQueueMap_IgnoresAddressesThrowingExceptions_getQueueNames(@Injectable AddressControl sendAddressControl,
                                                                                 @Injectable AddressControl splitAndJoinAddressControl) throws Exception {
        final String[] addressNames = {"jms.queue.DomibusSendMessageQueue", "jms.queue.DomibusSplitAndJoinQueue"};
        final String[] sendQueueNames = {"jms.queue.DomibusSendMessageQueue1", "jms.queue.DomibusSendMessageQueue2"};

        // GIVEN
        new MockUp<MBeanServerInvocationHandler>() {
            @Mock
            public <T> T newProxyInstance(MBeanServerConnection connection,
                                          ObjectName objectName,
                                          Class<T> interfaceClass,
                                          boolean notificationBroadcaster) {
                if (AddressControl.class.isAssignableFrom(interfaceClass)) {
                    if (objectName.getCanonicalName().contains("address=\"jms.queue.DomibusSendMessageQueue\"")) {
                        return (T) sendAddressControl;
                    } else if (objectName.getCanonicalName().contains("address=\"jms.queue.DomibusSplitAndJoinQueue\"")) {
                        return (T) splitAndJoinAddressControl;
                    } else {
                        throw new IllegalArgumentException("Unknown AddressControl object name: " + objectName);
                    }
                }
                throw new IllegalArgumentException("Unknown proxy interface argument: " + interfaceClass);
            }
        };

        new Expectations(jmsManager) {{
            domibusPropertyProvider.getProperty(ACTIVE_MQ_ARTEMIS_BROKER);
            result = "localhost";
            activeMQServerControl.getAddressNames();
            result = addressNames;

            sendAddressControl.getQueueNames();
            result = sendQueueNames;
            jmsManager.getAddressQueueMap("jms.queue.DomibusSendMessageQueue", sendQueueNames, RoutingType.ANYCAST, (ObjectNameBuilder) any);
            result = Arrays.stream(sendQueueNames).collect(Collectors.toMap(Function.identity(),
                    queueName -> getQueueObjectName("jms.queue.DomibusSendMessageQueue", queueName)));

            splitAndJoinAddressControl.getQueueNames();
            result = new Exception();
        }};

        // WHEN
        Map<String, ObjectName> queues = jmsManager.getQueueMap(RoutingType.ANYCAST);

        // THEN
        new Verifications() {{
            Assert.assertEquals("Should have built the map of queue object names from the provided addresses",
                    new HashSet(Arrays.asList(
                            "jms.queue.DomibusSendMessageQueue1->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSendMessageQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSendMessageQueue1\"",
                            "jms.queue.DomibusSendMessageQueue2->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSendMessageQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSendMessageQueue2\"")),
                    queues.entrySet().stream()
                            .map(entry -> entry.getKey() + "->" + entry.getValue().toString())
                            .collect(Collectors.toSet())
            );
        }};
    }

    @Test
    public void testGetQueueMap_IgnoresAddressesThrowingExceptions_getAddressQueueNames(@Injectable AddressControl sendAddressControl,
                                                                                        @Injectable AddressControl splitAndJoinAddressControl) throws Exception {
        final String[] addressNames = {"jms.queue.DomibusSendMessageQueue", "jms.queue.DomibusSplitAndJoinQueue"};
        final String[] sendQueueNames = {"jms.queue.DomibusSendMessageQueue1", "jms.queue.DomibusSendMessageQueue2"};
        final String[] splitAndJoinQueueNames = {"jms.queue.DomibusSplitAndJoinQueue1"};

        // GIVEN
        new MockUp<MBeanServerInvocationHandler>() {
            @Mock
            public <T> T newProxyInstance(MBeanServerConnection connection,
                                          ObjectName objectName,
                                          Class<T> interfaceClass,
                                          boolean notificationBroadcaster) {
                if (AddressControl.class.isAssignableFrom(interfaceClass)) {
                    if (objectName.getCanonicalName().contains("address=\"jms.queue.DomibusSendMessageQueue\"")) {
                        return (T) sendAddressControl;
                    } else if (objectName.getCanonicalName().contains("address=\"jms.queue.DomibusSplitAndJoinQueue\"")) {
                        return (T) splitAndJoinAddressControl;
                    } else {
                        throw new IllegalArgumentException("Unknown AddressControl object name: " + objectName);
                    }
                }
                throw new IllegalArgumentException("Unknown proxy interface argument: " + interfaceClass);
            }
        };

        new Expectations(jmsManager) {{
            domibusPropertyProvider.getProperty(ACTIVE_MQ_ARTEMIS_BROKER);
            result = "localhost";
            activeMQServerControl.getAddressNames();
            result = addressNames;

            sendAddressControl.getQueueNames();
            result = sendQueueNames;
            jmsManager.getAddressQueueMap("jms.queue.DomibusSendMessageQueue", sendQueueNames, RoutingType.ANYCAST, (ObjectNameBuilder) any);
            result = new DomibusJMXException("Error creating object name for address [jms.queue.DomibusSendMessageQueue]", new Exception());

            splitAndJoinAddressControl.getQueueNames();
            result = splitAndJoinQueueNames;
            jmsManager.getAddressQueueMap("jms.queue.DomibusSplitAndJoinQueue", splitAndJoinQueueNames, RoutingType.ANYCAST, (ObjectNameBuilder) any);
            result = Arrays.stream(splitAndJoinQueueNames).collect(Collectors.toMap(Function.identity(),
                    queueName -> getQueueObjectName("jms.queue.DomibusSplitAndJoinQueue", queueName)));
        }};

        // WHEN
        Map<String, ObjectName> queues = jmsManager.getQueueMap(RoutingType.ANYCAST);

        // THEN
        new Verifications() {{
            Assert.assertEquals("Should have built the map of queue object names from the provided addresses",
                    new HashSet(Arrays.asList(
                            "jms.queue.DomibusSplitAndJoinQueue1->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSplitAndJoinQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSplitAndJoinQueue1\"")),
                    queues.entrySet().stream()
                            .map(entry -> entry.getKey() + "->" + entry.getValue().toString())
                            .collect(Collectors.toSet())
            );
        }};
    }

    @Test
    public void testGetQueueMap_Queues() throws Exception {
        // GIVEN
        final Map<String, ObjectName> queueObjectNames = new HashMap<>();
        queueObjectNames.put("queue", ObjectName.getInstance("domain:address=queue"));

        new Expectations(jmsManager) {{
            jmsManager.getQueueMap(RoutingType.ANYCAST);
            result = queueObjectNames;
        }};

        // WHEN
        Map<String, ObjectName> queues = jmsManager.getQueueMap();

        // THEN
        new Verifications() {{
            Assert.assertEquals("Should have returned the correct queue object names", queueObjectNames, queues);
        }};
    }

    @Test
    public void testGetQueueMap_Topics() throws Exception {
        // GIVEN
        final Map<String, ObjectName> topicObjectNames = new HashMap<>();
        topicObjectNames.put("topic", ObjectName.getInstance("domain:address=topic"));

        new Expectations(jmsManager) {{
            jmsManager.getQueueMap(RoutingType.MULTICAST);
            result = topicObjectNames;
        }};

        // WHEN
        Map<String, ObjectName> topics = jmsManager.getTopicMap();

        // THEN
        new Verifications() {{
            Assert.assertEquals("Should have returned the correct topic object names", topicObjectNames, topics);
        }};
    }

    @Test
    public void testGetAddressQueueMap(@Injectable AddressControl sendAddressControl,
                                       @Injectable AddressControl splitAndJoinAddressControl,
                                       @Injectable ObjectNameBuilder objectNameBuilder) throws Exception {
        // GIVEN
        final String addressName = "jms.queue.DomibusSendMessageQueue";
        final String[] queueNames = {"jms.queue.DomibusSendMessageQueue1", "jms.queue.DomibusSendMessageQueue2"};

        new Expectations() {{
            objectNameBuilder.getQueueObjectName(toSimpleString(addressName), toSimpleString("jms.queue.DomibusSendMessageQueue1"), RoutingType.ANYCAST);
            result = getQueueObjectName(addressName, "jms.queue.DomibusSendMessageQueue1");

            objectNameBuilder.getQueueObjectName(toSimpleString(addressName), toSimpleString("jms.queue.DomibusSendMessageQueue2"), RoutingType.ANYCAST);
            result = getQueueObjectName(addressName, "jms.queue.DomibusSendMessageQueue2");
        }};

        // WHEN
        Map<String, ObjectName> queues = jmsManager.getAddressQueueMap(addressName, queueNames, RoutingType.ANYCAST, objectNameBuilder);

        // THEN
        new Verifications() {{
            Assert.assertEquals("Should have built the map of queue object names for the provided address",
                    new HashSet(Arrays.asList(
                            "jms.queue.DomibusSendMessageQueue1->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSendMessageQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSendMessageQueue1\"",
                            "DomibusSendMessageQueue1->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSendMessageQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSendMessageQueue1\"",
                            "jms.queue.DomibusSendMessageQueue2->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSendMessageQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSendMessageQueue2\"",
                            "DomibusSendMessageQueue2->org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\"jms.queue.DomibusSendMessageQueue\",subcomponent=queues,routing-type=\"anycast\",queue=\"jms.queue.DomibusSendMessageQueue2\"")),
                    queues.entrySet().stream()
                            .map(entry -> entry.getKey() + "->" + entry.getValue().toString())
                            .collect(Collectors.toSet())
            );
            Assert.assertSame("Should have created entries for both the fully qualified and the non qualified queue names (first queue)",
                    queues.get("jms.queue.DomibusSendMessageQueue1"), queues.get("DomibusSendMessageQueue1"));
            Assert.assertSame("Should have created entries for both the fully qualified and the non qualified queue names (first queue)",
                    queues.get("jms.queue.DomibusSendMessageQueue2"), queues.get("DomibusSendMessageQueue2"));
        }};
    }

    @Test(expected = DomibusJMXException.class)
    public void testGetAddressQueueMap_throwsException(@Injectable AddressControl sendAddressControl,
                                                       @Injectable AddressControl splitAndJoinAddressControl,
                                                       @Injectable ObjectNameBuilder objectNameBuilder) throws Exception {
        // GIVEN
        final String addressName = "jms.queue.DomibusSendMessageQueue";
        final String[] queueNames = {"jms.queue.DomibusSendMessageQueue1", "jms.queue.DomibusSendMessageQueue2"};

        new Expectations() {{
            objectNameBuilder.getQueueObjectName(toSimpleString(addressName), toSimpleString("jms.queue.DomibusSendMessageQueue1"), RoutingType.ANYCAST);
            result = getQueueObjectName(addressName, "jms.queue.DomibusSendMessageQueue1");

            objectNameBuilder.getQueueObjectName(toSimpleString(addressName), toSimpleString("jms.queue.DomibusSendMessageQueue2"), RoutingType.ANYCAST);
            result = new Exception();
        }};

        // WHEN
        jmsManager.getAddressQueueMap(addressName, queueNames, RoutingType.ANYCAST, objectNameBuilder);
    }

    private ObjectName getQueueObjectName(String addressName, String queueName) throws RuntimeException {
        try {
            return ObjectName.getInstance("org.apache.activemq.artemis:broker=\"localhost\",component=addresses,address=\""
                    + addressName + "\",subcomponent=queues,routing-type=\"anycast\",queue=\"" + queueName + "\"");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
}