package eu.domibus.test.jms

class JMSConstants {

    public static final def QUEUE_ACTION_PAUSE = 'pause'
    public static final def QUEUE_ACTION_RESUME = 'resume'
    public static final def QUEUE_ACTION_PURGE = 'purge'

    public static final def QUEUE_DATA_ENQUEUE_COUNT = 'enqueue'
    public static final def QUEUE_DATA_DISPATCH_COUNT = 'dispatch'
    public static final def QUEUE_DATA_DEQUEUE_COUNT = 'dequeue'
    public static final def QUEUE_DATA_QUEUE_COUNT = 'queue'

    public static final def ACTIVEMQ_DISPATCH_QUEUE = "domibus.internal.dispatch.queue"
    public static final def ACTIVEMQ_LARGEMESSAGE_QUEUE = "domibus.internal.largeMessage.queue"
    public static final def ACTIVEMQ_SPLITANDJOIN_QUEUE = "domibus.internal.splitAndJoin.queue"
    public static final def ACTIVEMQ_PULL_QUEUE = "domibus.internal.pull.queue"
    public static final def ACTIVEMQ_PULL_RECEIPT_QUEUE = "domibus.internal.pull.receipt.queue"
    public static final def ACTIVEMQ_RETENTIONMESSAGE_QUEUE = "domibus.internal.retentionMessage.queue"
    public static final def ACTIVEMQ_ALERT_QUEUE = "domibus.internal.alert.queue"
    public static final def ACTIVEMQ_EARCHIVE_QUEUE = "domibus.internal.earchive.queue"
    public static final def ACTIVEMQ_EARCHIVE_NOTIFICATION_QUEUE = "domibus.internal.earchive.notification.queue"
    public static final def ACTIVEMQ_EARCHIVE_NOTIFICATION_DLQ ="domibus.internal.earchive.notification.dlq"
    public static final def ACTIVEMQ_REPLICATION_QUEUE = "domibus.internal.ui.replication.queue"
    public static final def ACTIVEMQ_NOTIFICATION_QUEUE = "domibus.internal.notification.queue"
    public static final def ACTIVEMQ_NOTIFICATION_UNKNOWN ="domibus.internal.notification.unknown"


}
