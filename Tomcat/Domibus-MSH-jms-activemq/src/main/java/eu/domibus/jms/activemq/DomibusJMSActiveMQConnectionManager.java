package eu.domibus.jms.activemq;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.DomibusJMSException;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_BROKER_NAME;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_JMXURL;

/**
 * Connection manager that handles reconnections in case of a Master-Slave ActiveMQ cluster.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.1
 */
@Service
public class DomibusJMSActiveMQConnectionManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusJMSActiveMQConnectionManager.class);

    private final List<DomibusJMSActiveMQBroker> brokerCluster = new ArrayList<>();

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final ObjectProvider<MBeanServerConnection> mBeanServerConnections;

    private final ObjectProvider<BrokerViewMBean> mBeanProxyFactoryBeans;

    private final ObjectProvider<DomibusJMSActiveMQBroker> domibusJMSActiveMQBrokers;

    public DomibusJMSActiveMQConnectionManager(DomibusPropertyProvider domibusPropertyProvider,
                                               ObjectProvider<MBeanServerConnection> mBeanServerConnections,
                                               ObjectProvider<BrokerViewMBean> mBeanProxyFactoryBeans,
                                               ObjectProvider<DomibusJMSActiveMQBroker> domibusJMSActiveMQBrokers) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.mBeanServerConnections = mBeanServerConnections;
        this.mBeanProxyFactoryBeans = mBeanProxyFactoryBeans;
        this.domibusJMSActiveMQBrokers = domibusJMSActiveMQBrokers;
    }

    @PostConstruct
    public void init() {
        List<String> serviceUrls = domibusPropertyProvider.getCommaSeparatedPropertyValues(ACTIVE_MQ_JMXURL);
        LOG.info("Configured serviceUrls {}", serviceUrls);

        List<String> brokerNames = domibusPropertyProvider.getCommaSeparatedPropertyValues(ACTIVE_MQ_BROKER_NAME);
        LOG.info("Configured brokerNames {}", brokerNames);

        if (brokerNames.isEmpty()) {
            LOG.debug("At least one ActiveMQ broker configuration is required");
            throw new DomibusPropertyException("At least one ActiveMQ broker configuration is required");
        }

        if (serviceUrls.size() != brokerNames.size()) {
            LOG.debug("The number of ActiveMQ service URLs [{}] is different than the number of broker names [{}]", serviceUrls.size(), brokerNames.size());
            throw new DomibusPropertyException("The number of ActiveMQ service URLs is different than the number of broker names");
        }

        IntStream.range(0, brokerNames.size())
                .forEach(index -> {
                    String serviceUrl = serviceUrls.get(index);
                    String brokerName = brokerNames.get(index);

                    LOG.trace("Creating an ActiveMQ broker using the service URL [{}] and the broker name [{}]", serviceUrl, brokerName);

                    MBeanServerConnection mBeanServerConnection = mBeanServerConnections.getObject(serviceUrl);
                    BrokerViewMBean brokerViewMBean = mBeanProxyFactoryBeans.getObject(mBeanServerConnection, brokerName);
                    brokerCluster.add(domibusJMSActiveMQBrokers.getObject(brokerName + "@" + serviceUrl, mBeanServerConnection, brokerViewMBean));
                });
    }

    public Map<String, ObjectName> getQueueMap() {
        return getMasterDomibusActiveMQBroker().getQueueMap();
    }

    public QueueViewMBean getQueueViewMBean(ObjectName name) {
        return getMasterDomibusActiveMQBroker().getQueueViewMBean(name);
    }

    public QueueViewMBean getQueueViewMBean(String name) {
        return getMasterDomibusActiveMQBroker().getQueueViewMBean(name);
    }

    public DomibusJMSActiveMQBroker getMasterDomibusActiveMQBroker() {
        if (brokerCluster.isEmpty()) {
            LOG.debug("At least one ActiveMQ broker instance is required");
            throw new DomibusJMSException(DomibusCoreErrorCode.DOM_001, "At least one ActiveMQ broker instance is required");
        }

        Optional<DomibusJMSActiveMQBroker> domibusActiveMQBroker = brokerCluster.stream()
                    // the isSingleBroker() call below is used to prevent a JMX call on the master broker
                    // when there is only one broker instance configured in the cluster
                    .filter(currentBroker -> isSingleBroker() || isMasterSafely(currentBroker))
                    .findFirst();

        return domibusActiveMQBroker.orElseThrow(() -> {
            LOG.debug("No master ActiveMQ broker available at this time");
            return new DomibusJMSException(DomibusCoreErrorCode.DOM_001, "No master ActiveMQ broker available");
        });
    }

    private boolean isMasterSafely(DomibusJMSActiveMQBroker currentBroker) {
        boolean master = false;
        try {
            master = currentBroker.isMaster();
        } catch(Throwable e) { // NOSONAR: org.springframework.jmx.access.MBeanClientInterceptor.doInvoke(MethodInvocation) throws Throwable
            LOG.warn("Treating the current broker [{}] as slave because it is not reachable" , currentBroker.getBrokerDetails(), e);
        }
        return master;
    }

    /*
     * Returns {@code true} if there is only one ActiveMQ broker instance configured in the cluster; otherwise, {@code false}.
     */
    private boolean isSingleBroker() {
        return brokerCluster.size() == 1;
    }
}