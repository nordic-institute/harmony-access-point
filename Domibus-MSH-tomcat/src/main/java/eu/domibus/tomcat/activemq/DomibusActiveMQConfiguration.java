package eu.domibus.tomcat.activemq;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.tomcat.activemq.condition.DummyEmbeddedActiveMQCondition;
import eu.domibus.tomcat.activemq.condition.EmbeddedActiveMQCondition;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.spring.ActiveMQXAConnectionFactory;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Configuration
public class DomibusActiveMQConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusActiveMQConfiguration.class);

    @Value("${activeMQ.embedded.configurationFile}")
    Resource activeMQConfiguration;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Bean(name = "broker")
    @Conditional(DummyEmbeddedActiveMQCondition.class)
    public Object dummyActiveMQBroker() {
        LOGGER.debug("Creating a dummy bean to satisfy the depends-on dependencies");
        return new Object();
    }

    @Bean(name = "broker")
    @Conditional(EmbeddedActiveMQCondition.class)
    public BrokerFactoryBean activeMQBroker() {
        LOGGER.debug("Creating the embedded Active MQ broker from [{}]", activeMQConfiguration);
        final DomibusBrokerFactoryBean brokerFactoryBean = new DomibusBrokerFactoryBean();
        brokerFactoryBean.setConfig(activeMQConfiguration);
        return brokerFactoryBean;
    }

    @Bean(name = "xaJmsConnectionFactory")
    public ActiveMQXAConnectionFactory activeMQXAConnectionFactory() {
        ActiveMQXAConnectionFactory result = new ActiveMQXAConnectionFactory();

        final String brokerURL = domibusPropertyProvider.getProperty("activeMQ.transportConnector.uri");
        result.setBrokerURL(brokerURL);
        LOGGER.debug("Using ActiveMQ brokerURL [{}]", brokerURL);

        final String userName = domibusPropertyProvider.getProperty("activeMQ.username");
        result.setUserName(userName);
        LOGGER.debug("Using ActiveMQ userName [{}]", userName);

        final String password = domibusPropertyProvider.getProperty("activeMQ.password");
        result.setPassword(password);

        final Integer closeTimeout = domibusPropertyProvider.getIntegerProperty("activeMQ.connection.closeTimeout");
        result.setCloseTimeout(closeTimeout);
        LOGGER.debug("Using ActiveMQ closeTimeout [{}]", closeTimeout);

        final Integer responseTimeout = domibusPropertyProvider.getIntegerProperty("activeMQ.connection.connectResponseTimeout");
        result.setConnectResponseTimeout(responseTimeout);
        LOGGER.debug("Using ActiveMQ responseTimeout [{}]", responseTimeout);

        final RedeliveryPolicy defaultRedeliveryPolicy = new RedeliveryPolicy();
        defaultRedeliveryPolicy.setMaximumRedeliveries(0);
        result.getRedeliveryPolicyMap().setDefaultEntry(defaultRedeliveryPolicy);

        return result;
    }

}
