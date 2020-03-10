package eu.domibus.jms.weblogic;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean("domibusJMS-XAConnectionFactory")
    public ConnectionFactory connectionFactory() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/ConnectionFactory");
        return (ConnectionFactory) jndiObjectFactoryBean.getObject();
    }

    @Bean("sendMessageQueue")
    public Queue sendMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.dispatch.queue");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("sendLargeMessageQueue")
    public Queue sendLargeMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.largeMessage.queue");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("splitAndJoinQueue")
    public Queue splitAndJoinQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.splitAndJoin.queue");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("pullMessageQueue")
    public Queue pullMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.pull.queue");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("sendPullReceiptQueue")
    public Queue sendPullReceiptQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.pull.receipt.queue");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("retentionMessageQueue")
    public Queue retentionMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.retentionMessage.queue");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("alertMessageQueue")
    public Queue alertMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.alert.queue");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("uiReplicationQueue")
    public Queue uiReplicationQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.ui.replication.queue");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("notifyBackendQueue")
    public Queue notifyBackendQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.notification.queue");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("unknownReceiverQueue")
    public Queue unknownReceiverQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.notification.unknown");
        return (Queue) jndiObjectFactoryBean.getObject();
    }

    @Bean("clusterCommandTopic")
    public Topic clusterCommandTopic() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jms/domibus.internal.command");
        return (Topic) jndiObjectFactoryBean.getObject();
    }

    @Bean("internalDestinationResolver")
    public JndiDestinationResolver internalDestinationResolver() {
        JndiDestinationResolver result = new JndiDestinationResolver();
        result.setCache(true);
        result.setFallbackToDynamicDestination(false);
        return result;
    }
}
