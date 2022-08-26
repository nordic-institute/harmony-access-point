package eu.domibus.jms.activemq;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A Domibus ActiveMQ broker that could also participate in a master-slave setup.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.1
 */
public class DomibusJMSActiveMQBroker {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusJMSActiveMQBroker.class);

    private final String brokerDetails;

    private final MBeanServerConnection mBeanServerConnection;

    private final BrokerViewMBean brokerViewMBean;

    private final Map<String, ObjectName> queueMap = new HashMap<>();

    public DomibusJMSActiveMQBroker(String brokerDetails, MBeanServerConnection mBeanServerConnection, BrokerViewMBean brokerViewMBean) {
        this.brokerDetails = brokerDetails;
        this.mBeanServerConnection = mBeanServerConnection;
        this.brokerViewMBean = brokerViewMBean;
    }

    public boolean isMaster() {
        boolean slave = brokerViewMBean.isSlave();
        LOG.debug("Broker [{}] is {}currently in master mode", brokerDetails, slave ? "not " : "");
        return !slave;
    }

    public Map<String, ObjectName> getQueueMap() {
        if (queueMap.isEmpty()) {
            LOG.trace("Initialize queueMap using JMX");
            for (ObjectName name : brokerViewMBean.getQueues()) {
                QueueViewMBean queueMbean = getQueueViewMBean(name);
                queueMap.put(queueMbean.getName(), name);
            }
        }

        LOG.trace("queueMap [{}]", queueMap);
        return queueMap;
    }

    public QueueViewMBean getQueueViewMBean(ObjectName objectName) {
        return MBeanServerInvocationHandler.newProxyInstance(mBeanServerConnection, objectName, QueueViewMBean.class, true);
    }

    public QueueViewMBean getQueueViewMBean(String name) {
        ObjectName objectName = getQueueMap().get(name);
        return getQueueViewMBean(objectName);
    }

    public String getBrokerDetails() {
        return brokerDetails;
    }
}
