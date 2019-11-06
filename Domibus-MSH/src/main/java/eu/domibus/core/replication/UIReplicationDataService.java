package eu.domibus.core.replication;

/**
 * Replication Data service interface - all operations of writing data into {@code TB_MESSAGE_UI} table
 *
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIReplicationDataService {

    /**
     * replicates data on receiver side when a new user message is received
     *
     * @param messageId
     * @param jmsTimestamp
     */
    void userMessageReceived(final String messageId, long jmsTimestamp);

    /**
     * replicates data on sender side when a new user message is submitted
     *
     * @param messageId
     * @param jmsTimestamp
     */
    void userMessageSubmitted(final String messageId, final long jmsTimestamp);

    /**
     * updates/sync data on receiver/sender side when a change in message change appears
     *
     * @param messageId
     * @param jmsTimestamp
     */
    void messageChange(final String messageId, final long jmsTimestamp);

    /**
     * replicates data on sender side when a new signal message is submitted
     *
     * @param messageId
     * @param jmsTimestamp
     */
    void signalMessageSubmitted(final String messageId, final long jmsTimestamp);

    /**
     * replicates data on receiver side when a new signal message is received
     *
     * @param messageId
     * @param jmsTimestamp
     */
    void signalMessageReceived(final String messageId, final long jmsTimestamp);

}
