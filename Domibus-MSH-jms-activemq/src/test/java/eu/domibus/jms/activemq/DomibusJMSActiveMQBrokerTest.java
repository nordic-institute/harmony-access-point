package eu.domibus.jms.activemq;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import java.util.Map;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.0.1
 */
@RunWith(JMockit.class)
public class DomibusJMSActiveMQBrokerTest {

    @Tested
    private DomibusJMSActiveMQBroker domibusJMSActiveMQBroker;

    @Injectable
    private ObjectProvider<BrokerViewMBean> brokerViewMBeans;

    @Injectable
    private ObjectProvider<MBeanServerConnection> mBeanServerConnections;

    @Injectable
    private BrokerViewMBean brokerViewMBean;

    @Injectable
    private MBeanServerConnection mBeanServerConnection;

    @Injectable
    private String brokerName = "broker";

    @Injectable
    private String serviceUrl = "service:jmx:rmi:///jndi/rmi://localhost:123/jmxrmi";

    @Before
    public void ignorePostConstructInvocation() {
        new MockUp<DomibusJMSActiveMQBroker>() {
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
    public void isMaster_returnsTrueWhenBrokerViewMBeanReturnsTheSlaveFlagAsFalse() {
        // GIVEN
        new Expectations() {{
            brokerViewMBean.isSlave();
            result = false;
        }};

        // WHEN
        boolean master = domibusJMSActiveMQBroker.isMaster();

        // THEN
        Assert.assertTrue("Should have seen this broker as true when the broker view MBean doesn't see it as a slave", master);
    }

    @Test
    public void isMaster_returnsFalseWhenBrokerViewMBeanReturnsTheSlaveFlagAsTrue() {
        // GIVEN
        new Expectations() {{
            brokerViewMBean.isSlave();
            result = true;
        }};

        // WHEN
        boolean master = domibusJMSActiveMQBroker.isMaster();

        // THEN
        Assert.assertFalse("Should have seen this broker as false when the broker view MBean sees it as a slave", master);
    }

    @Test
    public void getQueueViewMBean_retrieveByQueueName(@Injectable QueueViewMBean queueViewMBean, @Injectable Map<String, ObjectName> queueMap) {
        // GIVEN
        final String queueName = "queueName";
        new MockUp<DomibusJMSActiveMQBroker>() {
            @Mock
            QueueViewMBean getQueueViewMBean(ObjectName objectName) {
                return queueViewMBean;
            }

            @Mock
            Map<String, ObjectName> getQueueMap() {
                return queueMap;
            }
        };
        new Expectations() {{
            queueMap.get(queueName);
        }};

        // WHEN
        QueueViewMBean result = domibusJMSActiveMQBroker.getQueueViewMBean(queueName);

        // THEN
        Assert.assertSame("Should have returned the correct MBean from the queue map when retrieving it by its queue name", queueViewMBean, result);
    }

    @Test
    public void getQueueViewMBean_retrieveByObjectName(@Injectable QueueViewMBean queueViewMBean, @Injectable ObjectName objectName) {
        // GIVEN
        new Expectations(MBeanServerInvocationHandler.class) {{
            MBeanServerInvocationHandler.newProxyInstance(mBeanServerConnection, objectName, QueueViewMBean.class, true);
            result = queueViewMBean;
        }};

        // WHEN
        QueueViewMBean result = domibusJMSActiveMQBroker.getQueueViewMBean(objectName);

        // THEN
        Assert.assertSame("Should have returned the correct MBean from the queue map when retrieving it by its object name", queueViewMBean, result);
    }

    @Test
    public void getQueueMap_returnsExistingQueueMapWhenAlreadyPopulated(@Injectable ObjectName objectName) throws Exception {
        // GIVEN
        Map<String, ObjectName> queueMap = getQueueMap();
        queueMap.put("queueName", objectName);
        Assert.assertFalse("Should have had the queue map already populated", queueMap.isEmpty());

        // WHEN
        domibusJMSActiveMQBroker.getQueueMap();

        // THEN
        new Verifications() {{
            brokerViewMBean.getQueues();
            times = 0;
        }};
    }

    @Test
    public void getQueueMap_lazyInitializesQueueMap(@Injectable ObjectName objectName,
                                                    @Injectable QueueViewMBean queueViewMBean) throws Exception {
        // GIVEN
        String queueName = "queueName";
        Map<String, ObjectName> queueMap = getQueueMap();
        Assert.assertTrue("Should have had the queue map not already populated", queueMap.isEmpty());
        new MockUp<DomibusJMSActiveMQBroker>() {
            @Mock
            QueueViewMBean getQueueViewMBean(ObjectName objectName) {
                return queueViewMBean;
            }
        };
        new Expectations() {{
            brokerViewMBean.getQueues();
            result = new ObjectName[] { objectName };

            queueViewMBean.getName();
            result = queueName;
        }};

        // WHEN
        Map<String, ObjectName> result = domibusJMSActiveMQBroker.getQueueMap();

        // THEN
        new FullVerifications() {{
            Assert.assertSame("Should have returned the same queue map", queueMap, result);
            Assert.assertSame("Should have populated the queue map", objectName, result.get(queueName));
        }};
    }

    @Test
    public void refresh(@Injectable ObjectName objectName) throws Exception {
        // GIVEN
        Map<String, ObjectName> queueMap = getQueueMap();
        queueMap.put("queueViewMBeanName", objectName);
        Assert.assertFalse("Should have correctly initialized the queue map", queueMap.isEmpty());
        new Expectations() {{
            mBeanServerConnections.getObject(serviceUrl);
            result = mBeanServerConnection;

            brokerViewMBeans.getObject(brokerName, mBeanServerConnection);
            result = brokerViewMBean;
        }};

        // WHEN
        domibusJMSActiveMQBroker.refresh();

        // THEN
        Assert.assertSame("Should have refreshed the broker view MBean", brokerViewMBean, getBrokerViewMBean());
        Assert.assertSame("Should have refreshed the server connection MBean", mBeanServerConnection, getMBeanServerConnection());
        Assert.assertTrue("Should have had the queue map cleared", queueMap.isEmpty());
    }

    private Map<String, ObjectName> getQueueMap() throws IllegalAccessException {
        return (Map<String, ObjectName>) FieldUtils.readField(domibusJMSActiveMQBroker, "queueMap", true);
    }

    private BrokerViewMBean getBrokerViewMBean() throws IllegalAccessException {
        return (BrokerViewMBean) FieldUtils.readField(domibusJMSActiveMQBroker, "brokerViewMBean", true);
    }

    private MBeanServerConnection getMBeanServerConnection() throws IllegalAccessException {
        return (MBeanServerConnection) FieldUtils.readField(domibusJMSActiveMQBroker, "mBeanServerConnection", true);
    }

}