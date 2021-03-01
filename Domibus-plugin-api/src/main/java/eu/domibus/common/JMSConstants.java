package eu.domibus.common;

/**
 * @author Cosmin Baciu
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
public interface JMSConstants {

    /**
     * Bean name for the JMS connection factory that is to be used when working with Spring's
     * {@link org.springframework.jms.core.JmsOperations JmsTemplate}.
     *
     * <p><b>Note: for performace reasons, the actual bean instance must implement caching of message producers (e.g.
     * {@link org.springframework.jms.connection.CachingConnectionFactory})<b/></p>
     */
    String DOMIBUS_JMS_CACHING_CONNECTION_FACTORY = "domibusJMS-CachingNonXAConnectionFactory";

    /**
     * Bean name for the JMS connection factory that is to be used when working with Spring
     * {@link org.springframework.jms.listener.DefaultMessageListenerContainer message listener containers}.
     *
     * <p><b>Note: the actual bean instance must take advantage of the caching provided by the message listeners
     * themselves and must not be used in conjunction with the
     * {@link org.springframework.jms.connection.CachingConnectionFactory} if dynamic scaling is required.<b/></p>
     */
    String DOMIBUS_JMS_CONNECTION_FACTORY = "domibusJMS-NonXAConnectionFactory";

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
