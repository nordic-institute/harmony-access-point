package eu.domibus.core.ebms3.sender.retry;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface RetryService {

    void enqueueMessage(long messageEntityId);

    List<Long> getMessagesNotAlreadyScheduled();

    void resetWaitingForReceiptPullMessages();

    void bulkExpirePullMessages();

    void bulkDeletePullMessages();
}
