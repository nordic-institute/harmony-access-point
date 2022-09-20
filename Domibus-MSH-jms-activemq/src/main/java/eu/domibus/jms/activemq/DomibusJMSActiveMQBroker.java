package eu.domibus.jms.activemq;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.springframework.beans.factory.ObjectProvider;

import javax.annotation.PostConstruct;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

/**
 * A Domibus ActiveMQ broker that could also participate in a master-slave setup.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0.1
 */
public class DomibusJMSActiveMQBroker {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusJMSActiveMQBroker.class);

    private final String brokerName;

    private final String serviceUrl;

    private final ObjectProvider<MBeanServerConnection> mBeanServerConnections;

    /**
     * Broker view MBean provider for the case when a broker needs to be refreshed.
     */
    private final ObjectProvider<BrokerViewMBean> brokerViewMBeans;

    private BrokerViewMBean brokerViewMBean;

    private MBeanServerConnection mBeanServerConnection;

    private final Map<String, ObjectName> queueMap = new HashMap<>();

    public DomibusJMSActiveMQBroker(String brokerName, String serviceUrl, ObjectProvider<MBeanServerConnection> mBeanServerConnections, ObjectProvider<BrokerViewMBean> brokerViewMBeans) {
        this.brokerName = brokerName;
        this.serviceUrl = serviceUrl;
        this.mBeanServerConnections = mBeanServerConnections;
        this.brokerViewMBeans = brokerViewMBeans;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    public boolean isMaster() {
        boolean slave = brokerViewMBean.isSlave();
        LOG.debug("Broker [{}] is {}currently in master mode", getBrokerDetails(), slave ? "not " : "");
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
        return brokerName + "@" + serviceUrl;
    }

    public void refresh() {
        LOG.trace("Refreshing broker view MBean [{}]", getBrokerDetails());
        mBeanServerConnection = mBeanServerConnections.getObject(serviceUrl);
        brokerViewMBean = brokerViewMBeans.getObject(brokerName, mBeanServerConnection);
        queueMap.clear();
    }
}