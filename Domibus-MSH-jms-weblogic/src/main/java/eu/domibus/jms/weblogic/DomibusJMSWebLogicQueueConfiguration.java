package eu.domibus.jms.weblogic;

import eu.domibus.common.JMSConstants;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusJMSWebLogicQueueConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusJMSWebLogicQueueConfiguration.class);

    @Bean(JMSConstants.DOMIBUS_JMS_CACHING_XACONNECTION_FACTORY)
    public ConnectionFactory cachingConnectionFactory(@Qualifier(JMSConstants.DOMIBUS_JMS_XACONNECTION_FACTORY) ConnectionFactory weblogicConnectionFactory,
                                               DomibusPropertyProvider domibusPropertyProvider) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        Integer sessionCacheSize = domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_CONNECTION_FACTORY_SESSION_CACHE_SIZE);
        LOGGER.debug("Using session cache size for connection factory [{}]", sessionCacheSize);
        cachingConnectionFactory.setSessionCacheSize(sessionCacheSize);
        cachingConnectionFactory.setTargetConnectionFactory(weblogicConnectionFactory);
        cachingConnectionFactory.setCacheConsumers(false);

        return cachingConnectionFactory;
    }

    @Bean(JMSConstants.DOMIBUS_JMS_XACONNECTION_FACTORY)
    public JndiObjectFactoryBean connectionFactory() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/ConnectionFactory");
        jndiObjectFactoryBean.setLookupOnStartup(false);
        jndiObjectFactoryBean.setExpectedType(ConnectionFactory.class);
        return jndiObjectFactoryBean;
    }

    @Bean("sendMessageQueue")
    public JndiObjectFactoryBean sendMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.dispatch.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("sendLargeMessageQueue")
    public JndiObjectFactoryBean sendLargeMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.largeMessage.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("splitAndJoinQueue")
    public JndiObjectFactoryBean splitAndJoinQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.splitAndJoin.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("pullMessageQueue")
    public JndiObjectFactoryBean pullMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.pull.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("sendPullReceiptQueue")
    public JndiObjectFactoryBean sendPullReceiptQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.pull.receipt.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("retentionMessageQueue")
    public JndiObjectFactoryBean retentionMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.retentionMessage.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("alertMessageQueue")
    public JndiObjectFactoryBean alertMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.alert.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("uiReplicationQueue")
    public JndiObjectFactoryBean uiReplicationQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.ui.replication.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("notifyBackendQueue")
    public JndiObjectFactoryBean notifyBackendQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.notification.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("unknownReceiverQueue")
    public JndiObjectFactoryBean unknownReceiverQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.notification.unknown");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean("clusterCommandTopic")
    public JndiObjectFactoryBean clusterCommandTopic() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.command");
        jndiObjectFactoryBean.setExpectedType(Topic.class);
        return jndiObjectFactoryBean;
    }

    @Bean("internalDestinationResolver")
    public JndiDestinationResolver internalDestinationResolver() {
        JndiDestinationResolver result = new JndiDestinationResolver();
        result.setCache(true);
        result.setFallbackToDynamicDestination(false);
        return result;
    }
}
