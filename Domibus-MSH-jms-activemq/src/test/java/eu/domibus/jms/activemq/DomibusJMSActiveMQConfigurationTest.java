package eu.domibus.jms.activemq;

import com.atomikos.jms.AtomikosConnectionFactoryBean;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.spring.ActiveMQXAConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.access.MBeanProxyFactoryBean;
import org.springframework.jmx.support.MBeanServerConnectionFactoryBean;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import java.net.MalformedURLException;

import static eu.domibus.jms.activemq.DomibusJMSActiveMQConfiguration.MQ_BROKER_NAME;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomibusJMSActiveMQConfigurationTest {

    @Tested
    DomibusJMSActiveMQConfiguration domibusJMSActiveMQConfiguration;

    @Test
    public void atomikosConnectionFactoryBean(@Injectable ActiveMQXAConnectionFactory activeMQXAConnectionFactory,
                                              @Injectable DomibusPropertyProvider domibusPropertyProvider,
                                              @Mocked AtomikosConnectionFactoryBean atomikosConnectionFactoryBean) {

        int maxPoolSize = 20;

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_XACONNECTION_FACTORY_MAX_POOL_SIZE);
            this.result = maxPoolSize;
        }};

        domibusJMSActiveMQConfiguration.atomikosConnectionFactoryBean(activeMQXAConnectionFactory, domibusPropertyProvider);

        new Verifications() {{
            atomikosConnectionFactoryBean.setUniqueResourceName("domibusJMS-XA");
            atomikosConnectionFactoryBean.setXaConnectionFactory(activeMQXAConnectionFactory);
            atomikosConnectionFactoryBean.setMaxPoolSize(maxPoolSize);
        }};
    }

    @Test
    public void mBeanServerConnectionFactoryBean(@Injectable DomibusPropertyProvider domibusPropertyProvider,
                                                 @Mocked MBeanServerConnectionFactoryBean mBeanServerConnectionFactoryBean) throws MalformedURLException {
        String activeMQURL = "service:jmx:rmi:///jndi/rmi://localhost:123/jmxrmi";

        new Expectations() {{
            domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_JMXURL);
            this.result = activeMQURL;
        }};

        domibusJMSActiveMQConfiguration.mBeanServerConnectionFactoryBean(domibusPropertyProvider);

        new Verifications() {{
            mBeanServerConnectionFactoryBean.setServiceUrl(activeMQURL);
            mBeanServerConnectionFactoryBean.setConnectOnStartup(false);
        }};
    }

    @Test
    public void mBeanProxyFactoryBean(@Injectable MBeanServerConnection mBeanServerConnection,
                                      @Injectable DomibusPropertyProvider domibusPropertyProvider,
                                      @Mocked MBeanProxyFactoryBean mBeanProxyFactoryBean) throws MalformedObjectNameException {
        String activeMQBrokerName = "localhost";
        String objectName = MQ_BROKER_NAME + activeMQBrokerName;

        new Expectations() {{
            domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_BROKER_NAME);
            this.result = activeMQBrokerName;
        }};

        domibusJMSActiveMQConfiguration.mBeanProxyFactoryBean(mBeanServerConnection, domibusPropertyProvider);

        new Verifications() {{
            mBeanProxyFactoryBean.setObjectName(objectName);
            mBeanProxyFactoryBean.setProxyInterface(BrokerViewMBean.class);
            mBeanProxyFactoryBean.setServer(mBeanServerConnection);
        }};
    }

    @Test
    public void jmsSender(@Injectable ConnectionFactory connectionFactory,
                          @Mocked JmsTemplate jmsTemplate) {
        domibusJMSActiveMQConfiguration.jmsSender(connectionFactory);

        new Verifications() {{
            jmsTemplate.setSessionTransacted(true);
            jmsTemplate.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
            jmsTemplate.setConnectionFactory(connectionFactory);
        }};
    }
}