package eu.domibus.jms.activemq;

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

    @Bean("sendMessageQueue")
    public ActiveMQQueue sendMessageQueue() {
        return new ActiveMQQueue("domibus.internal.dispatch.queue");
    }

    @Bean("sendLargeMessageQueue")
    public ActiveMQQueue sendLargeMessageQueue() {
        return new ActiveMQQueue("domibus.internal.largeMessage.queue");
    }

    @Bean("splitAndJoinQueue")
    public ActiveMQQueue splitAndJoinQueue() {
        return new ActiveMQQueue("domibus.internal.splitAndJoin.queue");
    }

    @Bean("pullMessageQueue")
    public ActiveMQQueue pullMessageQueue() {
        return new ActiveMQQueue("domibus.internal.pull.queue");
    }

    @Bean("sendPullReceiptQueue")
    public ActiveMQQueue sendPullReceiptQueue() {
        return new ActiveMQQueue("domibus.internal.pull.receipt.queue");
    }

    @Bean("retentionMessageQueue")
    public ActiveMQQueue retentionMessageQueue() {
        return new ActiveMQQueue("domibus.internal.retentionMessage.queue");
    }

    @Bean("alertMessageQueue")
    public ActiveMQQueue alertMessageQueue() {
        return new ActiveMQQueue("domibus.internal.alert.queue");
    }

    @Bean("clearPayloadQueue")
    public ActiveMQQueue clearPayloadQueue() {
        return new ActiveMQQueue("domibus.internal.clearPayload.queue");
    }

    @Bean("uiReplicationQueue")
    public ActiveMQQueue uiReplicationQueue() {
        return new ActiveMQQueue("domibus.internal.ui.replication.queue");
    }

    @Bean("notifyBackendQueue")
    public ActiveMQQueue notifyBackendQueue() {
        return new ActiveMQQueue("domibus.internal.notification.queue");
    }

    @Bean("unknownReceiverQueue")
    public ActiveMQQueue unknownReceiverQueue() {
        return new ActiveMQQueue("domibus.internal.notification.unknown");
    }

    @Bean("clusterCommandTopic")
    public ActiveMQTopic clusterCommandTopic() {
        return new ActiveMQTopic("domibus.internal.command");
    }
}

