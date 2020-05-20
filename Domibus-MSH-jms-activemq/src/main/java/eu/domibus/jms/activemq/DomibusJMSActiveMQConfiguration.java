package eu.domibus.jms.activemq;

import com.atomikos.jms.AtomikosConnectionFactoryBean;
import eu.domibus.api.jms.JMSConstants;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.spring.ActiveMQXAConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.access.MBeanProxyFactoryBean;
import org.springframework.jmx.support.MBeanServerConnectionFactoryBean;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import java.net.MalformedURLException;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusJMSActiveMQConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusJMSActiveMQConfiguration.class);

    public static final String MQ_BROKER_NAME = "org.apache.activemq:type=Broker,brokerName=";

    @DependsOn("broker")
    @Bean(value = "domibusJMS-XAConnectionFactory", initMethod = "init", destroyMethod = "close")
    public AtomikosConnectionFactoryBean atomikosConnectionFactoryBean(@Qualifier("xaJmsConnectionFactory") ActiveMQXAConnectionFactory activeMQXAConnectionFactory,
                                                                       DomibusPropertyProvider domibusPropertyProvider) {
        AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
        atomikosConnectionFactoryBean.setUniqueResourceName("domibusJMS-XA");
        atomikosConnectionFactoryBean.setXaConnectionFactory(activeMQXAConnectionFactory);
        int maxPoolSize = domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_XACONNECTION_FACTORY_MAX_POOL_SIZE);
        LOGGER.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_XACONNECTION_FACTORY_MAX_POOL_SIZE, maxPoolSize);

        atomikosConnectionFactoryBean.setMaxPoolSize(maxPoolSize);
        return atomikosConnectionFactoryBean;
    }

    @Bean("mbeanServerConnection")
    public MBeanServerConnectionFactoryBean mBeanServerConnectionFactoryBean(DomibusPropertyProvider domibusPropertyProvider) throws MalformedURLException {
        MBeanServerConnectionFactoryBean result = new MBeanServerConnectionFactoryBean();
        String activeMQJMXURL = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_JMXURL);
        LOGGER.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_JMXURL, activeMQJMXURL);
        result.setServiceUrl(activeMQJMXURL);
        result.setConnectOnStartup(false);

        return result;
    }

    @Bean("brokerViewMBean")
    public MBeanProxyFactoryBean mBeanProxyFactoryBean(@Qualifier("mbeanServerConnection") MBeanServerConnection mBeanServerConnection,
                                                       DomibusPropertyProvider domibusPropertyProvider) throws MalformedObjectNameException {
        MBeanProxyFactoryBean result = new MBeanProxyFactoryBean();

        String activeMQBrokerName = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_BROKER_NAME);
        String objectName = MQ_BROKER_NAME + activeMQBrokerName;
        LOGGER.debug("Configured property [objectName] with [{}]", objectName);

        result.setObjectName(objectName);
        result.setProxyInterface(BrokerViewMBean.class);
        result.setServer(mBeanServerConnection);

        return result;
    }

    @Bean("jmsSender")
    public JmsTemplate jmsSender(@Qualifier(JMSConstants.DOMIBUS_JMS_XACONNECTION_FACTORY) ConnectionFactory connectionFactory) {
        JmsTemplate result = new JmsTemplate();
        result.setSessionTransacted(true);
        result.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        result.setConnectionFactory(connectionFactory);

        return result;
    }


}
