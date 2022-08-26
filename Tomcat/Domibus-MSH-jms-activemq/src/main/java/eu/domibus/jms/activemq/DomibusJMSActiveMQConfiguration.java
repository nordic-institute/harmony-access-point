package eu.domibus.jms.activemq;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.DomibusJMSConstants;
import eu.domibus.jms.spi.helper.PriorityJmsTemplate;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.access.MBeanProxyFactoryBean;
import org.springframework.jmx.support.MBeanServerConnectionFactoryBean;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.net.MalformedURLException;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_CONNECTION_FACTORY_MAX_POOL_SIZE;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusJMSActiveMQConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusJMSActiveMQConfiguration.class);

    public static final String MQ_BROKER_NAME = "org.apache.activemq:type=Broker,brokerName=";

    public static final String MQ_CONNECTION_FACTORY = "jmsConnectionFactory";

    @Bean(DomibusJMSConstants.DOMIBUS_JMS_CACHING_CONNECTION_FACTORY)
    public ConnectionFactory cachingConnectionFactory(@Qualifier(DomibusJMSConstants.DOMIBUS_JMS_CONNECTION_FACTORY) ConnectionFactory activemqConnectionFactory,
                                               DomibusPropertyProvider domibusPropertyProvider) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        Integer sessionCacheSize = domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_CONNECTION_FACTORY_SESSION_CACHE_SIZE);
        LOGGER.debug("Using session cache size for connection factory [{}]", sessionCacheSize);
        cachingConnectionFactory.setSessionCacheSize(sessionCacheSize);
        cachingConnectionFactory.setTargetConnectionFactory(activemqConnectionFactory);
        cachingConnectionFactory.setCacheConsumers(false);

        return cachingConnectionFactory;
    }

    @DependsOn("brokerFactory")
    @Bean(value = DomibusJMSConstants.DOMIBUS_JMS_CONNECTION_FACTORY)
    public ConnectionFactory connectionFactory(@Qualifier(MQ_CONNECTION_FACTORY) ActiveMQConnectionFactory activeMQConnectionFactory,
                                               DomibusPropertyProvider domibusPropertyProvider) {
        int maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_JMS_CONNECTION_FACTORY_MAX_POOL_SIZE);
        LOGGER.debug("Configured property [{}] with [{}]", DOMIBUS_JMS_CONNECTION_FACTORY_MAX_POOL_SIZE, maxPoolSize);

        activeMQConnectionFactory.setMaxThreadPoolSize(maxPoolSize);
        return activeMQConnectionFactory;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public MBeanServerConnection mBeanServerConnections(String serviceUrl) throws IOException {
        MBeanServerConnectionFactoryBean result = new MBeanServerConnectionFactoryBean();
        result.setServiceUrl(serviceUrl);
        result.setConnectOnStartup(false);
        result.afterPropertiesSet();
        return result.getObject();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public BrokerViewMBean mBeanProxyFactoryBeans(MBeanServerConnection server, String brokerName) throws MalformedObjectNameException {
        MBeanProxyFactoryBean result = new MBeanProxyFactoryBean();
        result.setObjectName(MQ_BROKER_NAME + brokerName);
        result.setProxyInterface(BrokerViewMBean.class);
        result.setServer(server);
        result.afterPropertiesSet();
        return (BrokerViewMBean) result.getObject();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DomibusJMSActiveMQBroker domibusJMSActiveMQBroker(String brokerDetails, MBeanServerConnection mBeanServerConnection, BrokerViewMBean brokerViewMBean) {
        return new DomibusJMSActiveMQBroker(brokerDetails, mBeanServerConnection, brokerViewMBean);
    }

    @Bean("jmsSender")
    public JmsTemplate jmsSender(@Qualifier(DomibusJMSConstants.DOMIBUS_JMS_CACHING_CONNECTION_FACTORY) ConnectionFactory connectionFactory) {
        PriorityJmsTemplate result = new PriorityJmsTemplate();
        result.setSessionTransacted(true);
        result.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        result.setConnectionFactory(connectionFactory);
        return result;
    }
}