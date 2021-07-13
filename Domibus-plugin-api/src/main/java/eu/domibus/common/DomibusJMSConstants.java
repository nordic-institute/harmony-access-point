package eu.domibus.common;

/**
 * @since 5.0
 * @author Catalin Enache
 */
public final class DomibusJMSConstants {

    private DomibusJMSConstants() {}

    /**
     * Bean name for the JMS connection factory that is to be used when working with Spring's
     * {@link org.springframework.jms.core.JmsOperations JmsTemplate}.
     *
     * <p><b>Note: for performance reasons, the actual bean instance must implement caching of message producers (e.g.
     * {@link org.springframework.jms.connection.CachingConnectionFactory})<b/></p>
     */
    public static final String DOMIBUS_JMS_CACHING_CONNECTION_FACTORY = "domibusJMS-CachingConnectionFactory";

    /**
     * Bean name for the JMS connection factory that is to be used when working with Spring
     * {@link org.springframework.jms.listener.DefaultMessageListenerContainer message listener containers}.
     *
     * <p><b>Note: the actual bean instance must take advantage of the caching provided by the message listeners
     * themselves and must not be used in conjunction with the
     * {@link org.springframework.jms.connection.CachingConnectionFactory} if dynamic scaling is required.<b/></p>
     */
    public static final String DOMIBUS_JMS_CONNECTION_FACTORY = "domibusJMS-ConnectionFactory";

    /**
     * Queue names in Domibus
     */
    public static final String SEND_MESSAGE_QUEUE = "sendMessageQueue";
    public static final String SEND_LARGE_MESSAGE_QUEUE = "sendLargeMessageQueue";
    public static final String SPLIT_AND_JOIN_QUEUE = "splitAndJoinQueue";
    public static final String PULL_MESSAGE_QUEUE = "pullMessageQueue";
    public static final String SEND_PULL_RECEIPT_QUEUE = "sendPullReceiptQueue";
    public static final String RETENTION_MESSAGE_QUEUE = "retentionMessageQueue";
    public static final String ALERT_MESSAGE_QUEUE = "alertMessageQueue";
    public static final String UI_REPLICATION_QUEUE = "uiReplicationQueue";
    public static final String NOTIFY_BACKEND_QUEUE = "notifyBackendQueue";
    public static final String UNKNOWN_RECEIVER_QUEUE = "unknownReceiverQueue";
    public static final String CLUSTER_COMMAND_TOPIC = "clusterCommandTopic";
}
