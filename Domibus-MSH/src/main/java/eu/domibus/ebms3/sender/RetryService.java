package eu.domibus.ebms3.sender;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface RetryService {

    void enqueueMessages();

    void resetWaitingForReceiptPullMessages();

    void bulkExpirePullMessages();

    void bulkDeletePullMessages();
}
