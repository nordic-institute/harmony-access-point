package eu.domibus.plugin.webService.backend;

/**
 * @author Francois Gautier
 * @since 5.0
 */
public enum WSBackendMessageStatus {

    /**
     * The message is in the send queue.
     */
    SEND_ENQUEUED,

    /**
     * The final send attempt of the message has failed and there will be no more retries
     */
    SEND_FAILURE,

    /**
     * The last attempt to send the message has failed. There will  be a retry once the waiting interval of the
     * corresponding PMode has passed.
     */
    WAITING_FOR_RETRY,

    /**
     * The message has been sent successfully.
     */
    SENT
}
