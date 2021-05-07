package eu.domibus.jms.activemq;

import eu.domibus.common.JMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusJMSActiveMQQueueConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusJMSActiveMQQueueConfiguration.class);

    @Bean(JMSConstants.SEND_MESSAGE_QUEUE)
    public ActiveMQQueue sendMessageQueue() {
        return new ActiveMQQueue("domibus.internal.dispatch.queue");
    }

    @Bean(JMSConstants.SEND_LARGE_MESSAGE_QUEUE)
    public ActiveMQQueue sendLargeMessageQueue() {
        return new ActiveMQQueue("domibus.internal.largeMessage.queue");
    }

    @Bean(JMSConstants.SPLIT_AND_JOIN_QUEUE)
    public ActiveMQQueue splitAndJoinQueue() {
        return new ActiveMQQueue("domibus.internal.splitAndJoin.queue");
    }

    @Bean(JMSConstants.PULL_MESSAGE_QUEUE)
    public ActiveMQQueue pullMessageQueue() {
        return new ActiveMQQueue("domibus.internal.pull.queue");
    }

    @Bean(JMSConstants.SEND_PULL_RECEIPT_QUEUE)
    public ActiveMQQueue sendPullReceiptQueue() {
        return new ActiveMQQueue("domibus.internal.pull.receipt.queue");
    }

    @Bean(JMSConstants.RETENTION_MESSAGE_QUEUE)
    public ActiveMQQueue retentionMessageQueue() {
        return new ActiveMQQueue("domibus.internal.retentionMessage.queue");
    }

    @Bean(JMSConstants.ALERT_MESSAGE_QUEUE)
    public ActiveMQQueue alertMessageQueue() {
        return new ActiveMQQueue("domibus.internal.alert.queue");
    }

    @Bean(JMSConstants.UI_REPLICATION_QUEUE)
    public ActiveMQQueue uiReplicationQueue() {
        return new ActiveMQQueue("domibus.internal.ui.replication.queue");
    }

    @Bean(JMSConstants.NOTIFY_BACKEND_QUEUE)
    public ActiveMQQueue notifyBackendQueue() {
        return new ActiveMQQueue("domibus.internal.notification.queue");
    }

    @Bean(JMSConstants.UNKNOWN_RECEIVER_QUEUE)
    public ActiveMQQueue unknownReceiverQueue() {
        return new ActiveMQQueue("domibus.internal.notification.unknown");
    }

    @Bean(JMSConstants.CLUSTER_COMMAND_TOPIC)
    public ActiveMQTopic clusterCommandTopic() {
        return new ActiveMQTopic("domibus.internal.command");
    }
}

