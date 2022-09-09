package eu.domibus.jms.activemq;

import eu.domibus.jms.spi.InternalJMSConstants;
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

    @Bean(InternalJMSConstants.SEND_MESSAGE_QUEUE)
    public ActiveMQQueue sendMessageQueue() {
        return new ActiveMQQueue("domibus.internal.dispatch.queue");
    }

    @Bean(InternalJMSConstants.SEND_LARGE_MESSAGE_QUEUE)
    public ActiveMQQueue sendLargeMessageQueue() {
        return new ActiveMQQueue("domibus.internal.largeMessage.queue");
    }

    @Bean(InternalJMSConstants.SPLIT_AND_JOIN_QUEUE)
    public ActiveMQQueue splitAndJoinQueue() {
        return new ActiveMQQueue("domibus.internal.splitAndJoin.queue");
    }

    @Bean(InternalJMSConstants.PULL_MESSAGE_QUEUE)
    public ActiveMQQueue pullMessageQueue() {
        return new ActiveMQQueue("domibus.internal.pull.queue");
    }

    @Bean(InternalJMSConstants.SEND_PULL_RECEIPT_QUEUE)
    public ActiveMQQueue sendPullReceiptQueue() {
        return new ActiveMQQueue("domibus.internal.pull.receipt.queue");
    }

    @Bean(InternalJMSConstants.RETENTION_MESSAGE_QUEUE)
    public ActiveMQQueue retentionMessageQueue() {
        return new ActiveMQQueue("domibus.internal.retentionMessage.queue");
    }

    @Bean(InternalJMSConstants.ALERT_MESSAGE_QUEUE)
    public ActiveMQQueue alertMessageQueue() {
        return new ActiveMQQueue("domibus.internal.alert.queue");
    }

    @Bean(InternalJMSConstants.EARCHIVE_QUEUE)
    public ActiveMQQueue eArchiveQueue() {
        return new ActiveMQQueue("domibus.internal.earchive.queue");
    }

    @Bean(InternalJMSConstants.EARCHIVE_NOTIFICATION_QUEUE)
    public ActiveMQQueue eArchiveNotificationQueue() {
        return new ActiveMQQueue("domibus.internal.earchive.notification.queue");
    }

    @Bean(InternalJMSConstants.EARCHIVE_NOTIFICATION_DLQ)
    public ActiveMQQueue eArchiveNotificationDLQ() {
        return new ActiveMQQueue("domibus.internal.earchive.notification.dlq");
    }

    @Bean(InternalJMSConstants.NOTIFY_BACKEND_QUEUE)
    public ActiveMQQueue notifyBackendQueue() {
        return new ActiveMQQueue("domibus.internal.notification.queue");
    }

    @Bean(InternalJMSConstants.UNKNOWN_RECEIVER_QUEUE)
    public ActiveMQQueue unknownReceiverQueue() {
        return new ActiveMQQueue("domibus.internal.notification.unknown");
    }

    @Bean(InternalJMSConstants.CLUSTER_COMMAND_TOPIC)
    public ActiveMQTopic clusterCommandTopic() {
        return new ActiveMQTopic("domibus.internal.command");
    }
}

