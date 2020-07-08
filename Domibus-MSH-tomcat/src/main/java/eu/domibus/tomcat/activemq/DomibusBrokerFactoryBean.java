package eu.domibus.tomcat.activemq;

import java.util.List;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.jms.spi.InternalJMSManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * This factory bean overrides getObject method just for setting MaxBrowsePageSize for all JMS queues
 *
 * @author Tiago Miguel
 * @author Sebastian-Ion TINCU
 * @since 3.3.2
 */
public class DomibusBrokerFactoryBean extends BrokerFactoryBean {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusBrokerFactoryBean.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired(required = false)
    private List<BrokerPlugin> brokerPlugins;

    public DomibusBrokerFactoryBean(Resource config) {
        super(config);

        // Prevent auto-start, we're going to start the embedded broker later
        BrokerFactory.setStartDefault(false);
        setStart(false);
    }

    @Override
    public Object getObject() throws Exception {
        BrokerService broker = getBroker();

        if(brokerPlugins != null) {
            LOGGER.debug("Configure extra plugins for the embedded ActiveMQ broker: {}", brokerPlugins);
            brokerPlugins.forEach(brokerPlugin -> broker.setPlugins(ArrayUtils.add(broker.getPlugins(), brokerPlugin)));
        }

        LOGGER.info("Start the embedded ActiveMQ broker");
        broker.start();

        final int maxBrowsePageSize = NumberUtils.toInt(domibusPropertyProvider.getProperty(InternalJMSManager.PROP_MAX_BROWSE_SIZE));
        if (maxBrowsePageSize <= 0) {
            return broker;
        }

        ActiveMQDestination[] destinations = broker.getDestinations();
        for (ActiveMQDestination activeMQDestination : destinations) {
            final Destination destination = broker.getDestination(activeMQDestination);
            if (destination != null) {
                destination.setMaxBrowsePageSize(maxBrowsePageSize);
                LOGGER.debug("MaxBrowsePageSize was set to [{}] in [{}]", maxBrowsePageSize, activeMQDestination);
            }
        }
        return broker;
    }
}
