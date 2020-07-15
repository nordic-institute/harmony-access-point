package eu.domibus;

import org.apache.activemq.xbean.BrokerFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import eu.domibus.jms.TestDomibusBrokerFactoryBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.tomcat.activemq.condition.EmbeddedActiveMQCondition;

/**
 * </p>Test configuration for ActiveMQ.</p>
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
@Configuration
public class DomibusTestActiveMQConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusTestActiveMQConfiguration.class);

    @Value("${activeMQ.embedded.configurationFile}")
    private Resource activeMQConfiguration;

    @Primary
    @Bean(name = "brokerFactory")
    @Conditional(EmbeddedActiveMQCondition.class)
    public BrokerFactoryBean activeMQBroker() {
        LOG.debug("Creating the embedded test Active MQ broker from [{}]", activeMQConfiguration);
        return new TestDomibusBrokerFactoryBean(activeMQConfiguration);
    }

}