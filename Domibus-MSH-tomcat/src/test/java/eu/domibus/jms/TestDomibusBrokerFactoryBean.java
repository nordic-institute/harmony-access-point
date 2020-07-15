package eu.domibus.jms;

import java.util.List;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.tomcat.activemq.DomibusBrokerFactoryBean;

/**
 * <p>Test specific broker factory bean required to allow broker plugin registration needed in the tests.</p><br />
 *
 * <p>It delays automatic startup of the broker until the factory object is created. When creating the factory object, it
 * registers any existing broker plugins and then manually starts the broker.</p>
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
public class TestDomibusBrokerFactoryBean extends DomibusBrokerFactoryBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestDomibusBrokerFactoryBean.class);

    @Autowired(required = false)
    private List<BrokerPlugin> brokerPlugins;

    public TestDomibusBrokerFactoryBean(Resource config) {
        super(config);

        // Prevent auto-start, we're going to start the embedded broker later
        BrokerFactory.setStartDefault(false);
        setStart(false);
    }

    @Override
    public Object getObject() throws Exception {
        BrokerService broker = getBroker();

        if (brokerPlugins != null) {
            LOG.debug("Configure extra plugins for the embedded ActiveMQ broker: {}", brokerPlugins);
            brokerPlugins.forEach(brokerPlugin -> broker.setPlugins(ArrayUtils.add(broker.getPlugins(), brokerPlugin)));
        }

        LOG.info("Start the embedded ActiveMQ broker");
        broker.start();

        return super.getObject();
    }
}