package eu.domibus.test;

import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

/**
 * </p>Test configuration for ActiveMQ.</p>
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
@Configuration
public class DomibusTestActiveMQConfiguration {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusTestActiveMQConfiguration.class);

    @Value("${activeMQ.embedded.configurationFile}")
    private Resource activeMQConfiguration;

    @Primary
    @Bean(name = "brokerFactory")
    public BrokerFactoryBean activeMQBroker() {
        LOG.debug("Creating the embedded test Active MQ broker from [{}]", activeMQConfiguration);
        return new TestDomibusBrokerFactoryBean(activeMQConfiguration);
    }

}