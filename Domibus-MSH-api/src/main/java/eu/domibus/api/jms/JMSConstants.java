package eu.domibus.api.jms;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface JMSConstants {

    /**
     * Queue names in Domibus
     */
    String SEND_MESSAGE_QUEUE = "sendMessageQueue";
    String SEND_LARGE_MESSAGE_QUEUE = "sendLargeMessageQueue";
    String SPLIT_AND_JOIN_QUEUE = "splitAndJoinQueue";
    String PULL_MESSAGE_QUEUE = "pullMessageQueue";
    String SEND_PULL_RECEIPT_QUEUE = "sendPullReceiptQueue";
    String RETENTION_MESSAGE_QUEUE = "retentionMessageQueue";
    String ALERT_MESSAGE_QUEUE = "alertMessageQueue";
    String UI_REPLICATION_QUEUE = "uiReplicationQueue";
    String NOTIFY_BACKEND_QUEUE = "notifyBackendQueue";
    String UNKNOWN_RECEIVER_QUEUE = "unknownReceiverQueue";
    String CLUSTER_COMMAND_TOPIC = "clusterCommandTopic";
}
