package eu.domibus.jms.activemq;

import eu.domibus.api.jms.DomibusJMSException;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jmx.access.MBeanConnectFailureException;

import javax.management.MBeanServerConnection;
import java.rmi.ConnectException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_BROKER_NAME;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_JMXURL;

@RunWith(JMockit.class)
public class DomibusJMSActiveMQConnectionManagerTest {

    @Tested
    private DomibusJMSActiveMQConnectionManager domibusJMSActiveMQConnectionManager;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private ObjectProvider<MBeanServerConnection> mBeanServerConnections;

    @Injectable
    private ObjectProvider<BrokerViewMBean> mBeanProxyFactoryBeans;

    @Injectable
    private ObjectProvider<DomibusJMSActiveMQBroker> domibusJMSActiveMQBrokers;

    @Before
    public void ignorePostConstructInvocation() {
        new MockUp<DomibusJMSActiveMQConnectionManager>() {
            @Mock
            void init(Invocation invocation) {
                // ignore the first invocation due to @PostConstruct
                if (invocation.getInvocationCount() > 1) {
                    invocation.proceed();
                }
            }
        };
    }

    @Test
    public void init_throwsExceptionWhenNoBrokersConfiguredInsideTheCluster() {
        // GIVEN
        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(ACTIVE_MQ_BROKER_NAME);
            result = Collections.emptyList();
        }};

        // THEN
        DomibusPropertyException exception = Assert.assertThrows(
                "Should have thrown a DomibusPropertyException when no brokers are configured inside the cluster",
                DomibusPropertyException.class,
                // WHEN
                () -> domibusJMSActiveMQConnectionManager.init()
        );
        Assert.assertEquals("Should have thrown an exception specifying no brokers have been configured inside the cluster",
                "At least one ActiveMQ broker configuration is required", exception.getMessage());
        new FullVerifications() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(ACTIVE_MQ_JMXURL);
        }};
    }

    @Test
    public void init_throwsExceptionWhenNumberOfBrokerNamesDifferentThanNumberOfServiceUrls() {
        // GIVEN
        final List<String> serviceNames = Arrays.asList(
                "service:jmx:rmi:///jndi/rmi://broker1:1099/jmxrmi",
                "service:jmx:rmi:///jndi/rmi://broker2:1099/jmxrmi");
        final List<String> brokerNames = Collections.singletonList(
                "broker1");
        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(ACTIVE_MQ_JMXURL);
            result = serviceNames;

            domibusPropertyProvider.getCommaSeparatedPropertyValues(ACTIVE_MQ_BROKER_NAME);
            result = brokerNames;
        }};

        // THEN
        DomibusPropertyException exception = Assert.assertThrows(
                "Should have thrown a DomibusPropertyException when the number of broker names is different than number of service URLs",
                DomibusPropertyException.class,
                // WHEN
                () -> domibusJMSActiveMQConnectionManager.init()
        );
        Assert.assertEquals("Should have thrown an exception specifying number of broker names is different than number of service URLs",
                "The number of ActiveMQ service URLs is different than the number of broker names", exception.getMessage());
        new FullVerifications() { };
    }

    @Test
    public void init_createsClusterOfBrokersForEachBrokerConfiguration(@Injectable MBeanServerConnection serverConnection1,
                                                                       @Injectable MBeanServerConnection serverConnection2,
                                                                       @Injectable BrokerViewMBean brokerView1,
                                                                       @Injectable BrokerViewMBean brokerView2,
                                                                       @Injectable DomibusJMSActiveMQBroker domibusJMSActiveMQBroker1,
                                                                       @Injectable DomibusJMSActiveMQBroker domibusJMSActiveMQBroker2) throws Exception {
        // GIVEN
        final List<String> serviceNames = Arrays.asList(
                "service:jmx:rmi:///jndi/rmi://broker1:1099/jmxrmi",
                "service:jmx:rmi:///jndi/rmi://broker2:1099/jmxrmi");
        final List<String> brokerNames = Arrays.asList(
                "broker1",
                "broker2");
        new Expectations() {{
            domibusPropertyProvider.getCommaSeparatedPropertyValues(ACTIVE_MQ_JMXURL);
            result = serviceNames;

            domibusPropertyProvider.getCommaSeparatedPropertyValues(ACTIVE_MQ_BROKER_NAME);
            result = brokerNames;

            mBeanServerConnections.getObject("service:jmx:rmi:///jndi/rmi://broker1:1099/jmxrmi");
            result = serverConnection1;

            mBeanServerConnections.getObject("service:jmx:rmi:///jndi/rmi://broker2:1099/jmxrmi");
            result = serverConnection2;

            mBeanProxyFactoryBeans.getObject(serverConnection1, "broker1");
            result = brokerView1;

            mBeanProxyFactoryBeans.getObject(serverConnection2, "broker2");
            result = brokerView2;

            domibusJMSActiveMQBrokers.getObject("broker1@service:jmx:rmi:///jndi/rmi://broker1:1099/jmxrmi", serverConnection1, brokerView1);
            result = domibusJMSActiveMQBroker1;

            domibusJMSActiveMQBrokers.getObject("broker2@service:jmx:rmi:///jndi/rmi://broker2:1099/jmxrmi", serverConnection2, brokerView2);
            result = domibusJMSActiveMQBroker2;
        }};

        // WHEN
        domibusJMSActiveMQConnectionManager.init();

        // THEN
        new FullVerifications() {{
            List<DomibusJMSActiveMQBroker> brokerCluster = getDomibusJMSActiveMQBrokers();
            Assert.assertEquals("Should have populated the cluster with the correct number of entries", 2, brokerCluster.size());
            Assert.assertSame("Should have populated the cluster with the first broker", domibusJMSActiveMQBroker1, brokerCluster.get(0));
            Assert.assertSame("Should have populated the cluster with the second broker", domibusJMSActiveMQBroker2, brokerCluster.get(1));
        }};
    }

    @Test
    public void getMasterDomibusActiveMQBroker_throwsExceptionWhenClusterIsEmpty() throws Exception {
        // GIVEN
        List<DomibusJMSActiveMQBroker> brokerCluster = getDomibusJMSActiveMQBrokers();
        Assert.assertTrue("Should have had an empty cluster of brokers", brokerCluster.isEmpty());

        // THEN
        DomibusJMSException exception = Assert.assertThrows(
                "Should have thrown a DomibusJMSException when looking for an active master broker inside an empty cluster of brokers",
                DomibusJMSException.class,
                // WHEN
                () -> domibusJMSActiveMQConnectionManager.getMasterDomibusActiveMQBroker()
        );
        Assert.assertTrue("Should have thrown an exception specifying that at least one broker is required inside the cluster when looking for an active master broker",
                exception.getMessage().contains("At least one ActiveMQ broker instance is required"));
        new FullVerifications() { };
    }

    @Test
    public void getMasterDomibusActiveMQBroker_returnsSingleBrokerInstanceWhenClusterConstainsOnlyOneEntry(
            @Injectable DomibusJMSActiveMQBroker broker) throws Exception {
        // GIVEN
        List<DomibusJMSActiveMQBroker> brokerCluster = getDomibusJMSActiveMQBrokers();
        brokerCluster.add(broker);

        // WHEN
        DomibusJMSActiveMQBroker result = domibusJMSActiveMQConnectionManager.getMasterDomibusActiveMQBroker();

        // THEN
        Assert.assertEquals("Should have returned the single broker instance when the cluster has only one entry",
                broker, result);
    }

    @Test
    public void getMasterDomibusActiveMQBroker_returnsFirstMasterBrokerInstanceWhenClusterConstainsMultipleEntries(
                @Injectable DomibusJMSActiveMQBroker broker1,
                @Injectable DomibusJMSActiveMQBroker broker2) throws Exception {
        // GIVEN
        List<DomibusJMSActiveMQBroker> brokerCluster = getDomibusJMSActiveMQBrokers();
        brokerCluster.addAll(Arrays.asList(broker1, broker2));
        new Expectations() {{
            broker1.isMaster();
            result = false;

            broker2.isMaster();
            result = true;
        }};

        // WHEN
        DomibusJMSActiveMQBroker result = domibusJMSActiveMQConnectionManager.getMasterDomibusActiveMQBroker();

        // THEN
        Assert.assertEquals("Should have returned the first master broker instance when the cluster has multiple entry",
                broker2, result);
    }

    @Test
    public void getMasterDomibusActiveMQBroker_ignoresPossibleMastersBrokerInstancesToWhichTheClientDoesNotHaveIssuesConnectingToWhenClusterConstainsMultipleEntries(
            @Injectable DomibusJMSActiveMQBroker broker1,
            @Injectable DomibusJMSActiveMQBroker broker2) throws Exception {
        // GIVEN
        List<DomibusJMSActiveMQBroker> brokerCluster = getDomibusJMSActiveMQBrokers();
        brokerCluster.addAll(Arrays.asList(broker1, broker2));
        new Expectations() {{
            broker1.getBrokerDetails();
            result = "broker1@service:jmx:rmi:///jndi/rmi://broker1:1099/jmxrmi";

            broker1.isMaster();
            result = new MBeanConnectFailureException("I/O failure during JMX access",
                        new ConnectException("Connection refused to host: 172.K.."));

            broker2.isMaster();
            result = true;
        }};

        // WHEN
        DomibusJMSActiveMQBroker result = domibusJMSActiveMQConnectionManager.getMasterDomibusActiveMQBroker();

        // THEN
        Assert.assertEquals("Should have returned the first master broker instance to which the client doesn't have issues connecting to when the cluster has multiple entry",
                broker2, result);
    }

    @Test
    public void getMasterDomibusActiveMQBroker_throwsExceptionWhenClusterConstainsMultipleEntriesButNoneIsMaster(
            @Injectable DomibusJMSActiveMQBroker broker1,
            @Injectable DomibusJMSActiveMQBroker broker2) throws Exception {
        // GIVEN
        List<DomibusJMSActiveMQBroker> brokerCluster = getDomibusJMSActiveMQBrokers();
        brokerCluster.addAll(Arrays.asList(broker1, broker2));
        new Expectations() {{
            broker1.isMaster();
            result = false;

            broker2.isMaster();
            result = false;
        }};

        // THEN
        DomibusJMSException exception = Assert.assertThrows(
                "Should have thrown a DomibusJMSException when looking for an active master broker inside a cluster of brokers in which none is master",
                DomibusJMSException.class,
                // WHEN
                () -> domibusJMSActiveMQConnectionManager.getMasterDomibusActiveMQBroker()
        );
        Assert.assertTrue("Should have thrown an exception specifying that at no brokers are master inside the cluster",
                exception.getMessage().contains("No master ActiveMQ broker available"));
        new FullVerifications() { };
    }

    private List<DomibusJMSActiveMQBroker> getDomibusJMSActiveMQBrokers() throws IllegalAccessException {
        return (List<DomibusJMSActiveMQBroker>) FieldUtils.readField(domibusJMSActiveMQConnectionManager, "brokerCluster", true);
    }
}