package eu.domibus.jms.wildfly;

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

import static eu.domibus.common.JMSConstants.*;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusJMSWildflyQueueConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusJMSWildflyQueueConfiguration.class);

    @Bean(DOMIBUS_JMS_CACHING_CONNECTION_FACTORY)
    public ConnectionFactory cachingConnectionFactory(@Qualifier(DOMIBUS_JMS_CONNECTION_FACTORY) ConnectionFactory wildflyConnectionFactory,
                                               DomibusPropertyProvider domibusPropertyProvider) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        Integer sessionCacheSize = domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_CONNECTION_FACTORY_SESSION_CACHE_SIZE);
        LOGGER.debug("Using session cache size for connection factory [{}]", sessionCacheSize);
        cachingConnectionFactory.setSessionCacheSize(sessionCacheSize);
        cachingConnectionFactory.setTargetConnectionFactory(wildflyConnectionFactory);
        cachingConnectionFactory.setCacheConsumers(false);

        return cachingConnectionFactory;
    }

    @Bean(DOMIBUS_JMS_CONNECTION_FACTORY)
    public JndiObjectFactoryBean connectionFactory() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/ConnectionFactory");
        jndiObjectFactoryBean.setLookupOnStartup(false);
        jndiObjectFactoryBean.setExpectedType(ConnectionFactory.class);
        return jndiObjectFactoryBean;
    }

    @Bean(SEND_MESSAGE_QUEUE)
    public JndiObjectFactoryBean sendMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.dispatch.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(SEND_LARGE_MESSAGE_QUEUE)
    public JndiObjectFactoryBean sendLargeMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.largeMessage.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(SPLIT_AND_JOIN_QUEUE)
    public JndiObjectFactoryBean splitAndJoinQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.splitAndJoin.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(PULL_MESSAGE_QUEUE)
    public JndiObjectFactoryBean pullMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.pull.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(SEND_PULL_RECEIPT_QUEUE)
    public JndiObjectFactoryBean sendPullReceiptQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.pull.receipt.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(RETENTION_MESSAGE_QUEUE)
    public JndiObjectFactoryBean retentionMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.retentionMessage.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(ALERT_MESSAGE_QUEUE)
    public JndiObjectFactoryBean alertMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.alert.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(UI_REPLICATION_QUEUE)
    public JndiObjectFactoryBean uiReplicationQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.ui.replication.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(NOTIFY_BACKEND_QUEUE)
    public JndiObjectFactoryBean notifyBackendQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.notification.queue");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(UNKNOWN_RECEIVER_QUEUE)
    public JndiObjectFactoryBean unknownReceiverQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.notification.unknown");
        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

    @Bean(CLUSTER_COMMAND_TOPIC)
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
