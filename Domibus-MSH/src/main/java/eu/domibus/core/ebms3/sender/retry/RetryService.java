package eu.domibus.core.ebms3.sender.retry;

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
